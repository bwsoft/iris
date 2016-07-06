package com.bunny.iris.message.sbe;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.aGroup;

public class aSBEVarLengthField extends aSBEField {
	private final aSBEHeader header;
	
	aSBEVarLengthField(aGroup parent, aSBEHeader header) {
		super(parent, FieldType.RAW, (short) 1);
		this.header = header;
	}
	
	public aSBEHeader getHeader() {
		return header;
	}
}
