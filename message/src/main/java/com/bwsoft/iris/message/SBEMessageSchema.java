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
package com.bwsoft.iris.message;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import com.bwsoft.iris.message.sbe.SBEGroupHeader;
import com.bwsoft.iris.message.sbe.SBEMessage;
import com.bwsoft.iris.message.sbe.SBEMessageHeader;
import com.bwsoft.iris.message.sbe.SBEMessageSchemaHeader;
import com.bwsoft.iris.message.sbe.SBESchemaLoader;
import com.bwsoft.iris.message.sbe.SBEVarLengthFieldHeader;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

/**
 * A SBE message schema contains a collection of SBE messages that are defined within a 
 * SBE XML file. It is capable to parse, modify, or create messages within this schema scope.
 *
 * Use the following to create a schema from a XML schema file. The schemaFilename
 * can point toward a file in the filesystem or a file in the classpath. It is typically
 * done during the program initialization or in a static block.
 * 
 * <pre>
 * {@code
 *     // configure the schema usage. Turn off the safeMode for production use. 
 *     SBEMessageSchema.configSafeMode(false);
 *     
 *     // create SBEMessageSchema
 *     SBEMessageSchema schema = SBEMessageSchema.createSchema(schemaFilename);
 *     
 *     // obtain the definitions of all messages from the hashmap 
 *     HashMap<Integer, SBEMessage> messages = schema.getMsgLookup();
 *     
 *     // do something with the message definition such as obtaining field definitions for 
 *     // those fields that are being used repeatedly.
 *     ...
 * }
 * </pre>
 * 
 * Now use one of the wrap calls to parse or modify an existing SBE message buffer or one 
 * of the create calls to create a SBE message.
 * 
 * @author yzhou
 *
 */
/**
 * @author yzhou
 *
 */
public class SBEMessageSchema {
	private final SBEMessageSchemaHeader schemaHeader;
	private final SBEMessageHeader msgHeader;
	private final SBEGroupHeader grpHeader;
	private final SBEVarLengthFieldHeader varHeader;
	private final ByteOrder order;
	private final int schemaId;
	private final int version;
	private final DirectBuffer buffer;

	// contains the map between the template id and the message definition
	private final HashMap<Integer, SBEMessage> lookupTable;

	/**
	 * Create the message schema based upon the SBE XML.
	 * 
	 * @param xmlDefinition
	 * @return
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 */
	public static SBEMessageSchema createSBESchema(String xmlDefinition) throws FileNotFoundException, JAXBException {
		return SBESchemaLoader.loadSchema(xmlDefinition);
	}
	
	/**
	 * If safeMode is true, there are additional checks against the runtime error 
	 * caused by erroneous logic implementation. 
	 * 
	 * @param safeMode
	 */
	public static void configSafeMode(boolean safeMode) {
		if (safeMode) 
			SBESchemaLoader.safeModeOn();
		else
			SBESchemaLoader.safeModeOff();
	}
	
	/**
	 * The constructor is provided for internal use. Use {@link #createSBESchema(String)} instead.
	 * 
	 * @param schemaHeader
	 * @param msgHeader
	 * @param grpHeader
	 * @param varHeader
	 * @param lookupTable
	 */
	public SBEMessageSchema(SBEMessageSchemaHeader schemaHeader,
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
		this.version = this.schemaHeader.getVersion();
		this.lookupTable = lookupTable;
		this.buffer = new UnsafeBuffer(new byte[0]);
	}

	/**
	 * Create a GroupObject based upon a buffer. All SBE fields in the 
	 * buffer are accessible via the GroupObject.
	 * 
	 * The current SBE message size is the sum of the GroupObject size
	 * and the SBE message header size. 
	 * 
	 * @param buffer containing a complete SBE message. 
	 * @param offset the starting position of the message.
	 * @return GroupObject to access SBE fields
	 */
	public GroupObject wrapSbeBuffer(ByteBuffer buffer, int offset) {
		this.buffer.wrap(buffer);
		SBEMessage msg = this.wrapSbeBuffer(this.buffer, offset);
		if( null != msg ) {
			return msg.wrapSbeBuffer(buffer, offset);
		}
		return null;
	}
	
	/**
	 * Create a GroupObject based upon a buffer. All SBE fields in the 
	 * buffer are accessible via the GroupObject.
	 * 
	 * The current SBE message size is the sum of the GroupObject size
	 * and the SBE message header size. 
	 * 
	 * @param buffer containing a complete SBE message. 
	 * @param offset the starting position of the message.
	 * @return GroupObject to access SBE fields
	 */
	public GroupObject wrapSbeBuffer(byte[] buffer, int offset) {
		this.buffer.wrap(buffer);
		SBEMessage msg = this.wrapSbeBuffer(this.buffer, offset);
		if( null != msg ) {
			return msg.wrapSbeBuffer(buffer, offset);
		}
		return null;
	}

	/**
	 * Create a GroupObject based upon a buffer. All SBE fields in the 
	 * buffer are accessible via the GroupObject.
	 * 
	 * The current SBE message size is the sum of the GroupObject size
	 * and the SBE message header size. 
	 * 
	 * @param buffer containing a complete SBE message. 
	 * @param offset the starting position of the message.
	 * @param length it is no less than the size of the whole SBE message including the message header. 
	 * @return GroupObject to access SBE fields
	 */
	public GroupObject wrapSbeBuffer(ByteBuffer buffer, int offset, int length) {
		this.buffer.wrap(buffer);
		SBEMessage msg = this.wrapSbeBuffer(this.buffer, offset);
		if( null != msg ) {
			return msg.wrapSbeBuffer(buffer, offset, length);
		}
		return null;
	}
	
	/**
	 * Create a GroupObject based upon a buffer. All SBE fields in the 
	 * buffer are accessible via the GroupObject.
	 * 
	 * The current SBE message size is the sum of the GroupObject size
	 * and the SBE message header size. 
	 * 
	 * @param buffer containing a complete SBE message. 
	 * @param offset the starting position of the message.
	 * @param length it is no less than the size of the whole SBE message including the message header. 
	 * @return GroupObject to access SBE fields
	 */
	public GroupObject wrapSbeBuffer(byte[] buffer, int offset, int length) {
		this.buffer.wrap(buffer);
		SBEMessage msg = this.wrapSbeBuffer(this.buffer, offset);
		if( null != msg ) {
			return msg.wrapSbeBuffer(buffer, offset, length);
		}
		return null;
	}

	public GroupObject createSbeBuffer(int templateId, ByteBuffer buffer, int offset) {
		SBEMessage message = this.lookupTable.get(templateId);
		if( null != message ) {
			return message.createSbeBuffer(buffer, offset);
		} else {
			return null;
		}
	}
	
	private SBEMessage wrapSbeBuffer(DirectBuffer buffer, int offset) {
		int schemaId = this.msgHeader.getSchemaId(buffer, offset, order);
		int templateId = this.msgHeader.getTemplateId(buffer, offset, order);
		if( schemaId == this.schemaId ) {
			return this.lookupTable.get(templateId);
		} else {
			return null;
		}
	}
	
	/**
	 * Obtain a hash map contains all SBEMessage definitions found in the 
	 * current schema. The message template ID is the key.
	 * 
	 * @return
	 */
	public HashMap<Integer, SBEMessage> getMsgLookup() {
		return this.lookupTable;
	}
}
