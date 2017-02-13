//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:12:22 PM WET 
//


package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcommon;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TransportModeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TransportModeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="car"/>
 *     &lt;enumeration value="pedestrian"/>
 *     &lt;enumeration value="publicTransport"/>
 *     &lt;enumeration value="truck"/>
 *     &lt;enumeration value="bicycle"/>
 *     &lt;enumeration value="publicTransportTimeTable"/>
 *     &lt;enumeration value="bus"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TransportModeType")
@XmlEnum
public enum TransportModeType {


    /**
     * Route calculation for cars.
     * 
     */
    @XmlEnumValue("car")
    CAR("car"),

    /**
     * Route calculation for a pedestrian. As one effect, maneuvers will be optimized for walking, i.e. segments will consider actions relevant for pedestrians and maneuver instructions will contain texts suitable for a walking person. This mode disregards any traffic information.
     * 
     */
    @XmlEnumValue("pedestrian")
    PEDESTRIAN("pedestrian"),

    /**
     * Route calculation using public transport only.
     * 
     */
    @XmlEnumValue("publicTransport")
    PUBLIC_TRANSPORT("publicTransport"),

    /**
     * Route calculation for trucks. This mode will consider truck limitations on links and will use different speed assumptions when calculating the route.
     * 
     */
    @XmlEnumValue("truck")
    TRUCK("truck"),

    /**
     * Route calculation for bicycles.
     * <i>Note: not yet supported</i>
     * 
     */
    @XmlEnumValue("bicycle")
    BICYCLE("bicycle"),

    /**
     * Route calculation using public transport only with support for time table based routing. 
     * 					Note that time table based routing is not support in all regions. The routing service may choose to fallback
     * 					to simple public transport routing in case no time table information is available. In this case, the mode  
     * 					entry in the returned Route object will be set accordingly.
     * 					
     * 
     */
    @XmlEnumValue("publicTransportTimeTable")
    PUBLIC_TRANSPORT_TIME_TABLE("publicTransportTimeTable"),

    /**
     * Route calculation for busses. This mode will consider roads for which bus restrictions apply for the route calculations.
     * 
     */
    @XmlEnumValue("bus")
    BUS("bus");
    private final String value;

    TransportModeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TransportModeType fromValue(String v) {
        for (TransportModeType c: TransportModeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
