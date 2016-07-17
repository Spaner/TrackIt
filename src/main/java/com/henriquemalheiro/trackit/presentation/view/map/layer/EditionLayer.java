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
package com.henriquemalheiro.trackit.presentation.view.map.layer;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.utilities.Operation;
import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.Map.MapMode;
import com.henriquemalheiro.trackit.presentation.view.map.provider.RoutingType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.TransportMode;
import com.miguelpernas.trackit.business.utility.LegendMenu;

public class EditionLayer extends MapLayer implements EventPublisher {
	private static final long serialVersionUID = -910047318410541173L;
	private static EditionLayer editionLayer;
	private Map map;
	private static EditionToolbar editionToolbar;
	
	public EditionLayer(Map map) {
		super(map);
		DocumentManager.getInstance().setUpdateEdition(true);
		this.map = map;
		init();
	}

	private void init() {
		setOpaque(false);
		
		EventHandler handler = new EventHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
		addMouseWheelListener(handler);
		editionLayer = this;
		editionToolbar = new EditionToolbar(editionLayer);
		editionToolbar.setVisible(true);
	}
	
	public static void updateFollowRoads(){
		editionToolbar.dispose();
		editionToolbar = new EditionToolbar(editionLayer);
		editionToolbar.setVisible(true);
	}
	
	@Override
	public void finish() {
		editionToolbar.setVisible(false);
		DocumentManager.getInstance().setUpdateEdition(false);
		editionToolbar = null;
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.EDITION_LAYER;
	}
	
	@Override
	public List<Operation> getSupportedOperations(Location location) {
		List<Operation> supportedOperations = new ArrayList<Operation>();
		final boolean addToUndoManager = true;
		if (!map.getMapMode().equals(MapMode.EDITION)) {
			return supportedOperations;
		}
		
		List<DocumentItem> items = getItems();
		List<Trackpoint> candidateTrackpoints = new ArrayList<>();
		for (final DocumentItem item : items) {
			if (item.isCourse()) {
				Trackpoint trkpnt = getTrackpoint(item, location);
				if(trkpnt != null)
				candidateTrackpoints.add(trkpnt);
			}
		}
		
		if (!candidateTrackpoints.isEmpty()) {
			for (final Trackpoint trackpoint : candidateTrackpoints) {
				Operation deleteCoursePointOperation = new Operation(Messages.getMessage("operation.group.deleteTrackpoint"),
						getDeleteOperationName(trackpoint),
						Messages.getMessage("operation.description.deleteTrackpoint"),
						new Runnable() {
							@Override
							public void run() {
								DocumentManager.getInstance().removeTrackpoint((Course) trackpoint.getParent(), trackpoint, addToUndoManager);
							}
						});
				supportedOperations.add(deleteCoursePointOperation);
			}
		}
		
		return supportedOperations;
	}

	private String getDeleteOperationName(final Trackpoint trackpoint) {
		return String.format("%s %s (%s)",
				Messages.getMessage("operation.name.delete"),
				Messages.getMessage("trackpoint.label"),
				Formatters.getFormatedDistance(trackpoint.getDistance()));
	}
	
	private Trackpoint getTrackpoint(DocumentItem item, Location location) {
		double distance = Double.MAX_VALUE;
		Trackpoint candidateTrackpoint = null;
		
		for (Trackpoint trackpoint : item.getTrackpoints()) {
			double newDistance = Utilities.getGreatCircleDistance(trackpoint.getLatitude(), trackpoint.getLongitude(),
					location.getLatitude(), location.getLongitude()) * 1000.0;
			
			if (newDistance < distance) {
				distance = newDistance;
				candidateTrackpoint = trackpoint;
			}
		}
		
		final double referenceDistance = 100.0; 
		if (distance <= referenceDistance) {
			return candidateTrackpoint;
		}
		
		return null;
	}
	
