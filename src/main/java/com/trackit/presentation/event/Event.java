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
package com.trackit.presentation.event;

import com.trackit.business.common.Messages;

public enum Event {
    DOCUMENT_UPDATED("DOCUMENT_UPDATED"),
    DOCUMENT_DISCARDED("DOCUMENT_DISCARDED"),
    NOTHING_SELECTED("NOTHING_SELECTED"),
    DOCUMENTS_SELECTED("DOCUMENTS_SELECTED"),
    DOCUMENT_ADDED("DOCUMENT_ADDED"),
    DOCUMENT_SELECTED("DOCUMENT_SELECTED"),
    DOCUMENT_CHANGED("DOCUMENT_CHANGED"),
    ACTIVITIES_SELECTED("ACTIVITIES_SELECTED"),
    ACTIVITY_ADDED("ACTIVITY_ADDED"),
    ACTIVITY_SELECTED("ACTIVITY_SELECTED"),
    ACTIVITY_DISCARDED("ACTIVITY_DISCARDED"),
    ACTIVITY_REMOVED("ACTIVITY_REMOVED"),					//12335: 2018-06-29
    COURSES_SELECTED("COURSES_SELECTED"),
    COURSE_SELECTED("COURSE_SELECTED"),
    COURSE_DISCARDED("COURSE_DISCARDED"),					//12335: 2018-06-29
    COURSE_REMOVED("COURSE_REMOVED"),
    SESSIONS_SELECTED("SESSIONS_SELECTED"),
    SESSION_SELECTED("SESSION_SELECTED"),
    LAPS_SELECTED("LAPS_SELECTED"),
    LAP_SELECTED("LAP_SELECTED"),
    LENGTH_SELECTED("LENGTH_SELECTED"),
    TRACK_SELECTED("TRACK_SELECTED"),
    COURSE_POINTS_SELECTED("COURSE_POINTS_SELECTED"),
    COURSE_POINT_SELECTED("COURSE_POINT_SELECTED"),
    EVENTS_SELECTED("EVENTS_SELECTED"),
    EVENT_SELECTED("EVENT_SELECTED"),
    DEVICES_SELECTED("DEVICES_SELECTED"),
    DEVICE_SELECTED("DEVICE_SELECTED"),
    ANIMATION_MOVE("ANIMATION_MOVE"),
    WAYPOINTS_SELECTED("WAYPOINTS_SELECTED"),
    WAYPOINT_SELECTED("WAYPOINT_SELECTED"),
    TRACKPOINT_SELECTED("TRACKPOINT_SELECTED"),
    TRACKPOINT_HIGHLIGHTED("TRACKPOINT_HIGHLIGHTED"),
    ZOOM_TO_ITEM("ZOOM_TO_ITEM"),
    TRACKPOINTS_REMOVED("TRACKPOINTS_REMOVED"),
    SEGMENTS_SELECTED("SEGMENTS_SELECTED"),
    SEGMENT_SELECTED("SEGMENT_SELECTED"),
    SEGMENTS_CREATED("SEGMENTS_CREATED"),//57421
    COURSE_UPDATED("COURSE_UPDATED"),
    FOLDER_SELECTED("FOLDER_SELECTED"),
    MISCELANEOUS_SELECTION("MISCELANEOUS_SELECTION"),
    ACTIVITY_UPDATED("ACTIVITY_UPDATED"),//58406
    PICTURES_SELECTED("PICTURES_SELECTED"),//58406
    PICTURE_SELECTED("PICTURE_SELECTED"),//58406
	COORDINATES_TYPE_CHANGED("COORDINATES_TYPE_CHANGED"),	//12335: 2016-07-18
	GET_COORDINATES("GET_COORDINATES"),
	BY_BUTTON("BY_BUTTON"),
	NO_BUTTON("NO_BUTTON"),
	SEND_COORDINATES("SEND_COORDINATES");
	
    private String name;

    private Event(String eventName) {
        this.name = eventName;
    }

    private String getName() {
        return name;
    }

    private static final String[] messageCodes = {
    	"event.documentUpdated",    "event.documentDiscarded",
        "event.nothingSelected",    "event.documentsSelected", "event.documentAdded", 
        "event.documentSelected",   "event.documentChanged",
        "event.activitiesSelected", "event.activityAdded",     
        "event.activitySelected",
        "event.activityDiscarded", 	//12335: 2018-06-29
        "event.activityRemoved",
        "event.coursesSelected",
        "event.courseSelected",     
        "event.courseDiscarded",  	//12335: 2018-06-29
        "event.courseRemoved", 
        "event.sessionsSelected",   "event.sessionSelected",  "event.lapsSelected",   "event.lapSelected",
        "event.lengthSelected", "event.trackSelected", "event.coursePointsSelected", "event.coursePointSelected", "event.eventsSelected",
        "event.eventSelected", "event.devicesSelected", "event.deviceSelected", "event.animationMove", "event.waypointsSelected",
        "event.waypointSelected", "event.trackpointSelected", "event.trackpointHighlighted", "event.zoomToItem", "event.trackpointsRemoved",
        "event.segmentsSelected", "event.segmentSelected", "event.segmentsCreated", "event.courseUpdated", "event.folderSelected", "event.miscelaneousSelection",
        "event.activityUpdated",
        "event.picturesSelected", "event.pictureSelected",  //58406
        "event.coordinatesTypeChanged" ,// 12335:, 2016-07-18
        "event.getCoordinates", "event.byButton", "event.noButton", "event.sendCoordinates"		//71052 2017-04-12
    };

    public static Event lookup(String eventName) {
        for (Event event : values()) {
            if (eventName.equals(event.getName())) {
                return event;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return Messages.getMessage(messageCodes[this.ordinal()]);
    }
}