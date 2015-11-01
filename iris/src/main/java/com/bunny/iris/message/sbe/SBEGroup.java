package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

class SBEGroup extends AbstractSBEField {
	// child field definition
	private List<Field> children = new ArrayList<>();
	
	private short totalOccurrence;
	private short[] nodeIds;

	// child value node
	private SBEValueNode childValue;
	
	private short numFixedSizeFields;
	
	public SBEGroup(SBEMessage message) {
		super(message);
		totalOccurrence = 0;
		nodeIds = new short[SBEMessage.MAX_FIELD_OCCURRENCE];
		childValue = new SBEValueNode();
		numFixedSizeFields = 0;
	}
	
	void reset() {
		totalOccurrence = 0;
		for( Field field : children ) {
			((AbstractSBEField) field).reset();
		}
		super.reset();
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
					break;
				}				
			}
			((AbstractSBEField) field).finalized();
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
		case CHAR:
			newField = new SBEField(getMessage()).setParent(this).setType(type);
			children.add(newField);
			numFixedSizeFields ++;
			break;
		case GROUP:
			newField = new SBEGroup(getMessage()).setParent(this).setHeaderSize(getMessage().getGroupHeaderSize()).setType(FieldType.GROUP);
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
			for( int k = numFixedSizeFields; k < children.size(); k ++ ) {
				Field field = children.get(k);
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
	
	public Field getField(short id) {
		for( Field field : getChildField() ) {
			if( field.getID() == id ) {
				return field;
			}
			
			if( field.getType() == FieldType.GROUP ) {
				Field subField = ((SBEGroup) field).getField(id);
				if( subField != null ) 
					return subField;
			}
		}
		return null;
	}
	
	@Override
	public SBEGroup setArraySize(short size) {
		// cannot set array size
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getName()).append("{name=").append(getName()).append(",id=").append(getID());
		sb.append(",children=[");
		for( Field field : children ) {
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
