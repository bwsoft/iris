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

import java.io.UnsupportedEncodingException;

/**
 * A GroupObject is a section in a message that is defined by a group. Use 
 * {@link #getDefinition()} to obtain the definition for this message section.
 * Use one of the set/get method to set/modify or retrieve the values of fields in this
 * section.
 * 
 * To get/set a value of a field in the group, do
 * 
  * <pre>
 * {@code
 *     // Obtain the handle to the GroupObject, typically from its parent GroupObject
 *     GroupObject groupObj = ...;
 *     
 *     // Get the definition of the GroupObject 
 *     Group group = (Group) groupObj.getDefinition();
 *     
 *     // Get the field by name
 *     Field aNumberField = group.getField("field-name");
 *     
 *     // Get the field by ID (preferred method)
 *     Field aByteArrayField = group.getField(id); 
 *     
 *     // Use one of the get method to obtain the value 
 *     Number aNumber = groupObj.getNumber(aNumberField);
 *     
 *     // Or use one of the set method to set the value
 *     groupObj.setBytes(aByteArrayField, srcByteArray, offset, length);
 *     ...
 * }
 * </pre>
 *  
 * @author yzhou
 *
 */
public interface GroupObject {
	
	/**
	 * Obtain the definition of this group object. 
	 * 
	 * @return the definition of this group object. 
	 */
	public Field getDefinition();
	
	/**
	 * Get the definition of a field that belongs to this group. 
	 * 
	 * @param id the field id 
	 * @return the field definition or an IllegalArgumentException is thrown if the field does 
	 * not belong to this group.  
	 */
	public Field getField(short id);
	
	/**
	 * The storage size of the current GroupObject. It does not include the size of 
	 * header if there is.
	 * 
	 * @return size of the value storage of this GroupObject
	 */
	public int getSize();

	/**
	 * Retrieve this group object in its byte representation excluding its header.
	 * 
	 * @param dest the destination buffer to hold bytes
	 * @param destOffset offset in the buffer
	 * @param length maximum length to retrieve
	 * @return number of bytes retrieved. It is the minimal between the length provided or
	 * the total available length.
	 */
	public int getBytes(byte[] dest, int destOffset, int length);

	/**
	 * The storage size of a child field. It does not include the size of 
	 * header if there is.
	 * 
	 * For a fixed size field, the size is equal to the size of the field type multiplied by its dimension.
	 * For a constant field, the size is zero. 
	 *  
	 * An IllegalArgumentException will be thrown if field does not belong to this group.
	 * 
	 * @param field the field in this group
	 * @return the storage size of the field
	 */
	public int getSize(Field field);
	
	/**
	 * This is an unprotected method to return a character value of a field in this group. The
	 * method will not check if the field type is CHAR. 
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 */
	public char getChar(Field field);
	
	/**
	 * This is an unprotected method to set a character value to a field in this group. The
	 * method will not check if the field type is CHAR. 
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param c value to be set
	 */
	public void setChar(Field field, char c);

	/**
	 * This is an unprotected method to return a byte value of a field in this group. The
	 * method will not check if the field type is BYTE. 
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 */
	public byte getByte(Field field); 
	
	/**
	 * This is an unprotected method to set a byte value to a field in this group. The
	 * method will not check if the field type is BYTE. 
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param value value to be set
	 */
	public void setByte(Field field, byte value);

	/**
	 * Return a number of this field. 
	 * 
	 * byte, I8, U8, I16 needs to have a short type at minimal. U16, I32 need to have an int
	 * as minimal. U32, I64, U64 requires a long type.
	 * 
	 * Apply this method to other type of fields including constant field result in an IllegalArgumentException.
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 */
	public Number getNumber(Field field);
	
	/**
	 * Set a target field in this group with a number value. 
	 * 
	 * byte, I8, U8, I16 needs to have a short type at minimal. U16, I32 need to have an int
	 * as minimal. U32, I64, U64 requires a long type.
	 * 
	 * Apply this method to other type of fields including constant field result in an IllegalArgumentException.
	 * 
	 * @param field the field in this group
	 * @param value value to be set
	 */
	public void setNumber(Field field, Number value);
	
