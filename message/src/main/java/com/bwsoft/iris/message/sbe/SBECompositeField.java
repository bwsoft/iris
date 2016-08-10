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
package com.bwsoft.iris.message.sbe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.FieldHeader;
import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.Group;

/**
 * Represents a composite field in SBE message. The subfield is retrieved by its 
 * sequence in the definition with the first one starting at the index zero.
 * 
 * @author yzhou
 *
 */
class SBECompositeField extends SBEField implements Group {
	private static final long serialVersionUID = 1242147768313259509L;

	private final FieldHeader header = new FieldHeader() {
		@Override
		public short getSize() {
			return 0;
		}		
	};
	
	// child field definition
	private final List<Field> children = new ArrayList<>();
	private final LinkedHashMap<String,Field> groupFieldLookupByName = new LinkedHashMap<>(); 

	SBECompositeField(SBEGroup parent, short dimension) {
		super(parent, FieldType.COMPOSITE, dimension);
	}
	
	void buildNameIndex(String name, Field field) {
		if( groupFieldLookupByName.containsKey(name) ) {
			throw new IllegalArgumentException("cannot have fields of the same name in a group");
		}
		this.groupFieldLookupByName.put(name, field);
	}
	
	@Override
	public List<Field> getFields() {
		return children;
	}

	@Override 
	public Field getField(short id) {
		return children.get(id);
	}

	@Override
	public Field getField(String name) {
		return groupFieldLookupByName.get(name);
	}

	@Override
	public Field addField(short id, FieldType type, short arrayLength) {
		if( arrayLength < 1 ) {
			throw new IllegalArgumentException("zero length is not allowed");
		}
		SBEField newField = null;
		int currentOffset = header.getSize()+this.getRelativeOffset();
		
		switch( type ) {
		case U8:
		case U16:
		case U32:
		case U64:
		case I8:
		case I16:
		case I32:
		case I64:
		case BYTE:
		case CHAR:
		case DOUBLE:
		case FLOAT:
		case CONSTANT:
			if( children.size() > 0 ) {
				SBEField lastField = (SBEField) children.get(children.size()-1);
				currentOffset = lastField.getBlockSize() + lastField.getRelativeOffset(); 
			}
			newField = new SBEField(this, type, arrayLength).setRelativeOffset(currentOffset);
			this.setBlockSize(this.getBlockSize()+newField.getBlockSize()*newField.length());
			children.add(newField);
			
			// update the blocksize of its parent
			SBEGroup grp = (SBEGroup) this.getParent();
			grp.setBlockSize(grp.getBlockSize()+newField.getBlockSize()*newField.length());
			break;
		default:
			throw new IllegalArgumentException("composite field does not accept the child field of type: "+type);
		}
		return newField;
	}
}
