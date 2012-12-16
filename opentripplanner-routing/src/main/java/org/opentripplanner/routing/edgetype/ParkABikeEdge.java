package org.opentripplanner.routing.edgetype;

import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.vertextype.BikeParkingVertex;

/**
 * An edge that represents parking a bicycle in a bicycle storage facility of some sort.
 * @author mattwigway
 *
 */
public class ParkABikeEdge extends Edge {
    private static final long serialVersionUID = -9139925573261092565L;

    public ParkABikeEdge(BikeParkingVertex bpv) {
        super(bpv, bpv);
    }

    @Override
    public State traverse(State s0) {
        // TODO: reverse traversal (in transit case)
        // We can't park a bike if we don't have one
        if (!s0.getNonTransitMode().equals(TraverseMode.BICYCLE))
            return null;
        
        // we shouldn't leave a rented bike in a public parking lot
        if (s0.isBikeRenting())
            return null;
        
        RoutingRequest options = s0.getOptions();
        StateEditor s1 = s0.edit(this);
        
        // same both forwards and backwards
        s1.incrementTimeInSeconds(options.bikeParkingTime);
        
        // TODO: What if WALK isn't in the allowed modes?
        s1.setNonTransitMode(TraverseMode.WALK);
        
        return s1.makeState();
    }

    @Override
    public String getName() {
        return null;
    }

}
