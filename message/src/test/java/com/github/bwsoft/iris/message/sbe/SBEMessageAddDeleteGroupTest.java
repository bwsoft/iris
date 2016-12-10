package com.github.bwsoft.iris.message.sbe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.BitSet;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

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

/**
 * Following scenarios are being tested:
 *   1.) Insert a new row at the beginning of a non-empty repeating group
 *   2.) Populate the nested repeating group in the newly created row
 *   3.) Add multiple rows to a repeating group using various addGroupObject methods
 *   4.) Remove a middle row from a repeating group
 *   5.) Insert a row to a repeating group that contains nested repeating sub group
 *   6.) Delete a middle row from a repeating group that contains nested repeating sub group
 *   7.) Insert a row into a repeating group that contains a variable length data field
 *   8.) Remove the first row from a repeating group that contains a variable length data field 
 *   and the varibale length data field is not null.
 *   9.) Rewrap the whole message to ensure the decoder is correct
 *   
 * @author yzhou
 *
 */
public class SBEMessageAddDeleteGroupTest {
	private final static ByteBuffer sbeBuffer = ByteBuffer.allocateDirect(4096);
	private static SBEMessageSchema schema;

	private static Group performanceFiguresDefinition;
	private static Group accelerationDefinition;
	private static Field mph;
	private static Field seconds;

	private static Group fuelFiguresDefinition;
	private static Field usageDescriptionDefinition;

	@Rule
	public TestRule watcher = new TestWatcher() {
		@Override
		protected void starting(Description description) {
			System.out.format("\nCase: %s ...... started\n", description.getMethodName());
		}
		
		@Override
		protected void succeeded(Description description) {
			System.out.format("Case: %s ...... passed\n", description.getMethodName());
		}

		@Override
		protected void failed(Throwable e, Description description) {
			System.out.format("Case: %s ...... failed!!!!!!\n", description.getMethodName());
		}
	};

