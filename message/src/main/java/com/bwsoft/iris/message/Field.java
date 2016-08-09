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
 * A Field is identified by name and id. A group is a special field that 
 * contains other fields and/or groups. The group that this field is in is its parent group.
 * 
 * The field id, field type, its array length, and its parent are determined upon 
 * construction. It is typically created by the group it belongs to. 
 * 
 * @see com.bwsoft.iris.message.Group#addField(short, FieldType, short)
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
