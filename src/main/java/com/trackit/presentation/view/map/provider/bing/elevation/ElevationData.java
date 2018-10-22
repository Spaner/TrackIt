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
package com.trackit.presentation.view.map.provider.bing.elevation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ElevationData")
public class ElevationData {
	private int zoomLevel;
	private List<Integer> elevations;
	
	@XmlElement(name="ZoomLevel")
	public int getZoomLevel() {
		return zoomLevel;
	}
	
	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}
	
	@XmlElementWrapper(name="Elevations")
	@XmlElement(name="int")
	public List<Integer> getElevations() {
		if (elevations == null) {
			elevations = new ArrayList<Integer>();
		}
		
		return elevations;
	}
	
	public void setElevations(List<Integer> elevations) {
		this.elevations = elevations;
	}
	
	@Override
	public String toString() {
		return "ElevationData [zoomLevel=" + zoomLevel + ", elevations="
				+ elevations + "]";
	}
}
