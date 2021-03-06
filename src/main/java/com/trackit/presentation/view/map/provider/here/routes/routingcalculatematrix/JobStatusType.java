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
 * <p>Java class for JobStatusType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="JobStatusType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="submitted"/>
 *     &lt;enumeration value="accepted"/>
 *     &lt;enumeration value="running"/>
 *     &lt;enumeration value="completed"/>
 *     &lt;enumeration value="failed"/>
 *     &lt;enumeration value="cancelled"/>
 *     &lt;enumeration value="deleted"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "JobStatusType", namespace = "http://www.navteq.com/lbsp/Common/4")
@XmlEnum
public enum JobStatusType {


    /**
     * Indicates that a job has been submitted, but is not yet running.
     * 
     */
    @XmlEnumValue("submitted")
    SUBMITTED("submitted"),

    /**
     * Indicates that a job has been saved in the job queue and waits for execution.
     * 
     */
    @XmlEnumValue("accepted")
    ACCEPTED("accepted"),

    /**
     * Indicates that the job is currently being processed.
     * 
     */
    @XmlEnumValue("running")
    RUNNING("running"),

    /**
     * Indicates that the job has been completed (with or without errors).
     * 
     */
    @XmlEnumValue("completed")
    COMPLETED("completed"),

    /**
     * Indicates that the job has failed.
     * 
     */
    @XmlEnumValue("failed")
    FAILED("failed"),

    /**
     * Indicates that the job has been cancelled by the user.
     * 
     */
    @XmlEnumValue("cancelled")
    CANCELLED("cancelled"),

    /**
     * Indicates that the job has been deleted.
     * 
     */
    @XmlEnumValue("deleted")
    DELETED("deleted");
    private final String value;

    JobStatusType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static JobStatusType fromValue(String v) {
        for (JobStatusType c: JobStatusType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
