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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.CoursePointType;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayer;

public class MapPainterFactory {
	private static final MapPainterFactory instance = new MapPainterFactory();
	private static final MapPainter nullMapPainter = new NullMapPainter();

	private MapPainterFactory() {
	}

	public synchronized static MapPainterFactory getInstance() {
		return instance;
	}

	public MapPainter getNullMapPainter() {
		return nullMapPainter;
	}

	public MapPainter getMapPainter(MapLayer layer, Activity activity) {
		return new MapTrackPainter(layer, activity.getTrackpoints());
	}

	public MapPainter getMapPainter(MapLayer layer, Course course) {
		return new MapTrackPainter(layer, course.getTrackpoints());
	}

	public MapPainter getMapPainter(MapLayer layer, Lap lap) {
		return new MapTrackPainter(layer, lap.getTrackpoints());
	}

	public MapPainter getMapPainter(MapLayer layer, TrackSegment segment) {
		return new MapTrackPainter(layer, segment.getTrackpoints());
	}

	public MapPainter getMapPainter(MapLayer layer, Trackpoint trackpoint) {
		if (trackpoint.getLongitude() != null
				&& trackpoint.getLatitude() != null) {
			return new MapPointPainter(layer, trackpoint);
		} else {
			return new NullMapPainter();
		}
	}

	public MapPainter getMapPainter(MapLayer layer, Waypoint waypoint) {
		return new MapWaypointPainter(layer,
				Arrays.asList(new Waypoint[] { waypoint }));
	}

	public MapPainter getMapPainter(MapLayer layer, CoursePoint coursePoint) {
		String name = String.format("%s (%s)", coursePoint.getName(),
				coursePoint.getType().toString());

		ImageIcon icon = getCoursePointIcon(coursePoint.getType());
		Point hotSpot = new Point(16, 31);

		Waypoint waypoint = toWaypoint(name, icon, hotSpot,
				coursePoint.getLongitude(), coursePoint.getLatitude(),
				coursePoint.getAltitude(), coursePoint.getTime());

		return new MapWaypointPainter(layer,
				Arrays.asList(new Waypoint[] { waypoint }));
	}

	public MapPainter getWaypointsMapPainter(MapLayer layer,
			List<Waypoint> waypoints) {
		for (Waypoint waypoint : waypoints) {
			ImageIcon icon = ImageUtilities.createImageIcon("pins/32/pin8.png");
			Point hotSpot = new Point(16, 31);
			waypoint.setIcon(icon, hotSpot);

			final float scale = 1.2f;
			ImageIcon selectedIcon = getScaledIcon(icon, scale);
			int x = (int) (selectedIcon.getIconWidth() / 2.0f);
			int y = (int) (selectedIcon.getIconHeight());
			Point selectedIconHotSpot = new Point(x, y);
			waypoint.setSelectedIcon(selectedIcon, selectedIconHotSpot);
		}

		return new MapWaypointPainter(layer, waypoints);
	}

	public MapPainter getStartFinishMarkerMapPainter(MapLayer layer,
			Trackpoint start, Trackpoint finish) {
		List<Waypoint> markers = new ArrayList<>();

		if (start.getLongitude() != null && start.getLatitude() != null) {
			String startName = Messages.getMessage("marker.start.name");
			ImageIcon startIcon = getStartMarkerIcon();
			Point hotSpot = new Point(16, 31);
			Waypoint startWaypoint = toWaypoint(startName, startIcon, hotSpot,
					start.getLongitude(), start.getLatitude(),
					start.getAltitude(), start.getTimestamp());
			markers.add(startWaypoint);
		}

		if (finish.getLongitude() != null && finish.getLatitude() != null) {
			String finishName = Messages.getMessage("marker.finish.name");
			ImageIcon finishIcon = getFinishMarkerIcon();
			Point hotSpot = new Point(16, 31);
			Waypoint finishWaypoint = toWaypoint(finishName, finishIcon,
					hotSpot, finish.getLongitude(), finish.getLatitude(),
					finish.getAltitude(), finish.getTimestamp());
			markers.add(finishWaypoint);
		}

		return new MapWaypointPainter(layer, markers);
	}

	public MapPainter getLapsMarkerMapPainter(MapLayer layer, List<Lap> laps) {
		List<Waypoint> lapMarkers = new ArrayList<Waypoint>();

		for (int i = 1; i <= laps.size(); i++) {
			Lap lap = laps.get(i - 1);
			String lapName = String.format("Lap #%d", i);

			ImageIcon lapMarkerIcon = getLapMarkerIcon(i);
			Point hotSpot = new Point(16, 31);
			Trackpoint trackpoint = lap.getLastTrackpoint();
			if (trackpoint == null || trackpoint.getLongitude() == null
					|| trackpoint.getLatitude() == null) {
				return new NullMapPainter();
			}

			Waypoint lapMarkerWaypoint = toWaypoint(lapName, lapMarkerIcon,
					hotSpot, trackpoint.getLongitude(),
					trackpoint.getLatitude(), trackpoint.getAltitude(),
					trackpoint.getTimestamp());

			lapMarkers.add(lapMarkerWaypoint);
		}

		return new MapWaypointPainter(layer, lapMarkers);
	}

