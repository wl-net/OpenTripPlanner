package org.opentripplanner.analyst.batch;

import java.util.Iterator;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.analyst.core.SampleSource;

/**
 * A dynamic SampleList which builds samples based on an IndividualList.
 */
public class IndividualBackedSampleList implements SampleList {

    IndividualList individualList;
    SampleSource ss;
    
    public IndividualBackedSampleList(IndividualList individualList, SampleSource ss) {
        this.individualList = individualList;
        this.ss = ss;
    }
    
    @Override
    public Iterator<Sample> iterator() {
        return new Iterator<Sample>() {
            
            Iterator<Individual> ii = individualList.iterator();

            @Override
            public boolean hasNext() {
                return ii.hasNext();
            }

            @Override
            public Sample next() {
                Individual i = ii.next();
                return ss.getSample(i.lon, i.lat);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        };
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public int size() {
        return individualList.size();
    }

}
