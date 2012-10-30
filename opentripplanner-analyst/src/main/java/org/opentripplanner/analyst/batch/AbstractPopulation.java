package org.opentripplanner.analyst.batch;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractPopulation implements Population {

//    private int size = 0;
    
    // an Iterable over the samples in this population, or null if not yet computed
    protected SampleList sampleList = null; 

    // this will store the lat/lon/input data
    // can be a data source for a basic SampleList 
    private List<Individual> individuals;
    
    @Override
    public Iterator<Individual> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int totalSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int filteredSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SampleList getSampleList() {
        return sampleList;
    }

    @Override
    public void setup() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createIndividuals() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeAppropriateFormat(String fileName, ResultSet results) {
        // TODO Auto-generated method stub
        
    }

    
    
}
