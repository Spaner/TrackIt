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
package com.henriquemalheiro.trackit.presentation.view.map;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.OverlayLayout;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.MaxZoomExceededException;
import com.henriquemalheiro.trackit.business.exception.MinZoomExceededException;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.view.map.layer.EventsLayer;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayer;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayerType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProvider;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProviderType;
import com.pg58406.trackit.presentation.view.map.layer.PhotoLayer;

public class Map extends JPanel {
	private static final long serialVersionUID = 3892478944440491076L;
	
	public static enum CursorType {DRAGGABLE, DRAGGING, TARGET}; 
	public static enum MapMode {SIMPLE, SELECTION, EDITION, ANIMATION, ZOOMING, AREA/*58406*/, MULTI/*58406*/};
	public static final Cursor draggableCursor = createCursor(CursorType.DRAGGABLE);
	public static final Cursor draggingCursor = createCursor(CursorType.DRAGGING);
	public static final Cursor targetCursor = createCursor(CursorType.TARGET);
	
	private java.util.Map<MapLayerType, MapLayer> layersMap;
	private boolean simplifyTracks;
	
	private MapProvider mapProvider;
	private MapMode mapMode;
	private MapInfoPanel mapInfoPanel;
	private AnimationSlider animationSlider;
	
	private JLayeredPane layers;
	private List<DocumentItem> items;
	
	private static Logger logger = Logger.getLogger(Map.class.getName());
	
	public Map(MapProvider mapProvider) {
		this.mapProvider = mapProvider;
		init();
	}
	
