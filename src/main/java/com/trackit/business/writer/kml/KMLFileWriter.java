/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
 * Copyright (C) 2018 João Brisson
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
package com.trackit.business.writer.kml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.Doc;
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

import com.trackit.business.common.Constants;
import com.trackit.business.common.Formatters;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Track;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.Waypoint;
import com.trackit.business.exception.WriterException;
import com.trackit.business.writer.Writer;
import com.trackit.business.writer.WriterTemplate;

public class KMLFileWriter extends WriterTemplate implements Writer {
	private static final String KML = "kml";
	private static final String DOCUMENT = "Document";				//2018-04-16: 12335
	private static final String FOLDER = "Folder";
	
	// Schema and profiles support (12335: 2018-08-05)
	private static final String SCHEMA 			   = "Schema";
	private static final String ID	 			   = "id";
	private static final String SIMPLE_ARRAY_FIELD = "gx:SimpleArrayField";
	private static final String TYPE			   = "type";
	private static final String DISPLAY_NAME       = "displayName";
	private static final String EXTENDED_DATA      = "ExtendedData";
	private static final String SCHEMA_DATA        = "SchemaData";
	private static final String SCHEMA_URL         = "schemaUrl";
	private static final String SIMPLE_ARRAY_DATA  = "gx:SimpleArrayData";
	private static final String VALUE			   = "gx:value";
	
	private static final String NAME = "name";
	private static final String PLACEMARK = "Placemark";
	private static final String STYLE = "Style";
	private static final String LINE_STYLE = "LineStyle";
	private static final String COLOR = "color";
	private static final String COLOR_MODE = "colorMode";			//12335: 2018-08-21
	private static final String WIDTH = "width";
	private static final String STYLE_MAP = "StyleMap";				//12335: 2018-08-19
	private static final String PAIR = "Pair";						//12335: 2018-08-19
	private static final String KEY = "key";						//12335: 2018-08-19
	private static final String STYLE_URL = "styleUrl";				//12335: 2018-08-19
	private static final String LINE_STRING = "LineString";
	private static final String EXTRUDE = "extrude";
	private static final String TESSELLATE = "tessellate";
	private static final String ALTITUDE_MODE = "altitudeMode";
	private static final String COORDINATES = "coordinates";
//	private static final String TRACK = "Track";
	private static final String TRACK = "gx:Track";
	private static final String MULTI_TRACK = "gx:MultiTrack";
	private static final String WHEN = "when";
//	private static final String COORD = "coord";
	private static final String COORD = "gx:coord";
	
	
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
	
	// Style names (12335: 2018-08-20)
	private static final String SCHEMA_NAME = KMLExtendedDataType.getSchemaID().toLowerCase();
	private static final String WAYPOINT_STYLE 		= SCHEMA_NAME + "waypoint";
	private static final String START_STYLE 		= SCHEMA_NAME + "start";
	private static final String END_STYLE 			= SCHEMA_NAME + "end";
	private static final String HIDE_CHILDREN_STYLE = SCHEMA_NAME + "hide-children";
	private static final String NORMAL_STYLE 		= SCHEMA_NAME + "normal";
	private static final String HIGHLIGHT_STYLE 	= SCHEMA_NAME + "highlight";
	private static final String STYLE_MAP_STYLE 	= SCHEMA_NAME + "styleMap";
	private static final String COLOUR_GREEN		= "ff00ff00";
	private static final String START_MARKS[] = {
			SCHEMA_NAME + "1grn", SCHEMA_NAME + "2grn", SCHEMA_NAME + "3grn", SCHEMA_NAME + "4grn",
			SCHEMA_NAME + "5grn", SCHEMA_NAME + "6grn", SCHEMA_NAME + "7grn", SCHEMA_NAME + "8grn",
			SCHEMA_NAME + "9grn", SCHEMA_NAME + "10grn"
	};
	private static final String[] END_MARKS = {
		SCHEMA_NAME + "1", SCHEMA_NAME + "2", SCHEMA_NAME + "3", SCHEMA_NAME + "4", SCHEMA_NAME + "5",
		SCHEMA_NAME + "6", SCHEMA_NAME + "7", SCHEMA_NAME + "8", SCHEMA_NAME + "9", SCHEMA_NAME + "10"
	};
	
