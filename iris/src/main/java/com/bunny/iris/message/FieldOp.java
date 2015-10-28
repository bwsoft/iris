package com.bunny.iris.message;

public interface FieldOp {
	public FieldOp bind(FieldValue field);
	
	public byte getByte(short repeat);
	public int getU16(short repeat);
	public int getInt(short repeat);
	public long getU64(short repeat);
	public String getString(short repeat);
}
