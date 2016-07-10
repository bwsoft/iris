/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.bwsoft.iris.message.sbe;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.Group;
import com.bwsoft.iris.message.GroupObject;

import baseline.BooleanType;
import baseline.CarEncoder;
import baseline.EngineEncoder;
import baseline.Model;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.sbe.ir.generated.MessageHeaderEncoder;

public class SBEMessageTest {

	// a buffer that is populated with SBE message before all tests. 
	private final static ByteBuffer sbeBuffer = ByteBuffer.allocateDirect(4096);
	private final static int bufferOffset = 0;
	
	@BeforeClass
	public static void createSBEMessage() {
		CarEncoder encoder = new CarEncoder();

		UnsafeBuffer directBuffer = new UnsafeBuffer(sbeBuffer);

		// write message header using RealLogic generated code. 
		MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder()
		.wrap(directBuffer, bufferOffset)
		.blockLength(encoder.sbeBlockLength())
		.templateId(encoder.sbeTemplateId())
		.schemaId(encoder.sbeSchemaId())
		.version(encoder.sbeSchemaVersion());
		
		// encode the message body using RealLogic generated code
		encode(encoder, directBuffer, bufferOffset+headerEncoder.encodedLength());
	}
	
	/**
	 * Verify the XML based creation of SBE message.
	 *  
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 */
	@Test
	public void testSbeMessageDefinitionCreation() throws FileNotFoundException, JAXBException {
		// create a sbe message using SBESchemaLoader. 
		// The HashMap contains all messages defined in the xml with the message templateId as the key.
		// There is only one message in the XML.
		SBESchema factory = SBESchemaLoader.loadSchema("./src/test/resources/example-schema.xml");
		SBEMessage sbeMessage = factory.getMsgLookup().get(1);
		
		// Now create the same message manually
		SBEMessageSchema schema = new SBEMessageSchema("", 1, 0, "", "LITTLEENDIAN");
		SBEMessageHeader msgHeader = new SBEMessageHeader(FieldType.U16, FieldType.U16, FieldType.U16, FieldType.U16);
		SBEGroupHeader groupHeader = new SBEGroupHeader(FieldType.U8, FieldType.U16);
		SBEVarLengthFieldHeader varLengthFieldHeader = new SBEVarLengthFieldHeader(FieldType.U8);

		SBEMessage message = (SBEMessage) new SBEMessage(schema, msgHeader, groupHeader, varLengthFieldHeader).setName("Car").setID((short) 1);
		message.addChildField((short)1,FieldType.U64, (short) 1).setName("serialNumber");
		message.addChildField((short)2,FieldType.U16, (short) 1).setName("modelYear");
		message.addChildField((short)3,FieldType.U8, (short) 1).setName("available");
		message.addChildField((short)4, FieldType.CHAR, (short) 1).setName("code");
		message.addChildField((short)5,FieldType.I32, (short) 5).setName("someNumbers");
		message.addChildField((short) 6,FieldType.CHAR, (short) 6).setName("vehicleCode");
		message.addChildField((short) 7,FieldType.U8, (short) 1).setName("extras");
		Group engine = (Group) message.addChildField((short) 8,FieldType.COMPOSITE, (short) 1).setName("engine");
		engine.addChildField((short) 8,FieldType.U16, (short) 1).setName("capacity");
		engine.addChildField((short) 8, FieldType.U8, (short) 1).setName("numCylinders");
		engine.addChildField((short) 8, FieldType.CHAR, (short)3).setName("manufacturerCode");
		
		Group fuelFigure = (Group) message.addChildField((short) 9,FieldType.GROUP, (short) 1).setName("fuelFigures");
		fuelFigure.addChildField((short) 10,FieldType.U16, (short) 1).setName("speed");
		
		Group performanceFigures = (Group) message.addChildField((short) 12,FieldType.GROUP, (short) 1).setName("performanceFigures");
		performanceFigures.addChildField((short)13,FieldType.U8, (short) 1).setName("octaneRating");
		Group acceleration = (Group) performanceFigures.addChildField((short) 14,FieldType.GROUP, (short) 1).setName("acceleration");
		acceleration.addChildField((short) 15,FieldType.U16, (short) 1).setName("mph");
		
		message.addChildField((short) 17,FieldType.RAW, (short) 1).setName("make");
		message.addChildField((short) 18,FieldType.RAW, (short) 1).setName("model");
		message.addChildField((short) 19,FieldType.RAW, (short) 1).setName("activationCode");
		
		// compare two messages to ensure that they are the same
		System.out.println("Automaticall created message: "+sbeMessage.toString());
		System.out.println("Manually created message    : "+message.toString());
		
		Assert.assertEquals(message.toString(), sbeMessage.toString());
	}
	
