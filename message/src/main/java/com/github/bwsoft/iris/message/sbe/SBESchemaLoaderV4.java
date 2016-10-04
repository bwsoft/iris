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
package com.github.bwsoft.iris.message.sbe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.Group;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.Choice;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.CompositeDataType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.EncodedDataType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.EnumType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.GroupType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.BlockType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.MessageSchema;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.SetType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.ValidValue;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.MessageSchema.Types;
import com.github.bwsoft.iris.util.MessageUtil;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.RefType;

/**
 * A factory class to create a SBEMessageSchema.
 * 
 * @author yzhou
 *
 */
public class SBESchemaLoaderV4 {
	
	static final String SAFE_MODE = "safeMode";
	static final String OPTIMIZED_NUM_OF_GROUPS = "optimizedNumOfGroups";
	static final String OPTIMIZED_NUM_OF_GROUP_ROWS = "optimizedNumOfGroupRows";
	
	static final Properties properties = new Properties();
	
	static {
		properties.setProperty(SAFE_MODE, System.getProperty(SAFE_MODE, "true"));
		properties.setProperty(OPTIMIZED_NUM_OF_GROUP_ROWS, System.getProperty(OPTIMIZED_NUM_OF_GROUP_ROWS, "8"));
		properties.setProperty(OPTIMIZED_NUM_OF_GROUPS, System.getProperty(OPTIMIZED_NUM_OF_GROUPS,"128"));
	}
	
	private SBESchemaFieldTypes types = null;
	private SBEMessageHeader msgHeader = null;
	private SBEGroupHeader grpHeader = null;
	private SBEVarLengthFieldHeader varHeader = null;
	private SBEMessageSchemaHeader schemaHeader = null;
	
	// contains the map between the template id and the message definition
	private HashMap<Integer, SBEMessage> lookupTable = new HashMap<>();
	
	private SBESchemaLoaderV4() {
	}
	
	/**
	 * Turn on the safe mode that enables the additional check for runtime errors.
	 */
	public static void safeModeOn() {
		properties.setProperty(SAFE_MODE, "true");
	}
	
	/**
	 * Turn off the safe mode that disable the additional check for runtime errors for better 
	 * performance.
	 */
	public static void safeModeOff() {
		properties.setProperty(SAFE_MODE, "false");
	}
	
	/**
	 * Set an optimized value for number of groups. It is a balance between the memory usage
	 * and occasionally performance impact. There is a performance punishment to increase
	 * the internal memory when the number of groups in a message exceeds
	 * this value.
	 *  
	 * @param number optimized value
	 */
	public static void setOptimizedNumOfGroups(int number) {
		properties.setProperty(OPTIMIZED_NUM_OF_GROUPS, String.valueOf(number));
	}
	
	/**
	 * Set an optimized value for number of group rows. It is a balance between the memory usage
	 * and occasionally performance impact. There is a performance punishment to increase
	 * the internal memory when the number of group rows for a group in a message exceeds
	 * this value.
	 *  
	 * @param number optimized value
	 */
	public static void setOptimizedNumOfGroupRows(int number) {
		properties.setProperty(OPTIMIZED_NUM_OF_GROUP_ROWS, String.valueOf(number));		
	}
	
