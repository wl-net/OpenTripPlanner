package org.opentripplanner.analyst.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opentripplanner.analyst.batch.AbstractPopulation;
import org.opentripplanner.analyst.batch.PackedSampleList;
import org.opentripplanner.analyst.batch.ResultSet;
import org.opentripplanner.analyst.parameter.Style;
import org.opentripplanner.analyst.request.ColorModels;
import org.opentripplanner.analyst.request.RenderRequest;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass of slippytile -- combination of Tile and TileRequest
 * should actually be called 'raster' and combined with RasterPopulation 
 */
public class Tile extends AbstractPopulation { 

    /* STATIC */
    private static final Logger LOG = LoggerFactory.getLogger(Tile.class);        
    
    /* INSTANCE */
    public final Envelope2D bbox; // includes CRS
    public final int width; 
    public final int height; 
    final GridGeometry2D gg;      // maps grid coordinates to CRS coordinates

    public Tile(Envelope2D bbox, Integer width, Integer height) {
        this.bbox = bbox;
        this.width = width;
        this.height = height;
        GridEnvelope2D gridEnv = new GridEnvelope2D(0, 0, width, height);
        this.gg = new GridGeometry2D(gridEnv, (org.opengis.geometry.Envelope)(this.bbox));
        LOG.debug("preparing tile for {}", gg.getEnvelope2D());
    }
    
    public void resampleDynamic(SampleSource ss) {
        // TODO: check that gg intersects graph area 
        this.sampleList = new RasterSampleList(this, ss);
    }

    public void materializeSamples() {
//        if (sampleList == null)
//            resampleDynamic(); // push ssource injection down into generator 
        if (sampleList.isDynamic())
            sampleList = new PackedSampleList(sampleList);
    }    
    
    public int totalSize() {
        return width * height;
    }
    
    @Override
    public int hashCode() {
        return bbox.hashCode() * 42677 + width + height * 1307;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Tile) {
            Tile that = (Tile) other;
            return this.bbox.equals(that.bbox) &&
                   this.width  == that.width   &&
                   this.height == that.height;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("<tile, bbox=%s width=%d height=%d>", bbox, width, height);
    }
    
    ////////////////////
    
    /**
     * Methods to write tiles/populations out to images. Assuming it is not
     * needed as a library, this could eventually be merged into RasterPopulation.
     */

    protected BufferedImage getEmptyImage(Style style) {
        IndexColorModel colorModel = ColorModels.forStyle(style);
        if (colorModel == null)
            return new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        else
            return new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
    }
    
    public BufferedImage generateImage(ShortestPathTree spt, RenderRequest renderRequest) {
        long t0 = System.currentTimeMillis();
        BufferedImage image = getEmptyImage(renderRequest.style);
        byte[] imagePixelData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        int i = 0;
        final byte TRANSPARENT = (byte) 255;
        SampleOperator so = new ElapsedTimeSampleOperator();
        for (float f : so.evaluate(spt, this).results) {
            byte pixel;
            if ( ! Float.isInfinite(f)) {
                float t = f / 60;
                if (t >= 255)
                    t = 255;
                pixel = (byte) t;
            } else {
                pixel = TRANSPARENT;
            }
            //LOG.debug("f = {}, pixel = {}", f, pixel);
            imagePixelData[i] = pixel;
            i++;
        }
        long t1 = System.currentTimeMillis();
        LOG.debug("filled in tile image from SPT in {}msec", t1 - t0);
        return image;
    }

    public BufferedImage linearCombination(
            double k1, ShortestPathTree spt1, 
            double k2, ShortestPathTree spt2, 
            double intercept, RenderRequest renderRequest) {
        long t0 = System.currentTimeMillis();
        BufferedImage image = getEmptyImage(renderRequest.style);
        byte[] imagePixelData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        int i = 0;
        final byte TRANSPARENT = (byte) 255;
        SampleOperator so = new ElapsedTimeSampleOperator();
        for (float f : so.evaluate(spt1, this).results) {
            byte pixel = 0;
//            if ( ! Float.isInfinite(f)) {
//                double t = (k1 * s.eval(spt1) + k2 * s.eval(spt2)) / 60 + intercept; 
//                if (t < 0 || t > 255)
//                    t = TRANSPARENT;
//                pixel = (byte) t;
//            } else {
//                pixel = TRANSPARENT;
//            }
            imagePixelData[i] = pixel;
            i++;
        }
        long t1 = System.currentTimeMillis();
        LOG.debug("filled in tile image from SPT in {}msec", t1 - t0);
        return image;
    }

    public GridCoverage2D getGridCoverage2D(BufferedImage image) {
        GridCoverage2D gridCoverage = new GridCoverageFactory()
            .create("isochrone", image, gg.getEnvelope2D());
        return gridCoverage;
    }

    public static BufferedImage getLegend(Style style, int width, int height) {
        final int NBANDS = 150;
        final int LABEL_SPACING = 30; 
        IndexColorModel model = ColorModels.forStyle(style);
        if (width < 140 || width > 2000)
            width = 140;
        if (height < 25 || height > 2000)
            height = 25;
        if (model == null)
            return null;
        WritableRaster raster = model.createCompatibleWritableRaster(width, height);
        byte[] pixels = ((DataBufferByte) raster.getDataBuffer()).getData();
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++)
                pixels[row * width + col] = (byte) (col * NBANDS / width);
        BufferedImage legend = model.convertToIntDiscrete(raster, false);
        Graphics2D gr = legend.createGraphics();
        gr.setColor(new Color(0));
        gr.drawString("travel time (minutes)", 0, 10);
        float scale = width / (float) NBANDS;
        for (int i = 0; i < NBANDS; i += LABEL_SPACING)
            gr.drawString(Integer.toString(i), i * scale, height);
        return legend;
    }

    /////////////////////
    
    @Override
    public void writeAppropriateFormat(String outFileName, ResultSet results) {
        this.writeGeotiff(outFileName, results);
    }

    public void writeGeotiff(String fileName, ResultSet results) {
        LOG.info("writing geotiff.");
        float[][] imagePixelData = new float[rows][cols]; 
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = row * cols + col;
                float pixel = (float) results.results[index];
                imagePixelData[row][col] = pixel;
            }
        }
        GridCoverage2D coverage = new GridCoverageFactory().create("OTPAnalyst", imagePixelData, refEnvelope);
        try {
            GeoTiffWriteParams wp = new GeoTiffWriteParams();
            wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
            wp.setCompressionType("LZW");
            ParameterValueGroup params = new GeoTiffFormat().getWriteParameters();
            params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
            GeoTiffWriter writer = new GeoTiffWriter(new File(fileName));
            writer.write(coverage, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
        } catch (Exception e) {
            LOG.error("exception while writing geotiff.");
            e.printStackTrace();
        }
        LOG.info("done writing geotiff.");
    }

    @Override
    public void setup() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createIndividuals() {
        // TODO Auto-generated method stub
        
    }    
            
}
