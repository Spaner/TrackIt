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
package com.trackit.business.common;

import java.util.Properties;

import javax.print.DocFlavor.STRING;

public class Constants {
	public static final String APP_NAME = "Track It!";
	public static final String APP_NAME_NORMALIZED = "TrackIt";
	public static final String APP_LINK = "trackit.henriquemalheiro.com";
	public static final String APP_VERSION = "1.3.2 alpha";

	
	public static final double PAUSE_SPEED_THRESHOLD = 0.75 * 1000.0 / 3600.0;

	/* Preferences constants */
	public static final String PREFS_ROOT_NODE = "/com/henriquemalheiro/trackit";

	public static final class PrefsCategories {
		public static final String GLOBAL = "Global";
		public static final String CONNECTION = "Connection";
		public static final String SUMMARY = "Summary";
		public static final String BROWSE = "Browse";
		public static final String MAPS = "Maps";
		public static final String CHART = "Chart";
		public static final String TABLE = "Table";
		public static final String LOG = "Log";
		public static final String READER = "Reader";
		public static final String WRITER = "Writer";
		public static final String OPERATION = "Operation";
		public static final String JOIN = "Join";
		public static final String EDITION = "Edition";
		public static final String COLOR = "Color";//58406
		public static final String PAUSE = "Pause";//58406
		public static final String SEGMENTS = "Segments";//57421
	}

	public static final class PrefsSubCategories {
		public static final String MILITARY_MAPS_PROVIDER = "Military Maps Provider";
	}

	public static final class GlobalPreferences {
		public static final String COUNTRY = "Country";
		public static final String LANGUAGE = "Language";
		public static final String COORDINATES = "Coordinates";			// 12335: 2016-07-08
		public static final String APPLICATION_X = "Application X";
		public static final String APPLICATION_Y = "Application Y";
		public static final String APPLICATION_WIDTH = "Application Width";
		public static final String APPLICATION_HEIGHT = "Application Height";
		public static final String SHOW_FOLDER = "Show Folder";
		public static final String SHOW_SUMMARY = "Show Summary";
		public static final String SHOW_BROWSE = "Show Browse";
		public static final String SHOW_MAP = "Show Map";
		public static final String SHOW_GRIDLINES = "Show Gridlines";	// 12335: 2017-05-01
		public static final String SHOW_CHART = "Show Chart";
		public static final String SHOW_DATA = "Show Data";
		public static final String SHOW_LOG = "Show Log";
		public static final String LAST_IMPORT_DIRECTORY = "Last Import Directory";
		public static final String LAST_EXPORT_DIRECTORY = "Last Export Directory";
		public static final String LAST_PICTURE_DIRECTORY = "Last Picture Directory";
	}

	public static final class ConnectionPreferences {
		public static final String USE_PROXY = "Use Proxy";
		public static final String HOST = "Host";
		public static final String PORT = "Port";
		public static final String DOMAIN = "Domain";
		public static final String USER = "User";
		public static final String PASS = "Pass";
	}

	public static final class MapPreferences {
		public static final String CACHE_LOCATION = "Cache Location";
		public static final String DEFAULT_PROVIDER = "Default Provider";
		public static final String DEFAULT_MAP_TYPE = "Default Map Type";
		public static final String MILITARY_MAPS_MAP25K_LOCATION = "Military Maps 25k Location";
		public static final String TRACK_SIMPLIFICATION_MAX_VALUE = "Track Simplification Max Value";
		public static final String RESOLUTION = "Military Maps Resolution";
	}

	public static final class SummaryPreferences {
	}

	public static final class BrowsePreferences {
	}

	public static final class ChartPreferences {
		public static final String CHART_TYPE = "Chart Type";
		public static final String CHART_MODE = "Chart Mode";
		public static final String SHOW_ELEVATION_PROFILE = "Show Elevation Profile";
		public static final String SHOW_SPEED_PROFILE = "Show Speed Profile";
		public static final String SHOW_HEART_RATE_PROFILE = "Show Heart Rate Profile";
		public static final String SHOW_CADENCE_PROFILE = "Show Cadence Profile";
		public static final String SHOW_POWER_PROFILE = "Show Power Profile";
		public static final String SHOW_TEMPERATURE_PROFILE = "Show Temperature Profile";
		public static final String SHOW_COURSE_POINTS = "Show Course Points";
		public static final String SHOW_CLIMBS = "Show Climbs";
		public static final String SHOW_DESCENTS = "Show Descents";
		public static final String SHOW_GRADE = "Show Grade";
		public static final String SHOW_GRADE_PROFILE = "Show Grade Profile";
		public static final String SHOW_SMOOTHED_DATA = "Show Smoothed Data";
		public static final String VERTICAL_AXIS_STEPS = "Vertical Axis Steps";
		public static final String DATA_SERIES_VERTICAL_SPACE = "Data Series Vertical Space";
		public static final String ELEVATION_SMOOTHING_FACTOR = "Elevation Smoothing Factor";
		public static final String HEART_RATE_SMOOTHING_FACTOR = "Heart Rate Smoothing Factor";
		public static final String SPEED_SMOOTHING_FACTOR = "Speed Smoothing Factor";
		public static final String CADENCE_SMOOTHING_FACTOR = "Cadence Smoothing Factor";
		public static final String POWER_SMOOTHING_FACTOR = "Power Smoothing Factor";
		public static final String TEMPERATURE_SMOOTHING_FACTOR = "Temperature Smoothing Factor";
	}

