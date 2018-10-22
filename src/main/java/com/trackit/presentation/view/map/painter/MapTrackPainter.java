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
package com.trackit.presentation.view.map.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.trackit.business.common.Constants;
import com.trackit.business.domain.Trackpoint;
import com.trackit.presentation.view.map.layer.MapLayer;

public class MapTrackPainter implements MapPainter {
	private static final Color DEFAULT_LINE_COLOR = Color.DARK_GRAY;
	private static final Color DEFAULT_FILL_COLOR = Color.LIGHT_GRAY;
	
	private Graphics2D graphics;
	private MapLayer mapLayer;
	private MapPainterStyle style;
	private Map<String, Object> paintingAttributes;
	private List<Trackpoint> trackpoints;
	private boolean simplify;
	
	public MapTrackPainter(MapLayer mapLayer, List<Trackpoint> trackpoints) {
		this.mapLayer = mapLayer;
		this.trackpoints = trackpoints;
	}
	
	@Override
	public void paint(Graphics2D graphics, Map<String, Object> paintingAttributes) {
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		this.graphics = graphics;
		this.paintingAttributes = paintingAttributes;
		this.style = (MapPainterStyle) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.MAP_PAINTER_STYLE);
		this.simplify = (Boolean) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.SIMPLIFY);
		
		if (trackpoints == null || trackpoints.size() < 2) {
			return;
		}
		
		graphics.setStroke(new BasicStroke(4.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.setColor(getLineColor());
		paintTrack();

		graphics.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.setColor(getFillColor());
		paintTrack();
	}
	
	private void paintTrack() {
		List<List<Trackpoint>> trackSegmentsOnScreen = getTrackSegmentsOnScreen();
		
		for (List<Trackpoint> trackSegmentOnScreen : trackSegmentsOnScreen) {
			paintTrackSegmentOnScreen(trackSegmentOnScreen);
		}
	}
		
	private List<List<Trackpoint>> getTrackSegmentsOnScreen() {
		List<List<Trackpoint>> trackSegmentsOnScreen = new ArrayList<List<Trackpoint>>();
		
		List<Trackpoint> trackSegmentOnScreen = new ArrayList<Trackpoint>();
		Trackpoint trackpoint;
		for (int i = 0; i < trackpoints.size(); i++) {
			trackpoint = trackpoints.get(i);
			
			if (!isValid(trackpoint) || !isVisible(trackpoint)) {
				continue;
			} else if (insideMap(trackpoint)) {
				if (trackSegmentOnScreen.isEmpty() && i > 0) {
					trackSegmentOnScreen.add(trackpoints.get(i - 1));
				}
				trackSegmentOnScreen.add(trackpoint);
			} else if (!trackSegmentOnScreen.isEmpty()) {
				trackSegmentOnScreen.add(trackpoint);
				trackSegmentsOnScreen.add(trackSegmentOnScreen);
				trackSegmentOnScreen = new ArrayList<Trackpoint>();
			}
		}
		
		if (!trackSegmentOnScreen.isEmpty()) {
			trackSegmentsOnScreen.add(trackSegmentOnScreen);
		}
		
		return trackSegmentsOnScreen;
	}
	
	private boolean isVisible(Trackpoint trackpoint) {
		return (!simplify || trackpoint.isViewable());
	}

	private boolean isValid(Trackpoint trackpoint) {
		return (trackpoint.getLongitude() != null && trackpoint.getLatitude() != null);
	}

	private boolean insideMap(Trackpoint trackpoint) {
		Point pointOnScreen = getPointOnScreen(trackpoint);
		Shape mapBorder = getMapBorder();
		
		return mapBorder.contains(pointOnScreen);
	}
	
	private Point getPointOnScreen(Trackpoint trackpoint) {
		final int screenCenterX = mapLayer.getWidth() / 2;
		final int screenCenterY = mapLayer.getHeight() / 2;
		
		int[] offset = mapLayer.getMapProvider().getCenterOffsetInPixels(
				trackpoint.getLongitude(), trackpoint.getLatitude());
		
		Point pointOnScreen = new Point(screenCenterX + offset[0], screenCenterY + offset[1]);
		
		return pointOnScreen;
	}
	
	private Shape getMapBorder() {
		final int minX = 0;
		final int minY = 0;
		final int maxX = mapLayer.getWidth();
		final int maxY = mapLayer.getHeight();
		final int offset = 50;
		
		int[] xPoints = new int[] { minX - offset, maxX + offset, maxX + offset, minX - offset };
		int[] yPoints = new int[] { maxY + offset, maxY + offset, minY - offset, minY - offset };
		
		Shape mapBorder = new Polygon(xPoints, yPoints, 4);

		return mapBorder;
	}
	
	private void paintTrackSegmentOnScreen(List<Trackpoint> trackSegmentOnScreen) {
		final int screenCenterX = mapLayer.getWidth() / 2;
		final int screenCenterY = mapLayer.getHeight() / 2;

		/* Initialize track */
		Path2D track = new Path2D.Double(GeneralPath.WIND_EVEN_ODD, trackSegmentOnScreen.size());
		Trackpoint firstTrackpoint = trackSegmentOnScreen.get(0);
		
		/* Move to first trackpoint */
		int[] offset = mapLayer.getMapProvider().getCenterOffsetInPixels(
				firstTrackpoint.getLongitude(), firstTrackpoint.getLatitude());
		track.moveTo(screenCenterX + offset[0], screenCenterY + offset[1]);

		/* Add remaining trackpoints */
		for (Trackpoint trackpoint : trackSegmentOnScreen) {
			offset = mapLayer.getMapProvider().getCenterOffsetInPixels(
					trackpoint.getLongitude(), trackpoint.getLatitude());
			track.lineTo(screenCenterX + offset[0], screenCenterY + offset[1]);
		}

		graphics.draw(track);
	}
	
	private Color getLineColor() {
		Color lineColor = null;
		
		switch (style) {
		case REGULAR:
			lineColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.REGULAR_LINE_COLOR);
			break;
		case SELECTION:
			lineColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.SELECTION_LINE_COLOR);
			break;
		default:
			lineColor = DEFAULT_LINE_COLOR;
		}
		
		return lineColor;
	}
	
	private Color getFillColor() {
		Color fillColor = null;
		
		switch (style) {
		case REGULAR:
			fillColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.REGULAR_FILL_COLOR);
			break;
		case SELECTION:
			fillColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.SELECTION_FILL_COLOR);
			break;
		default:
			fillColor = DEFAULT_FILL_COLOR;
		}
		
		return fillColor;
	}
}