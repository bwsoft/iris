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
 * Header for the SBE message.
 * 
 * @author yzhou
 *
 */
public class SBEMessageHeader implements FieldHeader {
	private final FieldType templateIdType;
	private final FieldType schemaIdType;
	private final FieldType blockSizeType;
	private final FieldType versionType;
	private final int blockSizeOffset;
	private final int templateIdOffset;
	private final int schemaIdOffset;
	private final int versionOffset;
	private final short headerSize;
	
	SBEMessageHeader(FieldType templateIdType, FieldType schemaIdType, FieldType blockSizeType, FieldType versionType) {
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
	
	@Override
	public short getSize() {
		return this.headerSize;
	}

	/**
	 * Get the block size from the message header
	 * 
	 * @param buffer SBE message buffer
	 * @param offset the start position in the buffer
	 * @param order ByteOrder
	 * @return the message block size in bytes
	 */
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

	void putBlockSize(UnsafeBuffer buffer, int offset, ByteOrder order, int blockSize) {
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

	/**
	 * Return the message template ID from the SBE buffer.
	 * 
	 * @param buffer the SBE buffer 
	 * @param offset the start position of the SBE message in the buffer
	 * @param order ByteOrder
	 * @return the template ID of the message
	 */
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

	void putTemplateId(UnsafeBuffer buffer, int offset, ByteOrder order, int templateId) {
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

	/**
	 * Return the schema ID from the message buffer.
	 * 
	 * @param buffer the SBE buffer
	 * @param offset the start position of the SBE message in the buffer
	 * @param order ByteOrder
	 * @return the schema ID of the message
	 */
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

	void putSchemaId(UnsafeBuffer buffer, int offset, ByteOrder order, int schemaId) {
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

	/**
	 * Return the version of the message from the SBE buffer
	 * 
	 * @param buffer the SBE buffer
	 * @param offset the starting position of the SBE message in the buffer
	 * @param order ByteOrder 
	 * @return the version of the message
	 */
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

	void putVersion(UnsafeBuffer buffer, int offset, ByteOrder order, int version) {
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
