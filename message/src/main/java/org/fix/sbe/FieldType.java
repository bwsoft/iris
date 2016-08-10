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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.10.13 at 10:17:36 PM CDT 
//


package org.fix.sbe;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *                 A field of a message of a specified dataType
 *             
 * 
 * <p>Java class for fieldType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fieldType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{http://www.fixprotocol.org/ns/simple/1.0}fieldAttributes"/>
 *       &lt;attribute name="name" use="required" type="{http://www.fixprotocol.org/ns/simple/1.0}symbolicName_t" />
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="type" use="required" type="{http://www.fixprotocol.org/ns/simple/1.0}symbolicName_t" />
 *       &lt;attribute name="valueRef" type="{http://www.fixprotocol.org/ns/simple/1.0}symbolicName_t" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="offset" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="epoch" type="{http://www.w3.org/2001/XMLSchema}string" default="unix" />
 *       &lt;attribute name="timeUnit" type="{http://www.w3.org/2001/XMLSchema}string" default="nanosecond" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldType")
public class FieldType {

    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "id", required = true)
    @XmlSchemaType(name = "unsignedShort")
    protected int id;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "valueRef")
    protected String valueRef;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlAttribute(name = "offset")
    @XmlSchemaType(name = "unsignedInt")
    protected Long offset;
    @XmlAttribute(name = "epoch")
    protected String epoch;
    @XmlAttribute(name = "timeUnit")
    protected String timeUnit;
    @XmlAttribute(name = "sinceVersion")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger sinceVersion;
    @XmlAttribute(name = "semanticType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String semanticType;

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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the valueRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueRef() {
        return valueRef;
    }

    /**
     * Sets the value of the valueRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueRef(String value) {
        this.valueRef = value;
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
     * Gets the value of the offset property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getOffset() {
        return offset;
    }

    /**
     * Sets the value of the offset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOffset(Long value) {
        this.offset = value;
    }

    /**
     * Gets the value of the epoch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEpoch() {
        if (epoch == null) {
            return "unix";
        } else {
            return epoch;
        }
    }

    /**
     * Sets the value of the epoch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEpoch(String value) {
        this.epoch = value;
    }

    /**
     * Gets the value of the timeUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeUnit() {
        if (timeUnit == null) {
            return "nanosecond";
        } else {
            return timeUnit;
        }
    }

    /**
     * Sets the value of the timeUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeUnit(String value) {
        this.timeUnit = value;
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
     * Gets the value of the semanticType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSemanticType() {
        return semanticType;
    }

    /**
     * Sets the value of the semanticType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSemanticType(String value) {
        this.semanticType = value;
    }

}
