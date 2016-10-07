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

	public HashMap<String, List<SBECompositeTypeElement>> getCompositeDataTypes() {
		return sbeComposites;
	}

	public HashMap<String, SBEEnumType> getEnumTypes() {
		return sbeEnums;
	}

	public HashMap<String, SBESetType> getSetTypes() {
		return sbeChoices;
	}

	private HashMap<String, EncodedDataType> sbeTypes; // contains all the definitions of type in types
	private HashMap<String, List<SBECompositeTypeElement>> sbeComposites; // contains all the definition of composite in types
	private HashMap<String, SBEEnumType> sbeEnums; // contains all the map between enum name and its values
	private HashMap<String, SBESetType> sbeChoices; // contains all the map between a set/choice name and its corresponding bit set.

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
	private SBESetType addSetTypeNode(SetType type) {
		SBESetType sbeSet = new SBESetType();
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
	private SBEEnumType addEnumTypeNode(EnumType type) {
		SBEEnumType sbeEnum = new SBEEnumType();
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
		ArrayList<SBECompositeTypeElement> eTypes = new ArrayList<>();
		for( int j = 0; j < content.size(); j ++ ) {
			if( content.get(j) instanceof JAXBElement ) {
				JAXBElement<?> elm = (JAXBElement<?>) content.get(j);
				Object eType = elm.getValue();
				if( eType instanceof EncodedDataType ) {
					eTypes.add(new SBECompositeTypeElement(eType));
				} else if( eType instanceof EnumType ) {
					EnumType enumType = (EnumType) eType;

					// redefine enum type name to make it unique
					String simpleName = enumType.getName();
					enumType.setName(type.getName()+"."+enumType.getName());
					eTypes.add(new SBECompositeTypeElement(enumType));
					addEnumTypeNode(enumType).name = simpleName;
				} else if( eType instanceof SetType ) {
					SetType setType = (SetType) eType;

					// redefine set type name to make it unique
					String simpleName = setType.getName();
					setType.setName(type.getName()+"."+setType.getName());
					eTypes.add(new SBECompositeTypeElement(setType));
					addSetTypeNode(setType).name = simpleName;
				} else if( eType instanceof RefType ) {
					RefType rType = (RefType) eType;
					if( sbeComposites.containsKey(rType.getType()) ) {
						List<SBECompositeTypeElement> toBeAdded = sbeComposites.get(rType.getType());
						for( int i = 0; i < toBeAdded.size(); i ++ ) {
							SBECompositeTypeElement anElm = toBeAdded.get(i).clone();
							anElm.name = rType.getName();
							eTypes.add(anElm);
						}
					} else {
						eTypes.add(new SBECompositeTypeElement(eType));
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
			for( List<SBECompositeTypeElement> eTypeList : sbeComposites.values() ) {
				for( int i = 0; i < eTypeList.size(); i ++ ) {
					Object eType = eTypeList.get(i);
					if( eType instanceof RefType ) {
						RefType rType = (RefType) eType;
						if( sbeComposites.containsKey(rType.getType())) {
							List<SBECompositeTypeElement> toBeAdded = sbeComposites.get(rType.getType());
							eTypeList.remove(i);
							i --;
							for( int j = 0; j < toBeAdded.size(); j++) {
								SBECompositeTypeElement anElm = toBeAdded.get(0).clone();
								anElm.name = rType.getName();
								i++;
								eTypeList.add(i,anElm);
							}
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

	static class SBEEnumType {
		private String name;
		private FieldType primitiveType;
		HashMap<String, String> enumLookup = new HashMap<String, String>();
		
		String getName() {
			return name;
		}
		
		FieldType getPrimitiveType() {
			return primitiveType;
		}
	}
	
	static class SBESetType {
		private String name;
		private FieldType primitiveType;
		HashMap<String, Integer> bitLookup = new HashMap<String, Integer>();

		String getName() {
			return name;
		}
		
		FieldType getPrimitiveType() {
			return primitiveType;
		}
	}
	
	static class SBECompositeTypeElement {
		private String name;
		private Object type;
		
		private SBECompositeTypeElement() {
			this.name = null;
			this.type = null;
		}
		
		private SBECompositeTypeElement(Object type) {
			this.type = type;
			this.name = null;
		}
		
		Object getType() {
			return type;
		}
		
		String getName() {
			return name;
		}
		
		@Override
		public SBECompositeTypeElement clone() {
			SBECompositeTypeElement newElm = new SBECompositeTypeElement();
			newElm.name = name;
			newElm.type = type;
			return newElm;
		}
	}
}
