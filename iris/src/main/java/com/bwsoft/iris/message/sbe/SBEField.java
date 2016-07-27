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

import java.util.HashMap;

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.Group;

/**
 * A SBEField in the message. It can be a group field, variable length field, a composite 
 * field, and/or a fixed size field. 
 * 
 * @author yzhou
 *
 */
public class SBEField implements Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private short id;
	private String name;
	private FieldType type;
	
	private int blockSize;	
	private short arrayLength = 1;
	
	private int offset; 
	
	private HashMap<String, String> enumLookup;
	private HashMap<String, Integer> bitLookup;

	private Group parent;
	
	private String constValue;
	private FieldType constType;

	// can only be created by its parent and hence package scope
	SBEField(Group parent, FieldType type, short arrayLength) {
		this.parent = parent;
		this.type = type;
		this.arrayLength = arrayLength;
		this.blockSize = type.size();
		this.constValue = null;
	}
	
	public SBEField setConstantValue(String value) {
		this.constValue = value;
		return this;
	}
	
	public SBEField setConstantType(FieldType type) {
		this.constType = type;
		return this;
	}
	
	public String getConstantValue() {
		return this.constValue;
	}
	
	public FieldType getConstantType() {
		return this.constType;
	}
	
	/**
	 * It is the size to contain one element of this object. The total storage size of this 
	 * object is its block size multiplied by its array length.  
	 * 
	 * @param blockSize
	 * @return
	 */
	public SBEField setBlockSize(int blockSize) {
		this.blockSize = blockSize;
		return this;
	}
	
	/**
	 * It is the size to contain one element of this object. The total storage size of this 
	 * object is its block size multiplied by its array length.  
	 * 
	 * @return 
	 */
	public int getBlockSize() {
		return this.blockSize;
	}
	
	/**
	 * An offset relative to the value starting position of its parent group. 
	 *  
	 * @param offset
	 * @return
	 */
	public SBEField setRelativeOffset(int offset) {
		this.offset = offset;
		return this;
	}
	
	/**
	 * An offset relative to the value starting position of its parent group. 
	 *  
	 * @return
	 */
	public int getRelativeOffset() {
		return offset;
	}
	
	public SBEMessage getMessage() {
		Group grp = this.parent; 		
		if( grp == null ) {
			return (SBEMessage) this;
		} else {
			while( grp.getParent() != null ) {
				grp = grp.getParent();
			}
			return (SBEMessage) grp;
		}
	}
	
	void setEnumLookupTable(HashMap<String, String> lookupTable) {
		this.enumLookup = lookupTable;
	}
	
	boolean isEnumField() {
		return this.enumLookup != null;
	}
	
	String getEnumName(String value) {
		if( this.enumLookup == null ) 
			throw new UnsupportedOperationException("no enum conversion for type: "+this.getType());
		return this.enumLookup.get(value);
	}
	
	void setSetLookupTable(HashMap<String, Integer> lookupTable) {
		this.bitLookup = lookupTable;
	}
	
	int getSetBit(String bitName) {
		if( this.bitLookup == null ) {
			throw new UnsupportedOperationException("no bit selection is supported for type: "+this.getType());
		}
		return this.bitLookup.get(bitName);
	}

	boolean isChoiceField() {
		return null != this.bitLookup;
	}
	
	@Override
	public short getID() {
		return id;
	}

	@Override
	public Field setID(short id) {
		this.id = id;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Field setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public FieldType getType() {
		return type;
	}

	@Override
	public short length() {
		return arrayLength;
	}

	@Override
	public Group getParent() {
		return parent;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("name:").append(name);
		sb.append(",id:").append(id);
		sb.append(",type:").append(type.name());
		sb.append(",dimension:").append(length());
		sb.append("}");
		return sb.toString();
	}
}
