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

import java.util.Date;

import javax.swing.ImageIcon;

import com.trackit.business.common.FileType;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.utilities.ImageUtilities;
import com.trackit.presentation.view.folder.FolderTreeItem;

public abstract class Event extends TrackItBaseType implements DocumentItem, FolderTreeItem {
	private static ImageIcon icon = ImageUtilities.createImageIcon("chronometer_16.png");
	
	private Date time;
	private EventType event;
	private EventTypeType eventType;
	private Short eventGroup;
	private TimerTriggerType timer;
	private Float batteryLevel;
	private Integer activityIndex;
	private Integer sessionIndex;
	private Integer lapIndex;
	private Integer lengthIndex;
	private Integer coursePointIndex;
	private Float virtualPartnerSpeed;
	private Short heartRateLowAlert;
	private Short heartRateHighAlert;
	private Integer cadenceLowAlert;
	private Integer cadenceHighAlert;
	private Float speedLowAlert;
	private Float speedHighAlert;
	private Integer powerLowAlert;
	private Integer powerHighAlert;
	private Float timeDurationAlert;
	private Float accumulatedTimeDurationAlert;
	private Float distanceDurationAlert;
	private Float accumulatedDistanceDurationAlert;
	private Long caloriesDurationAlert;
	private Long accumulatedCaloriesDurationAlert;
	private FitnessEquipmentStateType fitnessEquipmentState;
	private Short recoveryHeartRate;
	
	public Event() {
		super();
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
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

	public TimerTriggerType getTimer() {
		return timer;
	}

	public void setTimer(TimerTriggerType timer) {
		this.timer = timer;
	}

	public Float getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(Float batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public Integer getActivityIndex() {
		return activityIndex;
	}

	public void setActivityIndex(Integer activityIndex) {
		this.activityIndex = activityIndex;
	}

	public Integer getSessionIndex() {
		return sessionIndex;
	}

	public void setSessionIndex(Integer sessionIndex) {
		this.sessionIndex = sessionIndex;
	}

	public Integer getLapIndex() {
		return lapIndex;
	}

	public void setLapIndex(Integer lapIndex) {
		this.lapIndex = lapIndex;
	}

	public Integer getLengthIndex() {
		return lengthIndex;
	}

	public void setLengthIndex(Integer lengthIndex) {
		this.lengthIndex = lengthIndex;
	}

	public Integer getCoursePointIndex() {
		return coursePointIndex;
	}

	public void setCoursePointIndex(Integer coursePointIndex) {
		this.coursePointIndex = coursePointIndex;
	}

	public Float getVirtualPartnerSpeed() {
		return virtualPartnerSpeed;
	}

	public void setVirtualPartnerSpeed(Float virtualPartnerSpeed) {
		this.virtualPartnerSpeed = virtualPartnerSpeed;
	}

	public Short getHeartRateLowAlert() {
		return heartRateLowAlert;
	}

	public void setHeartRateLowAlert(Short heartRateLowAlert) {
		this.heartRateLowAlert = heartRateLowAlert;
	}

	public Short getHeartRateHighAlert() {
		return heartRateHighAlert;
	}

	public void setHeartRateHighAlert(Short heartRateHighAlert) {
		this.heartRateHighAlert = heartRateHighAlert;
	}

	public Integer getCadenceLowAlert() {
		return cadenceLowAlert;
	}

	public void setCadenceLowAlert(Integer cadenceLowAlert) {
		this.cadenceLowAlert = cadenceLowAlert;
	}

	public Integer getCadenceHighAlert() {
		return cadenceHighAlert;
	}

	public void setCadenceHighAlert(Integer cadenceHighAlert) {
		this.cadenceHighAlert = cadenceHighAlert;
	}

	public Float getSpeedLowAlert() {
		return speedLowAlert;
	}

	public void setSpeedLowAlert(Float speedLowAlert) {
		this.speedLowAlert = speedLowAlert;
	}

	public Float getSpeedHighAlert() {
		return speedHighAlert;
	}

	public void setSpeedHighAlert(Float speedHighAlert) {
		this.speedHighAlert = speedHighAlert;
	}

	public Integer getPowerLowAlert() {
		return powerLowAlert;
	}

	public void setPowerLowAlert(Integer powerLowAlert) {
		this.powerLowAlert = powerLowAlert;
	}

	public Integer getPowerHighAlert() {
		return powerHighAlert;
	}

	public void setPowerHighAlert(Integer powerHighAlert) {
		this.powerHighAlert = powerHighAlert;
	}

	public Float getTimeDurationAlert() {
		return timeDurationAlert;
	}

	public void setTimeDurationAlert(Float timeDurationAlert) {
		this.timeDurationAlert = timeDurationAlert;
	}

	public Float getAccumulatedTimeDurationAlert() {
		return accumulatedTimeDurationAlert;
	}

	public void setAccumulatedTimeDurationAlert(Float accumulatedTimeDurationAlert) {
		this.accumulatedTimeDurationAlert = accumulatedTimeDurationAlert;
	}

	public Float getDistanceDurationAlert() {
		return distanceDurationAlert;
	}

	public void setDistanceDurationAlert(Float distanceDurationAlert) {
		this.distanceDurationAlert = distanceDurationAlert;
	}

	public Float getAccumulatedDistanceDurationAlert() {
		return accumulatedDistanceDurationAlert;
	}

	public void setAccumulatedDistanceDurationAlert(
			Float accumulatedDistanceDurationAlert) {
		this.accumulatedDistanceDurationAlert = accumulatedDistanceDurationAlert;
	}

	public Long getCaloriesDurationAlert() {
		return caloriesDurationAlert;
	}

	public void setCaloriesDurationAlert(Long caloriesDurationAlert) {
		this.caloriesDurationAlert = caloriesDurationAlert;
	}

	public Long getAccumulatedCaloriesDurationAlert() {
		return accumulatedCaloriesDurationAlert;
	}

	public void setAccumulatedCaloriesDurationAlert(Long accumulatedCaloriesDurationAlert) {
		this.accumulatedCaloriesDurationAlert = accumulatedCaloriesDurationAlert;
	}

	public FitnessEquipmentStateType getFitnessEquipmentState() {
		return fitnessEquipmentState;
	}

	public void setFitnessEquipmentState(
			FitnessEquipmentStateType fitnessEquipmentState) {
		this.fitnessEquipmentState = fitnessEquipmentState;
	}

	public Short getRecoveryHeartRate() {
		return recoveryHeartRate;
	}

	public void setRecoveryHeartRate(Short recoveryHeartRate) {
		this.recoveryHeartRate = recoveryHeartRate;
	}

	public abstract Trackpoint getTrackpoint();

	public abstract DocumentItem getParent();

	public static ImageIcon getIcon() {
		return icon;
	}

	@Override
	public int hashCode() {
		final Integer prime = 31;
		int result = 1;
		result = prime * result + ((time == null) ? 0 : time.hashCode());
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
		Event other = (Event) obj;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

	@Override
	public String getFolderTreeItemName() {
		return getEvent().toString();
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
		EventManager.getInstance()
			.publish(publisher, com.trackit.presentation.event.Event.EVENT_SELECTED, this);
	}
	
	public FileType[] getExportFileTypes() {
		return new FileType[0];
	}
}