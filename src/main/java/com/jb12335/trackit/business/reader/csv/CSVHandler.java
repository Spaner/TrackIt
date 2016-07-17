package com.jb12335.trackit.business.reader.csv;

import com.henriquemalheiro.trackit.presentation.view.map.provider.bingmaps.elevation.Elevation;
import com.henriquemalheiro.trackit.business.common.Location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Session;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.reader.XMLFileHandler;

import com.jb12335.trackit.business.readwrite.csv.CSVField;

//import jxl.read.biff.File;

public class CSVHandler extends DefaultHandler implements XMLFileHandler {
	
	private GPSDocument gpsDocument;
	private Activity activity;
	private Course course;
	private Session session;
	private Date    startTime, endTime;
	private Map<CSVField, Integer> lookup;
	int     idxDate, idxTime, idxLatitude, idxLongitude;
	private ArrayList<Trackpoint> trackpoints;
	private ArrayList<String> fields;
	private double  speedFactor = 3.6;
	private boolean hasDateTime = false;
	private boolean firstLine   = true;
	
	private static Logger logger = Logger.getLogger(CSVHandler.class.getName());
	
	private static String separator     = "\\s*,\\s*";
	
	static String DefaultHeading = "No,Latitude,Longitude,Elevation,"+
			"TotalDistance,DistanceFromLast,Speed,HeartRate,Cadence,Power,Grade";
	
	static SimpleDateFormat datetimeFormat = new SimpleDateFormat( "yyyy/MM/dd,HH:mm:ss.SSS");
			
	public  CSVHandler( Map<String, Object> options) {
		super();
		gpsDocument = null;
	}
	
	public GPSDocument getGPSDocument() {
		return gpsDocument;
	}

	public void parse( String filePath) {
		String nameToUse = "";
		try {
			File fileToRead = new File( filePath);
			nameToUse = fileToRead.getName();
			BufferedReader reader = new BufferedReader( new FileReader( fileToRead));
			trackpoints = new ArrayList<>();
			String line;
			if ( (line = reader.readLine()) != null )
			{
				fields = SplitCSV( line);
				if ( isFieldStructure() )
					positionsLookup();
				else {
					fields = SplitCSV( DefaultHeading);
					positionsLookup();
					processTrackpoint( line);
					speedFactor = 1.;
				}
				while( (line = reader.readLine()) != null  )
					processTrackpoint( line);
				reader.close();
			}
		} catch (Exception e) {
				
		} finally {
			if ( trackpoints != null && trackpoints.size() > 0 ) {
				// CSV has no document name so we set activity/course name 
				// as filename without extension
				if ( nameToUse.toLowerCase().endsWith( ".csv") ) 
					nameToUse = nameToUse.substring(0, nameToUse.lastIndexOf('.'));
				gpsDocument = new GPSDocument( null);
				gpsDocument.setFileName( filePath);
//				if ( !lookup.containsKey( CSVField.ELEVATION) ) {
//					ArrayList<Location> locations = new ArrayList<>();
//					for( int i=0; i<trackpoints.size(); i++ )
//						locations.add( new Location( trackpoints.get(i).getLongitude(), trackpoints.get(i).getLatitude()));
//					try {
//						Elevation.fetchElevations( locations);
//						for( int i = 0; i<trackpoints.size(); i++)
//							trackpoints.get(i).setAltitude( locations.get(i).getAltitude());
//					} catch (Exception e ) {}
//				}
				if ( hasActivityData() ) {
					activity = new Activity();
					activity.setName( nameToUse);
					session = new Session(activity);
					activity.add(session);
					session.setStartTime( trackpoints.get(0).getTimestamp());
					session.setEndTime(   trackpoints.get( trackpoints.size() - 1).getTimestamp());
					activity.add( trackpoints);
					for( Trackpoint point : trackpoints)
						point.setParent( activity);
					gpsDocument.add(activity);
				}
				else
				{
					course = new Course();
					course.setName( nameToUse);
					course.addTrackpoints(trackpoints);
					for( Trackpoint point : trackpoints)
						point.setParent( course);
					gpsDocument.add( course);
				}
			}
		}
	}
	
