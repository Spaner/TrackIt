/*
 * This file is part of Track It!.
 * Copyright (C) 2016 J M Brisson Lopes
 *                    Based on the 2015 version by Pedro Gomes
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
package com.trackit.business.utilities;


import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.io.IOException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.trackit.business.utilities.geo.GeotaggedTimeZone;

public class MetadataReader {

	private File file;
	private Double latitude;
	private Double longitude;
	private Double altitude;
	// 12335: 2016-09-21
//	private Date date;
//	private String time;
//	private String originalTime;
	private Date originalTimestamp;
	private Date digitizedTimestamp;
	private Date lastUpdatedTimestamp;
	private Date gpsTimestamp;
	private String gpsTimestampString;
	private String exifVersion;
	private String gpsVersion;
	private String make;
	private String model;
	//12335: 2016-09-29
	private int    orientation = 1;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

	public File getFile() {
		return file;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getAltitude() {
		return altitude;
	}

	public Date getLastUpdatedTimestamp() {
		return lastUpdatedTimestamp;
	}
	
	public Date getOriginalTimestamp() {
		return originalTimestamp;
	}
	
	public Date getDigitizedTimeStamp() {
		return digitizedTimestamp;
	}
	
	public Date getGPSDateTimestamp() {
		return gpsTimestamp;
	}
	
	public String getExifVersion() {
		return exifVersion;
	}
	
	public String getGPSVersion() {
		return gpsVersion;
	}
	
	public String getMake() {
		return make;
	}
	
	public String getModel() {
		return model;
	}
	
	public String getGPSTimestampString() {
		return gpsTimestampString;
	}
	
	//12335: 2016-09-29
	public int getOrientation() {
		return orientation;
	}
	
	public String toString() {
		String pictureData = "File name: " + file.getName() + "\n";
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy:MM:dd HH:mm:ss");
		if ( make != null )
			pictureData += "Make: " + make + "\t";
		if ( model != null )
			pictureData += "Model: " + model + "\t";
		if ( exifVersion != null )
			pictureData += "Exif version: " + exifVersion + "\tOrientation: " + orientation + "\n";
		pictureData += "Shot at: " + sdf.format( originalTimestamp) + "     " +
				       "Digitized at: " + sdf.format( digitizedTimestamp) + "     " +
				       "Last modified: " + sdf.format( lastUpdatedTimestamp);
		if ( latitude != null && longitude != null) {
			pictureData += "\nGPS ";
			if ( gpsVersion != null )
				pictureData += "version: " + gpsVersion + "     ";
			else
				pictureData += "    ";
			pictureData += String.format( "Lat: %.6f,   Lon: %.6f,    Alt: %.1f     UTC time: %s",
					latitude, longitude, altitude, gpsTimestampString);
		}
		return pictureData;
	}
	
	public MetadataReader(String filename) {
		file = new File(filename);
		Extract();
	}

	private void Extract() {	

		System.out.println();
		try {
			Metadata metadata = ImageMetadataReader.readMetadata( file);
			
//			for( Directory dir : metadata.getDirectories())
//				for( Tag tag: dir.getTags())
//					System.out.println( tag.getTagName() + ": " + tag);
			
			originalTimestamp = digitizedTimestamp = null;
			ExifSubIFDDirectory ifdSubDirectory = metadata.getDirectory( ExifSubIFDDirectory.class);
			if ( ifdSubDirectory != null ) {
				originalTimestamp  = ifdSubDirectory.getDate( ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				digitizedTimestamp = ifdSubDirectory.getDate( ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
				exifVersion        = ifdSubDirectory.getDescription( ExifSubIFDDirectory.TAG_EXIF_VERSION);
			}
			
			lastUpdatedTimestamp = null;
			ExifIFD0Directory ifd0Directory = metadata.getDirectory( ExifIFD0Directory.class);
			if ( ifd0Directory != null ) {
				lastUpdatedTimestamp = ifd0Directory.getDate( ExifIFD0Directory.TAG_DATETIME);
				make                 = ifd0Directory.getString( ExifIFD0Directory.TAG_MAKE);
				model                = ifd0Directory.getString( ExifIFD0Directory.TAG_MODEL);
				if ( ifd0Directory.containsTag( ExifIFD0Directory.TAG_ORIENTATION) )
					orientation      = ifd0Directory.getInt( ExifIFD0Directory.TAG_ORIENTATION);
			}

			latitude = longitude = null;
			altitude = 0.;
			gpsTimestamp = null;
			GpsDirectory gpsDirectory = metadata.getDirectory( GpsDirectory.class);
			String date = "", time = "";
			if ( gpsDirectory != null ) {
				try {
					GeoLocation location = gpsDirectory.getGeoLocation();
					latitude   = location.getLatitude();
					longitude  = location.getLongitude();
					if ( gpsDirectory.containsTag( GpsDirectory.TAG_ALTITUDE) )
						altitude   = gpsDirectory.getDouble( GpsDirectory.TAG_ALTITUDE);
					else
						altitude = null;
					gpsVersion = gpsDirectory.getDescription( GpsDirectory.TAG_VERSION_ID);
					time       = gpsDirectory.getDescription( GpsDirectory.TAG_TIME_STAMP);
					date       = gpsDirectory.getString( GpsDirectory.TAG_DATE_STAMP);
					System.out.println( "\t" + time + "\t\t" + date);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if ( time !=null && !time.isEmpty() ) {
						if ( date == null || date.isEmpty() ) {
							GeotaggedTimeZone oriZone = new GeotaggedTimeZone( latitude, longitude, originalTimestamp);
							SimpleDateFormat  sdf     = new SimpleDateFormat( "yyyy:MM:dd HH:mm:ss");
							sdf.setTimeZone( oriZone);
							date = ifdSubDirectory.getDescription( ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
							Date tmp = sdf.parse( date);
							sdf.setTimeZone( TimeZone.getTimeZone( "UTC"));
							date = sdf.format( tmp);
							date = date.substring( 0, date.indexOf( ' '));
						}
						gpsTimestampString = date + " " + time;
						sdf.setTimeZone( TimeZone.getTimeZone( "UTC"));
						gpsTimestamp = sdf.parse( gpsTimestampString);
					}
					else {
						gpsTimestampString = null;
						gpsTimestamp       = null;
					}
				} catch ( ParseException e) {
					e.printStackTrace();
				}
			}
			System.out.println( toString());
		}
		catch ( IOException e) {
			e.printStackTrace();
		}
		catch ( ImageProcessingException e) {
			e.printStackTrace();
		}
		catch (MetadataException e) {
			e.printStackTrace();
		}
	}
	
}