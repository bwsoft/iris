package com.bunny.iris.example;

/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import baseline.*;
import baseline.CarDecoder.PerformanceFiguresDecoder.AccelerationDecoder;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.sbe.SBEMessage;
import com.bunny.iris.message.sbe.SBESchemaLoader;

public class ExampleUsingGeneratedStub
{
	private static final String ENCODING_FILENAME = "sbe.encoding.filename";
	private static final byte[] VEHICLE_CODE;
	private static final byte[] MANUFACTURER_CODE;
	private static final byte[] MAKE;
	private static final byte[] MODEL;
	private static final UnsafeBuffer ACTIVATION_CODE;

	private static final MessageHeaderDecoder MESSAGE_HEADER_DECODER = new MessageHeaderDecoder();
	private static final MessageHeaderEncoder MESSAGE_HEADER_ENCODER = new MessageHeaderEncoder();
	private static final CarDecoder CAR_DECODER = new CarDecoder();
	private static final CarEncoder CAR_ENCODER = new CarEncoder();

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

	public static void main(final String[] args) throws Exception
	{
		System.out.println("\n*** Basic Stub Example ***");

		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
		final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);
		int bufferOffset = 0;
		int encodingLength = 0;

		// Setup for encoding a message

		MESSAGE_HEADER_ENCODER
		.wrap(directBuffer, bufferOffset)
		.blockLength(CAR_ENCODER.sbeBlockLength())
		.templateId(CAR_ENCODER.sbeTemplateId())
		.schemaId(CAR_ENCODER.sbeSchemaId())
		.version(CAR_ENCODER.sbeSchemaVersion());

		bufferOffset += MESSAGE_HEADER_ENCODER.encodedLength();
		encodingLength += MESSAGE_HEADER_ENCODER.encodedLength();
		encodingLength += encode(CAR_ENCODER, directBuffer, bufferOffset);

		// Optionally write the encoded buffer to a file for decoding by the On-The-Fly decoder

		final String encodingFilename = System.getProperty(ENCODING_FILENAME);
		if (encodingFilename != null)
		{
			try (final FileChannel channel = new FileOutputStream(encodingFilename).getChannel())
			{
				byteBuffer.limit(encodingLength);
				channel.write(byteBuffer);
			}
		}

		// Decode the encoded message

		bufferOffset = 0;
		MESSAGE_HEADER_DECODER.wrap(directBuffer, bufferOffset);

		// Lookup the applicable flyweight to decode this type of message based on templateId and version.
		final int templateId = MESSAGE_HEADER_DECODER.templateId();
		if (templateId != baseline.CarEncoder.TEMPLATE_ID)
		{
			throw new IllegalStateException("Template ids do not match");
		}

		final int actingBlockLength = MESSAGE_HEADER_DECODER.blockLength();
		final int schemaId = MESSAGE_HEADER_DECODER.schemaId();
		final int actingVersion = MESSAGE_HEADER_DECODER.version();

		bufferOffset += MESSAGE_HEADER_DECODER.encodedLength();
		decode(CAR_DECODER, directBuffer, bufferOffset, actingBlockLength, schemaId, actingVersion);
		myDecoder2(CAR_DECODER, directBuffer, bufferOffset, actingBlockLength, schemaId, actingVersion);
		myDecoder(CAR_DECODER, directBuffer, bufferOffset, actingBlockLength, schemaId, actingVersion);
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

