/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package com.github.bwsoft.iris.sample;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

import com.github.bwsoft.iris.message.Group;
import com.github.bwsoft.iris.message.GroupObject;
import com.github.bwsoft.iris.message.GroupObjectArray;
import com.github.bwsoft.iris.message.Message;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.message.sbe.SBEMessage;
import com.github.bwsoft.iris.sample.FuelFigures.FuelFigure;
import com.github.bwsoft.iris.util.MessageUtil;

/**
 * Sample code to create and parse SBE messages based upon the example-schema.xml in 
 * the resources.
 * 
 * @author yzhou
 *
 */
public class Car {
	
	private static final SBEMessageSchema schema;
	
	static {
		SBEMessageSchema.configSafeMode(false);
		SBEMessageSchema toBeCreated = null;
		try {
			toBeCreated = SBEMessageSchema.createSBESchema("src/main/resources/example-schema.xml");
		} catch (Exception e) {
			e.printStackTrace();
		} 
		schema = toBeCreated;
	}

	private enum BooleanType {
		FALSE(0),
		TRUE(1);
		
		private int value;
		BooleanType(int value) {
			this.value = value;
		}
		
		public int value() {
			return this.value;
		}
	}
	
	private enum Model {
		A('A'),
		B('B'),
		C('C');
		
		private char value;
		Model(char value) {
			this.value = value;
		}
		
		public char value() {
			return this.value;
		}
	}
	
	public Car() {
		
	}
	
	/**
	 * Encode a SBE message based upon the src/main/resources/example-schema.xml.
	 * 
	 * @param buffer
	 * @param offset
	 * @return the length of the encoded message
	 */
	public static int encode(ByteBuffer buffer, int offset) {
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, buffer, offset);
		System.out.println("Empty message: "+MessageUtil.toJsonString(msgObj));
		
		// Field: serialNumber - set up a number field of uint64
		msgObj.setNumber(msgObj.getField("serialNumber"), 1234567); 

		// Field: modelYear - set up a number field of unit16
		msgObj.setNumber(msgObj.getField("modelYear"), 2016); 

		// Field: available - set up an enum of type uint8
		msgObj.setNumber(msgObj.getField("available"), BooleanType.TRUE.value()); 

		// Field: code - set up an enum of type char.
		msgObj.setChar(msgObj.getField("code"),Model.C.value()); 

		// Field: somenumber - set up a number array of type int[5]
		Integer[] someNumbers = {1,2,10,4,5};
		msgObj.setNumbers(msgObj.getField("someNumbers"), someNumbers, 0, 5); 

		// Field: vehicleCode - set up a char array of char[6].
		char[] vehicleCode = {'m','y','c','o','d','e'}; 
		msgObj.setChars(msgObj.getField("vehicleCode"), vehicleCode, 0, 6); 
		
		// Field: extra - set up a bitset of uint8
		BitSet bitSet = new BitSet();
		bitSet.set(0);
		bitSet.set(2);
		byte value = bitSet.toByteArray()[0];
		msgObj.setByte(msgObj.getField("extras"), value);
		
		// Field: engine - set up a composite field. Ignore all constant fields.
		Group engine = (Group) msgObj.getField((short) 8);
		msgObj.setNumber(engine.getField("capacity"), 1500); // Field: capacity - the first field in the composite field 
		msgObj.setNumber(engine.getField("numCylinders"), 6); // Field: numCylinders - the second field in the composite
		// Jump to the 3rd field to skip the constant field. 
		char[] manufactureCode = {'V','T','I'};
		msgObj.setChars(engine.getField("manufacturerCode"), manufactureCode, 0, 3); // Field: manufactureCode - the third field in the composite.

