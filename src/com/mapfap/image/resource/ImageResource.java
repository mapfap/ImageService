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
import javax.ws.rs.DELETE;
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
import javax.xml.ws.WebServiceException;

import org.apache.commons.io.IOUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.eclipse.jetty.http.HttpHeader;
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
import com.mapfap.image.util.OAuthAccessTokenResponse;

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
	private static ImageProcessor processor;
	private static final String DEFAULT_IMAGE_PARAMS = "?width=300&height=300";
	
	private static final String GOOGLE_CLIENT_ID = "886476666960-6c4hgkc9bs339r58osjclma5cs3tfdtg.apps.googleusercontent.com";
	private static final String GOOGLE_CLIENT_SECRET = "o3ryzVBDTuynUsx-YmtqYjt4";
	private static final String GOOGLE_REDIRECT_URIS = "http://www.mapfap.tk/images/login/callback";
	private static final String CLIENT_SITE = "http://www.mapfap.com/play";
	
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
			  location = storeImage(getTimestamp() + ".png", bytes, null);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		return locationResponse(location);
	}
	
	@GET
	@Path("login")
	@Produces({ MediaType.TEXT_HTML })
	public Response login() {
		try {
			OAuthClientRequest request = OAuthClientRequest
					   .authorizationProvider(OAuthProviderType.GOOGLE)
					   .setClientId(GOOGLE_CLIENT_ID)
					   .setRedirectURI(GOOGLE_REDIRECT_URIS)
					   .setResponseType("code")
					   .setScope("https://www.googleapis.com/auth/plus.me")
					   .buildQueryMessage();
			return redirect(request.getLocationUri());
			
		} catch (OAuthSystemException e) {
			e.printStackTrace();
			throw new WebServiceException();
		}
	}
	
	@GET
	@Path("login/callback")
	@Produces({ MediaType.TEXT_HTML })
	public Response loginCallback(@QueryParam("code") String code) {
		try {
			OAuthClientRequest request = OAuthClientRequest
					.tokenProvider(OAuthProviderType.GOOGLE)
					.setGrantType(GrantType.AUTHORIZATION_CODE)
					.setClientId(GOOGLE_CLIENT_ID)
					.setClientSecret(GOOGLE_CLIENT_SECRET)
					.setRedirectURI(GOOGLE_REDIRECT_URIS)
					.setCode(code)
					.buildBodyMessage();

			OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
			OAuthAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request, OAuthAccessTokenResponse.class);
			String accessToken = oAuthResponse.getAccessToken();

			String name = getUserInfo(accessToken).get("name").toString();
			return redirect(CLIENT_SITE + "?name=" + name + "&token=" + accessToken);

		} catch (OAuthSystemException | OAuthProblemException e) {
			e.printStackTrace();
		}
		return Response.status(HttpStatus.BAD_REQUEST_400).build();
	}
	
	private JSONObject getUserInfo(String accessToken) {
		OAuthClientRequest bearerClientRequest;
		try {
			bearerClientRequest = new OAuthBearerClientRequest("https://www.googleapis.com/oauth2/v2/userinfo")
			.setAccessToken(accessToken)
			.buildQueryMessage();
			
			OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
			OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
			return new JSONObject(resourceResponse.getBody());
			
		} catch (OAuthSystemException | OAuthProblemException e) {
			e.printStackTrace();
			throw new WebServiceException();
		}
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
		URI location = storeImage(getTimestamp(), bytes, null);
		return locationResponse(location);
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
		fileName = getTimestamp() + "_" + fileName; // prevent name conflicted.
		
		URI location;
		if (accessToken != null) {			
			location = storeImage(fileName, bytes , getUserInfo(accessToken).getString("id"));
		} else {			
			location = storeImage(fileName, bytes, null);
		}
		
		return locationResponse(location);
	}

	/**
	 * Response with specified location header.
	 * This enables 'AJAX' to read location header.
	 * @param location URI of the resource.
	 * @return HTTP Response with specified location header.
	 */
	private Response locationResponse(URI location) {
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
	private URI storeImage(String fileName, byte[] bytes, String ownerID) {
		
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
			link.setHref(new URL(uriInfo.getAbsolutePath() + "/" + image.getId() + DEFAULT_IMAGE_PARAMS));
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
		
		Response r = validateWidthAndHeight(width, height);
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
			return error(HttpStatus.INTERNAL_SERVER_ERROR_500, "file is missing");
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
			return error(HttpStatus.FORBIDDEN_403, "this image are not allow to delete");
		}
		
		if (accessToken == null) {
			return error(HttpStatus.UNAUTHORIZED_401, "missing token");
		}
		
		// Now, acquire ID using accessToken and compare with image's ownerID.
		try {
		
		String userID = getUserInfo(accessToken).getString("id");
		if (userID.equals(image.getOwnerID())) {
			// It's OK to delete now.
			persistence.delete(image.getId());
			File file = fileManager.getFile(image.getFileName());
			if (file.isFile()) {
				file.delete();
			}
			return Response.ok().build();
		} else {
			return error(HttpStatus.FORBIDDEN_403, "you have no right to delete this image");
		}
		
		} catch (WebServiceException e) {			
			return error(HttpStatus.BAD_REQUEST_400, "invalid token");
		}
		
	}
	

	/**
	 * Validate width and height of Request.
	 * @param width width of image.
	 * @param height height of image.
	 * @return Response if request is invalid; otherwise return null;
	 */
	private Response validateWidthAndHeight(Integer width, Integer height) {
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
	private Response error(int status, String text) {
		return Response.status(status).entity(text).type(MediaType.TEXT_PLAIN).build();
	}
	
	/**
	 * Return response 302 to redirect the client.
	 * @param uri redirect location.
	 * @return response 302 for redirecting.
	 */
	private Response redirect(String uri) {
		return Response.status(HttpStatus.FOUND_302).header(HttpHeader.LOCATION.asString(), uri).build();
	}
	
	/**
	 * Get current timestamp which is time is milliseconds.
	 * @return current timestamp in milliseconds.
	 */
	private String getTimestamp() {
		return String.valueOf(Calendar.getInstance().getTimeInMillis());
	}
}