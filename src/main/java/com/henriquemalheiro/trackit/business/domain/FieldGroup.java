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


public enum FieldGroup {
	SUMMARY("SUMMARY"),
	TIMING("TIMING"),
	ELEVATION("ELEVATION"),
	HEART_RATE("HEART_RATE"),	
	CADENCE("CADENCE"),
	POWER("POWER"),
	TEMPERATURE("TEMPERATURE"),
	POSITIONING("POSITIONING"),
	DETAIL("DETAIL");

	private static final String[] iconName = {"map_small.png", "chronometer_16.png", "elevation_profile_16.png",
		"heart_16.png", "cadence_16.png", "power_16.png", "thermometer_16.png", "positioning_16.png", "document.png"};
	
	private static final ImageIcon[] fieldGroupIcons;
	private String name;
 	
	static {
		fieldGroupIcons = new ImageIcon[values().length];

		for (int i = 0; i < values().length; i++) {
			fieldGroupIcons[i] = ImageUtilities.createImageIcon(iconName[i]); 
		}
	}
	
	private FieldGroup(String fieldGroupName) {
		this.name = fieldGroupName;
	}
	
	private String getName() {
		return name;
	}
	
	public ImageIcon getIcon() {
		return fieldGroupIcons[this.ordinal()];
	}
	
	private static final String[] messageCodes = {"fieldGroup.title.summary", "fieldGroup.title.timing",
		"fieldGroup.title.elevation", "fieldGroup.title.heartRate", "fieldGroup.title.cadence",
		"fieldGroup.title.power", "fieldGroup.title.temperature", "fieldGroup.title.positioning",
		"fieldGroup.title.detail"};
	
	public static FieldGroup lookup(String fieldGroupName) {
		for (FieldGroup fieldGroup : values()) {
			if (fieldGroupName.equals(fieldGroup.getName())) {
				return fieldGroup;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}