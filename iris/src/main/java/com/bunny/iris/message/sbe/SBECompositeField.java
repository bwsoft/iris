package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

class SBECompositeField extends AbstractSBEField {
	// child field definition
	private List<Field> children = new ArrayList<>();
	
	private short totalOccurrence;
	private short[] nodeIds;

	// child value node
	private SBEValueNode childValue;
	
	private short complextNodeStartingPoint;
	
	public SBECompositeField(SBEMessage message) {
		super(message);
		totalOccurrence = 0;
		nodeIds = new short[SBEMessage.MAX_FIELD_OCCURRENCE];
		childValue = new SBEValueNode();
		complextNodeStartingPoint = 0;
	}
	
	void reset() {
		super.reset();
		totalOccurrence = 0;		
		for( Field field : children ) {
			((AbstractSBEField) field).reset();
		}
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
			int currentOffset = this.getHeaderSize();
			if( children.size() != 0 ) {
				Field field = children.get(children.size()-1);
				if( field.getType() == FieldType.GROUP || field.getType() == FieldType.RAW ) {
					throw new IllegalArgumentException("regular field has to be ahead of complex field");
				}
				currentOffset = ((SBEField)field).getRelativeOffset() + ((SBEField) field).getBlockSize()*field.getArraySize();
			}
			newField = new SBEField(getMessage()).setRelativeOffset(currentOffset).setParent(this).setType(type);
			children.add(newField);
			currentOffset += newField.getBlockSize()*newField.getArraySize();
			complextNodeStartingPoint ++;
			break;
		case GROUP:
			newField = new SBECompositeField(getMessage()).setParent(this).setHeaderSize(getMessage().getGroupHeaderSize()).setType(FieldType.GROUP);
			children.add(newField);
			break;
		case RAW:
			newField = new SBEVarLengthField(getMessage()).setParent(this).setHeaderSize(getMessage().getVarDataHeaderSize()).setType(FieldType.RAW);
			children.add(newField);
			break;
		default:
			throw new IllegalArgumentException("unrecognized type: "+type.name());
		}
		return newField;
	}
	
	@Override
	public List<Field> getChildField() {
		return children;
	};
	
	short addValue(SBEValueNode value) {
		nodeIds[totalOccurrence] = value.getNodeId();
		return totalOccurrence ++;
	}
		
	SBEValueNode getFieldValue(short idx) {
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
	
	@Override
	public void getChildValues(Consumer<FieldValue> consumer) {
		short currentOccurrence = 0;
		for( short i = 0; i < getTotalOccurrence(); i ++ ) {
			SBEValueNode value = getFieldValue(i);
			int offset = value.getOffset();
			short numRows = value.getNumRows();
			for( short j = 0; j < numRows; j ++ ) {
				for( Field childField : children ) {
					switch( childField.getType() ) {
					case GROUP:
						childValue.setField(childField);
						consumer.accept(childValue);
						break;
					default:
						childValue.setField(childField);
						childValue.setOffset(offset+((SBEField) childField).getRelativeOffset()); 
						childValue.setCurrentOccurrence(currentOccurrence);
						consumer.accept(childValue);
						break;
					}
				}
				offset += value.getRowSize(j);
			}
		}
	}
	
	@Override
	public void getChildValues(short occurrence, Consumer<FieldValue> consumer) {
		short currentOccurrence = 0;
		SBEValueNode value = getFieldValue(occurrence);
		int offset = value.getOffset();
		short numRows = value.getNumRows();
		for( short j = 0; j < numRows; j ++ ) {
			for( Field childField : children ) {
				switch( childField.getType() ) {
				case GROUP:
					childValue.setField(childField);
					consumer.accept(childValue);
					break;
				default:
					childValue.setField(childField);
					childValue.setOffset(offset+((SBEField) childField).getRelativeOffset()); 
					childValue.setCurrentOccurrence(currentOccurrence);
					consumer.accept(childValue);
					break;
				}
			}
			offset += value.getRowSize(j);
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
			for( int k = complextNodeStartingPoint; k < children.size(); k ++ ) {
				Field field = children.get(k);
				if( FieldType.GROUP == field.getType() ) {
					currentOffset += ((SBECompositeField) field).wrapForRead(currentOffset);				
				} else if( FieldType.RAW == field.getType() ) {
					currentOffset += ((SBEVarLengthField) field).wrapForRead(currentOffset);
				}
			}
			grpValue.setRowSize(i, currentOffset - startOffset);
			grpValue.increaseSize(currentOffset - startOffset);
		}
		return grpValue.getSize();
	}		
}
