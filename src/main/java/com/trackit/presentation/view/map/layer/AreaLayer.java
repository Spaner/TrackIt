/*
 * This file is part of Track It!.
 * Copyright (C) 2015 J M Brisson Lopes, Pedro Gomes
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.business.common.Pair;
import com.trackit.business.database.Database;
import com.trackit.business.dbsearch.DBSearch;
import com.trackit.business.dbsearch.SearchControl;
import com.trackit.business.dbsearch.SearchObject;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.GeographicBoundingBox;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.TrackSegment;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.Waypoint;
import com.trackit.business.utilities.UniqueValidFilenamesList;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.search.SearchInterface;
import com.trackit.presentation.utilities.ImageUtilities;
import com.trackit.presentation.view.map.Map;
import com.trackit.presentation.view.map.MapView;
import com.trackit.presentation.view.map.Map.MapMode;

public class AreaLayer extends MapLayer implements EventListener, EventPublisher {
	private static final long serialVersionUID = -910047315840641173L;
	private Location firstPoint;
	private Location lastPoint;
	private Point squareStart;
	private Point squareEnd;
	private boolean drawing;
	private HashMap<String, List<String>> itemNames; 
	private List<String> documentFilepaths; 			//12335: 2016-08-11
	private List<JButton> buttons;
	private boolean ifButton = true; 

	public AreaLayer(Map map) {
		super(map);
		this.map = map;
		this.setLayout(null);
		init();
		squareStart = null;
		drawing = false;
//		itemNames = new HashSet<String>();
		itemNames = null;
		buttons = new ArrayList<JButton>();

	}

	public Location getFirstPoint() {
		return firstPoint;
	}

	public void setFirstPoint(Location firstPoint) {
		this.firstPoint = firstPoint;
	}

	public Location getLastPoint() {
		return lastPoint;
	}

	public void setLastPoint(Location lastPoint) {
		this.lastPoint = lastPoint;
	}

	private void init() {
		setOpaque(false);
		EventHandler handler = new EventHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
		addMouseWheelListener(handler);
	}

	public MapLayerType getType() {
		return MapLayerType.AREA_LAYER;
	}

	public void removeButtons() {
		for (JButton button : buttons) {
			remove(button);
		}
		buttons.clear();
	}

	public void paintComponent(Graphics g) {
		if (drawing) {
			if (squareStart != null && squareEnd != null) {
				g.setColor(new Color(143, 254, 204, 75));
				int x = Math.min(squareStart.x, squareEnd.x);
				int y = Math.min(squareStart.y, squareEnd.y);
				int width = Math.max(squareStart.x - squareEnd.x, squareEnd.x
						- squareStart.x);
				int height = Math.max(squareStart.y - squareEnd.y, squareEnd.y
						- squareStart.y);
				g.drawRect(x, y, width, height);
				g.fillRect(x, y, width, height);
			}
		}
	}
	
	private Waypoint toWaypoint(String name, ImageIcon icon, Point hotSpot,
			Double longitude, Double latitude, Double altitude, Date time) {

		Waypoint waypoint = new Waypoint(latitude, longitude, altitude, name,
				time);

		waypoint.setIcon(icon, hotSpot);

		final float scale = 1.2f;
		ImageIcon selectedIcon = getScaledIcon(icon, scale);
		int x = (int) (selectedIcon.getIconWidth() / 2.0f);
		int y = (int) (selectedIcon.getIconHeight());
		Point selectedIconHotSpot = new Point(x, y);
		waypoint.setSelectedIcon(selectedIcon, selectedIconHotSpot);

		return waypoint;
	}
	
	private Point comparePosition(Point position){
		for (JButton button : buttons){
			if(position.x == button.getLocation().x && position.y == button.getLocation().y){
				position.x+=5;
				position.y-=10;
			}
		}
		return position;
	}

	private JButton createActivityButton(MapLayer layer,
			final Activity activity, Waypoint activityWaypoint) {
		ImageIcon icon = getIcon(activityWaypoint);
		Point iconHotSpot = getIconHotSpot(activityWaypoint);
		Point position = comparePosition(getPosition(activityWaypoint, icon, iconHotSpot, layer));		
		final int imageWidth = icon.getImage().getWidth(null);
		final int imageHeight = icon.getImage().getHeight(null);

		JButton button = new JButton(icon);
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setBounds(position.x, position.y, imageWidth, imageHeight);
		button.setLocation(position);
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				itemButtonPressed(activity);
			}
		});

		return button;
	}

	private JButton createCourseButton(MapLayer layer, final Course course,
			Waypoint courseWaypoint) {

		ImageIcon icon = getIcon(courseWaypoint);
		Point iconHotSpot = getIconHotSpot(courseWaypoint);
		Point position = comparePosition(getPosition(courseWaypoint, icon, iconHotSpot, layer));		
		final int imageWidth = icon.getImage().getWidth(null);
		final int imageHeight = icon.getImage().getHeight(null);

		JButton button = new JButton(icon);
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setBounds(position.x, position.y, imageWidth, imageHeight);
		button.setLocation(position);
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				itemButtonPressed(course);
			}
		});

		return button;
	}

	private void itemButtonPressed(DocumentItem item) {
		MapView mv = TrackIt.getApplicationPanel().getMapView();
		mv.getMap().setMode(MapMode.SELECTION);
		mv.getAreaButton().setSelected(false);
		mv.getSelectButton().setSelected(true);
		item.publishSelectionEvent(null);
		mv.zoomToFitFeature(item);
	}

	private ImageIcon getCourseMarkerIcon(String courseName) {
		ImageIcon baseIcon = ImageUtilities
				.createImageIcon("pins/32/pin11.png");
		String s = courseName.substring(0, Math.min(courseName.length(), 7));
		s.concat("...");
		BufferedImage icon = ImageUtilities.toBufferedImage(baseIcon);
		BufferedImage textLayer = getTextLayer(String.valueOf(s));

		BufferedImage courseMarkerIcon = ImageUtilities.combineImages(icon,
				textLayer);

		return new ImageIcon(courseMarkerIcon);
	}

	private ImageIcon getActivityMarkerIcon(String activityName) {
		ImageIcon baseIcon = ImageUtilities
				.createImageIcon("pins/32/pin12.png");
		String s = activityName.substring(0, Math.min(activityName.length(), 7));
		s.concat("...");
		BufferedImage icon = ImageUtilities.toBufferedImage(baseIcon);
		BufferedImage textLayer = getTextLayer(String.valueOf(s));

		BufferedImage activityMarkerIcon = ImageUtilities.combineImages(icon,
				textLayer);

		return new ImageIcon(activityMarkerIcon);
	}

	private ImageIcon getIcon(Waypoint waypoint) {
		return waypoint.getIcon();
	}

	private Point getIconHotSpot(Waypoint waypoint) {
		return waypoint.getIconHotSpot();
	}

	private Point getPosition(Waypoint waypoint, ImageIcon icon,
			Point iconHotSpot, MapLayer layer) {
		Location location = new Location(waypoint.getLongitude(),
				waypoint.getLatitude());
		Pair<Integer, Integer> centerOffset = layer.getMapProvider()
				.getCenterOffsetInPixels(location);

		final int screenCenterX = layer.getWidth() / 2;
		final int screenCenterY = layer.getHeight() / 2;

		int x = screenCenterX + centerOffset.getFirst() - iconHotSpot.x;
		int y = screenCenterY + centerOffset.getSecond() - iconHotSpot.y;

		return new Point(x, y);
	}

	private ImageIcon getScaledIcon(ImageIcon icon, float factor) {
		return new ImageIcon(getScaledImage(icon.getImage(), factor));
	}

	private BufferedImage getScaledImage(Image image, float factor) {
		return ImageUtilities.resize(image,
				(int) (image.getWidth(null) * factor),
				(int) (image.getHeight(null) * factor));
	}

	private BufferedImage getTextLayer(String message) {
		final int canvasWidth = 32;
		final int canvasHeight = 32;
		final int imageWidth = 10;
		final int imageHeight = 10;

		BufferedImage textLayer = new BufferedImage(canvasWidth, canvasHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = textLayer.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Font font = new Font(Font.SANS_SERIF, Font.BOLD, 9);
		graphics.setFont(font);
		graphics.setColor(Color.WHITE);

		int textWidth = graphics.getFontMetrics().stringWidth(message);
		int textHeight = graphics.getFontMetrics().getHeight();

		float x = (imageWidth / 2.0f) - (textWidth / 2.0f) + 11;
		float y = canvasHeight - 7
				- ((imageHeight / 2.0f) + (textHeight / 2.0f));
		graphics.drawString(message, x, y);

		graphics.dispose();

		return textLayer;
	}

	public void placeButtons() {		
		if (getComponents().length < itemNames.size()) {
			validate();
			updateUI();
			List<Activity> activities2Paint = new ArrayList<Activity>();
			List<Course> courses2Paint = new ArrayList<Course>();
//			for( String s: itemNames.keySet()) {
//				System.out.println("\nDOC " + s + "  " + itemNames.get(s));
//				for( String n: itemNames.get(s))
//					System.out.println("\t"+ n);
//			}
//			for( GPSDocument document: DocumentManager.getInstance().getFolder(DocumentManager.FOLDER_LIBRARY).getDocuments()) {
			for( GPSDocument document: DocumentManager.getInstance().getLibraryFolder().getDocuments()) {
				List<String> trackNames = itemNames.get(document.getFileName());
				System.out.println("Processing document " + document.getFileName());
				if ( trackNames != null ) {
					System.out.println("Processing document " + document.getFileName());
					for( Activity activity: document.getActivities() )
						if ( trackNames.contains(activity.getName()))
							activities2Paint.add(activity);
					for( Course course: document.getCourses() )
						if ( trackNames.contains(course.getName()) )
							courses2Paint.add(course);
				}
			}
//			for (GPSDocument d : DocumentManager.getInstance().getDocuments()) {
//				for (Activity a : d.getActivities()) {
//					if (itemNames.contains(a.getName())) {
//						activities2Paint.add(a);
//					}
//				}
//				for (Course c : d.getCourses()) {
//					if (itemNames.contains(c.getName())) {
//						courses2Paint.add(c);
//					}
//				}
//			}
			String startName = Messages.getMessage("activities.label");
			ImageIcon startIcon = null;
			for (final Activity activity : activities2Paint) {
				System.out.println("Ac " + activity.getName());
				startIcon = getActivityMarkerIcon(activity.getName());
				Trackpoint activityStart = activity.getFirstTrackpoint();
				Point hotSpot = new Point(16, 31);
				Waypoint activityWaypoint = toWaypoint(startName, startIcon,
						hotSpot, activityStart.getLongitude(),
						activityStart.getLatitude(),
						activityStart.getAltitude(),
						activityStart.getTimestamp());
				buttons.add(createActivityButton(this, activity,
						activityWaypoint));
			}
			startName = Messages.getMessage("course.label");
			for (final Course course : courses2Paint) {
				System.out.println("Co " + course.getName());
				startIcon = getCourseMarkerIcon(course.getName());
				Trackpoint courseStart = course.getFirstTrackpoint();
				Point hotSpot = new Point(16, 31);
				Waypoint courseWaypoint = toWaypoint(startName, startIcon,
						hotSpot, courseStart.getLongitude(),
						courseStart.getLatitude(), courseStart.getAltitude(),
						courseStart.getTimestamp());
				buttons.add(createCourseButton(this, course, courseWaypoint));
			}
			for (JButton button : buttons) {
				add(button);
			}
		}
	}

	private class EventHandler implements MouseListener, MouseMotionListener,
			MouseWheelListener {

		@Override
		public void mouseWheelMoved(MouseWheelEvent event) {			
			redispatchMouseWheelEvent(event);
			removeButtons();
			placeButtons();
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			if (squareStart != null) {
				squareEnd = event.getPoint();
				// System.out.println(squareEnd.x + ":" + squareEnd.y);
				validate();
				repaint();
			}
		}

		@Override
		public void mouseMoved(MouseEvent event) {
			

		}

		@Override
		public void mouseClicked(MouseEvent event) {
			
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if ( itemNames != null )
				itemNames.clear();
			removeButtons();
			if (SwingUtilities.isLeftMouseButton(event)) {
				drawing = true;
				setFirstPoint(getMapProvider().getLocation(event.getX(),
						event.getY(), getWidth(), getHeight()));
				squareStart = event.getPoint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (SwingUtilities.isLeftMouseButton(event)) {
				setLastPoint(getMapProvider().getLocation(event.getX(),
						event.getY(), getWidth(), getHeight()));
				double minLongitude = Math.min(getFirstPoint().getLongitude(),
						getLastPoint().getLongitude());
				double maxLongitude = Math.max(getFirstPoint().getLongitude(),
						getLastPoint().getLongitude());
				double minLatitude = Math.min(getFirstPoint().getLatitude(),
						getLastPoint().getLatitude());
				double maxLatitude = Math.max(getFirstPoint().getLatitude(),
						getLastPoint().getLatitude());
				squareStart = null;
				squareEnd = null;
				drawing = false;
				/*BoundingBox2 coordinates = new BoundingBox2(minLongitude, maxLongitude, minLatitude, maxLatitude);*/
