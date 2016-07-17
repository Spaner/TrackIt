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

public class LineDistance {
	private double latProjected;
	private double lonProjected;
	private double fraction;
	private double distance;
	
	public LineDistance() {
		this(0.0, 0.0, 0.0);
	}
	
	public LineDistance(double latProjected, double lonProjected, double fraction) {
		this.latProjected = latProjected;
		this.lonProjected = lonProjected;
		this.fraction = fraction;
		this.distance = 0.0;
	}
	
	public double getLatProjected() {
		return latProjected;
	}
	
	public void setLatProjected(double latProjected) {
		this.latProjected = latProjected;
	}
	
	public double getLonProjected() {
		return lonProjected;
	}
	
	public void setLonProjected(double lonProjected) {
		this.lonProjected = lonProjected;
	}
	
	public double getFraction() {
		return fraction;
	}
	
	public void setFraction(double fraction) {
		this.fraction = fraction;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}
}
