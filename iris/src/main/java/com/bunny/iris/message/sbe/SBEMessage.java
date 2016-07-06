package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

import com.bunny.iris.message.FieldType;

import uk.co.real_logic.agrona.DirectBuffer;

public class SBEMessage extends SBEGroup {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1434540350355065622L;
	
	private final SBEMessageSchema schema;
	private final SBEMessageHeader msgHeader;
	private final SBEGroupHeader grpHeader;
	private final SBEVarLengthFieldHeader varLengthFieldHeader;

	private final SBEParser parser;

	public SBEMessage(SBEMessageSchema schema, SBEMessageHeader header, SBEGroupHeader grpHeader, SBEVarLengthFieldHeader vHeader) {
		super(null, header, FieldType.MESSAGE);
		
		this.schema = schema;
		this.msgHeader = header;
		this.grpHeader = grpHeader;
		this.varLengthFieldHeader = vHeader;
		this.parser = new SBEParser(this);
	}

	public SBEMessageHeader getMsgHeader() {
		return msgHeader;
	}

	public SBEGroupHeader getGrpHeader() {
		return grpHeader;
	}

	public SBEVarLengthFieldHeader getVarLengthFieldHeader() {
		return varLengthFieldHeader;
	}

	public ByteOrder getByteOrder() {
		return schema.getOrder();
	}
	
	public SBEObject parse(DirectBuffer buffer, int offset) {
		return (SBEObject) this.parser.parse(buffer, offset).getGroupObject(0);
	}
}
