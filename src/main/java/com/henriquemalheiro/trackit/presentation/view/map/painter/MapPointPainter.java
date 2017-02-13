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
package com.henriquemalheiro.trackit.presentation.view.map.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayer;

public class MapPointPainter implements MapPainter {
	private static final Integer DEFAULT_WIDTH = 7;
	private static final Integer DEFAULT_LINE_WIDTH = 4; 

	private Graphics2D graphics;
	private MapLayer layer;
	private MapPainterStyle style;
	private Map<String, Object> paintingAttributes;
	
	private Trackpoint trackpoint;
	
	public MapPointPainter(MapLayer layer, Trackpoint trackpoint) {
		this.layer = layer;
		this.trackpoint = trackpoint;
	}

	@Override
	public void paint(Graphics2D graphics, Map<String, Object> paintingAttributes) {
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		this.graphics = graphics;
		this.paintingAttributes = paintingAttributes;
		this.style = (MapPainterStyle) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.MAP_PAINTER_STYLE);

		graphics.setColor(getLineColor());
		paintPoint(trackpoint, getWidth() + getLineWidth());

		graphics.setColor(getFillColor());
		paintPoint(trackpoint, getWidth());
	}
	
	private Color getLineColor() {
		Color lineColor = null;
		
		switch (style) {
		case REGULAR:
			lineColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.REGULAR_LINE_COLOR);
			break;
		case HIGHLIGHT:
			lineColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.REGULAR_LINE_COLOR);
			break;
		case SELECTION:
			lineColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.SELECTION_LINE_COLOR);
			break;
		default:
			lineColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.REGULAR_LINE_COLOR);
		}
		
		return lineColor;
	}

	private Color getFillColor() {
		Color fillColor = null;
		
		switch (style) {
		case REGULAR:
			fillColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.REGULAR_FILL_COLOR);
			break;
		case HIGHLIGHT:
			fillColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.REGULAR_FILL_COLOR);
			break;
		case SELECTION:
			fillColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.SELECTION_FILL_COLOR);
			break;
		default:
			fillColor = (Color) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.REGULAR_FILL_COLOR);
		}
		
		return fillColor;
	}
	
	private int getWidth() {
		Integer width = (Integer) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.WIDTH);
		
		if (width == null) {
			width = DEFAULT_WIDTH;
		}
		
		return width;
	}
	
	private int getLineWidth() {
		Integer lineWidth = (Integer) paintingAttributes.get(Constants.PAINTING_ATTRIBUTES.LINE_WIDTH);
		
		if (lineWidth == null) {
			lineWidth = DEFAULT_LINE_WIDTH;
		}
		
		return lineWidth;
	}

	private void paintPoint(Trackpoint trackpoint, int size) {
		final int screenCenterX = layer.getWidth() / 2;
		final int screenCenterY = layer.getHeight() / 2;
		
		if (trackpoint == null) {
			return;
		}

		int[] offset = layer.getMapProvider().getCenterOffsetInPixels(trackpoint.getLongitude(), trackpoint.getLatitude());
		int x = screenCenterX + offset[0];
		int y = screenCenterY + offset[1];
		
		graphics.fillOval(x - (size / 2), y - (size / 2), size, size);
	}
}