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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bwsoft.iris.message.Field;
import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.GroupObject;
import com.bwsoft.iris.message.GroupObjectArray;

class SBEObject implements GroupObject {
	private int offset; // relative to the start byte of the message.
	private int valueOffset; // offset + headersize
	private int blockSize; // size to contain the root element
	private int size; // all bytes including header bytes that compose this object.		

	// TODO: can be further optimized to be an array of child objects
	private LinkedHashMap<Short, SBEObjectArray> childFields;
	private final SBEObjectArray array;
	
	private final boolean safeMode;
	
	SBEObject(SBEObjectArray array)  {
		childFields = new LinkedHashMap<>();
		this.array = array;
		this.safeMode = Boolean.valueOf(SBESchemaLoader.properties.getProperty(SBESchemaLoader.SAFE_MODE));
	}
	
	void reset() {
		childFields.clear();
	}

	void addChildObject(short id, SBEObjectArray aChild) {
		if( ! childFields.containsKey(id)) {
			childFields.put(id,aChild);
		} else
			throw new IllegalStateException("A logical error in adding a duplicate child");
	}

	/**
	 * This reacts to the other SBEObject expansion/shrink shift. 
	 * All fields nbyte down to expand array or nbytes up to shrink array
	 * 
	 * @param nbytes
	 */
	void shift(int nbytes) {
		this.offset += nbytes;
		this.valueOffset += nbytes;
		for( SBEObjectArray arr : childFields.values() ) {
			arr.shift(nbytes);
		}
	}
	
	/**
	 * This reacts to the expansion/shrink of a field internally. 
	 * 
	 * @param field the field that is expanded or shrunk.
	 * @param nbytes
	 */
	void shift(Field field, int nbytes) {
		boolean startShift = false;
		for( SBEObjectArray array : childFields.values() ) {
			if( startShift ) {
				array.shift(nbytes);
			} else if( array.getDefinition().getID() == field.getID() ) {
				startShift = true;
			}
		}
		this.size += nbytes;
	}
	
	int getOffset() {
		return this.offset;
	}

	void setOffset(int offset) {
		this.offset = offset;
	}

	int getValueOffset() {
		return valueOffset;
	}

	void setValueOffset(int valueOffset) {
		this.valueOffset = valueOffset;
	}

	int getBlockSize() {
		return blockSize;
	}

	void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	@Override
	public SBEField getField(short fieldId) {
		SBEField field = (SBEField) ((SBEGroup) getDefinition()).getField(fieldId);
		if( null != field ) 
			return field;
		else
			throw new IllegalArgumentException("child field, "+fieldId+", not defined.");
	}

	@Override
	public int getSize() {
		return size;
	}
	
	@Override
	public int getBytes(byte[] dest, int destOffset, int length) {
		length = length > size ? size : length;
		array.getBuffer().getBytes(this.valueOffset, dest, destOffset, length);
		return length;
	}


	void setSize(int size) {
		this.size = size;
	}

