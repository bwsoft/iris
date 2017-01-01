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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.Group;
import com.github.bwsoft.iris.message.MsgCodecRuntimeException;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.message.sbe.SBESchemaFieldTypes.SBECompositeTypeElement;
import com.github.bwsoft.iris.message.sbe.fixsbe.BlockType;
import com.github.bwsoft.iris.message.sbe.fixsbe.EncodedDataType;
import com.github.bwsoft.iris.message.sbe.fixsbe.EnumType;
import com.github.bwsoft.iris.message.sbe.fixsbe.GroupType;
import com.github.bwsoft.iris.message.sbe.fixsbe.MessageSchema;
import com.github.bwsoft.iris.message.sbe.fixsbe.SetType;

/**
 * A factory class to create a SBEMessageSchema.
 * 
 * @author yzhou
 *
 */
public class SBESchemaLoader {
	
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
	
	private SBESchemaLoader() {
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
	 * @throws FileNotFoundException 
	 * @throws JAXBException if failed to parse the SBE xml schema
	 * @throws FactoryConfigurationError 
	 * @throws XMLStreamException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static SBEMessageSchema loadSchema(String schemaXML) throws FileNotFoundException, JAXBException, XMLStreamException, FactoryConfigurationError {
		SBESchemaLoader schemaCache = new SBESchemaLoader();
		
		InputStream is = null;
		File file = new File(schemaXML);
		if( ! file.exists() || ! file.isFile() ) {
			is = schemaCache.getClass().getResourceAsStream(schemaXML);
		} else {
			is = new FileInputStream(file);
		}
		if( is == null )
			throw new FileNotFoundException();
		
		JAXBContext context = JAXBContext.newInstance("com.github.bwsoft.iris.message.sbe.fixsbe");
		Unmarshaller um = context.createUnmarshaller();
		MessageSchema schema = (MessageSchema) um.unmarshal(is);
		
		// create schema header
		schemaCache.schemaHeader = new SBEMessageSchemaHeader(schema.getPackage(), schema.getId(), schema.getVersion().intValue(), schema.getSemanticVersion(), schema.getByteOrder());

		// load all type definitions
		schemaCache.types = SBESchemaFieldTypes.parseSchemaForFieldTypes(schema);
		
		// create message header
		schemaCache.msgHeader = SBEMessageHeader.getMessageHeader(schemaCache.types);

		// create group header
		schemaCache.grpHeader = SBEGroupHeader.getDefaultGroupHeader(schemaCache.types);
		
		// create var length field header
		schemaCache.varHeader = SBEVarLengthFieldHeader.getDefaultVarLengthFieldHeader(schemaCache.types);

		// parsing message
		List<BlockType> messageList = schema.getMessage();
		for( BlockType message : messageList ) {
			SBEGroup sbeMsg = (SBEGroup) schemaCache.processGroupTypeNode(null, message);
			if( null != message.getBlockLength() ) {
				int blockLength = message.getBlockLength().intValue();
				int calculatedBlockLength = sbeMsg.getBlockSize();
				if( blockLength >= calculatedBlockLength ) {
					sbeMsg.setBlockSize(blockLength);
				} else {
					throw new MsgCodecRuntimeException("message blockLength defined in xml is less than required. "+
							"Defined blockLength is "+blockLength+", but requires at least "+calculatedBlockLength+
							" bytes for message, "+sbeMsg.getName());
				}
			}
		}
		
		return new SBEMessageSchema(schemaCache.schemaHeader, schemaCache.msgHeader, schemaCache.grpHeader, schemaCache.varHeader, schemaCache.lookupTable);
	}
	
	private void processFieldTypeNode(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.FieldType fieldType) {
		Long offset = fieldType.getOffset();
		if( null != FieldType.getType(fieldType.getType()) ) {
			// field of primitive type
			group.addField((short)fieldType.getId(), FieldType.getType(fieldType.getType()), offset, (short)1).setName(fieldType.getName());
		} else if( types.getEncodedDataTypes().containsKey(fieldType.getType())) {
			// a simple type
			EncodedDataType dataType = types.getEncodedDataTypes().get(fieldType.getType());
			addEncodedDataTypeField(group, fieldType, dataType, offset);
		} else if( types.getEnumTypes().containsKey(fieldType.getType())) {
			// an enum type
			SBESchemaFieldTypes.SBEEnumType sbeEnum = types.getEnumTypes().get(fieldType.getType());
			addEnumTypeField(group, fieldType, sbeEnum, offset);
		} else if( types.getSetTypes().containsKey(fieldType.getType())) {
			// a set bit field
			SBESchemaFieldTypes.SBESetType sbeSet = types.getSetTypes().get(fieldType.getType());
			addSetTypeField(group, fieldType, sbeSet, offset);
		} else if( types.getCompositeDataTypes().containsKey(fieldType.getType())) {
			// composite field
			SBECompositeField compositeField = (SBECompositeField) group.addField((short)fieldType.getId(),FieldType.COMPOSITE, offset, (short) 1).setName(fieldType.getName());
			List<SBECompositeTypeElement> eTypes = types.getCompositeDataTypes().get(fieldType.getType());
			addFieldToCompositeType(compositeField, null, eTypes, (long) 0);
		} else {
			throw new InternalError("undefined type: "+fieldType.getType());				
		}
	}

	private Group processGroupTypeNode(Group group, BlockType groupType) {
		SBEGroup childGroup = null;
		if( null == group ) {
			childGroup = new SBEMessage(schemaHeader, msgHeader, grpHeader, varHeader);
			childGroup.setID((short) groupType.getId()).setName(groupType.getName());
			lookupTable.put(groupType.getId(), (SBEMessage) childGroup);
		} else {
			String dimensionType = ((GroupType) groupType).getDimensionType();
			SBEGroupHeader grpHeader = SBEGroupHeader.getGroupHeader(types, dimensionType);
			childGroup = (SBEGroup) group.addField((short)groupType.getId(),grpHeader, FieldType.GROUP, null, (short) 1).setName(groupType.getName());
		}
		
		List<com.github.bwsoft.iris.message.sbe.fixsbe.FieldType> fieldList = groupType.getField();
		for( com.github.bwsoft.iris.message.sbe.fixsbe.FieldType aField : fieldList ) {
			processFieldTypeNode(childGroup, aField);
		}
		
		List<GroupType> groupList = groupType.getGroup();
		for( GroupType aGroup : groupList ) {
			processGroupTypeNode(childGroup, aGroup);
		}
		
		List<com.github.bwsoft.iris.message.sbe.fixsbe.FieldType> dataList = groupType.getData(); 
		for( com.github.bwsoft.iris.message.sbe.fixsbe.FieldType aData : dataList ) {
			processVarFieldTypeNode(childGroup, aData);
		}
		return childGroup;
	}
	
	private void processVarFieldTypeNode(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.FieldType fieldType) {
		group.addField((short)fieldType.getId(),FieldType.RAW, null, (short) 1).setName(fieldType.getName());		
	}
	
	private static SBEField addEncodedDataTypeField(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.FieldType fieldType, EncodedDataType dataType, Long offset) {
		if( null == dataType.getPresence() || ! "constant".equals(dataType.getPresence().toLowerCase())) {
			// handle none constant simple field
			FieldType primitiveType = FieldType.getType(dataType.getPrimitiveType());
			if( primitiveType == null ) {
				throw new IllegalArgumentException("unrecognized primitive type: "+dataType.getPrimitiveType());
			}
			if( null != fieldType )
				return (SBEField) group.addField((short)fieldType.getId(),primitiveType, offset, dataType.getLength().shortValue()).setName(fieldType.getName());
			else
				return (SBEField) group.addField((short)0, primitiveType, offset, dataType.getLength().shortValue()).setName(dataType.getName());
		} else {
			// handle constant simple field
			short id = null == fieldType ? (short) 0 : (short) fieldType.getId();
			String name = null == fieldType ? dataType.getName() : fieldType.getName();
			SBEField field = (SBEField) group.addField(id,FieldType.CONSTANT, null, dataType.getLength().shortValue()).setName(name);
			field.setConstantValue(dataType.getValue()).setConstantType(FieldType.getType(dataType.getPrimitiveType()));
			return field;
		}		
	}

	private static SBEField addEnumTypeField(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.FieldType fieldType, SBESchemaFieldTypes.SBEEnumType sbeEnum, Long offset) {
		short id = null == fieldType ? (short) 0 : (short) fieldType.getId();
		String name = null == fieldType ? sbeEnum.getName() : fieldType.getName();
		if( null == fieldType || null == fieldType.getPresence() || ! "constant".equals(fieldType.getPresence().toLowerCase()) ) {
			SBEField enumField = (SBEField) group.addField(id, sbeEnum.getPrimitiveType(), offset, (short) 1).setName(name);
			enumField.setEnumLookupTable(sbeEnum.enumLookup);
			return enumField;
		} else {
			// handle constant field 
			SBEField enumField = (SBEField) group.addField(id, FieldType.CONSTANT, null, (short) 1).setName(name);
			String valueRef = fieldType.getValueRef();
			int indexOfSep = valueRef.indexOf('.');
			if( indexOfSep > 0 ) {
				enumField.setConstantValue(valueRef.substring(indexOfSep+1));
			} else {
				// This is a format error
				throw new InternalError("invalid value reference in field: "+fieldType.getName());
			}
			return enumField;
		}
	}
	
	private static SBEField addSetTypeField(Group group, com.github.bwsoft.iris.message.sbe.fixsbe.FieldType fieldType, SBESchemaFieldTypes.SBESetType sbeSet, Long offset) {
		short id = null == fieldType ? (short) 0 : (short) fieldType.getId();
		String name = null == fieldType ? sbeSet.getName() : fieldType.getName();
		SBEField choiceField = (SBEField) group.addField(id, sbeSet.getPrimitiveType(), offset, (short) 1).setName(name);
		choiceField.setSetLookupTable(sbeSet.bitLookup);
		return choiceField;
	}
	
	@SuppressWarnings("unchecked")
	private void addFieldToCompositeType(SBECompositeField compositeField, String prefix, List<SBECompositeTypeElement> eTypes, Long startPos) {
		for( SBECompositeTypeElement rawType : eTypes ) {
			Long  offset = rawType.getOffset();
			if( null != offset ) {
				offset = offset.longValue() + startPos.longValue();
			}
			if( rawType.getType() instanceof EncodedDataType ) {
				EncodedDataType encodedType = (EncodedDataType) rawType.getType();
				SBEField field = addEncodedDataTypeField(compositeField, null, encodedType, offset);
				String name = encodedType.getName();
				if( null != rawType.getName() ) {
					name = rawType.getName()+"."+encodedType.getName();
				}
				if( null != prefix ) {
					name = prefix + "." + name;
				}
				field.setName(name);
			} else if( rawType.getType() instanceof EnumType ) {
				EnumType enumType = (EnumType) rawType.getType();
				SBESchemaFieldTypes.SBEEnumType sbeEnum = types.getEnumTypes().get(enumType.getName());
				SBEField field = addEnumTypeField(compositeField, null, sbeEnum, offset);
				String name = sbeEnum.getName();
				if( null != rawType.getName() ) {
					name = rawType.getName()+"."+sbeEnum.getName();
				}
				if( null != prefix ) {
					name = prefix + "." + name;
				}
				field.setName(name);
			} else if( rawType.getType() instanceof SetType ) {
				SetType setType = (SetType) rawType.getType();
				SBESchemaFieldTypes.SBESetType sbeSet = types.getSetTypes().get(setType.getName());
				SBEField field = addSetTypeField(compositeField, null, sbeSet, offset);
				String name = sbeSet.getName();
				if( null != rawType.getName() ) {
					name = rawType.getName()+"."+sbeSet.getName();
				}
				if( null != prefix ) {
					name = prefix + "." + name;
				}
				field.setName(name);
			} else if( rawType.getType() instanceof List<?> ) {
				Long currentPos = (long) compositeField.getBlockSize();
				addFieldToCompositeType(compositeField, rawType.getName(), (List<SBECompositeTypeElement>) rawType.getType(), currentPos);
			} else {
				throw new InternalError("unrecognized field type in the composite data type: "+compositeField.getName());
			}
		}		
	}
}
