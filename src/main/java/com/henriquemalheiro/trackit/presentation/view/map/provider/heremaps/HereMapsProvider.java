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
package com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.Connection;
import com.henriquemalheiro.trackit.presentation.view.map.MapTile;
import com.henriquemalheiro.trackit.presentation.view.map.MapType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.Maneuver;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProvider;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProviderType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.SphericalMercatorMapProvider;
import com.henriquemalheiro.trackit.presentation.view.map.provider.SphericalMercatorMapTile;
import com.henriquemalheiro.trackit.presentation.view.map.provider.bingmaps.elevation.Elevation;
import com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.Routes;
import com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculateroute.DirectionType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculateroute.ManeuverType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculateroute.PrivateTransportActionType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculateroute.PrivateTransportManeuverType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculateroute.RouteLegType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.heremaps.routes.routingcalculateroute.RouteType;

public class HereMapsProvider extends SphericalMercatorMapProvider implements MapProvider {
	protected static final int MIN_ZOOM = 0;
	protected static final int MAX_ZOOM = 20;
	protected static final int DEFAULT_ZOOM = 4;
	private static final int TILE_WIDTH = 256;
	private static final int TILE_HEIGHT = 256;
	
	private static final String APP_ID = ""; // FIXME: add APP_ID
	private static final String APP_TOKEN = ""; // FIXME: add APP TOKEN
	private static final String MAP_TYPE = "normal.day";
	private static final String SATELLITE_TYPE = "satellite.day";
	private static final String HYBRID_TYPE = "hybrid.day";
	private static final String TERRAIN_TYPE = "terrain.day";
	
	private static final String mapTypeUrlTemplate = "http://%s/maptile/2.1/maptile/newest/%s/%d/%d/%d/256/png8?lg=ENG&token=%s&requestid=trackit&app_id=%s";
	private static final String satelliteTypeUrlTemplate = "http://%s/maptile/2.1/maptile/newest/%s/%d/%d/%d/256/png8?lg=ENG&token=%s&requestid=trackit&app_id=%s";;
	private static final String hybridTypeUrlTemplate = "http://%s/maptile/2.1/maptile/newest/%s/%d/%d/%d/256/png8?lg=ENG&token=%s&requestid=trackit&app_id=%s";
	private static final String terrainTypeUrlTemplate = "http://%s/maptile/2.1/maptile/newest/%s/%d/%d/%d/256/png8?lg=ENG&token=%s&requestid=trackit&app_id=%s";
	
	private static final Map<DirectionType, Maneuver> directionsMap;
	private static final Map<PrivateTransportActionType, Maneuver> actionsMap;
	private Map<String, Object> routingOptions;
	
	static {
		directionsMap = new HashMap<>();
		directionsMap.put(DirectionType.BEAR_LEFT, Maneuver.KEEP_LEFT);
		directionsMap.put(DirectionType.BEAR_RIGHT, Maneuver.KEEP_RIGHT);
		directionsMap.put(DirectionType.FORWARD, Maneuver.STRAIGHT);
		directionsMap.put(DirectionType.HARD_LEFT, Maneuver.SHARP_LEFT);
		directionsMap.put(DirectionType.HARD_RIGHT, Maneuver.SHARP_RIGHT);
		directionsMap.put(DirectionType.LEFT, Maneuver.LEFT);
		directionsMap.put(DirectionType.LIGHT_LEFT, Maneuver.SLIGHT_LEFT);
		directionsMap.put(DirectionType.LIGHT_RIGHT, Maneuver.SLIGHT_RIGHT);
		directionsMap.put(DirectionType.RIGHT, Maneuver.RIGHT);
		directionsMap.put(DirectionType.U_TURN_LEFT, Maneuver.U_TURN_LEFT);
		directionsMap.put(DirectionType.U_TURN_RIGHT, Maneuver.U_TURN_RIGHT);
		
		actionsMap = new HashMap<>();
		actionsMap.put(PrivateTransportActionType.ARRIVE, Maneuver.ARRIVE);
		actionsMap.put(PrivateTransportActionType.ARRIVE_AIRPORT, Maneuver.ARRIVE);
		actionsMap.put(PrivateTransportActionType.ARRIVE_LEFT, Maneuver.ARRIVE);
		actionsMap.put(PrivateTransportActionType.ARRIVE_RIGHT, Maneuver.ARRIVE);
		actionsMap.put(PrivateTransportActionType.CONTINUE, Maneuver.STRAIGHT);
		actionsMap.put(PrivateTransportActionType.DEPART, Maneuver.DEPART);
		actionsMap.put(PrivateTransportActionType.DEPART_AIRPORT, Maneuver.DEPART);
		actionsMap.put(PrivateTransportActionType.FERRY, Maneuver.FERRY);
		actionsMap.put(PrivateTransportActionType.LEFT_EXIT, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_FORK, Maneuver.FORK_LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_LOOP, Maneuver.U_TURN_LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_MERGE, Maneuver.MERGE);
		actionsMap.put(PrivateTransportActionType.LEFT_RAMP, Maneuver.RAMP_LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_ENTER, Maneuver.ROUNDABOUT_LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_1, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_2, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_3, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_4, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_5, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_6, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_7, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_8, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_9, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_10, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_11, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_EXIT_12, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_ROUNDABOUT_PASS, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_TURN, Maneuver.LEFT);
		actionsMap.put(PrivateTransportActionType.LEFT_U_TURN, Maneuver.U_TURN_LEFT);
		actionsMap.put(PrivateTransportActionType.MIDDLE_FORK, Maneuver.STRAIGHT);
		actionsMap.put(PrivateTransportActionType.NAME_CHANGE, Maneuver.STRAIGHT);
		actionsMap.put(PrivateTransportActionType.NAME_CHANGE, Maneuver.STRAIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_EXIT, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_FORK, Maneuver.FORK_RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_LOOP, Maneuver.U_TURN_RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_MERGE, Maneuver.MERGE);
		actionsMap.put(PrivateTransportActionType.RIGHT_RAMP, Maneuver.RAMP_RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_ENTER, Maneuver.ROUNDABOUT_RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_1, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_2, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_3, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_4, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_5, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_6, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_7, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_8, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_9, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_10, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_11, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_EXIT_12, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_ROUNDABOUT_PASS, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_TURN, Maneuver.RIGHT);
		actionsMap.put(PrivateTransportActionType.RIGHT_U_TURN, Maneuver.U_TURN_RIGHT);
		actionsMap.put(PrivateTransportActionType.SHARP_LEFT_TURN, Maneuver.SHARP_LEFT);
		actionsMap.put(PrivateTransportActionType.SHARP_RIGHT_TURN, Maneuver.SHARP_RIGHT);
		actionsMap.put(PrivateTransportActionType.SLIGHT_LEFT_TURN, Maneuver.SLIGHT_LEFT);
		actionsMap.put(PrivateTransportActionType.SLIGHT_RIGHT_TURN, Maneuver.SLIGHT_RIGHT);
		actionsMap.put(PrivateTransportActionType.TRAFFIC_CIRCLE, Maneuver.STRAIGHT);
	}
	
