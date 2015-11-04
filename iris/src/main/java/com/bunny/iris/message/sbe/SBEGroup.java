package com.bunny.iris.message.sbe;

import java.util.function.Consumer;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

class SBEGroup extends AbstractSBEParentField {
	
	private short totalOccurrence;
	private short[] nodeIds;

	// child value node
	private SBEValueNode childValue;
	
	public SBEGroup(SBEMessage message) {
		super(message);
		totalOccurrence = 0;
		nodeIds = new short[SBEMessage.MAX_FIELD_OCCURRENCE];
		childValue = new SBEValueNode();
		childValue.setNodeId((short) -1);
		childValue.setNumRows((short) 0);
	}
	
	void reset() {
		totalOccurrence = 0;
		super.reset();
	}
	
	short addValue(SBEValueNode value) {
		nodeIds[totalOccurrence] = value.getNodeId();
		return totalOccurrence ++;
	}
		
	@Override
	public short getTotalOccurrence() {
		return totalOccurrence;
	}
	
	@Override
	public SBEValueNode getFieldValue(short idx) {
		return getMessage().getValueNode(this.nodeIds[idx]);
	}
	
	@Override
	public void getValues(Consumer<FieldValue> consumer) {
		for( short i = 0; i < totalOccurrence; i ++ ) {
			consumer.accept(getMessage().getValueNode(this.nodeIds[i]));
		}
	}
	
	@Override
	public void getChildValues(Consumer<FieldValue> consumer) {
		short currentOccurrence = 0;
		for( short i = 0; i < getTotalOccurrence(); i ++ ) {
			SBEValueNode value = getFieldValue(i);
			int offset = value.getOffset();
			short numRows = value.getNumRows();
			for( short j = 0; j < numRows; j ++ ) {
				for( Field childField : getChildField() ) {
					switch( childField.getType() ) {
					case COMPOSITE:
						SBEValueNode compositeValue = ((SBECompositeField) childField).getFieldValue(value, j);
						consumer.accept(compositeValue);
						((SBECompositeField) childField).getChildValues(compositeValue, consumer);
						break;
					case GROUP:
						FieldValue grpValue = ((SBEGroup) childField).getFieldValue(currentOccurrence);
						consumer.accept(grpValue);
						((SBEGroup) childField).getChildValues((SBEValueNode)grpValue, consumer);
						break;
					case RAW:
						FieldValue rawValue = ((SBEVarLengthField) childField).getFieldValue(currentOccurrence);
						consumer.accept(rawValue);
					default:
						childValue.setField(childField);
						childValue.setOffset(offset+((SBEField) childField).getRelativeOffset()); 
						childValue.setCurrentOccurrence(currentOccurrence);
						consumer.accept(childValue);
						break;
					}
				}
				offset += value.getRowSize(j);
				currentOccurrence ++;
			}
		}
	}
	
	@Override
	void getChildValues(SBEValueNode myValue, Consumer<FieldValue> consumer) {
		int offset = myValue.getOffset();
		short numRows = myValue.getNumRows();
		for( short j = 0; j < numRows; j ++ ) {
			for( Field childField : getChildField() ) {
				switch( childField.getType() ) {
				case GROUP:
					FieldValue grpValue = ((SBEGroup) childField).getFieldValue(myValue.getCurrentOccurrence());
					consumer.accept(grpValue);
					((SBEGroup) childField).getChildValues((SBEValueNode)grpValue, consumer);
					break;
				case RAW:
					FieldValue rawValue = ((SBEVarLengthField) childField).getFieldValue(myValue.getCurrentOccurrence());
					consumer.accept(rawValue);
				default:
					childValue.setField(childField);
					childValue.setOffset(offset+((SBEField) childField).getRelativeOffset()); 
					childValue.setCurrentOccurrence(myValue.getCurrentOccurrence());
					consumer.accept(childValue);
					break;
				}
			}
			offset += myValue.getRowSize(j);
		}
	}
	
	int wrapForRead(int offset) {		
		SBEValueNode grpValue = getMessage().allocate();
		grpValue.setField(this);
		grpValue.setOffset(offset);
		grpValue.initCompositeNode();		
		grpValue.setSize(getHeaderSize());
		grpValue.setCurrentOccurrence(addValue(grpValue));
		
		int currentOffset = offset + getHeaderSize();
		for( short i = 0; i < grpValue.getNumRows(); i ++ ) {			
			int startOffset = currentOffset;
			currentOffset += getBlockSize();
			for( int k = getNumFixedSizeFields(); k < getChildField().size(); k ++ ) {
				Field field = getChildField().get(k);
				if( FieldType.GROUP == field.getType() ) {
					currentOffset += ((SBEGroup) field).wrapForRead(currentOffset);				
				} else if( FieldType.RAW == field.getType() ) {
					currentOffset += ((SBEVarLengthField) field).wrapForRead(currentOffset);
				}
			}
			grpValue.setRowSize(i, currentOffset - startOffset);
			grpValue.increaseSize(currentOffset - startOffset);
		}
		return grpValue.getSize();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getName()).append("{name=").append(getName()).append(",id=").append(getID());
		sb.append(",children=[");
		for( Field field : getChildField() ) {
			if( field.getType() == FieldType.GROUP || field.getType() == FieldType.MESSAGE ) {
				sb.append("{").append(((SBEGroup) field).toString()).append("}");
			} else {
				sb.append(field.getName());
			}
			sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
}
