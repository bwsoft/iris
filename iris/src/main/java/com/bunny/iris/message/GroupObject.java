package com.bunny.iris.message;

public interface GroupObject {
	public Field getDefinition();
	
	/**
	 * The storage size of the value of this field. It does not include the size of 
	 * header if there is.
	 * 
	 * The size is the size of the field type multiplied by its dimension in case of a fixed size field. 
	 *  
	 * @return size of the value storage of this field
	 */
	public int getSize();
	
	/**
	 * Return character field value. IllegalArgumentException will be thrown if field does not
	 * exist.
	 * 
	 * @param fieldId
	 * @return
	 */
	public char getChar(short fieldId);
	public byte getByte(short fieldId); 
	/**
	 * Return a number value of this field. The inference generic type needs to be able to hold the
	 * returned value. Otherwise a cast exception will be thrown.
	 * 
	 * byte, I8, U8, I16 needs to have a short type at minimal. U16, I32 need to have an int
	 * as minimal. U32, I64, U64 requires a long type.
	 * 
	 * Apply this method to other type of fields result in an IllegalArgumentException.
	 * 
	 * @param fieldId
	 * @param type The class of the destination type.
	 * @return
	 */
	public <T extends Number> T getNumber(short fieldId, Class<T> type);
	
	/**
	 * A fast method to retrieve a field of type U16/I16. No check for validation.
	 * 
	 * @param fieldId
	 * @return
	 */
	public int getU16(short fieldId);
	/**
	 * A fast method to retrieve a field of type INT/U32. No check for validation.
	 * 
	 * @param fieldId
	 * @return
	 */
	public int getInt(short fieldId);
	/**
	 * A fast method to retrieve a field of type U64/I64. No check for validation.
	 * 
	 * @param fieldId
	 * @return
	 */
	public long getLong(short fieldId);
	
	public int getChars(short fieldId, char[] dest, int destOffset, int length);
	public int getBytes(short fieldId, byte[] dest, int destOffset, int length);
	public int getU16Array(short fieldId, int[] dest, int destOffset, int length);
	public int getIntArray(short fieldId, int[] dest, int destOffset, int length);
	public int getLongArray(short fieldId, long[] dest, int destOffset, int length);
	public String getEnumName(short fieldId);
	public boolean isSet(short fieldId, String bitName);	
	public String getString(short fieldId);
	
	public GroupObjectArray getGroupArray(short fieldId);
}
