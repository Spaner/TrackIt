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
package com.trackit.presentation.view.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.BoundingBox2;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.business.common.NoneSelectedButtonGroup;
import com.trackit.business.dbsearch.SearchControl;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.GeographicBoundingBox;
import com.trackit.business.domain.Trackpoint;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.search.SearchInterface;
import com.trackit.presentation.utilities.ImageUtilities;
import com.trackit.presentation.view.map.Map.MapMode;
import com.trackit.presentation.view.map.layer.AreaLayer;
import com.trackit.presentation.view.map.layer.HighlightLayer;
import com.trackit.presentation.view.map.layer.MapLayerType;
import com.trackit.presentation.view.map.layer.PhotoLayer;
import com.trackit.presentation.view.map.provider.MapProvider;
import com.trackit.presentation.view.map.provider.MapProviderFactory;
import com.trackit.presentation.view.map.provider.MapProviderType;

public class MapView extends JPanel implements EventListener, EventPublisher {
	private static final long serialVersionUID = -3411760665588028587L;
	private static final int DEFAULT_PAN_AMOUNT = 10;
	
	private JPanel backgroundPanel;
	private Map map;
	private JComboBox<String> cbMapProviderSelector;
	private JComboBox<String> cbMapTypeSelector;
	
	private MapSelector mapSelector;
	
	private List<DocumentItem> items;
	
	public void setItems(List<DocumentItem> items) {
		this.items = items;
	}

	private boolean showGridlines;
	
	public MapView() {
		setLayout(new BorderLayout());
		initComponents();
	}
	
	private JToggleButton areaButton;
	private JToggleButton selectButton;
	
	/* Event Listener interface implementation */
	
	@Override
	public void process(com.trackit.presentation.event.Event event, DocumentItem item) {
		GPSDocument document;
		if(!map.getMapMode().equals(MapMode.MULTI) && !map.getMode().equals(MapMode.LEGEND)){
		switch (event) {
		case DOCUMENT_ADDED:
			document = (GPSDocument) item;
			items.clear();
			items.add(document);
			break;
		case DOCUMENT_SELECTED:
		case DOCUMENT_UPDATED:
			document = (GPSDocument) item;
			items.clear();
			items.add(document);
			break;
		case DOCUMENT_DISCARDED:
			break;
		case ACTIVITY_SELECTED:
		case COURSE_SELECTED:
		case COURSE_UPDATED:
			items.clear();
			items.add(item);
			break;
		case LAP_SELECTED:
		case SEGMENT_SELECTED:
		case COURSE_POINT_SELECTED:
			items.clear();
			items.add(item);
			items.add(item.getParent());
			break;
		case EVENT_SELECTED:
		case DEVICE_SELECTED:
			items.clear();
			items.add(item.getParent());
			items.addAll(item.getTrackpoints());
			break;
		case TRACKPOINT_SELECTED:
			items.clear();
			items.add(item);
			break;
		case WAYPOINT_SELECTED:
			items.clear();
			items.add(item);
			break;
		case PICTURES_SELECTED:
		case PICTURE_SELECTED:
			items.clear();
			items.add(item.getParent());
			break;
		case TRACKPOINT_HIGHLIGHTED:
			HighlightLayer highlightLayer = (HighlightLayer) map.getLayer(MapLayerType.HIGHLIGHT_LAYER);
			if (highlightLayer != null) {
				highlightLayer.setHighlight((Trackpoint) item);
			}
			break;
		case ANIMATION_MOVE:
			break;
		case ZOOM_TO_ITEM:
			zoomToFitFeatures(Arrays.asList(new DocumentItem[] { item }));
			break;
		case NOTHING_SELECTED:
			items.clear();
			break;
		case FOLDER_SELECTED:
			items.clear();
			break;
		case GET_COORDINATES:														// 71052: 2016-05-15
			//EventManager.getInstance().publish(MapView.this, Event.NO_BUTTON, null);
			map.setMode(MapMode.AREA);
			break;
		case SEND_COORDINATES:														// 71052: 2016-05-15
			map.setMode(MapMode.SIMPLE);
			SearchControl control = new SearchControl().getInstance();
//			control.newSearchCoordinates((BoundingBox) item);						// 12335: 2018-03-20
			control.newSearchCoordinates((GeographicBoundingBox) item);
			break;
		default:
			// Do nothing
		}
		} else {
			switch (event) {
			case TRACKPOINT_SELECTED:
				List<DocumentItem> copy = new ArrayList<DocumentItem>(items);
				Collections.copy(copy, items);
				for (DocumentItem t : copy){
					if(t instanceof Trackpoint){
						items.remove(t);
					}
				}
				items.add(0, item);
				break;
			default:
				// Do nothing			
			}
		}
		updateDisplay();
	}
	
