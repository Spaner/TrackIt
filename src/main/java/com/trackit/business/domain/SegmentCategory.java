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
package com.trackit.business.domain;

import com.trackit.business.common.Messages;

public enum SegmentCategory {
	UNCATEGORIZED_SEGMENT((short) 0),
	UNCATEGORIZED_CLIMB((short) 7),
	FOURTH_CATEGORY_CLIMB((short) 11),
	THIRD_CATEGORY_CLIMB((short) 12),
	SECOND_CATEGORY_CLIMB((short) 13),
	FIRST_CATEGORY_CLIMB((short) 14),
	HORS_CATEGORY_CLIMB((short) 15),
	UNCATEGORIZED_DESCENT((short) 20),
	CATEGORIZED_DESCENT((short) 25);
	
	private short value;
	
	private static final String[] messageCodes = { "segmentCategory.uncategorized", "segmentCategory.uncategorized",
		"segmentCategory.fourthCategoryClimb", "segmentCategory.thirdCategoryClimb", "segmentCategory.secondCategoryClimb",
		"segmentCategory.firstCategoryClimb", "segmentCategory.horsCategoryClimb", "segmentCategory.uncategorized",
		"segmentCategory.categorizedDescent" };
	
	private SegmentCategory(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return this.value;
	}
	
	public static SegmentCategory lookup(String segmentCategory) {
		for (SegmentCategory category : values()) {
			if (category.name().equalsIgnoreCase(segmentCategory)) {
				return category;
			}
		}
		
		return null;
	}
	
	public static boolean isCategorized(TrackSegment segment) {
		return (segment.getCategory() != SegmentCategory.UNCATEGORIZED_SEGMENT
				&& segment.getCategory() != SegmentCategory.UNCATEGORIZED_CLIMB
				&& segment.getCategory() != SegmentCategory.UNCATEGORIZED_DESCENT);
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}