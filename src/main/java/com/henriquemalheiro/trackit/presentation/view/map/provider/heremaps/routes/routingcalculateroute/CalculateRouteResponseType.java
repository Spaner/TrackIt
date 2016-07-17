//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:11:32 PM WET 
//


package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculateroute;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <b>CalculateRouteResponse</b> is the data structure for the responses from the CalculateRoute service.  A <b>CalculateRouteResponse</b> element always corresponds to a request of type <b>CalculateRouteRequest</b>.
 * 
 * <p>Java class for CalculateRouteResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CalculateRouteResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MetaInfo" type="{http://www.navteq.com/lbsp/Routing-Common/4}RouteResponseMetaInfoType"/>
 *         &lt;element name="Route" type="{http://www.navteq.com/lbsp/Routing-Common/4}RouteType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Language" type="{http://www.navteq.com/lbsp/Common/4}LanguageCodeParameterType" minOccurs="0"/>
 *         &lt;element name="SourceAttribution" type="{http://www.navteq.com/lbsp/Routing-Common/4}SourceAttributionType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Response", namespace = "http://www.navteq.com/lbsp/Routing-CalculateRoute/4", propOrder = {
    "metaInfo",
    "route",
    "language",
    "sourceAttribution"
})
public class CalculateRouteResponseType {

    @XmlElement(name = "MetaInfo", required = true)
    protected RouteResponseMetaInfoType metaInfo;
    @XmlElement(name = "Route")
    protected List<RouteType> route;
    @XmlElement(name = "Language")
    protected String language;
    @XmlElement(name = "SourceAttribution")
    protected SourceAttributionType sourceAttribution;

    /**
     * Gets the value of the metaInfo property.
     * 
     * @return
     *     possible object is
     *     {@link RouteResponseMetaInfoType }
     *     
     */
    public RouteResponseMetaInfoType getMetaInfo() {
        return metaInfo;
    }

    /**
     * Sets the value of the metaInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link RouteResponseMetaInfoType }
     *     
     */
    public void setMetaInfo(RouteResponseMetaInfoType value) {
        this.metaInfo = value;
    }

    /**
     * Gets the value of the route property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the route property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRoute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RouteType }
     * 
     * 
     */
    public List<RouteType> getRoute() {
        if (route == null) {
            route = new ArrayList<RouteType>();
        }
        return this.route;
    }

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguage(String value) {
        this.language = value;
    }

    /**
     * Gets the value of the sourceAttribution property.
     * 
     * @return
     *     possible object is
     *     {@link SourceAttributionType }
     *     
     */
    public SourceAttributionType getSourceAttribution() {
        return sourceAttribution;
    }

    /**
     * Sets the value of the sourceAttribution property.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceAttributionType }
     *     
     */
    public void setSourceAttribution(SourceAttributionType value) {
        this.sourceAttribution = value;
    }

}
