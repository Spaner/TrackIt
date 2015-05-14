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
package com.henriquemalheiro.trackit.business.reader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.exception.ReaderException;
import com.henriquemalheiro.trackit.business.reader.fit.FITFileReader;
import com.henriquemalheiro.trackit.business.reader.fitlog.FitlogFileReader;
import com.henriquemalheiro.trackit.business.reader.gpx.GPXFileReader;
import com.henriquemalheiro.trackit.business.reader.kml.KMLFileReader;
import com.henriquemalheiro.trackit.business.reader.tcx.TCXFileReader;


public class ReaderFactory {
	private static ReaderFactory instance;
	
	private ReaderFactory() {
	}
	
	public static ReaderFactory getInstance() {
		if (instance == null) {
			instance = new ReaderFactory();
		}
		
		return instance;
	}
	
	public Reader getReader(File file, Map<String, Object> options) throws ReaderException {
		if (file == null || !file.exists()) {
			throw new ReaderException("The file " + file + " was not found.");
		}
		
		String filename = file.getAbsolutePath();
		String extension = filename.substring(filename.lastIndexOf('.') + 1, filename.length()).toLowerCase();
		
		FileType fileType = FileType.lookup(extension);
		if (fileType == null) {
			throw new UnsupportedOperationException("The file " + "is not supported.");
		}
		
		if (options == null) {
			options = new HashMap<String, Object>();
		}
		
		switch (fileType) {
		case GPX:
			return new GPXFileReader(options);
		case TCX:
			return new TCXFileReader(options);
		case FIT:
			return new FITFileReader(options);
		case KML:
			return new KMLFileReader(options);
		case FITLOG:
			return new FitlogFileReader(options);
		case ALL:
			throw new UnsupportedOperationException("The filetype 'All' is not supported!");
		default:
			throw new UnsupportedOperationException("The filetype 'All' is not supported!");
		}
	}
}
