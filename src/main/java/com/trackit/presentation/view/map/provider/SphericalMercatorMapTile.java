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
package com.trackit.presentation.view.map.provider;

import java.awt.image.BufferedImage;

import com.trackit.presentation.view.map.MapTile;
import com.trackit.presentation.view.map.MapType;

public class SphericalMercatorMapTile implements MapTile {
	private long x;
	private long y;
	private final byte zoom;
	private final MapType mapType;
	private BufferedImage image;
	private boolean imageReady;
	private long priority;
	
	
	public SphericalMercatorMapTile(long x, long y, byte zoom, MapType mapType) {
		this.x = x;
		this.y = y;
		this.zoom = zoom;
		this.mapType = mapType;
		this.imageReady = false;
		this.priority = 0L;
	}
	
	public byte getZoom() {
		return zoom;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		imageReady = (image != null) ? true : false;
	}
	
	public boolean isImageReady() {
		return imageReady;
	}
	
	public long getX() {
		return x;
	}
	
	public long getY() {
		return y;
	}
	
	//12335: 2018-05-26
	public void invertY() {
		y = (1 << zoom) - 1 -y;
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
		result = prime * result + (int) (x ^ (x >>> 32));
		result = prime * result + (int) (y ^ (y >>> 32));
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
		SphericalMercatorMapTile other = (SphericalMercatorMapTile) obj;
		if (mapType != other.mapType)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (zoom != other.zoom)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + ", zoom=" + zoom + ", type=" + mapType + "]";
	}
	
	public String toQuadkey() {
        StringBuilder quadKey = new StringBuilder();
        
        for (int i = zoom; i > 0; i--) {
        	char digit = '0';
            int mask = 1 << (i - 1);
            
            if ((x & mask) != 0) {
                digit++;
            }
            
            if ((y & mask) != 0) {
                digit += 2;
            }
            
            quadKey.append(digit);
        }
        
        return quadKey.toString();
	}
}
