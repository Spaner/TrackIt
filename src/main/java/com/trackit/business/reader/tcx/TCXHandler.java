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
package com.trackit.business.reader.tcx;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
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
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.CoursePointType;
import com.trackit.business.domain.CourseTrack;
import com.trackit.business.domain.DeviceInfo;
import com.trackit.business.domain.DeviceTypeType;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.GarminProductType;
import com.trackit.business.domain.IntensityType;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.LapTriggerType;
import com.trackit.business.domain.Product;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.SportType;
import com.trackit.business.domain.Track;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.reader.XMLFileHandler;



public class TCXHandler extends DefaultHandler implements XMLFileHandler {
	private static final String ACTIVITY = "Activity";
	private static final String SPORT = "Sport";
	private static final String ID = "Id";
	private static final String COURSE = "Course";
	private static final String NAME = "Name";
	private static final String LAP = "Lap";
	private static final String START_TIME = "StartTime";
	private static final String TOTAL_TIME_SECONDS = "TotalTimeSeconds";
	private static final String DISTANCE_METERS = "DistanceMeters";
	private static final String MAXIMUM_SPEED = "MaximumSpeed";
	private static final String CALORIES = "Calories";
	private static final String HEART_RATE = "HeartRateBpm";
	private static final String AVERAGE_HEART_RATE = "AverageHeartRateBpm";
	private static final String MAXIMUM_HEART_RATE = "MaximumHeartRateBpm";
	private static final String VALUE = "Value";
	private static final String CADENCE = "Cadence";
	private static final String RUN_CADENCE = "RunCadence";
	private static final String CADENCE_SENSOR = "CadenceSensor";
	private static final String SPEED = "Speed";
	private static final String WATTS = "Watts";
	private static final String AVERAGE_SPEED = "AvgSpeed";
	private static final String MAXIMUM_BIKE_CADENCE = "MaxBikeCadence";
	private static final String BEGIN_POSITION = "BeginPosition";
	private static final String END_POSITION = "EndPosition";
	private static final String BEGIN_ALTITUDE_METERS = "BeginAltitudeMeters";
	private static final String END_ALTITUDE_METERS = "EndAltitudeMeters";
	private static final String INTENSITY = "Intensity";
	private static final String TRIGGER_METHOD = "TriggerMethod";
	private static final String LATITUDE_DEGREES = "LatitudeDegrees";
	private static final String LONGITUDE_DEGREES = "LongitudeDegrees";
	private static final String TRACK = "Track";
	private static final String TRACKPOINT = "Trackpoint";
	private static final String TIME = "Time";
	private static final String ALTITUDE_METERS = "AltitudeMeters";
	private static final String COURSE_POINT = "CoursePoint";
	private static final String POINT_TYPE = "PointType";
	private static final String NOTES = "Notes";
	private static final String CREATOR = "Creator";
	private static final String UNIT_ID = "UnitId";
	private static final String VERSION_MAJOR = "VersionMajor";
	private static final String VERSION_MINOR = "VersionMinor";
	private static final String ACTIVITY_TRACKPOINT_EXTENSION_V2 = "TPX";
	private static final String BIKE = "Bike";
	private static final String FOOTPOD = "Footpod";
	private static final String AVG_RUN_CADENCE= "AVGRunCadence";
	private static final String MAX_RUN_CADENCE= "MaxRunCadence";
	private static final String AVG_WATTS = "AvgWatts";
	private static final String MAX_WATTS = "MaxWatts";

	private GPSDocument gpsDocument;
	private Activity activity;
	private Session session;
	private Course course;
	private Lap lap;
	private Track track;
	private Trackpoint trackpoint;
	private CoursePoint coursePoint;
	private String textData;
	private boolean inTrackpoint;
	private boolean inActivity;
	private boolean inCourse;
	private boolean inLap;
	private boolean inLapBeginPosition;
	private boolean inLapEndPosition;
	private boolean firstTrackpointInTrack;
	private boolean inHeartRate;
	private boolean inAverageHeartRate;
	private boolean inMaximumHeartRate;
	private boolean inCoursePoint;
	private boolean inCreator;
	private boolean cadenceSensorPresent;
	private DeviceTypeType sensor;
	private String version;
	
