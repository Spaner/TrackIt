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

public enum FitnessEquipmentStateType {
	READY((short) 0),
	IN_USE((short) 1),
	PAUSED((short) 2),
	UNKNOWN((short) 3);
	
	private short value;
	
	private FitnessEquipmentStateType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"fitnessEquipmentState.ready", "fitnessEquipmentState.inUse",
		"fitnessEquipmentState.paused", "fitnessEquipmentState.unknown"};
	
	public static FitnessEquipmentStateType lookup(short value) {
		for (FitnessEquipmentStateType fitnessEquipmentState : values()) {
			if (fitnessEquipmentState.value == value) {
				return fitnessEquipmentState;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}