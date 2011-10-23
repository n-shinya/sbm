package auth;

import java.util.HashMap;
import java.util.Map;

import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Scope.Params;
import play.mvc.results.Redirect;

import com.google.gson.JsonObject;

public class SalesForceOAuth2 {

	public String authorizationURL;
	public String accessTokenURL;
	public String clientid;
	public String secret;

	public SalesForceOAuth2(String authorizationURL,
			String accessTokenURL,
			String clientid,
			String secret) {
		this.accessTokenURL = accessTokenURL;
		this.authorizationURL = authorizationURL;
		this.clientid = clientid;
		this.secret = secret;
	}

	public static boolean isCodeResponse() {
		return Params.current().get("code") != null;
	}

	public void retrieveVerificationCode(String callbackURL) {
		throw new Redirect(authorizationURL
				+ "?client_id=" + clientid
				+ "&response_type=code"
				+ "&redirect_uri=" + callbackURL);
	}

	public Response retrieveAccessToken(String callbackURL) {
		String accessCode = Params.current().get("code");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("client_id", clientid);
		params.put("client_secret", secret);
		params.put("redirect_uri", callbackURL);
		params.put("code", accessCode);
		params.put("grant_type", "authorization_code");
		HttpResponse response = WS.url(accessTokenURL).params(params).post();
		return new Response(response);
	}

	public static class Response {
		public final String id;
		public final String accessToken;
		public final Error error;
		public final WS.HttpResponse httpResponse;
		private Response(String accessToken, Error error, WS.HttpResponse response, String id) {
			this.accessToken = accessToken;
			this.error = error;
			this.httpResponse = response;
			this.id = id;
		}
		public Response(WS.HttpResponse response) {
			this.httpResponse = response;
			JsonObject jsonObj = response.getJson().getAsJsonObject();
			String token = jsonObj.get("access_token").getAsString();
			String id = jsonObj.get("id").getAsString();
			if (token != null && id != null) {
				this.accessToken = token;
				this.id = id;
				this.error = null;
			} else {
				this.accessToken = null;
				this.id = null;
				this.error = Error.oauth2(response);
			}
		}
		public static Response error(Error error, WS.HttpResponse response) {
			return new Response(null, error, response, null);
		}
	}

	public static class Error {
		public final Type type;
		public final String error;
		public final String description;
		public enum Type {
			COMMUNICATION,
			OAUTH,
			UNKNOWN
		}
		private Error(Type type, String error, String description) {
			this.type = type;
			this.error = error;
			this.description = description;
		}
		static Error communication() {
			return new Error(Type.COMMUNICATION, null, null);
		}
		static Error oauth2(WS.HttpResponse response) {
			if (response.getQueryString().containsKey("error")) {
				Map<String, String> qs = response.getQueryString();
				return new Error(Type.OAUTH,
						qs.get("error"),
						qs.get("error_description"));
			} else if (response.getContentType().startsWith("text/javascript")) {
				JsonObject jsonResponse = response.getJson().getAsJsonObject().getAsJsonObject("error");
				return new Error(Type.OAUTH,
						jsonResponse.getAsJsonPrimitive("type").getAsString(),
						jsonResponse.getAsJsonPrimitive("message").getAsString());
			} else {
				return new Error(Type.UNKNOWN, null, null);
			}
		}
		@Override 
		public String toString() {
			return "OAuth2 Error: " + type + " - " + error + " (" + description + ")";
		}
	}
}
