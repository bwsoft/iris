package com.bunny.iris.message.sbe;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NameNotFoundException;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.Field;
import com.bunny.iris.message.Group;
import com.bunny.iris.message.GroupObjectArray;
import com.bunny.iris.message.GroupObject;

public class SBEObject implements GroupObject {
	private int offset; // relative to the start byte of the message.
	private int valueOffset; // offset + headersize
	private int blockSize; // size to contain the root element
	private int size; // all bytes including header bytes that compose this object.		

	// TODO: can be further optimized to be an array of child objects
	private LinkedHashMap<Short, SBEObjectArray> childFields;
	private final SBEObjectArray array;
	
	public SBEObject(SBEObjectArray array)  {
		childFields = new LinkedHashMap<>();
		this.array = array;
	}
	
	public void reset() {
		childFields.clear();
	}

	public void addChildObject(short id, SBEObjectArray aChild) {
		if( ! childFields.containsKey(id)) 
			childFields.put(id,aChild);
		else
			throw new IllegalStateException("A logical error in adding a duplicate child");
	}

	public int getOffset() {
		return this.offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getValueOffset() {
		return valueOffset;
	}

	public void setValueOffset(int valueOffset) {
		this.valueOffset = valueOffset;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	private SBEField getField(short fieldId) {
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
	public SBEField getDefinition() {
		return array.getDefinition();
	}

	@Override
	public char getChar(short fieldId) {
		SBEField field = getField(fieldId);
		return array.getBuffer().getChar(valueOffset+field.getRelativeOffset());
	}

	@Override
	public byte getByte(short fieldId) {
		SBEField field = getField(fieldId);
		return array.getBuffer().getByte(valueOffset+field.getRelativeOffset());
	}

	@Override
	public <T extends Number> T getNumber(short fieldId, Class<T> type) {
		SBEField field = getField(fieldId);
		switch(field.getType()) {
		case BYTE:
		case U8:
			short sv = (short) (0xff & array.getBuffer().getByte(valueOffset)+field.getRelativeOffset());
			return type.cast(sv);
			
		case I8:
			return type.cast((short) array.getBuffer().getByte(valueOffset)+field.getRelativeOffset());
			
		case I16:
			return type.cast((short) array.getBuffer().getShort(valueOffset+field.getRelativeOffset(), array.getOrder()));
			
		case U16:
			return type.cast(0xffff & array.getBuffer().getShort(valueOffset+field.getRelativeOffset(), array.getOrder()));
			
		case I32:
			return type.cast(array.getBuffer().getInt(valueOffset+field.getRelativeOffset(), array.getOrder()));
			
		case U32:
			long lv = array.getBuffer().getInt(valueOffset+field.getRelativeOffset(), array.getOrder());
			lv = lv & 0xffffffff;
			return type.cast(lv);
			
		case U64:
		case I64:
			return type.cast(array.getBuffer().getLong(valueOffset+field.getRelativeOffset(), array.getOrder()));
			
		default:
			throw new IllegalArgumentException("type, "+field.getType().name()+", cannot be converted to a number");
		}
	}

	@Override
	public int getU16(short fieldId) {
		SBEField field = getField(fieldId);
		return array.getBuffer().getShort(valueOffset+field.getRelativeOffset(), array.getOrder());
	}

	@Override
	public int getInt(short fieldId) {
		SBEField field = getField(fieldId);
		return array.getBuffer().getInt(valueOffset+field.getRelativeOffset(), array.getOrder());
	}

	@Override
	public long getLong(short fieldId) {
		SBEField field = getField(fieldId);
		return array.getBuffer().getLong(valueOffset+field.getRelativeOffset(), array.getOrder());
	}

	@Override
	public int getChars(short fieldId, char[] dest, int destOffset, int length) {
		SBEField field = getField(fieldId);
		int len = length > field.length() ? field.length() : length;
		for( int i = 0; i < len; i ++ ) {
			dest[destOffset+i] = array.getBuffer().getChar(valueOffset+field.getRelativeOffset()+i*field.getBlockSize());
		}		
		return len;
	}

	@Override
	public int getBytes(short fieldId, byte[] dest, int destOffset, int length) {
		if( fieldId == getDefinition().getID() ) {
			length = length > getSize() ? getSize() : length;		
			array.getBuffer().getBytes(offset, dest, destOffset, length);
			return length;
		} else  {
			SBEField field = getField(fieldId);
			length = length > field.length()*field.getBlockSize() ? field.length()*field.getBlockSize() : length;		
			array.getBuffer().getBytes(offset, dest, destOffset, length);
			return length;			
		}
	}

	@Override
	public int getU16Array(short fieldId, int[] dest, int destOffset, int length) {
		SBEField field = getField(fieldId);
		int len = length > field.length() ? field.length() : length;
		for( int i = 0; i < len; i ++ ) {
			dest[destOffset+i] = array.getBuffer().getShort(valueOffset+field.getRelativeOffset()+i*field.getBlockSize(), array.getOrder());
		}		
		return len;
	}

	@Override
	public int getIntArray(short fieldId, int[] dest, int destOffset, int length) {
		SBEField field = getField(fieldId);
		int len = length > field.length() ? field.length() : length;
		for( int i = 0; i < len; i ++ ) {
			dest[destOffset+i] = array.getBuffer().getInt(valueOffset+field.getRelativeOffset()+i*field.getBlockSize(), array.getOrder());
		}		
		return len;
	}

	@Override
	public int getLongArray(short fieldId, long[] dest, int destOffset, int length) {
		SBEField field = getField(fieldId);
		int len = length > field.length() ? field.length() : length;
		for( int i = 0; i < len; i ++ ) {
			dest[destOffset+i] = array.getBuffer().getLong(valueOffset+field.getRelativeOffset()+i*field.getBlockSize(), array.getOrder());
		}		
		return len;
	}

	@Override
	public String getEnumName(short fieldId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSet(short fieldId, String bitName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getString(short fieldId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GroupObjectArray getGroupArray(short fieldId) {
		return childFields.get(fieldId);
	}
	
	public Map<Short, SBEObjectArray> getGroupList() {
		return childFields;
	}
}