	// KML icons locations (12335: 2018-08-20)
	private static final String KML_SHAPES 	= "http://maps.google.com/mapfiles/kml/shapes/";
	private static final String KML_PUSHPIN = "http://maps.google.com/mapfiles/kml/pushpin/";
	private static final String KML_PADDLE 	= "http://maps.google.com/mapfiles/kml/paddle/";
	
	private static final String NAMESPACE_XMLNS = "http://www.w3.org/2000/xmlns/";
	private static final String NAMESPACE_XSI = "xmlns:xsi";
	private static final String NAMESPACE_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String NAMESPACE_DEFAULT = "xmlns";
	private static final String NAMESPACE_EXTENSION = "xmlns:gx";					// 12335: 2018-07-25
	private static final String NAMESPACE_KML = "http://www.opengis.net/kml/2.2";
	private static final String NAMESPACE_KML_EXTENSION = "http://www.google.com/kml/ext/2.2";	// 12335: 2018-07-25
	private static final String NAMESPACE_XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
	private static final String NAMESPACE_KML_SCHEMA_LOCATION = "http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd";
	
	private boolean writeActivities;
	private boolean writeCourses;
	private boolean writeWaypoints;						//2018-04-16: 12335
	
	private static Logger logger = Logger.getLogger(KMLFileWriter.class.getName());

	public KMLFileWriter() {
		this(new HashMap<String, Object>());
	}
	
	public KMLFileWriter(Map<String, Object> options) {
		super(options);
		setUp();
	}

	private void setUp() {
//		getOptions().put(Constants.KML.ANIMATION_INFO, Boolean.FALSE);
		getOptions().put(Constants.KML.ANIMATION_INFO, Boolean.TRUE);
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
		writeWaypoints = (Boolean) getOptions().get( Constants.Writer.WRITE_WAYPOINTS);
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
		if ( gpsDocument.countActivitiesAndCourses() > 0 )
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
			
			
			kmlElement.appendChild( createTextElement( dom, NAME, gpsDocument.getName())); 	//12335: 2018-08-19
			kmlElement.appendChild( createSchemaElement( dom));								//12335: 2018-08-05
			createTrackMarksStyles( dom, kmlElement); 										//12335: 2018-08-19
			
			//2018-04-16: 12335 - write waypoints
			if ( writeWaypoints ) {
				for( Waypoint waypoint: gpsDocument.getWaypoints() )
					kmlElement.appendChild(  createWaypointIconPlacemark( dom, waypoint.getName(), waypoint));
			}
			
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

// 12335: 2018-07-25
//		kmlElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_XSI, NAMESPACE_XML_SCHEMA_INSTANCE);
		kmlElement.setAttributeNS(NAMESPACE_XMLNS, NAMESPACE_DEFAULT, NAMESPACE_KML);

// 12335: 2018-07-25
//		String schemaLocations = NAMESPACE_KML + " " + NAMESPACE_KML_SCHEMA_LOCATION;
//		kmlElement.setAttribute(NAMESPACE_XSI_SCHEMA_LOCATION, schemaLocations);
		
		// 12335: 2018-07-25
		kmlElement.setAttributeNS( NAMESPACE_XMLNS, NAMESPACE_EXTENSION, NAMESPACE_KML_EXTENSION);
		
		kmlElement.normalize();
//2018-04-16: 12335 - create a KML Document node to be able to hold more than one Folder
		Element kmlDocument = document.createElement( DOCUMENT);
		kmlElement.appendChild( kmlDocument);
		
//		return kmlElement;
		return kmlDocument;
	}
	
