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
package com.henriquemalheiro.trackit.business.writer.tcx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.IntensityType;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.SensorSourceType;
import com.henriquemalheiro.trackit.business.domain.SensorStateType;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.Track;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.WriterException;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterTemplate;

public class TCXFileWriter extends WriterTemplate implements Writer {
	private static final String TCX = "TrainingCenterDatabase";
	private static final String FOLDERS = "Folders";
	private static final String ACTIVITIES = "Activities";
	private static final String ACTIVITY = "Activity";
	private static final String SPORT = "Sport";
	private static final String ID = "Id";
	private static final String COURSES = "Courses";
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
	private static final String SPEED = "Speed";
	private static final String RUN_CADENCE = "RunCadence";
	private static final String WATTS = "Watts";
	private static final String POSITION = "Position";
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
	private static final String EXTENSIONS = "Extensions";
	private static final String AVERAGE_SPEED = "AvgSpeed";
	private static final String MAXIMUM_BIKE_CADENCE = "MaxBikeCadence";
	private static final String AVERAGE_RUN_CADENCE = "AvgRunCadence";
	private static final String MAXIMUM_RUN_CADENCE = "MaxRunCadence";
	private static final String AVERAGE_WATTS = "AvgWatts";
	private static final String MAXIMUM_WATTS = "MaxWatts";
	private static final String CADENCE_SENSOR = "CadenceSensor";
	private static final String SENSOR_STATE = "SensorState";
	private static final String ACTIVITY_LAP_EXTENSION_V2 = "LX";
	private static final String ACTIVITY_TRACKPOINT_EXTENSION_V2 = "TPX";
	
	private static final String NAMESPACE_XMLNS = "http://www.w3.org/2000/xmlns/";
	private static final String NAMESPACE_XSI = "xmlns:xsi";
	private static final String NAMESPACE_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String NAMESPACE_DEFAULT = "xmlns";
	private static final String NAMESPACE_TCX = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2";
	private static final String NAMESPACE_XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
	private static final String NAMESPACE_TCX_SCHEMA_LOCATION = "http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd";
	
	private static final String PREFIX_GARMIN_ACTIVITY_LAP_EXTENSION_V2 = "ns3";
	private static final String PREFIX_GARMIN_ACTIVITY_TRACKPOINT_EXTENSION_V2 = "ns3";
	
	private static final String NAMESPACE_GARMIN_ACTIVITY_EXTENSION_V2 = "xmlns:ns3";
	private static final String NAMESPACE_URI_GARMIN_ACTIVITY_EXTENSION_V2 = "http://www.garmin.com/xmlschemas/ActivityExtension/v2";
	private static final String NAMESPACE_GARMIN_ACTIVITY_EXTENSION_V2_SCHEMA_LOCATION = "http://www.garmin.com/xmlschemas/ActivityExtensionv2.xsd";
	
	private static final String XSI_TYPE = "xsi:type";
	private static final String HEART_RATE_IN_BEATS_PER_MINUTE_TYPE = "HeartRateInBeatsPerMinute_t";
	
	private boolean writeFolders;
	private boolean writeActivities;
	private boolean writeCourses;
	
	private static Logger logger = Logger.getLogger(TCXFileWriter.class.getName());
	
	public TCXFileWriter() {
		this(new HashMap<String, Object>());
	}
	
	public TCXFileWriter(Map<String, Object> options) {
		super(options);
		setUp();
	}
	
	private void setUp() {
		writeFolders = (Boolean) getOptions().get(Constants.Writer.WRITE_FOLDERS);
		writeActivities = (Boolean) getOptions().get(Constants.Writer.WRITE_ACTIVITIES);
		writeCourses = (Boolean) getOptions().get(Constants.Writer.WRITE_COURSES);
	}

	@Override
	public void write(List<GPSDocument> documents) throws WriterException {
		throw new UnsupportedOperationException("TCXFileWriter does not support multiple documents.");
	}
	
	@Override
	public void write(GPSDocument document) throws WriterException {
		String filename = document.getFileName();
		writeDocument(document, filename);
	}

