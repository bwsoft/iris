/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package com.bwsoft.iris.message.sbe;

import java.nio.ByteOrder;

import com.bwsoft.iris.message.FieldHeader;
import com.bwsoft.iris.message.FieldType;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

/**
 * This group header supports the header format that consists of 1 or 2 bytes of block size 
 * and 1 or 2 bytes of number of rows. 
 * 
 * @author yzhou
 *
 */
class SBEGroupHeader implements FieldHeader {
	private final FieldType numInGroupType;
	private final FieldType blockSizeType;
	
	private final short headerSize;
	
	public SBEGroupHeader(FieldType numInGroupType, FieldType blockSizeType) {
		this.numInGroupType = numInGroupType;
		this.blockSizeType = blockSizeType;
		this.headerSize = (short) (this.numInGroupType.size() + this.blockSizeType.size());
	}
	
	@Override
	public short getSize() {
		return headerSize;
	}
	
	int getBlockSize(DirectBuffer buffer, int groupStartOffset, ByteOrder order) {
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
	
	int getNumRows(DirectBuffer buffer, int groupStartOffset, ByteOrder order) {
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

	void putBlockSize(UnsafeBuffer buffer, int groupStartOffset, ByteOrder order, int blockSize) {
		switch( this.blockSizeType ) {
		case U8:
		case I8:
			buffer.putByte(groupStartOffset, (byte) blockSize); 
			break;

		case U16:
		case I16:
			buffer.putShort(groupStartOffset, (short) blockSize, order);
			break;

		default:
			throw new UnsupportedOperationException("Enum type, "+this.blockSizeType.name()+", not supported for block size");
		}
	}
	
	void putNumRows(UnsafeBuffer buffer, int groupStartOffset, ByteOrder order, int numRows) {
		switch( this.numInGroupType ) {
		case U8:
		case I8:
			buffer.putByte(groupStartOffset+this.blockSizeType.size(), (byte) numRows); 
			break;

		case U16:
		case I16:
			buffer.putShort(groupStartOffset+this.blockSizeType.size(), (short) numRows, order);
			break;

		default:
			throw new UnsupportedOperationException("Enum type, "+this.numInGroupType.name()+", not supported for num in group");
		}
	}
}
