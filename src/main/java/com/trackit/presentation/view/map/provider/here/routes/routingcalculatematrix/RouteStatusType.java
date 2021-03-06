//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:11:02 PM WET 
//


package com.trackit.presentation.view.map.provider.here.routes.routingcalculatematrix;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RouteStatusType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RouteStatusType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="rangeExceeded"/>
 *     &lt;enumeration value="failed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "RouteStatusType", namespace = "http://www.navteq.com/lbsp/Routing-CalculateMatrix/1")
@XmlEnum
public enum RouteStatusType {


    /**
     * Indicates that a route calculation was aborted due to exceeding the search range specified in the request. This applies for Matrix routes only.
     * 
     */
    @XmlEnumValue("rangeExceeded")
    RANGE_EXCEEDED("rangeExceeded"),

    /**
     * Indicates that a route calculation failed due to invalide or non-reachable destination waypoint.
     * 
     */
    @XmlEnumValue("failed")
    FAILED("failed");
    private final String value;

    RouteStatusType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RouteStatusType fromValue(String v) {
        for (RouteStatusType c: RouteStatusType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
