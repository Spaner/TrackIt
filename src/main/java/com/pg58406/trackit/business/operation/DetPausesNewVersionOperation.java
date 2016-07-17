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
package com.pg58406.trackit.business.operation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.pg58406.trackit.business.domain.Pause;
import com.pg58406.trackit.business.utility.ExcelWriter;

public class DetPausesNewVersionOperation{
	
	private List<Trackpoint> trackpoints;
	//private SimpleDateFormat dateFormat;
	
	public DetPausesNewVersionOperation(List<Trackpoint> trkpoints) {
		this.trackpoints = trkpoints;
		//dateFormat = new SimpleDateFormat("HH:mm:ss");		
	}

	public boolean DetectPauses(int start, int end, Double timestep,
			Double limit, List<Pause> pauses) {
		int n = trackpoints.size();
		double testSpeed;
		@SuppressWarnings("unused")
		double pauseTime = 0;
		boolean entering = true;
		List<Double> startTimes = new ArrayList<Double>();
		List<Double> endTimes = new ArrayList<Double>();

		Trackpoint trkp = trackpoints.get(0);
		Trackpoint trkpMinusOne;
		if (trkp.getSpeed() <= limit) {
			pauseTime = -trkp.getTimestamp().getTime() / 1000;
			entering = false;
			startTimes.add((double) (trkp.getTimestamp().getTime() / 1000));
		}

		for (int i = 1; i < n; i++) {
			trkp = trackpoints.get(i);
			trkpMinusOne = trackpoints.get(i - 1);
			testSpeed = (trkp.getSpeed() - limit)
					* (trkpMinusOne.getSpeed() - limit);
			if (testSpeed < 0) {
				double time = trkp.getTimestamp().getTime() / 1000;
				double timeMinusOne = trkpMinusOne.getTimestamp().getTime() / 1000;
				double t = (time - timeMinusOne)
						* (limit - trkpMinusOne.getSpeed())
						/ (trkp.getSpeed() - trkpMinusOne.getSpeed())
						+ timeMinusOne;
				if (entering){
					pauseTime -= t;
					startTimes.add(t);
				}
				else{
					pauseTime += t;
					endTimes.add(t);
				}
				entering ^= true;
			}
		}
		if (!entering){
			trkp = trackpoints.get(n-1);
			double time = trkp.getTimestamp().getTime() / 1000;
			pauseTime += time;
			endTimes.add(time);
		}
		
		pauses = writeFiles(startTimes, endTimes, pauses);
		
		return (startTimes.size()>0 && endTimes.size()>0);
	}
	
	public List<Pause> writeFiles(List<Double> startTimes, List<Double> endTimes, List<Pause> pauses){
		
		File dir = new File("XLS");
		if(!dir.exists()){
			dir.mkdir();
		}
		String separator = java.nio.file.FileSystems.getDefault().getSeparator();
		PrintWriter writer;
		ExcelWriter excelWriter = new ExcelWriter("XLS"+ separator +"DetPausesNEWVERSION.xls");
		int row = 1;
		try {
			writer = new PrintWriter("Pauses Log.txt", "UTF-8");
			for (int i = 0; i < startTimes.size(); i++) {
								
					Date startDate = new Date((long) (startTimes.get(i)*1000));
					Date endDate = new Date(
							(long) (endTimes.get(i)*1000));
					Double sec = new Double(
							(endDate.getTime() - startDate.getTime()) / 1000);
						excelWriter.writeValue(3, row, startDate);
						excelWriter.writeValue(4, row, endDate);
						row++;
						Pause pause = new Pause(startDate, endDate, sec);
						writer.println(pause.toString());
						pauses.add(pause);
			}
			if (pauses.size()==0) writer.println("No Pauses Detected!");
			writer.close();
			excelWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return pauses;
	}

}
