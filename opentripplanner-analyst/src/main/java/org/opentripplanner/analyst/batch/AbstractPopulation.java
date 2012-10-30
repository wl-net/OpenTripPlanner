package org.opentripplanner.analyst.batch;

import lombok.Getter;

public abstract class AbstractPopulation implements Population {

    //private int size = 0;
    
    // an Iterable over the samples in this population, or null if not yet computed
    @Getter 
    protected SampleList sampleList; 

    // this will store the lat/lon/input data and can be a data source for a basic SampleList 
    @Getter 
    protected final IndividualList individualList;
    
    public AbstractPopulation (IndividualList il, SampleList sl) {
        this.individualList = il;
        this.sampleList = sl;
    }

    public AbstractPopulation (SampleList sl) {
        this(null,sl);
    }
    
    public AbstractPopulation (IndividualList il) {
        this(il, new IndividualBackedSampleList(il));
    }

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
    
    public void materializeSamples() {
//      if (sampleList == null)
//          resampleDynamic(); // push ssource injection down into generator 
      if (sampleList.isDynamic())
          sampleList = new PackedSampleList(sampleList);
  }    
  
}
