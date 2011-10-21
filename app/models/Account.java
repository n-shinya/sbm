package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

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
