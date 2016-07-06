package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.Field;
import com.bunny.iris.message.Group;

public class SBEGroup extends SBEField implements Group {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final SBEHeader header;
	
	// child field definition
	private final LinkedHashMap<Short,Field> groupFieldLookup = new LinkedHashMap<>(); 
	private short numFixedSizeFields;

	public SBEGroup(SBEGroup parent, SBEHeader header, FieldType type) {
		super(parent, type,(short) 1);
		if( type != FieldType.GROUP && type != FieldType.MESSAGE ) {
			throw new IllegalArgumentException("ilegal type specified for a SBE group: "+type.name());
		}
		
		this.header = header;
	}
	
	public SBEHeader getHeader() {
		return header;
	}
	
	public short getNumFixedSizeFields() {
		return numFixedSizeFields;
	}
	
	@Override
	public List<Field> getChildFields() {
		return new ArrayList<>(this.groupFieldLookup.values());
	}
	
	@Override
	public Field getChildField(short id) {
		return groupFieldLookup.get(id);
	}

	@Override
	public Field addChildField(short id, FieldType type, short arrayLength) {
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
			if( groupFieldLookup.size() > 0 ) {
				SBEField lastField = (SBEField) getChildFields().get(groupFieldLookup.size()-1);
				currentOffset = lastField.getBlockSize()*lastField.length() + lastField.getRelativeOffset(); 
			}
			newField = new SBEField(this, type, arrayLength).setRelativeOffset(currentOffset);
			newField.setID(id);
			this.groupFieldLookup.put(id, newField);
			numFixedSizeFields ++;
			break;
		case COMPOSITE:
			if( groupFieldLookup.size() > 0 ) {
				SBEField lastField = (SBEField) getChildFields().get(groupFieldLookup.size()-1);
				currentOffset = lastField.getBlockSize()*lastField.length() + lastField.getRelativeOffset(); 
			}
			newField = new SBECompositeField(this,arrayLength);
			newField.setID(id);
			this.groupFieldLookup.put(id, newField);
			numFixedSizeFields ++;
			break;
		case GROUP:
			newField = new SBEGroup(this, getMessage().getGrpHeader(), FieldType.GROUP);
			newField.setID(id);
			this.groupFieldLookup.put(id, newField);
			break;
		case RAW:
			newField = new SBEVarLengthField(this, getMessage().getVarLengthFieldHeader());
			newField.setID(id);
			this.groupFieldLookup.put(id, newField);
			break;
		default:
			throw new IllegalArgumentException("unrecognized type: "+type.name());
		}
		return newField;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("name:").append(getName());
		sb.append(",id:").append(this.getID());
		sb.append(",type:").append(this.getType());
		for( Field field : getChildFields() ) {
			sb.append(",").append(field);
		}
		sb.append("}");
		return sb.toString();
	}
}
