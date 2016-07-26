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

import java.util.LinkedHashMap;
import java.util.Map;

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.GroupObject;
import com.bwsoft.iris.message.GroupObjectArray;

public class SBEObject implements GroupObject {
	private int offset; // relative to the start byte of the message.
	private int valueOffset; // offset + headersize
	private int blockSize; // size to contain the root element
	private int size; // all bytes including header bytes that compose this object.		

	// TODO: can be further optimized to be an array of child objects
	private LinkedHashMap<Short, SBEObjectArray> childFields;
	private final SBEObjectArray array;
	
	SBEObject(SBEObjectArray array)  {
		childFields = new LinkedHashMap<>();
		this.array = array;
	}
	
	void reset() {
		childFields.clear();
	}

	void addChildObject(short id, SBEObjectArray aChild) {
		if( ! childFields.containsKey(id)) 
			childFields.put(id,aChild);
		else
			throw new IllegalStateException("A logical error in adding a duplicate child");
	}

	public int getOffset() {
		return this.offset;
	}

	void setOffset(int offset) {
		this.offset = offset;
	}

	public int getValueOffset() {
		return valueOffset;
	}

	void setValueOffset(int valueOffset) {
		this.valueOffset = valueOffset;
	}

	public int getBlockSize() {
		return blockSize;
	}

	void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	@Override
	public SBEField getField(short fieldId) {
		SBEField field = (SBEField) ((SBEGroup) getDefinition()).getChildField(fieldId);
		if( null != field ) 
			return field;
		else
			throw new IllegalArgumentException("child field, "+fieldId+", not defined.");
	}

