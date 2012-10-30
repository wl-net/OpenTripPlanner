package org.opentripplanner.analyst.batch;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractPopulation implements Population {

    //private int size = 0;
    
    // an Iterable over the samples in this population, or null if not yet computed
    @Getter @Setter 
    private SampleList sampleList = null; 

    // this will store the lat/lon/input data and can be a data source for a basic SampleList 
    @Getter @Setter 
    private IndividualList individualList = null;
    
    @Override
    public int totalSize() {
        if (this.individualList != null)
            return this.individualList.size();
        else
            return this.sampleList.size();
    }

    @Override
    public int filteredSize() {
        return this.totalSize();
    }
    
}
