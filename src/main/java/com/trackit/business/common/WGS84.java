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
package com.trackit.business.common;

/**
 * This class provides methods and constants for dealing with distances on earth
 * using the World Geodetic System 1984
 */
public class WGS84 {
	
	/**
	 * Equatorial radius of earth is required for distance computation.
	 */
	public static final double EQUATORIALRADIUS = 6378137.0;

	/**
	 * Polar radius of earth is required for distance computation.
	 */
	public static final double POLARRADIUS = 6356752.3142;

	/**
	 * The flattening factor of the earth's ellipsoid is required for distance
	 * computation.
	 */
	public static final double INVERSEFLATTENING = 298.257223563;
	
	// 12335: 2016-07-17
	/**
	 * First eccentricity
	 */
	
	public static final double FIRSTECCENTRICITY2 = 0.006694380004260827;
	
	/**
	 *  Second eccentricity
	 */

	public static final double SECONDECCENTRICITY2 = 0.006739496756586903;

	/**
	 * The UTM projection scale factor
	 */
	public static final double K0 = 0.9996;

	/**
	 * The UTM projection false easting and northing values.
	 */
	public static final double FALSEEASTING =         500000.;
	public static final double FALSENORTHINGNORTH =        0.;
	public static final double FALSENORTHINGSOUTH = 10000000.;
	
	/**
	 * The UTM projection central latitude
	 */
	public static final double CENTRALLATITUDE = 0.;

}
