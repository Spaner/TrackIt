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
package com.trackit.business.writer.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garmin.fit.Field;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Formatters;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.UTMLocation;
import com.trackit.business.exception.WriterException;
import com.trackit.business.readwrite.csv.CSVField;
import com.trackit.business.writer.Writer;
import com.trackit.business.writer.WriterTemplate;


public class CSVFileWriter extends WriterTemplate implements Writer {
//	private static final String[] targetFields = new String[] {
//		"ID", "LATITUDE", "LONGITUDE", "ALTITUDE", "DISTANCE", "DISTANCE_FROM_PREVIOUS",
//		"SPEED", "HEART_RATE", "CADENCE", "POWER", "GRADE" };
	private static final CSVField [] csvFields = { CSVField.NO, CSVField.DATETIME,
			CSVField.LATITUDE, CSVField.LONGITUDE, CSVField.ELEVATION,
			CSVField.DISTANCE, CSVField.SPEED,
			CSVField.HEART_RATE, CSVField.CADENCE, CSVField.POWER, CSVField.GRADE,
			CSVField.BEARING				//12335: 2018-03-03 - added output only field
		};
	private static SimpleDateFormat datetimeFormat = new SimpleDateFormat( "yyyy/MM/dd,HH:mm:ss.SSS");
	private static final String LINE_TERMINATOR = "\n";
	boolean writeActivities;
	boolean writeCourses;
	
	BufferedWriter writer = null;
	
	UTMLocation last, current;				//12335: 2018-03-03 - to compute bearing
	
	public CSVFileWriter() {
		this(new HashMap<String, Object>());
	}
	
	public CSVFileWriter(Map<String, Object> options) {
		super(options);
		setUp();
	}
	
	private void setUp() {
		writeActivities = (Boolean) getOptions().get(Constants.Writer.WRITE_ACTIVITIES);
		writeCourses = (Boolean) getOptions().get(Constants.Writer.WRITE_COURSES);
	}
	
	@Override
	public void write(List<GPSDocument> documents) throws WriterException {
		for (GPSDocument document : documents) {
			write(document);
		}
	}
	
	@Override
	public void write(GPSDocument document) throws WriterException {
		String filename = document.getFileName();

		// 12335: 2016-05-20 Write first line with CSV line structure
		try {
			/*BufferedWriter*/ writer = new BufferedWriter(new FileWriter(new File(filename)));
			StringBuilder builder = new StringBuilder();
			for( CSVField field : csvFields)
				builder.append( field.fieldName + ",");
			removeLastComma( builder);
			builder.append( LINE_TERMINATOR);
			writer.write( builder.toString());
			
		if (writeActivities) {
			for (Activity activity : document.getActivities()) {
				writeDocument(activity.getTrackpoints(), filename);
			}
		}
		
		if (writeCourses) {
			for (Course course : document.getCourses()) {
				writeDocument(course.getTrackpoints(), filename);
			}
		}
			
			writer.close();
		} catch( IOException e) {
			throw new WriterException(e.getMessage());
		}
		// 12335: end
		
	}
	
	private void writeDocument(List<Trackpoint> trackpoints, String filename) throws WriterException {
		try {
//			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
			
			last = null;									//12335: 2018-03-03					
			for (Trackpoint trackpoint : trackpoints) {
				String line = getLine(trackpoint);
				writer.write(line);
			}
			
//			writer.close();
		} catch (IOException e) {
			throw new WriterException(e.getMessage());
		}
	}

	private String getLine(Trackpoint trackpoint) {
		StringBuilder lineBuilder = new StringBuilder();
//		for (String field : targetFields) {
		for ( CSVField field: csvFields ) {
			Object value = trackpoint.get(field.name);
//			value = (value == null ? "" : value);
			if ( value != null || field == CSVField.BEARING ) {
				if ( field == CSVField.DATETIME ) {
					value = datetimeFormat.format( value);
				}
				else {
//12335: 2017-08-05
//					if ( field == CSVField.SPEED ) {
//						double speed = (double) value * 3.6;
//						value = speed;
//					}
					int precision = -1;
					switch ( field) {
					case NO:
					case HEART_RATE:
					case CADENCE:
					case POWER:
						precision = 0;
						break;
					case ELEVATION:
						precision = 3;
						break;
					case SPEED:
						value = (double) value * 3.6;
						precision = -1;
						break;
					case BEARING:		//12335: 2018-03-03 - computes and writes bearing data
						current = new UTMLocation( trackpoint.getLatitude(), trackpoint.getLongitude(),
								   trackpoint.getAltitude());
						if ( last != null ) {
							double arc = 90. - Math.atan2( current.getNorthing() - last.getNorthing(),
														   current.getEasting()  - last.getEasting())
												* 180. / Math.PI;
							if ( arc < 0 )
								arc += 360;
							value = arc;
						} else
							value = 0;
						last = current;
						precision = 3;
						break;
					default:
						precision = -1;
						break;
					}
					if ( precision < 0 )
						value = Formatters.getDefaultDecimalFormat().format( value);
					else
						value = Formatters.getDefaultDecimalFormat( precision).format( value);
				}
			}
			else {
				value = ""; 
			}
			lineBuilder.append(String.format("%s,", value));
		}
		
		removeLastComma(lineBuilder);
		lineBuilder.append(LINE_TERMINATOR);
		
		return lineBuilder.toString();
	}

	private void removeLastComma(StringBuilder lineBuilder) {
		int lastPos = lineBuilder.length() - 1;
		if (lineBuilder.charAt(lastPos) == ',') {
			lineBuilder.deleteCharAt(lastPos);
		}
	}
}
