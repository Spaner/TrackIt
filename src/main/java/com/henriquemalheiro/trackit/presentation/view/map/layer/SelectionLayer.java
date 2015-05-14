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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.Map.MapMode;
import com.henriquemalheiro.trackit.presentation.view.map.MapUtilities;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterStyle;

public class SelectionLayer extends MapLayer {
	private static final long serialVersionUID = -3345263425389015474L;
	
	Set<DocumentItem> itemsToPaint;

	public SelectionLayer(Map map) {
		super(map);
		init();
	}

	private void init() {
		itemsToPaint = new HashSet<DocumentItem>();
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.SELECTION_LAYER;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;
		
		super.paintComponent(graphics);
		
		itemsToPaint.clear();
		for (DocumentItem item : getItems()) {
			if (!item.isActivity() && !item.isCourse()) {
				itemsToPaint.add(item);
			}
		}
		
		for (DocumentItem item : itemsToPaint) {
			java.util.Map<String, Object> attributes = MapUtilities.getPaintingAttributes(item, MapPainterStyle.SELECTION);
			attributes.put(Constants.PAINTING_ATTRIBUTES.SIMPLIFY, map.isSimplifyTracks());
			
			item.paint(graphics, this, attributes);
		}
	}
	
	@Override
	public List<DocumentItem> getItems(Location location) {
		List<DocumentItem> items = new ArrayList<DocumentItem>();
		
		if (map.getMode() != MapMode.SELECTION && map.getMode() != MapMode.MULTI) {
			return items;
		}
		
		List<Trackpoint> trackpoints = new ArrayList<Trackpoint>();
		for (DocumentItem item : map.getItems()) {
			while (item != null && !item.isActivity() && !item.isCourse()) {
				item = item.getParent();
			}
			
			if (item != null && (item.isActivity() || item.isCourse())) {
				trackpoints.addAll(item.getTrackpoints());
			}
		}
		
		double distance = Double.MAX_VALUE;
		Trackpoint candidateTrackpoint = null;
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLongitude() == null || trackpoint.getLatitude() == null) {
				continue;
			}
			
			double newDistance = Utilities.getGreatCircleDistance(trackpoint.getLatitude(), trackpoint.getLongitude(),
					location.getLatitude(), location.getLongitude()) * 1000.0;
			
			if (newDistance < distance) {
				distance = newDistance;
				candidateTrackpoint = trackpoint;
			}
		}
		
		final double referenceDistance = 250.0; 
		if (distance <= referenceDistance) {
			items.add(candidateTrackpoint);
		}
		
		return items;
	}
}
