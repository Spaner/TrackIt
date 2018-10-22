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
package com.trackit.business.reader.gpx;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.trackit.business.common.Constants;
import com.trackit.business.common.Formatters;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.ActivityLap;
import com.trackit.business.domain.ActivityTrack;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CourseLap;
import com.trackit.business.domain.CourseTrack;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.Track;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.Waypoint;
import com.trackit.business.reader.XMLFileHandler;


public class GPXHandler extends DefaultHandler implements XMLFileHandler {
	static enum GPXElement {
		GPX("gpx"),
		CREATOR("creator"),
		NAME("name"),
		TRACK("trk"),
		ROUTE("rte"),
		TRACK_SEGMENT("trkseg"),
		ROUTEPOINT("rtept"),
		TRACKPOINT("trkpt"),
		LATITUDE("lat"),
		LONGITUDE("lon"),
		ELEVATION("ele"),
		TIME("time"),
		HEART_RATE("hr"),
		CADENCE("cad"),
		TEMPERATURE("atemp"),
		SPEED("speed"),
		WAYPOINT("wpt"),
		COMMENTS("cmt"),
		DESCRIPTION("desc"),
		SOURCE("src"),
		SYMBOL("sym"),
		TYPE("type"),
		LINK("link"),
		HREF("href"),
		BOUNDS("bounds"),
		MIN_LAT("minlat"),
		MIN_LON("minlon"),
		MAX_LAT("maxlat"),
		MAX_LON("maxlon"),
		TEXT("text"),
		METADATA("metadata"),
		HEART_RATE_TRACK("heartRateTrack"),
		TRACK_HEART_RATE("heartRate"),
		BPM("bpm"),
		UNKNOWN("unknown");
		
		private String name;
		
		GPXElement(String name) {
			this.name = name;
		}
		
		static GPXElement lookup(String elementName) {
			GPXElement element = UNKNOWN;
			for (GPXElement currentElement : values()) {
				if (currentElement.name.equalsIgnoreCase(elementName)) {
					element = currentElement;
					break;
				}
			}
			return element;
		}
	}
	
	private static final String KEYMAZE = "KeyMaze";
	private static final String NEW_LINE = "\n";
	
	private Map<String, Object> options;
	private GPSDocument gpsDocument;
	private Activity activity;
	private Course course;
	private Lap lap;
	private Trackpoint trackpoint;
	private Waypoint waypoint; 
	private String name;
	private String description;
	private String comments;
	private String link;
	private String creator;
	private Date creationTime;
	private double[] bounds;
	private String type;
	private String textData;
	private boolean inTrackpoint;
	private boolean inWaypoint;
	private boolean inMetadata;
	private boolean hasExtensions;
	private List<Trackpoint> trackpoints;
	private List<Trackpoint> lapTrackpoints;
	private String notes;
	private Map<String, Short> heartRateMap;
	
	private boolean readActivities;
	private boolean readCourses;
	private boolean readWaypoints;
	
	private static Logger logger = Logger.getLogger(GPXHandler.class.getName());
	
	public GPXHandler(Map<String, Object> options) {
		super();
		
		this.options = options;
		readActivities = (Boolean) options.get(Constants.Reader.READ_ACTIVITIES);
		readCourses = (Boolean) options.get(Constants.Reader.READ_COURSES);
		readWaypoints = (Boolean) options.get(Constants.Reader.READ_WAYPOINTS);
	}
	
	@Override
	public void startDocument() throws SAXException {
		reset();
		gpsDocument = new GPSDocument((String) options.get(Constants.Reader.FILENAME));
	}

