package com.mapfap.image;

import java.io.IOException;
import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.mapfap.image.resource.ImageResource;

/**
 * 
 * RESTful web service using Jetty server on the specified port.
 * 
 * @author mapfap - Sarun Wongtanakarn
 * 
 */
public class JettyMain {

	static final int PORT = 8080;
	private static Server server;

	/**
	 * Start server.
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		startServer(PORT);
		waitForExit();
	}

	/**
	 * Create a Jetty server and a context, add Jetty ServletContainer which
	 * dispatches requests to JAX-RS resource objects, and start the Jetty
	 * server.
	 * 
	 * @param port running port of server.
	 */
	public static URI startServer(int port) {
		try {
			
			ResourceConfig resourceConfig = new ResourceConfig();		
			resourceConfig.packages(ImageResource.class.getPackage().getName());
			resourceConfig.register(MultiPartFeature.class);
			ServletContainer servletContainer = new ServletContainer(resourceConfig);
			ServletHolder sh = new ServletHolder(servletContainer);                
			Server server = new Server(port);		
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        context.setContextPath("/");
	        context.addServlet(sh, "/*");
			server.setHandler(context);

			System.out.println("Starting Jetty server on port " + port);
			server.start();
//
//			// returning server.getURI() is
//			// somehow cause an error with KUWIN network.
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
			// MemDaoFactory.getInstance().shutdown();
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