	private Waypoint toWaypoint(String name, ImageIcon icon, Point hotSpot,
			Double longitude, Double latitude, Double altitude, Date time) {

		Waypoint waypoint = new Waypoint(latitude, longitude, altitude, name,
				time);

		waypoint.setIcon(icon, hotSpot);

		final float scale = 1.2f;
		ImageIcon selectedIcon = getScaledIcon(icon, scale);
		int x = (int) (selectedIcon.getIconWidth() / 2.0f);
		int y = (int) (selectedIcon.getIconHeight());
		Point selectedIconHotSpot = new Point(x, y);
		waypoint.setSelectedIcon(selectedIcon, selectedIconHotSpot);

		return waypoint;
	}

	private ImageIcon getCoursePointIcon(CoursePointType type) {
		ImageIcon baseIcon = ImageUtilities.createImageIcon("pins/32/pin2.png");
		BufferedImage icon = ImageUtilities.toBufferedImage(baseIcon);

		BufferedImage coursePointIcon = ImageUtilities.combineImages(icon,
				getCoursePointTypeIcon(type));

		return new ImageIcon(coursePointIcon);
	}

	private ImageIcon getStartMarkerIcon() {
		ImageIcon baseIcon = ImageUtilities.createImageIcon("pins/32/pin1.png");
		BufferedImage icon = ImageUtilities.toBufferedImage(baseIcon);

		BufferedImage startIcon = new BufferedImage(32, 32,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = startIcon.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(Color.WHITE);

		int[] x = new int[] { 13, 22, 13 };
		int[] y = new int[] { 8, 12, 16 };
		Polygon triangle = new Polygon(x, y, x.length);
		graphics.fillPolygon(triangle);

		graphics.dispose();

		BufferedImage startMarkerIcon = ImageUtilities.combineImages(icon,
				startIcon);

		return new ImageIcon(startMarkerIcon);
	}

	private ImageIcon getFinishMarkerIcon() {
		ImageIcon baseIcon = ImageUtilities.createImageIcon("pins/32/pin1.png");
		BufferedImage icon = ImageUtilities.toBufferedImage(baseIcon);

		BufferedImage finishIcon = new BufferedImage(32, 32,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = finishIcon.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(Color.WHITE);
		graphics.fillRect(13, 8, 7, 7);
		graphics.dispose();

		BufferedImage finishMarkerIcon = ImageUtilities.combineImages(icon,
				finishIcon);

		return new ImageIcon(finishMarkerIcon);
	}

	private ImageIcon getLapMarkerIcon(int lapNumber) {
		ImageIcon baseIcon = ImageUtilities.createImageIcon("pins/32/pin1.png");
		BufferedImage icon = ImageUtilities.toBufferedImage(baseIcon);
		BufferedImage textLayer = getTextLayer(String.valueOf(lapNumber));

		BufferedImage lapMarkerIcon = ImageUtilities.combineImages(icon,
				textLayer);

		return new ImageIcon(lapMarkerIcon);
	}

	private ImageIcon getScaledIcon(ImageIcon icon, float factor) {
		return new ImageIcon(getScaledImage(icon.getImage(), factor));
	}

	private BufferedImage getScaledImage(Image image, float factor) {
		return ImageUtilities.resize(image,
				(int) (image.getWidth(null) * factor),
				(int) (image.getHeight(null) * factor));
	}

	private BufferedImage getCoursePointTypeIcon(CoursePointType type) {
		final int canvasWidth = 32;
		final int canvasHeight = 32;
		final int imageWidth = type.getIcon().getIconWidth();
		final int imageHeight = type.getIcon().getIconHeight();

		BufferedImage coursePointTypeIcon = new BufferedImage(canvasWidth,
				canvasHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = coursePointTypeIcon.createGraphics();
		float x = (canvasWidth / 2.0f) - (imageWidth / 2.0f);
		float y = (imageHeight / 2.0f) - 4f;
		graphics.drawImage(type.getIcon().getImage(), (int) x, (int) y,
				imageWidth, imageHeight, null);

		graphics.dispose();

		return coursePointTypeIcon;
	}

	private BufferedImage getTextLayer(String message) {
		final int canvasWidth = 32;
		final int canvasHeight = 32;
		final int imageWidth = 10;
		final int imageHeight = 10;

		BufferedImage textLayer = new BufferedImage(canvasWidth, canvasHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = textLayer.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Font font = new Font(Font.SANS_SERIF, Font.BOLD, 9);
		graphics.setFont(font);
		graphics.setColor(Color.WHITE);

		int textWidth = graphics.getFontMetrics().stringWidth(message);
		int textHeight = graphics.getFontMetrics().getHeight();

		float x = (imageWidth / 2.0f) - (textWidth / 2.0f) + 11;
		float y = canvasHeight - 7
				- ((imageHeight / 2.0f) + (textHeight / 2.0f));
		graphics.drawString(message, x, y);

		graphics.dispose();

		return textLayer;
	}
}