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
package com.henriquemalheiro.trackit.business.writer.gpx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.business.exception.WriterException;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterTemplate;


public class GPXFileWriter extends WriterTemplate implements Writer {
	private static final String GPX = "gpx";
	private static final String NAME = "name";
	private static final String METADATA = "metadata";
	private static final String LINK = "link";
	private static final String LINK_TEXT = "text";
	private static final String HREF = "href";
	private static final String TRACK = "trk";
	private static final String TRACK_SEGMENT = "trkseg";
	private static final String TRACKPOINT = "trkpt";
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "lon";
	private static final String ELEVATION = "ele";
	private static final String TIME = "time";
	private static final String EXTENSIONS = "extensions";
	private static final String TRACKPOINT_EXTENSION = "TrackPointExtension";
	private static final String HEART_RATE = "hr";
	private static final String CADENCE = "cad";
	private static final String TEMPERATURE = "atemp";
	private static final String WAYPOINT = "wpt";
	private static final String COMMENTS = "cmt";
	private static final String DESCRIPTION = "desc";
	private static final String SOURCE = "src";
	private static final String SYMBOL = "sym";
	private static final String TYPE = "type";
	private static final String CREATOR = "creator";
	private static final String VERSION = "version";
	
	private static final String NAMESPACE_XMLNS = "http://www.w3.org/2000/xmlns/";
	private static final String NAMESPACE_XSI = "xmlns:xsi";
	private static final String NAMESPACE_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String NAMESPACE_DEFAULT = "xmlns";
	private static final String NAMESPACE_GPX = "http://www.topografix.com/GPX/1/1";
	private static final String NAMESPACE_XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
	private static final String NAMESPACE_GPX_SCHEMA_LOCATION = "http://www.topografix.com/GPX/1/1/gpx.xsd";
	
	private static final String PREFIX_GARMIN_TRACKPOINT_EXTENSION_V2 = "gpxtpx";
	private static final String NAMESPACE_GARMIN_TRACKPOINT_EXTENSION_V2 = "xmlns:gpxtpx";
	private static final String NAMESPACE_URI_GARMIN_TRACKPOINT_EXTENSION_V2 = "http://www.garmin.com/xmlschemas/TrackPointExtension/v2";
	private static final String NAMESPACE_GARMIN_TRACKPOINT_EXTENSION_V2_SCHEMA_LOCATION = "http://www.garmin.com/xmlschemas/TrackPointExtensionv2.xsd";
	private static final String NAMESPACE_URI_GARMIN_GPX_EXTENSIONS_V3 = "http://www.garmin.com/xmlschemas/GpxExtensions/v3";
	private static final String NAMESPACE_GARMIN_GPX_EXTENSIONS_V3_SCHEMA_LOCATION = "http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd";
	
	private boolean writeActivities;
	private boolean writeCourses;
	private boolean writeWaypoints;
	
	private static Logger logger = Logger.getLogger(GPXFileWriter.class.getName());
	
	public GPXFileWriter() {
		this(new HashMap<String, Object>());
	}
	
	public GPXFileWriter(Map<String, Object> options) {
		super(options);
		
		getOptions().put(Constants.GPX.APP_NAME, Constants.APP_NAME);
		getOptions().put(Constants.GPX.APP_LINK, Constants.APP_LINK);
		getOptions().put(Constants.GPX.VERSION, Constants.APP_VERSION);
		setUp();
	}
	
	private void setUp() {
		writeActivities = (Boolean) getOptions().get(Constants.Writer.WRITE_ACTIVITIES);
		writeCourses = (Boolean) getOptions().get(Constants.Writer.WRITE_COURSES);
		writeWaypoints = (Boolean) getOptions().get(Constants.Writer.WRITE_WAYPOINTS);
	}

	@Override
	public void write(List<GPSDocument> documents) throws WriterException {
		for (GPSDocument document : documents) {
			writeDocument(document);
		}
	}
	
	@Override
	public void write(GPSDocument document) throws WriterException {
		writeDocument(document);
	}

	private void writeDocument(GPSDocument gpsDocument) throws WriterException {
		try {
			Document dom = getDOMTreeFromGPSDocument(gpsDocument);
			serializeXML(dom, gpsDocument.getFileName());
		} catch (GPXWriterException ge) {
			logger.error(ge.getMessage());
			throw new WriterException(ge);
		}
	}
	
