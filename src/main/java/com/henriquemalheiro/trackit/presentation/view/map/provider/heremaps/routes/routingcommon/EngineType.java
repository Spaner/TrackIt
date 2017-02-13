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
 * <p>Java class for EngineType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="EngineType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="diesel"/>
 *     &lt;enumeration value="gasoline"/>
 *     &lt;enumeration value="electric"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "EngineType")
@XmlEnum
public enum EngineType {

    @XmlEnumValue("diesel")
    DIESEL("diesel"),
    @XmlEnumValue("gasoline")
    GASOLINE("gasoline"),
    @XmlEnumValue("electric")
    ELECTRIC("electric");
    private final String value;

    EngineType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EngineType fromValue(String v) {
        for (EngineType c: EngineType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
