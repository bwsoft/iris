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
package com.github.bwsoft.iris.message;

/**
 * Defines all supported types. The size is defined as the number of bytes
 * that requires to hold this field. A size of zero indicates the size of the field is determined
 * dynamically.
 * 
 * @author yzhou
 *
 */
public enum FieldType {
	/**
	 * A type of an unsigned number of 8 bits. It can be a number or an array of numbers when length &gt; 1. 
	 */
	U8(1),
	/**
	 * A type of an unsigned number of 16 bits. It can be a number or an array of numbers when length &gt; 1. 
	 */
	U16(2),
	/**
	 * A type of an unsigned number of 32 bits. It can be a number or an array of numbers when length &gt; 1. 
	 */
	U32(4),
	/**
	 * A type of an unsigned number of 64 bits. It can be a number or an array of numbers when length &gt; 1. 
	 */
	U64(8),
	/**
	 * A type of a signed number of 8 bits. It can be a number or an array of numbers when length &gt; 1. 
	 */
	I8(1),
	/**
	 * A type of a signed number of 16 bits. It can be a number or an array of numbers when length &gt; 1. 
	 */
	I16(2),
	/**
	 * A type of a signed number of 32 bits. It can be a number or an array of numbers when length &gt; 1. 
	 */
	I32(4),
	/**
	 * A type of a signed number of 64 bits. It can be a number or an array of numbers when length &gt; 1. 
	 */
	I64(8),
	/**
	 * A type of a float number of 4 bytes. It can be a number or an array of numbers when length &gt; 1. 
	 */
	FLOAT(4),
	/**
	 * A type of a double number of 8 bytes. It can be a number or an array of numbers when length &gt; 1. 
	 */
	DOUBLE(8),
	/**
	 * A type of a character. It can be one character or an array of characters when length &gt; 1. 
	 */
	CHAR(1),
	/**
	 * A type of a byte. It can be one byte or an array of bytes when length &gt; 1. 
	 */
	BYTE(1),
	/**
	 * The value of which is never changed from one message to another.
	 */
	CONSTANT(0),   
	/**
	 * A variable length byte array.
	 * It always has a length of 1 and cannot be an array.
	 */
	RAW(0),
	/**
	 * A field that contains other simple field that is not a GROUP, MESSAGE, COMPOSITE, and/or RAW.
	 */
	COMPOSITE(0),
	/**
	 * A field that contains other fields including GROUPs.
	 * It always has a length of 1 and cannot be an array.
	 */
	GROUP(0),
	/**
	 * A special GROUP that is at the root level. 
	 * It always has a length of 1 and cannot be an array.
	 */
	MESSAGE(0);
	
	private int length;
	
	private FieldType(int len) {
		length = len;
	}
	
	/**
	 * @return the storage size of this field. A size of zero indicates the storage size 
	 * of the field is determined dynamically. 
	 */
	public int size() {
		return length;
	}
	
	/**
	 * @param name the type name in its string representation
	 * @return the type
	 */
	public static FieldType getType(String name) {
		switch(name.toLowerCase()) {
		case "int8":
			return FieldType.I8;
		case "int16":
			return FieldType.I16;
		case "int32":
			return FieldType.I32;
		case "int64":
			return FieldType.I64;
		case "uint8":
			return FieldType.U8;
		case "uint16":
			return FieldType.U16;
		case "uint32":
			return FieldType.U32;
		case "uint64":
			return FieldType.U64;
		case "char":
			return FieldType.CHAR;
		case "byte":
			return FieldType.BYTE;
		case "float":
			return FieldType.FLOAT;
		case "double":
			return FieldType.DOUBLE;
		default:
			return null;
		}
	}
}
