/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
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
package com.trackit.presentation.view.map.provider.bing.routes;

import java.awt.Point;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="EndLocation")
public class EndLocation {
	private String name;
	private Point point;
	private BoundingBox boundingBox;
	private String entityType;
	private Address address;
	private String confidence;
	private String matchCode;
	private GeocodePoint geocodePoint;
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="Name")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="Point")
	public Point getPoint() {
		return point;
	}
	
	public void setPoint(Point point) {
		this.point = point;
	}
	
	@XmlElement(name="BoundingBox")
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	
	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}
	
	@XmlElement(name="EntityType")
	public String getEntityType() {
		return entityType;
	}
	
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	@XmlElement(name="Address")
	public Address getAddress() {
		return address;
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="Confidence")
	public String getConfidence() {
		return confidence;
	}
	
	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="MatchCode")
	public String getMatchCode() {
		return matchCode;
	}
	
	public void setMatchCode(String matchCode) {
		this.matchCode = matchCode;
	}
	
	@XmlElement(name="GeocodePoint")
	public GeocodePoint getGeocodePoint() {
		return geocodePoint;
	}
	
	public void setGeocodePoint(GeocodePoint geocodePoint) {
		this.geocodePoint = geocodePoint;
	}
}
