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
package com.bwsoft.iris.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.Group;
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
	 * @return true if copy is successful
	 */
	public static boolean messageCopy(ByteBuffer original, int startOffset, int nth,
			ByteBuffer dest, int destOffset, SBEMessageSchema schema) {
		if( dest.hasArray() ) {
			return messageCopy(original, startOffset, nth, dest.array(), destOffset, schema);
		}
		
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
			if( msgObj == null )
				return false;
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
		return true;
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
	 * @return true if copy is successful
	 */
	public static boolean messageCopy(ByteBuffer original, int startOffset, int nth,
			byte[] dest, int destOffset, SBEMessageSchema schema) {
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
			if( null == msgObj )
				return false;
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();
		}
		
		GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();

		original.position(startOffset);
		original.slice().get(dest, destOffset, msgLength);
		return true;
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
	 * @return true if copy is successful
	 */
	public static boolean messageCopy(byte[] original, int startOffset, int nth,
			byte[] dest, int destOffset, SBEMessageSchema schema) {
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
			if( null == msgObj )
				return false;
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();
		}

		GroupObject msgObj = schema.wrapSbeBuffer(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();

		System.arraycopy(original, startOffset, dest, destOffset, msgLength);
		return true;
	}

	/**
	 * Create a Json expression for a GroupObject, including all of the nested
	 * subgroups. Handle byte arrays as String using platform default encoding type.
	 * 
	 * @param obj
	 * @return
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
	 * @param obj
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String toJsonString(GroupObject obj, String encodingType) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		SBEGroup group = (SBEGroup) obj.getDefinition();
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
				SBECompositeField compField = (SBECompositeField) field;
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
	 * @param array
	 * @return
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
	 * @param array
	 * @return
	 * @throws UnsupportedEncodingException 
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
	 * @param group
	 * @return
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
			if( ((SBEField) field).isEnumField() ) {
				value = ((SBEField) field).getEnumName(value);
			}
			sb.append(value);
			break;
			
		case CHAR:
			if( ((SBEField) field).isEnumField() ) {
				sb.append(((SBEField) field).getEnumName(obj.getString(field,encodingType)));
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
