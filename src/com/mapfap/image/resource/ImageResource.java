package com.mapfap.image.resource;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response storeImage(@FormDataParam("file") byte[] bytes, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
		String filePath = "images/" + contentDispositionHeader.getFileName();
		FileOutputStream out;
		try {
			out = new FileOutputStream(filePath);
			out.write(bytes);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		Mat source = Highgui.imread(file, Highgui.CV_LOAD_IMAGE_COLOR);
		
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
