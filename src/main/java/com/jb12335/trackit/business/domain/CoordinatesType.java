/*
 * This file is part of Track It!.
 * Copyright (C) 2016 João Brisson Lopes
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
package com.jb12335.trackit.business.domain;

import com.henriquemalheiro.trackit.business.common.Messages;

public enum CoordinatesType {
	DECIMAL_DEGREES         ((short) 0),
	DEGREES_DECIMAL_MINUTES ((short) 1),
	DEGREES_MINUTES_SECONDS ((short) 2),
	UTM                     ((short) 3);
	
	private short value;
	
	public static final CoordinatesType DEFAULT = DECIMAL_DEGREES;
	
	private CoordinatesType( short value) {
		this.value = value;
	}
	
	public short valueOf() {
		return value;
	}
	
	public static CoordinatesType lookup( int valueToLookup) {
		for( CoordinatesType type: values())
			if ( type.value == valueToLookup )
				return type;
		return DEFAULT;
	}
	
	private static final String[] messageCodes = {
		"coordinatesType.decimalDegrees",	
		"coordinatesType.degreesDecimalMinutes",	
		"coordinatesType.degreesMinutesSeconds",	
		"coordinatesType.utm"	
	};

	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}