	private void init() {
		items = new ArrayList<DocumentItem>();

		layersMap = new HashMap<>();
		layers = new JLayeredPane();
		layers.setPreferredSize(new Dimension(600, 600));
		layers.setLayout(new OverlayLayout(layers));
		
		addLayer(MapLayerType.PHOTO_LAYER);//58406
		addLayer(MapLayerType.EVENTS_LAYER);
		addLayer(MapLayerType.CUSTOM_SELECTION_LAYER);
		

		mapInfoPanel = new MapInfoPanel(Color.BLACK, 0.4f);
		mapInfoPanel.setVisible(true);
		updateMapInfo();
		
		animationSlider = new AnimationSlider(this, 0, 0, 0);
		animationSlider.setVisible(false);
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		layout.setHonorsVisibility(true);
		setLayout(layout);
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(layers)
						.addComponent(mapInfoPanel)
						.addComponent(animationSlider));
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addComponent(layers)
						.addComponent(mapInfoPanel)
						.addComponent(animationSlider));

		setMode(MapMode.SELECTION);
		simplifyTracks = false;
		
		refresh();
	}
	
	public MapProvider getMapProvider() {
		return mapProvider;
	}
	
	public void setMapProvider(MapProvider newMapProvider) {
		MapProviderType currentMapProviderType = MapProviderType.lookup(mapProvider.getName());
		MapProviderType newMapProviderType = MapProviderType.lookup(newMapProvider.getName());
		
		if (currentMapProviderType == MapProviderType.MILITARY_MAPS || newMapProviderType == MapProviderType.MILITARY_MAPS) {
			mapProvider.flushCache();
		}
		
		double groundResolution = mapProvider.getGroundResolution(getWidth());
		byte zoom = mapProvider.getZoom();

		mapProvider = newMapProvider;
		
		if (currentMapProviderType == MapProviderType.MILITARY_MAPS || newMapProviderType == MapProviderType.MILITARY_MAPS) {
			mapProvider.setZoom(groundResolution, getWidth());
		} else {
			mapProvider.setZoom(zoom);
		}
		
		refresh();
	}
	
	private void photoLayerStuff(){
		PhotoLayer aLayer = (PhotoLayer) this.getLayer(MapLayerType.PHOTO_LAYER);
		if(aLayer!=null){
			aLayer.removeButtons();
			aLayer.placeButtons();
		}
	}
	
	public void refresh() {
		
		photoLayerStuff();
		validate();//58046
		repaint();
	}
	
	public MapType getMapType() {
		return mapProvider.getMapType();
	}
	
	public void setMapType(MapType mapType) {
		mapProvider.setMapType(mapType);
		refresh();
	}
	
	public MapMode getMapMode() {
		return mapMode;
	}
	
	public void showMapInfo(boolean showMapInfo) {
		mapInfoPanel.setVisible(showMapInfo);
		refresh();
	}
	
	public List<MapLayer> getLayers() {
		return new ArrayList<MapLayer>(layersMap.values());
	}
	
	public MapLayer getLayer(MapLayerType type) {
		return layersMap.get(type);
	}
	
	public void addLayer(MapLayerType type) {
		MapLayer layer = type.createLayer(this);
		int priority = type.getPriority();
		
		if (layer != null) {
			layers.add(layer, new Integer(priority));
			layersMap.put(type, layer);
		}
	}
	
	public void removeLayer(MapLayerType type) {
		MapLayer layer = layersMap.get(type);
		if (layer != null) {
			layer.finish();
			layers.remove(layer);
			layersMap.remove(type);
		}
	}
	
	public List<DocumentItem> getItems() {
		return items;
	}
	
	public void setItems(List<DocumentItem> items) {
		this.items.clear();
		this.items.addAll(items);
		
		if (!items.isEmpty() && (items.get(0).isActivity() || items.get(0).isCourse())) {
			updateBottomPanel();
		}
	}
	
	public MapMode getMode() {
		return mapMode;
	}
	
	public void setMode(MapMode mapMode) {
		this.mapMode = mapMode;
		
		removeLayer(MapLayerType.HIGHLIGHT_LAYER);
		removeLayer(MapLayerType.EDITION_LAYER);
		removeLayer(MapLayerType.AREA_LAYER);//58406
		removeLayer(MapLayerType.MULTI_LAYER);//58406

		switch (mapMode) {
		case SIMPLE:
			setCursor(targetCursor);
			break;
		case SELECTION:
			setCursor(targetCursor);
			addLayer(MapLayerType.HIGHLIGHT_LAYER);
			break;
		case ANIMATION:
			setCursor(draggableCursor);
			break;
		case ZOOMING:
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			break;
		case EDITION:
			setCursor(targetCursor);
			addLayer(MapLayerType.HIGHLIGHT_LAYER);
			addLayer(MapLayerType.EDITION_LAYER);
			break;
		// 58406#####################################################
		case AREA:
			setCursor(targetCursor);
			addLayer(MapLayerType.AREA_LAYER);
			break;
		case MULTI:
			setCursor(targetCursor);
			addLayer(MapLayerType.HIGHLIGHT_LAYER);
			addLayer(MapLayerType.MULTI_LAYER);
			break;
		// ##########################################################
		default:
			setCursor(Cursor.getDefaultCursor());
		}
		revalidate();//58406
		updateBottomPanel();
	}
	
	private void updateBottomPanel() {
		if (mapMode == MapMode.ANIMATION) {
			AnimationSlider newAnimationSlider = getAnimationSlider();
			newAnimationSlider.setVisible(true);
			((GroupLayout) getLayout()).replace(animationSlider, newAnimationSlider);
			this.animationSlider = newAnimationSlider;
		} else {
			animationSlider.setVisible(false);
		}
		
		invalidate();
	}
	
	AnimationSlider getAnimationSlider() {
		List<Trackpoint> trackpoints = getAnimationTrackpoints();
		double distance = (trackpoints.isEmpty() ? 0.0 : trackpoints.get(trackpoints.size() - 1).getDistance());
		
		int minValue = 0;
		int maxValue = (int) Math.ceil(distance);
		
		return new AnimationSlider(this, minValue, maxValue, minValue);
	}
	
	void selectTrackpointAtDistance(double distance) {
		List<Trackpoint> trackpoints = getAnimationTrackpoints();
		
		Trackpoint trackpoint = (trackpoints.isEmpty() ? null : trackpoints.get(0));
		double currentDistance = 0.0;
		int pos = 0;
		while (currentDistance < distance && pos < trackpoints.size()) {
			trackpoint = trackpoints.get(pos);
			currentDistance = trackpoint.getDistance();
			pos++;
		}
		
		if (trackpoint != null) {
			EventManager.getInstance().publish(null, Event.TRACKPOINT_SELECTED, trackpoint);
		}
	}
	
	private List<Trackpoint> getAnimationTrackpoints() {
		List<Trackpoint> trackpoints = new ArrayList<Trackpoint>();
		
		List<DocumentItem> items = getItems();
		if (items.size() == 1) {
			DocumentItem item = items.get(0);
			while (item.getParent() != null && !item.isActivity() && !item.isCourse()) {
				item = item.getParent();
			}

			if (item != null && (item.isActivity() || item.isCourse())) {
				trackpoints = item.getTrackpoints();
			}
		}
		
		return trackpoints;
	}
	
	public boolean isSimplifyTracks() {
		return simplifyTracks;
	}
	
	void setSimplifyTracks(boolean simplify) {
		simplifyTracks = simplify;
	}
	
	public void updateMapInfo() {
		int x = (int) MouseInfo.getPointerInfo().getLocation().getX();
		int y = (int) MouseInfo.getPointerInfo().getLocation().getY();
		updateMapInfo(x, y);
	}
	
	public void updateMapInfo(int x, int y) {
		mapInfoPanel.update(getMapInfo(x, y));
	}
	
	private MapInfo getMapInfo(int x, int y) {
		MapInfo mapInfo = new MapInfo();
		MapProvider mapProvider = getMapProvider();
		
		x = Math.max(Math.min(x, getWidth()), 0);
		y = Math.max(Math.min(y, getHeight()), 0);
		
		mapInfo.setPointerX(x);
		mapInfo.setPointerY(y);
		
		Location currentLocation = mapProvider.getLocation(x, y, getWidth(), getHeight());
		mapInfo.setPointerLongitude(currentLocation.getLongitude());
		mapInfo.setPointerLatitude(currentLocation.getLatitude());
		mapInfo.setZoom(mapProvider.getZoom());
		mapInfo.setCenterLongitude(mapProvider.getCenterLocation().getLongitude());
		mapInfo.setCenterLatitude(mapProvider.getCenterLocation().getLatitude());
		
		return mapInfo;
	}

	public void zoomIn() {
		try {
			getMapProvider().increaseZoom(getWidth(), getHeight());
		} catch (MaxZoomExceededException e) {
			return;
		}
		refresh();
	}

	public void zoomOut() {
		try {
			getMapProvider().decreaseZoom(getWidth(), getHeight());
		} catch (MinZoomExceededException e) {
			return;
		}
		refresh();
	}
	
	public void pan(PanDirection direction, int amountInPixels) {
		getMapProvider().pan(direction, amountInPixels, getWidth(), getHeight());
		refresh();
	}
	
	public void pan(int xOffset, int yOffset) {
		getMapProvider().moveCenterLocation(-xOffset, -yOffset, getWidth(), getHeight());
		refresh();
	}
	
	public void displayPopUpMenu(Location location, JPopupMenu menu) {
		Pair<Integer, Integer> offset = getMapProvider().getCenterOffsetInPixels(location);
		
		int x = getWidth() / 2 + offset.getFirst();
		int y = getHeight() / 2 + offset.getSecond();
		
		menu.show(this, x, y);
	}
	
	private static Cursor createCursor(CursorType cursorType) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		URL url = null;
		
		switch (cursorType) {
		case DRAGGABLE:
			url = EventsLayer.class.getResource("/cursors/cursor_hand.gif");
			break;
		case DRAGGING:
			url = EventsLayer.class.getResource("/cursors/cursor_drag_hand.gif");
			break;
		case TARGET:
			return new Cursor(Cursor.HAND_CURSOR);
		default:
			return Cursor.getDefaultCursor();
		}
		
		Image image = null;
		try {
			image = ImageIO.read(url.openStream());
		} catch (IOException e) {
			logger.error(e.getMessage());
			return Cursor.getDefaultCursor();
		}
		
		Cursor cursor = toolkit.createCustomCursor(image, new Point(8, 8), cursorType.toString());
		return cursor;
	}
	
	private class MapInfoPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private TranslucidBanner backgroundPanel;
		private InfoPanel infoPanel;
		
		public MapInfoPanel(Color backgroundColor, float transparency) {
			setOpaque(false);
			setPreferredSize(new Dimension(600, 20));
			setLayout(new GridLayout(1, 0));
			
			backgroundPanel = new TranslucidBanner(backgroundColor, transparency);
			backgroundPanel.setBounds(0, 0, 600, 20);
			
			infoPanel = new InfoPanel();
			infoPanel.setBounds(0, -1, 600, 20);

			add(infoPanel);
		}
		
		public void update(MapInfo mapInfo) {
			infoPanel.update(mapInfo);
		}
	}
	
	private class InfoPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		JLabel latitude;
		JLabel longitude;
		JLabel zoom;
		JLabel x;
		JLabel y;
		JLabel centerLatitude;
		JLabel centerLongitude;

		public InfoPanel() {
			setOpaque(false);
			setLayout(new FlowLayout(FlowLayout.LEFT));

			JLabel latitudeLabel = createLabel("Latitude: ");
			latitude = createLabel("---");
			JLabel longitudeLabel = createLabel("Longitude: ");
			longitude = createLabel("---");
			JLabel zoomLabel = createLabel("Zoom: ");
			zoom = createLabel("---");
			JLabel xLabel = createLabel("X: ");
			x = createLabel("---");
			JLabel yLabel = createLabel("Y: ");
			y = createLabel("---");
			JLabel centerLatitudeLabel = createLabel("Center Latitude: ");
			centerLatitude = createLabel("---");
			JLabel centerLongitudeLabel = createLabel("Center Longitude: ");
			centerLongitude = createLabel("---");
			
			add(latitudeLabel);
			add(latitude);
			add(longitudeLabel);
			add(longitude);
			add(zoomLabel);
			add(zoom);
			add(xLabel);
			add(x);
			add(yLabel);
			add(y);
			add(centerLatitudeLabel);
			add(centerLatitude);
			add(centerLongitudeLabel);
			add(centerLongitude);
		}
		
		private JLabel createLabel(String text) {
			JLabel label = new JLabel(text);
			label.setForeground(Color.DARK_GRAY);
			label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
			return label;
		}
		
		public void update(MapInfo mapInfo) {
			NumberFormat nb = new DecimalFormat("0.000000");
			this.longitude.setText(mapInfo.getPointerLongitude() != null ? nb.format(mapInfo.getPointerLongitude()) : "---");
			this.latitude.setText(mapInfo.getPointerLatitude() != null ? nb.format(mapInfo.getPointerLatitude()) : "---");
			this.zoom.setText(mapInfo.getZoom() != null ? mapInfo.getZoom().toString() : "---");
			this.y.setText(mapInfo.getPointerY() != null ? mapInfo.getPointerY().toString() : "---");
			this.x.setText(mapInfo.getPointerX() != null ? mapInfo.getPointerX().toString() : "---");
			this.y.setText(mapInfo.getPointerY() != null ? mapInfo.getPointerY().toString() : "---");
			this.centerLongitude.setText(mapInfo.getCenterLongitude() != null ? nb.format(mapInfo.getCenterLongitude()) : "---");
			this.centerLatitude.setText(mapInfo.getCenterLatitude() != null ? nb.format(mapInfo.getCenterLatitude()) : "---");
		}
	}
	
	private class TranslucidBanner extends JPanel {
		private static final long serialVersionUID = 1L;
		private Color backgroundColor;
		private float alpha;

		public TranslucidBanner(Color backgroundColor, float alpha) {
			this.backgroundColor = backgroundColor;
			this.alpha = alpha;
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D graphics = (Graphics2D) g;
			
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
			graphics.setColor(backgroundColor);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
	
	public void clearItems(){
		items.clear();
	}
}
