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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="ItineraryItem")
public class ItineraryItem {
	private String travelMode;
	private double travelDistance;
	private int travelDuration;
	private ManeuverPoint maneuverPoint;
	private String instruction;
	private String compassDirection;
	private Detail detail;
	private String exit;
	private String tollZone;
	private String TransitTerminus;
	private int tripId;
	private String iconType;
	private String time;
	private int transitStopId;
	private String towardsRoadName;
	private String sideOfStreet;

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="TravelMode")
	public String getTravelMode() {
		return travelMode;
	}

	public void setTravelMode(String travelMode) {
		this.travelMode = travelMode;
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

    @XmlElement(name="ManeuverPoint")
	public ManeuverPoint getManeuverPoint() {
		return maneuverPoint;
	}

	public void setManeuverPoint(ManeuverPoint maneuverPoint) {
		this.maneuverPoint = maneuverPoint;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="Instruction")
	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="CompassDirection")
	public String getCompassDirection() {
		return compassDirection;
	}

	public void setCompassDirection(String compassDirection) {
		this.compassDirection = compassDirection;
	}

    @XmlElement(name="Detail")
	public Detail getDetail() {
		return detail;
	}

	public void setDetail(Detail detail) {
		this.detail = detail;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="Exit")
	public String getExit() {
		return exit;
	}

	public void setExit(String exit) {
		this.exit = exit;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="TollZone")
	public String getTollZone() {
		return tollZone;
	}

	public void setTollZone(String tollZone) {
		this.tollZone = tollZone;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="TransitTerminus")
	public String getTransitTerminus() {
		return TransitTerminus;
	}

	public void setTransitTerminus(String transitTerminus) {
		TransitTerminus = transitTerminus;
	}

    @XmlElement(name="TripId")
	public int getTripId() {
		return tripId;
	}

	public void setTripId(int tripId) {
		this.tripId = tripId;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="IconType")
	public String getIconType() {
		return iconType;
	}

	public void setIconType(String iconType) {
		this.iconType = iconType;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="Time")
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

    @XmlElement(name="TransitStopId")
	public int getTransitStopId() {
		return transitStopId;
	}

	public void setTransitStopId(int transitStopId) {
		this.transitStopId = transitStopId;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="TowardsRoadName")
	public String getTowardsRoadName() {
		return towardsRoadName;
	}

	public void setTowardsRoadName(String towardsRoadName) {
		this.towardsRoadName = towardsRoadName;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="SideOfStreet")
	public String getSideOfStreet() {
		return sideOfStreet;
	}

	public void setSideOfStreet(String sideOfStreet) {
		this.sideOfStreet = sideOfStreet;
	}
}