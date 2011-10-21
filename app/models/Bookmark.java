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
	
	public static List<Bookmark> findByUserId(int page, String userId) {
		if(userId.equals("all")) {
			return find("order by date desc").from((page -1) * 10).fetch(10);
		} else {
			return find("select b from Bookmark b join b.account as u where u.userId=? order by b.date desc", userId)
			.from((page -1) * 10)
			.fetch(10);
		}
	}
	
	public static List<Bookmark> findByUserIdAndIds(int page, String userId, List<Long> ids) {
		if(ids.isEmpty()) {
			return new ArrayList<Bookmark>();
		}
		if(userId.equals("all")) {
			return find("select b from Bookmark b where b.id in (?1) order by date desc", ids)
			.from((page -1) * 10)
			.fetch(10);
		} else {
			return find("select b from Bookmark b join b.account as u where u.userId=? and b.id in (?2) order by date desc", userId, ids)
			.from((page -1) * 10)
			.fetch(10);			
		}
	}

	public static long countByUser(String userId) {
		if(userId.equals("all")) {
			return count();
		} else {
			return count("select count(*) from Bookmark b join b.account as u where u.userId=?", userId);
		}
	}
	
	public static long countByIdsAndUser(List<Long> ids, String userId) {
		if(userId.equals("all")) {
			return count("select count(*) from Bookmark b where b.id in (?1)", ids);
		} else {
			return count("select count(*) from Bookmark b join b.account as u where b.id in (?1) and u.userId=?2", ids, userId);
		}
	}
}