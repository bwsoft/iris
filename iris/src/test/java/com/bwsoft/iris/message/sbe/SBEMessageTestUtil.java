/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.bwsoft.iris.message.sbe;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import baseline.BooleanType;
import baseline.CarEncoder;
import baseline.EngineEncoder;
import baseline.Model;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.sbe.ir.generated.MessageHeaderEncoder;

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
		encode(encoder, directBuffer, bufferOffset+headerEncoder.encodedLength());		
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
