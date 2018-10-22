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
 * <p>Java class for PublicTransportTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PublicTransportTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="busPublic"/>
 *     &lt;enumeration value="busTouristic"/>
 *     &lt;enumeration value="busIntercity"/>
 *     &lt;enumeration value="busExpress"/>
 *     &lt;enumeration value="railMetroRegional"/>
 *     &lt;enumeration value="railMetro"/>
 *     &lt;enumeration value="railLight"/>
 *     &lt;enumeration value="railRegional"/>
 *     &lt;enumeration value="trainRegional"/>
 *     &lt;enumeration value="trainIntercity"/>
 *     &lt;enumeration value="trainHighSpeed"/>
 *     &lt;enumeration value="monoRail"/>
 *     &lt;enumeration value="aerial"/>
 *     &lt;enumeration value="inclined"/>
 *     &lt;enumeration value="water"/>
 *     &lt;enumeration value="privateService"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PublicTransportTypeType")
@XmlEnum
public enum PublicTransportTypeType {

    @XmlEnumValue("busPublic")
    BUS_PUBLIC("busPublic"),
    @XmlEnumValue("busTouristic")
    BUS_TOURISTIC("busTouristic"),
    @XmlEnumValue("busIntercity")
    BUS_INTERCITY("busIntercity"),
    @XmlEnumValue("busExpress")
    BUS_EXPRESS("busExpress"),
    @XmlEnumValue("railMetroRegional")
    RAIL_METRO_REGIONAL("railMetroRegional"),
    @XmlEnumValue("railMetro")
    RAIL_METRO("railMetro"),
    @XmlEnumValue("railLight")
    RAIL_LIGHT("railLight"),
    @XmlEnumValue("railRegional")
    RAIL_REGIONAL("railRegional"),
    @XmlEnumValue("trainRegional")
    TRAIN_REGIONAL("trainRegional"),
    @XmlEnumValue("trainIntercity")
    TRAIN_INTERCITY("trainIntercity"),
    @XmlEnumValue("trainHighSpeed")
    TRAIN_HIGH_SPEED("trainHighSpeed"),
    @XmlEnumValue("monoRail")
    MONO_RAIL("monoRail"),
    @XmlEnumValue("aerial")
    AERIAL("aerial"),
    @XmlEnumValue("inclined")
    INCLINED("inclined"),
    @XmlEnumValue("water")
    WATER("water"),
    @XmlEnumValue("privateService")
    PRIVATE_SERVICE("privateService");
    private final String value;

    PublicTransportTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PublicTransportTypeType fromValue(String v) {
        for (PublicTransportTypeType c: PublicTransportTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}