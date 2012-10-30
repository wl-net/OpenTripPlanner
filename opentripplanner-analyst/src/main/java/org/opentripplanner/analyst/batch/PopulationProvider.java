package org.opentripplanner.analyst.batch;

/**
 * An interface for classes that can produce populations.
 * This allows us to use Spring "beans" to construct populations without introducing the usual
 * problems with sort-of-mutable objects that can exist in a quasi-initialized state. 
 */
public interface PopulationProvider {

    public Population getPopulation();
    
}
