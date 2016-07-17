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

public enum SessionTriggerType {
	ACTIVITY_END((short) 0),
	MANUAL((short) 1),
	AUTO_MULTI_SPORT((short) 2),
	FITNESS_EQUIPMENT((short) 3);
	
	private short value;
	
	private SessionTriggerType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"sessionTriggerType.activityEnd", "sessionTriggerType.manual",
		"sessionTriggerType.autoMultiSport", "sessionTriggerType.fitnessEquipment"};
	
	public static SessionTriggerType lookup(short value) {
		for (SessionTriggerType sessionTrigger : values()) {
			if (sessionTrigger.value == value) {
				return sessionTrigger;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}