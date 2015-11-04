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
	public short getDimension();

	public Field getParent();
	public Field setParent(Field parent);
	
	public List<Field> getChildField();
	/**
	 * Create a field that is a child field of this field.
	 * 
	 * @param type
	 * @param dimmension the dimmension of this type.
	 * @return The newly created child field.
	 */
	public Field addChildField(FieldType type, short dimmension);	
	public Field getMessage();
	
	public short getTotalOccurrence();
	/**
	 * Get values of all occurrences in sequence. The accept of the consumer will be invoked for each 
	 * retrieved value. 
	 * 
	 * The retrieved FieldValue cannot be cached by the consumer since it will be populated with the next FieldValue 
	 * in the next retrival. Copy the value in order to save or cache. 
	 * 
	 * @param consumer
	 */
	public void getValues(Consumer<FieldValue> consumer);
	/**
	 * This method is meant to retrieve the value of a particular occurrence. Do not use this method to 
	 * retrieve all values of all occurrences. Use getValues instead. 
	 * 
	 * @param nth the nth occurrence in the field
	 * @return the field value.
	 */
	public FieldValue getFieldValue(short occurrence);	
	public void getChildValues(Consumer<FieldValue> consumer);
}
