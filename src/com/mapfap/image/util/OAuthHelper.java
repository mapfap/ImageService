package com.mapfap.image.util;

import javax.xml.ws.WebServiceException;

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
import org.json.JSONObject;

/**
 * Help ImageResource on connect with OAuth.
 * Reduce coupling to Apache Oltu and easier to config.
 * 
 * @author Sarun Wongtanakarn
 */
public class OAuthHelper {
	
	private static final String GOOGLE_CLIENT_ID = "886476666960-6c4hgkc9bs339r58osjclma5cs3tfdtg.apps.googleusercontent.com";
	private static final String GOOGLE_CLIENT_SECRET = "o3ryzVBDTuynUsx-YmtqYjt4";
	private static final String GOOGLE_REDIRECT_URIS = "http://www.mapfap.tk/images/login/callback";
	private static final String GOOGLE_USERINFO = "https://www.googleapis.com/oauth2/v2/userinfo";
	private static final String SCOPE = "https://www.googleapis.com/auth/plus.me";
	public static final String CLIENT_SITE = "http://www.mapfap.com/play";
	
	/**
	 * Get the OAuth consent form.
	 * @return OAuth consent form.
	 * @throws OAuthSystemException if there is a problem on OAuth process ,let resource
	 * 				consider how to deal with it.
	 */
	public OAuthClientRequest getConsentForm() throws OAuthSystemException {
		return OAuthClientRequest
				   .authorizationProvider(OAuthProviderType.GOOGLE)
				   .setClientId(GOOGLE_CLIENT_ID)
				   .setRedirectURI(GOOGLE_REDIRECT_URIS)
				   .setResponseType("code")
				   .setScope(SCOPE)
				   .buildQueryMessage();
	}

	/**
	 * Exchange the access token with OAuth provider.
	 * @param code authorization code for exchange process.
	 * @return access token.
	 * @throws OAuthSystemException if there is a problem on OAuth process.
	 * @throws OAuthProblemException if there is a problem on OAuth process.
	 */
	public String getAccessToken(String code) throws OAuthSystemException, OAuthProblemException {
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
		return oAuthResponse.getAccessToken();
	}
	
	/**
	 * Use the 'access token'  to acquire user information.
	 * @param accessToken access token that received from previous process. 
	 * @return user information.
	 */
	public JSONObject getUserInfo(String accessToken) {
		OAuthClientRequest bearerClientRequest;
		try {
			bearerClientRequest = new OAuthBearerClientRequest(GOOGLE_USERINFO)
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
	
}
