/*
 * This file is part of Track It!.
 * Copyright (C) 2016 João Brisson Lopes
 * 
 * TrackIt! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Track It! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Track It!. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.jb12335.trackit.presentation.view.map.provider.googlemaps.timezone;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="TimeZoneResponse")
public class TimeZoneResponse {
	private String xmlStatus;
	private String rawOffset;
	private String dstOffset;
	private String timezoneID;
	private String timezoneName;
	private String errorMessage;

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="status")
	public String getStatus() {
		return xmlStatus;
	}
	
	public void setStatusCode(String xmlStatus) {
		this.xmlStatus = xmlStatus;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="raw_offset")
	public String getRawOffset() {
		return rawOffset;
	}
	
	public void setRawOffset(String rawOffset) {
		this.rawOffset = rawOffset;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="dst_offset")
	public String getDSTOffset() {
		return dstOffset;
	}
	
	public void setDSTOffset(String dstOffset) {
		this.dstOffset = dstOffset;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="time_zone_id")
	public String getTimezoneID() {
		return timezoneID;
	}
	
	public void setTimezoneID(String timezoneID) {
		this.timezoneID = timezoneID;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="time_zone_name")
	public String getTimezoneName() {
		return timezoneName;
	}
	
	public void setTimezoneName(String timezoneName) {
		this.timezoneName = timezoneName;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="error_message")
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
