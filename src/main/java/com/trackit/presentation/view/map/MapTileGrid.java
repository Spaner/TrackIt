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

import java.util.Arrays;

public class MapTileGrid {
	MapTile[][] grid;
	long xOffset;
	long yOffset;
	long width;
	long height;
	
	public MapTileGrid(long width, long height) {
		this.grid = new MapTile[(int) width][(int) height];
		this.xOffset = 0;
		this.yOffset = 0;
		this.width = width;
		this.height = height;
	}
	
	public MapTileGrid(MapTile[][] grid, long xOffset, long yOffset, long width, long height) {
		this.grid = grid;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.width = width;
		this.height = height;
	}

	public MapTile[][] getGrid() {
		return grid;
	}

	public void setGrid(MapTile[][] grid) {
		this.grid = grid;
	}
	
	public long getXOffset() {
		return xOffset;
	}

	public void setXOffset(long xOffset) {
		this.xOffset = xOffset;
	}

	public long getYOffset() {
		return yOffset;
	}

	public void setYOffset(long yOffset) {
		this.yOffset = yOffset;
	}

	public long getWidth() {
		return width;
	}

	public void setWidth(long width) {
		this.width = width;
	}

	public long getHeight() {
		return height;
	}

	public void setHeight(long height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return "MapTileGrid [grid=" + Arrays.toString(grid) + ", xOffset="
				+ xOffset + ", yOffset=" + yOffset + ", width=" + width
				+ ", height=" + height + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(grid);
		result = prime * result + (int) (height ^ (height >>> 32));
		result = prime * result + (int) (width ^ (width >>> 32));
		result = prime * result + (int) (xOffset ^ (xOffset >>> 32));
		result = prime * result + (int) (yOffset ^ (yOffset >>> 32));
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
		MapTileGrid other = (MapTileGrid) obj;
		if (!Arrays.equals(grid, other.grid))
			return false;
		if (height != other.height)
			return false;
		if (width != other.width)
			return false;
		if (xOffset != other.xOffset)
			return false;
		if (yOffset != other.yOffset)
			return false;
		return true;
	}
}
