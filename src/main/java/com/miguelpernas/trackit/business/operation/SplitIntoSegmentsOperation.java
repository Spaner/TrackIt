package com.miguelpernas.trackit.business.operation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.derby.impl.store.raw.data.SetReservedSpaceOperation;

import com.henriquemalheiro.trackit.business.common.ColorScheme;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CourseLap;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.business.operation.Operation;
import com.henriquemalheiro.trackit.business.operation.OperationBase;
import com.pg58406.trackit.business.common.ColorSchemeV2;

public class SplitIntoSegmentsOperation extends OperationBase implements Operation {
	private Course course;
	private TrackSegment segment;
	private double value;

	public SplitIntoSegmentsOperation() {
		super();
		// setUp();
	}

	public SplitIntoSegmentsOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		course = (Course) options.get(Constants.SplitIntoSegmentsOperation.COURSE);
		if (!((Double) options.get(Constants.SplitIntoSegmentsOperation.VALUE) == null)) {
			value = (Double) options.get(Constants.SplitIntoSegmentsOperation.VALUE);
		}
		if (!((TrackSegment) options.get(Constants.SplitIntoSegmentsOperation.SEGMENT) == null)) {
			segment = (TrackSegment) options.get(Constants.SplitIntoSegmentsOperation.SEGMENT);
		}
	}

	@Override
	public String getName() {
		return Constants.SplitIntoSegmentsOperation.NAME;
	}

	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		// Do nothing
	}

	public void switchColors(List<Course> courses) {
		int min = 0;
		int max = 255;
		int red = new Random().nextInt(max - min + 1) + min;
		int green = new Random().nextInt(max - min + 1) + min;
		int blue = new Random().nextInt(max - min + 1) + min;
		int alpha = max;
		List<Color> colorsUsed = new ArrayList<Color>();
		Color color;
		for (Course course : courses) {
			color = new Color(red, green, blue, alpha);
			if (colorsUsed.contains(color)) {

			}
			if (color != null) {
				int tempRed = color.getRed();
				int tempGreen = color.getGreen();
				int tempBlue = color.getBlue();
				Color selectionFill = new Color(255 - tempRed, 255 - tempGreen, 255 - tempBlue);

				ColorSchemeV2 colorScheme = new ColorSchemeV2(color, color.darker(), selectionFill.darker(),
						selectionFill);
				course.setColorSchemeV2(colorScheme);

				red = new Random().nextInt(max - min + 1) + min;
				green = new Random().nextInt(max - min + 1) + min;
				blue = new Random().nextInt(max - min + 1) + min;

			}
		}
	}

	public void processNumber(GPSDocument document) throws TrackItException {
		List<Course> segmentList = new ArrayList<Course>();
		segmentList = splitIntoNumberOfSegments(document);
		switchColors(segmentList);
		for (Course course : segmentList) {
			document.add(course);
			consolidate(this.course, course);
		}
		document.setChangedTrue();

	}

	public void processDuration(GPSDocument document) throws TrackItException {
		List<Course> segmentList = new ArrayList<Course>();
		segmentList = splitIntoTimedSegments(document);
		switchColors(segmentList);
		for (Course course : segmentList) {
			document.add(course);
			consolidate(this.course, course);
		}
		document.setChangedTrue();
		;
	}

	public void processLength(GPSDocument document) throws TrackItException {
		List<Course> segmentList = new ArrayList<Course>();
		segmentList = splitIntoLengthSegments(document);
		switchColors(segmentList);
		for (Course course : segmentList) {
			document.add(course);
			consolidate(this.course, course);
		}
		document.setChangedTrue();
		;
	}

	public void processCreateSegment(GPSDocument document) throws TrackItException {
		Course segment = createSegment(document);
		List<Course> courses = new ArrayList<Course>();
		courses.add(segment);
		switchColors(courses);
		document.add(segment);
		document.setChangedTrue();
		consolidate(this.course, segment);
	}

	private Course newCourse(int segmentNumber, GPSDocument document) {
		Course newCourse = new Course();
		newCourse.setParent(document);
//		newCourse.setSport(course.getSport());									//12335: 2016-06-15
//		newCourse.setSubSport(course.getSubSport());							//12335: 2016-06-15
		newCourse.setSportAndSubSport( course.getSport(), course.getSubSport());//12335: 2016-06-15

		String newCourseName = "";
		if(course.isSegment()){
			newCourseName = course.getName().concat("," + segmentNumber);
		}
		else{
			newCourseName = course.getName().concat(": Segment " + segmentNumber);
		}
		
		newCourse.setName(newCourseName);
		return newCourse;
	}

	private List<Course> splitIntoNumberOfSegments(GPSDocument document) {
		int numberOfSegments = (int) value;
		int numberOfTrackpoints = course.getTrackpoints().size();
		int segmentNumber;
		Double totalDistance = course.getDistance();
		Double averageSegmentLength = totalDistance / numberOfSegments;
		Double currentSegmentLength = averageSegmentLength;
		Double currentDistance = 0.0;

		List<Course> segmentList = new ArrayList<Course>();
		List<Trackpoint> copy = new ArrayList<Trackpoint>();
		for (int i = 0; i < numberOfTrackpoints; i++) {
			Trackpoint currentTrackpoint = course.getTrackpoints().get(i).clone();
			currentDistance = currentTrackpoint.getDistance();
			if (currentDistance >= currentSegmentLength || i == numberOfTrackpoints - 1) {
				// currentTrackpoint.setDistanceFromPrevious(0.0);
				// currentTrackpoint.setTimeFromPrevious(0.0);
				copy.add(currentTrackpoint);
				segmentNumber = this.course.getSegmentNumber();
				Course course = newCourse(segmentNumber, document);
				this.course.setSegmentNumber(1);
				course.setParentCourseId(this.course.getId());
				currentSegmentLength += averageSegmentLength;
				course.setTrackpoints(copy);
				copy.clear();
				copy.add(currentTrackpoint);
				consolidateCoursePoints(this.course, course);
				segmentList.add(course);
				//segmentNumber += 1;

			} else {
				copy.add(currentTrackpoint);
			}

		}

		return segmentList;
	}

	private List<Course> splitIntoTimedSegments(GPSDocument document) {
		Double segmentDuration = (Double) value / 1000;
		int numberOfTrackpoints = course.getTrackpoints().size();
		int segmentNumber;
		// Double totalDuration = course.getElapsedTime();
		Double averageSegmentDuration = segmentDuration;
		Double currentSegmentDuration = segmentDuration;
		Double currentTime = 0.0;

		List<Course> segmentList = new ArrayList<Course>();
		List<Trackpoint> copy = new ArrayList<Trackpoint>();
		for (int i = 0; i < numberOfTrackpoints; i++) {
			Trackpoint currentTrackpoint = course.getTrackpoints().get(i).clone();
			currentTime += currentTrackpoint.getTimeFromPrevious();
			if (currentTime >= currentSegmentDuration || i == numberOfTrackpoints - 1) {
				// currentTrackpoint.setDistanceFromPrevious(0.0);
				// currentTrackpoint.setTimeFromPrevious(0.0);
				copy.add(currentTrackpoint);
				segmentNumber = this.course.getSegmentNumber();
				Course course = newCourse(segmentNumber, document);
				this.course.setSegmentNumber(1);
				course.setParentCourseId(this.course.getId());
				currentSegmentDuration += averageSegmentDuration;
				course.setTrackpoints(copy);
				copy.clear();
				copy.add(currentTrackpoint);
				consolidateCoursePoints(this.course, course);
				segmentList.add(course);
				//segmentNumber += 1;

			} else {
				copy.add(currentTrackpoint);
			}

		}

		return segmentList;
	}

	private List<Course> splitIntoLengthSegments(GPSDocument document) {
		Double segmentLength = (Double) value;
		int numberOfTrackpoints = course.getTrackpoints().size();
		int segmentNumber ;
		// Double totalDistance = course.getDistance();
		Double averageSegmentLength = segmentLength;
		Double currentSegmentLength = segmentLength;
		Double currentDistance = 0.0;

		List<Course> segmentList = new ArrayList<Course>();
		List<Trackpoint> copy = new ArrayList<Trackpoint>();
		for (int i = 0; i < numberOfTrackpoints; i++) {
			Trackpoint currentTrackpoint = course.getTrackpoints().get(i).clone();
			currentDistance = currentTrackpoint.getDistance();
			if (currentDistance >= currentSegmentLength || i == numberOfTrackpoints - 1) {
				// currentTrackpoint.setDistanceFromPrevious(0.0);
				// currentTrackpoint.setTimeFromPrevious(0.0);
				copy.add(currentTrackpoint);
				segmentNumber = this.course.getSegmentNumber();
				Course course = newCourse(segmentNumber, document);
				this.course.setSegmentNumber(1);
				course.setParentCourseId(this.course.getId());
				currentSegmentLength += averageSegmentLength;
				course.setTrackpoints(copy);
				copy.clear();
				copy.add(currentTrackpoint);
				consolidateCoursePoints(this.course, course);
				segmentList.add(course);
				//segmentNumber += 1;

			} else {
				copy.add(currentTrackpoint);
			}

		}

		return segmentList;
	}

	private Course createSegment(GPSDocument document) {
		List<Trackpoint> segmentTrackpoints = segment.getTrackpoints();
		List<Trackpoint> courseTrackpoints = course.getTrackpoints();

		long startSegmentTime = segment.getStartTime().getTime();
		long endSegmentTime = segment.getEndTime().getTime();

		int numberOfSegmentTrackpoints = segmentTrackpoints.size();
		int numberOfCourseTrackpoints = courseTrackpoints.size();
		int segmentNumber;
		segmentNumber = this.course.getSegmentNumber();
		Course segmentCourse = newCourse(segmentNumber, document);
		this.course.setSegmentNumber(1);
		course.setParentCourseId(this.course.getId());
		
		Trackpoint currentCourseTrackpoint;
		long currentTime = 0;
		List<Trackpoint> matchingTrackpoints = new ArrayList<Trackpoint>();
		for (int i = 0; i < numberOfCourseTrackpoints; i++) {
			currentCourseTrackpoint = courseTrackpoints.get(i);
			currentTime = currentCourseTrackpoint.getTimestamp().getTime();
			if (currentTime >= startSegmentTime && currentTime <= endSegmentTime) {
				matchingTrackpoints.add(currentCourseTrackpoint.clone());
			}
		}

		// matchingTrackpoints.get(0).setTimeFromPrevious(0.0);
		// matchingTrackpoints.get(0).setDistanceFromPrevious(0.0);
		segmentCourse.setTrackpoints(matchingTrackpoints);

		return segmentCourse;
	}

	private void setSegmentValues(Course oldCourse, Course newCourse) {
		Double newDistance = 0.0;
		Double newMovingTime = 0.0;
		for (Trackpoint trackpoint : oldCourse.getTrackpoints()) {
			if (trackpoint.getTimestamp().getTime() <= newCourse.getLastTrackpoint().getTimestamp().getTime()) {
				if (!(oldCourse.isInsidePause(trackpoint.getTimestamp().getTime()))) {
					newMovingTime += trackpoint.getTimeFromPrevious();
					newDistance += trackpoint.getDistanceFromPrevious();
				}
			}
		}
		newCourse.setSegmentDistance(newDistance);
		newCourse.setSegmentMovingTime(newMovingTime);
	}

	private void consolidate(Course oldCourse, Course newCourse) {
		// consolidateCoursePoints(oldCourse, newCourse);
		setSegmentValues(oldCourse, newCourse);
		consolidateTrackpoints(newCourse);
		consolidateSegments(oldCourse, newCourse);
		consolidateLaps(newCourse);
		// consolidateCourses(oldCourse, newCourse);
		newCourse.consolidate(ConsolidationLevel.BASIC);
	}

	private void consolidateCoursePoints(Course oldCourse, Course newCourse) {
		Iterator<CoursePoint> iterator = oldCourse.getCoursePoints().iterator();

		while (iterator.hasNext()) {
			CoursePoint coursePoint = iterator.next();

			if (newCourse.getTrackpoints().contains(coursePoint.getTrackpoint())) {
				newCourse.add(coursePoint);
				iterator.remove();
			}
		}
	}

	private void consolidateTrackpoints(Course newCourse) {
		double offset = newCourse.getFirstTrackpoint().getDistance();
		double distance;

		for (Trackpoint trackpoint : newCourse.getTrackpoints()) {
			distance = trackpoint.getDistance() - offset;
			trackpoint.setDistance(distance);
			trackpoint.setParent(newCourse);
		}
	}

	private void consolidateSegments(Course oldCourse, Course newCourse) {
		Iterator<TrackSegment> segmentIterator = oldCourse.getSegments().iterator();

		while (segmentIterator.hasNext()) {
			TrackSegment segment = segmentIterator.next();
			List<Trackpoint> trackpoints = segment.getTrackpoints();

			Trackpoint firstTrackpoint = trackpoints.get(0);
			Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);

			if (oldCourse.getTrackpoints().contains(firstTrackpoint)
					&& newCourse.getTrackpoints().contains(lastTrackpoint)) {

				int firstIndex = trackpoints.indexOf(newCourse.getTrackpoints().get(0));
				int lastIndex = trackpoints.size();

				TrackSegment newSegment = new TrackSegment(newCourse);
				newSegment.setTrackpoints(trackpoints.subList(firstIndex, lastIndex));
				newCourse.add(newSegment);

				lastIndex = firstIndex + 1;
				firstIndex = 0;
				segment.setTrackpoints(trackpoints.subList(firstIndex, lastIndex));
			}
		}
	}

	private void consolidateLaps(Course newCourse) {
		CourseLap lap = new CourseLap(newCourse);
		lap.setStartTime(newCourse.getFirstTrackpoint().getTimestamp());
		lap.setEndTime(newCourse.getLastTrackpoint().getTimestamp());
		lap.consolidate(ConsolidationLevel.SUMMARY);
		newCourse.add(lap);
	}

	/*
	 * private void consolidateLaps(Course oldCourse, Course newCourse) {
	 * List<Lap> laps = oldCourse.getLaps();
	 * 
	 * Date oldCourseLastTime = oldCourse.getLastTrackpoint().getTimestamp();
	 * Lap intersectingLap = calculateIntersectingLap(laps, oldCourseLastTime);
	 * int intersectingLapIndex = laps.indexOf(intersectingLap);
	 * 
	 * CourseLap lap = new CourseLap(oldCourse);
	 * lap.setStartTime(intersectingLap.getStartTime());
	 * lap.setEndTime(oldCourse.getLastTrackpoint().getTimestamp()); if
	 * (lap.getStartTime() != lap.getEndTime()) { laps.add(intersectingLapIndex,
	 * lap); intersectingLapIndex++;
	 * 
	 * intersectingLap.setStartTime(newCourse.getFirstTrackpoint().getTimestamp(
	 * )); ((CourseLap) intersectingLap).setParent(oldCourse);
	 * oldCourse.setLaps(new ArrayList<>(laps.subList(0,
	 * intersectingLapIndex))); newCourse.setLaps(new
	 * ArrayList<>(laps.subList(intersectingLapIndex, laps.size()))); } else{
	 * oldCourse.setLaps(new ArrayList<>(laps.subList(0,
	 * intersectingLapIndex))); newCourse.setLaps(new
	 * ArrayList<>(laps.subList(intersectingLapIndex, laps.size()))); }
	 * 
	 * 
	 * for (Lap oldCourseLap : oldCourse.getLaps()) {
	 * oldCourseLap.consolidate(ConsolidationLevel.SUMMARY); }
	 * 
	 * for (Lap newCourseLap : newCourse.getLaps()) { ((CourseLap)
	 * newCourseLap).setParent(newCourse);
	 * newCourseLap.consolidate(ConsolidationLevel.SUMMARY); } }
	 */

	private Lap calculateIntersectingLap(List<Lap> laps, Date referenceTime) {
		for (Lap lap : laps) {
			if (lap.getEndTime().after(referenceTime)) {
				return lap;
			}
		}
		throw new IllegalStateException("Split operation: intersecting lap not found!");
	}

	private void consolidateCourses(Course oldCourse, Course newCourse) {
		oldCourse.consolidate(ConsolidationLevel.SUMMARY);
		oldCourse.setAttribute(Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME, ColorScheme.getNextColorScheme());
		newCourse.consolidate(ConsolidationLevel.SUMMARY);
		newCourse.setAttribute(Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME, ColorScheme.getNextColorScheme());
	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {

	}

	@Override
	public void undoOperation(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
	}

	@Override
	public void redoOperation(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub

	}
}
