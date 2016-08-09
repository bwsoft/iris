/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.bwsoft.iris.message;

import java.io.Serializable;

/**
 * The definition of a message field. It is identified by a name and an id. They have to be 
 * unique in its parent field, the {@link Group}, but the same name or id can appear in other 
 * groups.    
 * 
 * The field id, field type, its array length, and its parent typically need to be decided upon 
 * construction. And it is typically created by a factory method such as 
 * {@link Group#addField(short, FieldType, short)} in the {@link Group} class. 
 * 
 * @author yzhou
 *
 */
public interface Field extends Serializable {
	/**
	 * @return the id of the field
	 */
	public short getID();
	/**
	 * @param id the id of the field. 
	 * @return this field
	 */
	public Field setID(short id);
	
	/**
	 * @return the name of the field
	 */
	public String getName();
	/**
	 * @param name the name of the field
	 * @return this field
	 */
	public Field setName(String name);
	
	/**
	 * For available types, see {@link FieldType}.
	 * 
	 * @return the type of the field
	 */
	public FieldType getType();
	
	/**
	 * @return the header definition of the field
	 */
	public default FieldHeader getHeader() {
		return null;
	}
	
	/**
	 * @return the length, aka the dimension, of the field
	 */
	public short length(); // The array size if this field is an array.
	/**
	 * @return the parent of this field
	 */
	public Group getParent();
}
