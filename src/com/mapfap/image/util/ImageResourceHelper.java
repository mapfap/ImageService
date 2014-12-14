package com.mapfap.image.util;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import com.mapfap.image.entity.Image;
import com.mapfap.image.persistence.ImagePersistence;

/**
 * Helps ImageResource on miscellaneous works.
 * Reduce duplicated code and coupling.
 * 
 * @author Sarun Wongtanakarn
 */
public class ImageResourceHelper {

	private ImagePersistence persistence;
	private FileManager fileManager;
	private UriInfo uriInfo;
	
	public static final String DEFAULT_IMAGE_PARAMS = "?width=300&height=300";

	public ImageResourceHelper(ImagePersistence persistence, FileManager fileManager, UriInfo uriInfo) {
		this.persistence = persistence;
		this.fileManager = fileManager;
		this.uriInfo = uriInfo;
	}

	/**
	 * Validate width and height of Request.
	 * @param width width of image.
	 * @param height height of image.
	 * @return Response if request is invalid; otherwise return null;
	 */
	public Response validateWidthAndHeight(Integer width, Integer height) {
		if (width == null || height == null) {
			return error(HttpStatus.BAD_REQUEST_400, "width and height must be specify");
		} else if (width * height == 0) {
			return error(HttpStatus.BAD_REQUEST_400, "width and height can't be zero");
		} else if (width > 20000 || height > 20000) {
			return error(HttpStatus.BAD_REQUEST_400, "too large width and height");
		}
		return null;
	}

	/**
	 * Return the error response with described text.
	 * @param status HTTP status code for error.
	 * @param text text to be sent to user.
	 * @return error response with described text.
	 */
	public Response error(int status, String text) {
		return Response.status(status).entity(text).type(MediaType.TEXT_PLAIN).build();
	}
	
	/**
	 * Return response 302 to redirect the client.
	 * @param uri redirect location.
	 * @return response 302 for redirecting.
	 */
	public Response redirect(String uri) {
		return Response.status(HttpStatus.FOUND_302).header(HttpHeader.LOCATION.asString(), uri).build();
	}
	
	/**
	 * Get current timestamp which is time is milliseconds.
	 * @return current timestamp in milliseconds.
	 */
	public String getTimestamp() {
		return String.valueOf(Calendar.getInstance().getTimeInMillis());
	}
	
	/**
	 * Response with specified location header.
	 * This enables 'AJAX' to read location header.
	 * @param location URI of the resource.
	 * @return HTTP Response with specified location header.
	 */
	public Response locationResponse(URI location) {
		return Response.created(location).header("Access-Control-Expose-Headers", "Location").build();
	}
	
	/**
	 * Store image on the server.
	 * This will be called from all storeImage() methods.
	 * @param fileName name of file.
	 * @param bytes array of byte data of image.
	 * @param ownerID ID of image owner, null for anonymous upload.
	 * @return URI of image that stored.
	 */
	public URI storeImage(String fileName, byte[] bytes, String ownerID) {
		
		try {
			FileOutputStream out = fileManager.getFileOutputStream(fileName);
			out.write(bytes);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException();
		}
		
		Image image = new Image(fileName);
		
		String owner = "";
		if (ownerID != null) {	
			owner = ownerID;
		} 
		image.setOwnerID(owner);
		
		persistence.save(image);
		String id = persistence.load(image.getId()).getId(); // make sure it's saved.
		
		URI uri = null;
		try {
			uri = new URI(uriInfo.getAbsolutePath() + "/" + id + DEFAULT_IMAGE_PARAMS);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.out.println(uri.toString());
		return uri;
	}

}
