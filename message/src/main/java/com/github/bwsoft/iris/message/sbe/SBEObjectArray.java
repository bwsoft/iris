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

import java.nio.ByteOrder;

import org.agrona.concurrent.UnsafeBuffer;

import com.github.bwsoft.iris.message.Field;
import com.github.bwsoft.iris.message.GroupObject;
import com.github.bwsoft.iris.message.GroupObjectArray;

/**
 * A SBEObject is a representation of a SBE message field on the byte array. It contains all
 * bytes that compose this field. In case of a SBE group field, it represents all bytes of all
 * rows of the group. In case of a SBE message, it represents all bytes that form the message.
 * 
 * Typically a SBEObject is created for a composite field such as a SBE group, a variable 
 * length field, a SBE message. It is seldom created for a simple field since a simple field
 * SBEObject can be created by its parent SBEObject based upon the definition of the 
 * composite field. 
 * 
 * @author yzhou
 *
 */
class SBEObjectArray implements GroupObjectArray {
	private final int OPTIMIZED_DIMMENSION;
	private SBEField definition; // Field definition, name, id, etc.

	private final UnsafeBuffer buffer;
	private final ByteOrder order;
	
	private short dimmension;
	private SBEObject[] attrs;
	
	// attributes required for mutable update
	private SBEObjectArray parent;
	private short parentRow;
	private int offset;
	
	SBEObjectArray(UnsafeBuffer buffer, ByteOrder order) {
		OPTIMIZED_DIMMENSION = Integer.parseInt(SBESchemaLoader.properties.getProperty(SBESchemaLoader.OPTIMIZED_NUM_OF_GROUP_ROWS));

		this.buffer = buffer;
		this.order = order;
		this.dimmension = 0;
		this.attrs = new SBEObject[OPTIMIZED_DIMMENSION];
		for( int i = 0; i < OPTIMIZED_DIMMENSION; i ++ ) {
			this.attrs[i] = new SBEObject(this);
		}
	}

	int getOffset() {
		return this.offset;
	}

	void setOffset(int offset) {
		this.offset = offset;
	}

	void setParent(SBEObjectArray parent) {
		this.parent = parent;
	}
	
	void setParentRow(short row) {
		this.parentRow = row;
	}
	
	/**
	 * Shift nbytes down to expand array or up to shrink the array.
	 * 
	 * @param nbytes can be positive for downward shift or negative for an upward shift
	 */
	void shift(int nbytes) {
		this.offset += nbytes;
		for( int i = 0; i < dimmension; i ++ ) {
			attrs[i].shift(nbytes);
		}
	}
	
	/**
	 * Shift all fields by nbytes starting from the field of the nth row of this object.
	 * 
	 * @param nth the nth row of this object array.
	 * @param field the starting field of this row.
	 * @param nbytes number of bytes to be shifted.
	 */
	void shift(short nth, Field field, int nbytes) {
		// shift the remaining bytes in the same row down
		attrs[nth].shift(field, nbytes);
		
		// all other rows need to be shifted
		for( short i = (short) (nth+1); i < dimmension; i ++ ) {
			attrs[i].shift(nbytes);
		}
		
		// inform parent to shift
		if( null != parent ) {
			parent.shift(parentRow,this.definition,nbytes);
		}
	}
	
	/**
	 * Reset this SBEObject for reuse 
	 */
	void reset() {
		for( int i = 0; i < dimmension; i ++ )
			attrs[i].reset();
		this.dimmension = 0;
	}

	void setDefinition(Field definition) {
		this.definition = (SBEField) definition;
	}
	
	SBEObject addObject(short index) {
		if( index < attrs.length ) {
			if( index >= dimmension ) dimmension = (short) (index + 1);
			return attrs[index];
		} else {
			while( attrs.length <= index ) {
				SBEObject[] eAttrs = new SBEObject[attrs.length+OPTIMIZED_DIMMENSION];
				System.arraycopy(attrs, 0, eAttrs, 0, attrs.length);
				for( int i = attrs.length; i < eAttrs.length; i ++ ) {
					eAttrs[i] = new SBEObject(this);
				}
				attrs = eAttrs;
			}
			return addObject(index);
		}
	}
	
	UnsafeBuffer getBuffer() {
		return buffer;
	}

	ByteOrder getOrder() {
		return order;
	}

	@Override
	public SBEField getDefinition() {
		return definition;
	}

	@Override
	public GroupObject getGroupObject(int index) {
		if( index < dimmension ) 
			return attrs[index];
		else 
			throw new ArrayIndexOutOfBoundsException();		
	}

	@Override
	public short getNumOfGroups() {
		return dimmension;
	}
	
