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

@XmlRootElement(name="SubLegSummary")
public class SubLegSummary {
	private double travelDistance;
	private int travelDuration;
	private int travelDurationTraffic;
	private StartWaypoint startWaypoint;
	private EndWaypoint endWaypoint;
	
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
	
	@XmlElement(name="StartWaypoint")
	public StartWaypoint getStartWaypoint() {
		return startWaypoint;
	}

	public void setStartWaypoint(StartWaypoint startWaypoint) {
		this.startWaypoint = startWaypoint;
	}
	
	@XmlElement(name="EndWaypoint")
	public EndWaypoint getEndWaypoint() {
		return endWaypoint;
	}
	
	public void setEndWaypoint(EndWaypoint endWaypoint) {
		this.endWaypoint = endWaypoint;
	}
}