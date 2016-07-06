package com.bunny.iris.message.sbe;

import java.util.HashMap;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.aField;
import com.bunny.iris.message.aGroup;

public class aSBEField implements aField {

	private short id;
	private String name;
	private FieldType type;
	
	private int blockSize;	// size of this field
	private short repeat = 1;
	
	private int offset; // to the beginning of the group
	
	private HashMap<String, String> enumLookup;
	private HashMap<String, Integer> bitLookup;

	private aGroup parent;

	// can only be created by its parent and hence package scope
	aSBEField(aGroup parent, FieldType type, short dimension) {
		this.parent = parent;
		this.type = type;
		this.repeat = dimension;
		this.blockSize = type.size();
	}
	
	public aSBEField setBlockSize(int blockSize) {
		this.blockSize = blockSize;
		return this;
	}
	
	public int getBlockSize() {
		return this.blockSize;
	}
	
	public aSBEField setRelativeOffset(int offset) {
		this.offset = offset;
		return this;
	}
	
	public int getRelativeOffset() {
		return offset;
	}
	
	public aSBEMessage getMessage() {
		aGroup grp = this.parent; 		
		if( grp == null ) {
			return (aSBEMessage) this;
		} else {
			while( grp.getParent() != null ) {
				grp = grp.getParent();
			}
			return (aSBEMessage) grp;
		}
	}
	
	void setEnumLookupTable(HashMap<String, String> lookupTable) {
		this.enumLookup = lookupTable;
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

	@Override
	public short getID() {
		return id;
	}

	@Override
	public aField setID(short id) {
		this.id = id;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public aField setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public FieldType getType() {
		return type;
	}

	@Override
	public short getDimension() {
		return repeat;
	}

	@Override
	public aGroup getParent() {
		return parent;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("name:").append(name);
		sb.append(",id:").append(id);
		sb.append(",type:").append(type.name());
		sb.append(",dimension:").append(getDimension());
		sb.append("}");
		return sb.toString();
	}
}
