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

@XmlRootElement(name="Detail")
public class Detail {
	private String maneuverType;
	private int startPathIndex;
	private int endPathIndex;
	private int compassDegrees;
	private String mode;
	private int previousEntityId;
	private int nextEntityId;
	private String roadType;

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="ManeuverType")
	public String getManeuverType() {
		return maneuverType;
	}

	public void setManeuverType(String maneuverType) {
		this.maneuverType = maneuverType;
	}

    @XmlElement(name="StartPathIndex")
	public int getStartPathIndex() {
		return startPathIndex;
	}

	public void setStartPathIndex(int startPathIndex) {
		this.startPathIndex = startPathIndex;
	}

    @XmlElement(name="EndPathIndex")
	public int getEndPathIndex() {
		return endPathIndex;
	}

	public void setEndPathIndex(int endPathIndex) {
		this.endPathIndex = endPathIndex;
	}

    @XmlElement(name="CompassDegrees")
	public int getCompassDegrees() {
		return compassDegrees;
	}

	public void setCompassDegrees(int compassDegrees) {
		this.compassDegrees = compassDegrees;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="Mode")
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

    @XmlElement(name="PreviousEntityId")
	public int getPreviousEntityId() {
		return previousEntityId;
	}

	public void setPreviousEntityId(int previousEntityId) {
		this.previousEntityId = previousEntityId;
	}

    @XmlElement(name="NextEntityId")
	public int getNextEntityId() {
		return nextEntityId;
	}

	public void setNextEntityId(int nextEntityId) {
		this.nextEntityId = nextEntityId;
	}

	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="RoadType")
	public String getRoadType() {
		return roadType;
	}

	public void setRoadType(String roadType) {
		this.roadType = roadType;
	}
}