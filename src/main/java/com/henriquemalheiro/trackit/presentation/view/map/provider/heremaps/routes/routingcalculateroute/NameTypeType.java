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
 * <p>Java class for NameTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="NameTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="baseName"/>
 *     &lt;enumeration value="shortBaseName"/>
 *     &lt;enumeration value="abbreviation"/>
 *     &lt;enumeration value="primary"/>
 *     &lt;enumeration value="alternative"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "NameTypeType", namespace = "http://www.navteq.com/lbsp/Common/4")
@XmlEnum
public enum NameTypeType {

    @XmlEnumValue("baseName")
    BASE_NAME("baseName"),
    @XmlEnumValue("shortBaseName")
    SHORT_BASE_NAME("shortBaseName"),
    @XmlEnumValue("abbreviation")
    ABBREVIATION("abbreviation"),
    @XmlEnumValue("primary")
    PRIMARY("primary"),
    @XmlEnumValue("alternative")
    ALTERNATIVE("alternative");
    private final String value;

    NameTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NameTypeType fromValue(String v) {
        for (NameTypeType c: NameTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
