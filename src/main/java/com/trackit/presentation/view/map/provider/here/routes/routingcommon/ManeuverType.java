//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.29 at 05:12:22 PM WET 
//


package com.trackit.presentation.view.map.provider.here.routes.routingcommon;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * A maneuver describes the action needed to leave a street segment and enter the next link following the route. This type is an abstract base class for PrivateTransportManeuver and PublicTransportManeuver and only includes most common attributes which every maneuver type provides.
 * 
 * <p>Java class for ManeuverType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ManeuverType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Position" type="{http://www.navteq.com/lbsp/Common/4}GeoCoordinateType" minOccurs="0"/>
 *         &lt;element name="Instruction" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PlaceEquipment" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;list itemType="{http://www.navteq.com/lbsp/Routing-Common/4}PlaceEquipmentType" />
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TravelTime" type="{http://www.navteq.com/lbsp/Common/4}DurationType" minOccurs="0"/>
 *         &lt;element name="Length" type="{http://www.navteq.com/lbsp/Common/4}DistanceType" minOccurs="0"/>
 *         &lt;element name="Shape" type="{http://www.navteq.com/lbsp/Common/4}GeoPolylineType" minOccurs="0"/>
 *         &lt;sequence>
 *           &lt;element name="FirstPoint" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *           &lt;element name="LastPoint" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element name="Time" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Note" type="{http://www.navteq.com/lbsp/Routing-Common/4}RouteNoteType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="NextManeuver" type="{http://www.navteq.com/lbsp/Common/4}ElementReferenceType" minOccurs="0"/>
 *         &lt;element name="ToLink" type="{http://www.navteq.com/lbsp/Common/4}LinkIdType" minOccurs="0"/>
 *         &lt;element name="BoundingBox" type="{http://www.navteq.com/lbsp/Common/4}GeoBoundingBoxType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.navteq.com/lbsp/Common/4}ElementReferenceType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ManeuverType", propOrder = {
    "position",
    "instruction",
    "placeEquipment",
    "travelTime",
    "length",
    "shape",
    "firstPoint",
    "lastPoint",
    "time",
    "note",
    "nextManeuver",
    "toLink",
    "boundingBox"
})
@XmlSeeAlso({
    PrivateTransportManeuverType.class,
    PublicTransportManeuverType.class
})
public abstract class ManeuverType {

    @XmlElement(name = "Position")
    protected GeoCoordinateType position;
    @XmlElement(name = "Instruction", required = true)
    protected String instruction;
    @XmlList
    @XmlElement(name = "PlaceEquipment")
    protected List<PlaceEquipmentType> placeEquipment;
    @XmlElement(name = "TravelTime")
    protected Double travelTime;
    @XmlElement(name = "Length")
    protected Double length;
    @XmlList
    @XmlElement(name = "Shape")
    protected List<String> shape;
    @XmlElement(name = "FirstPoint")
    protected Integer firstPoint;
    @XmlElement(name = "LastPoint")
    protected Integer lastPoint;
    @XmlElement(name = "Time")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    @XmlElement(name = "Note")
    protected List<RouteNoteType> note;
    @XmlElement(name = "NextManeuver")
    protected String nextManeuver;
    @XmlElement(name = "ToLink")
    protected String toLink;
    @XmlElement(name = "BoundingBox")
    protected GeoBoundingBoxType boundingBox;
    @XmlAttribute(name = "id", required = true)
    protected String id;

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link GeoCoordinateType }
     *     
     */
    public GeoCoordinateType getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeoCoordinateType }
     *     
     */
    public void setPosition(GeoCoordinateType value) {
        this.position = value;
    }

    /**
     * Gets the value of the instruction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * Sets the value of the instruction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstruction(String value) {
        this.instruction = value;
    }

    /**
     * Gets the value of the placeEquipment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the placeEquipment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlaceEquipment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PlaceEquipmentType }
     * 
     * 
     */
    public List<PlaceEquipmentType> getPlaceEquipment() {
        if (placeEquipment == null) {
            placeEquipment = new ArrayList<PlaceEquipmentType>();
        }
        return this.placeEquipment;
    }

    /**
     * Gets the value of the travelTime property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTravelTime() {
        return travelTime;
    }

    /**
     * Sets the value of the travelTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTravelTime(Double value) {
        this.travelTime = value;
    }

    /**
     * Gets the value of the length property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLength(Double value) {
        this.length = value;
    }

    /**
     * Gets the value of the shape property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shape property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getShape().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getShape() {
        if (shape == null) {
            shape = new ArrayList<String>();
        }
        return this.shape;
    }

    /**
     * Gets the value of the firstPoint property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFirstPoint() {
        return firstPoint;
    }

    /**
     * Sets the value of the firstPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFirstPoint(Integer value) {
        this.firstPoint = value;
    }

    /**
     * Gets the value of the lastPoint property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLastPoint() {
        return lastPoint;
    }

    /**
     * Sets the value of the lastPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLastPoint(Integer value) {
        this.lastPoint = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTime(XMLGregorianCalendar value) {
        this.time = value;
    }

    /**
     * Gets the value of the note property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the note property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNote().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RouteNoteType }
     * 
     * 
     */
    public List<RouteNoteType> getNote() {
        if (note == null) {
            note = new ArrayList<RouteNoteType>();
        }
        return this.note;
    }

    /**
     * Gets the value of the nextManeuver property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNextManeuver() {
        return nextManeuver;
    }

    /**
     * Sets the value of the nextManeuver property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNextManeuver(String value) {
        this.nextManeuver = value;
    }

    /**
     * Gets the value of the toLink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToLink() {
        return toLink;
    }

    /**
     * Sets the value of the toLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToLink(String value) {
        this.toLink = value;
    }

    /**
     * Gets the value of the boundingBox property.
     * 
     * @return
     *     possible object is
     *     {@link GeoBoundingBoxType }
     *     
     */
    public GeoBoundingBoxType getBoundingBox() {
        return boundingBox;
    }

    /**
     * Sets the value of the boundingBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeoBoundingBoxType }
     *     
     */
    public void setBoundingBox(GeoBoundingBoxType value) {
        this.boundingBox = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
