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
import com.github.bwsoft.iris.message.sbe.fixsbe.EncodedDataType;

class SBEVarLengthFieldHeader implements FieldHeader {
	private final short headerSize;
	private final FieldType lengthType;
	
	/**
	 * Get var length field header.
	 * 
	 * @param cache SBESchemaCache loaded from the xml.
	 * @return
	 */
	static SBEVarLengthFieldHeader getDefaultVarLengthFieldHeader(SBESchemaFieldTypes cache, String varTypeName) {
		SBEVarLengthFieldHeader varHeader = null;
		if( cache.getCompositeDataTypes().containsKey(varTypeName) ) {
			List<SBECompositeTypeElement> eTypes = cache.getCompositeDataTypes().get(varTypeName);
			FieldType lengthType = FieldType.U8;
			for( SBECompositeTypeElement rawType : eTypes ) {
				if( ! (rawType.getType() instanceof EncodedDataType) ) {
					throw new IllegalArgumentException("Unsupported SBE type in variable length field header definition");
				}
				EncodedDataType type = (EncodedDataType) rawType.getType();
				if( "length".equals(type.getName()) )
					lengthType = FieldType.getType(type.getPrimitiveType());
			}
			if( null == lengthType ) {
				throw new IllegalArgumentException("unrecgnized primitive type in var length field header definition");					
			}
			varHeader = new SBEVarLengthFieldHeader(lengthType);
		} else {
			throw new IllegalArgumentException("The type for the variable length field, "+varTypeName+" is unrecognized");
		}
		return varHeader;
	}

	SBEVarLengthFieldHeader(FieldType lengthType) {
		this.lengthType = lengthType;
		headerSize = (short) this.lengthType.size();
	}
	
	@Override
	public short getSize() {
		return headerSize;
	}
	
	int getBlockSize(ByteBuffer buffer, int startOffset) {
		switch( headerSize ) {
		case 1:
			return buffer.get(startOffset);
		case 2:
			return buffer.getShort(startOffset);
		case 4:
			return buffer.getInt(startOffset);
		default:
			throw new IllegalArgumentException("SBE VAR field header size can only be 1, 2, or 4 (default)");
		}
	}
	
	void putBlockSize(ByteBuffer buffer, int startOffset, int value) {
		switch( headerSize ) {
		case 1:
			buffer.put(startOffset, (byte) value); 
			break;
			
		case 2:
			buffer.putShort(startOffset, (short) value);
			break;
		
		case 4:
			buffer.putInt(startOffset, value);
			break;
			
		default:
			throw new IllegalArgumentException("SBE VAR field header size can only be 1, 2, or 4 (default)");
		}
	}	
}
