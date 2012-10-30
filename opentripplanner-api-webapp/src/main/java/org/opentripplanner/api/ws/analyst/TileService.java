package org.opentripplanner.api.ws.analyst;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.opentripplanner.analyst.core.SlippyTile;
import org.opentripplanner.analyst.core.RasterPopulation;
import org.opentripplanner.analyst.parameter.Layer;
import org.opentripplanner.analyst.parameter.LayerList;
import org.opentripplanner.analyst.parameter.MIMEImageFormat;
import org.opentripplanner.analyst.parameter.Style;
import org.opentripplanner.analyst.parameter.StyleList;
import org.opentripplanner.analyst.request.RenderRequest;
import org.opentripplanner.analyst.request.Renderer;
import org.opentripplanner.api.common.RoutingResource;
import org.opentripplanner.routing.core.RoutingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.spring.Autowire;

// removed @Component, mixing spring and jersey annotations is bad?
@Path("/tile/{z}/{x}/{y}.png") 
@Autowire
public class TileService extends RoutingResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(TileService.class);

    @InjectParam
    private Renderer renderer;

    @PathParam("x") int x; 
    @PathParam("y") int y;
    @PathParam("z") int z;
    
    @QueryParam("layers")  @DefaultValue("traveltime") LayerList layers; 
    @QueryParam("styles")  @DefaultValue("color30")       StyleList styles;
    @QueryParam("format")  @DefaultValue("image/png")  MIMEImageFormat format;

    @GET @Produces("image/*")
    public Response tileGet() throws Exception { 
        
        RasterPopulation tileRequest = new SlippyTile(x, y, z);
        RoutingRequest sptRequestA = buildRequest(0);
        RoutingRequest sptRequestB = buildRequest(1);

        Layer layer = layers.get(0);
        Style style = styles.get(0);
        RenderRequest renderRequest = new RenderRequest(format, layer, style, true, false);

        return renderer.getResponse(tileRequest, sptRequestA, sptRequestB, renderRequest);
    }

}