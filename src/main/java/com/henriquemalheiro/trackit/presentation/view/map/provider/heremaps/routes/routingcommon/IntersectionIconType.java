//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:12:22 PM WET 
//


package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcommon;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Simplified representation of an intersection icon.
 * 
 * <p>Java class for IntersectionIconType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IntersectionIconType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Type" type="{http://www.navteq.com/lbsp/Routing-Common/4}IntersectionTypeType"/>
 *         &lt;element name="TrafficDirection" type="{http://www.navteq.com/lbsp/Routing-Common/4}TrafficDirectionType" minOccurs="0"/>
 *         &lt;element name="Leg" type="{http://www.navteq.com/lbsp/Routing-Common/4}IntersectionLegType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IntersectionIconType", propOrder = {
    "type",
    "trafficDirection",
    "leg"
})
public class IntersectionIconType {

    @XmlElement(name = "Type", required = true)
    protected IntersectionTypeType type;
    @XmlElement(name = "TrafficDirection")
    protected TrafficDirectionType trafficDirection;
    @XmlElement(name = "Leg")
    protected List<IntersectionLegType> leg;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link IntersectionTypeType }
     *     
     */
    public IntersectionTypeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link IntersectionTypeType }
     *     
     */
    public void setType(IntersectionTypeType value) {
        this.type = value;
    }

    /**
     * Gets the value of the trafficDirection property.
     * 
     * @return
     *     possible object is
     *     {@link TrafficDirectionType }
     *     
     */
    public TrafficDirectionType getTrafficDirection() {
        return trafficDirection;
    }

    /**
     * Sets the value of the trafficDirection property.
     * 
     * @param value
     *     allowed object is
     *     {@link TrafficDirectionType }
     *     
     */
    public void setTrafficDirection(TrafficDirectionType value) {
        this.trafficDirection = value;
    }

    /**
     * Gets the value of the leg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the leg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLeg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IntersectionLegType }
     * 
     * 
     */
    public List<IntersectionLegType> getLeg() {
        if (leg == null) {
            leg = new ArrayList<IntersectionLegType>();
        }
        return this.leg;
    }

}
