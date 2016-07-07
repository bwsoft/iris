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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.fix.sbe.Choice;
import org.fix.sbe.CompositeDataType;
import org.fix.sbe.EncodedDataType;
import org.fix.sbe.EnumType;
import org.fix.sbe.GroupType;
import org.fix.sbe.Message;
import org.fix.sbe.MessageSchema;
import org.fix.sbe.MessageSchema.Types;

import com.bwsoft.iris.message.FieldType;

import org.fix.sbe.SetType;
import org.fix.sbe.ValidValue;

public class SBESchemaLoader {
		
	HashMap<String, EncodedDataType> sbeTypes; // contains all the definitions of type in types
	HashMap<String, List<EncodedDataType>> sbeComposites; // contains all the definition of composite in types
	HashMap<String, SBEEnum> sbeEnums; // contains all the map between enum name and its values
	HashMap<String, SBESet> sbeChoices; // contains all the map between a set/choice name and its corresponding bit set.
	
	public SBESchemaLoader() {
		
	}
	
	public static HashMap<Integer, SBEMessage> loadSchema(String schemaXML) throws JAXBException, FileNotFoundException {
		SBESchemaLoader schemaCache = new SBESchemaLoader();
		
		// contains the map between the template id and the message definition
		HashMap<Integer, SBEMessage> lookupTable = new HashMap<>();

		InputStream is = null;
		File file = new File(schemaXML);
		if( ! file.exists() || ! file.isFile() ) {
			is = schemaCache.getClass().getResourceAsStream(schemaXML);
		} else {
			is = new FileInputStream(file);
		}
		if( is == null )
			throw new FileNotFoundException();
		
		JAXBContext context = JAXBContext.newInstance("org.fix.sbe");
		Unmarshaller um = context.createUnmarshaller();
		MessageSchema schema = (MessageSchema) um.unmarshal(is);
		
		// create schema header
		SBEMessageSchema schemaHeader = new SBEMessageSchema(schema.getPackage(), schema.getVersion().intValue(), schema.getSemanticVersion(), schema.getByteOrder());
		
		// base type has to be processed first
		schemaCache.sbeTypes = fetechTypes(schema);
		
		// parse enums
		schemaCache.sbeEnums = fetchEnums(schema, schemaCache.sbeTypes);
		
		// parse choices
		schemaCache.sbeChoices = fetchChoices(schema, schemaCache.sbeTypes);
		
		// composite type for last
		schemaCache.sbeComposites = fetchComposites(schema);
		
		// create message header
		SBEMessageHeader msgHeader = getMessageHeader(schemaCache.sbeComposites);

		// create group header
		SBEGroupHeader grpHeader = getGroupHeader(schemaCache.sbeComposites);
		
		// create var length field header
		SBEVarLengthFieldHeader varHeader = getVarLengthFieldHeader(schemaCache.sbeComposites);

		// parsing message
		List<Message> messageList = schema.getMessage();
		for( Message message : messageList ) {
			SBEMessage sbeMessage = new SBEMessage(schemaHeader, msgHeader, grpHeader, varHeader);
			sbeMessage.setID((short) message.getId()).setName(message.getName());
			lookupTable.put(message.getId(), sbeMessage);
			
			List<Serializable> msgContent = message.getContent();
			for( int i = 0; i < msgContent.size(); i ++ ) {
				Serializable content = msgContent.get(i);
				if( content instanceof JAXBElement ) {
					String elementName = ((JAXBElement<?>) content).getName().toString();
					Object type = ((JAXBElement<?>) content).getValue();
					processFieldsOfAGroup(schemaCache, sbeMessage, elementName, type);
				}
			}
		}
		return lookupTable;
	}
	
	/*
	 * Obtain all of type defined in types section. There can be multiple sections 
	 * of "types".
	 * 
	 * @param schema object of xml root element
	 */
	private static HashMap<String, EncodedDataType> fetechTypes(MessageSchema schema) {
		HashMap<String, EncodedDataType> sbeTypes = new HashMap<>();
		List<Types> typesList = schema.getTypes();
		for( int i = 0; i < typesList.size(); i ++ ) {
			List<EncodedDataType> typeList = typesList.get(i).getType();
			for( EncodedDataType type : typeList ) {
				sbeTypes.put(type.getName(),type);
			}
		}
		return sbeTypes;
	}
	
