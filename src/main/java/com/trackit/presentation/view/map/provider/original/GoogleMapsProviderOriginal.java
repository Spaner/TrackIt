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
package com.trackit.presentation.view.map.provider.original;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.trackit.TrackIt;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.Connection;
import com.trackit.business.utilities.GooglePolyline;
import com.trackit.presentation.utilities.ImageUtilities;
import com.trackit.presentation.view.map.MapTile;
import com.trackit.presentation.view.map.MapType;
import com.trackit.presentation.view.map.provider.MapProvider;
import com.trackit.presentation.view.map.provider.MapProviderType;
import com.trackit.presentation.view.map.provider.SphericalMercatorMapProvider;
import com.trackit.presentation.view.map.provider.SphericalMercatorMapTile;
import com.trackit.presentation.view.map.provider.bing.elevation.Elevation;
import com.trackit.presentation.view.map.provider.google.routes.Leg;
import com.trackit.presentation.view.map.provider.google.routes.Route;
import com.trackit.presentation.view.map.provider.google.routes.Routes;
import com.trackit.presentation.view.map.provider.google.routes.Step;

public class GoogleMapsProviderOriginal extends SphericalMercatorMapProvider implements MapProvider {
	protected static final int MIN_ZOOM = 0;
	protected static final int MAX_ZOOM = 20;
	protected static final int DEFAULT_ZOOM = 4;
	private static final int TILE_WIDTH = 256;
	private static final int TILE_HEIGHT = 256;
	private Map<String, Object> routingOptions;
	
	public GoogleMapsProviderOriginal(MapType mapType, Location centerLocation) {
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
					url = "http://mt1.google.com/vt/x=" + mapTile.getX() + "&y=" + mapTile.getY() + "&z=" + mapTile.getZoom();
					
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
//					url = "http://khm2.google.com/kh/v=132&x=" + mapTile.getX() + "&y=" + mapTile.getY() + "&z=" + mapTile.getZoom();
					url = "http://khm2.google.com/kh/v=719&x=" + mapTile.getX() + "&y=" + mapTile.getY() + "&z=" + mapTile.getZoom();
					System.out.println( url);
					tileImage = Connection.getInstance().getImageFromURL(url);
					if (tileImage != null) {
						mapTile.setImage(tileImage);
						storeTileImage(mapTile);
					}
				}
				break;
				
			case HYBRID:
				MapTile bgTile = new SphericalMercatorMapTile(mapTile.getX(), mapTile.getY(), mapTile.getZoom(), MapType.SATELLITE);
				MapTile fgTile = new SphericalMercatorMapTile(mapTile.getX(), mapTile.getY(), mapTile.getZoom(), mapTile.getMapType());
				
				bgTile.setImage(getCachedImage(bgTile));
				if (bgTile.getImage() == null) {
//					url = "http://khm2.google.com/kh/v=132&x=" + mapTile.getX() + "&y=" + mapTile.getY() + "&z=" + mapTile.getZoom();
					url = "http://khm2.google.com/kh/v=719&x=" + mapTile.getX() + "&y=" + mapTile.getY() + "&z=" + mapTile.getZoom();
//					logger.debug(url);
					tileImage = Connection.getInstance().getImageFromURL(url);
					if (tileImage != null) {
						bgTile.setImage(tileImage);
						storeTileImage(bgTile);
					}
				}
				
				fgTile.setImage(getCachedImage(mapTile));
				if (fgTile.getImage() == null) {
					url = "http://mt1.google.com/vt/lyrs=h@221000000&x=" + mapTile.getX() + "&y=" + mapTile.getY() + "&z=" + mapTile.getZoom();
//					logger.debug(url);
					tileImage = Connection.getInstance().getImageFromURL(url);
					if (tileImage != null) {
						fgTile.setImage(Connection.getInstance().getImageFromURL(url));
						storeTileImage(fgTile);
					}
				}
				
				if (bgTile.getImage() != null && fgTile.getImage() != null) {
					mapTile.setImage(ImageUtilities.combineImages(bgTile.getImage(), fgTile.getImage()));
				}
				
				break;
				
			case TERRAIN:
				mapTile.setImage(getCachedImage(mapTile));
				if (mapTile.getImage() == null) {
					url = "http://mt1.google.com/vt/v=t@131,r@221000000&x=" + mapTile.getX() + "&y=" + mapTile.getY() + "&z=" + mapTile.getZoom();
					tileImage = Connection.getInstance().getImageFromURL(url);
					if (tileImage != null) {
						mapTile.setImage(tileImage);
						storeTileImage(mapTile);
					}
				}
				break;
				
			default:
				throw new UnsupportedOperationException();
			}
		} catch (IOException e) {
			e.printStackTrace();
			mapTile.setImage(null);
			return;
		}
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
		final String cacheDirectory = TrackIt.getUserCacheLocation() + File.separator + "GoogleMaps" + File.separator;
		final String fileExtension = ".png";

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
					
					ManeuverOriginal maneuver = ManeuverOriginal.lookup(step.getManeuver());
					coursePoint.setType(maneuver.getCoursePointType());
					coursePoint.setName(getDescription(maneuver, step));
					
					course.add(coursePoint);
				}
			}
		}
	}

	private String getDescription(ManeuverOriginal maneuver, Step step) {
		if (maneuver == ManeuverOriginal.ROUNDABOUT_LEFT || maneuver == ManeuverOriginal.ROUNDABOUT_RIGHT) {
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