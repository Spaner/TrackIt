/*
 * This file is part of Track It!.
 * Copyright (C) 2016 J M Brisson Lopes
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
package com.jb12335.trackit.business.domain;

import java.util.List;

import org.apache.derby.iapi.services.loader.GeneratedMethod;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.WGS84;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventListener;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.view.map.MapInfo;
import com.henriquemalheiro.trackit.business.common.Messages;

public class CoordinatesFormatter implements EventListener{
	
	private CoordinatesType coordinatesType = CoordinatesType.DEFAULT;
	
	private static CoordinatesFormatter formatter = null;
	
	private static final String decimalDegreesFormat         = "%.6f";
	private static final String degreesDecimalMinutesFormat  = "%dº %.5f'";
	private static final String degreesMinutesSecondsFormat  = "%dº %d' %.4f\"";
	private static final String defaultCoordinateValueString = "---";
		
	public synchronized static CoordinatesFormatter getInstance() {
		if ( formatter == null )
			formatter = new CoordinatesFormatter();
		return formatter;
	}
	
	public CoordinatesFormatter( ) {
		EventManager.getInstance().register( this);
		setCoordinatesType();
	}
	
	private void setCoordinatesType() {
		coordinatesType = CoordinatesType.DEFAULT;
		int intType = TrackIt.getPreferences().getIntPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.COORDINATES, coordinatesType.valueOf()); 
		coordinatesType = CoordinatesType.lookup( intType);
		System.out.println( "Choice is " + coordinatesType.valueOf());
	}
	
	public void process( Event event, DocumentItem item) {
		if ( event == Event.COORDINATES_TYPE_CHANGED ) {
			setCoordinatesType();
		}
	}
	
	public void process( Event event, DocumentItem item, List<? extends DocumentItem> itens) {
	}
	
	public String toString( Double latitude, Double longitude, String prefix) {
		String localPrefix = prefix;
		if ( localPrefix.length() > 0 )
			localPrefix += " ";
		String coordinates = "";
		if ( coordinatesType != CoordinatesType.UTM && coordinatesType != CoordinatesType.MGRS ) {
			coordinates += localPrefix + Messages.getMessage ( "coordinates.latitude") + " ";
			if ( latitude != null ) {
				coordinates += formatCoordinate( latitude);
				if ( coordinatesType != CoordinatesType.DECIMAL_DEGREES )
					coordinates += " " + (latitude >= 0. ? 
										   Messages.getMessage( "coordinates.north.letter") :
										   Messages.getMessage( "coordinates.south.letter"));
			}
			else
				coordinates += defaultCoordinateValueString;
			
			coordinates += " ";
			
			coordinates += localPrefix + Messages.getMessage ( "coordinates.longitude") + " ";
			if ( longitude != null ) {
				coordinates += formatCoordinate( longitude);
				if ( coordinatesType != CoordinatesType.DECIMAL_DEGREES )
					coordinates += " " +  (longitude >= 0. ? 
										   Messages.getMessage( "coordinates.east.letter") :
										   Messages.getMessage( "coordinates.west.letter"));
			}
			else
				coordinates += defaultCoordinateValueString;
		}
		else {
			coordinates = localPrefix;
			if ( latitude != null && longitude != null) {
				UTMLocation utm = (new GeographicLocation( latitude, longitude)).toUTM();
				if ( coordinatesType == CoordinatesType.UTM )
					coordinates += Messages.getMessage( "coordinates.utm") + " " + utm.toString();
				else
					coordinates += Messages.getMessage( "coordinates.mgrs") + " " +utm.toMGRS().toString();
			}
			else
				coordinates += defaultCoordinateValueString;
		}
		return coordinates;
	}

	private String formatCoordinate( double value) {
		String text = "";
		switch (coordinatesType) {
		default:
		case DECIMAL_DEGREES:
			text = String.format( decimalDegreesFormat, value);
			break;
		case DEGREES_DECIMAL_MINUTES: {
			double absValue = Math.abs( value);
			int deg = (int) absValue;
			absValue = (absValue - deg) * 60.;
			text = String.format( degreesDecimalMinutesFormat, deg, absValue);
		}
			break;
		case DEGREES_MINUTES_SECONDS: {
			double absValue = Math.abs( value);
			int deg = (int) absValue;
			absValue = (absValue - deg) * 60.;
			int min = (int) absValue;
			absValue = (absValue - min) * 60.;
			text = String.format( degreesMinutesSecondsFormat, deg, min, absValue);
		}
			break;	
//		case UTM:
//			break;
//		default:
//			break;
		}
		return text;
	}
	
	// 2016-09-14: 12335
	public String toString() {
		return Messages.getMessage( "coordinatesFormatter.name");
	}
	
}
