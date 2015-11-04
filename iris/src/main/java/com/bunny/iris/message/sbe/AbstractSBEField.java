package com.bunny.iris.message.sbe;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;

abstract class AbstractSBEField implements Field {
	private SBEMessage message;	
	private AbstractSBEField parent;

	private short id;
	private String name;
	private FieldType type;
	
	private short headerSize;
	private int blockSize;	
	private short repeat = 1;
	
	public AbstractSBEField(SBEMessage message, short dimmension) {
		this.message = message;
		this.headerSize = 0;
		this.repeat = dimmension;
	}
	
	public AbstractSBEField(SBEMessage message) {
		this(message, (short) 1);
	}
	
	void reset() {		
	}
	
	short getHeaderSize() {
		return headerSize;
	}

	AbstractSBEField setHeaderSize(short headerSize) {
		this.headerSize = headerSize;
		return this;
	}

	int getBlockSize() {
		return blockSize;
	}
	
	AbstractSBEField setBlockSize(int blockLength) {
		this.blockSize = blockLength;
		return this;
	}
	
	@Override
	public short getDimension() {
		return this.repeat;
	}
	
	@Override
	public short getID() {
		return id;
	}

	@Override
	public AbstractSBEField setID(short id) {
		this.id = id;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public AbstractSBEField setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public FieldType getType() {
		return type;
	}

	@Override
	public AbstractSBEField setType(FieldType type) {
		this.type = type;
		if( blockSize == 0 ) blockSize = type.size();
		return this;
	}

	@Override
	public AbstractSBEField getParent() {
		return parent;
	}

	@Override
	public AbstractSBEField setParent(Field parent) {
		this.parent = (AbstractSBEField) parent;
		return this;
	}
	
	@Override
	public SBEMessage getMessage() {
		return message;
	}	
}
