package models;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import play.modules.mongo.MongoDB;
import play.modules.mongo.MongoEntity;
import play.modules.mongo.MongoModel;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@MongoEntity
public class Freeword extends MongoModel{
	
	public Long bookmarkId;
	
	public String terms;
	
	public Freeword(Bookmark bookmark) {
		this.bookmarkId = bookmark.id;
		StringBuilder sb = new StringBuilder();
		if(bookmark.title != null) {
			sb.append(bookmark.title.toLowerCase());
			sb.append(' ');
		}
		if(bookmark.tag.name != null) {
			sb.append(bookmark.tag.name.toLowerCase());
			sb.append(' ');
		}
		if(bookmark.memo != null) {
			sb.append(bookmark.memo.toLowerCase());
		}
		this.terms = sb.toString();
	}
	
	public static List<Long> findByTerms(String terms) {
		terms = terms.toLowerCase();
		DBCollection coll = MongoDB.db().getCollection("freeword");
		Pattern pattern = Pattern.compile(".*" + terms + ".*");
		DBCursor cursor = coll.find(new BasicDBObject("terms", pattern));
		List<Long> idList = new ArrayList<Long>();
		while(cursor.hasNext()) {
			DBObject dbobj = cursor.next();
			Long id = (Long)dbobj.get("bookmarkId");
			idList.add(id);
		}
		return idList;
	}
}
