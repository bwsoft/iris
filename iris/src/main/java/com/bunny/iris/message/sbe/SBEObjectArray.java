package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;
import java.util.List;

import com.bunny.iris.message.aField;
import com.bunny.iris.message.aGroup;
import com.bunny.iris.message.aGroupArray;
import com.bunny.iris.message.aGroupObject;

import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

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
public class SBEObjectArray implements aGroupArray {
	private final static int OPTIMIZED_DIMMENSION = 8;
	private aSBEField definition; // Field definition, name, id, etc.

	private final UnsafeBuffer buffer;
	private final ByteOrder order;
	
	private short dimmension;
	private SBEObject[] attrs;

	public SBEObjectArray(UnsafeBuffer buffer, ByteOrder order) {
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

	public void setDefinition(aField definition) {
		this.definition = (aSBEField) definition;
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
	
	UnsafeBuffer getBuffer() {
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
			if( definition instanceof aGroup ) {
				List<aField> childFields = ((aGroup) definition).getChildFields();
				for( aField childField : childFields ) {
					if( childField instanceof aSBEGroup || childField instanceof aSBEVarLengthField ) 
						break;
					sb.append(",");
					if( childField.getDimension() > 1 ) sb.append("[");
					boolean addComma2 = false;
					for( int j = 0; j < childField.getDimension(); j++ ) {
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
	public aSBEField getDefinition() {
		return definition;
	}

	@Override
	public aGroupObject getGroupObject(int index) {
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
