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
import java.util.HashMap;
import java.util.List;

import com.github.bwsoft.iris.message.FieldHeader;
import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.sbe.SBESchemaFieldTypes.SBECompositeTypeElement;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.EncodedDataType;

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
	
	/**
	 * Get group header definition.
	 * 
	 * @param cache SBE schema loaded from the xml file.
	 * @return
	 */
	static SBEGroupHeader getDefaultGroupHeader(SBESchemaFieldTypes cache) {
		SBEGroupHeader grpHeader = null;
		if( cache.getCompositeDataTypes().containsKey("groupSizeEncoding") ) {
			List<SBECompositeTypeElement> eTypes = cache.getCompositeDataTypes().get("groupSizeEncoding");
			FieldType numInGroupType = FieldType.U8;
			FieldType blockSizeType = FieldType.U16;
			for( SBECompositeTypeElement rawType : eTypes ) {
				if( ! (rawType.getType() instanceof EncodedDataType) ) {
					throw new IllegalArgumentException("Unsupported SBE type in groupSizeEncoding definition");
				}
				EncodedDataType type = (EncodedDataType) rawType.getType();
				if( "blockLength".equals(type.getName()) )
					blockSizeType = FieldType.getType(type.getPrimitiveType());
				else if( "numInGroup".equals(type.getName()) )
					numInGroupType = FieldType.getType(type.getPrimitiveType());
			}
			
			if( null == numInGroupType || null == blockSizeType ) {
				throw new IllegalArgumentException("unrecgnized primitive type in group header definition");	
			}
			grpHeader = new SBEGroupHeader(numInGroupType, blockSizeType);
		} else {
			grpHeader = new SBEGroupHeader(FieldType.U8, FieldType.U16);
		}
		return grpHeader;
	}
	
	/**
	 * Get group header definition.
	 * 
	 * @param cache SBE schema loaded from the xml file.
	 * @return
	 */
	static SBEGroupHeader getGroupHeader(SBESchemaFieldTypes cache, String compositeTypeName) {
		if("groupSizeEncoding".equals(compositeTypeName) )
			return null;
		
		SBEGroupHeader grpHeader = null;
		if( cache.getCompositeDataTypes().containsKey(compositeTypeName) ) {
			List<SBECompositeTypeElement> eTypes = cache.getCompositeDataTypes().get(compositeTypeName);
			FieldType numInGroupType = FieldType.U8;
			FieldType blockSizeType = FieldType.U16;
			for( SBECompositeTypeElement rawType : eTypes ) {
				if( ! (rawType.getType() instanceof EncodedDataType) ) {
					throw new IllegalArgumentException("Unsupported SBE type in "+compositeTypeName+" definition");
				}
				EncodedDataType type = (EncodedDataType) rawType.getType();
				if( "blockLength".equals(type.getName()) )
					blockSizeType = FieldType.getType(type.getPrimitiveType());
				else if( "numInGroup".equals(type.getName()) )
					numInGroupType = FieldType.getType(type.getPrimitiveType());
			}
			
			if( null == numInGroupType || null == blockSizeType ) {
				throw new IllegalArgumentException("unrecgnized primitive type in group header definition");	
			}
			grpHeader = new SBEGroupHeader(numInGroupType, blockSizeType);
		} else {
			grpHeader = new SBEGroupHeader(FieldType.U8, FieldType.U16);
		}
		return grpHeader;
	}

	SBEGroupHeader(FieldType numInGroupType, FieldType blockSizeType) {
		this.numInGroupType = numInGroupType;
		this.blockSizeType = blockSizeType;
		this.headerSize = (short) (this.numInGroupType.size() + this.blockSizeType.size());
	}
	
	@Override
	public short getSize() {
		return headerSize;
	}
	
	int getBlockSize(ByteBuffer buffer, int groupStartOffset) {
		switch( this.blockSizeType ) {
		case U8:
		case I8:
			return buffer.get(groupStartOffset);
		case U16:
		case I16:
			return buffer.getShort(groupStartOffset);
		default:
			throw new UnsupportedOperationException("Enum type, "+this.blockSizeType.name()+", not supported for block size");
		}
	}
	
	int getNumRows(ByteBuffer buffer, int groupStartOffset) {
		switch( this.numInGroupType ) {
		case U8:
		case I8:
			return buffer.get(groupStartOffset+this.blockSizeType.size());
		case U16:
		case I16:
			return buffer.getShort(groupStartOffset+this.blockSizeType.size());
		default:
			throw new UnsupportedOperationException("Enum type, "+this.numInGroupType.name()+", not supported for num in group");
		}
	}

	void putBlockSize(ByteBuffer buffer, int groupStartOffset, int blockSize) {
		switch( this.blockSizeType ) {
		case U8:
		case I8:
			buffer.put(groupStartOffset, (byte) blockSize); 
			break;

		case U16:
		case I16:
			buffer.putShort(groupStartOffset, (short) blockSize);
			break;

		default:
			throw new UnsupportedOperationException("Enum type, "+this.blockSizeType.name()+", not supported for block size");
		}
	}
	
	void putNumRows(ByteBuffer buffer, int groupStartOffset, int numRows) {
		switch( this.numInGroupType ) {
		case U8:
		case I8:
			buffer.put(groupStartOffset+this.blockSizeType.size(), (byte) numRows); 
			break;

		case U16:
		case I16:
			buffer.putShort(groupStartOffset+this.blockSizeType.size(), (short) numRows);
			break;

		default:
			throw new UnsupportedOperationException("Enum type, "+this.numInGroupType.name()+", not supported for num in group");
		}
	}
}
