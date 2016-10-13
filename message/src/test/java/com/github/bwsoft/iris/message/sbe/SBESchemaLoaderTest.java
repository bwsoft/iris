package com.github.bwsoft.iris.message.sbe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.util.MessageUtil;

/**
 * Test schema loader against RC4 standard
 * 
 * @author yzhou
 *
 */
public class SBESchemaLoaderTest {
	
	/**
	 * Manually convert the example-schemav4 into a JSON expression.
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

	@Test
	public void testSbeSchemaLoaderV4() throws JAXBException, SAXException, ParserConfigurationException, IOException {
		SBEMessageSchema schema = SBESchemaLoader.loadSchema("src/test/resources/example-schemav4.xml");
		SBEMessage message = schema.getMsgLookup().get(1);
		System.out.println(MessageUtil.toJsonString(message));
		System.out.println(convertExampleIntoJson());
		Assert.assertEquals(convertExampleIntoJson(), MessageUtil.toJsonString(message));
	}
}
