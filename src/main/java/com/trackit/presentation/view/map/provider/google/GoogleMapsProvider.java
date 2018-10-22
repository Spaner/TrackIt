/*
 * This file is part of Track It!.
 * Copyright (C) 2018 João Brisson Lopes
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

package com.trackit.presentation.view.map.provider.google;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trackit.presentation.view.map.provider.google.Maneuver;
import com.trackit.presentation.view.map.provider.google.routes.Leg;
import com.trackit.presentation.view.map.provider.google.routes.Route;
import com.trackit.presentation.view.map.provider.google.routes.Routes;
import com.trackit.presentation.view.map.provider.google.routes.Step;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.GooglePolyline;
import com.trackit.presentation.view.map.MapTile;
import com.trackit.presentation.view.map.MapType;
import com.trackit.presentation.view.map.provider.MapProvider;
import com.trackit.presentation.view.map.provider.MapProviderType;
import com.trackit.presentation.view.map.provider.SphericalMercatorMapProvider;
import com.trackit.presentation.view.map.provider.SphericalMercatorMapTile;
import com.trackit.presentation.view.map.provider.bing.elevation.Elevation;

public class GoogleMapsProvider extends SphericalMercatorMapProvider implements MapProvider {
	
	public static final String GOOGLE_KEY = "AIzaSyBRc0iY3fHJ6PVNFsZVaEEsM-7xD_zaZ7c";

	private Map<String, Object> routingOptions;

	public GoogleMapsProvider(MapType mapType, Location centerLocation ) {
		super(MapProviderType.GOOGLE_MAPS, mapType, centerLocation);
		tileFileExtension = "png";
	}	
	
	protected String composeURL( String template, SphericalMercatorMapTile mapTile) {
		return String.format( template, 
							  getTileCentralLocationAsString( mapTile.getX(), mapTile.getY()),
							  mapTile.getZoom(),
				              tileWidth, tileHeight*2, GOOGLE_KEY);
	}	

	public String getName() {
		return MapProviderType.GOOGLE_MAPS.getDescription();
    }

	@Override
	public boolean hasRoutingSupport() {
		return true;
	}

	@Override
	public Course getRoute(Location startLocation, Location endLocation, Map<String, Object> options) throws TrackItException {
		this.routingOptions = options;
		
		Course course = new Course();
		Route route = getFirstRoute(startLocation, endLocation);
		
		if (route != null) {
			addTrackpoints(course, route);
			addCoursePoints(course, route);
		}
		
		return course;
	}
	
	private Route getFirstRoute(Location startLocation, Location endLocation) throws TrackItException {
		List<Route> routes = Routes.getRoutes(startLocation, endLocation);
		
		return (routes != null && !routes.isEmpty() ? routes.get(0) : null);
	}

	private void addTrackpoints(Course course, Route route) {
		List<Location> locations = GooglePolyline.decode(route.getOverviewPolyline().getPoints());
		fecthElevation(locations);
		course.addTrackpoints(getTrackpoints(course, locations));
	}

	private void fecthElevation(List<Location> locations) {
		try {
			Elevation.fetchElevations(locations);
		} catch (TrackItException e) {
			logger.debug("Unable to fetch elevations", e);
		}
	}
	
	private List<Trackpoint> getTrackpoints(Course course, List<Location> locations) {
		List<Trackpoint> trackpoints = new ArrayList<>();
		for (Location location : locations) {
			trackpoints.add(getTrackpoint(course, location));
		}
		
		return trackpoints;
	}

	private Trackpoint getTrackpoint(Course course, Location location) {
		Trackpoint trackpoint = new Trackpoint(location.getLongitude(), location.getLatitude(), course);
		trackpoint.setAltitude(location.getAltitude());
		
		return trackpoint;
	}

	private void addCoursePoints(Course course, Route route) {
		boolean addDirectionCoursePoints = (Boolean) routingOptions.get(Constants.RoutingOptions.ADD_DIRECTION_COURSE_POINTS);
		
		if (!addDirectionCoursePoints) {
			return;
		}
		
		for (Leg leg : route.getLegs()) {
			for (Step step : leg.getSteps()) {
				if (step.getManeuver() != null) {
					CoursePoint coursePoint = new CoursePoint("", course);
					coursePoint.setLongitude(step.getStartLocation().getLng());
					coursePoint.setLatitude(step.getStartLocation().getLat());
					
					Maneuver maneuver = Maneuver.lookup(step.getManeuver());
					coursePoint.setType(maneuver.getCoursePointType());
					coursePoint.setName(getDescription(maneuver, step));
					
					course.add(coursePoint);
				}
			}
		}
	}

	private String getDescription(Maneuver maneuver, Step step) {
		if (maneuver == Maneuver.ROUNDABOUT_LEFT || maneuver == Maneuver.ROUNDABOUT_RIGHT) {
			String exit = getExit(step.getHtmlInstructions());
			return Messages.getMessage("maneuver.roundaboutExit", exit);
		} else {
			return maneuver.getDescription();
		}
	}

	private String getExit(String description) {
		final String patternStr = ".*<b>(\\d)\\.ª<\\/b>.*";
		final Pattern pattern = Pattern.compile(patternStr);
		final Matcher matcher = pattern.matcher(description);
		if (matcher.matches()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}

	@Override
	public boolean hasGeocodingSupport() {
		return false;
	}
}
