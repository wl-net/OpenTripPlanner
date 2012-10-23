package org.opentripplanner.analyst.batch;

import org.opentripplanner.analyst.core.SampleOperator;
import org.opentripplanner.routing.spt.ShortestPathTree;

/**
 * A SampleList is the part of a Population that the relationship between data points and graph
 * vertices. SampleLists may use packing and compression techniques, so the actual sample 
 * representation should be totally encapsulated. The samples are used indirectly, as a group,
 * by applying a SampleOperator.
 * 
 * @author abyrd
 *
 */
public abstract class SampleList {

    /** 
     * SampleOperators are applied to the entire list of Samples at once because they may be
     * packed or otherwise compressed.
     * 
     * An IEEE 754 float has 23 bits of significand. It can therefore exactly represent all 
     * integers up to 2**24 or 16777216. If the float is storing times in seconds (the OTP time 
     * quantum) this is equivalent to 4660 hours. This should be sufficient for most applications 
     * while still allowing some headroom for aggregation, scaling etc. without much loss of 
     * precision. 
     */
    public abstract float[] evaluate(SampleOperator sop, ShortestPathTree spt);

}
