//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:12:49 PM WET 
//


package com.trackit.presentation.view.map.provider.here.routes.routingversion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The NLP Routing Service returns a response element as answer to a verion request.
 * 
 * <p>Java class for VersionResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VersionResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MetaInfo" type="{http://www.navteq.com/lbsp/Routing-Version/1}VersionResponseMetaInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VersionResponseType", propOrder = {
    "metaInfo"
})
public class VersionResponseType {

    @XmlElement(name = "MetaInfo", required = true)
    protected VersionResponseMetaInfoType metaInfo;

    /**
     * Gets the value of the metaInfo property.
     * 
     * @return
     *     possible object is
     *     {@link VersionResponseMetaInfoType }
     *     
     */
    public VersionResponseMetaInfoType getMetaInfo() {
        return metaInfo;
    }

    /**
     * Sets the value of the metaInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link VersionResponseMetaInfoType }
     *     
     */
    public void setMetaInfo(VersionResponseMetaInfoType value) {
        this.metaInfo = value;
    }

}