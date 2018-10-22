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
package com.trackit.business.writer.fitlog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.print.Doc;
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

import com.trackit.business.common.Constants;
import com.trackit.business.common.Formatters;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.IntensityType;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.WriterException;
import com.trackit.business.writer.Writer;
import com.trackit.business.writer.WriterTemplate;

public class FitlogFileWriter extends WriterTemplate implements Writer {
	
	private static final String FITNESS_WORKBOOK = "FitnessWorkbook";
	private static final String ATHLETE_LOG = "AthleteLog";
	private static final String ACTIVITY = "Activity";
	private static final String ID = "Id";
	private static final String START_TIME = "StartTime";
	private static final String METADATA = "Metadata";
	private static final String SOURCE = "Source";
	private static final String CREATED = "Created";
	private static final String MODIFIED = "Modified";
	private static final String DURATION = "Duration";
	private static final String TOTAL_SECONDS = "TotalSeconds";
	private static final String DISTANCE = "Distance";
	private static final String TOTAL_METERS = "TotalMeters";
	private static final String ELEVATION = "Elevation";
	private static final String ASCEND_METERS = "AscendMeters";
	private static final String DESCEND_METERS = "DescendMeters";
	private static final String HEART_RATE = "HeartRate";
	private static final String AVERAGE_BPM = "AverageBPM";
	private static final String MAXIMUM_BPM = "MaximumBPM";
	private static final String CADENCE = "Cadence";
	private static final String AVERAGE_RPM = "AverageRPM";
	private static final String MAXIMUM_RPM = "MaximumRPM";
	private static final String POWER = "Power";
	private static final String AVERAGE_WATTS = "AverageWatts";
	private static final String MAXIMUM_WATTS = "MaximumWatts";
	private static final String CALORIES = "Calories";
	private static final String TOTAL_CAL = "TotalCal";
	private static final String NAME = "Name";
	private static final String LAPS = "Laps";
	private static final String LAP = "Lap";
	private static final String DURATION_SECONDS = "DurationSeconds";
	private static final String REST = "Rest";
	private static final String NOTES = "Notes";
	private static final String TRACK = "Track";
	private static final String TRACKPOINT = "pt";
	private static final String TRKPT_TIME = "tm";
	private static final String TRKPT_LATITUDE = "lat";
	private static final String TRKPT_LONGITUDE = "lon";
	private static final String TRKPT_ELEVATION = "ele";
	private static final String TRKPT_DISTANCE = "dist";
	private static final String TRKPT_HEART_RATE = "hr";
	private static final String TRKPT_CADENCE = "cadence";
	private static final String TRKPT_POWER = "power";
	
	private static final String NAMESPACE_XMLNS = "http://www.w3.org/2000/xmlns/";
	private static final String NAMESPACE_XSI = "xmlns:xsi";
	private static final String NAMESPACE_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String NAMESPACE_DEFAULT = "xmlns";
	
	private static final String NAMESPACE_FITLOG = "http://www.zonefivesoftware.com/xmlschemas/FitnessLogbook/v2";
	private static final String NAMESPACE_XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
	private static final String NAMESPACE_FITLOG_SCHEMA_LOCATION = "http://www.zonefivesoftware.com/xmlschemas/FitnessLogbook/v2/fitnesslog2.xsd";
	
	private boolean writeActivities;
	private boolean writeCourses;			//2018-05-07: 12335
	
	private static Logger logger = Logger.getLogger(FitlogFileWriter.class.getName());
	
	public FitlogFileWriter() {
		this(new HashMap<String, Object>());
	}
	
	public FitlogFileWriter(Map<String, Object> options) {
		super(options);
		setUp();
	}
	
	private void setUp() {
		writeActivities = (Boolean) getOptions().get(Constants.Writer.WRITE_ACTIVITIES);
		//2018-05-07: 12335
		writeCourses = (Boolean) getOptions().get( Constants.Writer.WRITE_COURSES);
	}

