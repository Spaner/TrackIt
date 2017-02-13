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

import java.util.Date;
import java.util.List;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.presentation.view.map.MapView;
import com.pg58406.trackit.business.domain.Pause;
import com.pg58406.trackit.business.domain.Picture;

public class PhotoPlacerBreadthFirst extends PhotoPlacer {

	public PhotoPlacerBreadthFirst(List<Picture> pictures, List<Pause> pauses,
			String targetName) {
		super(pictures, pauses, targetName);
		// pauses.remove(0);
	}

	public void EstimateLocation(List<Trackpoint> trackpoints) {
		CalculatePicturesIntervals(pictureList);

		int target = 0;
		int targetAccuracy = -1;
		for (int i = 0; i < pauseList.size(); i++) {
			int accurate = 0;
			Long middle = pauseList.get(i).getMiddle().getTime();
			for (int t = 0; t < timeBetweenPictures.size(); t++) {
				middle += timeBetweenPictures.get(t);
				if (IsInsidePause(middle)) {
					accurate++;
				}
			}
			if (accurate > targetAccuracy) {
				target = i;
				targetAccuracy = accurate;
			}
		}

		Pause targetPause = pauseList.get(target);
		Long pauseTime = targetPause.getMiddle().getTime();

		Picture targetPic = pictureList.get(0);
		targetPic.setTimestamp(targetPause.getMiddle());
		determinePosition(targetPic, trackpoints);
		Long drift = (long) 0;
		for (int i = 1; i < pictureList.size(); i++) {
			Picture p = pictureList.get(i);
			drift += timeBetweenPictures.get(i - 1);
			Date d = new Date(pauseTime + drift);
			pictureList.get(i).setTimestamp(d);
			determinePosition(p, trackpoints);
		}

		success();

		MapView mv = TrackIt.getApplicationPanel().getMapView();
		mv.updateDisplay();

	}

	private Boolean IsInsidePause(Long time) {
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
}
