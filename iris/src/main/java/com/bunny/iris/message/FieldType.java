package com.bunny.iris.message;

public enum FieldType {
	U8(1),
	U16(2),
	U32(4),
	U64(8),
	I8(1),
	I16(2),
	I32(4),
	I64(8),
	BYTE(1),
	RAW(0),
	GROUP(0),
	MESSAGE(0);
	
	private int length;
	
	private FieldType(int len) {
		length = len;
	}
	
	public int size() {
		return length;
	}
	
	public static FieldType getType(String name) {
		switch(name.toLowerCase()) {
		case "int8":
			return FieldType.I8;
		case "uint8":
			return FieldType.U8;
		default:
			throw new IllegalArgumentException("unrecognized field type: "+name);
		}
	}
}
