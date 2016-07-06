package com.bunny.iris.message;

import java.util.List;

/**
 * A group is a special field that contains other fields and/or nested groups.
 * 
 * A message is a group without parent.
 * 
 * @author yzhou
 *
 */
public interface Group extends Field {
	public List<Field> getChildFields();
	/**
	 * @param id
	 * @return
	 */
	public Field getChildField(short id);
	/**
	 * The factory class to add a field to this group. It returns the added Field object 
	 * for application to further modify its attributes.
	 * 
	 * id has to be unique among direct children of this group. The field is considered to be 
	 * an array by default. If not, specify the arrayLength to be 1.
	 * 
	 * @param id the id of the field
	 * @param type
	 * @param arrayLength
	 * @return
	 */
	public Field addChildField(short id, FieldType type, short arrayLength);	

	public default short length() {
		return 1;
	}
}