	public static void decode(
			final CarDecoder car,
			final UnsafeBuffer directBuffer,
			final int bufferOffset,
			final int actingBlockLength,
			final int schemaId,
			final int actingVersion)
					throws Exception
	{
		final byte[] buffer = new byte[128];
		final StringBuilder sb = new StringBuilder();

		car.wrap(directBuffer, bufferOffset, actingBlockLength, actingVersion);

		sb.append("\ncar.templateId=").append(car.sbeTemplateId());
		sb.append("\ncar.schemaId=").append(schemaId);
		sb.append("\ncar.schemaVersion=").append(car.sbeSchemaVersion());
		sb.append("\ncar.serialNumber=").append(car.serialNumber());
		sb.append("\ncar.modelYear=").append(car.modelYear());
		sb.append("\ncar.available=").append(car.available());
		sb.append("\ncar.code=").append(car.code());

		sb.append("\ncar.someNumbers=");
		for (int i = 0, size = CarEncoder.someNumbersLength(); i < size; i++)
		{
			sb.append(car.someNumbers(i)).append(", ");
		}

		sb.append("\ncar.vehicleCode=");
		for (int i = 0, size = CarEncoder.vehicleCodeLength(); i < size; i++)
		{
			sb.append((char)car.vehicleCode(i));
		}

		final OptionalExtrasDecoder extras = car.extras();
		sb.append("\ncar.extras.cruiseControl=").append(extras.cruiseControl());
		sb.append("\ncar.extras.sportsPack=").append(extras.sportsPack());
		sb.append("\ncar.extras.sunRoof=").append(extras.sunRoof());

		final EngineDecoder engine = car.engine();
		sb.append("\ncar.engine.capacity=").append(engine.capacity());
		sb.append("\ncar.engine.numCylinders=").append(engine.numCylinders());
		sb.append("\ncar.engine.maxRpm=").append(engine.maxRpm());
		sb.append("\ncar.engine.manufacturerCode=");
		for (int i = 0, size = EngineEncoder.manufacturerCodeLength(); i < size; i++)
		{
			sb.append((char)engine.manufacturerCode(i));
		}

		sb.append("\ncar.engine.fuel=").append(new String(buffer, 0, engine.getFuel(buffer, 0, buffer.length), "ASCII"));

		for (final CarDecoder.FuelFiguresDecoder fuelFigures : car.fuelFigures())
		{
			sb.append("\ncar.fuelFigures.speed=").append(fuelFigures.speed());
			sb.append("\ncar.fuelFigures.mpg=").append(fuelFigures.mpg());
		}

		for (final CarDecoder.PerformanceFiguresDecoder performanceFigures : car.performanceFigures())
		{
			sb.append("\ncar.performanceFigures.octaneRating=").append(performanceFigures.octaneRating());

			for (final AccelerationDecoder acceleration : performanceFigures.acceleration())
			{
				sb.append("\ncar.performanceFigures.acceleration.mph=").append(acceleration.mph());
				sb.append("\ncar.performanceFigures.acceleration.seconds=").append(acceleration.seconds());
			}
		}

		sb.append("\ncar.make.semanticType=").append(CarEncoder.makeMetaAttribute(MetaAttribute.SEMANTIC_TYPE));
		sb.append("\ncar.make=").append(car.make());

		sb.append("\ncar.model=").append(
				new String(buffer, 0, car.getModel(buffer, 0, buffer.length), CarEncoder.modelCharacterEncoding()));

		final UnsafeBuffer tempBuffer = new UnsafeBuffer(buffer);
		final int tempBufferLength = car.getActivationCode(tempBuffer, 0, tempBuffer.capacity());
		sb.append("\ncar.activationCode=").append(new String(buffer, 0, tempBufferLength));

		sb.append("\ncar.encodedLength=").append(car.encodedLength());

		System.out.println(sb);
	}
	
