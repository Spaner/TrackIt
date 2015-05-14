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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="EndWaypoint")
public class EndWaypoint {
	private Coordinate coordinate;
	private String description;
	private boolean isVia;
	private String locationIdentifier;
	private int routePathIndex;
	
	@XmlElement(name="Coordinate")
	public Coordinate getCoordinate() {
		return coordinate;
	}
	
	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="Description")
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlElement(name="IsVia")
	public boolean isVia() {
		return isVia;
	}
	
	public void setVia(boolean isVia) {
		this.isVia = isVia;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="LocationIdentifier")
	public String getLocationIdentifier() {
		return locationIdentifier;
	}
	
	public void setLocationIdentifier(String locationIdentifier) {
		this.locationIdentifier = locationIdentifier;
	}
	
	@XmlElement(name="RoutePathIndex")
	public int getRoutePathIndex() {
		return routePathIndex;
	}
	
	public void setRoutePathIndex(int routePathIndex) {
		this.routePathIndex = routePathIndex;
	}
}
