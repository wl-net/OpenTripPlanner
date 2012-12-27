/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

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