	/**
	 * Create a SBEMessageSchema from a SBE XML message template.
	 *  
	 * @param schemaXML the SBE XML message template in the classpath or in the file system.
	 * @return the SBEMessageSchema to create SBE messages.
	 * @throws JAXBException if failed to parse the SBE xml schema
	 * @throws FileNotFoundException if the SBE xml schema file cannot be found
	 */
	public static SBEMessageSchema loadSchema(String schemaXML) throws JAXBException, FileNotFoundException {
		SBESchemaLoaderV4 schemaCache = new SBESchemaLoaderV4();
		
		InputStream is = null;
		File file = new File(schemaXML);
		if( ! file.exists() || ! file.isFile() ) {
			is = schemaCache.getClass().getResourceAsStream(schemaXML);
		} else {
			is = new FileInputStream(file);
		}
		if( is == null )
			throw new FileNotFoundException();
		
		JAXBContext context = JAXBContext.newInstance("com.github.bwsoft.iris.message.sbe.fixsbe.rc4");
		Unmarshaller um = context.createUnmarshaller();
		MessageSchema schema = (MessageSchema) um.unmarshal(is);
		
		// create schema header
		schemaCache.schemaHeader = new SBEMessageSchemaHeader(schema.getPackage(), schema.getId(), schema.getVersion().intValue(), schema.getSemanticVersion(), schema.getByteOrder());

		// load all type definitions
		schemaCache.types = SBESchemaFieldTypes.parseSchemaForFieldTypes(schema);
		
		// create message header
		schemaCache.msgHeader = SBEMessageHeader.getMessageHeader(schemaCache.types);

		// create group header
		schemaCache.grpHeader = SBEGroupHeader.getGroupHeader(schemaCache.types);
		
		// create var length field header
		schemaCache.varHeader = SBEVarLengthFieldHeader.getVarLengthFieldHeader(schemaCache.types);

		// parsing message
		List<BlockType> messageList = schema.getMessage();
		for( BlockType message : messageList ) {
			schemaCache.processGroupTypeNode(null, message);
		}
		
		return new SBEMessageSchema(schemaCache.schemaHeader, schemaCache.msgHeader, schemaCache.grpHeader, schemaCache.varHeader, schemaCache.lookupTable);
	}
	
	private void processFieldTypeNode(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.rc4.FieldType fieldType) {
		if( null != FieldType.getType(fieldType.getType()) ) {
			// field of primitive type
			group.addField((short)fieldType.getId(), FieldType.getType(fieldType.getType()), (short)1).setName(fieldType.getName());
		} else if( types.getEncodedDataTypes().containsKey(fieldType.getType())) {
			// a simple type
			EncodedDataType dataType = types.getEncodedDataTypes().get(fieldType.getType());
			addEncodedDataTypeField(group, fieldType, dataType);
		} else if( types.getEnumTypes().containsKey(fieldType.getType())) {
			// an enum type
			SBESchemaFieldTypes.SBEEnum sbeEnum = types.getEnumTypes().get(fieldType.getType());
			addEnumTypeField(group, fieldType, sbeEnum);
		} else if( types.getSetTypes().containsKey(fieldType.getType())) {
			// a set bit field
			SBESchemaFieldTypes.SBESet sbeSet = types.getSetTypes().get(fieldType.getType());
			addSetTypeField(group, fieldType, sbeSet);
		} else if( types.getCompositeDataTypes().containsKey(fieldType.getType())) {
			// composite field
			SBECompositeField compositeField = (SBECompositeField) group.addField((short)fieldType.getId(),FieldType.COMPOSITE, (short) 1).setName(fieldType.getName());
			List<Object> eTypes = types.getCompositeDataTypes().get(fieldType.getType());
			for( Object rawType : eTypes ) {
				if( rawType instanceof EncodedDataType ) {
					addEncodedDataTypeField(compositeField, null, (EncodedDataType) rawType);
				} else if( rawType instanceof EnumType ) {
					EnumType enumType = (EnumType) rawType;
					SBESchemaFieldTypes.SBEEnum sbeEnum = types.getEnumTypes().get(enumType.getName());
					addEnumTypeField(group, null, sbeEnum);
				} else if( rawType instanceof SetType ) {
					SetType setType = (SetType) rawType;
					SBESchemaFieldTypes.SBESet sbeSet = types.getSetTypes().get(setType.getName());
					addSetTypeField(group, null, sbeSet);
				} else {
					throw new InternalError("unrecognized field type in the composite data type: "+fieldType.getName());
				}
			}
		} else {
			throw new InternalError("undefined type: "+fieldType.getType());				
		}		
	}