	// 12335: 2018-08-05
	private Element createSchemaElement( Document dom) {
		Element schemaElement = createEmptyElement( dom, SCHEMA);
		
		schemaElement.setAttribute( ID, KMLExtendedDataType.getSchemaID());
		for( KMLExtendedDataType kmlExtendedDataType: KMLExtendedDataType.values()) {
			Element simpleArrayField = createEmptyElement( dom, SIMPLE_ARRAY_FIELD);
			simpleArrayField.setAttribute( NAME, kmlExtendedDataType.getDataTypeID());
			simpleArrayField.setAttribute( TYPE, kmlExtendedDataType.getDataType());
			simpleArrayField.appendChild( createTextElement( dom, DISPLAY_NAME, kmlExtendedDataType.getDataLabel()));
			schemaElement.appendChild( simpleArrayField);
		}
		
		return schemaElement;
	}
	
	//12335: 2018-08-19
	private void createTrackMarksStyles( Document dom, Element kmlElement) {
		
		kmlElement.appendChild( createStyleMarkElement( dom, WAYPOINT_STYLE, KML_PUSHPIN + "red-pushpin.png", null, 1.));
		kmlElement.appendChild( createStyleMarkElement( dom, START_STYLE, 	 KML_PADDLE  + "go.png", null, 1.));
		kmlElement.appendChild( createStyleMarkElement( dom, END_STYLE, 	 KML_PADDLE  + "stop.png", null, 1.));
		for( int i=1; i<11; i++) {
			String number = String.format( "%d", i);
			kmlElement.appendChild( createStyleMarkElement( dom, END_MARKS [i-1], 
														KML_PADDLE + number + ".png", null, 1.));
			kmlElement.appendChild( createStyleMarkElement( dom, START_MARKS [i-1], 
				     									KML_PADDLE + number + ".png", COLOUR_GREEN, 1.));
		}
		
		Element checkHideChildren = createEmptyElement( dom, STYLE);
		checkHideChildren.setAttribute( ID, HIDE_CHILDREN_STYLE);
		Element listStyle = createEmptyElement( dom, LIST_STYLE);
		listStyle.appendChild( createTextElement( dom, LIST_ITEM_TYPE, "checkHideChildren"));
		checkHideChildren.appendChild( listStyle);
		kmlElement.appendChild( checkHideChildren);
		
		Element normal    = createStyleMarkElement( dom, NORMAL_STYLE, KML_SHAPES + "track.png", null, 1.);
		Element lineStyle = createEmptyElement( dom, LINE_STYLE);
		lineStyle.appendChild( createTextElement( dom, COLOR, "88ff0000"));
		lineStyle.appendChild( createTextElement( dom, WIDTH, "6"));
		normal.appendChild( lineStyle);
		kmlElement.appendChild( normal);
		
		Element highlight = createStyleMarkElement( dom, HIGHLIGHT_STYLE, KML_SHAPES + "track.png", null, 1.25);
		lineStyle = createEmptyElement( dom, LINE_STYLE);
		lineStyle.appendChild( createTextElement( dom, COLOR, "ff0000ff"));
		lineStyle.appendChild( createTextElement( dom, WIDTH, "8"));
		highlight.appendChild( lineStyle);
		kmlElement.appendChild( highlight);
		
		Element styleMap = createEmptyElement( dom, STYLE_MAP);
		styleMap.setAttribute( ID, STYLE_MAP_STYLE);
		Element pair = createEmptyElement( dom, PAIR);
		pair.appendChild( createTextElement( dom, KEY, "normal"));
		pair.appendChild( createTextElement( dom, STYLE_URL, "#" + NORMAL_STYLE));
		styleMap.appendChild( pair);
		pair = createEmptyElement( dom, PAIR);
		pair.appendChild( createTextElement( dom, KEY, "highlight"));
		pair.appendChild( createTextElement( dom, STYLE_URL, "#" + HIGHLIGHT_STYLE));
		styleMap.appendChild( pair);
		kmlElement.appendChild( styleMap);
	}
	
