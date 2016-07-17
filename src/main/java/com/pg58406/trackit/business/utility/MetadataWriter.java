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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.imaging.util.IoUtils;

public class MetadataWriter {

	public void update(String filepath, double longitude, double latitude,
			double altitude, Date date) {
		
		File jpegImageFile = new File(filepath);
		OutputStream os = null;
        boolean canThrow = false;
        try{
            TiffOutputSet outputSet = null;      

            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    outputSet = exif.getOutputSet();
                }
            }
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }
            
            {
            	final TiffOutputDirectory exifDirectory = outputSet
                        .getOrCreateExifDirectory();
            	
            	DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        		
    			TiffOutputField new_date_time_orig_field = new TiffOutputField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, FieldType.ASCII, formatter.format(date.getTime()).length(), formatter.format(date.getTime()).getBytes());
    			
    			exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
    			exifDirectory.add(new_date_time_orig_field);
            	
            	exifDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
    			exifDirectory.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE,
    					new RationalNumber((int) (altitude * 100), 100));
            }
            {
            	outputSet.setGPSInDegrees(longitude, latitude);
            }
            
            jpegImageFile.renameTo(new File(filepath+".backup"));
            jpegImageFile = new File(filepath+".backup");
            
            os = new FileOutputStream(new File(filepath));
            os = new BufferedOutputStream(os);

           new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);

            canThrow = true;
        } catch (ImageReadException e) {
			e.printStackTrace();
		} catch (ImageWriteException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();		
        } finally {
            try {
				IoUtils.closeQuietly(canThrow, os);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
}