	private void reset() {
		course = null;
		lap = null;
		trackpoint = null;
		waypoint = null; 
		name = null;
		description = null;
		comments = null;
		type = null;
		textData = null;
		inTrackpoint = false;
		inWaypoint = false;
		inMetadata = false;
		hasExtensions = false;
		trackpoints = new ArrayList<Trackpoint>();
		lapTrackpoints = null;
		notes = null;
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		
		GPXElement element = GPXElement.lookup(localName);
		switch (element) {
		case GPX:
			processGPXStart(attributes);
			break;
		case TRACK:
			processTrackStart();
			break;
		case ROUTE:
			processRouteStart();
			break;
		case TRACK_SEGMENT:
			processTrackSegmentStart();
			break;
		case TRACKPOINT:
			processTrackpointStart(attributes);
			break;
		case ROUTEPOINT:
			processRoutepointStart(attributes);
			break;
		case WAYPOINT:
			processWaypointStart(attributes);
			break;
		case BOUNDS:
			processBoundsStart(attributes);
			break;
		case LINK:
			processLinkStart(attributes);
			break;
		case METADATA:
			processMetadataStart();
			break;
		case TRACK_HEART_RATE:
			processTrackHeartRateStart(attributes);
			break;
		default:
			// Ignore element
		}
		
		clearReadData();
	}
	
	private void processGPXStart(Attributes attributes) {
		String creator = getValue(attributes, GPXElement.CREATOR);
		if (creator != null && !creator.isEmpty()) {
			this.creator = creator;
		}
	}
	
	private void processTrackStart() {
		if (readActivities || readCourses) {
			trackpoints = new ArrayList<Trackpoint>();
		}
	}
	
	private void processRouteStart() {
		if (readCourses) {
			trackpoints = new ArrayList<Trackpoint>();
		}
	}
	
	private void processTrackSegmentStart() {
		lapTrackpoints = new ArrayList<Trackpoint>();
	}
	
	private void processRoutepointStart(Attributes attributes) throws SAXException {
		if (readCourses) {
			trackpoint = new Trackpoint(null);
			inTrackpoint = true;
			
			try {
				String latitude = getValue(attributes, GPXElement.LATITUDE);
				trackpoint.setLatitude(parseDouble(latitude));
				
				String longitude = getValue(attributes, GPXElement.LONGITUDE);
				trackpoint.setLongitude(parseDouble(longitude));
			} catch (ParseException e) {
				logger.error(e.getMessage());
				throw new SAXException(e.getMessage());
			}
		}
	}
		
	private void processTrackpointStart(Attributes attributes) throws SAXException {
		if (readActivities || readCourses) {
			trackpoint = new Trackpoint(null);
			inTrackpoint = true;
			
			try {
				String latitude = getValue(attributes, GPXElement.LATITUDE);
				trackpoint.setLatitude(parseDouble(latitude));
				
				String longitude = getValue(attributes, GPXElement.LONGITUDE);
				trackpoint.setLongitude(parseDouble(longitude));
			} catch (ParseException e) {
				logger.error(e.getMessage());
				throw new SAXException(e.getMessage());
			}
		}
	}
	
	private void processWaypointStart(Attributes attributes) throws SAXException {
		if (readWaypoints) {
			waypoint = new Waypoint();
			waypoint.setParent(gpsDocument);
			inWaypoint = true;
			
			try {
				String latitude = getValue(attributes, GPXElement.LATITUDE);
				waypoint.setLatitude(parseDouble(latitude));
				
				String longitude = getValue(attributes, GPXElement.LONGITUDE);
				waypoint.setLongitude(parseDouble(longitude));
			} catch (ParseException e) {
				logger.error(e.getMessage());
				throw new SAXException(e.getMessage());
			}
		}
	}
	
	private void processBoundsStart(Attributes attributes) throws SAXException {
		if (readActivities || readCourses) {
			bounds = new double[4];
			
			try {
				String minLatitude = getValue(attributes, GPXElement.MIN_LAT);
				bounds[0] = parseDouble(minLatitude);

				String minLongitude = getValue(attributes, GPXElement.MIN_LON);
				bounds[1] = parseDouble(minLongitude);
				
				String maxLatitude = getValue(attributes, GPXElement.MAX_LAT);
				bounds[2] = parseDouble(maxLatitude);
				
				String maxLongitude = getValue(attributes, GPXElement.MAX_LON);
				bounds[3] = parseDouble(maxLongitude);
			} catch (ParseException e) {
				logger.error(e.getMessage());
				throw new SAXException(e.getMessage());
			}
		}
	}
	
