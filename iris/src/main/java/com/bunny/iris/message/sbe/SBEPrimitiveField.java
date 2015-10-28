package com.bunny.iris.message.sbe;

import com.bunny.iris.message.Message;

public class SBEPrimitiveField extends SBEField {
	private int blockSize;
	
	public SBEPrimitiveField(Message message) {
		super(message);
	}
	
	@Override
	public int getBlockSize() {
		return blockSize == 0 ? getType().size() : blockSize;
	}
	
	@Override
	public SBEField setBlockSize(int size) {
		this.blockSize = size;
		return this;
	}
}
