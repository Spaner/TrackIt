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
package com.trackit.business.reader.fitlog;

import java.text.ParseException;
import java.util.ArrayList;
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
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.ActivityLap;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.reader.XMLFileHandler;


public class FitlogHandler extends DefaultHandler implements XMLFileHandler {
	static enum GPXElement {
		FITNESS_WORKBOOK("FitnessWorkbook"),
		ATHLETE_LOG("AthleteLog"),
		ACTIVITY("Activity"),
		START_TIME("StartTime"),
		ID("Id"),
		METADATA("Metadata"),
		SOURCE("Source"),
		CREATED("Created"),
		MODIFIED("Modified"),
		DURATION("Duration"),
		TOTAL_SECONDS("TotalSeconds"),
		DISTANCE("Distance"),
		TOTAL_METERS("TotalMeters"),
		ELEVATION("Elevation"),
		DESCEND_METERS("DescendMeters"),
		ASCEND_METERS("AscendMeters"),
		HEART_RATE("HeartRate"),
		AVERAGE_HEART_RATE("AverageBPM"),
		MAXIMUM_HEART_RATE("MaximumBPM"),
		CADENCE("Cadence"),
		AVERAGE_CADENCE("AverageRPM"),
		MAXIMUM_CADENCE("MaximumRPM"),
		POWER("Power"),
		AVERAGE_POWER("AverageWatts"),
		MAXIMUM_POWER("MaximumWatts"),
		CALORIES("Calories"),
		TOTAL_CALORIES("TotalCal"),
		NOTES("Notes"),
		NAME("Name"),
		LAPS("Laps"),
		LAP("Lap"),
		DURATION_SECONDS("DurationSeconds"),
		REST("Rest"),
		ATHLETE("Athlete"),
		CATEGORY("Category"),
		LOCATION("Location"),
		TRACK("Track"),
		TRACKPOINT("pt"),
		TRACKPOINT_TIME("tm"),
		TRACKPOINT_LATITUDE("lat"),
		TRACKPOINT_LONGITUDE("lon"),
		TRACKPOINT_ELEVATION("ele"),
		TRACKPOINT_DISTANCE("dist"),
		TRACKPOINT_HEART_RATE("hr"),
		TRACKPOINT_CADENCE("cadence"),
		TRACKPOINT_POWER("power"),
		UNKNOWN("Unknown");
		
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
	
	private static final String NEW_LINE = "\n";
	
	private Map<String, Object> options;
	private GPSDocument gpsDocument;
	private Activity activity;
	private Lap lap;
	private Trackpoint trackpoint;
	private StringBuilder description;
	private boolean inActivity;
	private boolean inLap;
	private String textData;
	private List<Trackpoint> trackpoints;
	private Calendar calendar = Calendar.getInstance();
	
	private boolean readActivities;
	
	private static Logger logger = Logger.getLogger(FitlogHandler.class.getName());
	
	public FitlogHandler(Map<String, Object> options) {
		super();
		
		this.options = options;
		readActivities = (Boolean) options.get(Constants.Reader.READ_ACTIVITIES);
	}
	
	@Override
	public void startDocument() throws SAXException {
		initParsing();
	}

	private void initParsing() {
		gpsDocument = new GPSDocument((String) options.get(Constants.Reader.FILENAME));
		description = new StringBuilder();
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		
		GPXElement element = GPXElement.lookup(qName);
		switch (element) {
		case ATHLETE:
			processAthleteStart(attributes);
			break;
		case ACTIVITY:
			processActivityStart(attributes);
			break;
		case METADATA:
			processMetadataStart(attributes);
			break;
		case LAP:
			processLapStart(attributes);
			break;
		case TRACK:
			processTrackStart();
			break;
		case TRACKPOINT:
			processTrackpointStart(attributes);
			break;
		case DISTANCE:
			processDistanceStart(attributes);
			break;
		case DURATION:
			processDurationStart(attributes);
			break;
		case HEART_RATE:
			processHeartRateStart(attributes);
			break;
		case CADENCE:
			processCadenceStart(attributes);
			break;
		case POWER:
			processPowerStart(attributes);
			break;
		case CALORIES:
			processCaloriesStart(attributes);
			break;
		default:
			// Ignore element
		}
		
		clearReadData();
	}
	
	private void processAthleteStart(Attributes attributes) throws SAXException {
		String id = getValue(attributes, GPXElement.ID);
		description.append(String.format("Athlete Id: %s", id)).append(NEW_LINE);
		
		String name = getValue(attributes, GPXElement.NAME);
		description.append(String.format("Athlete Name: %s", name)).append(NEW_LINE);
	}
	
