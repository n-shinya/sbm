package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

@Entity
public class Bookmark extends Model {
	
	public String url;
	
	public String title;
	
	public String memo;
	
	public Date date;
	
	@ManyToOne
	public User user;
	
	@OneToOne
	public Tag tag;
	
	public static List<Bookmark> findByUsername(int page, String username) {
		if(username.equals("all")) {
			return find("order by date desc").from((page -1) * 10).fetch(10);
		} else {
			return find("select b from Bookmark b join b.user as u where u.name=? order by b.date desc", username)
			.from((page -1) * 10)
			.fetch(10);
		}
	}
	
	public static List<Bookmark> findByUsernameAndTitle(int page, String username, String title) {
		if(username.equals("all")) {
			return find("select b from Bookmark b where b.title like ? order by date desc", "%" + title + "%")
			.from((page -1) * 10)
			.fetch(10);
		} else {
			return find("select b from Bookmark b join b.user as u where u.name=? and b.title like ?  order by date desc", username, "%" + title + "%")
			.from((page -1) * 10)
			.fetch(10);			
		}
	}
	
	public static long countByQuery(String q, String username) {
		if(username.equals("all")) {
			return q == null ?
					count()
					: count("byTitleElike", "%" + q + "%");
		} else {
			return q == null ?
					count("select count(*) from Bookmark b join b.user as u where u.name=?", username)
					: count("select count(*) from Bookmark b join b.user as u where b.title like ? and u.name=?", "%" + q + "%", username);
		}
	}	
}