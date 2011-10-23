package models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Tag extends Model{

	public String name;
	
	public static Tagcloud findByUserId(String userId) {
		List<Object> tags;
		if(userId.equals("all")) {
			tags =
				find("select count(*), t.name from Tag t group by t.name")
				.fetch();
		} else {
			tags = 
				find("select count(*), t.name from Tag t, Bookmark b where t = b.tag and b.account.userId=? group by t.name" , userId)
				.fetch();
		}
		return toTagcloud(tags);
	}
	
	private static Tagcloud toTagcloud(List<Object> tags) {
		Tagcloud tagcloud = new Tagcloud();
		tagcloud.tagMap = new HashMap();
		long allCount = tags.size();
		for(Object tag : tags) {
			Object[] property = (Object[])tag;
			tagcloud.tagMap.put((String)property[1],calculateRank(allCount, (Long)property[0]));
		}
		return tagcloud;
	}
	
	public static long calculateRank(long allCount, long count) {
		double percentage = ((double)count / (double)allCount) * 100;
		if(percentage > 20) {
			return 5;
		}else if(percentage > 15) {
			return 4;
		}else if(percentage > 12) {
			return 3;
		}else if(percentage > 9) {
			return 2;
		}else {
			return 1;
		}
	}
	
	public static class Tagcloud {
		public Map<String, Long> tagMap;
	}
}
