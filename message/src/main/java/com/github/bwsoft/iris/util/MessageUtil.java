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
package com.github.bwsoft.iris.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.github.bwsoft.iris.message.Field;
import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.Group;
import com.github.bwsoft.iris.message.GroupObject;
import com.github.bwsoft.iris.message.GroupObjectArray;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.message.sbe.SBEField;
import com.github.bwsoft.iris.message.sbe.SBEMessage;

public class MessageUtil {

	/**
	 * Copy a message to a byte buffer
	 * 
	 * @param original the original message buffer
	 * @param startOffset the start offset of the first message
	 * @param nth nth message in this byte array. Starts from zero for the first message.
	 * @param dest the destination buffer
	 * @param destOffset the start position in the destination buffer
	 * @param schema schema to parse this message
	 * @return true if copy is successful
	 */
	public static boolean messageCopy(ByteBuffer original, int startOffset, int nth,
			ByteBuffer dest, int destOffset, SBEMessageSchema schema) {		
		// move to the nth message in the source buffer
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
			if( msgObj == null )
				return false;
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getSize();
		}
		
		GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getSize();

		original.position(startOffset);
		
		byte[] array = new byte[msgLength];
		original.get(array, 0, msgLength);
		
		dest.position(destOffset);
		dest.put(array,0,msgLength);
		return true;
	}
	
	/**
	 * Copy a SBE message to a byte array
	 * 
	 * @param original a buffer containing one or more SBE messages
	 * @param startOffset the start offset of the first message
	 * @param nth  nth message in this byte array. Starts from zero for the first message.
	 * @param dest the destination buffer
	 * @param destOffset the start position in the destination buffer
	 * @param schema schema to parse this message
	 * @return true if copy is successful
	 */
	public static boolean messageCopy(ByteBuffer original, int startOffset, int nth,
			byte[] dest, int destOffset, SBEMessageSchema schema) {
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
			if( null == msgObj )
				return false;
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getSize();
		}
		
		GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getSize();

		original.position(startOffset);
		original.get(dest, destOffset, msgLength);
		return true;
	}
	
	/**
	 * Create a Json expression for a GroupObject, including all of the nested
	 * subgroups. Handle byte arrays as String using platform default encoding type.
	 * 
	 * @param obj a GroupObject
	 * @return the Json representation of the GroupObject
	 */
	public static String toJsonString(GroupObject obj) {
		try {
			return toJsonString(obj, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
	 * Create a Json expression for a GroupObject, including all of the nested
	 * subgroups.
	 * 
	 * @param obj the GroupObject
	 * @param encodingType the encoding type to convert a byte array into a String
	 * @return the Json representation of the GroupObject
	 * @throws UnsupportedEncodingException if the encoding type is undefined or not supported
	 */
	public static String toJsonString(GroupObject obj, String encodingType) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		Group group = (Group) obj.getDefinition();
		boolean addComma = false;
		for( Field field : group.getFields() ) {
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
				sb.append(simpleFieldToJsonElement(obj,field, encodingType));
				break;
				
			case GROUP:
				sb.append("\"").append(field.getName()).append("\"").append(":");
				sb.append(toJsonString(obj.getGroupArray(field), encodingType));
				break;
				
			case COMPOSITE:
				sb.append("\"").append(field.getName()).append("\"").append(":");
				Group compField = (Group) field;
				sb.append("{");
				boolean addComma1 = false;
				for( Field cfield : compField.getFields() ) {
					if( addComma1 ) 
						sb.append(",");
					else
						addComma1 = true;
					sb.append(simpleFieldToJsonElement(obj,cfield, encodingType));
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
	 * subgroups. Handle byte arrays as String using platform default encoding type.
	 * 
	 * @param array the GroupObjectArray
	 * @return the Json representation of the GroupObjectArray
	 */
	public static String toJsonString(GroupObjectArray array) {
		try {
			return toJsonString(array, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
	 * Create a Json expression for a GroupObjectArray, including all of the nested
	 * subgroups.
	 * 
	 * @param array the GroupObjectArray
	 * @param encodingType the encoding type to convert a byte array into a string
	 * @return the Json representation of the GroupObjectArray
	 * @throws UnsupportedEncodingException if the encoding type is undefined or not supported
	 */
	public static String toJsonString(GroupObjectArray array, String encodingType) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		int dimmension = array.getNumOfGroups();
		if( dimmension > 1 ) sb.append("[");
		if( dimmension > 0 ) {
			sb.append(toJsonString(array.getGroupObject(0), encodingType));
			for( int i = 1; i < dimmension; i ++ ) {
				sb.append(",").append(toJsonString(array.getGroupObject(i), encodingType));
			}
		} else {
			sb.append("null");
		}
		if( dimmension > 1 ) sb.append("]");
		return sb.toString();		
	}
	
	/**
	 * Convert a group definition to a corresponding JSON expression
	 * 
	 * @param group a Group
	 * @return the Json string representation of the Group
	 */
	public static String toJsonString(Group group) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":").append("\"").append(group.getName()).append("\"");
		sb.append(",\"id\":").append(group.getID());
		sb.append(",\"type\":").append("\"").append(group.getType()).append("\"");
		for( Field field : group.getFields() ) {
			sb.append(",");
			
			if( field instanceof Group ) {
				sb.append(toJsonString((Group) field));
			} else {
				sb.append(toJsonString(field));
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
	private static String toJsonString(Field field) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":").append("\"").append(field.getName()).append("\"");
		sb.append(",\"id\":").append(field.getID());
		sb.append(",\"type\":").append("\"").append(field.getType()).append("\"");
		sb.append(",\"dimension\":").append(field.length());
		sb.append("}");
		return sb.toString();		
	}
	
	/**
	 * Create the JSON expression for a simple field in the format of "fieldname":"string value",
	 * or "fieldname" : number, or "fieldname" : null.
	 * 
	 * @param obj
	 * @param field
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private static String simpleFieldToJsonElement(GroupObject obj, Field field, String encodingType) throws UnsupportedEncodingException {
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
			String value = obj.getString(field,encodingType);
			if( ((SBEField)field).isEnumField() ) {
				value = obj.getEnumName(field);
			}
			sb.append(value);
			break;
			
		case CHAR:
			if( ((SBEField)field).isEnumField() ) {
				sb.append(obj.getEnumName(field));
				break;
			} // if not, handle the same way as BYTE
		case BYTE:
			sb.append("\"").append(obj.getString(field,encodingType)).append("\"");
			break;

		case RAW:
			String rawField = obj.getString(field,encodingType);
			if( null != rawField ) 
				sb.append("\"").append(obj.getString(field,encodingType)).append("\"");
			else
				sb.append("null");
			break;
					
		case CONSTANT:
			if( ((SBEField) field).getConstantType() == FieldType.BYTE ||
					((SBEField) field).getConstantType() == FieldType.CHAR )
				sb.append("\"").append(obj.getString(field)).append("\"");
			else
				sb.append(obj.getString(field));
			break;
			
		default:
			sb.append("\"OPAQUE\"");
			break;
		}	
		return sb.toString();
	}
}
