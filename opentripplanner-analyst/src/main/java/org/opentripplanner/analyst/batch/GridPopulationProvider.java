package org.opentripplanner.analyst.batch;

import lombok.Setter;

import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opentripplanner.analyst.core.RasterPopulation;
import org.opentripplanner.routing.services.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GridPopulationProvider implements PopulationProvider {

    private static Logger LOG = LoggerFactory.getLogger(GridPopulationProvider.class); 

    @Setter String name = "synthetic grid coverage";
    @Setter String crsCode = "EPSG:4326";
    @Setter boolean boundsFromGraph = false; // use graph envelope, overriding any specified bounds
    @Setter double left, top, right, bottom;
    @Setter int rows, cols;
    @Autowired GraphService graphService;
    
    @Override
    public Population getPopulation() {
        if (boundsFromGraph) {
            // graphService.getGraph()...
        }
        Population pop = null;
        try {
            CoordinateReferenceSystem crs = CRS.decode(crsCode, true);
            GridEnvelope2D gridEnvelope = new GridEnvelope2D(0, 0, cols, rows);
            ReferencedEnvelope refEnvelope = new ReferencedEnvelope(left, right, bottom, top, crs);
            GridGeometry2D gridGeometry = new GridGeometry2D(gridEnvelope, refEnvelope);
            pop = new RasterPopulation(gridGeometry);
        } catch (Exception e) {
            LOG.error("error decoding coordinate reference system code {}: {}", crsCode, e.getMessage());
        }
        return pop;
    }

}
