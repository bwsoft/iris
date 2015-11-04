package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

class SBEValueNode implements FieldValue {

	private AbstractSBEField definition;
	private short arrayId;
	
	private int offset;
	private int valueOffset;
	private int size;
	
	private short currentOccurrence;

	private short numRows;
	private int[] rowSize;
	
	public SBEValueNode() {
		numRows = 0;
		rowSize = new int[SBEMessage.MAX_FIELD_OCCURRENCE];
	}
	
	void reset() {
		numRows = 0;
	}
	
	short getNumRows() {
		return numRows;
	}

	void setNumRows(short numRows) {
		this.numRows = numRows;
	}

	int getRowSize(short idx) {
		return rowSize[idx];
	}

	void setRowSize(short idx, int rowSize) {
		this.rowSize[idx] = rowSize;
	}
	
	short getCurrentOccurrence() {
		return currentOccurrence;
	}

	void setCurrentOccurrence(short currentOccurrence) {
		this.currentOccurrence = currentOccurrence;
	}
	
	int getOffset() {
		return offset;
	}
	
	void setOffset(int offset) {
		this.offset = offset;
		this.valueOffset = this.offset + definition.getHeaderSize();
	}
	
	short getNodeId() {
		return arrayId;
	}

	void setNodeId(short id) {
		this.arrayId = id;
	}

	@Override
	public Field getField() {
		return definition;
	}

	@Override
	public void setField(Field field) {
		this.definition = (AbstractSBEField) field;
	}

	@Override
	public int getSize() {
		return size == 0 ? definition.getBlockSize()*definition.getDimension() : this.size;
	}

	@Override
	public void setSize(int size) {
		this.size = size;
	}
	
	void increaseSize(int size) {
		this.size += size;
	}
	
	@Override
	public char getChar(short index) {
		return getBuffer().getChar(valueOffset+index*definition.getBlockSize());
	}

	@Override
	public byte getByte(short index) {
		return getBuffer().getByte(valueOffset+index*definition.getBlockSize());
	}
	
	@Override
	public int getU16(short index) {
		return getBuffer().getShort(valueOffset+index*definition.getBlockSize(), getOrder());
	}
	
	@Override
	public int getInt(short index) {
		return getBuffer().getInt(valueOffset+index*definition.getBlockSize(), getOrder());
	}

	@Override
	public long getU64(short index) {
		return getBuffer().getLong(valueOffset+index*definition.getBlockSize(), getOrder());
	}
	
	@Override
	public int getBytes(byte[] dst, int offset) {
		if( dst.length - offset < this.getSize() ) {
			throw new ArrayIndexOutOfBoundsException("destination array size if less than the field size of: "+getSize());
		}
		getBuffer().getBytes(valueOffset, dst, offset, this.getSize());
		return this.getSize();
	}
	
	@Override
	public String getString(short index) {
		switch(definition.getType()) {
		case CHAR:
			return Character.toString(getChar(index));
		case BYTE:
			return Byte.toString(getByte(index));
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
		case RAW:
			int length = getSize() - definition.getHeaderSize();
			byte[] dstBuffer = new byte[length];
			getBuffer().getBytes(valueOffset, dstBuffer, 0, length);
			return new String(dstBuffer).trim();
		default:
			return "opaque of type: "+definition.getType().name()+", of size: "+size;
		}
	}
	
	@Override
	public String getEnumName() {
		if( definition instanceof SBEField ) {
			SBEField field = (SBEField) definition;
			return field.getEnumName(getString((short)0));
		} else {
			throw new UnsupportedOperationException("no enum conversion for type: "+definition.getType());
		}
	}

	@Override
	public boolean isSet(String bitName) {
		if( definition instanceof SBEField ) {
			SBEField field = (SBEField) definition;
			int bitIndex = field.getSetBit(bitName);
			long value = getU64((short) 0);
			return 0 != (value & (1 << bitIndex));
		} else {
			throw new UnsupportedOperationException("no bit set conversion for type: "+definition.getType());			
		}
	}
	
	void initCompositeNode() {
		if( FieldType.GROUP == definition.getType() ) {
			((SBEGroup)definition).setBlockSize(getBuffer().getShort(offset,getOrder()));
			this.setNumRows(getBuffer().getByte(offset+2));
		} else if( FieldType.MESSAGE == definition.getType() ) {
			this.setNumRows((short) 1);
		}
	}
	
	void initVarLengthNode() {
		switch( definition.getHeaderSize() ) {
		case 1:
			definition.setBlockSize(getBuffer().getByte(offset));
			break;
		case 2:
			definition.setBlockSize(getBuffer().getShort(offset,getOrder()));
			break;
		default:
			throw new IllegalArgumentException("SBE VAR field header size can only be 1 or 2 (default)");
		}
	}

	private UnsafeBuffer getBuffer() {
		return ((SBEMessage)definition.getMessage()).getBuffer();
	}
	
	private ByteOrder getOrder() {
		return ((SBEMessage)definition.getMessage()).getByteOrder();
	}
}
