//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:09:22 PM WET 
//


package com.trackit.presentation.view.map.provider.here.routes.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Generic container to keep textual values for any attribute in different languages and/or with different semantics and/or type.
 * 
 * <p>Java class for AlternativeValueType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AlternativeValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Key" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Type" type="{http://www.navteq.com/lbsp/Common/4}NameTypeType" minOccurs="0"/>
 *         &lt;element name="Semantics" type="{http://www.navteq.com/lbsp/Common/4}TextSemanticsType" minOccurs="0"/>
 *         &lt;element name="Language" type="{http://www.navteq.com/lbsp/Common/4}LanguageCodeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlternativeValueType", propOrder = {
    "key",
    "value",
    "type",
    "semantics",
    "language"
})
public class AlternativeValueType {

    @XmlElement(name = "Key", required = true)
    protected String key;
    @XmlElement(name = "Value", required = true)
    protected String value;
    @XmlElement(name = "Type")
    protected NameTypeType type;
    @XmlElement(name = "Semantics")
    protected TextSemanticsType semantics;
    @XmlElement(name = "Language")
    protected String language;

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link NameTypeType }
     *     
     */
    public NameTypeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link NameTypeType }
     *     
     */
    public void setType(NameTypeType value) {
        this.type = value;
    }

    /**
     * Gets the value of the semantics property.
     * 
     * @return
     *     possible object is
     *     {@link TextSemanticsType }
     *     
     */
    public TextSemanticsType getSemantics() {
        return semantics;
    }

    /**
     * Sets the value of the semantics property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextSemanticsType }
     *     
     */
    public void setSemantics(TextSemanticsType value) {
        this.semantics = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguage(String value) {
        this.language = value;
    }

}
