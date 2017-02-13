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
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.MapUtilities;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterStyle;

public class HighlightLayer extends MapLayer {
	private static final long serialVersionUID = 2194682578817877643L;
	
	private Trackpoint highlight;

	public HighlightLayer(Map map) {
		super(map);
		init();
	}

	private void init() {
		highlight = null;
		addMouseMotionListener(new MouseMotionHandler());
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.HIGHLIGHT_LAYER;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;

		super.paintComponent(graphics);
		
		if (highlight != null) {
			java.util.Map<String, Object> attributes = MapUtilities.getPaintingAttributes(highlight, MapPainterStyle.HIGHLIGHT);
			highlight.paint(graphics, this, attributes);
		}
	}
	
	public void setHighlight(Trackpoint trackpoint) {
		highlight = trackpoint;
		map.refresh();
	}
	
	private class MouseMotionHandler extends MouseMotionAdapter implements MouseMotionListener {
		@Override
		public void mouseMoved(MouseEvent event) {
			handleMouseMoved(event);
		}

		private void handleMouseMoved(MouseEvent event) {
			Location mouseLocation = getMapProvider().getLocation(event.getX(), event.getY(), getWidth(), getHeight());
			
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
						mouseLocation.getLatitude(), mouseLocation.getLongitude()) * 1000.0;
				
				if (newDistance < distance && !(map.isSimplifyTracks() && !trackpoint.isViewable())) {
					distance = newDistance;
					candidateTrackpoint = trackpoint;
				}
			}
			
			final double referenceDistance = 250.0; 
			if (distance <= referenceDistance) {
				highlight = candidateTrackpoint;
				highlight.publishHighlightEvent(null);
			} else {
				highlight = null;
				EventManager.getInstance().publish(null, Event.TRACKPOINT_HIGHLIGHTED, null);
			}
			
			map.refresh();
		}
	}
}