	public void writeDocument(GPSDocument gpsDocument, String filename) throws WriterException {
		try {
			Document dom = getDOMTreeFromGPSDocument(gpsDocument);
			serializeXML(dom, filename);
		} catch (TCXWriterException te) {
			throw new WriterException(te);
		}
	}
	
	private void serializeXML(Document document, String filename) throws TCXWriterException {
		try {
			DOMSource domSource = new DOMSource(document);
			
			File outputFile = new File(filename);
			StreamResult streamResult = new StreamResult(new FileOutputStream(outputFile));

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			
			transformer.transform(domSource, streamResult);
		} catch (TransformerException te) {
		    logger.error(te.getMessage());
		    throw new TCXWriterException(te.getMessage());
		} catch (FileNotFoundException fnfe) {
			logger.error(fnfe.getMessage());
			throw new TCXWriterException(fnfe.getMessage());
		}
	}
	
	private Document getDOMTreeFromGPSDocument(GPSDocument gpsDocument) throws TCXWriterException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.newDocument();
			
			Element tcxElement = createTCXElement(dom, gpsDocument);
			
			if (!writeFolders) {
				tcxElement.appendChild(createEmptyElement(dom, FOLDERS));
			}
			
			if (writeActivities) {
				Element activitiesElement = dom.createElement(ACTIVITIES);
				
				for (Activity activity : gpsDocument.getActivities()) {
					Element activityElement = createActivityElement(dom, activity);
					activitiesElement.appendChild(activityElement);
				}
				
				tcxElement.appendChild(activitiesElement);
			} else {
				tcxElement.appendChild(createEmptyElement(dom, ACTIVITIES));
			}
			
			if (writeCourses) {
				Element coursesElement = dom.createElement(COURSES);
				
				for (Course course : gpsDocument.getCourses()) {
					Element courseElement = createCourseElement(dom, course);
					coursesElement.appendChild(courseElement);
				}
				
				tcxElement.appendChild(coursesElement);
			} else {
				tcxElement.appendChild(createEmptyElement(dom, COURSES));
			}
			
