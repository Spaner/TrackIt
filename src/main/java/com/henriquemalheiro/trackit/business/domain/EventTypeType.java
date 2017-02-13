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

public enum EventTypeType {
	START((short) 0),
	STOP((short) 1),
	CONSECUTIVE_DEPRECIATED((short) 2),
	MARKER((short) 3),
	STOP_ALL((short) 4),
	BEGIN_DEPRECIATED((short) 5),
	END_DEPRECIATED((short) 6),
	END_ALL_DEPRECIATED((short) 7),
	STOP_DISABLE((short) 8),
	STOP_DISABLE_ALL((short) 9);
	
	private short value;
	
	private EventTypeType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"eventTypeType.start", "eventTypeType.stop", "eventTypeType.consecutiveDepreciated",
		"eventTypeType.marker", "eventTypeType.stopAll", "eventTypeType.beginDepreciated", "eventTypeType.endDepreciated",
		"eventTypeType.endAllDepreciated", "eventTypeType.stopDisable", "eventTypeType.stopDisableAll"};
	
	public static EventTypeType lookup(short value) {
		for (EventTypeType eventType : values()) {
			if (eventType.value == value) {
				return eventType;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}