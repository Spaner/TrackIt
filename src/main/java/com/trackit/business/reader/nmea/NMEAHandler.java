package com.trackit.business.reader.nmea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.Date;

import org.xml.sax.helpers.DefaultHandler;

import com.trackit.business.DocumentManager;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.Waypoint;
import com.trackit.business.reader.XMLFileHandler;

public class NMEAHandler extends DefaultHandler implements XMLFileHandler {
	
	private GPSDocument gpsDocument;
	private ArrayList<Trackpoint> trackpoints;
	private ArrayList<Waypoint>   waypoints;
	
	private static String separator     = "\\s*,\\s*";
	private Calendar calendar           = Calendar.getInstance( TimeZone.getTimeZone( "UTC"));
	private int      day,   month, year;
	private int      hours, minutes, seconds, miliseconds;

	public NMEAHandler( Map<String, Object> options) {
		super();
		gpsDocument = null;
	}
	
	@Override
	public GPSDocument getGPSDocument() {
		return gpsDocument;
	}

	public void parse( String filePath) {
		String nameToUse = "";
		try {
			File fileToRead = new File( filePath);
			nameToUse = DocumentManager.getInstance().getSingleTrackDocumentTrackName( filePath);
			BufferedReader reader = new BufferedReader( new FileReader( fileToRead));
			trackpoints = new ArrayList<>();
			waypoints   = new ArrayList<>();
			String line;
			while( (line = reader.readLine()) != null ) {
				if ( verifyCheckSum( line) ) {
					String[] fields = extractInfo( line).split( separator);
					switch ( fields[0]) {
					case "GPRMC":
						processRMC(fields);
						break;
					case "GPGGA":
						processGGA( fields);
						break;
					case "GPWPL":
						processWPL( fields);
						break;
					case "GPRMA":
						processRMA( fields);
						break;
					case "GPGLL":
						processGLL( fields);
						break;
					default:
						break;
					}
				}
			}
			reader.close();
		} catch (Exception e) {
				
		} finally {
			if ( trackpoints != null && trackpoints.size() > 0 ) {
				// NMEA has no document name so we set activity/course name 
				// as filename without extension
				gpsDocument = new GPSDocument( null);
				gpsDocument.setFileName( filePath);
				Course course = new Course();
				course.setName( nameToUse);
				course.addTrackpoints(trackpoints);
				for( Trackpoint point : trackpoints)
					point.setParent( course);
				if ( waypoints.size() > 0 )
					for( Waypoint wpt: waypoints)
						gpsDocument.add( wpt);
				gpsDocument.setName( nameToUse);
				gpsDocument.add( course);
			}
		}
		
	}
	
	private void processRMC( String[] fields) {
		if ( fields[3].length() > 0 && fields[5].length() > 0 ) {
			double latitude  = parseCoordinate( fields[3], fields[4]);
			double longitude = parseCoordinate( fields[5], fields[6]);
			Trackpoint pt = new Trackpoint( longitude, latitude);
			parseDayMonthYear( fields[9]);
			parseTime( fields[1]);
			calendar.set( year, 
					      month,
					      day,
					      hours,
					      minutes,
					      seconds);
			calendar.set( Calendar.MILLISECOND, miliseconds);
			pt.setTimestamp( calendar.getTime());
			
			if ( fields[7].length() > 0 )
				pt.setSpeed( Double.parseDouble( fields[7]) * .514444);
			trackpoints.add( pt);
		}
	}
	
	private void processGGA( String[] fields) {
		if ( fields[2].length() > 0 && fields[4].length() > 0 ) {
			double latitude = parseCoordinate( fields[2], fields[3]);
			double longitude = parseCoordinate( fields[4], fields[5]);
			parseTime( fields[1]);
			calendar.set( year, month, day, hours, minutes, seconds);
			Date timestamp = calendar.getTime();
			if ( fields[9].length() > 0 ) {
				double altitude = Double.parseDouble( fields[9]);
				for( Trackpoint pt: trackpoints)
					if ( pt.getLatitude() == latitude && pt.getLongitude() == longitude &&
					     pt.getTimestamp().equals( timestamp) ) {
						pt.setAltitude( altitude);
						break;
					}
			}
		}
	}
	
	private void processWPL( String[] fields) {
		if ( ! fields[1].isEmpty() && ! fields[3].isEmpty() ) {
			double latitude = parseCoordinate( fields[1], fields[2]);
			double longitude = parseCoordinate( fields[3], fields[4]);
			waypoints.add( new Waypoint( latitude, longitude, 0., fields[5], null));
		}
	}
	
	private void processGLL( String[] fields) {
		if ( ! fields[1].isEmpty() && ! fields[3].isEmpty() ) {
			double latitude  = parseCoordinate( fields[1], fields[2]);
			double longitude = parseCoordinate( fields[3], fields[4]);
			Trackpoint pt = new Trackpoint( longitude, latitude);
			if ( !fields[5].isEmpty() ) {
				parseTime( fields[5]);
				calendar.setTime( new Date());
				calendar.set( Calendar.HOUR_OF_DAY, hours);
				calendar.set( Calendar.MINUTE, minutes);
				calendar.set( Calendar.SECOND, seconds);
				calendar.set( Calendar.MILLISECOND, miliseconds);
				pt.setTimestamp( calendar.getTime());
			}
			trackpoints.add( pt);
		}
	}
	
	private void processRMA( String[] fields) {
		if ( ! fields[2].isEmpty() && ! fields[4].isEmpty() ) {
			double latitude  = parseCoordinate( fields[2], fields[3]);
			double longitude = parseCoordinate( fields[4], fields[5]);
			Trackpoint pt = new Trackpoint( longitude, latitude);
			if ( !fields[8].isEmpty() )
				pt.setSpeed( Double.parseDouble( fields[8]) * .514444);
			trackpoints.add( pt);
		}
	}
	
	private String extractInfo( String line) {
		int index = line.indexOf( '*');
		return line.substring( 1, index);
	}

	private boolean verifyCheckSum( String line) {
		int index = line.indexOf( '*');
		String extracted = extractInfo( line);
		int checksum = 0;
		for( int i=0; i< extracted.length(); i++)
			checksum ^= extracted.charAt( i);
		int lineChecksum = Integer.parseInt( line.substring( index+1), 16);
		return lineChecksum == checksum;
	}
	
	private double parseCoordinate( String valueStr, String direction) {
		double all = Double.parseDouble( valueStr);
		double degrees = Math.floor( all / 100.);
		double value   = (all - degrees * 100.) /60. + degrees;
		if ( direction.equals( "S") || direction.equals( "W") )
			value = - value;
		return value;
	}
	
	private void parseDayMonthYear( String dateString) {
		year  = Integer.parseInt( dateString.substring( 4, 6)) + 2000;
		month = Integer.parseInt( dateString.substring( 2, 4)) - 1;
	    day   = Integer.parseInt( dateString.substring( 0, 2));
	}
	
	private void parseTime( String timeString) {
		hours       = Integer.parseInt( timeString.substring( 0, 2));
		minutes     = Integer.parseInt( timeString.substring( 2, 4));
		seconds     = Integer.parseInt( timeString.substring( 4, 6));
		if ( timeString.length() > 7) {
			miliseconds = Integer.parseInt( timeString.substring( 7));
		}
		else
			miliseconds = 0;
	}
}
