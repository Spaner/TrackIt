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
package com.trackit.business.writer;

import java.io.File;
import java.util.Map;

import com.trackit.business.common.FileType;
import com.trackit.business.exception.WriterException;
import com.trackit.business.writer.csv.CSVFileWriter;
import com.trackit.business.writer.fit.FITFileWriter;
import com.trackit.business.writer.fitlog.FitlogFileWriter;
import com.trackit.business.writer.gpx.GPXFileWriter;
import com.trackit.business.writer.kml.KMLFileWriter;
import com.trackit.business.writer.nmea.NMEAFileWriter;
import com.trackit.business.writer.tcx.TCXFileWriter;


public class WriterFactory {
	private static WriterFactory instance;
	
	private WriterFactory() {
	}
	
	public static WriterFactory getInstance() {
		if (instance == null) {
			instance = new WriterFactory();
		}
		
		return instance;
	}
	
	public Writer getWriter(File outputFile, Map<String, Object> options) throws WriterException {
		if (outputFile == null) {
			throw new WriterException("Undefined output file.");
		}
		
		String filename = outputFile.getAbsolutePath();
		String extension = filename.substring(filename.lastIndexOf('.') + 1, filename.length()).toLowerCase();
		
		FileType fileType = FileType.lookup(extension);
		if (fileType == null) {
			throw new UnsupportedOperationException("The file " + outputFile + " is not supported.");
		}
		
		switch (fileType) {
		case GPX:
			return new GPXFileWriter(options);
		case TCX:
			return new TCXFileWriter(options);
		case FIT:
			return new FITFileWriter(options);
		case KML:
			return new KMLFileWriter(options);
		case CSV:
			return new CSVFileWriter(options);
		case FITLOG:
			return new FitlogFileWriter(options);
		case NMEA:
			return new NMEAFileWriter( options);
		default:
			throw new UnsupportedOperationException("The filetype is not supported!");
		}
	}
}
