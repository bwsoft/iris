package com.bunny.iris.message.sbe;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.Message;

class SBEGroup extends AbstractSBECompositeField {

	private int groupHeaderSize = 3;
	
	public SBEGroup(Message message) {
		super(message);
		setType(FieldType.GROUP);
	}
	
	@Override
	public SBEField addChild(FieldType type) {
		SBEField newField = null;
		
		switch( type ) {
		case U8:
		case U16:
		case U32:
		case U64:
		case I32:
		case BYTE:
			newField = new SBEPrimitiveField(getMessage()).setParent(this).setType(type);
			addChild(newField);
			break;
		case GROUP:
			newField = new SBEGroup(getMessage()).setParent(this);
			addChild(newField);
			break;
		default:
			throw new IllegalArgumentException("unrecognized type: "+type.name());
		}
		return newField;
	}
	
	SBEValueNode wrapForRead(SBEFieldOp op, SBEValueNode fieldValue, int offset) {
		this.reset();
		
		SBEValueNode grpValue = (SBEValueNode) this.allocateOccurrence(true);
		grpValue.setOffset(offset);
		short numGroup = op.bind(grpValue).fetchGroupNode();
		
		grpValue.setParentNode(fieldValue);
		grpValue.setSize(groupHeaderSize);
		grpValue.claimChildren(numGroup);
		
		int currentOffset = offset + groupHeaderSize;
		for( short i = 0; i < numGroup; i ++ ) {
			SBEValueNode value = (SBEValueNode) this.allocateOccurrence(false);
			value.setParentNode(grpValue);
			value.setOffset(currentOffset);
			grpValue.addChildNode(value);
			
			int startOffset = currentOffset;
			int fieldOffset = currentOffset;
			currentOffset += grpValue.getBlockLength();
			for( SBEField field : getChildren() ) {
				switch( field.getType() ) {
				case GROUP:
					SBEValueNode tmpValue = ((SBEGroup) field).wrapForRead(op, value, currentOffset);
					currentOffset += tmpValue.getSize();
					break;
				default:
					field.reset();
					
					SBEValueNode subFieldValue = (SBEValueNode) field.allocateOccurrence(false);
					subFieldValue.setParentNode(value);
					subFieldValue.setOffset(fieldOffset);
					value.addChildNode(subFieldValue);
					fieldOffset += field.getBlockSize();
					break;
				}
			}
			value.setSize(currentOffset - startOffset);
			grpValue.setSize(value.getSize()+grpValue.getSize());
		}
		fieldValue.addChildNode(grpValue);
		return grpValue;
	}	
}
