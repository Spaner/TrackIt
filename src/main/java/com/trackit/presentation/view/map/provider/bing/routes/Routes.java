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
package com.trackit.presentation.view.map.provider.bing.routes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
	private static final String SERVER = "http://dev.virtualearth.net";
	private static final String API_KEY = "AlmJ16Pgk8XSCVmfTLzXzSOJhofcHKFymy__9ZnJwOH_Cu1d1RfiAixZHhmEcKrG";
	private static final String URL_ROUTE = "%s/REST/v1/Routes/Driving?output=xml&routePathOutput=Points&wp.0=%f,%f&wp.1=%f,%f&avoid=highways&key=%s";
	
	private static Logger logger = Logger.getLogger(Routes.class.getName());
	
	public static List<Location> getRoute(Location start, Location end) throws TrackItException {
		Response response = null;
		
		try {
			String url = String.format(Locale.ENGLISH, URL_ROUTE, SERVER,
					start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude(), API_KEY);
			logger.debug(url);
	        JAXBContext context = JAXBContext.newInstance(Response.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
	        
	        InputStream inputStream = Connection.getInstance().getResource(url);
	        response = (Response) unmarshaller.unmarshal(inputStream);
	        inputStream.close();
	        
	        if (response == null) {
	        	throw new TrackItException("Unable to calculate route.");
	        }
	        
	        return getRoute(response);
		} catch (JAXBException e) {
			logger.error("Unable to calculate route data: " + e.getMessage());
			throw new TrackItException("Unable to calculate route data.");
		} catch (IOException e) {
			logger.error("Unable to calculate route data: " + e.getMessage());
			throw new TrackItException("Unable to calculate route elevation.");
		}
	}
	
	private static List<Location> getRoute(Response response) {
		List<Location> route = new ArrayList<>();
		
		List<Point> routePoints = response.getResourceSets().get(0).getResources().get(0).getRoutePath().getPoints();
		for (Point point : routePoints) {
			route.add(new Location(point.getLongitude(), point.getLatitude()));
		}
		
		return route;
	}

	public static void main(String[] args) throws TrackItException {
		Location start = new Location(-9.184189, 38.736589);
		Location end = new Location(-9.194145, 38.732438);
		
		List<Location> route = getRoute(start, end);
		for (Location location : route) {
			System.out.println(location);
		}
	}
}
