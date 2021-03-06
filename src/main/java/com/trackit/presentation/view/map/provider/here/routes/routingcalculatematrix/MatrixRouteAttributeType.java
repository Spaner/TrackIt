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
 * <p>Java class for MatrixRouteAttributeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MatrixRouteAttributeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="indices"/>
 *     &lt;enumeration value="summary"/>
 *     &lt;enumeration value="route"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MatrixRouteAttributeType", namespace = "http://www.navteq.com/lbsp/Routing-CalculateMatrix/1")
@XmlEnum
public enum MatrixRouteAttributeType {


    /**
     * Short value: "ix". Indicates whether StartIndex and DestinationIndex information should be returned in MatrixEntry instances for CalculateMatrixRoute requests. By default indices are returned, they can be omitted though, to reduce response sizes. When omitting indices, MatrixEntries will be ordered line by line from left to right, i.e. increasing the StartIndex in the outer loop and increasing the DestinationIndex in the inner loop.
     * 
     */
    @XmlEnumValue("indices")
    INDICES("indices"),

    /**
     * Short value: "su". Indicates whether only summary information should be included in matrix entries in the responses. By default matrix entries will contain route information including summaries. In case of failed routes or exceeded search ranges, matrix entries will contain a status information instead of summary entries (see <b>RouteStatusType</b>) to indicate why calculating a specific matrix entry failed. Note that options "ro" and "su" are mutually exclusive and connaot be combined.
     * 
     */
    @XmlEnumValue("summary")
    SUMMARY("summary"),

    /**
     * Short value: "ro". 'Indicates to return route elements in matrix entries. This is the default option. Note that options "ro" and "su" are mutually exclusive and connaot be combined.
     * 
     */
    @XmlEnumValue("route")
    ROUTE("route");
    private final String value;

    MatrixRouteAttributeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MatrixRouteAttributeType fromValue(String v) {
        for (MatrixRouteAttributeType c: MatrixRouteAttributeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
