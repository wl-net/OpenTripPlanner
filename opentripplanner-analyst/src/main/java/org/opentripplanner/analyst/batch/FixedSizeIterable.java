package org.opentripplanner.analyst.batch;

public interface FixedSizeIterable<T> extends Iterable<T> {
    public int size();
}