	@Override
	public void process(com.trackit.presentation.event.Event event, DocumentItem parent, List<? extends DocumentItem> items) {
		this.items.clear();
		
		switch (event) {
		case DOCUMENTS_SELECTED:
		case ACTIVITIES_SELECTED:
		case COURSES_SELECTED:
		case SESSIONS_SELECTED:
		case LAPS_SELECTED:
		case EVENTS_SELECTED:
		case DEVICES_SELECTED:
		case SEGMENTS_SELECTED:
		case COURSE_POINTS_SELECTED:
		case MISCELANEOUS_SELECTION:
			this.items.addAll(items);
			break;
		case WAYPOINTS_SELECTED:
			this.items.add(parent);
			break;
		case TRACKPOINTS_REMOVED:
			break;
		default:
			// Do nothing
		}
		
		updateDisplay();
	}
	
	public void updateDisplay() {
		map.setItems(items);
		map.validate();//58406
		map.repaint();
	}
	
	public JToggleButton getAreaButton(){
		return areaButton;
	}
	
	public JToggleButton getSelectButton(){
		return selectButton;
	}
	
	public void zoomToFitFeature(DocumentItem item) {
		BoundingBox2<Location> boundingBox = item.getBounds();
		map.getMapProvider().setZoom(boundingBox, map.getWidth(), map.getHeight());
		updateDisplay();
	}
	
	public void zoomToFitFeatures(List<DocumentItem> items) {
		if (items.isEmpty()) {
			return;
		}
		
		BoundingBox2<Location> boundingBox = getBounds(items);
		map.getMapProvider().setZoom(boundingBox, map.getWidth(), map.getHeight());
		updateDisplay();
	}
	
	private BoundingBox2<Location> getBounds(List<DocumentItem> items) {
		double minLongitude = 180.0;
		double minLatitude = 90.0;
		double maxLongitude = -180.0;
		double maxLatitude = -90.0;
		
		for (DocumentItem item : items) {
			for (Trackpoint trackpoint : item.getTrackpoints()) {
				if (trackpoint.getLongitude() == null || trackpoint.getLatitude() == null) {
					continue;
				}
				
				minLongitude = Math.min(minLongitude, trackpoint.getLongitude());
				minLatitude = Math.min(minLatitude, trackpoint.getLatitude());
				maxLongitude = Math.max(maxLongitude, trackpoint.getLongitude());
				maxLatitude = Math.max(maxLatitude, trackpoint.getLatitude());
			}
		}
		
		Location topLeft = new Location(minLongitude, maxLatitude);
		Location topRight = new Location(maxLongitude, maxLatitude);
		Location bottomRight = new Location(maxLongitude, minLatitude);
		Location bottomLeft = new Location(minLongitude, minLatitude);
		
		return new BoundingBox2<Location>(topLeft, topRight, bottomRight, bottomLeft);
	}
	