	public HereMapsProvider(MapType mapType, Location centerLocation) {
		super(mapType, centerLocation);
	}
	
	public int getTileWidth() {
		return TILE_WIDTH;
	}
	
	public int getTileHeight() {
		return TILE_HEIGHT;
	}
	
	public void fetchTileImage(MapTile tile) {
		SphericalMercatorMapTile mapTile = (SphericalMercatorMapTile) tile;
		String url = null;
		BufferedImage tileImage = null;
		
		try {
			switch (mapTile.getMapType()) {
			case MAP:
				mapTile.setImage(getCachedImage(mapTile));

				if (mapTile.getImage() == null) {
					url = String.format(mapTypeUrlTemplate, getServer(), MAP_TYPE, mapTile.getZoom(), mapTile.getX(), mapTile.getY(), APP_TOKEN, APP_ID);
					tileImage = Connection.getInstance().getImageFromURL(url);
					if (tileImage != null) {
						mapTile.setImage(tileImage);
						storeTileImage(mapTile);
					}
				}
				break;
				
			case SATELLITE:
				mapTile.setImage(getCachedImage(mapTile));
				if (mapTile.getImage() == null) {
					url = String.format(satelliteTypeUrlTemplate, getServer(), SATELLITE_TYPE, mapTile.getZoom(), mapTile.getX(), mapTile.getY(), APP_TOKEN, APP_ID);
					tileImage = Connection.getInstance().getImageFromURL(url);
					if (tileImage != null) {
						mapTile.setImage(tileImage);
						storeTileImage(mapTile);
					}
				}
				break;
				
			case HYBRID:
				mapTile.setImage(getCachedImage(mapTile));
				if (mapTile.getImage() == null) {
					url = String.format(hybridTypeUrlTemplate, getServer(), HYBRID_TYPE, mapTile.getZoom(), mapTile.getX(), mapTile.getY(), APP_TOKEN, APP_ID);
					tileImage = Connection.getInstance().getImageFromURL(url);
					if (tileImage != null) {
						mapTile.setImage(tileImage);
						storeTileImage(mapTile);
					}
				}
				break;
				
			case TERRAIN:
				mapTile.setImage(getCachedImage(mapTile));
				if (mapTile.getImage() == null) {
					url = String.format(terrainTypeUrlTemplate, getServer(), TERRAIN_TYPE, mapTile.getZoom(), mapTile.getX(), mapTile.getY(), APP_TOKEN, APP_ID);
					tileImage = Connection.getInstance().getImageFromURL(url);
					if (tileImage != null) {
						mapTile.setImage(tileImage);
						storeTileImage(mapTile);
					}
				}
				break;
				
			default:
				mapTile.setImage(getCachedImage(mapTile));

				if (mapTile.getImage() == null) {
					url = String.format(mapTypeUrlTemplate, getServer(), MAP_TYPE, mapTile.getZoom(), mapTile.getX(), mapTile.getY(), APP_TOKEN, APP_ID);
					tileImage = Connection.getInstance().getImageFromURL(url);
					if (tileImage != null) {
						mapTile.setImage(tileImage);
						storeTileImage(mapTile);
					}
				}
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			mapTile.setImage(null);
			return;
		}
	}
	
	private String getServer() {
		final String serverName = "%d.maps.nlp.nokia.com";
		final int minServerNum = 1;
		final int maxServerNum = 4;
		
		int serverNum = minServerNum + (int)(Math.random() * ((maxServerNum - minServerNum) + 1));
		
		return String.format(serverName, serverNum);
	}
	
	private BufferedImage getCachedImage(MapTile tile) throws IOException {
		File imageFile = new File(getTileFilename(tile));
		if (imageFile.exists()) {
			return ImageIO.read(imageFile);
		}
		
		return null;
	}
	
	private void storeTileImage(MapTile tile) throws IOException {
		File imageFile = new File(getTileFilename(tile));
		imageFile.mkdirs();
		ImageIO.write(tile.getImage(), "png", imageFile);
	}
	
	private String getTileFilename(MapTile tile) {
		final String cacheDirectory = TrackIt.getUserCacheLocation() + File.separator + "HereMaps" + File.separator;
		final String fileExtension = ".jpg";

		SphericalMercatorMapTile mapTile = (SphericalMercatorMapTile) tile;
		
		StringBuilder sb = new StringBuilder();
		sb.append(cacheDirectory).append(mapTile.getZoom()).append(File.separator).append(mapTile.getMapType().getNormalizedName());
		sb.append(File.separator).append(mapTile.getX()).append("_").append(mapTile.getY()).append(fileExtension);
		
		return sb.toString();
	}
	
	@Override
	public byte getMinZoom() {
		return MIN_ZOOM;
	}

	@Override
	public byte getMaxZoom() {
		return MAX_ZOOM;
	}
	
	@Override
	public byte getDefaultZoom() {
		return DEFAULT_ZOOM;
	}
	
	@Override
	public String getName() {
    	return MapProviderType.HERE_MAPS.getDescription();
    }
	
	@Override
	public boolean hasRoutingSupport() {
		return true;
	}

	@Override
	public Course getRoute(Location startLocation, Location endLocation, Map<String, Object> options) throws TrackItException {
		this.routingOptions = options;
		
		Course course = new Course();
		RouteType route = getFirstRoute(startLocation, endLocation);
		
		if (route != null) {
			addTrackpoints(course, route);
			addCoursePoints(course, route);
		}
		
		return course;
	}
	
	private RouteType getFirstRoute(Location startLocation, Location endLocation) throws TrackItException {
		List<RouteType> routes = Routes.getRoutes(startLocation, endLocation, routingOptions);
		
		return (routes != null && !routes.isEmpty() ? routes.get(0) : null);
	}

	private void addTrackpoints(Course course, RouteType route) {
		List<Location> locations = new ArrayList<>();
		List<String> shape = route.getShape();
		
		for (String coordinates : shape) {
			double latitude = Double.parseDouble(coordinates.substring(0, coordinates.indexOf(',')));
			double longitude = Double.parseDouble(coordinates.substring(coordinates.indexOf(',') + 1, coordinates.length()));
			Location location = new Location(longitude, latitude);
			locations.add(location);
		}
		
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

	private void addCoursePoints(Course course, RouteType route) {
		boolean addDirectionCoursePoints = (Boolean) routingOptions.get(Constants.RoutingOptions.ADD_DIRECTION_COURSE_POINTS);
		
		if (!addDirectionCoursePoints) {
			return;
		}
		
		for (RouteLegType leg : route.getLeg()) {
			for (ManeuverType maneuver : leg.getManeuver()) {
				PrivateTransportManeuverType maneuverType = (PrivateTransportManeuverType) maneuver;
				Maneuver maneuver2 = actionsMap.get(maneuverType.getAction());
				if (maneuver2 == Maneuver.DEPART || maneuver2 == Maneuver.ARRIVE) {
					continue;
				}
				
				CoursePoint coursePoint = new CoursePoint("", course);
				coursePoint.setLongitude(maneuverType.getPosition().getLongitude());
				coursePoint.setLatitude(maneuverType.getPosition().getLatitude());
				coursePoint.setType(directionsMap.get(maneuverType.getDirection()).getCoursePointType());
				coursePoint.setName(getDescription(maneuverType.getAction()));

				course.add(coursePoint);
			}
		}
	}

	private String getDescription(PrivateTransportActionType action) {
		if (action.name().toLowerCase().contains("exit")) {
			String exit = getExit(action.name());
			return Messages.getMessage("maneuver.roundaboutExit", exit);
		} else {
			return actionsMap.get(action).getDescription();
		}
	}

	private String getExit(String description) {
		final String patternStr = ".*(\\d)$";
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