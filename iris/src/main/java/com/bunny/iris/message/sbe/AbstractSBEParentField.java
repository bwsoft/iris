package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

abstract class AbstractSBEParentField extends AbstractSBEField {
	// child field definition
	private List<Field> children = new ArrayList<>();
	private short numFixedSizeFields;

	public AbstractSBEParentField(SBEMessage message) {
		super(message, (short) 1);
		numFixedSizeFields = 0;
	}
	
	void reset() {
		for( short i = numFixedSizeFields; i < children.size(); i ++ ) {
			AbstractSBEField field = (AbstractSBEField) children.get(i);
			((AbstractSBEField) field).reset();
		}
		super.reset();
	}
	
	@Override
	public List<Field> getChildField() {
		return children;
	};
	
	@Override
	public Field addChildField(FieldType type, short dimmension) {
		AbstractSBEField newField = null;
		int currentOffset = getHeaderSize();
		
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
				SBEField lastField = (SBEField) children.get(children.size()-1);
				currentOffset = lastField.getBlockSize()*lastField.getDimension() + lastField.getRelativeOffset(); 
			}
			newField = new SBEField(getMessage(), dimmension).setRelativeOffset(currentOffset).setParent(this).setType(type);
			children.add(newField);
			numFixedSizeFields ++;
			break;
		case COMPOSITE:
			if( children.size() > 0 ) {
				SBEField lastField = (SBEField) children.get(children.size()-1);
				currentOffset = lastField.getDimension()*lastField.getBlockSize() + lastField.getRelativeOffset(); 
			}
			newField = new SBECompositeField(getMessage()).setRelativeOffset(currentOffset).setParent(this).setType(FieldType.COMPOSITE);
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
	
	abstract void getChildValues(SBEValueNode myCurrentValue, Consumer<FieldValue> consumer);
	
	public short getNumFixedSizeFields() {
		return numFixedSizeFields;
	}
}