	@Override
	public void write(List<GPSDocument> documents) throws WriterException {
		throw new UnsupportedOperationException("FitlogFileWriter does not support multiple documents.");
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
		} catch (FitlogWriterException te) {
			throw new WriterException(te);
		}
	}
	
	private void serializeXML(Document document, String filename) throws FitlogWriterException {
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
		    throw new FitlogWriterException(te.getMessage());
		} catch (FileNotFoundException fnfe) {
			logger.error(fnfe.getMessage());
			throw new FitlogWriterException(fnfe.getMessage());
		}
	}
	
	private Document getDOMTreeFromGPSDocument(GPSDocument gpsDocument) throws FitlogWriterException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.newDocument();
			
			Element fitnessWorkbookElement = createFitnessWorkbookElement(dom, gpsDocument);
			Element athleteLogElement = createAthleteLogElement(dom);
			fitnessWorkbookElement.appendChild(athleteLogElement);
			if ( !gpsDocument.getName().isEmpty() ) {					//2018-05-07: 12335
				athleteLogElement.appendChild( createTextElement( dom, NAME, gpsDocument.getName()));
			}
			
			if (writeActivities) {
				for (Activity activity : gpsDocument.getActivities()) {
					Element activityElement = createActivityElement(dom, activity);
					athleteLogElement.appendChild(activityElement);
				}
			}
			
			//2018-05-07: 12335
			if ( writeCourses )
				for( Course course: gpsDocument.getCourses() ) {
					Element activityElement = createActivityElement( dom, course);
					athleteLogElement.appendChild( activityElement);
				}
			
			return dom;
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
			throw new FitlogWriterException(pce.getMessage());
		}
	}

	private Element createFitnessWorkbookElement(Document document, GPSDocument gpsDocument) {
		Element fitnessWorkbookElement = document.createElement(FITNESS_WORKBOOK);
		document.appendChild(fitnessWorkbookElement);
		
		fitnessWorkbookElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_XSI, NAMESPACE_XML_SCHEMA_INSTANCE);
		fitnessWorkbookElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_DEFAULT, NAMESPACE_FITLOG);
		
		String schemaLocations = NAMESPACE_FITLOG + " " + NAMESPACE_FITLOG_SCHEMA_LOCATION;
		fitnessWorkbookElement.setAttribute(NAMESPACE_XSI_SCHEMA_LOCATION, schemaLocations);
		
		fitnessWorkbookElement.normalize();
		
		return fitnessWorkbookElement;
	}
	
	private Element createAthleteLogElement(Document dom) {
		Element athleteLogElement = createEmptyElement(dom, ATHLETE_LOG);
		return athleteLogElement;
	}
	
	private Element createActivityElement(Document dom, Activity activity) {
		Element activityElement = createEmptyElement(dom, ACTIVITY);
		String id = UUID.randomUUID().toString();
		activityElement.setAttribute(ID, id);
		String startTime = Formatters.getSimpleDateFormat().format(activity.getStartTime());
		activityElement.setAttribute(START_TIME, startTime);
		
		appendMetadata(dom, activityElement, activity);
		appendSummaryInfo(dom, activityElement, activity);
		appendLaps(dom, activityElement, activity);
		appendTrack(dom, activityElement, activity);
		
		return activityElement;
	}
	
	//2018-05-07: 12335
	private Element createActivityElement(Document dom, Course course) {
		Element activityElement = createEmptyElement(dom, ACTIVITY);
		String id = UUID.randomUUID().toString();
		activityElement.setAttribute(ID, id);
		String startTime = Formatters.getSimpleDateFormat().format( course.getStartTime());
		activityElement.setAttribute(START_TIME, startTime);
		
		appendMetadata(dom, activityElement, course);
		appendSummaryInfo(dom, activityElement, course);
		appendLaps(dom, activityElement, course.getLaps());
		appendTrack(dom, activityElement, course.getTrackpoints(), course.getTrackpoints().get(0).getTimestamp());
		
		return activityElement;
	}
	
	private void appendMetadata(Document dom, Element activityElement, Activity activity) {
		Element metadataElement = createEmptyElement(dom, METADATA);
		metadataElement.setAttribute(SOURCE, Constants.APP_NAME);
		
		String creationTime = Formatters.getSimpleDateFormat().format(activity.getStartTime());
		metadataElement.setAttribute(CREATED, creationTime);
		
		String modificationTime = Formatters.getSimpleDateFormat().format(new Date(System.currentTimeMillis()));
		metadataElement.setAttribute(MODIFIED, modificationTime);
		
		activityElement.appendChild(metadataElement);
	}
	
	//2018-05-07
	private void appendMetadata(Document dom, Element activityElement, Course course) {
		Element metadataElement = createEmptyElement(dom, METADATA);
		metadataElement.setAttribute(SOURCE, Constants.APP_NAME);
		
		String creationTime = Formatters.getSimpleDateFormat().format( course.getStartTime());
		metadataElement.setAttribute(CREATED, creationTime);
		
		String modificationTime = Formatters.getSimpleDateFormat().format(new Date(System.currentTimeMillis()));
		metadataElement.setAttribute(MODIFIED, modificationTime);
		
		activityElement.appendChild(metadataElement);
	}
		
	private void appendSummaryInfo(Document dom, Element activityElement, Activity activity) {
		Element element;
		Session session = activity.getFirstSession();
		
		if (session.getTimerTime() != null) {
			element = createEmptyElement(dom, DURATION);
			element.setAttribute(TOTAL_SECONDS, Formatters.getDecimalFormat(1).format(session.getTimerTime()));
			activityElement.appendChild(element);
		}
		
		if (session.getDistance() != null) {
			element = createEmptyElement(dom, DISTANCE);
			element.setAttribute(TOTAL_METERS, Formatters.getDecimalFormat(0).format(session.getDistance()));
			activityElement.appendChild(element);
		}
		
		if (session.getTotalAscent() != null || session.getTotalDescent() != null) {
			element = createEmptyElement(dom, ELEVATION);
			if (session.getTotalAscent() != null) {
				element.setAttribute(ASCEND_METERS, session.getTotalAscent().toString());
			}
			if (session.getTotalDescent() != null) {
				element.setAttribute(DESCEND_METERS, session.getTotalDescent().toString());
			}
			activityElement.appendChild(element);
		}
		
		if (session.getAverageHeartRate() != null || session.getMaximumHeartRate() != null) {
			element = createEmptyElement(dom, HEART_RATE);
			if (session.getAverageHeartRate() != null) {
				element.setAttribute(AVERAGE_BPM, session.getAverageHeartRate().toString());
			}
			if (session.getMaximumHeartRate() != null) {
				element.setAttribute(MAXIMUM_BPM, session.getMaximumHeartRate().toString());
			}
			activityElement.appendChild(element);
		}
		
		if (session.getAverageCadence() != null || session.getMaximumCadence() != null) {
			element = createEmptyElement(dom, CADENCE);
			if (session.getAverageCadence() != null) {
				element.setAttribute(AVERAGE_RPM, session.getAverageCadence().toString());
			}
			if (session.getMaximumCadence() != null) {
				element.setAttribute(MAXIMUM_RPM, session.getMaximumCadence().toString());
			}
			activityElement.appendChild(element);
		}
		
		if (session.getAveragePower() != null || session.getMaximumPower() != null) {
			element = createEmptyElement(dom, POWER);
			if (session.getAveragePower() != null) {
				element.setAttribute(AVERAGE_WATTS, session.getAveragePower().toString());
			}
			if (session.getMaximumPower() != null) {
				element.setAttribute(MAXIMUM_WATTS, session.getMaximumPower().toString());
			}
			activityElement.appendChild(element);
		}
		
		if (session.getCalories() != null) {
			element = createEmptyElement(dom, CALORIES);
			element.setAttribute(TOTAL_CAL, session.getCalories().toString());
			activityElement.appendChild(element);
		}
		
		if (activity.getNotes() != null) {
			element = createTextElement(dom, NOTES, activity.getNotes());
			activityElement.appendChild(element);
		}
		
		if (activity.getName() != null) {
			element = createTextElement(dom, NAME, activity.getName());
			activityElement.appendChild(element);
		}
	}
	
	//2018-05-07: 12335
	private void appendSummaryInfo(Document dom, Element activityElement, Course course) {
		Element element;
//		Session session = activity.getFirstSession();
		
		if ( course.getTimerTime() != null) {
			element = createEmptyElement(dom, DURATION);
			element.setAttribute(TOTAL_SECONDS, Formatters.getDecimalFormat(1).format( course.getTimerTime()));
			activityElement.appendChild(element);
		}
		
		if ( course.getDistance() != null) {
			element = createEmptyElement(dom, DISTANCE);
			element.setAttribute(TOTAL_METERS, Formatters.getDecimalFormat(0).format( course.getDistance()));
			activityElement.appendChild(element);
		}
		
		if ( course.getTotalAscent() != null || course.getTotalDescent() != null) {
			element = createEmptyElement(dom, ELEVATION);
			if ( course.getTotalAscent() != null) {
				element.setAttribute(ASCEND_METERS, course.getTotalAscent().toString());
			}
			if ( course.getTotalDescent() != null) {
				element.setAttribute(DESCEND_METERS, course.getTotalDescent().toString());
			}
			activityElement.appendChild(element);
		}
		
		if ( course.getAverageHeartRate() != null || course.getMaximumHeartRate() != null) {
			element = createEmptyElement(dom, HEART_RATE);
			if ( course.getAverageHeartRate() != null) {
				element.setAttribute(AVERAGE_BPM, course.getAverageHeartRate().toString());
			}
			if ( course.getMaximumHeartRate() != null) {
				element.setAttribute(MAXIMUM_BPM, course.getMaximumHeartRate().toString());
			}
			activityElement.appendChild(element);
		}
		
		if ( course.getAverageCadence() != null || course.getMaximumCadence() != null) {
			element = createEmptyElement(dom, CADENCE);
			if ( course.getAverageCadence() != null) {
				element.setAttribute(AVERAGE_RPM, course.getAverageCadence().toString());
			}
			if ( course.getMaximumCadence() != null) {
				element.setAttribute(MAXIMUM_RPM, course.getMaximumCadence().toString());
			}
			activityElement.appendChild(element);
		}
		
		if ( course.getAveragePower() != null || course.getMaximumPower() != null) {
			element = createEmptyElement(dom, POWER);
			if ( course.getAveragePower() != null) {
				element.setAttribute(AVERAGE_WATTS, course.getAveragePower().toString());
			}
			if ( course.getMaximumPower() != null) {
				element.setAttribute(MAXIMUM_WATTS, course.getMaximumPower().toString());
			}
			activityElement.appendChild(element);
		}
		
		if ( course.getCalories() != null) {
			element = createEmptyElement(dom, CALORIES);
			element.setAttribute(TOTAL_CAL, course.getCalories().toString());
			activityElement.appendChild(element);
		}
		
		if ( course.getNotes() != null) {
			element = createTextElement(dom, NOTES, course.getNotes());
			activityElement.appendChild(element);
		}
		
		if ( course.getName() != null) {
			element = createTextElement(dom, NAME, course.getName());
			activityElement.appendChild(element);
		}
	}
	
	private void appendLaps(Document dom, Element activityElement, Activity activity) {
		Element lapsElement = createEmptyElement(dom, LAPS);
		
		for (Lap lap : activity.getLaps()) {
			appendLap(dom, lapsElement, lap);
		}
		
		activityElement.appendChild(lapsElement);
	}
	
	//2018-05-07: 12335
	private void appendLaps( Document dom, Element activityElement, List<Lap> laps) {
		Element lapsElement = createEmptyElement(dom, LAPS);
		for (Lap lap : laps) {
			appendLap(dom, lapsElement, lap);
		}
		
		activityElement.appendChild(lapsElement);
	}
	
	private void appendLap(Document dom, Element lapsElement, Lap lap) {
		Element lapElement = createLapElement(dom, lap);
		Element element;
		
		if (lap.getDistance() != null) {
			element = createEmptyElement(dom, DISTANCE);
			element.setAttribute(TOTAL_METERS, Formatters.getDecimalFormat(0).format(lap.getDistance()));
			lapElement.appendChild(element);
		}
		
		if (lap.getTotalAscent() != null || lap.getTotalDescent() != null) {
			element = createEmptyElement(dom, ELEVATION);
			if (lap.getTotalAscent() != null) {
				element.setAttribute(ASCEND_METERS, lap.getTotalAscent().toString());
			}
			if (lap.getTotalDescent() != null) {
				element.setAttribute(DESCEND_METERS, lap.getTotalDescent().toString());
			}
			lapElement.appendChild(element);
		}
		
		if (lap.getAverageHeartRate() != null || lap.getMaximumHeartRate() != null) {
			element = createEmptyElement(dom, HEART_RATE);
			if (lap.getAverageHeartRate() != null) {
				element.setAttribute(AVERAGE_BPM, lap.getAverageHeartRate().toString());
			}
			if (lap.getMaximumHeartRate() != null) {
				element.setAttribute(MAXIMUM_BPM, lap.getMaximumHeartRate().toString());
			}
			lapElement.appendChild(element);
		}
		
		if (lap.getAverageCadence() != null || lap.getMaximumCadence() != null) {
			element = createEmptyElement(dom, CADENCE);
			if (lap.getAverageCadence() != null) {
				element.setAttribute(AVERAGE_RPM, lap.getAverageCadence().toString());
			}
			if (lap.getMaximumCadence() != null) {
				element.setAttribute(MAXIMUM_RPM, lap.getMaximumCadence().toString());
			}
			lapElement.appendChild(element);
		}
		
		if (lap.getAveragePower() != null || lap.getMaximumPower() != null) {
			element = createEmptyElement(dom, POWER);
			if (lap.getAveragePower() != null) {
				element.setAttribute(AVERAGE_WATTS, lap.getAveragePower().toString());
			}
			if (lap.getMaximumPower() != null) {
				element.setAttribute(MAXIMUM_WATTS, lap.getMaximumPower().toString());
			}
			lapElement.appendChild(element);
		}
		
		if (lap.getCalories() != null) {
			element = createEmptyElement(dom, CALORIES);
			element.setAttribute(TOTAL_CAL, lap.getCalories().toString());
			lapElement.appendChild(element);
		}
		
		lapsElement.appendChild(lapElement);
	}
	
	private Element createLapElement(Document dom, Lap lap) {
		Element lapElement = createEmptyElement(dom, LAP);
		
		lapElement.setAttribute(START_TIME, Formatters.getSimpleDateFormat().format(lap.getStartTime()));
		lapElement.setAttribute(DURATION_SECONDS, Formatters.getDecimalFormat(1).format(lap.getTimerTime()));
		
		if (lap.getIntensity() != null) {
			lapElement.setAttribute(REST, lap.getIntensity() == IntensityType.REST ? "true" : "false");
		}
		
		if (lap.getNotes() != null) {
			lapElement.setAttribute(NOTES, lap.getNotes());
		}
		
		return lapElement;
	}
	
	private void appendTrack(Document dom, Element activityElement, Activity activity) {
		Element trackElement = createTrackElement(dom, activity);
		appendTrackpoints(dom, trackElement, activity);
		activityElement.appendChild(trackElement);
	}
	
	//2018-05-07: 12335
	private void appendTrack( Document dom, Element activityElement, List<Trackpoint> trackpoints, Date startTime) {
		Element trackElement = createTrackElement( dom, startTime);
		appendTrackpoints( dom, trackElement, trackpoints, startTime.getTime());
		activityElement.appendChild( trackElement);
	}
	
	private Element createTrackElement(Document dom, Activity activity) {
		Element trackElement = createEmptyElement(dom, TRACK);
		trackElement.setAttribute(START_TIME, Formatters.getSimpleDateFormat().format(activity.getStartTime()));
		
		return trackElement;
	}
	
	//2018-05-07: 12335
	private Element createTrackElement( Document dom, Date startTime) {
		Element trackElement = createEmptyElement( dom, TRACK);
		trackElement.setAttribute( START_TIME, Formatters.getSimpleDateFormat().format( startTime));
		return trackElement;
	}
	
	private void appendTrackpoints(Document dom, Element trackElement, Activity activity) {
		List<Trackpoint> trackpoints = activity.getTrackpoints();
		if (trackpoints.isEmpty()) {
			return;
		}
		
		long initialTime = trackpoints.get(0).getTimestamp().getTime();
		for (Trackpoint trackpoint : trackpoints) {
			Element trackpointElement = createTrackpointElement(dom, trackpoint, initialTime);
			trackElement.appendChild(trackpointElement);
		}
	}
	
	//2018-05-07: 12335
	private  void appendTrackpoints( Document dom,                 Element trackElement, 
			                         List<Trackpoint> trackpoints, long initialTime) {
		if ( trackpoints!= null && !trackpoints.isEmpty() ) {
			for( Trackpoint trackpoint: trackpoints) {
				Element trackpointElement = createTrackpointElement( dom, trackpoint, initialTime);
				trackElement.appendChild( trackpointElement);
			}
		}
	}

	//2018-05-07: 12335 - set 1 decimal place to elevation and 3 to distance
	private Element createTrackpointElement(Document dom, Trackpoint trackpoint, long initialTime) {
		Element trackpointElement = createEmptyElement(dom, TRACKPOINT);
		
		long time = (trackpoint.getTimestamp().getTime() - initialTime) / 1000;
		trackpointElement.setAttribute(TRKPT_TIME, String.valueOf(time));

		trackpointElement.setAttribute(TRKPT_LATITUDE,
				Formatters.getDecimalFormat(14).format(trackpoint.getLatitude()));
		trackpointElement.setAttribute(TRKPT_LONGITUDE,
				Formatters.getDecimalFormat(14).format(trackpoint.getLongitude()));
		
		if (trackpoint.getAltitude() != null) {
			trackpointElement.setAttribute(TRKPT_ELEVATION,
					Formatters.getDecimalFormat(1).format(trackpoint.getAltitude()));
		}
		
		if (trackpoint.getDistance() != null) {
			trackpointElement.setAttribute(TRKPT_DISTANCE,
					Formatters.getDecimalFormat(3).format(trackpoint.getDistance()));
		}
		
		if (trackpoint.getHeartRate() != null) {
			trackpointElement.setAttribute(TRKPT_HEART_RATE, String.valueOf(trackpoint.getHeartRate()));
		}
		
		if (trackpoint.getCadence() != null) {
			trackpointElement.setAttribute(TRKPT_CADENCE, String.valueOf(trackpoint.getCadence()));
		}
		
		if (trackpoint.getPower() != null) {
			trackpointElement.setAttribute(TRKPT_POWER, String.valueOf(trackpoint.getPower()));
		}
		
		return trackpointElement;
	}
}
