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

import com.trackit.business.common.TrackItTask;
import com.trackit.business.utilities.ThreadPool;
import com.trackit.presentation.view.map.MapTile;

public class MapTileCache {
	private MapTile[] cache;
	private int pos;
	
	private MapProvider mapProvider;
	
	public MapTileCache(int capacity, MapProvider mapProvider) {
		this.mapProvider = mapProvider;
		
		this.cache = new MapTile[capacity];
		pos = 0;
	}
	
	public MapTile getTile(final MapTile mapTile) {
		if (mapTile == null) {
			return null;
		}
		
		for (MapTile cachedTile : cache) {
			if (cachedTile != null && cachedTile.equals(mapTile)) {
				return cachedTile;
			}
		}
		
		ThreadPool.enqueueTask(new TrackItTask(new Runnable() {
			public void run() {
				mapProvider.fetchTileImage(mapTile);
			}
		}, mapTile.getPriority()));
		
		cache[pos] = mapTile;
		pos = (pos + 1) % cache.length;
		
		return mapTile;
	}
	
	public void flush() {
		for (int i = 0; i < cache.length; i++) {
			if (cache[i] != null) {
				if (cache[i].getImage() != null) {
					cache[i].getImage().flush();
					cache[i].setImage(null);
				}
				cache[i] = null;
			}
		}
		pos = 0;
	}
}