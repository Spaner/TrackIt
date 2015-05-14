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
package com.pg58406.trackit.business.utility;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;

public class MetadataReader {

	private File file;
	private Double latitude;
	private Double longitude;
	private Double altitude;
	private Date date;
	private String time;
	private String originalTime;

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

	public Date getDate() {
		return date;
	}

	public String getTime() {
		return time;
	}

	public String getOriginalTime() {
		return originalTime;
	}

	public MetadataReader(String filename) {
		file = new File(filename);
		Extract();

		/*File file = new File(filename);
		SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
		try {
			date = f.parse(f.format(file.lastModified()));
		} catch (ParseException e) {
			e.printStackTrace();
		}*/
	}

	public void Extract() {	

		Metadata metadata;
		try {
			metadata = ImageMetadataReader.readMetadata(file);	
						
			ExifIFD0Directory exifDirectory = metadata
					.getDirectory(ExifIFD0Directory.class);
			
			if(exifDirectory == null) return;

			date = exifDirectory
					.getDate(ExifIFD0Directory.TAG_DATETIME);

			GpsDirectory gpsDirectory = metadata
					.getDirectory(GpsDirectory.class);

			GeoLocation location = gpsDirectory.getGeoLocation();

			latitude = location.getLatitude();
			longitude = location.getLongitude();

		} catch (ImageProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

/*
 * private FileInputStream stream; private Double latitude; private Double
 * longitude; private Double altitude; private Date date; private String time;
 * private String originalTime;
 * 
 * public Double getLatitude() { return latitude; }
 * 
 * public Double getLongitude() { return longitude; }
 * 
 * public Double getAltitude() { return altitude; }
 * 
 * public Date getDate() { return date; }
 * 
 * public String getTempo(){ return time; }
 */

/*
 * public MetadataReader(String filename) { try { stream = new
 * FileInputStream(filename); } catch (FileNotFoundException e) {
 * e.printStackTrace(); } Extract();
 */
/*
 * File file = new File(filename); SimpleDateFormat f = new
 * SimpleDateFormat("dd-MMM-yyyy HH-mm-ss"); try { date =
 * f.parse(f.format(file.lastModified())); } catch (ParseException e) {
 * e.printStackTrace(); } System.out.println("Date: " + date);
 */

// }

/*
 * public void Extract() { Metadata metadata = new Metadata(); Parser parser =
 * new AutoDetectParser(); StringWriter writer = new StringWriter();
 * 
 * try { parser.parse(stream, new WriteOutContentHandler(writer), metadata, new
 * ParseContext()); } catch (Exception e) {
 * System.err.println(e.getClass().getName() + ": " + e.getMessage());
 * e.printStackTrace(); } String data; data = metadata.get(Metadata.LATITUDE);
 * if (data != null && !data.isEmpty()) latitude = Double.parseDouble(data);
 * System.out.println("Latitude: " + latitude); data =
 * metadata.get(Metadata.LONGITUDE); if (data != null && !data.isEmpty())
 * longitude = Double.parseDouble(data); System.out.println("Longitude: " +
 * longitude); data = metadata.get(Metadata.ALTITUDE); if (data != null &&
 * !data.isEmpty()) altitude = Double.parseDouble(data);
 * System.out.println("Altitude: " + altitude);
 */

/*
 * SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss"); try {
 * //date = f.parse(metadata.get(Metadata.DATE)); } catch (ParseException e) {
 * e.printStackTrace(); }
 */
/*
 * time = metadata.get(TikaCoreProperties.CREATED); System.out.println("Time: "
 * + time); originalTime = metadata.get(Metadata.ORIGINAL_DATE);
 * System.out.println("Original Time: " + originalTime); date =
 * metadata.getDate(TikaCoreProperties.CREATED);
 * System.out.println("Created Time: " + date); }
 * 
 * }
 */
