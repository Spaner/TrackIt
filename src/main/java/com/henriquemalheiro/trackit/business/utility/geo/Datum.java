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

import static com.henriquemalheiro.trackit.business.utility.geo.Geo.getESquare;
import static com.henriquemalheiro.trackit.business.utility.geo.Geo.toDegrees;
import static com.henriquemalheiro.trackit.business.utility.geo.Geo.toRadians;

import com.henriquemalheiro.trackit.business.common.Location;

public class Datum {
	private Datums datumType;
	private Elipsoid elipsoid;
	private double scaleFactor;
	private double northingN;
	private double northingS;
	private double easting;
	private double centralLongitude;
	private double centralLatitude;
	
    private double m1;
    private double m2;
    private double m4;
    private double m6;
    private double kZero;
    private double meridionalArcToCentralPoint;
	
    public Datum(Datums datumType, Elipsoid elipsoid, double scaleFactor, double northingN, double northingS, double easting,
    		double centralLongitude, double centralLatitude) {
    	
    	this.datumType = datumType;
    	this.elipsoid = elipsoid;
    	this.scaleFactor = scaleFactor;
    	this.northingN = northingN;
    	this.northingS = northingS;
    	this.easting = easting;
    	this.centralLongitude = centralLongitude;
    	this.centralLatitude = centralLatitude;
    	
    	initParameters();
    }
    
    private void initParameters() {
    	final double eSquare = getESquare(this);
    	
        m1 = ((-5.0 / 256.0 * eSquare - 3.0 / 64.0) * eSquare - 0.25) * eSquare + 1.0;
        m2 = ((45.0 / 1024.0 * eSquare + 3.0 / 32.0) * eSquare + 3.0 / 8.0) * eSquare;
        m4 = (45.0 / 1024.0 * eSquare + 15.0 / 256.0) * eSquare * eSquare;
        m6 = 35.0 / 3072.0 * eSquare * eSquare * eSquare;
        kZero = 1.0;
        
        final double phi = toRadians(centralLatitude);
        meridionalArcToCentralPoint = getLengthMeridianArc(phi);
    }

	private double getLengthMeridianArc(final double phi) {
		return (-m6 * Math.sin(6.0 * phi) + m4 * Math.sin(4.0 * phi) - m2 * Math.sin(2.0 * phi) + m1 * phi)
        		* elipsoid.getMajorRadius();
	}

