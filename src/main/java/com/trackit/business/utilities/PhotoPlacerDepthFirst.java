/*
 * This file is part of Track It!.
 * Copyright (C) 2018 J M Brisson Lopes
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
package com.trackit.business.utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trackit.TrackIt;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.Pause;
import com.trackit.business.domain.Picture;
import com.trackit.business.domain.Trackpoint;
import com.trackit.presentation.view.map.MapView;

public class PhotoPlacerDepthFirst extends PhotoPlacer{	

	protected List<Long> timeBetweenPausesLower;
	protected List<Long> timeBetweenPausesUpper;

	public PhotoPlacerDepthFirst( DocumentItem documentItem) {
		super( documentItem);
			timeBetweenPausesLower = new ArrayList<Long>();
			timeBetweenPausesUpper = new ArrayList<Long>();		
	}
	
	protected void CalculatePauseIntervals(List<Pause> pauses) {
		int i = 1;
		int limit = pauses.size();
		while (i < limit) {
			timeBetweenPausesLower.add(pauses.get(i).getStart().getTime()
					- pauses.get(i - 1).getEnd().getTime());
			timeBetweenPausesUpper.add(pauses.get(i).getEnd().getTime()
					- pauses.get(i - 1).getStart().getTime());
			i++;
		}

	}	
	
	public void EstimateLocation(List<Trackpoint> trackpoints) {
		// 12335: 2018-07-14 - guard against no pictures or no pauses
		if ( ! canLocatePictures() )
			return;

		CalculatePicturesIntervals(pictureList);
		// List<Picture> picsWithLocation = new ArrayList<Picture>();
		List<Picture> picsWithNoLocation = new ArrayList<Picture>();
		Picture temp, first = pictureList.get(0);
		picsWithNoLocation.add(first);
		for (int i = 1; i < pictureList.size(); i++) {
			temp = pictureList.get(i);
			if ( Math.abs( first.getTimestamp().getTime()
					     - temp.getTimestamp().getTime()) > 60000) {
				picsWithNoLocation.add(temp);
				first = temp;
			}
		}

		Long pauseIntervalInf, pauseIntervalSup;
		Long picInterval;
		Map<Integer, List<Integer>> places = new HashMap<Integer, List<Integer>>();
		int result = 0;

		if (picsWithNoLocation.size() == 1 || pauseList.size() == 1) {
			List<Integer> picPossiblePauses = new ArrayList<Integer>();
			picPossiblePauses.add(0);
			places.put(0, picPossiblePauses);
		} else {
			for (int picNumber = 0; picNumber < picsWithNoLocation.size() - 1; picNumber++) {
				picInterval = timeBetweenPictures.get(picNumber);
				List<Integer> picPossiblePauses = new ArrayList<Integer>();

				for (int pauseNumber = 0; pauseNumber < timeBetweenPausesLower
						.size(); pauseNumber++) {
					pauseIntervalInf = timeBetweenPausesLower.get(pauseNumber);
					pauseIntervalSup = timeBetweenPausesUpper.get(pauseNumber);
					if (picInterval > pauseIntervalInf
							&& picInterval < pauseIntervalSup) {
						picPossiblePauses.add(pauseNumber);
						continue;
					}
					for (int idk = pauseNumber - 1; idk >= 0; idk--) {
						pauseIntervalInf += timeBetweenPausesLower.get(idk);
						pauseIntervalSup += timeBetweenPausesUpper.get(idk);
						if (picInterval > pauseIntervalInf
								&& picInterval < pauseIntervalSup) {
							picPossiblePauses.add(pauseNumber);
						}
					}
				}
				places.put(picNumber, picPossiblePauses);
				if (picPossiblePauses.isEmpty())
					continue;
				if (picPossiblePauses.size() == 1) {
					result = picNumber;
					break;
				}
				if (places.get(result).isEmpty()
						|| places.get(result).size() > picPossiblePauses.size()) {
					result = picNumber;
				}
			}
		}

		Picture targetPic = picsWithNoLocation.get(result);

		Long targetTime = targetPic.getTimestamp().getTime();

		Pause targetPause = pauseList.get(places.get(result).get(0));

		Long pauseTime = targetPause.getMiddle().getTime();

		for (int i = 0; i < pictureList.size(); i++) {
			Picture p = pictureList.get(i);
			if (!targetPic.toString().equals(p.toString())) {
				Long t = targetTime - p.getTimestamp().getTime();
				Date d = new Date(pauseTime - t);
				p.setTimestamp(d);
				determinePosition(p, trackpoints);
			}
		}
		targetPic.setTimestamp(targetPause.getMiddle());
		determinePosition(targetPic, trackpoints);
		
		// 12335 - 2018-07-14
		reportSucess();
	}

}