	//12335: 2018-08-19
	private Element createStyleMarkElement( Document dom, String name, String uri, String colour, double scale) {
		Element style = createEmptyElement( dom, STYLE);
		style.setAttribute( ID, name);
		Element iconStyle = createEmptyElement( dom, ICON_STYLE);
		if ( scale != 1.) {
			iconStyle.appendChild( createTextElement( dom, SCALE, 
											Formatters.getDefaultDecimalFormat().format( scale)));
		}
		Element icon = createEmptyElement( dom, ICON);
		Element href = createTextElement( dom, HREF, uri);
		if ( colour != null ) {
			iconStyle.appendChild( createTextElement( dom, COLOR, colour));
			iconStyle.appendChild( createTextElement( dom, COLOR_MODE, "normal"));
		}
		icon.appendChild( href);
		iconStyle.appendChild( icon);
		style.appendChild( iconStyle);
		return style;
	}
	
	private Element createFolderElement(Document dom, Activity activity) {
		Element folderElement = createEmptyElement(dom, FOLDER);
		
		folderElement.appendChild( createTextElement(dom, NAME, activity.getName()));
		Element activityTrack = createPlacemarkElement( dom, activity.getLaps());
		activityTrack.appendChild( createTextElement( dom, NAME, activity.getName()));
		folderElement.appendChild( activityTrack);
		folderElement.appendChild( createTrackMarksFolderElement( dom, activity.getLaps()));
//		
//		for (Track track : activity.getTracks()) {
//			folderElement.appendChild(createPlacemarkElement(dom, track));
//		}
//		
//		folderElement.appendChild(createFolderElement(dom, activity.getLaps()));
		
//		boolean animation = (Boolean) getOptions().get(Constants.KML.ANIMATION_INFO);
//		if (animation) {
//			folderElement.appendChild(createFolderAnimationElement(dom, activity.getTrackpoints()));
//		}
		
		return folderElement;
	}
	
	private Element createFolderElement(Document dom, Course course) {
		Element folderElement = createEmptyElement(dom, FOLDER);
		
		folderElement.appendChild(createTextElement(dom, NAME, course.getName()));
		Element courseTrack = createPlacemarkElement( dom, course.getLaps());
		courseTrack.appendChild( createTextElement(dom, NAME, course.getName()));
		folderElement.appendChild( courseTrack);
		folderElement.appendChild( createTrackMarksFolderElement( dom, course.getLaps()));

// 2018-04-16: 12335
//		for (Track track : course.getTracks()) {
//			folderElement.appendChild(createPlacemarkElement(dom, track));
//		}
//		if ( course.getLaps().size() == 1 ) {
////			folderElement.appendChild( createLineStringPlacemark( dom, course.getName(), course.getTrackpoints()));
//			folderElement.appendChild( createStartEndIconPlacemark( dom, course.getLaps().get(0).getTrackpoints(), 1, 1));
//			folderElement.appendChild( createStopEndIconPlacemark( dom, course.getLaps().get(0).getTrackpoints(), 1, 1));
//		} else {
//			int lapNo = 0;
//			int noLaps = course.getLaps().size();
//			for( Lap lap: course.getLaps() ) {
//				Element lapFolder = createEmptyElement( dom, FOLDER);
//				folderElement.appendChild( lapFolder);
//				
//				String name = String.format( "Lap %d", ++lapNo);
//				lapFolder.appendChild( createTextElement( dom, NAME, name));
//				
//				lapFolder.appendChild( createLineStringPlacemark( dom, name, lap.getTrackpoints()));
//				
//				lapFolder.appendChild( createStartEndIconPlacemark( dom, course.getLaps().get(lapNo-1).getTrackpoints(), lapNo, noLaps));
//				lapFolder.appendChild( createStopEndIconPlacemark( dom, course.getLaps().get(lapNo-1).getTrackpoints(), lapNo, noLaps));
//			}
//		}
		
//		folderElement.appendChild( createFolderElement( dom, course.getLaps()));
		
//		boolean extendedInfo = (Boolean) getOptions().get(Constants.Writer.WRITE_COURSE_EXTENDED_INFO);
//		if (extendedInfo) {
//			folderElement.appendChild(createFolderElement(dom, course.getLaps()));
//			folderElement.appendChild(createCoursePointsFolderElement(dom, course.getCoursePoints()));
//		}
		
//		boolean animation = (Boolean) getOptions().get(Constants.KML.ANIMATION_INFO);
//		if (animation) {
//			folderElement.appendChild(createFolderAnimationElement(dom, course.getTrackpoints()));
//		}
		
		return folderElement;
	}
	
