package com.mapfap.image.resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.mapfap.image.entity.Image;
import com.mapfap.image.entity.atom.Entry;
import com.mapfap.image.entity.atom.Feed;
import com.mapfap.image.entity.atom.Link;
import com.mapfap.image.persistence.ImagePersistence;

/**
 * Resource for JAX-RS service.
 * Handing all the request to '/images'
 * 
 * @author Sarun Wongtanakarn
 */
@Singleton
@Path("/images")
public class ImageResource {

	@Context
	UriInfo uriInfo;
	private static ImagePersistence persistence;
	public static final String FILE_STORAGE = "images/";
	
	/**
	 * Construct ImageResource with setup necessary stuff.
	 */
	public ImageResource() {
		
		// TODO: Change the path of OpenCV native library.
		System.load(new File("/usr/local/share/OpenCV/java/libopencv_java2410.dylib").getAbsolutePath());
//		System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // this does not work.
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("images");
		EntityManager manager = factory.createEntityManager();
		persistence = new ImagePersistence(manager);
//		persistence.clearAll();
	}
	
	/**
	 * Store image on the server by using address of image on the Internet.
	 * @param element body of request which parsed with JAXB.
	 * @return result response. If it's done successfully, return location header too. 
	 */
	@POST
	@Path("")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response storeImage(JAXBElement<Link> element) {
		URL sourceURL = element.getValue().getHref();
		URI location = null;
		try {
			  InputStream is = sourceURL.openStream ();
			  byte[] bytes = IOUtils.toByteArray(is);
			  location = storeImage(Calendar.getInstance().getTimeInMillis() + "", bytes);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		
		return Response.created(location).build();
	}
	
	/**
	 * Store image on the server by sending the stream directly.
	 * @param bytes array of byte data of image.
	 * @return result response. If it's done successfully, return location header too.
	 */
	@POST
	@Path("")
	@Consumes({ "image/png", "image/jpg" })
	public Response storeImage(byte[] bytes) {
		// TODO: Just read
		return null;
	}
	
	/**
	 * Store image on the server by sending multi-part web form.
	 * @param bytes array of byte data.
	 * @return result response. If it's done successfully, return location header too.
	 */
	@POST
	@Path("")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response storeImage(@FormDataParam("file") byte[] bytes, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
		
		String fileName = contentDispositionHeader.getFileName();
		
		if (fileName.length() > 15) {
			fileName = fileName.substring(15);
		}
		fileName = Calendar.getInstance().getTimeInMillis() + "_" + fileName; // prevent name conflicted.
		
		URI uri = storeImage(fileName, bytes);
		return Response.created(uri).header("Access-Control-Allow-Origin", "*").header("Access-Control-Expose-Headers", "Location").build();
	}
	
	/**
	 * Store image on the server.
	 * This will be called from all storeImage() methods.
	 * @param fileName name of file.
	 * @param bytes array of byte data of image.
	 * @return URI of image that stored.
	 */
	private URI storeImage(String fileName, byte[] bytes) {
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
			uri = new URI(uriInfo.getAbsolutePath() + "/" + id + "/" + "?width=500&height=500");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.out.println(uri.toString());
		return uri;
	}
	
	/**
	 * List all images stored on the server.
	 * @return list of all images stored on the server.
	 * @throws MalformedURLException if URL of any images is false.
	 */
	@GET
	@Path("")
	@Produces({ MediaType.APPLICATION_ATOM_XML })
	public Response listImages() throws MalformedURLException {
		Feed feed = new Feed();		
		List<Entry> entries = new ArrayList<Entry>();
		feed.setEntries(entries);
		for (Image image : persistence.listImages()) {
			Entry entry = new Entry();
			Link link = new Link();
			entry.setLink(link);
			link.setHref(new URL(uriInfo.getAbsolutePath() + "/" + image.getId() + "?width=500&height=500"));
			entries.add(entry);
		}
		
		return Response.ok(feed).build();
	}

	/**
	 * Get image from server.
	 * @param id ID of image.
	 * @param width desired width of image.
	 * @param height desired height of image.
	 * @param brightness desired brightness of image.
	 * @return image according to all conditions requested.
	 */
	@GET 
	@Path("{id}")
	@Produces({ "image/png" })
	public Response getImage(
			@PathParam("id") String id,
			@QueryParam("width") Integer width,
			@QueryParam("height") Integer height,
			@QueryParam("brightness") Double brightness,
			@QueryParam("gaussian") boolean gaussian,
			@QueryParam("grayscale") boolean grayScale
			) {
		
		if (width == null || height == null) {
			return Response
					.status(HttpStatus.BAD_REQUEST_400)
					.entity("width and height must be specify")
					.type(MediaType.TEXT_PLAIN)
					.build();
		} else if (width * height == 0) {
			return Response
					.status(HttpStatus.BAD_REQUEST_400)
					.entity("width and height can't be zero")
					.type(MediaType.TEXT_PLAIN)
					.build();
		} else if ( width > 20000 || height > 20000 ) {
			return Response
					.status(HttpStatus.BAD_REQUEST_400)
					.entity("too large width and height")
					.type(MediaType.TEXT_PLAIN)
					.build();
		}
		
		Image image = persistence.load(id);
		
		if (image == null) {
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
		
		String fileName = image.getFileName();
		String newFileName = FILE_STORAGE + "_" + fileName;
		
		File file = new File(FILE_STORAGE + fileName);
		if (! file.isFile()) {
			return Response
					.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
					.entity("file is missing")
					.type(MediaType.TEXT_PLAIN)
					.build();
		}
		
		int colorMode = (grayScale) ? Highgui.CV_LOAD_IMAGE_GRAYSCALE : Highgui.CV_LOAD_IMAGE_COLOR;
		Mat original = Highgui.imread(FILE_STORAGE + fileName, colorMode);
		
		Mat result = new Mat();
		
		Size size = new Size(Math.abs(width), Math.abs(height));
		Imgproc.resize(original, result, size);
		
		if (gaussian) {
			Imgproc.GaussianBlur(result, result ,new Size(45, 45), 0);
		}
		if (width < 0) {
			Core.flip(result, result, 1);
		}
		
		if (height < 0) {
			Core.flip(result, result, 0);
		}
		
		
		if (brightness != null) {			
			result.convertTo(result, -1, brightness, 0);
		}
		
	    Highgui.imwrite(newFileName, result);
		
		return Response.ok(new File(newFileName)).header("Access-Control-Allow-Origin", "*").build();
	}
}