	public void moveToShowFeature(DocumentItem item) {
		List<Trackpoint> trackpoints = item.getTrackpoints();
		BoundingBox2<Location> boundingBox = getBoundingBox(trackpoints);

		double centerLongitude = boundingBox.getTopLeft().getLongitude() + (boundingBox.getTopLeft().getLongitude() - boundingBox.getTopRight().getLongitude()) / 2.0;
		double centerLatitude = boundingBox.getBottomLeft().getLatitude() + (boundingBox.getTopLeft().getLatitude() - boundingBox.getBottomLeft().getLatitude()) / 2.0;
		Location centerLocation = new Location(centerLongitude, centerLatitude);
		map.getMapProvider().moveCenterLocation(centerLocation, getWidth(), getHeight());
		
		updateDisplay();
	}
	
	private BoundingBox2<Location> getBoundingBox(List<Trackpoint> trackpoints) {
		double minLongitude = 181.0;
		double maxLongitude = -181.0;
		double minLatitude = 91.0;
		double maxLatitude = -91.0;
		
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getLongitude() != null && trackpoint.getLatitude() != null) {
				minLongitude = Math.min(minLongitude, trackpoint.getLongitude());
				maxLongitude = Math.max(maxLongitude, trackpoint.getLongitude());
				minLatitude = Math.min(minLatitude, trackpoint.getLatitude());
				maxLatitude = Math.max(maxLatitude, trackpoint.getLatitude());
			}
		}
		Location topLeft = new Location(minLongitude, maxLatitude);
		Location topRight = new Location(maxLongitude, maxLatitude);
		Location bottomRight = new Location(maxLongitude, minLatitude);
		Location bottomLeft = new Location(minLongitude, minLatitude);
		
		return new BoundingBox2<Location>(topLeft, topRight, bottomRight, bottomLeft);
	}
	
	List<GPSDocument> getGPSDocuments() {
		return DocumentManager.getInstance().getDocuments();
	}
	
	boolean isShowGridlines() {
		return showGridlines;
	}
	
	private void initComponents() {
//		MapsCatalog mapsCatalog = MapsCatalog.getInstance();
//		MapProviderData data = MapProviderData.getInstance();
		String defaultMapType = TrackIt.getPreferences().getPreference(Constants.PrefsCategories.MAPS, null,
				Constants.MapPreferences.DEFAULT_MAP_TYPE, "Satellite");//		MapType mapType = MapType.lookup(defaultMapType);
		MapType mapType = MapType.lookupByDescription( defaultMapType);
		
		String defaultProvider = TrackIt.getPreferences().getPreference(Constants.PrefsCategories.MAPS, null,
				Constants.MapPreferences.DEFAULT_PROVIDER, MapProviderType.GOOGLE_MAPS.getDescription());
		MapProviderType mapProvider = MapProviderType.lookup(defaultProvider);
		mapProvider = (mapProvider != null ? mapProvider : MapProviderType.GOOGLE_MAPS);
		
		System.out.println( "At startup " + defaultProvider + "  " + defaultMapType);

        mapSelector = new MapSelector( defaultProvider, defaultMapType);
		
		backgroundPanel = new JPanel();
		backgroundPanel.setLayout(new GridLayout(1, 0));
		backgroundPanel.setPreferredSize(new Dimension(600, 600));
		
		items = Collections.synchronizedList(new ArrayList<DocumentItem>());
		
		Location centerLocation = new Location(-9.20276875, 38.71617165);
		MapProvider mapLayerMapProvider = MapProviderFactory.getInstance().getMapProvider(mapProvider, mapType, centerLocation);
		
		map = new Map(mapLayerMapProvider);
		map.addLayer(MapLayerType.BACKGROUND_MAP_LAYER);
		map.addLayer(MapLayerType.ROOT_ITEMS_LAYER);
		map.addLayer(MapLayerType.SELECTION_LAYER);
		backgroundPanel.add(map);
		
		JToolBar mapToolbar = createJToolBar();
		JToolBar featuresToolbar = createFeaturesToolbar();
		
        add(mapToolbar, BorderLayout.PAGE_START);
        add(backgroundPanel, BorderLayout.CENTER);
		add(featuresToolbar, BorderLayout.LINE_START);
	}
	
	private JToolBar createJToolBar() {
		JToolBar mapToolbar = new JToolBar();
		mapToolbar.setFloatable(false);
		mapToolbar.setRollover(true);
		
		URL imageURL;
		JButton button;
		JToggleButton toggleButton;
		ImageIcon icon;
		
		NoneSelectedButtonGroup modeButtonGroup = new NoneSelectedButtonGroup();
		
		imageURL = MapView.class.getResource("/icons/selection_mode_16.png");
        selectButton = new JToggleButton();
        selectButton.setActionCommand("Selection Mode");
        selectButton.setToolTipText( Messages.getMessage( "mapView.tooltip.selectionMode")); //12335: 2016-08-08
        selectButton.setFocusable(false);
        selectButton.setSelected(true);
        selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Selection Mode")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						map.setMode(MapMode.SELECTION);
					}
				}
			}
		});
        icon = new ImageIcon(imageURL, "Selection Mode");
        selectButton.setIcon(icon);
        selectButton.setMinimumSize(new Dimension(24, 24));
        selectButton.setMaximumSize(new Dimension(24, 24));
        mapToolbar.add(selectButton);
        modeButtonGroup.add(selectButton);
		
		imageURL = MapView.class.getResource("/icons/pencil.png");
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("Edit Mode");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.editMode"));	//12335: 2016-08-08
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Edit Mode")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						map.setMode(MapMode.EDITION);
					}
				}
			}
		});
        icon = new ImageIcon(imageURL, "Edition Mode");
        toggleButton.setIcon(icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        mapToolbar.add(toggleButton);
        modeButtonGroup.add(toggleButton);
		
        imageURL = MapView.class.getResource("/icons/animation_mode_16.png");
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("Animation Mode");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.animationMode"));	//12335: 2016-08-08
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (e.getActionCommand().equals("Animation Mode")) {
        			if (((JToggleButton) e.getSource()).isSelected()) {
        				map.setMode(MapMode.ANIMATION);
        			}
        		}
        	}
        });
        icon = new ImageIcon(imageURL, "Animation Mode");
        toggleButton.setIcon(icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        mapToolbar.add(toggleButton);
        modeButtonGroup.add(toggleButton);
		
		//58406
        imageURL = MapView.class.getResource("/icons/global-search-icon.png");
        areaButton = new JToggleButton();
        areaButton.setActionCommand("Select Area Mode");
        areaButton.setToolTipText( Messages.getMessage( "mapView.tooltip.selectAreaMode"));	//12335: 2016-08-08
        areaButton.setFocusable(false);
        areaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Select Area Mode")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
							EventManager.getInstance().publish(MapView.this, Event.BY_BUTTON, null);  //71052: 2017-05-15
							map.setMode(MapMode.AREA);
					}
				}
			}
		});
        icon = new ImageIcon(imageURL, "Select Area Mode");
        areaButton.setIcon(icon);
        areaButton.setMinimumSize(new Dimension(24, 24));
        areaButton.setMaximumSize(new Dimension(24, 24));
        mapToolbar.add(areaButton);
        modeButtonGroup.add(areaButton);
        
        //NEED TO CHANGE ICON BELOW
        imageURL = MapView.class.getResource("/icons/comparison_icon.png");
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("View Multiple Tracks");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.simultaneousTracksDisplay"));	//12335: 2016-08-08
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("View Multiple Tracks")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						map.setMode(MapMode.MULTI);
					}
				}
			}
		});
        icon = new ImageIcon(imageURL, "View Multiple Tracks");
        toggleButton.setIcon(icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        mapToolbar.add(toggleButton);
        modeButtonGroup.add(toggleButton);
        //
        

        imageURL = MapView.class.getResource("/icons/mapLegend.png");
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("Map Legend");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.showMapLegend")); //12335: 2016-08-08
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Map Legend")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						map.setMode(MapMode.LEGEND);
					}
				}
			}
		});
        icon = new ImageIcon(imageURL, "Map Legend");
        toggleButton.setIcon(icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        mapToolbar.add(toggleButton);
        modeButtonGroup.add(toggleButton);
        
        
        mapToolbar.addSeparator();
        
        imageURL = MapView.class.getResource("/icons/smooth_16.png");
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("Simplification");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.simplification"));	//12335: 2016-08-08
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Simplification")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						map.setSimplifyTracks(true);
					} else {
						map.setSimplifyTracks(false);
					}
					updateDisplay();
				}
			}
		});
        icon = new ImageIcon(imageURL, "Simplification");
        toggleButton.setIcon(icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        mapToolbar.add(toggleButton);
        
        mapToolbar.addSeparator();
        
		imageURL = MapView.class.getResource("/icons/zoom_in.png");
        button = new JButton();
        button.setActionCommand("ZoomIn");
        button.setToolTipText("Zoom In");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.zoomIn();
			}
		});
        button.setIcon(new ImageIcon(imageURL, "Zoom In"));
        mapToolbar.add(button);
        
        imageURL = MapView.class.getResource("/icons/zoom_reset.png");
        button = new JButton();
        button.setActionCommand("Zoom to Item");
        button.setToolTipText( Messages.getMessage( "mapView.tooltip.zoomToItem"));	//12335: 2016-08-08 
        button.setFocusable( false);
        button.addActionListener(  new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocumentItem item = DocumentManager.getInstance().getSelectedDocument();
				if ( item != null )
					zoomToFitFeature( item);
			}
		});
        button.setIcon(new ImageIcon(imageURL, "Zoom to Item"));
        mapToolbar.add(button);
        
        imageURL = MapView.class.getResource("/icons/zoom_out.png");
        button = new JButton();
        button.setActionCommand("ZoomOut");
        button.setToolTipText( Messages.getMessage( "mapView.tooltip.zoomOut"));	//12335: 2016-08-08
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.zoomOut();
			}
		});
        button.setIcon(new ImageIcon(imageURL, "Zoom Out"));
        mapToolbar.add(button);
        
        imageURL = MapView.class.getResource("/icons/custom_zoom.png");
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("CustomZoom");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.customZoom"));	//12335: 2016-08-10
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("CustomZoom")) {
					if (((JToggleButton) e.getSource()).isSelected()) {
						map.setMode(MapMode.ZOOMING);
					} else {
						map.setMode(MapMode.SELECTION);
					}
				}
			}
		});
        icon = new ImageIcon(imageURL, "Custom Zoom");
        toggleButton.setIcon(icon);
        toggleButton.setMaximumSize(new Dimension(24, 24));
        mapToolbar.add(toggleButton);
        
        mapToolbar.addSeparator();
        
        imageURL = MapView.class.getResource("/icons/arrow_left.png");
        button = new JButton();
        button.setActionCommand("PanLeft");
        button.setToolTipText( Messages.getMessage( "mapView.tooltip.panLeft"));	//12335: 2016-08-10
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.pan(PanDirection.RIGHT, DEFAULT_PAN_AMOUNT);
				areaLayerStuff();//58406
				photoLayerStuff();//58406
			}
		});
        button.addMouseListener(new ToolbarButtonPressRepeat());
        button.setIcon(new ImageIcon(imageURL, "Pan Left"));
        button.setMaximumSize(new Dimension(22, 22));
        mapToolbar.add(button);
        
        imageURL = MapView.class.getResource("/icons/arrow_right.png");
        button = new JButton();
        button.setActionCommand("PanRight");
        button.setToolTipText( Messages.getMessage( "mapView.tooltip.panRight"));	//12335: 2016-08-10
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.pan(PanDirection.LEFT, DEFAULT_PAN_AMOUNT);
				areaLayerStuff();//58406
				photoLayerStuff();//58406
			}
		});
        button.addMouseListener(new ToolbarButtonPressRepeat());
        button.setIcon(new ImageIcon(imageURL, "Pan Right"));
        button.setMaximumSize(new Dimension(22, 22));
        mapToolbar.add(button);
        
        imageURL = MapView.class.getResource("/icons/arrow_up.png");
        button = new JButton();
        button.setActionCommand("PanUp");
        button.setToolTipText( Messages.getMessage( "mapView.tooltip.panUp"));	//12335: 2016-08-10
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.pan(PanDirection.UP, DEFAULT_PAN_AMOUNT);
				areaLayerStuff();//58406
				photoLayerStuff();//58406
			}
		});
        button.addMouseListener(new ToolbarButtonPressRepeat());
        button.setIcon(new ImageIcon(imageURL, "Pan Up"));
        button.setMaximumSize(new Dimension(22, 22));
        mapToolbar.add(button);
        
        imageURL = MapView.class.getResource("/icons/arrow_down.png");
        button = new JButton();
        button.setActionCommand("PanDown");
        button.setToolTipText( Messages.getMessage( "mapView.tooltip.panDown"));	//12335: 2016-08-10
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.pan(PanDirection.DOWN, DEFAULT_PAN_AMOUNT);
				areaLayerStuff();//58406
				photoLayerStuff();//58406
			}
		});
        button.addMouseListener(new ToolbarButtonPressRepeat());
        button.setIcon(new ImageIcon(imageURL, "Pan Down"));
        button.setMaximumSize(new Dimension(22, 22));
        mapToolbar.add(button);
        
        mapToolbar.addSeparator();
        
