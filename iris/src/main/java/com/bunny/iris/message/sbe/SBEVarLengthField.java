package com.bunny.iris.message.sbe;

import java.util.function.Consumer;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

class SBEVarLengthField extends SBEField {

	private short totalOccurrence;
	private short[] nodeIds;

	public SBEVarLengthField(SBEMessage message) {
		super(message, (short) 1);
		setType(FieldType.RAW);
		
		totalOccurrence = 0;
		nodeIds = new short[SBEMessage.MAX_FIELD_OCCURRENCE];
	}

	void reset() {
		totalOccurrence = 0;	
		super.reset();
	}

	short addValue(SBEValueNode value) {
		nodeIds[totalOccurrence] = value.getNodeId();
		return totalOccurrence ++;
	}
		
	public SBEValueNode getFieldValue(short idx) {
		return getMessage().getValueNode(this.nodeIds[idx]);
	}
	
	@Override
	public short getTotalOccurrence() {
		return totalOccurrence;
	}
	
	@Override
	public void getValues(Consumer<FieldValue> consumer) {
		for( short i = 0; i < totalOccurrence; i ++ ) {
			consumer.accept(getMessage().getValueNode(this.nodeIds[i]));
		}
	}

	int wrapForRead(int offset) {
		SBEValueNode grpValue = getMessage().allocate();
		grpValue.setField(this);
		grpValue.setOffset(offset);
		grpValue.initVarLengthNode();		
		grpValue.setSize(getBlockSize()+getHeaderSize());
		grpValue.setCurrentOccurrence(addValue(grpValue));
		grpValue.setNumRows((short)0);

		return grpValue.getSize();
	}	
}
