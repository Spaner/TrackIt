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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="leg")
public class Leg {
	private List<Step> steps;
	private Duration duration;
	private Distance distance;
	private StartLocation startLocation;
	private EndLocation endLocation;
	private String startAddress;
	private String endAddress;

	@XmlElement(name="step", type=Step.class)
	public List<Step> getSteps() {
		return steps;
	}
	
	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}
	
	@XmlElement(name="duration")
	public Duration getDuration() {
		return duration;
	}
	
	public void setDuration(Duration duration) {
		this.duration = duration;
	}
	
	@XmlElement(name="distance")
	public Distance getDistance() {
		return distance;
	}
	
	public void setDistance(Distance distance) {
		this.distance = distance;
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
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="start_address")
	public String getStartAddress() {
		return startAddress;
	}
	
	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="end_address")
	public String getEndAddress() {
		return endAddress;
	}
	
	public void setEndAddress(String endAddress) {
		this.endAddress = endAddress;
	}
}