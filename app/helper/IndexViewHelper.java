package helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Bookmark;

public class IndexViewHelper {
	
	private static String formatRegisterDate(Date date) {
		return new SimpleDateFormat("yyyy年MM月dd日").format(date);
	}
	
	public static List<IndexView> create(int pagenum) {
		List<Bookmark> bookmarks = Bookmark.all().from((pagenum -1) * 10).fetch(10);
		List<IndexView> list = new ArrayList<IndexView>();
		for(Bookmark bookmark : bookmarks) {
			IndexView view = new IndexView();
			view.bookmark = bookmark;
			view.registerDate = formatRegisterDate(bookmark.date);
			list.add(view);
		}
		return list;
	}

	public static class IndexView {
		public Bookmark bookmark;
		public String registerDate;		
	}
}
