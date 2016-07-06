package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

import com.bunny.iris.message.FieldType;

import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

/**
 * This group header supports the header format that consists of 1 or 2 bytes of block size 
 * and 1 or 2 bytes of number of rows. 
 * 
 * @author yzhou
 *
 */
public class SBEGroupHeader implements aSBEHeader {
	private final FieldType numInGroupType;
	private final FieldType blockSizeType;
	
	private final short headerSize;
	
	public SBEGroupHeader(FieldType numInGroupType, FieldType blockSizeType) {
		this.numInGroupType = numInGroupType;
		this.blockSizeType = blockSizeType;
		this.headerSize = (short) (this.numInGroupType.size() + this.blockSizeType.size());
	}
	
	public short getHeaderSize() {
		return headerSize;
	}
	
	public int getBlockSize(UnsafeBuffer buffer, int groupStartOffset, ByteOrder order) {
		switch( this.blockSizeType ) {
		case U8:
		case I8:
			return buffer.getByte(groupStartOffset);
		case U16:
		case I16:
			return buffer.getShort(groupStartOffset,order);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.blockSizeType.name()+", not supported for block size");
		}
	}
	
	public int getNumRows(UnsafeBuffer buffer, int groupStartOffset, ByteOrder order) {
		switch( this.numInGroupType ) {
		case U8:
		case I8:
			return buffer.getByte(groupStartOffset+this.blockSizeType.size());
		case U16:
		case I16:
			return buffer.getShort(groupStartOffset+this.blockSizeType.size(),order);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.numInGroupType.name()+", not supported for num in group");
		}
	}
}
