package com.bunny.iris.message;

/**
 * A group object is a continuous section in the message. The section contains several 
 * repeated structures, each of which is structured based upon the group definition. getNumOfGroups
 * returns the number of repeated structures.
 * 
 * numOfGroup: points to the nth group in the repeated structure.
 * fieldId: indicates a field in the current number of group. It has to be defined in the group definition. 
 * index: indicates the array index of the field.
 * 
 *  A GroupObject for a message will only contain one repeat of the structure. Hence its getNumOfGroups
 *  returns 1. 
 *  
 * @author yzhou
 *
 */
public interface aGroupObject {
	public aField getDefinition();
	
	/**
	 * @return the storage size of this field including the size of header, fixed size fields, and all variable length fields. 
	 * The size is the size of the field type multiplied by its dimension in case of a fixed size field. 
	 */
	public int getSize();
	
	public char getChar(short fieldId);
	public byte getByte(short fieldId); 
	public <T extends Number> T getNumber(short fieldId, Class<T> type);
	
	public int getU16(short fieldId);
	public int getInt(short fieldId);
	public long getLong(short fieldId);
	public int getChars(short fieldId, char[] dest, int destOffset, int length);
	public int getBytes(short fieldId, byte[] dest, int destOffset, int length);
	public int getU16Array(short fieldId, int[] dest, int destOffset, int length);
	public int getIntArray(short fieldId, int[] dest, int destOffset, int length);
	public int getLongArray(short fieldId, long[] dest, int destOffset, int length);
	public String getEnumName(short fieldId);
	public boolean isSet(short fieldId, String bitName);	
	public String getString(short fieldId);
	
	public aGroupArray getGroupArray(short fieldId);
}
