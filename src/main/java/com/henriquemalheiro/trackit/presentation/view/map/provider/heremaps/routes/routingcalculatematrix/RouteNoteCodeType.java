//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:11:02 PM WET 
//


package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculatematrix;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RouteNoteCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RouteNoteCodeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="routingOptionViolated"/>
 *     &lt;enumeration value="passingPlace"/>
 *     &lt;enumeration value="roadNameChanged"/>
 *     &lt;enumeration value="sharpCurveAhead"/>
 *     &lt;enumeration value="linkFeatureAhead"/>
 *     &lt;enumeration value="timeDependentRestriction"/>
 *     &lt;enumeration value="previousIntersection"/>
 *     &lt;enumeration value="nextIntersection"/>
 *     &lt;enumeration value="adminDivisionChange"/>
 *     &lt;enumeration value="countryChange"/>
 *     &lt;enumeration value="gateAccess"/>
 *     &lt;enumeration value="privateRoad"/>
 *     &lt;enumeration value="tollBooth"/>
 *     &lt;enumeration value="tollRoad"/>
 *     &lt;enumeration value="unpavedRoad"/>
 *     &lt;enumeration value="restrictedTurn"/>
 *     &lt;enumeration value="seasonalClosures"/>
 *     &lt;enumeration value="congestion"/>
 *     &lt;enumeration value="roadworks"/>
 *     &lt;enumeration value="accident"/>
 *     &lt;enumeration value="closure"/>
 *     &lt;enumeration value="trafficFlow"/>
 *     &lt;enumeration value="copyright"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "RouteNoteCodeType")
@XmlEnum
public enum RouteNoteCodeType {


    /**
     * Indicates that routing options have been violoated, i.e. the route contains route features that should have been avoided. The violated routing feature will be provided in the additional data container of the corresponding note.
     * 
     */
    @XmlEnumValue("routingOptionViolated")
    ROUTING_OPTION_VIOLATED("routingOptionViolated"),

    /**
     * Indicates that a special place (city, country border, POI, etc.) will be passed in the segment following the maneuver. The name and type of the place will be provided in the additional data container of the corresponding note.
     * 
     */
    @XmlEnumValue("passingPlace")
    PASSING_PLACE("passingPlace"),

    /**
     * Indicates that road name and/or route number will change at the given position without an additional maneuver taking place. The new street name and/or route number will be provided in the additional data container of the corresponding note.
     * 
     */
    @XmlEnumValue("roadNameChanged")
    ROAD_NAME_CHANGED("roadNameChanged"),

    /**
     * Indicates that a sharp curve is ahead of the maneuver's position.
     * 
     */
    @XmlEnumValue("sharpCurveAhead")
    SHARP_CURVE_AHEAD("sharpCurveAhead"),

    /**
     * Indicates that a special link characteristic will be met after the maneuver. The identifier for the link characteristic (see enumeration type LinkFlag) will be provided in the additional data container of the corresponding note.  
     * 
     */
    @XmlEnumValue("linkFeatureAhead")
    LINK_FEATURE_AHEAD("linkFeatureAhead"),

    /**
     * Indicates time dependent restrictions for the segment following the maneuver, e.g. "road closed in winter". The validity period of this restriction will be provided in the corresponding note.
     * 
     */
    @XmlEnumValue("timeDependentRestriction")
    TIME_DEPENDENT_RESTRICTION("timeDependentRestriction"),

    /**
     * Indicates the name of a previous intersection in a maneuver, e.g. for indicating the last intersection before arrival. The name of the intersection along with a descriptive text will be provided in the Text field of the corresponding note, e.g. "The last intersection is Markkulantie".
     * 
     */
    @XmlEnumValue("previousIntersection")
    PREVIOUS_INTERSECTION("previousIntersection"),

    /**
     * Indicates the name of a next intersection in a maneuver, e.g. for indicating the last intersection before arrival. The name of the intersection along with a descriptive text will be provided in the Text field of the corresponding note, e.g. "If you reach Rantalantie, you've gone too far".
     * 
     */
    @XmlEnumValue("nextIntersection")
    NEXT_INTERSECTION("nextIntersection"),

    /**
     * Indicates that some part of route crosses administrative division border (state, province, etc.)
     * 
     */
    @XmlEnumValue("adminDivisionChange")
    ADMIN_DIVISION_CHANGE("adminDivisionChange"),

    /**
     * Indicates that some part of route crosses country border.
     * 
     */
    @XmlEnumValue("countryChange")
    COUNTRY_CHANGE("countryChange"),

    /**
     * Indicates that some part of route enters or leaves a gated area.
     * 
     */
    @XmlEnumValue("gateAccess")
    GATE_ACCESS("gateAccess"),

    /**
     * Indicates that some part of route runs through a privately owned road.
     * 
     */
    @XmlEnumValue("privateRoad")
    PRIVATE_ROAD("privateRoad"),

    /**
     * Indicates that some part of route may require a stop at a toll booth.
     * 
     */
    @XmlEnumValue("tollBooth")
    TOLL_BOOTH("tollBooth"),

    /**
     * Indicates that some parts of the route are toll roads.
     * 
     */
    @XmlEnumValue("tollRoad")
    TOLL_ROAD("tollRoad"),

    /**
     * Indicates that some part of route is unpaved.
     * 
     */
    @XmlEnumValue("unpavedRoad")
    UNPAVED_ROAD("unpavedRoad"),

    /**
     * Indicates that some parts of the route may be subject to turn restrictions.
     * 
     */
    @XmlEnumValue("restrictedTurn")
    RESTRICTED_TURN("restrictedTurn"),

    /**
     * Indicates that one should expect seasonal closures on some part of route.
     * 
     */
    @XmlEnumValue("seasonalClosures")
    SEASONAL_CLOSURES("seasonalClosures"),

    /**
     * Indicates that one should expect congestions on some part of route.
     * 
     */
    @XmlEnumValue("congestion")
    CONGESTION("congestion"),

    /**
     * Indicates that there are roadworks expected on some part of route.
     * 
     */
    @XmlEnumValue("roadworks")
    ROADWORKS("roadworks"),

    /**
     * Indicates that there has been an accident reported on some part of route.
     * 
     */
    @XmlEnumValue("accident")
    ACCIDENT("accident"),

    /**
     * Indicates that there is a road closure affecting the route.
     * 
     */
    @XmlEnumValue("closure")
    CLOSURE("closure"),

    /**
     * Indicates that there is traffic flow (e.g. sluggish traffic) affecting the route.
     * 
     */
    @XmlEnumValue("trafficFlow")
    TRAFFIC_FLOW("trafficFlow"),

    /**
     * Indicates a RouteNote including copyright information.
     * 
     */
    @XmlEnumValue("copyright")
    COPYRIGHT("copyright");
    private final String value;

    RouteNoteCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RouteNoteCodeType fromValue(String v) {
        for (RouteNoteCodeType c: RouteNoteCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
