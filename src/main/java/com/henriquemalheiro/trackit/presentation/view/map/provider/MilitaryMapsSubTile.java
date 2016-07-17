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

import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.presentation.view.map.MapTile;
import com.henriquemalheiro.trackit.presentation.view.map.MapType;

public class MilitaryMapsSubTile extends MilitaryMapsTile implements MapTile {
	private int x;
	private int y;
	
	public MilitaryMapsSubTile(MilitaryMapsTile masterTile, int x, int y) {
		super(masterTile.getNumber(), masterTile.getLetter(), masterTile.getBoundingBox(), masterTile.getZoom(), masterTile.getMapType());
		this.x = x;
		this.y = y;
	}
	
	public MilitaryMapsSubTile(MilitaryMapsSubTile originalTile, int x, int y) {
		super(originalTile.getNumber(), originalTile.getLetter(), originalTile.getBoundingBox(), originalTile.getZoom(), originalTile.getMapType());
		this.x = x;
		this.y = y;
	}
	
	public MilitaryMapsSubTile(int number, char letter, int x, int y, BoundingBox2<Location> boundingBox, byte zoom, MapType mapType) {
		super(number, letter, boundingBox, zoom, mapType);
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MilitaryMapsSubTile other = (MilitaryMapsSubTile) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (getZoom() != other.getZoom())
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + "\n" + "[x=" + x + ", y=" + y + "]";
	}
}
