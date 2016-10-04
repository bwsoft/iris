package com.github.bwsoft.iris.message.sbe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.Choice;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.CompositeDataType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.EncodedDataType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.EnumType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.MessageSchema;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.RefType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.SetType;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.ValidValue;
import com.github.bwsoft.iris.message.sbe.fixsbe.rc4.MessageSchema.Types;

/**
 * @author yzhou
 *
 */
/**
 * @author yzhou
 *
 */
class SBESchemaFieldTypes {
	public HashMap<String, EncodedDataType> getEncodedDataTypes() {
		return sbeTypes;
	}

	public HashMap<String, List<Object>> getCompositeDataTypes() {
		return sbeComposites;
	}

	public HashMap<String, SBEEnum> getEnumTypes() {
		return sbeEnums;
	}

	public HashMap<String, SBESet> getSetTypes() {
		return sbeChoices;
	}

	private HashMap<String, EncodedDataType> sbeTypes; // contains all the definitions of type in types
	private HashMap<String, List<Object>> sbeComposites; // contains all the definition of composite in types
	private HashMap<String, SBEEnum> sbeEnums; // contains all the map between enum name and its values
	private HashMap<String, SBESet> sbeChoices; // contains all the map between a set/choice name and its corresponding bit set.

	static SBESchemaFieldTypes parseSchemaForFieldTypes(MessageSchema schema) {
		SBESchemaFieldTypes types = new SBESchemaFieldTypes();
		types.fetechTypes(schema);
		types.fetchEnums(schema);
		types.fetchChoices(schema);
		types.fetchComposites(schema);
		return types;
	}
	
	private SBESchemaFieldTypes() {
		sbeTypes = new HashMap<>();
		sbeComposites = new HashMap<>();
		sbeEnums = new HashMap<>();
		sbeChoices = new HashMap<>();		
	}
	
	/*
	 * Obtain all of type defined in types section. There can be multiple sections 
	 * of "types".
	 * 
	 * @param schema object of xml root element
	 */
	private void fetechTypes(MessageSchema schema) {
		List<Types> typesList = schema.getTypes();
		for( int i = 0; i < typesList.size(); i ++ ) {
			List<Object> types = typesList.get(i).getTypeOrCompositeOrEnum();
			for( Object type : types ) {
				if( type instanceof EncodedDataType ) {
					EncodedDataType encodedDataType = (EncodedDataType) type;
					sbeTypes.put(encodedDataType.getName(),encodedDataType);
				}
			}
		}
	}

	/**
	 * @param schema object of xml root element
	 */
	private void fetchEnums(MessageSchema schema) {
		List<Types> typesList = schema.getTypes();
		for( Types types : typesList ) {
			List<Object> enumList = types.getTypeOrCompositeOrEnum();
			for( Object rawType : enumList ) {
				if( rawType instanceof EnumType ) {
					addEnumTypeNode((EnumType) rawType);
				}
			}
		}
	}
	
	/**
	 * Find and load all definitions of set types.
	 * 
	 * @param schema object of xml root element
	 */
	private void fetchChoices(MessageSchema schema) {
		List<Types> typesList = schema.getTypes();
		for( Types types : typesList ) {
			List<Object> setList = types.getTypeOrCompositeOrEnum();
			for( Object rawType : setList ) {
				if( rawType instanceof SetType ) {
					addSetTypeNode((SetType) rawType);
				}
			}
		}
	}

	/**
	 * Find and load all definitions of compositions.
	 * 
	 * @param schema object of xml root element
	 */
	private void fetchComposites(MessageSchema schema) {
		List<Types> typesList = schema.getTypes();		
		for( int i = 0; i < typesList.size(); i ++ ) {
			List<Object> compositeList = typesList.get(i).getTypeOrCompositeOrEnum();
			for( Object rawType : compositeList ) {
				if( rawType instanceof CompositeDataType ) {
					addCompositeDataTypeNode((CompositeDataType) rawType);
				}
			}
		}

		resolveRefType();
	}

