package com.github.bwsoft.iris.message.sbe;

import java.io.IOException;
import java.nio.BufferOverflowException;
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
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.xml.sax.SAXException;

import com.github.bwsoft.iris.message.Group;
import com.github.bwsoft.iris.message.GroupObject;
import com.github.bwsoft.iris.message.GroupObjectArray;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.util.MessageUtil;


public class SBEMessageEncoderRC4Test {

	private static final String toBeCreated="{\"serialNumber\":1234567,\"modelYear\":2016,"+
	"\"available\":TRUE,\"code\":C,\"someNumbers\":[1,2,10,4,5],\"vehicleCode\":\"mycode\","+
			"\"extras\":5,\"engine\":{\"capacity\":1500,\"numCylinders\":6,\"maxRpm\":9000,"+
	"\"manufacturerCode\":\"VTI\",\"fuel\":\"Petrol\"},\"fuelFigures\":[{\"speed\":40,\"mpg\":25.5},"+
			"{\"speed\":60,\"mpg\":30.0}],\"performanceFigures\":{\"octaneRating\":89,"+
	"\"acceleration\":{\"mph\":60,\"seconds\":2.5}},\"make\":\"Honda\",\"model\":\"Accord\","+
			"\"activationCode\":\"deadbeef\"}";
	
	@Rule
	public TestRule watcher = new TestWatcher() {
		protected void starting(Description description) {
			System.out.format("\nStarting test: %s", description.getMethodName());
		}
	};

	@Rule 
	public ExpectedException exception = ExpectedException.none();
	
	@BeforeClass
	public static void description() {
		System.out.println("Description: Testing the encoder by building messages from an empty buffer");
	}
	
	@Test
	public void testCreateEmptyMessageWithByteArray() throws JAXBException, SAXException, ParserConfigurationException, IOException, XMLStreamException, FactoryConfigurationError {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int offset = 25;
		
		//  Create SBEMessageSchema
		SBEMessageSchema schema = SBEMessageSchema.createSBESchema("src/test/resources/example-schemav4.xml");
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, buffer, offset);
		
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
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField("fuelFigures"));
		GroupObject fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField("speed"), 40); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField("mpg"), 25.5); // Field: mpg
		String usageDescription = "this is a description of the usage";
		fuelFigure.setBytes(fuelFigure.getField("usageDescription"), usageDescription.getBytes("utf-8"), 0, usageDescription.length());
		
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
		
		System.out.println(MessageUtil.toJsonString(msgObj));
		Assert.assertEquals(toBeCreated, MessageUtil.toJsonString(msgObj));
		System.out.println(" ...... passed");
	}

	@Test
	public void testCreateEmptyMessageWithDirectBuffer() throws JAXBException, SAXException, ParserConfigurationException, IOException, XMLStreamException, FactoryConfigurationError {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		int offset = 365;
		
		//  Create SBEMessageSchema
		SBEMessageSchema schema = SBEMessageSchema.createSBESchema("src/test/resources/example-schema.xml");
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, buffer, offset);
		
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
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		GroupObject fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField((short) 10), 40); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField((short) 11), 25.5); // Field: mpg
		
		// Field: performanceFigures - set up a group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12));
		GroupObject performanceFigure = performanceFigures.addGroupObject(); // add a row to the group
		performanceFigure.setNumber(performanceFigure.getField((short) 13), 89); // Field: octaneRating
		
		// Field: acclerations - set up a group within another group
		GroupObjectArray acclerations = performanceFigure.getGroupArray(performanceFigure.getField((short) 14));
		GroupObject accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField((short) 15), 60);
		accleration.setNumber(accleration.getField((short) 16), 2.5);
		
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
		
		Assert.assertEquals(toBeCreated, MessageUtil.toJsonString(msgObj));		
		System.out.println(" ...... passed");
	}

	@Test
	public void testCreateEmptyMessageWithInsufficientBuffer() throws Exception {
		exception.expect(BufferOverflowException.class);
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(136);
		int offset = 60;
		
		//  Create SBEMessageSchema
		SBEMessageSchema schema = SBEMessageSchema.createSBESchema("src/test/resources/example-schema.xml");
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, buffer, offset);
		
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
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		GroupObject fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField((short) 10), 40); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField((short) 11), 25.5); // Field: mpg
		
		// Field: performanceFigures - set up a group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12));
		GroupObject performanceFigure = performanceFigures.addGroupObject(); // add a row to the group
		performanceFigure.setNumber(performanceFigure.getField((short) 13), 89); // Field: octaneRating
	
		// Field: acclerations - set up a group within another group
		GroupObjectArray acclerations = performanceFigure.getGroupArray(performanceFigure.getField((short) 14));
		try {
			acclerations.addGroupObject();
			throw new Exception("error in creating an element with insufficient space");
		} catch( BufferOverflowException e ) {
			System.out.println(" ...... passed");
			throw e;
		}
	}
}
