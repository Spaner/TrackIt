/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes, J M Brisson Lopes
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
package com.trackit.presentation.view.chart;

import static com.trackit.business.common.Messages.getMessage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.ColorScheme;
import com.trackit.business.common.ColorSchemeV2;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Messages;
import com.trackit.business.common.Pair;
import com.trackit.business.common.Predicate;
import com.trackit.business.common.Unit;
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.SegmentCategory;
import com.trackit.business.domain.SegmentColorCategory;
import com.trackit.business.domain.SegmentType;
import com.trackit.business.domain.TrackSegment;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.operation.AltitudeSmoothingOperation;
import com.trackit.business.utilities.Utilities;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;

public class ChartView extends JPanel implements EventListener, EventPublisher {
	private static final long serialVersionUID = -1172194726510817404L;

	private JPanel chartPanel;
	private Chart chart;
	private ChartType chartType;
	private ChartMode chartMode;

	enum ChartType {
		TIME, DISTANCE
	};

	enum ChartMode {
		SELECTION, INFO
	};

	private boolean showElevationProfile;
	private boolean showSpeedProfile;
	private boolean showHeartRateProfile;
	private boolean showCadenceProfile;
	private boolean showPowerProfile;
	private boolean showTemperatureProfile;
	private boolean showGrade;
	private boolean showCoursePoints;
	private boolean showClimbs;
	private boolean showDescents;
	private boolean showSmoothData;
	private boolean showGradeProfile;

	private boolean zoomed;

	private DocumentItem item;
	private DocumentItem selection;
	private Trackpoint highlight;
	private double[] distances;
	private double[] durations;

	private JToolBar mapToolbar;// 58406

	ChartMode getMode() {
		return chartMode;
	}

	public ChartView() {
		setLayout(new BorderLayout());

		init();
	}

	List<GPSDocument> getDocuments() {
		return DocumentManager.getInstance().getDocuments();
	}

	ChartType getChartMode() {
		return chartType;
	}
	
	//12335: 2017-07-18
	public boolean showElevation() {
		return showElevationProfile;
	}
	
	//12335: 2017-07-18
	public boolean showSpeed() {
		return showSpeedProfile;
	}
	
	//12335: 2017-07-18
	public boolean showHeartRate() {
		return showHeartRateProfile;
	}

	private void setItem(DocumentItem item) {
		this.item = item;
		zoomed = false;

		updateDistancesAndDurations();
	}

	private void updateDistancesAndDurations() {
		List<Trackpoint> trackpoints = item.getTrackpoints();
		distances = new double[trackpoints.size()];
		durations = new double[trackpoints.size()];

		if (trackpoints.size() > 0) {
			double initialTimestamp = trackpoints.get(0).getTimestamp().getTime();

			for (int i = 0; i < trackpoints.size(); i++) {
				distances[i] = trackpoints.get(i).getDistance();

				durations[i] = trackpoints.get(i).getTimestamp().getTime() - initialTimestamp;
				durations[i] /= 1000.0;
			}
		}
	}

	private void clearItem() {
		item = null;
	}

	private void setSelection(DocumentItem item) {
		selection = item;
	}

	private void clearSelection() {
		selection = null;
	}

	private void clearHighlight() {
		highlight = null;
	}

	Trackpoint selectByDistance(double distance) {
		if (item == null) {
			return null;
		}

		Trackpoint selectedTrackpoint = getTrackpointAtDistance(distance);
		if (selectedTrackpoint != null) {
			((DocumentItem) selectedTrackpoint).publishSelectionEvent(this);
			displaySelectedTrackpoint(selectedTrackpoint);
		}

		return selectedTrackpoint;
	}

//	private Trackpoint getTrackpointAtDistance(double distance) {
	//12335: 2017-07-18
	//		method is now public
	//		body moved to a try catch block to handle exceptions
	public Trackpoint getTrackpointAtDistance(double distance) {
		try {
			int index = Arrays.binarySearch(distances, distance);
			if (index < 0) {
				index = Math.abs(index) - 2;
				index = Math.max(0, index);
			}
//System.out.println( "Dist: " + distance + "  " + item.getTrackpoints().get(index).getDistance());
			return item.getTrackpoints().get(index);
		} catch (Exception e) {
			return null;
		}
	}

	TrackSegment selectByDistance(double initialDistance, double finalDistance) {
		if (item == null) {
			return null;
		}

		Trackpoint initialTrackpoint = getTrackpointAtDistance(initialDistance);
		Trackpoint finalTrackpoint = getTrackpointAtDistance(finalDistance);

		TrackSegment segment = null;
		if (!finalTrackpoint.equals(initialTrackpoint)) {
			segment = new TrackSegment(item);
			segment.setTrackpoints(item.getTrackpoints().subList(item.getTrackpoints().indexOf(initialTrackpoint),
					item.getTrackpoints().indexOf(finalTrackpoint) + 1));

			display(item, segment);
			setSelection(segment);
			segment.publishSelectionEvent(null);
		}

		highlight = null;

		return segment;
	}

