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

import org.agrona.concurrent.UnsafeBuffer;

import com.github.bwsoft.iris.message.sbe.rl_logic.BooleanType;
import com.github.bwsoft.iris.message.sbe.rl_logic.CarEncoder;
import com.github.bwsoft.iris.message.sbe.rl_logic.EngineEncoder;
import com.github.bwsoft.iris.message.sbe.rl_logic.MessageHeaderEncoder;
import com.github.bwsoft.iris.message.sbe.rl_logic.Model;

public class SBEMessageTestUtil {
	
	public static void createSBEMessageUsingRLEncoder(ByteBuffer sbeBuffer, int bufferOffset) {
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
		int nsize = encode1(encoder, directBuffer, bufferOffset+headerEncoder.encodedLength());		
		
		bufferOffset += (nsize + headerEncoder.encodedLength());
		// write message header using RealLogic generated code. 
		headerEncoder = new MessageHeaderEncoder()
		.wrap(directBuffer, bufferOffset)
		.blockLength(encoder.sbeBlockLength())
		.templateId(encoder.sbeTemplateId())
		.schemaId(encoder.sbeSchemaId())
		.version(encoder.sbeSchemaVersion());
		nsize = encode2(encoder, directBuffer, bufferOffset+headerEncoder.encodedLength());

		bufferOffset += (nsize + headerEncoder.encodedLength());
		// write message header using RealLogic generated code. 
		headerEncoder = new MessageHeaderEncoder()
		.wrap(directBuffer, bufferOffset)
		.blockLength(encoder.sbeBlockLength())
		.templateId(encoder.sbeTemplateId())
		.schemaId(encoder.sbeSchemaId())
		.version(encoder.sbeSchemaVersion());
		nsize = encode3(encoder, directBuffer, bufferOffset+headerEncoder.encodedLength());
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
	
	/**
	 * Create a sbe message with every repeating group and raw field.
	 * 
	 * @param car
	 * @param directBuffer
	 * @param bufferOffset
	 * @return
	 */
	public static int encode1(final CarEncoder car, final UnsafeBuffer directBuffer, final int bufferOffset)
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

	/**
	 * SBE buffer without fuel figure repeating group. And no equal length row in the performanceFigure 
	 * repeating group. The 2nd nested repeating group in the performanceFigure is not presented.
	 * 
	 * No raw data field of "MAKE".
	 * 
	 * @param car
	 * @param directBuffer
	 * @param bufferOffset
	 * @return
	 */
	public static int encode2(final CarEncoder car, final UnsafeBuffer directBuffer, final int bufferOffset)
	{
		final int srcOffset = 0;

		car.wrap(directBuffer, bufferOffset)
		.serialNumber(1235)
		.modelYear(2014)
		.available(BooleanType.FALSE)
		.code(Model.B)
		.putVehicleCode(VEHICLE_CODE, srcOffset);

		for (int i = 0, size = CarEncoder.someNumbersLength(); i < size; i++)
		{
			car.someNumbers(i, i*3);
		}

		car.extras()
		.clear()
		.cruiseControl(true)
		.sportsPack(false)
		.sunRoof(true);

		car.engine()
		.capacity(2000)
		.numCylinders((short)4)
		.putManufacturerCode(MANUFACTURER_CODE, srcOffset);

		car.fuelFiguresCount(0);

		final CarEncoder.PerformanceFiguresEncoder perfFigures = car.performanceFiguresCount(2);
		perfFigures.next()
		.octaneRating((short)95)
		.accelerationCount(3)
		.next().mph(30).seconds(4.0f)
		.next().mph(60).seconds(7.5f)
		.next().mph(100).seconds(12.2f);
		perfFigures.next()
		.octaneRating((short)99)
		.accelerationCount(0);

		car.make(new String(MAKE));
		car.putModel(MODEL, srcOffset, 0);
		car.putActivationCode(ACTIVATION_CODE, 0, ACTIVATION_CODE.capacity());

		return car.encodedLength();
	}	
	
	/**
	 * Unequally length of rows in repeating group performanceFigure
	 * 
	 * @param car
	 * @param directBuffer
	 * @param bufferOffset
	 * @return
	 */
	public static int encode3(final CarEncoder car, final UnsafeBuffer directBuffer, final int bufferOffset)
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
		.accelerationCount(2)
		.next().mph(30).seconds(4.0f)
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
