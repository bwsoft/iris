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
package com.bwsoft.iris.message.sbe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.FieldHeader;
import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.Group;

class SBEGroup extends SBEField implements Group {

	private static final long serialVersionUID = 6161858305622927888L;

	private final FieldHeader header;
	
	// child field definition
	private final LinkedHashMap<Short,Field> groupFieldLookup = new LinkedHashMap<>(); 
	private final LinkedHashMap<String,Field> groupFieldLookupByName = new LinkedHashMap<>(); 
	private short numFixedSizeFields;
	private short numGroupFields;
	private short numRawFields;

	SBEGroup(SBEGroup parent, FieldHeader header, FieldType type) {
		super(parent, type,(short) 1);
		if( type != FieldType.GROUP && type != FieldType.MESSAGE ) {
			throw new IllegalArgumentException("ilegal type specified for a SBE group: "+type.name());
		}
		
		this.header = header;
	}
	
	short getNumFixedSizeFields() {
		return numFixedSizeFields;
	}
	
	short getNumGroupFields() {
		return numGroupFields;
	}
	
	short getNumRawFields() {
		return numRawFields;
	}

	void buildNameIndex(String name, Field field) {
		if( groupFieldLookupByName.containsKey(name) ) {
			throw new IllegalArgumentException("cannot have fields of the same name in a group");
		}
		this.groupFieldLookupByName.put(name, field);
	}
	
	@Override
	public FieldHeader getHeader() {
		return header;
	}
	
	@Override
	public List<Field> getFields() {
		return new ArrayList<>(this.groupFieldLookup.values());
	}
	
	@Override
	public Field getField(short id) {
		return groupFieldLookup.get(id);
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
		if( this.groupFieldLookup.containsKey(id) ) {
			throw new IllegalArgumentException("id confliction detected with id = "+id);
		}
		
		SBEField newField = null;
		int currentOffset = 0;
		
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
		case FLOAT:
		case DOUBLE:
		case CONSTANT:
			if( groupFieldLookup.size() > 0 ) {
				SBEField lastField = (SBEField) getFields().get(groupFieldLookup.size()-1);
				currentOffset = lastField.getBlockSize()*lastField.length() + lastField.getRelativeOffset(); 
			}
			newField = new SBEField(this, type, arrayLength).setRelativeOffset(currentOffset);
			newField.setID(id);
			this.groupFieldLookup.put(id, newField);
			numFixedSizeFields ++;
			
			// update the block size of the group
			int blockSize = this.getBlockSize();
			this.setBlockSize(blockSize+newField.getBlockSize()*newField.length());
			break;
		case COMPOSITE:
			if( groupFieldLookup.size() > 0 ) {
				SBEField lastField = (SBEField) getFields().get(groupFieldLookup.size()-1);
				currentOffset = lastField.getBlockSize()*lastField.length() + lastField.getRelativeOffset(); 
			}
			newField = new SBECompositeField(this,arrayLength).setRelativeOffset(currentOffset);
			newField.setID(id);
			this.groupFieldLookup.put(id, newField);
			numFixedSizeFields ++;
			break;
		case GROUP:
			newField = new SBEGroup(this, getMessage().getGrpHeader(), FieldType.GROUP);
			newField.setID(id);
			this.groupFieldLookup.put(id, newField);
			numGroupFields ++;
			break;
		case RAW:
			newField = new SBEVarLengthField(this, getMessage().getVarLengthFieldHeader());
			newField.setID(id);
			this.groupFieldLookup.put(id, newField);
			numRawFields ++;
			break;
		default:
			throw new IllegalArgumentException("unrecognized type: "+type.name());
		}
		return newField;
	}
}
