package com.bunny.iris.message;

import java.util.List;
import java.util.function.Consumer;

public interface Field {
	public static final short DEFAULT_MAX_REPEATS = 256;

	public short getID();
	public Field setID(short id);
	
	public String getName();
	public Field setName(String name);
	
	public FieldType getType();
	public Field setType(FieldType type);	
		
	public short getArraySize();
	public Field setArraySize(short repeats);

	public Field getParent();
	public Field setParent(Field parent);
	
	public Field getMessage();
	
	public List<Field> getChildField();
	/**
	 * Create a field that is a child field of this field.
	 * 
	 * @param type
	 * @return The newly created child field.
	 */
	public Field addChildField(FieldType type);
	
	public short getTotalOccurrence();
	public void getValues(Consumer<FieldValue> consumer);
	public FieldValue getFieldValue(short i);
	
	public void getChildValues(Consumer<FieldValue> consumer);
	public void getChildValues(short occurrence, Consumer<FieldValue> consumer);
}
