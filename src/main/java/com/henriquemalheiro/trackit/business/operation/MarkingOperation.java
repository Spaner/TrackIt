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
package com.henriquemalheiro.trackit.business.operation;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.CoursePointType;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.SegmentCategory;
import com.henriquemalheiro.trackit.business.domain.SegmentType;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class MarkingOperation extends OperationBase implements Operation {
	private Course course;
	private boolean markClimbStart;
	private boolean markClimbFinish;
	private boolean markClimbMaxGrade;
	private boolean markClimbMinGrade;
	private boolean markDescentStart;
	private boolean markDescentFinish;
	private boolean markDescentMaxGrade;
	private boolean markDescentMinGrade;
	private boolean removeExistingMarks;
	
	public MarkingOperation() {
		super();
		
		options.put(Constants.Operation.PROCESS_FOLDERS, Boolean.FALSE);
		options.put(Constants.Operation.PROCESS_ACTIVITIES, Boolean.FALSE);
		options.put(Constants.Operation.PROCESS_WAYPOINTS, Boolean.FALSE);
		
		options.put(Constants.MarkingOperation.MARK_CLIMB_START, Boolean.TRUE);
		options.put(Constants.MarkingOperation.MARK_CLIMB_FINISH, Boolean.TRUE);
		options.put(Constants.MarkingOperation.MARK_CLIMB_MAX_GRADE, Boolean.FALSE);
		options.put(Constants.MarkingOperation.MARK_CLIMB_MIN_GRADE, Boolean.FALSE);
		options.put(Constants.MarkingOperation.MARK_DESCENT_START, Boolean.TRUE);
		options.put(Constants.MarkingOperation.MARK_DESCENT_FINISH, Boolean.TRUE);
		options.put(Constants.MarkingOperation.MARK_DESCENT_MAX_GRADE, Boolean.FALSE);
		options.put(Constants.MarkingOperation.MARK_DESCENT_MIN_GRADE, Boolean.FALSE);
		options.put(Constants.MarkingOperation.REMOVE_EXISTING_MARKS, Boolean.TRUE);
		
		setUp();
	}
	
	public MarkingOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		this.markClimbStart = (Boolean) options.get(Constants.MarkingOperation.MARK_CLIMB_START);
		this.markClimbFinish = (Boolean) options.get(Constants.MarkingOperation.MARK_CLIMB_FINISH);
		this.markClimbMaxGrade = (Boolean) options.get(Constants.MarkingOperation.MARK_CLIMB_MAX_GRADE);
		this.markClimbMinGrade = (Boolean) options.get(Constants.MarkingOperation.MARK_CLIMB_MIN_GRADE);
		this.markDescentStart = (Boolean) options.get(Constants.MarkingOperation.MARK_DESCENT_START);
		this.markDescentFinish = (Boolean) options.get(Constants.MarkingOperation.MARK_DESCENT_FINISH);
		this.markDescentMaxGrade = (Boolean) options.get(Constants.MarkingOperation.MARK_DESCENT_MAX_GRADE);
		this.markDescentMinGrade = (Boolean) options.get(Constants.MarkingOperation.MARK_DESCENT_MIN_GRADE);
		this.removeExistingMarks = (Boolean) options.get(Constants.MarkingOperation.REMOVE_EXISTING_MARKS);
	}

	@Override
	public String getName() {
		return Constants.DetectClimbsAndDescentsOperation.NAME;
	}
	
	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		for (GPSDocument document : documents) {
			process(document);
		}
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		new DetectClimbsDescentsOperation(options).process(document);
		
		if (processCourses) {
			for (Course course : document.getCourses()) {
				this.course = course;
				
				if (removeExistingMarks) {
					clearCoursePoints(course);
				}
				
				List<CoursePoint> coursePointsClimbs = createClimbCoursePoints(course);
				course.addCoursePoints(coursePointsClimbs);
				
				List<CoursePoint> coursePointsDescents = createDescentCoursePoints(course);
				course.addCoursePoints(coursePointsDescents);
				
				List<CoursePoint> coursePointsMaxMinGrades = createMaxMinGradeCoursePoints(course);
				course.addCoursePoints(coursePointsMaxMinGrades);
				
				reorderCoursePoints(course);
			}
		}
	}

	private void clearCoursePoints(Course course) {
		List<CoursePointType> coursePointTypes = new ArrayList<CoursePointType>();
		coursePointTypes.add(CoursePointType.HORS_CATEGORY);
		coursePointTypes.add(CoursePointType.FIRST_CATEGORY);
		coursePointTypes.add(CoursePointType.SECOND_CATEGORY);
		coursePointTypes.add(CoursePointType.THIRD_CATEGORY);
		coursePointTypes.add(CoursePointType.FOURTH_CATEGORY);
		coursePointTypes.add(CoursePointType.SUMMIT);
		coursePointTypes.add(CoursePointType.SPRINT);
		coursePointTypes.add(CoursePointType.VALLEY);
		coursePointTypes.add(CoursePointType.DANGER);
		
		Iterator<CoursePoint> iter = course.getCoursePoints().iterator();
		while (iter.hasNext()) {
			CoursePoint coursePoint = iter.next();
			if (coursePointTypes.contains(coursePoint.getType())) {
				iter.remove();
			}
		}
	}

	private List<CoursePoint> createClimbCoursePoints(Course course) {
		List<CoursePoint> coursePoints = new ArrayList<CoursePoint>();
		
		int summitCount = 1;
		for (TrackSegment trackSegment : course.getSegments()) {
			if (trackSegment.getType() == SegmentType.CLIMB
					&& (trackSegment.getCategory() == SegmentCategory.FOURTH_CATEGORY_CLIMB
							|| trackSegment.getCategory() == SegmentCategory.THIRD_CATEGORY_CLIMB
							|| trackSegment.getCategory() == SegmentCategory.SECOND_CATEGORY_CLIMB
							|| trackSegment.getCategory() == SegmentCategory.FIRST_CATEGORY_CLIMB
							|| trackSegment.getCategory() == SegmentCategory.HORS_CATEGORY_CLIMB)) {
				
				Trackpoint trackpoint = trackSegment.getTrackpoints().get(0);
				CoursePoint coursePoint = createCoursePoint(trackpoint);

				NumberFormat df = Formatters.getDecimalFormat(1);
					
				String distance = (trackSegment.getDistance() >= 1000.0
						? df.format(trackSegment.getDistance() / 1000).replace('.', 'k') : trackSegment.getDistance().intValue() + "m");
				
				String grades = String.valueOf(df.format(trackSegment.getAverageGrade())).replace('.', '%')
						+ Math.round(trackSegment.getMaximumGrade());
				
				coursePoint.setName(distance + grades);
				coursePoint.setType(getCoursePointType(trackSegment.getCategory()));
				
				if (markClimbStart) {
					coursePoints.add(coursePoint);
				}
				
				trackpoint = trackSegment.getTrackpoints().get(trackSegment.getTrackpoints().size() - 1);
				coursePoint = createCoursePoint(trackpoint);
				coursePoint.setName("Summit #" + summitCount);
				coursePoint.setType(CoursePointType.SUMMIT);
				
				if (markClimbFinish) {
					coursePoints.add(coursePoint);
				}
				
				summitCount++;
			}
		}
		
		return coursePoints;
	}
	
	private List<CoursePoint> createDescentCoursePoints(Course course) {
		List<CoursePoint> coursePoints = new ArrayList<CoursePoint>();
		
		int valleyCount = 1;
		for (TrackSegment trackSegment : course.getSegments()) {
			if (trackSegment.getType() == SegmentType.DESCENT
					&& trackSegment.getCategory() == SegmentCategory.CATEGORIZED_DESCENT) {
				
				CoursePoint coursePoint = createCoursePoint(trackSegment.getTrackpoints().get(0));
				
				NumberFormat df = Formatters.getDecimalFormat(1);
				String distance = df.format(trackSegment.getDistance() / 1000).replace('.', 'k');
				String grades = Math.round(trackSegment.getAverageGrade()) + "%" + Math.round(trackSegment.getMaximumGrade());
				
				coursePoint.setName(distance + grades);
				coursePoint.setType(getCoursePointType(trackSegment.getCategory()));
				
				if (markDescentStart) {
					coursePoints.add(coursePoint);
				}
				
				Trackpoint trackpoint = trackSegment.getTrackpoints().get(trackSegment.getTrackpoints().size() - 1);
				coursePoint = createCoursePoint(trackpoint);
				coursePoint.setName("Valley #" + valleyCount);
				coursePoint.setType(CoursePointType.VALLEY);
				
				if (markDescentFinish) {
					coursePoints.add(coursePoint);
				}
				
				valleyCount++;
			}
		}
		
		return coursePoints;
	}
	
	private CoursePointType getCoursePointType(SegmentCategory segmentCategory) {
		switch (segmentCategory) {
		case FOURTH_CATEGORY_CLIMB:
			return CoursePointType.FOURTH_CATEGORY;
		case THIRD_CATEGORY_CLIMB:
			return CoursePointType.THIRD_CATEGORY;
		case SECOND_CATEGORY_CLIMB:
			return CoursePointType.SECOND_CATEGORY;
		case FIRST_CATEGORY_CLIMB:
			return CoursePointType.FIRST_CATEGORY;
		case HORS_CATEGORY_CLIMB:
			return CoursePointType.HORS_CATEGORY;
		case CATEGORIZED_DESCENT:
			return CoursePointType.SPRINT;
		default:
			return CoursePointType.GENERIC;
		}
	}

	private List<CoursePoint> createMaxMinGradeCoursePoints(Course course) {
		List<CoursePoint> coursePoints = new ArrayList<CoursePoint>();
		
		for (TrackSegment segment : course.getSegments()) {
			if (isCategorizedClimb(segment) || isCategorizedDescent(segment)) {
				double minGrade = 50.0;
				double maxGrade = -50.0;
				Trackpoint minGradeTrackpoint = null;
				Trackpoint maxGradeTrackpoint = null;
				
				for (Trackpoint trackpoint : segment.getTrackpoints()) {
					if (trackpoint.getGrade() < minGrade) {
						minGrade = trackpoint.getGrade();
						minGradeTrackpoint = trackpoint;
					}
					
					if (trackpoint.getGrade() > maxGrade) {
						maxGrade = trackpoint.getGrade();
						maxGradeTrackpoint = trackpoint;
					}
				}
				
				if (markClimbMaxGrade(segment, maxGradeTrackpoint) || markDescentMinGrade(segment, maxGradeTrackpoint)) {
					String coursePointName = "Max " + Formatters.getDecimalFormat(1).format(maxGradeTrackpoint.getGrade()) + "%";

					CoursePoint coursePoint = createCoursePoint(maxGradeTrackpoint);
					coursePoint.setName(coursePointName);
					coursePoint.setType(CoursePointType.DANGER);
					
					coursePoints.add(coursePoint);
				}
				
				if (markClimbMinGrade(segment, minGradeTrackpoint) || markDescentMaxGrade(segment, minGradeTrackpoint)) {
					String coursePointName = "Min " + Formatters.getDecimalFormat(1).format(minGradeTrackpoint.getGrade()) + "%";
					
					CoursePoint coursePoint = createCoursePoint(minGradeTrackpoint);
					coursePoint.setName(coursePointName);
					coursePoint.setType(CoursePointType.DANGER);
					
					coursePoints.add(coursePoint);
				}
			}
		}
		
		return coursePoints;
	}

	private boolean isCategorizedDescent(TrackSegment segment) {
		return segment.getType() == SegmentType.DESCENT && segment.getCategory() != SegmentCategory.UNCATEGORIZED_DESCENT;
	}

	private boolean isCategorizedClimb(TrackSegment segment) {
		return segment.getType() == SegmentType.CLIMB && segment.getCategory() != SegmentCategory.UNCATEGORIZED_CLIMB;
	}

	private boolean markClimbMaxGrade(TrackSegment trackSegment, Trackpoint maxGradeTrackpoint) {
		return (trackSegment.getType() == SegmentType.CLIMB && maxGradeTrackpoint != null && markClimbMaxGrade);
	}
	
	private boolean markClimbMinGrade(TrackSegment trackSegment, Trackpoint minGradeTrackpoint) {
		return (trackSegment.getType() == SegmentType.CLIMB && minGradeTrackpoint != null && markClimbMinGrade);
	}
	
	private boolean markDescentMaxGrade(TrackSegment trackSegment, Trackpoint minGradeTrackpoint) {
		return (trackSegment.getType() == SegmentType.DESCENT && minGradeTrackpoint != null && markDescentMaxGrade);
	}
	
	private boolean markDescentMinGrade(TrackSegment trackSegment, Trackpoint maxGradeTrackpoint) {
		return (trackSegment.getType() == SegmentType.DESCENT && maxGradeTrackpoint != null && markDescentMinGrade);
	}
	
	private CoursePoint createCoursePoint(Trackpoint trackpoint) {
		String coursePointName = String.format("Trkpt #%d", trackpoint.getId());
		CoursePoint coursePoint = new CoursePoint(coursePointName, course);
		coursePoint.setTime(trackpoint.getTimestamp());
		coursePoint.setDistance(trackpoint.getDistance());
		coursePoint.setLatitude(trackpoint.getLatitude());
		coursePoint.setLongitude(trackpoint.getLongitude());
		coursePoint.setAltitude(trackpoint.getAltitude());
		coursePoint.setTime(trackpoint.getTimestamp());
		coursePoint.setType(CoursePointType.GENERIC);
		
		trackpoint.setCoursePoint(coursePoint);
		trackpoint.setSticky(true);
		trackpoint.setViewable(true);
		
		return coursePoint;
	}
	
	private void reorderCoursePoints(Course course) {
		Collections.sort(course.getCoursePoints(), new Comparator<CoursePoint>() {
			@Override
			public int compare(CoursePoint coursePoint1, CoursePoint coursePoint2) {
				return coursePoint1.getDistance().compareTo(coursePoint2.getDistance());
			}
		});
	}
}