	private void serializeXML(Document document, String filename) throws GPXWriterException {
		try {
			DOMSource domSource = new DOMSource(document);
			
			File outputFile = new File(filename);
			StreamResult streamResult = new StreamResult(new FileOutputStream(outputFile));

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			
			transformer.transform(domSource, streamResult);
		} catch (TransformerException te) {
		    logger.error(te.getMessage());
		    throw new GPXWriterException(te.getMessage());
		} catch (FileNotFoundException fnfe) {
			logger.error("The output file was not found!");
			throw new GPXWriterException(fnfe.getMessage());
		}
	}
	
	private Document getDOMTreeFromGPSDocument(GPSDocument gpsDocument) throws GPXWriterException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.newDocument();
			dom.setXmlStandalone(true);
			
			Element gpxElement = createGPXElement(dom, gpsDocument);
			
			if (writeWaypoints) {
				for (Waypoint waypoint : gpsDocument.getWaypoints()) {
					Element waypointElement = createWaypointElement(dom, waypoint);
					gpxElement.appendChild(waypointElement);
				}
				
				for (Course course : gpsDocument.getCourses()) {
					for (CoursePoint coursePoint : course.getCoursePoints()) {
						Waypoint waypoint = new Waypoint(coursePoint.getLatitude(), coursePoint.getLongitude(), coursePoint.getAltitude(),
								coursePoint.getName(), coursePoint.getTime());
						waypoint.setType(coursePoint.getType().name());
						Element waypointElement = createWaypointElement(dom, waypoint);
						gpxElement.appendChild(waypointElement);
					}
				}
			}
			
			if (writeActivities) {
				for (Activity activity : gpsDocument.getActivities()) {
					Element trackElement = dom.createElement(TRACK);
					gpxElement.appendChild(trackElement);
					
					trackElement.appendChild(createTextElement(dom, NAME, activity.getName()));
					
					for (Lap lap : activity.getLaps()) {
						Element trackSegment = dom.createElement(TRACK_SEGMENT);
						trackElement.appendChild(trackSegment);
						
						for (Trackpoint trackpoint : lap.getTrackpoints()) {
							Element trackpointElement = createTrackpointElement(dom, trackpoint, true);
							trackSegment.appendChild(trackpointElement);
						}
					}
				}
			}
			
			if (writeCourses) {
				for (Course course : gpsDocument.getCourses()) {
					Element trackElement = dom.createElement(TRACK);
					gpxElement.appendChild(trackElement);
					
					trackElement.appendChild(createTextElement(dom, NAME, course.getName()));
					
					for (Lap lap : course.getLaps()) {
						Element trackSegment = dom.createElement(TRACK_SEGMENT);
						trackElement.appendChild(trackSegment);
						
						for (Trackpoint trackpoint : lap.getTrackpoints()) {
							if (!trackpoint.isViewable()) {
								continue;
							}
							Element trackpointElement = createTrackpointElement(dom, trackpoint, false);
							trackSegment.appendChild(trackpointElement);
						}
					}
				}
			}
			
