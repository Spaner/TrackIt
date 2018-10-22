/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
 * Copyright (C) 2017 J M Brisson Lopes
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

import com.trackit.business.common.WGS84;
import com.trackit.presentation.view.map.provider.MapProvider;


/**
 * A class that implements spherical mercator projection.
 */
public class SphericalMercatorProjection {
	private MapProvider mapProvider;
	
	public SphericalMercatorProjection(MapProvider mapProvider) {
		this.mapProvider = mapProvider;
	}
	
	/**
	 * Convert a longitude coordinate (in degrees) to a horizontal distance in
	 * meters from the zero meridian
	 * 
	 * @param longitude in degrees
	 * @return longitude in meters in spherical mercator projection
	 */
	public double longitudeToMetersX(double longitude) {
		return WGS84.EQUATORIALRADIUS * Math.toRadians(longitude);
	}

	/**
	 * Convert a meter measure to a longitude
	 * 
	 * @param x in meters
	 * @return longitude in degrees in spherical mercator projection
	 */
	public double metersXToLongitude(double x) {
		return Math.toDegrees(x / WGS84.EQUATORIALRADIUS);
	}

	/**
	 * Convert a meter measure to a latitude
	 * 
	 * @param y in meters
	 * @return latitude in degrees in spherical mercator projection
	 */
	public double metersYToLatitude(double y) {
		return Math.toDegrees(Math.atan(Math.sinh(y / WGS84.EQUATORIALRADIUS)));
	}

	/**
	 * Convert a latitude coordinate (in degrees) to a vertical distance in
	 * meters from the equator
	 * 
	 * @param latitude in degrees
	 * @return latitude in meters in spherical mercator projection
	 */
	public double latitudeToMetersY(double latitude) {
		return WGS84.EQUATORIALRADIUS
				* Math.log(Math.tan(Math.PI / 4
						+ 0.5 * Math.toRadians(latitude)));
	}

	/**
	 * Calculate the distance on the ground that is represented by a single
	 * pixel on the map.
	 * 
	 * @param latitude the latitude coordinate at which the resolution should be
	 *        calculated.
	 * @param zoom the zoom level at which the resolution should be calculated.
	 * @return the ground resolution at the given latitude and zoom level.
	 */
	public double calculateGroundResolution(double latitude, byte zoom) {
		return Math.cos(latitude * Math.PI / 180) * 40075016.686
				/ (mapProvider.getTileWidth() << zoom);
	}

