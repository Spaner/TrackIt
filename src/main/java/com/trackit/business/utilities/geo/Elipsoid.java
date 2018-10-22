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

public class Elipsoid {
	private double majorRadius;
	private double minorRadius;
	
	public Elipsoid(double majorRadius, double minorRadius) {
		this.majorRadius = majorRadius;
		this.minorRadius = minorRadius;
	}

	public double getMajorRadius() {
		return majorRadius;
	}

	public void setMajorRadius(double majorRadius) {
		this.majorRadius = majorRadius;
	}

	public double getMinorRadius() {
		return minorRadius;
	}

	public void setMinorRadius(double minorRadius) {
		this.minorRadius = minorRadius;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(majorRadius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minorRadius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Elipsoid other = (Elipsoid) obj;
		if (Double.doubleToLongBits(majorRadius) != Double
				.doubleToLongBits(other.majorRadius))
			return false;
		if (Double.doubleToLongBits(minorRadius) != Double
				.doubleToLongBits(other.minorRadius))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Elipsoid [majorRadius=" + majorRadius + ", minorRadius=" + minorRadius + "]";
	}
}
