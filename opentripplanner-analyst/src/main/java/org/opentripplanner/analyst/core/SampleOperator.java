package org.opentripplanner.analyst.core;

import org.opentripplanner.analyst.batch.Population;
import org.opentripplanner.analyst.batch.ResultSet;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.spt.ShortestPathTree;

/**
 * Each implementation or instance of a SampleOperator provides custom logic for converting the 
 * States pulled from a ShortestPathTree into numbers. This allows pluggable behavior, so images
 * or other Analyst results can show travel time with or without initial wait time, number of 
 * transfers, weight, or any other path characteristic. 
 * 
 * You can imagine this as an infix operator:
 * ShortestPathTree [SampleOperator] SampleSet => ResultSet 
 * 
 * i.e. a SampleOperator produces a ResultSet by combining a ShortestPathTree with a SampleSet.
 * 
 * The method that actually iterates over the Samples is implemented in Population itself.
 * This allows for optimizations to avoid re-fetching vertices, as well as population
 * compression.
 * 
 * @author abyrd
 */
public abstract class SampleOperator {

    /** If true, report smaller values in results. If false, report greater values. */
    protected boolean minimize = true;
    
    /** Implement this method to supply logic for converting States to result numbers */
    public abstract float evaluate(State state, double distance);

    /**
     * Find the States in the ShortestPathTree for the given Sample, evaluate each Sample, and
     * return the better of the two results if there is more than one.
     * 
     * Ideally we would want to take the true weight of the path into consideration when choosing
     * which result to return. This would involve adding in the walk weight and making sure
     * walk limits are not exceeded. 
     * 
     * We also need a simple way to show travel time for a pure earliest arrival search.
     */
    private float evaluate(ShortestPathTree spt, Sample sample) {
        float bestResult = minimize ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        if (sample != null) {
            State s0 = spt.getState(sample.v0);
            State s1 = spt.getState(sample.v1);
            if (s0 != null)
                bestResult = evaluate(s0, sample.d0);
            if (s1 != null) {
                float r = evaluate(s1, sample.d1);
                if (minimize) {
                    if (r < bestResult)
                        bestResult = r;
                } else {
                    if (r > bestResult)
                        bestResult = r;
                }
            }
        }
        return bestResult;
    }

    public ResultSet evaluate(ShortestPathTree spt, Population population) {         
        float[] results = new float[population.totalSize()];
        int i = 0;
        // iterate over samples that have not been filtered out
        for (Sample sample : population.getSampleList()) {             
            results[i] = evaluate(spt, sample);
            i += 1;
        }
        return new ResultSet(population, spt, results);
    }
    
    // TODO implement these obsolete methods in subclasses
    
//    public byte evalBoardings(ShortestPathTree spt) {
//        State s0 = spt.getState(v0);
//        State s1 = spt.getState(v1);
//        int m0 = 255;
//        int m1 = 255;
//        if (s0 != null)
//            m0 = (s0.getNumBoardings()); 
//        if (s1 != null)
//            m1 = (s1.getNumBoardings()); 
//        return (byte) ((m0 < m1) ? m0 : m1); 
//    }
//    
//    public byte evalByte(ShortestPathTree spt) {
//        long t = eval(spt) / 60;
//        if (t >= 255)
//            t = 255;
//        return (byte) t;
//    }
//    
//    public long eval(ShortestPathTree spt) {
//        State s0 = spt.getState(v0);
//        State s1 = spt.getState(v1);
//        long m0 = Long.MAX_VALUE;
//        long m1 = Long.MAX_VALUE;
//        if (s0 != null)
//            m0 = (s0.getActiveTime() + t0); 
//        if (s1 != null)
//            m1 = (s1.getActiveTime() + t1); 
//        return (m0 < m1) ? m0 : m1; 
//    }

}
