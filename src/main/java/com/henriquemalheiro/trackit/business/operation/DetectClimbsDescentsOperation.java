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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Direction;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Predicate;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.SegmentType;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.Utilities;

public class DetectClimbsDescentsOperation extends OperationBase implements Operation {
	private static final double MIN_CLIMB_PERCENTAGE = 0.75;
	private static final double MIN_DESCENT_PERCENTAGE = 0.55;

	private Logger logger = Logger.getLogger(DetectClimbsDescentsOperation.class.getName());

	private double climbGradeLowerLimit;
	private double descentGradeLowerLimit;

	private Course course;
	private List<TrackSegment> segments;

	public DetectClimbsDescentsOperation() {
		super();
		options.put(Constants.DetectClimbsAndDescentsOperation.CLIMB_GRADE_LOWER_LIMIT, 1.9);
		options.put(Constants.DetectClimbsAndDescentsOperation.DESCENT_GRADE_LOWER_LIMIT, -3.5);
		setUp();
	}

	public DetectClimbsDescentsOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		climbGradeLowerLimit = (Double) options.get(Constants.DetectClimbsAndDescentsOperation.CLIMB_GRADE_LOWER_LIMIT);
		descentGradeLowerLimit = (Double) options
		        .get(Constants.DetectClimbsAndDescentsOperation.DESCENT_GRADE_LOWER_LIMIT);
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
		if (processCourses) {
			for (Course course : document.getCourses()) {
				this.course = course;
				createTrackSegments();
				course.setSegments(segments);
			}
		}
	}

	private void createTrackSegments() throws TrackItException {
		final double extremeSmoothingFactor = 300.0;
		final double normalSmoothingFactor = 60.0;

		smoothGradeData(extremeSmoothingFactor);
		breakTrackIntoSegments();
		mergeClimbs();
		mergeDescents();
		smoothGradeData(normalSmoothingFactor);
		consolidateSegments();
		printSegmentsInfo();
	}

	private void smoothGradeData(double smoothingFactor) throws TrackItException {
		Map<String, Object> options = new HashMap<>();
		options.put(Constants.SmoothingOperation.FACTOR, smoothingFactor);

		GPSDocument document = new GPSDocument(course.getParent().getFileName());
		document.add(course);

		new SmoothingOperation(options).process(document);

		if (document == null || document.getCourses().isEmpty()) {
			throw new TrackItException("Smoothing operation did not return any course.");
		}

		course = document.getCourses().get(0);
	}

	private void consolidateSegments() {
		for (TrackSegment segment : segments) {
			segment.consolidate(ConsolidationLevel.SUMMARY);
		}
	}

	private void breakTrackIntoSegments() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		List<Trackpoint> buffer = new ArrayList<Trackpoint>();
		segments = new ArrayList<TrackSegment>();
		TrackSegment segment = null;

		double distance = 0.0;
		double time = 0.0;

		SegmentType lastType = null;
		SegmentType type = null;
		double grade;
		for (Trackpoint trackpoint : trackpoints) {
			grade = (trackpoint.getGrade() != null ? trackpoint.getGrade() : 0.0);
			type = getSegmentType(grade);

			if (type.equals(lastType)) {
				distance += trackpoint.getDistanceFromPrevious();
				buffer.add(trackpoint);
			} else {
				segment = new TrackSegment(course);
				segment.setTime(time);
				segment.setDistance(distance);
				segment.setType(lastType);
				segment.getTrackpoints().addAll(buffer);
				segment.consolidate(ConsolidationLevel.SUMMARY);

				segments.add(segment);

				distance = 0.0;
				time = 0.0;
				buffer.clear();

				buffer.add(trackpoint);
				distance += trackpoint.getDistanceFromPrevious();
				lastType = type;
			}
		}

		if (buffer.size() > 0) {
			segment = new TrackSegment(course);
			segment.setTime(time);
			segment.setDistance(distance);
			segment.setType(lastType);
			segment.getTrackpoints().addAll(buffer);
			segment.consolidate(ConsolidationLevel.SUMMARY);

			segments.add(segment);
		}
	}

	private void mergeClimbs() {
		mergeSegments(SegmentType.CLIMB);
	}

	private void mergeDescents() {
		mergeSegments(SegmentType.DESCENT);
	}

	private void mergeSegments(SegmentType type) {
		List<TrackSegment> segments = getSegments(type);
		sortSegments(segments);

		while (!segments.isEmpty()) {
			TrackSegment segment = segments.get(0);
			List<TrackSegment> adjacentSegments = getAdjacentSegments(segment);

			for (TrackSegment adjacentSegment : adjacentSegments) {
				int minCat = Math.max(segment.getCategory().getValue(), adjacentSegment.getCategory().getValue());
				TrackSegment mergedSegment = mergeSegments(segment, adjacentSegment);

				if (validMergedSegment(mergedSegment, minCat)) {
					replaceSegments(segment, adjacentSegment, mergedSegment);
					segments.remove(adjacentSegment);
					segments.add(mergedSegment);
					break;
				}
			}

			segments.remove(segment);
			sortSegments(segments);
		}
	}

	private boolean validMergedSegment(TrackSegment mergedSegment, int minCat) {
		boolean valid = true;

		valid &= mergedSegment.getCategory().getValue() >= minCat && isCategorizedSegment(mergedSegment);
		valid &= validSegmentPercentage(mergedSegment);

		return valid;
	}

	private boolean validSegmentPercentage(TrackSegment segment) {
		int total = 0;
		int[] count = new int[3];

		for (Trackpoint trackpoint : segment.getTrackpoints()) {
			double grade = (trackpoint.getGrade() != null ? trackpoint.getGrade() : 0.0);

			if (grade >= climbGradeLowerLimit) {
				count[0]++;
			} else if (grade <= descentGradeLowerLimit) {
				count[2]++;
			} else {
				count[1]++;
			}

			total++;
		}

		SegmentType type = segment.getType();
		switch (type) {
		case CLIMB:
			return (count[0] / (double) total) >= MIN_CLIMB_PERCENTAGE;
		case DESCENT:
			return (count[2] / (double) total) >= MIN_DESCENT_PERCENTAGE;
		default:
			return true;
		}
	}

	private TrackSegment mergeSegments(TrackSegment segment1, TrackSegment segment2) {
		List<TrackSegment> contiguousSegments = getContiguousSegments(segment1, segment2);

		return createMergedSegment(contiguousSegments);
	}

	private TrackSegment createMergedSegment(List<TrackSegment> contiguousSegments) {
		TrackSegment mergedSegment = new TrackSegment(course);

		for (TrackSegment contiguousSegment : contiguousSegments) {
			mergedSegment.addTrackpoints(contiguousSegment.getTrackpoints());
		}

		mergedSegment.consolidate(ConsolidationLevel.SUMMARY);

		return mergedSegment;
	}

	private List<TrackSegment> getContiguousSegments(TrackSegment segment1, TrackSegment segment2) {
		int segmentIndex1 = segments.indexOf(segment1);
		int segmentIndex2 = segments.indexOf(segment2);
		int fromIndex = segmentIndex2 > segmentIndex1 ? segmentIndex1 : segmentIndex2;
		int toIndex = segmentIndex2 > segmentIndex1 ? segmentIndex2 : segmentIndex1;

		return segments.subList(fromIndex, toIndex + 1);
	}

	private void replaceSegments(TrackSegment start, TrackSegment end, TrackSegment replacement) {
		int startIndex = segments.indexOf(start);
		int endIndex = segments.indexOf(end);
		int fromIndex = endIndex > startIndex ? startIndex : endIndex;
		int toIndex = endIndex > startIndex ? endIndex : startIndex;

		List<TrackSegment> newSegments = new ArrayList<>();
		newSegments.addAll(segments.subList(0, fromIndex));
		newSegments.add(replacement);
		newSegments.addAll(segments.subList(toIndex + 1, segments.size()));

		segments = newSegments;
	}

	private List<TrackSegment> getSegments(final SegmentType type) {
		return Utilities.filter(segments, new Predicate<TrackSegment>() {
			@Override
			public boolean apply(TrackSegment segment) {
				return segment.getType() == type;
			}
		});
	}

	private boolean isCategorizedSegment(TrackSegment segment) {
		boolean categorized = false;

		if (segment.getType() == SegmentType.CLIMB) {
			categorized |= (segment.getAverageGrade() != null && segment.getAverageGrade() >= climbGradeLowerLimit);
		} else if (segment.getType() == SegmentType.DESCENT) {
			categorized |= (segment.getAverageGrade() != null && segment.getAverageGrade() <= descentGradeLowerLimit);
		}

		return categorized;
	}

	private void sortSegments(List<TrackSegment> segments) {
		Collections.sort(segments, new Comparator<TrackSegment>() {
			@Override
			public int compare(TrackSegment segment1, TrackSegment segment2) {
				Double segment1MaxAltitude = segment1.getMaximumAltitude();
				segment1MaxAltitude = segment1MaxAltitude != null ? segment1MaxAltitude : 0.0;

				Double segment2MaxAltitude = segment2.getMaximumAltitude();
				segment2MaxAltitude = segment2MaxAltitude != null ? segment2MaxAltitude : 0.0;

				return segment2MaxAltitude.compareTo(segment1MaxAltitude);
			}
		});
	}

	private List<TrackSegment> getAdjacentSegments(TrackSegment segment) {
		List<TrackSegment> adjacentSegments = new ArrayList<>();
		int segmentIndex = segments.indexOf(segment);

		if (segmentIndex == -1) {
			return adjacentSegments;
		}

		SegmentType type = segment.getType();

		TrackSegment adjacentRighSegment = getAdjacentSegment(segmentIndex, type, Direction.DIRECT);
		if (adjacentRighSegment != null) {
			adjacentSegments.add(adjacentRighSegment);
		}

		TrackSegment adjacentLeftSegment = getAdjacentSegment(segmentIndex, type, Direction.REVERSE);
		if (adjacentLeftSegment != null) {
			adjacentSegments.add(adjacentLeftSegment);
		}

		sortSegments(adjacentSegments);

		return adjacentSegments;
	}

	private TrackSegment getAdjacentSegment(int segmentIndex, SegmentType type, Direction direction) {
		ListIterator<TrackSegment> iter = segments.listIterator(segmentIndex);
		skipCurrentSegment(direction, iter);
		TrackSegment segment = null;

		while (hasNext(iter, direction)) {
			segment = getNext(iter, direction);

			if (segment.getType() == type) {
				break;
			}
		}

		return segment;
	}

	private void skipCurrentSegment(Direction direction, ListIterator<TrackSegment> iter) {
		if (direction == Direction.DIRECT) {
			iter.next();
		}
	}

	private boolean hasNext(ListIterator<TrackSegment> iter, Direction direction) {
		return (direction == Direction.DIRECT ? iter.hasNext() : iter.hasPrevious());
	}

	private TrackSegment getNext(ListIterator<TrackSegment> iter, Direction direction) {
		return (direction == Direction.DIRECT ? iter.next() : iter.previous());
	}

	private SegmentType getSegmentType(double grade) {
		if (grade >= climbGradeLowerLimit) {
			return SegmentType.CLIMB;
		} else if (grade <= descentGradeLowerLimit) {
			return SegmentType.DESCENT;
		} else {
			return SegmentType.FLAT;
		}
	}

	private void printSegmentsInfo() {
		if (segments == null) {
			return;
		}

		for (TrackSegment trackSegment : segments) {
			logger.info(String.format("Segment id: %d", trackSegment.getId()));
			logger.info(trackSegment);
		}

		printMaxMinGrades();
		printAverageTrackpointDistance();
	}

	private void printMaxMinGrades() {
		double minGrade = Double.MAX_VALUE;
		double maxGrade = Double.MIN_VALUE;
		Trackpoint minGradeTrkpt = null;
		Trackpoint maxGradeTrkpt = null;

		for (Trackpoint trackpoint : course.getTrackpoints()) {
			double tempMinGrade = Math.min(trackpoint.getGrade(), minGrade);
			if (tempMinGrade < minGrade) {
				minGrade = tempMinGrade;
				minGradeTrkpt = trackpoint;
			}

			double tempMaxGrade = Math.max(trackpoint.getGrade(), maxGrade);
			if (tempMaxGrade > maxGrade) {
				maxGrade = tempMaxGrade;
				maxGradeTrkpt = trackpoint;
			}
		}

		logger.info(String.format("Min Grade: %s%% (%s)",
		        Formatters.getDecimalFormat(1).format(minGradeTrkpt.getGrade()),
		        Formatters.getFormatedDistance(minGradeTrkpt.getDistance())));
		logger.info(String.format("Max Grade: %s%% (%s)",
		        Formatters.getDecimalFormat(1).format(maxGradeTrkpt.getGrade()),
		        Formatters.getFormatedDistance(maxGradeTrkpt.getDistance())));
	}

	private void printAverageTrackpointDistance() {
		double distanceSum = 0.0;
		for (int i = 1; i < course.getTrackpoints().size(); i++) {
			distanceSum += course.getTrackpoints().get(i).getDistanceFromPrevious();
		}

		double averageTrackpointDistance = distanceSum / course.getTrackpoints().size();

		logger.info(String.format("Average trackpoint distance: %s",
		        Formatters.getFormatedDistance(averageTrackpointDistance)));
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