	/**
	 * @param schema object of xml root element
	 * @param sbeTypes loaded types returned from fetchTypes
	 * @return
	 */
	private static HashMap<String, SBEEnum> fetchEnums(MessageSchema schema, HashMap<String, EncodedDataType> sbeTypes) {
		HashMap<String, SBEEnum> sbeEnums = new HashMap<>();
		List<Types> typesList = schema.getTypes();
		for( Types types : typesList ) {
			List<EnumType> enumList = types.getEnum();
			for( EnumType type : enumList ) {
				SBEEnum sbeEnum = new SBEEnum();
				sbeEnum.primitiveType = FieldType.getType(type.getEncodingType());
				if( null == sbeEnum.primitiveType ) {
					EncodedDataType eType = sbeTypes.get(type.getEncodingType());
					if( null != eType ) {
						sbeEnum.primitiveType = FieldType.getType(eType.getPrimitiveType());
					}
				}

				if( null == sbeEnum.primitiveType ) 
					throw new IllegalArgumentException("unrecognized primitive type, "+type.getEncodingType()+", in type definition: "+type.getName());

				sbeEnums.put(type.getName(), sbeEnum);
				List<Serializable> enumContents = type.getContent();
				for( int j = 0; j < enumContents.size(); j ++ ) {
					if( enumContents.get(j) instanceof JAXBElement ) {
						@SuppressWarnings("unchecked")
						ValidValue validValue = ((JAXBElement<ValidValue>)enumContents.get(j)).getValue();
						sbeEnum.enumLookup.put(validValue.getValue(), validValue.getName());
					}
				}
			}
		}
		return sbeEnums;
	}
	
	/**
	 * Find and load all definitions of set types.
	 * 
	 * @param schema object of xml root element
	 * @param sbeTypes loaded types returned from fetchTypes
	 * @return
	 */
	private static HashMap<String, SBESet> fetchChoices(MessageSchema schema, HashMap<String, EncodedDataType> sbeTypes) {
		HashMap<String, SBESet> sbeChoices = new HashMap<>();
		List<Types> typesList = schema.getTypes();
		for( Types types : typesList ) {
			List<SetType> setList = types.getSet();
			for( SetType type : setList ) {
				SBESet sbeSet = new SBESet();
				sbeSet.primitiveType = FieldType.getType(type.getEncodingType());
				if( null == sbeSet.primitiveType ) {
					EncodedDataType eType = sbeTypes.get(type.getEncodingType());
					if( null != eType ) {
						sbeSet.primitiveType = FieldType.getType(eType.getPrimitiveType());
					}
				}

				if( null == sbeSet.primitiveType ) 
					throw new IllegalArgumentException("unrecognized primitive type, "+type.getEncodingType()+", in type definition: "+type.getName());

				sbeChoices.put(type.getName(), sbeSet);
				List<Serializable> setContents = type.getContent();
				for( int j = 0; j < setContents.size(); j ++ ) {
					if( setContents.get(j) instanceof JAXBElement ) {
						@SuppressWarnings("unchecked")
						Choice choice = ((JAXBElement<Choice>)setContents.get(j)).getValue();
						sbeSet.bitLookup.put(choice.getName(), choice.getValue().intValue());
					}
				}
			}
		}
		return sbeChoices;
	}
	
	/**
	 * Find and load all definitions of compositions.
	 * 
	 * @param schema
	 * @return
	 */
	private static HashMap<String, List<EncodedDataType>> fetchComposites(MessageSchema schema) {
		HashMap<String, List<EncodedDataType>> sbeComposites = new HashMap<String, List<EncodedDataType>>();
		List<Types> typesList = schema.getTypes();		
		for( int i = 0; i < typesList.size(); i ++ ) {
			List<CompositeDataType> compositeList = typesList.get(i).getComposite();
			for( CompositeDataType type : compositeList ) {
				List<Serializable> content = type.getContent();
				ArrayList<EncodedDataType> eTypes = new ArrayList<EncodedDataType>();
				for( int j = 0; j < content.size(); j ++ ) {
					if( content.get(j) instanceof JAXBElement ) {
						@SuppressWarnings("unchecked")
						JAXBElement<EncodedDataType> elm = (JAXBElement<EncodedDataType>) content.get(j);
						EncodedDataType eType = elm.getValue();
						eTypes.add(eType);
					}
				}
				sbeComposites.put(type.getName(), eTypes);
			}
		}	
		return sbeComposites;
	}
	
