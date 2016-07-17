//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:12:22 PM WET 
//


package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcommon;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Logs position, speed and heading at a point in time. This information is usually provided by a GPS device.
 * 
 * <p>Java class for GeoPositionLogType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GeoPositionLogType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.navteq.com/lbsp/Common/4}GeoPositionType">
 *       &lt;sequence>
 *         &lt;element name="DistanceToPrevious" type="{http://www.navteq.com/lbsp/Common/4}DistanceType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeoPositionLogType", propOrder = {
    "distanceToPrevious"
})
public class GeoPositionLogType
    extends GeoPositionType
{

    @XmlElement(name = "DistanceToPrevious")
    protected double distanceToPrevious;

    /**
     * Gets the value of the distanceToPrevious property.
     * 
     */
    public double getDistanceToPrevious() {
        return distanceToPrevious;
    }

    /**
     * Sets the value of the distanceToPrevious property.
     * 
     */
    public void setDistanceToPrevious(double value) {
        this.distanceToPrevious = value;
    }

}
