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

import java.io.InputStream;
import java.util.Map;

import com.garmin.fit.ActivityMesgListener;
import com.garmin.fit.BikeProfileMesgListener;
import com.garmin.fit.BloodPressureMesgListener;
import com.garmin.fit.BufferedRecordMesgListener;
import com.garmin.fit.CapabilitiesMesgListener;
import com.garmin.fit.CourseMesgListener;
import com.garmin.fit.CoursePointMesgListener;
import com.garmin.fit.Decode;
import com.garmin.fit.DeviceInfoMesgListener;
import com.garmin.fit.DeviceSettingsMesgListener;
import com.garmin.fit.EventMesgListener;
import com.garmin.fit.FieldCapabilitiesMesgListener;
import com.garmin.fit.FileCapabilitiesMesgListener;
import com.garmin.fit.FileCreatorMesgListener;
import com.garmin.fit.FileIdMesgListener;
import com.garmin.fit.GoalMesgListener;
import com.garmin.fit.HrZoneMesgListener;
import com.garmin.fit.HrmProfileMesgListener;
import com.garmin.fit.HrvMesgListener;
import com.garmin.fit.LapMesgListener;
import com.garmin.fit.LengthMesgListener;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.MesgCapabilitiesMesgListener;
import com.garmin.fit.MesgListener;
import com.garmin.fit.MesgWithEventListener;
import com.garmin.fit.MetZoneMesgListener;
import com.garmin.fit.MonitoringInfoMesgListener;
import com.garmin.fit.MonitoringMesgListener;
import com.garmin.fit.PadMesgListener;
import com.garmin.fit.PowerZoneMesgListener;
import com.garmin.fit.RecordMesgListener;
import com.garmin.fit.ScheduleMesgListener;
import com.garmin.fit.SdmProfileMesgListener;
import com.garmin.fit.SessionMesgListener;
import com.garmin.fit.SoftwareMesgListener;
import com.garmin.fit.SportMesgListener;
import com.garmin.fit.TotalsMesgListener;
import com.garmin.fit.UserProfileMesgListener;
import com.garmin.fit.WeightScaleMesgListener;
import com.garmin.fit.WorkoutMesgListener;
import com.garmin.fit.WorkoutStepMesgListener;
import com.garmin.fit.ZonesTargetMesgListener;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.exception.ReaderException;
import com.henriquemalheiro.trackit.business.reader.ReaderTemplate;


public class FITFileReader extends ReaderTemplate {
	public FITFileReader() {
		super();
	}
	
	public FITFileReader(Map<String, Object> options) {
		super(options);
	}
	
	public GPSDocument read(InputStream inputStream, String filePath) throws ReaderException {//58406
		try {
			startTimer();
			GPSDocument document = readFITFile(inputStream);
			stopTimer();
			printTimerInfo();
			
			document.setFileName(filePath);
			for(Activity a : document.getActivities()){
				if(a.getTrackpoints().get(0).getSpeed() != null)
					a.setNoSpeedInFile(false);
			}
			for(Course c : document.getCourses()){
				if(c.getTrackpoints().get(0).getSpeed() != null)
					c.setNoSpeedInFile(false);
			}
			return document;
		} catch (RuntimeException | FITReaderException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new ReaderException(e.getMessage());
		}
	}

	private GPSDocument readFITFile(final InputStream inputStream) throws FITReaderException {
		Decode decode = new Decode();
		final FITListener listener = new FITListener(this);
		final MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
		
		mesgBroadcaster.addListener((ActivityMesgListener) listener);
		mesgBroadcaster.addListener((BikeProfileMesgListener) listener);
		mesgBroadcaster.addListener((BloodPressureMesgListener) listener);
		mesgBroadcaster.addListener((BufferedRecordMesgListener) listener);
		mesgBroadcaster.addListener((CapabilitiesMesgListener) listener);
		mesgBroadcaster.addListener((CourseMesgListener) listener);
		mesgBroadcaster.addListener((CoursePointMesgListener) listener);
		mesgBroadcaster.addListener((DeviceInfoMesgListener) listener);
		mesgBroadcaster.addListener((DeviceSettingsMesgListener) listener);
		mesgBroadcaster.addListener((EventMesgListener) listener);
		mesgBroadcaster.addListener((FieldCapabilitiesMesgListener) listener);
		mesgBroadcaster.addListener((FileCapabilitiesMesgListener) listener);
		mesgBroadcaster.addListener((FileCreatorMesgListener) listener);
		mesgBroadcaster.addListener((FileIdMesgListener) listener);
		mesgBroadcaster.addListener((GoalMesgListener) listener);
		mesgBroadcaster.addListener((HrZoneMesgListener) listener);
		mesgBroadcaster.addListener((HrmProfileMesgListener) listener);
		mesgBroadcaster.addListener((HrvMesgListener) listener);
		mesgBroadcaster.addListener((LapMesgListener) listener);
		mesgBroadcaster.addListener((LengthMesgListener) listener);
		mesgBroadcaster.addListener((MesgCapabilitiesMesgListener) listener);
		mesgBroadcaster.addListener((MesgListener) listener);
		mesgBroadcaster.addListener((MesgWithEventListener) listener);
		mesgBroadcaster.addListener((MetZoneMesgListener) listener);
		mesgBroadcaster.addListener((MonitoringInfoMesgListener) listener);
		mesgBroadcaster.addListener((MonitoringMesgListener) listener);
		mesgBroadcaster.addListener((PadMesgListener) listener);
		mesgBroadcaster.addListener((PowerZoneMesgListener) listener);
		mesgBroadcaster.addListener((RecordMesgListener) listener);
		mesgBroadcaster.addListener((ScheduleMesgListener) listener);
		mesgBroadcaster.addListener((SdmProfileMesgListener) listener);
		mesgBroadcaster.addListener((SessionMesgListener) listener);
		mesgBroadcaster.addListener((SoftwareMesgListener) listener);
		mesgBroadcaster.addListener((SportMesgListener) listener);
		mesgBroadcaster.addListener((TotalsMesgListener) listener);
		mesgBroadcaster.addListener((UserProfileMesgListener) listener);
		mesgBroadcaster.addListener((WeightScaleMesgListener) listener);
		mesgBroadcaster.addListener((WorkoutMesgListener) listener);
		mesgBroadcaster.addListener((WorkoutStepMesgListener) listener);
		mesgBroadcaster.addListener((ZonesTargetMesgListener) listener);
		
		mesgBroadcaster.run(inputStream);
		return listener.getGPSDocument();
	}
}