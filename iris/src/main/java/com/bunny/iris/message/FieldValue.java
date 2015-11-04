package com.bunny.iris.message;

public interface FieldValue {
	public Field getField();
	public void setField(Field field);
	
	/**
	 * @return the storage size of this field including the size of header, fixed size fields, and all variable length fields. 
	 * The size is the size of the field type multiplied by its dimension in case of a fixed size field. 
	 */
	public int getSize();
	public void setSize(int size);
	
	public char getChar(short repeat);
	public byte getByte(short repeat);
	public int getU16(short repeat);
	public int getInt(short repeat);
	public long getU64(short repeat);
	public int getBytes(byte[] dest, int offset);
	public String getEnumName();
	public boolean isSet(String bitName);
	public String getString(short repeat);
}
