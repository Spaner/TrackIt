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
package com.trackit.business.domain;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.trackit.business.common.Formatters;
import com.trackit.business.common.Messages;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.Utilities;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.view.map.layer.MapLayer;
import com.trackit.presentation.view.map.painter.MapPainter;
import com.trackit.presentation.view.map.painter.MapPainterFactory;

public class Trackpoint extends TrackItBaseType implements Comparable<Trackpoint> {
	private Date timestamp;
	private Double timeFromPrevious;
	private Double latitude;
	private Double longitude;
	private Double altitude;
	private Double distance;
	private Double distanceFromPrevious;
	private Double speed;
	private Short heartRate;
	private Short cadence;
	private Integer power;
	private Float grade;
	private Short resistance;
	private Float timeFromCourse;
	private Byte temperature;
	private Short cycles;
	private Long totalCycles;
	private Short leftRightBalance;
	private Short gpsAccuracy;
	private Float verticalSpeed;
	private Integer calories;
	private CoursePoint coursePoint;
	private Boolean sticky;
	private Boolean viewable;
	private DocumentItem parent;
	
	public Trackpoint(DocumentItem parent) {
		super();
		this.parent = parent;
		this.sticky = false;
		this.viewable = true;
	}
	
	public Trackpoint(double longitude, double latitude, DocumentItem parent) {
		this(parent);
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public Trackpoint(double longitude, double latitude) {
		this(longitude, latitude, null);
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Double getTimeFromPrevious() {
		return timeFromPrevious;
	}
	
	/*public Double getTimeFromPrevious() {
		long time = Math.round(timeFromPrevious);
		Double returnTime = new Double(TimeUnit.MILLISECONDS.toSeconds(time));
		return returnTime;
	}*/

	public void setTimeFromPrevious(Double timeFromPrevious) {
		this.timeFromPrevious = timeFromPrevious;
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

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Double getDistanceFromPrevious() {
		return distanceFromPrevious;
	}

	public void setDistanceFromPrevious(Double distanceFromPrevious) {
		this.distanceFromPrevious = distanceFromPrevious;
	}
	
	//12335: 2017-03-16
	public double getDistanceFrom( Trackpoint another) {
		return Utilities.getGreatCircleDistance( this.latitude,    this.longitude,
				                                 another.latitude, another.longitude) * 1000.;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	public Short getHeartRate() {
		return heartRate;
	}

	public void setHeartRate(Short heartRate) {
		this.heartRate = heartRate;
	}

	public Short getCadence() {
		return cadence;
	}

	public void setCadence(Short cadence) {
		this.cadence = cadence;
	}

	public Integer getPower() {
		return power;
	}

	public void setPower(Integer power) {
		this.power = power;
	}

	public Float getGrade() {
		return grade;
	}

	public void setGrade(Float grade) {
		this.grade = grade;
	}

	public Short getResistance() {
		return resistance;
	}

	public void setResistance(Short resistance) {
		this.resistance = resistance;
	}

	public Float getTimeFromCourse() {
		return timeFromCourse;
	}

	public void setTimeFromCourse(Float timeFromCourse) {
		this.timeFromCourse = timeFromCourse;
	}

	public Byte getTemperature() {
		return temperature;
	}

	public void setTemperature(Byte temperature) {
		this.temperature = temperature;
	}

	public Short getCycles() {
		return cycles;
	}

	public void setCycles(Short cycles) {
		this.cycles = cycles;
	}

	public Long getTotalCycles() {
		return totalCycles;
	}

	public void setTotalCycles(Long totalCycles) {
		this.totalCycles = totalCycles;
	}

	public Short getLeftRightBalance() {
		return leftRightBalance;
	}

	public void setLeftRightBalance(Short leftRightBalance) {
		this.leftRightBalance = leftRightBalance;
	}

	public Short getGpsAccuracy() {
		return gpsAccuracy;
	}

	public void setGpsAccuracy(Short gpsAccuracy) {
		this.gpsAccuracy = gpsAccuracy;
	}

	public Float getVerticalSpeed() {
		return verticalSpeed;
	}

	public void setVerticalSpeed(Float verticalSpeed) {
		this.verticalSpeed = verticalSpeed;
	}

	public Integer getCalories() {
		return calories;
	}

	public void setCalories(Integer calories) {
		this.calories = calories;
	}

	public CoursePoint getCoursePoint() {
		return coursePoint;
	}

	public void setCoursePoint(CoursePoint coursePoint) {
		this.coursePoint = coursePoint;
	}

	public boolean isSticky() {
		return sticky;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	public boolean isViewable() {
		return viewable;
	}

	public void setViewable(boolean viewable) {
		this.viewable = viewable;
	}
	
	public DocumentItem getParent() {
		return parent;
	}
	
	public void setParent(DocumentItem parent) {
		this.parent = parent;
	}
	
	@Override
	public int compareTo(Trackpoint trackpoint) {
		return (getId() > trackpoint.getId() ? 1 : (getId() == trackpoint.getId() ? 0 : -1));
	}
	
	@Override
	public String getDocumentItemName() {
		return String.format("%s (%s)",
				Messages.getMessage("trackpoint.label"),
				Formatters.getFormatedDistance(getDistance()));
	}
	
	@Override
	public List<Trackpoint> getTrackpoints() {
		return Arrays.asList(this);
	}
	
	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.TRACKPOINT_SELECTED, this);
	}
	
	@Override
	public void publishHighlightEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.TRACKPOINT_HIGHLIGHTED, this);
	}
	
	/* Paintable Interface */
	
	@Override
	public void paint(Graphics2D graphics, MapLayer layer, Map<String, Object> paintingAttributes) {
		MapPainter painter = MapPainterFactory.getInstance().getMapPainter(layer, this);
		painter.paint(graphics, paintingAttributes);
	}
	
	//12335: 2017-03-24
	public Trackpoint clone() {
		return clone( this);
	}
	
//	public Trackpoint clone() {								//12335: 2017-03-24
	public Trackpoint clone( TrackItBaseType parent){
		Trackpoint trackpoint = new Trackpoint( parent);
		trackpoint.setTimestamp(this.timestamp);
		trackpoint.setTimeFromPrevious(this.timeFromPrevious);
		trackpoint.setLatitude(this.latitude);
		trackpoint.setLongitude(this.longitude);
		trackpoint.setAltitude(this.altitude);
		trackpoint.setDistance(this.distance);
		trackpoint.setDistanceFromPrevious(this.distanceFromPrevious);
		trackpoint.setSpeed(this.speed);
		trackpoint.setHeartRate(this.heartRate);
		trackpoint.setCadence(this.cadence);
		trackpoint.setPower(this.power);
		trackpoint.setGrade(this.grade);
		trackpoint.setResistance(this.resistance);
		trackpoint.setTimeFromCourse(this.timeFromCourse);
		trackpoint.setTemperature(this.temperature);
		trackpoint.setCycles(this.cycles);
		trackpoint.setTotalCycles(this.totalCycles);
		trackpoint.setLeftRightBalance(this.leftRightBalance);
		trackpoint.setGpsAccuracy(this.gpsAccuracy);
		trackpoint.setVerticalSpeed(this.verticalSpeed);
		trackpoint.setCalories(this.calories);
		trackpoint.setCoursePoint(this.coursePoint);
		return trackpoint;
	}
	
	public void copyData(Trackpoint copy){
		copy.setTimestamp(this.timestamp);
		copy.setTimeFromPrevious(this.timeFromPrevious);
		copy.setLatitude(this.latitude);
		copy.setLongitude(this.longitude);
		copy.setAltitude(this.altitude);
		copy.setDistance(this.distance);
		copy.setDistanceFromPrevious(this.distanceFromPrevious);
		copy.setSpeed(this.speed);
		copy.setHeartRate(this.heartRate);
		copy.setCadence(this.cadence);
		copy.setPower(this.power);
		copy.setGrade(this.grade);
		copy.setResistance(this.resistance);
		copy.setTimeFromCourse(this.timeFromCourse);
		copy.setTemperature(this.temperature);
		copy.setCycles(this.cycles);
		copy.setTotalCycles(this.totalCycles);
		copy.setLeftRightBalance(this.leftRightBalance);
		copy.setGpsAccuracy(this.gpsAccuracy);
		copy.setVerticalSpeed(this.verticalSpeed);
		copy.setCalories(this.calories);
		copy.setCoursePoint(this.coursePoint);
	}
}
