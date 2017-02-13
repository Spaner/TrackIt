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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.exception.ReaderException;

public abstract class ReaderTemplate implements Reader {
	private static final String FEATURE_NAMESPACES = "http://xml.org/sax/features/namespaces";
	private static final String FEATURE_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
	private static final String PARSER_SCHEMA_LANGUAGE_PROPERTY = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";
	
	private SAXParser parser;
	private XMLReader reader;
	private XMLFileHandler handler;
	private ErrorHandler errorHandler;
	
	private long executionTime;
	
	protected Map<String, Object> options;
	protected Logger logger = Logger.getLogger(ReaderTemplate.class.getName()); 
	
	public ReaderTemplate() {
		options = new HashMap<String, Object>();
		options.putAll(getDefaultOptions());
	}
	
	public ReaderTemplate(Map<String, Object> options) {
		this();
		this.options.putAll(options);
	}

	public abstract GPSDocument read(InputStream inputStream, String filePath) throws ReaderException;//58406
	
	protected GPSDocument readXMLFile(InputStream inputStream, final XMLFileHandler handler,
			final ErrorHandler errorHandler) throws ReaderException {
		try {
			this.handler = handler;
			this.errorHandler = errorHandler;
			this.parser = getParser();
			this.reader = getReader();
			
			return readXML(inputStream);
		} catch(SAXException se) {
			logger.error(se.getMessage());
			throw new ReaderException(se.getMessage());
		}
	}

	private GPSDocument readXML(final InputStream inputStream) throws ReaderException {
		startTimer();
		try {
			reader.parse(new InputSource(inputStream));
		} catch (SAXException e) {
			throw new ReaderException(e.getMessage());
		} catch (IOException e) {
			throw new ReaderException(e.getMessage());
		}
		stopTimer();
		printTimerInfo();
		return handler.getGPSDocument();
	}
	
	protected void startTimer() {
		executionTime = System.currentTimeMillis();
	}
	
	protected void stopTimer() {
		executionTime = System.currentTimeMillis() - executionTime;
	}
	
	protected void printTimerInfo() {
		//58406 - comment logger to simplify debug
		logger.info("File read in " + executionTime + " miliseconds.");
	}
	
	private SAXParser getParser() throws SAXException {
		SAXParser parser = null;
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature(FEATURE_NAMESPACES, true);
			spf.setFeature(FEATURE_NAMESPACE_PREFIXES, true);
			spf.setValidating((Boolean) options.get(Constants.Reader.VALIDATE_DOCUMENT));
			spf.setNamespaceAware(true);

			parser = spf.newSAXParser();
			parser.setProperty(PARSER_SCHEMA_LANGUAGE_PROPERTY, XML_SCHEMA_URI);
		} catch (ParserConfigurationException pce) {
			logger.error(pce.getMessage());
			throw new SAXException(pce.getMessage());
		}
		return parser;
	}
	
	private XMLReader getReader() throws SAXException {
		XMLReader reader = parser.getXMLReader();
		reader.setContentHandler(handler);
		reader.setErrorHandler(errorHandler);
		
		return reader;
	}
	
	protected Element createEmptyElement(Document document, String name) {
		Element emptyElement = document.createElement(name);
		
		return emptyElement;
	}
	
	protected Element createEmptyElementNS(Document document, String prefix, String name) {
		String fullQName = prefix + ":" + name;
		Element emptyElement = document.createElement(fullQName);
		
		return emptyElement;
	}
	
	protected Element createDateTimeElement(Document document, String name, Date dateTime) {
		Element dateTimeElement = document.createElement(name);
		Text time = document.createTextNode(String.valueOf(Formatters.getSimpleDateFormatMilis().format(dateTime)));
		dateTimeElement.appendChild(time);
		
		return dateTimeElement;
	}
	
	protected Element createTextElement(Document document, String name, String value) {
		Element textElement = document.createElement(name);
		Text text = document.createTextNode(value);
		textElement.appendChild(text);
		
		return textElement;
	}
	
	protected Element createShortElementNS(Document document, String prefix, String name, Short value) {
		String fullQName = prefix + ":" + name;
		Element shortElement = document.createElement(fullQName);
		
		Text shortValue = document.createTextNode(value.toString());
		shortElement.appendChild(shortValue);
		
		return shortElement;
	}
	
	protected Element createDoubleElementNS(Document document, String prefix, String name, Double value) {
		String fullQName = prefix + ":" + name;
		Element doubleElement = document.createElement(fullQName);
		
		Text doubleValue = document.createTextNode(value.toString());
		doubleElement.appendChild(doubleValue);
		
		return doubleElement;
	}
	
	protected Element createDoubleElement(Document document, String name, Double value) {
		Element doubleElement = document.createElement(name);
		Text doubleValue = document.createTextNode(String.valueOf(Formatters.getDecimalFormat().format(value)));
		doubleElement.appendChild(doubleValue);
		
		return doubleElement;
	}
	
	protected Element createShortElement(Document document, String name, Short value) {
		Element shortElement = document.createElement(name);
		Text shortValue = document.createTextNode(String.valueOf(value));
		shortElement.appendChild(shortValue);
		
		return shortElement;
	}
	
	public Map<String, Object> getOptions() {
		return options;
	}
	
	private Map<String, Object> getDefaultOptions() {
		Map<String, Object> options = new HashMap<String, Object>();
		
		
		boolean readFolders = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.READER, null,
				Constants.Reader.READ_FOLDERS, false);
		options.put(Constants.Reader.READ_FOLDERS, readFolders);

		boolean readActivities = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.READER, null,
				Constants.Reader.READ_ACTIVITIES, true);
		options.put(Constants.Reader.READ_ACTIVITIES, readActivities);
		
		boolean readCourses = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.READER, null,
				Constants.Reader.READ_COURSES, true);
		options.put(Constants.Reader.READ_COURSES, readCourses);
		
		boolean readWaypoints = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.READER, null,
				Constants.Reader.READ_WAYPOINTS, true);
		options.put(Constants.Reader.READ_WAYPOINTS, readWaypoints);
		
		boolean readIntoMultipleDocuments = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.READER, null,
				Constants.Reader.READ_INTO_MULTIPLE_DOCUMENTS, false);
		options.put(Constants.Reader.READ_INTO_MULTIPLE_DOCUMENTS, readIntoMultipleDocuments);
		
		return options;
	}
}