	public static final class TablePreferences {
	}

	public static final class LogPreferences {
	}

	public static final class JoinPreferences {
		public static final String WARN_DISTANCE_EXCEEDED = "Warn Distance Exceeded";
		public static final String WARNING_DISTANCE = "Warning Distance";
		public static final String MINIMUM_DISTANCE = "Minimum Distance";
		public static final String WARN_DISTANCE_BELOW = "Warn Distance Below";
		public static final String JOIN_OPTIONS = "Join Speed Options";
		public static final String JOIN_SPEED = "Join Speed Value";
		public static final String JOIN_TIME = "Join Time Value";
		public static final String FOLLOW_ROADS = "Follow Roads";
		public static final String ROUTING_TYPE = "Routing Type";
		public static final String TRANSPORT_MODE = "Transport Mode";
		public static final String AVOID_HIGHWAYS = "Avoid Highways";
		public static final String AVOID_TOLL_ROADS = "Avoid Toll Roads";
		public static final String ADD_COURSE_POINTS_AT_JUNCTIONS = "Add Course Points at Junctions";
		public static final String KEEP_ORIGINAL_TIMES_AT_POINT_REMOVAL = "Keep Original Times at Point Removal";
	}
	//57421
	public static final class SplitIntoSegmentsPreferences {
		public static final String NUMBER_OF_SEGMENTS = "Number of segments";
		public static final String DURATION_OF_SEGMENTS = "Duration of segments";
		public static final String LENGTH_OF_SEGMENTS = "Length of segments";
		public static final String IS_NUMBER = "Is Number";
		public static final String IS_DURATION = "Is Duration";
		public static final String IS_LENGTH = "Is Length";
	}

	/* Reader Options */
	public static class Reader {
		public static final String READ_FOLDERS = "readFolders";
		public static final String READ_ACTIVITIES = "readActivities";
		public static final String READ_COURSES = "readCourses";
		public static final String READ_WAYPOINTS = "readWaypoints";
		public static final String READ_INTO_MULTIPLE_DOCUMENTS = "readIntoMultipleDocuments";
		public static final String FILENAME = "filename";
		public static final String VALIDATE_DOCUMENT = "validateDocument";
	}

	/* Writer Options */
	public static class Writer {
		public static final String OUTPUT_DIR = "writerOutputDir";
		public static final String WRITE_FOLDERS = "writeFolders";
		public static final String WRITE_ACTIVITIES = "writeActivities";
		public static final String WRITE_COURSES = "writeCourses";
		public static final String WRITE_WAYPOINTS = "writeWaypoints";
		public static final String WRITE_COURSE_EXTENDED_INFO = "writeCourseExtendedInfo";
	}

	/* KML Options */
	public static class KML {
		public static final String ANIMATION_INFO = "animationInfo";
		public static final String LAPS_NAME = "lapsName";
		public static final String LAPS_DESCRIPTION = "lapsDescription";
		public static final String LAP_NAME = "lapName";
		public static final String TRACK_NAME = "trackName";
		public static final String TRACK_COLOR = "trackColor";
		public static final String TRACK_WIDTH = "trackWidth";
		public static final String TRACK_EXTRUDE = "trackExtrude";
		public static final String TRACK_TESSELLATE = "trackTesselate";
		public static final String TRACK_ALTITUDE_MODE = "trackAltitudeMode";
		public static final String COURSE_POINTS_NAME = "coursePointsName";
		public static final String COURSE_POINTS_DESCRIPTION = "coursePointsDescription";
	}

	/* GPX Options */
	public static class GPX {
		public static final String APP_NAME = "appName";
		public static final String APP_LINK = "appLink";
		public static final String VERSION = "version";
	}

	/* Map Attributes */
	public static class MAP_ATTRIBUTES {
	}

