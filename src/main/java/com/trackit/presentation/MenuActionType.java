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
package com.trackit.presentation;


enum MenuActionType {
	NEW_DOCUMENT,
	OPEN_DOCUMENT,
	CLOSE_DOCUMENT,				// 12335: 2015-09-23
	CLOSE_ALL_DOCUMENTS,		// 12335: 2015-09-23
	IMPORT_DOCUMENT,
	EXPORT_DOCUMENT,//58406
	IMPORT_PICTURE,//58406
	RESTART,//58406
	EXIT,
	NEW_COURSE,
	NEW_SEARCH,					//12335: 2017-03-10
	REFINE_SEARCH,				//12335: 2017-03-10
	CALENDAR_MENU,				//71052: 2017-07-10
	SPLIT_AT_SELECTED,
	JOIN,
	REVERSE,
	RETURN,
	SHOW_FOLDER,
	SHOW_SUMMARY,
	SHOW_MAP,
	SHOW_CHART,
	SHOW_TABLE,
	SHOW_LOG,
	DETECT_CLIMBS_AND_DESCENTS,
	MARKING,
	SET_PACE,
	CONSOLIDATION,
	SIMPLIFICATION,
	ALTITUDE_SMOOTHING,
	MENU_PREFERENCES,
	DETECT_PAUSES,//58406
	AUTO_LOCATE_PICTURES,//58406
	REMOVE_PAUSES,
	TEST,//57421
	COLOR_GRADING,//57421
	UNDO,//57421
	REDO,//57421
	DUPLICATE, //12335: 2017-03-30
//	COPY,//57421				//12335: 2017-04-08
	COPYTO, 					//12335: 2017-04-08
	MOVETO, //12335: 2017-03-27
	ADD_PAUSE,//57421
	CHANGE_PAUSE_DURATION,//57421
	REMOVE_PAUSE,//57421
	CREATE_SEGMENT,//57421
	SPLIT_INTO_SEGMENTS,//57421
	COMPARE_SEGMENTS;//57421
	
	static MenuActionType lookup(String menuAction) {
		for (MenuActionType menuActionType : values()) {
			if (menuActionType.name().equalsIgnoreCase(menuAction)) {
				return menuActionType;
			}
		}
		return null;
	}
}
