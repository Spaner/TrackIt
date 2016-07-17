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

import java.util.Date;

import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;

public class Length extends TrackItBaseType {
	private Date startTime;
	private Date endTime;
	private EventType event;
	private EventTypeType eventType;
	private Short eventGroup;
	private Double elapsedTime;
	private Double timerTime;
	private Long strokes;
	private Double averageSpeed;
	private SwimStrokeType swimStroke;
	private Short averageSwimmingCadence;
	private Integer calories;
	private LengthTypeType lengthType;
	private DocumentItem parent;
	
	public Length(DocumentItem parent) {
		super();
		
		this.parent = parent;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public EventType getEvent() {
		return event;
	}

	public void setEvent(EventType event) {
		this.event = event;
	}

	public EventTypeType getEventType() {
		return eventType;
	}

	public void setEventType(EventTypeType eventType) {
		this.eventType = eventType;
	}

	public Short getEventGroup() {
		return eventGroup;
	}

	public void setEventGroup(Short eventGroup) {
		this.eventGroup = eventGroup;
	}

	public Double getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(Double elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public Double getTimerTime() {
		return timerTime;
	}

	public void setTimerTime(Double timerTime) {
		this.timerTime = timerTime;
	}

	public Long getStrokes() {
		return strokes;
	}

	public void setStrokes(Long strokes) {
		this.strokes = strokes;
	}

	public Double getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(Double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	public SwimStrokeType getSwimStroke() {
		return swimStroke;
	}

	public void setSwimStroke(SwimStrokeType swimStroke) {
		this.swimStroke = swimStroke;
	}

	public Short getAverageSwimmingCadence() {
		return averageSwimmingCadence;
	}

	public void setAverageSwimmingCadence(Short averageSwimmingCadence) {
		this.averageSwimmingCadence = averageSwimmingCadence;
	}

	public Integer getCalories() {
		return calories;
	}

	public void setCalories(Integer calories) {
		this.calories = calories;
	}

	public LengthTypeType getLengthType() {
		return lengthType;
	}

	public void setLengthType(LengthTypeType lengthType) {
		this.lengthType = lengthType;
	}

	public DocumentItem getParent() {
		return parent;
	}

	public void setParent(DocumentItem parent) {
		this.parent = parent;
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.LENGTH_SELECTED, this);
	}
}	
