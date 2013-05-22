package org.opentripplanner.api.standalone;

import java.io.IOException;
import java.net.BindException;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

public class GrizzlyServer {

    private static final Logger LOG = LoggerFactory.getLogger(GrizzlyServer.class);

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {

        /* CONFIGURE GRIZZLY SERVER */
        LOG.info("Starting OTP Grizzly server...");
        // Rather than use Jersey's GrizzlyServerFactory we will construct it manually, so we can
        // set the number of threads, etc.
        HttpServer httpServer = new HttpServer();
        NetworkListener networkListener = new NetworkListener("sample-listener", "localhost", PORT);
        ThreadPoolConfig threadPoolConfig = ThreadPoolConfig.defaultConfig()
                .setCorePoolSize(2).setMaxPoolSize(4);
        networkListener.getTransport().setWorkerThreadPoolConfig(threadPoolConfig);
        httpServer.addListener(networkListener);
        ResourceConfig rc = new PackagesResourceConfig("org.opentripplanner");
        // DelegatingFilterProxy.class.getName() does not seem to work out of the box.
        // Register a custom authentication filter.
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, 
                 new String[] { JerseyAuthFilter.class.getName() });
        // Provide Jersey a factory class that gets injected objects from the Spring context
        IoCComponentProviderFactory ioc_factory = OTPConfigurator.fromCommandLineArguments(args);

        /* ADD A COUPLE OF HANDLERS (~SERVLETS) */
        // 1. A Grizzly wrapper around the Jersey WebApplication. 
        //    We cannot set the context path to /opentripplanner-api-webapp/ws
        //    https://java.net/jira/browse/GRIZZLY-1481?focusedCommentId=360385&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_360385
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, rc, ioc_factory);
        httpServer.getServerConfiguration().addHttpHandler(handler, "/ws/");
        // 2. A static content server for the client JS apps etc.
        //    This is a filesystem path, not classpath.
        //    Files are relative to the project dir, so
        //    from ./ we can reach e.g. target/classes/data-sources.xml
        final String clientPath = "../opentripplanner-webapp/target/opentripplanner-webapp/";
        httpServer.getServerConfiguration().addHttpHandler(new StaticHttpHandler(clientPath), "/");
        
        /* RELINQUISH CONTROL TO THE SERVER THREAD */
        try {
            httpServer.start(); 
            LOG.info("Grizzly server running.");
            Thread.currentThread().join();
        } catch (BindException be) {
            LOG.error("Cannot bind to port {}. Is it already in use?", PORT);
        } catch (InterruptedException ie) {
            httpServer.stop();
        }
        
    }
    
}