package controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;

import com.google.gson.JsonObject;

import play.Play;
import play.data.validation.Required;
import play.libs.Crypto;
import play.libs.OAuth2;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;
import play.mvc.Controller;
import play.utils.Java;
import auth.SalesForceOAuth2;
import auth.SalesForceOAuth2.Response;

public class OAuthSecure extends Controller {

	static SalesForceOAuth2 SALESFORCE = new SalesForceOAuth2(
			"https://login.salesforce.com/services/oauth2/authorize", 
			"https://na1.salesforce.com/services/oauth2/token", 
			"3MVG9QDx8IX8nP5R5mp.ZlRAFH8lHakNMek4_IrP6b1f9eYTE2hZVcndpubcIMiprs.6aXeqYjX.CHEkZ9nOD", 
			"6065091529342173268");
		
	
	@Before(unless={"login", "authenticate", "logout"})
	static void checkAccess() throws Throwable {
		// Authent
		if(!session.contains("userId")) {
			flash.put("url", "GET".equals(request.method) ? request.url : "/"); // seems a good default
			login();
		}
		// Checks
		Check check = getActionAnnotation(Check.class);
		if(check != null) {
			check(check);
		}
		check = getControllerInheritedAnnotation(Check.class);
		if(check != null) {
			check(check);
		}
	}

	private static void check(Check check) throws Throwable {
		for(String profile : check.value()) {
			boolean hasProfile = (Boolean)Security.invoke("check", profile);
			if(!hasProfile) {
				Security.invoke("onCheckFailed", profile);
			}
		}
	}

	public static void login() throws Throwable {
		if (OAuth2.isCodeResponse()) {
			flash.keep("url");
			Response response = SALESFORCE.retrieveAccessToken(authURL());
			Map<String, String> params = new HashMap<String, String>();
			params.put("Authorization", "OAuth " + response.accessToken);
			HttpResponse idResponse = WS.url(response.id).headers(params).post();
			JsonObject jsonObj = 
				idResponse.getJson().getAsJsonObject();
			String userId = jsonObj.get("user_id").getAsString();
			String displayName = jsonObj.get("display_name").getAsString();
			String thumbnail = jsonObj.get("photos").getAsJsonObject().get("thumbnail").getAsString();
			System.out.println(idResponse.getString());
			session.put("displayName", displayName);
			authenticate(userId, thumbnail);
		}
		flash.keep("url");

		SALESFORCE.retrieveVerificationCode(authURL());
	}

	public static void authenticate(String userId, String thumbnail) throws Throwable {
		Boolean allowed = (Boolean)Security.invoke("authenticate", userId, "dummy");
		if(validation.hasErrors() || !allowed) {
			flash.keep("url");
			flash.error("secure.error");
			params.flash();
			login();
		}
		session.put("userId", userId);
		// Redirect to the original URL (or /)
		
		if(!Account.exist(userId)) {
			new Account(userId, session.get("displayName"), thumbnail).save();
		}
		
		redirectToOriginalURL();
	}

	public static void logout() throws Throwable {
		Security.invoke("onDisconnect");
		session.clear();
		response.removeCookie("rememberme");
		Security.invoke("onDisconnected");
		flash.success("secure.logout");
		login();
	}

	static void redirectToOriginalURL() throws Throwable {
		Security.invoke("onAuthenticated");
		String url = flash.get("url");
		if(url == null) {
			url = "/";
		}
		redirect(url);
	}
	
	private static String authURL() {
		return play.mvc.Router.getFullUrl("OAuthSecure.login");
	}

	public static class Security extends Controller {

		/**
		 * This method is called during the authentication process. This is where you check if
		 * the user is allowed to log in into the system. This is the actual authentication process
		 * against a third party 	 (most of the time a DB).
		 *
		 * @param 
		 * @param password
		 * @return true if the authentication process succeeded
		 */
		static boolean authenticate(String email, String password) {
			return true;
		}

		/**
		 * This method checks that a profile is allowed to view this page/method. This method is called prior
		 * to the method's controller annotated with the @Check method. 
		 *
		 * @param profile
		 * @return true if you are allowed to execute this controller method.
		 */
		static boolean check(String profile) {
			return true;
		}

		/**
		 * This method returns the current connected 
		 * @return
		 */
		static String connected() {
			return session.get("userId");
		}
		
		static String displayName() {
			return session.get("displayName");
		}

		/**
		 * Indicate if a user is currently connected
		 * @return  true if the user is connected
		 */
		static boolean isConnected() {
			return session.contains("userId");
		}

		/**
		 * This method is called after a successful authentication.
		 * You need to override this method if you with to perform specific actions (eg. Record the time the user signed in)
		 */
		static void onAuthenticated() {
		}

		 /**
		 * This method is called before a user tries to sign off.
		 * You need to override this method if you wish to perform specific actions (eg. Record the name of the user who signed off)
		 */
		static void onDisconnect() {
		}

		 /**
		 * This method is called after a successful sign off.
		 * You need to override this method if you wish to perform specific actions (eg. Record the time the user signed off)
		 */
		static void onDisconnected() {
		}

		/**
		 * This method is called if a check does not succeed. By default it shows the not allowed page (the controller forbidden method).
		 * @param profile
		 */
		static void onCheckFailed(String profile) {
			forbidden();
		}

		private static Object invoke(String m, Object... args) throws Throwable {
			Class security = null;
			List<Class> classes = Play.classloader.getAssignableClasses(Security.class);
			if(classes.size() == 0) {
				security = Security.class;
			} else {
				security = classes.get(0);
			}
			try {
				return Java.invokeStaticOrParent(security, m, args);
			} catch(InvocationTargetException e) {
				throw e.getTargetException();
			}
		}
	}
}