		// Field: fuelFigures - set up a group
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField("fuelFigures"));
		GroupObject fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField("speed"), 40); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField("mpg"), 25.5); // Field: mpg
		
		// Field: performanceFigures - set up a group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures"));
		GroupObject performanceFigure = performanceFigures.addGroupObject(); // add a row to the group
		performanceFigure.setNumber(performanceFigure.getField("octaneRating"), 89); // Field: octaneRating
		
		// Field: acclerations - set up a group within another group
		GroupObjectArray acclerations = performanceFigure.getGroupArray(performanceFigure.getField("acceleration"));
		GroupObject accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField("mph"), 60);
		accleration.setNumber(accleration.getField("seconds"), 2.5);
		
		// Field: fuelFigures - to add another row in the previous repeating group.
		// It is a good practice to add fields in sequence. But not necessary.
		fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField("speed"), 60); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField("mpg"), 30); // Field: mpg
		
		// Field: model - set up a raw field. It is good to follow the order of a raw field. 
		// But not necessary.
		byte[] model = "Accord".getBytes();
		msgObj.setBytes(msgObj.getField("model"), model, 0, model.length);

		// Field: make - set up a raw field
		byte[] make = "Honda".getBytes();
		msgObj.setBytes(msgObj.getField("make"), make, 0, make.length);
		
		// Field: activationCode - set up a raw field
		byte[] activationCode = "deadbeef".getBytes();
		msgObj.setBytes(msgObj.getField("activationCode"), activationCode, 0, activationCode.length);
		
		// print out the message
		System.out.println("Created message:"+MessageUtil.toJsonString(msgObj));
		
		// return the total size of the message
		return ((SBEMessage) msgObj.getDefinition()).getHeader().getSize() + msgObj.getSize();
	}
	
	/**
	 * Decode a SBE message
	 * 
	 * @param buffer
	 * @param offset
	 */
	public static void decode(ByteBuffer buffer, int offset) {
		// wrap the SBE message
		GroupObject msgObj = schema.wrapSbeBuffer(buffer, offset);
		System.out.println("The message template Id: "+msgObj.getDefinition().getID());
		System.out.println("The message is: "+MessageUtil.toJsonString(msgObj));
		
		// Field: modelYear - get a number field of unit16
		System.out.println("Field: modelYear, Value: " + msgObj.getNumber(msgObj.getField((short) 2))); 

		// Field: available - get an enum of type uint8
		// Option 1: to get its value
		Number availableValue = msgObj.getNumber(msgObj.getField("available")); 
		// Option 2: to get its name, but slower in performance due to a Hashmap lookup.
		String availableEnumName = msgObj.getEnumName(msgObj.getField("available")); 
		System.out.println("Field: available, Enum Value: "+availableValue.shortValue()+", Enum Name: "+availableEnumName);
	
		// Field: someNumbers - get a number array
		Integer[] someNumbers = new Integer[5];
		msgObj.getNumbers(msgObj.getField("someNumbers"), someNumbers, 0, 5);
		System.out.print("Field: someNumbers, Value: ");
		for( int i = 0; i < someNumbers.length; i ++ ) {
			System.out.format("%d ", someNumbers[i]);
		}
		System.out.println();
		
		// Field: extra - get a bit choice field
		// Option 1: to get its value
		Number extraValue = msgObj.getNumber(msgObj.getField("extras"));
		// Option 2: to get the bit set of all options (slower)
		boolean sunRoof = msgObj.isSet(msgObj.getField("extras"), "sunRoof");
		boolean sportsPack = msgObj.isSet(msgObj.getField("extras"), "sportsPack");
		boolean cruiseControl = msgObj.isSet(msgObj.getField("extras"), "cruiseControl");
		System.out.println("Field: extra, Value: "+extraValue.intValue()+", sunRoof? "+sunRoof+", sportsPack? "+sportsPack+" ,cruiseControl? "+cruiseControl);

		// Field: engine - get a composite field
		Group engine = (Group) msgObj.getField("engine");
		Number capacity = msgObj.getNumber(engine.getField((short) 0)); // Field: capacity - the first field in the composite field 
		Number numCylinders = msgObj.getNumber(engine.getField((short) 1)); // Field: numCylinders - the second field in the composite
		String maxRpm = msgObj.getString(engine.getField((short) 2)); // Field: maxRpm, a constant field. 
		char[] manufactureCode = new char[128];
		msgObj.getChars(engine.getField((short) 3), manufactureCode, 0, 128); // Field: manufactureCode - the third field in the composite.
		String fuel = msgObj.getString(engine.getField((short) 4));
		System.out.println("Field: engine, Value: capacity="+capacity.shortValue()
		+", numCylinders="+numCylinders.shortValue()
		+", maxRpm="+maxRpm
		+",manufactureCode="+new String(manufactureCode).trim()
		+",fuel="+fuel);
		
		// Field: model - get a raw field
		byte[] model = new byte[10];
		msgObj.getBytes(msgObj.getField("model"), model, 0, model.length);
		System.out.println("Field: model, Value: "+new String(model));
		
		// Field: performanceFigures - get a repeating group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures"));
		GroupObject performanceFigure = performanceFigures.getGroupObject(0); // get the first row
		Number octaneRating = performanceFigure.getNumber(performanceFigure.getField("octaneRating"));
		System.out.println("Field: performanceFigures[0].octaneRating, Value: "+octaneRating.intValue());

		// Field: acclerations - a nested repeating group
		GroupObjectArray acclerations = performanceFigure.getGroupArray(performanceFigure.getField("acceleration"));
		GroupObject accleration = acclerations.getGroupObject(0);
		Number mph = accleration.getNumber(accleration.getField("mph"));
		Number seconds = accleration.getNumber(accleration.getField("seconds"));
		System.out.println("Field: performanceFigures[0].accleration[0], Value: mph="+mph.intValue()+", seconds="+seconds.floatValue());
	}
	
	/**
	 * Modify a SBE message
	 * 
	 * @param buffer
	 * @param offset
	 */
	public static void modify(ByteBuffer buffer, int offset) {
		// wrap the SBE message
		GroupObject msgObj = schema.wrapSbeBuffer(buffer, offset);
		System.out.println("The message template Id: "+msgObj.getDefinition().getID());
		System.out.println("The message is:         "+MessageUtil.toJsonString(msgObj));
		
		// Field: modelYear - to update to 2018
		msgObj.setNumber(msgObj.getField((short) 2), 2018); 
		
		// Field: performanceFigures - to add a row
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12));
		GroupObject performanceFigure = performanceFigures.addGroupObject(); // add a row to the group
		performanceFigure.setNumber(performanceFigure.getField((short) 13), 90); // Field: octaneRating

		GroupObjectArray acclerations = performanceFigure.getGroupArray(performanceFigure.getField((short) 14));
		GroupObject accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField((short) 15), 60);
		accleration.setNumber(accleration.getField((short) 16), 2.5);

		// Field: performanceFigures row 1 - to add another accleration
		performanceFigure = performanceFigures.getGroupObject(0); 
		acclerations = performanceFigure.getGroupArray(performanceFigure.getField((short) 14));
		accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField((short) 15), 90);
		accleration.setNumber(accleration.getField((short) 16), 3.5);
		
		// Field: fuelFigures - to remove the first row
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		fuelFigures.deleteGroupObject(0);
		
		// Field: activationCode - to remove the raw field
		msgObj.setBytes(msgObj.getField((short) 19), null, 0, 0);

		System.out.println("The altered message is: "+MessageUtil.toJsonString(msgObj));
	}
	
	/**
	 * This demonstrates on how to cache the field definitions for better performance. See 
	 * {@link FuelFigures} for the code structure.
	 *  
	 * @param sbeBuffer
	 * @param offset
	 */
	public static void demonstrateOfBetterCoding(ByteBuffer sbeBuffer, int offset) {
		// obtain the message definition from the schema
		Message sbeMessage = schema.getMsgLookup().get(1);
		
		// create and init FuelFigures
		FuelFigures fuelFigures = new FuelFigures();
		fuelFigures.init(sbeMessage);
		
		// Obtain the GroupObject from the sbeBuffer
		GroupObject msgObj = schema.wrapSbeBuffer(sbeBuffer, offset);
		if( null != msgObj && 1 == msgObj.getDefinition().getID() ) {
			// Use FuelFigures to obtain the fuel figures from Car message
			List<FuelFigure> figures = fuelFigures.getFuelFigures(msgObj);
			for( FuelFigure figure : figures ) {
				System.out.format("Fuel figure: speed=%d mpg=%f\n", figure.speed, figure.mpg);
			}
		}
	}
	
	public static void main(String[] args) {
		// a direct buffer to hold SBE message
		ByteBuffer buffer = ByteBuffer.allocateDirect(1028);
		
		// populate buffer with a sbe message
		System.out.println("Demonstrate on how to encode a message");
		int msgLength = Car.encode(buffer, 0);
		System.out.println();
		
		// the created message can be copied. We will show how to use byte array
		// To avoid create garbage, it is better to obtain byte array of a ByteBuffer
		ByteBuffer dupBuffer = ByteBuffer.allocate(1028);
		MessageUtil.messageCopy(buffer, 0, 0, dupBuffer.array(), 0, schema);
		
		// parse message
		System.out.println("Demonstrate decoder by decoding various types of fields");
		Car.decode(dupBuffer, 0);
		System.out.println();
		
		// modify message
		System.out.println("Demonstrate on how to alter a message by adding/removing rows from repeating groups");
		Car.modify(buffer, 0);
		System.out.println();
		
		// use a better structure that improves the performance and the code structure 
		System.out.println("Demonstrate better coding using FuelFigures to list rows in the repeating group");
		System.out.println("Fuel figures in the original encoded message: ");
		Car.demonstrateOfBetterCoding(dupBuffer, 0);
		System.out.println();
		
		System.out.println("Fuel figures in the modified message: ");
		Car.demonstrateOfBetterCoding(buffer, 0);
	}
}
