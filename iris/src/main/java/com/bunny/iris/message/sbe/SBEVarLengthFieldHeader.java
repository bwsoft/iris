package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

import com.bunny.iris.message.FieldType;

import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

public class SBEVarLengthFieldHeader implements SBEHeader {
	private final short headerSize;
	private final FieldType lengthType;
	
	private ByteOrder order;
	
	public SBEVarLengthFieldHeader(FieldType lengthType) {
		this.lengthType = lengthType;
		headerSize = (short) this.lengthType.size();
	}
	
	public short getHeaderSize() {
		return headerSize;
	}
	
	public int getBlockSize(UnsafeBuffer buffer, int startOffset) {
		switch( headerSize ) {
		case 1:
			return buffer.getByte(startOffset);
		case 2:
			return buffer.getShort(startOffset,order);
		default:
			throw new IllegalArgumentException("SBE VAR field header size can only be 1 or 2 (default)");
		}
	}
}
