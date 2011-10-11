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
		this.terms = bookmark.title + " " + bookmark.tag.name + " " + bookmark.memo;
	}
	
	public static List<Long> findByTerms(String terms) {
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