	/**
	 * Get SBEMessageHeader definition
	 * 
	 * @param sbeComposites composite type hashmap returned from fetchComposites.
	 * @return
	 */
	private static SBEMessageHeader getMessageHeader(HashMap<String, List<EncodedDataType>> sbeComposites) {
		SBEMessageHeader msgHeader = null;
		if( sbeComposites.containsKey("messageHeader") ) {
			List<EncodedDataType> eTypes = sbeComposites.get("messageHeader");
			FieldType blockLength = FieldType.U16;
			FieldType templateId = FieldType.U16;
			FieldType schemaId = FieldType.U16;
			FieldType version = FieldType.U16;
			for( EncodedDataType type : eTypes ) {
				if( "blockLength".equals(type.getName()) )
					blockLength = FieldType.getType(type.getPrimitiveType());
				else if( "templateId".equals(type.getName()) )
					templateId = FieldType.getType(type.getPrimitiveType());
				else if( "schemaId".equals(type.getName()) )
					schemaId = FieldType.getType(type.getPrimitiveType());
				else if( "version".equals(type.getName()) )
					version = FieldType.getType(type.getPrimitiveType());
			}
			
			if( null == blockLength || null == templateId || null == schemaId || null == version ) {
				throw new IllegalArgumentException("unrecgnized primitive type in message header definition");
			}
			
			msgHeader = new SBEMessageHeader(templateId, schemaId, blockLength, version);
		} else {
			msgHeader = new SBEMessageHeader(FieldType.U16, FieldType.U16, FieldType.U16, FieldType.U16);
		}
		return msgHeader;
	}
	
	/**
	 * Get group header definition.
	 * 
	 * @param sbeComposites composite type hashmap returned from fetchComposites.
	 * @return
	 */
	private static SBEGroupHeader getGroupHeader(HashMap<String, List<EncodedDataType>> sbeComposites) {
		SBEGroupHeader grpHeader = null;
		if( sbeComposites.containsKey("groupSizeEncoding") ) {
			List<EncodedDataType> eTypes = sbeComposites.get("groupSizeEncoding");
			FieldType numInGroupType = FieldType.U8;
			FieldType blockSizeType = FieldType.U16;
			for( EncodedDataType type : eTypes ) {
				if( "blockLength".equals(type.getName()) )
					blockSizeType = FieldType.getType(type.getPrimitiveType());
				else if( "numInGroup".equals(type.getName()) )
					numInGroupType = FieldType.getType(type.getPrimitiveType());
			}
			
			if( null == numInGroupType || null == blockSizeType ) {
				throw new IllegalArgumentException("unrecgnized primitive type in group header definition");	
			}
			grpHeader = new SBEGroupHeader(numInGroupType, blockSizeType);
		} else {
			grpHeader = new SBEGroupHeader(FieldType.U8, FieldType.U16);
		}
		return grpHeader;
	}
	
	/**
	 * Get var length field header.
	 * 
	 * @param sbeComposites composite type hashmap returned from fetchComposites.
	 * @return
	 */
	private static SBEVarLengthFieldHeader getVarLengthFieldHeader(HashMap<String, List<EncodedDataType>> sbeComposites) {
		SBEVarLengthFieldHeader varHeader = null;
		if( sbeComposites.containsKey("varDataEncoding") ) {
			List<EncodedDataType> eTypes = sbeComposites.get("varDataEncoding");
			FieldType lengthType = FieldType.U8;
			for( EncodedDataType type : eTypes )
				if( "length".equals(type.getName()) )
					lengthType = FieldType.getType(type.getPrimitiveType());
			if( null == lengthType ) {
				throw new IllegalArgumentException("unrecgnized primitive type in var length field header definition");					
			}
			varHeader = new SBEVarLengthFieldHeader(lengthType);
		} else {
			varHeader = new SBEVarLengthFieldHeader(FieldType.U8);
		}
		return varHeader;
	}
	
