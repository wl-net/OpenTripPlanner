package org.opentripplanner.analyst.batch;

import java.io.File;

import lombok.Setter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opentripplanner.analyst.core.RasterPopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RasterFilePopulationProvider implements PopulationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RasterPopulation.class);

    /* configuration fields */
    @Setter int band = 0; // raster band to read    
    @Setter String sourceFilename;
    
    /* derived fields */
    protected CoordinateReferenceSystem coverageCRS; // from input raster or config string
    protected GridEnvelope2D gridEnvelope; // the envelope for the pixels
    protected ReferencedEnvelope refEnvelope; // the envelope in the CRS
    protected GridGeometry2D gridGeometry; // relationship between the grid envelope and the CRS envelope
    
    @Override
    public Population getPopulation() {
        Population pop = null;
        LOG.info("Loading population from raster file {}", sourceFilename);
        try {
            File rasterFile = new File(sourceFilename);
            // determine file format and CRS, then load raster
            AbstractGridFormat format = GridFormatFinder.findFormat(rasterFile);
            AbstractGridCoverage2DReader reader = format.getReader(rasterFile);
            GridCoverage2D coverage = reader.read(null);
            pop = new RasterPopulation(coverage);
            LOG.info("Done loading raster from file.");
        } catch (Exception ex) {
            LOG.error("Error loading population from raster file: {}", ex);
        }
        return pop;
    }
        
}
