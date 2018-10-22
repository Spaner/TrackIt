/*
 * This file is part of Track It!.
 * Copyright (C) 2017 João Brisson Lopes
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

package com.trackit.business.domain;

import java.util.ArrayList;
import java.util.List;

import com.trackit.business.common.Messages;
import com.trackit.business.utilities.Utilities;

//12335: 2017-08-09: added ALL
//					 changed value type from boolean to short
//					 added getList( boolean) and lookup( String)

public enum CircuitType {
	OPEN( 		(short) 0),
	LOOP( 		(short) 1),
	ALL(        (short) 2);

	private static double PERCENT_THRESHOLD = .05;

	private short value;
	
	private CircuitType( short value) {
		this.value = value;
	}
	public short getValue() {
		return this.value;
	}

	public static CircuitType lookup( short circuit) {
		for (CircuitType type : values()) {
			if ( circuit == type.getValue() ) {
				return type;
			}
		}
		return null;
	}
	
	public static CircuitType lookup( String name) {
		for( CircuitType circular: values() ) {
			if ( circular.toString().equals( name) )
				return circular;
		}
		return null;
	}

	public static CircuitType getType( Double startLatitude, Double startLongitude, 
									   Double endLatitude, Double endLongitude,
									   double allowance) {
		if ( Utilities.getGreatCircleDistance( startLatitude, startLongitude, endLatitude, endLongitude) 
																						<= allowance )
			return CircuitType.LOOP;
		return CircuitType.OPEN;
	}
	
	public static CircuitType getType( Double pathLength, Double elapsedTime,
									   Trackpoint start,  Trackpoint end, 
									   double allowance) {
		if ( pathLength != null && elapsedTime != null) {
			if ( pathLength == 0. || elapsedTime == 0. )
				return CircuitType.LOOP;
			double endsDistance = start.getDistanceFrom( end);
			System.out.print( "CircuitType says " + pathLength + "  " + endsDistance + "  ");
			if ( Math.abs( pathLength - endsDistance) / pathLength < .05 )
				return CircuitType.LOOP;
		}
		return CircuitType.OPEN;
	}
	
	//12335: 2018-07-02
	public static CircuitType getType( List<Trackpoint> trackpoints, double allowance) {
		if ( trackpoints != null && trackpoints.size() > 1 ) {
			double[] latitude  = new double[ trackpoints.size()];
			double[] longitude = new double[ trackpoints.size()];
			for( int i=0; i<trackpoints.size(); i++) {
				latitude [i] = trackpoints.get( i).getLatitude();
				longitude[i] = trackpoints.get( i).getLongitude();
			}
			GeographicBoundingBox box = new GeographicBoundingBox( latitude, longitude);
			double diagonalWidth = box.diagonalLength();
			double endsDistance = trackpoints.get(0).getDistanceFrom( trackpoints.get( trackpoints.size()-1));
			if ( diagonalWidth > allowance / PERCENT_THRESHOLD ) {
				if ( endsDistance <= allowance )
					return LOOP;
			} else {
				if ( endsDistance <= diagonalWidth * PERCENT_THRESHOLD )
					return LOOP;
			}
		}
		return CircuitType.OPEN;
	}
	
	public static List<CircuitType> getList( boolean includeAll) {
		List<CircuitType> list = new ArrayList<>();
		if ( includeAll )
			list.add( CircuitType.ALL);
		list.add( CircuitType.OPEN);
		list.add( CircuitType.LOOP);
		return list;
	}

	@Override
	public String toString() {
		return Messages.getMessage( messageCodes[this.ordinal()]);
	}
	
	private static String[] messageCodes = {
			"circuitType.open",
			"circuitType.loop",
			"circuitType.allTypes"};
	
}
