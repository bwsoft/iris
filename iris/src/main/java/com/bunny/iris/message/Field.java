package com.bunny.iris.message;

import java.io.Serializable;

/**
 * A Field is identified by name and id. It is in a structured group which is a special field
 * that contains other fields and/or groups. The group that this field is in is its parent group.
 * 
 * The field id, field type, its array length, and its parent are determined upon 
 * construction. It is typically created by the group it belongs to.
 * 
 * @author yzhou
 *
 */
public interface Field extends Serializable {
	public short getID();
	public Field setID(short id);
	
	public String getName();
	public Field setName(String name);
	
	public FieldType getType();
	public short length(); // The array size if this field is an array.
	public Group getParent();
}
