//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:11:02 PM WET 
//


package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculatematrix;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The NavigationWaypointParameter is used to define a waypoint based on navigational information like link positions. A common use case for this scenario is when the user specifies a waypoint by selecting a place or a location after having executed a search.
 * 
 * <p>Java class for NavigationWaypointParameterType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NavigationWaypointParameterType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.navteq.com/lbsp/Routing-Common/4}WaypointParameterType">
 *       &lt;sequence>
 *         &lt;element name="DisplayPosition" type="{http://www.navteq.com/lbsp/Common/4}GeoCoordinateType" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="StreetPosition" type="{http://www.navteq.com/lbsp/Routing-Common/4}StreetPositionType" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="LinkPosition" type="{http://www.navteq.com/lbsp/Routing-Common/4}LinkPositionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NavigationWaypointParameterType", propOrder = {
    "displayPosition",
    "streetPosition",
    "linkPosition"
})
public class NavigationWaypointParameterType
    extends WaypointParameterType
{

    @XmlElement(name = "DisplayPosition")
    protected GeoCoordinateType displayPosition;
    @XmlElement(name = "StreetPosition")
    protected List<StreetPositionType> streetPosition;
    @XmlElement(name = "LinkPosition")
    protected List<LinkPositionType> linkPosition;

    /**
     * Gets the value of the displayPosition property.
     * 
     * @return
     *     possible object is
     *     {@link GeoCoordinateType }
     *     
     */
    public GeoCoordinateType getDisplayPosition() {
        return displayPosition;
    }

    /**
     * Sets the value of the displayPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeoCoordinateType }
     *     
     */
    public void setDisplayPosition(GeoCoordinateType value) {
        this.displayPosition = value;
    }

    /**
     * Gets the value of the streetPosition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the streetPosition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStreetPosition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StreetPositionType }
     * 
     * 
     */
    public List<StreetPositionType> getStreetPosition() {
        if (streetPosition == null) {
            streetPosition = new ArrayList<StreetPositionType>();
        }
        return this.streetPosition;
    }

    /**
     * Gets the value of the linkPosition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkPosition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkPosition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinkPositionType }
     * 
     * 
     */
    public List<LinkPositionType> getLinkPosition() {
        if (linkPosition == null) {
            linkPosition = new ArrayList<LinkPositionType>();
        }
        return this.linkPosition;
    }

}
