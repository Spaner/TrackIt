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
package com.trackit.business.utilities.geo;

public class DatumFactory {
	private static DatumFactory datumFactory;
	
	private DatumFactory() {
	}
	
	public static DatumFactory getInstance() {
		if (datumFactory == null) {
			datumFactory = new DatumFactory();
		}
		
		return datumFactory;
	}
	
	public Datum getDatum(Datums datum) {
		switch (datum) {
		case WGS84:
			return new Datum(datum, ElipsoidFactory.getInstance().getElipsoid(Elipsoids.WGS84), 0.9996, 0.0, 10000000.0, 500000.0, 0.0, -1000.0);
		case SHGM:
			return new Datum(datum, ElipsoidFactory.getInstance().getElipsoid(Elipsoids.HAYFORD), 1.0, 300000.0, 0.0, 200000.0,
					Geo.toDegrees(-8.0, 7.0, 54.862), Geo.toDegrees(39.0, 40.0));
		case LX:
			return new Datum(datum, ElipsoidFactory.getInstance().getElipsoid(Elipsoids.HAYFORD), 1.0, 0.0, 0.0, 0.0,
					Geo.toDegrees(-8.0, 7.0, 54.862), Geo.toDegrees(39.0, 40.0));
		default:
			throw new IllegalArgumentException("The datum " + datum + " is not supported.");
		}
	}
}
