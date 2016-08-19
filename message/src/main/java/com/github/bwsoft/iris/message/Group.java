/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package com.github.bwsoft.iris.message;

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
	public List<Field> getFields();
	/**
	 * Get a subfield that belongs to this group.
	 * 
	 * @param id the id of the field
	 * @return the field or null if no such Field
	 */
	public Field getField(short id);
	/**
	 * Get a subfield that belongs to this group.
	 * 
	 * @param name the name of the field
	 * @return the field or null if no such Field
	 */
	public Field getField(String name);
	/**
	 * The factory class to add a field to this group. It returns the added Field object 
	 * for the application to further modify its attributes.
	 * 
	 * id and name have to be unique among direct children of this group. The field is considered to be 
	 * an array by default. If not, specify the arrayLength to be 1.
	 * 
	 * @param id the id of the field
	 * @param type the type of this field. See available types in {@link FieldType}.
	 * @param arrayLength the array size of this field
	 * @return the newly created field
	 */
	public Field addField(short id, FieldType type, short arrayLength);	

	/* (non-Javadoc)
	 * @see com.github.bwsoft.iris.message.Field#length()
	 */
	public default short length() {
		return 1;
	}
}
