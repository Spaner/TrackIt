package com.trackit.business.reader.nmea;

import java.io.InputStream;
import java.util.Map;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.trackit.business.domain.GPSDocument;
import com.trackit.business.exception.ReaderException;
import com.trackit.business.reader.ReaderTemplate;

public class NMEAFileReader extends ReaderTemplate implements ErrorHandler {
	
	public NMEAFileReader() {
		super();
	}
	
	public NMEAFileReader( Map<String, Object> options) {
		super( options);
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		logger.warn( exception.getMessage());
		throw new SAXException( exception.getMessage());
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		logger.error( exception.getMessage());
		throw exception;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		logger.error( exception.getMessage());
		throw exception;
	}

	@Override
	public GPSDocument read(InputStream inputStream, String filePath) throws ReaderException {
		GPSDocument document = null;
		try {
			NMEAHandler handler = new NMEAHandler( options);
			handler.parse( filePath);
			document = handler.getGPSDocument();
		} 
		 catch ( Throwable t ) {
			logger.error( t.getMessage());
			throw new ReaderException( "Failed to read NMEA file");
		}
		return document;
	}

}
