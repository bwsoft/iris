package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.Message;

class SBEVarLengthField extends SBEField {

//	private ByteOrder order;
//	
//	private int headerSize = 2;
//	private int headerOffset[];
//	
//	private SBEFieldOp operator;
//
	public SBEVarLengthField(Message message) {
		super(message);
		setType(FieldType.RAW);
//		headerOffset = new int[DEFAULT_MAX_REPEATS];
	}
//	
//	public SBEVarLengthField(int maxRepeats) {
//		setType(FieldType.RAW);
//		headerOffset = new int[maxRepeats];
//	}
//
//	@Override
//	public int getHeaderSize() {
//		return headerSize;
//	}
//	
//	@Override
//	public Field setHeaderSize(int headerSize) {
//		if( headerSize == 0 || headerSize > 2 ) {
//			throw new IllegalArgumentException("header size has to be 1 or 2 (default)");
//		}
//		this.headerSize = headerSize;
//		return this;
//	}
//
//	@Override
//	public Field setRepeat(short repeats) {
//		throw new UnsupportedOperationException("not supported for type: " + getType().name());
//	}
//
//	@Override
//	public int getSize() {
//		return getHeaderSize() + getBlockSize();
//	}
//
//	@Override
//	public ByteOrder getByteOrder() {
//		return order;
//	}
//
//	@Override
//	public SBEField setByteOrder(ByteOrder order) {
//		this.order = order;
//		return this;
//	}
//
//	@Override
//	public int getHeaderOffset(short parentId) {
//		return headerOffset[parentId];
//	}
//
//	@Override
//	public SBEField setHeaderOffset(short parentId, int offset) {
//		this.headerOffset[parentId] = offset;
//		return this;
//	}
//
//	@Override
//	public int getRepeatOffset(short rowId) {
//		if( rowId == 0 )
//			return getHeaderOffset((short) 0) + getHeaderSize();
//		else
//			throw new ArrayIndexOutOfBoundsException("repeat cannot exceed 1");	
//	}
//
//	@Override
//	public SBEField setRepeatOffset(short rowId, int offset) {
//		throw new UnsupportedOperationException("not supported for type: " + getType().name());
//	}
//
//	@Override
//	public SBEFieldOp getOp() {
//		return operator;
//	}
//
//	@Override
//	public SBEField setOp(SBEFieldOp op) {
//		this.operator = op;
//		return this;
//	}
//
//	int wrapForRead(short parentId, int offset) {
//		setHeaderOffset(parentId, offset);
//		getOp().bind(this).fetchVarLengthNode(parentId);
//
//		return getSize();
//	}	
}
