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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.trackit.TrackIt;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Formatters;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.DeviceInfo;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.Event;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.Track;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.exception.WriterException;

public abstract class WriterTemplate implements Writer {
	protected Map<String, Object> options;
	
	public WriterTemplate() {
		options = new HashMap<String, Object>();
		options.putAll(getDefaultOptions());
	}
	
	public WriterTemplate(Map<String, Object> options) {
		this();
		this.options.putAll(options);
	}
	
	public void visit(DocumentItem documentItem) throws TrackItException {
		throw new UnsupportedOperationException();
	}
	
	public void visit(GPSDocument document)  {
		throw new UnsupportedOperationException();
	}
	
	public void visit(Activity activity) throws WriterException {
		throw new UnsupportedOperationException();
	}
	
	public void visit(Course course) throws WriterException {
		throw new UnsupportedOperationException();
	}

	public void visit(Session session) throws WriterException {
		throw new UnsupportedOperationException();
	}
	
	public void visit(Lap lap) throws WriterException {
		throw new UnsupportedOperationException();
	}

	public void visit(Event event) throws WriterException {
		throw new UnsupportedOperationException();
	}
	
	public void visit(DeviceInfo device) throws WriterException {
		throw new UnsupportedOperationException();
	}

	public void visit(CoursePoint coursePoint) throws WriterException {
		throw new UnsupportedOperationException();
	}

	public void visit(Track track) throws WriterException {
		throw new UnsupportedOperationException();
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
		Text time = document.createTextNode(Formatters.getSimpleDateFormatMilis().format(dateTime));
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

	protected Element createByteElementNS(Document document, String prefix, String name, Byte value) {
		String fullQName = prefix + ":" + name;
		Element byteElement = document.createElement(fullQName);
		
		Text byteValue = document.createTextNode(value.toString());
		byteElement.appendChild(byteValue);
		
		return byteElement;
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
//12335: 2017-08-06
//		Text doubleValue = document.createTextNode(String.valueOf(Formatters.getDecimalFormat().format(value)));
		Text doubleValue = document.createTextNode(String.valueOf(Formatters.getDefaultDecimalFormat().format(value)));
		doubleElement.appendChild(doubleValue);
		
		return doubleElement;
	}
	
	protected Element createShortElement(Document document, String name, Short value) {
		Element shortElement = document.createElement(name);
		Text shortValue = document.createTextNode(String.valueOf(value));
		shortElement.appendChild(shortValue);
		
		return shortElement;
	}
	
	protected Element createIntegerElement(Document document, String name, Integer value) {
		Element integerElement = document.createElement(name);
		Text integerValue = document.createTextNode(String.valueOf(value));
		integerElement.appendChild(integerValue);
		
		return integerElement;
	}
	
	public Map<String, Object> getOptions() {
		return options;
	}
	
	private Map<String, Object> getDefaultOptions() {
		Map<String, Object> options = new HashMap<String, Object>();
		
		boolean writeFolders = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.WRITER, null,
				Constants.Writer.WRITE_FOLDERS, false);
		options.put(Constants.Writer.WRITE_FOLDERS, writeFolders);

		boolean writeActivities = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.WRITER, null,
				Constants.Writer.WRITE_ACTIVITIES, true);
		options.put(Constants.Writer.WRITE_ACTIVITIES, writeActivities);
		
		boolean writeCourses = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.WRITER, null,
				Constants.Writer.WRITE_COURSES, true);
		options.put(Constants.Writer.WRITE_COURSES, writeCourses);
		
		boolean writeWaypoints = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.WRITER, null,
				Constants.Writer.WRITE_WAYPOINTS, true);
		options.put(Constants.Writer.WRITE_WAYPOINTS, writeWaypoints);
		
		boolean writeCourseExtendedInfo = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.WRITER, null,
				Constants.Writer.WRITE_COURSE_EXTENDED_INFO, false);
		options.put(Constants.Writer.WRITE_COURSE_EXTENDED_INFO, writeCourseExtendedInfo);
		
		return options;
	}
}
