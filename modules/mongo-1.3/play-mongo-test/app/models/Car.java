package models;

import play.modules.mongo.MongoEntity;
import play.modules.mongo.MongoModel;

@MongoEntity
public class Car extends MongoModel {
	public String name;
	public String colour;
	public int topSpeed;
	
	public Driver driver;
	
	public Car(){
		
	}
	
	public Car(String name, String colour, int topSpeed) {
		this.name = name;
		this.colour = colour;
		this.topSpeed = topSpeed;
	}
	
	
}
