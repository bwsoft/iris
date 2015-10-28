package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;
import com.bunny.iris.message.Message;

import uk.co.real_logic.agrona.DirectBuffer;

public class SBEMessage extends AbstractSBECompositeField implements Message {
	
	private int messageHeaderSize = 8;
	private int size;
	
	private short valueCount;
	private SBEValueNode values[];

	private SBEFieldOp operator;
	
	private SBEValueNode fieldValue;
		
	public SBEMessage() {
		super(null);
		setType(FieldType.MESSAGE);
		operator = new SBEFieldOp();
		
		values = new SBEValueNode[128];
		valueCount = 0;
		for( int i = 0; i < 128; i ++ ) {
			values[i] = new SBEValueNode();
		}
	}
	
	@Override
	public Message getMessage() {
		return this;
	}
	
	@Override 
	public SBEFieldOp getFieldOp() {
		return operator;
	}
	
	public FieldValue allocate(boolean isHeaderNode) {
		SBEValueNode value = values[valueCount];
		value.reset();
		
		value.setNodeId(valueCount);
		value.setGroupHeadNode(isHeaderNode);
		valueCount ++;
		return value;
	}
	
	@Override
	public FieldValue getValueNode(short nodeId) {
		return values[nodeId];
	}
	
	@Override
	public int getTotalFields() {
		return valueCount;
	}
	
	public SBEMessage wrapForRead(DirectBuffer buffer, int offset, int blockLength, ByteOrder order) {
		valueCount = 0;
		this.reset();
		
		fieldValue = (SBEValueNode) allocate(false);
		fieldValue.setField(this);
		fieldValue.setOffset(offset);
		fieldValue.setBlockLength(blockLength);
		
		operator.bind(buffer).setByteOrder(order);
		
		int currentOffset = messageHeaderSize + offset + fieldValue.getBlockLength();
		int fieldOffset = messageHeaderSize + offset;
		for( SBEField field : getChildren() ) {
			if( FieldType.GROUP == field.getType() ) {
				SBEValueNode grpValue = ((SBEGroup) field).wrapForRead(operator, fieldValue, currentOffset);
				currentOffset += grpValue.getSize();
			} else {
				SBEValueNode subFieldValue = (SBEValueNode) field.allocateOccurrence(false);
				subFieldValue.setParentNode(fieldValue);
				subFieldValue.setOffset(fieldOffset);
				fieldValue.addChildNode(subFieldValue);
				fieldOffset += field.getBlockSize();
			}
		}
		size = currentOffset - offset;
		return this;
	}

	@Override
	public int getSize() {
		return size;
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
//		case RAW:
//			Field varLengthField = new SBEVarLengthField().setOp(getOp()).setByteOrder(getByteOrder()).setParent(this);
//			addChild(varLengthField);
//			return varLengthField;
		default:
			throw new IllegalArgumentException("unrecognized type: "+type.name());
		}
		
		return newField;
	}

	public void printClaims() {
		for( short i = 0; i < fieldValue.getNumChildNodes(); i ++ ) {
			FieldValue value = fieldValue.getChildNode(i);
			System.out.println(value.formattedString("  "));
		}
	}
}
