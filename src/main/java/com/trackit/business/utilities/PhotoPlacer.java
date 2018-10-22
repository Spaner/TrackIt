/*
 * This file is part of Track It!.
 * Copyright (C) 2015 Pedro Gomes
 *           (C) 2018 J M Brisson Lopes
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import com.trackit.TrackIt;
import com.trackit.business.common.Formatters;
import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.Pause;
import com.trackit.business.domain.Picture;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.operation.ReportingPauseDetectionOperation;
import com.trackit.business.operation.NonReportingPauseDetectionOperation;
import com.trackit.business.operation.PauseDetectionPicCaseOperation;
import com.trackit.presentation.view.map.MapView;

import jxl.write.DateFormat;

public abstract class PhotoPlacer {
	
	// Weights (12335: 2018-07-10)
	private static double PICTURE_AT_ACCEPTABLE_LOCATION =  1.0;
	private static double PICTURE_AT_UNCERTAIN_LOCATION  =  0.5;
	private static double PICTURE_OUTSIDE_BOUNDS         = -2.0;

	protected DocumentItem   documentItem;						// 12335: 2018-07-14
	protected List<Picture>  pictureList;
	protected List<Pause>    pauseList;
	protected List<Long>     timeBetweenPictures;
	protected List<Date>	 originalTimestamps;				// 12335: 2018-07-12
	protected List<Location> originalLocations;					// 12335: 2018-07-12
	protected boolean		 locationPossible = false;			// 12335: 2018-07-14
	protected int            noPhotosInPauses = 0;				// 12335: 2018-07-14
	protected int			 noPhotosOutsidePauses = 0;			// 12335: 2018-07-14
	protected int			 noPhotosOutsideTimeBounds = 0;		// 12335: 2018-07-14

	public PhotoPlacer( DocumentItem documentItem) {
		if ( documentItem.isActivity() || documentItem.isCourse() ) {
			pictureList = documentItem.isActivity() ? ((Activity) documentItem).getPictures() :
													  ((Course) documentItem).getPictures();
			if ( pictureList != null && ! pictureList.isEmpty() ) {
				new NonReportingPauseDetectionOperation().process( documentItem);
				pauseList = documentItem.isActivity() ? ((Activity) documentItem).getPauses() :
													    ((Course) documentItem).getPauses();
				if ( pauseList != null && ! pauseList.isEmpty() )
				{
					this.documentItem   = documentItem;
					locationPossible    = true;
					timeBetweenPictures = new ArrayList<Long>();
					originalLocations   = new ArrayList<>();
					originalTimestamps  = new ArrayList<>();
					for( Picture picture: pictureList) {
						originalTimestamps.add( new Date( picture.getTimestamp().getTime()));
						Location location = new Location( picture.getLongitude(), picture.getLatitude());
						location.setAltitude( picture.getAltitude());
						originalLocations.add(  location);
					}
				} else {
					OperationConfirmationDialog.showOperationCompletionDialog(
							Messages.getMessage( "applicationPanel.menu.autoLocatePictures.failureNoPauses",
												 documentItem.getDocumentItemName()),
							Messages.getMessage( "messages.warning"));
					
				}
			} else {
				OperationConfirmationDialog.showOperationCompletionDialog(
						Messages.getMessage( "applicationPanel.menu.autoLocatePictures.failureNoPhotos",
											 documentItem.getDocumentItemName()),
						Messages.getMessage( "messages.warning"));
			}
			
		}
	}
	
	// 12335: 2018-07-14 - return first photo timw shift in seconds
	protected double getTimeShift() {
		return (pictureList.get( 0).getTimestamp().getTime() - originalTimestamps.get( 0).getTime())
				* .001;
	}

	protected void CalculatePicturesIntervals(List<Picture> pictures) {
		Collections.sort(pictures, new PictureComparator());
		int i = 1;
		int limit = pictures.size();

		while (i < limit) {
			timeBetweenPictures.add(pictures.get(i).getTimestamp().getTime()
					- pictures.get(i - 1).getTimestamp().getTime());
			i++;
		}
	}
	
	// 123325: 2018-07-14
	public boolean canLocatePictures() {
		return locationPossible;
	}

	public abstract void EstimateLocation(List<Trackpoint> trackpoints);

	protected void determinePosition(Picture pic, List<Trackpoint> trackpoints) {
		Long target = pic.getTimestamp().getTime()/* +3600000 */;
		Trackpoint currentTrackpoint = trackpoints.get(0);
		Trackpoint nextTrackpoint;
		Long currentTimestamp, nextTimestamp;
		for (int i = 1; i < trackpoints.size(); i++) {
			nextTrackpoint = trackpoints.get(i);
			currentTimestamp = currentTrackpoint.getTimestamp().getTime();
			nextTimestamp = nextTrackpoint.getTimestamp().getTime();
			if (target >= currentTimestamp && target <= nextTimestamp) {
				pic.setLatitude((currentTrackpoint.getLatitude() + nextTrackpoint.getLatitude())/2);
				pic.setLongitude((currentTrackpoint.getLongitude() + nextTrackpoint.getLongitude())/2);
				pic.setAltitude((currentTrackpoint.getAltitude() + nextTrackpoint.getAltitude())/2);
				break;
			}
			currentTrackpoint = nextTrackpoint;
		}
	}
		
	// 12335: 2018-07-14
	protected void reportSucess() {
		double score = getSolutionRating();
		
		double timeShift = getTimeShift();
		
		writeXlsLog();
		
		TrackIt.getApplicationPanel().getMapView().updateDisplay();
		if ( OperationConfirmationDialog.showOperationConfirmationDialog(
				Messages.getMessage( "applicationPanel.menu.autoLocatePictures.success",
						timeShift >= 0 ? "+": "-", 
						Formatters.getZeroUnleadFormatedDuration( Math.abs (timeShift)),
						Formatters.getDecimalFormat( 2).format( score),
						noPhotosInPauses, noPhotosOutsidePauses, noPhotosOutsideTimeBounds), 
				Messages.getMessage( "applicationPanel.menu.autoLocatePictures.okToChange")) ) {
			updatePicMetadata();
		} else {
			// revert times and locations, update display
			for( int i=0; i<pictureList.size(); i++) {
				Picture picture = pictureList.get( i);
				picture.setTimestamp( originalTimestamps.get( i));
				picture.setLatitude(  originalLocations.get( i).getLatitude());
				picture.setLongitude( originalLocations.get( i).getLongitude());
				picture.setAltitude(  originalLocations.get( i).getAltitude());
			}
			TrackIt.getApplicationPanel().getMapView().updateDisplay();
		}
	}
	
	protected void updatePicMetadata(){
		for (Picture p : pictureList){
			p.updateEXIF();
		}
	}
	
	// 12335: 2018-07-14
	protected double getSolutionRating() {
		List<Trackpoint> trackpoints = documentItem.isActivity() ?
											((Activity) documentItem).getTrackpoints() :
											((Course) documentItem).getTrackpoints();
		long minTime = trackpoints.get( 0).getTimestamp().getTime();
		long maxTime = trackpoints.get( trackpoints.size()-1).getTimestamp().getTime();
		
		noPhotosInPauses = noPhotosOutsidePauses = noPhotosOutsideTimeBounds = 0;
		for( Picture picture: pictureList) {
			long pictureTime = picture.getTimestamp().getTime();
			if ( pictureTime >= minTime && pictureTime <= maxTime ) {
				if (  isInsidePause( pictureTime) )
					noPhotosInPauses++;
				else
					noPhotosOutsidePauses++;
			} else
				noPhotosOutsideTimeBounds++;
		}
		
		double score = ( noPhotosInPauses          * PICTURE_AT_ACCEPTABLE_LOCATION  +
			     		 noPhotosOutsidePauses     * PICTURE_AT_UNCERTAIN_LOCATION   +
			     		 noPhotosOutsideTimeBounds * PICTURE_OUTSIDE_BOUNDS            )
				/    (pictureList.size() * PICTURE_AT_ACCEPTABLE_LOCATION);
		return score;
	}
	
	protected Boolean isInsidePause( Long time) {
		Boolean result = false;
		for (Pause p : pauseList) {
			Long start = p.getStart().getTime();
			Long end = p.getEnd().getTime();
			if (time > start && time < end) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	// 12335: 2018-07-12
	protected void writeXlsLog() {
		String directory  = "XLS";
		File dir = new File( directory);
		if ( !dir.exists() )
			dir.mkdir();
		ExcelWriter eWriter = new ExcelWriter( directory + File.separator + "PhotoLocations.xls");
		int row = 0;
		eWriter.writeString( 0, row, "Name");
		eWriter.writeString( 1, row, "Original date");
		eWriter.writeString( 2, row, "Date/Time");
		eWriter.writeString( 3, row, "Time diff");
		eWriter.writeString( 4, row, "Ori Latitude");
		eWriter.writeString( 5, row, "Ori Longitude");
		eWriter.writeString( 6, row, "New Latitude");
		eWriter.writeString( 7, row, "New Longitude");
		eWriter.writeString( 8, row, "Moved by (m)");
		SimpleDateFormat utc = new SimpleDateFormat( "HH:mm:ss");
		utc.setTimeZone( TimeZone.getTimeZone("UTC"));
		for( Picture p: pictureList ) {
			eWriter.writeString( 0, row+1, p.getName());
			eWriter.writeString( 1, row+1, utc.format( originalTimestamps.get( row)));
			eWriter.writeString( 2, row+1, utc.format( p.getTimestamp()));
			eWriter.writeDouble( 3, row+1, (originalTimestamps.get( row).getTime()-p.getTimestamp().getTime()) * .001);
			eWriter.writeCoordinate( 4, row+1, originalLocations.get( row).getLatitude());
			eWriter.writeCoordinate( 5, row+1, originalLocations.get( row).getLongitude());
			eWriter.writeCoordinate( 6, row+1, p.getLatitude());
			eWriter.writeCoordinate( 7, row+1, p.getLongitude());
			Location location = originalLocations.get( row);
			eWriter.writeDouble( 8, row+1, 
					Utilities.getGreatCircleDistance( location.getLatitude(), location.getLongitude(),
													  p.getLatitude(), 		  p.getLongitude())
					* 1000.);
			row++;
		}
		eWriter.close();
	}
}