	/**
	 * Convert a latitude coordinate (in degrees) to a pixel Y coordinate at a
	 * certain zoom level.
	 * 
	 * @param latitude the latitude coordinate that should be converted.
	 * @param zoom the zoom level at which the coordinate should be converted.
	 * @return the pixel Y coordinate of the latitude value.
	 */
	public double latitudeToPixelY(double latitude, byte zoom) {
		double sinLatitude = Math.sin(latitude * Math.PI / 180);
		return ((0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude))
				/ (4 * Math.PI)) * (mapProvider.getTileHeight() << zoom));
	}

	/**
	 * Convert a latitude coordinate (in degrees) to a tile Y number at a
	 * certain zoom level.
	 * 
	 * @param latitude the latitude coordinate that should be converted.
	 * @param zoom the zoom level at which the coordinate should be converted.
	 * @return the tile Y number of the latitude value.
	 */
	public long latitudeToTileY(double latitude, byte zoom) {
		return pixelYToTileY(latitudeToPixelY(latitude, zoom), zoom);
	}

	/**
	 * Convert a longitude coordinate (in degrees) to a pixel X coordinate at a
	 * certain zoom level.
	 * 
	 * @param longitude the longitude coordinate that should be converted.
	 * @param zoom the zoom level at which the coordinate should be converted.
	 * @return the pixel X coordinate of the longitude value.
	 */
	public double longitudeToPixelX(double longitude, byte zoom) {
		return ((longitude + 180) / 360 * (mapProvider.getTileWidth() << zoom));
	}

	/**
	 * Convert a longitude coordinate (in degrees) to the tile X number at a
	 * certain zoom level.
	 * 
	 * @param longitude the longitude coordinate that should be converted.
	 * @param zoom the zoom level at which the coordinate should be converted.
	 * @return the tile X number of the longitude value.
	 */
	public long longitudeToTileX(double longitude, byte zoom) {
		return pixelXToTileX(longitudeToPixelX(longitude, zoom), zoom);
	}

	/**
	 * Convert a pixel X coordinate at a certain zoom level to a longitude
	 * coordinate.
	 * 
	 * @param pixelX the pixel X coordinate that should be converted.
	 * @param zoom the zoom level at which the coordinate should be converted.
	 * @return the longitude value of the pixel X coordinate.
	 */
	public double pixelXToLongitude(double pixelX, byte zoom) {
		return 360 * ((pixelX / (mapProvider.getTileWidth() << zoom)) - 0.5);
	}

	/**
	 * Convert a pixel X coordinate to the tile X number.
	 * 
	 * @param pixelX the pixel X coordinate that should be converted.
	 * @param zoom the zoom level at which the coordinate should be converted.
	 * @return the tile X number.
	 */
	public long pixelXToTileX(double pixelX, byte zoom) {
		return (long) Math.min(Math.max((pixelX / mapProvider.getTileWidth()), 0),
				Math.pow(2, zoom) - 1);
	}

	/**
	 * Convert a tile X number to a pixel X coordinate
	 * 
	 * @param tileX the tile X number that should be converted
	 * @return the pixel X coordinate
	 */
	public double tileXToPixelX(long tileX) {
		return tileX * mapProvider.getTileWidth();
	}

	/**
	 * Convert a tile Y number to a pixel Y coordinate
	 * 
	 * @param tileY the tile Y number that should be converted
	 * @return the pixel Y coordinate
	 */
	public double tileYToPixelY(long tileY) {
		return tileY * mapProvider.getTileHeight();
	}

	/**
	 * Convert a pixel Y coordinate at a certain zoom level to a latitude
	 * coordinate.
	 * 
	 * @param pixelY the pixel Y coordinate that should be converted.
	 * @param zoom the zoom level at which the coordinate should be converted.
	 * @return the latitude value of the pixel Y coordinate.
	 */
	public double pixelYToLatitude(double pixelY, byte zoom) {
		double y = 0.5 - (pixelY / (mapProvider.getTileHeight() << zoom));
		return 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
	}

	/**
	 * Converts a pixel Y coordinate to the tile Y number.
	 * 
	 * @param pixelY the pixel Y coordinate that should be converted.
	 * @param zoom the zoom level at which the coordinate should be converted.
	 * @return the tile Y number.
	 */
	public long pixelYToTileY(double pixelY, byte zoom) {
		return (long) Math.min(Math.max((pixelY / mapProvider.getTileHeight()), 0),
				Math.pow(2, zoom) - 1);
	}

	/**
	 * Convert a tile X number at a certain zoom level to a longitude
	 * coordinate.
	 * 
	 * @param tileX the tile X number that should be converted.
	 * @param zoom the zoom level at which the number should be converted.
	 * @return the longitude value of the tile X number.
	 */
	public double tileXToLongitude(long tileX, byte zoom) {
		return pixelXToLongitude(tileX * mapProvider.getTileWidth(), zoom);
	}

	/**
	 * Convert a tile Y number at a certain zoom level to a latitude coordinate.
	 * 
	 * @param tileY the tile Y number that should be converted.
	 * @param zoom the zoom level at which the number should be converted.
	 * @return the latitude value of the tile Y number.
	 */
	public double tileYToLatitude(long tileY, byte zoom) {
		return pixelYToLatitude(tileY * mapProvider.getTileHeight(), zoom);
	}
	
	/**
	 * Determines the map width and height (in pixels) at a specified level
	 * of detail.
	 * 
	 * @param zoom the zoom level at which the map size should be determined.
	 * @return the map width and height, in pixels, at the specified zoom level
	 */
	public long getMapSize(byte zoom) {
		return (256 << zoom);
	}
	
	/**
	 * Converts tile XY coordinates into a QuadKey at a specified zoom level.
	 * 
	 * @param tileX the tile X number to be considered
	 * @param tileY the tile Y number to be considered
	 * @param zoom the zoom level to be considered
	 * @return the quadkey to the specified tile
	 */
    public static String tileXYZToQuadKey(int tileX, int tileY, int zoom) {
        StringBuilder quadKey = new StringBuilder();
        
        for (int i = zoom; i > 0; i--) {
        	char digit = '0';
            int mask = 1 << (i - 1);
            
            if ((tileX & mask) != 0) {
                digit++;
            }
            
            if ((tileY & mask) != 0) {
                digit++;
                digit++;
            }
            
            quadKey.append(digit);
        }
        
        return quadKey.toString();
    }

    /**
     * Converts a QuadKey into a tile with XY coordinates and zoom level.
     * 
     * @param quadKey the quadkey to convert
     */
    public static int[] quadKeyToTileXYZ(String quadKey) {
        int tileX = 0;
        int tileY = 0;
        int zoom = quadKey.length();
        
        for (int i = zoom; i > 0; i--) {
            int mask = 1 << (i - 1);
            
            switch (quadKey.charAt(zoom - i)) {
                case '0':
                    break;

                case '1':
                    tileX |= mask;
                    break;

                case '2':
                    tileY |= mask;
                    break;

                case '3':
                    tileX |= mask;
                    tileY |= mask;
                    break;

                default:
                    throw new IllegalArgumentException("Invalid QuadKey digit sequence.");
            }
        }
        
        return (new int[] { tileX, tileY, zoom });
    }
}
