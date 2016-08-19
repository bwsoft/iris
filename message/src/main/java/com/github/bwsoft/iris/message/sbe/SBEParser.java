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
import java.nio.ByteOrder;
import java.util.List;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import com.github.bwsoft.iris.message.Field;
import com.github.bwsoft.iris.message.FieldType;

class SBEParser {
	
	private final SBEMessage message;
	private final SBEObjectFactory sbeObjFactory;
	private final UnsafeBuffer buffer;
	private final ByteOrder order;
	
	private int messageHeaderSize;
	private int groupHeaderSize;
	private int varFieldHeaderSize;
	
	SBEParser(SBEMessage message) {
		this.message = message;
		this.buffer = new UnsafeBuffer(new byte[0]);
		this.order = message.getByteOrder();
		sbeObjFactory = new SBEObjectFactory(buffer, order);
		
		messageHeaderSize = message.getHeader().getSize();
		groupHeaderSize = message.getGrpHeader().getSize();
		varFieldHeaderSize = message.getVarLengthFieldHeader().getSize();
	}
	
	SBEObjectArray wrapSbeBuffer(DirectBuffer buffer, int offset) {
		this.buffer.wrap(buffer);
		return parse(offset);
	}

	SBEObjectArray wrapSbeBuffer(ByteBuffer buffer, int offset) {
		this.buffer.wrap(buffer);
		return parse(offset);
	}
	
	SBEObjectArray wrapSbeBuffer(ByteBuffer buffer, int offset, int length) {
		this.buffer.wrap(buffer,offset,length);
		return parse(0);
	}

	SBEObjectArray wrapSbeBuffer(byte[] buffer, int offset) {
		this.buffer.wrap(buffer);
		return parse(offset);
	}

	SBEObjectArray wrapSbeBuffer(byte[] buffer, int offset, int length) {
		this.buffer.wrap(buffer, offset, length);
		return parse(0);
	}

	SBEObjectArray createSbeBuffer(ByteBuffer buffer, int offset) {
		this.buffer.wrap(buffer);
		
		// create message header
		writeMessageHeader(offset);
		
		// null out array in its necessary positions
		int nsize = this.message.getNumGroupFields()*this.message.getGrpHeader().getSize() 
				+ this.message.getNumRawFields()*this.message.getVarLengthFieldHeader().getSize();
		int startOffset = this.message.getBlockSize() + offset;
		SBEObjectArray.fillArray(this.buffer, startOffset, nsize, (byte) 0);
		
		return parse(offset);
	}
	
	SBEObjectArray createSbeBuffer(byte[] buffer, int offset) {
		this.buffer.wrap(buffer);
		
		// create message header
		writeMessageHeader(offset);
		
		// null out array in its necessary positions
		int nsize = this.message.getNumGroupFields()*this.message.getGrpHeader().getSize() 
				+ this.message.getNumRawFields()*this.message.getVarLengthFieldHeader().getSize();
		int startOffset = this.message.getBlockSize() + offset;
		SBEObjectArray.fillArray(this.buffer, startOffset, nsize, (byte) 0);
		
		return parse(offset);
	}

	private void writeMessageHeader(int offset) {
		SBEMessageHeader header = (SBEMessageHeader) this.message.getHeader();
		SBEMessageSchemaHeader schemaHeader = this.message.getMsgSchemaHeader();
		
		header.putSchemaId(this.buffer, offset, order, schemaHeader.getId());
		header.putVersion(this.buffer, offset, order, schemaHeader.getVersion());
		header.putTemplateId(this.buffer, offset, order, this.message.getID());
		header.putBlockSize(this.buffer, offset, order, message.getBlockSize());
	}
	
	private SBEObjectArray parse(int offset) {
		sbeObjFactory.returnAll();

		SBEObjectArray rowObj = sbeObjFactory.get();
		rowObj.setDefinition(message);
		rowObj.setOffset(offset);
		rowObj.setParent(null);
		rowObj.setParentRow((short) 0);

		SBEObject rowAttr = rowObj.addObject((short) 0);
		rowAttr.setOffset(offset);
		rowAttr.setValueOffset(offset+messageHeaderSize);
		
		int blockSize = ((SBEMessageHeader) message.getHeader()).getBlockSize(this.buffer, offset, order);
		rowAttr.setBlockSize(blockSize);
		message.setBlockSize(blockSize);

		List<Field> fieldList = message.getFields();
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

		SBEObjectArray rowObj = sbeObjFactory.get();
		rowObj.setDefinition(field);
		rowObj.setOffset(offset);
		rowObj.setParent(parent);
		rowObj.setParentRow((short) parentIndex);
		parent.addObject((short) parentIndex).addChildObject(field.getID(), rowObj);

		if( numRows > 0 ) {
			int currentOffset = offset + size;

			List<Field> fieldList = field.getFields();
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
		int blockSize = header.getBlockSize(buffer, offset, order);
		SBEObjectArray sbeObj = sbeObjFactory.get();
		sbeObj.setDefinition(field);
		sbeObj.setOffset(offset);
		sbeObj.setParent(parent);
		sbeObj.setParentRow((short) parentIndex);
		SBEObject attr = sbeObj.addObject((short) 0);
		attr.setOffset(offset);
		attr.setValueOffset(offset+varFieldHeaderSize);
		attr.setSize(blockSize);
		attr.setBlockSize(blockSize);
		parent.addObject((short) parentIndex).addChildObject(field.getID(),sbeObj);
		
		return attr.getSize()+varFieldHeaderSize;
	}	

	void wrapGroupObject(SBEObject rowAttr, SBEGroup field, SBEObjectArray parent, int parentIndex) {	
		int currentOffset = field.getBlockSize() + rowAttr.getValueOffset();
		List<Field> fieldList = field.getFields();
		for( int k = field.getNumFixedSizeFields(); k < fieldList.size(); k ++ ) {
			Field subfield = fieldList.get(k);
			if( FieldType.GROUP == subfield.getType() ) {
				SBEObjectArray rowObj = sbeObjFactory.get();
				rowObj.setDefinition(subfield);
				rowObj.setOffset(currentOffset);
				rowObj.setParent(parent);
				rowObj.setParentRow((short) parentIndex);
				rowAttr.addChildObject(subfield.getID(), rowObj);

				currentOffset += ((SBEGroup) subfield).getHeader().getSize();				
			} else if( FieldType.RAW == subfield.getType() ) {
				SBEObjectArray sbeObj = sbeObjFactory.get();
				sbeObj.setDefinition(subfield);
				sbeObj.setOffset(currentOffset);
				sbeObj.setParent(parent);
				sbeObj.setParentRow((short) parentIndex);
				SBEObject attr = sbeObj.addObject((short) 0);
				attr.setOffset(currentOffset);
				attr.setValueOffset(currentOffset+varFieldHeaderSize);
				attr.setSize(0);
				attr.setBlockSize(0);
				rowAttr.addChildObject(subfield.getID(), sbeObj);

				currentOffset += varFieldHeaderSize;
			}
		}
	}
	
	SBEObject getRootObject() {
		return (SBEObject) sbeObjFactory.getRoot().getGroupObject(0);
	}
}