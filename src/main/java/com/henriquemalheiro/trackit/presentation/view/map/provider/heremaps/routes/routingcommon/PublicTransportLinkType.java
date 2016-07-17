//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:12:22 PM WET 
//


package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcommon;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A public transport link is a link traversed in a route using public transport.
 * 
 * <p>Java class for PublicTransportLinkType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PublicTransportLinkType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.navteq.com/lbsp/Routing-Common/4}RouteLinkType">
 *       &lt;sequence>
 *         &lt;element name="NextStopName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Line" type="{http://www.navteq.com/lbsp/Common/4}ElementReferenceType" minOccurs="0"/>
 *         &lt;element name="NextStopId" type="{http://www.navteq.com/lbsp/Common/4}TransitStopIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicTransportLinkType", propOrder = {
    "nextStopName",
    "line",
    "nextStopId"
})
public class PublicTransportLinkType
    extends RouteLinkType
{

    @XmlElement(name = "NextStopName")
    protected String nextStopName;
    @XmlElement(name = "Line")
    protected String line;
    @XmlElement(name = "NextStopId")
    protected String nextStopId;

    /**
     * Gets the value of the nextStopName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNextStopName() {
        return nextStopName;
    }

    /**
     * Sets the value of the nextStopName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNextStopName(String value) {
        this.nextStopName = value;
    }

    /**
     * Gets the value of the line property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLine() {
        return line;
    }

    /**
     * Sets the value of the line property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLine(String value) {
        this.line = value;
    }

    /**
     * Gets the value of the nextStopId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNextStopId() {
        return nextStopId;
    }

    /**
     * Sets the value of the nextStopId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNextStopId(String value) {
        this.nextStopId = value;
    }

}
