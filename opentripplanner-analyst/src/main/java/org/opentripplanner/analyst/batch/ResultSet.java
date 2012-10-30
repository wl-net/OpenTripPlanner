package org.opentripplanner.analyst.batch;

import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the results of applying a SampleOperator to a SampleSet and an SPT.
 * Allows populations to be reused in several places.
 * 
 * An IEEE 754 float has 23 bits of significand. It can therefore exactly represent all 
 * integers up to 2**24 or 16777216. If the float is storing times in seconds (the OTP time 
 * quantum) this is equivalent to 4660 hours. This should be sufficient for most applications 
 * while still allowing some headroom for aggregation, scaling etc. without much loss of 
 * precision. 
 */
public class ResultSet {

    private static final Logger LOG = LoggerFactory.getLogger(ResultSet.class);

    public Population population;
    public RoutingRequest routingRequest; // do not store the SPT to allow garbage collection
    public float[] results;
    
    public ResultSet(Population population, ShortestPathTree spt, float[] results) {
        this.population = population;
        this.routingRequest = spt.getOptions();
        this.results = results;
    }
    
    public void writeAppropriateFormat(String outFileName) {
        population.writeAppropriateFormat(outFileName, this);
    }
    
}
