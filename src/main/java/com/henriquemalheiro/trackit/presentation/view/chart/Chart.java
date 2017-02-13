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
package com.henriquemalheiro.trackit.presentation.view.chart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.ColorScheme;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.common.Unit;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.utility.TrackItPreferences;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.view.chart.ChartView.ChartMode;
import com.henriquemalheiro.trackit.presentation.view.chart.ChartView.ChartType;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterStyle;
import com.pg58406.trackit.business.common.ColorSchemeV2;

class Chart extends JComponent {
	private static final long serialVersionUID = -4485178334068285482L;

	private ChartView chartView;
	private ChartPanel chart = new ChartPanel(this);
	List<DataSeries> dataSeries;
	List<CoursePoint> coursePoints;
	Scale scale;
	private Axis xAxis;
	private Axis[] yAxis;

	Chart(ChartView chartView, Scale scale) {
		setOpaque(true);
		setDoubleBuffered(true);
		this.chartView = chartView;
		this.scale = scale;
		dataSeries = new ArrayList<DataSeries>();

		setLayout(new BorderLayout());
		initChart();
	}

	ChartView getChartView() {
		return chartView;
	}

	void setScale(Scale scale) {
		this.scale = scale;
		initChart();
	}

	void setDataSeries(List<DataSeries> dataSeries) {
		this.dataSeries = dataSeries;

		initChart();
		validate();// 58406
		repaint();
	}

	void setCoursePoints(List<CoursePoint> coursePoints) {
		this.coursePoints = coursePoints;
		validate();// 58406
		repaint();
	}

	boolean showGrade() {
		return chartView.isShowGrade();
	}

	private void initChart() {
		BorderLayout layout = (BorderLayout) getLayout();

		if (layout.getLayoutComponent(BorderLayout.CENTER) != null) {
			remove(layout.getLayoutComponent(BorderLayout.CENTER));
		}
		add(chart);

		if (layout.getLayoutComponent(BorderLayout.LINE_START) != null) {
			remove(layout.getLayoutComponent(BorderLayout.LINE_START));
		}

		if (layout.getLayoutComponent(BorderLayout.PAGE_END) != null) {
			remove(layout.getLayoutComponent(BorderLayout.PAGE_END));
		}

		if (dataSeries.isEmpty()) {
			validate();
			return;
		}

		addVerticalAxis();
		addHorizontalAxis();
		validate();
	}

	private void addHorizontalAxis() {
		switch (chartView.getChartMode()) {
		case DISTANCE:
			xAxis = new DistanceAxis(scale, "Distance", Unit.METER, ColorScheme.LIGHT_GREEN);
			break;
		case TIME:
			xAxis = new TimeAxis(scale, "Time", Unit.SECOND, ColorScheme.LIGHT_GREEN);
			break;
		}

		JPanel xAxisPanel = new JPanel();
		xAxisPanel.setLayout(new BoxLayout(xAxisPanel, BoxLayout.LINE_AXIS));
		xAxisPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		JPanel horizontalGap = createGap();

		xAxisPanel.add(horizontalGap);
		xAxisPanel.add(xAxis);
		add(xAxisPanel, BorderLayout.PAGE_END);
	}

	private void addVerticalAxis() {
		JPanel verticalAxisPanel = new JPanel();
		verticalAxisPanel.setLayout(new BoxLayout(verticalAxisPanel, BoxLayout.LINE_AXIS));
		verticalAxisPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		yAxis = new Axis[dataSeries.size()];
		for (int i = 0; i < dataSeries.size(); i++) {
			yAxis[i] = new VerticalAxis(dataSeries.get(i));
			verticalAxisPanel.add(yAxis[i]);
		}
		add(verticalAxisPanel, BorderLayout.LINE_START);
	}

	private JPanel createGap() {
		int x = 55 * yAxis.length;
		int y = 55;
		Dimension gapDimension = new Dimension(x, y);

		JPanel horizontalGap = new JPanel();
		horizontalGap.setMinimumSize(gapDimension);
		horizontalGap.setPreferredSize(gapDimension);
		horizontalGap.setMaximumSize(gapDimension);
		horizontalGap.setBackground(Color.WHITE);

		return horizontalGap;
	}
}

