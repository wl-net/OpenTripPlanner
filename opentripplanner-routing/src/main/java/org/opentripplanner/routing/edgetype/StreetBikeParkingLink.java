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

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.BikeParkingVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;

/**
 * Link a bike parking area to the street.
 * @author mattwigway
 */
public class StreetBikeParkingLink extends Edge {
    private static final long serialVersionUID = -1358634924191182567L;

    public StreetBikeParkingLink(BikeParkingVertex v1, StreetVertex v2) {
        super(v1, v2);
    }
    
    public StreetBikeParkingLink(StreetVertex v1, BikeParkingVertex v2) {
        super(v1, v2);
    }

    @Override
    public State traverse(State s0) {
        StateEditor s1 = s0.edit(this);
        
        // almost free
        s1.incrementTimeInSeconds(1);
        s1.incrementWeight(1);
        s1.setBackMode(s0.getNonTransitMode());
        
        return s1.makeState();
    }

    @Override
    public String getName() {
        return null;
    }

}
