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

import com.bwsoft.iris.message.FieldHeader;
import com.bwsoft.iris.message.FieldType;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

class SBEVarLengthFieldHeader implements FieldHeader {
	private final short headerSize;
	private final FieldType lengthType;
	
	SBEVarLengthFieldHeader(FieldType lengthType) {
		this.lengthType = lengthType;
		headerSize = (short) this.lengthType.size();
	}
	
	@Override
	public short getSize() {
		return headerSize;
	}
	
	int getBlockSize(DirectBuffer buffer, int startOffset, ByteOrder order) {
		switch( headerSize ) {
		case 1:
			return buffer.getByte(startOffset);
		case 2:
			return buffer.getShort(startOffset,order);
		default:
			throw new IllegalArgumentException("SBE VAR field header size can only be 1 or 2 (default)");
		}
	}
	
	void putBlockSize(UnsafeBuffer buffer, int startOffset, ByteOrder order, int value) {
		switch( headerSize ) {
		case 1:
			buffer.putByte(startOffset, (byte) value); 
			break;
			
		case 2:
			buffer.putShort(startOffset, (short) value, order);
			break;
			
		default:
			throw new IllegalArgumentException("SBE VAR field header size can only be 1 or 2 (default)");
		}
	}	
}
