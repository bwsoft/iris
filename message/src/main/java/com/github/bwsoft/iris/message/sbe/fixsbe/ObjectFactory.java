//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.29 at 08:51:30 PM CST 
//


package com.github.bwsoft.iris.message.sbe.fixsbe;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.github.bwsoft.iris.message.sbe.fixsbe.v1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Message_QNAME = new QName("http://fixprotocol.io/2016/sbe", "message");
    private final static QName _CompositeDataTypeRef_QNAME = new QName("", "ref");
    private final static QName _CompositeDataTypeSet_QNAME = new QName("", "set");
    private final static QName _CompositeDataTypeComposite_QNAME = new QName("", "composite");
    private final static QName _CompositeDataTypeType_QNAME = new QName("", "type");
    private final static QName _CompositeDataTypeEnum_QNAME = new QName("", "enum");
    private final static QName _SetTypeChoice_QNAME = new QName("", "choice");
    private final static QName _EnumTypeValidValue_QNAME = new QName("", "validValue");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.github.bwsoft.iris.message.sbe.fixsbe.v1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MessageSchema }
     * 
     */
    public MessageSchema createMessageSchema() {
        return new MessageSchema();
    }

    /**
     * Create an instance of {@link MessageSchema.Types }
     * 
     */
    public MessageSchema.Types createMessageSchemaTypes() {
        return new MessageSchema.Types();
    }

    /**
     * Create an instance of {@link BlockType }
     * 
     */
    public BlockType createBlockType() {
        return new BlockType();
    }

    /**
     * Create an instance of {@link GroupType }
     * 
     */
    public GroupType createGroupType() {
        return new GroupType();
    }

    /**
     * Create an instance of {@link ValidValue }
     * 
     */
    public ValidValue createValidValue() {
        return new ValidValue();
    }

    /**
     * Create an instance of {@link CompositeDataType }
     * 
     */
    public CompositeDataType createCompositeDataType() {
        return new CompositeDataType();
    }

    /**
     * Create an instance of {@link EnumType }
     * 
     */
    public EnumType createEnumType() {
        return new EnumType();
    }

    /**
     * Create an instance of {@link RefType }
     * 
     */
    public RefType createRefType() {
        return new RefType();
    }

    /**
     * Create an instance of {@link EncodedDataType }
     * 
     */
    public EncodedDataType createEncodedDataType() {
        return new EncodedDataType();
    }

    /**
     * Create an instance of {@link SetType }
     * 
     */
    public SetType createSetType() {
        return new SetType();
    }

    /**
     * Create an instance of {@link Choice }
     * 
     */
    public Choice createChoice() {
        return new Choice();
    }

    /**
     * Create an instance of {@link FieldType }
     * 
     */
    public FieldType createFieldType() {
        return new FieldType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BlockType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fixprotocol.io/2016/sbe", name = "message")
    public JAXBElement<BlockType> createMessage(BlockType value) {
        return new JAXBElement<BlockType>(_Message_QNAME, BlockType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ref", scope = CompositeDataType.class)
    public JAXBElement<RefType> createCompositeDataTypeRef(RefType value) {
        return new JAXBElement<RefType>(_CompositeDataTypeRef_QNAME, RefType.class, CompositeDataType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "set", scope = CompositeDataType.class)
    public JAXBElement<SetType> createCompositeDataTypeSet(SetType value) {
        return new JAXBElement<SetType>(_CompositeDataTypeSet_QNAME, SetType.class, CompositeDataType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompositeDataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "composite", scope = CompositeDataType.class)
    public JAXBElement<CompositeDataType> createCompositeDataTypeComposite(CompositeDataType value) {
        return new JAXBElement<CompositeDataType>(_CompositeDataTypeComposite_QNAME, CompositeDataType.class, CompositeDataType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EncodedDataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "type", scope = CompositeDataType.class)
    public JAXBElement<EncodedDataType> createCompositeDataTypeType(EncodedDataType value) {
        return new JAXBElement<EncodedDataType>(_CompositeDataTypeType_QNAME, EncodedDataType.class, CompositeDataType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "enum", scope = CompositeDataType.class)
    public JAXBElement<EnumType> createCompositeDataTypeEnum(EnumType value) {
        return new JAXBElement<EnumType>(_CompositeDataTypeEnum_QNAME, EnumType.class, CompositeDataType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Choice }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "choice", scope = SetType.class)
    public JAXBElement<Choice> createSetTypeChoice(Choice value) {
        return new JAXBElement<Choice>(_SetTypeChoice_QNAME, Choice.class, SetType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidValue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "validValue", scope = EnumType.class)
    public JAXBElement<ValidValue> createEnumTypeValidValue(ValidValue value) {
        return new JAXBElement<ValidValue>(_EnumTypeValidValue_QNAME, ValidValue.class, EnumType.class, value);
    }

}