	private boolean readActivities;
	private boolean readCourses;
	
	private static Logger logger = Logger.getLogger(TCXHandler.class.getName());
	
	public TCXHandler(Map<String, Object> options) {
		super();
		
		readActivities = (Boolean) options.get(Constants.Reader.READ_ACTIVITIES);
		readCourses = (Boolean) options.get(Constants.Reader.READ_COURSES);
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
		gpsDocument = new GPSDocument(null);
		
		init();
	}

	@Override
	public void endDocument() throws SAXException {
		fixCoursesLaps();									//2018-05-05: 12335
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		try {
			if (localName.equalsIgnoreCase(ACTIVITY) && readActivities) {
				activity = new Activity();
				inActivity = true;
				
				session = new Session(activity);
				
				String sport = getAttribute(attributes, SPORT);
				SportType sportType;
				if (sport.equals("Biking")) {
					sportType = SportType.CYCLING;
				} else if (sport.equals("Running")) {
					sportType = SportType.RUNNING;
				} else {
					sportType = SportType.GENERIC;
				}
				session.setSport(sportType);
				
				activity.add(session);
			} else if (localName.equalsIgnoreCase(COURSE) && readCourses) {
				course = new Course();
				inCourse = true;
			} else if (localName.equalsIgnoreCase(LAP) && (readActivities || readCourses)) {
				if (inActivity) {
					lap = new ActivityLap(activity);
					String startTime = getAttribute(attributes, START_TIME);
					if (startTime != null && !startTime.isEmpty()) {
						lap.setStartTime(parseDateTime(startTime));
					}
					lap.setSport(session.getSport());
				} else if (inCourse) {
					lap = new CourseLap(course);
					String startTime = getAttribute(attributes, START_TIME);
					if (startTime != null && !startTime.isEmpty()) {
						lap.setStartTime(parseDateTime(startTime));
					}
				}
				inLap = true;
			} else if (localName.equalsIgnoreCase(BEGIN_POSITION) && inLap) {
				inLapBeginPosition = true;
			} else if (localName.equalsIgnoreCase(END_POSITION) && inLap) {
				inLapEndPosition = true;
			} else if (localName.equalsIgnoreCase(TRACK) && (readActivities || readCourses)) {
				if (inActivity) {
					track = new ActivityTrack(activity);
					if (lap.getStartTime() != null) {
						track.setStartTime(lap.getStartTime());
					}
				} else if (inCourse) {
					track = new CourseTrack(course);
				}
				firstTrackpointInTrack = true;
			} else if (localName.equalsIgnoreCase(TRACKPOINT) && (readActivities || readCourses)) {
				trackpoint = new Trackpoint((inCourse ? course : activity));
				inTrackpoint = true;
			} else if (localName.equalsIgnoreCase(AVERAGE_HEART_RATE) && (readActivities || readCourses)) {
				inAverageHeartRate = true;
			} else if (localName.equalsIgnoreCase(MAXIMUM_HEART_RATE) && (readActivities || readCourses)) {
				inMaximumHeartRate = true;
			} else if (localName.equalsIgnoreCase(HEART_RATE) && (readActivities || readCourses)) {
				inHeartRate = true;
			} else if (localName.equalsIgnoreCase(COURSE_POINT) && readCourses) {
				coursePoint = new CoursePoint("", course);
				inCoursePoint = true;
			} else if (localName.equalsIgnoreCase(CREATOR) && (readActivities || readCourses)) {
				inCreator = true;
			} else if (localName.equalsIgnoreCase(ACTIVITY_TRACKPOINT_EXTENSION_V2) && inActivity && inTrackpoint) {
				String sensorType = getAttribute(attributes, CADENCE_SENSOR); 
				if (sensorType != null) {
					if (sensorType.equals(BIKE)) {
						sensor = DeviceTypeType.BIKE_SPEED_CADENCE; 
						cadenceSensorPresent = true;
					} else if (sensorType.equals(FOOTPOD)) {
						sensor = DeviceTypeType.STRIDE_SPEED_DISTANCE;
						cadenceSensorPresent = true;
					}
				}
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
		
		textData = "";
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		textData = textData + new String(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try {
			if (localName.equalsIgnoreCase(CREATOR) && inActivity) {
				activity.setCreator(textData);
			} else if (localName.equalsIgnoreCase(CREATOR) && inCourse){
				course.setCreator(textData);
			} else if (localName.equalsIgnoreCase(ACTIVITY) && inActivity) {
				Session session = activity.getSessions().get(0);
				session.setStartTime(activity.getFirstLap().getStartTime());
				session.setEndTime(activity.getLastLap().getEndTime());
				
				activity.setStartTime(session.getStartTime());
				activity.setEndTime(session.getEndTime());
				activity.setName(Formatters.getSimpleDateFormat().format(activity.getStartTime()));
				
				if (cadenceSensorPresent) {
					DeviceInfo device = new DeviceInfo(activity);
					device.setDeviceIndex((short) 1);
					device.setDeviceType(sensor);
					device.setTime(activity.getFirstTrackpoint().getTimestamp());
					activity.add(device);
				}
				
				gpsDocument.add(activity);
				inActivity = false;
			} else if (localName.equalsIgnoreCase(COURSE) && inCourse) {
				gpsDocument.add(course);
				inCourse = false;
			} else if (localName.equalsIgnoreCase(LAP) && inLap ) {
				if ( inActivity) {
				if (trackpoint != null) {
					lap.setEndTime(trackpoint.getTimestamp());
				} else if (lap.getStartTime() != null && lap.getTimerTime() != null) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(lap.getStartTime());
					calendar.add(Calendar.MILLISECOND, (int) Math.round(lap.getTimerTime() * 1000.0));
					lap.setEndTime(calendar.getTime());
				}
				}
				
				if (inActivity) {
					activity.add(lap);
				} else if (inCourse) {
					course.add(lap);
				}
				inLap = false;
			} else if (localName.equalsIgnoreCase(BEGIN_POSITION) && inLap) {
				inLapBeginPosition = false;
			} else if (localName.equalsIgnoreCase(END_POSITION) && inLap) {
				inLapEndPosition = false;
			} else if (localName.equalsIgnoreCase(TRACK) && (inActivity || inCourse)) {
				track.setEndTime(trackpoint.getTimestamp());
				if (inActivity) {
					activity.add(track);
				} else if (inCourse) {
					course.add(track);
				}
			} else if (localName.equalsIgnoreCase(TRACKPOINT) && (inActivity || inCourse)) {
				if (firstTrackpointInTrack) {
					track.setStartTime(trackpoint.getTimestamp());//					lap.setStartTime( trackpoint.getTimestamp());		//2018-04-19 : 12335 - added
					firstTrackpointInTrack = false;
				}
				
				if (inActivity) {
					activity.add(trackpoint);
				} else if (inCourse) {
					course.getTrackpoints().add(trackpoint);
				}
				inTrackpoint = false;
			} else if (localName.equalsIgnoreCase(COURSE_POINT) && inCoursePoint) {
				course.add(coursePoint);
				inCoursePoint = false;
			} else if (localName.equalsIgnoreCase(ID) && inActivity) {
				activity.setName(Formatters.getSimpleDateFormat().format(parseDateTime(textData)));
			} else if (localName.equalsIgnoreCase(TOTAL_TIME_SECONDS) && inLap) {
				lap.setTimerTime(parseDouble(textData));
			} else if (localName.equalsIgnoreCase(DISTANCE_METERS) && (inLap || inTrackpoint)) {
				if (inTrackpoint) {
					trackpoint.setDistance(parseDouble(textData));
				} else if (inLap) {
					lap.setDistance(parseDouble(textData));
				}
			} else if (localName.equalsIgnoreCase(MAXIMUM_SPEED) && inLap) {
				lap.setMaximumSpeed(parseDouble(textData));
			} else if (localName.equalsIgnoreCase(CALORIES) && inLap) {
				lap.setCalories(Integer.parseInt(textData));
			} else if (localName.equalsIgnoreCase(VALUE) && (inLap || inHeartRate)) {
				if (inHeartRate) {
					trackpoint.setHeartRate(Short.parseShort(textData));
					inHeartRate = false;
				} else if (inAverageHeartRate) {
					lap.setAverageHeartRate(Short.parseShort(textData));
					inAverageHeartRate = false;
				} else if (inMaximumHeartRate) {
					lap.setMaximumHeartRate(Short.parseShort(textData));
					inMaximumHeartRate = false;
				}
			} else if (localName.equalsIgnoreCase(CADENCE) && (inLap || inTrackpoint)) {
				if (inTrackpoint) {
					trackpoint.setCadence(Short.parseShort(textData));
				} else if (inLap) {
					lap.setAverageCadence(Short.parseShort(textData));
				}
			} else if (localName.equalsIgnoreCase(TRIGGER_METHOD) && (inLap)) {
				lap.setTrigger(LapTriggerType.lookup(textData));
			} else if (localName.equalsIgnoreCase(INTENSITY) && inLap) {
				lap.setIntensity(IntensityType.lookup(textData));
			} else if (localName.equalsIgnoreCase(TIME) && (inTrackpoint || inCoursePoint)) {
				if (inCoursePoint) {
					coursePoint.setTime(parseDateTime(textData));
				} else if (inTrackpoint) {
					trackpoint.setTimestamp(parseDateTime(textData));
				}
			} else if (localName.equalsIgnoreCase(LATITUDE_DEGREES) && (inTrackpoint || inCoursePoint || inLap)) {
				if (inLapBeginPosition) {
					lap.setStartLatitude(parseDouble(textData));
				} else if (inLapEndPosition) {
					lap.setEndLatitude(parseDouble(textData));
				} else if (inCoursePoint) {
					coursePoint.setLatitude(parseDouble(textData));
				} else if (inTrackpoint) {
					trackpoint.setLatitude(parseDouble(textData));
				}
			} else if (localName.equalsIgnoreCase(LONGITUDE_DEGREES) && (inTrackpoint || inCoursePoint || inLap)) {
				if (inLapBeginPosition) {
					lap.setStartLongitude(parseDouble(textData));
				} else if (inLapEndPosition) {
					lap.setEndLongitude(parseDouble(textData));
				} else if (inCoursePoint) {
					coursePoint.setLongitude(parseDouble(textData));
				} else if (inTrackpoint) {
					trackpoint.setLongitude(parseDouble(textData));
				}
			} else if (localName.equalsIgnoreCase(ALTITUDE_METERS) && (inTrackpoint || inCoursePoint)) {
				if (inCoursePoint) {
					coursePoint.setAltitude(parseDouble(textData));
				} else if (inTrackpoint) {
					trackpoint.setAltitude(parseDouble(textData));
				}
			} else if (localName.equalsIgnoreCase(BEGIN_ALTITUDE_METERS) && inLap) {
				lap.setStartAltitude(parseDouble(textData));
			} else if (localName.equalsIgnoreCase(END_ALTITUDE_METERS) && inLap) {
				lap.setEndAltitude(parseDouble(textData));
			} else if (localName.equalsIgnoreCase(AVERAGE_SPEED) && inLap) {
				lap.setAverageSpeed(parseDouble(textData));
			} else if (localName.equalsIgnoreCase(MAXIMUM_BIKE_CADENCE) && inLap) {
				lap.setMaximumCadence(Short.parseShort(textData));
			} else if (localName.equalsIgnoreCase(DISTANCE_METERS) && inTrackpoint) {
				trackpoint.setDistance(parseDouble(textData));
			} else if (localName.equalsIgnoreCase(SPEED) && inTrackpoint) {
				trackpoint.setSpeed(parseDouble(textData));
			} else if (localName.equalsIgnoreCase(WATTS) && inTrackpoint) {
				trackpoint.setPower(Integer.parseInt(textData));
			} else if (localName.equalsIgnoreCase(RUN_CADENCE) && inTrackpoint) {
				trackpoint.setCadence(Short.parseShort(textData));
			} else if (localName.equalsIgnoreCase(AVG_RUN_CADENCE) && inLap) {
				lap.setAverageRunningCadence(Short.parseShort(textData));
			} else if (localName.equalsIgnoreCase(MAX_RUN_CADENCE) && inLap) {
				lap.setMaximumRunningCadence(Short.parseShort(textData));
			} else if (localName.equalsIgnoreCase(AVG_WATTS) && inLap) {
				lap.setAveragePower(Integer.parseInt(textData));
			} else if (localName.equalsIgnoreCase(MAX_WATTS) && inLap) {
				lap.setMaximumPower(Integer.parseInt(textData));
			} else if (localName.equalsIgnoreCase(NAME)) {
				if (inCoursePoint) {
					coursePoint.setName(textData);
				} else if (inCourse && !inCreator) {
					course.setName(textData);
				} else if (inActivity && inCreator) {
					Product product = GarminProductType.lookup(textData);
					if (product != null) {
						activity.getMetadata().setProduct(product);
					}
				}
			} else if (localName.equalsIgnoreCase(UNIT_ID) && (inActivity && inCreator)) {
				activity.getMetadata().setSerialNumber(Long.parseLong(textData));
			} else if (localName.equalsIgnoreCase(VERSION_MAJOR) && (inActivity && inCreator)) {
				version = textData;
			} else if (localName.equalsIgnoreCase(VERSION_MINOR) && (inActivity && inCreator)) {
				version = version + textData;
				activity.getMetadata().setSoftwareVersion(Integer.parseInt(version));
			} else if (localName.equalsIgnoreCase(POINT_TYPE) && inCoursePoint) {
				coursePoint.setType(CoursePointType.lookup(textData));
			} else if (localName.equalsIgnoreCase(NOTES) && (inCourse || inCoursePoint || inActivity)) {
				if (inCourse) {
					course.setNotes(textData);
				} else if (inCoursePoint) {
					coursePoint.setNotes(textData);
				} else if (inActivity) {
					activity.setNotes(textData);
				}
			} else if (localName.equalsIgnoreCase(CREATOR) && (inCreator)) {
				inCreator = false;
			}
			
			textData = "";
		} catch (ParseException pe) {
			pe.printStackTrace();
			throw new SAXException(pe.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	private void init() {
		inActivity = false;
		inCourse = false;
		inLap = false;
		inLapBeginPosition = false;
		inLapEndPosition = false;
		firstTrackpointInTrack = false;
		inTrackpoint = false;
		inCoursePoint = false;
		inHeartRate = false;
		inAverageHeartRate = false;
		inMaximumHeartRate = false;
		inCreator = false;
		cadenceSensorPresent = false;
		sensor = null;
	}
	
	private double parseDouble(String number) throws ParseException {
//12335: 2017-08-19
//		double result = Formatters.getDecimalFormat().parse(number).doubleValue();
		double result = Formatters.getDefaultDecimalFormat().parse(number).doubleValue();
		return result;
	}
	
	private Date parseDateTime(String dateTime) throws ParseException {
		final int DATE_FORMAT_SECONDS_LENGTH = 20;
		final int DATE_FORMAT_MILIS_LENGTH = 24;
		
		if (dateTime.length() == DATE_FORMAT_SECONDS_LENGTH) {
			return Formatters.getSimpleDateFormat().parse(dateTime);
		} else if (dateTime.length() == DATE_FORMAT_MILIS_LENGTH) {
			return Formatters.getSimpleDateFormatMilis().parse(dateTime);
		}
		
		throw new ParseException("DateTime format not recognized!", 0);
	}
	
	private String getAttribute(Attributes attributes, String attributeName) {
		for (int i = 0; i < attributes.getLength(); i++) {
			if (attributes.getLocalName(i).equals(attributeName)) {
				return attributes.getValue(i);
			}
		}
		
		return null;
	}
	
	//2018-05-05: 12335
	private void fixCoursesLaps() {
		for ( Course course: gpsDocument.getCourses() ) {			
			int trackNo = 0;
			List<Track> tracks = course.getTracks();
			Date startTime = null;
			Date endTime = null;
			for ( Lap lap: course.getLaps() ) {
				if ( startTime == null ) {
					startTime = tracks.get( trackNo).getStartTime();
				}
				endTime   = new Date( (new Double( lap.getTimerTime()*1000.)).longValue() + startTime.getTime());
				lap.setStartTime( startTime);
				lap.setEndTime( endTime);
				if ( endTime.equals( tracks.get( trackNo).getEndTime()) ) {
					startTime = null;
					trackNo++;
				} else {
					startTime = endTime;
					while( startTime.after( tracks.get( trackNo).getEndTime()) )
						trackNo++;
				}
			}
		}
	}
	
	public GPSDocument getGPSDocument() {
		return gpsDocument;
	}
}
