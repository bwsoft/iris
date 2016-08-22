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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.github.bwsoft.iris.message.GroupObject;
import com.github.bwsoft.iris.message.GroupObjectArray;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.util.MessageUtil;

public class SBEMessageEncoderTest {
	// a buffer that is populated with SBE message before all tests. 
	private final static ByteBuffer sbeBuffer = ByteBuffer.allocateDirect(4096);
	private final static int bufferOffset = 0;
	private final static ByteBuffer newSbeBuffer1 = ByteBuffer.allocateDirect(4096);
	private final static ByteBuffer newSbeBuffer2 = ByteBuffer.allocate(4096);

	// create SBEMessageSchema based upon the schema.
	private final static SBEMessageSchema factory;
	static {
		SBEMessageSchema schema = null;
		try{
			schema = SBEMessageSchema.createSBESchema("./src/test/resources/example-schema.xml");
		} catch( Exception e ) {
			e.printStackTrace();
		}
		factory = schema;
	};

	@Rule
	public TestRule watcher = new TestWatcher() {
		protected void starting(Description description) {
			System.out.format("\nStarting test: %s", description.getMethodName());
		}
	};
	
	/**
	 * Use RL SBE encoder to create a SBE message and store it in sbeBuffer
	 */
	@BeforeClass
	public static void createSBEMessage() {
		System.out.println("Description: testing encoder by modifying SBE messages created from RL SBE encoder");
		SBEMessageTestUtil.createSBEMessageUsingRLEncoder(sbeBuffer, bufferOffset);
	}
	
	@Test
	public void alterExistingSbeMessageByAddingRowsToNoneEmptyRepeatingGroup() throws UnsupportedEncodingException {
		// copy the first sbe message to a new buffer
		int offset = 34;
		MessageUtil.messageCopy(sbeBuffer, bufferOffset, 0, newSbeBuffer1, offset, factory);
		
		// wrap the copied message
		GroupObject msgObj = factory.wrapSbeBuffer(newSbeBuffer1, offset);
		String expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":[{\"speed\":30,\"mpg\":35.9},{\"speed\":55,\"mpg\":49.0},"+
				"{\"speed\":75,\"mpg\":40.0}],\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},"+
				"{\"mph\":100,\"seconds\":12.2}]},{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8}]}],\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		
		// insert a new row for fuel figure
		int sizeBefore = msgObj.getSize();
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		GroupObject newFuelFigure = fuelFigures.addGroupObject();
		newFuelFigure.setNumber(newFuelFigure.getField((short) 10), (short) 100);
		newFuelFigure.setNumber(newFuelFigure.getField((short) 11), 35.0);
		expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":[{\"speed\":30,\"mpg\":35.9},{\"speed\":55,\"mpg\":49.0},"+
				"{\"speed\":75,\"mpg\":40.0},{\"speed\":100,\"mpg\":35.0}],\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},"+
				"{\"mph\":100,\"seconds\":12.2}]},{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8}]}],\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		
		// verify the size change is correct
		Assert.assertEquals(sizeBefore+newFuelFigure.getSize(), msgObj.getSize());

		// re-warp message to verify the re-wrap produces the same result
		String unparsedMsg = msgObj.toString();
		msgObj = factory.wrapSbeBuffer(newSbeBuffer1, offset);
		Assert.assertEquals(unparsedMsg, msgObj.toString());
		
		// add a row to the accleration group of the second row of the performanceFigure group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12)); 
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		
		GroupObject performanceFigure2 = performanceFigures.getGroupObject((short) 1);
		GroupObjectArray accleration = performanceFigure2.getGroupArray(performanceFigure2.getField((short) 14));
		int originalDimmension = accleration.getNumOfGroups();
		Assert.assertEquals(3, originalDimmension);
		
		int originalPerformanceFigureSize = performanceFigure2.getSize();
		sizeBefore = msgObj.getSize();
		GroupObject newAccleration = accleration.addGroupObject();
		newAccleration.setNumber(newAccleration.getField((short) 15), 120);
		newAccleration.setNumber(newAccleration.getField((short) 16), 14.1);
		expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":[{\"speed\":30,\"mpg\":35.9},{\"speed\":55,\"mpg\":49.0},"+
				"{\"speed\":75,\"mpg\":40.0},{\"speed\":100,\"mpg\":35.0}],\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},"+
				"{\"mph\":100,\"seconds\":12.2}]},{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8},{\"mph\":120,\"seconds\":14.1}]}],\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		Assert.assertEquals(originalPerformanceFigureSize+newAccleration.getSize(), performanceFigure2.getSize());
		Assert.assertEquals(sizeBefore+newAccleration.getSize(), msgObj.getSize());		

		// re-warp message to verify the re-wrap produces the same result
		unparsedMsg = msgObj.toString();
		msgObj = factory.wrapSbeBuffer(newSbeBuffer1, offset);
		Assert.assertEquals(unparsedMsg, msgObj.toString());
		System.out.println(" ...... passed");
	}	
	
