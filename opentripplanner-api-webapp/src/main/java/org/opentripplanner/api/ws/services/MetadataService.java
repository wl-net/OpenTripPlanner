package org.opentripplanner.api.ws.services;

import java.util.HashMap;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import org.opentripplanner.api.ws.GraphMetadata;
import org.opentripplanner.routing.services.GraphService;
import org.springframework.stereotype.Component;

@Component
public class MetadataService {
    
    @Getter @Setter @Inject private GraphService graphService;

    HashMap<String, GraphMetadata> metadata = new HashMap<String, GraphMetadata>();
    
    public synchronized GraphMetadata getMetadata(String routerId) {
        GraphMetadata data = metadata.get(routerId);
        if (data == null) {
            data = new GraphMetadata(graphService.getGraph(routerId));
            metadata.put(routerId, data);
        }
        return data;
    }

}
