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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.business.common.BoundingBox;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderTreeItem;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayer;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainter;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterFactory;

public class CoursePoint extends TrackItBaseType implements FolderTreeItem {
	private String name;
	private CoursePointType type;
	private Date time;
	private Double distance;
	private Double latitude;
	private Double longitude;
	private Double altitude;
	private String notes;
	private Course parent;
	
	public CoursePoint(String name, Course parent) {
		this(name, CoursePointType.GENERIC, parent);
	}
	
	public CoursePoint(String name, CoursePointType type, Course parent) {
		super();
		
		this.name = name;
		this.type = type;
		this.parent = parent;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CoursePointType getType() {
		return type;
	}

	public void setType(CoursePointType type) {
		this.type = type;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Double getDistance() {
		return distance;
	}
	
	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getAltitude() {
		return altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Trackpoint getTrackpoint() {
		List<Trackpoint> trackpoints = getParent().getTrackpoints(time, time);
		return (trackpoints != null && !trackpoints.isEmpty() ? trackpoints.get(0) : null);
	}

	public Course getParent() {
		return parent;
	}

	public void setParent(Course parent) {
		this.parent = parent;
	}
	
	@Override
	public List<Trackpoint> getTrackpoints() {
		return getParent().getTrackpoints(time, time);
	}
	
	@Override
	public String getDocumentItemName() {
		return String.format("%s (%s [%s])", Messages.getMessage("coursePoint.label"), getName(), getType());
	}

	@Override
	public String getFolderTreeItemName() {
		return name;
	}

	@Override
	public ImageIcon getOpenIcon() {
		return new ImageIcon(type.getIcon().getImage());
	}

	@Override
	public ImageIcon getClosedIcon() {
		return new ImageIcon(type.getIcon().getImage());
	}

	@Override
	public ImageIcon getLeafIcon() {
		return new ImageIcon(type.getIcon().getImage());
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.COURSE_POINT_SELECTED, this);
	}
	
	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}
	
	/* Paintable Interface */
	
	@Override
	public void paint(Graphics2D graphics, MapLayer layer, Map<String, Object> paintingAttributes) {
		MapPainter painter = MapPainterFactory.getInstance().getMapPainter(layer, this);
		painter.paint(graphics, paintingAttributes);
	}
	
	@Override
	public void consolidate(ConsolidationLevel level) {
		final boolean recalculate = (level == ConsolidationLevel.RECALCULATION);
		final Course course = getParent();
		
		if (getType() == null) {
			setType(CoursePointType.GENERIC);
		}
		
		if (getName() == null) {
			setName(String.format("%s #%d", Messages.getMessage("coursePoint.label"), course.getCoursePoints().indexOf(this)));
		}
		
		Trackpoint trackpoint = getTrackpointByLocation();
		trackpoint = (trackpoint == null ? course.getFirstTrackpoint() : trackpoint);
		
		if (getTime() == null || recalculate || !getTime().equals(trackpoint.getTimestamp())) {
			setTime(trackpoint.getTimestamp());
		}
		
		if (getDistance() == null || recalculate || !getDistance().equals(trackpoint.getDistance())) {
			setDistance(trackpoint.getDistance());
		}
		
		if (getLongitude() == null || recalculate) {
			setLongitude(trackpoint.getLongitude());
		}
		
		if (getLatitude() == null || recalculate) {
			setLatitude(trackpoint.getLatitude());
		}
		
		if (getAltitude() == null || recalculate) {
			setAltitude(trackpoint.getAltitude());
		}
	}

	private Trackpoint getTrackpointByLocation() {
		Trackpoint targetTrackpoint = null;
		double distance = Double.MAX_VALUE;
		
		if (getLongitude() != null && getLatitude() != null) {
			BoundingBox coursePointBB = new BoundingBox(new Location(getLongitude(), getLatitude()), 50.0);
			
			for (Trackpoint trackpoint : getParent().getTrackpoints()) {
				if (coursePointBB.contains(new Location(trackpoint.getLongitude(), trackpoint.getLatitude()))) {
					if (targetTrackpoint == null || calculateDistance(trackpoint) < distance) {
						targetTrackpoint = trackpoint;
						distance = calculateDistance(trackpoint);
					}
				}
			}
		}
		
		return targetTrackpoint;
	}

	private double calculateDistance(Trackpoint trackpoint) {
		return Utilities.getGreatCircleDistance(getLatitude(), getLongitude(),
				trackpoint.getLatitude(), trackpoint.getLongitude()) * 1000.0;
	}
}
