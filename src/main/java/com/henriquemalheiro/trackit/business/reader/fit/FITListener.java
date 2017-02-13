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
package com.henriquemalheiro.trackit.business.reader.fit;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.util.Date;

import org.apache.log4j.Logger;

import com.garmin.fit.ActivityMesg;
import com.garmin.fit.ActivityMesgListener;
import com.garmin.fit.BatteryStatus;
import com.garmin.fit.BikeProfileMesg;
import com.garmin.fit.BikeProfileMesgListener;
import com.garmin.fit.BloodPressureMesg;
import com.garmin.fit.BloodPressureMesgListener;
import com.garmin.fit.BufferedRecordMesg;
import com.garmin.fit.BufferedRecordMesgListener;
import com.garmin.fit.CapabilitiesMesg;
import com.garmin.fit.CapabilitiesMesgListener;
import com.garmin.fit.CourseMesg;
import com.garmin.fit.CourseMesgListener;
import com.garmin.fit.CoursePointMesg;
import com.garmin.fit.CoursePointMesgListener;
import com.garmin.fit.DateTime;
import com.garmin.fit.DeviceInfoMesg;
import com.garmin.fit.DeviceInfoMesgListener;
import com.garmin.fit.DeviceSettingsMesg;
import com.garmin.fit.DeviceSettingsMesgListener;
import com.garmin.fit.DeviceType;
import com.garmin.fit.DisplayMeasure;
import com.garmin.fit.Event;
import com.garmin.fit.EventMesg;
import com.garmin.fit.EventMesgListener;
import com.garmin.fit.FieldCapabilitiesMesg;
import com.garmin.fit.FieldCapabilitiesMesgListener;
import com.garmin.fit.File;
import com.garmin.fit.FileCapabilitiesMesg;
import com.garmin.fit.FileCapabilitiesMesgListener;
import com.garmin.fit.FileCreatorMesg;
import com.garmin.fit.FileCreatorMesgListener;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.FileIdMesgListener;
import com.garmin.fit.Fit;
import com.garmin.fit.GoalMesg;
import com.garmin.fit.GoalMesgListener;
import com.garmin.fit.HrZoneMesg;
import com.garmin.fit.HrZoneMesgListener;
import com.garmin.fit.HrmProfileMesg;
import com.garmin.fit.HrmProfileMesgListener;
import com.garmin.fit.HrvMesg;
import com.garmin.fit.HrvMesgListener;
import com.garmin.fit.Intensity;
import com.garmin.fit.LapMesg;
import com.garmin.fit.LapMesgListener;
import com.garmin.fit.LapTrigger;
import com.garmin.fit.LengthMesg;
import com.garmin.fit.LengthMesgListener;
import com.garmin.fit.LengthType;
import com.garmin.fit.Manufacturer;
import com.garmin.fit.Mesg;
import com.garmin.fit.MesgCapabilitiesMesg;
import com.garmin.fit.MesgCapabilitiesMesgListener;
import com.garmin.fit.MesgListener;
import com.garmin.fit.MesgWithEvent;
import com.garmin.fit.MesgWithEventListener;
import com.garmin.fit.MetZoneMesg;
import com.garmin.fit.MetZoneMesgListener;
import com.garmin.fit.MonitoringInfoMesg;
import com.garmin.fit.MonitoringInfoMesgListener;
import com.garmin.fit.MonitoringMesg;
import com.garmin.fit.MonitoringMesgListener;
import com.garmin.fit.PadMesg;
import com.garmin.fit.PadMesgListener;
import com.garmin.fit.PowerZoneMesg;
import com.garmin.fit.PowerZoneMesgListener;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;
import com.garmin.fit.ScheduleMesg;
import com.garmin.fit.ScheduleMesgListener;
import com.garmin.fit.SdmProfileMesg;
import com.garmin.fit.SdmProfileMesgListener;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionMesgListener;
import com.garmin.fit.SessionTrigger;
import com.garmin.fit.SoftwareMesg;
import com.garmin.fit.SoftwareMesgListener;
import com.garmin.fit.Sport;
import com.garmin.fit.SportMesg;
import com.garmin.fit.SportMesgListener;
import com.garmin.fit.SubSport;
import com.garmin.fit.SwimStroke;
import com.garmin.fit.TotalsMesg;
import com.garmin.fit.TotalsMesgListener;
import com.garmin.fit.UserProfileMesg;
import com.garmin.fit.UserProfileMesgListener;
import com.garmin.fit.WeightScaleMesg;
import com.garmin.fit.WeightScaleMesgListener;
import com.garmin.fit.WorkoutMesg;
import com.garmin.fit.WorkoutMesgListener;
import com.garmin.fit.WorkoutStepMesg;
import com.garmin.fit.WorkoutStepMesgListener;
import com.garmin.fit.ZonesTargetMesg;
import com.garmin.fit.ZonesTargetMesgListener;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.ActivityEvent;
import com.henriquemalheiro.trackit.business.domain.ActivityLap;
import com.henriquemalheiro.trackit.business.domain.ActivityTrack;
import com.henriquemalheiro.trackit.business.domain.ActivityType;
import com.henriquemalheiro.trackit.business.domain.BatteryStatusType;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CourseEvent;
import com.henriquemalheiro.trackit.business.domain.CourseLap;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.CoursePointType;
import com.henriquemalheiro.trackit.business.domain.CourseTrack;
import com.henriquemalheiro.trackit.business.domain.DeviceInfo;
import com.henriquemalheiro.trackit.business.domain.DeviceTypeType;
import com.henriquemalheiro.trackit.business.domain.DisplayMeasureType;
import com.henriquemalheiro.trackit.business.domain.EventType;
import com.henriquemalheiro.trackit.business.domain.EventTypeType;
import com.henriquemalheiro.trackit.business.domain.FitnessEquipmentStateType;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.GarminProductType;
import com.henriquemalheiro.trackit.business.domain.GenericProduct;
import com.henriquemalheiro.trackit.business.domain.IntensityType;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.LapTriggerType;
import com.henriquemalheiro.trackit.business.domain.Length;
import com.henriquemalheiro.trackit.business.domain.LengthTypeType;
import com.henriquemalheiro.trackit.business.domain.ManufacturerType;
import com.henriquemalheiro.trackit.business.domain.Session;
import com.henriquemalheiro.trackit.business.domain.SessionTriggerType;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.SubSportType;
import com.henriquemalheiro.trackit.business.domain.SwimStrokeType;
import com.henriquemalheiro.trackit.business.domain.TimerTriggerType;
import com.henriquemalheiro.trackit.business.domain.Track;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.utility.Utilities;


