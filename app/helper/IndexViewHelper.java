package helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import models.Bookmark;
import models.Freeword;
import models.Tag;

public class IndexViewHelper {
	
	private static String format(Date date) {
		return new SimpleDateFormat("yyyy年MM月dd日").format(date);
	}
	
	public static List<IndexView> create(int page, String q, String userId) {
		
		List<Bookmark> bookmarks;
		if(q == null || q.equals("")) {
			bookmarks = Bookmark.findByUserId(page, userId);
		} else {
			bookmarks = Bookmark.findByUserIdAndIds(page, userId, Freeword.findByTerms(q));
		}
		List<IndexView> list = new ArrayList<IndexView>();
		for(Bookmark bookmark : bookmarks) {
			IndexView view = new IndexView();
			view.bookmark = bookmark;
			view.registerDate = format(bookmark.date);
			list.add(view);
		}
		return list;
	}
	
	public static long count(String q, String userId) {
		if(q == null || q.equals("")) {
			return Bookmark.countByUser(userId);
		} else {
			List<Long> ids = Freeword.findByTerms(q);
			if(ids.isEmpty()) {
				return 0L;
			} else {
				return Bookmark.countByIdsAndUser(ids, userId);
			}
		}
	}
	
	public static class IndexView {
		public Bookmark bookmark;
		public String registerDate;
	}
}