	public static void myDecoder(
			final CarDecoder car,
			final UnsafeBuffer directBuffer,
			final int bufferOffset,
			final int actingBlockLength,
			final int schemaId,
			final int actingVersion
			) {
		SBEMessage message = new SBEMessage()
				.setBlockSize(actingBlockLength)
				.setByteOrder(ByteOrder.LITTLE_ENDIAN)
				.setMessageHeaderSize((short)8)
				.setGroupHeaderSize((short) 3)
				.setVarDataHeaderSize((short) 1);
		message.addChildField(FieldType.U64).setName("serialNumber").setID((short)1);
		message.addChildField(FieldType.U16).setName("modelYear").setID((short)2);
		message.addChildField(FieldType.U8).setName("available").setID((short)3);
		message.addChildField(FieldType.BYTE).setName("code").setID((short)4);
		Field someNumbers = message.addChildField(FieldType.I32).setName("someNumber").setID((short)5).setArraySize((short)5);
		
		Field fuelFigure = message.addChildField(FieldType.GROUP).setName("fuelFigures").setID((short) 9);
		fuelFigure.addChildField(FieldType.U16).setName("speed").setID((short) 10);
		
		Field performanceFigures = message.addChildField(FieldType.GROUP).setName("performanceFigures").setID((short) 12);
		performanceFigures.addChildField(FieldType.U8).setName("octaneRating").setID((short)13);
		Field acceleration = performanceFigures.addChildField(FieldType.GROUP).setName("acceleration").setID((short) 14);
		Field mph = acceleration.addChildField(FieldType.U16).setName("mph").setID((short) 15);
		
		Field make = message.addChildField(FieldType.RAW).setName("make").setID((short) 17);
		Field model = message.addChildField(FieldType.RAW).setName("model").setID((short) 18);
		Field activationCode = message.addChildField(FieldType.RAW).setName("activationCode").setID((short) 17);
		message.finalizeDefinition();
		long currentTime = System.currentTimeMillis();
		int count = 10000000;
		for( int i = 0; i < count; i ++ )
			message.wrapForRead(directBuffer, bufferOffset-8);
		long diff = System.currentTimeMillis() - currentTime;
		System.out.println("Performance = "+diff + "/" +count+" ms");
		
		short occurrence = mph.getTotalOccurrence();
		mph.getValues(value -> System.out.println("mph="+value.getString((short) 0)));
		System.out.println("Total message size= "+message.getSize());
		
		someNumbers.getValues(value -> {
			Field field = value.getField();
			for( short i = 0; i < field.getArraySize(); i ++ ) {
				System.out.println("someNumbers="+value.getString(i));
			}
		});
		
		performanceFigures.getChildValues(value -> {
			System.out.println("name="+value.getField().getName()+", value="+value.getString((short)0));
		});
		
		make.getValues(value->{
			System.out.println("name="+value.getField().getName()+", value="+value.getString((short)0));			
		});
	}
	public static void myDecoder2(
			final CarDecoder car,
			final UnsafeBuffer directBuffer,
			final int bufferOffset,
			final int actingBlockLength,
			final int schemaId,
			final int actingVersion
			) throws FileNotFoundException, JAXBException {
		SBESchemaLoader loader = new SBESchemaLoader();
		HashMap<Integer, SBEMessage> lookup = loader.loadSchema("/home/yzhou/Projects/iris/iris/src/test/resources/example-schema.xml");
		SBEMessage sbeMessage = lookup.get(1);
		
		Field serialNumber = sbeMessage.getField((short) 1);
		Field available = sbeMessage.getField((short) 3);
		Field code = sbeMessage.getField((short) 4);
		Field someNumbers = sbeMessage.getField((short) 5);
		Field vehicleCode = sbeMessage.getField((short) 6);
		Field extras = sbeMessage.getField((short) 7);
		
		sbeMessage.wrapForRead(directBuffer, bufferOffset-8);
		serialNumber.getValues(v->System.out.println(v.getString((short) 0)));
		someNumbers.getValues(v->{
			System.out.println(someNumbers.getName()+":");
			for( short i = 0; i < someNumbers.getArraySize(); i ++ ) {
				System.out.println("    "+v.getString(i));
			}
		});
		
		available.getValues(v->System.out.println("available :"+v.getEnumName()));
		code.getValues(v->System.out.println("car code: "+v.getEnumName()));
		vehicleCode.getValues(v->{
			byte[] values = new byte[v.getSize()];
			System.out.println("Vehicle code: "+new String(values, 0, v.getBytes(values, 0)));
		});
		extras.getValues(v->{
			System.out.println("Extra: is sunroof="+v.isSet("sunRoof")+", cruiseControl="+v.isSet("cruiseControl"));
		});
	}
}
