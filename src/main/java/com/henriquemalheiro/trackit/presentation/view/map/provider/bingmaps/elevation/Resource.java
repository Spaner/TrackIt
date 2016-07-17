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
package com.henriquemalheiro.trackit.presentation.view.map.provider.bingmaps.elevation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Resource")
public class Resource {
	private List<ElevationData> elevationData;
	
	@XmlElement(name="ElevationData")
	public List<ElevationData> getElevationData() {
		if (elevationData == null) {
			elevationData = new ArrayList<ElevationData>();
		}
		
		return elevationData;
	}
	
	public void setElevationData(List<ElevationData> elevationData) {
		this.elevationData = elevationData;
	}

	@Override
	public String toString() {
		return "Resource [elevationData=" + elevationData + "]";
	}
}