package org.opentripplanner.routing.vertextype;

import org.opentripplanner.routing.graph.AbstractVertex;
import org.opentripplanner.routing.graph.Graph;

public class BikeParkingVertex extends AbstractVertex {
    private static final long serialVersionUID = 4589711404852519024L;

    public BikeParkingVertex(Graph g, String label, double x, double y) {
        super(g, label, x, y);
    }
    
    public BikeParkingVertex(Graph g, String label, double x, double y, String name) {
        super(g, label, x, y, name);
    }
    
    public String toString() {
        return "BikeParkingVertex " + this.getLabel();
    }
}
