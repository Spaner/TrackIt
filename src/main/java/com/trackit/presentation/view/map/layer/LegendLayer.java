/*
 * This file is part of Track It!.
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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.utilities.LegendMenu;
import com.trackit.business.utilities.MultipleViewMenu;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.view.map.Map;
import com.trackit.presentation.view.map.MapView;

public class LegendLayer extends MapLayer implements EventPublisher,
		EventListener {
	private static final long serialVersionUID = -5710441980337137403L;
	private static LegendMenu legendMenu;

	public LegendLayer(Map map) {
		super(map);
		DocumentManager.getInstance().setUpdateLegend(true);
		setOpaque(false);
		EventHandler handler = new EventHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
		addMouseWheelListener(handler);
		MapView mv = TrackIt.getApplicationPanel().getMapView();
		mv.setItems(new ArrayList<DocumentItem>());
		mv.updateDisplay();
		legendMenu = new LegendMenu();
		legendMenu.setVisible(true);
	}

	public static LegendMenu getLegendMenu(){
		return legendMenu;
	}
	
	public static void update(){
		legendMenu.dispose();
		legendMenu = new LegendMenu();
		legendMenu.setVisible(true);
	}
	
	public void finish() {
		legendMenu.setVisible(false);
		DocumentManager.getInstance().setUpdateLegend(false);
	}

	@Override
	public void process(Event event, DocumentItem item) {

	}

	@Override
	public void process(Event event, DocumentItem parent,
			List<? extends DocumentItem> items) {

	}

	@Override
	public MapLayerType getType() {
		return MapLayerType.LEGEND_LAYER;
	}

	private class EventHandler implements MouseListener, MouseMotionListener,
			MouseWheelListener {

		@Override
		public void mouseWheelMoved(MouseWheelEvent event) {
			redispatchMouseWheelEvent(event);
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			redispatchMouseEvent(event);
		}

		@Override
		public void mouseMoved(MouseEvent event) {
			redispatchMouseEvent(event);
		}

		@Override
		public void mouseClicked(MouseEvent event) {
			redispatchMouseEvent(event);
		}

		@Override
		public void mousePressed(MouseEvent event) {
			redispatchMouseEvent(event);
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			redispatchMouseEvent(event);
		}

		@Override
		public void mouseEntered(MouseEvent event) {
			redispatchMouseEvent(event);
		}

		@Override
		public void mouseExited(MouseEvent event) {
			redispatchMouseEvent(event);
		}

		private void redispatchMouseEvent(MouseEvent event) {
			HighlightLayer eventsLayer = (HighlightLayer) map
					.getLayer(MapLayerType.HIGHLIGHT_LAYER);
			eventsLayer.dispatchEvent(new MouseEvent(eventsLayer,
					event.getID(), event.getWhen(), event.getModifiers(), event
							.getX(), event.getY(), event.getClickCount(), event
							.isPopupTrigger()));
		}

		private void redispatchMouseWheelEvent(MouseWheelEvent event) {
			HighlightLayer eventsLayer = (HighlightLayer) map
					.getLayer(MapLayerType.HIGHLIGHT_LAYER);
			eventsLayer.dispatchEvent(new MouseWheelEvent(eventsLayer, event
					.getID(), event.getWhen(), event.getModifiers(), event
					.getX(), event.getY(), event.getClickCount(), event
					.isPopupTrigger(), event.getScrollType(), event
					.getScrollAmount(), event.getWheelRotation()));
		}

	}
}
