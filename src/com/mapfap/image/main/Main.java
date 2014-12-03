package com.mapfap.image.main;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.DoSFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;

import com.mapfap.image.resource.ImageResource;

/**
 * Main class of this app. 
 * Setup the configuration for Jetty deployment.
 * 
 * @author Sarun Wongtanakarn
 */
public class Main {

	// TODO: Change this !!!!!!!!!!
	public static String OPENCV_PATH_MAC = "/usr/local/share/OpenCV/java/libopencv_java2410.dylib"; // For Mac.
	public static String OPENCV_PATH_LINUX = "/usr/local/lib/libopencv_core.so"; // For Linux.
	
	public static String OPENCV_PATH;
	
	
	static final int PORT = 8080;
	private static Server server;

	/**
	 * Start server.
	 * @param args specify openCV native library path.
	 */
	public static void main(String[] args) {
		
		checkOS();
		
		if (args.length > 0) {
			OPENCV_PATH = args[0];
		}
		
		startServer(PORT);
		System.out.println("[Debug] OpenCV path set to: " + OPENCV_PATH);
		waitForExit();
	}

	/**
	 * 
	 */
	private static void checkOS() {
		String os = System.getProperty("os.name");
		if (os.contains("Mac")) {
			OPENCV_PATH = OPENCV_PATH_MAC;
		} else {			
			OPENCV_PATH = OPENCV_PATH_LINUX;
		}
	}

	/**
	 * Create a Jetty server and a context, add Jetty ServletContainer which
	 * dispatches requests to JAX-RS resource objects, and start the Jetty
	 * server.
	 * @param port running port of server.
	 */
	public static URI startServer(int port) {
		try {
			
			ResourceConfig resourceConfig = new ResourceConfig();		
			resourceConfig.packages(ImageResource.class.getPackage().getName());
			resourceConfig.register(MultiPartFeature.class);
			resourceConfig.register(RolesAllowedDynamicFeature.class);
			
			ServletContainer servletContainer = new ServletContainer(resourceConfig);
			ServletHolder holder = new ServletHolder(servletContainer);        
			holder.setInitParameter(ServerProperties.JSON_PROCESSING_FEATURE_DISABLE, "false");
			
			server = new Server(port);		
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        context.setContextPath("/");
	        context.addServlet(holder, "/*");
	        
	        
	        FilterHolder filterHolder = new FilterHolder( DoSFilter.class );
			filterHolder.setInitParameter("maxRequestsPerSec", "1");
			filterHolder.setInitParameter("delayMs", "-1");
			
			final EnumSet<DispatcherType> REQUEST_SCOPE = EnumSet.of(DispatcherType.REQUEST);
			context.addFilter(filterHolder, "/*", REQUEST_SCOPE);
	        
			server.setHandler(context);

			System.out.println("Starting Jetty server on port " + port);
			server.start();

			// This will be used by UnitTest
			return new URI("http://127.0.0.1:" + port + "/");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;

	}

	/**
	 * Wait key input from user to exit.
	 */
	public static void waitForExit() {
		System.out.println("Server started.  Press ENTER to exit.");
		try {
			System.in.read();
			System.out.println("Stopping server.");
			stopServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stop the server.
	 */
	public static void stopServer() {
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