	private void processTrackpoint( String line) {
		try {
			fields = SplitCSV( line);
			if ( firstLine )
				processFirstLine();
			Double latitude  = parseDouble( fields.get( idxLatitude));
			Double longitude = parseDouble( fields.get( idxLongitude));
			Trackpoint point = new Trackpoint( longitude, latitude);
			if ( hasDateTime ) {
				String datetime = fields.get( idxDate) + "," + fields.get( idxTime);
				point.setTimestamp( parseDateTime( datetime));
			}
			for( CSVField field : lookup.keySet() ) {
				if ( field == CSVField.TIME_FROM_LAST )
				{
					if ( trackpoints.size() == 0 ) {
						startTime = new Date();
						point.setTimestamp( startTime);
					}
					else {
						double since = parseDoubleFromField( CSVField.TIME_FROM_LAST);
						Date previous = trackpoints.get( trackpoints.size() - 1).getTimestamp();
						point.setTimestamp( new Date( previous.getTime() + (long) (1000 * since + .5)));
					}
				}
				else {
					double value = parseDoubleFromField( field);
					if ( !Double.isNaN( value) ) {
						switch ( field ) {
						case ELEVATION:
							point.setAltitude( value);				break;
						case DISTANCE:
							point.setDistance( value); 				break;
						case DISTANCE_FROM_LAST:
							point.setDistanceFromPrevious( value); 	break;
						case SPEED:
							point.setSpeed( value / speedFactor );	break;
						case GRADE:
							point.setGrade( (float) value);			break;
						case HEART_RATE:
							point.setHeartRate( (short) value);		break;
						case CADENCE:
							point.setCadence( (short) value); 		break;
						case POWER:
							point.setPower(  (int) value); 			break;
						case TEMPERATURE:
							point.setTemperature( (byte) value); 	break;
						default:
							break;
						}
					}
				}
			}
			trackpoints.add( point);
		} catch ( ParseException e) {
			logger.error( e.getMessage());
//			throw ( e.getMessage());
		}
		
		
	}
	
	private void processFirstLine() {
		// Objective: inspect first data line and eliminate from lookup any blank or null fields 
		if ( firstLine ) {
			ArrayList<CSVField> elements = new ArrayList<CSVField>( lookup.keySet());
			for (CSVField element : elements) {
				if ( fields.get( lookup.get( element)).isEmpty() )
					lookup.remove( element);
			}
			firstLine = false;
		}
	}
	
	private double parseDoubleFromField( CSVField element) throws ParseException {
		return parseDouble( fields.get( lookup.get( element)));
	}
	
	private double parseDouble( String element) throws ParseException {
		try {
			return Formatters.getDecimalFormat().parse(element).doubleValue();
		} catch (ParseException e) {
			return Double.NaN; 
		}
	}

	private Date parseDateTime(String dateTime) throws ParseException {
		String strToParse = dateTime;
		if ( strToParse.lastIndexOf( '.') == -1 )
			strToParse += ".000";
		return datetimeFormat.parse( strToParse);
	}
	
	private boolean hasActivityData() {
		if ( hasDateTime || lookup.containsKey( CSVField.TIME_FROM_LAST) )
			if ( lookup.containsKey( CSVField.HEART_RATE) 	||
				 lookup.containsKey( CSVField.CADENCE) 		||
				 lookup.containsKey( CSVField.POWER)  		||
				 lookup.containsKey( CSVField.TEMPERATURE))
				return true;
		return false;
	}
	
	private boolean isFieldStructure() {
		// Legal GPS CSV files with field structure in the first line
		// must define fields for latitude and longitude.
		
		boolean hasLatitude  = false,
				hasLongitude = false;
		for( String field : fields) {
			CSVField element = CSVField.lookupByField( field);
			if ( element != CSVField.UNKNOWN ) {
				if ( element == CSVField.LATITUDE )
					hasLatitude = true;
				if ( element == CSVField.LONGITUDE || element == CSVField.LONGTITUDE )
					hasLongitude = true;
			}
		}
		if ( hasLatitude && hasLongitude )
			return true;
		return false;
	}

	private void positionsLookup() {
		// Unrecognized fields are not indexed and will not be processed when reading points
		lookup = new HashMap<>();
		idxDate = idxTime = idxLatitude = idxLongitude = -1;
		hasDateTime = false;
		for( int i=0; i<fields.size(); i++) {
			String field = fields.get( i);
			CSVField target = CSVField.lookupByField( field);
			if ( target != CSVField.UNKNOWN ) {
				// Handle misspelled longitude
				if ( target == CSVField.LONGTITUDE )
					target = CSVField.LONGITUDE;
				// Put target and index into hashmap, except mandatory latitude and longitude
				switch ( target ) {
				case LATITUDE:  idxLatitude  = i; break;
				case LONGITUDE: idxLongitude = i; break;
				case DATE:
				case TIME:
					if ( target == CSVField.DATE )
						idxDate = i;
					else
						idxTime = i;
				default:
					lookup.put( target, new Integer(i));
				}
			}
		}
		// Date and time fields must be processed together
		if ( idxDate != -1 && idxTime != -1 ) {
			lookup.remove( CSVField.DATE);
			lookup.remove( CSVField.TIME);
			hasDateTime = true;
		}
	}
	
	private static ArrayList<String> SplitCSV( String line) {
		ArrayList<String> result = new ArrayList<String>();
		
		if ( line != null ) {
			String[] splitLine = line.split( separator);
			for( int i=0; i<splitLine.length; i++) {
				if ( !(splitLine[i] == null) || splitLine[i].length() != 0 )
					result.add( splitLine[i].trim());
			}
		}
		return result;
	}

	public static void setSeparator( char separatorChar) {
		separator = "\\s*" + separatorChar + "\\s*";
	}
	
}