	/* Painting Attributes */
	public static class PAINTING_ATTRIBUTES {
		public static final String COLOR_SCHEME = "colorScheme";
		public static final String MAP_PAINTER_STYLE = "mapPainterStyle";
		public static final String FILL_COLOR = "fillColor";
		public static final String WIDTH = "width";
		public static final String LINE_WIDTH = "lineWidth";
		public static final String REGULAR_LINE_COLOR = "regularLineColor";
		public static final String SELECTION_LINE_COLOR = "selectionLineColor";
		public static final String HIGHLIGHT_LINE_COLOR = "higlightLineColor";
		public static final String REGULAR_FILL_COLOR = "regularFillColor";
		public static final String SELECTION_FILL_COLOR = "selectionFillColor";
		public static final String HIGHLIGHT_FILL_COLOR = "higlightFillColor";
		public static final String SIMPLIFY = "simplify";
	}

	/* Operation Options */
	public static class Operation {
		public static final String PROCESS_FOLDERS = "processFolders";
		public static final String PROCESS_ACTIVITIES = "processActivities";
		public static final String PROCESS_COURSES = "processCourses";
		public static final String PROCESS_WAYPOINTS = "processWaypoints";
	}

	/* ConsolidationOperation Operation Options */
	public static class ConsolidationOperation {
		public static final String NAME = "ConsolidationOperation";
		public static final String LEVEL = "Level";
	}

	/* Detect Climbs & Descents Operation Options */
	public static class DetectClimbsAndDescentsOperation {
		public static final String NAME = "Create Segments based on Grade";
		public static final String CLIMB_GRADE_LOWER_LIMIT = "ClimbGradeLowerLimit";
		public static final String DESCENT_GRADE_LOWER_LIMIT = "DescentGradeLowerLimit";
		public static final String FINE_TUNNING = "FineTunning";
	}

	/* Mark Climbs & Descents Operation Options */
	public static class MarkingOperation {
		public static final String NAME = "Marking Climbs & Descents";
		public static final String MARK_CLIMB_START = "Mark Climb Start";
		public static final String MARK_CLIMB_FINISH = "Mark Climb Finish";
		public static final String MARK_CLIMB_MAX_GRADE = "Mark Climb Max Grade";
		public static final String MARK_CLIMB_MIN_GRADE = "Mark Climb Min Grade";
		public static final String MARK_DESCENT_START = "Mark Descent Start";
		public static final String MARK_DESCENT_FINISH = "Mark Descent Finish";
		public static final String MARK_DESCENT_MAX_GRADE = "Mark Descent Max Grade";
		public static final String MARK_DESCENT_MIN_GRADE = "Mark Descent Min Grade";
		public static final String REMOVE_EXISTING_MARKS = "Remove Existing Marks";
	}

	/* Altitude Smoothing Operation Options */
	public static class AltitudeSmoothingOperation {
		public static final String NAME = "Altitude Smoothing";
	}

	/* Activities to Courses Operation Options */
	public static class ActivitiesToCoursesOperation {
		public static final String NAME = "Activities to Courses";
		public static final String REMOVE_ACTIVITIES = "RemoveActivities";
	}

	/* Split at Selected Operation Options */
	public static class SplitAtSelectedOperation {
		public static final String NAME = "Split at Selected";
		public static final String COURSE = "course";
		public static final String TRACKPOINT = "Trackpoint";
		public static final String OLD_NAME = "Old Name";
		public static final String OLD_ID = "Old ID";
	}
	
	/* Split Into Segments Operation Options */ //57421
	public static class SplitIntoSegmentsOperation {
		public static final String NAME = "Split into segments";
		public static final String COURSE = "course";
		public static final String SEGMENT = "segment";
		public static final String VALUE = "Value";
		public static final String OLD_NAME = "Old Name";
		public static final String OLD_ID = "Old ID";
	}
	
	
	
	/* Copy Operation Options */
//	public static class CopyOperation {
//		public static final String NAME = "Copy selected";
//		public static final String COURSE = "course";
//	}
	
	/* CopyTo Operation Options */						//12335: 2017-04-06
	public static class CopyToOperation {
		public static final String ADD_TO_UNDO_MANAGER          = "AddToUndoManager";
		public static final String ITEMS                        = "Items";
		public static final String TARGET_ITEMS_IDS             = "TargetItemsIDs";
		public static final String TARGET_DOCUMENTS_IDS         = "TargetDocumentsIDs";
		public static final String DESTINATION_DOCUMENT_ID      = "DestinationDocumentID";
		public static final String DESTINATION_DOCUMENT_CREATED = "DestinationDocumentCreated";
	}
	
	/* Duplicate Operation Options */					//12335: 2017-04-06
	public static class DuplicateOperation {
		public static final String ADD_TO_UNDO_MANAGER    = "AddToUndoManager";
		public static final String ITEMS                  = "Items";
		public static final String IDS_TO_SET             = "IDsToSet";
		public static final String EXTRA_IDS_TO_SET       = "ExtraIDsToSet";
		public static final String ORIGIN_ITEMS_IDS       = "OriginItemsIDs";
	}
	
	/*Undo Options 57421*/
	public static class UndoOperation{
		public static final String UNDO = "undo";
		public static final String REDO = "redo";
	}

