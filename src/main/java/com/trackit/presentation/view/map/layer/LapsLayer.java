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
package com.trackit.presentation.view.map.layer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trackit.business.DocumentManager;
import com.trackit.business.common.BoundingBox;
import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CourseLap;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.utilities.Utilities;
import com.trackit.presentation.utilities.Operation;
import com.trackit.presentation.view.map.Map;
import com.trackit.presentation.view.map.painter.MapPainter;
import com.trackit.presentation.view.map.painter.MapPainterFactory;

public class LapsLayer extends MapLayer {
	private static final long serialVersionUID = -3345263425389015474L;
	
	Set<DocumentItem> itemsToPaint;

	public LapsLayer(Map map) {
		super(map);
		init();
	}

	private void init() {
		itemsToPaint = new HashSet<DocumentItem>();
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.LAPS_LAYER;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;
		
		super.paintComponent(graphics);
		
		itemsToPaint.clear();
		for (DocumentItem item : getItems()) {
			while (item != null && !item.isCourse() && !item.isActivity()) {
				item = item.getParent();
			}
			
			if (item != null) {
				itemsToPaint.add(item);
			}
		}
		
		for (DocumentItem item : itemsToPaint) {
			List<Lap> laps = getLaps(item);
			
			MapPainter painter = MapPainterFactory.getInstance().getLapsMarkerMapPainter(this, laps);
			painter.paint(graphics, new HashMap<String, Object>());
		}
	}
	
	private List<Lap> getLaps(DocumentItem item) {
		List<Lap> laps = new ArrayList<Lap>();
		
		if (item.isActivity()) {
			laps.addAll(((Activity) item).getLaps());
		} else if (item.isCourse()) {
			laps.addAll(((Course) item).getLaps());
		}
		
		return laps;
	}
	
	@Override
	public List<Operation> getSupportedOperations(Location location) {
		List<Operation> supportedOperations = new ArrayList<Operation>();
		
		addAddLapOperation(location, supportedOperations);
		addRemoveLapOperation(location, supportedOperations);
		
		return supportedOperations;
	}

	private void addAddLapOperation(Location location, List<Operation> supportedOperations) {
		final Trackpoint trackpoint = getTrackpoint(location);
		if (trackpoint != null) {
			Operation addLapOperation = new Operation(Messages.getMessage("operation.group.addLap"),
					Messages.getMessage("operation.name.addLap"),
					Messages.getMessage("operation.description.addLap"),
					new Runnable() {
						@Override
						public void run() {
							DocumentManager.getInstance().addLap((Course) trackpoint.getParent(), trackpoint);
						}
					});
			supportedOperations.add(addLapOperation);
		}
	}
	
	private Trackpoint getTrackpoint(Location location) {
		final double searchRadius = 25.0;
		double distance = Double.MAX_VALUE;
		Trackpoint candidateTrackpoint = null;
		BoundingBox searchArea = new BoundingBox(location, searchRadius);
		
		for (DocumentItem item : getItems()) {
			if (!item.isCourse()) {
				continue;
			}

			for (Trackpoint trackpoint : item.getTrackpoints()) {
				if (!searchArea.contains(new Location(trackpoint.getLongitude(), trackpoint.getLatitude()))) {
					continue;
				}
				
				double newDistance = Utilities.getGreatCircleDistance(trackpoint.getLatitude(), trackpoint.getLongitude(),
						location.getLatitude(), location.getLongitude()) * 1000.0;
				
				if (newDistance < distance) {
					distance = newDistance;
					candidateTrackpoint = trackpoint;
				}
			}
		}
			
		return (distance <= searchRadius ? candidateTrackpoint : null);
	}
	
	private void addRemoveLapOperation(Location location, List<Operation> supportedOperations) {
		final CourseLap lap = getLap(location);
		if (lap != null) {
			Operation removeLapOperation = new Operation(Messages.getMessage("operation.group.removeLap"),
					Messages.getMessage("operation.name.removeLap"),
					Messages.getMessage("operation.description.removeLap"),
					new Runnable() {
						@Override
						public void run() {
							DocumentManager.getInstance().removeLap(lap);
						}
					});
			supportedOperations.add(removeLapOperation);
		}
	}
	
	private CourseLap getLap(Location location) {
		final double searchRadius = 25.0;
		BoundingBox searchArea = new BoundingBox(location, searchRadius);
		
		for (DocumentItem item : getItems()) {
			if (!item.isCourse()) {
				continue;
			}

			Course course = (Course) item;
			for (Trackpoint trackpoint : course.getTrackpoints()) {
				if (!searchArea.contains(new Location(trackpoint.getLongitude(), trackpoint.getLatitude()))) {
					continue;
				}
				
				for (Lap lap : course.getLaps()) {
					if (lap.getStartTime().equals(trackpoint.getTimestamp())) {
						return (CourseLap) lap;
					}
				}
			}
		}
			
		return null;
	}
}
