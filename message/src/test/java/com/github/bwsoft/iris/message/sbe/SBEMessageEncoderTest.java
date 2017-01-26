package com.github.bwsoft.iris.message.sbe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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


/**
 * This tests the creation of a SBE message based upon RC4 spec.
 *   1.) Test the creation of a constant field with an enum type and a valueRef to an enum value.
 *   2.) Test the creation of a composite that includes a ref to another composite type.
 *   3.) Test the creation of a repeating group that contains variable length field in the first row but not 
 *   in the second row.
 *   4.) Test the recognition of an offset attribute on a field in a nested repeating group. 
 *   5.) Test the creation based upon the byte array and direct buffer.
 *   6.) Test the throw of an exception if the underneath buffer size is insufficient. 
 *   7.) Test the out of sequence creation of a row in the repeating group. 
 *   The second row of fuelFigures is not added until after the creation of the performance figure.
 *   8.) Add additional rows to the non-empty repeating groups including nested 
 *   repeating groups.  
 *   9.) Add additional rows to groups that do not contain any row initially. Groups
 *   include also the nested empty groups. 
 *   10.) Add/remove variable length field in the repeating group
 *   
 * @author yzhou
 *
 */
public class SBEMessageEncoderTest {
	
	private static SBEMessageSchema schema;
	private static ByteBuffer sbeBuffer = ByteBuffer.allocate(1024);
	private static int sbeOffset = 25;

	private static String toBeCreated;
	
	@BeforeClass
	public static void createInitMessage() throws Exception {
		// build JSON expression of the init message
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		addUnquotedField(sb, "serialNumber", "1234567").append(",");
		addUnquotedField(sb, "modelYear", "2016").append(",");
		addUnquotedField(sb, "available", "T").append(",");
		addUnquotedField(sb, "code", "D").append(",");
		addUnquotedField(sb, "someNumbers", "[1,2,10,4,5]").append(",");
		addQuotedField(sb,"vehicleCode","mycode").append(",");
		addUnquotedField(sb, "extras", "5").append(",");
		addUnquotedField(sb, "discountedModel", "C").append(",");
		
		// engine
		sb.append("\"").append("engine").append("\":{");
		addUnquotedField(sb, "capacity", "1500").append(",");
		addUnquotedField(sb, "numCylinders", "6").append(",");
		addUnquotedField(sb, "maxRpm", "9000").append(",");
		addQuotedField(sb, "manufacturerCode", "VTI").append(",");
		addQuotedField(sb, "fuel", "Petrol").append(",");
		addUnquotedField(sb, "booster.BoostType", "SUPERCHARGER").append(",");
		addUnquotedField(sb, "booster.horsePower", "3");
		sb.append("}").append(",");
		
		// fuel figures
		sb.append("\"").append("fuelFigures").append("\":[");
		sb.append("{");
		addUnquotedField(sb, "speed", "40").append(",");
		addUnquotedField(sb, "mpg", "25.5").append(",");
		addQuotedField(sb, "usageDescription", "this is a description of the usage");
		sb.append("},");
		sb.append("{");
		addUnquotedField(sb, "speed", "60").append(",");
		addUnquotedField(sb, "mpg", "30.0").append(",");
		addUnquotedField(sb, "usageDescription", "null");
		sb.append("}");
		sb.append("]").append(",");
		
		// performance figure
		sb.append("\"").append("performanceFigures").append("\":");
		sb.append("{");
		addUnquotedField(sb, "octaneRating", "89").append(",");
		
		// acceleration
		sb.append("\"").append("acceleration").append("\":");
		sb.append("{");
		addUnquotedField(sb, "mph", "60").append(",");
		addUnquotedField(sb, "seconds", "2.5");
		sb.append("}");
		
		sb.append("}").append(",");
		
		addQuotedField(sb, "make", "Honda").append(",");
		addQuotedField(sb, "model", "Accord").append(",");
		addQuotedField(sb, "activationCode", "deadbeef");
		
		sb.append("}");
		toBeCreated = sb.toString();

		//  Create init message
		schema = SBEMessageSchema.createSBESchema("src/test/resources/example-schemav4.xml");
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, sbeBuffer, sbeOffset);
		
