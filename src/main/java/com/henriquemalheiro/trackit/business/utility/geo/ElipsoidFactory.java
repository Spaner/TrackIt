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
package com.henriquemalheiro.trackit.business.utility.geo;

public class ElipsoidFactory {
	private static ElipsoidFactory elipsoidFactory;
	
	private ElipsoidFactory() {
	}
	
	public static ElipsoidFactory getInstance() {
		if (elipsoidFactory == null) {
			elipsoidFactory = new ElipsoidFactory(); 
		}
		
		return elipsoidFactory;
	}
	
	public Elipsoid getElipsoid(Elipsoids elipsoid) {
		switch (elipsoid) {
		case WGS84:
			return new Elipsoid(6378137.0, 6356752.3142);
		case HAYFORD:
			return new Elipsoid(6378388.0, 6356911.9);
		default:
			throw new IllegalArgumentException("Elipsoid " + elipsoid + " not supported.");
		}
	}
}
