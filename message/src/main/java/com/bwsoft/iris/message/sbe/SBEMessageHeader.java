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
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

public class SBEMessageHeader implements SBEHeader {
	private final FieldType templateIdType;
	private final FieldType schemaIdType;
	private final FieldType blockSizeType;
	private final FieldType versionType;
	private final int blockSizeOffset;
	private final int templateIdOffset;
	private final int schemaIdOffset;
	private final int versionOffset;
	private final short headerSize;
	
	public SBEMessageHeader(FieldType templateIdType, FieldType schemaIdType, FieldType blockSizeType, FieldType versionType) {
		this.templateIdType = templateIdType;
		this.schemaIdType = schemaIdType;
		this.blockSizeType = blockSizeType;
		this.versionType = versionType;
		this.blockSizeOffset = 0;
		this.templateIdOffset = this.blockSizeOffset + this.blockSizeType.size();
		this.schemaIdOffset = this.templateIdOffset + this.templateIdType.size();
		this.versionOffset = this.schemaIdOffset + this.schemaIdType.size();
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

	public void putBlockSize(UnsafeBuffer buffer, int offset, ByteOrder order, int blockSize) {
		switch( this.blockSizeType ) {
		case U8:
		case I8:
			buffer.putByte(offset+blockSizeOffset, (byte) blockSize);
			break;

		case U16:
		case I16:
			buffer.putShort(offset+blockSizeOffset, (short) blockSize, order);
			break;
			
		default:
			throw new UnsupportedOperationException("Enum type, "+this.blockSizeType.name()+", not supported for block size");
		}
	}

	public int getTemplateId(DirectBuffer buffer, int offset, ByteOrder order) {
		switch( this.templateIdType ) {
		case U8:
		case I8:
			return buffer.getByte(offset+templateIdOffset);
		case U16:
		case I16:
			return buffer.getShort(offset+templateIdOffset,order);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.templateIdType.name()+", not supported for templateId");
		}
	}

	public void putTemplateId(UnsafeBuffer buffer, int offset, ByteOrder order, int templateId) {
		switch( this.templateIdType ) {
		case U8:
		case I8:
			buffer.putByte(offset+templateIdOffset, (byte) templateId);
			break;
			
		case U16:
		case I16:
			buffer.putShort(offset+templateIdOffset, (short) templateId, order);
			break;
			
		default:
			throw new UnsupportedOperationException("Enum type, "+this.templateIdType.name()+", not supported for templateId");
		}
	}

	public int getSchemaId(DirectBuffer buffer, int offset, ByteOrder order) {
		switch( this.schemaIdType ) {
		case U8:
		case I8:
			return buffer.getByte(offset+schemaIdOffset);
		case U16:
		case I16:
			return buffer.getShort(offset+schemaIdOffset,order);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.schemaIdType.name()+", not supported for schema");
		}
	}

	public void putSchemaId(UnsafeBuffer buffer, int offset, ByteOrder order, int schemaId) {
		switch( this.schemaIdType ) {
		case U8:
		case I8:
			buffer.putByte(offset+schemaIdOffset, (byte) schemaId);
			break;
			
		case U16:
		case I16:
			buffer.putShort(offset+schemaIdOffset,(short) schemaId, order);
			break;
			
		default:
			throw new UnsupportedOperationException("Enum type, "+this.schemaIdType.name()+", not supported for schema");
		}
	}

	public int getVersion(DirectBuffer buffer, int offset, ByteOrder order) {
		switch( this.versionType ) {
		case U8:
		case I8:
			return buffer.getByte(offset+versionOffset);
		case U16:
		case I16:
			return buffer.getShort(offset+versionOffset,order);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.versionType.name()+", not supported for version");
		}
	}

	public void putVersion(UnsafeBuffer buffer, int offset, ByteOrder order, int version) {
		switch( this.versionType ) {
		case U8:
		case I8:
			buffer.putByte(offset+versionOffset, (byte) version);
			break;
			
		case U16:
		case I16:
			buffer.putShort(offset+versionOffset,(short) version, order);
			break;
			
		default:
			throw new UnsupportedOperationException("Enum type, "+this.versionType.name()+", not supported for version");
		}
	}
}
