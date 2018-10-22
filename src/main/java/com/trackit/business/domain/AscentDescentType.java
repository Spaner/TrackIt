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

import com.trackit.business.common.Messages;

public enum AscentDescentType {
	
	UNDEFINED(       (short) 0),
	FLAT(            (short) 1),
	ASCENT_ONLY(     (short) 2),
	DESCENT_ONLY(    (short) 3),
	ASCENT_DESCENT ( (short) 4);
	
	private short value;
	
	private AscentDescentType( short value) {
		this.value = value;
	}

	public short getValue() {
		return this.value;
	}

	public static AscentDescentType lookup( short value) {
		for (AscentDescentType type : values()) {
			if ( value == type.getValue() ) {
				return type;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return Messages.getMessage( messageCodes[this.ordinal()]);
	}
	
	private static String[] messageCodes = {
			"ascentDescentType.undefined",
			"ascentDescentType.flat",
			"ascentDescentType.ascentOnly",
			"ascentDescentType.descentOnly",
			"ascentDescentType.ascentDescent"};
}
