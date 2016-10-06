/*
 * This file is part of Track It!.
 * Copyright (C) 2016 J M Brisson Lopes
 *               This is a complete rewrite of the file with the same name by Pedro Gomes, 2015
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
package com.jb12335.trackit.business.utilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

public class MetadataWriter {
	
	private static TagInfo DATE_TIME    = TiffTagConstants.TIFF_TAG_DATE_TIME;
	private static TagInfo GPS_ALTITUDE = GpsTagConstants.GPS_TAG_GPS_ALTITUDE;
	private static TagInfo GPS_DATE     = GpsTagConstants.GPS_TAG_GPS_DATE_STAMP;
	private static TagInfo GPS_TIME     = GpsTagConstants.GPS_TAG_GPS_TIME_STAMP;
	
	private static DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

	public void update( String filepath, double longitude, double latitude, double altitude, 
			            Date date) {
		
		File jpegImageFile = new File(filepath);
		OutputStream os    = null;
		try {
            TiffOutputSet     outputSet = null;  
            TiffImageMetadata exif;
            
            final ImageMetadata metadata         = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if ( jpegMetadata != null ) {
            	exif = jpegMetadata.getExif();
            	if ( exif != null )
            		outputSet = exif.getOutputSet();
            }
            if ( outputSet == null )
            	outputSet = new TiffOutputSet();
            
//            System.out.println( "Previously:");
//            printTagValue( jpegMetadata, DATE_TIME);
//            printTagValue( jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
//            printTagValue( jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
//            printTagValue( jpegMetadata, GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
//            printTagValue( jpegMetadata, GPS_DATE);
//            printTagValue( jpegMetadata, GPS_TIME);

            // Set Exif Tiff Rev. 6.0 file change date and time attribute (0x132) 
            // to the current date and time
            // Note: Exif IFD attributes "Date and time of original data generation" (0x9003) and
            // "Date and time of digital data generation" (0x9004) are never changed
            
        	if ( fieldExists( jpegMetadata, DATE_TIME) )
        		outputSet.getRootDirectory().removeField( TiffTagConstants.TIFF_TAG_DATE_TIME);
            String updateDateString = formatter.format( new Date()); 
            TiffOutputField updateDate = new TiffOutputField( DATE_TIME, FieldType.ASCII,
            								updateDateString.length(), updateDateString.getBytes());
			outputSet.getRootDirectory().add( updateDate);
			
			// GPS IFD attributes
			
			TiffOutputDirectory gpsDirectory = outputSet.getGPSDirectory();
			if ( gpsDirectory == null )
				gpsDirectory = outputSet.addGPSDirectory();
			
			// Latitude (0x01 and 0x02) and Longitude (0x03 and 0x04)
        	outputSet.setGPSInDegrees( longitude, latitude);

        	// Altitude (0x06)
			if ( fieldExists( jpegMetadata, GPS_ALTITUDE) )
            	gpsDirectory.removeField( GPS_ALTITUDE);
    		gpsDirectory.add( GpsTagConstants.GPS_TAG_GPS_ALTITUDE,
    						  new RationalNumber((int) (altitude * 100), 100));
        	
    		DateFormat utcFormatter = ((DateFormat) formatter.clone());
    		utcFormatter.setTimeZone( TimeZone.getTimeZone("UTC"));
			String gpsTimestamp = utcFormatter.format( date);
			
        	// GPS date (0x1D)
			String gpsDate = gpsTimestamp.substring( 0, gpsTimestamp.indexOf(' '));
			if ( fieldExists( jpegMetadata, GPS_DATE) )
				gpsDirectory.removeField( GPS_DATE);
			TiffOutputField gpsDateField = new TiffOutputField( GPS_DATE, FieldType.ASCII,
													            gpsDate.length(), gpsDate.getBytes());
			gpsDirectory.add( gpsDateField);
        	
        	// GPS time (0x07)
			String gpsTime = gpsTimestamp.substring( gpsTimestamp.indexOf( ' ') + 1);
			ByteOrder order = outputSet.byteOrder;
			int hours = Integer.parseInt( gpsTime.substring( 0,2));
			int minutes = Integer.parseInt( gpsTime.substring( 3,5));
			int seconds = Integer.parseInt( gpsTime.substring( 6,8));
			byte[] rationals = new byte[24];
			intToByteArray( hours, rationals,  0, order);
			intToByteArray(  1, rationals,  4, order);
			intToByteArray( minutes, rationals,  8, order);
			intToByteArray(  1, rationals, 12, order);
			intToByteArray( seconds, rationals, 16, order);
			intToByteArray(  1, rationals, 20, order);
			if ( fieldExists( jpegMetadata, GPS_TIME) )
				gpsDirectory.removeField( GPS_TIME);    			
			TiffOutputField gpsTimeField = new TiffOutputField( GPS_TIME, FieldType.RATIONAL, 3, rationals);
			gpsDirectory.add( gpsTimeField);
			
			// Keep the original file
            jpegImageFile.renameTo( new File( filepath + ".backup"));
            jpegImageFile = new File( filepath + ".backup");

            // Now write a similar file with the new attribute values
            os = new FileOutputStream(new File(filepath));
            os = new BufferedOutputStream(os);

           new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);

		} catch (ImageReadException e) {
			e.printStackTrace();
		} catch (ImageWriteException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();		
        }
	}
	
	private void printTagValue( final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
		if ( field == null )
			System.out.println( tagInfo.name + ": Not found.");
		else
			System.out.println( tagInfo.name + ": " + field.getValueDescription());
	}
	
	private static boolean fieldExists( final JpegImageMetadata metadata, final TagInfo tagInfo) {
		return (getField( metadata, tagInfo) != null) ;
	}
	
	private static TiffField getField( final JpegImageMetadata metadata, final TagInfo tagInfo) {
		return metadata.findEXIFValueWithExactMatch( tagInfo);
	}
	
	private static void intToByteArray( int value, byte[] bytes, int offset, ByteOrder order) {
		long val = value;
		if ( order.equals( java.nio.ByteOrder.nativeOrder()) ) {
			bytes[3+offset] = (byte) (( val & 0xFF000000L) >> 24);
			bytes[2+offset] = (byte) (( val & 0x00FF0000L) >> 16);
			bytes[1+offset] = (byte) (( val & 0x0000FF00L) >>  8);
			bytes[  offset] = (byte)  ( val & 0x000000FFL) ;
		}
		else {
			bytes[  offset] = (byte) (( val & 0xFF000000L) >> 24);
			bytes[1+offset] = (byte) (( val & 0x00FF0000L) >> 16);
			bytes[2+offset] = (byte) (( val & 0x00FF0000L) >>  8);
			bytes[3+offset] = (byte)  ( val & 0x000000FFL) ;
		}
	}
}
