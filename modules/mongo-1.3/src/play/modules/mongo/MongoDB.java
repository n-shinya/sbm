package play.modules.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Play;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;


public class MongoDB {
	
    private static Mongo mongo;
    private static DB db;
    
    private static String host;
    private static Integer port;
    private static String dbname;

    /**
     * Obtain a reference to the mongo database.
     * 
     * @return - a reference to the Mongo database
     */
	public static DB db() {
		if (db==null){
			if(Play.configuration.containsKey("mongo.username") && Play.configuration.containsKey("mongo.password")){
				String username = Play.configuration.getProperty("mongo.username");
				String passwd = Play.configuration.getProperty("mongo.password");
				init(username, passwd);
			}
			else{
				init();
			}
		}
		
		return db;
	}
	
	/**
	 * Static initialiser.
	 * 
	 * @throws UnknownHostException
	 * @throws MongoException
	 */
	public static void init() {		
		
		if (host == null || port == null || dbname == null){
			host = Play.configuration.getProperty("mongo.host", "localhost");
			port = Integer.parseInt(Play.configuration.getProperty("mongo.port", "27017"));
			dbname = Play.configuration.getProperty("mongo.database", "play." + Play.configuration.getProperty("application.name"));
		}
		
		Logger.info("initializing DB ["+host+"]["+port+"]["+dbname+"]");
		
		try {
			mongo = new Mongo(host, port);
			db = mongo.getDB(dbname);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
	}
	
	public static void init(String username, String password){
		init();
		db.authenticate(username, password.toCharArray());
	}
	
	/**
	 * Creates an index.
	 * 
	 * @param collectionName
	 * @param indexString
	 */
	public static void index(String collectionName, String indexString){
		DBCollection c = db().getCollection(collectionName);
		DBObject indexKeys = createOrderDbObject(indexString);
		c.ensureIndex(indexKeys);
	}
	
	/**
	 * Removes an index. 
	 * 
	 * @param collectionName
	 * @param indexString
	 */
	public static void dropIndex(String collectionName, String indexString){
		DBCollection c = db().getCollection(collectionName);
		DBObject indexKeys = createOrderDbObject(indexString);
		c.dropIndex(indexKeys);
	}
	
	/** 
	 * Removes all indexes.
	 * 
	 * @param collectionName
	 */
	public static void dropIndexes(String collectionName){
		DBCollection c = db().getCollection(collectionName);
		c.dropIndexes();
	}
	
	/**
	 * Return a list of index names.
	 * 
	 * @param collectionName
	 * @return
	 */
	public static String[] getIndexes(String collectionName){
		List<String> indexNames = new ArrayList<String>();
		DBCollection c = db().getCollection(collectionName);
		List<DBObject> indexes = c.getIndexInfo();
		for (DBObject o : indexes){
			indexNames.add((String)o.get("name"));
		}
		
		return indexNames.toArray(new String[indexNames.size()]);
	}
	
	/**
	 * Adds a user to the database. We must manually set the readOnly parameter
	 * because the java mongo API does not yet support it. It will only work
	 * with database versions > 1.3.
	 * 
	 * @param username
	 * @param passwd
	 * @param readOnly
	 */
	public static void addUser(String username, String passwd, boolean readOnly){
		db().addUser(username, passwd.toCharArray());
		DBCollection c = db().getCollection("system.users");
		
		DBObject userObj = c.findOne(new BasicDBObject("user", username));
		if (userObj != null){
			userObj.put("readOnly", readOnly);
			c.save(userObj);
		}
	}
	
	/**
	 * Removes a user from the database.
	 * 
	 * @param username
	 */
	public static void removeUser(String username){
		DBCollection c = db().getCollection("system.users");
		
		DBObject userObj = c.findOne(new BasicDBObject("user", username));
		if (userObj != null){
			c.remove(userObj);
		}
	}
	
	/**
	 * Authenticates a user against a database.
	 * 
	 * @param username
	 * @param password
	 */
	public static boolean authenticate(String username, String password){
		return db().authenticate(username, password.toCharArray());
	}
	
	
	/**
	 * Counts the records in the collection.
	 * 
	 * @param collectionName
	 * @return - number of records in the collection
	 */
	public static long count(String collectionName){		
		return db().getCollection(collectionName).getCount();
	}
	
	/**
	 * Counts the records in the collection matching the query string.
	 * 
	 * @param collectionName - the queried collection
	 * @param query - the query string
	 * @param params - parameters for the query string
	 * @return
	 */
	public static long count(String collectionName, String query, Object[] params){
		return db().getCollection(collectionName).getCount(createQueryDbObject(query, params));
	}
	
	/**
	 * Provides a cursor to the objects in a collection, matching the query string.
	 * 
	 * @param collectionName - the target collection
	 * @param query - the query string
	 * @param params - parameters for the query
	 * @param clazz - the type of MongoModel
	 * @return - a mongo cursor
	 */
	@SuppressWarnings("rawtypes")
	public static MongoCursor find(String collectionName, String query, Object[] params, Class clazz){
		return new MongoCursor(db().getCollection(collectionName).find(createQueryDbObject(query, params)),clazz);
	}
	
	/**
	 * Provides a cursor to the objects in a collection.
	 * 
	 * 
	 * @param collectionName - the target collection
	 * @param clazz - the type of MongoModel
	 * @return - a mongo cursor
	 */
	@SuppressWarnings("rawtypes") 
	public static MongoCursor find(String collectionName, Class clazz){
		return new MongoCursor(db().getCollection(collectionName).find(),clazz);
	}
	
	/**
	 * Saves a model to its collection.
	 * @param <T> - the type of MongoModel to save
	 * @param collectionName - the collection to save it to
	 * @param model - the model to save
	 * @return - an instance of the model saved
	 */
	public static <T extends MongoModel> T save(String collectionName, T model){
		/* 
		 * Perhaps it would be better to immediately save the object to the database and assign its id. 
		 * 
		 */
		DBObject dbObject = new BasicDBObject(MongoMapper.convertValue(model, Map.class));
		
		if (model.get_id() == null){
			db().getCollection(collectionName).insert(dbObject);
			model.set_id((ObjectId)(dbObject.get("_id")));
		}
		else{
			dbObject.removeField("_id");
			db().getCollection(collectionName).update(new BasicDBObject("_id",model.get_id()), dbObject);
		}
		
		return model;
	}
	
	/**
	 * Deletes a model from a collection.
	 * 
	 * @param <T> - the type of model
	 * @param collectionName - the collection
	 * @param model - the model
	 */
	public static <T extends MongoModel> void delete (String collectionName, T model){
		DBObject dbObject = new BasicDBObject("_id", model.get_id());
		db().getCollection(collectionName).remove(dbObject);
	}
	
	/**
	 * Deletes models from a collection that match a specific query string
	 * 
	 * @param collectionName - the collection 
	 * @param query - the query string
	 * @param params - parameters for the query string
	 * @return - the number of models deleted
	 */
	public static long delete (String collectionName, String query, Object[] params){
		DBObject dbObject = createQueryDbObject(query, params);
		long deleteCount = db().getCollection(collectionName).getCount(dbObject);
		db().getCollection(collectionName).remove(dbObject);
		
		return deleteCount;
	}
	
	/**
	 * Deletes all models from the collection.
	 * 
	 * @param collectionName - the collection
	 * @return - the number of models deleted
	 */
	public static long deleteAll (String collectionName){
		long deleteCount = count(collectionName);
		db().getCollection(collectionName).drop();
		return deleteCount;
	}
	
	/**
	 * Creates a query object for use with other methods
	 * 
	 * @param query - the query string
	 * @param values - values for the query
	 * @return - a DBObject representing the query
	 */
	public static DBObject createQueryDbObject(String query, Object[] values){
		
		String keys = extractKeys(query);
		
    	DBObject object = new BasicDBObject(); 	
    	String [] keyList = keys.split(",");
    	
    	if (keyList.length > values.length){
    		throw new IllegalArgumentException("Not enough values for the keys provided");
    	}
    	
		for (int i = 0; i < keyList.length; i++){
			object.put(keyList[i].trim(), values[i]);
		}
    	
    	return object;
    }
	
	/**
	 * Creates an ordering object for use with other methods
	 * 
	 * @param query - the query string
	 * @param values - values for the query
	 * @return - a DBObject representing the ordering
	 */
	public static DBObject createOrderDbObject(String query){
		
		String keys = extractKeys(query);
		
    	DBObject object = new BasicDBObject(); 	
    	String [] keyList = keys.split(",");
    	
		for (int i = 0; i < keyList.length; i++){
			
			int value = 1;
			if (keyList[i].charAt(0) == '-'){
				value = -1;
				keyList[i] = keyList[i].substring(1);
			}
			
			object.put(keyList[i].trim(), value);
		}  
    	
    	return object;
    }
	
	/**
	 * Extracts parameter names from a query string
	 * 
	 * @param queryString - the query string
	 * @return - a comma seperated string of parameter names
	 */
	private static String extractKeys(String queryString){
		queryString = queryString.substring(2);
		List<String> keys = new ArrayList<String>();
        String[] parts = queryString.split("And");
        for (String part : parts){
        	if (part.charAt(0) == '-'){
        		keys.add((part.charAt(0) + "") + (part.charAt(1) + "").toLowerCase() + part.substring(2));
        	}
        	else{
        		keys.add((part.charAt(0) + "").toLowerCase() + part.substring(1));
        	}
        }
        return StringUtils.join(keys.toArray(), ",");
	}

} 
