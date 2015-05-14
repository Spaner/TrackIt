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
package com.henriquemalheiro.trackit.business.writer.kml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Track;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.WriterException;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterTemplate;

public class KMLFileWriter extends WriterTemplate implements Writer {
	private static final String KML = "kml";
	private static final String FOLDER = "Folder";
	
	private static final String NAME = "name";
	private static final String PLACEMARK = "Placemark";
	private static final String STYLE = "Style";
	private static final String LINE_STYLE = "LineStyle";
	private static final String COLOR = "color";
	private static final String WIDTH = "width";
	private static final String LINE_STRING = "LineString";
	private static final String EXTRUDE = "extrude";
	private static final String TESSELLATE = "tessellate";
	private static final String ALTITUDE_MODE = "altitudeMode";
	private static final String COORDINATES = "coordinates";
	private static final String TRACK = "Track";
	private static final String WHEN = "when";
	private static final String COORD = "coord";
	
	
	private static final String DESCRIPTION = "description";
	private static final String ICON_STYLE = "IconStyle";
	private static final String ICON = "Icon";
	private static final String SCALE = "scale";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String XUNITS = "xunits";
	private static final String YUNITS = "yunits";
	private static final String HREF = "href";
	private static final String HOT_SPOT = "hotSpot";
	private static final String POINT = "Point";
	private static final String LIST_STYLE = "ListStyle";
	private static final String LIST_ITEM_TYPE = "listItemType";
	
	private static final String NAMESPACE_XMLNS = "http://www.w3.org/2000/xmlns/";
	private static final String NAMESPACE_XSI = "xmlns:xsi";
	private static final String NAMESPACE_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String NAMESPACE_DEFAULT = "xmlns";
	private static final String NAMESPACE_KML = "http://www.opengis.net/kml/2.2";
	private static final String NAMESPACE_XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
	private static final String NAMESPACE_KML_SCHEMA_LOCATION = "http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd";
	
	private boolean writeActivities;
	private boolean writeCourses;
	
	private static Logger logger = Logger.getLogger(KMLFileWriter.class.getName());

	public KMLFileWriter() {
		this(new HashMap<String, Object>());
	}
	
	public KMLFileWriter(Map<String, Object> options) {
		super(options);
		setUp();
	}

	private void setUp() {
		getOptions().put(Constants.KML.ANIMATION_INFO, Boolean.FALSE);
		getOptions().put(Constants.KML.LAPS_NAME, "Laps");
		getOptions().put(Constants.KML.LAPS_DESCRIPTION, "Laps, start and end splits recorded in the activity.");
		getOptions().put(Constants.KML.LAP_NAME, "Lap");
		getOptions().put(Constants.KML.TRACK_NAME, "Track");
		getOptions().put(Constants.KML.TRACK_COLOR, "FF0000FF");
		getOptions().put(Constants.KML.TRACK_WIDTH, new Float(3.0));
		getOptions().put(Constants.KML.TRACK_EXTRUDE, Boolean.FALSE);
		getOptions().put(Constants.KML.TRACK_TESSELLATE, Boolean.TRUE);
		getOptions().put(Constants.KML.TRACK_ALTITUDE_MODE, "clampToGround");
		getOptions().put(Constants.KML.COURSE_POINTS_NAME, "Course Points");
		getOptions().put(Constants.KML.COURSE_POINTS_DESCRIPTION, "Course points marked on the course.");
		
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
		writeDocument(document, filename);
	}
	
	public void writeDocument(GPSDocument gpsDocument, String filename) throws WriterException {
		try {
			Document dom = getDOMTreeFromGPSDocument(gpsDocument);
			serializeXML(dom, filename);
		} catch (KMLWriterException we) {
			throw new WriterException(we);
		}
	}
	
