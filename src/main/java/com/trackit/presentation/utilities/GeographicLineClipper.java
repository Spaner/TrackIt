/*
 * This file is part of Track It!.
 * Copyright (C) 2017 J Brisson Lopes
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
package com.trackit.presentation.utilities;

import java.util.ArrayList;
import java.util.List;

import com.trackit.business.domain.GeographicLocation;

public class GeographicLineClipper extends LineClipper {

	public GeographicLineClipper( double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
		super( minLongitude, maxLongitude, minLatitude, maxLatitude);
		if ( isValid )
			if ( !testLatitude( minLatitude)   || !testLatitude( maxLatitude )  || 
				 !testLongitude( minLongitude) || !testLongitude( maxLongitude)    )
				isValid = false;
	}
	public List<GeographicLocation> clip( GeographicLocation location1, GeographicLocation location2) {
		if(  process( location1.getLongitude(), location1.getLatitude(),
				      location2.getLongitude(), location2.getLatitude()) ) {
			List<GeographicLocation> locations = new ArrayList<>();
			locations.add( new GeographicLocation( y1, x1));
			locations.add( new GeographicLocation( y2, x2));
			return locations;
		}
		return null;
	}
	
	private static boolean testLatitude( double latitude) {
		if ( latitude >=-90. && latitude <= 90. )
			return true;
		return false;
	}
	
	private static boolean testLongitude( double longitude) {
		if ( longitude >= -180. && longitude <= 180. )
			return true;
		return false;
	}

	public String toString() {
		return String.format( "%10.3f %10.3f %10.3f %10.3f", x1, y1, x2, y2) + super.toString();
	}
}
