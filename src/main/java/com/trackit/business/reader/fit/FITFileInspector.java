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
package com.trackit.business.reader.fit;

import static com.trackit.business.common.Messages.getMessage;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import com.garmin.fit.ActivityMesg;
import com.garmin.fit.ActivityMesgListener;
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
import com.garmin.fit.Decode;
import com.garmin.fit.DeviceInfoMesg;
import com.garmin.fit.DeviceInfoMesgListener;
import com.garmin.fit.DeviceSettingsMesg;
import com.garmin.fit.DeviceSettingsMesgListener;
import com.garmin.fit.EventMesg;
import com.garmin.fit.EventMesgListener;
import com.garmin.fit.FieldCapabilitiesMesg;
import com.garmin.fit.FieldCapabilitiesMesgListener;
import com.garmin.fit.FileCapabilitiesMesg;
import com.garmin.fit.FileCapabilitiesMesgListener;
import com.garmin.fit.FileCreatorMesg;
import com.garmin.fit.FileCreatorMesgListener;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.FileIdMesgListener;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.GoalMesg;
import com.garmin.fit.GoalMesgListener;
import com.garmin.fit.HrZoneMesg;
import com.garmin.fit.HrZoneMesgListener;
import com.garmin.fit.HrmProfileMesg;
import com.garmin.fit.HrmProfileMesgListener;
import com.garmin.fit.HrvMesg;
import com.garmin.fit.HrvMesgListener;
import com.garmin.fit.LapMesg;
import com.garmin.fit.LapMesgListener;
import com.garmin.fit.LengthMesg;
import com.garmin.fit.LengthMesgListener;
import com.garmin.fit.Mesg;
import com.garmin.fit.MesgBroadcaster;
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
import com.garmin.fit.SoftwareMesg;
import com.garmin.fit.SoftwareMesgListener;
import com.garmin.fit.SportMesg;
import com.garmin.fit.SportMesgListener;
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
import com.trackit.business.common.Formatters;

public class FITFileInspector {
	private static Set<String> usedMessages = new LinkedHashSet<String>();
	
