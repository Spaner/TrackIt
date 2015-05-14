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

import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Location;

public class Geo {
	
	public static double toDegrees(double radians) {
        return radians * 180. / Math.PI;
    }

    public static double toDegrees(double degrees, double minutes) {
    	return (minutes / 60. + Math.abs(degrees)) * degrees / Math.abs(degrees);
    }

    public static double toDegrees(double degrees, double minutes, double seconds) {
    	return toDegrees(degrees, seconds / 60. + minutes);
    }
    
    public static double toRadians(double degrees) {
		return degrees * Math.PI / 180.0;
	}
    
    public static double getESquare(Datum datum) {
    	double majorRadius = datum.getElipsoid().getMajorRadius();
    	double minorRadius = datum.getElipsoid().getMinorRadius();
    	
    	return (1.0 - Math.pow(minorRadius / majorRadius, 2));
    }
    
    public static double getEPrimeSquare(Datum datum) {
    	double eSquare = getESquare(datum);
    	
    	return (eSquare / (1.0 - eSquare));
    }
    
    public static double getCentralLongitude(Datum datum, double longitude) {
		return getCentralLongitude(datum, computeZone(longitude));
    }
	
	public static int computeZone(double longitude) {
		return ((int) ((longitude + 180.0) / 6.0) + 1);
    }
	
	public static double getCentralLongitude(Datum datum, int zone) {
		if (datum.getCentralLongitude() == -1000.0) {
			return zone * 6.0 - 183.0;
		} else {
            return datum.getCentralLongitude();
		}
    }
	
	public static double getWidth(BoundingBox2<Location> boundingBox) {
		double minLongitude = boundingBox.getTopLeft().getLongitude();
		double maxLongitude = boundingBox.getTopRight().getLongitude();
		
		return (maxLongitude - minLongitude);
	}
	
	public static double getHeight(BoundingBox2<Location> boundingBox) {
		double minLatitude = boundingBox.getBottomRight().getLatitude();
		double maxLatitude = boundingBox.getTopRight().getLatitude();
		
		return (maxLatitude - minLatitude);
	}
}