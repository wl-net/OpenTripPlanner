import java.util.Iterator;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.analyst.batch.IndividualList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Wraps a GridCoverage2D so you can iterate over it as a series of Individuals. */
public class GridCoverageIndividualList implements IndividualList {

    private static Logger LOG = LoggerFactory.getLogger(GridCoverageIndividualList.class); 

    int band = 0;
    GridCoverage2D coverage;
    MathTransform worldToWGS84;
    GridGeometry2D gridGeometry; // contains grid to world mapping and math transform
    GridEnvelope2D gridEnvelope; // contains the width and height in pixels
    
    public GridCoverageIndividualList(GridCoverage2D coverage) {
        this.coverage = coverage;
        CoordinateReferenceSystem coverageCRS = coverage.getCoordinateReferenceSystem2D();
        gridGeometry = coverage.getGridGeometry();
        gridEnvelope = gridGeometry.getGridRange2D();
        try {
            worldToWGS84 = CRS.findMathTransform(coverageCRS, DefaultGeographicCRS.WGS84);
        } catch (FactoryException e) {
            LOG.debug("could not find MathTransform.");
            e.printStackTrace();
        }
    }
    
    @Override
    public int size() {
        return gridEnvelope.width * gridEnvelope.height;
    }

    @Override
    public Iterator<Individual> iterator() {
        return new Iterator<Individual> () {

            // grid coordinate object to be reused for reading each cell in the raster
            GridCoordinates2D coord = new GridCoordinates2D();
            // evaluating a raster returns an array of results, one per band or sample dimension
            float[] val = new float[coverage.getNumSampleDimensions()];
            
            @Override
            public boolean hasNext() {
                return coord.y < gridEnvelope.height;
            }
            
            @Override
            public Individual next() {
                // We are performing 2 transforms here. It would likely be more efficient to 
                // compose the grid-to-CRS and CRS-to-WGS84 transforms into grid-to-WGS84.
                // However the ConcatenatedTransform appears to just run two transforms one
                // after the other.
                Individual result = null;
                try {                    
                    // 1. Find coordinates for current raster cell in raster CRS
                    DirectPosition pos = gridGeometry.gridToWorld(coord);
                    // 2. Convert Raster CRS to WGS84 for use in OTP
                    worldToWGS84.transform(pos, pos);
                    double lon = pos.getOrdinate(0);
                    double lat = pos.getOrdinate(1);
                    // evaluate the coverage using grid (pixel) rather than world coordinates 
                    coverage.evaluate(coord, val); 
                    // return an Individual for this grid cell
                    String label = coord.y + "_" + coord.x;
                    result = new Individual(label, lon, lat, val[band]);
                } catch (Exception e) {
                    LOG.error("error creating individual for gridcoverage");
                    e.printStackTrace();
                }
                coord.x += 1;
                if (coord.x > gridEnvelope.width) {
                    coord.x = 0;
                    coord.y += 1;
                }
                return result;
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();   
            }
        };
    }    
    
}
