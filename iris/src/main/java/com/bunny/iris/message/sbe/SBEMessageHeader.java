package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

import com.bunny.iris.message.FieldType;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

public class SBEMessageHeader implements SBEHeader {
	private final FieldType templateIdType;
	private final FieldType schemaIdType;
	private final FieldType blockSizeType;
	private final FieldType versionType;
	private final int blockSizeOffset;
	private final short headerSize;
	
	public SBEMessageHeader(FieldType templateIdType, FieldType schemaIdType, FieldType blockSizeType, FieldType versionType) {
		this.templateIdType = templateIdType;
		this.schemaIdType = schemaIdType;
		this.blockSizeType = blockSizeType;
		this.versionType = versionType;
		this.blockSizeOffset = 0;
		this.headerSize = (short) (this.templateIdType.size() + this.schemaIdType.size() + this.blockSizeType.size() + this.versionType.size());
	}
	
	public short getHeaderSize() {
		return this.headerSize;
	}

	public int getBlockSize(DirectBuffer buffer, int offset, ByteOrder order) {
		switch( this.blockSizeType ) {
		case U8:
		case I8:
			return buffer.getByte(offset+blockSizeOffset);
		case U16:
		case I16:
			return buffer.getShort(offset+blockSizeOffset,order);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.blockSizeType.name()+", not supported for block size");
		}
	}
}
