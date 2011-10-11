package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Account extends Model {

	public String name;
	
	public static Account findByName(String name) {
		return find("byName", name).first();
	}
}
