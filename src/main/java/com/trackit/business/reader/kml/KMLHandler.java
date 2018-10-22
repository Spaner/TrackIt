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
package com.trackit.business.reader.kml;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.trackit.business.common.Constants;
import com.trackit.business.common.Formatters;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CourseTrack;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Track;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.Waypoint;
import com.trackit.business.reader.XMLFileHandler;
import com.trackit.business.utilities.StringUtilities;
import com.trackit.business.writer.kml.KMLExtendedDataType;


public class KMLHandler extends DefaultHandler implements XMLFileHandler {
	private static final String FOLDER = "Folder";
	private static final String LINESTRING = "LineString";
	private static final String COORDINATES = "coordinates";
	private static final String NAME = "name";
	private static final String PLACEMARK = "Placemark";
	private static final String POINT = "Point";
	private static final String DESCRIPTION = "description";
	private static final String DOCUMENT = "Document";
	
	private static final String TRACK = "Track";		// 12335: 2018-08-09
	private static final String WHEN  = "when";			// 12335: 2018-08-09
	private static final String COORD = "coord";		// 12335: 2018-08-09
	private static final String VALUE = "value";		// 12335: 2018-08-13
	private static final String SIMPLE_ARRAY_DATA = "SimpleArrayData";

	
	private static final String WAYPOINTS_FOLDER_NAME = "Waypoints";

	private Map<String, Object> options;
	private GPSDocument gpsDocument;
	private Course course;
	private Track track;
	private String courseName;
	private String textData;
	private boolean inCourse;
	private boolean inFolder;
	private boolean inWaypoints;
	private boolean inWaypoint;
	private Waypoint waypoint;
	private boolean inDocument;//58406
	
	private boolean inTrack;
	private boolean inSimpleArrayData;
	private KMLExtendedDataType dataType;
	private List<Date> whenDates;
	private List<Location> locations;
	private List<Short> heartRate;
	private List<Short> cadence;
	private List<Integer> power;
	private List<Double> speed;
	
	private boolean readCourses;
	
	private static Logger logger = Logger.getLogger(KMLHandler.class.getName());
	
	public KMLHandler(Map<String, Object> options) {
		super();
		
		this.options = options;
		
		readCourses = (Boolean) options.get(Constants.Reader.READ_COURSES);
	}
	
	@Override
	public void warning(SAXParseException e) throws SAXException {
        logger.error(e.getMessage());
    }

	@Override
    public void error(SAXParseException e) throws SAXException {
        logger.error(e.getMessage());
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        logger.error(e.getMessage());
    }
	