	private static class Listener implements ActivityMesgListener, BikeProfileMesgListener, BloodPressureMesgListener,
			BufferedRecordMesgListener, CapabilitiesMesgListener, CourseMesgListener, CoursePointMesgListener,
			DeviceInfoMesgListener, DeviceSettingsMesgListener, EventMesgListener, FieldCapabilitiesMesgListener,
			FileCapabilitiesMesgListener, FileCreatorMesgListener, FileIdMesgListener, GoalMesgListener,
			HrZoneMesgListener, HrmProfileMesgListener, HrvMesgListener, LapMesgListener, LengthMesgListener,
			MesgCapabilitiesMesgListener, MesgListener, MesgWithEventListener, MetZoneMesgListener,
			MonitoringInfoMesgListener, MonitoringMesgListener,	PadMesgListener, PowerZoneMesgListener, RecordMesgListener,
			ScheduleMesgListener, SdmProfileMesgListener, SessionMesgListener, SoftwareMesgListener, SportMesgListener,
			TotalsMesgListener, UserProfileMesgListener, WeightScaleMesgListener, WorkoutMesgListener,
			WorkoutStepMesgListener, ZonesTargetMesgListener {
		
		public void onMesg(FileIdMesg mesg) {
			usedMessages.add("FileIdMesg");
			System.out.println(getMessage("fitFileInspector.message", "FileIdMesg"));
		}

		public void onMesg(UserProfileMesg mesg) {
			usedMessages.add("UserProfileMesg");
			System.out.println(getMessage("fitFileInspector.message", "UserProfileMesg"));
		}

		public void onMesg(ZonesTargetMesg mesg) {
			usedMessages.add("ZonesTargetMesg");
			System.out.println(getMessage("fitFileInspector.message", "ZonesTargetMesg"));
		}

		public void onMesg(WorkoutStepMesg mesg) {
			usedMessages.add("WorkoutStepMesg");
			System.out.println(getMessage("fitFileInspector.message", "WorkoutStepMesg"));
		}

		public void onMesg(WorkoutMesg mesg) {
			usedMessages.add("WorkoutMesg");
			System.out.println(getMessage("fitFileInspector.message", "WorkoutMesg"));
		}

		public void onMesg(WeightScaleMesg mesg) {
			usedMessages.add("WeightScaleMesg");
			System.out.println(getMessage("fitFileInspector.message", "WeightScaleMesg"));
		}

		public void onMesg(TotalsMesg mesg) {
			usedMessages.add("TotalsMesg");
			System.out.println(getMessage("fitFileInspector.message", "TotalsMesg"));
		}

		public void onMesg(SportMesg mesg) {
			usedMessages.add("SportMesg");
			System.out.println(getMessage("fitFileInspector.message", "SportMesg"));
		}

		public void onMesg(SoftwareMesg mesg) {
			usedMessages.add("SoftwareMesg");
			System.out.println(getMessage("fitFileInspector.message", "SoftwareMesg"));
		}

		public void onMesg(SessionMesg mesg) {
			usedMessages.add("SessionMesg");
			System.out.println(getMessage("fitFileInspector.message", "SessionMesg"));
		}

		public void onMesg(SdmProfileMesg mesg) {
			usedMessages.add("SdmProfileMesg");
			System.out.println(getMessage("fitFileInspector.message", "SdmProfileMesg"));
		}

		public void onMesg(ScheduleMesg mesg) {
			usedMessages.add("ScheduleMesg");
			System.out.println(getMessage("fitFileInspector.message", "ScheduleMesg"));
		}

		public void onMesg(RecordMesg mesg) {
			usedMessages.add("RecordMesg");
		}

		public void onMesg(PowerZoneMesg mesg) {
			usedMessages.add("PowerZoneMesg");
			System.out.println(getMessage("fitFileInspector.message", "PowerZoneMesg"));
		}

		public void onMesg(PadMesg mesg) {
			usedMessages.add("PadMesg");
			System.out.println(getMessage("fitFileInspector.message", "PadMesg"));
		}

		public void onMesg(MonitoringMesg mesg) {
			usedMessages.add("MonitoringMesg");
			System.out.println(getMessage("fitFileInspector.message", "MonitoringMesg"));
		}

		public void onMesg(MonitoringInfoMesg mesg) {
			usedMessages.add("MonitoringInfoMesg");
			System.out.println(getMessage("fitFileInspector.message", "MonitoringInfoMesg"));
		}

		public void onMesg(MetZoneMesg mesg) {
			usedMessages.add("MetZoneMesg");
			System.out.println(getMessage("fitFileInspector.message", "MetZoneMesg"));
		}

		public void onMesg(MesgWithEvent message) {
			usedMessages.add("MesgWithEvent");
		}

		public void onMesg(Mesg mesg) {
			usedMessages.add("Mesg");
		}

		public void onMesg(MesgCapabilitiesMesg mesg) {
			usedMessages.add("MesgCapabilitiesMesg");
			System.out.println(getMessage("fitFileInspector.message", "MesgCapabilitiesMesg"));
		}

		public void onMesg(LengthMesg mesg) {
			usedMessages.add("LengthMesg");
			System.out.println(getMessage("fitFileInspector.message", "LengthMesg"));
		}

		public void onMesg(LapMesg mesg) {
			usedMessages.add("LapMesg");
			System.out.println(getMessage("fitFileInspector.message", "LapMesg"));
		}

		public void onMesg(HrvMesg mesg) {
			usedMessages.add("HrvMesg");
			System.out.println(getMessage("fitFileInspector.message", "HrvMesg"));
		}

		public void onMesg(HrmProfileMesg mesg) {
			usedMessages.add("HrmProfileMesg");
			System.out.println(getMessage("fitFileInspector.message", "HrmProfileMesg"));
		}

		public void onMesg(HrZoneMesg mesg) {
			usedMessages.add("HrZoneMesg");
			System.out.println(getMessage("fitFileInspector.message", "HrZoneMesg"));
		}

		public void onMesg(GoalMesg mesg) {
			usedMessages.add("GoalMesg");
			System.out.println(getMessage("fitFileInspector.message", "GoalMesg"));
		}

		public void onMesg(FileCreatorMesg mesg) {
			usedMessages.add("FileCreatorMesg");
			System.out.println(getMessage("fitFileInspector.message", "FileCreatorMesg"));
		}

		public void onMesg(FileCapabilitiesMesg mesg) {
			usedMessages.add("FileCapabilitiesMesg");
			System.out.println(getMessage("fitFileInspector.message", "FileCapabilitiesMesg"));
		}

		public void onMesg(FieldCapabilitiesMesg mesg) {
			usedMessages.add("FieldCapabilitiesMesg");
			System.out.println(getMessage("fitFileInspector.message", "FieldCapabilitiesMesg"));
		}

		public void onMesg(EventMesg mesg) {
			usedMessages.add("EventMesg");
			System.out.println(getMessage("fitFileInspector.message", "EventMesg"));
			System.out.println("  Timestamp: " + Formatters.getSimpleDateFormatMilis().format(mesg.getTimestamp().getDate()));
			System.out.println("  Data: " + mesg.getData());
			System.out.println("  Event: " + mesg.getEvent().toString());
			System.out.println("  Event type: " + mesg.getEventType().toString());
			System.out.println("  Event group: " + mesg.getEventGroup());
		}

		public void onMesg(DeviceSettingsMesg mesg) {
			usedMessages.add("DeviceSettingsMesg");
			System.out.println(getMessage("fitFileInspector.message", "DeviceSettingsMesg"));
		}

		public void onMesg(DeviceInfoMesg mesg) {
			usedMessages.add("DeviceInfoMesg");
			System.out.println(getMessage("fitFileInspector.message", "DeviceInfoMesg"));
		}

		public void onMesg(CoursePointMesg mesg) {
			usedMessages.add("CoursePointMesg");
			System.out.println(getMessage("fitFileInspector.message", "CoursePointMesg"));
		}

		public void onMesg(CourseMesg mesg) {
			usedMessages.add("CourseMesg");
			System.out.println(getMessage("fitFileInspector.message", "CourseMesg"));
		}

		public void onMesg(CapabilitiesMesg mesg) {
			usedMessages.add("CapabilitiesMesg");
			System.out.println(getMessage("fitFileInspector.message", "CapabilitiesMesg"));
		}

		public void onMesg(BufferedRecordMesg message) {
			usedMessages.add("BufferedRecordMesg");
		}

		public void onMesg(BloodPressureMesg mesg) {
			usedMessages.add("BloodPressureMesg");
			System.out.println(getMessage("fitFileInspector.message", "BloodPressureMesg"));
		}

		public void onMesg(BikeProfileMesg mesg) {
			usedMessages.add("BikeProfileMesg");
			System.out.println(getMessage("fitFileInspector.message", "BikeProfileMesg"));
		}

		public void onMesg(ActivityMesg mesg) {
			usedMessages.add("ActivityMesg");
			System.out.println(getMessage("fitFileInspector.message", "ActivityMesg"));
		}
	}

