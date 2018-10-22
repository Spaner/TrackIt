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
package com.trackit.business.domain;

import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.event.EventPublisher;

public class Video extends MultimediaItem{

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

}