	/**
	 * Build a message based upon the provided GroupObject. The GroupObject can be based upon a ByteArray or a direct buffer.
	 * 
	 * @param msgObj
	 * @throws UnsupportedEncodingException
	 */
	private static void createMessage(GroupObject msgObj) throws UnsupportedEncodingException {
		// Field: serialNumber - set up a number field of uint64
		msgObj.setNumber(msgObj.getField("serialNumber"), 1234567); 

		// Field: modelYear - set up a number field of unit16
		msgObj.setNumber(msgObj.getField("modelYear"), 2016); 

		// Field: available - set up an enum of type uint8
		msgObj.setNumber(msgObj.getField("available"), 1); 

		// Field: code - set up an enum of type char.
		msgObj.setChar(msgObj.getField("code"),'C'); 

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
		Group engine = (Group) msgObj.getField("engine");
		msgObj.setNumber(engine.getField("capacity"), 1500); // Field: capacity - the first field in the composite field 
		msgObj.setNumber(engine.getField("numCylinders"), 6); // Field: numCylinders - the second field in the composite
		// Jump to the 3rd field to skip the constant field. 
		char[] manufactureCode = {'V','T','I'};
		msgObj.setChars(engine.getField("manufacturerCode"), manufactureCode, 0, 3); // Field: manufactureCode - the third field in the composite.
		msgObj.setChar(engine.getField("booster.BoostType"), 'S');
		msgObj.setNumber(engine.getField("booster.horsePower"), 3);
		
		// Field: fuelFigures - set up a group
		fuelFiguresDefinition = (Group) msgObj.getField("fuelFigures");
		GroupObjectArray fuelFigures = msgObj.getGroupArray(fuelFiguresDefinition);
		GroupObject fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField("speed"), 40); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField("mpg"), 25.5); // Field: mpg
		usageDescriptionDefinition = fuelFigure.getField("usageDescription");
		String usageDescription = "this is a description of the usage";
		fuelFigure.setBytes(usageDescriptionDefinition, usageDescription.getBytes("utf-8"), 0, usageDescription.length());
		
		// Field: performanceFigures - set up a group
		performanceFiguresDefinition = (Group) msgObj.getField("performanceFigures");
		GroupObjectArray performanceFigures = msgObj.getGroupArray(performanceFiguresDefinition);
		GroupObject performanceFigure = performanceFigures.addGroupObject(); // add a row to the group
		performanceFigure.setNumber(performanceFigure.getField("octaneRating"), 89); // Field: octaneRating
		
		// Field: acclerations - set up a group within another group
		accelerationDefinition = (Group) performanceFiguresDefinition.getField("acceleration");
		GroupObjectArray acclerations = performanceFigure.getGroupArray(accelerationDefinition);
		GroupObject accleration = acclerations.addGroupObject();
		mph = accleration.getField("mph");
		accleration.setNumber(accleration.getField("mph"), 60);
		seconds = accleration.getField("seconds");
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
		
		System.out.println("Actual  : "+MessageUtil.toJsonString(msgObj));
	}

	@BeforeClass
	public static void initTest() throws JAXBException, SAXException, ParserConfigurationException, IOException, XMLStreamException, FactoryConfigurationError {
		//  Create SBEMessageSchema
		schema = SBEMessageSchema.createSBESchema("src/test/resources/example-schemav4.xml");
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, sbeBuffer, 0);

		createMessage(msgObj);
	}
	
	@Test
	public void testAddDeleteGroupRows() throws UnsupportedEncodingException {
		GroupObject msgObj = schema.wrapSbeBuffer(sbeBuffer, 0);
		
		// verify the initial creation of the performanceGroup is correct
		GroupObjectArray performanceFigures = msgObj.getGroupArray(performanceFiguresDefinition);
		Assert.assertEquals(1, performanceFigures.getNumOfGroups());
		
		// current acceleration
		GroupObjectArray accelerations = performanceFigures.getGroupObject(0).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// 1.) Insert a new row at the beginning of a non-empty repeating group
		GroupObject newPerformance = performanceFigures.addGroupObject(0);
		Assert.assertEquals(2, performanceFigures.getNumOfGroups());
		
		// verify previous acceleration is not affected 
		accelerations = performanceFigures.getGroupObject(1).getGroupArray(accelerationDefinition);
		Assert.assertEquals(1, accelerations.getNumOfGroups());
		Assert.assertEquals(60, accelerations.getGroupObject(0).getNumber(mph).intValue());
		Assert.assertEquals(2.5, accelerations.getGroupObject(0).getFloat(seconds),0.001);
		
		// 2.) Populate the nested repeating group in the newly created row
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
		
		// 3.) Add multiple rows to a repeating group using various addGroupObject methods
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
		
		// 4.) Remove a middle row from a repeating group
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
		
		// 5.) Insert a row to a repeating group that contains nested repeating sub group
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
		
		// 6.) Delete a middle row from a repeating group that contains nested repeating sub group
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

		// 7.) Insert a row into a repeating group that contains a variable length data field
		GroupObjectArray fuelFigures = msgObj.getGroupArray(fuelFiguresDefinition);
		GroupObject fuelFigure = fuelFigures.addGroupObject(1); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField("speed"), 61);
		
		fuelFigure = fuelFigures.getGroupObject(1);		
		String usageDescription = "newly added description";
		fuelFigure.setBytes(usageDescriptionDefinition, usageDescription.getBytes("utf-8"), 0, usageDescription.length());
		
		// verify the definition of the first row is not affected
		fuelFigure = fuelFigures.getGroupObject(0);
		Assert.assertEquals("this is a description of the usage", fuelFigure.getString(usageDescriptionDefinition));
		Assert.assertEquals(40, fuelFigure.getNumber(fuelFigure.getField("speed")));

		fuelFigure = fuelFigures.getGroupObject(1);		
		Assert.assertEquals("newly added description", fuelFigure.getString(usageDescriptionDefinition));
		Assert.assertEquals(61, fuelFigure.getNumber(fuelFigure.getField("speed")));

		// verify the first field of the next row is not affected
		fuelFigure = fuelFigures.getGroupObject(2);
		Assert.assertEquals(60, fuelFigure.getNumber(fuelFigure.getField("speed")));
		
		// 8.) Remove the first row from a repeating group that contains a variable length data field 
		// and the varibale length data field is not null.
		fuelFigures.deleteGroupObject(0);
		fuelFigure = fuelFigures.getGroupObject(0);		
		Assert.assertEquals("newly added description", fuelFigure.getString(usageDescriptionDefinition));
		Assert.assertEquals(61, fuelFigure.getNumber(fuelFigure.getField("speed")));
		fuelFigure = fuelFigures.getGroupObject(1);
		Assert.assertEquals(60, fuelFigure.getNumber(fuelFigure.getField("speed")));
		
		// 9.) Rewrap the whole message to ensure the decoder is correct
		String beforeWrapping = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(sbeBuffer, 0);
		Assert.assertEquals(beforeWrapping, MessageUtil.toJsonString(msgObj));
	}
}