	/**
	 * This is an unprotected method to return an integer value of a field in this group.
	 * This method can only be applied to fields of type U16. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 */ 
	public int getU16(Field field);

	/**
	 * This is an unprotected method to return a short value of a field in this group.
	 * This method can only be applied to fields of type I16. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 */
	public short getI16(Field field);
	
	/**
	 * This is an unprotected method to return an integer value of a field in this group.
	 * This method can only be applied to fields of type I32. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 */
	public int getInt(Field field);

	/**
	 * This is an unprotected method to return a long value of a field in this group.
	 * This method can only be applied to fields of type U32. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 */
	public long getU32(Field field);
	
	/**
	 * This is an unprotected method to return a long value of a field in this group.
	 * This method can only be applied to fields of type U64 or I64. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 * 
	 */
	public long getLong(Field field);
	
	/**
	 * This is an unprotected method to return a float value of a field in this group.
	 * This method can only be applied to fields of type FLOAT. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 */
	public float getFloat(Field field);
	
	/**
	 * This is an unprotected method to return a double value of a field in this group.
	 * This method can only be applied to fields of type DOUBLE. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @return the value of this field
	 */
	public double getDouble(Field field);
	
	/**
	 * This is an unprotected method to return a character array of a field in this group. The
	 * method will not check if the field type is CHAR. 
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of characters retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getChars(Field field, char[] dest, int destOffset, int length);
	
	/**
	 * This is an unprotected method to set value to a character array of a field in this group. 
	 * The method will not check if the field type is CHAR. 
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param src the source array that contains value to be set
	 * @param srcOffset the starting position in the source array
	 * @param length the number of characters that are set.
	 * @return the number of characters retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int setChars(Field field, char[] src, int srcOffset, int length);
	
	/**
	 * Return a byte array representation of the field. All types of fields 
	 * can be converted into a byte array. It is purely the byte copy of the field content 
	 * excluding the field header if there is any.
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of bytes retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getBytes(Field field, byte[] dest, int destOffset, int length);
	
	/**
	 * Set bytes to a destination field in this group. If the target field size is 
	 * longer than the src, all bytes are copied and the remaining bytes in the target 
	 * field is untouched. If the target field size is shorter than the src, the byte 
	 * array is truncated. If the target field is a raw field, the src is copied as it is 
	 * with the raw field size adjusted automatically. It is effectively to remove the raw
	 * field from the message by setting it to a zero length. 
	 *  
	 * @param field the field in this group
	 * @param src the source array that contains value to be set
	 * @param srcOffset the starting offset of the source array
	 * @param length the length attempted to be set
	 * @return the number of bytes that is set. It is minimal between the length and the
	 * storage size.
	 */
	public int setBytes(Field field, byte[] src, int srcOffset, int length);
	
	/**
	 * Adjust the size of a raw field without modifying its content. If the underlying field
	 * is not a raw field, an UnsupportedOperationException is thrown.
	 * 
	 * @param field a raw field
	 * @param length its new length
	 */
	public void adjustSizeForRawField(Field field, int length);

	/**
	 * Return a number array of this field. The inference generic type needs to be able to hold the
	 * returned value. Otherwise a cast exception will be thrown.
	 * 
	 * byte, I8, U8, I16 needs to have a short type at minimal. U16, I32 need to have an int
	 * as minimal. U32, I64, U64 requires a long type.
	 * 
	 * Apply this method to other type of fields including constant fields result in an IllegalArgumentException.
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of Numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getNumbers(Field field, Number[] dest, int destOffset, int length);
	
	/**
	 * Set numbers to a destination field in this group. If the target field size is 
	 * longer than the src, all numbers are copied and the remaining numbers in the target 
	 * field is untouched. If the target field size is shorter than the src, the number 
	 * array is truncated. The method is only applicable to a number type of a field. 
	 * 
	 * An attempt to set a larger value in a smaller storage place will result in an 
	 * error that may not be reported.
	 *  
	 * @param field the subfield
	 * @param src the source array that contains value to be set
	 * @param srcOffset the starting offset of the source array
	 * @param length the length attempted to be set
	 * @return the number of numbers that is set. It is the minimal between the length and the
	 * storage size.
	 */
	public int setNumbers(Field field, Number[] src, int srcOffset, int length);
	
