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

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.GroupObject;
import com.bwsoft.iris.message.GroupObjectArray;

import uk.co.real_logic.agrona.DirectBuffer;

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
public class SBEObjectArray implements GroupObjectArray {
	private final static int OPTIMIZED_DIMMENSION = 8;
	private SBEField definition; // Field definition, name, id, etc.

	private final DirectBuffer buffer;
	private final ByteOrder order;
	
	private short dimmension;
	private SBEObject[] attrs;

	SBEObjectArray(DirectBuffer buffer, ByteOrder order) {
		this.buffer = buffer;
		this.order = order;
		this.dimmension = 0;
		this.attrs = new SBEObject[OPTIMIZED_DIMMENSION];
		for( int i = 0; i < OPTIMIZED_DIMMENSION; i ++ ) {
			this.attrs[i] = new SBEObject(this);
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
	
	DirectBuffer getBuffer() {
		return buffer;
	}

	ByteOrder getOrder() {
		return order;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if( dimmension > 1 ) sb.append("[");
		if( dimmension > 0 ) {
			sb.append(attrs[0]);
			for( int i = 1; i < dimmension; i ++ ) {
				sb.append(",").append(attrs[i]);
			}
		} else {
			sb.append("null");
		}
		if( dimmension > 1 ) sb.append("]");
		return sb.toString();
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
}