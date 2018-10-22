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

import java.util.HashMap;
import java.util.Map;

import com.trackit.business.common.Location;

public class MolodenskyTransformation {
	private static Map<Datums, MolodenskyCoeficients> coeficientsMap;
	private Datum fromDatum;
	private Datum toDatum;
	
	static {
		coeficientsMap = new HashMap<Datums, MolodenskyCoeficients>();

		MolodenskyCoeficients coeficients = new MolodenskyCoeficients(0.0, 0.0, 0.0);
		coeficientsMap.put(Datums.WGS84, coeficients);
		
		coeficients = new MolodenskyCoeficients(-304.046, -60.376, 103.64);
		coeficientsMap.put(Datums.SHGM, coeficients);
		
		coeficients = new MolodenskyCoeficients(-304.046, -60.376, 103.64);
		coeficientsMap.put(Datums.LX, coeficients);
	}
	
	public MolodenskyTransformation(Datum fromDatum, Datum toDatum) {
		this.fromDatum = fromDatum;
		this.toDatum = toDatum;
	}
	
	public Location transform(Location location) {
		if (location.isCartesian()) {
			throw new IllegalArgumentException("Location must be in geographic coordinates.");
		}
		
		final double longitude = location.getLongitude();
		final double latitude = location.getLatitude();
		final double altitude = location.getAltitude();
		
        double slat = Math.sin(Geo.toRadians(latitude));
        double clat = Math.cos(Geo.toRadians(latitude));
        double slon = Math.sin(Geo.toRadians(longitude));
        double clon = Math.cos(Geo.toRadians(longitude));
        double ssqlat = slat * slat;

        Elipsoid toElipsoid = toDatum.getElipsoid();
        double da = toElipsoid.getMajorRadius();
        double df = -toElipsoid.getMinorRadius() / toElipsoid.getMajorRadius();

        double dx, dy,dz;
        MolodenskyCoeficients coeficients;
        if (Datums.WGS84.equals(toDatum.getDatumType())) {
        	coeficients = coeficientsMap.get(fromDatum.getDatumType());
            dx = coeficients.getDx();
            dy = coeficients.getDy();
            dz = coeficients.getDz();
        } else {
        	coeficients = coeficientsMap.get(toDatum.getDatumType());
        	dx = -coeficients.getDx();
            dy = -coeficients.getDy();
            dz = -coeficients.getDz();
        }
        
        Elipsoid fromElipsoid = fromDatum.getElipsoid();
        double esq = 1.0 - Math.pow(fromElipsoid.getMinorRadius() / fromElipsoid.getMajorRadius(), 2);
        da -= fromElipsoid.getMajorRadius();
        df += fromElipsoid.getMinorRadius() / fromElipsoid.getMajorRadius();
        double adb = fromElipsoid.getMajorRadius() / fromElipsoid.getMinorRadius();
        double rn = fromElipsoid.getMajorRadius() / Math.sqrt(1.0 - esq * ssqlat);
        double rm = fromElipsoid.getMajorRadius() * (1.0 - esq) / Math.pow((1.0 - esq * ssqlat), 1.5);

        double dLat = (((((-dx * slat * clon - dy * slat * slon) + dz * clat)
        		+ (da * ((rn * esq * slat * clat) / fromElipsoid.getMajorRadius())))
        		+ (df * (rm * adb + rn / adb) * slat * clat)))
        		/ (rm + location.getAltitude());
        double dLon = (-dx * slon + dy * clon) / ((rn + location.getAltitude()) * clat);

        double dh = (dx * clat * clon) + (dy * clat * slon) + (dz * slat)
            - (da * (fromElipsoid.getMajorRadius() / rn)) + ((df * rn * ssqlat) / adb);

        return new Location(Geo.toDegrees(dLon) + longitude, Geo.toDegrees(dLat) + latitude, dh + altitude, toDatum, Location.GEOGRAPHIC);
    }
}
