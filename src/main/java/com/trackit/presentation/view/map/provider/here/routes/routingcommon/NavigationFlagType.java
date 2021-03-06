//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:12:22 PM WET 
//


package com.trackit.presentation.view.map.provider.here.routes.routingcommon;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NavigationFlagType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="NavigationFlagType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="baseRoute"/>
 *     &lt;enumeration value="complexIntersection"/>
 *     &lt;enumeration value="freewayIntersection"/>
 *     &lt;enumeration value="oneWay"/>
 *     &lt;enumeration value="roundabout"/>
 *     &lt;enumeration value="fork"/>
 *     &lt;enumeration value="motorway"/>
 *     &lt;enumeration value="tunnel"/>
 *     &lt;enumeration value="slipRoad"/>
 *     &lt;enumeration value="boatFerry"/>
 *     &lt;enumeration value="railFerry"/>
 *     &lt;enumeration value="start"/>
 *     &lt;enumeration value="destination"/>
 *     &lt;enumeration value="speedWarning"/>
 *     &lt;enumeration value="frontier"/>
 *     &lt;enumeration value="motorwayEntry"/>
 *     &lt;enumeration value="motorwayChange"/>
 *     &lt;enumeration value="motorwayExit"/>
 *     &lt;enumeration value="connected"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "NavigationFlagType")
@XmlEnum
public enum NavigationFlagType {

    @XmlEnumValue("baseRoute")
    BASE_ROUTE("baseRoute"),
    @XmlEnumValue("complexIntersection")
    COMPLEX_INTERSECTION("complexIntersection"),
    @XmlEnumValue("freewayIntersection")
    FREEWAY_INTERSECTION("freewayIntersection"),
    @XmlEnumValue("oneWay")
    ONE_WAY("oneWay"),
    @XmlEnumValue("roundabout")
    ROUNDABOUT("roundabout"),
    @XmlEnumValue("fork")
    FORK("fork"),
    @XmlEnumValue("motorway")
    MOTORWAY("motorway"),
    @XmlEnumValue("tunnel")
    TUNNEL("tunnel"),
    @XmlEnumValue("slipRoad")
    SLIP_ROAD("slipRoad"),
    @XmlEnumValue("boatFerry")
    BOAT_FERRY("boatFerry"),
    @XmlEnumValue("railFerry")
    RAIL_FERRY("railFerry"),
    @XmlEnumValue("start")
    START("start"),
    @XmlEnumValue("destination")
    DESTINATION("destination"),
    @XmlEnumValue("speedWarning")
    SPEED_WARNING("speedWarning"),
    @XmlEnumValue("frontier")
    FRONTIER("frontier"),
    @XmlEnumValue("motorwayEntry")
    MOTORWAY_ENTRY("motorwayEntry"),
    @XmlEnumValue("motorwayChange")
    MOTORWAY_CHANGE("motorwayChange"),
    @XmlEnumValue("motorwayExit")
    MOTORWAY_EXIT("motorwayExit"),
    @XmlEnumValue("connected")
    CONNECTED("connected");
    private final String value;

    NavigationFlagType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NavigationFlagType fromValue(String v) {
        for (NavigationFlagType c: NavigationFlagType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
