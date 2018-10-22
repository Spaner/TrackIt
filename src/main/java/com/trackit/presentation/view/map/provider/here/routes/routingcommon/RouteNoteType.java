//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:12:22 PM WET 
//


package com.trackit.presentation.view.map.provider.here.routes.routingcommon;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;


/**
 * Route notes are used to store additional information about the route. These notes can either be related to the calculation itself (like violated routing options)  or to the characteristics of the route (like entering a toll road, passing a border, etc.).
 * 
 * <p>Java class for RouteNoteType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RouteNoteType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Type" type="{http://www.navteq.com/lbsp/Routing-Common/4}RouteNoteTypeType"/>
 *         &lt;element name="Code" type="{http://www.navteq.com/lbsp/Routing-Common/4}RouteNoteCodeType"/>
 *         &lt;element name="Text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Position" type="{http://www.navteq.com/lbsp/Common/4}GeoCoordinateType" minOccurs="0"/>
 *         &lt;element name="LinkIds" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;list itemType="{http://www.navteq.com/lbsp/Common/4}LinkIdType" />
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ValidityPeriod" type="{http://www.navteq.com/lbsp/Common/4}PeriodType" minOccurs="0"/>
 *         &lt;element name="AdditionalData" type="{http://www.navteq.com/lbsp/Common/4}KeyValuePairType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RouteNoteType", propOrder = {
    "type",
    "code",
    "text",
    "position",
    "linkIds",
    "validityPeriod",
    "additionalData"
})
public class RouteNoteType {

    @XmlElement(name = "Type", required = true)
    protected RouteNoteTypeType type;
    @XmlElement(name = "Code", required = true)
    protected RouteNoteCodeType code;
    @XmlElement(name = "Text")
    protected String text;
    @XmlElement(name = "Position")
    protected GeoCoordinateType position;
    @XmlList
    @XmlElement(name = "LinkIds")
    protected List<String> linkIds;
    @XmlElement(name = "ValidityPeriod")
    protected PeriodType validityPeriod;
    @XmlElement(name = "AdditionalData")
    protected List<KeyValuePairType> additionalData;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link RouteNoteTypeType }
     *     
     */
    public RouteNoteTypeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link RouteNoteTypeType }
     *     
     */
    public void setType(RouteNoteTypeType value) {
        this.type = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link RouteNoteCodeType }
     *     
     */
    public RouteNoteCodeType getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link RouteNoteCodeType }
     *     
     */
    public void setCode(RouteNoteCodeType value) {
        this.code = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link GeoCoordinateType }
     *     
     */
    public GeoCoordinateType getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeoCoordinateType }
     *     
     */
    public void setPosition(GeoCoordinateType value) {
        this.position = value;
    }

    /**
     * Gets the value of the linkIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkIds() {
        if (linkIds == null) {
            linkIds = new ArrayList<String>();
        }
        return this.linkIds;
    }

    /**
     * Gets the value of the validityPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link PeriodType }
     *     
     */
    public PeriodType getValidityPeriod() {
        return validityPeriod;
    }

    /**
     * Sets the value of the validityPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link PeriodType }
     *     
     */
    public void setValidityPeriod(PeriodType value) {
        this.validityPeriod = value;
    }

    /**
     * Gets the value of the additionalData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the additionalData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdditionalData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link KeyValuePairType }
     * 
     * 
     */
    public List<KeyValuePairType> getAdditionalData() {
        if (additionalData == null) {
            additionalData = new ArrayList<KeyValuePairType>();
        }
        return this.additionalData;
    }

}