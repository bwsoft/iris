package com.bwsoft.iris.message.sbe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import com.bwsoft.iris.message.GroupObject;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

/**
 * A schema is defined by a SBE XML file. It contains a set of messages
 * and its associated attributes. It is able to parse a byte array to 
 * obtain a GroupObject.
 * 
 * The schema is always created by the SBESchemaLoader. 
 * 
 * @author yzhou
 *
 */
public class SBESchema {
	private final SBEMessageSchema schemaHeader;
	private final SBEMessageHeader msgHeader;
	private final SBEGroupHeader grpHeader;
	private final SBEVarLengthFieldHeader varHeader;
	private final ByteOrder order;
	private final int schemaId;
	private final DirectBuffer buffer;

	// contains the map between the template id and the message definition
	private final HashMap<Integer, SBEMessage> lookupTable;

	SBESchema(SBEMessageSchema schemaHeader,
			SBEMessageHeader msgHeader,
			SBEGroupHeader grpHeader,
			SBEVarLengthFieldHeader varHeader,
			HashMap<Integer, SBEMessage> lookupTable) {
		this.schemaHeader = schemaHeader;
		this.msgHeader = msgHeader;
		this.grpHeader = grpHeader;
		this.varHeader = varHeader;
		this.order = this.schemaHeader.getOrder();
		this.schemaId = this.schemaHeader.getId();
		this.lookupTable = lookupTable;
		this.buffer = new UnsafeBuffer(new byte[0]);
	}

	public GroupObject wrapForRead(ByteBuffer buffer, int offset) {
		this.buffer.wrap(buffer);
		SBEMessage msg = this.wrapForRead(this.buffer, offset);
		if( null != msg ) {
			return msg.wrapForRead(buffer, offset);
		}
		return null;
	}
	
	public GroupObject wrapForRead(byte[] buffer, int offset) {
		this.buffer.wrap(buffer);
		SBEMessage msg = this.wrapForRead(this.buffer, offset);
		if( null != msg ) {
			return msg.wrapForRead(buffer, offset);
		}
		return null;
	}

	public GroupObject wrapForRead(ByteBuffer buffer, int offset, int length) {
		this.buffer.wrap(buffer);
		SBEMessage msg = this.wrapForRead(this.buffer, offset);
		if( null != msg ) {
			return msg.wrapForRead(buffer, offset, length);
		}
		return null;
	}
	
	public GroupObject wrapForRead(byte[] buffer, int offset, int length) {
		this.buffer.wrap(buffer);
		SBEMessage msg = this.wrapForRead(this.buffer, offset);
		if( null != msg ) {
			return msg.wrapForRead(buffer, offset, length);
		}
		return null;
	}

	private SBEMessage wrapForRead(DirectBuffer buffer, int offset) {
		int schemaId = this.msgHeader.getSchemaId(buffer, offset, order);
		int templateId = this.msgHeader.getTemplateId(buffer, offset, order);
		if( schemaId == this.schemaId ) {
			return this.lookupTable.get(templateId);
		} else {
			return null;
		}
	}
	
	public HashMap<Integer, SBEMessage> getMsgLookup() {
		return this.lookupTable;
	}
}
