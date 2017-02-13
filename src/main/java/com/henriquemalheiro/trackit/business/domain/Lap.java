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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.view.data.DataType;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderTreeItem;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayer;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainter;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterFactory;
import com.pg58406.trackit.business.domain.Pause;

public abstract class Lap extends TrackItBaseType implements DocumentItem, FolderTreeItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7800512688845876607L;

	private static ImageIcon icon = ImageUtilities.createImageIcon("lap_16.png");

	private Date startTime;
	private Date endTime;
	private LapTriggerType trigger;
	private EventType event;
	private EventTypeType eventType;
	private Short eventGroup;
	private Double startLatitude;
	private Double startLongitude;
	private Double startAltitude;
	private Double endLatitude;
	private Double endLongitude;
	private Double endAltitude;
	private SportType sport;
	private SubSportType subSport;
	private Double elapsedTime;
	private Double timerTime;
	private Double movingTime;
	private Double pausedTime;
	private Double distance;
	private Long cycles;
	private Long strides;
	private Integer calories;
	private Integer fatCalories;
	private Double averageSpeed;
	private Double averageMovingSpeed;
	private Double maximumSpeed;
	private Short averageHeartRate;
	private Short minimumHeartRate;
	private Short maximumHeartRate;
	private Short averageCadence;
	private Short maximumCadence;
	private Short averageRunningCadence;
	private Short maximumRunningCadence;
	private Integer averagePower;
	private Integer maximumPower;
	private Integer totalAscent;
	private Integer totalDescent;
	private IntensityType intensity;
	private Integer normalizedPower;
	private Integer leftRightBalance;
	private Long work;
	private Float averageStrokeDistance;
	private SwimStrokeType swimStroke;
	private Float averageAltitude;
	private Float minimumAltitude;
	private Float maximumAltitude;
	private Float altitudeDifference;
	private Float averageGrade;
	private Float averagePositiveGrade;
	private Float averageNegativeGrade;
	private Float maximumPositiveGrade;
	private Float maximumNegativeGrade;
	private Byte averageTemperature;
	private Byte minimumTemperature;
	private Byte maximumTemperature;
	private Float averagePositiveVerticalSpeed;
	private Float averageNegativeVerticalSpeed;
	private Float maximumPositiveVerticalSpeed;
	private Float maximumNegativeVerticalSpeed;
	private Float[] timeInHeartRateZone;
	private Float[] timeInSpeedZone;
	private Float[] timeInCadenceZone;
	private Float[] timeInPowerZone;
	private Double northeastLatitude;
	private Double northeastLongitude;
	private Double southwestLatitude;
	private Double southwestLongitude;
	private Short gpsAccuracy;
	private Integer firstLengthIndex;
	private Integer numberOfLengths;
	private Integer numberOfActiveLengths;
	private String notes;
	protected Track firstTrack;
	protected Track lastTrack;
	private List<Pause> pauses;

	public Lap() {
		super();
		this.trigger = LapTriggerType.MANUAL;
		this.event = EventType.LAP;
		this.eventType = EventTypeType.STOP;
		this.eventGroup = 0;
		this.intensity = IntensityType.ACTIVE;
		//sport = SportType.GENERIC;
		//subSport = SubSportType.GENERIC;
		pauses = new ArrayList<Pause>();
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

	public LapTriggerType getTrigger() {
		return trigger;
	}

	public void setTrigger(LapTriggerType trigger) {
		this.trigger = trigger;
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

	public Double getStartLatitude() {
		return startLatitude;
	}

	public void setStartLatitude(Double startLatitude) {
		this.startLatitude = startLatitude;
	}

	public Double getStartLongitude() {
		return startLongitude;
	}

	public void setStartLongitude(Double startLongitude) {
		this.startLongitude = startLongitude;
	}

	public Double getStartAltitude() {
		return startAltitude;
	}

	public void setStartAltitude(Double startAltitude) {
		this.startAltitude = startAltitude;
	}

	public Double getEndLatitude() {
		return endLatitude;
	}

	public void setEndLatitude(Double endLatitude) {
		this.endLatitude = endLatitude;
	}

	public Double getEndLongitude() {
		return endLongitude;
	}

	public void setEndLongitude(Double endLongitude) {
		this.endLongitude = endLongitude;
	}

	public Double getEndAltitude() {
		return endAltitude;
	}

	public void setEndAltitude(Double endAltitude) {
		this.endAltitude = endAltitude;
	}

	public SportType getSport() {
		return sport;
	}

	public void setSport(SportType sport) {
		this.sport = sport;
	}

	public SubSportType getSubSport() {
		return subSport;
	}

	public void setSubSport(SubSportType subSport) {
		this.subSport = subSport;
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

	public Double getMovingTime() {
		return movingTime;
	}

	public void setMovingTime(Double movingTime) {
		this.movingTime = movingTime;
	}

	public Double getPausedTime() {
		return pausedTime;
	}

	public void setPausedTime(Double pausedTime) {
		this.pausedTime = pausedTime;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Long getCycles() {
		return cycles;
	}

	public void setCycles(Long cycles) {
		this.cycles = cycles;
	}

	public Long getStrides() {
		return strides;
	}

	public void setStrides(Long strides) {
		this.strides = strides;
	}

	public Integer getCalories() {
		return calories;
	}

	public void setCalories(Integer calories) {
		this.calories = calories;
	}

	public Integer getFatCalories() {
		return fatCalories;
	}

	public void setFatCalories(Integer fatCalories) {
		this.fatCalories = fatCalories;
	}

	public Double getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(Double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	public Double getAverageMovingSpeed() {
		return averageMovingSpeed;
	}

	public void setAverageMovingSpeed(Double averageMovingSpeed) {
		this.averageMovingSpeed = averageMovingSpeed;
	}

	public Double getMaximumSpeed() {
		return maximumSpeed;
	}

	public void setMaximumSpeed(Double maximumSpeed) {
		this.maximumSpeed = maximumSpeed;
	}

	public Short getAverageHeartRate() {
		return averageHeartRate;
	}

	public void setAverageHeartRate(Short averageHeartRate) {
		this.averageHeartRate = averageHeartRate;
	}

	public Short getMinimumHeartRate() {
		return minimumHeartRate;
	}

	public void setMinimumHeartRate(Short minimumHeartRate) {
		this.minimumHeartRate = minimumHeartRate;
	}

	public Short getMaximumHeartRate() {
		return maximumHeartRate;
	}

	public void setMaximumHeartRate(Short maximumHeartRate) {
		this.maximumHeartRate = maximumHeartRate;
	}

	public Short getAverageCadence() {
		return averageCadence;
	}

	public void setAverageCadence(Short averageCadence) {
		this.averageCadence = averageCadence;
	}

	public Short getMaximumCadence() {
		return maximumCadence;
	}

	public void setMaximumCadence(Short maximumCadence) {
		this.maximumCadence = maximumCadence;
	}

	public Short getAverageRunningCadence() {
		return averageRunningCadence;
	}

	public void setAverageRunningCadence(Short averageRunningCadence) {
		this.averageRunningCadence = averageRunningCadence;
	}

	public Short getMaximumRunningCadence() {
		return maximumRunningCadence;
	}

	public void setMaximumRunningCadence(Short maximumRunningCadence) {
		this.maximumRunningCadence = maximumRunningCadence;
	}

	public Integer getAveragePower() {
		return averagePower;
	}

	public void setAveragePower(Integer averagePower) {
		this.averagePower = averagePower;
	}

	public Integer getMaximumPower() {
		return maximumPower;
	}

	public void setMaximumPower(Integer maximumPower) {
		this.maximumPower = maximumPower;
	}

	public Integer getTotalAscent() {
		return totalAscent;
	}

	public void setTotalAscent(Integer totalAscent) {
		this.totalAscent = totalAscent;
	}

	public Integer getTotalDescent() {
		return totalDescent;
	}

	public void setTotalDescent(Integer totalDescent) {
		this.totalDescent = totalDescent;
	}

	public IntensityType getIntensity() {
		return intensity;
	}

	public void setIntensity(IntensityType intensity) {
		this.intensity = intensity;
	}

	public Integer getNormalizedPower() {
		return normalizedPower;
	}

	public void setNormalizedPower(Integer normalizedPower) {
		this.normalizedPower = normalizedPower;
	}

	public Integer getLeftRightBalance() {
		return leftRightBalance;
	}

	public void setLeftRightBalance(Integer leftRightBalance) {
		this.leftRightBalance = leftRightBalance;
	}

	public Long getWork() {
		return work;
	}

	public void setWork(Long work) {
		this.work = work;
	}

	public Float getAverageStrokeDistance() {
		return averageStrokeDistance;
	}

	public void setAverageStrokeDistance(Float averageStrokeDistance) {
		this.averageStrokeDistance = averageStrokeDistance;
	}

	public SwimStrokeType getSwimStroke() {
		return swimStroke;
	}

	public void setSwimStroke(SwimStrokeType swimStroke) {
		this.swimStroke = swimStroke;
	}

	public Float getAverageAltitude() {
		return averageAltitude;
	}

	public void setAverageAltitude(Float averageAltitude) {
		this.averageAltitude = averageAltitude;
	}

	public Float getMinimumAltitude() {
		return minimumAltitude;
	}

	public void setMinimumAltitude(Float minimumAltitude) {
		this.minimumAltitude = minimumAltitude;
	}

	public Float getMaximumAltitude() {
		return maximumAltitude;
	}

	public void setMaximumAltitude(Float maximumAltitude) {
		this.maximumAltitude = maximumAltitude;
	}

	public Float getAltitudeDifference() {
		return altitudeDifference;
	}

	public void setAltitudeDifference(Float altitudeDifference) {
		this.altitudeDifference = altitudeDifference;
	}

	public Float getAverageGrade() {
		return averageGrade;
	}

	public void setAverageGrade(Float averageGrade) {
		this.averageGrade = averageGrade;
	}

	public Float getAveragePositiveGrade() {
		return averagePositiveGrade;
	}

	public void setAveragePositiveGrade(Float averagePositiveGrade) {
		this.averagePositiveGrade = averagePositiveGrade;
	}

	public Float getAverageNegativeGrade() {
		return averageNegativeGrade;
	}

	public void setAverageNegativeGrade(Float averageNegativeGrade) {
		this.averageNegativeGrade = averageNegativeGrade;
	}

	public Float getMaximumPositiveGrade() {
		return maximumPositiveGrade;
	}

	public void setMaximumPositiveGrade(Float maximumPositiveGrade) {
		this.maximumPositiveGrade = maximumPositiveGrade;
	}

	public Float getMaximumNegativeGrade() {
		return maximumNegativeGrade;
	}

	public void setMaximumNegativeGrade(Float maximumNegativeGrade) {
		this.maximumNegativeGrade = maximumNegativeGrade;
	}

	public Byte getAverageTemperature() {
		return averageTemperature;
	}

	public void setAverageTemperature(Byte averageTemperature) {
		this.averageTemperature = averageTemperature;
	}

	public Byte getMinimumTemperature() {
		return minimumTemperature;
	}

	public void setMinimumTemperature(Byte minimumTemperature) {
		this.minimumTemperature = minimumTemperature;
	}

	public Byte getMaximumTemperature() {
		return maximumTemperature;
	}

	public void setMaximumTemperature(Byte maximumTemperature) {
		this.maximumTemperature = maximumTemperature;
	}

	public Float getAveragePositiveVerticalSpeed() {
		return averagePositiveVerticalSpeed;
	}

	public void setAveragePositiveVerticalSpeed(Float averagePositiveVerticalSpeed) {
		this.averagePositiveVerticalSpeed = averagePositiveVerticalSpeed;
	}

	public Float getAverageNegativeVerticalSpeed() {
		return averageNegativeVerticalSpeed;
	}

	public void setAverageNegativeVerticalSpeed(Float averageNegativeVerticalSpeed) {
		this.averageNegativeVerticalSpeed = averageNegativeVerticalSpeed;
	}

	public Float getMaximumPositiveVerticalSpeed() {
		return maximumPositiveVerticalSpeed;
	}

	public void setMaximumPositiveVerticalSpeed(Float maximumPositiveVerticalSpeed) {
		this.maximumPositiveVerticalSpeed = maximumPositiveVerticalSpeed;
	}

	public Float getMaximumNegativeVerticalSpeed() {
		return maximumNegativeVerticalSpeed;
	}

	public void setMaximumNegativeVerticalSpeed(Float maximumNegativeVerticalSpeed) {
		this.maximumNegativeVerticalSpeed = maximumNegativeVerticalSpeed;
	}

	public Float[] getTimeInHeartRateZone() {
		return timeInHeartRateZone;
	}

	public void setTimeInHeartRateZone(Float[] timeInHeartRateZone) {
		this.timeInHeartRateZone = timeInHeartRateZone;
	}

	public Float[] getTimeInSpeedZone() {
		return timeInSpeedZone;
	}

	public void setTimeInSpeedZone(Float[] timeInSpeedZone) {
		this.timeInSpeedZone = timeInSpeedZone;
	}

	public Float[] getTimeInCadenceZone() {
		return timeInCadenceZone;
	}

	public void setTimeInCadenceZone(Float[] timeInCadenceZone) {
		this.timeInCadenceZone = timeInCadenceZone;
	}

	public Float[] getTimeInPowerZone() {
		return timeInPowerZone;
	}

	public void setTimeInPowerZone(Float[] timeInPowerZone) {
		this.timeInPowerZone = timeInPowerZone;
	}

	public Double getNortheastLatitude() {
		return northeastLatitude;
	}

	public void setNortheastLatitude(Double northeastLatitude) {
		this.northeastLatitude = northeastLatitude;
	}

	public Double getNortheastLongitude() {
		return northeastLongitude;
	}

	public void setNortheastLongitude(Double northeastLongitude) {
		this.northeastLongitude = northeastLongitude;
	}

	public Double getSouthwestLatitude() {
		return southwestLatitude;
	}

	public void setSouthwestLatitude(Double southwestLatitude) {
		this.southwestLatitude = southwestLatitude;
	}

	public Double getSouthwestLongitude() {
		return southwestLongitude;
	}

	public void setSouthwestLongitude(Double southwestLongitude) {
		this.southwestLongitude = southwestLongitude;
	}

	public Short getGpsAccuracy() {
		return gpsAccuracy;
	}

	public void setGpsAccuracy(Short gpsAccuracy) {
		this.gpsAccuracy = gpsAccuracy;
	}

	public Integer getFirstLengthIndex() {
		return firstLengthIndex;
	}

	public void setFirstLengthIndex(Integer firstLengthIndex) {
		this.firstLengthIndex = firstLengthIndex;
	}

	public Integer getNumberOfLengths() {
		return numberOfLengths;
	}

	public void setNumberOfLengths(Integer numberOfLengths) {
		this.numberOfLengths = numberOfLengths;
	}

	public Integer getNumberOfActiveLengths() {
		return numberOfActiveLengths;
	}

	public void setNumberOfActiveLengths(Integer numberOfActiveLengths) {
		this.numberOfActiveLengths = numberOfActiveLengths;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public abstract DocumentItem getParent();

	public abstract List<Track> getTracks();

	public abstract List<Length> getLengths();

	public abstract List<Trackpoint> getTrackpoints();
	
	public Track getFirstTrack(){
		return firstTrack;
	}
	
	public Track getLastTrack(){
		return lastTrack;
	}
	
	public void setFirstTrack(Track track){
		this.firstTrack = track;
	}
	
	public void setLastTrack(Track track){
		this.lastTrack = track;
	}

	public void setTracks(List<Track> tracks) {
		if (tracks == null || tracks.isEmpty()) {
			firstTrack = null;
			lastTrack = null;
		} else {
			firstTrack = tracks.get(0);
			lastTrack = tracks.get(tracks.size() - 1);
		}
	}

	public void setTrackpoints(List<Trackpoint> trackpoints) {
		if (trackpoints.isEmpty()) {
			throw new IllegalStateException("Cannot set lap trackpoints with an empty list.");
		}

		reset();

		Trackpoint firstTrackpoint = trackpoints.get(0);
		Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);

		setStartTime(firstTrackpoint.getTimestamp());
		setEndTime(lastTrackpoint.getTimestamp());
	}

	public Trackpoint getFirstTrackpoint() {
		List<Trackpoint> trackpoints = getTrackpoints();

		if (trackpoints != null && trackpoints.size() > 0) {
			return trackpoints.get(0);
		} else {
			return null;
		}
	}

	public Trackpoint getLastTrackpoint() {
		List<Trackpoint> trackpoints = getTrackpoints();

		if (trackpoints != null && trackpoints.size() > 0) {
			return trackpoints.get(trackpoints.size() - 1);
		} else {
			return null;
		}
	}

	public void add(Trackpoint trackpoint) {
		long startTime = getStartTime().getTime();
		long endTime = getEndTime().getTime();
		long currentTime = trackpoint.getTimestamp().getTime();

		reset();

		if (currentTime < startTime) {
			setStartTime(new Date(currentTime));
			setEndTime(new Date(endTime));
		} else if (currentTime > endTime) {
			setStartTime(new Date(startTime));
			setEndTime(new Date(currentTime));
		}

		consolidate(ConsolidationLevel.RECALCULATION);
	}

	@Override
	public String getFolderTreeItemName() {
		return Messages.getMessage("folderView.label.lapId", getParent().getLaps().indexOf(this));
	}

	@Override
	public ImageIcon getOpenIcon() {
		return icon;
	}

	@Override
	public ImageIcon getClosedIcon() {
		return icon;
	}

	@Override
	public ImageIcon getLeafIcon() {
		return icon;
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.LAP_SELECTED, this);
	}

	/* Paintable Interface */

	@Override
	public void paint(Graphics2D graphics, MapLayer layer, Map<String, Object> paintingAttributes) {
		MapPainter painter = MapPainterFactory.getInstance().getMapPainter(layer, this);
		painter.paint(graphics, paintingAttributes);
	}

	/* Operations */
	@Override
	public void consolidate(ConsolidationLevel level) {
		boolean recalculation = (level == ConsolidationLevel.SUMMARY || level == ConsolidationLevel.RECALCULATION);
		List<Trackpoint> trackpoints = getTrackpoints();

		if (trackpoints.size() == 0) {
			consolidateStationaryLap();
			return;
		}

		Trackpoint firstTrackpoint = trackpoints.get(0);
		Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);

		/* Core fields */

		if (getStartTime() == null || recalculation) {
			setStartTime(firstTrackpoint.getTimestamp());
		}

		if (getEndTime() == null || recalculation) {
			setEndTime(lastTrackpoint.getTimestamp());
		}

		if (getStartLatitude() == null || recalculation) {
			setStartLatitude(firstTrackpoint.getLatitude());
		}

		if (getStartLongitude() == null || recalculation) {
			setStartLongitude(firstTrackpoint.getLongitude());
		}

		if (getStartAltitude() == null || recalculation) {
			setStartAltitude(firstTrackpoint.getAltitude());
		}

		if (getEndLatitude() == null || recalculation) {
			setEndLatitude(lastTrackpoint.getLatitude());
		}

		if (getEndLongitude() == null || recalculation) {
			setEndLongitude(lastTrackpoint.getLongitude());
		}

		if (getEndAltitude() == null || recalculation) {
			setEndAltitude(lastTrackpoint.getAltitude());
		}

		if (getIntensity() == null) {
			setIntensity(IntensityType.ACTIVE);
		}

		if (getTrigger() == null) {
			setTrigger(LapTriggerType.MANUAL);
		}

		if (getSport() == null) {
			//setSport(SportType.GENERIC);
		}

		/* Calculated fields */

		Short minimumHeartRate = 300;
		Integer averageHeartRate = 0;
		Short maximumHeartRate = 0;
		Integer averageCadence = 0;
		Short maximumCadence = 0;
		Double maximumSpeed = 0.0;
		Double elapsedTime = 0.0;
		Double movingTime = 0.0;
		Double pausedTime = 0.0;
		Double totalAscent = 0.0;
		Double totalDescent = 0.0;
		Float minimumGrade = 100.0f;
		Float maximumGrade = -100.0f;
		Float averageAltitude = 0.0f;
		Float minimumAltitude = 10000.0f;
		Float maximumAltitude = -100.0f;
		Float averageGrade = 0.0f;
		Float averagePositiveGrade = 0.0f;
		Float averageNegativeGrade = 0.0f;
		int averageTemperature = 0;
		byte minimumTemperature = 100;
		byte maximumTemperature = -100;
		double northEastLongitude = -181.0;
		double northEastLatitude = -91.0;
		double southWestLongitude = 181.0;
		double southWestLatitude = 91.0;

		Double lastAltitude = firstTrackpoint.getAltitude();
		int trackpointsWithHeartRate = 0;
		int trackpointsWithCadence = 0;
		int trackpointsWithTemperature = 0;
		int trackpointsWithAltitude = 0;
		int trackpointsWithGrade = 0;
		int trackpointsWithPositiveGrade = 0;
		int trackpointsWithNegativeGrade = 0;
		double time = 0.0;
		double distance = 0.0;
		boolean isFirstLap = true;
		if (trackpoints.get(0).getDistanceFromPrevious() != 0) {
			isFirstLap = false;
		}
		boolean isFirstTrackpoint = true;
		double limit = getSpeedThreshold();
		for (Trackpoint trackpoint : trackpoints) {

			if (trackpoint.getAltitude() != null && lastAltitude != null) {
				Double altitudeDiff = trackpoint.getAltitude() - lastAltitude;

				if (altitudeDiff > 0.0) {
					totalAscent += altitudeDiff;
				} else if (altitudeDiff < 0.0) {
					totalDescent -= altitudeDiff;
				}

				averageAltitude += trackpoint.getAltitude().floatValue();
				minimumAltitude = (float) Math.min(minimumAltitude, trackpoint.getAltitude());
				maximumAltitude = (float) Math.max(maximumAltitude, trackpoint.getAltitude());
				trackpointsWithAltitude++;

				lastAltitude = trackpoint.getAltitude();
			} else if (trackpoint.getAltitude() != null && lastAltitude == null) {
				lastAltitude = trackpoint.getAltitude();
			}
			if (isFirstTrackpoint && !isFirstLap) {
				distance += 0;
				time += 0;
				pausedTime += 0;
				movingTime += 0;
				isFirstTrackpoint = false;
			} else {
				distance += trackpoint.getDistanceFromPrevious();
				time += trackpoint.getTimeFromPrevious();
				if ((trackpoint.getDistanceFromPrevious() - trackpoint.getTimeFromPrevious()) < limit) {
					pausedTime += trackpoint.getTimeFromPrevious();
				} else {
					movingTime += trackpoint.getTimeFromPrevious();
				}
			}

			if (trackpoint.getHeartRate() != null) {
				averageHeartRate += trackpoint.getHeartRate();
				minimumHeartRate = (short) Math.min(minimumHeartRate, trackpoint.getHeartRate());
				maximumHeartRate = (short) Math.max(maximumHeartRate, trackpoint.getHeartRate());
				trackpointsWithHeartRate++;
			}

			if (trackpoint.getCadence() != null && trackpoint.getCadence() > 0) {
				averageCadence += trackpoint.getCadence();
				maximumCadence = (short) Math.max(maximumCadence, trackpoint.getCadence());
				trackpointsWithCadence++;
			}

			maximumSpeed = (trackpoint.getSpeed() > maximumSpeed) ? trackpoint.getSpeed() : maximumSpeed;

			if (trackpoint.getGrade() != null) {
				averageGrade += trackpoint.getGrade();
				minimumGrade = Math.min(minimumGrade, trackpoint.getGrade());
				maximumGrade = Math.max(maximumGrade, trackpoint.getGrade());
				trackpointsWithGrade++;

				if (trackpoint.getGrade() >= 0.0f) {
					averagePositiveGrade += trackpoint.getGrade();
					trackpointsWithPositiveGrade++;
				}

				if (trackpoint.getGrade() < 0.0f) {
					averageNegativeGrade += trackpoint.getGrade();
					trackpointsWithNegativeGrade++;
				}
			}

			if (trackpoint.getTemperature() != null) {
				averageTemperature += trackpoint.getTemperature().byteValue();
				minimumTemperature = (byte) Math.min(minimumTemperature, trackpoint.getTemperature());
				maximumTemperature = (byte) Math.max(maximumTemperature, trackpoint.getTemperature());
				trackpointsWithTemperature++;
			}

			if (trackpoint.getLongitude() != null && trackpoint.getLatitude() != null) {
				northEastLongitude = Math.max(northEastLongitude, trackpoint.getLongitude());
				northEastLatitude = Math.max(northEastLatitude, trackpoint.getLatitude());
				southWestLongitude = Math.min(southWestLongitude, trackpoint.getLongitude());
				southWestLatitude = Math.min(southWestLatitude, trackpoint.getLatitude());
			}
		}

		elapsedTime = (endTime.getTime() - startTime.getTime()) / 1000.0;

		if (getTotalAscent() == null || recalculation) {
			setTotalAscent(Double.valueOf(totalAscent).intValue());
		}

		if (getTotalDescent() == null || recalculation) {
			setTotalDescent(Double.valueOf(totalDescent).intValue());
		}

		if (getDistance() == null || recalculation) {
			setDistance(distance);
		}

		if (getTimerTime() == null || recalculation) {
			setTimerTime(time);
		}

		if (getPausedTime() == null || recalculation) {

			if (pauses == null || pauses.isEmpty()) {
				setPausedTime(pausedTime);
			} else {
				pausedTime = 0.;
				for (Pause p : pauses) {
					pausedTime += p.getDuration();
				}
				setPausedTime(pausedTime);
			}
		}

		if (getMovingTime() == null || recalculation) {
			setMovingTime(time - pausedTime);
		}

		if (getElapsedTime() == null || recalculation) {
			setElapsedTime(elapsedTime);
		}

		if ((getAverageHeartRate() == null || recalculation) && trackpointsWithHeartRate > 0) {
			setAverageHeartRate((short) (averageHeartRate / trackpointsWithHeartRate));
		} else if (getAverageHeartRate() == null || getAverageHeartRate() == 0) {
			setAverageHeartRate(null);
		}

		if ((getMinimumHeartRate() == null || recalculation) && minimumHeartRate != 300) {
			setMinimumHeartRate(minimumHeartRate);
		} else if (getMinimumHeartRate() != null && (getMinimumHeartRate() == 0 || minimumHeartRate == 300)) {
			setMinimumHeartRate(null);
		}

		if (getMaximumHeartRate() == null || recalculation) {
			setMaximumHeartRate(maximumHeartRate);
		} else if (getMaximumHeartRate() == 0) {
			setMaximumHeartRate(null);
		}

		if ((getAverageCadence() == null || recalculation) && trackpointsWithCadence > 0) {
			setAverageCadence((short) (averageCadence / trackpointsWithCadence));
		} else if (getAverageCadence() != null && getAverageCadence() == 0) {
			setAverageCadence(null);
		}

		if (getMaximumCadence() == null || recalculation) {
			setMaximumCadence(maximumCadence);
		} else if (getMaximumCadence() == 0) {
			setMaximumCadence(null);
		}

		if (getAverageSpeed() == null || recalculation) {
			setAverageSpeed(getDistance() / getTimerTime());
		}

		if (getAverageMovingSpeed() == null || recalculation) {
			setAverageMovingSpeed(getDistance() / getMovingTime());
		}

		if (getMaximumSpeed() == null || recalculation) {
			setMaximumSpeed(maximumSpeed);
		}

		if ((getAverageAltitude() == null || recalculation) && trackpointsWithAltitude > 0) {
			setAverageAltitude(averageAltitude / trackpointsWithAltitude);
		}

		if ((getMinimumAltitude() == null || recalculation) && minimumAltitude != 10000) {
			setMinimumAltitude(minimumAltitude);
		}

		if ((getMaximumAltitude() == null || recalculation) && maximumAltitude != -100) {
			setMaximumAltitude(maximumAltitude);
		}

		if ((getAltitudeDifference() == null || recalculation)
				&& (minimumAltitude != null && maximumAltitude != null)) {
			setAltitudeDifference(maximumAltitude - minimumAltitude + 1);
		}

		if ((getAverageGrade() == null || recalculation) && averageGrade != 0.0f) {
			setAverageGrade(averageGrade / trackpointsWithGrade);
		}

		if ((getAveragePositiveGrade() == null || recalculation) && averagePositiveGrade > 0.0f) {
			setAveragePositiveGrade(averagePositiveGrade / trackpointsWithPositiveGrade);
		}

		if ((getAverageNegativeGrade() == null || recalculation) && averageNegativeGrade < 0.0f) {
			setAverageNegativeGrade(averageNegativeGrade / trackpointsWithNegativeGrade);
		}

		if ((getMaximumNegativeGrade() == null || recalculation) && minimumGrade != 100.0f) {
			setMaximumNegativeGrade(minimumGrade);
		}

		if ((getMaximumPositiveGrade() == null || recalculation) && maximumGrade != -100.0f) {
			setMaximumPositiveGrade(maximumGrade);
		}

		if ((getMaximumNegativeGrade() == null || recalculation) && minimumGrade != 100.0f) {
			setMaximumNegativeGrade(minimumGrade);
		}

		if ((getAverageTemperature() == null || recalculation) && averageTemperature != 0) {
			setAverageTemperature((byte) (averageTemperature / trackpointsWithTemperature));
		}

		if ((getMinimumTemperature() == null || recalculation) && minimumTemperature != 100) {
			setMinimumTemperature(minimumTemperature);
		}

		if ((getMaximumTemperature() == null || recalculation) && maximumTemperature != -100) {
			setMaximumTemperature(maximumTemperature);
		}

		if ((getNortheastLongitude() == null || recalculation) && northEastLongitude != -181.0) {
			setNortheastLongitude(northEastLongitude);
		}

		if ((getNortheastLatitude() == null || recalculation) && northEastLatitude != -91.0) {
			setNortheastLatitude(northEastLatitude);
		}

		if ((getSouthwestLongitude() == null || recalculation) && southWestLongitude != 181.0) {
			setSouthwestLongitude(southWestLongitude);
		}

		if ((getSouthwestLatitude() == null || recalculation) && southWestLatitude != 91.0) {
			setSouthwestLatitude(southWestLatitude);
		}

		Track track = null;
		if (getParent().getTracks().isEmpty() || recalculation) {
			if (getParent().isActivity()) {
				Activity parent = (Activity) getParent();
				track = new ActivityTrack(parent);
				track.setStartTime(getStartTime());
				track.setEndTime(getEndTime());
				parent.add(track);
			} else if (getParent().isCourse()) {
				Course parent = (Course) getParent();
				track = new CourseTrack(parent);
				track.setStartTime(getStartTime());
				track.setEndTime(getEndTime());
				parent.add(track);
			}
		}
	}

	private void consolidateStationaryLap() {
		setTimerTime(getTimerTime() != null ? getTimerTime() : 0.0);
		setElapsedTime(getElapsedTime() != null ? getElapsedTime() : getTimerTime());
		setMovingTime(getMovingTime() != null ? getMovingTime() : getTimerTime());
		setPausedTime(getTimerTime() - getMovingTime());
	}

	public void reset() {
		setStartTime(null);
		setEndTime(null);
		setTrigger(LapTriggerType.MANUAL);
		setEvent(EventType.LAP);
		setEventType(EventTypeType.STOP);
		setEventGroup((short) 0);
		setStartLongitude(null);
		setStartLatitude(null);
		setStartAltitude(null);
		setEndLongitude(null);
		setEndLatitude(null);
		setEndAltitude(null);
		setSport(null);
		setSubSport(null);
		setElapsedTime(null);
		setTimerTime(null);
		setMovingTime(null);
		setPausedTime(null);
		setDistance(null);
		setCycles(null);
		setStrides(null);
		setCalories(null);
		setFatCalories(null);
		setAverageSpeed(null);
		setAverageMovingSpeed(null);
		setMaximumSpeed(null);
		setAverageHeartRate(null);
		setMinimumHeartRate(null);
		setMaximumHeartRate(null);
		setAverageCadence(null);
		setMaximumCadence(null);
		setAverageRunningCadence(null);
		setMaximumRunningCadence(null);
		setAveragePower(null);
		setMaximumPower(null);
		setTotalAscent(null);
		setTotalDescent(null);
		setIntensity(IntensityType.ACTIVE);
		setNormalizedPower(null);
		setLeftRightBalance(null);
		setWork(null);
		setAverageStrokeDistance(null);
		setSwimStroke(null);
		setAverageAltitude(null);
		setMinimumAltitude(null);
		setMaximumAltitude(null);
		setAltitudeDifference(null);
		setAverageGrade(null);
		setAveragePositiveGrade(null);
		setAverageNegativeGrade(null);
		setMaximumPositiveGrade(null);
		setMaximumNegativeGrade(null);
		setAverageTemperature(null);
		setMinimumTemperature(null);
		setMaximumTemperature(null);
		setAveragePositiveVerticalSpeed(null);
		setAverageNegativeVerticalSpeed(null);
		setMaximumPositiveVerticalSpeed(null);
		setMaximumNegativeVerticalSpeed(null);
		setTimeInHeartRateZone(null);
		setTimeInSpeedZone(null);
		setTimeInCadenceZone(null);
		setTimeInPowerZone(null);
		setNortheastLongitude(null);
		setNortheastLatitude(null);
		setSouthwestLongitude(null);
		setSouthwestLatitude(null);
		setGpsAccuracy(null);
		setFirstLengthIndex(null);
		setNumberOfLengths(null);
		setNumberOfLengths(null);
		setNotes(null);
	}

	@Override
	public List<DataType> getDisplayableElements() {
		return Arrays.asList(new DataType[] { DataType.LAP, DataType.TRACK, DataType.TRACKPOINT, DataType.EVENT });
	}

	@Override
	public List<? extends DocumentItem> getDisplayedElements(DataType dataType) {
		switch (dataType) {
		case LAP:
			return Arrays.asList(new DocumentItem[] { this });
		case TRACK:
			return getTracks();
		case TRACK_SEGMENT:
			return getSegments();
		case TRACKPOINT:
			return getTrackpoints();
		case EVENT:
			return getEvents();
		default:
			return Collections.emptyList();
		}
	}

	@Override
	public String getDocumentItemName() {
		return String.format("%s #%d", Messages.getMessage("lap.label"), getParent().getLaps().indexOf(this) + 1);
	}

	public List<Pause> getPauses() {
		return pauses;
	}

	public void setPauses(List<Pause> pauses) {
		this.pauses = pauses;
	}

	public void addPause(Pause pause) {
		pauses.add(pause);
	}

	private Double getSpeedThreshold() {
		double limit = 0.0;
		List<SportType> sportList = Arrays.asList(SportType.values());
		List<SubSportType> subSportList = Arrays.asList(SubSportType.values());
		short sportID = -1;
		short subSportID = -1;
		if (this.getParent().isActivity()) {
			Activity activity = ((Activity) this.getParent());
			if(activity.getSport()==null || activity.getSport()==SportType.INVALID){
				sportID = DocumentManager.getInstance().getDatabase().getSport(activity);
				subSportID = DocumentManager.getInstance().getDatabase().getSubSport(activity);
			}
			else{
				sportID = activity.getSport().getSportID();
				subSportID = activity.getSubSport().getSubSportID();
			}
		} else if (this.getParent().isCourse()) {
			Course course = ((Course) this.getParent());
			if(course.getSport()==null || course.getSport()==SportType.INVALID){
				sportID = DocumentManager.getInstance().getDatabase().getSport(course);
				subSportID = DocumentManager.getInstance().getDatabase().getSubSport(course);
			}
			else{
				sportID = course.getSport().getSportID();
				subSportID = course.getSubSport().getSubSportID();
			}
		}
			for (SportType sportIt : sportList) {
				if (sportIt.getSportID() == sportID) {
					this.setSport(sportIt);
					break;
				}
			}
			for (SubSportType subSportIt : subSportList) {
				if (subSportIt.getSubSportID() == subSportID) {
					this.setSubSport(subSportIt);
					break;
				}
			}
		

		limit = DocumentManager.getInstance().getDatabase().getPauseThresholdSpeed(sport, subSport, false);
		// double limit = 3.6;
		return limit;
	}
	
	

}