	@Override
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public int getSize(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			switch( field.getType() ) {
			case RAW: 
				GroupObjectArray array  = childFields.get(field.getID());
				if( null == array ) 
					return 0;
				else
					return array.getGroupObject(0).getSize();

			case GROUP:
				array  = childFields.get(field.getID());
				if( null == array ) 
					return 0;
				else {
					int size = 0;
					for( short i = 0; i < array.getNumOfGroups(); i ++ )
						size += array.getGroupObject(i).getSize();
					return size;
				}
				
			default:
				SBEField sfield = (SBEField) field;
				return sfield.getBlockSize()*sfield.length();
			}
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public SBEField getDefinition() {
		return array.getDefinition();
	}

	@Override
	public char getChar(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			return array.getBuffer().getChar(valueOffset+((SBEField) field).getRelativeOffset());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public byte getByte(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			return array.getBuffer().getByte(valueOffset+((SBEField) field).getRelativeOffset());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public Number getNumber(Field field) {
		int iv = 0;
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			switch(field.getType()) {
			case BYTE:
			case U8:
				short sv = (short) (0xff & array.getBuffer().getByte(valueOffset+((SBEField) field).getRelativeOffset()));
				return sv;

			case I8:
				return (short) array.getBuffer().getByte(valueOffset+((SBEField) field).getRelativeOffset());

			case I16:
				return (short) array.getBuffer().getShort(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());

			case U16:
				return (0xffff & array.getBuffer().getShort(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder()));

			case I32:
				return array.getBuffer().getInt(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());

			case U32:
				long lv = array.getBuffer().getInt(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());
				lv = lv & 0xffffffff;
				return lv;

			case U64:
			case I64:
				return array.getBuffer().getLong(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());

			case FLOAT:
				return array.getBuffer().getFloat(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());

			case DOUBLE:
				return array.getBuffer().getDouble(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());
				
			default:
				throw new IllegalArgumentException("type, "+field.getType().name()+", cannot be converted to a number");
			}
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public int getU16(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			return 0xffff & array.getBuffer().getShort(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override 
	public short getI16(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			return array.getBuffer().getShort(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}
	
	@Override
	public int getInt(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			return array.getBuffer().getInt(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public long getU32(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			long lv = array.getBuffer().getInt(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());
			lv = lv & 0xffffffff;
			return lv;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public long getLong(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			return array.getBuffer().getLong(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public float getFloat(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			return array.getBuffer().getFloat(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public double getDouble(Field field) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			return array.getBuffer().getDouble(valueOffset+((SBEField) field).getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getChars(Field field, char[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getChar(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public int getBytes(Field field, byte[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			switch(field.getType()) {
			case RAW:
				SBEObjectArray objArray = this.childFields.get(field.getID());
				if( null != objArray ) {
					SBEObject obj = (SBEObject) objArray.getGroupObject(0);
					length = length > obj.getSize() ? obj.getSize() : length;
					array.getBuffer().getBytes(obj.getValueOffset(), dest, destOffset, length);
					return length;								
				} else {
					return 0;
				}

			case GROUP:
				objArray = this.childFields.get(field.getID());
				if( null != objArray ) {
					int len = 0;
					for( short i = 0; i < objArray.getNumOfGroups(); i ++ ) {
						len += objArray.getGroupObject(i).getSize();
					}
					SBEObject obj = (SBEObject) objArray.getGroupObject(0);
					length = length > len ? len : length;
					array.getBuffer().getBytes(obj.getValueOffset(), dest, destOffset, length);
					return length;								
				} else {
					return 0;
				}
				
			default:
				length = length > field.length()*((SBEField) field).getBlockSize() ? field.length()*((SBEField) field).getBlockSize() : length;		
				array.getBuffer().getBytes(valueOffset+((SBEField) field).getRelativeOffset(), dest, destOffset, length);
				return length;			
			}
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public int getNumbers(Field field, Number[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			switch(field.getType()) {
			case BYTE:
			case U8:
				for( int i = 0; i < len; i ++ ) {
					short sv = (short) (0xff & array.getBuffer().getByte(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize()));
					dest[destOffset+i] = sv;
				}
				return len;

			case I8:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = (short) array.getBuffer().getByte(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize());
				}
				return len;

			case I16:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getShort(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
				}
				return len;

			case U16:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = 0xffff & array.getBuffer().getShort(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
				}
				return len;
				
			case I32:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getInt(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
				}
				return len;

			case U32:
				for( int i = 0; i < len; i ++ ) {
					long lv = array.getBuffer().getInt(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
					lv = lv & 0xffffffff;
					dest[destOffset+i] = lv;
				}
				return len;

			case U64:
			case I64:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getLong(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
				}
				return len;

			case FLOAT:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getFloat(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
				}
				return len;

			case DOUBLE:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getDouble(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
				}
				return len;
				
			default:
				throw new IllegalArgumentException("type, "+field.getType().name()+", cannot be converted to a number");
			}
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
		
	}

	@Override
	public int getU8Array(Field field, short[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = (short) (0xff & array.getBuffer().getByte(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize()));
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getI8Array(Field field, short[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getByte(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getU16Array(Field field, int[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = 0xffff & array.getBuffer().getShort(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public int getI16Array(Field field, short[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getShort(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getIntArray(Field field, int[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getInt(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public int getU32Array(Field field, long[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				long lv = array.getBuffer().getInt(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
				dest[destOffset+i] = lv & 0xffffffff;
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getLongArray(Field field, long[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getLong(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}
	
	@Override
	public int getFloatArray(Field field, float[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getFloat(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public int getDoubleArray(Field field, double[] dest, int destOffset, int length) {
		if( null != field.getParent() && field.getParent().getID() == this.getDefinition().getID() ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getDouble(valueOffset+((SBEField) field).getRelativeOffset()+i*((SBEField) field).getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}				
	}

	@Override
	public String getEnumName(Field field) {
		if( ((SBEField) field).isEnumField() ) {
			String value = getString(field);
			value = ((SBEField) field).getEnumName(value);
			if( null != value ) 
				return value;
			else
				throw new IllegalArgumentException("value in the message is not a valid enum value.");
		}
		throw new IllegalArgumentException("not a enum field");
	}

	@Override
	public boolean isSet(Field field, String bitName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getString(Field field) {
		switch( field.getType() ) {
		case CHAR:
		case BYTE:
			int len = field.length();
			byte[] buffer = new byte[len];
			array.getBuffer().getBytes(valueOffset+((SBEField) field).getRelativeOffset(), buffer, 0, len);
			return new String(buffer);
			
		case I8:
		case U8:
		case I16:
		case U16:
		case I32:
		case U32:
		case I64:
		case U64:
			if( field.length() == 1 ) {
				Number value = this.getNumber(field);
				return String.valueOf(value.longValue());
			} else {
				Number[] value = new Number[field.length()];
				this.getNumbers(field, value, 0, field.length());
				StringBuilder sb = new StringBuilder();
				sb.append("[").append(value[0].longValue());
				for( int i = 1; i < value.length; i ++ ) {
					sb.append(",").append(value[i].longValue());
				}
				sb.append("]");
				return sb.toString();
			}
			
		case FLOAT:
			if( field.length() == 1 ) {
				Number value = this.getNumber(field);
				return String.valueOf(value.floatValue());
			} else {
				Number[] value = new Number[field.length()];
				this.getNumbers(field, value, 0, field.length());
				StringBuilder sb = new StringBuilder();
				sb.append("[").append(value[0].floatValue());
				for( int i = 1; i < value.length; i ++ ) {
					sb.append(",").append(value[i].floatValue());
				}
				sb.append("]");
				return sb.toString();
			}
			
		case DOUBLE:
			if( field.length() == 1 ) {
				Number value = this.getNumber(field);
				return String.valueOf(value.doubleValue());
			} else {
				Number[] value = new Number[field.length()];
				this.getNumbers(field, value, 0, field.length());
				StringBuilder sb = new StringBuilder();
				sb.append("[").append(value[0].doubleValue());
				for( int i = 1; i < value.length; i ++ ) {
					sb.append(",").append(value[i].doubleValue());
				}
				sb.append("]");
				return sb.toString();
			}
		
		case RAW:
			// TODO: SBE schema is missing encoding information.
			byte buf[] = new byte[getSize(field)];
			return new String(buf,0,this.getBytes(field, buf, 0, buf.length));
			
		default:
			return "{\"opaque\":\""+field.getName()+"\"}";
		}
	}

	@Override
	public GroupObjectArray getGroupArray(Field field) {
		if( null != field.getParent() && FieldType.GROUP == field.getType() && field.getParent().getID() == this.getDefinition().getID() ) {
			return childFields.get(field.getID());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", is not a group field or does not belong to this group, "+this.getDefinition().getID());
		}
	}
	
	public Map<Short, SBEObjectArray> getGroupList() {
		return childFields;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		SBEGroup group = (SBEGroup) this.getDefinition();
		boolean addComma = false;
		for( Field field : group.getChildFields() ) {
			if( addComma ) 
				sb.append(",");
			else
				addComma = true;
			sb.append("\"").append(field.getName()).append("\"").append(":");
			switch( field.getType() ) {
			case I8:
			case U8:
			case I16:
			case U16:
			case I32:
			case U32:
			case I64:
			case U64:
			case FLOAT:
			case DOUBLE:
				String value = getString(field);
				if( ((SBEField) field).isEnumField() ) {
					value = ((SBEField) field).getEnumName(value);
				}
				sb.append(value);
				break;
				
			case CHAR:
				if( ((SBEField) field).isEnumField() ) {
					sb.append(((SBEField) field).getEnumName(getString(field)));
					break;
				} // if not handle the same way as BYTE
			case BYTE:
				sb.append("\"").append(getString(field)).append("\"");
				break;

			case RAW:
				sb.append("\"").append(getString(field)).append("\"");
				break;
				
			case GROUP:
				sb.append(getGroupArray(field));
				break;
				
			default:
				sb.append("\"opaque field\"");
				break;
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
