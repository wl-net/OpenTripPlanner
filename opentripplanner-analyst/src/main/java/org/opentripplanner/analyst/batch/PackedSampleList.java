package org.opentripplanner.analyst.batch;

import java.util.List;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.analyst.core.SampleOperator;
import org.opentripplanner.analyst.request.SampleFactory;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.ShortestPathTree;

import com.vividsolutions.jts.geom.Coordinate;

public class PackedSampleList extends SampleList {

    private int length;
    
    private Vertex[] vertices;
    
    private float[] distances;

    public PackedSampleList(List<Coordinate> lc, SampleFactory sf) {
        length = lc.size();
        int i = 0;
        for (Coordinate c : lc) {
            Sample s = sf.getSample(c.x, c.y);
            vertices[i] = s.v0;
            distances[i] = s.t0;
            i += 1;
            vertices[i] = s.v1;
            distances[i] = s.t1;
            i += 1;
        }
    }
    
    @Override
    public float[] evaluate(SampleOperator sop, ShortestPathTree spt) {
        int i = 0;
        int j = 0;
        float[] results = new float[length];
        while (i < vertices.length) {
            State s0 = spt.getState(vertices[i]);
            float d0 = distances[i];
            float r0 = sop.evaluate(s0, d0);
            ++i;
            State s1 = spt.getState(vertices[i]);
            float d1 = distances[i];
            float r1 = sop.evaluate(s1, d1);
            ++i;
            results[j] = r0 < r1 ? r0 : r1;
            ++j;
        }
        return results;
    }

}
