package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.aField;
import com.bunny.iris.message.aGroup;

public class aSBEGroup extends aSBEField implements aGroup {

	private final aSBEHeader header;
	
	// child field definition
	private final List<aField> children = new ArrayList<>();
	private final LinkedHashMap<Short,aField> groupFieldLookup = new LinkedHashMap<>(); 
	private short numFixedSizeFields;

	public aSBEGroup(aSBEGroup parent, aSBEHeader header, FieldType type) {
		super(parent, type,(short) 1);
		if( type != FieldType.GROUP && type != FieldType.MESSAGE ) {
			throw new IllegalArgumentException("ilegal type specified for a SBE group: "+type.name());
		}
		
		this.header = header;
	}
	
	public aSBEHeader getHeader() {
		return header;
	}
	
	public short getNumFixedSizeFields() {
		return numFixedSizeFields;
	}
	
	@Override
	public List<aField> getChildFields() {
		return children;
	}
	
	@Override
	public aField getChildField(short id) {
		return groupFieldLookup.get(id);
	}

	@Override
	public aField addChildField(short id, FieldType type, short dimmension) {
		aSBEField newField = null;
//		int currentOffset = header.getHeaderSize();
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
			if( children.size() > 0 ) {
				aSBEField lastField = (aSBEField) children.get(children.size()-1);
				currentOffset = lastField.getBlockSize()*lastField.getDimension() + lastField.getRelativeOffset(); 
			}
			newField = new aSBEField(this, type, dimmension).setRelativeOffset(currentOffset);
			newField.setID(id);
			children.add(newField);
			this.groupFieldLookup.put(id, newField);
			numFixedSizeFields ++;
			break;
		case COMPOSITE:
			if( children.size() > 0 ) {
				aSBEField lastField = (aSBEField) children.get(children.size()-1);
				currentOffset = lastField.getBlockSize()*lastField.getDimension() + lastField.getRelativeOffset(); 
			}
			newField = new aSBECompositeField(this,dimmension);
			newField.setID(id);
			children.add(newField);
			this.groupFieldLookup.put(id, newField);
			numFixedSizeFields ++;
			break;
		case GROUP:
			newField = new aSBEGroup(this, getMessage().getGrpHeader(), FieldType.GROUP);
			newField.setID(id);
			children.add(newField);
			this.groupFieldLookup.put(id, newField);
			break;
		case RAW:
			newField = new aSBEVarLengthField(this, getMessage().getVarLengthFieldHeader());
			newField.setID(id);
			children.add(newField);
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
		for( aField field : this.children ) {
			sb.append(",").append(field);
		}
		sb.append("}");
		return sb.toString();
	}
}
