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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.temporal.ValueRange;
import java.util.List;

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.FieldType;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

public class SBEParser {
	
	private final SBEMessage message;
	private final SBEObjectFactory sbeObjFactory;
	private final DirectBuffer buffer;
	private final ByteOrder order;
	
	private int messageHeaderSize;
	private int groupHeaderSize;
	private int varFieldHeaderSize;
	
	public SBEParser(SBEMessage message) {
		this.message = message;
		this.buffer = new UnsafeBuffer(new byte[0]);
		this.order = message.getByteOrder();
		sbeObjFactory = new SBEObjectFactory(buffer, order);
		
		messageHeaderSize = message.getMsgHeader().getHeaderSize();
		groupHeaderSize = message.getGrpHeader().getHeaderSize();
		varFieldHeaderSize = message.getVarLengthFieldHeader().getHeaderSize();
	}
	
	public SBEObjectArray wrapForRead(DirectBuffer buffer, int offset) {
		this.buffer.wrap(buffer);
		return parse(offset);
	}

	public SBEObjectArray wrapForRead(ByteBuffer buffer, int offset) {
		this.buffer.wrap(buffer);
		return parse(offset);
	}
	
	public SBEObjectArray wrapForRead(ByteBuffer buffer, int offset, int length) {
		this.buffer.wrap(buffer,offset,length);
		return parse(0);
	}

	public SBEObjectArray wrapForRead(byte[] buffer, int offset) {
		this.buffer.wrap(buffer);
		return parse(offset);
	}

	public SBEObjectArray wrapForRead(byte[] buffer, int offset, int length) {
		this.buffer.wrap(buffer, offset, length);
		return parse(0);
	}

	private SBEObjectArray parse(int offset) {
		sbeObjFactory.returnAll();

		SBEObjectArray rowObj = sbeObjFactory.get();
		rowObj.setDefinition(message);

		SBEObject rowAttr = rowObj.addObject((short) 0);
		rowAttr.setOffset(offset);
		rowAttr.setValueOffset(offset+messageHeaderSize);
		
		int blockSize = message.getMsgHeader().getBlockSize(this.buffer, offset, order);
		rowAttr.setBlockSize(blockSize);
		message.setBlockSize(blockSize);

		List<Field> fieldList = message.getChildFields();
		int numFixedSizeFields = message.getNumFixedSizeFields();
		int currentOffset = rowAttr.getValueOffset()+rowAttr.getBlockSize();
		for( int k = numFixedSizeFields; k < fieldList.size(); k ++ ) {
			Field subfield = fieldList.get(k);
			if( FieldType.GROUP == subfield.getType() ) {
				currentOffset += wrapGroupRead(currentOffset,(SBEGroup) subfield, rowObj, 0);				
			} else if( FieldType.RAW == subfield.getType() ) {
				currentOffset += wrapVarRead(currentOffset, (SBEVarLengthField) subfield, rowObj, 0);
			}
		}

		int size = (currentOffset - offset - messageHeaderSize);
		rowAttr.setSize(size);

		return rowObj;		
	}
	
	private int wrapGroupRead(int offset, SBEGroup field, SBEObjectArray parent, int parentIndex) {			
		SBEGroupHeader header = (SBEGroupHeader) field.getHeader();
		int numRows = header.getNumRows(buffer, offset, order);
		int blockSize = header.getBlockSize(buffer, offset, order);
		int size = groupHeaderSize; 
		
		if( numRows > 0 ) {
			int currentOffset = offset + size;
			SBEObjectArray rowObj = sbeObjFactory.get();
			rowObj.setDefinition(field);
			parent.addObject((short) parentIndex).addChildObject(field.getID(), rowObj);

			List<Field> fieldList = field.getChildFields();
			int numFixedSizeFields = field.getNumFixedSizeFields();

			for( short i = 0; i < numRows; i ++ ) {	
				int startOffset = currentOffset;
				SBEObject rowAttr = rowObj.addObject(i);
				rowAttr.setOffset(offset);
				rowAttr.setValueOffset(startOffset);
				rowAttr.setBlockSize(blockSize);
				
				currentOffset += blockSize;
				for( int k = numFixedSizeFields; k < fieldList.size(); k ++ ) {
					Field subfield = fieldList.get(k);
					if( FieldType.GROUP == subfield.getType() ) {
						currentOffset += wrapGroupRead(currentOffset,(SBEGroup) subfield, rowObj, i);				
					} else if( FieldType.RAW == subfield.getType() ) {
						currentOffset += wrapVarRead(currentOffset, (SBEVarLengthField) subfield, rowObj, i);
					}
				}

				size += (currentOffset - startOffset);
				rowAttr.setSize(currentOffset - startOffset);
			}
		}
		return size;
	}

	private int wrapVarRead(int offset, SBEVarLengthField field, SBEObjectArray parent, int parentIndex) {
		SBEVarLengthFieldHeader header = (SBEVarLengthFieldHeader) field.getHeader();
		int blockSize = header.getBlockSize(buffer, offset);
		SBEObjectArray sbeObj = sbeObjFactory.get();
		sbeObj.setDefinition(field);
		SBEObject attr = sbeObj.addObject((short) 0);
		attr.setOffset(offset);
		attr.setValueOffset(offset+varFieldHeaderSize);
		attr.setSize(blockSize);
		parent.addObject((short) parentIndex).addChildObject(field.getID(),sbeObj);
		
		return attr.getSize()+varFieldHeaderSize;
	}	
}
