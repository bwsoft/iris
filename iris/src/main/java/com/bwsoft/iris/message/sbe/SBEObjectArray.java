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
import java.util.List;

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.Group;
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

	public SBEObjectArray(DirectBuffer buffer, ByteOrder order) {
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
	public void reset() {
		for( int i = 0; i < dimmension; i ++ )
			attrs[i].reset();
		this.dimmension = 0;
	}

	public void setDefinition(Field definition) {
		this.definition = (SBEField) definition;
	}
	
	public SBEObject addObject(short index) {
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

//	public SBEObjectArray getGroup(int rowId, int id) {
//		return attrs[rowId].getSubField(id);
//	}
//	
//	public short getFieldU16(int rowId, int id, int index) {
//		switch( definition.getType() ) {
//		case GROUP:
//			for( aField field : ((aSBEGroup) definition).getChildField() ) {
//				if( id == field.getID() ) {
//					int voff = this.attrs[rowId].valueOffset + ((aSBEField) field).getRelativeOffset() - ((aSBEGroup) this.definition).getHeader().getHeaderSize();
//					return this.buffer.getShort(voff+index*definition.getBlockSize(), this.order);
//				}
//			}
//			break;
//		default:
//			throw new IllegalArgumentException("cannot get a subfield from a non-group object");
//		}
//		return -1;
//	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if( dimmension > 1 ) sb.append("[");
		boolean addComma = false;
		for( int i = 0; i < dimmension; i ++ ) {
			if( addComma ) sb.append(",");
			else	addComma = true;
			sb.append("{name:").append(this.definition.getName()).append(",");
			sb.append("id:").append(this.definition.getID());
			if( definition instanceof Group ) {
				List<Field> childFields = ((Group) definition).getChildFields();
				for( Field childField : childFields ) {
					if( childField instanceof SBEGroup || childField instanceof SBEVarLengthField ) 
						break;
					sb.append(",");
					if( childField.length() > 1 ) sb.append("[");
					boolean addComma2 = false;
					for( int j = 0; j < childField.length(); j++ ) {
						if( addComma2 ) sb.append(",");
						else addComma2 = true;
						sb.append("{");
						sb.append("name:").append(childField.getName());
						sb.append(",id:").append(childField.getID());
						sb.append(",type:").append(childField.getType().name());
						sb.append("}");
					}
				}
			}
			for( SBEObjectArray child : this.attrs[i].getGroupList().values() ) {
				sb.append(",");
				sb.append(child.toString());
			}
			sb.append("}");
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
