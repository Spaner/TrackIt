/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package com.henriquemalheiro.trackit.business.common;

import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.util.VincentyGeodesy;

import com.henriquemalheiro.trackit.business.utility.Utilities;

public class BoundingBox {
	private long id;
	private int minLat;
	private int maxLat;
	private int minLon;
	private int maxLon;

	public BoundingBox(Location center, double radius) {
		this.id = IdGenerator.INSTANCE.getNextId();
		
		Location upperLeft = calculateUpperLeft(center, radius);
		Location lowerRight = calculateLowerRight(center, radius); 
		
		Pair<Integer, Integer> upperLeftSC = toSemicircles(upperLeft);
		Pair<Integer, Integer> lowerRightSC = toSemicircles(lowerRight);
		
		minLon = Math.min(upperLeftSC.getFirst(), lowerRightSC.getFirst());
		maxLon = Math.max(upperLeftSC.getFirst(), lowerRightSC.getFirst());
		minLat = Math.min(upperLeftSC.getSecond(), lowerRightSC.getSecond());
		maxLat = Math.max(upperLeftSC.getSecond(), lowerRightSC.getSecond());
	}
	
	private Pair<Integer, Integer> toSemicircles(Location location) {
		int longitude = Utilities.degreesToSemicircles(location.getLongitude());
		int latitude = Utilities.degreesToSemicircles(location.getLatitude());
		return Pair.create(longitude, latitude);
	}

	private Location calculateUpperLeft(Location location, double radius) {
		WGS84Point center = new WGS84Point(location.getLatitude(), location.getLongitude());
		
		WGS84Point upperLeft = VincentyGeodesy.moveInDirection(center, 180, radius);
		upperLeft = VincentyGeodesy.moveInDirection(upperLeft, 90, radius);
		
		return new Location(upperLeft.getLongitude(), upperLeft.getLatitude()); 
	}
	
	private Location calculateLowerRight(Location location, double radius) {
		WGS84Point center = new WGS84Point(location.getLatitude(), location.getLongitude());
		
		WGS84Point lowerRight = VincentyGeodesy.moveInDirection(center, 0, radius);
		lowerRight = VincentyGeodesy.moveInDirection(lowerRight, 270, radius);
		
		return new Location(lowerRight.getLongitude(), lowerRight.getLatitude()); 
	}

	public Location getUpperLeft() {
		return new Location(minLon, maxLat);
	}

	public Location getLowerRight() {
		return new Location(maxLon, minLat);
	}

	public int getLatitudeSize() {
		return maxLat - minLat;
	}

	public int getLongitudeSize() {
		return maxLon - minLon;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof BoundingBox) {
			BoundingBox that = (BoundingBox) obj;
			return minLat == that.minLat && minLon == that.minLon && maxLat == that.maxLat && maxLon == that.maxLon;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + hashCode(minLat);
		result = 37 * result + hashCode(maxLat);
		result = 37 * result + hashCode(minLon);
		result = 37 * result + hashCode(maxLon);
		return result;
	}

	private static int hashCode(double x) {
		long f = Double.doubleToLongBits(x);
		return (int) (f ^ (f >>> 32));
	}

	public boolean contains(Location location) {
		Pair<Integer, Integer> locationSC = toSemicircles(location);
		
		return (locationSC.getSecond() >= minLat) && (locationSC.getFirst() >= minLon) && (locationSC.getSecond() <= maxLat)
				&& (locationSC.getFirst() <= maxLon);
	}

	public boolean intersects(BoundingBox other) {
		return !(other.minLon > maxLon || other.maxLon < minLon || other.minLat > maxLat || other.maxLat < minLat);
	}

	@Override
	public String toString() {
		return getUpperLeft() + " -> " + getLowerRight();
	}

	public Location getCenterPoint() {
		int centerLatitude = (minLat + maxLat) / 2;
		int centerLongitude = (minLon + maxLon) / 2;
		return new Location(Utilities.semicirclesToDegrees(centerLongitude),
				Utilities.semicirclesToDegrees(centerLatitude));
	}

	public void expandToInclude(BoundingBox other) {
		if (other.minLon < minLon) {
			minLon = other.minLon;
		}
		if (other.maxLon > maxLon) {
			maxLon = other.maxLon;
		}
		if (other.minLat < minLat) {
			minLat = other.minLat;
		}
		if (other.maxLat > maxLat) {
			maxLat = other.maxLat;
		}
	}
	
	public long getId() {
		return id;
	}

	public int getMinLon() {
		return minLon;
	}

	public int getMinLat() {
		return minLat;
	}

	public int getMaxLat() {
		return maxLat;
	}

	public int getMaxLon() {
		return maxLon;
	}
}