	Trackpoint selectByDuration(double duration) {
		if (item == null) {
			return null;
		}

		Trackpoint selectedTrackpoint = getTrackpointAtDuration(duration);
		if (selectedTrackpoint != null) {
			((DocumentItem) selectedTrackpoint).publishSelectionEvent(this);
			displaySelectedTrackpoint(selectedTrackpoint);
		}

		return selectedTrackpoint;
	}

	/*
//	private Trackpoint getTrackpointAtDuration(double duration) {
	public Trackpoint getTrackpointAtDuration(double duration) {
		int index = Arrays.binarySearch(durations, duration);
		if (index < 0) {
			index = Math.abs(index) - 2;
			index = Math.max(0, index);
		}
		return item.getTrackpoints().get(index);
	}
	*/
	
	//12335: 2017-07-18
	//		method is now public
	//		body moved to a try catch block to handle exceptions
	public Trackpoint getTrackpointAtDuration( double duration) {
		try {
			int index = Arrays.binarySearch( durations, duration);
			if (index < 0) {
				index = Math.abs(index) - 2;
				index = Math.max(0, index);
			}
			return item.getTrackpoints().get(index);
		}
		catch (Exception e) {
			return null;
		}
	}

	TrackSegment selectByDuration(double initialDuration, double finalDuration) {
		if (item == null) {
			return null;
		}

		Trackpoint initialTrackpoint = getTrackpointAtDuration(initialDuration);
		Trackpoint finalTrackpoint = getTrackpointAtDuration(finalDuration);

		TrackSegment segment = null;
		if (!finalTrackpoint.equals(initialTrackpoint)) {
			segment = new TrackSegment(item);
			segment.setTrackpoints(item.getTrackpoints().subList(item.getTrackpoints().indexOf(initialTrackpoint),
					item.getTrackpoints().indexOf(finalTrackpoint) + 1));

			display(item, segment);
			setSelection(segment);
			segment.publishSelectionEvent(null);
		}

		highlight = null;

		return segment;
	}

	void highlightByDistance(double distance) {
		if (item == null) {
			return;
		} else {
			Trackpoint highlightedTrackpoint = getTrackpointAtDistance(distance);
			highlightTrackpoint(highlightedTrackpoint);
		}
	}

	void highlightByDuration(double duration) {
		if (item == null) {
			return;
		} else {
			Trackpoint highlightedTrackpoint = getTrackpointAtDuration(duration);
			highlightTrackpoint(highlightedTrackpoint);
		}
	}

	private void highlightTrackpoint(Trackpoint trackpoint) {
		if (trackpoint != null) {
			trackpoint.publishHighlightEvent(null);
		} else {
			EventManager.getInstance().publish(null,
					com.trackit.presentation.event.Event.TRACKPOINT_HIGHLIGHTED, null);
		}

		displayHighlightedTrackpoint(trackpoint);
	}

	private void init() {
		chartPanel = new JPanel();
		chartPanel.setLayout(new GridLayout(1, 0));

		String type = TrackIt.getPreferences().getPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.CHART_TYPE, "DISTANCE");
		chartType = ChartType.valueOf(type);

