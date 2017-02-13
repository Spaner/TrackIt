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
package com.pg58406.trackit.business.operation.oldversions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.pg58406.trackit.business.domain.Pause;
import com.pg58406.trackit.business.operation.DetectPausesInterface;
import com.pg58406.trackit.business.utility.ExcelWriter;

public class DetPausesOpDistanceNoAVG implements DetectPausesInterface{
	// Simple Distance Interpolation
	//private DocumentItem item;
	private Double[] mInterpolatedField;
	private Double[] mDistance;
	private Date[] mTime;
	private Integer[] mSeconds;
	//private SimpleDateFormat dateFormat;

	public DetPausesOpDistanceNoAVG(List<Trackpoint> trkpoints) {
		//dateFormat = new SimpleDateFormat("HH:mm:ss");
		int listSize = trkpoints.size();
		mInterpolatedField = null;
		mDistance = new Double[listSize];
		mTime = new Date[listSize];
		mSeconds = new Integer[listSize];
		Trackpoint t = trkpoints.get(0);
		mDistance[0] = t.getDistance();
		mTime[0] = t.getTimestamp();
		mSeconds[0] = (int) (t.getTimestamp().getTime() / 1000);
		int i = 1;
		Trackpoint trk;
		while (i < listSize) {
			trk = trkpoints.get(i);
			mDistance[i] = trk.getDistance();
			mTime[i] = trk.getTimestamp();
			mSeconds[i] = (int) (mTime[i].getTime() / 1000);
			i++;
		}
	}

	public boolean DetectPauses(int start, int end, Double timestep,
			Double limit, List<Pause> pauses) {
		File dir = new File("XLS");
		if(!dir.exists()){
			dir.mkdir();
		}
		ExcelWriter excelWriter = new ExcelWriter("XLS\\DetPausesDistanceNoAVG.xls");
		boolean any = false;
		int no = InterpolateDistance(start, end, timestep);

		int currentTime = (int) mSeconds[start];
		int idx = 0;
		int iend;
		PrintWriter writer;
		int row = 1;
		try {
			writer = new PrintWriter("Pauses Log.txt", "UTF-8");
			while (idx < no) {
				if (mInterpolatedField[idx] <= limit) {
					iend = idx;
					while (iend < (no - 1)
							&& mInterpolatedField[iend + 1] <= limit)
						iend++;
					Date startDate = new Date(
							(long) (mTime[start].getTime() + (idx * 1000 * timestep)));
					Date endDate = new Date(
							(long) (mTime[start].getTime() + (iend * 1000 * timestep)));
					Double sec = new Double(
							(endDate.getTime() - startDate.getTime()) / 1000);
					// if (sec > 30) {
					excelWriter.writeValue(3, row, startDate);
					excelWriter.writeValue(4, row, endDate);
					row++;
					Pause pause = new Pause(startDate, endDate, sec);
					writer.println(pause.toString());
					pause.setCurrentTime(currentTime);
					pauses.add(pause);
					any = true;
					// }
					idx = iend + 1;
					currentTime = new Double((mSeconds[start]) + (idx + iend)
							* timestep * .5).intValue();
					pauses.add(pause);
					any = true;
				} else
					idx++;
			}
			writer.close();
			excelWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return any;
	}

	// Long seconds = date.getTime()/1000;
	private int InterpolateDistance(int start, int end, Double timestep) {
		int no = new Double(((mSeconds[end]) - (mSeconds[start])) / timestep
				+ 1).intValue();
		if (no > 1) {
			if (!(mInterpolatedField != null))
				mInterpolatedField = null;
			mInterpolatedField = new Double[no];
			mInterpolatedField[0] = mDistance[start];
			double currentTime = mSeconds[start];
			double factor;
			int idx = start;
			for (int i = 1; i < no; i++) {
				currentTime += timestep;
				while (currentTime > (mSeconds[idx]))
					idx++;
				factor = (currentTime - (mSeconds[idx - 1]))
						/ ((mSeconds[idx]) - (mSeconds[idx - 1]));
				mInterpolatedField[i] = (1 - factor) * mDistance[idx - 1]
						+ factor * mDistance[idx];
			}
			for (int i = no - 1; i > 0; i--)
				mInterpolatedField[i] -= mInterpolatedField[i - 1];
		} else
			no = 0;
		try {
			PrintWriter writer = new PrintWriter(
					"mInterpolatedField Values.txt", "UTF-8");
			int i = 0;
			for (Double d : mInterpolatedField) {
				if (d < timestep)
					writer.println("[" + i + "]\t" + d + "\tWARNING");
				else
					writer.println("[" + i + "]\t" + d);
				i++;
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return no;
	}
}
