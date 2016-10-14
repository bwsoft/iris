package com.github.bwsoft.iris.message.sbe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.github.bwsoft.iris.message.FieldType;
import com.github.bwsoft.iris.message.MsgCodecRuntimeException;
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
					EncodedDataType encodedType = (EncodedDataType) eType;
					SBECompositeTypeElement compElm = new SBECompositeTypeElement(eType);
					compElm.offset = encodedType.getOffset();
					eTypes.add(compElm);
					FieldType primitiveType = FieldType.getType(encodedType.getPrimitiveType());
					if( null == primitiveType ) {
						throw new MsgCodecRuntimeException("unrecognized primitive type in the encoded type, "+encodedType.getName());
					}
					int dimension = encodedType.getLength().intValue();
					compElm.size = dimension*primitiveType.size();
				} else if( eType instanceof EnumType ) {
					EnumType enumType = (EnumType) eType;

					// redefine enum type name to make it unique
					String simpleName = enumType.getName();
					enumType.setName(type.getName()+"."+enumType.getName());
					SBECompositeTypeElement compElm = new SBECompositeTypeElement(enumType);
					compElm.offset = enumType.getOffset();
					eTypes.add(compElm);
					SBEEnumType sbeEnumType = addEnumTypeNode(enumType);
					sbeEnumType.name = simpleName;
					compElm.size = sbeEnumType.getPrimitiveType().size();
				} else if( eType instanceof SetType ) {
					SetType setType = (SetType) eType;

					// redefine set type name to make it unique
					String simpleName = setType.getName();
					setType.setName(type.getName()+"."+setType.getName());
					SBECompositeTypeElement compElm = new SBECompositeTypeElement(setType);
					compElm.offset = setType.getOffset();
					eTypes.add(new SBECompositeTypeElement(setType));
					SBESetType sbeSetType = addSetTypeNode(setType);
					sbeSetType.name = simpleName;
					compElm.size = sbeSetType.getPrimitiveType().size();
				} else if( eType instanceof RefType ) {
					RefType rType = (RefType) eType;
					SBECompositeTypeElement compElm = new SBECompositeTypeElement(eType);
					compElm.offset = rType.getOffset();
					eTypes.add(compElm);
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
		for( List<SBECompositeTypeElement> eTypeList : sbeComposites.values() ) {
			for( int i = 0; i < eTypeList.size(); i ++ ) {
				SBECompositeTypeElement eType = eTypeList.get(i);
				if( eType.getType() instanceof RefType ) {
					RefType rType = (RefType) eType.getType();
					SBECompositeTypeElement elm = this.resolveRefType(rType,0);
					eTypeList.remove(i);
					eTypeList.add(i,elm);
				}
			}
		}
	}
	
	private SBECompositeTypeElement resolveRefType(RefType rType, int level) {
		if( level > 10 ) {
			throw new MsgCodecRuntimeException("the level of cross-reference in the composite type exceeds the limit of 10.");
		}
		if( sbeComposites.containsKey(rType.getType()) ) {
			List<SBECompositeTypeElement> toBeAdded = sbeComposites.get(rType.getType());
			for( int i = 0; i < toBeAdded.size(); i ++ ) {
				SBECompositeTypeElement typeElement = toBeAdded.get(i);
				if( typeElement.getType() instanceof RefType ) {
					RefType newRefType = (RefType)typeElement.getType();
					SBECompositeTypeElement replacement = resolveRefType(newRefType, level+1);
					toBeAdded.remove(i);
					toBeAdded.add(i,replacement);
				}
			}
			SBECompositeTypeElement elm = new SBECompositeTypeElement(toBeAdded);
			elm.name = rType.getName();
			elm.offset = rType.getOffset();
			return elm;
		} else {
			throw new MsgCodecRuntimeException("Issue encountered in schema definition with unresolvable RefType: "+rType.getName());			
		}
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
		private Long offset;
		private int size;
		
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
		
		Long getOffset() {
			return offset;
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
