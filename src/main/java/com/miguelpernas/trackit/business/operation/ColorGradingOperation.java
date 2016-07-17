package com.miguelpernas.trackit.business.operation;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.SegmentCategory;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.business.operation.Operation;
import com.henriquemalheiro.trackit.business.operation.OperationBase;
import com.henriquemalheiro.trackit.business.operation.SmoothingOperation;
import com.miguelpernas.trackit.business.domain.SegmentColorCategory;
import com.pg58406.trackit.business.common.ColorSchemeV2;

public class ColorGradingOperation extends OperationBase implements Operation {

	//private Logger logger = Logger.getLogger(DetectClimbsDescentsOperation.class.getName());

	//private double climbGradeLowerLimit;
	//private double descentGradeLowerLimit;
	private double gradeLimit;
	private int gradeScaleIntervals = 9;
	private double gradeInterval;
	private double[] intervals;
	private double[] roundedIntervals;
	private Color descentMin, descent9, descent8, descent7, descent6, descent5, descent4, descent3, descent2, descent1, flat,
			climb1, climb2, climb3, climb4, climbMax;

	private Course course;
	private List<TrackSegment> segments;
	private boolean needsDescents;

	public ColorGradingOperation() {
		super();
		// setUp();
	}

	public ColorGradingOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		course = (Course) options.get(Constants.SplitIntoSegmentsOperation.COURSE);
		needsDescents = needsDescents();
		gradeLimit = course.getSubSport().getGradeLimit();
		// gradeScaleIntervals = 15;
		gradeInterval = gradeLimit * 2 / gradeScaleIntervals;
		intervals = new double[gradeScaleIntervals + 3];
		roundedIntervals = new double[gradeScaleIntervals + 3];
		for (int i = 0; i <= gradeScaleIntervals + 2; i++) {
			if (!needsDescents) {
				intervals[i] = -gradeLimit + gradeInterval * i;
				roundedIntervals[i] = round(-gradeLimit + gradeInterval * i, 1);
			} else {
				gradeInterval = gradeLimit / gradeScaleIntervals;
				intervals[i] = -gradeLimit + gradeInterval * i;
				roundedIntervals[i] = round(-gradeLimit + gradeInterval * i, 1);
			}
		}
		//double down = Math.floor(gradeScaleIntervals / 2.0);
		//double up = Math.ceil(gradeScaleIntervals / 2.0);
		//climbGradeLowerLimit = intervals[(int) up];
		//descentGradeLowerLimit = intervals[(int) down];
		