	private static void processFieldsOfAGroup(SBESchemaLoader schemaCache, SBEGroup group, String elementName, Object elementType) {
		if( elementType instanceof org.fix.sbe.FieldType ) {
			org.fix.sbe.FieldType fieldType = (org.fix.sbe.FieldType) elementType;

			if( null != FieldType.getType(fieldType.getType()) ) {
				// field of primitive type
				group.addChildField((short)fieldType.getId(), FieldType.getType(fieldType.getType()), (short)1).setName(fieldType.getName());
			} else if( schemaCache.sbeTypes.containsKey(fieldType.getType())) {
				// a simple type
				EncodedDataType dataType = schemaCache.sbeTypes.get(fieldType.getType());
				if( null == dataType.getPresence() || ! "constant".equals(dataType.getPresence().toLowerCase())) {
					// handle none constant simple field
					FieldType primitiveType = FieldType.getType(dataType.getPrimitiveType());
					if( primitiveType == null ) {
						throw new IllegalArgumentException("unrecognized primitive type: "+dataType.getPrimitiveType());
					}
					group.addChildField((short)fieldType.getId(),primitiveType,dataType.getLength().shortValue()).setName(fieldType.getName());
				} else {
					//TODO: handle constant simple field
				}
			} else if( schemaCache.sbeEnums.containsKey(fieldType.getType())) {
				// an enum type
				SBEEnum sbeEnum = schemaCache.sbeEnums.get(fieldType.getType());
				SBEField enumField = (SBEField) group.addChildField((short)fieldType.getId(), sbeEnum.primitiveType, (short) 1).setName(fieldType.getName());
				enumField.setEnumLookupTable(sbeEnum.enumLookup);
			} else if( schemaCache.sbeChoices.containsKey(fieldType.getType())) {
				// a set bit field
				SBESet sbeSet = schemaCache.sbeChoices.get(fieldType.getType());
				SBEField choiceField = (SBEField) group.addChildField((short)fieldType.getId(), sbeSet.primitiveType, (short) 1).setName(fieldType.getName());
				choiceField.setSetLookupTable(sbeSet.bitLookup);
			} else if( schemaCache.sbeComposites.containsKey(fieldType.getType()) && ! "data".equals(elementName)) {
				// composite field
				SBECompositeField compositeField = (SBECompositeField) group.addChildField((short)fieldType.getId(),FieldType.COMPOSITE, (short) 1).setName(fieldType.getName());
				List<EncodedDataType> eTypes = schemaCache.sbeComposites.get(fieldType.getType());
				for( EncodedDataType eType : eTypes ) {			
					if( null == eType.getPresence() || ! "constant".equals(eType.getPresence().toLowerCase())) {
						// handle none constant simple field
						FieldType primitiveType = FieldType.getType(eType.getPrimitiveType());
						if( primitiveType == null ) {
							throw new IllegalArgumentException("unrecognized primitive type: "+eType.getPrimitiveType());
						}
						compositeField.addChildField((short)fieldType.getId(),primitiveType,eType.getLength().shortValue()).setName(eType.getName());
					} else {
						//TODO: handle constant simple field
					}
				}
			} else if(schemaCache.sbeComposites.containsKey(fieldType.getType()) && "data".equals(elementName) ) {
				// variable length field
				group.addChildField((short)fieldType.getId(),FieldType.RAW, (short) 1).setName(fieldType.getName());
			}
		} else if( elementType instanceof GroupType ) {
			GroupType groupType = (GroupType) elementType;
			SBEGroup childGroup = (SBEGroup) group.addChildField((short)groupType.getId(),FieldType.GROUP, (short) 1).setName(groupType.getName());
			List<org.fix.sbe.FieldType> childFieldList = groupType.getField();
			for( org.fix.sbe.FieldType type : childFieldList ) {
				processFieldsOfAGroup(schemaCache, childGroup, "field", type);
			}
			List<GroupType> childGroups = groupType.getGroup();
			for( GroupType subGroup : childGroups ) {
				processFieldsOfAGroup(schemaCache, childGroup, "group", subGroup);
			}
		}
	}
	
	private static class SBEEnum {
		FieldType primitiveType;
		HashMap<String, String> enumLookup = new HashMap<String, String>();
	}
	
	private static class SBESet {
		FieldType primitiveType;
		HashMap<String, Integer> bitLookup = new HashMap<String, Integer>();
	}
	
	public static void main(String[] args) throws FileNotFoundException, JAXBException {
		SBESchemaLoader loader = new SBESchemaLoader();
		HashMap<Integer, SBEMessage> lookup = loader.loadSchema("src/test/resources/example-schema.xml");
		lookup.forEach((k,v) -> {
			System.out.println(v);
		});
	}
}
