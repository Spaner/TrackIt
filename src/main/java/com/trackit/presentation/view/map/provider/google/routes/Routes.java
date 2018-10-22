/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
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
package com.trackit.presentation.view.map.provider.google.routes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.trackit.business.common.Location;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.Connection;

public class Routes {
	private static final String SERVER = "http://maps.googleapis.com/";
	private static final String URL_ROUTE = "%smaps/api/directions/xml?origin=%f,%f&destination=%f,%f&sensor=false&mode=driving&avoid=tolls&avoid=highways&avoid=ferries&language=pt";
	
	private static Logger logger = Logger.getLogger(Routes.class.getName());
	
	public static List<Route> getRoutes(Location start, Location end) throws TrackItException {
		DirectionsResponse directionsResponse = null;
		
		try {
			String url = String.format(Locale.ENGLISH, URL_ROUTE, SERVER,
					start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
			logger.debug(url);
	        JAXBContext context = JAXBContext.newInstance(DirectionsResponse.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
	        
	        InputStream inputStream = Connection.getInstance().getResource(url);
	        directionsResponse = (DirectionsResponse) unmarshaller.unmarshal(inputStream);
	        inputStream.close();
	        
	        if (directionsResponse == null || directionsResponse.getRoutes() == null || directionsResponse.getRoutes().isEmpty()) {
	        	throw new TrackItException("Unable to calculate route.");
	        }
	        
	        return directionsResponse.getRoutes();
		} catch (JAXBException e) {
			logger.error("Unable to calculate route data: " + e.getMessage());
			throw new TrackItException("Unable to calculate route data.");
		} catch (IOException e) {
			logger.error("Unable to calculate route data: " + e.getMessage());
			throw new TrackItException("Unable to calculate route elevation.");
		}
	}
}