	private Element createPlacemarkElement(Document dom, Track track) {
		System.out.println( track.getDocumentItemName());
		Element placemarkElement = createEmptyElement(dom, PLACEMARK);
		
//		String trackName = (String) getOptions().get(Constants.KML.TRACK_NAME);
//		placemarkElement.appendChild(createTextElement(dom, NAME, trackName + " " + track.getId()));
		DocumentItem parent = track.getParent();
		String trackName = parent.getDocumentItemName();
		if ( parent instanceof Activity )
			trackName = ((Activity) parent).getName();
		else if ( parent instanceof Course )
			trackName = ((Course) parent).getName();
		placemarkElement.appendChild(createTextElement(dom, NAME, trackName));
		
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
		for (Trackpoint trackpoint : track.getParent().getTrackpoints()) {
			if (trackpoint.getLatitude() == null || trackpoint.getLongitude() == null) {
				continue;
			}
			
//12335: 2017-08-08
//			coordinatesText.append(Formatters.getDecimalFormat().format(trackpoint.getLongitude())).append(",");
//			coordinatesText.append(Formatters.getDecimalFormat().format(trackpoint.getLatitude()));
			coordinatesText.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getLongitude())).append(",");
			coordinatesText.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getLatitude()));
			
			if (trackpoint.getAltitude() != null) {
//12335: 2017-08-08
//				coordinatesText.append(",").append(Formatters.getDecimalFormat().format(trackpoint.getAltitude()));
				coordinatesText.append(",").append(Formatters.getDefaultDecimalFormat().format(trackpoint.getAltitude()));
			}
			coordinatesText.append(" ");
		}
		
		Element coordinatesElement = createTextElement(dom, COORDINATES, coordinatesText.toString());
		lineStringElement.appendChild(coordinatesElement);
		placemarkElement.appendChild(lineStringElement);
		
		return placemarkElement;
	}
	
	//12335: 2018-08-20
	private Element createPlacemarkElement( Document dom, List<Lap> laps) {
		Element placemark = createEmptyElement( dom, PLACEMARK);
		placemark.appendChild( createTextElement( dom, STYLE_URL, "#" + STYLE_MAP_STYLE));
		Element trackElement;
		if ( laps.size() == 1 ) {
			trackElement = createTrackElement( dom, laps.get(0).getTrackpoints());
			trackElement.setAttribute( ID, Messages.getMessage( "lap.label") + " 1");
			placemark.appendChild( trackElement);
		}
		else {
			int lapNo = 0;
			Element multiTrack = createEmptyElement( dom, MULTI_TRACK);
			for( Lap lap: laps) {
				lapNo++;
				trackElement = createTrackElement( dom, lap.getTrackpoints());
				trackElement.setAttribute( ID, Messages.getMessage( "lap.label") + String.format( " %d", lapNo));
				multiTrack.appendChild( trackElement);
			}
			placemark.appendChild( multiTrack);
		}
		return placemark;
	}
	
	//12335: 2018-08-20
	private Element createTrackElement( Document dom, List<Trackpoint> trackpoints) {
		Element trackElement  = createEmptyElement( dom, TRACK);
		
		boolean hasSpeed     = false;
		boolean hasPower     = false;
		boolean hasCadence   = false;
		boolean hasHeartRate = false;
		
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLatitude() == null || trackpoint.getLongitude() == null) {
				continue;
			}
			
			Element whenElement = createTextElement(dom, WHEN, 
								Formatters.getSimpleDateFormat().format(trackpoint.getTimestamp()));
			trackElement.appendChild(whenElement);
			
			if ( trackpoint.getSpeed() != null )
				hasSpeed = true;
			if ( trackpoint.getPower() != null )
				hasPower = true;
			if ( trackpoint.getCadence() != null )
				hasCadence = true;
			if ( trackpoint.getHeartRate() != null )
				hasHeartRate = true;
		}
		
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLatitude() == null || trackpoint.getLongitude() == null) {
				continue;
			}
			
			Element coordElement = createCoordElement(dom, trackpoint);
			trackElement.appendChild(coordElement);
		}
		
		Element extendedDataElement = createEmptyElement( dom, EXTENDED_DATA);
		Element schemaData = createEmptyElement( dom, SCHEMA_DATA);
		schemaData.setAttribute( SCHEMA_URL, "#" + KMLExtendedDataType.getSchemaID());
		
		schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.SPEED));
		if ( hasHeartRate )
			schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.HEART_RATE));
		if ( hasPower )
			schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.POWER));
		if ( hasCadence )
			schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.CADENCE));
		schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.GRADE));
		
		extendedDataElement.appendChild( schemaData);
		trackElement.appendChild( extendedDataElement);
					
		return trackElement;
	}
	
	//12335: 2018-08-21
	private Element createTrackMarksFolderElement( Document dom, List<Lap> laps) {
		String name = "";
		if ( laps.size() == 1 )
			name = Messages.getMessage( "kml.marksLabel.startEnd");
		else
			name = Messages.getMessage( "kml.marksLabel.startEndLaps");
		Element marksFolder = createEmptyElement( dom, FOLDER);
		marksFolder.appendChild( createTextElement( dom, NAME, name));
		marksFolder.appendChild( createTextElement( dom, STYLE_URL, "#" + HIDE_CHILDREN_STYLE));
		
		for( int i=0; i<laps.size(); i++) {
			marksFolder.appendChild( createStartEndIconPlacemark( dom, laps.get(i).getTrackpoints(), i, laps.size()));
			marksFolder.appendChild( createStopEndIconPlacemark(  dom, laps.get(i).getTrackpoints(), i, laps.size()));
			}
		
		return marksFolder;
	}
	
	//2018-04-16: 12335
	private Element createLineStringPlacemark( Document dom, String name, List<Trackpoint> trackpoints) {
		Element placemarkElement = createEmptyElement(dom, PLACEMARK);
		
		placemarkElement.appendChild(createTextElement(dom, NAME, name));
		
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
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLatitude() == null || trackpoint.getLongitude() == null) {
				continue;
			}
			
			coordinatesText.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getLongitude())).append(",");
			coordinatesText.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getLatitude()));
			
			if (trackpoint.getAltitude() != null) {
				coordinatesText.append(",").append(Formatters.getDefaultDecimalFormat().format(trackpoint.getAltitude()));
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
		
//		System.out.println( laps.get(0).getParent().getDocumentItemName());
//		System.out.println( laps.get(0).getTracks().get(0));
//		System.out.println( laps.get(0).getTracks().get(0).getTrackpoints().get(0));
//		Element pointElement = createPointElement(dom, laps.get(0).getTracks().get(0).getTrackpoints().get(0));
		Element pointElement = createPointElement(dom, laps.get(0).getTrackpoints().get(0));
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
	
	private Element createStartEndIconPlacemark( Document dom, List<Trackpoint> lapTrackpoints, int lapNo, int noLaps) {
		String url;
//		String name = "Start";
		String name = Messages.getMessage( "kml.startPin.name");
		if ( lapNo == 0 ) {
//			url  = "http://maps.google.com/mapfiles/kml/paddle/go.png";
			url = START_STYLE;
		}
		else {
//			url = "http://maps.google.com/mapfiles/kml/paddle/"+ (lapNo-1)+ ".png";
			url = START_MARKS[ lapNo];
			name += " " + (lapNo+1);
		}
		return createCourseEndIconPlacemark( dom, name, lapTrackpoints.get(0), url);
	}
	
	private Element createStopEndIconPlacemark( Document dom, List<Trackpoint> lapTrackpoints, int lapNo, int noLaps) {
		String url;
//		String name = "End";
		String name = Messages.getMessage( "kml.endPin.name");
		if ( lapNo == (noLaps - 1) ) {
//			url  = "http://maps.google.com/mapfiles/kml/paddle/red-square.png";
			url = END_STYLE;
		}
		else {
//			url = "http://maps.google.com/mapfiles/kml/paddle/"+ lapNo + ".png";
			url = END_MARKS[ lapNo];
			name += " " + (lapNo+1);
		}
		return createCourseEndIconPlacemark( dom, name, lapTrackpoints.get( lapTrackpoints.size()-1), url);
	}
	
	private Element createCourseEndIconPlacemark( Document dom, String name, Trackpoint trackpoint, String iconURL) {
		Element courseEndElement = createEmptyElement(dom, PLACEMARK);

		courseEndElement.appendChild( createTextElement(dom, NAME, name));

		Element styleElement = createEmptyElement(dom, STYLE);
		courseEndElement.appendChild(styleElement);
		
		courseEndElement.appendChild( createTextElement( dom, STYLE_URL, iconURL));
		
//		Element iconStyleElement = createEmptyElement(dom, ICON_STYLE);
//		styleElement.appendChild(iconStyleElement);
//		Element scaleElement = createTextElement(dom, SCALE, "1.0");
//		iconStyleElement.appendChild(scaleElement);
//		Element iconElement = createEmptyElement(dom, ICON);
//		Element iconHrefElement = createTextElement(dom, HREF, iconURL);
//		iconElement.appendChild(iconHrefElement);
//		iconStyleElement.appendChild(iconElement);
//		Element hotSpotElement = createEmptyElement(dom, HOT_SPOT);
//		hotSpotElement.setAttribute(YUNITS, "fraction");
//		hotSpotElement.setAttribute(Y, "0.0");
//		hotSpotElement.setAttribute(XUNITS, "fraction");
//		hotSpotElement.setAttribute(X, "0.45");
//		iconStyleElement.appendChild(hotSpotElement);

		Element pointElement = createPointElement(dom,trackpoint);
		courseEndElement.appendChild(pointElement);

		return courseEndElement;
	}
	
	private Element createWaypointIconPlacemark( Document dom, String name, Waypoint waypoint) {
		Element courseEndElement = createEmptyElement(dom, PLACEMARK);

		courseEndElement.appendChild( createTextElement(dom, NAME, waypoint.getName()));

		Element styleElement = createEmptyElement(dom, STYLE);
		courseEndElement.appendChild(styleElement);
		
		Element iconStyleElement = createEmptyElement(dom, ICON_STYLE);
		styleElement.appendChild(iconStyleElement);
		Element scaleElement = createTextElement(dom, SCALE, "1.0");
		iconStyleElement.appendChild(scaleElement);
		Element iconElement = createEmptyElement(dom, ICON);
		Element iconHrefElement = createTextElement(dom, HREF, "http://maps.google.com/mapfiles/kml/pushpin/red-pushpin.png");
		iconElement.appendChild(iconHrefElement);
		iconStyleElement.appendChild(iconElement);
		Element hotSpotElement = createEmptyElement(dom, HOT_SPOT);
		hotSpotElement.setAttribute(YUNITS, "fraction");
		hotSpotElement.setAttribute(Y, "0.0");
		hotSpotElement.setAttribute(XUNITS, "fraction");
		hotSpotElement.setAttribute(X, "0.45");
		iconStyleElement.appendChild(hotSpotElement);

		Element pointElement = createPointElement(dom, waypoint);
		courseEndElement.appendChild(pointElement);

		return courseEndElement;
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
		
		boolean hasPower     = false;				// 12335: 2018-08-06
		boolean hasCadence   = false;				// 12335: 2018-08-06
		boolean hasHeartRate = false;				// 12335: 2018-08-06
		boolean hasSpeed     = false;				// 12335: 2018-08-14
		
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLatitude() == null || trackpoint.getLongitude() == null) {
				continue;
			}
			
			Element whenElement = createTextElement(dom, WHEN, Formatters.getSimpleDateFormat().format(trackpoint.getTimestamp()));
			trackElement.appendChild(whenElement);
			
			// 12335: 2018-08-06
			if ( trackpoint.getSpeed() != null )
				hasSpeed = true;
			if ( trackpoint.getPower() != null )
				hasPower = true;
			if ( trackpoint.getCadence() != null )
				hasCadence = true;
			if ( trackpoint.getHeartRate() != null )
				hasHeartRate = true;
		}
		
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLatitude() == null || trackpoint.getLongitude() == null) {
				continue;
			}
			
			Element coordElement = createCoordElement(dom, trackpoint);
			trackElement.appendChild(coordElement);
		}
		
		// 12335: 2018-08-06
		Element extendedDataElement = createEmptyElement( dom, EXTENDED_DATA);
		Element schemaData = createEmptyElement( dom, SCHEMA_DATA);
		schemaData.setAttribute( SCHEMA_URL, "#" + KMLExtendedDataType.getSchemaID());
		
		schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.SPEED));
		if ( hasHeartRate )
			schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.HEART_RATE));
		if ( hasPower )
			schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.POWER));
		if ( hasCadence )
			schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.CADENCE));
		schemaData.appendChild( createSimpleExtendedDataArrayElement( dom, trackpoints, KMLExtendedDataType.GRADE));
		
		extendedDataElement.appendChild( schemaData);
		trackElement.appendChild( extendedDataElement);
			
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
//12335: 2017-08-08
//		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getLongitude())).append(",");
//		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getLatitude())).append(",");
//		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getAltitude()));
		coordinatesSB.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getLongitude())).append(",");
		coordinatesSB.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getLatitude())).append(",");
		coordinatesSB.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getAltitude()));
		Element coordinatesElement = createTextElement(dom, COORDINATES, coordinatesSB.toString());
		pointElement.appendChild(coordinatesElement);
		
		return pointElement;
	}

	private Element createPointElement(Document dom, Waypoint waypoint) {
		Element pointElement = createEmptyElement(dom, POINT);
		
		StringBuffer coordinatesSB = new StringBuffer();
		coordinatesSB.append(Formatters.getDefaultDecimalFormat().format(waypoint.getLongitude())).append(",");
		coordinatesSB.append(Formatters.getDefaultDecimalFormat().format(waypoint.getLatitude())).append(",");
		coordinatesSB.append(Formatters.getDefaultDecimalFormat().format(waypoint.getAltitude()));
		Element coordinatesElement = createTextElement(dom, COORDINATES, coordinatesSB.toString());
		pointElement.appendChild(coordinatesElement);
		
		return pointElement;
	}

	private Element createCoordElement(Document dom, Trackpoint trackpoint) {
		StringBuffer coordinatesSB = new StringBuffer();
		//12335: 2017-08-08
//		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getLongitude())).append(" ");
//		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getLatitude())).append(" ");
//		coordinatesSB.append(Formatters.getDecimalFormat().format(trackpoint.getAltitude()));
		coordinatesSB.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getLongitude())).append(" ");
		coordinatesSB.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getLatitude())).append(" ");
		coordinatesSB.append(Formatters.getDefaultDecimalFormat().format(trackpoint.getAltitude()));

		Element coordElement = createTextElement(dom, COORD, coordinatesSB.toString());
		return coordElement;
	}
	
	// 12335: 2018-08-06
	private Element createSimpleExtendedDataArrayElement( Document dom, List<Trackpoint> trackpoints, 
														   KMLExtendedDataType kmlExtendedDataType) {
		Element simpleArrayData = createEmptyElement( dom, SIMPLE_ARRAY_DATA);
		simpleArrayData.setAttribute( NAME, kmlExtendedDataType.getDataTypeID());
		for( Trackpoint trackpoint: trackpoints)
			simpleArrayData.appendChild( 
					createTextElement( dom, VALUE, kmlExtendedDataType.formatValue( trackpoint)));			
		return simpleArrayData;
	}
}