	/**
	 * This is an unprotected method to return a short array of a field in this group.
	 * This method can only be applied to fields of type U8. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group.
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getU8Array(Field field, short[] dest, int destOffset, int length);

	/**
	 * This is an unprotected method to return a short array of a field in this group.
	 * This method can only be applied to fields of type I8. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getI8Array(Field field, short[] dest, int destOffset, int length);

	/**
	 * This is an unprotected method to return an integer array of a field in this group.
	 * This method can only be applied to fields of type U16. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getU16Array(Field field, int[] dest, int destOffset, int length);

	/**
	 * This is an unprotected method to return an integer array of a field in this group.
	 * This method can only be applied to fields of type I16. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getI16Array(Field field, short[] dest, int destOffset, int length);

	/**
	 * This is an unprotected method to return an integer array of a field in this group.
	 * This method can only be applied to fields of type I32. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getIntArray(Field field, int[] dest, int destOffset, int length);

	/**
	 * This is an unprotected method to return a long array of a field in this group.
	 * This method can only be applied to fields of type U32. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getU32Array(Field field, long[] dest, int destOffset, int length);

	/**
	 * This is an unprotected method to return a long array of a field in this group.
	 * This method can only be applied to fields of type U64 or I64. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getLongArray(Field field, long[] dest, int destOffset, int length);

	/**
	 * This is an unprotected method to return a float array of a field in this group.
	 * This method can only be applied to fields of type FLOAT. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getFloatArray(Field field, float[] dest, int destOffset, int length);

	/**
	 * This is an unprotected method to return a double array of a field in this group.
	 * This method can only be applied to fields of type DOUBLE. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param field the field in this group
	 * @param dest the destination array to hold values
	 * @param destOffset the destination array starting offset
	 * @param length max attempted length to be retrieved
	 * @return the number of numbers retrieved. It is the minimal between the length and 
	 * the available length in the message.
	 */
	public int getDoubleArray(Field field, double[] dest, int destOffset, int length);
	
	/**
	 * This applies to an enum field. The primitive type of the enum field is a number or 
	 * a char. The method returns the corresponding enum name based upon the value contained in the 
	 * SBE message.
	 * 
	 * This is a convenient method. For better performance, use a get method to obtain the 
	 * value of the enum directly.
	 *  
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is not an enum type.
	 * 
	 * @param field the field in this group
	 * @return the enum name based upon the value in the sbe message.
	 */
	public String getEnumName(Field field);
	
	/**
	 * Test if a bitName is set. Bit name of a choice is defined in the SBE XML.
	 *  
	 * This is a convenient method. For better performance, use a get method to obtain the 
	 * value of the choice directly.
	 *  
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is not a choice type.
	 * 
	 * @param field the choice field
	 * @param bitName the bitname defined in the SBE XML for the current choice.
	 * @return true if set.
	 */
	public boolean isSet(Field field, String bitName);	
	
	/**
	 * This is applicable to all type of fields. If the field is a number/raw, it returns its 
	 * string representation. 
	 * 
	 * It is not designed for high performance usage. Use getBytes instead.
	 * 
	 * @param field the field in this group
	 * @param encodingType the encoding type to convert byte array
	 * @return the string representation of this field
	 * @throws UnsupportedEncodingException if the encoding type is not recognized or supported
	 */
	public String getString(Field field, String encodingType)  throws UnsupportedEncodingException;
	
	/**
	 * This is applicable to all type of fields. If the field is a number/raw, it returns its 
	 * string representation. It uses the platform default charset encoding type.
	 * 
	 * It is not designed for high performance usage. Use getBytes instead.
	 * 
	 * @param field the field in this group
	 * @return the string representation of this field
	 */
	public String getString(Field field);
	
	/**
	 * Obtain a GroupObjectArray. The field has to be a type of GROUP or an IllegalArgumentException
	 * will be thrown.
	 * 
	 * @param field the field in this group
	 * @return the GroupObjectArray for the corresponding repeating group field. 
	 */
	public GroupObjectArray getGroupArray(Field field);
}
