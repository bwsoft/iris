package com.bwsoft.iris.util;

import java.nio.ByteBuffer;

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.GroupObject;
import com.bwsoft.iris.message.GroupObjectArray;
import com.bwsoft.iris.message.SBEMessageSchema;
import com.bwsoft.iris.message.sbe.SBECompositeField;
import com.bwsoft.iris.message.sbe.SBEField;
import com.bwsoft.iris.message.sbe.SBEGroup;
import com.bwsoft.iris.message.sbe.SBEMessage;

public class MessageUtil {

	/**
	 * Copy a message to a byte buffer
	 * 
	 * @param original
	 * @param startOffset
	 * @param nth
	 * @param dest
	 * @param destOffset
	 * @param schema
	 */
	public static void messageCopy(ByteBuffer original, int startOffset, int nth,
			ByteBuffer dest, int destOffset, SBEMessageSchema schema) {
		if( dest.hasArray() ) {
			messageCopy(original, startOffset, nth, dest.array(), destOffset, schema);
		}
		
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();
		}
		
		GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();

		original.position(startOffset);
		
		byte[] array = new byte[msgLength];
		original.slice().get(array, 0, msgLength);
		
		dest.position(destOffset);
		dest.slice().put(array,0,msgLength);
	}
	
	/**
	 * Copy a SBE message to a byte array
	 * 
	 * @param original a buffer containing one or more SBE messages
	 * @param startOffset the start offset of the first message
	 * @param nth  nth message in this byte array. Starts from zero for the first message.
	 * @param dest
	 * @param destOffset
	 * @param schema schema to parse this message
	 */
	public static void messageCopy(ByteBuffer original, int startOffset, int nth,
			byte[] dest, int destOffset, SBEMessageSchema schema) {
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();
		}
		
		GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();

		original.position(startOffset);
		original.slice().get(dest, destOffset, msgLength);
	}
	
	/**
	 * Copy a SBE message to a byte array
	 * 
	 * @param original a buffer containing one or more SBE messages
	 * @param startOffset the start offset of the first message
	 * @param nth  nth message in this byte array. Starts from zero for the first message.
	 * @param dest
	 * @param destOffset
	 * @param schema schema to parse this message
	 */
	public static void messageCopy(byte[] original, int startOffset, int nth,
			byte[] dest, int destOffset, SBEMessageSchema schema) {
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();
		}

		GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();

		System.arraycopy(original, startOffset, dest, destOffset, msgLength);
	}
	
	/**
	 * Create a Json expression for a GroupObject, including all of the nested
	 * subgroups.
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJsonString(GroupObject obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		SBEGroup group = (SBEGroup) obj.getDefinition();
		boolean addComma = false;
		for( Field field : group.getChildFields() ) {
			if( addComma ) 
				sb.append(",");
			else
				addComma = true;
			switch( field.getType() ) {
			case I8:
			case U8:
			case I16:
			case U16:
			case I32:
			case U32:
			case I64:
			case U64:
			case FLOAT:
			case DOUBLE:
			case CHAR:
			case BYTE:
			case RAW:
			case CONSTANT:
				sb.append(simpleFieldToJsonElement(obj,field));
				break;
				
			case GROUP:
				sb.append("\"").append(field.getName()).append("\"").append(":");
				sb.append(toJsonString(obj.getGroupArray(field)));
				break;
				
			case COMPOSITE:
				sb.append("\"").append(field.getName()).append("\"").append(":");
				SBECompositeField compField = (SBECompositeField) field;
				sb.append("{");
				boolean addComma1 = false;
				for( Field cfield : compField.getChildFields() ) {
					if( addComma1 ) 
						sb.append(",");
					else
						addComma1 = true;
					sb.append(simpleFieldToJsonElement(obj,cfield));
				}
				sb.append("}");
				break;
				
			default:
				sb.append("\"").append(field.getName()).append("\"").append(":");
				sb.append("\"INTERNAL ERROR - UNPROCESSED FIELD TYPE\"");
			}
		}
		sb.append("}");
		return sb.toString();		
	}

	/**
	 * Create a Json expression for a GroupObjectArray, including all of the nested
	 * subgroups.
	 * 
	 * @param array
	 * @return
	 */
	public static String toJsonString(GroupObjectArray array) {
		StringBuilder sb = new StringBuilder();
		int dimmension = array.getNumOfGroups();
		if( dimmension > 1 ) sb.append("[");
		if( dimmension > 0 ) {
			sb.append(toJsonString(array.getGroupObject(0)));
			for( int i = 1; i < dimmension; i ++ ) {
				sb.append(",").append(toJsonString(array.getGroupObject(i)));
			}
		} else {
			sb.append("null");
		}
		if( dimmension > 1 ) sb.append("]");
		return sb.toString();		
	}
	
	/**
	 * Create the JSON expression for a simple field in the format of "fieldname":"string value",
	 * or "fieldname" : number, or "fieldname" : null.
	 * 
	 * @param obj
	 * @param field
	 * @return
	 */
	private static String simpleFieldToJsonElement(GroupObject obj, Field field) {
		StringBuilder sb = new StringBuilder();

		sb.append("\"").append(field.getName()).append("\"").append(":");
		switch( field.getType() ) {
		case I8:
		case U8:
		case I16:
		case U16:
		case I32:
		case U32:
		case I64:
		case U64:
		case FLOAT:
		case DOUBLE:
			String value = obj.getString(field);
			if( ((SBEField) field).isEnumField() ) {
				value = ((SBEField) field).getEnumName(value);
			}
			sb.append(value);
			break;
			
		case CHAR:
			if( ((SBEField) field).isEnumField() ) {
				sb.append(((SBEField) field).getEnumName(obj.getString(field)));
				break;
			} // if not, handle the same way as BYTE
		case BYTE:
			sb.append("\"").append(obj.getString(field)).append("\"");
			break;

		case RAW:
			String rawField = obj.getString(field);
			if( null != rawField ) 
				sb.append("\"").append(obj.getString(field)).append("\"");
			else
				sb.append("null");
			break;
					
		case CONSTANT:
			if( ((SBEField) field).getConstantType() == FieldType.BYTE ||
					((SBEField) field).getConstantType() == FieldType.CHAR )
				sb.append("\"").append(((SBEField) field).getConstantValue()).append("\"");
			else
				sb.append(((SBEField) field).getConstantValue());
			break;
			
		default:
			sb.append("\"OPAQUE\"");
			break;
		}	
		return sb.toString();
	}
}
