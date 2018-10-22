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
package com.trackit.business.writer.fit;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.garmin.fit.ActivityMesg;
import com.garmin.fit.CourseMesg;
import com.garmin.fit.CoursePointMesg;
import com.garmin.fit.DateTime;
import com.garmin.fit.Event;
import com.garmin.fit.EventMesg;
import com.garmin.fit.EventType;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.Intensity;
import com.garmin.fit.LapMesg;
import com.garmin.fit.LapTrigger;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionTrigger;
import com.garmin.fit.Sport;
import com.garmin.fit.SubSport;
import com.trackit.business.common.Constants;
import com.trackit.business.common.FileType;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.WriterException;
import com.trackit.business.utilities.Utilities;
import com.trackit.business.utilities.StringUtilities;
import com.trackit.business.writer.Writer;
import com.trackit.business.writer.WriterTemplate;

public class FITFileWriter extends WriterTemplate implements Writer {
	private boolean writeActivities;
	private boolean writeCourses;
	private String outputDir;
	
	private static Logger logger = Logger.getLogger(FITFileWriter.class.getName());

	public FITFileWriter() {
		this(new HashMap<String, Object>());
	}
	
	public FITFileWriter(Map<String, Object> options) {
		super(options);
		setUp();
	}
	
	private void setUp() {
		writeActivities = (Boolean) getOptions().get(Constants.Writer.WRITE_ACTIVITIES);
		writeCourses = (Boolean) getOptions().get(Constants.Writer.WRITE_COURSES);
		outputDir = (String) getOptions().get(Constants.Writer.OUTPUT_DIR);
	}
	
	@Override
	public void write(List<GPSDocument> documents) throws WriterException {
		throw new UnsupportedOperationException("FitFileWriter does not support multiple documents.");
	}
	
	@Override
	public void write(GPSDocument document) throws WriterException {
		if (outputDir == null) {
			logger.error("Output directory required.");
			throw new WriterException("Output directory required.");
		}
		outputDir += (!outputDir.endsWith(File.separator) ? File.separator : "");
		
		if (writeActivities) {
			String activityFilename;
			for (Activity activity : document.getActivities()) {
				activityFilename = String.format("%s%s.%s", outputDir, activity.getName(), FileType.FIT.getExtension());
				activityFilename = activityFilename.replace(":", "-");
				writeFitFile(activity, activityFilename);
			}
		}
		
		if (writeCourses) {
			String courseFilename;
			for (Course course : document.getCourses()) {
				courseFilename = String.format("%s%s_course.%s", outputDir, course.getName(), FileType.FIT.getExtension());
				writeFitFile(course, courseFilename);
			}
		}
	}
	
	private void writeFitFile(Course course, String filename) throws FITWriterException {
		FileEncoder encoder = null;
		File file = new File(filename);
		file.mkdirs();
		
		try {
			encoder = new FileEncoder(file);
		} catch (FitRuntimeException e) {
			logger.error("Error opening file test.fit");
			throw new FITWriterException(e.getMessage());
		}
		
		int fileIdLocalMesg = 0;
		int courseLocalMesg = 1;
		int lapLocalMesg = 2;
		int eventLocalMesg = 3;
		int recordLocalMesg = 4;
		int coursePointLocalMesg = 5;

		writeFileIdMesg(encoder, fileIdLocalMesg, course);
		
		writeCourseMesg(encoder, courseLocalMesg, course);
		
		writeLapMesg(encoder, lapLocalMesg, course.getLaps());

		writeEventMesg(encoder, eventLocalMesg, course.getTrackpoints(), Event.TIMER, EventType.START);
		
		writeRecordMesg(encoder, recordLocalMesg, course.getTrackpoints(), false);
		
		writeCoursePointMesg(encoder, coursePointLocalMesg, course.getCoursePoints());
		
		writeEventMesg(encoder, eventLocalMesg, course.getTrackpoints(), Event.TIMER, EventType.STOP_ALL);
		
		try {
			encoder.close();
		} catch(FitRuntimeException e) {
			logger.error("Error closing encode.");
			throw new FITWriterException(e.getMessage());
		}
	}
	
