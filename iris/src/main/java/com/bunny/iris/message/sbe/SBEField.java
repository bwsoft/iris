package com.bunny.iris.message.sbe;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;
import com.bunny.iris.message.Message;

public class SBEField implements Field {
	private Message message;
	
	private SBEField parent;

	private short id;
	private String name;
	private FieldType type;
	
	private int blockSize = 0;
	
	private short repeat = 1;	

	private short totalOccurrence;
	private short[] occurrence;
	
	public SBEField(Message message) {
		this.message = message;
		totalOccurrence = 0;
		occurrence = new short[128];
	}

	protected void reset() {
		totalOccurrence = 0;
	}
	
	public Message getMessage() {
		return message;
	}
	
	@Override
	public short getID() {
		return id;
	}

	@Override
	public SBEField setID(short id) {
		this.id = id;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SBEField setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public FieldType getType() {
		return type;
	}

	@Override
	public SBEField setType(FieldType type) {
		this.type = type;
		return this;
	}

	protected int getBlockSize() {
		return this.blockSize;
	}

	protected SBEField setBlockSize(int blockLength) {
		this.blockSize = blockLength;
		return this;
	}

	@Override
	public short getArraySize() {
		return this.repeat;
	}

	@Override
	public SBEField setArraySize(short repeats) {
		this.repeat = repeats;
		return this;
	}

	@Override
	public SBEField getChild(short id) {
		throw new UnsupportedOperationException("not supported for field type: "+type.name());
	}

	@Override
	public SBEField addChild(FieldType type) {
		throw new UnsupportedOperationException("not supported for field type: "+type.name());
	}
	
	@Override
	public SBEField getParent() {
		return parent;
	}

	@Override
	public SBEField setParent(Field parent) {
		this.parent = (SBEField) parent;
		return this;
	}
	
	@Override
	public FieldValue allocateOccurrence(boolean isNode) {
		FieldValue value = message.allocate(isNode);
		value.setField(this);
		value.setSize(getBlockSize());
		if( FieldType.GROUP == this.getType() ) {
			if( ! isNode ) {
				// row container is being tracked in the children of the groupNode.
				return value;
			}
		}
		
		this.occurrence[this.totalOccurrence] = value.getNodeId();
		totalOccurrence ++;
		return value;
	}
	
	@Override
	public FieldValue getOccurrence(short index) {
		return message.getValueNode(occurrence[index]);
	}
	
	@Override
	public short getTotalOccurrence() {
		return totalOccurrence;
	}
}
