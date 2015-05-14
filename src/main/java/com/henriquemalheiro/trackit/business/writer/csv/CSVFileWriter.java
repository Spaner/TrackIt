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
package com.henriquemalheiro.trackit.business.writer.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.WriterException;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterTemplate;


public class CSVFileWriter extends WriterTemplate implements Writer {
	private static final String[] targetFields = new String[] {
		"ID", "LATITUDE", "LONGITUDE", "ALTITUDE", "DISTANCE", "DISTANCE_FROM_PREVIOUS",
		"SPEED", "HEART_RATE", "CADENCE", "POWER", "GRADE" };
	private static final String LINE_TERMINATOR = "\n";
	boolean writeActivities;
	boolean writeCourses;
	
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
	}
	
	private void writeDocument(List<Trackpoint> trackpoints, String filename) throws WriterException {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));

			for (Trackpoint trackpoint : trackpoints) {
				String line = getLine(trackpoint);
				writer.write(line);
			}
			
			writer.close();
		} catch (IOException e) {
			throw new WriterException(e.getMessage());
		}
	}

	private String getLine(Trackpoint trackpoint) {
		StringBuilder lineBuilder = new StringBuilder();
		for (String field : targetFields) {
			Object value = trackpoint.get(field);
			value = (value == null ? "" : value);
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