			return dom;
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
			throw new GPXWriterException(pce.getMessage());
		}
	}

	private Element createGPXElement(Document document, GPSDocument gpsDocument) {
		Element gpxElement = document.createElement(GPX);
		document.appendChild(gpxElement);
		
		boolean hasExtensions = gpsDocument.getActivities().size() > 0 ? true : false;
		
		gpxElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_XSI, NAMESPACE_XML_SCHEMA_INSTANCE);
		gpxElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_DEFAULT, NAMESPACE_GPX);
		
		if (hasExtensions) {
			gpxElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_GARMIN_TRACKPOINT_EXTENSION_V2, NAMESPACE_URI_GARMIN_TRACKPOINT_EXTENSION_V2);
		}
		
		String schemaLocations = NAMESPACE_GPX + " " + NAMESPACE_GPX_SCHEMA_LOCATION;
		if (hasExtensions) {
			schemaLocations += " " + NAMESPACE_URI_GARMIN_GPX_EXTENSIONS_V3 + " " + NAMESPACE_GARMIN_GPX_EXTENSIONS_V3_SCHEMA_LOCATION;
			schemaLocations += " " + NAMESPACE_URI_GARMIN_TRACKPOINT_EXTENSION_V2 + " " + NAMESPACE_GARMIN_TRACKPOINT_EXTENSION_V2_SCHEMA_LOCATION;
		}
		
		gpxElement.setAttribute(NAMESPACE_XSI_SCHEMA_LOCATION, schemaLocations);
		
		gpxElement.setAttribute(CREATOR, (String) getOptions().get(Constants.GPX.APP_NAME));
		gpxElement.setAttribute(VERSION, (String) getOptions().get(Constants.GPX.VERSION));
		
		gpxElement.normalize();
		
		Element metadataElement = document.createElement(METADATA);
		gpxElement.appendChild(metadataElement);
		
		Element linkElement = document.createElement(LINK);
		Attr hrefAttribute = document.createAttribute(HREF);
		hrefAttribute.setValue((String) getOptions().get(Constants.GPX.APP_LINK));
		linkElement.setAttributeNode(hrefAttribute);
		metadataElement.appendChild(linkElement);
		
		linkElement.appendChild(createTextElement(document, LINK_TEXT, (String) getOptions().get(Constants.GPX.APP_NAME)));
		
		Date now = new Date(System.currentTimeMillis());
		metadataElement.appendChild(createDateTimeElement(document, TIME, now));
		
		return gpxElement;
	}

	private Element createTrackpointElement(Document dom, Trackpoint trackpoint, boolean withExtensions) {
		Element trkptElement = createEmptyElement(dom, TRACKPOINT);
		trkptElement.setAttribute(LONGITUDE, Formatters.getDecimalFormat().format(trackpoint.getLongitude()));
		trkptElement.setAttribute(LATITUDE, Formatters.getDecimalFormat().format(trackpoint.getLatitude()));
		
		if (trackpoint.getAltitude() != null) {
			trkptElement.appendChild(createDoubleElement(dom, ELEVATION, trackpoint.getAltitude()));
		}
		
		if (trackpoint.getTimestamp() != null) {
			trkptElement.appendChild(createDateTimeElement(dom, TIME, trackpoint.getTimestamp()));
		}

		if (withExtensions && (trackpoint.getHeartRate() != null || trackpoint.getCadence() != null || trackpoint.getTemperature() != null)) {
			Element extensionsElement = createEmptyElement(dom, EXTENSIONS);
			
			Element trackpointExtensionElement = createEmptyElementNS(dom, PREFIX_GARMIN_TRACKPOINT_EXTENSION_V2, TRACKPOINT_EXTENSION);
			extensionsElement.appendChild(trackpointExtensionElement);
			
			if (trackpoint.getHeartRate() != null) {
				trackpointExtensionElement.appendChild(
						createShortElementNS(dom, PREFIX_GARMIN_TRACKPOINT_EXTENSION_V2, HEART_RATE, trackpoint.getHeartRate()));
			}
			
			if (trackpoint.getCadence() != null) {
				trackpointExtensionElement.appendChild(
						createShortElementNS(dom, PREFIX_GARMIN_TRACKPOINT_EXTENSION_V2, CADENCE, trackpoint.getCadence()));
			}
			
			if (trackpoint.getTemperature() != null) {
				trackpointExtensionElement.appendChild(
						createByteElementNS(dom, PREFIX_GARMIN_TRACKPOINT_EXTENSION_V2, TEMPERATURE, trackpoint.getTemperature()));
			}
			
			trkptElement.appendChild(extensionsElement);
		}

		return trkptElement;
	}
	
	private Element createWaypointElement(Document dom, Waypoint waypoint) {
		Element waypointElement = createEmptyElement(dom, WAYPOINT);
		waypointElement.setAttribute(LONGITUDE, Formatters.getDecimalFormat().format(waypoint.getLongitude()));
		waypointElement.setAttribute(LATITUDE, Formatters.getDecimalFormat().format(waypoint.getLatitude()));

		if (waypoint.getName() != null) {
			waypointElement.appendChild(createTextElement(dom, NAME, waypoint.getName()));
		}
		
		if (waypoint.getAltitude() != null) {
			waypointElement.appendChild(createDoubleElement(dom, ELEVATION, waypoint.getAltitude()));
		}
		
		if (waypoint.getTime() != null) {
			waypointElement.appendChild(createDateTimeElement(dom, TIME, waypoint.getTime()));
		}
		
		if (waypoint.getComments() != null) {
			waypointElement.appendChild(createTextElement(dom, COMMENTS, waypoint.getComments()));
		}
		
		if (waypoint.getDescription() != null) {
			waypointElement.appendChild(createTextElement(dom, DESCRIPTION, waypoint.getDescription()));
		}
		
		if (waypoint.getSource() != null) {
			waypointElement.appendChild(createTextElement(dom, SOURCE, waypoint.getSource()));
		}
		
		if (waypoint.getSym() != null) {
			waypointElement.appendChild(createTextElement(dom, SYMBOL, waypoint.getSym()));
		}
		
		if (waypoint.getType() != null) {
			waypointElement.appendChild(createTextElement(dom, TYPE, waypoint.getType()));
		}

		return waypointElement;
	}
}
