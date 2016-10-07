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
package com.github.bwsoft.iris.message.sbe;

import java.nio.ByteBuffer;
import java.util.List;

import com.github.bwsoft.iris.message.FieldHeader;
import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.sbe.SBESchemaFieldTypes.SBECompositeTypeElement;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.EncodedDataType;

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
	
	/**
	 * Get SBEMessageHeader definition
	 * 
	 * @param cache the loaded schema.
	 * @return
	 */
	static SBEMessageHeader getMessageHeader(SBESchemaFieldTypes cache) {
		SBEMessageHeader msgHeader = null;
		if( cache.getCompositeDataTypes().containsKey("messageHeader") ) {
			List<SBECompositeTypeElement> eTypes = cache.getCompositeDataTypes().get("messageHeader");
			FieldType blockLength = FieldType.U16;
			FieldType templateId = FieldType.U16;
			FieldType schemaId = FieldType.U16;
			FieldType version = FieldType.U16;
			for( SBECompositeTypeElement rawType : eTypes ) {
				if( ! (rawType.getType() instanceof EncodedDataType) ) {
					throw new IllegalArgumentException("Unsupported SBE type in messageHeader definition");
				}
				EncodedDataType type = (EncodedDataType) rawType.getType();
				if( "blockLength".equals(type.getName()) )
					blockLength = FieldType.getType(type.getPrimitiveType());
				else if( "templateId".equals(type.getName()) )
					templateId = FieldType.getType(type.getPrimitiveType());
				else if( "schemaId".equals(type.getName()) )
					schemaId = FieldType.getType(type.getPrimitiveType());
				else if( "version".equals(type.getName()) )
					version = FieldType.getType(type.getPrimitiveType());
			}
			
			if( null == blockLength || null == templateId || null == schemaId || null == version ) {
				throw new IllegalArgumentException("unrecgnized primitive type in message header definition");
			}
			
			msgHeader = new SBEMessageHeader(templateId, schemaId, blockLength, version);
		} else {
			msgHeader = new SBEMessageHeader(FieldType.U16, FieldType.U16, FieldType.U16, FieldType.U16);
		}
		return msgHeader;
	}
	
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
	 * @return the message block size in bytes
	 */
	public int getBlockSize(ByteBuffer buffer, int offset) {
		switch( this.blockSizeType ) {
		case U8:
		case I8:
			return buffer.get(offset+blockSizeOffset);
		case U16:
		case I16:
			return buffer.getShort(offset+blockSizeOffset);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.blockSizeType.name()+", not supported for block size");
		}
	}

	void putBlockSize(ByteBuffer buffer, int offset, int blockSize) {
		switch( this.blockSizeType ) {
		case U8:
		case I8:
			buffer.put(offset+blockSizeOffset, (byte) blockSize);
			break;

		case U16:
		case I16:
			buffer.putShort(offset+blockSizeOffset, (short) blockSize);
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
	 * @return the template ID of the message
	 */
	public int getTemplateId(ByteBuffer buffer, int offset) {
		switch( this.templateIdType ) {
		case U8:
		case I8:
			return buffer.get(offset+templateIdOffset);
		case U16:
		case I16:
			return buffer.getShort(offset+templateIdOffset);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.templateIdType.name()+", not supported for templateId");
		}
	}

	void putTemplateId(ByteBuffer buffer, int offset, int templateId) {
		switch( this.templateIdType ) {
		case U8:
		case I8:
			buffer.put(offset+templateIdOffset, (byte) templateId);
			break;
			
		case U16:
		case I16:
			buffer.putShort(offset+templateIdOffset, (short) templateId);
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
	 * @return the schema ID of the message
	 */
	public int getSchemaId(ByteBuffer buffer, int offset) {
		switch( this.schemaIdType ) {
		case U8:
		case I8:
			return buffer.get(offset+schemaIdOffset);
		case U16:
		case I16:
			return buffer.getShort(offset+schemaIdOffset);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.schemaIdType.name()+", not supported for schema");
		}
	}

	void putSchemaId(ByteBuffer buffer, int offset, int schemaId) {
		switch( this.schemaIdType ) {
		case U8:
		case I8:
			buffer.put(offset+schemaIdOffset, (byte) schemaId);
			break;
			
		case U16:
		case I16:
			buffer.putShort(offset+schemaIdOffset,(short) schemaId);
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
	 * @return the version of the message
	 */
	public int getVersion(ByteBuffer buffer, int offset) {
		switch( this.versionType ) {
		case U8:
		case I8:
			return buffer.get(offset+versionOffset);
		case U16:
		case I16:
			return buffer.getShort(offset+versionOffset);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.versionType.name()+", not supported for version");
		}
	}

	void putVersion(ByteBuffer buffer, int offset, int version) {
		switch( this.versionType ) {
		case U8:
		case I8:
			buffer.put(offset+versionOffset, (byte) version);
			break;
			
		case U16:
		case I16:
			buffer.putShort(offset+versionOffset,(short) version);
			break;
			
		default:
			throw new UnsupportedOperationException("Enum type, "+this.versionType.name()+", not supported for version");
		}
	}
}