	/**
	 * Add a group row to a group without any row initially
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void alterExistingSbeMessageByAddingRowsToGroupsThatDoNotHaveAnyRowBefore() throws UnsupportedEncodingException {
		int offset = 632;
		// copy the second new SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, bufferOffset, 1, newSbeBuffer2, offset, factory);
		
		// wrap the copied message
		GroupObject msgObj = factory.wrapSbeBuffer(newSbeBuffer2, offset);
		String expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":null,\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null}],\"make\":\"Honda\",\"model\":null,\"activationCode\":"+
				"\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));

		int sizeBefore = msgObj.getSize();
		
		// add row
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		GroupObject newFuelFigure = fuelFigures.addGroupObject();
		newFuelFigure.setNumber(newFuelFigure.getField((short) 10), (short) 30);
		newFuelFigure.setNumber(newFuelFigure.getField((short) 11), 35.9);
		expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":{\"speed\":30,\"mpg\":35.9},\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null}],\"make\":\"Honda\",\"model\":null,\"activationCode\":"+
				"\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		Assert.assertEquals(sizeBefore+newFuelFigure.getSize(), msgObj.getSize());
		Assert.assertEquals(6, newFuelFigure.getSize());
		
		// re-warp message to verify the re-wrap produces the same result
		String unparsedMsg = msgObj.toString();
		msgObj = factory.wrapSbeBuffer(newSbeBuffer2, offset);
		Assert.assertEquals(unparsedMsg, msgObj.toString());
		
		// add another row
		newFuelFigure = fuelFigures.addGroupObject();
		newFuelFigure.setNumber(newFuelFigure.getField((short) 10), (short) 55);
		newFuelFigure.setNumber(newFuelFigure.getField((short) 11), 49.0);
		expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":[{\"speed\":30,\"mpg\":35.9},{\"speed\":55,\"mpg\":49.0}],\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null}],\"make\":\"Honda\",\"model\":null,\"activationCode\":"+
				"\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		Assert.assertEquals(sizeBefore+2*newFuelFigure.getSize(), msgObj.getSize());
		Assert.assertEquals(6, newFuelFigure.getSize());
		
		// add a row to the accleration group of the second row of the performanceFigure group
		// it is empty initially
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12)); 
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		
		GroupObject performanceFigure2 = performanceFigures.getGroupObject((short) 1);
		GroupObjectArray accleration = performanceFigure2.getGroupArray(performanceFigure2.getField((short) 14));
		int originalDimmension = accleration.getNumOfGroups();
		Assert.assertEquals(0, originalDimmension);
		sizeBefore = msgObj.getSize();
		int performanceFigure2Size = performanceFigure2.getSize();
		GroupObject newAccleration = accleration.addGroupObject();
		newAccleration.setNumber(newAccleration.getField((short) 15), 120);
		newAccleration.setNumber(newAccleration.getField((short) 16), 14.1);
		expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":[{\"speed\":30,\"mpg\":35.9},{\"speed\":55,\"mpg\":49.0}],\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":{\"mph\":120,\"seconds\":14.1}}],\"make\":\"Honda\",\"model\":null,\"activationCode\":"+
				"\"deadbeef\"}";

		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		Assert.assertEquals(sizeBefore+newAccleration.getSize(), msgObj.getSize());
		Assert.assertEquals(performanceFigure2Size+newAccleration.getSize(), performanceFigure2.getSize());

		// re-warp message to verify the re-wrap produces the same result
		unparsedMsg = msgObj.toString();
		msgObj = factory.wrapSbeBuffer(newSbeBuffer2, offset);
		Assert.assertEquals(unparsedMsg, msgObj.toString());
		System.out.println(" ...... passed");
	}	
	
	/**
	 * Add a row to a group that contains another group
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void alterExistingSbeMessageByAddingARowToARepeatingGroupThatContainsANestedRepeatingGroup() throws UnsupportedEncodingException {
		// copy the second new SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, bufferOffset, 1, newSbeBuffer1, 0, factory);

		// wrap the copied message
		GroupObject msgObj = factory.wrapSbeBuffer(newSbeBuffer1, 0);
		String expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":null,\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null}],\"make\":\"Honda\",\"model\":null,\"activationCode\":"+
				"\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		int sizeBefore = msgObj.getSize();
		
		// add a row to the performance figures
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12)); 
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		GroupObject newPerformanceFigure = performanceFigures.addGroupObject();
		newPerformanceFigure.setNumber(newPerformanceFigure.getField((short) 13), 89);
		expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":null,\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null},{\"octaneRating\":89,\"acceleration\":null}],\"make\":\"Honda\",\"model\":null,\"activationCode\":"+
				"\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		Assert.assertEquals(sizeBefore+newPerformanceFigure.getSize(), msgObj.getSize());

		// re-warp message to verify the re-wrap produces the same result
		String unparsedMsg = msgObj.toString();
		msgObj = factory.wrapSbeBuffer(newSbeBuffer1, 0);
		Assert.assertEquals(unparsedMsg, msgObj.toString());

		// populate the nested subgroup
		GroupObjectArray acclerations = newPerformanceFigure.getGroupArray(newPerformanceFigure.getField((short) 14));
		GroupObject newAccleration = acclerations.addGroupObject();
		newAccleration.setNumber(newAccleration.getField((short) 15), 120);
		newAccleration.setNumber(newAccleration.getField((short) 16), 14.1);
		expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":null,\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null},{\"octaneRating\":89,\"acceleration\":{\"mph\":120,\"seconds\":14.1}}],\"make\":\"Honda\",\"model\":null,\"activationCode\":"+
				"\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));

		// re-warp message to verify the re-wrap produces the same result
		String originalMsg = msgObj.toString();
		Assert.assertEquals(originalMsg, factory.wrapSbeBuffer(newSbeBuffer1, 0).toString());
		System.out.println(" ...... passed");
	}	
	
	@Test
	public void deleteExistingSbeGroup() throws UnsupportedEncodingException {
		int offset = 127;
		
		// copy the first SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, bufferOffset, 0, newSbeBuffer1, offset, factory);

		// wrap the copied message
		GroupObject msgObj = factory.wrapSbeBuffer(newSbeBuffer1, offset);
		String expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":[{\"speed\":30,\"mpg\":35.9},{\"speed\":55,\"mpg\":49.0},"+
				"{\"speed\":75,\"mpg\":40.0}],\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},"+
				"{\"mph\":100,\"seconds\":12.2}]},{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8}]}],\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		int sizeBefore = msgObj.getSize();
		
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		Assert.assertEquals(3, fuelFigures.getNumOfGroups());
		fuelFigures.deleteGroupObject(1);
		expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":[{\"speed\":30,\"mpg\":35.9},"+
				"{\"speed\":75,\"mpg\":40.0}],\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},"+
				"{\"mph\":100,\"seconds\":12.2}]},{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8}]}],\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		String beforeWrap = msgObj.toString();
		msgObj = factory.wrapSbeBuffer(newSbeBuffer1, offset);
		Assert.assertEquals(beforeWrap, msgObj.toString());
		Assert.assertEquals(sizeBefore-6, msgObj.getSize());
		
		// delete all
		fuelFigures.deleteGroupObject(1);
		fuelFigures.deleteGroupObject(0);
		expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":null,"+
				"\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},"+
				"{\"mph\":100,\"seconds\":12.2}]},{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8}]}],\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		
		// delete the nested repeating group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12)); 
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		GroupObject performanceFigure = performanceFigures.getGroupObject(0);
		GroupObjectArray acclerations = performanceFigure.getGroupArray(performanceFigure.getField((short) 14));
		acclerations.deleteGroupObject(1);
		expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":null,"+
				"\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},"+
				"{\"mph\":100,\"seconds\":12.2}]},{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8}]}],\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));

		GroupObject accleration = acclerations.getGroupObject(1);
		Assert.assertEquals(100, accleration.getNumber(accleration.getField((short) 15)).intValue());
		Assert.assertEquals(12.2, accleration.getNumber(accleration.getField((short) 16)).floatValue(), 0.01);
		acclerations.deleteGroupObject(0);
		acclerations.deleteGroupObject(0);
		expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":null,"+
				"\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":null},{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8}]}],\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";

		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		beforeWrap = msgObj.toString();
		msgObj = factory.wrapSbeBuffer(newSbeBuffer1, offset);
		Assert.assertEquals(beforeWrap, msgObj.toString());
		
		// delete the performance figure 
		performanceFigures.deleteGroupObject(0);
		expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":null,"+
				"\"performanceFigures\":{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8}]},\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";

		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		
		// add a new performance figure
		GroupObject newPerformanceFigure = performanceFigures.addGroupObject();
		newPerformanceFigure.setNumber(newPerformanceFigure.getField((short) 13), 89);
		expectedMsg = "{" +
				"\"serialNumber\":1234,\"modelYear\":2013,\"available\":TRUE,\"code\":A," +
				"\"someNumbers\":[0,1,2,3,4],\"vehicleCode\":\"abcdef\",\"extras\":6,"+
				"\"engine\":{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,"+
				"\"manufacturerCode\":\"123\",\"fuel\":\"Petrol\"},"+
				"\"fuelFigures\":null,"+
				"\"performanceFigures\":[{\"octaneRating\":99,\"acceleration\":"+
				"[{\"mph\":30,\"seconds\":3.8},{\"mph\":60,\"seconds\":7.1},"+
				"{\"mph\":100,\"seconds\":11.8}]},{\"octaneRating\":89,\"acceleration\":null}],\"make\":\"Honda\",\"model\":\"Civic VTi\","+
				"\"activationCode\":\"deadbeef\"}";

		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		beforeWrap = msgObj.toString();
		msgObj = factory.wrapSbeBuffer(newSbeBuffer1, offset);
		Assert.assertEquals(beforeWrap, msgObj.toString());
		System.out.println(" ...... passed");
	}
	
	@Test
	public void addRemoveRawField() throws UnsupportedEncodingException {
		int offset = 672;
		
		// copy the 2nd SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, bufferOffset, 1, newSbeBuffer2, offset, factory);

		// wrap the copied message
		GroupObject msgObj = factory.wrapSbeBuffer(newSbeBuffer2, offset);
		String expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":null,\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null}],\"make\":\"Honda\",\"model\":null,\"activationCode\":"+
				"\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		
		byte[] newModel = "Honda Accord".getBytes();
		msgObj.setBytes(msgObj.getField((short) 18), newModel, 0, newModel.length);
		expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":null,\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null}],\"make\":\"Honda\",\"model\":\"Honda Accord\",\"activationCode\":"+
				"\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));

		newModel = "Accord".getBytes();
		msgObj.setBytes(msgObj.getField((short) 18), newModel, 0, newModel.length);
		expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":null,\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null}],\"make\":\"Honda\",\"model\":\"Accord\",\"activationCode\":"+
				"\"deadbeef\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));

		// delete all var fields
		msgObj.setBytes(msgObj.getField((short) 17), newModel, 0, 0);
		msgObj.setBytes(msgObj.getField((short) 18), newModel, 0, 0);
		msgObj.setBytes(msgObj.getField((short) 19), newModel, 0, 0);
		expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":null,\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null}],\"make\":null,\"model\":null,\"activationCode\":null}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));

		newModel = "cafe".getBytes();
		msgObj.setBytes(msgObj.getField((short) 19), newModel, 0, newModel.length);
		expectedMsg = "{\"serialNumber\":1235,\"modelYear\":2014,\"available\":FALSE,\"code\":B,"+
				"\"someNumbers\":[0,3,6,9,12],\"vehicleCode\":\"abcdef\",\"extras\":5,\"engine\":"+
				"{\"capacity\":2000,\"numCylinders\":4,\"maxRpm\":9000,\"manufacturerCode\":\"123\","+
				"\"fuel\":\"Petrol\"},\"fuelFigures\":null,\"performanceFigures\":[{\"octaneRating\":95,"+
				"\"acceleration\":[{\"mph\":30,\"seconds\":4.0},{\"mph\":60,\"seconds\":7.5},{\"mph\":100,\"seconds\":12.2}]},"+
			    "{\"octaneRating\":99,\"acceleration\":null}],\"make\":null,\"model\":null,\"activationCode\":\"cafe\"}";
		Assert.assertEquals(expectedMsg, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		String beforeWrap = msgObj.toString();
		Assert.assertEquals(beforeWrap, factory.wrapSbeBuffer(newSbeBuffer2, offset).toString());
		System.out.println(" ...... passed");
	}
}
