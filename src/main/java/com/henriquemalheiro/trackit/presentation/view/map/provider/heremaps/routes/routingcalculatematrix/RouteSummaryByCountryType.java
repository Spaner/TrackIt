//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:11:02 PM WET 
//


package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculatematrix;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Summary for a country
 * 
 * <p>Java class for RouteSummaryByCountryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RouteSummaryByCountryType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.navteq.com/lbsp/Routing-Common/4}RouteSummaryType">
 *       &lt;sequence>
 *         &lt;element name="Country" type="{http://www.navteq.com/lbsp/Common/4}CountryCodeType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RouteSummaryByCountryType", propOrder = {
    "country"
})
public class RouteSummaryByCountryType
    extends RouteSummaryType
{

    @XmlElement(name = "Country", required = true)
    protected String country;

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

}
