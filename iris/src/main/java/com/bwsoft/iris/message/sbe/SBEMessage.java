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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.GroupObject;
import com.bwsoft.iris.message.Message;

import uk.co.real_logic.agrona.DirectBuffer;

public class SBEMessage extends SBEGroup implements Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1434540350355065622L;
	
	private final SBEMessageSchemaHeader schema;
	private final SBEMessageHeader msgHeader;
	private final SBEGroupHeader grpHeader;
	private final SBEVarLengthFieldHeader varLengthFieldHeader;

	private final SBEParser parser;

	public SBEMessage(SBEMessageSchemaHeader schema, SBEMessageHeader header, SBEGroupHeader grpHeader, SBEVarLengthFieldHeader vHeader) {
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
	
	public SBEObject warpForRead(DirectBuffer buffer, int offset) {
		return (SBEObject) this.parser.wrapForRead(buffer, offset).getGroupObject(0);
	}

	@Override
	public GroupObject wrapForRead(ByteBuffer buffer, int offset) {
		return (SBEObject) this.parser.wrapForRead(buffer, offset).getGroupObject(0);
	}

	@Override
	public GroupObject wrapForRead(ByteBuffer buffer, int offset, int length) {
		return (SBEObject) this.parser.wrapForRead(buffer, offset, length).getGroupObject(0);
	}
	
	@Override
	public GroupObject wrapForRead(byte[] buffer, int offset) {
		return (SBEObject) this.parser.wrapForRead(buffer, offset).getGroupObject(0);
	}

	@Override
	public GroupObject wrapForRead(byte[] buffer, int offset, int length) {
		return (SBEObject) this.parser.wrapForRead(buffer, offset, length).getGroupObject(0);
	}
}
