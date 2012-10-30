package org.opentripplanner.analyst.batch;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class MaterializedIndividualList implements IndividualList {

    // map from field names to field numbers for all individuals in this list
    private Map<String, Integer> fieldMapping = Maps.newHashMap();
    
    /** Allow creating IndividualLists via Spring XML */
    public MaterializedIndividualList(List<Individual> individuals) {
        
    }
    
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
