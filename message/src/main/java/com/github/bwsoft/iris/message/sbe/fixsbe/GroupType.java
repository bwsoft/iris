//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.29 at 08:51:30 PM CST 
//


package com.github.bwsoft.iris.message.sbe.fixsbe;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				A repeating group contains an array of entries
 * 			
 * 
 * <p>Java class for groupType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="groupType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://fixprotocol.io/2016/sbe}blockType">
 *       &lt;attribute name="dimensionType" type="{http://fixprotocol.io/2016/sbe}symbolicName_t" default="groupSizeEncoding" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "groupType")
public class GroupType
    extends BlockType
{

    @XmlAttribute(name = "dimensionType")
    protected String dimensionType;

    /**
     * Gets the value of the dimensionType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDimensionType() {
        if (dimensionType == null) {
            return "groupSizeEncoding";
        } else {
            return dimensionType;
        }
    }

    /**
     * Sets the value of the dimensionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDimensionType(String value) {
        this.dimensionType = value;
    }

}
