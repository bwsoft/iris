/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package com.bwsoft.iris.message.sbe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.GroupObject;
import com.bwsoft.iris.message.Message;

import uk.co.real_logic.agrona.DirectBuffer;

/**
 * The representation of a SBE message. It contains the definition of a SBE message. 
 * Use {@link com.bwsoft.iris.util.MessageUtil#toJsonString(com.bwsoft.iris.message.Group)}
 * to obtain the Json representation of the message definition. It provides wrap calls
 * to wrap the existing SBE message buffer for read and modification. It provides create 
 * calls to create a SBE message. Typically do not make direct calls to wraps or creates in
 * this class. Use the corresponding calls in the {@link com.bwsoft.iris.message.SBEMessageSchema}
 * instead. 
 * 
 * @author yzhou
 *
 */
public class SBEMessage extends SBEGroup implements Message {

	private static final long serialVersionUID = -1434540350355065622L;
	
	private final SBEMessageSchemaHeader schema;
	private final SBEMessageHeader msgHeader;
	private final SBEGroupHeader grpHeader;
	private final SBEVarLengthFieldHeader varLengthFieldHeader;
	
	private final boolean safeMode;

	private final ThreadLocal<SBEParser> parser = new ThreadLocal<SBEParser>() {
		@Override
		protected SBEParser initialValue() {
			return new SBEParser(SBEMessage.this);
		}
	};
	
	SBEMessage(SBEMessageSchemaHeader schema, SBEMessageHeader header, SBEGroupHeader grpHeader, SBEVarLengthFieldHeader vHeader) {
		super(null, header, FieldType.MESSAGE);
		
		this.schema = schema;
		this.msgHeader = header;
		this.grpHeader = grpHeader;
		this.varLengthFieldHeader = vHeader;
		this.safeMode = Boolean.valueOf(SBESchemaLoader.properties.getProperty(SBESchemaLoader.SAFE_MODE));
	}

	SBEParser getParser() {
		return parser.get();
	}
	
	SBEObject getRootObject() {
		return parser.get().getRootObject();
	}
	
	SBEMessageSchemaHeader getMsgSchemaHeader() {
		return schema;
	}
	
	SBEGroupHeader getGrpHeader() {
		return grpHeader;
	}

	SBEVarLengthFieldHeader getVarLengthFieldHeader() {
		return varLengthFieldHeader;
	}

	ByteOrder getByteOrder() {
		return schema.getOrder();
	}
	
	boolean safeMode() {
		return this.safeMode;
	}
	
	SBEObject warpSbeBuffer(DirectBuffer buffer, int offset) {
		return (SBEObject) this.parser.get().wrapSbeBuffer(buffer, offset).getGroupObject(0);
	}

	@Override
	public GroupObject wrapSbeBuffer(ByteBuffer buffer, int offset) {
		return (SBEObject) this.parser.get().wrapSbeBuffer(buffer, offset).getGroupObject(0);
	}

	@Override
	public GroupObject wrapSbeBuffer(ByteBuffer buffer, int offset, int length) {
		return (SBEObject) this.parser.get().wrapSbeBuffer(buffer, offset, length).getGroupObject(0);
	}
	
	@Override
	public GroupObject wrapSbeBuffer(byte[] buffer, int offset) {
		return (SBEObject) this.parser.get().wrapSbeBuffer(buffer, offset).getGroupObject(0);
	}

	@Override
	public GroupObject wrapSbeBuffer(byte[] buffer, int offset, int length) {
		return (SBEObject) this.parser.get().wrapSbeBuffer(buffer, offset, length).getGroupObject(0);
	}
	
	@Override
	public GroupObject createSbeBuffer(ByteBuffer buffer, int offset) {
		return this.parser.get().createSbeBuffer(buffer, offset).getGroupObject(0);
	}
	
	@Override
	public GroupObject createSbeBuffer(byte[] buffer, int offset) {
		return this.parser.get().createSbeBuffer(buffer, offset).getGroupObject(0);
	}
}
