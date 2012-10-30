package org.opentripplanner.analyst.batch;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

public class IndividualList implements FixedSizeIterable<Individual> {

    // map from field names to field numbers for all individuals in this list
    private Map<String, Integer> fieldMapping = Maps.newHashMap();
    
    @Override
    public Iterator<Individual> iterator() {
        return new Iterator<Individual> (){

            @Override
            public boolean hasNext() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Individual next() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void remove() {
                // TODO Auto-generated method stub
                
            }
        };
    }

    @Override
    public int size() {
        return 0;
    }
    
}