	private void processLinkStart(Attributes attributes) {
		if (readActivities || readCourses) {
			link = getValue(attributes, GPXElement.HREF);
		}
	}
	
	private void processMetadataStart() {
		inMetadata = true;
	}
	
	private void processTrackHeartRateStart(Attributes attributes) throws SAXException {
		if (readActivities || readCourses) {
			trackpoint = new Trackpoint(null);
			inTrackpoint = true;
			
			try {
				String timeValue = getValue(attributes, GPXElement.TIME);
				String time = Formatters.getSimpleDateFormat().format(parseDateTime(timeValue));
				
				String heartRateValue = getValue(attributes, GPXElement.BPM);
				Short heartRate = Short.parseShort(heartRateValue);
				
				if (heartRateMap == null) {
					heartRateMap = new HashMap<String, Short>();
				}
				
				heartRateMap.put(time, heartRate);
			} catch (ParseException e) {
				logger.error(e.getMessage());
				throw new SAXException(e.getMessage());
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		textData = textData + new String(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		GPXElement element = GPXElement.lookup(localName);
		switch (element) {
		case TRACK:
			processTrackEnd();
			break;
		case ROUTE:
			processRouteEnd();
			break;
		case TRACK_SEGMENT:
			processTrackSegmentEnd();
			break;
		case ROUTEPOINT:
			processRoutepointEnd();
			break;
		case TRACKPOINT:
			processTrackpointEnd();
			break;
		case WAYPOINT:
			processWaypointEnd();
			break;
		case NAME:
			processNameEnd();
			break;
		case ELEVATION:
			processElevationEnd();
			break;
		case TIME:
			processTimeEnd();
			break;
		case HEART_RATE:
			processHeartRateEnd();
			break;
		case CADENCE:
			processCadenceEnd();
			break;
		case SPEED:
			processSpeedEnd();
			break;
		case TEMPERATURE:
			processTemperatureEnd();
			break;
		case COMMENTS:
			processCommentsEnd();
			break;
		case DESCRIPTION:
			processDescriptionEnd();
			break;
		case SOURCE:
			processSourceEnd();
			break;
		case SYMBOL:
			processSymbolEnd();
			break;
		case TYPE:
			processTypeEnd();
			break;
		case TEXT:
			processTextEnd();
			break;
		case METADATA:
			processMetadataEnd();
			break;
		default:
			// Do nothing
		}
		
		clearReadData();
	}
	
	private void processTrackEnd() {
		if (readActivities || readCourses) {
			processNotes();
			
			if (hasExtensions) {
				processActivity();
			} else {
				processCourse();
			}
		}
		
		reset();
	}
	
	private void processRouteEnd() {
		if (readCourses) {
			processNotes();
			processCourse();
		}
	}
	
	private void processNotes() {
		StringBuilder notes = new StringBuilder();
		
		if (description != null) {
			notes.append(description).append(NEW_LINE);
		}
		
		if (comments != null) {
			notes.append(comments).append(NEW_LINE);
		}
		
		if (type != null) {
			notes.append("Track Type: ").append(type).append(NEW_LINE);
		}
		
		if (link != null) {
			notes.append(link).append(NEW_LINE);
		}
		
		if (creationTime != null) {
			String creationTimeStr = Formatters.getSimpleDateFormat().format(creationTime);
			notes.append(creationTimeStr).append(NEW_LINE);
		}
		
		this.notes = notes.toString();
	}
	
	private void processActivity() {
		if (activity == null) {
			activity = new Activity();
		}
		
		activity.setName(name);
		activity.setNotes(notes);
		activity.setCreator(creator);//58406
		
		Session session = getSession(activity);
		activity.add(session);
		
		activity.setStartTime(session.getStartTime());
		activity.add(trackpoints);
		
		boolean heartRateMapPresent = (heartRateMap != null);
		
		for (Trackpoint currentTrackpoint : trackpoints) {
			currentTrackpoint.setParent(activity);
			
			if (heartRateMapPresent) {
				String timestamp = Formatters.getSimpleDateFormat().format(currentTrackpoint.getTimestamp());
				Short heartRate = heartRateMap.get(timestamp);
				currentTrackpoint.setHeartRate(heartRate);
			}
		}
		
		heartRateMap = null;

		gpsDocument.add(activity);
		activity = null;
	}
	
	private Session getSession(Activity activity) {
		Session session = new Session(activity);
		session.setStartTime(trackpoints.get(0).getTimestamp());
		session.setEndTime(trackpoints.get(trackpoints.size() - 1).getTimestamp());
		
		if (bounds != null) {
			session.setSouthwestLatitude(bounds[0]);
			session.setNortheastLongitude(bounds[1]);
			session.setNortheastLatitude(bounds[2]);
			session.setSouthwestLongitude(bounds[3]);
		}
		
		return session;
	}
	
	private void processCourse() {
		if (course == null) {
			course = new Course();
		}
		course.setName(name);
		course.setNotes(notes);
		course.addTrackpoints(trackpoints);
		
		//course.setStartTime(course.getTrackpoints().get(0).getTimestamp());
		//course.setEndTime(course.getTrackpoints().get(course.getTrackpoints().size()-1).getTimestamp());
		
		course.setCreator(creator);//58406
		
		boolean heartRateMapPresent = (heartRateMap != null);
		for (Trackpoint currentTrackpoint : trackpoints) {
			currentTrackpoint.setParent(course);
			
			if (heartRateMapPresent) {
				String timestamp = Formatters.getSimpleDateFormat().format(currentTrackpoint.getTimestamp());
				Short heartRate = heartRateMap.get(timestamp);
				currentTrackpoint.setHeartRate(heartRate);
			}
		}
		heartRateMap = null;
		
		if (bounds != null) {
			course.setSouthwestLatitude(bounds[0]);
			course.setNortheastLongitude(bounds[1]);
			course.setNortheastLatitude(bounds[2]);
			course.setSouthwestLongitude(bounds[3]);
		}

		gpsDocument.add(course);
		course = null;
	}
	
	private void processTrackSegmentEnd() {
		if (readActivities || readCourses) {
			Lap lap;
			Track track;

			if (hasExtensions) {
				if (activity == null) {
					activity = new Activity();
				}
				track = new ActivityTrack(activity);
				activity.add(track);
				
				lap = new ActivityLap(activity);
				activity.add(lap);
			} else {
				if (course == null) {
					course = new Course();
				}
				track = new CourseTrack(course);
				course.add(track);
				
				lap = new CourseLap(course);
				course.add(lap);
			}
			
			track.setStartTime(lap.getStartTime());
			track.setEndTime(lap.getEndTime());
			
			lap.setTrackpoints(lapTrackpoints);
			lapTrackpoints = null;
		}
	}
	
	private void processRoutepointEnd() {
		if (readCourses) {
			trackpoints.add(trackpoint);
			inTrackpoint = false;
			
			if (lap != null && trackpoint.getTimestamp() != null) {
				lap.setEndTime(trackpoint.getTimestamp());
				
				Track track = new CourseTrack(course);
				track.setStartTime(lap.getStartTime());
				track.setEndTime(lap.getEndTime());
				course.add(track);
			}
			lap = null;
		}
	}
	
	private void processTrackpointEnd( ) {
		if (readActivities || readCourses) {
			trackpoints.add(trackpoint);
			lapTrackpoints.add(trackpoint);
			inTrackpoint = false;
		}
	}
	
	private void processWaypointEnd() {
		if (readWaypoints) {
			gpsDocument.add(waypoint);
			inWaypoint = false;
		}
	}
	
	private void processNameEnd() {
		//12335: 2016-09-21 to get the name of the whole document
		if ( inMetadata )
			gpsDocument.setName(textData);
		// end
		if (readActivities || readCourses || readWaypoints) {
			if (inWaypoint) {
				waypoint.setName(textData);
			} else {
				name = textData;
			}
		}
	}
	
	private void processElevationEnd() throws SAXException {
		try {
			if (inTrackpoint) {
				trackpoint.setAltitude(parseDouble(textData));
			} else if (inWaypoint) {
				waypoint.setAltitude(parseDouble(textData));
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processTimeEnd() throws SAXException {
		try {
			if (inMetadata) {
				creationTime = parseDateTime(textData);
			} else  if (inTrackpoint) {
				trackpoint.setTimestamp(parseDateTime(textData));
			} else if (inWaypoint) {
				waypoint.setTime(parseDateTime(textData));
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
		
	private void processHeartRateEnd() throws SAXException {
		try {
			if (inTrackpoint) {
				trackpoint.setHeartRate((short) parseDouble(textData));
				hasExtensions = true;
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processCadenceEnd() throws SAXException {
		try {
			if (inTrackpoint) {
				trackpoint.setCadence((short) parseDouble(textData));
				hasExtensions = true;
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processTemperatureEnd() throws SAXException{
		try {
			if (inTrackpoint) {
				trackpoint.setTemperature((byte) parseDouble(textData));
				hasExtensions = true;
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processSpeedEnd() throws SAXException {
		try {
			if (inTrackpoint) {
				double speed = parseDouble(textData);
				if (creator != null && !creator.isEmpty() && creator.contains(KEYMAZE)) {
					speed = speed / 10.0 * 1000.0 / 3600.0;
				}
				trackpoint.setSpeed(speed);
				hasExtensions = true;
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processCommentsEnd() {
		if (inWaypoint) {
			waypoint.setComments(textData);
		} else {
			comments = textData;
		}
	}
	
	private void processDescriptionEnd() {
		if (inWaypoint) {
			waypoint.setDescription(textData);
		} else {
			description = textData;
		}
	}
	
	private void processSourceEnd() {
		if (inWaypoint) {
			waypoint.setSource(textData);
		}
	}
	
	private void processSymbolEnd() {
		if (inWaypoint) {
			waypoint.setSym(textData);
		}
	}
	
	private void processTypeEnd() {
		if (inWaypoint) {
			waypoint.setType(textData);
		} else {
			type = textData;
		}
	}
	
	private void processTextEnd() {
		StringBuilder linkText = new StringBuilder();
		linkText.append(textData);
		linkText.append(" (");
		linkText.append(link != null ? link : "");
		linkText.append(")");
		link = linkText.toString();
	}
	
	private void processMetadataEnd() {
		inMetadata = false;
	}

	private void clearReadData() {
		textData = "";
	}
	
	private String getValue(Attributes attributes, GPXElement element) {
		String value = "";
		for (int i = 0; i < attributes.getLength(); i++) {
	        String name = attributes.getQName(i);
	        GPXElement currentElement = GPXElement.lookup(name);
	        if (currentElement == element) {
	        	value = attributes.getValue(i);
	        	break;
	        }
	    }
		return value;
	}
	
	private double parseDouble(String number) throws ParseException {
//		return Formatters.getDecimalFormat().parse(number).doubleValue();		//12335: 2017-08-06
		return Formatters.getDefaultDecimalFormat().parse(number).doubleValue();
	}

	private Date parseDateTime(String dateTime) throws ParseException {
		return Formatters.parseDate(dateTime);
	}
	
	@Override
	public GPSDocument getGPSDocument() {
		return gpsDocument;
	}
}
