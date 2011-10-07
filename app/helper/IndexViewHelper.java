package helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Bookmark;

public class IndexViewHelper {
	
	private static String format(Date date) {
		return new SimpleDateFormat("yyyy年MM月dd日").format(date);
	}
	
	public static List<IndexView> create(int page, String q, String username) {
		
		if(username.equals("all")) {
			
		}
	
		List<Bookmark> bookmarks = q == null ?
					Bookmark.findByUsername(page, username)
					: Bookmark.findByUsernameAndTitle(page, username, q);
		
		List<IndexView> list = new ArrayList<IndexView>();
		for(Bookmark bookmark : bookmarks) {
			IndexView view = new IndexView();
			view.bookmark = bookmark;
			view.registerDate = format(bookmark.date);
			list.add(view);
		}
		return list;
	}
	
	public static class IndexView {
		public Bookmark bookmark;
		public String registerDate;
	}
}
