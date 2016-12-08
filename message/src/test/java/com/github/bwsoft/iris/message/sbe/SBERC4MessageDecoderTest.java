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
package com.github.bwsoft.iris.message.sbe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
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
import com.github.bwsoft.iris.message.sbe.SBEMessage;
import com.github.bwsoft.iris.util.MessageUtil;

/**
 * Test the SBE decoder various methods
 *    1.) Test method to obtain the enum name
 *    2.) Test method to retrieve a Numbers array
 *    3.) Test method to getBytes
 *    4.) Test method to getByte
 *    5.) Test method isSet for bit set fields
 *    6.) Test MessageUtil copy utility to create a byte buffer that contains three consecutive 
 *    SBE messages
 *    
 * @author yzhou
 *
 */
public class SBERC4MessageDecoderTest {

	private static SBEMessageSchema factory;
	
	// a buffer that is populated with SBE message before all tests. 
	private final static ByteBuffer sbeBuffer = ByteBuffer.allocateDirect(4096);
	private final static int bufferOffset[] = new int[3];

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

	@Rule 
	public ExpectedException exception = ExpectedException.none();
	
	/**
	 * Populate sbeBuffer with a couple of SBE messages for decoding.
	 * 
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	@BeforeClass
	public static void createSBEMessage() throws JAXBException, SAXException, ParserConfigurationException, IOException, XMLStreamException, FactoryConfigurationError {
		//  Create SBEMessageSchema
		factory = SBEMessageSchema.createSBESchema("src/test/resources/example-schemav4.xml");
		bufferOffset[0] = 231;
		
		// create the SBE message for Car
		// create a message for testing decoder purpose
		GroupObject msgObj = factory.createSbeBuffer(1, sbeBuffer, bufferOffset[0]);
		createMessageForDecoding(msgObj);
		
		// use the message in the encoding test to create some noise
		bufferOffset[1] = bufferOffset[0] + msgObj.getSize() + 
				((SBEMessage) msgObj.getDefinition()).getHeader().getSize();
		msgObj = factory.createSbeBuffer(1, sbeBuffer, bufferOffset[1]);
		SBERC4MessageEncoderTest.createEmptyMessage(msgObj);
		
		// now create a message for testing decoder purpose
		bufferOffset[2] = bufferOffset[1] + msgObj.getSize() + 
				((SBEMessage) msgObj.getDefinition()).getHeader().getSize();
		msgObj = factory.createSbeBuffer(1, sbeBuffer, bufferOffset[2]);
		createMessageForDecoding(msgObj);
	}

	/**
	 * Build a message based upon the provided GroupObject. The GroupObject can be based upon a ByteArray or a direct buffer.
	 * 
	 * @param msgObj
	 * @throws UnsupportedEncodingException
	 */
	public static void createMessageForDecoding(GroupObject msgObj) throws UnsupportedEncodingException {
		// Field: serialNumber - set up a number field of uint64
		msgObj.setNumber(msgObj.getField("serialNumber"), 1234); 

		// Field: modelYear - set up a number field of unit16
		msgObj.setNumber(msgObj.getField("modelYear"), 2013); 

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
		fuelFigure.setNumber(fuelFigure.getField("speed"), 30); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField("mpg"), 35.9); // Field: mpg
		String usageDescription = "this is a description of the usage";
		fuelFigure.setBytes(fuelFigure.getField("usageDescription"), usageDescription.getBytes("utf-8"), 0, usageDescription.length());
		
		// Field: performanceFigures - set up a group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures"));
		GroupObject performanceFigure = performanceFigures.addGroupObject(); // add a row to the group
		performanceFigure.setNumber(performanceFigure.getField("octaneRating"), 95); // Field: octaneRating
		
