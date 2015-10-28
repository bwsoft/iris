package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

import com.bunny.iris.message.FieldOp;
import com.bunny.iris.message.FieldValue;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

public class SBEFieldOp implements FieldOp {
	private SBEValueNode field;
	private UnsafeBuffer buffer = new UnsafeBuffer(new byte[0]);
	private ByteOrder order;

	SBEFieldOp bind(DirectBuffer buffer) {
		this.buffer.wrap(buffer);
		return this;
	}
	
	@Override
	public SBEFieldOp bind(FieldValue field) {
		this.field = (SBEValueNode) field;
		return this;
	}
	
	public ByteOrder getByteOrder() {
		return order;
	}
	
	public SBEFieldOp setByteOrder(ByteOrder order) {
		this.order = order;
		return this;
	}
	
	@Override
	public byte getByte(short index) {
		return buffer.getByte(field.getOffset()+index*((SBEField)field.getField()).getBlockSize());
	}
	
	@Override
	public int getU16(short index) {
		return buffer.getShort(field.getOffset()+index*((SBEField)field.getField()).getBlockSize(), order);
	}
	
	@Override
	public int getInt(short index) {
		return buffer.getInt(field.getOffset()+index*((SBEField)field.getField()).getBlockSize(), order);
	}

	@Override
	public long getU64(short index) {
		return buffer.getLong(field.getOffset()+index*((SBEField)field.getField()).getBlockSize(), order);
	}
	
	@Override
	public String getString(short index) {
		switch(field.getField().getType()) {
		case I8:
		case U8:
			long value = getByte(index);
			return String.valueOf(value);
		case U16:
			value = getU16(index);
			return String.valueOf(value);
		case I32:
			value = getInt(index);
			return String.valueOf(value);
		case U64:
			value = getU64(index);
			return String.valueOf(value);
		default:
			return "opaque of type: "+field.getField().getType().name()+", of size: "+field.getSize();
		}
	}

	short fetchGroupNode() {
		int blockLength = this.buffer.getShort(field.getOffset(),order);
		byte numGroups = this.buffer.getByte(field.getOffset()+2);
		field.setBlockLength(blockLength);
		return numGroups;
	}
	
//	boolean fetchVarLengthNode(short parentId) {
//		SBEVarLengthField vlField = (SBEVarLengthField) field;
//		int originalBlockLength = vlField.getBlockSize();
//		int blockLength = 0;
//		switch( vlField.getHeaderSize() ) {
//		case 1:
//			blockLength = this.buffer.getByte(vlField.getHeaderOffset(parentId));
//			break;
//		case 2:
//			blockLength = this.buffer.getShort(vlField.getHeaderOffset(parentId),vlField.getByteOrder());
//			break;
//		default:
//			throw new IllegalArgumentException("SBE VAR field header size can only be 1 or 2 (default)");
//		}
//
//		if( originalBlockLength != blockLength ) {
//			vlField.setBlockSize(blockLength);
//			return true;
//		} else {
//			return false;
//		}		
//	}
}
