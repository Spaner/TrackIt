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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.trackit.business.common.Constants;
import com.trackit.business.common.Location;
import com.trackit.business.common.Pair;
import com.trackit.business.domain.Waypoint;
import com.trackit.presentation.utilities.ImageUtilities;
import com.trackit.presentation.view.map.layer.MapLayer;

public class MapWaypointPainter implements MapPainter {
	private static final Color DEFAULT_LINE_COLOR = Color.DARK_GRAY;
	private static final Color DEFAULT_FILL_COLOR = Color.LIGHT_GRAY;
	
	private MapLayer layer;
	private Map<String, Object> paintingAttributes;
	private MapPainterStyle style;
	private List<Waypoint> waypoints;
	
	private Graphics2D graphics;
	
	public MapWaypointPainter(MapLayer layer, List<Waypoint> waypoints) {
		this.layer = layer;
		this.waypoints = waypoints;
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
		
		for (Waypoint waypoint : waypoints) {
			paintWaypoint(waypoint);
		}
	}
	
	private void paintWaypoint(Waypoint waypoint) {
		ImageIcon icon = getIcon(waypoint);
		Point iconHotSpot = getIconHotSpot(waypoint);
		
		if (icon != null) {
			Point position = getPosition(waypoint, icon, iconHotSpot);
			paintImage(icon, position);
		
			if (isSelection()) {
				paintSelection(waypoint, icon, position);
			}
		}
	}
	
	private ImageIcon getIcon(Waypoint waypoint) {
		return isSelection() ? waypoint.getSelectedIcon() : waypoint.getIcon();
	}
	
	private Point getIconHotSpot(Waypoint waypoint) {
		return isSelection() ? waypoint.getSelectedIconHotSpot() : waypoint.getIconHotSpot();
	}
	
	private Point getPosition(Waypoint waypoint, ImageIcon icon, Point iconHotSpot) {
		Location location = new Location(waypoint.getLongitude(), waypoint.getLatitude());
		Pair<Integer, Integer> centerOffset = layer.getMapProvider().getCenterOffsetInPixels(location);
		
		final int screenCenterX = layer.getWidth() / 2;
		final int screenCenterY = layer.getHeight() / 2;
		
		int x = screenCenterX + centerOffset.getFirst() - iconHotSpot.x;
		int y = screenCenterY + centerOffset.getSecond() - iconHotSpot.y;
		
		return new Point(x, y);
	}
	
	private void paintImage(ImageIcon icon, Point position) {
		final int imageWidth = icon.getImage().getWidth(null);
		final int imageHeight = icon.getImage().getHeight(null);
		
		graphics.drawImage(icon.getImage(), position.x, position.y, imageWidth, imageHeight, null);
	}
	
	private void paintSelection(Waypoint waypoint, ImageIcon icon, Point position) {
		String[] info = waypoint.getDocumentItemName().split("\n");
		
		Font font = new Font(null, Font.BOLD, 9);
		graphics.setFont(font);
		
		int textWidth = 0;
		for (String line : info) {
			textWidth = Math.max(textWidth, graphics.getFontMetrics().stringWidth(line));
		}
		int lineHeight = graphics.getFontMetrics().getAscent();
		int textHeight = lineHeight * info.length;
		int padding = 10;
		int interlineSpace = 5;
		int width = textWidth + padding;
		int height = textHeight + padding + (interlineSpace * (info.length - 1));
		
		graphics.setColor(ImageUtilities.applyTransparency(getFillColor(), 0.6f));
		graphics.fill(new RoundRectangle2D.Float(position.x + 24, position.y - 24, width, height, 10, 10));
		
		graphics.setColor(getLineColor());
		graphics.setStroke(new BasicStroke(1.0f));
		graphics.draw(new RoundRectangle2D.Float(position.x + 24, position.y - 24, width, height, 10, 10));
		
		graphics.setColor(Color.BLACK);
		for (int i = 0; i < info.length; i++) {
			String line = info[i];
			graphics.drawString(line, position.x + 26, (position.y - padding) + (i * (lineHeight + interlineSpace)));
		}
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
	
	private boolean isSelection() {
		return style == MapPainterStyle.SELECTION;
	}
}