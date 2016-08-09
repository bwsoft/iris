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
 * The base class for all fields in a SBE message. It extends 
 * the basic {@link com.bwsoft.iris.message.Field} by adding a couple of 
 * additional operations that are specific to SBE message.  
 * 
 * @author yzhou
 *
 */
public class SBEField implements Field {
	
	private static final long serialVersionUID = 2777827335710316701L;

	private short id;
	private String name;
	private FieldType type;
	
	private int blockSize;	
	private short arrayLength = 1;
	
	private int offset; 
	
	private HashMap<String, String> enumLookup;
	private HashMap<String, Integer> bitLookup;

	private SBEMessage message;
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
		
		if( parent == null ) {
			this.message = (SBEMessage) this;
		} else {
			this.message = ((SBEField) parent).getMessage();
		}
	}
	
	SBEField setConstantValue(String value) {
		this.constValue = value;
		return this;
	}
	
	SBEField setConstantType(FieldType type) {
		this.constType = type;
		return this;
	}
	
	/**
	 * Return the value of a constant field or null for other field types. 
	 * 
	 * @return the value of a constant field or null for other field types
	 */
	public String getConstantValue() {
		return this.constValue;
	}
	
	/**
	 * Return the type for the value in the constant field or null if other field types.
	 * 
	 * @return the type for the value in the constant field or null if other field types.
	 */
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
	SBEField setBlockSize(int blockSize) {
		this.blockSize = blockSize;
		return this;
	}
	
	/**
	 * It is the size to contain one element of this object. The total storage size of this 
	 * object is its block size multiplied by its array length.  
	 * 
	 * @return 
	 */
	int getBlockSize() {
		return this.blockSize;
	}
	
	/**
	 * An offset relative to the value starting position of its parent group. 
	 *  
	 * @param offset
	 * @return
	 */
	SBEField setRelativeOffset(int offset) {
		this.offset = offset;
		return this;
	}
	
	/**
	 * An offset relative to the value starting position of its parent group. 
	 *  
	 * @return
	 */
	int getRelativeOffset() {
		return offset;
	}
	
	/**
	 * Obtain the message definition.
	 * 
	 * @return the SBEMessage definition
	 */
	public SBEMessage getMessage() {
		return this.message;
	}
	
	void setEnumLookupTable(HashMap<String, String> lookupTable) {
		this.enumLookup = lookupTable;
	}
	
	/**
	 * Test if the field is an enum field.
	 * 
	 * @return true if the field is an enum field
	 */
	public boolean isEnumField() {
		return this.enumLookup != null;
	}
	
	/**
	 * Return the enum name based upon its value or throw an UnsupportedOperationException
	 * if the field is not an enum field.
	 * 
	 * @param value the value of the enum
	 * @return the enum name based upon its value
	 */
	public String getEnumName(String value) {
		if( this.enumLookup == null ) 
			throw new UnsupportedOperationException("no enum conversion for type: "+this.getType());
		return this.enumLookup.get(value);
	}
	
	void setSetLookupTable(HashMap<String, Integer> lookupTable) {
		this.bitLookup = lookupTable;
	}
	
	/**
	 * Get the corresponding bit position for a given bitName. 
	 * 
	 * @param bitName the name of the bit position defined in the SBE xml template
	 * @return the corresponding bit position
	 */
	public int getSetBit(String bitName) {
		if( this.bitLookup == null ) {
			throw new UnsupportedOperationException("no bit selection is supported for type: "+this.getType());
		} else if( ! bitLookup.containsKey(bitName) ) {
			throw new IllegalArgumentException("nonexist bitname for a choice field: "+bitName);
		}
		return this.bitLookup.get(bitName);
	}

	/**
	 * Test if the field is a BitSet field
	 * @return true if it is
	 */
	public boolean isChoiceField() {
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
		
		// build name index
		if( null != this.parent ) {
			if( parent instanceof SBEGroup ) {
				((SBEGroup) parent).buildNameIndex(name, this);
			} else if( parent instanceof SBECompositeField ) {
				((SBECompositeField) parent).buildNameIndex(name, this);
			} else {
				throw new InternalError("unrecognized group type in SBE");
			}
		}
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
}
