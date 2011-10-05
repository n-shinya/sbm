package controllers;

import helper.IndexViewHelper;
import helper.IndexViewHelper.IndexView;

import java.util.Date;
import java.util.List;

import models.Bookmark;
import models.Tag;
import play.mvc.Before;
import play.mvc.Controller;

public class Application extends Controller {

	@Before
	static void setConnectedUser() {
		//TODO
		renderArgs.put("user", "nishinaka_s");
		renderArgs.put("userImage", "n-shinya");
	}
	
	public static void index() {
		List<IndexView> indexView = IndexViewHelper.create(1);
		long count = Bookmark.count();
		render(indexView, count);
	}
	
	public static void clip(String url, String title) {
		render(url, title);
	}
	
	public static void register(String url, String title, String memo, String tagname) {
		Tag tag = new Tag();
		tag.name = tagname;
		tag.save();
		Bookmark bookmark = new Bookmark();
		bookmark.url = url;
		bookmark.title = title;
		bookmark.memo = memo;
		bookmark.date = new Date();
		bookmark.tag = tag;
		bookmark.save();
		index();
	}
	
	public static void delete(Long id) {
		Bookmark bookmark = Bookmark.findById(id);
		bookmark.delete();
		index();
	}
	
	
}