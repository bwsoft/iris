package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.List;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.Field;
import com.bunny.iris.message.Group;

/**
 * Represents a composite field in SBE message. The subfield is retrieved by its 
 * sequence in the definition with the first one starting at the index zero.
 * 
 * @author yzhou
 *
 */
public class SBECompositeField extends SBEField implements Group {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final SBEHeader header = new SBEHeader() {
		@Override
		public short getHeaderSize() {
			return 0;
		}		
	};
	
	// child field definition
	private final List<Field> children = new ArrayList<>();

	SBECompositeField(SBEGroup parent, short dimension) {
		super(parent, FieldType.COMPOSITE, dimension);
	}
	
	@Override
	public List<Field> getChildFields() {
		return children;
	}

	@Override 
	public Field getChildField(short id) {
		return children.get(id);
	}
	
	@Override
	public Field addChildField(short id, FieldType type, short arrayLength) {
		if( arrayLength < 1 ) {
			throw new IllegalArgumentException("zero length is not allowed");
		}
		SBEField newField = null;
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
				SBEField lastField = (SBEField) children.get(children.size()-1);
				currentOffset = lastField.getBlockSize() + lastField.getRelativeOffset(); 
			}
			newField = new SBEField(this, type, arrayLength).setRelativeOffset(currentOffset);
			this.setBlockSize(this.getBlockSize()+newField.getBlockSize()*newField.length());
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
		for( Field field : this.children ) {
			sb.append(",").append(field);
		}
		sb.append("}");
		return sb.toString();
	}
}
