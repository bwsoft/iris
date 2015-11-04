package com.bunny.iris.message.sbe;

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
import org.fix.sbe.SetType;
import org.fix.sbe.ValidValue;

import com.bunny.iris.message.FieldType;

public class SBESchemaLoader {
	public HashMap<Integer, SBEMessage> loadSchema(String schemaXML) throws JAXBException, FileNotFoundException {
		InputStream is = null;
		File file = new File(schemaXML);
		if( ! file.exists() || ! file.isFile() ) {
			is = this.getClass().getResourceAsStream(schemaXML);
		} else {
			is = new FileInputStream(file);
		}
		if( is == null )
			throw new FileNotFoundException();
		
		JAXBContext context = JAXBContext.newInstance("org.fix.sbe");
		Unmarshaller um = context.createUnmarshaller();
		MessageSchema schema = (MessageSchema) um.unmarshal(is);
		
		HashMap<Integer, SBEMessage> lookupTable = new HashMap<Integer, SBEMessage>();
		HashMap<String, EncodedDataType> sbeTypes = new HashMap<String, EncodedDataType>();
		HashMap<String, List<EncodedDataType>> sbeComposites = new HashMap<String, List<EncodedDataType>>();
		HashMap<String, SBEEnum> sbeEnums = new HashMap<>();
		HashMap<String, SBESet> sbeChoices = new HashMap<String, SBESchemaLoader.SBESet>();
		
		// base type has to be processed first
		List<Types> typesList = schema.getTypes();
		for( int i = 0; i < typesList.size(); i ++ ) {
			List<EncodedDataType> typeList = typesList.get(i).getType();
			for( EncodedDataType type : typeList ) {
				sbeTypes.put(type.getName(),type);
			}
		}
		
		// followed by enum and choices
		for( int i = 0; i < typesList.size(); i ++ ) {
			// handle enum
			List<EnumType> enumList = typesList.get(i).getEnum();
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
			
			// handle choices
			List<SetType> setList = typesList.get(i).getSet();
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
		
		// composite type for last
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
		
		// parsing message
		List<Message> messageList = schema.getMessage();
		for( Message message : messageList ) {
			SBEMessage sbeMessage = new SBEMessage();
			sbeMessage.setByteOrder(schema.getByteOrder()).setSchemaId((short) schema.getId()).setID((short) message.getId()).setName(message.getName());
			lookupTable.put(message.getId(), sbeMessage);
			
			List<Serializable> msgContent = message.getContent();
			for( int i = 0; i < msgContent.size(); i ++ ) {
				Serializable content = msgContent.get(i);
				if( content instanceof JAXBElement ) {
					Object type = ((JAXBElement<?>) content).getValue();
					if( type instanceof org.fix.sbe.FieldType ) {
						org.fix.sbe.FieldType fieldType = (org.fix.sbe.FieldType) type;
						 
						if( null != FieldType.getType(fieldType.getType()) ) {
							// field of primitive type
							sbeMessage.addChildField(FieldType.getType(fieldType.getType()), (short)1).setID((short)fieldType.getId()).setName(fieldType.getName());
						} else if( sbeTypes.containsKey(fieldType.getType())) {
							// a simple type
							EncodedDataType dataType = sbeTypes.get(fieldType.getType());
							if( null == dataType.getPresence() || ! "constant".equals(dataType.getPresence().toLowerCase())) {
								// handle none constant simple field
								FieldType primitiveType = FieldType.getType(dataType.getPrimitiveType());
								if( primitiveType == null ) {
									throw new IllegalArgumentException("unrecognized primitive type: "+dataType.getPrimitiveType());
								}
								sbeMessage.addChildField(primitiveType,dataType.getLength().shortValue()).setID((short)fieldType.getId()).setName(fieldType.getName());//.setArraySize(dataType.getLength().shortValue());
							} else {
								//TODO: handle constant simple field
							}
						} else if( sbeEnums.containsKey(fieldType.getType())) {
							// an enum type
							SBEEnum sbeEnum = sbeEnums.get(fieldType.getType());
							SBEField enumField = (SBEField) sbeMessage.addChildField(sbeEnum.primitiveType, (short) 1).setID((short)fieldType.getId()).setName(fieldType.getName());
							enumField.setEnumLookupTable(sbeEnum.enumLookup);
						} else if( sbeChoices.containsKey(fieldType.getType())) {
							// a set bit field
							SBESet sbeSet = sbeChoices.get(fieldType.getType());
							SBEField choiceField = (SBEField) sbeMessage.addChildField(sbeSet.primitiveType, (short) 1).setID((short)fieldType.getId()).setName(fieldType.getName());
							choiceField.setSetLookupTable(sbeSet.bitLookup);
						}
					} else if( content instanceof GroupType ) {
						
					}
				}
			}
//			sbeMessage.finalizeDefinition();
		}
		return lookupTable;
	}
	
	private class SBEEnum {
		FieldType primitiveType;
		HashMap<String, String> enumLookup = new HashMap<String, String>();
	}
	
	private class SBESet {
		FieldType primitiveType;
		HashMap<String, Integer> bitLookup = new HashMap<String, Integer>();
	}
	
	public static void main(String[] args) throws FileNotFoundException, JAXBException {
		SBESchemaLoader loader = new SBESchemaLoader();
		HashMap<Integer, SBEMessage> lookup = loader.loadSchema(args[0]);
		lookup.forEach((k,v) -> {
			System.out.println(v);
		});
	}
}
