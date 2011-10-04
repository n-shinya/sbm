package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class User extends Model {

	public String name;
	
	public static User findByName(String username) {
		return find("byName", username).first();
	}
}
