/*
 * This file is part of Track It!.
 * Copyright (C) 2017-2018 J Brisson Lopes
 * based on: Copyright (C) 2015 Pedro Gomes
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
package com.trackit.business.operation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trackit.business.common.Constants;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Pause;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.ExcelWriter;

public class PauseDetectionOperation extends OperationBase implements Operation {

	protected DetPausesNewVersionOperation detector;
	protected double					   speedThreshold;
	
	protected static String separator     = java.nio.file.FileSystems.getDefault().getSeparator();
	protected static String ExcelFilename = "XLS"+ separator +"DetPausesNEWVERSION.xls";
	protected static String TextFilename  = "Pauses Log.txt";
	
	public PauseDetectionOperation() {
		super();
	}

	public PauseDetectionOperation(Map<String, Object> options) {
		super( options);
	}

	@Override
	public String getName() {
		return Constants.PauseDetectionOperation.NAME;
	}
	
	public static String getTextFilename() {
		return TextFilename;
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		if ( processActivities )
			for( Activity activity: document.getActivities())
				process( activity);
		if ( processCourses )
			for( Course course: document.getCourses())
				process( course);
	}

	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		for( GPSDocument document: documents)
			try {
				process( document);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

	}
	
	public int process( DocumentItem item) {
		speedThreshold = getSpeedThreshold( item);
		int noPauses   = 0;
		List<Pause> pauses = null;
		if ( item.isActivity() ) {
			for( Session session: ((Activity) item).getSessions() )
				for( Lap lap: session.getLaps() ) {
					lap.getPauses().clear();
					pauses    = detectPauses( lap.getTrackpoints());
					noPauses += pauses.size();
					lap.setPauses( pauses);
				}
		}
		else
			if ( item.isCourse() ) {
				Course course = (Course) item;
				course.getPauses().clear();
				pauses    = detectPauses( course.getTrackpoints());
				noPauses += pauses.size();
				course.setPauses( pauses);
			}
		done( item, pauses);
		return noPauses;
	}
	
	protected  List<Pause> detectPauses( List<Trackpoint> trackpoints) {
		setup( trackpoints);
		List<Pause> pauses = new ArrayList<>();
		detector.DetectPauses( 1, trackpoints.size()-1, 1.0, speedThreshold, pauses);
		return pauses;
	}
	
	protected void done( DocumentItem item, List<Pause> pauses) {
		 writeFiles( pauses);
		item.publishSelectionEvent( null);
	}
	
	protected void setup( List<Trackpoint> trackpoints) {
		detector = new DetPausesNewVersionOperation( trackpoints);
	}
	
	protected double getSpeedThreshold( DocumentItem item) {
		double limit = 1.5;			// The original pause speed threshold value from pg.
		if ( item.isCourse() )
			limit = ((Course) item).getSubSport().getPauseThresholdSpeed();
		else
			if ( item.isActivity() )
				limit = ((Activity) item).getSubSport().getPauseThresholdSpeed();
		return limit / 3.6;
	}

	public void writeFiles( List<Pause> pauses){
		
		if ( pauses != null ) {
			File dir = new File("XLS");
			if( !dir.exists() ){
				dir.mkdir();
			}
			try {
				ExcelWriter excelWriter = new ExcelWriter( ExcelFilename);
				PrintWriter writer      = new PrintWriter( TextFilename, "UTF-8");
				if ( ! pauses.isEmpty() ) {
					int row = 1;
					for( Pause pause: pauses) {					
						Double sec = (pause.getEnd().getTime() - pause.getStart().getTime()) *.001;
						excelWriter.writeValue(  3, row, pause.getStart());
						excelWriter.writeValue(  4, row, pause.getEnd());
						excelWriter.writeDouble( 5, row, sec);
						row++;
						writer.println( pause.toString());
					}
				} else {
					writer.println("No Pauses Detected!");					
				}
				writer.close();
				excelWriter.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
	}

	@Override
	public void undoOperation(List<GPSDocument> document) throws TrackItException {
	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
	}

	@Override
	public void redoOperation(List<GPSDocument> document) throws TrackItException {
	}

}
