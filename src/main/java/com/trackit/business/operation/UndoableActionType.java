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
package com.trackit.business.operation;

//57421
public enum UndoableActionType {
	ADD_TRACKPOINT,
	APPEND,
	ADD_ROUTE,
	REMOVE_TRACKPOINT,
	JOIN,
	SPLIT,
	REVERSE,
	ADD_PAUSE,
	CHANGE_PAUSE_DURATION,
	REMOVE_PAUSE,
	REMOVE_PAUSES,
	SET_PACE,
	DUPLICATE,						// 12335: 2017-03-30
	RENAME,							// 12335: 2017-04-25
	COPY,
	COPY_TO,						// 12335: 2017-04-03
	MOVE_TO,						// 12335: 2017-03-30
	APPEND_JOIN,
	SMART_PACE,
	CHANGE_START_TIME,
	CHANGE_END_TIME,
	SPLIT_INTO_SEGMENTS,
	CREATE_SEGMENT;
	
	public static UndoableActionType lookup(String action) {
		for (UndoableActionType actionType : values()) {
			if (actionType.name().equalsIgnoreCase(action)) {
				return actionType;
			}
		}
		return null;
	}
}
