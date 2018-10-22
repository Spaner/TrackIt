package com.trackit.business.reader.csv;

import com.trackit.business.exception.TrackItException;

public class CSVReaderException extends TrackItException{
	private static final long serialVersionUID = 8003336681171099L;
	
	public CSVReaderException( String message) {
		super( message);
	}

}
