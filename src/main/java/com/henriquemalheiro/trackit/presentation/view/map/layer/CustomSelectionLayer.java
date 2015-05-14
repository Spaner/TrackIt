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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.Map.MapMode;

public class CustomSelectionLayer extends MapLayer {
	private static final long serialVersionUID = 4375257826181378903L;

	private Map map;
	private Selection selection;
	
	private int initialX;
	private int initialY;
	private int lastX;
	private int lastY;
	
	public CustomSelectionLayer(Map map) {
		super(map);
		this.map = map;
		init();
	}

	private void init() {
		setOpaque(false);
		
		selection = new Selection(0, 0, 0, 0);
		
		EventHandler handler = new EventHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.CUSTOM_SELECTION_LAYER;
	}

	private class EventHandler extends MouseAdapter implements MouseListener, MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent event) {
			handleMouseDragged(event);
		}

		private void handleMouseDragged(MouseEvent event) {
			int newX = event.getX() - lastX;
			int newY = event.getY() - lastY;

			lastY += newY;
			lastX += newX;
			
			if (map.getMode() == MapMode.ZOOMING) {
				updateSelection(event.getX(), event.getY());
			}
		}
		
		private void updateSelection(int newX, int newY) {
			int x = Math.min(initialX, newX);
			int y = Math.min(initialY, newY);
			int width = Math.abs(newX - initialX);
			int height = Math.abs(newY - initialY);
			
			selection = new Selection(x, y, width, height);

			map.refresh();
		}

		@Override
		public void mousePressed(MouseEvent event) {
			handleMousePressed(event);
		}

		private void handleMousePressed(MouseEvent event) {
			initialX = event.getX();
			initialY = event.getY();
			lastX = initialX;
			lastY = initialY;
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (map.getMode() == MapMode.ZOOMING) {
				zoomToSelection();
			}
		}

		private void zoomToSelection() {
			int xOffset = selection.x - (getWidth() / 2);
			int yOffset = (getHeight() / 2) - selection.y;

			getMapProvider().setZoom(xOffset, yOffset, selection.width, selection.height, getWidth(), getHeight());
				
			selection.clear();
			map.refresh();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D graphics = (Graphics2D) g;
		selection.paintSelection(graphics);
	}

	private class Selection extends Rectangle {
		private static final long serialVersionUID = 1L;

		public Selection(int x, int y, int width, int height) {
			super(x, y, width, height);
		}
		
	    public void paintSelection(Graphics2D g) {
	    	final float dash1[] = {2.0f};
	    	final BasicStroke dashed = new BasicStroke(1.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dash1, 0.0f);
	    	g.setStroke(dashed);
	        g.setColor(ImageUtilities.applyTransparency(Color.DARK_GRAY, 0.5f));
	        g.fillRoundRect(x, y, width, height, 5, 5);
	    }
	    
	    public void clear() {
	    	x = 0;
	    	y = 0;
	    	width = 0;
	    	height = 0;
	    }
	}
}
