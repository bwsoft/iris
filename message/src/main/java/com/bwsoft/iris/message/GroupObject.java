/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.bwsoft.iris.message;

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
	 * The storage size of the value of the current GroupObject. It does not include the size of 
	 * header if there is.
	 * 
	 * @return size of the value storage of this field
	 */
	public int getSize();

	/**
	 * Retrieve this group object in its byte representation excluding its header.
	 * 
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
	 */
	public int getBytes(byte[] dest, int destOffset, int length);

	/**
	 * The storage size of the value of a child field. It does not include the size of 
	 * header if there is.
	 * 
	 * For a fixed size field, the size is equal to the size of the field type multiplied by its dimension.
	 * For a constant field, the size is zero. 
	 *  
	 * An IllegalArgumentException will be thrown if field does not belong to this group.
	 * 
	 * @param field
	 * @return
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
	 * @param fieldId
	 * @return
	 */
	public char getChar(Field field);

	/**
	 * This is an unprotected method to return a byte value of a field in this group. The
	 * method will not check if the field type is BYTE. 
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param fieldId
	 * @return
	 */
	public byte getByte(Field field); 

	/**
	 * Return a number of this field. 
	 * 
	 * byte, I8, U8, I16 needs to have a short type at minimal. U16, I32 need to have an int
	 * as minimal. U32, I64, U64 requires a long type.
	 * 
	 * Apply this method to other type of fields including constant field result in an IllegalArgumentException.
	 * 
	 * @param fieldId
	 * @return
	 */
	public Number getNumber(Field field);
	
	/**
	 * This is an unprotected method to return an integer value of a field in this group.
	 * This method can only be applied to fields of type U16. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is a constant field. Use {@link #getString(Field)} to retrieve the value 
	 * of a constant field. 
	 * 
	 * @param fieldId
	 * @return
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
	 * @param fieldId
	 * @return
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
	 * @param fieldId
	 * @return
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
	 * @param fieldId
	 * @return
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
	 * @param fieldId
	 * @return
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
	 * @param fieldId
	 * @return
	 * 
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
	 * @param fieldId
	 * @return
	 * 
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
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
	 */
	public int getChars(Field field, char[] dest, int destOffset, int length);
	
	/**
	 * Return a byte array representation of the field. All types of fields 
	 * can be converted into a byte array. It is purely the byte copy of the field content 
	 * excluding the field header if there is any.
	 * 
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
	 */
	public int getBytes(Field field, byte[] dest, int destOffset, int length);
	
	/**
	 * Return a number array of this field. The inference generic type needs to be able to hold the
	 * returned value. Otherwise a cast exception will be thrown.
	 * 
	 * byte, I8, U8, I16 needs to have a short type at minimal. U16, I32 need to have an int
	 * as minimal. U32, I64, U64 requires a long type.
	 * 
	 * Apply this method to other type of fields including constant fields result in an IllegalArgumentException.
	 * 
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
	 */
	public int getNumbers(Field field, Number[] dest, int destOffset, int length);
	
	/**
	 * This is an unprotected method to return a short array of a field in this group.
	 * This method can only be applied to fields of type U8. However the method 
	 * does not validate the field type but assumes it is the responsibility of the application.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group.
	 * 
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
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
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
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
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
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
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
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
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
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
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
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
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
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
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
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
	 * @param field
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @return
	 */
	public int getDoubleArray(Field field, double[] dest, int destOffset, int length);
	
	/**
	 * This applies to an enum field. The primitive type of the enum field is a number or 
	 * a char. The method returns the corresponding enum name based upon the value contained in the 
	 * SBE message.
	 * 
	 * An IllegalArgumentException will be thrown if field does not belong to this group or
	 * if the field is not an enum type.
	 * 
	 * @param field
	 * @return the enum name based upon the value on the sbe message.
	 */
	public String getEnumName(Field field);
	
	/**
	 * Test if a bitName is set. Bit name of a choice is defined in the SBE XML.
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
	 * @param field
	 * @return
	 */
	public String getString(Field field);
	
	/**
	 * Obtain a GroupObjectArray. The field has to be a type of GROUP or an IllegalArgumentException
	 * will be thrown.
	 * 
	 * @param field
	 * @return
	 */
	public GroupObjectArray getGroupArray(Field field);
}
