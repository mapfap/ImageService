package com.mapfap.image.resource;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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

import com.mapfap.image.entity.Image;
import com.mapfap.image.persistence.ImagePersistence;

@Singleton
@Path("/images")
public class ImageResource {

	@Context
	UriInfo uriInfo;
	private static ImagePersistence persistence;

	public ImageResource() {
		System.load(new File("/usr/local/share/OpenCV/java/libopencv_java2410.dylib").getAbsolutePath());
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("images");
		EntityManager manager = factory.createEntityManager();
		persistence = new ImagePersistence(manager);
//		persistence.clearAll();
	}
	
	public static final String FILE_STORAGE = "images/";
	
	@POST
	@Path("")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response storeImage(@FormDataParam("file") byte[] bytes, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
		String fileName = contentDispositionHeader.getFileName().substring(15);
		fileName = Calendar.getInstance().getTimeInMillis() + "_" + fileName; // prevent name conflicted.
		
		String filePath = FILE_STORAGE + fileName;
		try {
			FileOutputStream out = new FileOutputStream(filePath);
			out.write(bytes);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException();
		}
		
		Image image = new Image(fileName);
		persistence.save(image);
		String id = persistence.load(image.getId()).getId(); // make sure it's saved.
		
		URI uri = null;
		try {
			uri = new URI(uriInfo.getAbsolutePath() + "/" + id + "100x100");
			System.out.println(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return Response.created(uri).build();
	}

	@GET 
	@Path("{id}/{width : \\d+}x{height : \\d+}")
	@Produces({ "image/png" })
	public Response getImage(@PathParam("id") String id, @PathParam("width") int width, @PathParam("height") int height) {
		if (width * height == 0) {
			return Response
					.status(HttpStatus.BAD_REQUEST_400)
					.entity("width or height can't be zero")
					.type(MediaType.TEXT_PLAIN)
					.build();
		}
		
		Image image = persistence.load(id);
		
		if (image == null) {
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
		
		String fileName = image.getFileName();
		String newFileName = FILE_STORAGE + "_" +fileName;
		
		Mat original = Highgui.imread(FILE_STORAGE + fileName);
		Mat result = new Mat();
		Size size = new Size(width, height);
		Imgproc.resize(original, result, size);
		Highgui.imwrite(newFileName, result);
		
		return Response.ok(new File(newFileName)).build();
	}
}