	private void writeEventMesg(FileEncoder encoder, int localMesgIndex, List<Trackpoint> trackpoints, Event event, EventType eventType) {
		switch (eventType) {
		case START:
			if (trackpoints != null && trackpoints.size() > 0) {
				EventMesg eventMesg = new EventMesg();
				eventMesg.setLocalNum(localMesgIndex);
				
				Trackpoint firstTrackpoint = trackpoints.get(0);
				if (firstTrackpoint != null && firstTrackpoint.getTimestamp() != null) {
					eventMesg.setTimestamp(new DateTime(firstTrackpoint.getTimestamp()));
				}
				
				eventMesg.setEvent(Event.TIMER);
				eventMesg.setEventType(EventType.START);
				
				encoder.write(eventMesg);
			}
			break;
			
		case STOP_ALL:
			if (trackpoints != null && trackpoints.size() > 0) {
				EventMesg eventMesg = new EventMesg();
				eventMesg.setLocalNum(localMesgIndex);
				
				Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);
				if (lastTrackpoint != null && lastTrackpoint.getTimestamp() != null) {
					eventMesg.setTimestamp(new DateTime(lastTrackpoint.getTimestamp()));
				}
				
				eventMesg.setEvent(Event.TIMER);
				eventMesg.setEventType(EventType.STOP_ALL);
				
				encoder.write(eventMesg);
			}
			break;
			
		default:
		}
	}

	private void writeFitFile(Activity activity, String filename) throws FITWriterException {
		FileEncoder encoder = null;
		File file = new File(filename);
		int messageIndex = 0;
		
		try {
			encoder = new FileEncoder(file);
		} catch (FitRuntimeException e) {
			logger.error("Error opening fit file: " + filename);
			throw new FITWriterException(e.getMessage());
		}
		
		int fileIdLocalMesg = 0;
		int eventLocalMesg = 1;
		int recordLocalMesg = 2;
		int lapLocalMesg = 3;
		int sessionLocalMesg = 4;
		int activityLocalMesg = 5;

		writeFileIdMesg(encoder, fileIdLocalMesg, activity);
		
		writeEventMesg(encoder, eventLocalMesg, activity.getTrackpoints(), Event.TIMER, EventType.START);
		
		for (Session session : activity.getSessions()) {
			for (Lap lap : session.getLaps()) {
				writeRecordMesg(encoder, recordLocalMesg, lap.getTrackpoints(), true);
				writeLapMesg(encoder, lapLocalMesg, lap, messageIndex++);
			}
	
			writeEventMesg(encoder, eventLocalMesg, activity.getTrackpoints(), Event.TIMER, EventType.STOP_ALL);
	
			writeSessionMesg(encoder, sessionLocalMesg, session, messageIndex++);
		}

		writeActivityMesg(encoder, activityLocalMesg, activity);
		
		try {
			encoder.close();
		} catch(FitRuntimeException e) {
			logger.error("Error closing encoder.");
			throw new FITWriterException(e.getMessage());
		}
	}
	
	private void writeFileIdMesg(FileEncoder encoder, int localMesgIndex, Course course) {
		FileIdMesg fileIdMesg = new FileIdMesg();
		fileIdMesg.setLocalNum(localMesgIndex);
		
		fileIdMesg.setManufacturer(255);
//		fileIdMesg.setManufacturer(1);
		fileIdMesg.setProduct(0);
//		fileIdMesg.setGarminProduct(1169);
		fileIdMesg.setSerialNumber(1L);
//		fileIdMesg.setSerialNumber(3819576796L);
		fileIdMesg.setType(com.garmin.fit.File.COURSE);
		fileIdMesg.setTimeCreated(new DateTime(System.currentTimeMillis()));
//		fileIdMesg.setTimeCreated(new DateTime(34178L));
		
		encoder.write(fileIdMesg);
	}
	
	private void writeFileIdMesg(FileEncoder encoder, int localMesgIndex, Activity activity) {
		FileIdMesg fileIdMesg = new FileIdMesg();
		fileIdMesg.setLocalNum(localMesgIndex);
		
		fileIdMesg.setManufacturer(255);
		fileIdMesg.setProduct(0);
		fileIdMesg.setSerialNumber(1L);
		fileIdMesg.setType(com.garmin.fit.File.ACTIVITY);
		fileIdMesg.setTimeCreated(new DateTime(System.currentTimeMillis()));
		
		encoder.write(fileIdMesg);
	}
	
	private void writeLapMesg(FileEncoder encoder, int localMesgIndex, List<Lap> laps) {
		int messageIndex = 0;
		
		for (Lap lap : laps) {
			writeLapMesg(encoder, localMesgIndex, lap, messageIndex++);
		}
	}
	
	private void writeLapMesg(FileEncoder encoder, int localMesgIndex, Lap lap, int messageIndex) {
		LapMesg lapMesg = new LapMesg();
		lapMesg.setLocalNum(localMesgIndex);
		
		if (lap.getSport() != null) {
			lapMesg.setSport(Sport.valueOf(lap.getSport().name()));
		}
		
		if (lap.getTrigger() != null) {
			lapMesg.setLapTrigger(LapTrigger.valueOf(lap.getTrigger().name()));
		} else {
			lapMesg.setLapTrigger(LapTrigger.MANUAL);
		}
		
		lapMesg.setMessageIndex(messageIndex);
		
		if (lap.getIntensity() != null) {
			lapMesg.setIntensity(Intensity.valueOf(lap.getIntensity().name()));
		} else {
			lapMesg.setIntensity(Intensity.ACTIVE);
		}
		
		if (lap.getStartTime() != null) {
			lapMesg.setStartTime(new DateTime(lap.getStartTime()));
		} else {
			Trackpoint firstTrackpoint = lap.getFirstTrackpoint();
			if (firstTrackpoint != null && firstTrackpoint.getTimestamp() != null) {
				lapMesg.setStartTime(new DateTime(firstTrackpoint.getTimestamp()));
			}
		}
		
		if (lap.getEndTime() != null) {
			lapMesg.setTimestamp(new DateTime(lap.getEndTime()));
		} else {
			Trackpoint lastTrackpoint = lap.getLastTrackpoint();
			if (lastTrackpoint != null && lastTrackpoint.getTimestamp() != null) {
				lapMesg.setTimestamp(new DateTime(lastTrackpoint.getTimestamp()));
			}
		}
		
		if (lap.getElapsedTime() != null) {
			lapMesg.setTotalElapsedTime(lap.getElapsedTime().floatValue());
		}
		
		if (lap.getTimerTime() != null) {
			lapMesg.setTotalTimerTime(lap.getTimerTime().floatValue());
		}
		
		if (lap.getDistance() != null) {
			lapMesg.setTotalDistance(lap.getDistance().floatValue());
		}
		
		if (lap.getStartLatitude() != null) {
			lapMesg.setStartPositionLat(Utilities.degreesToSemicircles(lap.getStartLatitude()));
		} else {
			lapMesg.setStartPositionLat(Utilities.degreesToSemicircles(lap.getFirstTrackpoint().getLatitude()));
		}
		
		if (lap.getStartLongitude() != null) {
			lapMesg.setStartPositionLong(Utilities.degreesToSemicircles(lap.getStartLongitude()));
		} else {
			lapMesg.setStartPositionLong(Utilities.degreesToSemicircles(lap.getFirstTrackpoint().getLongitude()));
		}
		
		if (lap.getEndLatitude() != null) {
			lapMesg.setEndPositionLat(Utilities.degreesToSemicircles(lap.getEndLatitude()));
		} else {
			lapMesg.setEndPositionLat(Utilities.degreesToSemicircles(lap.getLastTrackpoint().getLatitude()));
		}
		
		if (lap.getEndLongitude() != null) {
			lapMesg.setEndPositionLong(Utilities.degreesToSemicircles(lap.getEndLongitude()));
		} else {
			lapMesg.setEndPositionLong(Utilities.degreesToSemicircles(lap.getLastTrackpoint().getLongitude()));
		}
		
		if (lap.getAverageSpeed() != null) {
			lapMesg.setAvgSpeed(lap.getAverageSpeed().floatValue());
		}
		
		if (lap.getAverageHeartRate() != null) {
			lapMesg.setAvgHeartRate(lap.getAverageHeartRate());
		}
		
		if (lap.getAverageCadence() != null) {
			lapMesg.setAvgCadence(lap.getAverageCadence());
		}
		
		if (lap.getMaximumSpeed() != null) {
			lapMesg.setMaxSpeed(lap.getMaximumSpeed().floatValue());
		}
		
		if (lap.getMinimumHeartRate() != null) {
			lapMesg.setMinHeartRate(lap.getMinimumHeartRate());
		}
		
		if (lap.getMaximumHeartRate() != null) {
			lapMesg.setMaxHeartRate(lap.getMaximumHeartRate());
		}
		
		if (lap.getMaximumCadence() != null) {
			lapMesg.setMaxCadence(lap.getMaximumCadence());
		}
		
		if (lap.getTotalAscent() != null) {
			lapMesg.setTotalAscent(lap.getTotalAscent());
		}
		
		if (lap.getTotalDescent() != null) {
			lapMesg.setTotalDescent(lap.getTotalDescent());
		}
		
		if (lap.getCalories() != null) {
			lapMesg.setTotalCalories(lap.getCalories());
		}
		
		encoder.write(lapMesg);
	}
	
	private void writeRecordMesg(FileEncoder encoder, int localMesgIndex, List<Trackpoint> trackpoints, boolean extendedData) {
		RecordMesg recordMesg = null;
		
		for (Trackpoint trackpoint : trackpoints) {
			recordMesg = new RecordMesg();
			recordMesg.setLocalNum(localMesgIndex);
			
			if (trackpoint.getTimestamp() != null) {
				recordMesg.setTimestamp(new DateTime(trackpoint.getTimestamp()));
			}
			
			if (trackpoint.getDistance() != null) {
				recordMesg.setDistance(trackpoint.getDistance().floatValue());
			}
			
			if (trackpoint.getLatitude() != null) {
				recordMesg.setPositionLat(Utilities.degreesToSemicircles(trackpoint.getLatitude()));
			}
			
			if (trackpoint.getLongitude() != null) {
				recordMesg.setPositionLong(Utilities.degreesToSemicircles(trackpoint.getLongitude()));
			}
			
			if (trackpoint.getAltitude() != null) {
				recordMesg.setAltitude(trackpoint.getAltitude().floatValue());
			}
			
			if (trackpoint.getSpeed() != null && extendedData) {
				recordMesg.setSpeed(trackpoint.getSpeed().floatValue());
			}
			
			if (trackpoint.getTemperature() != null && extendedData) {
				recordMesg.setTemperature(trackpoint.getTemperature().byteValue());
			}
			
			if (trackpoint.getHeartRate() != null && extendedData) {
				recordMesg.setHeartRate(trackpoint.getHeartRate());
			}
			
			if (trackpoint.getCadence() != null && extendedData) {
				recordMesg.setCadence(trackpoint.getCadence());
			}
			
			if (trackpoint.getTotalCycles() != null && extendedData) {
				recordMesg.setTotalCycles(trackpoint.getTotalCycles());
			}
			
			encoder.write(recordMesg);
		}
	}
	
	private void writeCoursePointMesg(FileEncoder encoder, int localMesgIndex, List<CoursePoint> coursePoints) {
		CoursePointMesg coursePointMesg = null;
		
		int index = 0;
		for (CoursePoint coursePoint : coursePoints) {
			coursePointMesg = new CoursePointMesg();
			coursePointMesg.setLocalNum(localMesgIndex);
			coursePointMesg.setMessageIndex(index++);
			
			coursePointMesg.setDistance(coursePoint.getTrackpoint().getDistance().floatValue());
			coursePointMesg.setPositionLat(Utilities.degreesToSemicircles(coursePoint.getTrackpoint().getLatitude()));
			coursePointMesg.setPositionLong(Utilities.degreesToSemicircles(coursePoint.getTrackpoint().getLongitude()));
			coursePointMesg.setTimestamp(new DateTime(coursePoint.getTrackpoint().getTimestamp()));
			coursePointMesg.setName( StringUtilities.pad(coursePoint.getName(), ' ', 10, StringUtilities.RIGHT_PAD));
			coursePointMesg.setType(com.garmin.fit.CoursePoint.valueOf(coursePoint.getType().name()));
			
			encoder.write(coursePointMesg);
		}
	}
	
	private void writeCourseMesg(FileEncoder encoder, int localMesgIndex, Course course) {
		CourseMesg courseMesg = new CourseMesg();
		courseMesg.setLocalNum(localMesgIndex);
		
		courseMesg.setName(StringUtilities.pad(course.getName(), ' ', 16, StringUtilities.RIGHT_PAD));
		courseMesg.setSport(Sport.CYCLING);
//		courseMesg.setCapabilities(771L);
		
		encoder.write(courseMesg);
	}
	
	private void writeSessionMesg(FileEncoder encoder, int localMesgIndex, Session session, int messageIndex) {
		SessionMesg sessionMesg = new SessionMesg();
		sessionMesg.setLocalNum(localMesgIndex);
		
		sessionMesg.setTimestamp(new DateTime(session.getTrackpoints().get(session.getTrackpoints().size() - 1).getTimestamp()));
		sessionMesg.setStartTime(new DateTime(session.getTrackpoints().get(0).getTimestamp()));
		
		if (session.getStartLatitude() != null) {
			sessionMesg.setStartPositionLat(Utilities.degreesToSemicircles(session.getStartLatitude()));
		} else {
			sessionMesg.setStartPositionLat(Utilities.degreesToSemicircles(session.getFirstTrackpoint().getLatitude()));
		}
		
		if (session.getStartLongitude() != null) {
			sessionMesg.setStartPositionLong(Utilities.degreesToSemicircles(session.getStartLongitude()));
		} else {
			sessionMesg.setStartPositionLong(Utilities.degreesToSemicircles(session.getFirstTrackpoint().getLongitude()));
		}
		
		if (session.getElapsedTime() != null) {
			sessionMesg.setTotalElapsedTime(session.getElapsedTime().floatValue());
		}

		if (session.getTimerTime() != null) {
			sessionMesg.setTotalTimerTime(session.getTimerTime().floatValue());
		}
		
		if (session.getDistance() != null) {
			sessionMesg.setTotalDistance(session.getDistance().floatValue());
		}
		
		if (session.getCycles() != null) {
			sessionMesg.setTotalCycles(session.getCycles());
		}
		
		if (session.getNortheastLatitude() != null) {
			sessionMesg.setNecLat(Utilities.degreesToSemicircles(session.getNortheastLatitude()));
		}
		
		if (session.getNortheastLongitude() != null) {
			sessionMesg.setNecLong(Utilities.degreesToSemicircles(session.getNortheastLongitude()));
		}
		
		if (session.getSouthwestLatitude() != null) {
			sessionMesg.setSwcLat(Utilities.degreesToSemicircles(session.getSouthwestLatitude()));
		}
		
		if (session.getSouthwestLongitude() != null) {
			sessionMesg.setSwcLong(Utilities.degreesToSemicircles(session.getSouthwestLongitude()));
		}
		
		sessionMesg.setMessageIndex(messageIndex);
		
		if (session.getCalories() != null) {
			sessionMesg.setTotalCalories(session.getCalories());
		}
		
		if (session.getAverageSpeed() != null) {
			sessionMesg.setAvgSpeed(session.getAverageSpeed().floatValue());
		}

		if (session.getMaximumSpeed() != null) {
			sessionMesg.setMaxSpeed(session.getMaximumSpeed().floatValue());
		}
		
		if (session.getTotalAscent() != null) {
			sessionMesg.setTotalAscent(session.getTotalAscent());
		}
		
		if (session.getTotalDescent() != null) {
			sessionMesg.setTotalDescent(session.getTotalDescent());
		}
		
		sessionMesg.setFirstLapIndex(0);
		
		sessionMesg.setNumLaps(session.getLaps().size());
		
		sessionMesg.setEvent(Event.SESSION);

		sessionMesg.setEventType(EventType.STOP);
		
		sessionMesg.setSport(Sport.CYCLING);
		
		sessionMesg.setSubSport(SubSport.ROAD);
		
		if (session.getAverageHeartRate() != null) {
			sessionMesg.setAvgHeartRate(session.getAverageHeartRate());
		}
		
		if (session.getMaximumHeartRate() != null) {
			sessionMesg.setMaxHeartRate(session.getMaximumHeartRate());
		}
		
		if (session.getAverageCadence() != null) {
			sessionMesg.setAvgCadence(session.getAverageCadence());
		}
		
		if (session.getMaximumCadence() != null) {
			sessionMesg.setMaxCadence(session.getMaximumCadence());
		}
		
		sessionMesg.setTrigger(SessionTrigger.ACTIVITY_END);
		
		encoder.write(sessionMesg);
	}

	private void writeActivityMesg(FileEncoder encoder, int localMesgIndex, Activity activity) {
		ActivityMesg activityMesg = new ActivityMesg();
		activityMesg.setLocalNum(localMesgIndex);
		activityMesg.setTimestamp(new DateTime(activity.getLastTrackpoint().getTimestamp()));
		activityMesg.setTotalTimerTime(activity.getTotalTimerTime().floatValue());
		activityMesg.setNumSessions(activity.getSessions().size());
		activityMesg.setType(com.garmin.fit.Activity.MANUAL);
		activityMesg.setEvent(Event.ACTIVITY);
		activityMesg.setEventType(EventType.STOP);
		
		encoder.write(activityMesg);
	}
}
