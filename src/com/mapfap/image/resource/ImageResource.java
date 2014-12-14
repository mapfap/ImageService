package com.mapfap.image.resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceException;

import org.apache.commons.io.IOUtils;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONObject;

import com.mapfap.image.entity.Image;
import com.mapfap.image.entity.atom.Entry;
import com.mapfap.image.entity.atom.Feed;
import com.mapfap.image.entity.atom.Link;
import com.mapfap.image.persistence.ImagePersistence;
import com.mapfap.image.processing.ImageProcessor;
import com.mapfap.image.processing.ImageProcessorFactory;
import com.mapfap.image.processing.ProcessInstruction;
import com.mapfap.image.util.FileManager;
import com.mapfap.image.util.ImageResourceHelper;
import com.mapfap.image.util.OAuthHelper;

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
	private FileManager fileManager;
	private static ImagePersistence persistence;
	private ImageResourceHelper helper;
	private OAuthHelper oAuthHelper;
	private static ImageProcessor processor;
	
	/**
	 * Construct ImageResource with setup necessary stuff.
	 */
	public ImageResource() {
		ImageProcessorFactory imageProcessorFactory = ImageProcessorFactory.getInstance();
		processor = imageProcessorFactory.getImageProcessor();
//		persistence.clearAll();
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("images");
		EntityManager manager = factory.createEntityManager();
		persistence = new ImagePersistence(manager);
	
		fileManager = FileManager.getInstance();
		fileManager.createFileStorage();
		
		helper = new ImageResourceHelper(persistence, fileManager, uriInfo);
		oAuthHelper = new OAuthHelper();
	}
	
	/**
	 * Login page for user to access OAuth provider.
	 * For now, it's Google OAuth 2.0.
	 * @return Response redirect user to OAuth provider. 
	 */
	@GET
	@Path("login")
	@Produces({ MediaType.TEXT_HTML })
	public Response login() {
		try {
			OAuthClientRequest request = oAuthHelper.getConsentForm();
			return helper.redirect(request.getLocationUri());
		} catch (OAuthSystemException e) {
			e.printStackTrace();
			throw new WebServiceException();
		}
	}
	
	/**
	 * This is callback is for OAuth provider to send 'authorization code'.
	 * and then begin exchange it for 'access token'.
	 * 
	 * @param code the authorization code
	 * @return Response to redirect user back to the origin page.
	 */
	@GET
	@Path("login/callback")
	@Produces({ MediaType.TEXT_HTML })
	public Response loginCallback(@QueryParam("code") String code) {
		try {
			String accessToken = oAuthHelper.getAccessToken(code);
			String name = oAuthHelper.getUserInfo(accessToken).getString("name");
			return helper.redirect(OAuthHelper.CLIENT_SITE + "?name=" + name + "&token=" + accessToken);

		} catch (OAuthSystemException | OAuthProblemException e) {
			e.printStackTrace();
			return Response.status(HttpStatus.BAD_REQUEST_400).build();
		}
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
			  location = helper.storeImage(helper.getTimestamp() + ".png", bytes, null);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		return helper.locationResponse(location);
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
		URI location = helper.storeImage(helper.getTimestamp(), bytes, null);
		return helper.locationResponse(location);
	}
	
	/**
	 * Store image on the server by sending multi-part web form.
	 * @param bytes array of byte data.
	 * @return result response. If it's done successfully, return location header too.
	 */
	@POST
	@Path("")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response storeImage(
			@FormDataParam("file") byte[] bytes,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
			// This suppose to be HeaderParam("Authoriztion")
			// But due to Ajax's limitation of CORS, I have to use it as QueryParam 
			@QueryParam("token") String accessToken
		) {
		
		String fileName = contentDispositionHeader.getFileName();
		
		if (fileName.length() > 15) {
			fileName = fileName.substring(15);
		}
		fileName = helper.getTimestamp() + "_" + fileName; // prevent name conflicted.
		
		URI location;
		if (accessToken != null) {
			try {
				JSONObject userInfo = oAuthHelper.getUserInfo(accessToken);				
				location = helper.storeImage(fileName, bytes , userInfo.getString("id"));
			} catch (WebServiceException e) {
				return helper.error(HttpStatus.BAD_REQUEST_400, "invalid token");
			}
		
		} else {			
			location = helper.storeImage(fileName, bytes, null);
		}
		
		return helper.locationResponse(location);
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
			link.setHref(new URL(uriInfo.getAbsolutePath() + "/" + image.getId() + ImageResourceHelper.DEFAULT_IMAGE_PARAMS));
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
			@QueryParam("grayscale") boolean grayscale
			) {
		
		Response r = helper.validateWidthAndHeight(width, height);
		if (r != null) {
			return r;
		}
		
		Image image = persistence.load(id);
		
		if (image == null) {
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
		
//		System.out.println("owner: " + image.getOwnerID());
		
		String fileName = image.getFileName();
		
		File file = fileManager.getFile(fileName);
		if (! file.isFile()) {
			return helper.error(HttpStatus.INTERNAL_SERVER_ERROR_500, "file is missing");
		}
		
		String outputFileName = processor.process(fileName, new ProcessInstruction(width, height, brightness, gaussian, grayscale));
		return Response.ok(new File(outputFileName)).build();
	}
	
	/**
	 * Delete image from server.
	 * @param id ID of image to be deleted.
	 * @param accessToken access token for authentication.
	 * @return Response OK if success.
	 */
	@DELETE 
	@Path("{id}")
	@Produces({ "image/png" })
	public Response deleteImage(
			@PathParam("id") String id,
			// This suppose to be HeaderParam("Authoriztion")
			// But due to Ajax's limitation of CORS, I have to use it as QueryParam 
			@QueryParam("token") String accessToken
			) {
		
		Image image = persistence.load(id);
		if (image == null) {
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
		
		// Anonymous uploaded images are not allow to deleted. 
		if (image.getOwnerID().equals("")) {
			return helper.error(HttpStatus.FORBIDDEN_403, "this image are not allow to delete");
		}
		
		if (accessToken == null) {
			return helper.error(HttpStatus.UNAUTHORIZED_401, "missing token");
		}
		
		// Now, acquire ID using accessToken and compare with image's ownerID.
		try {
		
		String userID = oAuthHelper.getUserInfo(accessToken).getString("id");
		if (userID.equals(image.getOwnerID())) {
			// It's OK to delete now.
			persistence.delete(image.getId());
			File file = fileManager.getFile(image.getFileName());
			if (file.isFile()) {
				file.delete();
			}
			return Response.ok().build();
		} else {
			return helper.error(HttpStatus.FORBIDDEN_403, "you have no right to delete this image");
		}
		
		} catch (WebServiceException e) {			
			return helper.error(HttpStatus.BAD_REQUEST_400, "invalid token");
		}
		
	}
}