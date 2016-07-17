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
package com.henriquemalheiro.trackit.presentation.view.map.provider;

import java.awt.image.BufferedImage;

import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.presentation.view.map.MapTile;
import com.henriquemalheiro.trackit.presentation.view.map.MapType;

public class MilitaryMapsTile implements MapTile {
	private int number;
	private char letter;
	private BoundingBox2<Location> boundingBox;
	private final byte zoom;
	private final MapType mapType;
	private BufferedImage image;
	private boolean imageReady;
	private long priority;
	
	
	public MilitaryMapsTile(int number, char letter, BoundingBox2<Location> boundingBox, byte zoom, MapType mapType) {
		this.number = number;
		this.letter = letter;
		this.boundingBox = boundingBox;
		this.zoom = zoom;
		this.mapType = mapType;
		this.image = null;
		this.imageReady = false;
		this.priority = 0L;
	}
	
	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		imageReady = true;
	}
	
	public boolean isImageReady() {
		return imageReady;
	}
	
	public int getNumber() {
		return number;
	}
	
	public char getLetter() {
		return letter;
	}
	
	public BoundingBox2<Location> getBoundingBox() {
		return boundingBox;
	}
	
	public byte getZoom() {
		return zoom;
	}
	
	public MapType getMapType() {
		return mapType;
	}
	
	public long getPriority() {
		return priority;
	}
	
	public void setPriority(long priority) {
		this.priority = priority;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mapType == null) ? 0 : mapType.hashCode());
		result = prime * result + (int) (number ^ (number >>> 32));
		result = prime * result + zoom;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MilitaryMapsTile other = (MilitaryMapsTile) obj;
		if (mapType != other.mapType)
			return false;
		if (number != other.number)
			return false;
		if (zoom != other.zoom)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[number=" + number + letter + ", zoom=" + zoom + "]";
	}
}
