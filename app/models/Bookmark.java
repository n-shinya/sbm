package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

@Entity
public class Bookmark extends Model {
	
	public String url;
	
	public String title;
	
	public String memo;
	
	public Date date;
	
	@OneToOne
	public User user;
	
	@OneToOne
	public Tag tag;
	
}
