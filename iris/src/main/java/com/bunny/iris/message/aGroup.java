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
public interface aGroup extends aField {
	public List<aField> getChildFields();
	public aField getChildField(short id);
	public aField addChildField(short id, FieldType type, short dimmension);	

	public default short getDimension() {
		return 1;
	}
}
