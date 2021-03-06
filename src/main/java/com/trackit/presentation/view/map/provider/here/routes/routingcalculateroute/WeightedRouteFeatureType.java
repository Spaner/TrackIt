//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:11:32 PM WET 
//


package com.trackit.presentation.view.map.provider.here.routes.routingcalculateroute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * This type is used to assign a weight to a RouteFeature.
 * 
 * <p>Java class for WeightedRouteFeatureType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WeightedRouteFeatureType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.navteq.com/lbsp/Routing-Common/4>RouteFeatureType">
 *       &lt;attribute name="weight" use="required" type="{http://www.navteq.com/lbsp/Routing-Common/4}RouteFeatureWeightType" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeightedRouteFeatureType", propOrder = {
    "value"
})
public class WeightedRouteFeatureType {

    @XmlValue
    protected RouteFeatureType value;
    @XmlAttribute(name = "weight", required = true)
    protected byte weight;

    /**
     * The routing features can be used to define special conditions on the calculated route. The user can weight each feature with positive or negative weights, see type RouteFeatureWeight.
     * 
     * @return
     *     possible object is
     *     {@link RouteFeatureType }
     *     
     */
    public RouteFeatureType getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link RouteFeatureType }
     *     
     */
    public void setValue(RouteFeatureType value) {
        this.value = value;
    }

    /**
     * Gets the value of the weight property.
     * 
     */
    public byte getWeight() {
        return weight;
    }

    /**
     * Sets the value of the weight property.
     * 
     */
    public void setWeight(byte value) {
        this.weight = value;
    }

}
