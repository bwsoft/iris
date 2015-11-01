package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

public class SBECompositeField extends SBEField {
	// child field definition
	private List<Field> children = new ArrayList<>();
	
	private SBEValueNode value;
		
	public SBECompositeField(SBEMessage message) {
		super(message);
		
		value = new SBEValueNode();
		value.setNodeId((short) -1);
		value.setField(this);
		value.setNumRows((short) 1);
	}
	
	void finalized() {
		int currentOffset = this.getHeaderSize();
		for( Field field : children ) {
			if( ! isFinalized() ) {
				switch( field.getType() ) {
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
					((SBEField) field).setRelativeOffset(currentOffset);
					currentOffset += field.getArraySize()*((SBEField) field).getBlockSize();
					value.setRowSize((short) 0, currentOffset);
					value.setSize(currentOffset);
					break;
				}				
			}
			((AbstractSBEField) field).finalized();
		}		
	}

	@Override
	public List<Field> getChildField() {
		return children;
	}

	@Override
	public Field addChildField(FieldType type) {
		AbstractSBEField newField = null;
		
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
			newField = new SBEField(getMessage()).setParent(this).setType(type);
			children.add(newField);
			break;
		default:
			throw new IllegalArgumentException("unrecognized type: "+type.name());
		}
		return newField;
	}

	@Override
	public short getTotalOccurrence() {
		short occurrence = 0;
		AbstractSBEField parent = this.getParent();
		short parentOccurrence = parent.getTotalOccurrence();
		for( short i = 0; i < parentOccurrence; i ++ ) {
			SBEValueNode value = (SBEValueNode) parent.getFieldValue(i);
			occurrence += value.getNumRows();
		}
		return occurrence;
	}

	@Override
	public void getValues(Consumer<FieldValue> consumer) {
		short currentOccurrence = 0;
		value.setSize(this.getBlockSize()*this.getArraySize());
		AbstractSBEField parent = this.getParent();
		short parentOccurrence = parent.getTotalOccurrence();
		for( short i = 0; i < parentOccurrence; i ++ ) {
			SBEValueNode parentValue = (SBEValueNode) parent.getFieldValue(i);
			int offset = parentValue.getOffset();
			short numRows = parentValue.getNumRows();
			for( short j = 0; j < numRows; j ++ ) {
				value.setOffset(offset+this.getRelativeOffset());
				value.setCurrentOccurrence(currentOccurrence++);
				offset += parentValue.getRowSize(j);
				consumer.accept(value);
			}
		}
	}

	@Override
	public void getChildValues(Consumer<FieldValue> consumer) {
	}

	@Override
	public void getChildValues(short occurrence, Consumer<FieldValue> consumer) {
	}
}