	/**
	 * @param type a SetType node
	 */
	private SBESet addSetTypeNode(SetType type) {
		SBESet sbeSet = new SBESet();
		sbeSet.primitiveType = FieldType.getType(type.getEncodingType());
		sbeSet.name = type.getName();
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
		
		return sbeSet;
	}
	
	/**
	 * @param type a EnumType XML node
	 */
	private SBEEnum addEnumTypeNode(EnumType type) {
		SBEEnum sbeEnum = new SBEEnum();
		sbeEnum.primitiveType = FieldType.getType(type.getEncodingType());
		sbeEnum.name = type.getName();
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
		return sbeEnum;
	}
	
	private void addCompositeDataTypeNode(CompositeDataType type) {
		List<Serializable> content = type.getContent();
		
		// create an array list to contain all types in the definition
		ArrayList<Object> eTypes = new ArrayList<>();
		for( int j = 0; j < content.size(); j ++ ) {
			if( content.get(j) instanceof JAXBElement ) {
				JAXBElement<?> elm = (JAXBElement<?>) content.get(j);
				Object eType = elm.getValue();
				if( eType instanceof EncodedDataType ) {
					eTypes.add(eType);
				} else if( eType instanceof EnumType ) {
					EnumType enumType = (EnumType) eType;

					// redefine enum type name to make it unique
					enumType.setName(type.getName()+"."+enumType.getName());
					eTypes.add(enumType);
					addEnumTypeNode(enumType).name = enumType.getName();
				} else if( eType instanceof SetType ) {
					SetType setType = (SetType) eType;

					// redefine set type name to make it unique
					setType.setName(type.getName()+"."+setType.getName());
					eTypes.add(setType);
					addSetTypeNode(setType).name = setType.getName();
				} else if( eType instanceof RefType ) {
					RefType rType = (RefType) eType;
					if( sbeComposites.containsKey(rType.getType()) ) {
						eTypes.addAll(sbeComposites.get(rType.getType()));
					} else {
						eTypes.add(eType);
					}
				}
				else {
					throw new UnsupportedOperationException("Unrecogined element in composite");
				}
			}
		}
		sbeComposites.put(type.getName(), eTypes);		
	}
	
	/**
	 * loop through all SBE composite types to resovle all reftypes
	 */
	private void resolveRefType() {
		int refCountDiscovered = 0;
		int refCountPrevious = 0;
		do {
			for( List<Object> eTypeList : sbeComposites.values() ) {
				for( int i = 0; i < eTypeList.size(); i ++ ) {
					Object eType = eTypeList.get(i);
					if( eType instanceof RefType ) {
						RefType rType = (RefType) eType;
						if( sbeComposites.containsKey(rType.getType())) {
							List<Object> toBeAdded = sbeComposites.get(rType.getType());
							eTypeList.remove(i);
							eTypeList.add(i, toBeAdded);
							i = i + toBeAdded.size() - 1;
							refCountDiscovered ++;
						}
						else {
							throw new InternalError("Issue encountered in schema definition with unresolvable RefType: "+rType.getName());
						}
					}
				}
			}
			if( refCountPrevious == 0 || refCountPrevious >= refCountDiscovered ) {
				refCountPrevious = refCountDiscovered;
				refCountDiscovered = 0;
			} else {
				throw new InternalError("Issue encountered in schema definition with circular reference in RefType definitions.");				
			}
		} while ( refCountPrevious != 0 );				
	}

	static class SBEEnum {
		private String name;
		FieldType primitiveType;
		HashMap<String, String> enumLookup = new HashMap<String, String>();
		
		String getName() {
			return name;
		}
	}
	
	static class SBESet {
		private String name;
		FieldType primitiveType;
		HashMap<String, Integer> bitLookup = new HashMap<String, Integer>();

		String getName() {
			return name;
		}
	}
}
