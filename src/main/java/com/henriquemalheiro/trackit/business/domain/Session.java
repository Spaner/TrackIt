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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.view.data.DataType;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderTreeItem;

public class Session extends TrackItBaseType implements DocumentItem, FolderTreeItem {
	private static ImageIcon icon = ImageUtilities.createImageIcon("session_16.png");
	
	private Date startTime;
	private Date endTime;
	private SessionTriggerType trigger;
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
	private Integer normalizedPower;
	private Integer leftRightBalance;
	private Long work;
	private Float trainingEffect;
	private Float trainingStressScore;
	private Float intensityFactor;
	private Float averageStrokeCount;
	private Float averageStrokeDistance;
	private SwimStrokeType swimStroke;
	private Float poolLength;
	private DisplayMeasureType poolLengthUnit;
	private Integer numberOfActiveLengths;
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
	private Float[] timeInCadenceZone;
	private Float[] timeInSpeedZone;
	private Float[] timeInPowerZone;
	private Double northeastLatitude;
	private Double northeastLongitude;
	private Double southwestLatitude;
	private Double southwestLongitude;
	private Short gpsAccuracy;
	private Float averageLapTime;
	private Integer bestLapIndex;
	private Activity parent;
	
	public Session(Activity parent) {
		super();
		this.parent = parent;
		trigger = SessionTriggerType.ACTIVITY_END;
		event = EventType.SESSION;
		eventType = EventTypeType.STOP;
		eventGroup = 0;
		sport = SportType.GENERIC;
		subSport = SubSportType.GENERIC;
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

	public SessionTriggerType getTrigger() {
		return trigger;
	}

	public void setTrigger(SessionTriggerType trigger) {
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

	public Float getTrainingEffect() {
		return trainingEffect;
	}

	public void setTrainingEffect(Float trainingEffect) {
		this.trainingEffect = trainingEffect;
	}

	public Float getTrainingStressScore() {
		return trainingStressScore;
	}

	public void setTrainingStressScore(Float trainingStressScore) {
		this.trainingStressScore = trainingStressScore;
	}

	public Float getIntensityFactor() {
		return intensityFactor;
	}

	public void setIntensityFactor(Float intensityFactor) {
		this.intensityFactor = intensityFactor;
	}

	public Float getAverageStrokeCount() {
		return averageStrokeCount;
	}

	public void setAverageStrokeCount(Float averageStrokeCount) {
		this.averageStrokeCount = averageStrokeCount;
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

	public Float getPoolLength() {
		return poolLength;
	}

	public void setPoolLength(Float poolLength) {
		this.poolLength = poolLength;
	}

	public DisplayMeasureType getPoolLengthUnit() {
		return poolLengthUnit;
	}

	public void setPoolLengthUnit(DisplayMeasureType poolLengthUnit) {
		this.poolLengthUnit = poolLengthUnit;
	}

	public Integer getNumberOfActiveLengths() {
		return numberOfActiveLengths;
	}

	public void setNumberOfActiveLengths(Integer numberOfActiveLengths) {
		this.numberOfActiveLengths = numberOfActiveLengths;
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

	public Float[] getTimeInCadenceZone() {
		return timeInCadenceZone;
	}

	public void setTimeInCadenceZone(Float[] timeInCadenceZone) {
		this.timeInCadenceZone = timeInCadenceZone;
	}

	public Float[] getTimeInSpeedZone() {
		return timeInSpeedZone;
	}

	public void setTimeInSpeedZone(Float[] timeInSpeedZone) {
		this.timeInSpeedZone = timeInSpeedZone;
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

	public Float getAverageLapTime() {
		return averageLapTime;
	}

	public void setAverageLapTime(Float averageLapTime) {
		this.averageLapTime = averageLapTime;
	}

	public Integer getBestLapIndex() {
		return bestLapIndex;
	}

	public void setBestLapIndex(Integer bestLapIndex) {
		this.bestLapIndex = bestLapIndex;
	}

	public Activity getParent() {
		return parent;
	}

	public static ImageIcon getIcon() {
		return icon;
	}

	public List<Lap> getLaps() {
		return parent.getLaps(getStartTime(), getEndTime());
	}
	
	public List<Track> getTracks() {
		return parent.getTracks(getStartTime(), getEndTime());
	}
	
	public List<Trackpoint> getTrackpoints() {
		return parent.getTrackpoints(getStartTime(), getEndTime());
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
	
	@Override
	public void consolidate(ConsolidationLevel level) {
		double elapsedTime = 0.0;
		double timerTime = 0.0;
		double movingTime = 0.0;
		double pausedTime = 0.0;
		double distance = 0.0;
		double maximumSpeed = 0.0;
		double averageHeartRate = 0;
		short minimumHeartRate = 300;
		short maximumHeartRate = 0;
		double averageCadence = 0;
		short maximumCadence = 0;
		double averageRunningCadence = -1;
		short maximumRunningCadence = 0;
		double averagePower = 0;
		int maximumPower = -1;
		int calories = -1;
		int fatCalories = -1;
		double averageTemperature = 0;
		byte minimumTemperature = 127;
		byte maximumTemperature = -128;
		float averageAltitude = 0.0f;
		float minimumAltitude = 10000.0f;
		float maximumAltitude = -100.0f;
		float averagePositiveGrade = 0.0f;
		float averageNegativeGrade = 0.0f;
		float maximumPositiveGrade = -100.0f;
		float maximumNegativeGrade = 100.0f;
		int totalAscent = 0;
		int totalDescent = 0;
		double northEastLongitude = -181.0;
		double northEastLatitude = -91.0;
		double southWestLongitude = 181.0;
		double southWestLatitude = 91.0;
		
		boolean recalculation = (level == ConsolidationLevel.SUMMARY || level == ConsolidationLevel.RECALCULATION);
		
		if (sport == null) {
			sport = SportType.GENERIC;
		}
		
		if (subSport == null) {
			subSport = SubSportType.GENERIC;
		}
		
		for (Lap lap : getLaps()) {
			elapsedTime += lap.getElapsedTime();
			timerTime += lap.getTimerTime();
			pausedTime += lap.getPausedTime();
			movingTime += lap.getMovingTime();
			distance += lap.getDistance();
		}
		
		for (Lap lap : getLaps()) {
			double lapPercentage = lap.getTimerTime() / timerTime;
			
			if (lap.getMaximumSpeed() != null) {
				maximumSpeed = Math.max(maximumSpeed, lap.getMaximumSpeed());
			}
			
			if (lap.getAverageHeartRate() != null) {
				averageHeartRate += (lap.getAverageHeartRate() * lapPercentage);
			}
			
			if (lap.getMinimumHeartRate() != null) {
				minimumHeartRate = (short) Math.min(minimumHeartRate, lap.getMinimumHeartRate());
			}
			
			if (lap.getMaximumHeartRate() != null) {
				maximumHeartRate = (short) Math.max(maximumHeartRate, lap.getMaximumHeartRate());
			}
			
			if (lap.getAverageCadence() != null) {
				averageCadence += (lap.getAverageCadence() * lapPercentage);
			}
			
			if (lap.getMaximumCadence() != null) {
				maximumCadence = (short) Math.max(maximumCadence, lap.getMaximumCadence());
			}
			
			if (lap.getMaximumRunningCadence() != null) {
				maximumRunningCadence = (short) Math.max(maximumRunningCadence, lap.getMaximumRunningCadence());
			}

			if (lap.getAveragePower() != null) {
				averagePower += (lap.getAveragePower() * lapPercentage);
			}
			
			if (lap.getMaximumPower() != null) {
				maximumPower = (int) Math.max(maximumPower, lap.getMaximumPower());
			}
			
			if (lap.getAverageAltitude() != null) {
				averageAltitude += (lap.getAverageAltitude() * lapPercentage);
			}
			
			if (lap.getMinimumAltitude() != null) {
				minimumAltitude = (float) Math.min(minimumAltitude, lap.getMinimumAltitude());
			}
			
			if (lap.getMaximumAltitude() != null) {
				maximumAltitude = (float) Math.max(maximumAltitude, lap.getMaximumAltitude());
			}
			
			if (lap.getAverageTemperature() != null) {
				averageTemperature += (lap.getAverageTemperature() * lapPercentage);
			}
			
			if (lap.getMinimumTemperature() != null) {
				minimumTemperature = (byte) Math.min(minimumTemperature, lap.getMinimumTemperature());
			}
			
			if (lap.getMaximumTemperature() != null) {
				maximumTemperature = (byte) Math.max(maximumTemperature, lap.getMaximumTemperature());
			}
			
			if (lap.getTotalAscent() != null) {
				totalAscent += lap.getTotalAscent();
			}
			
			if (lap.getTotalDescent() != null) {
				totalDescent += lap.getTotalDescent();
			}
			
			if (lap.getCalories() != null) {
				calories += lap.getCalories();
			}
			
			if (lap.getFatCalories() != null) {
				fatCalories += lap.getFatCalories();
			}
			
			if (lap.getAveragePositiveGrade() != null) {
				averagePositiveGrade += (lap.getAveragePositiveGrade() * lapPercentage);
			}
			
			if (lap.getAverageNegativeGrade() != null) {
				averageNegativeGrade += (lap.getAverageNegativeGrade() * lapPercentage);
			}
			
			if (lap.getMaximumNegativeGrade() != null) {
				maximumNegativeGrade = Math.min(maximumNegativeGrade, lap.getMaximumNegativeGrade());
			}
			
			if (lap.getMaximumPositiveGrade() != null) {
				maximumPositiveGrade = Math.max(maximumPositiveGrade, lap.getMaximumPositiveGrade());
			}
		}
		
		for (Trackpoint trackpoint : parent.getTrackpoints()) {
			if (trackpoint.getLongitude() != null && trackpoint.getLatitude() != null) {
				northEastLongitude = Math.max(northEastLongitude, trackpoint.getLongitude());
				northEastLatitude = Math.max(northEastLatitude,  trackpoint.getLatitude());
				southWestLongitude = Math.min(southWestLongitude, trackpoint.getLongitude());
				southWestLatitude = Math.min(southWestLatitude, trackpoint.getLatitude());
			}
		}
		
		if ((startLongitude == null || recalculation) && getFirstTrackpoint() != null) {
			startLongitude = getFirstTrackpoint().getLongitude();
		}
		
		if ((startLatitude == null || recalculation) && getFirstTrackpoint() != null) {
			startLatitude = getFirstTrackpoint().getLatitude();
		}
		
		if ((startAltitude == null || recalculation) && getFirstTrackpoint() != null) {
			startAltitude = getFirstTrackpoint().getAltitude();
		}
		
		if ((endLongitude == null || recalculation) && getLastTrackpoint() != null) {
			endLongitude = getLastTrackpoint().getLongitude();
		}
		
		if ((endLatitude == null || recalculation) && getLastTrackpoint() != null) {
			endLatitude = getLastTrackpoint().getLatitude();
		}
		
		if ((endAltitude == null || recalculation) && getLastTrackpoint() != null) {
			endAltitude = getLastTrackpoint().getAltitude();
		}
		
		if (this.elapsedTime == null || recalculation) {
			this.elapsedTime = elapsedTime;
		}
		
		if (this.timerTime == null || recalculation) {
			this.timerTime = timerTime;
		}
		
		if (this.movingTime == null || recalculation) {
			this.movingTime = movingTime;
		}
		
		if (this.pausedTime == null || recalculation) {
			this.pausedTime = pausedTime;
		}
		
		if (this.distance == null || recalculation) {
			this.distance = distance;
		}
		
		if (this.averageSpeed == null || recalculation) {
			this.averageSpeed = this.distance / this.timerTime;
		}
		
		if (this.averageMovingSpeed == null || recalculation) {
			this.averageMovingSpeed = this.distance / this.movingTime;
		}
		
		if (this.maximumSpeed == null || recalculation) {
			this.maximumSpeed = maximumSpeed;
		}
		
		if ((this.averageHeartRate == null || recalculation) && averageHeartRate > 0) {
			this.averageHeartRate = (short) Math.round(averageHeartRate);
		}
		
		if ((this.minimumHeartRate == null || recalculation) && minimumHeartRate != 300) {
			this.minimumHeartRate = minimumHeartRate;
		}
		
		if ((this.maximumHeartRate == null || recalculation) && maximumHeartRate > 0) {
			this.maximumHeartRate = maximumHeartRate;
		}
		
		if ((this.averageCadence == null || recalculation) && averageCadence > 0) {
			this.averageCadence = (short) Math.round(averageCadence);
		}
		
		if ((this.maximumCadence == null || recalculation) && maximumCadence > 0) {
			this.maximumCadence = maximumCadence;
		}
		
		if ((this.averageRunningCadence == null || recalculation) && averageRunningCadence > 0) {
			this.averageRunningCadence = (short) Math.round(averageRunningCadence);
		}
		
		if ((this.maximumRunningCadence == null || recalculation) && maximumRunningCadence > 0) {
			this.maximumRunningCadence = maximumRunningCadence;
		}
		
		if ((this.averagePower == null || recalculation) && averagePower > 0) {
			this.averagePower = (int) Math.round(averagePower);
		}
		
		if ((this.maximumPower == null || recalculation) && maximumPower > -1) {
			this.maximumPower = maximumPower;
		}
		
		if ((this.calories == null || recalculation) && calories > -1) {
			this.calories = calories;
		}
		
		if ((this.fatCalories == null || recalculation) && fatCalories > -1) {
			this.fatCalories = fatCalories;
		}
		
		if ((this.averageAltitude == null || recalculation) && averageAltitude != 0.0f) {
			this.averageAltitude = (float) Math.round(averageAltitude);
		}
		
		if ((this.minimumAltitude == null || recalculation) && minimumAltitude != 10000.0f) {
			this.minimumAltitude = minimumAltitude;
		}
		
		if ((this.maximumAltitude == null || recalculation) && maximumAltitude != -100.0f) {
			this.maximumAltitude = maximumAltitude;
		}
		
		if ((this.altitudeDifference == null || recalculation) && (getMinimumAltitude() != null && getMaximumAltitude() != null)) {
			this.altitudeDifference = (getMaximumAltitude() - getMinimumAltitude());
		}
		
		if ((this.averageTemperature == null || recalculation) && averageTemperature != 0) {
			this.averageTemperature = (byte) Math.round(averageTemperature);
		}
		
		if ((this.minimumTemperature == null || recalculation) && minimumTemperature < 127) {
			this.minimumTemperature = minimumTemperature;
		}
		
		if ((this.maximumTemperature == null || recalculation) && maximumTemperature > -128) {
			this.maximumTemperature = maximumTemperature;
		}
		
		if (this.totalAscent == null || recalculation) {
			this.totalAscent = totalAscent;
		}
		
		if (this.totalDescent == null || recalculation) {
			this.totalDescent = totalDescent;
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
		
		if ((this.averageGrade == null || recalculation) && (getStartAltitude() != null && getEndAltitude() != null)) {
			this.averageGrade = (float) ((getEndAltitude() - getStartAltitude()) / getDistance());
		}
		
		if ((this.averagePositiveGrade == null || recalculation) && averagePositiveGrade != 0.0f) {
			this.averagePositiveGrade = averagePositiveGrade;
		}
		
		if ((this.averageNegativeGrade == null || recalculation) && averageNegativeGrade != 0.0f) {
			this.averageNegativeGrade = averageNegativeGrade;
		}
		
		if ((this.maximumPositiveGrade == null || recalculation) && maximumPositiveGrade != -100.0f) {
			this.maximumPositiveGrade = maximumPositiveGrade;
		}
		
		if ((this.maximumNegativeGrade == null || recalculation) && maximumNegativeGrade != 100.0f) {
			this.maximumNegativeGrade = maximumNegativeGrade;
		}
	}
	
	@Override
	public String getDocumentItemName() {
		return String.format("%s #%d", Messages.getMessage("session.label"), getParent().getSessions().indexOf(this) + 1);
	}
	
	@Override
	public FileType[] getSupportedFileTypes() {
		return new FileType[0];
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	@Override
	public String getFolderTreeItemName() {
		return Messages.getMessage("folderView.label.sessionId", getParent().getSessions().indexOf(this) + 1);
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
		EventManager.getInstance().publish(publisher, Event.SESSION_SELECTED, this);
	}
	
	public FileType[] getExportFileTypes() {
		return new FileType[0];
	}
	
	@Override
	public List<DataType> getDisplayableElements() {
		return Arrays.asList(new DataType[] { DataType.SESSION, DataType.TRACKPOINT });
	}
	
	@Override
	public List<? extends DocumentItem> getDisplayedElements(DataType dataType) {
		switch (dataType) {
		case SESSION:
			return Arrays.asList(new DocumentItem[] { this });
		case TRACKPOINT:
			return getTrackpoints();
		default:
			return Collections.emptyList();
		}
	}
}
