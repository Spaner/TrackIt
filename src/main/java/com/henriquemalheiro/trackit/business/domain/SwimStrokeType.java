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
package com.henriquemalheiro.trackit.business.domain;

import com.henriquemalheiro.trackit.business.common.Messages;

public enum SwimStrokeType {
	FREESTYLE((short) 0),
	BACKSTROKE((short) 1),
	BREASTSTROKE((short) 2),
	BUTTERFLY((short) 3),
	DRILL((short) 4),
	MIXED((short) 5),
	IM((short) 6);
	
	private short value;
	
	private SwimStrokeType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"swimStroke.freestyle", "swimStroke.backstroke", "swimStroke.breakstroke",
		"swimStroke.butterfly", "swimStroke.drill", "swimStroke.mixed"};
	
	public static SwimStrokeType lookup(short value) {
		for (SwimStrokeType swimStroke : values()) {
			if (swimStroke.value == value) {
				return swimStroke;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}