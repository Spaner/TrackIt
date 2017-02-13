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

public enum DisplayMeasureType {
	METRIC((short) 0),
	STATUTE((short) 1),
	NAUTICAL((short) 2);  //12335: 2016-07-24
	
	private short value;
	
	private DisplayMeasureType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"displayMeasureType.metric",
												  "displayMeasureType.statute",
												  "displayMeasureType.nautical"}; //12335:2016-07-24
	
	public static DisplayMeasureType lookup(short value) {
		for (DisplayMeasureType displayMeasure : values()) {
			if (displayMeasure.value == value) {
				return displayMeasure;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}