		createEmptyMessage(msgObj);
	}
	
	private static StringBuilder addUnquotedField(StringBuilder sb, String fieldName, String value) {
		sb.append("\"").append(fieldName).append("\":").append(value);
		return sb;
	}

	private static StringBuilder addQuotedField(StringBuilder sb, String fieldName, String value) {
		sb.append("\"").append(fieldName).append("\":").append("\"").append(value).append("\"");
		return sb;
	}

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
	 * Test a message creation based upon a byte array.
	 * 
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	@Test
	public void testCreateEmptyMessageWithByteArray() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int offset = 25;
		
		//  Create SBEMessageSchema
		SBEMessageSchema schema = SBEMessageSchema.createSBESchema("src/test/resources/example-schemav4.xml");
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, buffer, offset);
		
		createEmptyMessage(msgObj);
		Assert.assertEquals(toBeCreated, MessageUtil.toJsonString(msgObj));		
	}

	/**
	 * Test a message creation based upon a direct buffer.
	 * 
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	@Test
	public void testCreateEmptyMessageWithDirectBuffer() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		int offset = 365;
		
		//  Create SBEMessageSchema
		SBEMessageSchema schema = SBEMessageSchema.createSBESchema("src/test/resources/example-schemav4.xml");
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, buffer, offset);
		
		createEmptyMessage(msgObj);
		Assert.assertEquals(toBeCreated, MessageUtil.toJsonString(msgObj));		
	}
	
	/**
	 * Test the throw of an exception upon insufficient buffer size.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateEmptyMessageWithInsufficientBuffer() throws Exception {
		exception.expect(BufferOverflowException.class);
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(210);
		int offset = 60;
		
		//  Create SBEMessageSchema
		SBEMessageSchema schema = SBEMessageSchema.createSBESchema("src/test/resources/example-schemav4.xml");
		
		// create the SBE message for Car
		GroupObject msgObj = schema.createSbeBuffer(1, buffer, offset);
		
		try {
			createEmptyMessage(msgObj);
			throw new Exception("error in creating an element with insufficient space");
		} catch( BufferOverflowException e ) {
			System.out.println("BufferOverflowException is thrown.");
			throw e;
		}
	}

	/**
	 * 8.) Add additional rows to the non-empty repeating groups including nested 
	 * repeating groups.
	 *   
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void alterExistingSbeMessageByAddingRowsToNoneEmptyRepeatingGroup() throws UnsupportedEncodingException {
		// copy the sbe message to a new buffer
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int offset = 34;
		MessageUtil.messageCopy(sbeBuffer, sbeOffset, 0, buffer, offset, schema);
		
		// wrap the copied message
		GroupObject msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(toBeCreated, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		
		// insert a new row for fuel figure
		int sizeBefore = msgObj.getSize();
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField("fuelFigures"));
		GroupObject newFuelFigure = fuelFigures.addGroupObject();
		newFuelFigure.setNumber(newFuelFigure.getField("speed"), (short) 100);
		newFuelFigure.setNumber(newFuelFigure.getField("mpg"), 35.0);
		
		// verify the size change is correct
		Assert.assertEquals(sizeBefore+newFuelFigure.getSize(), msgObj.getSize());

		// re-warp message to verify the re-wrap produces the same result
		String unparsedMsg = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(unparsedMsg, MessageUtil.toJsonString(msgObj));
		
		// add a row to the accleration group to the first row of the performanceFigure group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures")); 
		Assert.assertEquals(1, performanceFigures.getNumOfGroups()); // two rows originally
		
		GroupObject performanceFigure_0 = performanceFigures.getGroupObject((short) 0);
		GroupObjectArray accleration = performanceFigure_0.getGroupArray(performanceFigure_0.getField("acceleration"));
		int originalDimmension = accleration.getNumOfGroups();
		Assert.assertEquals(1, originalDimmension);
		
		int originalPerformanceFigureSize = performanceFigure_0.getSize();
		sizeBefore = msgObj.getSize();
		GroupObject newAccleration = accleration.addGroupObject();
		newAccleration.setNumber(newAccleration.getField("mph"), 120);
		newAccleration.setNumber(newAccleration.getField("seconds"), 14.1);
		Assert.assertEquals(originalPerformanceFigureSize+newAccleration.getSize(), performanceFigure_0.getSize());
		Assert.assertEquals(sizeBefore+newAccleration.getSize(), msgObj.getSize());		

		// re-warp message to verify the re-wrap produces the same result
		unparsedMsg = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(unparsedMsg, MessageUtil.toJsonString(msgObj));
	}	

	/**
	 * 9.) Add additional rows to groups that do not contain any row initially. Groups
	 * include also the nested empty groups.
	 *  
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void alterExistingSbeMessageByAddingRowsToGroupsThatDoNotHaveAnyRowBefore() throws UnsupportedEncodingException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		int offset = 632;
		
		// copy the second new SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, sbeOffset, 0, buffer, offset, schema);
		
		// wrap the copied message
		GroupObject msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(toBeCreated, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));

		// remove all of the rows in the fuelFigures repeating group
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField("fuelFigures"));
		int nGroups = fuelFigures.getNumOfGroups();
		for( int i = 0; i < nGroups; i ++ )
			fuelFigures.deleteGroupObject(0);
		
		// rewrap message 
		msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(0, msgObj.getGroupArray(msgObj.getField("fuelFigures")).getNumOfGroups());

		int sizeBefore = msgObj.getSize();
		
		// add row
		fuelFigures = msgObj.getGroupArray(msgObj.getField("fuelFigures"));
		GroupObject newFuelFigure = fuelFigures.addGroupObject();
		newFuelFigure.setNumber(newFuelFigure.getField("speed"), (short) 30);
		newFuelFigure.setNumber(newFuelFigure.getField("mpg"), 35.9);
		Assert.assertEquals(sizeBefore+newFuelFigure.getSize(), msgObj.getSize());
		Assert.assertEquals(12, newFuelFigure.getSize());
		
		// re-warp message to verify the re-wrap produces the same result
		String unparsedMsg = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(unparsedMsg, MessageUtil.toJsonString(msgObj));
		fuelFigures = msgObj.getGroupArray(msgObj.getField("fuelFigures"));
		newFuelFigure = fuelFigures.getGroupObject(0);
		Assert.assertEquals(30, newFuelFigure.getNumber(newFuelFigure.getField("speed")));
		Assert.assertEquals(35.9f, newFuelFigure.getNumber(newFuelFigure.getField("mpg")));
		
		// add another row
		newFuelFigure = fuelFigures.addGroupObject();
		newFuelFigure.setNumber(newFuelFigure.getField("speed"), (short) 55);
		newFuelFigure.setNumber(newFuelFigure.getField("mpg"), 49.0);
		Assert.assertEquals(sizeBefore+2*newFuelFigure.getSize(), msgObj.getSize());
		Assert.assertEquals(12, newFuelFigure.getSize());
		
		// add a row to the accleration group of the second row of the performanceFigure group
		// it is empty initially
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures")); 
		performanceFigures.addGroupObject();
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		
		GroupObject performanceFigure_2 = performanceFigures.getGroupObject((short) 1);
		GroupObjectArray accleration = performanceFigure_2.getGroupArray(performanceFigure_2.getField("acceleration"));
		int originalDimmension = accleration.getNumOfGroups();
		Assert.assertEquals(0, originalDimmension);
		sizeBefore = msgObj.getSize();
		int performanceFigure2Size = performanceFigure_2.getSize();
		GroupObject newAccleration = accleration.addGroupObject();
		newAccleration.setNumber(newAccleration.getField("mph"), 120);
		newAccleration.setNumber(newAccleration.getField("seconds"), 14.1);
		Assert.assertEquals(sizeBefore+newAccleration.getSize(), msgObj.getSize());
		Assert.assertEquals(performanceFigure2Size+newAccleration.getSize(), performanceFigure_2.getSize());

		// re-warp message to verify the re-wrap produces the same result
		unparsedMsg = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(unparsedMsg, MessageUtil.toJsonString(msgObj));

		performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures")); 
		performanceFigure_2 = performanceFigures.getGroupObject((short) 1);
		accleration = performanceFigure_2.getGroupArray(performanceFigure_2.getField("acceleration"));
		newAccleration = accleration.getGroupObject(0);
		Assert.assertEquals(120, newAccleration.getNumber(newAccleration.getField("mph")));
		Assert.assertEquals(14.1f, newAccleration.getNumber(newAccleration.getField("seconds")));
	}	

	/**
	 * Add a row to a group that contains another group
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void alterExistingSbeMessageByAddingARowToARepeatingGroupThatContainsANestedRepeatingGroup() throws UnsupportedEncodingException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		// copy the second new SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, sbeOffset, 0, buffer, 0, schema);

		// wrap the copied message
		GroupObject msgObj = schema.wrapSbeBuffer(buffer, 0);
		Assert.assertEquals(toBeCreated, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		
		// add a row to the performance figures
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures")); 
		performanceFigures.addGroupObject();
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		int sizeBefore = msgObj.getSize();

		GroupObject newPerformanceFigure = performanceFigures.addGroupObject();
		newPerformanceFigure.setNumber(newPerformanceFigure.getField("octaneRating"), 89);
		Assert.assertEquals(sizeBefore+newPerformanceFigure.getSize(), msgObj.getSize());

		// re-warp message to verify the re-wrap produces the same result
		String unparsedMsg = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, 0);
		Assert.assertEquals(unparsedMsg, MessageUtil.toJsonString(msgObj));

		// populate the nested subgroup
		performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures")); 
		newPerformanceFigure = performanceFigures.getGroupObject(2);
		GroupObjectArray acclerations = newPerformanceFigure.getGroupArray(newPerformanceFigure.getField("acceleration"));
		GroupObject newAccleration = acclerations.addGroupObject();
		newAccleration.setNumber(newAccleration.getField("mph"), 120);
		newAccleration.setNumber(newAccleration.getField("seconds"), 14.1);

		// re-warp message to verify the re-wrap produces the same result
		unparsedMsg = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, 0);
		Assert.assertEquals(unparsedMsg, MessageUtil.toJsonString(msgObj));

		// verify the newly added value
		performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures")); 
		newPerformanceFigure = performanceFigures.getGroupObject(2);
		acclerations = newPerformanceFigure.getGroupArray(newPerformanceFigure.getField("acceleration"));
		newAccleration = acclerations.getGroupObject(0);
		Assert.assertEquals(120, newAccleration.getNumber(newAccleration.getField("mph")));
		Assert.assertEquals(14.1f, newAccleration.getNumber(newAccleration.getField("seconds")));
	}
	
	@Test
	public void deleteExistingSbeGroup() throws UnsupportedEncodingException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int offset = 127;
		
		// copy the first SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, sbeOffset, 0, buffer, offset, schema);

		// wrap the copied message
		GroupObject msgObj = schema.wrapSbeBuffer(buffer, offset);
		int sizeBefore = msgObj.getSize();
		
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField("fuelFigures"));
		Assert.assertEquals(2, fuelFigures.getNumOfGroups());
		fuelFigures.deleteGroupObject(1);

		String beforeWrap = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(beforeWrap, MessageUtil.toJsonString(msgObj));
		Assert.assertEquals(sizeBefore-12, msgObj.getSize());
		
		// delete all
		fuelFigures.deleteGroupObject(0);
		Assert.assertEquals(0, fuelFigures.getNumOfGroups());
		
		// delete the nested repeating group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures")); 
		performanceFigures.addGroupObject();
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		GroupObject performanceFigure = performanceFigures.getGroupObject(0);
		GroupObjectArray acclerations = performanceFigure.getGroupArray(performanceFigure.getField("acceleration"));
		GroupObject newAccleration = acclerations.addGroupObject();
		newAccleration.setNumber(newAccleration.getField("mph"), 120);
		newAccleration.setNumber(newAccleration.getField("seconds"), 14.1);
		Assert.assertEquals(2, acclerations.getNumOfGroups());
		
		acclerations.deleteGroupObject(0);

		GroupObject accleration = acclerations.getGroupObject(0);
		Assert.assertEquals(120, accleration.getNumber(accleration.getField("mph")).intValue());
		Assert.assertEquals(14.1, accleration.getNumber(accleration.getField("seconds")).floatValue(), 0.01);
		acclerations.deleteGroupObject(0);

		beforeWrap = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(beforeWrap, MessageUtil.toJsonString(msgObj));
		
		// delete the performance figure 
		performanceFigures = msgObj.getGroupArray(msgObj.getField("performanceFigures")); 
		performanceFigures.deleteGroupObject(0);
		
		// add a new performance figure
		GroupObject newPerformanceFigure = performanceFigures.addGroupObject();
		newPerformanceFigure.setNumber(newPerformanceFigure.getField("octaneRating"), 89);
		beforeWrap = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(beforeWrap, MessageUtil.toJsonString(msgObj));
	}

	@Test
	public void addRemoveRawField() throws UnsupportedEncodingException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		int offset = 672;
		
		// copy the 2nd SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, sbeOffset, 0, buffer, offset, schema);

		// wrap the copied message
		GroupObject msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(toBeCreated, MessageUtil.toJsonString(msgObj, Charset.defaultCharset().name()));
		
		byte[] newModel = "Honda Accord".getBytes();
		msgObj.setBytes(msgObj.getField("model"), newModel, 0, newModel.length);
		Assert.assertEquals("Honda Accord", msgObj.getString(msgObj.getField("model")));
		Assert.assertEquals("deadbeef", msgObj.getString(msgObj.getField("activationCode")));

		newModel = "Accord".getBytes();
		msgObj.setBytes(msgObj.getField("model"), newModel, 0, newModel.length);
		Assert.assertEquals("Accord", msgObj.getString(msgObj.getField("model")));
		Assert.assertEquals("deadbeef", msgObj.getString(msgObj.getField("activationCode")));

		// delete all var fields
		msgObj.setBytes(msgObj.getField("make"), newModel, 0, 0);
		msgObj.setBytes(msgObj.getField("model"), newModel, 0, 0);
		msgObj.setBytes(msgObj.getField("activationCode"), newModel, 0, 0);
		Assert.assertNull(msgObj.getString(msgObj.getField("model")));
		Assert.assertNull(msgObj.getString(msgObj.getField("activationCode")));

		String beforeWrap = MessageUtil.toJsonString(msgObj);
		msgObj = schema.wrapSbeBuffer(buffer, offset);
		Assert.assertEquals(beforeWrap, MessageUtil.toJsonString(msgObj));
		
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField("fuelFigures"));
		GroupObject fuelFigure = fuelFigures.getGroupObject(1);
		Assert.assertNull(fuelFigure.getString(fuelFigure.getField("usageDescription")));
		fuelFigure.setBytes(fuelFigure.getField("usageDescription"), "Value now".getBytes(), 0, 9);
		fuelFigure = fuelFigures.getGroupObject(0);
		Assert.assertEquals("this is a description of the usage", fuelFigure.getString(fuelFigure.getField("usageDescription")));
		fuelFigure.setBytes(fuelFigure.getField("usageDescription"), null, 0, 0);
		fuelFigure = fuelFigures.getGroupObject(1);
		Assert.assertEquals("Value now", fuelFigure.getString(fuelFigure.getField("usageDescription")));		
	}
	
	/**
	 * Build a message based upon the provided GroupObject. The GroupObject can be based upon a ByteArray or a direct buffer.
	 * 
	 * @param msgObj
	 * @throws UnsupportedEncodingException
	 */
	public static void createEmptyMessage(GroupObject msgObj) throws UnsupportedEncodingException {
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
		
		System.out.println("Actual  : "+MessageUtil.toJsonString(msgObj));
		System.out.println("Expected: "+toBeCreated);
	}
}