	void restartCourse() {
		
		Object[] options = { Messages.getMessage("messages.yes"),
				Messages.getMessage("messages.no") };

		int dialogResult = JOptionPane.showOptionDialog(null,
				Messages.getMessage("editionToolbar.warning.restartCourse"),
				null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
	            null,
	            options,
	            options[1]);
		
		if (dialogResult == JOptionPane.YES_OPTION) {
			for (DocumentItem item : getItems()) {
				if (!item.isCourse()) {
					continue;
				}
				Course course = (Course) item;
				course.getTrackpoints().clear();
				course.getLaps().clear();
				course.getCoursePoints().clear();
				course.publishUpdateEvent(null);
			}
		}
	}

	private class EventHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent event) {
			redispatchMouseWheelEvent(event);
		}
		
		@Override
		public void mouseDragged(MouseEvent event) {
			handleMouseDragged(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseDragged(MouseEvent event) {
		}
		

		@Override
		public void mouseMoved(MouseEvent event) {
			handleMouseMoved(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseMoved(MouseEvent event) {
		}

		@Override
		public void mouseClicked(MouseEvent event) {
			handleMouseClick(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseClick(MouseEvent event) {
			if (SwingUtilities.isLeftMouseButton(event)) {
				processLeftClick(event);
			}
		}

		private void processLeftClick(MouseEvent event) {
			int numberOfClicks = event.getClickCount();
			
			switch (numberOfClicks) {
			case 2:
				processDoubleClick(event);
				break;
			case 1:
				processSingleClick(event);
				break;
			}
		}

		private void processSingleClick(MouseEvent event) {
			if (!isCourse()) {
				return;
			}
			
			Course course = (Course) map.getItems().get(0);
			Location location = getMapProvider().getLocation(event.getX(), event.getY(), getWidth(), getHeight());
			
			if (course.getTrackpoints().isEmpty()) {
				addTrackpoint(event, course, location);
			} else {
				addTrackpoints(event, course, location);
			}
		}
		
		private boolean isCourse() {
			List<DocumentItem> items = map.getItems();
			return (!items.isEmpty() && items.get(0).isCourse());
		}
		
		boolean getFollowRoads(Course course){
			boolean followRoads = course.getSubSport().getFollowRoads();
			return followRoads;
		}

		private void addTrackpoints(MouseEvent event, Course course, Location location) {
			try {
				RoutingType routingType = RoutingType.lookup(TrackIt.getPreferences().getPreference(
						Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.ROUTING_TYPE, RoutingType.FASTEST.getRoutingTypeName()));
				TransportMode transportMode = TransportMode.lookup(TrackIt.getPreferences().getPreference(
						Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.TRANSPORT_MODE, TransportMode.CAR.getTransportModeName()));
				/*boolean followRoads = TrackIt.getPreferences().getBooleanPreference(
						Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.FOLLOW_ROADS, true);*/
				//boolean followRoads = course.getSubSport().getFollowRoads();
				boolean followRoads = getFollowRoads(course);
				boolean avoidHighways = TrackIt.getPreferences().getBooleanPreference(
						Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.AVOID_HIGHWAYS, true);
				boolean avoidTollRoads = TrackIt.getPreferences().getBooleanPreference(
						Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.AVOID_TOLL_ROADS, true);
				boolean addDirectionCoursePoints = TrackIt.getPreferences().getBooleanPreference(
						Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.ADD_COURSE_POINTS_AT_JUNCTIONS, true);
				final boolean addToUndoManager = true;
				if (followRoads) {
					java.util.Map<String, Object> routingOptions = new HashMap<>();
					routingOptions.put(Constants.RoutingOptions.ROUTING_TYPE, routingType);
					routingOptions.put(Constants.RoutingOptions.TRANSPORT_MODE, transportMode);
					routingOptions.put(Constants.RoutingOptions.AVOID_HIGHWAYS, avoidHighways);
					routingOptions.put(Constants.RoutingOptions.AVOID_TOLL_ROADS, avoidTollRoads);
					routingOptions.put(Constants.RoutingOptions.ADD_DIRECTION_COURSE_POINTS, addDirectionCoursePoints);
					
					DocumentManager.getInstance().appendRoute(getMapProvider(), routingOptions, course, location, addToUndoManager);
				} else {
					addTrackpoint(event, course, location);
				}
			} catch (TrackItException e) {
				JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), e.getMessage(), "", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		
		private void addTrackpoint(MouseEvent event, Course course, Location location) {
			List<Trackpoint> trackpoints = course.getTrackpoints();
			boolean addToUndoManager = true;
			Trackpoint trackpoint = new Trackpoint(course);
			trackpoint.setLongitude(location.getLongitude());
			trackpoint.setLatitude(location.getLatitude());
			
			if (shiftClick(event)) {
				DocumentManager.getInstance().addTrackpoint(course, trackpoint, calculateTrackpointIndex(trackpoints, trackpoint), addToUndoManager);
			} else {
				DocumentManager.getInstance().addTrackpoint(course, trackpoint, addToUndoManager);
			}
		}

		private boolean shiftClick(MouseEvent event) {
			return ((event.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK)) == InputEvent.SHIFT_DOWN_MASK);
		}
		
		private int calculateTrackpointIndex(List<Trackpoint> trackpoints, Trackpoint newTrackpoint) {
			Trackpoint trackpoint = getNearestTrackpoint(trackpoints, new Location(newTrackpoint.getLongitude(), newTrackpoint.getLatitude()));
			Trackpoint previousTrackpoint = getPreviousTrackpoint(trackpoints, trackpoint);
			Trackpoint nextTrackpoint = getNextTrackpoint(trackpoints, trackpoint);
			
			if (previousTrackpoint == null) {
				return 0;
			} else if (nextTrackpoint == null) {
				return trackpoints.size();
			}
			
			Line2D.Double lineToPrevious = getLine(previousTrackpoint, trackpoint);
			Line2D.Double lineToNext = getLine(trackpoint, nextTrackpoint);
			Point2D.Double newPoint = getPointOnScreen(newTrackpoint);
			
			double distToLineToPrevious = lineToPrevious.ptSegDist(newPoint.x, newPoint.y);
			double distToLineToNext = lineToNext.ptSegDist(newPoint.x, newPoint.y);
			
			if (distToLineToNext <= distToLineToPrevious) {
				return trackpoints.indexOf(trackpoint) + 1;
			} else {
				return trackpoints.indexOf(trackpoint);
			}
		}
		
		private Trackpoint getNearestTrackpoint(List<Trackpoint> trackpoints, Location location) {
			Trackpoint nearestTrackpoint = null;
			
			List<Trackpoint> candidateTrackpoints = getCandidateTrackpoints(trackpoints, location, 1000.0);
			if (!candidateTrackpoints.isEmpty()) {
				nearestTrackpoint = candidateTrackpoints.get(0);
			}
			
			return nearestTrackpoint;
		}
		
		private List<Trackpoint> getCandidateTrackpoints(List<Trackpoint> trackpoints, final Location location, double referenceDistance) {
			List<Trackpoint> candidateTrackpoints = new ArrayList<Trackpoint>();
			
			for (Trackpoint trackpoint : trackpoints) {
				double distance = Utilities.getGreatCircleDistance(trackpoint.getLatitude(), trackpoint.getLongitude(),
						location.getLatitude(), location.getLongitude()) * 1000.0;
				
				if (distance < referenceDistance) {
					candidateTrackpoints.add(trackpoint);
				}
			}
			
			Collections.sort(candidateTrackpoints, new Comparator<Trackpoint>() {

				@Override
				public int compare(Trackpoint trackpoint1, Trackpoint trackpoint2) {
					Double distance1 = Utilities.getGreatCircleDistance(trackpoint1.getLatitude(), trackpoint1.getLongitude(),
							location.getLatitude(), location.getLongitude()) * 1000.0;
					Double distance2 = Utilities.getGreatCircleDistance(trackpoint2.getLatitude(), trackpoint2.getLongitude(),
							location.getLatitude(), location.getLongitude()) * 1000.0;
					return distance1.compareTo(distance2);
				}
			});
			
			return candidateTrackpoints;
		}
		
		private Trackpoint getPreviousTrackpoint(List<Trackpoint> trackpoints, Trackpoint trackpoint) {
			Trackpoint previousTrackpoint = null;
			
			int trackpointIndex = trackpoints.indexOf(trackpoint);
			if (trackpointIndex > 0) {
				previousTrackpoint = trackpoints.get(trackpointIndex - 1);
			}
			
			return previousTrackpoint;
		}
		
		private Trackpoint getNextTrackpoint(List<Trackpoint> trackpoints, Trackpoint trackpoint) {
			Trackpoint nextTrackpoint = null;
			
			int trackpointIndex = trackpoints.indexOf(trackpoint);
			if (trackpointIndex < (trackpoints.size() - 1)) {
				nextTrackpoint = trackpoints.get(trackpointIndex + 1);
			}
			
			return nextTrackpoint;
		}
		
		private Line2D.Double getLine(Trackpoint trackpoint1, Trackpoint trackpoint2) {
			Point2D.Double trackpoint1XY = getPointOnScreen(trackpoint1);
			Point2D.Double trackpoint2XY = getPointOnScreen(trackpoint2);
			
			if (trackpoint2XY.x > trackpoint1XY.x) {
				return new Line2D.Double(trackpoint1XY, trackpoint2XY);
			} else {
				return new Line2D.Double(trackpoint2XY, trackpoint1XY);
			}
		}
		
		private Point2D.Double getPointOnScreen(Trackpoint trackpoint) {
			final double centerX = getWidth() / 2.0;
			final double centerY = getHeight() / 2.0;
			
			int[] offset = getMapProvider().getCenterOffsetInPixels(trackpoint.getLongitude(), trackpoint.getLatitude());
			double x = centerX + offset[0];
			double y = centerY + offset[1];
			
			return new Point2D.Double(x, y);
		}
		
		private void processDoubleClick(MouseEvent event) {
		}

		@Override
		public void mouseEntered(MouseEvent event) {
			handleMouseEntered(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseEntered(MouseEvent event) {
		}

		@Override
		public void mouseExited(MouseEvent event) {
			handleMouseExited(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseExited(MouseEvent event) {
		}

		@Override
		public void mousePressed(MouseEvent event) {
			handleMousePressed(event);
			redispatchMouseEvent(event);
		}

		private void handleMousePressed(MouseEvent event) {
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			handleMouseReleased(event);
			redispatchMouseEvent(event);
		}
		
		private void handleMouseReleased(MouseEvent event) {
		}

		private void redispatchMouseEvent(MouseEvent event) {
			EventsLayer eventsLayer = (EventsLayer) map.getLayer(MapLayerType.EVENTS_LAYER);
			eventsLayer.dispatchEvent(new MouseEvent(eventsLayer, event.getID(), event.getWhen(), event.getModifiers(),
						event.getX(), event.getY(), event.getClickCount(), event.isPopupTrigger()));
		}
		
		private void redispatchMouseWheelEvent(MouseWheelEvent event) {
			EventsLayer eventsLayer = (EventsLayer) map.getLayer(MapLayerType.EVENTS_LAYER);
			eventsLayer.dispatchEvent(new MouseWheelEvent(eventsLayer, event.getID(), event.getWhen(), event.getModifiers(),
						event.getX(), event.getY(), event.getClickCount(), event.isPopupTrigger(), event.getScrollType(),
						event.getScrollAmount(), event.getWheelRotation()));
		}
	}
}