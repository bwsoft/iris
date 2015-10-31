package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

public class SBEField extends AbstractSBEField {
	private SBEValueNode value;
	private int relativeOffset = 0;
	
	public SBEField(SBEMessage message) {
		super(message);
		value = new SBEValueNode();
		value.setNodeId((short) -1);
		value.setField(this);
		value.setNumRows((short) 0);
	}
	
	int getRelativeOffset() {
		return relativeOffset;
	}
	
	AbstractSBEField setRelativeOffset(int offset) {
		this.relativeOffset = offset;
		return this;
	}
	
	@Override
	public List<Field> getChildField() {
		return new ArrayList<Field>();
	}

	@Override
	public Field addChildField(FieldType type) {
		throw new UnsupportedOperationException("cannot add child for field type: "+getType());
	}

	@Override
	public short getTotalOccurrence() {
		short occurrence = 0;
		SBECompositeField parent = (SBECompositeField) this.getParent();
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
		SBECompositeField parent = (SBECompositeField) this.getParent();
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