	private void serializeXML(Document document, String filename) throws KMLWriterException {
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
		    throw new KMLWriterException(te.getMessage());
		} catch (FileNotFoundException fnfe) {
			logger.error(fnfe.getMessage());
			throw new KMLWriterException(fnfe.getMessage());
		}
	}
	
	private Document getDOMTreeFromGPSDocument(GPSDocument gpsDocument) throws KMLWriterException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.newDocument();
			dom.setXmlStandalone(true);
			
			Element kmlElement = createKMLElement(dom, gpsDocument);
			
			if (writeActivities) {
				for (Activity activity : gpsDocument.getActivities()) {
					Element folderElement = createFolderElement(dom, activity);
					kmlElement.appendChild(folderElement);
				}
			}
			
			if (writeCourses) {
				for (Course course : gpsDocument.getCourses()) {
					Element courseElement = createFolderElement(dom, course);
					kmlElement.appendChild(courseElement);
				}
			}
			
			return dom;
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
			throw new KMLWriterException(pce.getMessage());
		}
	}

	private Element createKMLElement(Document document, GPSDocument gpsDocument) {
		Element kmlElement = document.createElement(KML);
		document.appendChild(kmlElement);
		
		kmlElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_XSI, NAMESPACE_XML_SCHEMA_INSTANCE);
		kmlElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_DEFAULT, NAMESPACE_KML);
		
		String schemaLocations = NAMESPACE_KML + " " + NAMESPACE_KML_SCHEMA_LOCATION;
		kmlElement.setAttribute(NAMESPACE_XSI_SCHEMA_LOCATION, schemaLocations);
		
		kmlElement.normalize();
		
		return kmlElement;
	}
	
	private Element createFolderElement(Document dom, Activity activity) {
		Element folderElement = createEmptyElement(dom, FOLDER);
		
		folderElement.appendChild(createTextElement(dom, NAME, activity.getName()));
		
		for (Track track : activity.getTracks()) {
			folderElement.appendChild(createPlacemarkElement(dom, track));
		}
		
		folderElement.appendChild(createFolderElement(dom, activity.getLaps()));
		
		boolean animation = (Boolean) getOptions().get(Constants.KML.ANIMATION_INFO);
		if (animation) {
			folderElement.appendChild(createFolderAnimationElement(dom, activity.getTrackpoints()));
		}
		
		return folderElement;
	}
	
	private Element createFolderElement(Document dom, Course course) {
		Element folderElement = createEmptyElement(dom, FOLDER);
		
		folderElement.appendChild(createTextElement(dom, NAME, course.getName()));
		
		for (Track track : course.getTracks()) {
			folderElement.appendChild(createPlacemarkElement(dom, track));
		}
		
		boolean extendedInfo = (Boolean) getOptions().get(Constants.Writer.WRITE_COURSE_EXTENDED_INFO);
		if (extendedInfo) {
			folderElement.appendChild(createFolderElement(dom, course.getLaps()));
			folderElement.appendChild(createCoursePointsFolderElement(dom, course.getCoursePoints()));
		}
		
		boolean animation = (Boolean) getOptions().get(Constants.KML.ANIMATION_INFO);
		if (animation) {
			folderElement.appendChild(createFolderAnimationElement(dom, course.getTrackpoints()));
		}
		
		return folderElement;
	}
	
	private Element createPlacemarkElement(Document dom, Track track) {
		Element placemarkElement = createEmptyElement(dom, PLACEMARK);
		
		String trackName = (String) getOptions().get(Constants.KML.TRACK_NAME);
		placemarkElement.appendChild(createTextElement(dom, NAME, trackName + " " + track.getId()));
		
		Element styleElement = createEmptyElement(dom, STYLE);
		Element lineStyleElement = createEmptyElement(dom, LINE_STYLE);
		styleElement.appendChild(lineStyleElement);
		
		String trackColor = (String) getOptions().get(Constants.KML.TRACK_COLOR);
		Element colorElement = createTextElement(dom, COLOR, trackColor);
		lineStyleElement.appendChild(colorElement);
		
		String trackWidth = ((Float) getOptions().get(Constants.KML.TRACK_WIDTH)).toString();
		Element widthElement = createTextElement(dom, WIDTH, trackWidth);
		lineStyleElement.appendChild(widthElement);
		
		Element lineStringElement = createEmptyElement(dom, LINE_STRING);
		
		String trackExtrude = ((Boolean) getOptions().get(Constants.KML.TRACK_EXTRUDE)).toString();
		Element extrudeElement = createTextElement(dom, EXTRUDE, trackExtrude);
		lineStringElement.appendChild(extrudeElement);
		
		String trackTessellate = ((Boolean) getOptions().get(Constants.KML.TRACK_TESSELLATE)).toString();
		Element tessellateElement = createTextElement(dom, TESSELLATE, trackTessellate);
		lineStringElement.appendChild(tessellateElement);
		
		String trackAltitudeMode = (String) getOptions().get(Constants.KML.TRACK_ALTITUDE_MODE);
		Element altitudeModeElement = createTextElement(dom, ALTITUDE_MODE, trackAltitudeMode);
		lineStringElement.appendChild(altitudeModeElement);
		
		placemarkElement.appendChild(styleElement);
		
		StringBuffer coordinatesText = new StringBuffer();
		for (Trackpoint trackpoint : track.getTrackpoints()) {
			if (trackpoint.getLatitude() == null || trackpoint.getLongitude() == null) {
				continue;
			}
			
			coordinatesText.append(Formatters.getDecimalFormat().format(trackpoint.getLongitude())).append(",");
			coordinatesText.append(Formatters.getDecimalFormat().format(trackpoint.getLatitude()));
			
			if (trackpoint.getAltitude() != null) {
				coordinatesText.append(",").append(Formatters.getDecimalFormat().format(trackpoint.getAltitude()));
			}
			coordinatesText.append(" ");
		}
		
		Element coordinatesElement = createTextElement(dom, COORDINATES, coordinatesText.toString());
		lineStringElement.appendChild(coordinatesElement);
		placemarkElement.appendChild(lineStringElement);
		
		return placemarkElement;
	}
	
	private Element createFolderElement(Document dom, List<Lap> laps) {
		Element folderElement = createEmptyElement(dom, FOLDER);
		
		String lapsName = (String) getOptions().get(Constants.KML.LAPS_NAME);
		Element nameElement = createTextElement(dom, NAME, lapsName);
		folderElement.appendChild(nameElement);
		
		String lapsDescription = (String) getOptions().get(Constants.KML.LAPS_DESCRIPTION);
		Element descriptionElement = createTextElement(dom, DESCRIPTION, lapsDescription);
		folderElement.appendChild(descriptionElement);
		
		// Start point
		Element startElement = createEmptyElement(dom, PLACEMARK);
		folderElement.appendChild(startElement);
		
		nameElement = createTextElement(dom, NAME, "Start");
		startElement.appendChild(nameElement);
		
		Element styleElement = createEmptyElement(dom, STYLE);
		startElement.appendChild(styleElement);
		
		Element iconStyleElement = createEmptyElement(dom, ICON_STYLE);
		styleElement.appendChild(iconStyleElement);
		Element scaleElement = createTextElement(dom, SCALE, "1.3");
		iconStyleElement.appendChild(scaleElement);
		Element iconElement = createEmptyElement(dom, ICON);
		Element iconHrefElement = createTextElement(dom, HREF, "http://connect.garmin.com/image/main/icons/kml/grn-play.png");
		iconElement.appendChild(iconHrefElement);
		iconStyleElement.appendChild(iconElement);
		Element hotSpotElement = createEmptyElement(dom, HOT_SPOT);
		hotSpotElement.setAttribute(YUNITS, "fraction");
		hotSpotElement.setAttribute(Y, "0.0");
		hotSpotElement.setAttribute(XUNITS, "fraction");
		hotSpotElement.setAttribute(X, "0.5");
		iconStyleElement.appendChild(hotSpotElement);
		
		Element pointElement = createPointElement(dom, laps.get(0).getTracks().get(0).getTrackpoints().get(0));
		startElement.appendChild(pointElement);
		
		// Lap points
		for (Lap lap : laps) {
			Element lapElement = createEmptyElement(dom, PLACEMARK);
			folderElement.appendChild(lapElement);
			
			String lapName = (String) getOptions().get(Constants.KML.LAP_NAME);
			nameElement = createTextElement(dom, NAME, lapName + " " + lap.getId());
			lapElement.appendChild(nameElement);
			
			styleElement = createEmptyElement(dom, STYLE);
			lapElement.appendChild(styleElement);
			
			iconStyleElement = createEmptyElement(dom, ICON_STYLE);
			styleElement.appendChild(iconStyleElement);
			scaleElement = createTextElement(dom, SCALE, "1.0");
			iconStyleElement.appendChild(scaleElement);
			iconElement = createEmptyElement(dom, ICON);
			iconHrefElement = createTextElement(dom, HREF, "http://maps.google.com/mapfiles/kml/shapes/flag.png");
			iconElement.appendChild(iconHrefElement);
			iconStyleElement.appendChild(iconElement);
			hotSpotElement = createEmptyElement(dom, HOT_SPOT);
			hotSpotElement.setAttribute(YUNITS, "fraction");
			hotSpotElement.setAttribute(Y, "0.0");
			hotSpotElement.setAttribute(XUNITS, "fraction");
			hotSpotElement.setAttribute(X, "0.45");
			iconStyleElement.appendChild(hotSpotElement);
			
			Trackpoint lastTrackpoint = lap.getLastTrackpoint();
			pointElement = createPointElement(dom, lastTrackpoint);
			lapElement.appendChild(pointElement);
		}

		// End point
		Element endElement = createEmptyElement(dom, PLACEMARK);
		folderElement.appendChild(endElement);
		
		nameElement = createTextElement(dom, NAME, "End");
		endElement.appendChild(nameElement);
		
		styleElement = createEmptyElement(dom, STYLE);
		endElement.appendChild(styleElement);
		
		iconStyleElement = createEmptyElement(dom, ICON_STYLE);
		styleElement.appendChild(iconStyleElement);
		scaleElement = createTextElement(dom, SCALE, "1.3");
		iconStyleElement.appendChild(scaleElement);
		iconElement = createEmptyElement(dom, ICON);
		iconHrefElement = createTextElement(dom, HREF, "http://maps.google.com/mapfiles/kml/paddle/red-square.png");
		iconElement.appendChild(iconHrefElement);
		iconStyleElement.appendChild(iconElement);
		hotSpotElement = createEmptyElement(dom, HOT_SPOT);
		hotSpotElement.setAttribute(YUNITS, "fraction");
		hotSpotElement.setAttribute(Y, "0.0");
		hotSpotElement.setAttribute(XUNITS, "fraction");
		hotSpotElement.setAttribute(X, "0.5");
		iconStyleElement.appendChild(hotSpotElement);
		
		Lap lastLap = laps.get(laps.size() - 1);
		Trackpoint lastTrackpoint = lastLap.getLastTrackpoint();
		pointElement = createPointElement(dom, lastTrackpoint);
		endElement.appendChild(pointElement);
		
		return folderElement;
	}
	
	private Element createCoursePointsFolderElement(Document dom, List<CoursePoint> coursePoints) {
		Element folderElement = createEmptyElement(dom, FOLDER);
		
		String coursePointsName = (String) getOptions().get(Constants.KML.COURSE_POINTS_NAME);
		Element nameElement = createTextElement(dom, NAME, coursePointsName);
		folderElement.appendChild(nameElement);
		
		String coursePointsDescription = (String) getOptions().get(Constants.KML.COURSE_POINTS_DESCRIPTION);
		Element descriptionElement = createTextElement(dom, DESCRIPTION, coursePointsDescription);
		folderElement.appendChild(descriptionElement);
		
		// Course points
		for (CoursePoint coursePoint : coursePoints) {
			Element coursePointElement = createEmptyElement(dom, PLACEMARK);
			folderElement.appendChild(coursePointElement);
			
			nameElement = createTextElement(dom, NAME, coursePoint.getName());
			coursePointElement.appendChild(nameElement);
			
			Element styleElement = createEmptyElement(dom, STYLE);
			coursePointElement.appendChild(styleElement);
			
			Element iconStyleElement = createEmptyElement(dom, ICON_STYLE);
			styleElement.appendChild(iconStyleElement);
			Element scaleElement = createTextElement(dom, SCALE, "1.0");
			iconStyleElement.appendChild(scaleElement);
			Element iconElement = createEmptyElement(dom, ICON);
			Element iconHrefElement = createTextElement(dom, HREF, "http://maps.google.com/mapfiles/kml/shapes/flag.png");
			iconElement.appendChild(iconHrefElement);
			iconStyleElement.appendChild(iconElement);
			Element hotSpotElement = createEmptyElement(dom, HOT_SPOT);
			hotSpotElement.setAttribute(YUNITS, "fraction");
			hotSpotElement.setAttribute(Y, "0.0");
			hotSpotElement.setAttribute(XUNITS, "fraction");
			hotSpotElement.setAttribute(X, "0.45");
			iconStyleElement.appendChild(hotSpotElement);
			
			Element pointElement = createPointElement(dom, coursePoint.getTrackpoint());
			coursePointElement.appendChild(pointElement);
		}

		return folderElement;
	}
	
	private Element createFolderAnimationElement(Document dom, List<Trackpoint> trackpoints) {
		Element folderElement = createEmptyElement(dom, FOLDER);
		
		Element nameElement = createTextElement(dom, NAME, "Track Points");
		folderElement.appendChild(nameElement);
		
		Element descriptionElement = createTextElement(dom, DESCRIPTION, "Toggle to enable or disable animation.");
		folderElement.appendChild(descriptionElement);
		
		Element styleElement = createEmptyElement(dom, STYLE);
		folderElement.appendChild(styleElement);
		Element listStyleElement = createEmptyElement(dom, LIST_STYLE);
		styleElement.appendChild(listStyleElement);
		
		Element listItemTypeElement = createTextElement(dom, LIST_ITEM_TYPE, "checkHideChildren");
		listStyleElement.appendChild(listItemTypeElement);
		
		Element placemarkElement = createEmptyElement(dom, PLACEMARK);

		Element trackElement = createEmptyElement(dom, TRACK);
		placemarkElement.appendChild(trackElement);
		
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLatitude() == null || trackpoint.getLongitude() == null) {
				continue;
			}
			
			Element whenElement = createTextElement(dom, WHEN, Formatters.getSimpleDateFormat().format(trackpoint.getTimestamp()));
			trackElement.appendChild(whenElement);
		}
		
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLatitude() == null || trackpoint.getLongitude() == null) {
				continue;
			}
			
			Element coordElement = createCoordElement(dom, trackpoint);
			trackElement.appendChild(coordElement);
		}
			
		styleElement = createEmptyElement(dom, STYLE);
		placemarkElement.appendChild(styleElement);
		
		Element iconStyleElement = createEmptyElement(dom, ICON_STYLE);
		styleElement.appendChild(iconStyleElement);
		Element colorElement = createTextElement(dom, COLOR, "FF00FFFF");
		iconStyleElement.appendChild(colorElement);
		Element scaleElement = createTextElement(dom, SCALE, "0.9");
		iconStyleElement.appendChild(scaleElement);
		Element iconElement = createEmptyElement(dom, ICON);
		Element iconHrefElement = createTextElement(dom, HREF, "http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png");
		iconElement.appendChild(iconHrefElement);
		iconStyleElement.appendChild(iconElement);
			
		folderElement.appendChild(placemarkElement);
		
		return folderElement;
	}
		
	private Element createPointElement(Document dom, Trackpoint trackpoint) {
		Element pointElement = createEmptyElement(dom, POINT);
		
		StringBuffer coordinatesSB = new StringBuffer();
		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getLongitude())).append(",");
		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getLatitude())).append(",");
		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getAltitude()));
		Element coordinatesElement = createTextElement(dom, COORDINATES, coordinatesSB.toString());
		pointElement.appendChild(coordinatesElement);
		
		return pointElement;
	}

	private Element createCoordElement(Document dom, Trackpoint trackpoint) {
		StringBuffer coordinatesSB = new StringBuffer();
		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getLongitude())).append(" ");
		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getLatitude())).append(" ");
		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getAltitude()));

		Element coordElement = createTextElement(dom, COORD, coordinatesSB.toString());
		return coordElement;
	}
}