public class FITListener implements ActivityMesgListener, BikeProfileMesgListener, BloodPressureMesgListener,
		BufferedRecordMesgListener, CapabilitiesMesgListener, CourseMesgListener, CoursePointMesgListener,
		DeviceInfoMesgListener, DeviceSettingsMesgListener, EventMesgListener, FieldCapabilitiesMesgListener,
		FileCapabilitiesMesgListener, FileCreatorMesgListener, FileIdMesgListener, GoalMesgListener,
		HrZoneMesgListener, HrmProfileMesgListener, HrvMesgListener, LapMesgListener, LengthMesgListener,
		MesgCapabilitiesMesgListener, MesgListener, MesgWithEventListener, MetZoneMesgListener,
		MonitoringInfoMesgListener, MonitoringMesgListener, PadMesgListener, PowerZoneMesgListener,
		RecordMesgListener, ScheduleMesgListener, SdmProfileMesgListener, SessionMesgListener,
		SoftwareMesgListener, SportMesgListener, TotalsMesgListener, UserProfileMesgListener,
		WeightScaleMesgListener, WorkoutMesgListener, WorkoutStepMesgListener, ZonesTargetMesgListener {
	
	private GPSDocument gpsDocument;
	private Activity activity;
	private Course course;
	
	private boolean isActivity;
	private boolean isCourse;
	
	private boolean readActivities;
	private boolean readCourses;
	
	private float accumulatedTimeDurationAlert = 0.0F;
	private float accumulatedDistanceDurationAlert = 0.0F;
	private long accumulatedCaloriesDurationAlert = 0L;
	
	Logger logger = Logger.getLogger(FITListener.class.getName());
	
	public FITListener(FITFileReader reader) {
		super();
		
		readActivities = (Boolean) reader.getOptions().get(Constants.Reader.READ_ACTIVITIES);
		readCourses = (Boolean) reader.getOptions().get(Constants.Reader.READ_COURSES);
		
		gpsDocument = new GPSDocument((String) reader.getOptions().get(Constants.Reader.FILENAME));
	}
	
	public void onMesg(FileIdMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
		
		if (mesg.getType() != null && mesg.getType() != File.INVALID) {
			switch (mesg.getType()) {
			case COURSE:
				if (!readCourses) {
					break;
				}
				
				course = new Course();
				isCourse = true;
				
				gpsDocument.add(course);
				break;
				
			case ACTIVITY:
				if(!readActivities) {
					break;
				}
				
				activity = new Activity();
				
				if (mesg.getManufacturer() != null && mesg.getManufacturer() != Manufacturer.INVALID) {
					activity.getMetadata().setManufacturer(ManufacturerType.lookup(mesg.getManufacturer().shortValue()));
				}
				
				if (mesg.getProduct() != null && mesg.getProduct() != Fit.UINT16_INVALID) {
					if (activity.getMetadata().getManufacturer() == ManufacturerType.GARMIN) {
						activity.getMetadata().setProduct(GarminProductType.lookup(mesg.getProduct().shortValue()));
						activity.setCreator(GarminProductType.lookup(mesg.getProduct().shortValue()).name());
					} else {
						activity.getMetadata().setProduct(new GenericProduct(mesg.getProduct().shortValue()));
					}
				}
				
				if (mesg.getSerialNumber() != null && mesg.getSerialNumber() != Fit.UINT32Z_INVALID) {
					activity.getMetadata().setSerialNumber(mesg.getSerialNumber());
				}
				
				if (mesg.getTimeCreated() != null && mesg.getTimeCreated().getTimestamp() != DateTime.INVALID) {
					activity.setStartTime(mesg.getTimeCreated().getDate());
					activity.getMetadata().setTimeCreated(mesg.getTimeCreated().getDate());
				}
				
				isActivity = true;
				
				gpsDocument.add(activity);
				break;

			default:
				break;
			}
		}
	}

	public void onMesg(UserProfileMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(ZonesTargetMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(WorkoutStepMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(WorkoutMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(WeightScaleMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(TotalsMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(SportMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(SoftwareMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(SessionMesg sessionMesg) {
		logger.trace(getMessage("fitListener.processingMessage", "SessionMesg"));

		if (readActivities && isActivity) {
			Session session = new Session(activity);
			
			if (sessionMesg.getStartTime() != null && sessionMesg.getStartTime().getTimestamp() != DateTime.INVALID) {
				session.setStartTime(sessionMesg.getStartTime().getDate());
			}
			
			if (sessionMesg.getTimestamp() != null && sessionMesg.getTimestamp().getTimestamp() != DateTime.INVALID) {
				session.setEndTime(sessionMesg.getTimestamp().getDate());
			}
			
			if (sessionMesg.getTrigger() != null && sessionMesg.getTrigger() != SessionTrigger.INVALID) {
				session.setTrigger(SessionTriggerType.valueOf(sessionMesg.getTrigger().name()));
			}
			
			if (sessionMesg.getEvent() != null && sessionMesg.getEvent() != Event.INVALID) {
				session.setEvent(EventType.valueOf(sessionMesg.getEvent().name()));
			}

			if (sessionMesg.getEventType() != null && sessionMesg.getEventType() != com.garmin.fit.EventType.INVALID) {
				session.setEventType(EventTypeType.valueOf(sessionMesg.getEventType().name()));
			}
			
			if (sessionMesg.getEventGroup() != null && sessionMesg.getEventGroup() != Fit.UINT8_INVALID) {
				session.setEventGroup(sessionMesg.getEventGroup());
			}
			
			if (sessionMesg.getStartPositionLat() != null && sessionMesg.getStartPositionLat() != Fit.SINT32_INVALID) {
				session.setStartLatitude(Utilities.semicirclesToDegrees(sessionMesg.getStartPositionLat()));
			}
			
			if (sessionMesg.getStartPositionLong() != null && sessionMesg.getStartPositionLong() != Fit.SINT32_INVALID) {
				session.setStartLongitude(Utilities.semicirclesToDegrees(sessionMesg.getStartPositionLong()));
			}
			
			if (sessionMesg.getSport() != null && sessionMesg.getSport() != Sport.INVALID) {
				session.setSport(SportType.valueOf(sessionMesg.getSport().name()));
			}
			
			if (sessionMesg.getSubSport() != null && sessionMesg.getSubSport() != SubSport.INVALID) {
				session.setSubSport(SubSportType.valueOf(sessionMesg.getSubSport().name()));
			}
			
			if (sessionMesg.getTotalElapsedTime() != null && sessionMesg.getTotalElapsedTime() != Fit.FLOAT32_INVALID) {
				session.setElapsedTime(sessionMesg.getTotalElapsedTime().doubleValue());
			}
			
			if (sessionMesg.getTotalTimerTime() != null && sessionMesg.getTotalTimerTime() != Fit.FLOAT32_INVALID) {
				session.setTimerTime(sessionMesg.getTotalTimerTime().doubleValue());
			}

			if (sessionMesg.getTotalMovingTime() != null && sessionMesg.getTotalMovingTime() != Fit.FLOAT32_INVALID) {
				session.setMovingTime(sessionMesg.getTotalMovingTime().doubleValue());
			}
			
			if (sessionMesg.getTotalDistance() != null && sessionMesg.getTotalDistance() != Fit.FLOAT32_INVALID) {
				session.setDistance(sessionMesg.getTotalDistance().doubleValue());
			}
			
			if (sessionMesg.getAvgSpeed() != null && sessionMesg.getAvgSpeed() != Fit.FLOAT32_INVALID) {
				session.setAverageSpeed(sessionMesg.getAvgSpeed().doubleValue());
			}
			
			if (sessionMesg.getMaxSpeed() != null && sessionMesg.getMaxSpeed() != Fit.FLOAT32_INVALID) {
				session.setMaximumSpeed(sessionMesg.getMaxSpeed().doubleValue());
			}
			
			if (sessionMesg.getAvgPosVerticalSpeed() != null && sessionMesg.getAvgPosVerticalSpeed() != Fit.FLOAT32_INVALID) {
				session.setAveragePositiveVerticalSpeed(sessionMesg.getAvgPosVerticalSpeed().floatValue());
			}
			
			if (sessionMesg.getAvgNegVerticalSpeed() != null && sessionMesg.getAvgNegVerticalSpeed() != Fit.FLOAT32_INVALID) {
				session.setAverageNegativeVerticalSpeed(sessionMesg.getAvgNegVerticalSpeed().floatValue()); 
			}
			
			if (sessionMesg.getMaxPosVerticalSpeed() != null && sessionMesg.getMaxPosVerticalSpeed() != Fit.FLOAT32_INVALID) {
				session.setMaximumPositiveVerticalSpeed(sessionMesg.getMaxPosVerticalSpeed().floatValue());
			}
			
			if (sessionMesg.getMaxNegVerticalSpeed() != null && sessionMesg.getMaxNegVerticalSpeed() != Fit.FLOAT32_INVALID) {
				session.setMaximumNegativeVerticalSpeed(sessionMesg.getMaxNegVerticalSpeed().floatValue()); 
			}
			
			if (sessionMesg.getAvgHeartRate() != null && sessionMesg.getAvgHeartRate() != Fit.SINT16_INVALID) {
				session.setAverageHeartRate(sessionMesg.getAvgHeartRate());
			}
			
			if (sessionMesg.getMinHeartRate() != null && sessionMesg.getMinHeartRate() != Fit.SINT16_INVALID) {
				session.setMinimumHeartRate(sessionMesg.getMinHeartRate());
			}
			
			if (sessionMesg.getMaxHeartRate() != null && sessionMesg.getMaxHeartRate() != Fit.SINT16_INVALID) {
				session.setMaximumHeartRate(sessionMesg.getMaxHeartRate());
			}
			
			if (sessionMesg.getAvgCadence() != null && sessionMesg.getAvgCadence() != Fit.SINT16_INVALID) {
				session.setAverageCadence(sessionMesg.getAvgCadence());
			}
			
			if (sessionMesg.getMaxCadence() != null && sessionMesg.getMaxCadence() != Fit.SINT16_INVALID) {
				session.setMaximumCadence(sessionMesg.getMaxCadence());
			}
			
			if (sessionMesg.getAvgRunningCadence() != null && sessionMesg.getAvgRunningCadence() != Fit.SINT16_INVALID) {
				session.setAverageRunningCadence(sessionMesg.getAvgRunningCadence());
			}
			
			if (sessionMesg.getMaxRunningCadence() != null && sessionMesg.getMaxRunningCadence() != Fit.SINT16_INVALID) {
				session.setMaximumRunningCadence(sessionMesg.getMaxRunningCadence());
			}
			
			if (sessionMesg.getTotalCycles() != null && sessionMesg.getTotalCycles() != Fit.UINT32_INVALID) {
				session.setCycles(sessionMesg.getTotalCycles());
			}

			if (sessionMesg.getTotalStrides() != null && sessionMesg.getTotalStrides() != Fit.UINT32_INVALID) {
				session.setStrides(sessionMesg.getTotalStrides());
			}
			
			if (sessionMesg.getAvgStrokeCount() != null && sessionMesg.getAvgStrokeCount() != Fit.FLOAT32_INVALID) {
				session.setAverageStrokeCount(sessionMesg.getAvgStrokeCount());
			}

			if (sessionMesg.getAvgStrokeDistance() != null && sessionMesg.getAvgStrokeDistance() != Fit.FLOAT32_INVALID) {
				session.setAverageStrokeDistance(sessionMesg.getAvgStrokeDistance());
			}

			if (sessionMesg.getSwimStroke() != null && sessionMesg.getSwimStroke() != SwimStroke.INVALID) {
				session.setSwimStroke(SwimStrokeType.valueOf(sessionMesg.getSwimStroke().name()));
			}
			
			if (sessionMesg.getPoolLength() != null && sessionMesg.getPoolLength() != Fit.FLOAT32_INVALID) {
				session.setPoolLength(sessionMesg.getPoolLength());
			}
			
			if (sessionMesg.getPoolLengthUnit() != null && sessionMesg.getPoolLengthUnit() != DisplayMeasure.INVALID) {
				session.setPoolLengthUnit(DisplayMeasureType.valueOf(sessionMesg.getPoolLengthUnit().name()));
			}
			
			if (sessionMesg.getNumActiveLengths() != null && sessionMesg.getNumActiveLengths() != Fit.UINT16_INVALID) {
				session.setNumberOfActiveLengths(sessionMesg.getNumActiveLengths());
			}
			
			if (sessionMesg.getAvgPower() != null && sessionMesg.getAvgPower() != Fit.UINT16_INVALID) {
				session.setAveragePower(sessionMesg.getAvgPower());
			}
			
			if (sessionMesg.getMaxPower() != null && sessionMesg.getMaxPower() != Fit.UINT16_INVALID) {
				session.setMaximumPower(sessionMesg.getMaxPower());
			}

			if (sessionMesg.getNormalizedPower() != null && sessionMesg.getNormalizedPower() != Fit.UINT16_INVALID) {
				session.setNormalizedPower(sessionMesg.getNormalizedPower());
			}
			
			if (sessionMesg.getLeftRightBalance() != null && sessionMesg.getLeftRightBalance() != Fit.UINT16_INVALID) {
				session.setLeftRightBalance(sessionMesg.getLeftRightBalance());
			}

			if (sessionMesg.getTotalWork() != null && sessionMesg.getTotalWork() != Fit.UINT32_INVALID) {
				session.setWork(sessionMesg.getTotalWork());
			}
			
			if (sessionMesg.getIntensityFactor() != null && sessionMesg.getIntensityFactor() != Fit.FLOAT32_INVALID) {
				session.setIntensityFactor(sessionMesg.getIntensityFactor());
			}

			if (sessionMesg.getTotalTrainingEffect() != null && sessionMesg.getTotalTrainingEffect() != Fit.FLOAT32_INVALID) {
				session.setTrainingEffect(sessionMesg.getTotalTrainingEffect());
			}
			
			if (sessionMesg.getTrainingStressScore() != null && sessionMesg.getTrainingStressScore() != Fit.FLOAT32_INVALID) {
				session.setTrainingStressScore(sessionMesg.getTrainingStressScore());
			}
			
			if (sessionMesg.getTotalAscent() != null && sessionMesg.getTotalAscent() != Fit.UINT16_INVALID) {
				session.setTotalAscent(sessionMesg.getTotalAscent());
			}
			
			if (sessionMesg.getTotalDescent() != null && sessionMesg.getTotalDescent() != Fit.UINT16_INVALID) {
				session.setTotalDescent(sessionMesg.getTotalDescent());
			}

			if (sessionMesg.getAvgAltitude() != null && sessionMesg.getAvgAltitude() != Fit.FLOAT32_INVALID) {
				session.setAverageAltitude(sessionMesg.getAvgAltitude());
			}

			if (sessionMesg.getMinAltitude() != null && sessionMesg.getMinAltitude() != Fit.FLOAT32_INVALID) {
				session.setMinimumAltitude(sessionMesg.getMinAltitude());
			}
			
			if (sessionMesg.getMaxAltitude() != null && sessionMesg.getMaxAltitude() != Fit.FLOAT32_INVALID) {
				session.setMaximumAltitude(sessionMesg.getMaxAltitude());
			}

			if (sessionMesg.getAvgGrade() != null && sessionMesg.getAvgGrade() != Fit.FLOAT32_INVALID) {
				session.setAverageGrade(sessionMesg.getAvgGrade());
			}
			
			if (sessionMesg.getAvgPosGrade() != null && sessionMesg.getAvgPosGrade() != Fit.FLOAT32_INVALID) {
				session.setAveragePositiveGrade(sessionMesg.getAvgPosGrade());
			}

			if (sessionMesg.getAvgNegGrade() != null && sessionMesg.getAvgNegGrade() != Fit.FLOAT32_INVALID) {
				session.setAverageNegativeGrade(sessionMesg.getAvgNegGrade());
			}
			
			if (sessionMesg.getMaxPosGrade() != null && sessionMesg.getMaxPosGrade() != Fit.FLOAT32_INVALID) {
				session.setMaximumPositiveGrade(sessionMesg.getMaxPosGrade());
			}
			
			if (sessionMesg.getMaxNegGrade() != null && sessionMesg.getMaxNegGrade() != Fit.FLOAT32_INVALID) {
				session.setMaximumNegativeGrade(sessionMesg.getMaxNegGrade());
			}
			
			if (sessionMesg.getTotalCalories() != null && sessionMesg.getTotalCalories() != Fit.SINT32_INVALID
					&& sessionMesg.getTotalCalories() > 0) {
				session.setCalories(sessionMesg.getTotalCalories());
			}

			if (sessionMesg.getTotalFatCalories() != null && sessionMesg.getTotalFatCalories() != Fit.SINT32_INVALID
					&& sessionMesg.getTotalFatCalories() > 0) {
				session.setFatCalories(sessionMesg.getTotalFatCalories());
			}

			if (sessionMesg.getAvgTemperature() != null && sessionMesg.getAvgTemperature() != Fit.SINT8_INVALID) {
				session.setAverageTemperature(sessionMesg.getAvgTemperature());
			}

			if (sessionMesg.getMaxTemperature() != null && sessionMesg.getMaxTemperature() != Fit.SINT8_INVALID) {
				session.setMaximumTemperature(sessionMesg.getMaxTemperature());
			}
			
			if (sessionMesg.getNecLat() != null && sessionMesg.getNecLat() != Fit.SINT32_INVALID) {
				session.setNortheastLatitude(Utilities.semicirclesToDegrees(sessionMesg.getNecLat()));
			}
			
			if (sessionMesg.getNecLong() != null && sessionMesg.getNecLong() != Fit.SINT32_INVALID) {
				session.setNortheastLongitude(Utilities.semicirclesToDegrees(sessionMesg.getNecLong()));
			}
			
			if (sessionMesg.getSwcLat() != null && sessionMesg.getSwcLat() != Fit.SINT32_INVALID) {
				session.setSouthwestLatitude(Utilities.semicirclesToDegrees(sessionMesg.getSwcLat()));
			}
			
			if (sessionMesg.getSwcLong() != null && sessionMesg.getSwcLong() != Fit.SINT32_INVALID) {
				session.setSouthwestLongitude(Utilities.semicirclesToDegrees(sessionMesg.getSwcLong()));
			}

			if (sessionMesg.getGpsAccuracy() != null && sessionMesg.getGpsAccuracy() != Fit.UINT8_INVALID) {
				session.setGpsAccuracy(sessionMesg.getGpsAccuracy());
			}
			
			if (sessionMesg.getAvgLapTime() != null && sessionMesg.getAvgLapTime() != Fit.FLOAT32_INVALID) {
				session.setAverageLapTime(sessionMesg.getAvgLapTime());
			}

			if (sessionMesg.getBestLapIndex() != null && sessionMesg.getBestLapIndex() != Fit.UINT16_INVALID) {
				session.setBestLapIndex(sessionMesg.getBestLapIndex());
			}
			
			activity.add(session);
		}
	}

	public void onMesg(SdmProfileMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(ScheduleMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(RecordMesg trackpointMesg) {
		logger.trace(getMessage("fitListener.processingMessage",  "RecordMesg"));
		
		Trackpoint trackpoint = null;
		if (isActivity) {
			trackpoint = new Trackpoint(activity);
		} else {
			trackpoint = new Trackpoint(course);
		}

		if (trackpointMesg.getTimestamp() != null && trackpointMesg.getTimestamp().getTimestamp() != DateTime.INVALID) {
			trackpoint.setTimestamp(trackpointMesg.getTimestamp().getDate());
		}
		
		if (trackpointMesg.getPositionLat() != null && trackpointMesg.getPositionLat() != Fit.SINT32_INVALID) {
			trackpoint.setLatitude(Utilities.semicirclesToDegrees(trackpointMesg.getPositionLat()));
		}
		
		if (trackpointMesg.getPositionLong() != null && trackpointMesg.getPositionLong() != Fit.SINT32_INVALID) {
			trackpoint.setLongitude(Utilities.semicirclesToDegrees(trackpointMesg.getPositionLong()));
		}
		
		if (trackpointMesg.getAltitude() != null && trackpointMesg.getAltitude() != Fit.FLOAT32_INVALID) {
			trackpoint.setAltitude(trackpointMesg.getAltitude().doubleValue());
		}

		if (trackpointMesg.getDistance() != null && trackpointMesg.getDistance() != Fit.FLOAT32_INVALID) {
			trackpoint.setDistance(trackpointMesg.getDistance().doubleValue());
		}
		
		if (trackpointMesg.getSpeed() != null && trackpointMesg.getSpeed() != Fit.FLOAT32_INVALID) {
			trackpoint.setSpeed(trackpointMesg.getSpeed().doubleValue());
		}
		
		if (trackpointMesg.getHeartRate() != null && trackpointMesg.getHeartRate() != Fit.SINT16_INVALID) {
			trackpoint.setHeartRate(trackpointMesg.getHeartRate());
		}
		
		if (trackpointMesg.getCadence() != null && trackpointMesg.getCadence() != Fit.SINT16_INVALID) {
			trackpoint.setCadence(trackpointMesg.getCadence());
		}

		if (trackpointMesg.getPower() != null && trackpointMesg.getPower() != Fit.UINT16_INVALID) {
			trackpoint.setPower(trackpointMesg.getPower());
		}
		
		if (trackpointMesg.getGrade() != null && trackpointMesg.getGrade() != Fit.FLOAT32_INVALID) {
			trackpoint.setGrade(trackpointMesg.getGrade());
		}
		
		if (trackpointMesg.getResistance() != null && trackpointMesg.getResistance() != Fit.UINT8_INVALID) {
			trackpoint.setResistance(trackpointMesg.getResistance());
		}
		
		if (trackpointMesg.getTimeFromCourse() != null && trackpointMesg.getTimeFromCourse() != Fit.FLOAT32_INVALID) {
			trackpoint.setTimeFromCourse(trackpointMesg.getTimeFromCourse());
		}
		
		if (trackpointMesg.getTemperature() != null && trackpointMesg.getTemperature() != Fit.SINT8_INVALID) {
			trackpoint.setTemperature(trackpointMesg.getTemperature());
		}
		
		if (trackpointMesg.getCycles() != null && trackpointMesg.getCycles() != Fit.SINT16_INVALID) {
			trackpoint.setCycles(trackpointMesg.getCycles());
		}
		
		if (trackpointMesg.getTotalCycles() != null && trackpointMesg.getTotalCycles() != Fit.UINT32_INVALID) {
			trackpoint.setTotalCycles(trackpointMesg.getTotalCycles());
		}

		if (trackpointMesg.getLeftRightBalance() != null && trackpointMesg.getLeftRightBalance() != Fit.UINT8_INVALID) {
			trackpoint.setLeftRightBalance(trackpointMesg.getLeftRightBalance());
		}
		
		if (trackpointMesg.getGpsAccuracy() != null && trackpointMesg.getGpsAccuracy() != Fit.UINT8_INVALID) {
			trackpoint.setGpsAccuracy(trackpointMesg.getGpsAccuracy());
		}
		
		if (trackpointMesg.getVerticalSpeed() != null && trackpointMesg.getVerticalSpeed() != Fit.FLOAT32_INVALID) {
			trackpoint.setVerticalSpeed(trackpointMesg.getVerticalSpeed());
		}
		
		if (trackpointMesg.getCalories() != null && trackpointMesg.getCalories() != Fit.UINT16_INVALID) {
			trackpoint.setCalories(trackpointMesg.getCalories());
		}
		
		if (isActivity) {
			activity.add(trackpoint);
		} else {
			course.add(trackpoint);
		}
	}

	public void onMesg(PowerZoneMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(PadMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(MonitoringMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(MonitoringInfoMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(MetZoneMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(MesgWithEvent mesg) {
		logger.trace(getMessage("fitListener.processingMessage", "MesgWithEvent"));
	}

	public void onMesg(Mesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(MesgCapabilitiesMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(LengthMesg lengthMesg) {
		logger.trace(getMessage("fitListener.processingMessage", lengthMesg.getName()));
		
		Length length = new Length(activity);
		
		if (lengthMesg.getStartTime() != null && lengthMesg.getStartTime().getTimestamp() != DateTime.INVALID) {
			length.setStartTime(lengthMesg.getStartTime().getDate());
		}
		
		if (lengthMesg.getTimestamp() != null && lengthMesg.getTimestamp().getTimestamp() != DateTime.INVALID) {
			length.setEndTime(lengthMesg.getTimestamp().getDate());
		}
		
		if (lengthMesg.getEvent() != null && lengthMesg.getEvent() != Event.INVALID) {
			length.setEvent(EventType.valueOf(lengthMesg.getEvent().name()));
		}

		if (lengthMesg.getEventType() != null && lengthMesg.getEventType() != com.garmin.fit.EventType.INVALID) {
			length.setEventType(EventTypeType.valueOf(lengthMesg.getEventType().name()));
		}
		
		if (lengthMesg.getEventGroup() != null && lengthMesg.getEventGroup() != Fit.UINT8_INVALID) {
			length.setEventGroup(lengthMesg.getEventGroup());
		}
		
		if (lengthMesg.getTotalElapsedTime() != null && lengthMesg.getTotalElapsedTime() != Fit.FLOAT32_INVALID) {
			length.setElapsedTime(lengthMesg.getTotalElapsedTime().doubleValue());
		}
		
		if (lengthMesg.getTotalTimerTime() != null && lengthMesg.getTotalTimerTime() != Fit.FLOAT32_INVALID) {
			length.setTimerTime(lengthMesg.getTotalTimerTime().doubleValue());
		}
		
		if (lengthMesg.getTotalStrokes() != null && lengthMesg.getTotalStrokes() != Fit.UINT16_INVALID) {
			length.setStrokes(lengthMesg.getTotalStrokes().longValue());
		}
		
		if (lengthMesg.getAvgSpeed() != null && lengthMesg.getAvgSpeed() != Fit.FLOAT32_INVALID) {
			length.setAverageSpeed(lengthMesg.getAvgSpeed().doubleValue());
		}
		
		if (lengthMesg.getSwimStroke() != null && lengthMesg.getSwimStroke() != SwimStroke.INVALID) {
			length.setSwimStroke(SwimStrokeType.valueOf(lengthMesg.getSwimStroke().name()));
		}
		
		if (lengthMesg.getAvgSwimmingCadence() != null && lengthMesg.getAvgSwimmingCadence() != Fit.UINT8_INVALID) {
			length.setAverageSwimmingCadence(lengthMesg.getAvgSwimmingCadence());
		}
		
		if (lengthMesg.getTotalCalories() != null && lengthMesg.getTotalCalories() != Fit.UINT16_INVALID) {
			length.setCalories(lengthMesg.getTotalCalories());
		}
		
		if (lengthMesg.getLengthType() != null && lengthMesg.getLengthType() != LengthType.INVALID) {
			length.setLengthType(LengthTypeType.valueOf(lengthMesg.getLengthType().name()));
		}
		
		activity.add(length);
	}

	public void onMesg(LapMesg lapMesg) {
		logger.trace(getMessage("fitListener.processingMessage", "LapMesg"));
		
		if ((readActivities && isActivity) || (readCourses && isCourse)) {
			Lap lap;
			if (isActivity) {
				lap = new ActivityLap(activity);
			} else {
				lap = new CourseLap(course);
			}
			
			if (lapMesg.getStartTime() != null && lapMesg.getStartTime().getTimestamp() != DateTime.INVALID) {
				lap.setStartTime(lapMesg.getStartTime().getDate());
			}
			
			if (lapMesg.getTimestamp() != null && lapMesg.getTimestamp().getTimestamp() != DateTime.INVALID) {
				lap.setEndTime(lapMesg.getTimestamp().getDate());
			}
			
			if (lapMesg.getLapTrigger() != null && lapMesg.getLapTrigger() != LapTrigger.INVALID) {
				lap.setTrigger(LapTriggerType.valueOf(lapMesg.getLapTrigger().name()));
			}
			
			if (lapMesg.getEvent() != null && lapMesg.getEvent() != Event.INVALID) {
				lap.setEvent(EventType.valueOf(lapMesg.getEvent().name()));
			}
	
			if (lapMesg.getEventType() != null && lapMesg.getEventType() != com.garmin.fit.EventType.INVALID) {
				lap.setEventType(EventTypeType.valueOf(lapMesg.getEventType().name()));
			}
			
			if (lapMesg.getEventGroup() != null && lapMesg.getEventGroup() != Fit.UINT8_INVALID) {
				lap.setEventGroup(lapMesg.getEventGroup());
			}
			
			if (lapMesg.getStartPositionLat() != null && lapMesg.getStartPositionLat() != Fit.SINT32_INVALID) {
				lap.setStartLatitude(Utilities.semicirclesToDegrees(lapMesg.getStartPositionLat()));
			}
			
			if (lapMesg.getStartPositionLong() != null && lapMesg.getStartPositionLong() != Fit.SINT32_INVALID) {
				lap.setStartLongitude(Utilities.semicirclesToDegrees(lapMesg.getStartPositionLong()));
			}
			
			if (lapMesg.getSport() != null && lapMesg.getSport() != Sport.INVALID) {
				lap.setSport(SportType.valueOf(lapMesg.getSport().name()));
			}
			
			if (lapMesg.getSubSport() != null && lapMesg.getSubSport() != SubSport.INVALID) {
				lap.setSubSport(SubSportType.valueOf(lapMesg.getSubSport().name()));
			}
			
			if (lapMesg.getTotalElapsedTime() != null && lapMesg.getTotalElapsedTime() != Fit.FLOAT32_INVALID) {
				lap.setElapsedTime(lapMesg.getTotalElapsedTime().doubleValue());
			}
			
			if (lapMesg.getTotalTimerTime() != null && lapMesg.getTotalTimerTime() != Fit.FLOAT32_INVALID) {
				lap.setTimerTime(lapMesg.getTotalTimerTime().doubleValue());
			}
	
			if (lapMesg.getTotalMovingTime() != null && lapMesg.getTotalMovingTime() != Fit.FLOAT32_INVALID) {
				lap.setMovingTime(lapMesg.getTotalMovingTime().doubleValue());
			}
			
			if (lapMesg.getTotalDistance() != null && lapMesg.getTotalDistance() != Fit.FLOAT32_INVALID) {
				lap.setDistance(lapMesg.getTotalDistance().doubleValue());
			}
			
			if (lapMesg.getAvgSpeed() != null && lapMesg.getAvgSpeed() != Fit.FLOAT32_INVALID) {
				lap.setAverageSpeed(lapMesg.getAvgSpeed().doubleValue());
			}
			
			if (lapMesg.getMaxSpeed() != null && lapMesg.getMaxSpeed() != Fit.FLOAT32_INVALID) {
				lap.setMaximumSpeed(lapMesg.getMaxSpeed().doubleValue());
			}
			
			if (lapMesg.getAvgPosVerticalSpeed() != null && lapMesg.getAvgPosVerticalSpeed() != Fit.FLOAT32_INVALID) {
				lap.setAveragePositiveVerticalSpeed(lapMesg.getAvgPosVerticalSpeed().floatValue());
			}
			
			if (lapMesg.getAvgNegVerticalSpeed() != null && lapMesg.getAvgNegVerticalSpeed() != Fit.FLOAT32_INVALID) {
				lap.setAverageNegativeVerticalSpeed(lapMesg.getAvgNegVerticalSpeed().floatValue()); 
			}
			
			if (lapMesg.getMaxPosVerticalSpeed() != null && lapMesg.getMaxPosVerticalSpeed() != Fit.FLOAT32_INVALID) {
				lap.setMaximumPositiveVerticalSpeed(lapMesg.getMaxPosVerticalSpeed().floatValue());
			}
			
			if (lapMesg.getMaxNegVerticalSpeed() != null && lapMesg.getMaxNegVerticalSpeed() != Fit.FLOAT32_INVALID) {
				lap.setMaximumNegativeVerticalSpeed(lapMesg.getMaxNegVerticalSpeed().floatValue()); 
			}
			
			if (lapMesg.getAvgHeartRate() != null && lapMesg.getAvgHeartRate() != Fit.SINT16_INVALID) {
				lap.setAverageHeartRate(lapMesg.getAvgHeartRate());
			}
			
			if (lapMesg.getMinHeartRate() != null && lapMesg.getMinHeartRate() != Fit.SINT16_INVALID) {
				lap.setMinimumHeartRate(lapMesg.getMinHeartRate());
			}
			
			if (lapMesg.getMaxHeartRate() != null && lapMesg.getMaxHeartRate() != Fit.SINT16_INVALID) {
				lap.setMaximumHeartRate(lapMesg.getMaxHeartRate());
			}
			
			if (lapMesg.getAvgCadence() != null && lapMesg.getAvgCadence() != Fit.SINT16_INVALID) {
				lap.setAverageCadence(lapMesg.getAvgCadence());
			}
			
			if (lapMesg.getMaxCadence() != null && lapMesg.getMaxCadence() != Fit.SINT16_INVALID) {
				lap.setMaximumCadence(lapMesg.getMaxCadence());
			}
			
			if (lapMesg.getAvgRunningCadence() != null && lapMesg.getAvgRunningCadence() != Fit.SINT16_INVALID) {
				lap.setAverageRunningCadence(lapMesg.getAvgRunningCadence());
			}
			
			if (lapMesg.getMaxRunningCadence() != null && lapMesg.getMaxRunningCadence() != Fit.SINT16_INVALID) {
				lap.setMaximumRunningCadence(lapMesg.getMaxRunningCadence());
			}
			
			if (lapMesg.getTotalCycles() != null && lapMesg.getTotalCycles() != Fit.UINT32_INVALID) {
				lap.setCycles(lapMesg.getTotalCycles());
			}
	
			if (lapMesg.getTotalStrides() != null && lapMesg.getTotalStrides() != Fit.UINT32_INVALID) {
				lap.setStrides(lapMesg.getTotalStrides());
			}
			
			if (lapMesg.getAvgStrokeDistance() != null && lapMesg.getAvgStrokeDistance() != Fit.FLOAT32_INVALID) {
				lap.setAverageStrokeDistance(lapMesg.getAvgStrokeDistance());
			}
	
			if (lapMesg.getSwimStroke() != null && lapMesg.getSwimStroke() != SwimStroke.INVALID) {
				lap.setSwimStroke(SwimStrokeType.valueOf(lapMesg.getSwimStroke().name()));
			}
			
			if (lapMesg.getNumActiveLengths() != null && lapMesg.getNumActiveLengths() != Fit.UINT16_INVALID) {
				lap.setNumberOfActiveLengths(lapMesg.getNumActiveLengths());
			}
			
			if (lapMesg.getAvgPower() != null && lapMesg.getAvgPower() != Fit.UINT16_INVALID) {
				lap.setAveragePower(lapMesg.getAvgPower());
			}
			
			if (lapMesg.getMaxPower() != null && lapMesg.getMaxPower() != Fit.UINT16_INVALID) {
				lap.setMaximumPower(lapMesg.getMaxPower());
			}
	
			if (lapMesg.getNormalizedPower() != null && lapMesg.getNormalizedPower() != Fit.UINT16_INVALID) {
				lap.setNormalizedPower(lapMesg.getNormalizedPower());
			}
			
			if (lapMesg.getLeftRightBalance() != null && lapMesg.getLeftRightBalance() != Fit.UINT16_INVALID) {
				lap.setLeftRightBalance(lapMesg.getLeftRightBalance());
			}
	
			if (lapMesg.getTotalWork() != null && lapMesg.getTotalWork() != Fit.UINT32_INVALID) {
				lap.setWork(lapMesg.getTotalWork());
			}
			
			if (lapMesg.getIntensity() != null && lapMesg.getIntensity() != Intensity.INVALID) {
				lap.setIntensity(IntensityType.valueOf(lapMesg.getIntensity().name()));
			}
	
			if (lapMesg.getTotalAscent() != null && lapMesg.getTotalAscent() != Fit.UINT16_INVALID) {
				lap.setTotalAscent(lapMesg.getTotalAscent());
			}
			
			if (lapMesg.getTotalDescent() != null && lapMesg.getTotalDescent() != Fit.UINT16_INVALID) {
				lap.setTotalDescent(lapMesg.getTotalDescent());
			}
	
			if (lapMesg.getAvgAltitude() != null && lapMesg.getAvgAltitude() != Fit.FLOAT32_INVALID) {
				lap.setAverageAltitude(lapMesg.getAvgAltitude());
			}
	
			if (lapMesg.getMinAltitude() != null && lapMesg.getMinAltitude() != Fit.FLOAT32_INVALID) {
				lap.setMinimumAltitude(lapMesg.getMinAltitude());
			}
			
			if (lapMesg.getMaxAltitude() != null && lapMesg.getMaxAltitude() != Fit.FLOAT32_INVALID) {
				lap.setMaximumAltitude(lapMesg.getMaxAltitude());
			}
	
			if (lapMesg.getAvgGrade() != null && lapMesg.getAvgGrade() != Fit.FLOAT32_INVALID) {
				lap.setAverageGrade(lapMesg.getAvgGrade());
			}
			
			if (lapMesg.getAvgPosGrade() != null && lapMesg.getAvgPosGrade() != Fit.FLOAT32_INVALID) {
				lap.setAveragePositiveGrade(lapMesg.getAvgPosGrade());
			}
	
			if (lapMesg.getAvgNegGrade() != null && lapMesg.getAvgNegGrade() != Fit.FLOAT32_INVALID) {
				lap.setAverageNegativeGrade(lapMesg.getAvgNegGrade());
			}
			
			if (lapMesg.getMaxPosGrade() != null && lapMesg.getMaxPosGrade() != Fit.FLOAT32_INVALID) {
				lap.setMaximumPositiveGrade(lapMesg.getMaxPosGrade());
			}
			
			if (lapMesg.getMaxNegGrade() != null && lapMesg.getMaxNegGrade() != Fit.FLOAT32_INVALID) {
				lap.setMaximumNegativeGrade(lapMesg.getMaxNegGrade());
			}
			
			if (lapMesg.getTotalCalories() != null && lapMesg.getTotalCalories() != Fit.SINT32_INVALID
					&& lapMesg.getTotalCalories() > 0) {
				lap.setCalories(lapMesg.getTotalCalories());
			}
	
			if (lapMesg.getTotalFatCalories() != null && lapMesg.getTotalFatCalories() != Fit.SINT32_INVALID
					&& lapMesg.getTotalFatCalories() > 0) {
				lap.setFatCalories(lapMesg.getTotalFatCalories());
			}
	
			if (lapMesg.getAvgTemperature() != null && lapMesg.getAvgTemperature() != Fit.SINT8_INVALID) {
				lap.setAverageTemperature(lapMesg.getAvgTemperature());
			}
	
			if (lapMesg.getMaxTemperature() != null && lapMesg.getMaxTemperature() != Fit.SINT8_INVALID) {
				lap.setMaximumTemperature(lapMesg.getMaxTemperature());
			}
			
			if (lapMesg.getGpsAccuracy() != null && lapMesg.getGpsAccuracy() != Fit.UINT8_INVALID) {
				lap.setGpsAccuracy(lapMesg.getGpsAccuracy());
			}
			
			if (isActivity) {
				Track track = new ActivityTrack(activity);
				track.setStartTime(lap.getStartTime());
				track.setEndTime(lap.getEndTime());
				
				activity.add(track);
				activity.add(lap);
			} else if (isCourse) {
				Track track = new CourseTrack(course);
				track.setStartTime(lap.getStartTime());
				track.setEndTime(lap.getEndTime());
				
				course.add(track);
				course.add(lap);
			}
		}
	}

	public void onMesg(HrvMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(HrmProfileMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(HrZoneMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(GoalMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(FileCreatorMesg mesg) {
		if (!isActivity) {
			return;
		}
		
		if (mesg.getSoftwareVersion() != null && mesg.getSoftwareVersion() != Fit.UINT16_INVALID) {
			activity.getMetadata().setSoftwareVersion(mesg.getSoftwareVersion());
		}

		if (mesg.getHardwareVersion() != null && mesg.getHardwareVersion() != Fit.UINT8_INVALID) {
			activity.getMetadata().setHardwareVersion(mesg.getHardwareVersion());
		}
	}

	public void onMesg(FileCapabilitiesMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(FieldCapabilitiesMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(EventMesg eventMesg) {
		logger.trace(getMessage("fitListener.processingMessage", eventMesg.getName()));
		
		com.henriquemalheiro.trackit.business.domain.Event event;
		if (isActivity) {
			event = new ActivityEvent(activity);
		} else {
			event = new CourseEvent(course);
		}
		
		if (eventMesg.getTimestamp() != null && eventMesg.getTimestamp().getTimestamp() != DateTime.INVALID) {
			event.setTime(eventMesg.getTimestamp().getDate());
		} else {
			return;
		}
		
		if (eventMesg.getEvent() != null && eventMesg.getEvent() != Event.INVALID) {
			event.setEvent(EventType.lookup(eventMesg.getEvent().getValue()));
		} else {
			return;
		}

		if (eventMesg.getEventType() != null && eventMesg.getEventType() != com.garmin.fit.EventType.INVALID) {
			event.setEventType(EventTypeType.lookup(eventMesg.getEventType().getValue()));
		}

		if (eventMesg.getEventGroup() != null && eventMesg.getEventGroup() != Fit.UINT8_INVALID) {
			event.setEventGroup(eventMesg.getEventGroup().shortValue());
		}
		
		switch (event.getEvent()) {
		
		case VIRTUAL_PARTNER_PACE :
			event.setVirtualPartnerSpeed(eventMesg.getVirtualPartnerSpeed());
			break;
		case SPEED_HIGH_ALERT :
			event.setSpeedHighAlert(eventMesg.getSpeedHighAlert());
			break;
		case SPEED_LOW_ALERT :
			event.setSpeedLowAlert(eventMesg.getSpeedLowAlert());
			break;
		case HR_HIGH_ALERT :
			event.setHeartRateHighAlert(eventMesg.getHrHighAlert());
			break;
		case HR_LOW_ALERT :
			event.setHeartRateLowAlert(eventMesg.getHrLowAlert());
			break;
		case CAD_HIGH_ALERT :
			event.setCadenceHighAlert(eventMesg.getCadHighAlert());
			break;
		case CAD_LOW_ALERT :
			event.setCadenceLowAlert(eventMesg.getCadLowAlert());
			break;
		case POWER_HIGH_ALERT :
			event.setPowerHighAlert(eventMesg.getPowerHighAlert());
			break;
		case POWER_LOW_ALERT :
			event.setPowerLowAlert(eventMesg.getPowerLowAlert());
			break;
		case TIME_DURATION_ALERT :
			accumulatedTimeDurationAlert += eventMesg.getTimeDurationAlert();
			event.setTimeDurationAlert(eventMesg.getTimeDurationAlert());
			event.setAccumulatedTimeDurationAlert(accumulatedTimeDurationAlert);
			break;
		case DISTANCE_DURATION_ALERT :
			accumulatedDistanceDurationAlert += eventMesg.getDistanceDurationAlert();
			event.setDistanceDurationAlert(eventMesg.getDistanceDurationAlert());
			event.setAccumulatedDistanceDurationAlert(accumulatedDistanceDurationAlert);
			break;
		case CALORIE_DURATION_ALERT :
			accumulatedCaloriesDurationAlert += eventMesg.getCalorieDurationAlert();
			event.setCaloriesDurationAlert(eventMesg.getCalorieDurationAlert());
			event.setAccumulatedCaloriesDurationAlert(accumulatedCaloriesDurationAlert);
			break;
		case FITNESS_EQUIPMENT :
			event.setFitnessEquipmentState(FitnessEquipmentStateType.lookup(eventMesg.getFitnessEquipmentState().getValue()));
			break;
		case TIMER :
			if (eventMesg.getTimerTrigger() != null) {
				event.setTimer(TimerTriggerType.lookup(eventMesg.getTimerTrigger().getValue()));
			}
			break;
		case COURSE_POINT :
			event.setCoursePointIndex(eventMesg.getCoursePointIndex());
			break;
		case BATTERY :
			event.setBatteryLevel(eventMesg.getBatteryLevel());
			break;
		case ACTIVITY :
			event.setActivityIndex(eventMesg.getData().intValue());
			break;
		case SESSION :
			event.setSessionIndex(eventMesg.getData().intValue());
			break;
		case LAP :
			event.setLapIndex(eventMesg.getData().intValue());
			break;
		case LENGTH :
			event.setLengthIndex(eventMesg.getData().intValue());
			break;
		case RECOVERY_HR :
			event.setRecoveryHeartRate(eventMesg.getData().shortValue());
			break;
		case OFF_COURSE :
			// No data available
			break;
		case BATTERY_LOW :
			// No data available
			break;
		case POWER_UP :
			// Not supported
			break;
		case POWER_DOWN :
			// Not supported
			break;
		case WORKOUT_STEP :
			// Not supported
			break;
		case WORKOUT :
			// Not supported
			break;
		}
		
		if (isActivity) {
			activity.add(event);
		} else {
			course.add(event);
		}
	}

	public void onMesg(DeviceSettingsMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(DeviceInfoMesg deviceInfoMesg) {
		logger.trace(getMessage("fitListener.processingMessage", deviceInfoMesg.getName()));
		
		DeviceInfo deviceInfo = new DeviceInfo(activity);
		
		if (deviceInfoMesg.getTimestamp() != null && deviceInfoMesg.getTimestamp().getTimestamp() != DateTime.INVALID) {
			deviceInfo.setTime(deviceInfoMesg.getTimestamp().getDate());
		}
		
		if (deviceInfoMesg.getDeviceIndex() != null && deviceInfoMesg.getDeviceIndex() != Fit.UINT8_INVALID) {
			deviceInfo.setDeviceIndex(deviceInfoMesg.getDeviceIndex());
		}
		
		if (deviceInfoMesg.getDeviceType() != null && deviceInfoMesg.getDeviceType() != DeviceType.INVALID) {
			deviceInfo.setDeviceType(DeviceTypeType.lookup(deviceInfoMesg.getDeviceType().shortValue()));
		}
		
		if (deviceInfoMesg.getManufacturer() != null && deviceInfoMesg.getManufacturer() != Manufacturer.INVALID) {
			deviceInfo.setManufacturer(ManufacturerType.lookup(deviceInfoMesg.getManufacturer().shortValue()));
		}
		
		if (deviceInfoMesg.getSerialNumber() != null && deviceInfoMesg.getSerialNumber() != Fit.UINT32Z_INVALID) {
			deviceInfo.setSerialNumber(deviceInfoMesg.getSerialNumber());
		}
		
		if (deviceInfoMesg.getProduct() != null && deviceInfoMesg.getProduct() != Fit.UINT16_INVALID) {
			if (deviceInfo.getManufacturer() == ManufacturerType.GARMIN) {
				deviceInfo.setProduct(GarminProductType.lookup(deviceInfoMesg.getProduct().shortValue()));
			} else {
				deviceInfo.setProduct(new GenericProduct(deviceInfoMesg.getProduct().shortValue()));
			}
		}
		
		if (deviceInfoMesg.getSoftwareVersion() != null && deviceInfoMesg.getSoftwareVersion() != Fit.FLOAT32_INVALID) {
			deviceInfo.setSoftwareVersion(deviceInfoMesg.getSoftwareVersion());
		}

		if (deviceInfoMesg.getHardwareVersion() != null && deviceInfoMesg.getHardwareVersion() != Fit.UINT8_INVALID) {
			deviceInfo.setHardwareVersion(deviceInfoMesg.getHardwareVersion());
		}

		if (deviceInfoMesg.getCumOperatingTime() != null && deviceInfoMesg.getCumOperatingTime() != Fit.UINT32_INVALID) {
			deviceInfo.setCummulativeOperatingTime(deviceInfoMesg.getCumOperatingTime());
		}

		if (deviceInfoMesg.getBatteryVoltage() != null && deviceInfoMesg.getBatteryVoltage() != Fit.FLOAT32_INVALID) {
			deviceInfo.setBatteryVoltage(deviceInfoMesg.getBatteryVoltage());
		}
		
		if (deviceInfoMesg.getBatteryStatus() != null && deviceInfoMesg.getBatteryStatus() != BatteryStatus.INVALID) {
			deviceInfo.setBatteryStatus(BatteryStatusType.lookup(deviceInfoMesg.getBatteryStatus().shortValue()));
		}
		
		activity.add(deviceInfo);
	}

	public void onMesg(CoursePointMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", "CoursePointMesg"));
		
		Date timestamp = null;
		
		CoursePoint coursePoint = new CoursePoint("", course);

		if (mesg.getName() != null && mesg.getName() != Fit.STRING_INVALID) {
			coursePoint.setName(mesg.getName().trim());
		}
		
		if (mesg.getType() != null && mesg.getType().getValue() != Fit.ENUM_INVALID) {
			coursePoint.setType(CoursePointType.lookup(mesg.getType().name()));
		}

		if (mesg.getTimestamp() != null && mesg.getTimestamp().getTimestamp() != DateTime.INVALID) {
			timestamp = mesg.getTimestamp().getDate();
			coursePoint.setTime(timestamp);
		}

		if (mesg.getDistance() != null && mesg.getDistance() != Fit.FLOAT32_INVALID) {
		}
		
		if (mesg.getPositionLat() != null && mesg.getPositionLat() != Fit.SINT32_INVALID) {
			coursePoint.setLatitude(Utilities.semicirclesToDegrees(mesg.getPositionLat()));
		}
		
		if (mesg.getPositionLong() != null && mesg.getPositionLong() != Fit.SINT32_INVALID) {
			coursePoint.setLongitude(Utilities.semicirclesToDegrees(mesg.getPositionLong()));
		}
		
		coursePoint.setParent(course);
		course.add(coursePoint);
	}

	public void onMesg(CourseMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", "CourseMesg"));
		
		if (mesg.getName() != null && mesg.getName() != Fit.STRING_INVALID) {
			course.setName(mesg.getName().trim());
		}
		
		if (mesg.getSport() != null && mesg.getSport().getValue() != Fit.ENUM_INVALID) {
//			course.setSport(SportType.valueOf(mesg.getSport().name()));
			course.setSportAndSubSport( SportType.valueOf(mesg.getSport().name()),
										SubSportType.GENERIC_SUB);
		}
		
		if (mesg.getCapabilities() != null && mesg.getCapabilities() != Fit.UINT32Z_INVALID) {
			System.out.println("Capabilities: " + mesg.getCapabilities());
		}
	}

	public void onMesg(CapabilitiesMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(BufferedRecordMesg mesg) {
//			logger.trace("Processing message BufferedRecordMesg");
	}

	public void onMesg(BloodPressureMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(BikeProfileMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", mesg.getName()));
	}

	public void onMesg(ActivityMesg mesg) {
		logger.trace(getMessage("fitListener.processingMessage", "ActivityMesg"));
		
		if (mesg.getTimestamp() != null && mesg.getTimestamp().getTimestamp() != DateTime.INVALID) {
			activity.setEndTime(mesg.getTimestamp().getDate());
		}
		
		if (mesg.getTotalTimerTime() != null && mesg.getTotalTimerTime() != Fit.FLOAT32_INVALID) {
			activity.setTotalTimerTime(mesg.getTotalTimerTime().doubleValue());
		}
		
		if (mesg.getType() != null && mesg.getType() != com.garmin.fit.Activity.INVALID) {
			activity.setType(ActivityType.lookup(mesg.getType().getValue()));
		}
		
		if (mesg.getEvent() != null && mesg.getEvent() != Event.INVALID) {
			activity.setEvent(EventType.lookup(mesg.getEvent().getValue()));
		}

		if (mesg.getEventType() != null && mesg.getEventType() != com.garmin.fit.EventType.INVALID) {
			activity.setEventType(EventTypeType.lookup(mesg.getEventType().getValue()));
		}

		if (mesg.getEventGroup() != null && mesg.getEventGroup() != Fit.UINT8_INVALID) {
			activity.setEventGroup(mesg.getEventGroup().shortValue());
		}
	}
	
	public GPSDocument getGPSDocument() {
		return gpsDocument;
	}
}