	@Override
	public GroupObject addGroupObject() {
		SBEGroup grp = (SBEGroup) definition;
		
		// get size of an empty row
		int nsize = grp.getBlockSize() 
				+ grp.getNumGroupFields()*grp.getHeader().getSize() 
				+ grp.getNumRawFields()*grp.getMessage().getVarLengthFieldHeader().getSize();
		
		// add attr
		int blockSize = 0;
		int valueOffset = 0;
		if( dimmension > 0 ) {
			SBEObject lastObj = attrs[dimmension-1];
			valueOffset = lastObj.getValueOffset()+lastObj.getSize();
			blockSize = lastObj.getBlockSize();
		} else {
			// TODO: exception out if version is not the same
			valueOffset = this.offset + grp.getHeader().getSize();
			blockSize = grp.getBlockSize();
			((SBEGroupHeader) grp.getHeader()).putBlockSize(buffer, offset, order, blockSize);
		}
		SBEObject newObj = this.addObject(dimmension);
		newObj.setOffset(this.offset);
		newObj.setValueOffset(valueOffset);
		newObj.setBlockSize(blockSize);
		newObj.setSize(nsize);

		// update header to record both block size and the number of rows
		((SBEGroupHeader) grp.getHeader()).putNumRows(buffer, offset, order, dimmension);
		
		// shift the array
		shiftArray(newObj.getValueOffset(), nsize);
		
		if( nsize - blockSize > 0 ) {
			// fill array with zero for the section of groups and raws	
			fillArray(this.buffer, newObj.getValueOffset()+blockSize, nsize-blockSize, (byte) 0);
			
			// wrap new array
			SBEParser parser = this.definition.getMessage().getParser();
			parser.wrapGroupObject(newObj, grp, this, dimmension-1);
		}
		
		// notify the parent about the shift of nsize
		if( null != this.parent ) {
			this.parent.shift(parentRow, this.definition, nsize);
		}
		
		return newObj;
	}
	
	@Override
	public void deleteGroupObject(int n) {
		if( n < dimmension ) {
			SBEObject toBeDeleted = attrs[n];
			int nsize = toBeDeleted.getSize();
						
			// update header to record both block size and the number of rows
			SBEGroup grp = (SBEGroup) definition;
			((SBEGroupHeader) grp.getHeader()).putNumRows(buffer, offset, order, dimmension-1);
			
			// shift the array
			shiftArray(toBeDeleted.getValueOffset()+nsize, -nsize);
			
			// shift remaining attrs up
			for( int i = n+1; i < dimmension; i ++ ) {
				attrs[i].shift(-nsize);
				attrs[i-1] = attrs[i]; 
			}
			
			attrs[dimmension-1] = toBeDeleted;
			toBeDeleted.reset();
			dimmension --;
			
			// notify the parent about the shift of nsize
			if( null != this.parent ) {
				this.parent.shift(parentRow, this.definition, -nsize);
			}			
		}
	}
	
	GroupObject adjustRawGroupSize(int newSize) {
		SBEVarLengthField field = (SBEVarLengthField) this.definition;
		SBEObject raw = this.attrs[0];

		// update header to record both block size
		int originalSize = raw.getSize();
		int nsize = newSize - originalSize;
		if( nsize == 0 ) 
			return raw;
		
		((SBEVarLengthFieldHeader) field.getHeader()).putBlockSize(buffer, offset, order, newSize);
		
		// shift the array
		shiftArray(raw.getValueOffset()+raw.getSize(), nsize);
		
		raw.setBlockSize(newSize);
		raw.setSize(newSize);
		
		// notify the parent about the shift of nsize
		if( null != this.parent ) {
			this.parent.shift(parentRow, this.definition, nsize);
		}
		
		return raw;
	}
	
	private void shiftArray(int offset, int nsize) {
		byte[] array = this.buffer.byteArray();
		int length = this.definition.getMessage().getRootObject().getSize() + this.definition.getMessage().getHeader().getSize() - offset;		
		if( null != array ) {
			System.arraycopy(array, offset, array, offset+nsize, length);
		} else {
			array = new byte[length];
			this.buffer.getBytes(offset, array, 0, length);
			this.buffer.putBytes(offset+nsize, array, 0, length);
		}
	}
	
	static void fillArray(UnsafeBuffer buffer, int offset, int nsize, byte value) {
		byte[] array = buffer.byteArray();
		if( null != array ) {
			array[offset] = value;
		    for( int i = 1; i < nsize; i += i ) {
		    	System.arraycopy(array, offset, array, i+offset, ((nsize - i) < i) ? (nsize - i) : i);
		    }
		} else {
			for( int i = 0; i < nsize; i ++ ) {
				buffer.putByte(offset+i, value);
			}
		}		
	}
}
