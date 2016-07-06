package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;
import java.util.List;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.Field;

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
	
	public SBEObjectArray parse(DirectBuffer buffer, int offset) {
		sbeObjFactory.returnAll();
		this.buffer.wrap(buffer);

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
		SBEObjectArray sbeObj = sbeObjFactory.get();
		sbeObj.setDefinition(field);
		SBEObject attr = sbeObj.addObject((short) 0);
		attr.setOffset(offset);
		attr.setValueOffset(offset+varFieldHeaderSize);
		attr.setSize(field.getBlockSize());
		parent.addObject((short) parentIndex).addChildObject(field.getID(),sbeObj);
		
		return attr.getSize()+varFieldHeaderSize;
	}	
}
