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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

@Singleton
@Path("/images")
public class ImageResource {

	@Context
	UriInfo uriInfo;

	public ImageResource() {
		System.load(new File("/usr/local/share/OpenCV/java/libopencv_java2410.dylib").getAbsolutePath());
	}
	
	public static final String FILE_STORAGE = "images/";
	
	@POST
	@Path("")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response storeImage(@FormDataParam("file") byte[] bytes, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
		String filePath = FILE_STORAGE + contentDispositionHeader.getFileName();
		try {
			FileOutputStream out = new FileOutputStream(filePath);
			out.write(bytes);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException();
		}
		
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
		if (width * height == 0) {
			return Response
					.status(HttpStatus.BAD_REQUEST_400)
					.entity("width or height can't be zero")
					.type(MediaType.TEXT_PLAIN)
					.build();
		}
		
		Mat original = Highgui.imread("test.png");
		Mat result = new Mat();
		Size size = new Size(width, height);
		Imgproc.resize(original, result, size);
		Highgui.imwrite("test2.png", result);
		
		return Response.ok(new File("test2.png")).build();
	}
}
