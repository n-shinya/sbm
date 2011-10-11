package models;

import java.util.ArrayList;
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
	public Account account;
	
	@OneToOne
	public Tag tag;
	
	public static List<Bookmark> findByUsername(int page, String name) {
		if(name.equals("all")) {
			return find("order by date desc").from((page -1) * 10).fetch(10);
		} else {
			return find("select b from Bookmark b join b.account as u where u.name=? order by b.date desc", name)
			.from((page -1) * 10)
			.fetch(10);
		}
	}
	
	public static List<Bookmark> findByUsernameAndIds(int page, String name, List<Long> ids) {
		if(ids.isEmpty()) {
			return new ArrayList<Bookmark>();
		}
		if(name.equals("all")) {
			return find("select b from Bookmark b where b.id in (?1) order by date desc", ids)
			.from((page -1) * 10)
			.fetch(10);
		} else {
			return find("select b from Bookmark b join b.account as u where u.name=? and b.id in (?2) order by date desc", name, ids)
			.from((page -1) * 10)
			.fetch(10);			
		}
	}

	public static long countByUser(String name) {
		if(name.equals("all")) {
			return count();
		} else {
			return count("select count(*) from Bookmark b join b.account as u where u.name=?", name);
		}
	}
	
	public static long countByIdsAndUser(List<Long> ids, String name) {
		if(name.equals("all")) {
			return count("select count(*) from Bookmark b where b.id in (?1)", ids);
		} else {
			return count("select count(*) from Bookmark b join b.account as u where b.id in (?1) and u.name=?2", ids, name);
		}
	}
}