package org.opentripplanner.analyst.core;

import org.opentripplanner.routing.core.State;

/**
 * A sample operator which finds the number of vehicles to reach a given point.
 */
public class BoardingsSampleOperator extends SampleOperator {

    public BoardingsSampleOperator() {
        this.minimize = true;
    }

    @Override
    public float evaluate(State state, double distance) {
        return state.getNumBoardings();
    }

}
