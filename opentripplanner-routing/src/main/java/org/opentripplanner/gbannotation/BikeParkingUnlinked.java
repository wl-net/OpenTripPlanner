package org.opentripplanner.gbannotation;

import org.opentripplanner.routing.vertextype.BikeParkingVertex;

import lombok.AllArgsConstructor;

/**
 * Represents a disconnected bike rental station in the graph.
 * @author mattwigway
 *
 */
@AllArgsConstructor
public class BikeParkingUnlinked extends GraphBuilderAnnotation {
    private static final long serialVersionUID = -1126815206409170028L;

    public static final String FMT = "Bike parking %s not near any streets; " +
    		"it will not be usable."; 
    
    private final BikeParkingVertex parking;
            
    @Override
    public String getMessage() {
        return String.format(FMT, parking);
    }

}
