package com.mapfap.image.resource;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Singleton
@Path("/images")
public class ImageResource {

	@Context
	UriInfo uriInfo;
	private CacheControl cacheControl;

	public ImageResource() {
		cacheControl = new CacheControl();
		cacheControl.setMaxAge( 86400 );
	}
	
	@POST
	@Path("")
	public Response storeImage(@HeaderParam("Content-Type") String type, File file) {
		System.out.println(file.length());
		System.out.println(type);
		URI uri = null;
		try {
			uri = new URI("o");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return Response.created(uri).build();
	}
	

	@GET 
	@Path("{id : \\d+}/{width : \\d+}x{height : \\d+}")
	@Produces({ "image/png" })
	public Response getImage(@PathParam("id") long id, @PathParam("width") int width, @PathParam("height") int height) {
		File f = new File("test.png");
		return Response.ok(f).build();
	}
}
