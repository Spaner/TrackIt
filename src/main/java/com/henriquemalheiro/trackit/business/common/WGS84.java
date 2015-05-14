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
package com.henriquemalheiro.trackit.business.common;

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
}
