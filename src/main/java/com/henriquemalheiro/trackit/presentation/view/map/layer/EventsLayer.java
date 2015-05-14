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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.exception.MaxZoomExceededException;
import com.henriquemalheiro.trackit.business.exception.MinZoomExceededException;
import com.henriquemalheiro.trackit.presentation.utilities.Operation;
import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProvider;

public class EventsLayer extends MapLayer {
	private static final long serialVersionUID = -910047318410541173L;
	
	private Map map;
	
	private int initialX;
	private int initialY;
	private int lastX;
	private int lastY;
	
	public EventsLayer(Map map) {
		super(map);
		this.map = map;
		init();
	}

	private void init() {
		setOpaque(false);
		
		EventHandler handler = new EventHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
		addMouseWheelListener(handler);
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.EVENTS_LAYER;
	}

	private class EventHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent event) {
			handleMouseWheelMoved(event);
		}

		private void handleMouseWheelMoved(MouseWheelEvent event) {
			MapProvider mapProvider = getMapProvider();
			
			if (event.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				byte minZoom = mapProvider.getMinZoom();
				byte maxZoom = mapProvider.getMaxZoom();
				byte zoom = mapProvider.getZoom();
				
				int offset = event.getWheelRotation();
				try {
					if (offset > 0 && zoom > minZoom) {
						mapProvider.decreaseZoom(getWidth(), getHeight());
					} else if (offset < 0 && zoom < maxZoom) {
						mapProvider.increaseZoom(getWidth(), getHeight());
					}
				} catch (MinZoomExceededException e1) {
					return;
				} catch (MaxZoomExceededException e1) {
					return;
				}
				
				map.refresh();
			}
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			handleMouseDragged(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseDragged(MouseEvent event) {
			int newX = event.getX() - lastX;
			int newY = event.getY() - lastY;

			lastY += newY;
			lastX += newX;
			
			switch (map.getMode()) {
			case SIMPLE:
			case EDITION:
			case SELECTION:
			case ANIMATION:
			case AREA://58406
			case MULTI:
				map.setCursor(Map.draggingCursor);
				map.pan(newX, newY);
				break;
			case ZOOMING:
				map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				break;
			}
			
			map.updateMapInfo(event.getX(), event.getY());
		}
		

		@Override
		public void mouseMoved(MouseEvent event) {
			handleMouseMoved(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseMoved(MouseEvent event) {
			map.updateMapInfo(event.getX(), event.getY());
		}

		@Override
		public void mouseClicked(MouseEvent event) {
			handleMouseClick(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseClick(MouseEvent event) {
			if (SwingUtilities.isRightMouseButton(event)) {
				processRightClick(event);
			} else {
				processLeftClick(event);
			}
		}

		private void processRightClick(MouseEvent event) {
			List<Operation> supportedOperations = new ArrayList<Operation>();
			Location location = getMapProvider().getLocation(event.getX(), event.getY(), getWidth(), getHeight());
			
			for (MapLayer layer : map.getLayers()) {
				supportedOperations.addAll(layer.getSupportedOperations(location));
			}
			
			displayOperationsPopUpMenu(location, supportedOperations);
		}
		
		private void displayOperationsPopUpMenu(Location location, List<Operation> operations) {
			JPopupMenu menu = new JPopupMenu();
			
			for (final Operation operation : operations) {
				JMenuItem menuItem = new JMenuItem(operation.getName());
				menuItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						operation.actionPerformed(null);
						map.refresh();
					}
				});
				
				menu.add(menuItem);
			}
			
			map.displayPopUpMenu(location, menu);
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
			List<DocumentItem> selectableItems = new ArrayList<DocumentItem>();
			Location location = getMapProvider().getLocation(event.getX(), event.getY(), getWidth(), getHeight());
			
			for (MapLayer layer : map.getLayers()) {
				selectableItems.addAll(layer.getItems(location));
			}
			
			if (selectableItems.size() == 1) {
				selectableItems.get(0).publishSelectionEvent(null);
			} else if (selectableItems.size() > 1) {
				displayPopUpMenu(location, selectableItems);
			}
		}
		
		private void displayPopUpMenu(Location location, List<DocumentItem> items) {
			JPopupMenu menu = new JPopupMenu();
			
			for (final DocumentItem item : items) {
				JMenuItem menuItem = new JMenuItem(item.getDocumentItemName());
				menuItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						item.publishSelectionEvent(null);
					}
				});
				
				menu.add(menuItem);
			}
			
			map.displayPopUpMenu(location, menu);
		}

		private void processDoubleClick(MouseEvent event) {
			MapProvider mapProvider = getMapProvider();
			
			int xOffset = event.getX() - (getWidth() / 2);
			int yOffset = (getHeight() / 2) - event.getY();

			mapProvider.moveCenterLocation(xOffset, -yOffset, getWidth(), getHeight());

			try {
				mapProvider.increaseZoom(getWidth(), getHeight());
			} catch (MaxZoomExceededException e) {
				return;
			}
			
			map.refresh();
		}

		@Override
		public void mouseEntered(MouseEvent event) {
			handleMouseEntered(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseEntered(MouseEvent event) {
			map.updateMapInfo(event.getX(), event.getY());
		}

		@Override
		public void mouseExited(MouseEvent event) {
			handleMouseExited(event);
			redispatchMouseEvent(event);
		}

		private void handleMouseExited(MouseEvent event) {
			map.updateMapInfo(event.getX(), event.getY());
		}

		@Override
		public void mousePressed(MouseEvent event) {
			handleMousePressed(event);
			redispatchMouseEvent(event);
		}

		private void handleMousePressed(MouseEvent event) {
			initialX = event.getX();
			initialY = event.getY();
			lastX = initialX;
			lastY = initialY;
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			handleMouseReleased(event);
			redispatchMouseEvent(event);
		}
		
		private void handleMouseReleased(MouseEvent event) {
			switch (map.getMode()) {
			case EDITION:
				map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				break;
			case SELECTION:
			case MULTI: //58406
				map.setCursor(Map.targetCursor);
				break;
			case ZOOMING:
				// Do nothing
				break;
			default:
				map.setCursor(Map.draggableCursor);
			}
		}

		private void redispatchMouseEvent(MouseEvent event) {
			List<MapLayer> layers = map.getLayers();
			for (MapLayer layer : layers) {
				if (layer.equals(EventsLayer.this) || layer.getType().getPriority() > EventsLayer.this.getType().getPriority()) {
					continue;
				}
				
				layer.dispatchEvent(new MouseEvent(layer, event.getID(), event.getWhen(), event.getModifiers(),
						event.getX(), event.getY(), event.getClickCount(), event.isPopupTrigger()));
			}
		}
	}
}
