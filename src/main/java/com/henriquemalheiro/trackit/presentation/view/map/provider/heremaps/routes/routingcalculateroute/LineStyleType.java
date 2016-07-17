//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:11:32 PM WET 
//


package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculateroute;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LineStyleType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LineStyleType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="solid"/>
 *     &lt;enumeration value="dotted"/>
 *     &lt;enumeration value="dash"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LineStyleType")
@XmlEnum
public enum LineStyleType {

    @XmlEnumValue("solid")
    SOLID("solid"),
    @XmlEnumValue("dotted")
    DOTTED("dotted"),
    @XmlEnumValue("dash")
    DASH("dash");
    private final String value;

    LineStyleType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LineStyleType fromValue(String v) {
        for (LineStyleType c: LineStyleType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
