package com.bunny.iris.message;

public interface Field {
	public static final short DEFAULT_MAX_REPEATS = 256;

	public short getID();
	public Field setID(short id);
	
	public String getName();
	public Field setName(String name);
	
	public FieldType getType();
	public Field setType(FieldType type);	
		
	/**
	 * @return the array size of each occurrence in the message 
	 */
	public short getArraySize();
	public Field setArraySize(short repeats);

	public Field getChild(short id);
	public Field addChild(FieldType type);
	
	public Field getParent();
	public Field setParent(Field parent);
	
	/**
	 * @param n nth occurrence.
	 * @return the value of nth occurrence
	 */
	public FieldValue getOccurrence(short n);
	public FieldValue allocateOccurrence(boolean isNode);
	public short getTotalOccurrence();
}
