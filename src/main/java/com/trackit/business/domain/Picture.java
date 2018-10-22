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
package com.trackit.business.domain;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.trackit.business.database.Database;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.MetadataReader;
import com.trackit.business.utilities.MetadataWriter;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.view.folder.FolderTreeItem;

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
	//12335: 2016-09-29
	private int orientation;

	public Picture(File file, double mpLatitude, double mpLongitude, double mpAltitude, PhotoContainer container){
		super();
		try {
			this.geotagged = true;
			this.image = ImageIO.read(file);
//			this.icon = getIcon(16, 16);
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
//			this.timestamp = reader.getDate(); 		//2016-09-19: 12335
			this.timestamp = getTimestamp( reader);
			this.name = file.getName();
			this.filePath = file.getCanonicalPath();
			this.container = container;
			this.containerFilePath = container.getFilepath();
			//12335: 2016-09-29
			this.orientation = reader.getOrientation();
			if ( orientation != 1 )
				image = applyExifOrientation( image, orientation);
			this.icon = getIcon(36, 36);

//          12335 : 2016-10-07 : Do not update any picture registration before document close time
//			Database.getInstance().updatePicture(this);
			
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

	//12335: 2016-09-25: looks for the picture's timestamp in the following order
	/* 	  i) GPS date and time
	 *	 ii) original datetime
	 *  iii) modified datetime
	 */
	private Date getTimestamp( MetadataReader reader) {
		Date date = reader.getGPSDateTimestamp();
		if ( date == null )
			date = reader.getOriginalTimestamp();
		if ( date == null )
			date = reader.getLastUpdatedTimestamp();
		return date;
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
	
	public int getOrientation() {		// 12335: 2016-10-19
		return orientation;
	}
	
	public PhotoContainer getContainer() {
		return container;
	}
	
	//12335: 2016-09-29
	public ImageIcon getIcon() {
		return icon;
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
	
	//12335: 2016-09-29: Apply Exif orientation to image so that it display upright
	static BufferedImage applyExifOrientation( BufferedImage original, int exifOrientation) {
		if ( exifOrientation != 1 ) {
			AffineTransform transform = new AffineTransform();
			switch ( exifOrientation ) {
			case 2:  	// flip x
				transform.scale( -1., 1.);
				transform.translate( -original.getWidth(), 0.);
				break;
			case 3:		// rotate PI
				transform.translate( original.getWidth(), original.getHeight());
				transform.rotate( Math.PI);
				break;
			case 4:		// flip y
				transform.scale( 1., -1.);
				transform.translate( 0., original.getHeight());
				break;
			case 5:		// rotate -PI/2, flip y
				transform.rotate( -Math.PI / 2.);
				transform.scale( -1, 1.);
				break;
			case 6:		// rotate -PI/2, -width
				transform.translate( original.getHeight(), 0);
				transform.rotate( Math.PI/2);
				break;
			case 7:		//rotate PI/2, flip
				transform.scale( -1., 1.);
				transform.translate( -original.getHeight(), 0.);
				transform.translate( 0., original.getWidth());
				transform.rotate( 3 * Math.PI / 2);
				break;
			case 8:		// rotate PI/2
				transform.translate( 0, original.getWidth());
				transform.rotate( 3 * Math.PI / 2);
				break;
			default:
				break;
			}
			AffineTransformOp transformOp = new AffineTransformOp( transform,
					                                               AffineTransformOp.TYPE_BILINEAR);
			BufferedImage destination = new BufferedImage( original.getHeight(), original.getWidth(),
					                                       original.getType());
			destination = transformOp.filter( original, destination);
			return destination;
		}
		return original;
	}
	
//	public ImageIcon getIcon(double boundWidth, double boundHeight) {
	private ImageIcon getIcon(double boundWidth, double boundHeight) {
		
		//12335: 2016-09-29: icons keep picture's aspect ratio
		//                   icons' smaller dimension is always 36 pixels
		double width= image.getWidth();
//		double origPicWidth = image.getWidth();
		double height = image.getHeight();
//		double origPicHeight = image.getHeight();
		if ( width >= height ) {
			width  = boundHeight * width / height;
			height = boundHeight;
		}
		else {
			height = boundWidth * height / width;
			width  = boundWidth;
		}
//		if (width >= boundWidth) {
//			// scale width to fit
//			width = boundWidth;
//			// scale height to maintain aspect ratio
//			height = (width * height) / origPicWidth;
//		}
//		System.out.println( "width, oWidth, height, oHeight: " + width + "  " + origPicWidth + "  " + height + "  " + origPicHeight);
//
//		if (origPicHeight >= boundHeight) {
//			// scale height to fit instead
//			height = boundHeight;
//			// scale width to maintain aspect ratio
//			width = (height * origPicWidth) / origPicHeight;
//		}

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
