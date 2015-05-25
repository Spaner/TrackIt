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
package com.henriquemalheiro.trackit.presentation.task;

public enum ActionType {
	CONSOLIDATION,
	SET_PACE,
	DETECT_CLIMBS_DESCENTS,
	MARKING,
	NEW_COURSE,
	CLEAR_SEGMENTS,
	SIMPLIFICATION,
	REVERSE,
	DETECT_PAUSES,//58406
	REMOVE_PAUSES,
	ADD_LAP,
	AUTO_LOCATE_PICTURES,//58406
	IMPORT_PICTURE,//58406
	UNDO,//57421
	REDO,//57421
	COPY;
}