	@Override
	public void startDocument() throws SAXException {
		gpsDocument = new GPSDocument((String) options.get(Constants.Reader.FILENAME));
		
		init();
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (localName.equalsIgnoreCase(LINESTRING) && readCourses) {
			course = new Course();
			track = new CourseTrack(course);
			course.add(track);
			if (courseName != null) {
				course.setName(courseName);
			}
			inCourse = true;
		} else if (localName.equalsIgnoreCase(FOLDER) && readCourses) {
			inFolder = true;
		} else if (localName.equalsIgnoreCase(PLACEMARK) && inWaypoints) {
			waypoint = new Waypoint();
		} else if (localName.equalsIgnoreCase(POINT) && inWaypoints) {
			inWaypoint = true;
		//58406#############################################################################
		} else if (localName.equalsIgnoreCase(DOCUMENT)){
			inDocument = true;
		} else if ( localName.equalsIgnoreCase( TRACK) ) {
			inTrack = true;
			whenDates = new ArrayList<>();
			locations = new ArrayList<>();
			heartRate = cadence = null;
			power     = null;
			speed     = null;
		} else if ( localName.equalsIgnoreCase( SIMPLE_ARRAY_DATA) && inTrack) {
			System.out.println( attributes.getValue( "name"));
			dataType = KMLExtendedDataType.lookup( attributes.getValue( "name"));
			if ( dataType != null ) {
				inSimpleArrayData = true;
				switch ( dataType) {
				case SPEED:
					speed = new ArrayList<>();
					break;
				case HEART_RATE:
					heartRate = new ArrayList<>();
					break;
				case CADENCE:
					cadence = new ArrayList<>();
					break;
				case POWER:
					power = new ArrayList<>();
					break;
				default:
					break;
				}
			}
		}
		//##################################################################################
		textData = "";
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		textData = textData + new String(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equalsIgnoreCase(LINESTRING) && inCourse) {
			gpsDocument.add(course);
			inCourse = false;
		} else if (localName.equalsIgnoreCase(COORDINATES) && (inCourse || inWaypoint)) {
			List<Location> coordinates = parseCoordinates(textData);
			for (Location location : coordinates) {
				if (inWaypoint) {
					waypoint.setLongitude(location.getLongitude());
					waypoint.setLatitude(location.getLatitude());
					waypoint.setAltitude(location.getAltitude());
				} else if (inCourse) {
					Trackpoint trackpoint = new Trackpoint(course);
					trackpoint.setLongitude(location.getLongitude());
					trackpoint.setLatitude(location.getLatitude());
					trackpoint.setAltitude(location.getAltitude());
					course.add(trackpoint);
				}
			}
		} else if (localName.equalsIgnoreCase(NAME) && readCourses) {
			String name = textData;
			
			if (courseName == null) {
				courseName = textData;
			} else if (name.equalsIgnoreCase(WAYPOINTS_FOLDER_NAME) && inFolder) {
				inWaypoints = true;
			} else if (inWaypoints) {
				waypoint.setName(name);
			} else {
				courseName = textData; // override with the most specific name
			}
		} else if (localName.equalsIgnoreCase(DESCRIPTION) && inWaypoint) {
			waypoint.setDescription(textData);
		} else if (localName.equalsIgnoreCase(POINT) && inWaypoint) {
			gpsDocument.add(waypoint);
			waypoint.setParent(gpsDocument);
			inWaypoint = false;
		} else if (localName.equalsIgnoreCase(FOLDER) && inFolder) {
			inFolder = false;
			if (inWaypoints) {
				inWaypoints = false;
			}
		} else if ( localName.equalsIgnoreCase( TRACK) && inTrack) {
			System.out.println( "whenCount: " + whenDates.size() + "  locCount " + locations.size());
			System.out.println( heartRate != null ? heartRate.size() : "null");
			System.out.println( cadence != null ? cadence.size() : "null");
			System.out.println( power != null ? power.size() : "null");
			course = new Course();
			track = new CourseTrack(course);
			course.add(track);
			gpsDocument.add( course);
			for( int i=0; i<whenDates.size(); i++) {
				Trackpoint trackpoint = new Trackpoint( null);
				trackpoint.setLongitude( locations.get(i).getLongitude());
				trackpoint.setLatitude( locations.get(i).getLatitude());
				trackpoint.setAltitude( locations.get( i).getAltitude());
				trackpoint.setTimestamp( whenDates.get( i));
				if ( speed != null )
					trackpoint.setSpeed( speed.get( i));
				if ( heartRate != null )
					trackpoint.setHeartRate( heartRate.get( i));
				if ( cadence != null )
					trackpoint.setCadence( cadence.get( i));
				if ( power != null )
					trackpoint.setPower( power.get( i));
				course.add( trackpoint);
				trackpoint.setParent( course);
			}
			inTrack = false;
		} else if ( localName.equalsIgnoreCase( WHEN) && inTrack ) {
			try {
				whenDates.add( Formatters.parseDate( textData));
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else if ( localName.equalsIgnoreCase( COORD) ) {
			List<String> coords = StringUtilities.splitString( textData, " ", true);
			Location location = new Location();
			location.setLongitude( Double.parseDouble( coords.get(0)));
			location.setLatitude( Double.parseDouble( coords.get(1)));
			location.setAltitude( Double.parseDouble( coords.get(2)));
			locations.add( location);
		} else if ( localName.equalsIgnoreCase( SIMPLE_ARRAY_DATA)) {
			inSimpleArrayData = false;
			dataType = null;
		} else if ( localName.equalsIgnoreCase( VALUE) && inSimpleArrayData ) {
			switch (dataType) {
			case HEART_RATE:
				heartRate.add( Short.parseShort( textData));
				break;
			case CADENCE:
				cadence.add( Short.parseShort( textData));
				break;
			case POWER:
				power.add( Integer.parseInt( textData));
				break;
			case SPEED:
				speed.add(  Double.parseDouble( textData) / 3.6) ;
				break;
			default:
				break;
			}
		}
		//58406#############################################################################
		else if (localName.equalsIgnoreCase(NAME) && inDocument){
			course.setCreator(textData);
		} else if (localName.equalsIgnoreCase(DOCUMENT) && inDocument){
			inDocument = false;
		}
		//##################################################################################
		textData = "";
	}
	
	private void init() {
		inCourse = false;
		inFolder = false;
		inWaypoints = false;
		inWaypoint = false;
		courseName = null;
		inDocument = false;//58406
	}
	
	private List<Location> parseCoordinates(String text) {
		final String doublePattern = "(-?\\d+.?\\d*)";
		final String spacePattern = "([\\s\\n]+)";
		final String coordinateSpacePattern = String.format("%s,%s,?%s?%s", doublePattern, doublePattern, doublePattern, spacePattern);
		final String coordinateCommaPattern = String.format("%s,%s,%s,", doublePattern, doublePattern, doublePattern);
		final String patternStr = String.format("(%s)|(%s)", coordinateSpacePattern, coordinateCommaPattern);
		
		List<Location> locations = new ArrayList<Location>();
		
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			Location location = new Location();
			boolean withError = false;
			for (int i = 1; i < matcher.groupCount(); i++) {
				try {
					switch (i) {
					case 2:
					case 7:
						if (matcher.group(i) != null) {
							location.setLongitude(parseDouble(matcher.group(i)));
						}
						break;
					case 3:
					case 8:
						if (matcher.group(i) != null) {
							location.setLatitude(parseDouble(matcher.group(i)));
						}
						break;
					case 4:
					case 9:
						if (matcher.group(i) != null) {
							location.setAltitude(parseDouble(matcher.group(i)));
						}
						break;
					default:
						// Do nothing
					}
				} catch (ParseException pe) {
					logger.error(pe.getMessage());
					withError = true;
				} catch (Throwable t) {
					logger.error(t);
				}
			}
			
			if (!withError) {
				locations.add(location);
			}
		}
		
		return locations;
	}
		
	private class Location {
		double latitude;
		double longitude;
		double altitude;
		
		Location() {
		}

		public double getLatitude() {
			return latitude;
		}

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		public double getAltitude() {
			return altitude;
		}

		public void setAltitude(double altitude) {
			this.altitude = altitude;
		}
	}
	
	private double parseDouble(String number) throws ParseException {
//12335: 2017-08-08
//		double result = Formatters.getDecimalFormat().parse(number).doubleValue();
		double result = Formatters.getDefaultDecimalFormat().parse(number).doubleValue();
		return result;
	}
	
	public GPSDocument getGPSDocument() {
		return gpsDocument;
	}
}
