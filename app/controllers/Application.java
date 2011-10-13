package controllers;

import helper.IndexViewHelper;
import helper.IndexViewHelper.IndexView;

import java.util.Date;
import java.util.List;

import models.Account;
import models.Bookmark;
import models.Freeword;
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
	
	public static void index(int page, String q, String username) {
		if(page < 1) {
			page = 1;
		}
		if(username == null) {
			username = "all";
		}
		List<IndexView> indexView = IndexViewHelper.create(page, q, username);
		long count = IndexViewHelper.count(q, username);
		List<Account> users = Account.findAll();
		render(indexView, count, page, q, username, users);
	}
	
	public static void search(String q, String username) {
		index(1, q, username);
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
		bookmark.account = Account.findByName("n-shinya");
		bookmark.save();
		new Freeword(bookmark).save();
		redirect(url);
	}
	
	public static void delete(Long id) {
		Bookmark bookmark = Bookmark.findById(id);
		bookmark.delete();
		index(1, null, null);
	}	
}