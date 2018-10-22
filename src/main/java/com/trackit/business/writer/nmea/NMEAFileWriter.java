package com.trackit.business.writer.nmea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Date;

import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.UTMLocation;
import com.trackit.business.domain.Waypoint;
import com.trackit.business.exception.WriterException;
import com.trackit.business.utilities.geo.NOAAMagneticDeclinationService;
import com.trackit.business.writer.Writer;
import com.trackit.business.writer.WriterTemplate;
import com.trackit.presentation.view.map.layer.MapControlsLayer;

public class NMEAFileWriter extends WriterTemplate implements Writer {
	
	private BufferedWriter writer = null;
	private double bottomLeftDeclination;
	private double bottomRightDeclination;
	private double topLeftDeclination ;
	private double topRightDeclination;
	private double minLat, maxLat;
	private double minLon, maxLon;
	
	public NMEAFileWriter() {
		this(new HashMap<String, Object>());
	}
	
	public NMEAFileWriter( Map<String, Object> options) {
		super( options);
	}

	@Override
	public void write(GPSDocument document) throws WriterException {
		String filename = document.getFileName();
		try {
			writer = new BufferedWriter( new FileWriter( new File( filename)));
			for( Activity activity: document.getActivities() )
				writeDocument( activity.getTrackpoints(), filename);
			for( Course course: document.getCourses() )
				writeDocument( course.getTrackpoints(), filename);
			System.out.println( "Way pts " + document.getWaypoints() != null + "  " + document.getWaypoints().size());
			if ( document.getWaypoints() != null )
				writeWaypoints( document.getWaypoints());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new WriterException(e.getMessage());
		}
	}

	@Override
	public void write(List<GPSDocument> documents) throws WriterException {
		for( GPSDocument document: documents)
			write( document);
	}

	private void writeDocument( List<Trackpoint> trackpoints, String filename) throws WriterException {
		maxLat =  -90.;
		minLat =   90.;
		maxLon = -180.;
		minLon =  180.;
		for( Trackpoint pt: trackpoints) {
			maxLat = Math.max( maxLat, pt.getLatitude());
			minLat = Math.min( minLat, pt.getLatitude());
			maxLon = Math.max( maxLon, pt.getLongitude());
			minLon = Math.min( minLon, pt.getLongitude());
		}
		Date date = trackpoints.get( 0).getTimestamp();
		NOAAMagneticDeclinationService service = new NOAAMagneticDeclinationService();
		bottomLeftDeclination  = service.getDeclination( minLat, minLon, date);
		bottomRightDeclination = service.getDeclination( minLat, maxLon, date);
		topLeftDeclination     = service.getDeclination( maxLat, minLon, date);
		topRightDeclination    = service.getDeclination( maxLat, maxLon, date);

		String line;
		Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "UTC"));
		try {
			UTMLocation last = null, current;
			for( Trackpoint pt: trackpoints) {
				calendar.setTime( pt.getTimestamp());
				String dayStamp = String.format( "%02d%02d%02d", calendar.get( Calendar.DAY_OF_MONTH),
										                         calendar.get( Calendar.MONTH)+1,
										                         calendar.get( Calendar.YEAR) % 100);
				String timeStamp = String.format( "%02d%02d%02d.%03d", calendar.get( Calendar.HOUR_OF_DAY),
						                                          calendar.get(Calendar.MINUTE),
						                                          calendar.get( Calendar.SECOND),
						                                          calendar.get(Calendar.MILLISECOND));
				line = "$GPRMC," + timeStamp + ",V," + transformCoordinate( pt.getLatitude(), true)
				                 + "," + transformCoordinate( pt.getLongitude(), false);
				if ( pt.getSpeed() != null )
					line += String.format( Locale.UK, ",%.2f", pt.getSpeed() * 1.943844);
				double arc = 0;
				current = new UTMLocation( pt.getLatitude(), pt.getLongitude(), 0.);
				if ( last != null ) {
					arc = 90. -  Math.atan2( current.getNorthing()-last.getNorthing(), current.getEasting()-last.getEasting()) * 180. / Math.PI;
					if ( arc < 0. )
						arc +=360.;
				}
				last = current;
				line += String.format( Locale.UK, ",%06.2f,", arc) + dayStamp + "," + getLocalDeclination( pt);
				line = addCheckSum( line) + "\n";
				writer.write( line);
				if ( pt.getAltitude() != null ) {
					line = "$GPGGA," + timeStamp + "," + transformCoordinate( pt.getLatitude(), true)
					       + "," + transformCoordinate( pt.getLongitude(), false)
					       + ",0,00,0.0," + String.format( Locale.UK, "%.3f", pt.getAltitude())
					       + ",M,0.0,M,,";
					line = addCheckSum( line) + "\n";
					writer.write( line);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void writeWaypoints( List<Waypoint> waypoints) {
		try {
			String line;
			for( Waypoint wpt: waypoints) {
				line = "$GPWPL," + transformCoordinate( wpt.getLatitude(),  true) + "," +
			                       transformCoordinate( wpt.getLongitude(), false) + "," +
						wpt.getName();
				line = addCheckSum( line) + "\n";
				writer.write( line);
			}			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private static String transformCoordinate(  double coordinateValue, boolean isLatitude) {
		double value = Math.abs( coordinateValue);
		double degrees = Math.floor( value);
		return String.format( Locale.UK, (isLatitude ? "%07.3f,%s": "%09.3f,%s"), 
				  				(value - degrees) * 60. + degrees * 100.,
				  				(isLatitude ? (coordinateValue>=0?"N":"S"):
				  							  (coordinateValue>=0?"E":"W")));
	}
	
	private String getLocalDeclination( Trackpoint pt) {
		double bottomDeclination = (bottomRightDeclination - bottomLeftDeclination) *
				(pt.getLongitude() - minLon) / (maxLon - minLon) + bottomLeftDeclination;
		double topDeclination = (topRightDeclination - topLeftDeclination) *
				(pt.getLongitude() - minLon) / (maxLon - minLon) + topLeftDeclination;
		double declination = (topDeclination - bottomDeclination) *
				(pt.getLatitude() - minLat) / (maxLat - minLat) + bottomDeclination;
		return String.format( Locale.UK, "%.1f,%s", Math.abs( declination), (declination>=0 ? "E": "W"));
	}
	
	private String addCheckSum( String line) {
		int checksum = 0;
		for( int i=1; i<line.length(); i++)
			checksum ^= line.charAt( i);
		line += "*" + String.format( "%02X", checksum);
		return line;
	}
}
