package org.opentripplanner.api.standalone;

import org.opentripplanner.analyst.core.GeometryIndex;
import org.opentripplanner.analyst.request.SPTCache;
import org.opentripplanner.analyst.request.TileCache;
import org.opentripplanner.api.ws.PlanGenerator;
import org.opentripplanner.api.ws.services.MetadataService;
import org.opentripplanner.jsonp.JsonpCallbackFilter;
import org.opentripplanner.routing.algorithm.GenericAStar;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.impl.DefaultRemainingWeightHeuristicFactoryImpl;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.RetryingPathServiceImpl;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.services.RemainingWeightHeuristicFactory;
import org.opentripplanner.routing.services.SPTService;

public class OTPConfigurator {
    
    public static OTPComponentProviderFactory fromCommandLineArguments(String[] args) {
        
        // The GraphService
        GraphServiceImpl graphService = new GraphServiceImpl();
        graphService.setPath("/var/otp/graphs/");
        graphService.setDefaultRouterId("pdx");

        // The PathService which wraps the SPTService
        RetryingPathServiceImpl pathService = new RetryingPathServiceImpl();
        pathService.setFirstPathTimeout(10.0);
        pathService.setMultiPathTimeout(1.0);
        
        // An adapter to make Jersey see OTP as a dependency injection framework.
        // Associate our specific instances with their interface classes.
        OTPComponentProviderFactory cpf = new OTPComponentProviderFactory(); 
        cpf.bind(RoutingRequest.class, new RoutingRequest());
        cpf.bind(GraphService.class, graphService);
        cpf.bind(SPTService.class, new GenericAStar());
        cpf.bind(PathService.class, pathService);
        cpf.bind(PlanGenerator.class, new PlanGenerator());
        cpf.bind(MetadataService.class, new MetadataService());
        cpf.bind(JsonpCallbackFilter.class, new JsonpCallbackFilter());
        cpf.bind(RemainingWeightHeuristicFactory.class, new DefaultRemainingWeightHeuristicFactoryImpl()); 

        // Optional Analyst Modules
        cpf.bind(SPTCache.class, new SPTCache());
        cpf.bind(TileCache.class, new TileCache());
        cpf.bind(GeometryIndex.class, new GeometryIndex());
        
        // Perform field injection on bound instances and call post-construct methods
        cpf.doneBinding();        
        return cpf;         
        
    }

}