	/**
	 * Demonstrate the usage of the SBEMessage parser
	 * 
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 */
	@Test
	public void sbeMessageTest() throws FileNotFoundException, JAXBException {
		// create SBEMessageFactory by loading them from schemas.
		SBESchema factory = SBESchemaLoader.loadSchema("./src/test/resources/example-schema.xml");
		
		// wrap message for reading
		GroupObject obj = factory.wrapForRead(sbeBuffer, bufferOffset);

		if( null != obj ) {
			// it is the message belong to this message schema.
			// obtain the message defintion 
			SBEMessage msg = (SBEMessage) obj.getDefinition();
			
			// print out message ID
			System.out.println("Get a message of ID: "+msg.getID());
			
			// retrieve a value of a field by following the message structure
			System.out.println(obj.toString()); // TODO display whole object in json
			System.out.println("mph: "+obj.getGroupArray((short)12).getGroupObject((short) 1).getGroupArray((short)14).getGroupObject(1).getNumber((short) 15, Integer.class));
			
		}
	}
	
	@AfterClass
	public static void cleanup() {
		
	}
	
	private static final byte[] VEHICLE_CODE;
	private static final byte[] MANUFACTURER_CODE;
	private static final byte[] MAKE;
	private static final byte[] MODEL;
	private static final UnsafeBuffer ACTIVATION_CODE;

	static
	{
		try
		{
			VEHICLE_CODE = "abcdef".getBytes(CarEncoder.vehicleCodeCharacterEncoding());
			MANUFACTURER_CODE = "123".getBytes(EngineEncoder.manufacturerCodeCharacterEncoding());
			MAKE = "Honda".getBytes(CarEncoder.makeCharacterEncoding());
			MODEL = "Civic VTi".getBytes(CarEncoder.modelCharacterEncoding());
			ACTIVATION_CODE = new UnsafeBuffer(ByteBuffer.wrap(new byte[]{'d', 'e', 'a', 'd', 'b', 'e', 'e', 'f'}));
		}
		catch (final UnsupportedEncodingException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public static int encode(final CarEncoder car, final UnsafeBuffer directBuffer, final int bufferOffset)
	{
		final int srcOffset = 0;

		car.wrap(directBuffer, bufferOffset)
		.serialNumber(1234)
		.modelYear(2013)
		.available(BooleanType.TRUE)
		.code(Model.A)
		.putVehicleCode(VEHICLE_CODE, srcOffset);

		for (int i = 0, size = CarEncoder.someNumbersLength(); i < size; i++)
		{
			car.someNumbers(i, i);
		}

		car.extras()
		.clear()
		.cruiseControl(true)
		.sportsPack(true)
		.sunRoof(false);

		car.engine()
		.capacity(2000)
		.numCylinders((short)4)
		.putManufacturerCode(MANUFACTURER_CODE, srcOffset);

		car.fuelFiguresCount(3)
		.next().speed(30).mpg(35.9f)
		.next().speed(55).mpg(49.0f)
		.next().speed(75).mpg(40.0f);

		final CarEncoder.PerformanceFiguresEncoder perfFigures = car.performanceFiguresCount(2);
		perfFigures.next()
		.octaneRating((short)95)
		.accelerationCount(3)
		.next().mph(30).seconds(4.0f)
		.next().mph(60).seconds(7.5f)
		.next().mph(100).seconds(12.2f);
		perfFigures.next()
		.octaneRating((short)99)
		.accelerationCount(3)
		.next().mph(30).seconds(3.8f)
		.next().mph(60).seconds(7.1f)
		.next().mph(100).seconds(11.8f);

		car.make(new String(MAKE));
		car.putModel(MODEL, srcOffset, MODEL.length);
		car.putActivationCode(ACTIVATION_CODE, 0, ACTIVATION_CODE.capacity());

		return car.encodedLength();
	}	
}
