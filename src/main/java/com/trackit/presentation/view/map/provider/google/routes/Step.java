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
package com.trackit.presentation.view.map.provider.google.routes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="step")
public class Step {
	private String travelMode;
	private StartLocation startLocation;
	private EndLocation endLocation;
	private Polyline polyline;
	private Duration duration;
	private String htmlInstructions;
	private Distance distance;
	private String maneuver;
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="travel_mode")
	public String getTravelMode() {
		return travelMode;
	}
	
	public void setTravelMode(String travelMode) {
		this.travelMode = travelMode;
	}
	
	@XmlElement(name="start_location")
	public StartLocation getStartLocation() {
		return startLocation;
	}
	
	public void setStartLocation(StartLocation startLocation) {
		this.startLocation = startLocation;
	}
	
	@XmlElement(name="end_location")
	public EndLocation getEndLocation() {
		return endLocation;
	}
	
	public void setEndLocation(EndLocation endLocation) {
		this.endLocation = endLocation;
	}
	
	@XmlElement(name="polyline")
	public Polyline getPolyline() {
		return polyline;
	}
	
	public void setPolyline(Polyline polyline) {
		this.polyline = polyline;
	}
	
	@XmlElement(name="duration")
	public Duration getDuration() {
		return duration;
	}
	
	public void setDuration(Duration duration) {
		this.duration = duration;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="html_instructions")
	public String getHtmlInstructions() {
		return htmlInstructions;
	}
	
	public void setHtmlInstructions(String htmlInstructions) {
		this.htmlInstructions = htmlInstructions;
	}
	
	@XmlElement(name="distance")
	public Distance getDistance() {
		return distance;
	}
	
	public void setDistance(Distance distance) {
		this.distance = distance;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="maneuver")
	public String getManeuver() {
		return maneuver;
	}
	
	public void setManeuver(String maneuver) {
		this.maneuver = maneuver;
	}
}