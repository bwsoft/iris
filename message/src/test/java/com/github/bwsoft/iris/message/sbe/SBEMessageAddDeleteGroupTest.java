package com.github.bwsoft.iris.message.sbe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.BitSet;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.xml.sax.SAXException;

import com.github.bwsoft.iris.message.Field;
import com.github.bwsoft.iris.message.Group;
import com.github.bwsoft.iris.message.GroupObject;
import com.github.bwsoft.iris.message.GroupObjectArray;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.util.MessageUtil;

public class SBEMessageAddDeleteGroupTest {
	private final static ByteBuffer sbeBuffer = ByteBuffer.allocateDirect(4096);
	private static SBEMessageSchema schema;
	private static Group performanceFiguresDefinition;
	private static Group fuelFiguresDefinition;
	private static Group accelerationDefinition;
	private static Field mph;
	private static Field seconds;

	@Rule
	public TestRule watcher = new TestWatcher() {
		protected void starting(Description description) {
			System.out.format("\nStarting test: %s", description.getMethodName());
		}
	};

	@BeforeClass
	public static void initTest() throws JAXBException, SAXException, ParserConfigurationException, IOException {
		System.out.println("Description: Testing Add/Delete Group Rows");

		//  Create SBEMessageSchema
		schema = SBEMessageSchema.createSBESchema("src/test/resources/example-schema.xml");
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, sbeBuffer, 0);
		
		// Field: serialNumber - set up a number field of uint64
		msgObj.setNumber(msgObj.getField((short) 1), 1234567); 

		// Field: modelYear - set up a number field of unit16
		msgObj.setNumber(msgObj.getField((short) 2), 2016); 

		// Field: available - set up an enum of type uint8
		msgObj.setNumber(msgObj.getField((short) 3), 1); 

		// Field: code - set up an enum of type char.
		msgObj.setChar(msgObj.getField((short) 4),'C'); 

		// Field: somenumber - set up a number array of type int[5]
		Integer[] someNumbers = {1,2,10,4,5};
		msgObj.setNumbers(msgObj.getField((short) 5), someNumbers, 0, 5); 

		// Field: vehicleCode - set up a char array of char[6].
		char[] vehicleCode = {'m','y','c','o','d','e'}; 
		msgObj.setChars(msgObj.getField((short) 6), vehicleCode, 0, 6); 
		
		// Field: extra - set up a bitset of uint8
		BitSet bitSet = new BitSet();
		bitSet.set(0);
		bitSet.set(2);
		byte value = bitSet.toByteArray()[0];
		msgObj.setByte(msgObj.getField((short) 7), value);
		
		// Field: engine - set up a composite field. Ignore all constant fields.
		Group engine = (Group) msgObj.getField((short) 8);
		msgObj.setNumber(engine.getField((short) 0), 1500); // Field: capacity - the first field in the composite field 
		msgObj.setNumber(engine.getField((short) 1), 6); // Field: numCylinders - the second field in the composite
		// Jump to the 3rd field to skip the constant field. 
		char[] manufactureCode = {'V','T','I'};
		msgObj.setChars(engine.getField((short) 3), manufactureCode, 0, 3); // Field: manufactureCode - the third field in the composite.

