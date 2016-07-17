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
package com.henriquemalheiro.trackit.business.domain;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.view.data.DataType;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderTreeItem;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayer;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainter;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterFactory;
import com.miguelpernas.trackit.business.domain.SegmentColorCategory;
import com.pg58406.trackit.business.common.ColorSchemeV2;
import com.pg58406.trackit.business.common.ColorSchemeV2Container;

public class TrackSegment extends TrackItBaseType implements DocumentItem, FolderTreeItem, ColorSchemeV2Container, Comparable<TrackSegment> {
	private static ImageIcon icon = ImageUtilities.createImageIcon("events_16.png");

	private Double startLatitude;
	private Double startLongitude;
	private Double startAltitude;
	private Double endLatitude;
	private Double endLongitude;
	private Double endAltitude;
	private Double minimumAltitude;
	private Double maximumAltitude;
	private Date startTime;
	private Date endTime;
	private Double time;
	private Double distance;
	private Double averageSpeed;
	private Double maximumSpeed;
	private Double averageGrade;
	private Double minimumGrade;
	private Double maximumGrade;
	private Short averageHeartRate;
	private Short minimumHeartRate;
	private Short maximumHeartRate;
	private Short averageCadence;
	private Short maximumCadence;
	private Short averageRunningCadence;
	private Short maximumRunningCadence;
	private Integer averagePower;
	private Integer maximumPower;
	private Byte averageTemperature;
	private Byte minimumTemperature;
	private Byte maximumTemperature;
	private Integer altitudeDifference;
	private Integer totalAscent;
	private Integer totalDescent;
	private SegmentType type;
	private SegmentCategory category;
	private Double density;
	private DocumentItem parent;
	private ColorSchemeV2 colorScheme;// 58406
	private SegmentColorCategory colorCategory; //57421
	private double[] intervals; //57421

	private double climbDistanceLowerLimit;
	private double climbGradeLowerLimit;
	private double descentDistanceLowerLimit;
	private double descentGradeLowerLimit;

	private List<Trackpoint> trackpoints;

	public TrackSegment(DocumentItem parent) {
		super();
		this.parent = parent;

		trackpoints = new ArrayList<Trackpoint>();
		category = SegmentCategory.UNCATEGORIZED_SEGMENT;
		colorCategory = SegmentColorCategory.UNCATEGORIZED;

		climbDistanceLowerLimit = 400.0;
		climbGradeLowerLimit = 1.9;
		descentDistanceLowerLimit = 150.0;
		descentGradeLowerLimit = -3.5;
		intervals = new double[2];
	}
	
