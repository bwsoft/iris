package com.bunny.iris.message;

import java.io.Serializable;

/**
 * A Field is identified by name and id. It is in a structured group which is a special field
 * that contains other fields and/or groups. This group is its parent group.
 * 
 * The field type, its array dimension, and its parent are determined upon construction. It
 * is typically created by the group it belongs to.
 * 
 * @author yzhou
 *
 */
public interface aField extends Serializable {
	public short getID();
	public aField setID(short id);
	
	public String getName();
	public aField setName(String name);
	
	public FieldType getType();
	public short getDimension();
	public aGroup getParent();
}