	private void processActivityStart(Attributes attributes) throws SAXException {
		if (readActivities) {
			activity = new Activity();
			activity.setParent(gpsDocument);
			inActivity = true;
			
			try {
				String id = getValue(attributes, GPXElement.ID);
				description.append(String.format("Activity Id: %s", id)).append(NEW_LINE);
				activity.setCreator(getValue(attributes, GPXElement.SOURCE));//58406
				String startTime = getValue(attributes, GPXElement.START_TIME);
				activity.setStartTime(parseDateTime(startTime));
			} catch (ParseException e) {
				logger.error(e.getMessage());
				throw new SAXException(e.getMessage());
			}
		}
		
		activity.add(getSession(activity));
	}
	
	private Session getSession(Activity activity) {
		Session session = new Session(activity);
		session.setStartTime(activity.getStartTime());
		session.setEndTime(activity.getEndTime());
		
		return session;
	}
	
	private void processMetadataStart(Attributes attributes) throws SAXException {
		try {
			String source = getValue(attributes, GPXElement.SOURCE);
			description.append(String.format("Source: %s", source)).append(NEW_LINE);
			activity.setCreator(source);
			String created = getValue(attributes, GPXElement.CREATED);
			Date timeCreated = parseDateTime(created);
			activity.getMetadata().setTimeCreated(timeCreated);
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processLapStart(Attributes attributes) throws SAXException {
		lap = new ActivityLap(activity);
		inLap = true;
		
		try {
			String startTime = getValue(attributes, GPXElement.START_TIME);
			lap.setStartTime(parseDateTime(startTime));
			
			String durationSeconds = getValue(attributes, GPXElement.DURATION_SECONDS);
			lap.setTimerTime(parseDouble(durationSeconds));
			
			calendar.setTime(lap.getStartTime());
			calendar.add(Calendar.SECOND, lap.getTimerTime().intValue());
			lap.setEndTime(calendar.getTime());
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processTrackStart() {
		trackpoints = new ArrayList<Trackpoint>();
	}
	
	private void processTrackpointStart(Attributes attributes) throws SAXException {
		trackpoint = new Trackpoint(activity);
			
		try {
			String time = getValue(attributes, GPXElement.TRACKPOINT_TIME);
			calendar.setTime(activity.getStartTime());
			calendar.add(Calendar.SECOND, Integer.valueOf(time));
			trackpoint.setTimestamp(calendar.getTime());
			
			String latitude = getValue(attributes, GPXElement.TRACKPOINT_LATITUDE);
			if (!latitude.isEmpty()) {
				trackpoint.setLatitude(parseDouble(latitude));
			}
			
			String longitude = getValue(attributes, GPXElement.TRACKPOINT_LONGITUDE);
			if (!longitude.isEmpty()) {
				trackpoint.setLongitude(parseDouble(longitude));
			}
			
			String altitude = getValue(attributes, GPXElement.TRACKPOINT_ELEVATION);
			if (!altitude.isEmpty()) {
				trackpoint.setAltitude(parseDouble(altitude));
			}
			
			String distance = getValue(attributes, GPXElement.TRACKPOINT_DISTANCE);
			if (!distance.isEmpty()) {
				trackpoint.setDistance(parseDouble(distance));
			}
			
			String heartRate = getValue(attributes, GPXElement.TRACKPOINT_HEART_RATE);
			if (!heartRate.isEmpty()) {
				trackpoint.setHeartRate((short) parseDouble(heartRate));
			}
			
			String cadence = getValue(attributes, GPXElement.TRACKPOINT_CADENCE);
			if (!cadence.isEmpty()) {
				trackpoint.setCadence((short) parseDouble(cadence));
			}
			
			String power = getValue(attributes, GPXElement.TRACKPOINT_POWER);
			if (!power.isEmpty()) {
				trackpoint.setPower((int) parseDouble(power));
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processDistanceStart(Attributes attributes) throws SAXException {
		try {
			String totalMeters = getValue(attributes, GPXElement.TOTAL_METERS);
			if (!totalMeters.isEmpty()) {
				double distance = parseDouble(totalMeters);
				
				if (inLap) {
					lap.setDistance(distance);
				} else if (inActivity) {
					activity.getFirstSession().setDistance(distance);
				}
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processDurationStart(Attributes attributes) throws SAXException {
		try {
			String totalSeconds = getValue(attributes, GPXElement.TOTAL_SECONDS);
			if (!totalSeconds.isEmpty()) {
				double duration = parseDouble(totalSeconds);
				
				if (inActivity) {
					activity.setTotalTimerTime(duration);
					calendar.setTime(activity.getStartTime());
					calendar.add(Calendar.SECOND, activity.getTotalTimerTime().intValue());
					activity.setEndTime(calendar.getTime());
					
					activity.getFirstSession().setTimerTime(duration);
				}
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new SAXException(e.getMessage());
		}
	}
	
	private void processHeartRateStart(Attributes attributes) {
		String avgHeartRate = getValue(attributes, GPXElement.AVERAGE_HEART_RATE);
		if (!avgHeartRate.isEmpty()) {
			short averageHeartRate = Short.valueOf(avgHeartRate);
		
			if (inLap) {
				lap.setAverageHeartRate(averageHeartRate);
			} else if (inActivity) {
				activity.getFirstSession().setAverageHeartRate(averageHeartRate);
			}
		}
		
		String maxHeartRate = getValue(attributes, GPXElement.MAXIMUM_HEART_RATE);
		if (!maxHeartRate.isEmpty()) {
			short maximumHeartRate = Short.valueOf(maxHeartRate);
					
			if (inLap) {
				lap.setMaximumHeartRate(maximumHeartRate);
			} else if (inActivity) {
				activity.getFirstSession().setMaximumHeartRate(maximumHeartRate);
			}
		}
	}
	
	private void processCadenceStart(Attributes attributes) {
		String avgCadence = getValue(attributes, GPXElement.AVERAGE_CADENCE);
		if (!avgCadence.isEmpty()) {
			short averageCadence = Short.valueOf(avgCadence);
		
			if (inLap) {
				lap.setAverageCadence(averageCadence);
			} else if (inActivity) {
				activity.getFirstSession().setAverageCadence(averageCadence);
			}
		}
		
		String maxCadence = getValue(attributes, GPXElement.MAXIMUM_CADENCE);
		if (!maxCadence.isEmpty()) {
			short maximumCadence = Short.valueOf(maxCadence);
		
			if (inLap) {
				lap.setMaximumCadence(maximumCadence);
			} else if (inActivity) {
				activity.getFirstSession().setMaximumCadence(maximumCadence);
			}
		}
	}
	
	private void processPowerStart(Attributes attributes) {
		String avgPower = getValue(attributes, GPXElement.AVERAGE_POWER);
		if (!avgPower.isEmpty()) {
			int averagePower = Integer.valueOf(avgPower);
		
			if (inLap) {
				lap.setAveragePower(averagePower);
			} else if (inActivity) {
				activity.getFirstSession().setAveragePower(averagePower);
			}
		}
		
		String maxPower = getValue(attributes, GPXElement.MAXIMUM_POWER);
		if (!maxPower.isEmpty()) {
			int maximumPower = Integer.valueOf(maxPower);
		
			if (inLap) {
				lap.setMaximumPower(maximumPower);
			} else if (inActivity) {
				activity.getFirstSession().setMaximumPower(maximumPower);
			}
		}
	}
	
	private void processCaloriesStart(Attributes attributes) {
		String cals = getValue(attributes, GPXElement.TOTAL_CALORIES);
		if (!cals.isEmpty()) {
			int calories = Integer.valueOf(cals);
		
			if (inLap) {
				lap.setCalories(calories);
			} else if (inActivity) {
				activity.getFirstSession().setCalories(calories);
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
		case ACTIVITY:
			processActivityEnd();
			break;
		case LAP:
			processLapEnd();
			break;
		case TRACK:
			processTrackEnd();
			break;
		case TRACKPOINT:
			processTrackpointEnd();
			break;
		case NAME:						//2018-05-07: 12335
			processNameEnd();
			break;
		case NOTES:						//2018-05-07: 12335
			processNotesEnd();
			break;
		default:
			// Do nothing
		}
		
		clearReadData();
	}
	
	private void processActivityEnd() {
//		activity.setNotes(description.toString());			//2018-05-07: 12335
		activity.setNotes( activity.getNotes() + "\n" + description.toString());
		activity.setEndTime(activity.getLastTrackpoint().getTimestamp());
		activity.getFirstSession().setEndTime(activity.getEndTime());
		gpsDocument.add(activity);
		inActivity = false;
	}
	
	private void processLapEnd() {
		activity.add(lap);
		lap = null;
		inLap = false;
	}
	
	private void processTrackEnd() {
		activity.setTrackpoints(trackpoints);
	}
	
	private void processTrackpointEnd() {
		trackpoints.add(trackpoint);
	}
	
	//2018-05-07: 12335
	private void processNameEnd() {
		System.out.println( inActivity + "  " + textData);
		if ( inActivity )
			activity.setName( textData);
		else
			gpsDocument.setName( textData);
	}
	
	//2018-05-07: 12335
	private void processNotesEnd() {
		System.out.println( textData);
		activity.setNotes( textData);
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
		return Formatters.getDecimalFormat().parse(number).doubleValue();
	}

	private Date parseDateTime(String dateTime) throws ParseException {
		final int DATE_FORMAT_SECONDS_LENGTH = 20;
		final int DATE_FORMAT_MILIS_LENGTH = 24;
		
		if (dateTime.length() == DATE_FORMAT_SECONDS_LENGTH) {
			return Formatters.getSimpleDateFormat().parse(dateTime);
		} else if (dateTime.length() == DATE_FORMAT_MILIS_LENGTH) {
			return Formatters.getSimpleDateFormatMilis().parse(dateTime);
		}
		throw new ParseException(Messages.getMessage("gpxHandler.invalidDateTimeFormat"), 0);
	}
	
	@Override
	public GPSDocument getGPSDocument() {
		return gpsDocument;
	}
}
