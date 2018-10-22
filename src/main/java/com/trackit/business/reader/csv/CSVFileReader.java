package com.trackit.business.reader.csv;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.trackit.TrackIt;
import com.trackit.business.common.Constants;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.exception.ReaderException;
import com.trackit.business.reader.ReaderTemplate;
import com.trackit.business.utilities.TrackItPreferences;

public class CSVFileReader extends ReaderTemplate implements ErrorHandler{
	public CSVFileReader() {
		this( new HashMap<String, Object>());
	}
	
	public  CSVFileReader( Map<String, Object> options) {
		super( options);
		setPreferences();
	}
	
	private void setPreferences() {
		TrackItPreferences prefs = TrackIt.getPreferences();
		boolean validateDocument = prefs.getBooleanPreference(
										Constants.PrefsCategories.READER,  null, 
										Constants.Reader.VALIDATE_DOCUMENT, false);
		this.options.put( Constants.Reader.VALIDATE_DOCUMENT, validateDocument);
	}
	
	@Override
	public GPSDocument read( final InputStream inputStream, String filePath) throws ReaderException {
		GPSDocument gpsDocument = null;
		try {
			CSVHandler handler = new CSVHandler( options);
			handler.parse( filePath);
			gpsDocument = handler.getGPSDocument();
			
//			Course course = new Course();
//			course.setName( "Course simples");
//			gpsDocument.add(course);
//			ArrayList<Trackpoint> trackpoints = new ArrayList<Trackpoint>();
//			Trackpoint point = new Trackpoint( -9., 38.);
////			point.setTimestamp( new Date());
//			trackpoints.add( point);
//			point = new Trackpoint( -8., 38.);
////			point.setTimestamp(new Date( (new Date()).getTime() + 1000 * 3600 * 12));
//			trackpoints.add( point);
//			course.addTrackpoints(trackpoints);
//			for( Trackpoint pt : trackpoints)
//				pt.setParent( course);
//			
//			Date now = new Date();
//			Date then = new Date( now.getTime() + 1000 * 3600 *12);
//			trackpoints = new ArrayList<Trackpoint>();
//			point = new Trackpoint( -8., 38.);
//			point.setTimestamp( now);
//			trackpoints.add( point);
//			point = new Trackpoint( -8., 37.);
//			point.setTimestamp( then);
//			trackpoints.add(point);
//			Activity activity = new Activity();
//			activity.setName( "ACTIVITY!");
//			gpsDocument.add(activity);
//			Session session = new Session(activity);
//			activity.add(session);
//			session.setStartTime( now);
//			session.setEndTime( then);
//			activity.add( trackpoints);
//			for( Trackpoint pt : trackpoints)
//				pt.setParent( activity);
			
			
		} catch ( Throwable t) {
			logger.error( t.getMessage());
			throw new ReaderException( "Failed to read CSV file");
		}
		return gpsDocument;
	}
	
	@Override
	public void warning( SAXParseException e) throws SAXException {
		logger.warn( e.getMessage());
		throw new SAXException( e.getMessage());
	}
	
	@Override
	public void error( SAXParseException e) throws SAXException {
		logger.error( e.getMessage());
		throw e;
	}
	
	@Override
	public void fatalError( SAXParseException e) throws SAXException {
		logger.error( e.getMessage());
		throw e;
	}

}