			return dom;
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
			throw new TCXWriterException(pce.getMessage());
		}
	}

	private Element createTCXElement(Document document, GPSDocument gpsDocument) {
		Element tcxElement = document.createElement(TCX);
		document.appendChild(tcxElement);
		
		boolean writeActivities = (Boolean) getOptions().get(Constants.Writer.WRITE_ACTIVITIES);
		boolean hasActivityLapExtensions = false;
		if (gpsDocument.getActivities().size() > 0 && writeActivities) {
			for (Activity activity : gpsDocument.getActivities()) {
				if (activity.getLaps().size() > 0 &&
						(activity.getFirstLap().getAverageSpeed() != null
								|| activity.getFirstLap().getMaximumCadence() != null
								|| activity.getFirstLap().getAverageRunningCadence() != null
								|| activity.getFirstLap().getMaximumRunningCadence() != null
								|| activity.getFirstLap().getAveragePower() != null
								|| activity.getFirstLap().getMaximumPower() != null)) {
					hasActivityLapExtensions = true;
					break;
				}
			}
		}
		
		tcxElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_XSI, NAMESPACE_XML_SCHEMA_INSTANCE);
		tcxElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_DEFAULT, NAMESPACE_TCX);
		
		boolean writeCourseExtendedInfo = (Boolean) getOptions().get(Constants.Writer.WRITE_COURSE_EXTENDED_INFO);
		if (gpsDocument.getActivities().size() > 0 || (gpsDocument.getCourses().size() > 0 && writeCourseExtendedInfo)) {
			tcxElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_GARMIN_ACTIVITY_EXTENSION_V2, NAMESPACE_URI_GARMIN_ACTIVITY_EXTENSION_V2);
		}
		
		String schemaLocations = NAMESPACE_TCX + " " + NAMESPACE_TCX_SCHEMA_LOCATION;
		if (hasActivityLapExtensions) {
			schemaLocations += " " + NAMESPACE_URI_GARMIN_ACTIVITY_EXTENSION_V2 + " " + NAMESPACE_GARMIN_ACTIVITY_EXTENSION_V2_SCHEMA_LOCATION;
		}
		
		tcxElement.setAttribute(NAMESPACE_XSI_SCHEMA_LOCATION, schemaLocations);
		
		tcxElement.normalize();
		
		return tcxElement;
	}
	
	private Element createActivityElement(Document dom, Activity activity) {
		Element activityElement = createEmptyElement(dom, ACTIVITY);
		
		SportType sportType = activity.getFirstSession().getSport();
		String sport = "";
		if (sportType == SportType.CYCLING) {
			sport = "Biking";
		} else if (sportType == SportType.RUNNING) {
			sport = SportType.RUNNING.toString();
		}
		activityElement.setAttribute(SPORT, sport);
		
		activityElement.appendChild(createDateTimeElement(dom, ID, activity.getStartTime()));
		
		for (Lap lap : activity.getLaps()) {
			Element activityLapElement = createActivityLapElement(dom, lap);
			activityElement.appendChild(activityLapElement);
		}
		
		if (activity.getNotes() != null) {
			activityElement.appendChild(createTextElement(dom, NOTES, activity.getNotes()));
		}
		
		return activityElement;
	}
	
	private Element createActivityLapElement(Document dom, Lap lap) {
		Element activityLapElement = createEmptyElement(dom, LAP);
		activityLapElement.setAttribute(START_TIME, Formatters.getSimpleDateFormatMilis().format(lap.getStartTime()));
		
		activityLapElement.appendChild(createDoubleElement(dom, TOTAL_TIME_SECONDS, lap.getTimerTime()));
		activityLapElement.appendChild(createDoubleElement(dom, DISTANCE_METERS, lap.getDistance()));
		
		if (lap.getMaximumSpeed() != null) {
			activityLapElement.appendChild(createDoubleElement(dom, MAXIMUM_SPEED, lap.getMaximumSpeed()));
		}
		
		if (lap.getCalories() == null) {
			lap.setCalories(0);
		}
		activityLapElement.appendChild(createIntegerElement(dom, CALORIES, lap.getCalories()));
		
		if (lap.getAverageHeartRate() != null) {
			Element lapAverageHeartRate = createEmptyElement(dom, AVERAGE_HEART_RATE);
			lapAverageHeartRate.setAttribute(XSI_TYPE, HEART_RATE_IN_BEATS_PER_MINUTE_TYPE);
			lapAverageHeartRate.appendChild(createShortElement(dom, VALUE, lap.getAverageHeartRate()));
			activityLapElement.appendChild(lapAverageHeartRate);
		}
		
		if (lap.getMaximumHeartRate() != null) {
			Element lapMaximumHeartRate = createEmptyElement(dom, MAXIMUM_HEART_RATE);
			lapMaximumHeartRate.setAttribute(XSI_TYPE, HEART_RATE_IN_BEATS_PER_MINUTE_TYPE);
			lapMaximumHeartRate.appendChild(createShortElement(dom, VALUE, lap.getMaximumHeartRate()));
			activityLapElement.appendChild(lapMaximumHeartRate);
		}
		
		if (lap.getIntensity() != null) {
			activityLapElement.appendChild(createTextElement(dom, INTENSITY, lap.getIntensity().toString()));
		} else {
			activityLapElement.appendChild(createTextElement(dom, INTENSITY, IntensityType.ACTIVE.toString()));
		}
		
		if (lap.getAverageCadence() != null) {
			activityLapElement.appendChild(createShortElement(dom, CADENCE, lap.getAverageCadence()));
		}
		
		activityLapElement.appendChild(createTextElement(dom, TRIGGER_METHOD, lap.getTrigger().toString()));
		
		for (Track track : lap.getTracks()) {
			Element trackElement = createTrackElement(dom, track, true, lap);
			activityLapElement.appendChild(trackElement);
		}
		
		if (lap.getNotes() != null) {
			activityLapElement.appendChild(createTextElement(dom, NOTES, lap.getNotes()));
		}
		
		if (lap.getAverageSpeed() != null || lap.getMaximumCadence() != null) {
			Element extensionsElement = createEmptyElement(dom, EXTENSIONS);
			
			Element lapExtensionElement = createEmptyElementNS(dom, PREFIX_GARMIN_ACTIVITY_LAP_EXTENSION_V2, ACTIVITY_LAP_EXTENSION_V2);
			
			if (lap.getAverageSpeed() != null) {
				Element averageSpeedElement = createDoubleElement(dom, AVERAGE_SPEED, lap.getAverageSpeed());
				lapExtensionElement.appendChild(averageSpeedElement);
			}
			
			if (lap.getMaximumCadence() != null) {
				Element maximumCadenceElement = createShortElement(dom, MAXIMUM_BIKE_CADENCE, lap.getMaximumCadence());
				lapExtensionElement.appendChild(maximumCadenceElement);
			}

			if (lap.getAverageRunningCadence() != null) {
				Element averageRunningCadenceElement = createShortElement(dom, AVERAGE_RUN_CADENCE, lap.getAverageRunningCadence());
				lapExtensionElement.appendChild(averageRunningCadenceElement);
			}

			if (lap.getMaximumRunningCadence() != null) {
				Element maximumRunningCadenceElement = createShortElement(dom, MAXIMUM_RUN_CADENCE, lap.getMaximumRunningCadence());
				lapExtensionElement.appendChild(maximumRunningCadenceElement);
			}

			if (lap.getAveragePower() != null) {
				Element averagePowerElement = createIntegerElement(dom, AVERAGE_WATTS, lap.getAveragePower());
				lapExtensionElement.appendChild(averagePowerElement);
			}
			
			if (lap.getMaximumPower() != null) {
				Element maximumPowerElement = createIntegerElement(dom, MAXIMUM_WATTS, lap.getMaximumPower());
				lapExtensionElement.appendChild(maximumPowerElement);
			}

			extensionsElement.appendChild(lapExtensionElement);
			activityLapElement.appendChild(extensionsElement);
		}
		
		return activityLapElement;
	}
	
	private Element createTrackElement(Document dom, Track track, boolean extendedInfo, Lap lap) {
		Element trackElement = createEmptyElement(dom, TRACK);
		
		for (Trackpoint trackpoint : track.getTrackpoints()) {
			Element trackpointElement = createTrackpointElement(dom, trackpoint, extendedInfo, lap);
			trackElement.appendChild(trackpointElement);
		}
		
		return trackElement;
	}
		
	private Element createTrackpointElement(Document dom, Trackpoint trackpoint, boolean extendedData, Lap lap) {
		Element trackpointElement = createEmptyElement(dom, TRACKPOINT);
		
		trackpointElement.appendChild(createDateTimeElement(dom, TIME, trackpoint.getTimestamp()));
		
		if (trackpoint.getLatitude() != null && trackpoint.getLongitude() != null) {
			Element positionElement = createEmptyElement(dom, POSITION);
			positionElement.appendChild(createDoubleElement(dom, LATITUDE_DEGREES, trackpoint.getLatitude()));
			positionElement.appendChild(createDoubleElement(dom, LONGITUDE_DEGREES, trackpoint.getLongitude()));
			trackpointElement.appendChild(positionElement);
		}
		
		if (trackpoint.getAltitude() != null) {
			trackpointElement.appendChild(createDoubleElement(dom, ALTITUDE_METERS, trackpoint.getAltitude()));
		}
		
		if (trackpoint.getDistance() != null) {
			trackpointElement.appendChild(createDoubleElement(dom, DISTANCE_METERS, trackpoint.getDistance()));
		}
		
		if (trackpoint.getHeartRate() != null && extendedData) {
			Element heartRateElement = createEmptyElement(dom, HEART_RATE);
			heartRateElement.setAttribute(XSI_TYPE, HEART_RATE_IN_BEATS_PER_MINUTE_TYPE);
			heartRateElement.appendChild(createShortElement(dom, VALUE, trackpoint.getHeartRate()));
			trackpointElement.appendChild(heartRateElement);
		}
		
		if (trackpoint.getCadence() != null && extendedData && lap.getSport().equals(SportType.CYCLING)) {
			trackpointElement.appendChild(createShortElement(dom, CADENCE, trackpoint.getCadence()));
			trackpointElement.appendChild(createTextElement(dom, SENSOR_STATE, SensorStateType.PRESENT.getDescription()));
		}
		
		if (extendedData) {
			Element extensionsElement = createEmptyElement(dom, EXTENSIONS);
			Element trackpointExtensionElement = createEmptyElementNS(dom, PREFIX_GARMIN_ACTIVITY_TRACKPOINT_EXTENSION_V2, ACTIVITY_TRACKPOINT_EXTENSION_V2);
			
			if (trackpoint.getSpeed() != null) {
				Element speedElement = createDoubleElement(dom, SPEED, trackpoint.getSpeed());
				trackpointExtensionElement.appendChild(speedElement);
			}
			
			if (trackpoint.getCadence() != null) {
				if (lap.getSport().equals(SportType.CYCLING)) {
					trackpointExtensionElement.setAttribute(CADENCE_SENSOR, SensorSourceType.BIKE.getDescription());
				} else if (lap.getSport().equals(SportType.RUNNING)) {
					trackpointExtensionElement.setAttribute(CADENCE_SENSOR, SensorSourceType.FOOTPOD.getDescription());
				} else {
					trackpointExtensionElement.setAttribute(CADENCE_SENSOR, SensorSourceType.UNDEFINED.getDescription());
				}
			}
	
			if (trackpoint.getCadence() != null && extendedData && lap.getSport().equals(SportType.RUNNING)) {
				Element runCadenceElement = createShortElement(dom, RUN_CADENCE, trackpoint.getCadence());
				trackpointExtensionElement.appendChild(runCadenceElement);
			}

			if (trackpoint.getPower() != null && extendedData) {
				Element powerElement = createIntegerElement(dom, WATTS, trackpoint.getPower());
				trackpointExtensionElement.appendChild(powerElement);
			}
				
			extensionsElement.appendChild(trackpointExtensionElement);
			trackpointElement.appendChild(extensionsElement);
		}

		return trackpointElement;
	}
	
	private Element createCourseElement(Document dom, Course course) {
		Element courseElement = createEmptyElement(dom, COURSE);
		
		courseElement.appendChild(createTextElement(dom, NAME, course.getName()));
		
		for (Lap lap : course.getLaps()) {
			Element courseLapElement = createCourseLapElement(dom, lap);
			courseElement.appendChild(courseLapElement);
		}
		
		for (Lap lap : course.getLaps()) {
			for (Track track: lap.getTracks()) {
				boolean writeCourseExtendedInfo = (Boolean) getOptions().get(Constants.Writer.WRITE_COURSE_EXTENDED_INFO);
				Element trackElement = createTrackElement(dom, track, writeCourseExtendedInfo, lap);
				courseElement.appendChild(trackElement);
			}
		}
		
		if (course.getNotes() != null) {
			courseElement.appendChild(createTextElement(dom, NOTES, course.getNotes()));
		}
		
		for (CoursePoint coursePoint : course.getCoursePoints()) {
			Element coursePointElement = createCoursePointElement(dom, coursePoint);
			courseElement.appendChild(coursePointElement);
		}
		
		return courseElement;
	}
	
	private Element createCourseLapElement(Document dom, Lap lap) {
		Element courseLapElement = createEmptyElement(dom, LAP);
		
		courseLapElement.appendChild(createDoubleElement(dom, TOTAL_TIME_SECONDS, lap.getTimerTime()));
		courseLapElement.appendChild(createDoubleElement(dom, DISTANCE_METERS, lap.getDistance()));
		
		if (lap.getStartLatitude() != null && lap.getStartLongitude() != null) {
			Element beginPositionElement = createEmptyElement(dom, BEGIN_POSITION);
			beginPositionElement.appendChild(createDoubleElement(dom, LATITUDE_DEGREES, lap.getStartLatitude()));
			beginPositionElement.appendChild(createDoubleElement(dom, LONGITUDE_DEGREES, lap.getStartLongitude()));
			courseLapElement.appendChild(beginPositionElement);
		}
		
		if (lap.getStartAltitude() != null) {
			courseLapElement.appendChild(createDoubleElement(dom, BEGIN_ALTITUDE_METERS, lap.getStartAltitude()));
		}
		
		if (lap.getEndLatitude() != null && lap.getEndLongitude() != null) {
			Element endPositionElement = createEmptyElement(dom, END_POSITION);
			endPositionElement.appendChild(createDoubleElement(dom, LATITUDE_DEGREES, lap.getEndLatitude()));
			endPositionElement.appendChild(createDoubleElement(dom, LONGITUDE_DEGREES, lap.getEndLongitude()));
			courseLapElement.appendChild(endPositionElement);
		}
		
		if (lap.getEndAltitude() != null) {
			courseLapElement.appendChild(createDoubleElement(dom, END_ALTITUDE_METERS, lap.getEndAltitude()));
		}
		
		if (lap.getAverageHeartRate() != null) {
			Element lapAverageHeartRate = createEmptyElement(dom, AVERAGE_HEART_RATE);
			lapAverageHeartRate.setAttribute(XSI_TYPE, HEART_RATE_IN_BEATS_PER_MINUTE_TYPE);
			lapAverageHeartRate.appendChild(createShortElement(dom, VALUE, lap.getAverageHeartRate()));
			courseLapElement.appendChild(lapAverageHeartRate);
		}
		
		if (lap.getMaximumHeartRate() != null) {
			Element lapMaximumHeartRate = createEmptyElement(dom, MAXIMUM_HEART_RATE);
			lapMaximumHeartRate.setAttribute(XSI_TYPE, HEART_RATE_IN_BEATS_PER_MINUTE_TYPE);
			lapMaximumHeartRate.appendChild(createShortElement(dom, VALUE, lap.getMaximumHeartRate()));
			courseLapElement.appendChild(lapMaximumHeartRate);
		}
		
		courseLapElement.appendChild(createTextElement(dom, INTENSITY,
				(lap.getIntensity() != null ? lap.getIntensity().toString() : IntensityType.ACTIVE.toString())));
		
		if (lap.getAverageCadence() != null) {
			courseLapElement.appendChild(createShortElement(dom, CADENCE, lap.getAverageCadence()));
		}
		
		return courseLapElement;
	}
	
	private Element createCoursePointElement(Document dom, CoursePoint coursePoint) {
		Element coursePointElement = createEmptyElement(dom, COURSE_POINT);
		
		if (coursePoint.getName() != null) {
			coursePointElement.appendChild(createTextElement(dom, NAME, coursePoint.getName()));
		}
		
		if (coursePoint.getTime() != null) {
			coursePointElement.appendChild(createDateTimeElement(dom, TIME, coursePoint.getTime()));
		}
		
		Element positionElement = createEmptyElement(dom, POSITION);
		positionElement.appendChild(createDoubleElement(dom, LATITUDE_DEGREES, coursePoint.getLatitude()));
		positionElement.appendChild(createDoubleElement(dom, LONGITUDE_DEGREES, coursePoint.getLongitude()));
		coursePointElement.appendChild(positionElement);
		
		if (coursePoint.getAltitude() != null) {
			coursePointElement.appendChild(createDoubleElement(dom, ALTITUDE_METERS, coursePoint.getAltitude()));
		}
		
		coursePointElement.appendChild(createTextElement(dom, POINT_TYPE, coursePoint.getType().toString()));
		
		if (coursePoint.getNotes() != null) {
			coursePointElement.appendChild(createTextElement(dom, NOTES, coursePoint.getNotes()));
		}

		return coursePointElement;
	}
}
