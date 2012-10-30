package org.opentripplanner.analyst.batch;

import java.util.Iterator;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.graph.Vertex;

public class PackedSampleList implements SampleList {

    private final int length;
    
    private final Vertex[] vertices;
    
    private final float[] distances;

    public PackedSampleList(SampleList sl) {
        length = sl.getSize();
        vertices = new Vertex[length * 2];
        distances = new float[length * 2];
        int i = 0;
        for (Sample s : sl) {
            vertices[i] = s.v0;
            distances[i] = s.d0;
            i += 1;
            vertices[i] = s.v1;
            distances[i] = s.d1;
            i += 1;
        }
    }
    
    @Override
    public Iterator<Sample> iterator() {
        return new Iterator<Sample>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < length;
            }

            @Override
            public Sample next() {
                return new Sample(vertices[i], distances[i], vertices[i+1], distances[i+1]);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        };
    }

    @Override
    public int getSize() {
        return length;
    }

}
