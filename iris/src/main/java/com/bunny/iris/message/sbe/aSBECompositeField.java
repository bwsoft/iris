package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.List;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.aField;
import com.bunny.iris.message.aGroup;

public class aSBECompositeField extends aSBEField implements aGroup {
	private final aSBEHeader header = new aSBEHeader() {
		@Override
		public short getHeaderSize() {
			return 0;
		}		
	};
	
	// child field definition
	private final List<aField> children = new ArrayList<>();

	aSBECompositeField(aSBEGroup parent, short dimension) {
		super(parent, FieldType.COMPOSITE, dimension);
	}
	
	@Override
	public List<aField> getChildFields() {
		return children;
	}

	@Override 
	public aField getChildField(short id) {
		return children.get(id);
	}
	
	@Override
	public aField addChildField(short id, FieldType type, short dimmension) {
		aSBEField newField = null;
		int currentOffset = header.getHeaderSize();
		
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
				currentOffset = lastField.getBlockSize() + lastField.getRelativeOffset(); 
			}
			newField = new aSBEField(this, type, dimmension).setRelativeOffset(currentOffset);
			this.setBlockSize(this.getBlockSize()+newField.getBlockSize()*newField.getDimension());
			children.add(newField);
			break;
		default:
			throw new IllegalArgumentException("composite field does not accept the child field of type: "+type);
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
