package com.github.bwsoft.iris.sample;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.BitSet;

import javax.xml.bind.JAXBException;

import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.Group;
import com.github.bwsoft.iris.message.GroupObject;
import com.github.bwsoft.iris.message.GroupObjectArray;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.util.MessageUtil;

/**
 * There is a certain relationship beween SBE and FIX. SBE is an attempt to 
 * improve the parsing performance over the traditional FIX message. There may be 
 * a case that it needs to convert SBE back into FIX.
 * 
 * This kind of conversion may not be able to happen all the time. In case, SBE is designed
 * to be upon a FIX message, this demo shows how to convert it back to FIX by looping through
 * all SBE fields. It is only for Demo purpose. No header fields are provided in the 
 * generated FIX message. 
 * 
 * @author yzhou
 *
 */
public class ConvertSBEToFixLikeMessage {
	
	private static final String xmlSchema = "src/main/resources/example-schema.xml";
	private static final char ctrlA = '\u0001';
	
	public static void main(String[] args) throws FileNotFoundException, JAXBException, UnsupportedEncodingException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		// create a SBE message
		GroupObject msgObj = ConvertSBEToFixLikeMessage.encode(buffer, 0);
		
		// A string builder to build fix string
		StringBuilder sb = new StringBuilder();
		
		// TODO: add fix header to sb
		
		// Build fix string 
		MessageUtil.loopThroughAllFields(msgObj, (field,grpObj) -> {
			if( FieldType.GROUP == field.getType() ) {
				int numOfGroup = grpObj.getGroupArray(field).getNumOfGroups();
				if( numOfGroup > 0 )
					sb.append(field.getID()).append("=").append(numOfGroup).append(ctrlA);
			} else if( FieldType.COMPOSITE == field.getType() ) {
				// handle composite as a one row group
				sb.append(field.getID()).append("=").append(1).append(ctrlA);
			} else {
				try {
					sb.append(field.getID()).append("=").append(grpObj.getString(field,"utf-8")).append(ctrlA);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
		
		// add a checksum
		byte[] fixAray = sb.toString().getBytes();
		int sum = 0;
		for( byte each : fixAray ) {
			sum = sum + each;
		}
		sum = sum % 256;
		sb.append(10).append("=").append(sum);
		System.out.println("Fix message: "+sb.toString());
	}
	
	private static GroupObject encode(ByteBuffer buffer, int offset) throws FileNotFoundException, JAXBException, UnsupportedEncodingException {	
		SBEMessageSchema schema = SBEMessageSchema.createSBESchema(xmlSchema);

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
		byte[] model = "Accord".getBytes("utf-8");
		msgObj.setBytes(msgObj.getField((short) 18), model, 0, model.length);

		// Field: make - set up a raw field
		byte[] make = "Honda".getBytes("utf-8");
		msgObj.setBytes(msgObj.getField((short) 17), make, 0, make.length);
		
		// Field: activationCode - set up a raw field
		byte[] activationCode = "deadbeef".getBytes("utf-8");
		msgObj.setBytes(msgObj.getField((short) 19), activationCode, 0, activationCode.length);
		
		// return the total size of the message
		return msgObj;
	}

}
