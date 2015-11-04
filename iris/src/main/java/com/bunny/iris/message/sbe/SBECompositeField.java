package com.bunny.iris.message.sbe;

import java.util.function.Consumer;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

public class SBECompositeField extends AbstractSBEParentField {
	private SBEValueNode value, childValue;
	private int relativeOffset;
		
	public SBECompositeField(SBEMessage message) {
		super(message);
		
		value = new SBEValueNode();
		value.setNodeId((short) -1);
		value.setField(this);
		value.setNumRows((short) 1);
		
		childValue = new SBEValueNode();
		childValue.setNodeId((short) -1);
		childValue.setNumRows((short) 0);
	}

	int getRelativeOffset() {
		return relativeOffset;
	}
	
	SBECompositeField setRelativeOffset(int offset) {
		this.relativeOffset = offset;
		return this;
	}
	
	@Override
	public Field addChildField(FieldType type, short dimmension) {
		if( type == FieldType.GROUP || type == FieldType.COMPOSITE || type == FieldType.RAW ) {
			throw new IllegalArgumentException("composite field does not accept the child field of type: "+type);
		}
		SBEField field = (SBEField) super.addChildField(type, dimmension);
		value.setRowSize((short) 0, field.getBlockSize()*field.getDimension()+field.getRelativeOffset());
		value.setSize(field.getBlockSize()*field.getDimension()+field.getRelativeOffset());
		return field;
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
	public FieldValue getFieldValue(short occurrence) {
		short currentOccurrence = 0;
		AbstractSBEField parent = this.getParent();
		short parentOccurrence = parent.getTotalOccurrence();
		for( short i = 0; i < parentOccurrence; i ++ ) {
			SBEValueNode parentValue = (SBEValueNode) parent.getFieldValue(i);
			int offset = parentValue.getOffset();
			short numRows = parentValue.getNumRows();
			for( short j = 0; j < numRows; j ++ ) {
				if( currentOccurrence == occurrence ) {
					value.setOffset(offset+this.getRelativeOffset());
					value.setCurrentOccurrence(currentOccurrence);
					return value;
				}
				currentOccurrence ++;
				offset += parentValue.getRowSize(j);
			}
		}
		throw new ArrayIndexOutOfBoundsException(occurrence+" exceeds the maximum occurrence: "+getTotalOccurrence());
	}

	SBEValueNode getFieldValue(SBEValueNode parentValue, short numRow) {
		int offset = parentValue.getOffset();
		short numRows = parentValue.getNumRows();
		for( short j = 0; j < numRows; j ++ ) {
			if( numRow == j ) {
				value.setOffset(offset+this.getRelativeOffset());
				value.setCurrentOccurrence((short)(parentValue.getCurrentOccurrence()+j));
				return value;
			}
			offset += parentValue.getRowSize(j);
		}
		throw new ArrayIndexOutOfBoundsException((parentValue.getCurrentOccurrence()+numRow)+" exceeds the maximum occurrence: "+getTotalOccurrence());		
	}

	@Override
	public void getChildValues(Consumer<FieldValue> consumer) {
		short currentOccurrence = 0;
		AbstractSBEField parent = this.getParent();
		short parentOccurrence = parent.getTotalOccurrence();
		for( short i = 0; i < parentOccurrence; i ++ ) {
			SBEValueNode parentValue = (SBEValueNode) parent.getFieldValue(i);
			int offset = parentValue.getOffset();
			short numRows = parentValue.getNumRows();
			for( short j = 0; j < numRows; j ++ ) {
				for( Field childField : getChildField() ) {
					childValue.setField(childField);
					childValue.setOffset(offset+this.getRelativeOffset()+((SBEField) childField).getRelativeOffset());
					childValue.setCurrentOccurrence(currentOccurrence);
					consumer.accept(childValue);
				}
				currentOccurrence ++;
				offset += parentValue.getRowSize(j);
			}
		}
	}
	
	@Override
	void getChildValues(SBEValueNode myCurrentValue, Consumer<FieldValue> consumer) {
		for( Field childField : getChildField() ) {
			childValue.setField(childField);
			childValue.setOffset(myCurrentValue.getOffset()+((SBEField) childField).getRelativeOffset());
			childValue.setCurrentOccurrence(myCurrentValue.getCurrentOccurrence());
			consumer.accept(childValue);
		}
	}
}
