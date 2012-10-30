package org.opentripplanner.analyst.batch;

import java.io.File;

import lombok.Setter;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoverageRasterPopulation {

    private static final Logger LOG = LoggerFactory.getLogger(RasterPopulation.class);

    /* configuration fields */
    @Setter int band = 0; // raster band to read
    
    /* derived fields */
    protected CoordinateReferenceSystem coverageCRS; // from input raster or config string
    protected GridEnvelope2D gridEnvelope; // the envelope for the pixels
    protected ReferencedEnvelope refEnvelope; // the envelope in the CRS
    protected GridGeometry2D gridGeometry; // relationship between the grid envelope and the CRS envelope
    
    @Override
    public void createIndividuals() {
        LOG.info("Loading population from raster file {}", sourceFilename);
        try {
            File rasterFile = new File(sourceFilename);
            // determine file format and CRS, then load raster
            AbstractGridFormat format = GridFormatFinder.findFormat(rasterFile);
            AbstractGridCoverage2DReader reader = format.getReader(rasterFile);
            GridCoverage2D coverage = reader.read(null);
            this.coverageCRS = coverage.getCoordinateReferenceSystem();
            GridGeometry2D gridGeometry = coverage.getGridGeometry();
            GridEnvelope2D gridEnvelope = gridGeometry.getGridRange2D();
            gridGeometry.getGridToCRS();
            // because we may want to produce an empty raster rather than loading one, alongside the coverage we 
            // store the row/col dimensions and the referenced envelope in the original coordinate reference system.
            this.cols  = gridEnvelope.width;
            this.rows = gridEnvelope.height;
            this.createIndividuals0();
        } catch (Exception ex) {
            throw new IllegalStateException("Error loading population from raster file: ", ex);
        }
        LOG.info("Done loading raster from file.");
    }
        
}
