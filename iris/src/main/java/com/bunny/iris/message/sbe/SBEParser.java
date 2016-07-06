package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;
import java.util.List;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.aField;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

public class SBEParser {
	
	private final aSBEMessage message;
	private final SBEObjectFactory sbeObjFactory;
	private final UnsafeBuffer buffer;
	private final ByteOrder order;
	
	private int messageHeaderSize;
	private int groupHeaderSize;
	private int varFieldHeaderSize;
	
	private long resetTime;
	private long wrapTime;
	
	public SBEParser(aSBEMessage message) {
		this.message = message;
		this.buffer = new UnsafeBuffer(new byte[0]);
		this.order = message.getByteOrder();
		sbeObjFactory = new SBEObjectFactory(buffer, order);
		
		messageHeaderSize = message.getMsgHeader().getHeaderSize();
		groupHeaderSize = message.getGrpHeader().getHeaderSize();
		varFieldHeaderSize = message.getVarLengthFieldHeader().getHeaderSize();
	}
	
	public SBEObjectArray parse(DirectBuffer buffer, int offset) {
//		long startTime = System.nanoTime();
		sbeObjFactory.returnAll();
//		long endTime = System.nanoTime();
//		resetTime += (endTime - startTime);
		this.buffer.wrap(buffer);

		SBEObjectArray rowObj = sbeObjFactory.get();
		rowObj.setDefinition(message);

		SBEObject rowAttr = rowObj.addObject((short) 0);
		rowAttr.setOffset(offset);
		rowAttr.setValueOffset(offset+messageHeaderSize);
		
		int blockSize = message.getMsgHeader().getBlockSize(this.buffer, offset, order);
		rowAttr.setBlockSize(blockSize);
		message.setBlockSize(blockSize);

		List<aField> fieldList = message.getChildFields();
		int numFixedSizeFields = message.getNumFixedSizeFields();
		int currentOffset = rowAttr.getValueOffset()+rowAttr.getBlockSize();
		for( int k = numFixedSizeFields; k < fieldList.size(); k ++ ) {
			aField subfield = fieldList.get(k);
			if( FieldType.GROUP == subfield.getType() ) {
				currentOffset += wrapGroupRead(currentOffset,(aSBEGroup) subfield, rowObj, 0);				
			} else if( FieldType.RAW == subfield.getType() ) {
				currentOffset += wrapVarRead(currentOffset, (aSBEVarLengthField) subfield, rowObj, 0);
			}
		}

		int size = (currentOffset - offset - messageHeaderSize);
		rowAttr.setSize(size);

		return rowObj;		
	}
	
	public void startStat() {
		resetTime = 0;
		wrapTime = 0;
	}
	
	public void reportTime() {
		System.out.println("total reset time: "+resetTime);
		System.out.println("Total wrap time: "+wrapTime);
	}
	
	private int wrapGroupRead(int offset, aSBEGroup field, SBEObjectArray parent, int parentIndex) {			
		SBEGroupHeader header = (SBEGroupHeader) field.getHeader();
		int numRows = header.getNumRows(buffer, offset, order);
		int blockSize = header.getBlockSize(buffer, offset, order);
		int size = groupHeaderSize; 
		
		if( numRows > 0 ) {
			int currentOffset = offset + size;
			SBEObjectArray rowObj = sbeObjFactory.get();
			rowObj.setDefinition(field);
			parent.addObject((short) parentIndex).addChildObject(field.getID(), rowObj);

			List<aField> fieldList = field.getChildFields();
			int numFixedSizeFields = field.getNumFixedSizeFields();

			for( short i = 0; i < numRows; i ++ ) {	
				int startOffset = currentOffset;
				SBEObject rowAttr = rowObj.addObject(i);
				rowAttr.setOffset(offset);
				rowAttr.setValueOffset(startOffset);
				rowAttr.setBlockSize(blockSize);
				
				currentOffset += blockSize;
				for( int k = numFixedSizeFields; k < fieldList.size(); k ++ ) {
					aField subfield = fieldList.get(k);
					if( FieldType.GROUP == subfield.getType() ) {
						currentOffset += wrapGroupRead(currentOffset,(aSBEGroup) subfield, rowObj, i);				
					} else if( FieldType.RAW == subfield.getType() ) {
						currentOffset += wrapVarRead(currentOffset, (aSBEVarLengthField) subfield, rowObj, i);
					}
				}

				size += (currentOffset - startOffset);
				rowAttr.setSize(currentOffset - startOffset);
			}
		}
		return size;
	}

	private int wrapVarRead(int offset, aSBEVarLengthField field, SBEObjectArray parent, int parentIndex) {
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
