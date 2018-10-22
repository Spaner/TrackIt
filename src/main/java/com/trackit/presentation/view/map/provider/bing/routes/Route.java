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

@XmlRootElement(name="Route")
public class Route {
	private String id;
	private BoundingBox boundingBox;
	private String distanceUnit;
	private String durationUnit;
	private double travelDistance;
	private int travelDuration;
	private int travelDurationTraffic;
	private RouteLeg routeLeg;
	private RoutePath routePath;
	
    @XmlElement(name="Id")
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlElement(name="BoundingBox")
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	
	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="DistanceUnit")
	public String getDistanceUnit() {
		return distanceUnit;
	}
	
	public void setDistanceUnit(String distanceUnit) {
		this.distanceUnit = distanceUnit;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="DurationUnit")
	public String getDurationUnit() {
		return durationUnit;
	}

	public void setDurationUnit(String durationUnit) {
		this.durationUnit = durationUnit;
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

	@XmlElement(name="TravelDurationTraffic")
	public int getTravelDurationTraffic() {
		return travelDurationTraffic;
	}

	public void setTravelDurationTraffic(int travelDurationTraffic) {
		this.travelDurationTraffic = travelDurationTraffic;
	}

	@XmlElement(name="RouteLeg")
	public RouteLeg getRouteLeg() {
		return routeLeg;
	}

	public void setRouteLeg(RouteLeg routeLeg) {
		this.routeLeg = routeLeg;
	}

	@XmlElement(name="RoutePath")
	public RoutePath getRoutePath() {
		return routePath;
	}

	public void setRoutePath(RoutePath routePath) {
		this.routePath = routePath;
	}
}