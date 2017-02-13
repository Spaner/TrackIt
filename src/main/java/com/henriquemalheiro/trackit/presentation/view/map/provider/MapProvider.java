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

import java.util.Map;

import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.exception.MaxZoomExceededException;
import com.henriquemalheiro.trackit.business.exception.MinZoomExceededException;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.view.map.MapTile;
import com.henriquemalheiro.trackit.presentation.view.map.MapTileGrid;
import com.henriquemalheiro.trackit.presentation.view.map.MapType;
import com.henriquemalheiro.trackit.presentation.view.map.PanDirection;


public interface MapProvider {
	
	/**
	 * Move the center location by the specified offsets.
	 * 
	 * @param xOffset the offset in pixels, in the horizontal direction
	 * @param yOffset the offset in pixels, in the vertical direction
	 * @param width the screen's width
	 * @param height the screen's height
	 */
	public void moveCenterLocation(int xOffset, int yOffset, int width, int height);
	
	/**
	 * Move the center location by the specified location.
	 * 
	 * @param location the new center location
	 * @param width the screen's width
	 * @param height the screen's height
	 */
	public void moveCenterLocation(Location location, int width, int height);
	
	/**
	 * Get the offset in pixels from a location to the center location, in both directions.
	 * 
	 * @param location the location
	 * @return the offset in pixels to the center location, in both directions, x and y
	 */
	public Pair<Integer, Integer> getCenterOffsetInPixels(Location location);
	public int[] getCenterOffsetInPixels(double longitude, double latitude);
	
	/**
	 * Gets a grid of map tiles, filling the specified width and height.
	 * 
	 * @param width the desired width
	 * @param height the desired height
	 * @return the grid of map tiles
	 */
	public MapTileGrid getMapTileGrid(long width, long height);
	
	/**
	 * Get the current center location.
	 *  
	 * @return the current center location
	 */
	public Location getCenterLocation();
	
	/**
	 * Get the location at the screen coordinates.
	 *  
	 * @param x the screen x coordinate
	 * @param y the screen y coordinate
	 * @param width the screen's width
	 * @param height the screen's height
	 * 
	 * @return the location at the screen coordinates
	 */
	public Location getLocation(int x, int y, int width, int height);
	
	/**
	 * Get the distance on the ground that is represented by a single pixel on the map.
	 * 
	 * @param width the screen's width
	 * 
	 * @return the ground resolution at the given latitude and zoom level
	 */
	public double getGroundResolution(int width);
	
	/**
	 * Get the bounding box for a rectangular screen selection.
	 * 
	 * @param x the top left horizontal coordinate
	 * @param y the top left vertical coordinate
	 * @param width the width of the selection
	 * @param height the height of the selection
	 * 
	 * @return the bounding box for the given rectangular screen selection
	 */
//	public BoundingBox<Double> getBoundingBox(int x, int y, int width, int height);
	
	/**
	 * Get the current zoom level.
	 *  
	 * @return the current zoom level
	 */
	public byte getZoom();
	
	/**
	 * Set the zoom level to the specified zoom.
	 * 
	 * @param the desired zoom level
	 */
	public void setZoom(byte zoom);
	
	/**
	 * Set the zoom level for the specified ground resolution.
	 * 
	 * @param groundResolution the desired ground resolution
	 * @param width the screen's width
	 */
	public void setZoom(double groundResolution, int width);
	
	/**
	 * Set the zoom level for the specified bounding box.
	 * 
	 * @param boundingBox the desired bounding box
	 * @param width the screen's width
	 * @param height the screen's height
	 */
	public void setZoom(BoundingBox2<Location> boundingBox, int width, int height);
	
	/**
	 * Get the minimum zoom level.
	 *  
	 * @return the minimum zoom level
	 */
	public byte getMinZoom();
	
	/**
	 * Get the maximum zoom level.
	 *  
	 * @return the maximum zoom level
	 */
	public byte getMaxZoom();
	
	/**
	 * Increase the zoom level, not exceeding the maximum zoom level.
	 * 
	 * @param width the screen's width
	 * @param height the screen's height
	 * 
	 * @throws MaxZoomExceededException
	 */
	public void increaseZoom(int width, int height) throws MaxZoomExceededException;

	/**
	 * Decrease the zoom level, not exceeding the minimum zoom level.
	 * 
	 * @param width the screen's width
	 * @param height the screen's height
	 * 
	 * @throws MinZoomExceededException
	 */
	public void decreaseZoom(int width, int height) throws MinZoomExceededException;
	
	/**
	 * Set the zoom level for a given selection.
	 * 
	 * @param x the x coordinate of the top left corner of the selection 
	 * @param y the y coordinate of the top left corner of the selection
	 * @param selectionWidth the width of the selection
	 * @param selectionHeight the height of the selection 
	 * @param width the desired width
	 * @param height the desired height 
	 */
	public void setZoom(int x, int y, int selectionWidth, int selectionHeight, int width, int height);
	
	/**
	 * Pan the map in the specified direction, and in the specified number of pixels.
	 * 
	 * @param width the screen's width
	 * @param height the screen's height
	 */
	public void pan(PanDirection direction, int numberOfPixels, int width, int height);
	
	/**
	 * Get the current map type.
	 *  
	 * @return the current map type 
	 */
	public MapType getMapType();
	
	/**
	 * Set the map type.
	 * 
	 * @param mapType the new map type
	 */
	public void setMapType(MapType mapType);
	
	/**
	 * Get the width of a tile.
	 *  
	 * @return the width of a tile 
	 */
	public int getTileWidth();
	
	/**
	 * Get the height of a tile.
	 *  
	 * @return the height of a tile 
	 */
	public int getTileHeight();
	
	/**
	 * Set the appropriate image for the tile.
	 *  
	 * @param mapTile the map tile for which the image is needed
	 */
	public void fetchTileImage(MapTile mapTile);
	
	public void flushCache();
	
	public String getName();
	
	public boolean hasRoutingSupport();
	
	public Course getRoute(Location startLocation, Location endLocation, Map<String, Object> options) throws TrackItException;
	
	public boolean hasGeocodingSupport();
	
	public Location getLocation(String address);
}
