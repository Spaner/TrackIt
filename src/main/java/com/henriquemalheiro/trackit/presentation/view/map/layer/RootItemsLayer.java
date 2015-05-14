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
import java.util.HashSet;
import java.util.Set;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.MapUtilities;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterStyle;

public class RootItemsLayer extends MapLayer {
	private static final long serialVersionUID = 1327152070939782671L;
	private Set<DocumentItem> itemsToPaint;
	
	public RootItemsLayer(Map map) {
		super(map);
		init();
	}

	private void init() {
		itemsToPaint = new HashSet<DocumentItem>();
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.ROOT_ITEMS_LAYER;
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
			} else if (item instanceof Waypoint) {
				GPSDocument document = (GPSDocument) item.getParent();
				itemsToPaint.addAll(document.getActivities());
				itemsToPaint.addAll(document.getCourses());
			} else {
				while (item != null && !item.isCourse() && !item.isActivity()) {
					item = item.getParent();
				}
				
				if (item != null) {
					itemsToPaint.add(item);
				}
			}
		}
		
		for (DocumentItem item : itemsToPaint) {
			GPSDocument document = (GPSDocument) item.getParent();
			for (Activity activity : document.getActivities()) {
				if (!itemsToPaint.contains(activity)) {
					java.util.Map<String, Object> attributes = MapUtilities.getPaintingAttributes(activity, MapPainterStyle.HIGHLIGHT);
					attributes.put(Constants.PAINTING_ATTRIBUTES.SIMPLIFY, map.isSimplifyTracks());
					activity.paint(graphics, this, attributes);
				}
			}
			
			for (Course course : document.getCourses()) {
				if (!itemsToPaint.contains(course)) {
					java.util.Map<String, Object> attributes = MapUtilities.getPaintingAttributes(course, MapPainterStyle.HIGHLIGHT);
					attributes.put(Constants.PAINTING_ATTRIBUTES.SIMPLIFY, map.isSimplifyTracks());
					course.paint(graphics, this, attributes);
				}
			}
		}
		
		for (DocumentItem item : itemsToPaint) {
			java.util.Map<String, Object> attributes = MapUtilities.getPaintingAttributes(item, MapPainterStyle.REGULAR);
			attributes.put(Constants.PAINTING_ATTRIBUTES.SIMPLIFY, map.isSimplifyTracks());
			
			item.paint(graphics, this, attributes);
		}
	}
}