		String mode = TrackIt.getPreferences().getPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.CHART_MODE, "SELECTION");
		chartMode = ChartMode.valueOf(mode);

		Scale scale = new Scale(0, 100, true);

		chart = new Chart(this, scale);
		chartPanel.add(chart);

		showSpeedProfile = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.SHOW_SPEED_PROFILE, true);
		showElevationProfile = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.SHOW_ELEVATION_PROFILE, false);
		showHeartRateProfile = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.SHOW_HEART_RATE_PROFILE, false);
		showCadenceProfile = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.SHOW_CADENCE_PROFILE, false);
		showPowerProfile = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.SHOW_POWER_PROFILE, false);
		showTemperatureProfile = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.SHOW_TEMPERATURE_PROFILE, false);
		showGradeProfile = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.SHOW_GRADE_PROFILE, false);

		showSmoothData = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.SHOW_SMOOTHED_DATA, false);

		showGrade = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.SHOW_GRADE, false);

		mapToolbar = createJToolBar();

		add(mapToolbar, BorderLayout.PAGE_START);
		add(chartPanel, BorderLayout.CENTER);

		item = null;
		selection = null;
	}

	boolean isShowGrade() {
		return showGrade;
	}

	private void display(DocumentItem item) {
		display(item.getTrackpoints(), new ArrayList<Trackpoint>(), item.getSegments());
		showCoursePoints(item);
	}

	private void display(DocumentItem item, DocumentItem selection) {
		List<Trackpoint> selectionTrackpoints = (selection == null ? Collections.<Trackpoint> emptyList()
				: selection.getTrackpoints());

		display(item.getTrackpoints(), selectionTrackpoints, item.getSegments());
		showCoursePoints(item);
	}

	private void showCoursePoints(DocumentItem item) {
		DocumentItem rootParent = getRootItem(item);
		if (rootParent != null) {
			chart.setCoursePoints(getFilteredCoursePoints(rootParent.getCoursePoints()));
		}
	}

	private List<CoursePoint> getFilteredCoursePoints(List<CoursePoint> coursePoints) {
		return Utilities.filter(coursePoints, new Predicate<CoursePoint>() {
			@Override
			public boolean apply(CoursePoint coursePoint) {
				switch (coursePoint.getType()) {
				case HORS_CATEGORY:
				case FIRST_CATEGORY:
				case SECOND_CATEGORY:
				case THIRD_CATEGORY:
				case FOURTH_CATEGORY:
				case SUMMIT:
					return showClimbs;
				case SPRINT:
				case VALLEY:
					return showDescents;
				default:
					return showCoursePoints;
				}
			}
		});
	}

	private void clearDisplay() {
		clearItem();
		clearSelection();
		clearHighlight();
		chart.setScale(null);
		chart.setDataSeries(new ArrayList<DataSeries>());
		chart.validate();// 58406
		chart.repaint();
	}

	private void display(List<Trackpoint> trackpoints, List<Trackpoint> selection) {
		display(trackpoints, selection, new ArrayList<TrackSegment>());
	}

	private void display(List<Trackpoint> trackpoints, List<Trackpoint> selection, List<TrackSegment> segments) {
		if (trackpoints == null || trackpoints.isEmpty()) {
			chart.setScale(null);
			chart.setDataSeries(new ArrayList<DataSeries>());
			return;
		}

		Trackpoint firstTrackpoint = trackpoints.get(0);
		Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);

		if ((firstTrackpoint.getDistance() == null && firstTrackpoint.getTimestamp() == null)
				|| firstTrackpoint.getAltitude() == null) {
			clearDisplay();
			return;
		}

		if (firstTrackpoint.getTimestamp() == null) {
			chartType = ChartType.DISTANCE;
		}

		double minValue = 0.0;
		double maxValue = 0.0;

		if (chartType == ChartType.DISTANCE) {
			minValue = firstTrackpoint.getDistance();
			maxValue = lastTrackpoint.getDistance();
		} else if (chartType == ChartType.TIME) {
			long initialTime = firstTrackpoint.getTimestamp().getTime();
			minValue = 0;
			maxValue = (lastTrackpoint.getTimestamp().getTime() - initialTime) / 1000;
		}

		double minElevation = Double.MAX_VALUE;
		double maxElevation = Double.MIN_VALUE;
		double minHeartRate = Double.MAX_VALUE;
		double maxHeartRate = Double.MIN_VALUE;
		double minSpeed = Double.MAX_VALUE;
		double maxSpeed = Double.MIN_VALUE;
		double minCadence = Double.MAX_VALUE;
		double maxCadence = Double.MIN_VALUE;
		double minPower = Double.MAX_VALUE;
		double maxPower = Double.MIN_VALUE;
		double minTemperature = 0.0;
		double maxTemperature = 0.0;

		double[][] elevationData = new double[trackpoints.size()][2];
		double[][] heartRateData = new double[trackpoints.size()][2];
		double[][] speedData = new double[trackpoints.size()][2];
		double[][] cadenceData = new double[trackpoints.size()][2];
		double[][] powerData = new double[trackpoints.size()][2];
		double[][] temperatureData = new double[trackpoints.size()][2];

		long initialTime = 0;
		if (trackpoints.size() > 0 && trackpoints.get(0).getTimestamp() != null) {
			initialTime = trackpoints.get(0).getTimestamp().getTime() / 1000;
		}

		for (int i = 0; i < trackpoints.size(); i++) {
			Trackpoint trackpoint = trackpoints.get(i);

			if (trackpoint.getAltitude() == null) {
				continue;
			}

			minElevation = Math.min(minElevation, trackpoint.getAltitude());
			maxElevation = Math.max(maxElevation, trackpoint.getAltitude());

			if (trackpoint.getHeartRate() != null) {
				minHeartRate = Math.min(minHeartRate, trackpoint.getHeartRate());
				maxHeartRate = Math.max(maxHeartRate, trackpoint.getHeartRate());
				heartRateData[i][1] = trackpoint.getHeartRate();
			} else {
				heartRateData[i][1] = 0.0;
				minHeartRate = 0.0;
				maxHeartRate = 256.0;
			}

			if (trackpoint.getTemperature() != null) {
				minTemperature = Math.min(minTemperature, trackpoint.getTemperature());
				maxTemperature = Math.max(maxTemperature, trackpoint.getTemperature());
				temperatureData[i][1] = trackpoint.getTemperature();
			} else {
				temperatureData[i][1] = 0;
				minTemperature = 0.0;
				maxTemperature = 30.0;
			}

			if (trackpoint.getSpeed() != null) {
				double speed = trackpoint.getSpeed() * 3600.0 / 1000.0;

				minSpeed = Math.min(minSpeed, speed);
				maxSpeed = Math.max(maxSpeed, speed);
				speedData[i][1] = speed;
			} else {
				speedData[i][1] = 0.0;
				minSpeed = 0.0;
				maxSpeed = 50.0;
			}

			if (trackpoint.getPower() != null) {
				minPower = Math.min(minPower, trackpoint.getPower());
				maxPower = Math.max(maxPower, trackpoint.getPower());
				powerData[i][1] = trackpoint.getPower();
			} else {
				powerData[i][1] = 0.0;
				minPower = 0.0;
				maxPower = 400.0;
			}

			if (trackpoint.getCadence() != null) {
				minCadence = Math.min(minCadence, trackpoint.getCadence());
				maxCadence = Math.max(maxCadence, trackpoint.getCadence());
				cadenceData[i][1] = trackpoint.getCadence();
			} else {
				cadenceData[i][1] = 0;
				minCadence = 0.0;
				maxCadence = 100.0;
			}

			double xValue = (chartType == ChartType.DISTANCE ? trackpoint.getDistance()
					: (trackpoint.getTimestamp().getTime() / 1000 - initialTime));

			elevationData[i][0] = xValue;
			heartRateData[i][0] = xValue;
			speedData[i][0] = xValue;
			cadenceData[i][0] = xValue;
			powerData[i][0] = xValue;
			temperatureData[i][0] = xValue;

			elevationData[i][1] = trackpoint.getAltitude();
		}

		List<DataSeries> dataSeries = new ArrayList<DataSeries>();
		Scale scale;
		DataSeries series;

		if (showElevationProfile) {
			if (showSmoothData) {
				double elevationSmoothingFactor = TrackIt.getPreferences().getDoublePreference(
						Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.ELEVATION_SMOOTHING_FACTOR,
						34.0);
				AltitudeSmoothingOperation.smoothing(elevationData, elevationSmoothingFactor, true);
			}
			scale = new Scale(minElevation, maxElevation, 5, true);
			series = new DataSeries("Elevation", scale, Unit.METER, ColorScheme.LIGHT_GREEN, true, elevationData);

			if (!selection.isEmpty()) {
				series.setSelection(trackpoints.indexOf(selection.get(0)),
						trackpoints.indexOf(selection.get(selection.size() - 1)));
			}

			if (highlight != null) {
				int highlightIndex = trackpoints.indexOf(highlight);
				series.setHighlight(Pair.create(elevationData[highlightIndex][0], elevationData[highlightIndex][1]));
			} else {
				series.setHighlight(null);
			}

			if (!segments.isEmpty()) {
				SegmentColorCategory colorCat = segments.get(0).getColorCategory();
				if (colorCat == SegmentColorCategory.UNCATEGORIZED) {
					List<TrackSegment> filteredSegments = Utilities.filter(segments, new Predicate<TrackSegment>() {
						@Override
						public boolean apply(TrackSegment segment) {
							return segment.getType() == SegmentType.CLIMB
									&& segment.getCategory() != SegmentCategory.UNCATEGORIZED_CLIMB;
						}
					});

					List<Pair<Integer, Integer>> shadings = new ArrayList<Pair<Integer, Integer>>();
					for (TrackSegment segment : filteredSegments) {
						int startIndex = trackpoints.indexOf(segment.getTrackpoints().get(0));
						int endIndex = trackpoints
								.indexOf(segment.getTrackpoints().get(segment.getTrackpoints().size() - 1));
						Pair<Integer, Integer> shading = Pair.create(startIndex, endIndex);

						shadings.add(shading);
					}
					boolean color = false;
					series.setDoColors(color);
					series.setShadings(shadings);
				} else {
					List<TrackSegment> filteredSegments = Utilities.filter(segments, new Predicate<TrackSegment>() {
						@Override
						public boolean apply(TrackSegment segment) {
							return segment.getColorCategory() != SegmentColorCategory.UNCATEGORIZED;
						}
					});

					List<Pair<ColorSchemeV2, Pair<Integer, Integer>>> colorShadings = new ArrayList<Pair<ColorSchemeV2, Pair<Integer, Integer>>>();
					for (TrackSegment segment : filteredSegments) {
						int startIndex = trackpoints.indexOf(segment.getTrackpoints().get(0));
						int endIndex = trackpoints
								.indexOf(segment.getTrackpoints().get(segment.getTrackpoints().size() - 1));
						Pair<ColorSchemeV2, Pair<Integer, Integer>> shading = Pair.create(segment.getColorSchemeV2(),
								Pair.create(startIndex, endIndex));

						colorShadings.add(shading);
					}
					boolean color = true;
					series.setDoColors(color);
					series.setColorShadings(colorShadings);
					TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
							Constants.ChartPreferences.SHOW_GRADE_PROFILE, showGradeProfile);
				}
			}

			dataSeries.add(series);
		}
		if (showHeartRateProfile) {
			scale = new Scale(minHeartRate, maxHeartRate, 5, true);

			if (showSmoothData) {
				double heartRateSmoothingFactor = TrackIt.getPreferences().getDoublePreference(
						Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.HEART_RATE_SMOOTHING_FACTOR,
						54.0);
				if (heartRateSmoothingFactor > 0.0) {
					AltitudeSmoothingOperation.smoothing(heartRateData, heartRateSmoothingFactor, true);
				}
			}

			series = new DataSeries("Heart Rate", scale, Unit.BPM, ColorScheme.LIGHT_RED, false, heartRateData);
			dataSeries.add(series);
		}

		if (showSpeedProfile) {
			scale = new Scale(minSpeed, maxSpeed, 5, true);

			if (showSmoothData) {
				double speedSmoothingFactor = TrackIt.getPreferences().getDoublePreference(
						Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.SPEED_SMOOTHING_FACTOR, 34.0);
				if (speedSmoothingFactor > 0.0) {
					AltitudeSmoothingOperation.smoothing(speedData, speedSmoothingFactor, true);
				}
			}

			series = new DataSeries("Speed", scale, Unit.KILOMETER_PER_HOUR, ColorScheme.LIGHT_BLUE, false, speedData);
			if (!selection.isEmpty()) {
				series.setSelection(trackpoints.indexOf(selection.get(0)),
						trackpoints.indexOf(selection.get(selection.size() - 1)));
			}

			if (highlight != null) {
				int highlightIndex = trackpoints.indexOf(highlight);
				series.setHighlight(Pair.create(speedData[highlightIndex][0], speedData[highlightIndex][1]));
			} else {
				series.setHighlight(null);
			}

			if (!segments.isEmpty()) {
				List<TrackSegment> filteredSegments = Utilities.filter(segments, new Predicate<TrackSegment>() {
					@Override
					public boolean apply(TrackSegment segment) {
						return segment.getType() == SegmentType.CLIMB
								&& segment.getCategory() != SegmentCategory.UNCATEGORIZED_CLIMB;
					}
				});

				List<Pair<Integer, Integer>> shadings = new ArrayList<Pair<Integer, Integer>>();
				for (TrackSegment segment : filteredSegments) {
					int startIndex = trackpoints.indexOf(segment.getTrackpoints().get(0));
					int endIndex = trackpoints
							.indexOf(segment.getTrackpoints().get(segment.getTrackpoints().size() - 1));
					Pair<Integer, Integer> shading = Pair.create(startIndex, endIndex);

					shadings.add(shading);
				}
				boolean color = false;
				series.setDoColors(color);
				series.setShadings(shadings);
			}
			dataSeries.add(series);
		}

		if (showPowerProfile) {
			scale = new Scale(minPower, maxPower, 5, true);

			if (showSmoothData) {
				double powerSmoothingFactor = TrackIt.getPreferences().getDoublePreference(
						Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.POWER_SMOOTHING_FACTOR, 24.0);
				if (powerSmoothingFactor > 0.0) {
					AltitudeSmoothingOperation.smoothing(powerData, powerSmoothingFactor, true);
				}
			}

			series = new DataSeries("Power", scale, Unit.WATT, ColorScheme.DARK_YELLOW, false, powerData);
			dataSeries.add(series);
		}

		if (showCadenceProfile) {
			scale = new Scale(minCadence, maxCadence, 5, true);

			if (showSmoothData) {
				double cadenceSmoothingFactor = TrackIt.getPreferences().getDoublePreference(
						Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.CADENCE_SMOOTHING_FACTOR,
						34.0);
				if (cadenceSmoothingFactor > 0.0) {
					AltitudeSmoothingOperation.smoothing(cadenceData, cadenceSmoothingFactor, true);
				}
			}

			series = new DataSeries("Cadence", scale, Unit.RPM, ColorScheme.LIGHT_ORANGE, false, cadenceData);
			dataSeries.add(series);
		}

		if (showTemperatureProfile) {
			scale = new Scale(minTemperature, maxTemperature, 5, true);
			if (showSmoothData) {
				AltitudeSmoothingOperation.smoothing(temperatureData, 74.0, true);
			}
			series = new DataSeries("Temperature", scale, Unit.DEGREES_CELSIUS, ColorScheme.DARK_YELLOW, false,
					temperatureData);
			dataSeries.add(series);
		}

		scale = new Scale(minValue, maxValue, false);
		chart.setScale(scale);
		chart.setDataSeries(dataSeries);
	}

	private void updateDisplay() {
		if (item == null) {
			return;
		} else if (selection != null) {
			display(item.getTrackpoints(), selection.getTrackpoints());
		} else {
			display(item);
		}
	}

	private JToolBar createJToolBar() {
		JToolBar mapToolbar = new JToolBar();
		mapToolbar.setFloatable(false);
		mapToolbar.setRollover(true);

		URL imageURL;
		JToggleButton toggleButton;
		ImageIcon icon;

		ButtonGroup chartModeGroup = new ButtonGroup();

		String mode = TrackIt.getPreferences().getPreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.CHART_MODE, ChartMode.SELECTION.name());
		ChartMode lastChartMode = ChartMode.valueOf(mode);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("SelectionMode");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.selectionMode"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(lastChartMode == ChartMode.SELECTION);
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("SelectionMode")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						chartMode = ChartMode.SELECTION;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.CHART_MODE, chartMode.name());
			}
		});
		imageURL = ChartView.class.getResource("/icons/selection_mode_16.png");
		icon = new ImageIcon(imageURL, "Selection Mode");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);
		chartModeGroup.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("InfoMode");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.infoMode"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(lastChartMode == ChartMode.INFO);
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("InfoMode")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						chartMode = ChartMode.INFO;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.CHART_MODE, chartMode.name());
			}
		});
		imageURL = ChartView.class.getResource("/icons/info_mode_16.png");
		icon = new ImageIcon(imageURL, "Info Mode");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);
		chartModeGroup.add(toggleButton);

		mapToolbar.addSeparator();

		JToggleButton distanceToggleButton = new JToggleButton();
		distanceToggleButton.setActionCommand("DistanceChartType");
		distanceToggleButton.setToolTipText(getMessage("chartView.tooltip.showDistance"));
		distanceToggleButton.setFocusable(false);
		distanceToggleButton.setSelected(chartType == ChartType.DISTANCE);
		distanceToggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("DistanceChartType")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						chartType = ChartType.DISTANCE;
						updateDisplay();
					}
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.CHART_TYPE, chartType.name());
			}
		});
		imageURL = ChartView.class.getResource("/icons/ruler_16.png");
		icon = new ImageIcon(imageURL, "Distance Chart Type");
		distanceToggleButton.setIcon(icon);
		distanceToggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(distanceToggleButton);

		JToggleButton timeToggleButton = new JToggleButton();
		timeToggleButton.setActionCommand("TimeChartType");
		timeToggleButton.setToolTipText(getMessage("chartView.tooltip.showTime"));
		timeToggleButton.setFocusable(false);
		timeToggleButton.setSelected(chartType == ChartType.TIME);
		timeToggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("TimeChartType")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						chartType = ChartType.TIME;
						updateDisplay();
					}
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.CHART_TYPE, chartType.name());
			}
		});
		imageURL = ChartView.class.getResource("/icons/chronometer_16.png");
		icon = new ImageIcon(imageURL, "Time Chart Type");
		timeToggleButton.setIcon(icon);
		timeToggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(timeToggleButton);

		ButtonGroup chartTypeGroup = new ButtonGroup();
		chartTypeGroup.add(distanceToggleButton);
		chartTypeGroup.add(timeToggleButton);

		mapToolbar.addSeparator();

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowElevationProfile");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showElevationProfile"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showElevationProfile);
		// toggleButton.setEnabled(false);//58406
		if (item != null && !item.getTrackpoints().isEmpty() && item.getTrackpoints().get(0).getAltitude() == null) {
			showElevationProfile = false;
			toggleButton.setEnabled(false);
		}
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowElevationProfile")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showElevationProfile = true;
					} else {
						showElevationProfile = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_ELEVATION_PROFILE, showElevationProfile);
			}
		});
		imageURL = ChartView.class.getResource("/icons/elevation_profile_16.png");
		icon = new ImageIcon(imageURL, "Show Elevation Profile");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowSpeedProfile");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showSpeedProfile"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showSpeedProfile);
		if (item != null && !item.getTrackpoints().isEmpty() && item.getTrackpoints().get(0).getSpeed() == null) {
			showSpeedProfile = false;
			toggleButton.setEnabled(false);
		}
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowSpeedProfile")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showSpeedProfile = true;
					} else {
						showSpeedProfile = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_SPEED_PROFILE, showSpeedProfile);
			}
		});
		imageURL = ChartView.class.getResource("/icons/speedometer_16.png");
		icon = new ImageIcon(imageURL, "Show Speed Profile");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowHeartRateProfile");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showHeartRateProfile"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showHeartRateProfile);
		if (item != null && !item.getTrackpoints().isEmpty() && item.getTrackpoints().get(0).getHeartRate() == null) {
			showHeartRateProfile = false;
			toggleButton.setEnabled(false);
		}
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowHeartRateProfile")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showHeartRateProfile = true;
					} else {
						showHeartRateProfile = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_HEART_RATE_PROFILE, showHeartRateProfile);
			}
		});
		imageURL = ChartView.class.getResource("/icons/heart_16.png");
		icon = new ImageIcon(imageURL, "Show Heart Rate Profile");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowCadenceProfile");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showCadenceProfile"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showCadenceProfile);
		if (item != null && !item.getTrackpoints().isEmpty() && item.getTrackpoints().get(0).getCadence() == null) {
			showCadenceProfile = false;
			toggleButton.setEnabled(false);
		}
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowCadenceProfile")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showCadenceProfile = true;
					} else {
						showCadenceProfile = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_CADENCE_PROFILE, showCadenceProfile);
			}
		});
		imageURL = ChartView.class.getResource("/icons/cadence_16.png");
		icon = new ImageIcon(imageURL, "Show Cadence Profile");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowPowerProfile");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showPowerProfile"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showPowerProfile);
		if (item != null && !item.getTrackpoints().isEmpty() && item.getTrackpoints().get(0).getPower() == null) {
			showPowerProfile = false;
			toggleButton.setEnabled(false);
		}
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowPowerProfile")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showPowerProfile = true;
					} else {
						showPowerProfile = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_POWER_PROFILE, showPowerProfile);
			}
		});
		imageURL = ChartView.class.getResource("/icons/power_16.png");
		icon = new ImageIcon(imageURL, "Show Power Profile");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowTemperatureProfile");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showTemperatureProfile"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showTemperatureProfile);
		if (item != null && !item.getTrackpoints().isEmpty() && item.getTrackpoints().get(0).getTemperature() == null) {
			showTemperatureProfile = false;
			toggleButton.setEnabled(false);
		}
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowTemperatureProfile")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showTemperatureProfile = true;
					} else {
						showTemperatureProfile = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_TEMPERATURE_PROFILE, showTemperatureProfile);
			}
		});
		imageURL = ChartView.class.getResource("/icons/thermometer_16.png");
		icon = new ImageIcon(imageURL, "Show Temperature Profile");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		mapToolbar.addSeparator();

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowGrade");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showGrade"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showGrade);
		toggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowGrade")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showGrade = true;
					} else {
						showGrade = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_GRADE, showGrade);
			}
		});
		imageURL = ChartView.class.getResource("/icons/percent.png");
		icon = new ImageIcon(imageURL, "Show Grade");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowCoursePoints");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showCoursePoints"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showCoursePoints);
		toggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowCoursePoints")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showCoursePoints = true;
					} else {
						showCoursePoints = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_COURSE_POINTS, showCoursePoints);
			}
		});
		imageURL = ChartView.class.getResource("/icons/coursepoints/Generic.png");
		icon = new ImageIcon(imageURL, "Show Course Points");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowClimbs");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showClimbs"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showClimbs);
		toggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowClimbs")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showClimbs = true;
					} else {
						showClimbs = false;
					}
					chart.validate();// 58406
					chart.repaint();
					// updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_CLIMBS, showClimbs);
			}
		});
		imageURL = ChartView.class.getResource("/icons/coursepoints/Summit.png");
		icon = new ImageIcon(imageURL, "Show Climbs");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowDescents");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showDescents"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showDescents);
		toggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowDescents")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showDescents = true;
					} else {
						showDescents = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_DESCENTS, showDescents);
			}
		});
		imageURL = ChartView.class.getResource("/icons/coursepoints/Valley.png");
		icon = new ImageIcon(imageURL, "Show Descents");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowGradeProfile");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showGradeProfile"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showGradeProfile);
		if (item != null && item.getSegments().isEmpty()){
				showCadenceProfile = false;
				toggleButton.setEnabled(false);
			
		}
		else if(item != null && !item.getSegments().isEmpty() && !(item.getSegments().get(0).getColorCategory() != SegmentColorCategory.UNCATEGORIZED)){
			showCadenceProfile = false;
			toggleButton.setEnabled(false);
		}
		toggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowGradeProfile")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showGradeProfile = true;
					} else {
						showGradeProfile = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_GRADE_PROFILE, showGradeProfile);
			}
		});
		imageURL = ChartView.class.getResource("/icons/elevation_profile_17.png");
		icon = new ImageIcon(imageURL, "ShowGradeProfile");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		mapToolbar.addSeparator();

		toggleButton = new JToggleButton();
		toggleButton.setActionCommand("ShowSmoothedData");
		toggleButton.setToolTipText(getMessage("chartView.tooltip.showSmoothedData"));
		toggleButton.setFocusable(false);
		toggleButton.setSelected(showSmoothData);
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ShowSmoothedData")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						showSmoothData = true;
					} else {
						showSmoothData = false;
					}
					updateDisplay();
				}
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.CHART, null,
						Constants.ChartPreferences.SHOW_SMOOTHED_DATA, showSmoothData);
			}
		});
		imageURL = ChartView.class.getResource("/icons/smooth_16.png");
		icon = new ImageIcon(imageURL, "Show Smoothed Data");
		toggleButton.setIcon(icon);
		toggleButton.setMaximumSize(new Dimension(24, 24));
		mapToolbar.add(toggleButton);

		return mapToolbar;
	}

	/* Event Listener interface implementation */

	@Override
	public void process(com.trackit.presentation.event.Event event, DocumentItem item) {
		clearHighlight();
		switch (event) {
		case DOCUMENT_ADDED:
		case DOCUMENT_SELECTED:
			processDocumentSelected((GPSDocument) item);
			break;
		case ACTIVITY_SELECTED:
		case ACTIVITY_UPDATED:
		case COURSE_SELECTED:
		case COURSE_UPDATED:
			remove(mapToolbar);
			mapToolbar = createJToolBar();
			add(mapToolbar, BorderLayout.PAGE_START);
			processRootItemSelected(item);
			break;
		case SESSION_SELECTED:
		case LAP_SELECTED:
		case SEGMENT_SELECTED:
			processNonRootItemSelected(item);
			break;
		case TRACKPOINT_SELECTED:
			processTrackpointSelected((Trackpoint) item);
			break;
		case EVENT_SELECTED:
		case DEVICE_SELECTED:
		case COURSE_POINT_SELECTED:
		case WAYPOINT_SELECTED:
			processRootItemSelected(getRootItem(item.getParent()));
			break;
		case TRACKPOINT_HIGHLIGHTED:
		case ANIMATION_MOVE:
			processTrackpointHighlighted((Trackpoint) item);
			break;
		case ZOOM_TO_ITEM:
			processZoomedItem(item);
			break;
		default:
			clearItem();
			clearSelection();
			clearDisplay();
		}
		chart.validate();// 58406
		chart.repaint();
	}

	@Override
	public void process(com.trackit.presentation.event.Event event, DocumentItem parent,
			List<? extends DocumentItem> items) {
		switch (event) {
		case SESSIONS_SELECTED:
		case LAPS_SELECTED:
		case SEGMENTS_SELECTED:
		case DEVICES_SELECTED:
		case EVENTS_SELECTED:
		case COURSE_POINTS_SELECTED:
			processRootItemSelected(parent);
			break;
		default:
			clearDisplay();
		}
	}

	boolean isZoomed() {
		return zoomed;
	}

	private void processZoomedItem(DocumentItem item) {
		display(item);
		setItem(item);
		clearSelection();
		zoomed = (!item.isActivity() && !item.isCourse());
	}

	void resetZoom() {
		DocumentItem rootItem = getRootItem(item);

		if (rootItem != null) {
			display(rootItem);
			setItem(rootItem);
			clearSelection();

			rootItem.publishSelectionEvent(null);
			EventManager.getInstance().publish(null, com.trackit.presentation.event.Event.ZOOM_TO_ITEM,
					rootItem);
		}

		zoomed = false;
	}

	private void processDocumentSelected(GPSDocument document) {
		clearDisplay();
	}

	private void processRootItemSelected(DocumentItem item) {
		display(item);
		setItem(item);
		clearSelection();
	}

	private void processNonRootItemSelected(DocumentItem item) {
		display(item.getParent(), item);
		setItem(item.getParent());
		setSelection(item);
	}

	private void processTrackpointSelected(Trackpoint trackpoint) {
		if (!item.getTrackpoints().contains(trackpoint)) {
			setItem(item.getParent());
			display(item.getParent(), item);
		} else {
			setSelection(trackpoint);
			display(trackpoint.getParent(), trackpoint);
		}
	}

	private void processTrackpointHighlighted(Trackpoint trackpoint) {
		displayHighlightedTrackpoint(trackpoint);
	}

	private void displaySelectedTrackpoint(Trackpoint trackpoint) {
		if (trackpoint != null && item.getTrackpoints().contains(trackpoint)) {
			setSelection(trackpoint);
			display(item, selection);
		}
	}

	private void displayHighlightedTrackpoint(Trackpoint trackpoint) {
		if (trackpoint != null && item != null && item.getTrackpoints().contains(trackpoint)) {
			highlight = trackpoint;
			display(item, selection);
		} else {
			highlight = null;
		}
	}

	public DocumentItem getRootItem(DocumentItem item) {
		if (item == null) {
			return item;
		}

		DocumentItem rootItem = item;
		while (rootItem.getParent() != null && !rootItem.isActivity() && !rootItem.isCourse()) {
			rootItem = rootItem.getParent();
		}

		return rootItem;
	}

	@Override
	public String toString() {
		return Messages.getMessage("view.chart.name");
	}

	void zoomItem(DocumentItem item) {
		setItem(item);
		clearSelection();
		EventManager.getInstance().publish(null, com.trackit.presentation.event.Event.ZOOM_TO_ITEM,
				item);
	}

	void zoomItem(Trackpoint trackpoint) {
		EventManager.getInstance().publish(this, com.trackit.presentation.event.Event.ZOOM_TO_ITEM,
				trackpoint);
	}
}
