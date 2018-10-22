/*
 * This file is part of Track It!.
 * Copyright (C) 2018 João Brisson
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

package com.trackit.business.utilities.geo;

import javax.swing.JOptionPane;
import java.awt.Component;

import com.trackit.business.common.Messages;
import com.trackit.business.domain.GeographicBoundingBox;

public class GeolocationService {

	protected String                location = "";
	protected String                status   = "";
	protected GeographicBoundingBox oldBox = null;
	
	public GeographicBoundingBox getBoundingBox( String locationName, Double range) {
		GeographicBoundingBox newBox = null;
		if ( location.isEmpty() || (!locationName.isEmpty() && !locationName.equals( location)) ) {
			double[] coordinates = getLocationCoordinates( locationName);
			if ( coordinates != null )
				oldBox = new GeographicBoundingBox( coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
			else
				return null;
		}
		newBox = new GeographicBoundingBox( oldBox);
		newBox.checkBoxRange( range, true);	
		location = locationName;
		return newBox;
	}
	
	protected double[] getLocationCoordinates( String locationName) {
		return null;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void showStatus( Component component, boolean isError, String info) {
		String messageToDisplay;
		String messageTitle;
		if ( isError ) {
			messageToDisplay = Messages.getMessage( "geolocation.dialog.errorMessage", status);
			messageTitle = Messages.getMessage(     "geolocation.dialog.errorHeadline");
		} else {
			messageToDisplay = Messages.getMessage( "geolocation.dialog.noSuchLocation", info);
			messageTitle = Messages.getMessage(     "geolocation.dialog.warningHeadline");
		}
		JOptionPane.showMessageDialog( component, messageToDisplay, messageTitle, JOptionPane.ERROR_MESSAGE);
		
	}

}
