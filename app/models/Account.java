package models;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

import auth.SalesForceOAuth2.Response;

import com.google.gson.JsonObject;

import controllers.OAuthSecure;

import play.db.jpa.Model;
import play.libs.OAuth2;
import play.libs.WS;
import play.libs.WS.HttpResponse;

@Entity
public class Account extends Model {

	public String userId;
	
	public String displayName;
	
	public String thumbnail;
	
	public static Account userId(String userId) {
		return find("byUserId", userId).first();
	}
	
	public static boolean exist(String userId) {
		return count("byUserId", userId) > 0;
	}
	
	public Account(String userId, String displayName, String thumbnail) {
		this.userId = userId;
		this.displayName = displayName;
		this.thumbnail = thumbnail;
	}	
}
