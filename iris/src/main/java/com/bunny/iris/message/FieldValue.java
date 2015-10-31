package com.bunny.iris.message;

public interface FieldValue {
	public Field getField();
	public void setField(Field field);
	
	public int getSize();
	public void setSize(int size);
	
	public byte getByte(short repeat);
	public int getU16(short repeat);
	public int getInt(short repeat);
	public long getU64(short repeat);
	public String getString(short repeat);
}
