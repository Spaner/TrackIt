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
package com.henriquemalheiro.trackit.business.domain;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.IdGenerator;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.Unit;
import com.henriquemalheiro.trackit.business.common.UnitCategory;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.task.ActionType;
import com.henriquemalheiro.trackit.presentation.view.data.DataType;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderTreeItem;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayer;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainter;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterFactory;


public abstract class TrackItBaseType implements DocumentItem, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3719978530673201262L;
	protected static Map<String, Map<String, FieldMetadata>> fieldsMetadata;
	private Map<String, Object> attributes;
	private long id;
	
	protected static Logger logger = Logger.getLogger(TrackItBaseType.class.getName());
	
	public TrackItBaseType() {
		id = IdGenerator.INSTANCE.getNextId();
		attributes = new HashMap<String, Object>();
	}
	
	static {
		final String EMPTY_LINE = "^\\s*$"; 
		fieldsMetadata = new HashMap<String, Map<String, FieldMetadata>>();
		
		try {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(TrackItBaseType.class.getResourceAsStream("Field.def")));
	        String line;
	        FieldMetadata metadata = null;
	        String className = "";
	        String[] properties;
	        
	        while ((line = reader.readLine()) != null) {
	        	if (line.matches(EMPTY_LINE)) {
	        		continue;
	        	}
	        	
	        	if (line.startsWith("[")) {
	        		className = line.substring(1, line.length() - 1);
	        		if (!fieldsMetadata.containsKey(className)) {
	        			fieldsMetadata.put(className, new LinkedHashMap<String, FieldMetadata>());
	        		}
	        	} else {
	        		properties = line.split(",");
	        		
	        		List<FieldGroup> groupsList = new ArrayList<FieldGroup>();
	        		if(!properties[5].isEmpty() && properties[5].equals("course.elapsedTime")){
	        			int i = 1;
	        		}
	        		if (!properties[4].isEmpty()) {
	        			
	        			String[] groups = properties[4].split("\\|");
	        			for (String group : groups) {
							groupsList.add(FieldGroup.lookup(group));
						}
	        		}
	        		
	        		metadata = new FieldMetadata(className + "." + properties[0], properties[1],
	        				UnitCategory.valueOf(properties[2]), Unit.valueOf(properties[3]),
	        				groupsList, properties[5]);
	        		fieldsMetadata.get(metadata.getClassName()).put(metadata.getFieldName(), metadata);
	        	}
	        }
	    } catch (FileNotFoundException e) {
	        logger.error("Error reading Field.def file.");
	        System.exit(-1);
	    } catch (IOException e) {
	    	logger.error("Error reading Field.def file.");
	    	System.exit(-1);
	    }
	}
	
	public static Collection<FieldMetadata> getFieldsMetadata(String className) {
		return fieldsMetadata.get(className).values();
	}

	public static List<String> getFieldNames(String className) {
		List<String> fieldNames = new ArrayList<String>(); 

		Collection<FieldMetadata> metadata = getFieldsMetadata(className);
		for (FieldMetadata fieldMetadata : metadata) {
			fieldNames.add(fieldMetadata.getFieldName());
		}
		
		return fieldNames;
	}

	public static FieldMetadata getMetadata(String className, String fieldName) {
		return fieldsMetadata.get(className).get(fieldName);
	}
	
	public static void setMetadata(String className, String fieldName, FieldMetadata metadata) {
		fieldsMetadata.get(className).put(fieldName, metadata);
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public void setId(long id) {
		this.id = id;
	}
	
	public Object get(String fieldName) {
		final String regex = "^([^\\W_])|_+([^\\W_])|([^\\W_]+)|_+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(fieldName);
		
		StringBuilder methodName = new StringBuilder("get");
		while (matcher.find()) {
			methodName.append(matcher.group(1) != null ? matcher.group(1).toUpperCase() : "");
			methodName.append(matcher.group(2) != null ? matcher.group(2).toUpperCase() : "");
			methodName.append(matcher.group(3) != null ? matcher.group(3).toLowerCase() : "");
		}
		
		try {
			Method method = this.getClass().getMethod(methodName.toString(), new Class<?>[0]);
			if(method.getName().equals("getStartTime")){
				int i = 0;
			}
			return method.invoke(this, new Object[0]);
		} catch (Exception e) {
			logger.trace("Attribute with name " + fieldName + " not found.");
		}
		
		return null;
	}
	
	public void set(String fieldName, Object value) {
		final String regex = "^([^\\W_])|_+([^\\W_])|([^\\W_]+)|_+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(fieldName);
		
		StringBuilder methodName = new StringBuilder("set");
		while (matcher.find()) {
			methodName.append(matcher.group(1) != null ? matcher.group(1).toUpperCase() : "");
			methodName.append(matcher.group(2) != null ? matcher.group(2).toUpperCase() : "");
			methodName.append(matcher.group(3) != null ? matcher.group(3).toLowerCase() : "");
		}
		
		try {
			Class<?> parameterClass = null;
			Collection<FieldMetadata> metadata = getFieldsMetadata(this.getClass().getName());
			for (FieldMetadata fieldMetadata : metadata) {
				if (fieldMetadata.getFieldName().equalsIgnoreCase(fieldName)) {
					parameterClass = Class.forName(fieldMetadata.getType());
					break;
				}
			}
			
			Method method = this.getClass().getMethod(methodName.toString(), new Class<?>[] { parameterClass });
			method.invoke(this, new Object[] { value });
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrackItBaseType other = (TrackItBaseType) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String getDocumentItemName() {
		return this.getClass().getName();
	}
	
	@Override
	public List<ActionType> getSupportedActions() {
		return Collections.emptyList();
	}
	
	@Override
	public com.henriquemalheiro.trackit.business.common.FileType[] getSupportedFileTypes() {
		return new com.henriquemalheiro.trackit.business.common.FileType[0];
	}
	
	@Override
	public boolean isActivity() {
		return false;
	}
	
	@Override
	public boolean isCourse() {
		return false;
	}
	
	@Override
	public List<Trackpoint> getTrackpoints() {
		return new ArrayList<Trackpoint>();
	}
	
	@Override
	public List<Lap> getLaps() {
		return new ArrayList<Lap>();
	}
	
	@Override
	public List<Track> getTracks() {
		return new ArrayList<Track>();
	}
	
	@Override
	public List<Event> getEvents() {
		return new ArrayList<Event>();
	}
	
	@Override
	public List<DeviceInfo> getDevices() {
		return new ArrayList<DeviceInfo>();
	}
	
	@Override
	public List<CoursePoint> getCoursePoints() {
		return new ArrayList<CoursePoint>();
	}
	
	@Override
	public List<TrackSegment> getSegments() {
		return new ArrayList<TrackSegment>();
	}
	
	@Override
	public BoundingBox2<Location> getBounds() {
		return getBounds(getTrackpoints());
	}
	
	private BoundingBox2<Location> getBounds(List<Trackpoint> trackpoints) {
		double[] minMaxCoordinates = getMinMaxCoordinates(trackpoints);
		return createBoundingBox(minMaxCoordinates);
	}
	
	private double[] getMinMaxCoordinates(List<Trackpoint> trackpoints) {
		double minLongitude = 181.0;
		double maxLongitude = -181.0;
		double minLatitude = 91.0;
		double maxLatitude = -91.0;
		
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLongitude() != null && trackpoint.getLatitude() != null) {
				minLongitude = Math.min(minLongitude, trackpoint.getLongitude());
				maxLongitude = Math.max(maxLongitude, trackpoint.getLongitude());
				minLatitude = Math.min(minLatitude, trackpoint.getLatitude());
				maxLatitude = Math.max(maxLatitude, trackpoint.getLatitude());
			}
		}
		return new double[] { minLongitude, minLatitude, maxLongitude, maxLatitude };
	}

	private BoundingBox2<Location> createBoundingBox(double[] minMaxCoordinates) {
		double minLongitude = minMaxCoordinates[0];
		double minLatitude = minMaxCoordinates[1];
		double maxLongitude = minMaxCoordinates[2];
		double maxLatitude = minMaxCoordinates[3];
		
		Location topLeft = new Location(minLongitude, maxLatitude);
		Location topRight = new Location(maxLongitude, maxLatitude);
		Location bottomRight = new Location(maxLongitude, minLatitude);
		Location bottomLeft = new Location(minLongitude, minLatitude);
		
		return new BoundingBox2<Location>(topLeft, topRight, bottomRight, bottomLeft);
	}
	
	@Override
	public DocumentItem getParent() {
		return null;
	}

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}
	
	@Override
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	@Override
	public abstract void accept(Visitor visitor) throws TrackItException;
	
	@Override
	public void consolidate(ConsolidationLevel level) {
	}
	
	@Override
	public void publishHighlightEvent(EventPublisher publisher) {
	}
	
	@Override
	public void publishUpdateEvent(EventPublisher publisher) {
	}
	
	@Override
	public void paint(Graphics2D graphics, MapLayer layer, Map<String, Object> paintingAttributes) {
		MapPainter nullPainter = MapPainterFactory.getInstance().getNullMapPainter();
		nullPainter.paint(graphics, paintingAttributes);
	}
	
	/* FolderTreeItem Interface Implementation */
	
	public boolean acceptItem(FolderTreeItem item) {
		return false;
	}
	
	@Override
	public List<DataType> getDisplayableElements() {
		return Collections.emptyList();
	}
	
	@Override
	public List<? extends DocumentItem> getDisplayedElements(DataType dataType) {
		return Collections.emptyList();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getDocumentItemName()).append("\n");
		
		Collection<FieldMetadata> metadata = Lap.getFieldsMetadata(this.getClass().getName());
		Object value;
		String formattedValue;
		for (FieldMetadata fieldMetadata : metadata) {
			builder.append(Messages.getMessage(fieldMetadata.getMessageCode())).append(": ");
			
			value = get(fieldMetadata.getFieldName());
			formattedValue = (value != null ? Formatters.getFormatedValue(value, fieldMetadata) : "---");
			builder.append(formattedValue).append("\n");
		}
		
		return builder.toString();
	}
}