	public Elipsoid getElipsoid() {
		return elipsoid;
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public double getNorthingN() {
		return northingN;
	}

	public double getNorthingS() {
		return northingS;
	}

	public double getEasting() {
		return easting;
	}

	public double getCentralLongitude() {
		return centralLongitude;
	}

	public double getCentralLatitude() {
		return centralLatitude;
	}
	
	public Datums getDatumType() {
		return this.datumType;
	}
	
	public Location geographicToCartesian(Location location) {
		final double longitude = location.getLongitude();
		final double latitude = location.getLatitude();
		
		final double eSquare = getESquare(this);
		final double ePrimeSquare = Geo.getEPrimeSquare(this);
		
		double phi = toRadians(latitude);
		double cphi = Math.cos(phi);
	    double lambda = toRadians(longitude);
	    double lambda0 = toRadians(Geo.getCentralLongitude(this, longitude));
	    double N = elipsoid.getMajorRadius() / Math.sqrt(1.0 - (1.0 - cphi * cphi) * eSquare);
	    double T = Math.pow(Math.tan(phi), 2);
	    double C = cphi * cphi * ePrimeSquare;
	    double A = cphi * (lambda - lambda0);
	    double M = getLengthMeridianArc(phi);
	    
	    double newLongitude = ((((T - 18.0) * T + 5 - 0 + (-58.0 * T + 14 - 0) * C) * A * A / 120.0
	    		+ (1.0 - T + C)) * A * A / 6.0 + 1.0) * A * N;
	    double newLatitude = ((((T - 58.0) * T + 61.0) * A * A / 720.0
	    		+ ((4.0 * C + 9.0) * C + 5.0 - T) / 24.0) * A * A + 0.5) * A * A * N * Math.tan(phi)
	    		+ M - meridionalArcToCentralPoint;
	        
        newLongitude += easting;
        if (latitude >= 0.0) {
            newLatitude += northingN;
        } else {
        	newLatitude += northingS;
        }
        
        return new Location(newLongitude, newLatitude, location.getDatum(), Location.CARTESIAN);
	}
	
	public Location cartesianToGeographic(Location location, boolean isSouth, int zone) {
		final double longitude = location.getLongitude() - easting;
		final double latitude = location.getLatitude();
		
		final double eSquare = getESquare(this);
		final double ePrimeSquare = Geo.getEPrimeSquare(this);

		double sigma1 = 0.0;
        double M = latitude;
        
        if (isSouth) {
            M -= northingS;
        } else {
            M -= northingN;
        }

        double phi = toRadians(getCentralLatitude());
        sigma1 = (-m6 * Math.sin(6.0 * phi) + m4 * Math.sin(4.0 * phi)
        		- m2 * Math.sin(2.0 * phi) + m1 * phi) * elipsoid.getMajorRadius();
        
        M += sigma1;
        M /= kZero;
        
        double mu = M / m1 / elipsoid.getMajorRadius();
        double e1 = Math.sqrt(1.0 - eSquare);
        e1 = (1.0 - e1) / (1.0 + e1);
        double j1 = (-27.0 / 32.0 * e1 * e1 + 1.5) * e1;
        double j2 = (-55.0 / 32.0 * e1 * e1 + 21.0 / 16) * e1 * e1;
        double j3 = 151.0 / 96.0 * e1 * e1 * e1;
        double j4 = 1097.0 / 512.0 * e1 * e1 * e1 * e1;
        double fp = Math.sin(8.0 * mu) * j4 + Math.sin(6.0 * mu) * j3 + Math.sin(4.0 * mu) * j2
        		+ Math.sin(2.0 * mu) * j1 + mu;

        double c1 = Math.pow(Math.cos(fp), 2) * ePrimeSquare;
        double t1 = Math.pow(Math.tan(fp), 2);
        double dd = -Math.pow(Math.sin(fp), 2) * eSquare + 1.0;
        double r1 = (1.0 - eSquare) * elipsoid.getMajorRadius() / Math.pow(dd, 1.5);
        double n1 = elipsoid.getMajorRadius() / Math.sqrt(dd);
        double d = longitude / (n1 * kZero);

        double q1 = Math.tan(fp) * n1 / r1;
        double q2 = d * d / 2;

        double q5 = d;
        double q6 = ( 1.0 + 2.0 * t1 + c1) * d * d * d / 6.0;

        double q3 = ((-9.0 * c1 + 3.0) * t1 + c1 + 5.0) * d * d * d * d / 24.0;
        double q4 = ((45.0 * t1 + 90.0) * t1 + 61.0) * d * d * d * d * d * d / 720.0;
        double q7 = ((24.0 * t1 + 28.0) * t1 + 5.0) * d * d * d * d * d / 120.0;

        double newLongitude = (q5 - q6 + q7) / Math.cos(fp);
        double newLatitude = fp - (q2 - q3 + q4) * q1;

        return new Location(toDegrees(newLongitude) + Geo.getCentralLongitude(this, zone), toDegrees(newLatitude), location.getDatum(), Location.GEOGRAPHIC);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datumType == null) ? 0 : datumType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Datum other = (Datum) obj;
		if (datumType != other.datumType)
			return false;
		return true;
	}

	public static Location transform(Datum fromDatum, Datum toDatum, Location location) {
		MolodenskyTransformation molodensky = new MolodenskyTransformation(fromDatum, toDatum);
		return molodensky.transform(location);
    }
}
