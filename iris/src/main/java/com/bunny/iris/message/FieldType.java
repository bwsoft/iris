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
	CHAR(1),
	BYTE(1),
	RAW(0),
	COMPOSITE(0),
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
		default:
			return null;
		}
	}
}