		// Field: fuelFigures - set up a group
		fuelFiguresDefinition = (Group) msgObj.getField("fuelFigures");
		GroupObjectArray fuelFigures = msgObj.getGroupArray(fuelFiguresDefinition);
		GroupObject fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField((short) 10), 40); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField((short) 11), 25.5); // Field: mpg
		
		// Field: performanceFigures - set up a group
		performanceFiguresDefinition = (Group) msgObj.getField("performanceFigures");
		GroupObjectArray performanceFigures = msgObj.getGroupArray(performanceFiguresDefinition);
		GroupObject performanceFigure = performanceFigures.addGroupObject(); // add a row to the group
		performanceFigure.setNumber(performanceFigure.getField((short) 13), 89); // Field: octaneRating
		
		// Field: acclerations - set up a group within another group
		accelerationDefinition = (Group) performanceFiguresDefinition.getField("acceleration");
		GroupObjectArray acclerations = performanceFigure.getGroupArray(accelerationDefinition);
		GroupObject accleration = acclerations.addGroupObject();
		mph = accleration.getField("mph");
		accleration.setNumber(mph, 60);
		seconds = accleration.getField("seconds");
		accleration.setNumber(seconds, 2.5);
		
		// Field: fuelFigures - to add another row in the previous repeating group.
		// It is a good practice to add fields in sequence. But not necessary.
		fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField((short) 10), 60); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField((short) 11), 30); // Field: mpg
		
		// Field: model - set up a raw field. It is good to follow the order of a raw field. 
		// But not necessary.
		byte[] model = "Accord".getBytes();
		msgObj.setBytes(msgObj.getField((short) 18), model, 0, model.length);

		// Field: make - set up a raw field
		byte[] make = "Honda".getBytes();
		msgObj.setBytes(msgObj.getField((short) 17), make, 0, make.length);
		
		// Field: activationCode - set up a raw field
		byte[] activationCode = "deadbeef".getBytes();
		msgObj.setBytes(msgObj.getField((short) 19), activationCode, 0, activationCode.length);
	}
	
	@Test
	public void testAddDeleteGroupRows() {
		GroupObject msgObj = schema.wrapSbeBuffer(sbeBuffer, 0);
		
		// verify the initial creation of the performanceGroup is correct
		GroupObjectArray performanceFigures = msgObj.getGroupArray(performanceFiguresDefinition);
		Assert.assertEquals(1, performanceFigures.getNumOfGroups());
		
		// current acceleration
		GroupObjectArray accelerations = performanceFigures.getGroupObject(0).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// insert a new row ahead
		GroupObject newPerformance = performanceFigures.addGroupObject(0);
		Assert.assertEquals(2, performanceFigures.getNumOfGroups());
		
		// verify previous acceleration is not affected 
		accelerations = performanceFigures.getGroupObject(1).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// create an acceleration in the new performance
		accelerations = newPerformance.getGroupArray(accelerationDefinition);
		GroupObject acceleration = accelerations.addGroupObject(0);
		acceleration.setNumber(mph, 1);
		acceleration.setNumber(seconds, 4.6);
		Assert.assertEquals(1, accelerations.getGroupObject(0).getNumber(mph));
		
		// verify previous acceleration is not affected
		accelerations = performanceFigures.getGroupObject(1).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// add two more acceleration to the new performance
		accelerations = newPerformance.getGroupArray(accelerationDefinition);
		acceleration = accelerations.addGroupObject();
		acceleration.setNumber(mph, 3);
		acceleration.setNumber(seconds, 8.6);
		
		acceleration = accelerations.addGroupObject(1);
		acceleration.setNumber(mph, 2);
		acceleration.setNumber(seconds, 8.6);
		
		// verify previous acceleration is not affected
		accelerations = performanceFigures.getGroupObject(1).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// verify the three newly added acceleration as expected
		accelerations = performanceFigures.getGroupObject(0).getGroupArray(accelerationDefinition);
		Assert.assertEquals(3, accelerations.getNumOfGroups());
		Assert.assertEquals(1, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(4.6, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		Assert.assertEquals(2, accelerations.getGroupObject(1).getNumber(mph).intValue());
		Assert.assertEquals(8.6, accelerations.getGroupObject(1).getFloat(seconds),0.001);
		Assert.assertEquals(3, accelerations.getGroupObject(2).getNumber(mph).intValue());
		Assert.assertEquals(8.6, accelerations.getGroupObject(2).getFloat(seconds),0.001);
		
		// remove the second acceleration this is newly added
		accelerations.deleteGroupObject(1);

		// verify previous acceleration is not affected
		accelerations = performanceFigures.getGroupObject(1).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// re-add a new second acceleration
		accelerations = performanceFigures.getGroupObject(0).getGroupArray(accelerationDefinition);
		acceleration = accelerations.addGroupObject(1);
		acceleration.setNumber(mph, 2);
		acceleration.setNumber(seconds, 9.6);
		
		// verify the newly added acceleration as expected
		Assert.assertEquals(3, accelerations.getNumOfGroups());
		Assert.assertEquals(1, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(4.6, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		Assert.assertEquals(2, accelerations.getGroupObject(1).getNumber(mph).intValue());
		Assert.assertEquals(9.6, accelerations.getGroupObject(1).getFloat(seconds),0.001);
		Assert.assertEquals(3, accelerations.getGroupObject(2).getNumber(mph).intValue());
		Assert.assertEquals(8.6, accelerations.getGroupObject(2).getFloat(seconds),0.001);
		
		// verify no impact to the second row in performance figure
		accelerations = performanceFigures.getGroupObject(1).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// insert another performance figure in between
		newPerformance = performanceFigures.addGroupObject(1);
		accelerations = newPerformance.getGroupArray(accelerationDefinition);
		acceleration = accelerations.addGroupObject();
		acceleration.setNumber(mph, 11);
		acceleration.setNumber(seconds, 100.5);
		acceleration = accelerations.addGroupObject();
		acceleration.setNumber(mph, 12);
		acceleration.setNumber(seconds, 12.5);
		
		
		// verify no impact to the 3rd row in performance figure
		accelerations = performanceFigures.getGroupObject(2).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// delete the newly added performance figure
		performanceFigures.deleteGroupObject(1);

		// verify no impact to the 2nd row in performance figure
		accelerations = performanceFigures.getGroupObject(1).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		Assert.assertEquals(2, performanceFigures.getNumOfGroups());
		
		// re-add removed row
		newPerformance = performanceFigures.addGroupObject(1);
		accelerations = newPerformance.getGroupArray(accelerationDefinition);
		acceleration = accelerations.addGroupObject();
		acceleration.setNumber(mph, 11);
		acceleration.setNumber(seconds, 100.5);
		acceleration = accelerations.addGroupObject();
		acceleration.setNumber(mph, 12);
		acceleration.setNumber(seconds, 12.5);

		// verify no impact to the 3rd row in performance figure
		accelerations = performanceFigures.getGroupObject(2).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// verify the re-warpping give the same result
		String beforeWrapping = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(sbeBuffer, 0);
		Assert.assertEquals(beforeWrapping, MessageUtil.toJsonString(msgObj));
		System.out.println(" ...... passed");
	}
}