	private void processGroupTypeNode(Group group, BlockType groupType) {
		SBEGroup childGroup = null;
		if( null == group ) {
			childGroup = new SBEMessage(schemaHeader, msgHeader, grpHeader, varHeader);
			childGroup.setID((short) groupType.getId()).setName(groupType.getName());
			lookupTable.put(groupType.getId(), (SBEMessage) childGroup);
		} else {
			childGroup = (SBEGroup) group.addField((short)groupType.getId(),FieldType.GROUP, (short) 1).setName(groupType.getName());
		}
		
		List<com.github.bwsoft.iris.message.sbe.fixsbe.rc4.FieldType> fieldList = groupType.getField();
		for( com.github.bwsoft.iris.message.sbe.fixsbe.rc4.FieldType aField : fieldList ) {
			processFieldTypeNode(childGroup, aField);
		}
		
		List<GroupType> groupList = groupType.getGroup();
		for( GroupType aGroup : groupList ) {
			processGroupTypeNode(childGroup, aGroup);
		}
		
		List<com.github.bwsoft.iris.message.sbe.fixsbe.rc4.FieldType> dataList = groupType.getData(); 
		for( com.github.bwsoft.iris.message.sbe.fixsbe.rc4.FieldType aData : dataList ) {
			processVarFieldTypeNode(childGroup, aData);
		}
	}
	
	private void processVarFieldTypeNode(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.rc4.FieldType fieldType) {
		group.addField((short)fieldType.getId(),FieldType.RAW, (short) 1).setName(fieldType.getName());		
	}
	
	private static void addEncodedDataTypeField(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.rc4.FieldType fieldType, EncodedDataType dataType) {
		if( null == dataType.getPresence() || ! "constant".equals(dataType.getPresence().toLowerCase())) {
			// handle none constant simple field
			FieldType primitiveType = FieldType.getType(dataType.getPrimitiveType());
			if( primitiveType == null ) {
				throw new IllegalArgumentException("unrecognized primitive type: "+dataType.getPrimitiveType());
			}
			if( null != fieldType )
				group.addField((short)fieldType.getId(),primitiveType,dataType.getLength().shortValue()).setName(fieldType.getName());
			else
				group.addField((short)0, primitiveType, dataType.getLength().shortValue()).setName(dataType.getName());
		} else {
			// handle constant simple field
			short id = null == fieldType ? (short) 0 : (short) fieldType.getId();
			String name = null == fieldType ? dataType.getName() : fieldType.getName();
			SBEField field = (SBEField) group.addField(id,FieldType.CONSTANT,dataType.getLength().shortValue()).setName(name);
			field.setConstantValue(dataType.getValue()).setConstantType(FieldType.getType(dataType.getPrimitiveType()));
		}		
	}

	private static void addEnumTypeField(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.rc4.FieldType fieldType, SBESchemaFieldTypes.SBEEnum sbeEnum) {
		short id = null == fieldType ? (short) 0 : (short) fieldType.getId();
		String name = null == fieldType ? sbeEnum.getName() : fieldType.getName();
		SBEField enumField = (SBEField) group.addField(id, sbeEnum.primitiveType, (short) 1).setName(name);
		enumField.setEnumLookupTable(sbeEnum.enumLookup);
	}
	
	private static void addSetTypeField(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.rc4.FieldType fieldType, SBESchemaFieldTypes.SBESet sbeSet) {
		short id = null == fieldType ? (short) 0 : (short) fieldType.getId();
		String name = null == fieldType ? sbeSet.getName() : fieldType.getName();
		SBEField choiceField = (SBEField) group.addField(id, sbeSet.primitiveType, (short) 1).setName(name);
		choiceField.setSetLookupTable(sbeSet.bitLookup);
	}

	public static void main(String args[]) throws FileNotFoundException, JAXBException {
		SBEMessageSchema schema = SBESchemaLoaderV4.loadSchema("src/test/resources/example-schemav4.xml");
		SBEMessage message = schema.getMsgLookup().get(1);
		System.out.println(MessageUtil.toJsonString(message));
	}
}