//				BoundingBox coordinates = new BoundingBox(maxLatitude, minLatitude, maxLongitude, minLongitude);
				GeographicBoundingBox coordinates = new GeographicBoundingBox( minLatitude, minLongitude, maxLatitude, maxLongitude);
				EventManager.getInstance().publish(AreaLayer.this, Event.SEND_COORDINATES, coordinates);
			}
		}

		@Override
		public void mouseEntered(MouseEvent event) {
			

		}

		@Override
		public void mouseExited(MouseEvent event) {
			

		}

		private void redispatchMouseWheelEvent(MouseWheelEvent event) {
			EventsLayer eventsLayer = (EventsLayer) map
					.getLayer(MapLayerType.EVENTS_LAYER);
			eventsLayer.dispatchEvent(new MouseWheelEvent(eventsLayer, event
					.getID(), event.getWhen(), event.getModifiers(), event
					.getX(), event.getY(), event.getClickCount(), event
					.isPopupTrigger(), event.getScrollType(), event
					.getScrollAmount(), event.getWheelRotation()));
		}

	}

	@Override
	public void process(Event event, DocumentItem item) {
		if(event.equals(Event.BY_BUTTON)){
			ifButton = true;
		}
		else{
			ifButton = false;
		}
	}

	@Override
	public void process(Event event, DocumentItem parent, List<? extends DocumentItem> items) {
		// TODO Auto-generated method stub
		
	}

	

	

}