	@Override
	public int getSize(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
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
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			return array.getBuffer().getChar(valueOffset+sfield.getRelativeOffset());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public void setChar(Field field, char c) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			array.getBuffer().putChar(valueOffset+sfield.getRelativeOffset(),c);
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public byte getByte(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			return array.getBuffer().getByte(valueOffset+sfield.getRelativeOffset());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public void setByte(Field field, byte value) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			array.getBuffer().putByte(valueOffset+sfield.getRelativeOffset(), value);
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public Number getNumber(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			switch(field.getType()) {
			case BYTE:
			case U8:
				short sv = (short) (0xff & array.getBuffer().getByte(valueOffset+sfield.getRelativeOffset()));
				return sv;

			case I8:
				return (short) array.getBuffer().getByte(valueOffset+sfield.getRelativeOffset());
				
			case I16:
				return (short) array.getBuffer().getShort(valueOffset+sfield.getRelativeOffset(), array.getOrder());
				
			case U16:
				return (0xffff & array.getBuffer().getShort(valueOffset+sfield.getRelativeOffset(), array.getOrder()));

			case I32:
				return array.getBuffer().getInt(valueOffset+sfield.getRelativeOffset(), array.getOrder());

			case U32:
				long lv = array.getBuffer().getInt(valueOffset+sfield.getRelativeOffset(), array.getOrder());
				lv = lv & 0xffffffff;
				return lv;

			case U64:
			case I64:
				return array.getBuffer().getLong(valueOffset+sfield.getRelativeOffset(), array.getOrder());
				
			case FLOAT:
				return array.getBuffer().getFloat(valueOffset+sfield.getRelativeOffset(), array.getOrder());

			case DOUBLE:
				return array.getBuffer().getDouble(valueOffset+sfield.getRelativeOffset(), array.getOrder());
				
			default:
				throw new IllegalArgumentException("type, "+field.getType().name()+", cannot be converted to a number");
			}
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public void setNumber(Field field, Number value) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			switch(field.getType()) {
			case BYTE:
			case U8:
			case I8:
				array.getBuffer().putByte(valueOffset+sfield.getRelativeOffset(), (byte) value.shortValue());
				break;
				
			case I16:
				array.getBuffer().putShort(valueOffset+sfield.getRelativeOffset(), value.shortValue(), array.getOrder());
				break;
				
			case U16:
				array.getBuffer().putShort(valueOffset+sfield.getRelativeOffset(), (short) value.intValue(), array.getOrder());
				break;

			case I32:
				array.getBuffer().putInt(valueOffset+sfield.getRelativeOffset(), value.intValue(), array.getOrder());
				break;
				
			case U32:
				array.getBuffer().putInt(valueOffset+sfield.getRelativeOffset(), (int) value.longValue(), array.getOrder());
				break;

			case U64:
			case I64:
				array.getBuffer().putLong(valueOffset+sfield.getRelativeOffset(), value.longValue(), array.getOrder());
				break;
				
			case FLOAT:
				array.getBuffer().putFloat(valueOffset+sfield.getRelativeOffset(), value.floatValue(), array.getOrder());
				break;
				
			case DOUBLE:
				array.getBuffer().putDouble(valueOffset+sfield.getRelativeOffset(), value.doubleValue(), array.getOrder());
				break;
				
			default:
				throw new IllegalArgumentException("type, "+field.getType().name()+", cannot be converted to a number");
			}
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public int getU16(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			return 0xffff & array.getBuffer().getShort(valueOffset+sfield.getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override 
	public short getI16(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			return array.getBuffer().getShort(valueOffset+sfield.getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}
	
	@Override
	public int getInt(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			return array.getBuffer().getInt(valueOffset+sfield.getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public long getU32(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			long lv = array.getBuffer().getInt(valueOffset+sfield.getRelativeOffset(), array.getOrder());
			lv = lv & 0xffffffff;
			return lv;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public long getLong(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			return array.getBuffer().getLong(valueOffset+sfield.getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public float getFloat(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			return array.getBuffer().getFloat(valueOffset+sfield.getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public double getDouble(Field field) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			return array.getBuffer().getDouble(valueOffset+sfield.getRelativeOffset(), array.getOrder());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getChars(Field field, char[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = (char) array.getBuffer().getByte(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}
	
	@Override
	public int setChars(Field field, char[] src, int srcOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				array.getBuffer().putChar(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), src[i]);
			}
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}


	@Override
	public int getBytes(Field field, byte[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
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
				
			case MESSAGE:
				length = length > getSize() ? getSize() : length;
				array.getBuffer().getBytes(getValueOffset(), dest, destOffset, length);
				return length;								
				
			case CONSTANT:
				length = length > sfield.getConstantValue().getBytes().length ? sfield.getConstantValue().getBytes().length : length;
				System.arraycopy(sfield.getConstantValue().getBytes(), 0, dest, destOffset, length);
				return length;
						
			default:
				length = length > field.length()*sfield.getBlockSize() ? field.length()*sfield.getBlockSize() : length;		
				array.getBuffer().getBytes(valueOffset+sfield.getRelativeOffset(), dest, destOffset, length);
				return length;			
			}
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public int setBytes(Field field, byte[] src, int srcOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			switch(field.getType()) {
			case RAW:
				SBEObjectArray objArray = this.childFields.get(field.getID());
				if( null != objArray ) {
					SBEObject obj = (SBEObject) objArray.getGroupObject(0);
					objArray.adjustRawGroupSize(length);
					
					if( length > 0 ) {
						array.getBuffer().putBytes(obj.getValueOffset(), src, srcOffset, length);
					}
					return length;								
				} else {
					throw new InternalError("internal error: unrecognized raw field");
				}

			case GROUP:
			case MESSAGE:
			case CONSTANT:
				throw new UnsupportedOperationException("cannot set bytes for field type: "+field.getType());
						
			default:
				length = length > field.length()*sfield.getBlockSize() ? field.length()*sfield.getBlockSize() : length;		
				array.getBuffer().putBytes(valueOffset+sfield.getRelativeOffset(), src, srcOffset, length);
				return length;			
			}
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getNumbers(Field field, Number[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			switch(field.getType()) {
			case BYTE:
			case U8:
				for( int i = 0; i < len; i ++ ) {
					short sv = (short) (0xff & array.getBuffer().getByte(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize()));
					dest[destOffset+i] = sv;
				}
				return len;

			case I8:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = (short) array.getBuffer().getByte(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize());
				}
				return len;

			case I16:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getShort(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
				}
				return len;

			case U16:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = 0xffff & array.getBuffer().getShort(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
				}
				return len;
				
			case I32:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getInt(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
				}
				return len;

			case U32:
				for( int i = 0; i < len; i ++ ) {
					long lv = array.getBuffer().getInt(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
					lv = lv & 0xffffffff;
					dest[destOffset+i] = lv;
				}
				return len;

			case U64:
			case I64:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getLong(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
				}
				return len;

			case FLOAT:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getFloat(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
				}
				return len;

			case DOUBLE:
				for( int i = 0; i < len; i ++ ) {
					dest[destOffset+i] = array.getBuffer().getDouble(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
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
	public int setNumbers(Field field, Number[] src, int srcOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			switch(field.getType()) {
			case BYTE:
			case U8:
			case I8:
				for( int i = 0; i < len; i ++ ) {
					array.getBuffer().putByte(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), (byte) src[srcOffset+i].shortValue());
				}
				return len;

			case I16:
				for( int i = 0; i < len; i ++ ) {
					array.getBuffer().putShort(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), src[srcOffset+i].shortValue(), array.getOrder());
				}
				return len;

			case U16:
				for( int i = 0; i < len; i ++ ) {
					array.getBuffer().putShort(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), (short) src[srcOffset+i].intValue(), array.getOrder());
				}
				return len;
				
			case I32:
				for( int i = 0; i < len; i ++ ) {
					array.getBuffer().putInt(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), src[srcOffset+i].intValue(), array.getOrder());
				}
				return len;

			case U32:
				for( int i = 0; i < len; i ++ ) {
					array.getBuffer().putInt(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), (int) src[srcOffset+i].longValue(), array.getOrder());
				}
				return len;

			case U64:
			case I64:
				for( int i = 0; i < len; i ++ ) {
					array.getBuffer().putLong(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), src[srcOffset+i].longValue(), array.getOrder());
				}
				return len;

			case FLOAT:
				for( int i = 0; i < len; i ++ ) {
					array.getBuffer().putFloat(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), src[srcOffset+i].floatValue(), array.getOrder());
				}
				return len;

			case DOUBLE:
				for( int i = 0; i < len; i ++ ) {
					array.getBuffer().putDouble(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), src[srcOffset+i].doubleValue(), array.getOrder());
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
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = (short) (0xff & array.getBuffer().getByte(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize()));
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getI8Array(Field field, short[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getByte(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getU16Array(Field field, int[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = 0xffff & array.getBuffer().getShort(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public int getI16Array(Field field, short[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getShort(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getIntArray(Field field, int[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getInt(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}

	@Override
	public int getU32Array(Field field, long[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				long lv = array.getBuffer().getInt(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
				dest[destOffset+i] = lv & 0xffffffff;
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}

	@Override
	public int getLongArray(Field field, long[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getLong(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}
	}
	
	@Override
	public int getFloatArray(Field field, float[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getFloat(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}		
	}
	
	@Override
	public int getDoubleArray(Field field, double[] dest, int destOffset, int length) {
		SBEField sfield = (SBEField) field;
		if( validateField(sfield) ) {
			int len = length > field.length() ? field.length() : length;
			for( int i = 0; i < len; i ++ ) {
				dest[destOffset+i] = array.getBuffer().getDouble(valueOffset+sfield.getRelativeOffset()+i*sfield.getBlockSize(), array.getOrder());
			}		
			return len;
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", does not belong to this group, "+this.getDefinition().getID());
		}				
	}

	@Override
	public String getEnumName(Field field) {
		SBEField sfield = (SBEField) field;
		if( sfield.isEnumField() ) {
			String value = null;
			try {
				value = getString(field, Charset.defaultCharset().name());
			} catch (UnsupportedEncodingException e) {
				return null;
			}
			if( null != value && ! "".equals(value.trim()) ) {
				value = sfield.getEnumName(value);
				if( null != value ) 
					return value;
				else
					throw new IllegalArgumentException("value in the message is not a valid enum value.");
			} else {
				return null;
			}
		}
		throw new IllegalArgumentException("not a enum field");
	}

	@Override
	public boolean isSet(Field field, String bitName) {
		SBEField sfield = (SBEField) field;
		if( sfield.isChoiceField() ) {
			int bitValue = sfield.getSetBit(bitName);
			int mask = 1 << bitValue;
			int value = getNumber(field).intValue();
			return (mask & value) != 0;
		}
		throw new IllegalArgumentException("not a choice field");
	}

	@Override 
	public String getString(Field field) {
		try {
			return getString(field, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	@Override
	public String getString(Field field, String encodingType) throws UnsupportedEncodingException {
		SBEField sfield = (SBEField) field;
		
		switch( field.getType() ) {
		case CHAR:
		case BYTE:
		case RAW:
			// TODO: SBE schema is missing encoding information.
			int nsize = getSize(field);
			if( nsize == 0 ) 
				return null;
			byte buf[] = new byte[nsize];
			String str = new String(buf,0,this.getBytes(field, buf, 0, buf.length), encodingType);
			return str.trim();
				
		case CONSTANT:
			return sfield.getConstantValue();
			
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
		
		default:
			return "OPAQUE";
		}
	}

	@Override
	public GroupObjectArray getGroupArray(Field field) {
		if( FieldType.GROUP == field.getType() && validateField((SBEField)field) ) {
			return childFields.get(field.getID());
		} else {
			throw new IllegalArgumentException("field, "+field.getID()+", is not a group field or does not belong to this group, "+this.getDefinition().getID());
		}
	}
	
	Map<Short, SBEObjectArray> getGroupList() {
		return childFields;
	}
	
	private boolean validateField(SBEField field) {
		return  ! safeMode ||
				( field.getParent() == this.getDefinition() ||
				  (
					 // handle child elements of a composite field
					 null != field.getParent() &&
				     FieldType.COMPOSITE == field.getParent().getType() && 
				     field.getParent().getParent() == this.getDefinition()
				   )
				 );
	}
}
