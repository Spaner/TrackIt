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
package com.henriquemalheiro.trackit.presentation.view.map.provider.googlemaps.elevation;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.Connection;
import com.henriquemalheiro.trackit.business.utility.GooglePolyline;

public class Elevation {
	private static final String SERVER = "http://maps.googleapis.com";
	private static final String URL = "%s/maps/api/elevation/xml?sensor=false&locations=enc:%s";
	
	private static Logger logger = Logger.getLogger(Elevation.class.getName());
	
	public static void fetchElevation(Location location) throws TrackItException {
		fetchElevations(Arrays.asList(location));
	}
	
	public static void fetchElevations(List<Location> locations) throws TrackItException {
		List<Float> elevations = getElevations(locations);
		for (int i = 0; i < locations.size(); i++) {
			locations.get(i).setAltitude(elevations.get(i));
		}
	}
	
	public static List<Float> getElevations(List<Location> locations) throws TrackItException {
		try {
			StringBuilder url = new StringBuilder(String.format(Locale.ENGLISH, URL, SERVER, encodeLocations(locations)));
			logger.debug(url);
			
	        JAXBContext context = JAXBContext.newInstance(ElevationResponse.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
	        
	        InputStream inputStream = Connection.getInstance().getResource(url.toString());
	        ElevationResponse response = (ElevationResponse) unmarshaller.unmarshal(inputStream);
	        inputStream.close();
	        
	        if (response == null) {
	        	throw new TrackItException("Unable to fetch elevation.");
	        }
	        
	        List<Float> elevations = new ArrayList<>();
	        for (ElevationResponse.Result result : response.getResult()) {
	        	elevations.add(result.getElevation());
			}
	        
	        return elevations;
		} catch (JAXBException e) {
			logger.error("Unable to fetch elevation data: " + e.getMessage());
			throw new TrackItException("Unable to fetch elevation.");
		} catch (IOException e) {
			logger.error("Unable to fetch elevation data: " + e.getMessage());
			throw new TrackItException("Unable to fetch elevation.");
		}
	}
	
	private static Object encodeLocations(List<Location> locations) {
		String encodedLocations = GooglePolyline.encode(locations);
		
		try {
			return URLEncoder.encode(encodedLocations, "UTF-8");
		} catch(UnsupportedEncodingException uee) {
			logger.error("Failed to encode locations");
			return encodedLocations;
		}
	}
}
