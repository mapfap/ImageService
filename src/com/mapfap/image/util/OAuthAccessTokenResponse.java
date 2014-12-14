package com.mapfap.image.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Holder class for holding data in OAuth process.
 * This fix bugs occurs from interaction between Apache Oltu and Google OAuth.
 * @author Sarun Wongtanakarn
 */
public class OAuthAccessTokenResponse extends OAuthJSONAccessTokenResponse {

	/**
	 * Fix this method, just read JSON object and put data to HashMap.
	 */
	@Override
	protected void setBody(String body) throws OAuthProblemException {
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			JSONObject obj = new JSONObject(body);
			Iterator<?> it = obj.keys();
			while (it.hasNext()) {
				Object o = it.next();
				if (o instanceof String) {
					String key = (String) o;
					params.put(key, obj.get(key));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		parameters = params;
	}

}
