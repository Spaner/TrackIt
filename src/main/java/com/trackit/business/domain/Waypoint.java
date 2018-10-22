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
package com.trackit.business.domain;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.trackit.business.common.Formatters;
import com.trackit.business.common.Location;
import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.utilities.ImageUtilities;
import com.trackit.presentation.view.folder.FolderTreeItem;
import com.trackit.presentation.view.map.layer.MapLayer;
import com.trackit.presentation.view.map.painter.MapPainter;
import com.trackit.presentation.view.map.painter.MapPainterFactory;

public class Waypoint extends TrackItBaseType implements Comparable<Waypoint>, DocumentItem, FolderTreeItem {
	private Double latitude;
	private Double longitude;
	private Double altitude;
	private Date time;
	private String name;
	private String comments;
	private String description;
	private String source;
	private String link;
	private String sym;
	private String type;
	private ImageIcon icon;
	private Point iconHotSpot = new Point(0, 15);
	private ImageIcon selectedIcon;
	private Point selectedIconHotSpot = new Point(7, 30);
	private GPSDocument parent;
	
	public Waypoint() {
		this.icon = ImageUtilities.createImageIcon("red_pin_16.png");
		this.selectedIcon = ImageUtilities.createImageIcon("pink_pin_32.png");
	}
	
	public Waypoint(Double latitude, Double longitude, Double altitude, String name, Date dateTime) {
		this();
		
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.name = name;
		this.time = dateTime;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getAltitude() {
		return altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getName() {
		String name;
		if (this.name != null) {
			name = this.name;
		} else if (time != null) {
			name = Formatters.getSimpleDateFormatMilis().format(time);
		} else {
			name = "Waypoint" + getId();
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getSym() {
		return sym;
	}

	public void setSym(String sym) {
		this.sym = sym;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ImageIcon getIcon() {
		return icon;
	}
	
	public Point getIconHotSpot() {
		return iconHotSpot;
	}
	
	public void setIcon(ImageIcon icon, Point hotSpot) {
		this.icon = icon;
		this.iconHotSpot = hotSpot;
	}
	
	public void setIcon(ImageIcon icon) {
		setIcon(icon, new Point(0, 0));
	}
	
	public ImageIcon getSelectedIcon() {
		if (selectedIcon == null && icon != null) {
			return icon;
		} else {
			return selectedIcon;
		}
	}
	
	public Point getSelectedIconHotSpot() {
		return selectedIconHotSpot;
	}
	
	public void setSelectedIcon(ImageIcon selectedIcon, Point selectedIconHotSpot) {
		this.selectedIcon = selectedIcon;
		this.selectedIconHotSpot = selectedIconHotSpot;
	}
	
	public void setSelectedIcon(ImageIcon selectedIcon) {
		setSelectedIcon(selectedIcon, new Point(0, 0));
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.name).append(" (").append(getType()).append(") ");
		sb.append("[").append(Formatters.getDecimalFormat(7).format(getLatitude())).append(", ");
		sb.append(Formatters.getDecimalFormat().format(getLongitude())).append("]");
		
		return sb.toString();
	}
	
	@Override
	public String getDocumentItemName() {
		if (getAltitude() != null) {
			return String.format("%s (%s)", getName(), Formatters.getFormatedAltitude(getAltitude()));
		} else {
			return getName();
		}
	}

	@Override
	public int compareTo(Waypoint wpt) {
		return getName().compareTo(wpt.getName());
	}
	
	public GPSDocument getParent() {
		return parent;
	}
	
	public void setParent(GPSDocument parent) {
		this.parent = parent;
	}
	
	/* FolderTreeItem interface implementation */

	@Override
	public String getFolderTreeItemName() {
		return getName();
	}

	@Override
	public ImageIcon getOpenIcon() {
		return icon;
	}

	@Override
	public ImageIcon getClosedIcon() {
		return icon;
	}

	@Override
	public ImageIcon getLeafIcon() {
		return icon;
	}

	/* DocumentItem interface implementation */

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}
	
	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.WAYPOINT_SELECTED, this);
	}

	@Override
	public List<Trackpoint> getTrackpoints() {
		Trackpoint trackpoint = new Trackpoint(getParent());
		trackpoint.setLatitude(latitude);
		trackpoint.setLongitude(longitude);
		trackpoint.setAltitude(altitude);
		return Arrays.asList(new Trackpoint[] { trackpoint });
	}
	
	/* Paintable Interface */
	
	@Override
	public void paint(Graphics2D graphics, MapLayer layer, Map<String, Object> paintingAttributes) {
		Location topLeftLocation = layer.getMapProvider().getLocation(0, 0, layer.getWidth(), layer.getHeight());
		Location bottomRightLocation = layer.getMapProvider().getLocation(layer.getWidth(), layer.getHeight(), layer.getWidth(), layer.getHeight());
		double minLongitude = topLeftLocation.getLongitude();
		double minLatitude = bottomRightLocation.getLatitude();
		double maxLongitude = bottomRightLocation.getLongitude();
		double maxLatitude = topLeftLocation.getLatitude();
		
		if (getLongitude() < minLongitude
				|| getLongitude() > maxLongitude
				|| getLatitude() < minLatitude
				|| getLatitude() > maxLatitude) {
			return;
		}
		
		MapPainter painter = MapPainterFactory.getInstance().getMapPainter(layer, this);
		painter.paint(graphics, paintingAttributes);
	}
}
