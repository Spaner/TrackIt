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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Trackpoint;
import com.trackit.presentation.view.map.Map;
import com.trackit.presentation.view.map.painter.MapPainter;
import com.trackit.presentation.view.map.painter.MapPainterFactory;

public class StartFinishLayer extends MapLayer {
	private static final long serialVersionUID = 868019341841095506L;
	
	private Set<DocumentItem> itemsToPaint;

	public StartFinishLayer(Map map) {
		super(map);
		init();
	}

	private void init() {
		itemsToPaint = new HashSet<DocumentItem>();
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.START_FINISH_LAYER;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;
		
		super.paintComponent(graphics);
		
		itemsToPaint.clear();
		for (DocumentItem item : getItems()) {
			if (item instanceof GPSDocument) {
				GPSDocument document = (GPSDocument) item;
				itemsToPaint.addAll(document.getActivities());
				itemsToPaint.addAll(document.getCourses());
			} else {
				while (item != null && !item.isCourse() && !item.isActivity()) {
					item = item.getParent();
				}
				
				if (item != null && (item.isCourse() || item.isActivity())) {
					itemsToPaint.add(item);
				}
			}
		}
		
		for (DocumentItem item : itemsToPaint) {
			List<Trackpoint> trackpoints = item.getTrackpoints();
			if (trackpoints.isEmpty()) {
				return;
			}
			
			MapPainter painter = MapPainterFactory.getInstance().getStartFinishMarkerMapPainter(this,
					trackpoints.get(0), trackpoints.get(trackpoints.size() - 1));
			painter.paint(graphics, new HashMap<String, Object>());
		}
	}
}