		// Field: acclerations - set up a group within another group
		GroupObjectArray acclerations = performanceFigure.getGroupArray(performanceFigure.getField("acceleration"));
		GroupObject accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField("mph"), 30);
		accleration.setNumber(accleration.getField("seconds"), 4.0);

		accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField("mph"), 60);
		accleration.setNumber(accleration.getField("seconds"), 7.5);
		
		accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField("mph"), 100);
		accleration.setNumber(accleration.getField("seconds"), 12.2);

		// Field: fuelFigures - to add another row in the previous repeating group.
		// It is a good practice to add fields in sequence. But not necessary.
		fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField("speed"), 55); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField("mpg"), 49.0); // Field: mpg
		
		fuelFigure = fuelFigures.addGroupObject(); // add a row to the group
		fuelFigure.setNumber(fuelFigure.getField("speed"), 75); // Field: speed 
		fuelFigure.setNumber(fuelFigure.getField("mpg"), 40.0); // Field: mpg

		// Field: model - set up a raw field. It is good to follow the order of a raw field. 
		// But not necessary.
		byte[] model = "Civic VTi".getBytes();
		msgObj.setBytes(msgObj.getField("model"), model, 0, model.length);

		performanceFigure = performanceFigures.addGroupObject(); // add a row to the group
		performanceFigure.setNumber(performanceFigure.getField("octaneRating"), 99); // Field: octaneRating
		
		// Field: acclerations - set up a group within another group
		acclerations = performanceFigure.getGroupArray(performanceFigure.getField("acceleration"));
		accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField("mph"), 40);
		accleration.setNumber(accleration.getField("seconds"), 3.8);

		accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField("mph"), 80);
		accleration.setNumber(accleration.getField("seconds"), 7.1);

		accleration = acclerations.addGroupObject();
		accleration.setNumber(accleration.getField("mph"), 90);
		accleration.setNumber(accleration.getField("seconds"), 8.1);

		// Field: make - set up a raw field
		byte[] make = "Honda".getBytes();
		msgObj.setBytes(msgObj.getField("make"), make, 0, make.length);
		
		// Field: activationCode - set up a raw field
		byte[] activationCode = "deadbeef".getBytes();
		msgObj.setBytes(msgObj.getField("activationCode"), activationCode, 0, activationCode.length);
	}
	
	/**
	 * Parse the first SBE message field by field to verify the correctness 
	 * 
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void fieldByFieldVerificationOfADecodingResult() throws UnsupportedEncodingException {
		// copy the first sbe message to a byte array to test the copy facility
		byte[] firstSBE = new byte[1024];

		// test the copy utility
		MessageUtil.messageCopy(sbeBuffer, bufferOffset[0], 0, firstSBE, 0, factory);
		
		ByteBuffer firstSBEBuffer = ByteBuffer.wrap(firstSBE);
		
		// wrap message to obtain GroupObject
		GroupObject msgObj = factory.wrapSbeBuffer(firstSBEBuffer, 0);
		
		fieldByFieldVerificationOfADecodingResult(msgObj);
		
		// get the third message which is the same as the first and retry
		MessageUtil.messageCopy(sbeBuffer, bufferOffset[2], 0, firstSBE, 14, factory);
		firstSBEBuffer = ByteBuffer.wrap(firstSBE,0, 1024);
		msgObj = factory.wrapSbeBuffer(firstSBEBuffer, 14);
		fieldByFieldVerificationOfADecodingResult(msgObj);
	}
	
	private void fieldByFieldVerificationOfADecodingResult(GroupObject msgObj) throws UnsupportedEncodingException {
		Assert.assertEquals(1, msgObj.getDefinition().getID()); // assert the message is the Car message
		
		Assert.assertEquals(1234, msgObj.getNumber(msgObj.getField("serialNumber")).longValue());
		Assert.assertEquals(2013, msgObj.getNumber(msgObj.getField("modelYear")).shortValue());
		Assert.assertEquals("T", msgObj.getEnumName(msgObj.getField("available")));
		Assert.assertEquals("D", msgObj.getEnumName(msgObj.getField("code")));
		Long[] someNumbersActual = new Long[5];
		Long[] someNumbersExpect = {1l,2l,10l,4l,5l};
		msgObj.getNumbers(msgObj.getField("someNumbers"), someNumbersActual, 0, 5);
		Assert.assertArrayEquals(someNumbersExpect, someNumbersActual);
		byte[] vehicleCodeActual = new byte[6];
		msgObj.getBytes(msgObj.getField("vehicleCode"), vehicleCodeActual, 0, 6);
		Assert.assertArrayEquals("mycode".getBytes(), vehicleCodeActual);
		Assert.assertEquals(true, msgObj.isSet(msgObj.getField("extras"), "sunRoof"));
		Assert.assertEquals(false, msgObj.isSet(msgObj.getField("extras"), "sportsPack"));
		Assert.assertEquals(true, msgObj.isSet(msgObj.getField("extras"), "cruiseControl"));

		Group engine = (Group) msgObj.getField("engine");
		Assert.assertEquals(1500, msgObj.getNumber(engine.getField("capacity")).shortValue());
		Assert.assertEquals(6, msgObj.getNumber(engine.getField("numCylinders")).shortValue());
		Assert.assertEquals("9000", msgObj.getString(engine.getField("maxRpm"), Charset.defaultCharset().name())); // constant field is always a string value
		char[] manufactureCodeActual = new char[3];
		msgObj.getChars(engine.getField("manufacturerCode"), manufactureCodeActual, 0, 3);
		Assert.assertArrayEquals("VTI".toCharArray(), manufactureCodeActual);
		Assert.assertEquals("Petrol", msgObj.getString(engine.getField("fuel"), Charset.defaultCharset().name()));
		Assert.assertEquals("SUPERCHARGER",msgObj.getEnumName(engine.getField("booster.BoostType")));
		Assert.assertEquals(3, msgObj.getByte(engine.getField("booster.horsePower")));
		
		Group fuelFigures = (Group) msgObj.getField("fuelFigures");
		GroupObjectArray fuelFiguresArray = msgObj.getGroupArray(fuelFigures);
		Assert.assertEquals(3, fuelFiguresArray.getNumOfGroups());
		int speedActual[] = new int[3];
		int speedExpected[] = {30,55,75};
		float mpgActual[] = new float[3];
		float mpgExpected[] = {35.9f, 49.0f, 40.0f};
		String[] usageDescriptionExpected = {
				"this is a description of the usage",
				"",
				""
			};
		byte[][] usageDescriptionActual = new byte[3][125];
		for( short i = 0; i < 3; i ++ ) {
			GroupObject fuelFigureObj = fuelFiguresArray.getGroupObject(i);
			speedActual[i] = fuelFigureObj.getNumber(fuelFigures.getField("speed")).intValue();
			mpgActual[i] = fuelFigureObj.getNumber(fuelFigures.getField("mpg")).floatValue();
			fuelFigureObj.getBytes(fuelFigures.getField("usageDescription"), usageDescriptionActual[i], 0, 125);
		}
		Assert.assertArrayEquals(speedExpected, speedActual);
		Assert.assertArrayEquals(mpgExpected, mpgActual, 0.001f);
		Assert.assertEquals(usageDescriptionExpected[0], new String(usageDescriptionActual[0]).trim());
		Assert.assertEquals(usageDescriptionExpected[1], new String(usageDescriptionActual[1]).trim());
		Assert.assertEquals(usageDescriptionExpected[2], new String(usageDescriptionActual[2]).trim());
		
		Group performanceFigures = (Group) msgObj.getField("performanceFigures");
		GroupObjectArray performanceFiguresArray = msgObj.getGroupArray(performanceFigures);
		Assert.assertEquals(2, performanceFiguresArray.getNumOfGroups());
		int octaneRatingActual[] = {95,99};
		int mphActual[][] = {{30,60,100},{40,80, 90}};
		float secondsActual[][] = {{4.0f, 7.5f, 12.2f},{3.8f,7.1f,8.1f}};
		for( short i = 0; i < 2; i ++ ) {
			GroupObject performanceFiguresObj = performanceFiguresArray.getGroupObject(i);
			Assert.assertEquals(octaneRatingActual[i], performanceFiguresObj.getNumber(performanceFigures.getField("octaneRating")).intValue());
			
			GroupObjectArray acclerationsArray = performanceFiguresObj.getGroupArray(performanceFigures.getField("acceleration"));
			Assert.assertEquals(3, acclerationsArray.getNumOfGroups());
			for( short j=0; j < 3; j ++ ){
				GroupObject accelerationObj = acclerationsArray.getGroupObject(j);
				Assert.assertEquals(mphActual[i][j], accelerationObj.getNumber(accelerationObj.getField("mph")).intValue());
				Assert.assertEquals(secondsActual[i][j], accelerationObj.getNumber(accelerationObj.getField("seconds")).floatValue(), 0.001f);
			}
		}
		
		byte[] makeValue = new byte[5];
		msgObj.getBytes(msgObj.getField("make"), makeValue, 0, 5);
		Assert.assertArrayEquals("Honda".getBytes("utf-8"), makeValue);

		byte[] modelValue = new byte[9];
		msgObj.getBytes(msgObj.getField("model"), modelValue, 0, 9);
		Assert.assertArrayEquals("Civic VTi".getBytes(), modelValue);

		byte[] activationCodeValue = new byte[8];
		msgObj.getBytes(msgObj.getField("activationCode"), activationCodeValue, 0, 8);
		Assert.assertArrayEquals("deadbeef".getBytes(), activationCodeValue);		
	}

	@AfterClass
	public static void cleanup() {
		
	}	
}