	public static void main(String[] args) {
		Decode decode = new Decode();
		MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
		Listener listener = new Listener();
		FileInputStream in;

		System.out.println("FIT Decode Example Application");

		if (args.length != 1) {
			System.out.println("Usage: java -jar DecodeExample.jar <filename>");
			return;
		}

		try {
			in = new FileInputStream(args[0]);
		} catch (java.io.IOException e) {
			throw new RuntimeException("Error opening file " + args[0] + " [1]");
		}

		try {
			if (!Decode.checkIntegrity((InputStream) in))
				throw new RuntimeException("FIT file integrity failed.");
		} finally {
			try {
				in.close();
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			in = new FileInputStream(args[0]);
		} catch (java.io.IOException e) {
			throw new RuntimeException("Error opening file " + args[0] + " [2]");
		}

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

		try {
			mesgBroadcaster.run(in);
		} catch (FitRuntimeException e) {
			System.err.print(getMessage("fitFileInspector.errorDecodingFile"));
			System.err.println(e.getMessage());

			try {
				in.close();
			} catch (java.io.IOException f) {
				throw new RuntimeException(f);
			}

			return;
		}

		try {
			in.close();
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		
		System.out.println();
		System.out.println(getMessage("fitFileInspector.usedMessages"));
		for (String messageType : usedMessages) {
			System.out.println(messageType);
		}

		System.out.println(getMessage("fitFileInspector.decodeComplete", args[0]));
	}
}
