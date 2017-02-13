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
package com.miguelpernas.trackit.business.domain;

import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;

public enum SegmentColorCategory {
	CLIMB((short) 0),
	DESCENT((short) 1),
	FLAT((short) 2),
	UNCATEGORIZED((short)3),
	MECHANICAL_CLIMB((short)4),
	STEEP_CLIMB((short)5),
	STEEP_DESCENT((short)6);
	
	private short value;
	
	private static final String[] messageCodes = { "segmentColorCategory.climb", "segmentColorCategory.descent",
		"segmentColorCategory.flat", "segmentColorCategory.uncategorized", "segmentColorCategory.mechanicalClimb", "segmentColorCategory.steepClimb", "segmentColorCategory.steepDescent"};
	
	private SegmentColorCategory(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return this.value;
	}
	
	public static SegmentColorCategory lookup(String segmentCategory) {
		for (SegmentColorCategory category : values()) {
			if (category.name().equalsIgnoreCase(segmentCategory)) {
				return category;
			}
		}
		
		return null;
	}
	
	public static boolean isCategorized(TrackSegment segment) {
		return (segment.getColorCategory() != SegmentColorCategory.UNCATEGORIZED);
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}