package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

public class SBEMessage extends SBECompositeField {
	public static final short MAX_FIELD_OCCURRENCE=128;
	
	private short groupHeaderSize = 3;
	private short varDataHeaderSize = 2;
	
	private int size;
	
	private short valueCount;
	private SBEValueNode values[];

	private UnsafeBuffer buffer = new UnsafeBuffer(new byte[0]);
	private ByteOrder order;
	
	public SBEMessage() {
		super(null);
		setType(FieldType.MESSAGE);
		
		values = new SBEValueNode[MAX_FIELD_OCCURRENCE];
		for( short i = 0; i < values.length; i ++ ) {
			values[i] = new SBEValueNode();
			values[i].setNodeId(i);
		}
		
		valueCount = 0;
		
		order = ByteOrder.nativeOrder();
	}
	
	SBEValueNode allocate() {
		SBEValueNode value = values[valueCount];
		value.reset();
		valueCount ++;
		return value;
	}
	
	SBEValueNode getValueNode(short nodeId) {
		return values[nodeId];
	}
	
	public UnsafeBuffer getBuffer() {
		return buffer;
	}
	
	public ByteOrder getByteOrder() {
		return order;
	}
	
	public SBEMessage setByteOrder(ByteOrder order) {
		this.order = order;
		return this;
	}

	@Override
	public SBEMessage setBlockSize(int length) {
		return (SBEMessage) super.setBlockSize(length);
	}
	
	public SBEMessage setMessageHeaderSize(short size) {
		this.setHeaderSize(size);
		return this;
	}
	
	public SBEMessage setGroupHeaderSize(short size) {
		this.groupHeaderSize = size;
		return this;
	}
	
	public short getGroupHeaderSize() {
		return this.groupHeaderSize;
	}
	
	public SBEMessage setVarDataHeaderSize(short size) {
		this.varDataHeaderSize = size;
		return this;
	}
	
	public short getVarDataHeaderSize() {
		return this.varDataHeaderSize;
	}
	
	public int getTotalFields() {
		return valueCount;
	}
	
	public int getSize() {
		return size;
	}
	
	@Override
	public SBEMessage getMessage() {
		return this;
	}
	
	public SBEMessage wrapForRead(DirectBuffer buffer, int offset) {
		this.reset();
		for( Field field : getChildField() ) {
			((AbstractSBEField) field).reset();
		}
		
		this.buffer.wrap(buffer);
		
		valueCount = 0;
		
		size = this.wrapForRead(offset);
		return this;
	}
}
