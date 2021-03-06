//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:11:32 PM WET 
//


package com.trackit.presentation.view.map.provider.here.routes.routingcalculateroute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Defines the current travel progress in a navigation scenario.
 * 
 * <p>Java class for TravelProgressType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TravelProgressType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mappedPosition" type="{http://www.navteq.com/lbsp/Routing-Common/4}WaypointType"/>
 *         &lt;element name="remainDistance" type="{http://www.navteq.com/lbsp/Common/4}DistanceType"/>
 *         &lt;element name="remainTime" type="{http://www.navteq.com/lbsp/Common/4}DurationType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TravelProgressType", propOrder = {
    "mappedPosition",
    "remainDistance",
    "remainTime"
})
public class TravelProgressType {

    @XmlElement(required = true)
    protected WaypointType mappedPosition;
    protected double remainDistance;
    protected double remainTime;

    /**
     * Gets the value of the mappedPosition property.
     * 
     * @return
     *     possible object is
     *     {@link WaypointType }
     *     
     */
    public WaypointType getMappedPosition() {
        return mappedPosition;
    }

    /**
     * Sets the value of the mappedPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link WaypointType }
     *     
     */
    public void setMappedPosition(WaypointType value) {
        this.mappedPosition = value;
    }

    /**
     * Gets the value of the remainDistance property.
     * 
     */
    public double getRemainDistance() {
        return remainDistance;
    }

    /**
     * Sets the value of the remainDistance property.
     * 
     */
    public void setRemainDistance(double value) {
        this.remainDistance = value;
    }

    /**
     * Gets the value of the remainTime property.
     * 
     */
    public double getRemainTime() {
        return remainTime;
    }

    /**
     * Sets the value of the remainTime property.
     * 
     */
    public void setRemainTime(double value) {
        this.remainTime = value;
    }

}
