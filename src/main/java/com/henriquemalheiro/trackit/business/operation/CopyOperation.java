package com.henriquemalheiro.trackit.business.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.Event;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Track;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.decoratedlist.EventList;
import com.pg58406.trackit.business.domain.Pause;
import com.pg58406.trackit.business.domain.Picture;

public class CopyOperation extends OperationBase implements Operation {
	
	private Course course;
	
	public CopyOperation() {
		super();
		setUp();
	}
	
	public CopyOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}
	
	private void setUp() {
		course = (Course) options.get(Constants.CopyOperation.COURSE);
	}

	@Override
	public String getName() {
		return Constants.CopyOperation.NAME;
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		//Course course = document.getCourses().get(0);
		Course newCourse = copy(document);
		document.add(newCourse);
		document.setChangedTrue();
	}
	
	
	private Course copy(GPSDocument document) {
		
		Course newCourse = new Course();
		newCourse.setParent(document);
		String newCourseName = course.getName().concat(" 2");
		newCourse.setName(newCourseName);
		
		//newCourse.setName(course.getName());
		newCourse.setSport(course.getSport());
		newCourse.setSubSport(course.getSubSport());
		newCourse.setNotes(course.getNotes());
		newCourse.setElapsedTime(course.getElapsedTime());
		newCourse.setTimerTime(course.getTimerTime());
		newCourse.setMovingTime(course.getMovingTime());
		newCourse.setPausedTime(course.getPausedTime());
		newCourse.setDistance(course.getDistance());
		newCourse.setAverageSpeed(course.getAverageSpeed());
		newCourse.setAverageMovingSpeed(course.getAverageMovingSpeed());
		newCourse.setMaximumSpeed(course.getMaximumSpeed());
		newCourse.setAverageHeartRate(course.getAverageHeartRate());
		newCourse.setMinimumHeartRate(course.getMinimumHeartRate());
		newCourse.setMaximumHeartRate(course.getMaximumHeartRate());
		newCourse.setAverageCadence(course.getAverageCadence());
		newCourse.setMaximumCadence(course.getMaximumCadence());
		newCourse.setAverageRunningCadence(course.getAverageRunningCadence());
		newCourse.setMaximumRunningCadence(course.getMaximumRunningCadence());
		newCourse.setAveragePower(course.getAveragePower());
		newCourse.setMaximumPower(course.getMaximumPower());
		newCourse.setCalories(course.getCalories());
		newCourse.setFatCalories(course.getFatCalories());
		newCourse.setAverageTemperature(course.getAverageTemperature());
		newCourse.setMinimumTemperature(course.getMinimumTemperature());
		newCourse.setMaximumTemperature(course.getMaximumTemperature());
		newCourse.setTotalAscent(course.getTotalAscent());
		newCourse.setTotalDescent(course.getTotalDescent());
		newCourse.setAverageAltitude(course.getAverageAltitude());
		newCourse.setMinimumAltitude(course.getMinimumAltitude());
		newCourse.setMaximumAltitude(course.getMaximumAltitude());
		newCourse.setAverageGrade(course.getAverageGrade());
		newCourse.setAveragePositiveGrade(course.getAveragePositiveGrade());
		newCourse.setAverageNegativeGrade(course.getAverageNegativeGrade());
		newCourse.setMaximumPositiveGrade(course.getMaximumPositiveGrade());
		newCourse.setMaximumNegativeGrade(course.getMaximumNegativeGrade());
		newCourse.setAveragePositiveVerticalSpeed(course.getAveragePositiveVerticalSpeed());
		newCourse.setAverageNegativeVerticalSpeed(course.getAverageNegativeVerticalSpeed());
		newCourse.setMaximumPositiveVerticalSpeed(course.getMaximumPositiveVerticalSpeed());
		newCourse.setMaximumNegativeVerticalSpeed(course.getMaximumNegativeVerticalSpeed());
		newCourse.setStartLatitude(course.getStartLatitude());
		newCourse.setStartLongitude(course.getStartLongitude());
		newCourse.setStartAltitude(course.getStartAltitude());
		newCourse.setEndLatitude(course.getEndLatitude());
		newCourse.setEndLongitude(course.getEndLongitude());
		newCourse.setEndAltitude(course.getEndAltitude());
		newCourse.setNortheastLatitude(course.getNortheastLatitude());
		newCourse.setNortheastLongitude(course.getNortheastLongitude());
		newCourse.setSouthwestLatitude(course.getSouthwestLatitude());
		newCourse.setSouthwestLongitude(course.getSouthwestLongitude());
		//newCourse.setParent(course.getParent());
		newCourse.setUnsavedChanges(course.getUnsavedChanges());
		newCourse.setFilepath(course.getFilepath());
		newCourse.setCreator(course.getCreator());
		newCourse.setColorSchemeV2(course.getColorSchemeV2());
		newCourse.setNoSpeedInFile(course.getNoSpeedInFile());
		
		List<Lap> newLaps = new ArrayList<Lap>();
		newLaps.addAll(course.getLaps());
		newCourse.setLaps(newLaps);
		
		List<Track> newTracks = new ArrayList<Track>();
		newTracks.addAll(course.getTracks());
		newCourse.setTracks(newTracks);
		
		EventList<Trackpoint> newTrackpoints = new EventList<Trackpoint>(new ArrayList<Trackpoint>());
		newTrackpoints.addAll(course.getTrackpoints());
		newCourse.setTrackpoints(newTrackpoints);
		
		EventList<CoursePoint> newCoursePoints = new EventList<CoursePoint>(new ArrayList<CoursePoint>());
		newCoursePoints.addAll(course.getCoursePoints());
		newCourse.setCoursePoints(newCoursePoints);
		
		List<TrackSegment> newSegments = new ArrayList<TrackSegment>();
		newSegments.addAll(course.getSegments());
		newCourse.setSegments(newSegments);
		
		List<Event> newEvents = new ArrayList<Event>();
		newEvents.addAll(course.getEvents());
		newCourse.setEvents(newEvents);
		
		List<Pause> newPauses = new ArrayList<Pause>();
		newPauses.addAll(course.getPauses());
		newCourse.setPauses(newPauses);

		List<Picture> newPictures = new ArrayList<Picture>();
		newPictures.addAll(course.getPictures());
		newCourse.setPictures(newPictures);
		
		return newCourse;
	
	}


	@Override
	public void process(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub
		
	}

}
