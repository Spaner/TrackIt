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
package com.trackit.presentation.view.map;

public class MapInfo {
	private Double pointerLatitude;
	private Double pointerLongitude;
	private Integer pointerX;
	private Integer pointerY;
	private Byte zoom;
	private Double centerLongitude;
	private Double centerLatitude;
	
	public MapInfo() {
	}

	public Double getPointerLatitude() {
		return pointerLatitude;
	}

	public void setPointerLatitude(Double pointerLatitude) {
		this.pointerLatitude = pointerLatitude;
	}

	public Double getPointerLongitude() {
		return pointerLongitude;
	}

	public void setPointerLongitude(Double pointerLongitude) {
		this.pointerLongitude = pointerLongitude;
	}

	public Integer getPointerX() {
		return pointerX;
	}

	public void setPointerX(Integer pointerX) {
		this.pointerX = pointerX;
	}

	public Integer getPointerY() {
		return pointerY;
	}

	public void setPointerY(Integer pointerY) {
		this.pointerY = pointerY;
	}
	
	public Byte getZoom() {
		return zoom;
	}

	public void setZoom(Byte zoom) {
		this.zoom = zoom;
	}
	
	public Double getCenterLatitude() {
		return centerLatitude;
	}

	public void setCenterLatitude(Double centerLatitude) {
		this.centerLatitude = centerLatitude;
	}
	
	public Double getCenterLongitude() {
		return centerLongitude;
	}

	public void setCenterLongitude(Double centerLongitude) {
		this.centerLongitude = centerLongitude;
	}
}