//        mapToolbar.add(cbMapProvider);
//        mapToolbar.add(cbMapType);
        
        cbMapProviderSelector = mapSelector.mapProviderComboBox();
        cbMapTypeSelector     = mapSelector.mapTypeComboBox();
        mapToolbar.add( cbMapProviderSelector);
        mapToolbar.add( cbMapTypeSelector);
        cbMapProviderSelector.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MapProviderType selectedMapProvider = mapSelector.handleMapProviderChangeEvent();
				if ( selectedMapProvider != null ) {
					System.out.println( selectedMapProvider.name());
					MapProvider oldMapProvider = map.getMapProvider();
					MapProvider newMapProvider =  MapProviderFactory.getInstance().getMapProvider( selectedMapProvider, oldMapProvider.getMapType(), oldMapProvider.getCenterLocation());
					map.setMapProvider(newMapProvider);
					updateDisplay();
				}
			}
		});
        
        cbMapTypeSelector.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				MapType mapType = mapSelector.handleMapTypeChangeEvent();
				if ( mapType != null ) {
					map.setMapType( mapType);
					updateDisplay();
				}
			}
		});

        return mapToolbar;
	}
	
	//2018-05-16: 12335
	public void setLocale( Locale locale) {
		mapSelector.setLocale( locale);
	}
	
	//58406########################################################################
	private void areaLayerStuff(){
		
		AreaLayer aLayer = (AreaLayer) map.getLayer(MapLayerType.AREA_LAYER);
		if(aLayer!=null){
			aLayer.removeButtons();
			aLayer.placeButtons();
		}
	}
	
	private void photoLayerStuff(){
		PhotoLayer aLayer = (PhotoLayer) map.getLayer(MapLayerType.PHOTO_LAYER);
		if(aLayer!=null){
			aLayer.removeButtons();
			aLayer.placeButtons();
		}
	}
	//#############################################################################
	
	private class ToolbarButtonPressRepeat extends MouseAdapter {
		private final int MAX_PAN_AMOUNT = 30;
		private Timer timer;
		private int panAmount;

		@Override
		public void mousePressed(final MouseEvent event) {
			super.mousePressed(event);
			if (timer == null) {
				timer = new Timer();
			}
			
			panAmount = 1;
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					String actionCommand = ((JButton) event.getSource()).getActionCommand();
					
					panAmount++;
					panAmount = Math.min(panAmount, MAX_PAN_AMOUNT);
					
					if (actionCommand.equals("PanLeft")) {
						map.pan(PanDirection.RIGHT, panAmount);
					} else if (actionCommand.equals("PanRight")) {
						map.pan(PanDirection.LEFT, panAmount);
					} else if (actionCommand.equals("PanUp")) {
						map.pan(PanDirection.UP, panAmount);
					} else if (actionCommand.equals("PanDown")) {
						map.pan(PanDirection.DOWN, panAmount);
					}
				}
			}, 350, 150);
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			super.mouseReleased(event);
			
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		}
	}
	
	private JToolBar createFeaturesToolbar() {
		JToolBar featuresToolbar = new JToolBar();
		featuresToolbar.setFloatable(false);
		featuresToolbar.setRollover(true);
		featuresToolbar.setOrientation(JToolBar.VERTICAL);
		
		URL imageURL;
		JToggleButton toggleButton;
		
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("ShowCoursePoints");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.showCoursePoints"));	//12335: 2016-08-10
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean selected = ((JToggleButton) e.getSource()).isSelected();
				showCoursePoints(selected);
			}
		});
        ImageIcon icon = ImageUtilities.createImageIcon("coursepoint_icon.png");;
        toggleButton.setIcon(icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        showCoursePoints(true);
        toggleButton.setSelected(true);
        featuresToolbar.add(toggleButton);
        
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("ShowLapMarkers");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.showLapMarkers"));	//12335: 2016-08-10
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean selected = ((JToggleButton) e.getSource()).isSelected();
				showLapMarkers(selected);
			}
		});
        icon = ImageUtilities.createImageIcon("lap_icon.png");
        toggleButton.setIcon(icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        showLapMarkers(true);
        toggleButton.setSelected(true);
        featuresToolbar.add(toggleButton);
        
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("ShowStartFinish");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.showStartFinish"));	//12335: 2016-08-10
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean selected = ((JToggleButton) e.getSource()).isSelected();
				showStartFinish(selected);
			}
		});
        icon = ImageUtilities.createImageIcon("start_icon.png");
        toggleButton.setIcon(icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        showStartFinish(true);
        toggleButton.setSelected(true);
        featuresToolbar.add(toggleButton);

        imageURL = MapView.class.getResource("/icons/pink_pin_24.png");
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand("ShowWaypoints");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.showWaypoints"));	//12335: 2016-08-10
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean selected = ((JToggleButton) e.getSource()).isSelected();
				showWaypoints(selected);
			}
		});
        icon = new ImageIcon(imageURL, "Show Waypoints");
        toggleButton.setIcon(icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        showWaypoints(true);
        toggleButton.setSelected(true);
        featuresToolbar.add(toggleButton);
        
        //12335: 2017-05-03
		boolean showGridlines = TrackIt.getPreferences().getBooleanPreference(
											Constants.PrefsCategories.GLOBAL, null, 
											Constants.GlobalPreferences.SHOW_GRIDLINES, false);
        imageURL = MapView.class.getResource( "/icons/grid_24.png");
        toggleButton = new JToggleButton();
        toggleButton.setActionCommand( "ShowGridlines");
        toggleButton.setToolTipText( Messages.getMessage( "mapView.tooltip.showGridlines"));
        toggleButton.setFocusable( false);
        toggleButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e) {
				boolean selected = ((JToggleButton) e.getSource()).isSelected();
				showGridlines( selected);
			}
		});
        icon = new ImageIcon( imageURL, "ShowGridlines");
        toggleButton.setIcon( icon);
        toggleButton.setMinimumSize(new Dimension(24, 24));
        toggleButton.setMaximumSize(new Dimension(24, 24));
        toggleButton.setSelected(showGridlines);
        featuresToolbar.add(toggleButton);
        showGridlines(showGridlines);
       
        return featuresToolbar;
	}
	
	private void showCoursePoints(boolean showCoursePoints) {
		if (showCoursePoints) {
			map.addLayer(MapLayerType.COURSE_POINTS_LAYER);
		} else {
			map.removeLayer(MapLayerType.COURSE_POINTS_LAYER);
		}
		
		updateDisplay();
	}
	
	private void showLapMarkers(boolean showLapMarkers) {
		if (showLapMarkers) {
			map.addLayer(MapLayerType.LAPS_LAYER);
		} else {
			map.removeLayer(MapLayerType.LAPS_LAYER);
		}
		
		updateDisplay();
	}
	
	private void showStartFinish(boolean showStartFinish) {
		if (showStartFinish) {
			map.addLayer(MapLayerType.START_FINISH_LAYER);
		} else {
			map.removeLayer(MapLayerType.START_FINISH_LAYER);
		}
		
		updateDisplay();
	}
	
	private void showWaypoints(boolean showWaypoints) {
		if (showWaypoints) {
			map.addLayer(MapLayerType.WAYPOINTS_LAYER);
		} else {
			map.removeLayer(MapLayerType.WAYPOINTS_LAYER);
		}
		
		updateDisplay();
	}
	
	//12335: 2017-05-03
	private void showGridlines( boolean showGridlines) {
		if ( showGridlines )
			map.addLayer( MapLayerType.GRID_LAYER);
		else
			map.removeLayer( MapLayerType.GRID_LAYER);
		
		TrackIt.getPreferences().setPreference( 
					Constants.PrefsCategories.GLOBAL, null, 
					Constants.GlobalPreferences.SHOW_GRIDLINES, showGridlines);
		updateDisplay();
	}
	
	private class MapTypeActionListener implements ActionListener {
		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent event) {
			JComboBox<String> cbMapType = (JComboBox<String>) event.getSource();
	        String mapTypeName = (String) cbMapType.getSelectedItem();
	        updateMapType(mapTypeName);
	        
	        TrackIt.getPreferences().setPreference(Constants.PrefsCategories.MAPS, null,
	        		Constants.MapPreferences.DEFAULT_MAP_TYPE, mapTypeName);
		}
	}
	
	private void updateMapType(String mapTypeName) {
//		MapType mapType = MapType.lookup(mapTypeName);
		MapType mapType = MapType.lookupByDescription(mapTypeName);
		if (mapType == null) {
			throw new IllegalStateException("The selected mapType does not exist!");
		}
		
		map.setMapType(mapType);
		updateDisplay();
	}
	
	private class MapProviderActionListener implements ActionListener {
		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent event) {
			JComboBox<String> cbMapType = (JComboBox<String>) event.getSource();
	        String selectedMapProvider = (String) cbMapType.getSelectedItem();
	        MapProviderType mapProviderType = MapProviderType.lookup(selectedMapProvider);
	        updateMapType(mapProviderType);
	        
	        TrackIt.getPreferences().setPreference(Constants.PrefsCategories.MAPS, null, Constants.MapPreferences.DEFAULT_PROVIDER, selectedMapProvider);
		}
	}
	
	private void updateMapType(MapProviderType mapProviderType) {
		if (mapProviderType == null) {
			throw new IllegalStateException("The selected mapTypeProvider does not exist!");
		}
		
		MapProvider oldMapProvider = map.getMapProvider();
		MapProvider newMapProvider =  MapProviderFactory.getInstance().getMapProvider(mapProviderType, oldMapProvider.getMapType(), oldMapProvider.getCenterLocation());
		map.setMapProvider(newMapProvider);
		
		updateDisplay();
	}
	
	@Override
	public String toString() {
		return Messages.getMessage("view.mapview.name");
	}
	
	
	//58406
	public Map getMap(){
		return this.map;
	}
}
