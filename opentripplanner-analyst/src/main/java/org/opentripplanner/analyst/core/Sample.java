package org.opentripplanner.analyst.core;

import org.opentripplanner.routing.graph.Vertex;

/**
 * A Sample holds some cached data about the relationship between a geographic point and a graph.
 * It is the result of some relatively time-consuming distance operations and can be cached to speed
 * up subsequent re-evaluations of the travel time surface with respect to a different SPT.
 * It contains references to two vertices and the distances to those vertices because 
 * we assume that each point may be accessible from two different vertices, which are usually
 * the two ends of the nearest street.
 */
public class Sample {
    public final float d0, d1;
    public final Vertex v0, v1;
    
    public Sample (Vertex v0, float d0, Vertex v1, float d1) {
        this.v0 = v0;
        this.d0 = d0;
        this.v1 = v1;
        this.d1 = d1;
    }
    
    public String toString() {
        return String.format("Sample: %s at %d m or %s at %d m\n", v0, d0, v1, d1);
    }
    
}

