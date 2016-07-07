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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.10.13 at 10:17:36 PM CDT 
//


package org.fix.sbe;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A repeating group contains an array of entries
 *             
 * 
 * <p>Java class for groupType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="groupType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="field" type="{http://www.fixprotocol.org/ns/simple/1.0}fieldType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="group" type="{http://www.fixprotocol.org/ns/simple/1.0}groupType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.fixprotocol.org/ns/simple/1.0}symbolicName_t" />
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="blockLength" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="sinceVersion" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="0" />
 *       &lt;attribute name="dimensionType" type="{http://www.fixprotocol.org/ns/simple/1.0}symbolicName_t" default="groupSizeEncoding" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "groupType", propOrder = {
    "field",
    "group"
})
public class GroupType {

    protected List<FieldType> field;
    protected List<GroupType> group;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "id", required = true)
    @XmlSchemaType(name = "unsignedShort")
    protected int id;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlAttribute(name = "blockLength")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger blockLength;
    @XmlAttribute(name = "sinceVersion")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger sinceVersion;
    @XmlAttribute(name = "dimensionType")
    protected String dimensionType;

    /**
     * Gets the value of the field property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the field property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldType }
     * 
     * 
     */
    public List<FieldType> getField() {
        if (field == null) {
            field = new ArrayList<FieldType>();
        }
        return this.field;
    }

    /**
     * Gets the value of the group property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the group property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GroupType }
     * 
     * 
     */
    public List<GroupType> getGroup() {
        if (group == null) {
            group = new ArrayList<GroupType>();
        }
        return this.group;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the blockLength property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBlockLength() {
        return blockLength;
    }

    /**
     * Sets the value of the blockLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBlockLength(BigInteger value) {
        this.blockLength = value;
    }

    /**
     * Gets the value of the sinceVersion property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSinceVersion() {
        if (sinceVersion == null) {
            return new BigInteger("0");
        } else {
            return sinceVersion;
        }
    }

    /**
     * Sets the value of the sinceVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSinceVersion(BigInteger value) {
        this.sinceVersion = value;
    }

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
