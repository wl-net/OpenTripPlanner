package org.opentripplanner.analyst.batch;

import java.util.Iterator;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.graph.Vertex;

public class PackedSampleList implements SampleList {

    private final int nSamples;
    
    private final int length; // internal length of array, 2x number of samples
    
    private final Vertex[] vertices;
    
    private final float[] distances;

    // TODO: run-length encoding of null samples
    public PackedSampleList(SampleList sl) {
        nSamples = sl.getSize();
        length = 2 * nSamples;
        vertices = new Vertex[length];
        distances = new float[length];
        int i = 0;
        for (Sample s : sl) {
            if (s == null) {
                vertices[i] = null;
                distances[i] = 0;
                i += 1;
                vertices[i] = null;
                distances[i] = 0;
                i += 1;
            } else {
                vertices[i] = s.v0;
                distances[i] = s.d0;
                i += 1;
                vertices[i] = s.v1;
                distances[i] = s.d1;
                i += 1;
            }
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
                Sample sample = null;
                if (vertices[i] != null || vertices[i+1] != null )
                    sample = new Sample(vertices[i], distances[i], vertices[i+1], distances[i+1]);
                i += 2;
                return sample;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        };
    }

    @Override
    public int getSize() {
        return nSamples;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

}
