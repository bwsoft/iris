package com.bunny.iris.message;

public interface FieldValue {
	public Field getField();
	public void setField(Field field);
	
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
