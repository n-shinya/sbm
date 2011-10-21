package controllers;

import helper.IndexViewHelper;
import helper.IndexViewHelper.IndexView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controllers.OAuthSecure.Security;

import models.Account;
import models.Bookmark;
import models.Freeword;
import models.Tag;
import models.Tag.Tagcloud;
import play.libs.OAuth2;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import auth.SalesForceOAuth2;
import auth.SalesForceOAuth2.Response;

@With(OAuthSecure.class)
public class Application extends Controller {

	@Before
	static void setConnectedUser() {
		renderArgs.put("displayName", Security.displayName());
	}
	
	public static void index(String userId, int page, String query) {
		if(page < 1) {
			page = 1;
		}
		if(userId == null) {
			userId = "all";
		}
		List<IndexView> indexView = IndexViewHelper.create(page, query, userId);
		long count = IndexViewHelper.count(query, userId);
		List<Account> users = Account.findAll();
		Tagcloud cloud = Tag.findByUserId(userId);
		render(indexView, count, page, query, userId, users, cloud);
	}
	
	public static void search(String userId, String query) {
		index(userId, 1, query);
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
		bookmark.account = Account.userId(Security.connected());
		bookmark.save();
		new Freeword(bookmark).save();
		redirect(url);
	}
	
	public static void delete(Long id) {
		Bookmark bookmark = Bookmark.findById(id);
		bookmark.delete();
		index(null, 1, null);
	}
}