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
package com.github.bwsoft.iris.message;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import com.github.bwsoft.iris.message.sbe.SBEMessage;
import com.github.bwsoft.iris.message.sbe.SBEMessageHeader;
import com.github.bwsoft.iris.message.sbe.SBEMessageSchemaHeader;
import com.github.bwsoft.iris.message.sbe.SBESchemaLoader;


/**
 * A factory class to create SBE messages within a scope of a message schema.
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
 *     // use one of the wrap calls to parse a buffer 
 *     SBEMessage aMessage = schema.wrapSbeBuffer(...);
 *     
 *     // use one of the create  calls to create a sbe message
 *     SBEMessage aNewMessage = schema.createSbeBuffer(templateId, ...);
 *     
 *     // one can also examine all message definitions in the schema
 *     HashMap<Integer, SBEMessage> definitions = schema.getMsgLookup()
 *     ...
 * }
 * </pre>
 * 
 * @author yzhou
 *
 */
public class SBEMessageSchema {
	private final SBEMessageSchemaHeader schemaHeader;
	private final SBEMessageHeader msgHeader;
	private final FieldHeader grpHeader;
	private final FieldHeader varHeader;
	private final ByteOrder order;
	private final int schemaId;
	private final int version;

	// contains the map between the template id and the message definition
	private final HashMap<Integer, SBEMessage> lookupTable;

	/**
	 * Create the message schema based upon the SBE XML.
	 * 
	 * @param xmlDefinition the SBE xml template filename. It can be in a classpath or filesystem
	 * @return a SBEMessageSchema to parse or create SBE messages.
	 * @throws JAXBException if there is an error in parsing the SBE xml schema file
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws FactoryConfigurationError 
	 * @throws XMLStreamException 
	 */
	public static SBEMessageSchema createSBESchema(String xmlDefinition) throws JAXBException, SAXException, ParserConfigurationException, IOException, XMLStreamException, FactoryConfigurationError {
		return SBESchemaLoader.loadSchema(xmlDefinition);
	}
	
	/**
	 * If safeMode is true, there are additional checks against the runtime error 
	 * caused by erroneous logic implementation. 
	 * 
	 * This value can also be turned on/off by specifying -DsafeMode=ture/false
	 * 
	 * @param safeMode false to turn off the safe mode for better performance
	 */
	public static void configSafeMode(boolean safeMode) {
		if (safeMode) 
			SBESchemaLoader.safeModeOn();
		else
			SBESchemaLoader.safeModeOff();
	}
	
	/**
	 * Set an optimized value for number of groups. It is a balance between the memory usage
	 * and occasionally performance impact. There is a performance punishment to increase
	 * the internal memory when the number of groups in a message exceeds
	 * this value.
	 *  
	 * This value can also be set by specifying -DoptimizedNumOfGroups=128 (default)
	 * 
	 * @param number optimized value
	 */
	public static void setOptimizedNumOfGroups(int number) {
		SBESchemaLoader.setOptimizedNumOfGroups(number);
	}
	
	/**
	 * Set an optimized value for number of group rows. It is a balance between the memory usage
	 * and occasionally performance impact. There is a performance punishment to increase
	 * the internal memory when the number of group rows for a group in a message exceeds
	 * this value.
	 *  
	 * This value can also be set by specifying -DoptimizedNumOfGroupRows=8 (default)
	 * 
	 * @param number optimized value
	 */
	public static void setOptimizedNumOfGroupRows(int number) {
		SBESchemaLoader.setOptimizedNumOfGroupRows(number);
	}
	
	/**
	 * The constructor is provided for internal use. Use {@link #createSBESchema(String)} instead.
	 * 
	 * @param schemaHeader the schema header
	 * @param msgHeader the message header
	 * @param grpHeader the group header
	 * @param varHeader the variable length field header
	 * @param lookupTable a lookup table contains all messages defined in this schema
	 */
	public SBEMessageSchema(SBEMessageSchemaHeader schemaHeader,
			SBEMessageHeader msgHeader,
			FieldHeader grpHeader,
			FieldHeader varHeader,
			HashMap<Integer, SBEMessage> lookupTable) {
		this.schemaHeader = schemaHeader;
		this.msgHeader = msgHeader;
		this.grpHeader = grpHeader;
		this.varHeader = varHeader;
		this.order = this.schemaHeader.getOrder();
		this.schemaId = this.schemaHeader.getId();
		this.version = this.schemaHeader.getVersion();
		this.lookupTable = lookupTable;
	}

	/**
	 * Create a GroupObject based upon a buffer. All SBE fields in the 
	 * buffer are accessible via the GroupObject.
	 * 
	 * The current SBE message size is the sum of the GroupObject size
	 * and the SBE message header size. 
	 * 
	 * The limit of the ByteBuffer is untouched. It is users' responsibility to ensure an adequate 
	 * setting of limit in case the message is needed to be altered to a different size 
	 * before invoking the call. It is also users' responsibility to re-adjust the limit 
	 * after the call is returned if needed. The size of the message can be obtained by 
	 * the {@link GroupObject#getSize()} call with an addition of the message header size. 
	 * 
	 * The position of the ByteBuffer is altered by the routine as needed. 
	 * 
	 * @param buffer containing a complete SBE message. 
	 * @param offset the starting position of the message.
	 * @return GroupObject to access SBE fields
	 */
	public GroupObject wrapSbeBuffer(ByteBuffer buffer, int offset) {
		SBEMessage msg = this.getSBEMessage(buffer, offset);
		if( null != msg ) {
			return msg.wrapSbeBuffer(buffer, offset);
		}
		return null;
	}
	
	/**
	 * Create a SBE message using the provided buffer. 
	 * 
	 * The limit of the ByteBuffer is untouched. It is users' responsibility to ensure an adequate 
	 * setting of limit to hold the newly created message before invoking the call. 
	 * It is also users' responsibility to re-adjust the limit 
	 * after the call is returned if needed. The size of the message can be obtained by 
	 * the {@link GroupObject#getSize()} call with an addition of the message header size. 
	 * 
	 * The position of the ByteBuffer is altered by the routine as needed. 
	 * 
	 * @param templateId the target message template ID
	 * @param buffer the buffer for building SBE message
	 * @param offset the starting position of the message
	 * @return a GroupObject to set values for fields in this message or null if the message cannot be created. 
	 */
	public GroupObject createSbeBuffer(int templateId, ByteBuffer buffer, int offset) {
		SBEMessage message = this.lookupTable.get(templateId);
		if( null != message ) {
			return message.createSbeBuffer(buffer, offset);
		} else {
			return null;
		}
	}
	
	private SBEMessage getSBEMessage(ByteBuffer buffer, int offset) {
		buffer.order(order);
		int schemaId = this.msgHeader.getSchemaId(buffer, offset);
		int templateId = this.msgHeader.getTemplateId(buffer, offset);
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
	 * @return a HashMap containing all available message definitions in the schema
	 */
	public HashMap<Integer, SBEMessage> getMsgLookup() {
		return this.lookupTable;
	}
}
