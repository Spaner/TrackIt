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
package com.henriquemalheiro.trackit.presentation.view.map.provider.bingmaps.routes;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="RouteLeg")
public class RouteLeg {
	private List<SubLegSummary> subLegSummaries;
	private double travelDistance;
	private int travelDuration;
	private ActualStart actualStart;
	private ActualEnd actualEnd;
	private StartLocation startLocation;
	private EndLocation endLocation;
	private List<ItineraryItem> itineraryItems;
	private String startTime;
	private String endTime;
	private String description;
	private String routeRegion;
	
	@XmlElementWrapper(name="SubLegSummaries")
	@XmlElement(name="SubLegSummary")
	public List<SubLegSummary> getSubLegSummaries() {
		return subLegSummaries;
	}
	
	public void setSubLegSummaries(List<SubLegSummary> subLegSummaries) {
		this.subLegSummaries = subLegSummaries;
	}
	
	@XmlElement(name="TravelDistance")
	public double getTravelDistance() {
		return travelDistance;
	}
	
	public void setTravelDistance(double travelDistance) {
		this.travelDistance = travelDistance;
	}
	
	@XmlElement(name="TravelDuration")
	public int getTravelDuration() {
		return travelDuration;
	}
	
	public void setTravelDuration(int travelDuration) {
		this.travelDuration = travelDuration;
	}
	
	@XmlElement(name="ActualStart")
	public ActualStart getActualStart() {
		return actualStart;
	}
	
	public void setActualStart(ActualStart actualStart) {
		this.actualStart = actualStart;
	}
	
	@XmlElement(name="ActualEnd")
	public ActualEnd getActualEnd() {
		return actualEnd;
	}
	
	public void setActualEnd(ActualEnd actualEnd) {
		this.actualEnd = actualEnd;
	}
	
	@XmlElement(name="StartLocation")
	public StartLocation getStartLocation() {
		return startLocation;
	}
	
	public void setStartLocation(StartLocation startLocation) {
		this.startLocation = startLocation;
	}
	
	@XmlElement(name="EndLocation")
	public EndLocation getEndLocation() {
		return endLocation;
	}
	
	public void setEndLocation(EndLocation endLocation) {
		this.endLocation = endLocation;
	}
	
	@XmlElementWrapper(name="RouteLeg")
	@XmlElement(name="ItineraryItem")
	public List<ItineraryItem> getItineraryItems() {
		return itineraryItems;
	}
	
	public void setItineraryItems(List<ItineraryItem> itineraryItems) {
		this.itineraryItems = itineraryItems;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="StartTime")
	public String getStartTime() {
		return startTime;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="EndTime")
	public String getEndTime() {
		return endTime;
	}
	
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="Description")
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="RouteRegion")
	public String getRouteRegion() {
		return routeRegion;
	}
	
	public void setRouteRegion(String routeRegion) {
		this.routeRegion = routeRegion;
	}
}