	public static void switchColors(List<TrackSegment> segments) {
		int min = 0;
		int max = 255;
		int red = new Random().nextInt(max - min + 1) + min;
		int green = new Random().nextInt(max - min + 1) + min;
		int blue = new Random().nextInt(max - min + 1) + min;
		int alpha = max;
		List<Color> colorsUsed = new ArrayList<Color>();
		Color color;
		for (TrackSegment segment : segments) {
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
				segment.setColorSchemeV2(colorScheme);

				red = new Random().nextInt(max - min + 1) + min;
				green = new Random().nextInt(max - min + 1) + min;
				blue = new Random().nextInt(max - min + 1) + min;

			}
		}
	}

	public Double getStartLatitude() {
		return startLatitude;
	}

	public void setStartLatitude(Double startLatitude) {
		this.startLatitude = startLatitude;
	}

	public Double getStartLongitude() {
		return startLongitude;
	}

	public void setStartLongitude(Double startLongitude) {
		this.startLongitude = startLongitude;
	}

	public Double getStartAltitude() {
		return startAltitude;
	}

	public void setStartAltitude(Double startAltitude) {
		this.startAltitude = startAltitude;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Double getEndLatitude() {
		return endLatitude;
	}

	public void setEndLatitude(Double endLatitude) {
		this.endLatitude = endLatitude;
	}

	public Double getEndLongitude() {
		return endLongitude;
	}

	public void setEndLongitude(Double endLongitude) {
		this.endLongitude = endLongitude;
	}

	public Double getEndAltitude() {
		return endAltitude;
	}

	public void setEndAltitude(Double endAltitude) {
		this.endAltitude = endAltitude;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Double getTime() {
		return time;
	}

	public void setTime(Double time) {
		this.time = time;
	}

	public Double getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(Double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	public Double getMaximumSpeed() {
		return maximumSpeed;
	}

	public void setMaximumSpeed(Double maximumSpeed) {
		this.maximumSpeed = maximumSpeed;
	}

	public Double getAverageGrade() {
		return averageGrade;
	}

	public void setAverageGrade(Double averageGrade) {
		this.averageGrade = averageGrade;
	}

	public Double getMaximumGrade() {
		return maximumGrade;
	}

	public void setMaximumGrade(Double maximumGrade) {
		this.maximumGrade = maximumGrade;
	}

	public Double getMinimumGrade() {
		return minimumGrade;
	}

	public void setMinimumGrade(Double minimumGrade) {
		this.minimumGrade = minimumGrade;
	}

	public Short getAverageHeartRate() {
		return averageHeartRate;
	}

	public void setAverageHeartRate(Short averageHeartRate) {
		this.averageHeartRate = averageHeartRate;
	}

	public Short getMinimumHeartRate() {
		return minimumHeartRate;
	}

	public void setMinimumHeartRate(Short minimumHeartRate) {
		this.minimumHeartRate = minimumHeartRate;
	}

	public Short getMaximumHeartRate() {
		return maximumHeartRate;
	}

	public void setMaximumHeartRate(Short maximumHeartRate) {
		this.maximumHeartRate = maximumHeartRate;
	}

	public Short getAverageCadence() {
		return averageCadence;
	}

	public void setAverageCadence(Short averageCadence) {
		this.averageCadence = averageCadence;
	}

	public Short getMaximumCadence() {
		return maximumCadence;
	}

	public void setMaximumCadence(Short maximumCadence) {
		this.maximumCadence = maximumCadence;
	}

	public Short getAverageRunningCadence() {
		return averageRunningCadence;
	}

	public void setAverageRunningCadence(Short averageRunningCadence) {
		this.averageRunningCadence = averageRunningCadence;
	}

	public Short getMaximumRunningCadence() {
		return maximumRunningCadence;
	}

	public void setMaximumRunningCadence(Short maximumRunningCadence) {
		this.maximumRunningCadence = maximumRunningCadence;
	}

	public Integer getAveragePower() {
		return averagePower;
	}

	public void setAveragePower(Integer averagePower) {
		this.averagePower = averagePower;
	}

	public Integer getMaximumPower() {
		return maximumPower;
	}

	public void setMaximumPower(Integer maximumPower) {
		this.maximumPower = maximumPower;
	}

	public Byte getAverageTemperature() {
		return averageTemperature;
	}

	public void setAverageTemperature(Byte averageTemperature) {
		this.averageTemperature = averageTemperature;
	}

	public Byte getMinimumTemperature() {
		return minimumTemperature;
	}

	public void setMinimumTemperature(Byte minimumTemperature) {
		this.minimumTemperature = minimumTemperature;
	}

	public Byte getMaximumTemperature() {
		return maximumTemperature;
	}

	public void setMaximumTemperature(Byte maximumTemperature) {
		this.maximumTemperature = maximumTemperature;
	}

	public Integer getAltitudeDifference() {
		return altitudeDifference;
	}

	public void setAltitudeDifference(Integer altitudeDifference) {
		this.altitudeDifference = altitudeDifference;
	}

	public Integer getTotalAscent() {
		return totalAscent;
	}

	public void setTotalAscent(Integer totalAscent) {
		this.totalAscent = totalAscent;
	}

	public Integer getTotalDescent() {
		return totalDescent;
	}

	public void setTotalDescent(Integer totalDescent) {
		this.totalDescent = totalDescent;
	}

	@Override
	public List<Trackpoint> getTrackpoints() {
		return trackpoints;
	}

	public void setTrackpoints(List<Trackpoint> trackpoints) {
		this.trackpoints = trackpoints;
		consolidate(ConsolidationLevel.RECALCULATION);
	}

	public void addTrackpoints(List<Trackpoint> trackpoints) {
		this.trackpoints.addAll(trackpoints);
	}

	public SegmentType getType() {
		return type;
	}

	public void setType(SegmentType type) {
		this.type = type;
	}

	public SegmentCategory getCategory() {
		return category;
	}

	public void setCategory(SegmentCategory segmentCategory) {
		this.category = segmentCategory;
	}
	
	public SegmentColorCategory getColorCategory() {
		return colorCategory;
	}

	public void setColorCategory(SegmentColorCategory colorCategory) {
		this.colorCategory = colorCategory;
	}

	public Double getDensity() {
		return density;
	}

	public void setDensity(Double density) {
		this.density = density;
	}

	public Double getMinimumAltitude() {
		return minimumAltitude;
	}

	public void setMinimumAltitude(Double minimumAltitude) {
		this.minimumAltitude = minimumAltitude;
	}

	public Double getMaximumAltitude() {
		return maximumAltitude;
	}

	public void setMaximumAltitude(Double maximumAltitude) {
		this.maximumAltitude = maximumAltitude;
	}

	public double getClimbDistanceLowerLimit() {
		return climbDistanceLowerLimit;
	}

	public void setClimbDistanceLowerLimit(double climbDistanceLowerLimit) {
		this.climbDistanceLowerLimit = climbDistanceLowerLimit;
	}

	public double getClimbGradeLowerLimit() {
		return climbGradeLowerLimit;
	}

	public void setClimbGradeLowerLimit(double climbGradeLowerLimit) {
		this.climbGradeLowerLimit = climbGradeLowerLimit;
	}

	public double getDescentDistanceLowerLimit() {
		return descentDistanceLowerLimit;
	}

	public void setDescentDistanceLowerLimit(double descentDistanceLowerLimit) {
		this.descentDistanceLowerLimit = descentDistanceLowerLimit;
	}

	public double getDescentGradeLowerLimit() {
		return descentGradeLowerLimit;
	}

	public void setDescentGradeLowerLimit(double descentGradeLowerLimit) {
		this.descentGradeLowerLimit = descentGradeLowerLimit;
	}

	//57421
	public double getLowerInterval(){
		return intervals[0];
	}
	
	//57421
	public double getHigherInterval(){
		return intervals[1];
	}
	
	//57421
	public void setInterval(double low, double high){
		intervals[0] = low;
		intervals[1] = high;
	}
	
	@Override
	public DocumentItem getParent() {
		return parent;
	}

	public void setParent(DocumentItem parent) {
		this.parent = parent;
	}

	@Override
	public String getDocumentItemName() {
		return getFolderTreeItemName();
	}

	@Override
	public String toString() {
		if (getTrackpoints().size() == 0) {
			return "[segment]";
		}

		StringBuffer sb = new StringBuffer();

		Trackpoint firstTrackpoint = getTrackpoints().get(0);
		Trackpoint lastTrackpoint = getTrackpoints().get(getTrackpoints().size() - 1);

		sb.append("[Segment]:");
		sb.append("[")
		        .append(Formatters.getFormatedDistance(firstTrackpoint.getDistance()
		                - firstTrackpoint.getDistanceFromPrevious())).append(",");
		sb.append(Formatters.getFormatedDistance(lastTrackpoint.getDistance())).append("]");
		sb.append(" Distance: " + Formatters.getDecimalFormat(3).format(getDistance() / 1000)).append(" km ");
		sb.append(" Time: " + (getTime() != null ? Formatters.getFormatedDuration(getTime()) : ""));
		sb.append(" Avg Grade: " + Formatters.getDecimalFormat(1).format(getAverageGrade()));
		sb.append(" Min Grade: " + Formatters.getDecimalFormat(1).format(getMinimumGrade()));
		sb.append(" Max Grade: " + Formatters.getDecimalFormat(1).format(getMaximumGrade()));
		sb.append(" Type: " + getType().toString());
		sb.append(" Category: " + getCategory().toString());
		sb.append(" Color Category: " + getColorCategory().toString());
		sb.append("(" + trackpoints.get(0).getId() + "," + trackpoints.get(trackpoints.size() - 1).getId() + ")");

		return sb.toString();
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.SEGMENT_SELECTED, this);
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	@Override
	public void consolidate(ConsolidationLevel level) {
		if (trackpoints.isEmpty()) {
			reset();
		} else {
			update();
		}
	}

	private void reset() {
		setDistance(0.0);
		setTime(0.0);
		setAverageSpeed(0.0);
		setMaximumSpeed(0.0);
		setStartTime(null);
		setEndTime(null);
		setStartLatitude(null);
		setStartLongitude(null);
		setStartAltitude(null);
		setEndLatitude(null);
		setEndLongitude(null);
		setEndAltitude(null);
		setMinimumAltitude(null);
		setMaximumAltitude(null);
		setAverageHeartRate(null);
		setMinimumHeartRate(null);
		setMaximumHeartRate(null);
		setAverageCadence(null);
		setMaximumCadence(null);
		setAverageRunningCadence(null);
		setMaximumRunningCadence(null);
		setAveragePower(null);
		setMaximumPower(null);
		setAverageTemperature(null);
		setMinimumTemperature(null);
		setMaximumTemperature(null);
		setAverageGrade(null);
		setMinimumGrade(null);
		setMaximumGrade(null);
		setAltitudeDifference(null);
		setTotalAscent(null);
		setTotalDescent(null);
		setDensity(null);
		setType(SegmentType.FLAT);
		setCategory(SegmentCategory.UNCATEGORIZED_SEGMENT);
		setColorCategory(SegmentColorCategory.UNCATEGORIZED);
	}

	private void update() {
		Trackpoint firstTrackpoint = trackpoints.get(0);
		Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);

		setDistance(lastTrackpoint.getDistance() - firstTrackpoint.getDistance());
		if (firstTrackpoint.getTimestamp() != null && lastTrackpoint.getTimestamp() != null) {
			setTime((lastTrackpoint.getTimestamp().getTime() - firstTrackpoint.getTimestamp().getTime()) / 1000.0);
			setAverageSpeed(getDistance() / getTime());
		} else {
			setTime(null);
			setAverageSpeed(null);
		}
		setStartTime(firstTrackpoint.getTimestamp());
		setEndTime(lastTrackpoint.getTimestamp());
		setStartLatitude(firstTrackpoint.getLatitude());
		setStartLongitude(firstTrackpoint.getLongitude());
		setStartAltitude(firstTrackpoint.getAltitude());
		setEndLatitude(lastTrackpoint.getLatitude());
		setEndLongitude(lastTrackpoint.getLongitude());
		setEndAltitude(lastTrackpoint.getAltitude());
		updateSummaryData();
		updateGrades();
		setSegmentType();
		setCategory(calculateCategory());
		setColorCategory(calculateColorCategory());
	}

	private void updateSummaryData() {
		double maximumSpeed = Double.MIN_VALUE;
		double averageHeartRate = 0.0;
		short minimumHeartRate = Short.MAX_VALUE;
		short maximumHeartRate = Short.MIN_VALUE;
		double averageCadence = 0.0;
		short maximumCadence = Short.MIN_VALUE;
		double averageRunningCadence = 0.0;
		short maximumRunningCadence = Short.MIN_VALUE;
		double averagePower = 0.0;
		int maximumPower = Integer.MIN_VALUE;
		double averageTemperature = 0.0;
		byte minimumTemperature = Byte.MAX_VALUE;
		byte maximumTemperature = Byte.MIN_VALUE;
		double minimumAltitude = Double.MAX_VALUE;
		double maximumAltitude = Double.MIN_VALUE;

		double factor;
		for (Trackpoint trackpoint : getTrackpoints()) {
			factor = trackpoint.getTimeFromPrevious() / getTime();

			if (trackpoint.getSpeed() != null) {
				maximumSpeed = Math.max(maximumSpeed, trackpoint.getSpeed());
			}

			if (trackpoint.getHeartRate() != null) {
				averageHeartRate += trackpoint.getHeartRate() * factor;
				minimumHeartRate = (short) Math.min(minimumHeartRate, trackpoint.getHeartRate());
				maximumHeartRate = (short) Math.max(maximumHeartRate, trackpoint.getHeartRate());
			}

			if (trackpoint.getCadence() != null) {
				averageCadence += trackpoint.getCadence() * factor;
				maximumCadence = (short) Math.max(maximumCadence, trackpoint.getCadence());
			}

			if (trackpoint.getPower() != null) {
				averagePower += trackpoint.getPower() * factor;
				maximumPower = Math.max(maximumPower, trackpoint.getPower());
			}

			if (trackpoint.getTemperature() != null) {
				averageTemperature += trackpoint.getTemperature() * factor;
				minimumTemperature = (byte) Math.min(minimumTemperature, trackpoint.getTemperature());
				maximumTemperature = (byte) Math.max(maximumTemperature, trackpoint.getTemperature());
			}

			if (trackpoint.getAltitude() != null) {
				minimumAltitude = Math.min(minimumAltitude, trackpoint.getAltitude());
				maximumAltitude = Math.max(maximumAltitude, trackpoint.getAltitude());
			}
		}

		setMaximumSpeed(maximumSpeed != Double.MIN_VALUE ? maximumSpeed : null);
		setAverageHeartRate(averageHeartRate > 0.0 ? (short) averageHeartRate : null);
		setMinimumHeartRate(minimumHeartRate != Short.MAX_VALUE ? minimumHeartRate : null);
		setMaximumHeartRate(maximumHeartRate != Short.MIN_VALUE ? maximumHeartRate : null);
		setAverageCadence(averageCadence > 0.0 ? (short) averageCadence : null);
		setMaximumCadence(maximumCadence != Short.MIN_VALUE ? maximumCadence : null);
		setAverageRunningCadence(averageRunningCadence > 0.0 ? (short) averageRunningCadence : null);
		setMaximumRunningCadence(maximumRunningCadence != Short.MIN_VALUE ? maximumRunningCadence : null);
		setAveragePower(averagePower > 0.0 ? (int) averagePower : null);
		setMaximumPower(maximumPower != Integer.MIN_VALUE ? maximumPower : null);
		setAverageTemperature(averageTemperature != 0.0 ? (byte) averageTemperature : null);
		setMinimumTemperature(minimumTemperature != Byte.MAX_VALUE ? minimumTemperature : null);
		setMaximumTemperature(maximumTemperature != Byte.MIN_VALUE ? maximumTemperature : null);
		setMinimumAltitude(minimumAltitude != Double.MAX_VALUE ? minimumAltitude : null);
		setMaximumAltitude(maximumAltitude != Double.MIN_VALUE ? maximumAltitude : null);
		setDensity(trackpoints.size() / distance);
	}

	private void updateGrades() {
		double minGrade = 100.0;
		double avgGrade = 0.0;
		double maxGrade = -100.0;
		double segmentAltitudeDifference = 0.0;
		double totalAscent = 0.0;
		double totalDescent = 0.0;

		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getGrade() == null) {
				setAverageGrade(0.0);
				setMinimumGrade(0.0);
				setMaximumGrade(0.0);
				setAltitudeDifference(0);
				setTotalAscent(0);
				setTotalDescent(0);
				return;
			}

			minGrade = Math.min(trackpoint.getGrade(), minGrade);
			maxGrade = Math.max(trackpoint.getGrade(), maxGrade);
		}

		if (trackpoints.size() > 0) {
			double segmentDistance = trackpoints.get(trackpoints.size() - 1).getDistance()
			        - trackpoints.get(0).getDistance();
			if (segmentDistance == 0) {
				avgGrade = minGrade;
			} else {
				segmentAltitudeDifference = trackpoints.get(trackpoints.size() - 1).getAltitude()
				        - trackpoints.get(0).getAltitude();
				avgGrade = segmentAltitudeDifference / segmentDistance * 100;
			}
		} else {
			avgGrade = 0.0;
		}

		double lastAltitude = 0;
		boolean firstTrackpoint = true;
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getAltitude() != null) {
				if (firstTrackpoint) {
					firstTrackpoint = false;
				} else {
					Double altitudeDifference = trackpoint.getAltitude() - lastAltitude;

					if (altitudeDifference > 0.0) {
						totalAscent += altitudeDifference;
					} else if (altitudeDifference < 0.0) {
						totalDescent -= altitudeDifference;
					}
				}

				lastAltitude = trackpoint.getAltitude();
			}
		}

		setAverageGrade(avgGrade);
		setMinimumGrade(minGrade);
		setMaximumGrade(maxGrade);
		setAltitudeDifference((int) Math.ceil(segmentAltitudeDifference));
		setTotalAscent((int) Math.ceil(totalAscent));
		setTotalDescent((int) Math.ceil(totalDescent));
	}

	private void setSegmentType() {
		double avgGrade = calculateAvgGrade();

		if (avgGrade >= climbGradeLowerLimit) {
			setType(SegmentType.CLIMB);
		} else if (avgGrade <= descentGradeLowerLimit) {
			setType(SegmentType.DESCENT);
		} else {
			setType(SegmentType.FLAT);
		}
	}

	private double calculateAvgGrade() {
		double avgGrade = 0.0;
		int count = 0;

		for (Trackpoint trackpoint : trackpoints) {
			Float grade = trackpoint.getGrade();

			if (grade != null) {
				avgGrade += grade;
				count++;
			}
		}

		avgGrade /= count;

		return avgGrade;
	}

	private SegmentCategory calculateCategory() {
		SegmentCategory category = SegmentCategory.UNCATEGORIZED_SEGMENT;

		switch (type) {
		case CLIMB:
			category = calculateClimbCategory();
			break;
		case DESCENT:
			category = calculateDescentCategory();
			break;
		default:
			// do nothing
		}

		return category;
	}
	private SegmentColorCategory calculateColorCategory() {
		SegmentColorCategory category = SegmentColorCategory.UNCATEGORIZED;

		/*switch (type) {
		case CLIMB:
			category = SegmentColorCategory.CLIMB;
			break;
		case DESCENT:
			category = SegmentColorCategory.DESCENT;
			break;
		case FLAT:
			category = SegmentColorCategory.FLAT;
			break;
		default:
			// do nothing
		}*/

		return category;
	}

	private SegmentCategory calculateClimbCategory() {
		if (isHorsCategoryClimb()) {
			return SegmentCategory.HORS_CATEGORY_CLIMB;
		} else if (isFirstCategoryClimb()) {
			return SegmentCategory.FIRST_CATEGORY_CLIMB;
		} else if (isSecondCategoryClimb()) {
			return SegmentCategory.SECOND_CATEGORY_CLIMB;
		} else if (isThirdCategoryClimb()) {
			return SegmentCategory.THIRD_CATEGORY_CLIMB;
		} else if (isFourthCategoryClimb()) {
			return SegmentCategory.FOURTH_CATEGORY_CLIMB;
		} else {
			return SegmentCategory.UNCATEGORIZED_CLIMB;
		}
	}

	private boolean isHorsCategoryClimb() {
		boolean horsCategory = false;
		horsCategory |= (between(distance, 10000.0, 15000.0) && averageGrade >= 7.0);
		horsCategory |= (between(distance, 15000.0, 20000.0) && averageGrade >= 6.0);
		horsCategory |= (distance >= 20000.0 && averageGrade >= 5.0);

		return horsCategory;
	}

	private boolean isFirstCategoryClimb() {
		boolean firstCategory = false;
		firstCategory |= (between(distance, 7000.0, 11000.0) && averageGrade >= 6.5);
		firstCategory |= (distance >= 11000.0 && averageGrade >= 5.0);

		return firstCategory;
	}

	private boolean isSecondCategoryClimb() {
		boolean secondCategory = false;
		secondCategory |= (between(distance, 5000.0, 7000.0) && averageGrade >= 5.0);
		secondCategory |= (distance >= 7000.0 && averageGrade >= 3.5);

		return secondCategory;
	}

	private boolean isThirdCategoryClimb() {
		boolean thirdCategory = false;
		thirdCategory |= (between(distance, 500.0, 1000.0) && averageGrade >= 7.5);
		thirdCategory |= (between(distance, 1000.0, 2000.0) && averageGrade >= 6.5);
		thirdCategory |= (between(distance, 2000.0, 4000.0) && averageGrade >= 4.5);
		thirdCategory |= (between(distance, 4000.0, 7500.0) && averageGrade >= 3.5);

		return thirdCategory;
	}

	private boolean isFourthCategoryClimb() {
		boolean fourthCategory = false;
		fourthCategory |= (between(distance, climbDistanceLowerLimit, 1000.0) && averageGrade > 6.5);
		fourthCategory |= (between(distance, 1000, 2500.0) && averageGrade >= 4.5);
		fourthCategory |= (between(distance, 2500.0, 7000.0) && averageGrade >= climbGradeLowerLimit);

		return fourthCategory;
	}

	private SegmentCategory calculateDescentCategory() {
		return isCategorizedDescent() ? SegmentCategory.CATEGORIZED_DESCENT : SegmentCategory.UNCATEGORIZED_DESCENT;
	}

	private boolean isCategorizedDescent() {
		boolean categorizedDescent = false;
		categorizedDescent |= (between(distance, descentDistanceLowerLimit, 900.0) && minimumGrade <= -15.0);
		categorizedDescent |= (distance >= 900.0 && averageGrade <= descentGradeLowerLimit);

		return categorizedDescent;
	}

	private boolean between(double value, double lowerLimit, double upperLimit) {
		return (value >= lowerLimit && value <= upperLimit);
	}

	/* Paintable Interface */

	@Override
	public void paint(Graphics2D graphics, MapLayer layer, Map<String, Object> paintingAttributes) {
		MapPainter painter = MapPainterFactory.getInstance().getMapPainter(layer, this);
		painter.paint(graphics, paintingAttributes);
	}

	@Override
	public List<DataType> getDisplayableElements() {
		return Arrays.asList(new DataType[] { DataType.TRACK_SEGMENT, DataType.TRACKPOINT });
	}

	@Override
	public List<? extends DocumentItem> getDisplayedElements(DataType dataType) {
		switch (dataType) {
		case TRACK_SEGMENT:
			return Arrays.asList(new DocumentItem[] { this });
		case TRACKPOINT:
			return getTrackpoints();
		default:
			return Collections.emptyList();
		}
	}

	/* Folder Tree Item Interface Implementation */
	@Override
	public String getFolderTreeItemName() {
		String name = null;
		if ((isClimb() || isDescent()) && (!uncategorized() || colorUncategorized())) {
			name = String.format("%s (%.1f%%)", category, averageGrade);
		
		}
		else if(!colorUncategorized()){
			if(isSteepClimb()){
				name = String.format("%s (> %.1f%%)", colorCategory, getLowerInterval());
			}
			else if(isSteepDescent()){
				name = String.format("%s (< %.1f%%)", colorCategory, getHigherInterval());
			}
			else if(isMechanicalClimb()){
				name = String.format("%s", colorCategory);
			}
			else{
				name = String.format("%s (%.1f%%, %.1f%%)", colorCategory, getLowerInterval(), getHigherInterval());
			}
		} else {
			name = getType().toString();
		}
		return name;
	}
	
	private boolean colorUncategorized() {
		return Objects.equals(SegmentColorCategory.UNCATEGORIZED, colorCategory);
	}

	private boolean isClimb() {
		return Objects.equals(SegmentType.CLIMB, type);
	}

	private boolean isDescent() {
		return Objects.equals(SegmentType.DESCENT, type);
	}
	
	private boolean isMechanicalClimb() {
		return Objects.equals(SegmentColorCategory.MECHANICAL_CLIMB, colorCategory);
	}
	
	private boolean isSteepClimb() {
		return Objects.equals(SegmentColorCategory.STEEP_CLIMB, colorCategory);
	}
	
	private boolean isSteepDescent() {
		return Objects.equals(SegmentColorCategory.STEEP_DESCENT, colorCategory);
	}

	private boolean uncategorized() {
		return Objects.equals(SegmentCategory.UNCATEGORIZED_SEGMENT, category)
		        || Objects.equals(SegmentCategory.UNCATEGORIZED_CLIMB, category)
		        || Objects.equals(SegmentCategory.UNCATEGORIZED_DESCENT, category);
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
	public boolean isSegment(){
		return true;
	}

	@Override
	public ColorSchemeV2 getColorSchemeV2() {
		return colorScheme;
	}
	@Override
	public void setColorSchemeV2(ColorSchemeV2 colorScheme) {
		this.colorScheme = colorScheme;
	}

	@Override
	public int compareTo(TrackSegment another) {
		if(this.getLowerInterval() < another.getLowerInterval()){
			return -1;
		}
		else{
			return 1;
		}
	}
}
