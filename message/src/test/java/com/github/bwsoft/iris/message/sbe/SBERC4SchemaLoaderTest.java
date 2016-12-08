package com.github.bwsoft.iris.message.sbe;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.xml.sax.SAXException;

import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.MsgCodecRuntimeException;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.util.MessageUtil;

/**
 * Test schema loader against RC4 standard. This testing case verifies the following:
 *     1.) All fields are being recognized in the correct sequence
 *     2.) The specified block size attribute is recognized
 *     3.) Verify the calculated block size that is the sum of field lengths and the extra size 
 *     introduced by field paddings.
 *     4.) Verify the calculated block size that is the sum of field lengths and the extra size 
 *     introduced by field paddings using the nested repeating group. 
 *     5.) No offset is allowed to be specified in a field within a composite. Verify the block size of 
 *     a composite field is the sum of field lengths of all its fields.
 *     6.) Verify the loader detects the wrong specified block size that is smaller than required.  
 *       
 * @author yzhou
 *
 */
public class SBERC4SchemaLoaderTest {
	
	/**
	 * Manually convert the example-schemav4 into a JSON expression.
	 * 
	 * @return
	 */
	private static String convertExampleIntoJson() {		
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":").append("\"").append("Car").append("\"");
		sb.append(",");
		sb.append("\"id\":").append(1);
		sb.append(",");
		sb.append("\"type\":").append("\"").append(FieldType.MESSAGE.name()).append("\"");
		sb.append(",");
		sb.append(convertAFieldIntoJson("serialNumber", 1, FieldType.U64.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("modelYear", 2, FieldType.U16.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("available", 3, FieldType.U8.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("code", 4, FieldType.CHAR.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("someNumbers", 5, FieldType.U32.name(), 5));
		sb.append(",");
		sb.append(convertAFieldIntoJson("vehicleCode", 6, FieldType.CHAR.name(), 6));
		sb.append(",");
		sb.append(convertAFieldIntoJson("extras", 7, FieldType.U8.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("discountedModel", 8, FieldType.CONSTANT.name(), 1));
		sb.append(",");
		sb.append("{");
		sb.append("\"name\":").append("\"").append("engine").append("\"");
		sb.append(",");
		sb.append("\"id\":").append(9);
		sb.append(",");
		sb.append("\"type\":").append("\"").append(FieldType.COMPOSITE.name()).append("\"");
		sb.append(",");
		sb.append(convertAFieldIntoJson("capacity", 0, FieldType.U16.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("numCylinders", 0, FieldType.U8.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("maxRpm", 0, FieldType.CONSTANT.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("manufacturerCode", 0, FieldType.CHAR.name(), 3));
		sb.append(",");
		sb.append(convertAFieldIntoJson("fuel", 0, FieldType.CONSTANT.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("booster.BoostType", 0, FieldType.CHAR.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("booster.horsePower", 0, FieldType.U8.name(), 1));
		sb.append("}");
		sb.append(",");
		sb.append("{");
		sb.append("\"name\":").append("\"").append("fuelFigures").append("\"");
		sb.append(",");
		sb.append("\"id\":").append(10);
		sb.append(",");
		sb.append("\"type\":").append("\"").append(FieldType.GROUP.name()).append("\"");
		sb.append(",");
		sb.append(convertAFieldIntoJson("speed", 11, FieldType.U16.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("mpg", 12, FieldType.FLOAT.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("usageDescription", 200, FieldType.RAW.name(), 1));
		sb.append("}");
		sb.append(",");
		sb.append("{");
		sb.append("\"name\":").append("\"").append("performanceFigures").append("\"");
		sb.append(",");
		sb.append("\"id\":").append(13);
		sb.append(",");
		sb.append("\"type\":").append("\"").append(FieldType.GROUP.name()).append("\"");
		sb.append(",");
		sb.append(convertAFieldIntoJson("octaneRating", 14, FieldType.U8.name(), 1));
		sb.append(",");
		sb.append("{");
		sb.append("\"name\":").append("\"").append("acceleration").append("\"");
		sb.append(",");
		sb.append("\"id\":").append(15);
		sb.append(",");
		sb.append("\"type\":").append("\"").append(FieldType.GROUP.name()).append("\"");
		sb.append(",");
		sb.append(convertAFieldIntoJson("mph", 16, FieldType.U16.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("seconds", 17, FieldType.FLOAT.name(), 1));
		sb.append("}");		
		sb.append("}");
		sb.append(",");
		sb.append(convertAFieldIntoJson("make", 18, FieldType.RAW.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("model", 19, FieldType.RAW.name(), 1));
		sb.append(",");
		sb.append(convertAFieldIntoJson("activationCode", 20, FieldType.RAW.name(), 1));
		sb.append("}");
		return sb.toString();
	}
	
	private static String convertAFieldIntoJson(String name, int id, String type, int dimension) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":").append("\"").append(name).append("\"");
		sb.append(",");
		sb.append("\"id\":").append(id);
		sb.append(",");
		sb.append("\"type\":").append("\"").append(type).append("\"");
		sb.append(",");
		sb.append("\"dimension\":").append(dimension);
		sb.append("}");
		return sb.toString();
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
	public final ExpectedException exception = ExpectedException.none();

	/**
	 * This test loads RC4 schema and convert it into JSON expression and compare the expression with the 
	 * manually created JSON expression to ensure the similarity. 
	 *  
	 * Verify the program is handling block size and offset attributes correctly.
	 *  
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	@Test
	public void testSbeSchemaLoaderV4() throws JAXBException, SAXException, ParserConfigurationException, IOException, XMLStreamException, FactoryConfigurationError {
		SBEMessageSchema schema = SBESchemaLoader.loadSchema("src/test/resources/example-schemav4.xml");
		SBEMessage message = schema.getMsgLookup().get(1);
		System.out.println(MessageUtil.toJsonString(message));
		System.out.println(convertExampleIntoJson());
		
		// 1.) ensure the loaded schema is correct by comparing its JSON expression with the 
		// manually created JSON expression
		Assert.assertEquals(convertExampleIntoJson(), MessageUtil.toJsonString(message));

		// 2.) verify the specified message block size is recognized to be 64.
		// Minimal message block size is 53 which includes the sum of field lengths and 
		// extra paddings at field id 3 and 4..
		Assert.assertEquals(64, message.getBlockSize());
		
		// 3.) Verify the calculated block size that is the sum of field lengths and the extra size 
		// introduced by field paddings.
		SBEGroup fuelFigures = (SBEGroup) message.getField("fuelFigures");
		Assert.assertEquals(8, fuelFigures.getBlockSize());
		
		// 4.) Verify the calculated block size that is the sum of field lengths and the extra size 
		// introduced by field paddings using the nested repeating group to do the verification. 
		SBEGroup performanceFigures = (SBEGroup) message.getField("performanceFigures");
		SBEGroup acceleration = (SBEGroup) performanceFigures.getField("acceleration");
		Assert.assertEquals(8,acceleration.getBlockSize());
		
		// 5.) No offset is allowed to be specified in a field within a composite. Verify the block size of 
		// a composite field is the sum of field lengths of all its fields.
		SBEField engine = (SBEField) message.getField("engine");
		Assert.assertEquals(8,engine.getBlockSize());
	}
	
	/**
	 * Verify the loader is exceptioning out upon the detection of a wrongly specified block size at 
	 * the message root level.
	 *  
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	@Test
	public void testSbeSchemaLoaderV4withError() throws FileNotFoundException, JAXBException, XMLStreamException, FactoryConfigurationError {
		exception.expect(MsgCodecRuntimeException.class);
		// 6.) Verify the loader detects the wrong specified block size that is smaller than required.  
		SBESchemaLoader.loadSchema("src/test/resources/example-schemav4-with-error.xml");
	}
}
