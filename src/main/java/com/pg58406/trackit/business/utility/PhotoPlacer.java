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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import java.io.File;
import java.text.SimpleDateFormat;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.pg58406.trackit.business.domain.Pause;
import com.pg58406.trackit.business.domain.Picture;

import jxl.write.DateFormat;

public abstract class PhotoPlacer {

	public PhotoPlacer(List<Picture> pictures, List<Pause> pauses, String targetName) {
		timeBetweenPictures = new ArrayList<Long>();
		this.pictureList = pictures;
		this.pauseList = pauses;
		this.targetName = targetName;
	}

	protected List<Picture> pictureList;
	protected List<Pause> pauseList;
	protected List<Long> timeBetweenPictures;
	protected String targetName;

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
	
	protected void noPausesFail(){
		String message = Messages.getMessage(
				"applicationPanel.menu.autoLocatePictures.failure",
				targetName);
		
		JOptionPane.showMessageDialog(
				TrackIt.getApplicationFrame(), message);
	}
	
	protected void success(){
		String message = Messages.getMessage("applicationPanel.menu.autoLocatePictures.success");
		
		int n = JOptionPane.showConfirmDialog(TrackIt.getApplicationFrame(), message,
				"", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if (n == 0) updatePicMetadata();
		
		//2016-09-17: 12335: temporary
		File dir = new File( "XLS");
		if ( !dir.exists() )
			dir.mkdir();
		String separator = java.nio.file.FileSystems.getDefault().getSeparator();
		ExcelWriter eWriter = new ExcelWriter( "XLS" + separator + "Photos.xls");
		int row = 0;
		SimpleDateFormat utc = new SimpleDateFormat( "HH:mm:ss");
		utc.setTimeZone( TimeZone.getTimeZone("UTC"));
		for( Picture p: pictureList ) {
			System.out.println( p.getName() + " " + p.getTimestamp() + "  " + p.getLatitude() + "  " + p.getLongitude());
			eWriter.writeString( 0, row, p.getName());
			eWriter.writeString( 1, row, utc.format( p.getTimestamp()));
			eWriter.writeCoordinate( 2, row, p.getLatitude());
			eWriter.writeCoordinate( 3, row, p.getLongitude());
			eWriter.writeDouble( 4, row, p.getAltitude());
			row++;
		}
		eWriter.close();
	}
	
	protected void updatePicMetadata(){
		for (Picture p : pictureList){
			p.updateEXIF();
		}
	}
}
