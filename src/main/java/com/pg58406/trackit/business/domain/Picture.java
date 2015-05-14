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
package com.pg58406.trackit.business.domain;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Visitor;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderTreeItem;
import com.pg58406.trackit.business.db.Database;
import com.pg58406.trackit.business.utility.MetadataReader;
import com.pg58406.trackit.business.utility.MetadataWriter;

public class Picture extends MultimediaItem implements DocumentItem, FolderTreeItem{

	private Double latitude;
	private Double longitude;
	private Double altitude;
	private Date timestamp;
	private BufferedImage image;
	private String name;
	private String filePath;
	private PhotoContainer container;
	private String containerFilePath;
	private ImageIcon icon;
	private boolean geotagged;	

	public Picture(File file, double mpLatitude, double mpLongitude, double mpAltitude, PhotoContainer container){
		super();
		try {
			this.geotagged = true;
			this.image = ImageIO.read(file);
			this.icon = getIcon(16, 16);
			MetadataReader reader = new MetadataReader(file.getAbsolutePath());
			this.latitude = reader.getLatitude();
			this.longitude = reader.getLongitude();
			if(latitude == null || longitude == null){
				this.latitude = mpLatitude;
				this.longitude = mpLongitude;
				this.geotagged = false;
			}
			this.altitude = reader.getAltitude();
			this.altitude = mpAltitude;
			this.timestamp = reader.getDate();
			this.name = file.getName();
			this.filePath = file.getCanonicalPath();
			this.container = container;
			this.containerFilePath = container.getFilepath();
			Database.getInstance().updatePicture(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DocumentItem getParent() {
		return (DocumentItem)container;
	}	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getContainerFilePath() {
		return containerFilePath;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String path) {
		this.filePath = path;
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

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setImage(BufferedImage image){
		this.image = image;
	}
	
	public BufferedImage getImage(){
		return image;
	}
	
	public PhotoContainer getContainer() {
		return container;
	}
	
	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.PICTURE_SELECTED, this);
		
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
		
	}
	
	public String toString(){
		return name;
	}
	
	@Override
	public String getDocumentItemName() {
		return name;
	}

	@Override
	public String getFolderTreeItemName() {
		return name;
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
	
	public ImageIcon getIcon(double boundWidth, double boundHeight) {
		double width= image.getWidth();
		double origPicWidth = image.getWidth();
		double height = image.getHeight();
		double origPicHeight = image.getHeight();
		if (width >= boundWidth) {
			// scale width to fit
			width = boundWidth;
			// scale height to maintain aspect ratio
			height = (width * height) / origPicWidth;
		}

		if (origPicHeight >= boundHeight) {
			// scale height to fit instead
			height = boundHeight;
			// scale width to maintain aspect ratio
			width = (height * origPicWidth) / origPicHeight;
		}

		BufferedImage resized = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, (int)width, (int)height, 0, 0, image.getWidth(),
				image.getHeight(), null);
		g.dispose();
		
		return new ImageIcon(resized);
	}
	
	public void setCoordinates(Double latitude, Double longitude, Double altitude){
		setLatitude(latitude);
		setLongitude(longitude);
		setAltitude(altitude);
		updateEXIF();
	}
	
	public void updateEXIF(){
			new MetadataWriter().update(filePath, longitude, latitude, altitude, timestamp);
	}
	
	public boolean isGeotagged() {
		return geotagged;
	}
	
	public void printCoordinates(){
		System.out.print(name + ": ");
		System.out.println(latitude + "; " + longitude);
	}
}
