import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import models.Car;
import models.Driver;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class MongoModelTest extends UnitTest {

	@Before
	public void init(){
		Car.deleteAll();
	}
	
    @Test
    public void save(){
    	long before = Car.count();
    	
    	Car myCar = new Car("Toyota","white",150);
    	
    	myCar.save();
    	
    	assertEquals(before+1, Car.count());
    }
    
    @Test
    public void find(){
    	
    	for (int i = 1; i <= 100; i++){
    		new Car("Toyota v"+i,"white",150+i).save();
    	}
    	
    	//get all cars
    	List<Car> allCars = Car.find().fetch();
    	assertEquals(100, allCars.size());

    	//get any five cars
    	List<Car> fiveCars = Car.find().fetch(5);
    	assertEquals(5, fiveCars.size());

    	//get any two cars with offset of 3
    	List<Car> offsetCars = Car.find().from(3).fetch(2);
    	assertEquals(2, offsetCars.size());
    	assertEquals("Toyota v4", offsetCars.get(0).name);
    	assertEquals("Toyota v5", offsetCars.get(1).name);

    	//get the third page of 20 cars
    	List<Car> pageOfCars = Car.find().fetch(3,20);
    	assertEquals(20, pageOfCars.size());
    	assertEquals("Toyota v41", pageOfCars.get(0).name);

    	//get only one car
    	Car c = Car.find().first();
    	assertEquals("Toyota v1", c.name);
    }
    
    @Test
    public void query(){
    	new Car("Toyota", "white", 150).save();
    	new Car("Toyota", "red", 150).save();
    	new Car("Toyota", "green", 150).save();
    	new Car("Toyota", "white", 160).save();
    	new Car("Holden", "blue", 150).save();
    	
    	List<Car> toyotas = Car.find("byName", "Toyota").fetch();
    	assertEquals(4, toyotas.size());

    	List<Car> whiteToyotas = Car.find("byNameAndColour", "Toyota", "white").fetch();
    	assertEquals(2, whiteToyotas.size());
    }
    
    @Test
    public void ordering(){
    	new Car("AToyota", "white", 150).save();
    	new Car("BToyota", "red", 150).save();
    	new Car("YToyota", "green", 150).save();
    	new Car("ZToyota", "white", 160).save();
    	new Car("Holden", "blue", 150).save();
    	
    	List<Car> ascendingNameCars = Car.find().order("byName").fetch();
    	assertEquals("AToyota", ascendingNameCars.get(0).name);
    	assertEquals("BToyota", ascendingNameCars.get(1).name);

    	// to perform a descending order, prefix the field with '-'
    	List<Car> descendingNameCars = Car.find().order("by-Name").fetch();
    	assertEquals("ZToyota", descendingNameCars.get(0).name);
    	assertEquals("YToyota", descendingNameCars.get(1).name);
    }
    
    @Test
    public void count(){
    	new Car("Toyota", "white", 150).save();
    	new Car("Toyota", "red", 150).save();
    	new Car("Toyota", "green", 150).save();
    	new Car("Toyota", "white", 160).save();
    	new Car("Holden", "blue", 150).save();
    	
    	assertEquals(5, Car.count());

    	// alternatively, pass a query string
    	assertEquals(4, Car.count("byName", "Toyota"));
    }
    
    @Test
    public void delete(){
    	new Car("Toyota", "white", 150).save();
    	new Car("Toyota", "red", 150).save();
    	new Car("Toyota", "green", 150).save();
    	new Car("Toyota", "white", 160).save();
    	new Car("Holden", "blue", 150).save();
    	new Car("Holden", "black", 150).save();
    	
    	assertEquals(6, Car.count());
    	
    	Car c = Car.find().first();
    	c.delete();
    	assertEquals(5, Car.count());
    	
    	// or to delete using a query string
    	Car.delete("byName","Toyota");
    	assertEquals(2, Car.count());

    	// or just delete everything
    	Car.deleteAll();
    	assertEquals(0, Car.count());
    }
    
    @Test
    public void innerModel() throws Exception {
    	DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    	
    	Car car = new Car("Toyota", "white", 150);
    	car.driver = new Driver("Andrew", df.parse("02/02/1973"));
    	car.save();
    	
    	car = new Car("Holden", "Red", 150);
    	car.driver = new Driver("Sam", df.parse("03/03/1975"));
    	car.save();
    	
    	Car andrewCar = Car.find("byDriver.name","Andrew").first();
    	assertEquals("Andrew", andrewCar.driver.name);
    	assertEquals("02/02/1973", df.format(andrewCar.driver.dob));
    }

    @Test
    public void mongoInformation(){
    	Car myCar = new Car("Toyota", "white", 150);
    	myCar.save();

    	ObjectId id = myCar.get_id();
    	String colName = myCar.getCollectionName();
    	
    	assertNotNull(id);
    	assertEquals("car", colName);
    }
    
    @Test
    public void mongoIndexes(){
    	Car myCar = new Car("Toyota", "white", 150);
    	myCar.save();
    	
    	// index the name field of Car
    	Car.index("onName");

    	// create a descending index by prepending the '-' character
    	Car.index("on-Name");

    	// create a composite index by combining field names
    	Car.index("onNameAndColour");
    	
    	assertEquals(4, Car.getIndexes().length);
    	
    	// remove an index
    	Car.dropIndex("onName");
    	
    	assertEquals(3, Car.getIndexes().length);
    }
}