class ChartPanel extends JPanel {
	private static final long serialVersionUID = -4270576996686885963L;

	private Chart chart;
	private Point mousePosition = new Point(0, 0);
	private Double selectionInitialValue;
	private JButton resetZoom;
	private double dataSeriesVerticalSpace;
	private double defaultSteps;

	ChartPanel(Chart chart) {
		this.chart = chart;
		init();
	}

	private void init() {
		dataSeriesVerticalSpace = getDataSeriesVerticalSpace();
		defaultSteps = getDefaultSteps();

		addMouseListener(new MouseHandler());
		addMouseMotionListener(new MouseHandler());
		createResetZoomButton();

		validate();// 58406
		repaint();
	}

	private void createResetZoomButton() {
		resetZoom = new JButton(Messages.getMessage("chartView.button.resetZoom"));
		resetZoom.setVisible(false);
		resetZoom.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		resetZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ChartPanel.this.chart.getChartView().resetZoom();
			}
		});

		add(resetZoom);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getParent().getWidth(), getParent().getHeight());
	}

	@Override
	public void paintComponent(Graphics g) {
		resetZoom.setVisible(chart.getChartView().isZoomed());

		super.paintComponents(g);

		Graphics2D graphics = (Graphics2D) g;
		graphics.setColor(new Color(255, 255, 255));
		graphics.fillRect(0, 0, getWidth(), getHeight());
		graphics.setClip(1, 1, getWidth() - 1, getHeight() - 1);

		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (chart.dataSeries.isEmpty()) {
			drawEmptyChart(graphics);
		} else {
			if (chart.coursePoints != null && !chart.coursePoints.isEmpty()) {
				drawCoursePoints(graphics, chart.coursePoints);
			}

			drawDataSeries(graphics);
		}

		if (chart.getChartView().getMode() == ChartMode.SELECTION) {
			setCursor(Cursor.getDefaultCursor());
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			drawGuidelines(graphics);
			drawInfo(graphics);
		}
	}

	private void drawEmptyChart(Graphics2D graphics) {
		Font originalFont = graphics.getFont();
		Font newFont = originalFont.deriveFont(24.0f);
		newFont = newFont.deriveFont(Font.BOLD);

		String label = "No data to display";
		FontMetrics metrics = graphics.getFontMetrics(newFont);
		int labelWidth = metrics.stringWidth(label);

		graphics.setFont(newFont);
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.drawString(label, (int) (getWidth() / 2.0 - (labelWidth / 2.0)),
				(int) (getHeight() / 2 - (metrics.getHeight() / 2.0)));

		graphics.setFont(originalFont);
	}

	private void drawDataSeries(Graphics2D graphics) {
		if (!chart.dataSeries.isEmpty()) {
			drawGrid(graphics);
			for (DataSeries data : chart.dataSeries) {
				drawData(graphics, data);
			}
		}
	}

	private void drawGuidelines(Graphics2D graphics) {
		if (mousePosition != null) {
			graphics.setColor(Color.DARK_GRAY);
			graphics.setStroke(new BasicStroke(0.9f));
			graphics.drawLine((int) mousePosition.getX(), getHeight(), (int) mousePosition.getX(), 0);
			graphics.drawLine(0, (int) mousePosition.getY(), getWidth(), (int) mousePosition.getY());
		}
	}

	private void drawInfo(Graphics2D graphics) {
		if (!isInfoToDisplay()) {
			return;
		}

		double value = getValueAtMousePosition();
		String info = getInfo(value);
		displayInfo(graphics, info);

		// Trackpoint trackpoint = null;
		// if (isDistanceMode()) {
		// trackpoint = chart.getChartView().getTrackpointAtDistance(value);
		// } else if (isTimeMode()) {
		// trackpoint = chart.getChartView().getTrackpointAtTime(value);
		// }
		//
		// if (trackpoint != null) {
		// String info = getInfo(trackpoint);
		// displayInfo(info);
		// }
	}

	private void displayInfo(Graphics2D graphics, String info) {
		Font font = new Font(null, Font.BOLD, 9);
		graphics.setFont(font);

		int textWidth = graphics.getFontMetrics().stringWidth(info);
		int textHeight = graphics.getFontMetrics().getAscent();
		int padding = 5;
		int width = textWidth + padding;
		int height = textHeight + padding;

		graphics.setColor(ColorScheme.LIGHT_ORANGE.getSelectionFillColor());
		graphics.fill(new RoundRectangle2D.Float((int) mousePosition.getX() + 12, (int) mousePosition.getY() - 20,
				width, height, 10, 10));

		graphics.setColor(ColorScheme.LIGHT_ORANGE.getSelectionLineColor());
		graphics.setStroke(new BasicStroke(1.0f));
		graphics.draw(new RoundRectangle2D.Float((int) mousePosition.getX() + 12, (int) mousePosition.getY() - 20,
				width, height, 10, 10));

		graphics.setColor(Color.BLACK);
		graphics.drawString(info, (int) mousePosition.getX() + 16, (int) mousePosition.getY() - 10);
	}

	private boolean isDistanceMode() {
		return (chart.getChartView().getChartMode().equals(ChartType.DISTANCE));
	}

	private boolean isTimeMode() {
		return (chart.getChartView().getChartMode().equals(ChartType.TIME));
	}

	private String getInfo(double value) {
		String info = null;

		if (isDistanceMode()) {
			info = String.format("Distance: %s", Formatters.getFormatedDistance(value));
		} else if (isTimeMode()) {
			info = String.format("Time: %s", Formatters.getFormatedDuration(value));
		}

		return info;
	}

	private boolean isInfoToDisplay() {
		boolean infoToDisplay = (chart != null);
		infoToDisplay &= (chart.scale != null);
		infoToDisplay &= (chart.dataSeries != null);
		infoToDisplay &= (mousePosition != null);
		return infoToDisplay;
	}

	private void drawGrid(Graphics2D graphics) {
		double height = getHeight() * dataSeriesVerticalSpace;
		double pixelsPerStep = height / defaultSteps;

		graphics.setColor(Color.LIGHT_GRAY);
		for (int i = 0; i < defaultSteps; i++) {
			graphics.drawLine(0, (int) (getHeight() - (pixelsPerStep * i)), getWidth(),
					(int) (getHeight() - (pixelsPerStep * i)));
		}
	}

	private void drawData(Graphics2D graphics, DataSeries series) {
		if (dataAvailable(series)) {
			double[][] data = series.getData();
			drawSegment(graphics, series, data, MapPainterStyle.HIGHLIGHT);
			if(!TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.CHART, null,
					Constants.ChartPreferences.SHOW_GRADE_PROFILE, false)){
				drawShadings(graphics, series);
			}
			else{
				drawColorShadings(graphics, series);
			}
			
			drawSelection(graphics, series);
			drawHighlight(graphics, series);
		}
	}

	private boolean dataAvailable(DataSeries series) {
		return series.getData().length > 0;
	}

	private void drawSegment(Graphics2D graphics, DataSeries series, double[][] data, MapPainterStyle style) {
		Path2D chartLine = drawProfile(series.getScale(), data);
		if (series.isSolid()) {
			drawProfileShading(graphics, chartLine, series, data, style);
		}

		Color strokeColor = getLineColor(series.getColorScheme(), style);
		strokeColor = strokeColor.brighter();
		graphics.setColor(strokeColor);
		graphics.setStroke(new BasicStroke(0.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.draw(chartLine);
	}
	
	private void drawColoredSegment(Graphics2D graphics, DataSeries series, Pair<ColorSchemeV2, double[][]> data, MapPainterStyle style) {
		Path2D chartLine = drawProfile(series.getScale(), data.getSecond());
		if (series.isSolid()) {
			drawColorProfileShading(graphics, chartLine, series, data.getSecond(), style, data.getFirst());
		}

		Color strokeColor = getLineColor(series.getColorScheme(), style);
		strokeColor = strokeColor.brighter();
		graphics.setColor(strokeColor);
		graphics.setStroke(new BasicStroke(0.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.draw(chartLine);
	}

	private void drawSelection(Graphics2D graphics, DataSeries series) {
		double[][] selection = series.getSelection();
		if (isSegment(selection)) {
			drawSegment(graphics, series, selection, MapPainterStyle.SELECTION);
		} else if (isTrackpoint(selection)) {
			drawTrackpoint(graphics, series, selection[0][0], selection[0][1], MapPainterStyle.SELECTION);
		}
	}

	private void drawHighlight(Graphics2D graphics, DataSeries series) {
		double[][] highlight = series.getHighlight();
		if (highlight != null) {
			drawTrackpoint(graphics, series, highlight[0][0], highlight[0][1], MapPainterStyle.HIGHLIGHT);
		}
	}

	private void drawShadings(Graphics2D graphics, DataSeries series) {
		List<double[][]> shadingData = series.getShadings();
			for (double[][] data : shadingData) {
				drawSegment(graphics, series, data, MapPainterStyle.SHADING);
			}
		
	}
	
	private void drawColorShadings(Graphics2D graphics, DataSeries series) {
		List<Pair<ColorSchemeV2, double[][]>> shadingData = series.getColorShadings();
		for (Pair<ColorSchemeV2, double[][]> data : shadingData) {
			drawColoredSegment(graphics, series, data, MapPainterStyle.SHADING);
		}
		
	}

	private boolean isSegment(double[][] selection) {
		return (selection.length > 1);
	}

	private boolean isTrackpoint(double[][] selection) {
		return (selection.length == 1);
	}

	private Path2D drawProfile(Scale yScale, double[][] data) {
		Point2D.Double point = getProfilePoint(data[0][0], data[0][1], yScale);

		Path2D chartLine = new Path2D.Double(GeneralPath.WIND_EVEN_ODD);
		chartLine.moveTo(point.x, point.y);

		for (int i = 0; i < data.length; i++) {
			point = getProfilePoint(data[i][0], data[i][1], yScale);
			chartLine.lineTo(point.x, point.y);
		}

		return chartLine;
	}

	private Point2D.Double getProfilePoint(double xi, double yi, Scale yScale) {
		double pixelsPerMeter = getPixelsPerMeter(yScale);
		double x = (xi - chart.scale.getMinValue()) * getWidth()
				/ (chart.scale.getMaxValue() - chart.scale.getMinValue() + 1);
		double y = getHeight() - (yi - yScale.getMinValue()) * pixelsPerMeter;

		return new Point2D.Double(x, y);
	}

	private void drawProfileShading(Graphics2D graphics, Path2D chartLine, DataSeries series, double[][] data,
			MapPainterStyle style) {

		double xi = (data[0][0] - chart.scale.getMinValue()) * getWidth()
				/ (chart.scale.getMaxValue() - chart.scale.getMinValue() + 1);
		double x = xi + (data[data.length - 1][0] - data[0][0]) * getWidth()
				/ (chart.scale.getMaxValue() - chart.scale.getMinValue() + 1);
		double y = getHeight();

		// Close path
		chartLine.lineTo(x, y);
		chartLine.lineTo(xi, y);
		chartLine.closePath();

		// Draw shading
		graphics.setPaint(getGradientFill(series.getColorScheme(), style));
		graphics.fill(chartLine);
	}
	
	private void drawColorProfileShading(Graphics2D graphics, Path2D chartLine, DataSeries series, double[][] data,
			MapPainterStyle style, ColorSchemeV2 color) {

		double xi = (data[0][0] - chart.scale.getMinValue()) * getWidth()
				/ (chart.scale.getMaxValue() - chart.scale.getMinValue() + 1);
		double x = xi + (data[data.length - 1][0] - data[0][0]) * getWidth()
				/ (chart.scale.getMaxValue() - chart.scale.getMinValue() + 1);
		double y = getHeight();

		// Close path
		chartLine.lineTo(x, y);
		chartLine.lineTo(xi, y);
		chartLine.closePath();

		// Draw shading
		graphics.setPaint(getGradientColorFill(color, style));
		graphics.fill(chartLine);
	}

	private void drawTrackpoint(Graphics2D graphics, DataSeries series, double xValue, double yValue,
			MapPainterStyle style) {

		Scale yScale = series.getScale();
		double pixelsPerMeter = getPixelsPerMeter(yScale);

		int x = (int) ((xValue - chart.scale.getMinValue()) * getWidth()
				/ (chart.scale.getMaxValue() - chart.scale.getMinValue()));
		int y = (int) (getHeight() - ((yValue - yScale.getMinValue()) * pixelsPerMeter));

		// Draw border
		int size = 10;
		graphics.setColor(getLineColor(series.getColorScheme(), style));
		graphics.fillOval(x - (size / 2), y - (size / 2), size, size);

		// Fill trackpoint
		size = 8;
		graphics.setColor(getFillColor(series.getColorScheme(), style));
		graphics.fillOval(x - (size / 2), y - (size / 2), size, size);
	}

	private void drawCoursePoints(Graphics2D graphics, List<CoursePoint> coursePoints) {
		for (CoursePoint coursePoint : coursePoints) {
			drawCoursePoint(graphics, coursePoint);
		}
	}

	private void drawCoursePoint(Graphics2D graphics, CoursePoint coursePoint) {
		final int distanceFromTop = 20;

		double distance = coursePoint.getTrackpoint().getDistance();

		if (distance > chart.scale.getMaxValue() || distance < chart.scale.getMinValue()) {
			return;
		}

		int x = (int) ((coursePoint.getTrackpoint().getDistance() - chart.scale.getMinValue()) * getWidth()
				/ (chart.scale.getMaxValue() - chart.scale.getMinValue() + 1));
		int y = distanceFromTop;

		graphics.setStroke(new BasicStroke(1.0f));
		graphics.setColor(new Color(163, 123, 17));
		graphics.drawLine(x, y, x, getHeight());

		Image image = coursePoint.getType().getIcon().getImage();
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		graphics.drawImage(image, x - (imageWidth / 2), y - (imageHeight / 2), imageWidth, imageHeight, null);
	}

	private Color getLineColor(ColorScheme colorScheme, MapPainterStyle style) {
		Color lineColor;

		switch (style) {
		case SELECTION:
			lineColor = colorScheme.getSelectionLineColor();
			break;
		case HIGHLIGHT:
			lineColor = colorScheme.getLineColor();
			break;
		case SHADING:
			lineColor = colorScheme.getLineColor();
			lineColor = ImageUtilities.applyTransparency(lineColor, 0.25f);
			break;
		case REGULAR:
			lineColor = colorScheme.getLineColor();
			break;
		default:
			lineColor = colorScheme.getLineColor();
		}

		return lineColor;
	}

	private Color getFillColor(ColorScheme colorScheme, MapPainterStyle style) {
		Color fillColor;

		switch (style) {
		case SELECTION:
			fillColor = colorScheme.getSelectionFillColor();
			fillColor = ImageUtilities.applyTransparency(fillColor, 0.5f);
			break;
		case SHADING:
			fillColor = colorScheme.getFillColor();
			fillColor = ImageUtilities.applyTransparency(fillColor, 0f);
			break;
		case HIGHLIGHT:
			fillColor = colorScheme.getFillColor();
			break;
		case REGULAR:
			fillColor = colorScheme.getFillColor();
			break;
		default:
			fillColor = colorScheme.getFillColor();
		}

		return fillColor;
	}

	private GradientPaint getGradientFill(ColorScheme colorScheme, MapPainterStyle style) {
		final float ALPHA = 0.663f;

		float x1 = getWidth() / 2;
		float y1 = getHeight();
		float x2 = getWidth() / 2;
		float y2 = getHeight() * (1 - 0.8f);
		Color initialColor = getFillColor(colorScheme, style);
		initialColor = ImageUtilities.applyTransparency(initialColor, ALPHA);
		Color finalColor = ImageUtilities.applyTransparency(Color.WHITE, ALPHA);

		return new GradientPaint(x1, y1, initialColor, x2, y2, finalColor);
	}
	
	private GradientPaint getGradientColorFill(ColorSchemeV2 colorScheme, MapPainterStyle style) {
		final float ALPHA = 0.663f;

		float x1 = getWidth() / 2;
		float y1 = getHeight();
		float x2 = getWidth() / 2;
		float y2 = getHeight() * (1 - 0.8f);
		Color initialColor = colorScheme.getFillColor();
		initialColor = ImageUtilities.applyTransparency(initialColor, ALPHA);
		Color finalColor = ImageUtilities.applyTransparency(Color.WHITE, ALPHA);

		return new GradientPaint(x1, y1, initialColor, x2, y2, finalColor);
	}

	private double getPixelsPerMeter(Scale yScale) {
		int height = (int) (getHeight() * dataSeriesVerticalSpace * yScale.getSteps() / defaultSteps);
		double pixelsPerMeter = height / yScale.getRange();
		return pixelsPerMeter;
	}

	private double getDataSeriesVerticalSpace() {
		double dataSeriesVerticalSpace = TrackItPreferences.getInstance().getDoublePreference(
				Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.DATA_SERIES_VERTICAL_SPACE, 0.8);
		return dataSeriesVerticalSpace;
	}

	private double getDefaultSteps() {
		double defaultSteps = TrackItPreferences.getInstance().getDoublePreference(Constants.PrefsCategories.CHART,
				null, Constants.ChartPreferences.VERTICAL_AXIS_STEPS, 7.0);
		return defaultSteps;
	}

	private double getValueAtMousePosition() {
		if (chart.scale == null) {
			return -1.0;
		}

		double offset = mousePosition.getX() * (chart.scale.getMaxValue() - chart.scale.getMinValue()) / getWidth();

		return chart.scale.getMinValue() + offset;
	}

	private class MouseHandler extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent event) {
			mousePosition = new Point(event.getX(), event.getY());
			selectionInitialValue = getValueAtMousePosition();
			selectTrackpoint();
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			mousePosition = new Point(event.getX(), event.getY());
			double value = getValueAtMousePosition();

			if (value == selectionInitialValue) {
				selectTrackpoint();
			} else {
				TrackSegment segment = selectSegment(value);
				if (segment != null) {
					chart.getChartView().zoomItem(segment);
				}
			}

			selectionInitialValue = null;
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			mousePosition = new Point(event.getX(), event.getY());
			selectSegment(getValueAtMousePosition());
			validate();// 58406
			repaint();
		}

		private void selectTrackpoint() {
			if (isDistanceMode()) {
				chart.getChartView().selectByDistance(selectionInitialValue);
			} else if (isTimeMode()) {
				chart.getChartView().selectByDuration(selectionInitialValue);
			}
		}

		private TrackSegment selectSegment(double value) {
			TrackSegment segment = null;

			double start = (selectionInitialValue < value ? selectionInitialValue : value);
			double end = (selectionInitialValue >= value ? selectionInitialValue : value);

			if (isDistanceMode()) {
				segment = chart.getChartView().selectByDistance(start, end);
			} else if (isTimeMode()) {
				segment = chart.getChartView().selectByDuration(start, end);
			}

			return segment;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mousePosition = new Point(e.getX(), e.getY());
			double value = getValueAtMousePosition();

			if (isDistanceMode()) {
				chart.getChartView().highlightByDistance(value);
			} else if (isTimeMode()) {
				chart.getChartView().highlightByDuration(value);
			}

			validate();// 58406
			repaint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			mousePosition = new Point(e.getX(), e.getY());
			validate();// 58406
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			mousePosition = null;
			validate();// 58406
			repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			mousePosition = new Point(e.getX(), e.getY());

			if (e.getClickCount() == 2) {
				zoomToItem();
			}
		}

		private void zoomToItem() {
			double value = getValueAtMousePosition();
			Trackpoint trackpoint = null;

			if (isDistanceMode()) {
				trackpoint = chart.getChartView().selectByDistance(value);
			} else {
				trackpoint = chart.getChartView().selectByDuration(value);
			}

			chart.getChartView().zoomItem(trackpoint);
		}
	}
}