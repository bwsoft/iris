/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.bwsoft.iris.message.sbe;

import java.nio.ByteOrder;

import com.bwsoft.iris.message.FieldType;

import uk.co.real_logic.agrona.DirectBuffer;

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
