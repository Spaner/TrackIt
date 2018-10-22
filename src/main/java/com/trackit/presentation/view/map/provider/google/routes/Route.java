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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="route")
public class Route {
	private String summary;
	private List<Leg> legs;
	private String copyrights;
	private OverviewPolyline overviewPolyline;
	private Bounds bounds;
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="summary")
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	@XmlElement(name="leg", type=Leg.class)
	public List<Leg> getLegs() {
		if (legs == null) {
			legs = new ArrayList<Leg>();
		}
		return legs;
	}
	
	public void setLegs(List<Leg> legs) {
		this.legs = legs;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlElement(name="copyrights")
	public String getCopyrights() {
		return copyrights;
	}
	
	public void setCopyrights(String copyrights) {
		this.copyrights = copyrights;
	}
	
	@XmlElement(name="overview_polyline")
	public OverviewPolyline getOverviewPolyline() {
		return overviewPolyline;
	}

	public void setOverviewPolyline(OverviewPolyline overviewPolyline) {
		this.overviewPolyline = overviewPolyline;
	}
	
	@XmlElement(name="bounds")
	public Bounds getBounds() {
		return bounds;
	}

	public void setBounds(Bounds bounds) {
		this.bounds = bounds;
	}
}