	/* Join Operation Options */
	public static class JoinOperation {
		public static final String NAME = "Join";
		public static final String COURSES = "courses";
		public static final String ADD_LAP_MARKER = "Add Lap Marker";
		public static final String MERGE_JOIN = "merge Join";
	}
	
	/* Reverse Operation Options 57421 */
	public static class ReverseOperation {
		public static final String NAME = "Reverse";	//12335: 2017-04-17
		public static final String NORMAL = "Normal";
		public static final String RETURN = "Return";
		public static final String RETURN_NEW = "Return New";
	}
	

	/* Set PaceMaker Operation Options */
	public static class SetPaceOperation {
		public static final String NAME = "Set PaceMaker";
		public static final String COURSES = "Courses";
		public static final String METHOD = "Method";
		public static final String SPEED = "Speed";
		public static final String TIME = "Time";
		public static final String PERCENTAGE = "Percentage";
		public static final String INCLUDE_PAUSES = "IncludePauses";
		public static final String WEIGHT = "Weight";
		public static final String SPORT = "Sport";
		public static final String NEW_START_DATE = "New Start Date";
		public static final String NEW_END_DATE = "New End Date";
		public static final String OLD_START_DATE = "Old Start Date";
		public static final String OLD_END_DATE = "Old End Date";
	}

	/* Sandbox Operation Options */
	public static class SandboxOperation {
		public static final String NAME = "Sandbox";
	}

	/* Track Simplification Operation Options */
	public static class TrackSimplificationOperation {
		public static final String NAME = "Track Simplification";
		public static final String COURSES = "courses";
		public static final String NUMBER_OF_POINTS = "numberOfPoints";
		public static final String REMOVE_TRACKPOINTS = "removeTrackpoints";
	}

	public static final class RemovePausesOperation {
		public static final String SPEED_THRESHOLD = "Speed Threshold";
	}

	public static final class EditionPreferences {
		public static final String FOLLOW_ROADS = "Follow Roads";
		public static final String ROUTING_TYPE = "Routing Type";
		public static final String TRANSPORT_MODE = "Transport Mode";
		public static final String AVOID_HIGHWAYS = "Avoid Highways";
		public static final String AVOID_TOLL_ROADS = "Avoid Toll Roads";
		public static final String ADD_COURSE_POINTS_AT_JUNCTIONS = "Add Course Points at Junctions";
		public static final String KEEP_ORIGINAL_TIMES_AT_POINT_REMOVAL = "Keep Original Times at Point Removal";//58406
		
	}

	public static final class RoutingOptions {
		public static final String ROUTING_TYPE = "Routing Type";
		public static final String TRANSPORT_MODE = "Transport Mode";
		public static final String AVOID_HIGHWAYS = "Avoid Highways";
		public static final String AVOID_TOLL_ROADS = "Avoid Toll Roads";
		public static final String ADD_DIRECTION_COURSE_POINTS = "Add Direction Course Points";
	}

	public static final class SmoothingOperation {
		public static final String NAME = "Smoothing";
		public static final String FACTOR = "Smoothing Factor";
	}
	
	// 58406
	// Color customization fields
	public static final class ColorPreferences {
		public static final String FILL_RGB = "Fill RGB";
		public static final String LINE_RGB = "Line RGB";
		public static final String SEL_FILL_RGB = "Selected Fill RGB";
		public static final String SEL_LINE_RGB = "Selected Line RGB";
		public static final String FIRST_COLOR_FILL = "Fill Color";
		public static final String FIRST_COLOR_LINE = "Line Color";
		public static final String FIRST_COLOR_FILL_SELECT = "Fill Color when selected";
		public static final String FIRST_COLOR_LINE_SELECT = "Line Color when selected";
		public static final String AC_TRACE_COLOR = "Activity/Course Trace Color";
		public static final String THUMBNAIL_FRAME_COLOR = "Photographs Thumbnail Frame Color";
	}
	
	public static class PauseDetectionOperation {
		public static final String NAME = "Pause Detection";
	}
	
	public static final class PausePreferences {
		public static final String SPEED_THRESHOLD = "Speed Threshold";
	}
	
	public static final class PauseDialogOptions {
		public static final String CHANGE_DURATION = "Change Duration";
		public static final String ADD_PAUSE = "Add Pause";
	}
	
	public static final class ExtraUndoOptions{
		public static final String ADD_TO_MANAGER = "Add to Undo Manager";
		public static final String SPLIT_UNDO = "Split Undo";
		public static final String JOIN_UNDO = "Join Undo";
		public static final String APPEND_UNDO = "Append Undo";
		public static final String EXTRA_ID = "Extra ID";
	}
	
	public static final double DEFAULT_SPEED_MS = 10 * 1000.0 / 3600.0;
	
}