		setUpColors();
	}

	private boolean needsDescents() {
		SportType sport = course.getSport();
		if (sport.equals(SportType.ALPINE_SKIING) || sport.equals(SportType.CROSS_COUNTRY_SKIING)
				|| sport.equals(SportType.SNOWBOARDING)) {
			return true;
		}
		return false;
	}

	private void setUpColors() {
		int min = 0;
		int max = 255;
		int inc = 255 / (gradeScaleIntervals + 1 / 2);

		descentMin = new Color(min, max, min, max);
		flat = new Color(max, max, min, max);
		climbMax = new Color(max, min, min, max);

		climb1 = new Color(max, max - inc, min, max);
		climb2 = new Color(max, max - inc * 2, min, max);
		climb3 = new Color(max, max - inc * 3, min, max);
		climb4 = new Color(max, max - inc * 4, min, max);
		
		if (needsDescents) {
			descent1 = new Color(max - inc * 4, max, min, max);
			descent2 = new Color(max - inc * 3, max, min, max);
			descent3 = new Color(max - inc * 2, max, min, max);
			descent4 = new Color(max - inc, max, min, max);
			descent5 = new Color(max, max, min, max);
			descent6 = new Color(max, max - inc, min, max);
			descent7 = new Color(max, max - inc * 2, min, max);
			descent8 = new Color(max, max - inc * 3, min, max);
			descent9 = new Color(max, max - inc * 4, min, max);
		}
		else{
			descent1 = new Color(max - inc, max, min, max);
			descent2 = new Color(max - inc * 2, max, min, max);
			descent3 = new Color(max - inc * 3, max, min, max);
			descent4 = new Color(max - inc * 4, max, min, max);
		}

	}

	@Override
	public String getName() {
		return Constants.SplitIntoSegmentsOperation.NAME;
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		for (Course course : document.getCourses()) {
			this.course = course;
			createTrackSegments();
			course.setSegments(segments);
		}
	}

	private void createTrackSegments() throws TrackItException {
		final double extremeSmoothingFactor = 300.0;
		final double normalSmoothingFactor = 60.0;

		smoothGradeData(extremeSmoothingFactor);
		breakTrackIntoSegments();
		printSegments();
		// mergeClimbs();
		// mergeDescents();
		//smoothGradeData(normalSmoothingFactor);
		consolidateSegments();
		// printSegmentsInfo();
		colorSegments();
		//printSegments();
	}
	
	private void printSegments(){
		int i = 1;
		for(TrackSegment segment : segments){
			System.out.println("Segmento " + i + ", Intervalo [" + segment.getLowerInterval() + ";" + segment.getHigherInterval() + "]");
			System.out.print("Declives: ");
			for(int j = 0; j<segment.getTrackpoints().size()-1;j++){
				double roundGrade = (double)Math.round(segment.getTrackpoints().get(j).getGrade()*100)/100;
				System.out.print(roundGrade + "; ");
			}
			/*for(Trackpoint trackpoint : segment.getTrackpoints()){
				double roundGrade = (double)Math.round(trackpoint.getGrade()*100)/100;
				System.out.print(roundGrade + "; ");
			}*/
			System.out.println();
			if(segment.getTrackpoints().size() == 2){
				double averageGrade = (segment.getTrackpoints().get(0).getGrade() + segment.getTrackpoints().get(1).getGrade())/2;
				double roundGrade = (double)Math.round(averageGrade*100)/100;
				System.out.println("Declive Médio: " + roundGrade);
			}
			i++;
			//System.out.println();
		}
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

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private void colorSegments() {
		Color currentColor;
		int red, green, blue;
		Color selectionFill;
		ColorSchemeV2 colorScheme;

		for (TrackSegment segment : segments) {
			if (needsDescents) {
				currentColor = checkDescentInterval(segment, segment.getLowerInterval(), segment.getHigherInterval());
			} else {
				currentColor = checkInterval(segment, segment.getLowerInterval(), segment.getHigherInterval());
			}
			red = currentColor.getRed();
			green = currentColor.getGreen();
			blue = currentColor.getBlue();
			selectionFill = new Color(red, green, blue);
			segment.setCategory(SegmentCategory.UNCATEGORIZED_SEGMENT);
			colorScheme = new ColorSchemeV2(currentColor, currentColor.darker(), selectionFill.darker(), selectionFill);
			segment.setColorSchemeV2(colorScheme);
		}
	}

	private int getGradeInterval(double grade) {
		int interval = 0;
		int test = (int) Math.ceil(intervals.length / 2);
		if (grade < intervals[0]) {
			interval = 0;
			return interval;
		}
		if (grade > intervals[gradeScaleIntervals]) {
			interval = gradeScaleIntervals;
			return interval+1;
		}
		for (int i = 1; i < intervals.length - 1; i++) {
			if (grade >= intervals[i - 1] && grade < intervals[i] && i <= Math.floor(intervals.length / 2)) {
				interval = i;
				return i;
			}
			if (grade >= intervals[i - 1] && grade <= intervals[i] && i == test) {
				interval = i;
				return i;
			}
			if (grade > intervals[i - 1] && grade <= intervals[i] && i >= Math.ceil(intervals.length / 2)) {
				interval = i;
				return i;
			}
		}

		return interval;
	}

	private void breakTrackIntoSegments() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		List<Trackpoint> buffer = new ArrayList<Trackpoint>();
		segments = new ArrayList<TrackSegment>();
		TrackSegment segment = null;
		int i = 1;
		double distance = 0.0;
		double time = 0.0;
		double grade;
		double previousGrade = trackpoints.get(0).getGrade();
		int currentInterval = 0;
		Trackpoint currentTrackpoint = trackpoints.get(0);
		int previousInterval = getGradeInterval(trackpoints.get(0).getGrade());
		//System.out.println("Segmento " + i);
		for (Trackpoint trackpoint : trackpoints) {
			
			currentTrackpoint = trackpoint;
			grade = (trackpoint.getGrade() != null ? trackpoint.getGrade() : 0.0);
			//System.out.println("Declive = " + grade);
			currentInterval = getGradeInterval(grade);
			
			if (currentInterval == previousInterval) {
				distance += trackpoint.getDistanceFromPrevious();
				buffer.add(trackpoint);
				previousGrade = grade;
				previousInterval = currentInterval;
			} else {
				segment = new TrackSegment(course);
				segment.setTime(time);
				segment.setDistance(distance);
				if (buffer.size() == 1) {
					double averageGrade = (trackpoint.getGrade() + previousGrade) / 2;
					int interval = getGradeInterval(averageGrade);
					segment.setInterval(round(intervals[interval] - gradeInterval, 1), round(intervals[interval], 1));
					//System.out.println("Declive Médio = " + averageGrade);

				} else {
					segment.setInterval(round(intervals[previousInterval] - gradeInterval, 1),
							round(intervals[previousInterval], 1));
				}
				//System.out.println("Intervalo = [" + segment.getLowerInterval()+","+segment.getHigherInterval()+"]");
				buffer.add(trackpoint);
				segment.getTrackpoints().addAll(buffer);
				segment.consolidate(ConsolidationLevel.SUMMARY);

				segments.add(segment);
				i++;
				//System.out.println("Segmento " + i);
				//System.out.println("Declive = " + grade);
				distance = 0.0;
				time = 0.0;
				buffer.clear();

				buffer.add(trackpoint);
				previousGrade = grade;
				previousInterval = currentInterval;
				distance += trackpoint.getDistanceFromPrevious();
			}
		}

		if (buffer.size() > 0) {
			segment = new TrackSegment(course);
			segment.setTime(time);
			segment.setDistance(distance);
			if (buffer.size() == 1) {
				double averageGrade = (currentTrackpoint.getGrade() + previousGrade) / 2;
				int interval = getGradeInterval(averageGrade);
				segment.setInterval(round(intervals[interval] - gradeInterval, 1), round(intervals[interval], 1));
			} else {

				segment.setInterval(round(intervals[previousInterval] - gradeInterval, 1),
						round(intervals[previousInterval], 1));
			}
			segment.getTrackpoints().addAll(buffer);
			segment.consolidate(ConsolidationLevel.SUMMARY);

			segments.add(segment);
		}
	}


	//general coloring function - currently not used
	private Color checkGrade(double grade) {

		int min = 0;
		int max = 255;
		Color color = new Color(min, min, min, max);

		Color maxRed = new Color(max, min, min, max);
		Color midYellow = new Color(max, max, min, max);
		Color minGreen = new Color(min, max, min, max);

		int red;
		int green;
		int blue;

		double maxGrade = gradeLimit;
		double minGrade = -gradeLimit;
		double midGrade = 0.0;
		if (grade >= maxGrade) {
			color = maxRed;
		} else if (grade == midGrade) {
			color = midYellow;
		} else if (grade <= minGrade) {
			color = minGreen;
		} else if (grade < maxGrade && grade > midGrade) {
			red = max;
			green = max - ((int) (max * (grade / maxGrade)));
			blue = min;
			color = new Color(red, green, blue, max);
		} else if (grade < midGrade && grade > minGrade) {
			red = max - ((int) (max * (Math.abs(grade) / maxGrade)));
			green = max;
			blue = min;
			color = new Color(red, green, blue, max);
		}

		return color;
	}

	private Color checkDescentInterval(TrackSegment segment, double low, double high) {
		Color color = new Color(0, 0, 0, 255);
		int i = 0;
		if (low < roundedIntervals[i]) {
			color = climbMax;
			segment.setColorCategory(SegmentColorCategory.STEEP_DESCENT);
			return color;
		}
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent9;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent8;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent7;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent6;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent5;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent4;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent3;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent2;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent1;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		segment.setColorCategory(SegmentColorCategory.MECHANICAL_CLIMB);
		return color;
	}

	private Color checkInterval(TrackSegment segment, double low, double high) {
		Color color = new Color(0, 0, 0, 255);
		int i = 0;
		if (low < roundedIntervals[i]) {
			color = descentMin;
			segment.setColorCategory(SegmentColorCategory.STEEP_DESCENT);
			return color;
		}
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent4;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent3;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent2;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = descent1;
			segment.setColorCategory(SegmentColorCategory.DESCENT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = flat;
			segment.setColorCategory(SegmentColorCategory.FLAT);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = climb1;
			segment.setColorCategory(SegmentColorCategory.CLIMB);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = climb2;
			segment.setColorCategory(SegmentColorCategory.CLIMB);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = climb3;
			segment.setColorCategory(SegmentColorCategory.CLIMB);
			return color;
		}
		i++;
		if (low == roundedIntervals[i] && high == roundedIntervals[i + 1]) {
			color = climb4;
			segment.setColorCategory(SegmentColorCategory.CLIMB);
			return color;
		}
		if (high > roundedIntervals[i]) {
			color = climbMax;
			segment.setColorCategory(SegmentColorCategory.STEEP_CLIMB);
			return color;
		}

		return color;
	}

	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		// Do nothing
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

}
