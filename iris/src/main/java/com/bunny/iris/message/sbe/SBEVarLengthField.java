package com.bunny.iris.message.sbe;

import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.Group;

public class SBEVarLengthField extends SBEField {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2032780172159012674L;
	private final SBEHeader header;
	
	SBEVarLengthField(Group parent, SBEHeader header) {
		super(parent, FieldType.RAW, (short) 1);
		this.header = header;
	}
	
	public SBEHeader getHeader() {
		return header;
	}
}
