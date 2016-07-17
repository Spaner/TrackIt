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

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;

public enum CoursePointType {
	GENERIC((short) 0),
	SUMMIT((short) 1),
	VALLEY((short) 2),
	WATER((short) 3),
	FOOD((short) 4),
	DANGER((short) 5),
	LEFT((short) 6),
	RIGHT((short) 7),
	STRAIGHT((short) 8),
	FIRST_AID((short) 9),
	FOURTH_CATEGORY((short) 10),
	THIRD_CATEGORY((short) 11),
	SECOND_CATEGORY((short) 12),
	FIRST_CATEGORY((short) 13),
	HORS_CATEGORY((short) 14),
	SPRINT((short) 15);
	
	private short value;
	private static final ImageIcon[] coursePointIcons;
	
	private static final String[] messageCodes = {"coursePoint.generic", "coursePoint.summit", "coursePoint.valley",
		"coursePoint.water", "coursePoint.food", "coursePoint.danger", "coursePoint.left", "coursePoint.right",
		"coursePoint.straight", "coursePoint.firstAid", "coursePoint.fourthCategory", "coursePoint.thirdCategory",
		"coursePoint.secondCategory", "coursePoint.firstCategory", "coursePoint.horsCategory", "coursePoint.sprint"};
	
	
	static {
		final String ICON_EXTENSION = ".png";
		
		coursePointIcons = new ImageIcon[values().length];
		
		for (int i = 0; i < values().length; i++) {
			String iconFilename = Messages.getMessage(messageCodes[i]).replace(" ", "") + ICON_EXTENSION;
			coursePointIcons[i] = ImageUtilities.createImageIcon("coursepoints", iconFilename); 
		}
	}
	
	private CoursePointType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return this.value;
	}
	
	public ImageIcon getIcon() {
		return coursePointIcons[this.getValue()];
	}
	
	public static CoursePointType lookup(short value) {
		for (CoursePointType type : values()) {
			if (type.value == value) {
				return type;
			}
		}
		return null;
	}
	
	public static CoursePointType lookup(String coursePointType) {
		for (CoursePointType type : values()) {
			if (type.name().equalsIgnoreCase(coursePointType)) {
				return type;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}