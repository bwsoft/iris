package com.github.bwsoft.iris.sample;

import java.util.ArrayList;
import java.util.List;

import com.github.bwsoft.iris.message.Field;
import com.github.bwsoft.iris.message.Group;
import com.github.bwsoft.iris.message.GroupObject;
import com.github.bwsoft.iris.message.GroupObjectArray;
import com.github.bwsoft.iris.message.Message;

/**
 * A coding sample to demonstrate how to cache field definitions and group them together for better 
 * performance
 * 
 * @author yzhou
 *
 */
public class FuelFigures {
	
	private Group fuelFigures;
	private Field speed;
	private Field mpg;
	
	private List<FuelFigure> figures;
	
	public FuelFigures() {
		figures = new ArrayList<FuelFigures.FuelFigure>();
	}
	
	/**
	 * Obtain field definition of all fields in this group
	 * 
	 * @param message
	 */
	public void init(Message message) {
		if( 1 != message.getID() ) {
			// it is not Car message
			throw new IllegalArgumentException("not a Car message");
		}
		
		fuelFigures = (Group) message.getField("fuelFigures");
		speed = fuelFigures.getField("speed");
		mpg = fuelFigures.getField("mpg");
	}
	
	/**
	 * Obtain a list of Fuel figures from a message object. 
	 * 
	 * @param msgObj
	 * @return
	 */
	public List<FuelFigure> getFuelFigures(GroupObject msgObj) {
		figures.clear();
		GroupObjectArray fuelFiguresGroup = msgObj.getGroupArray(fuelFigures);
		for( int i = 0; i < fuelFiguresGroup.getNumOfGroups(); i ++ ) {
			GroupObject aFigureObj = fuelFiguresGroup.getGroupObject(i);
			FuelFigure aFigure = new FuelFigure();
			aFigure.speed = aFigureObj.getFloat(speed);
			aFigure.mpg = aFigureObj.getFloat(mpg);
			figures.add(aFigure);
		}
		return figures;
	}
	
	/**
	 * Add a fuel figure
	 * 
	 * @param msgObj
	 * @param aFigure
	 */
	public void addFuelFigure(GroupObject msgObj, FuelFigure aFigure) {
		GroupObjectArray fuelFiguresGroup = msgObj.getGroupArray(fuelFigures);
		GroupObject aNewFigure = fuelFiguresGroup.addGroupObject();
		aNewFigure.setNumber(speed, aFigure.speed);
		aNewFigure.setNumber(mpg, aFigure.mpg);
	}
	
	/**
	 * A row for fuel figure repeating group
	 * 
	 * @author yzhou
	 *
	 */
	public static class FuelFigure {
		public float speed;
		public float mpg;
	}
}
