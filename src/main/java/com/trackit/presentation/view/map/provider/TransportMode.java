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
package com.trackit.presentation.view.map.provider;

import com.trackit.business.common.Messages;

public enum TransportMode {
	CAR("car"),
	PEDESTRIAN("pedestrian");
	
	private static final String[] messageCodes = {"transportMode.car", "transportMode.pedestrian"};
	
	private String transportModeName;
	
	private TransportMode(String transportModeName) {
		this.transportModeName = transportModeName;
	}
	
	public String getTransportModeName() {
		return transportModeName;
	}
	
	public String getDescription() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
	
	public static TransportMode lookup(String transportModeName) {
		for (TransportMode transportMode : values()) {
			if (transportMode.getTransportModeName().equalsIgnoreCase(transportModeName)) {
				return transportMode;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
}