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
package com.trackit.presentation.view.map.provider.here.routes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import com.trackit.business.common.Constants;
import com.trackit.business.common.Location;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.Connection;
import com.trackit.presentation.view.map.provider.RoutingType;
import com.trackit.presentation.view.map.provider.TransportMode;
import com.trackit.presentation.view.map.provider.here.routes.routingcalculateroute.CalculateRouteType;
import com.trackit.presentation.view.map.provider.here.routes.routingcalculateroute.RouteType;

public class Routes {
	private static final String ROUTING_API_VERSION = "7.2";
	private static final String APP_ID = "P4P9xwgtSCv7SoyRKjFT";
	private static final String APP_CODE = "VMzF1FYn7r49ST6BIlaAbw";
	private static final String SERVER = "http://route.api.here.com";
	private static final String URL_ROUTE = "%s/routing/%s/calculateroute.xml?app_id=%s&app_code=%s&waypoint0=geo!%f,%f&waypoint1=geo!%f,%f"
			+ "&mode=%s;%s;traffic:disabled;motorway:%s,tollroad:%s&representation=navigation&routeattributes=sh";
	
	private static Logger logger = Logger.getLogger(Routes.class.getName());
	
	public static List<RouteType> getRoutes(Location start, Location end, Map<String, Object> routingOptions) throws TrackItException {
		CalculateRouteType directionsResponse = null;
		
		try {
			String routingType = ((RoutingType) routingOptions.get(Constants.RoutingOptions.ROUTING_TYPE)).getRoutingTypeName();
			routingType = (routingType != null ? routingType : RoutingType.FASTEST.getRoutingTypeName());
			String transportMode = ((TransportMode) routingOptions.get(Constants.RoutingOptions.TRANSPORT_MODE)).getTransportModeName();
			transportMode = (transportMode != null ? transportMode : TransportMode.CAR.getTransportModeName());
			
			boolean avoidHighways = (Boolean) routingOptions.get(Constants.RoutingOptions.AVOID_HIGHWAYS);
			int avoidHighwaysOption = avoidHighways ? -3 : 0;
			boolean avoidTollRoads = (Boolean) routingOptions.get(Constants.RoutingOptions.AVOID_TOLL_ROADS);
			int avoidTollRoadsOption = avoidTollRoads ? -3 : 0;
			
			String url = String.format(Locale.ENGLISH, URL_ROUTE, SERVER, ROUTING_API_VERSION, APP_ID, APP_CODE,
					start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude(), routingType, transportMode,
					avoidHighwaysOption, avoidTollRoadsOption);
			
			logger.debug(url);
	        JAXBContext context = JAXBContext.newInstance(CalculateRouteType.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
	        
	        InputStream inputStream = Connection.getInstance().getResource(url);
	        Source source = new StreamSource(inputStream);
	        JAXBElement<CalculateRouteType> root = unmarshaller.unmarshal(source, CalculateRouteType.class);
	        directionsResponse = root.getValue();
	        inputStream.close();
	        
	        if (directionsResponse == null
	        		|| directionsResponse.getResponse() == null
	        		|| directionsResponse.getResponse().getRoute() == null
	        		|| directionsResponse.getResponse().getRoute().isEmpty()) {
	        	throw new TrackItException("Unable to calculate route.");
	        }
	        
	        return directionsResponse.getResponse().getRoute();
		} catch (JAXBException e) {
			logger.error("Unable to calculate route data: " + e.getMessage());
			throw new TrackItException("Unable to calculate route data.");
		} catch (IOException e) {
			logger.error("Unable to calculate route data: " + e.getMessage());
			throw new TrackItException("Unable to calculate route elevation.");
		}
	}
}
