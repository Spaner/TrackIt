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
package com.trackit.presentation.view.map.provider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.ws.spi.Invoker;

import org.apache.log4j.Logger;
import org.sqlite.SQLiteConfig.TempStore;

import com.trackit.TrackIt;
import com.trackit.business.common.BoundingBox2;
import com.trackit.business.common.Formatters;
import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.business.common.Pair;
import com.trackit.business.domain.Course;
import com.trackit.business.exception.MaxZoomExceededException;
import com.trackit.business.exception.MinZoomExceededException;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.Connection;
import com.trackit.presentation.utilities.ImageUtilities;
import com.trackit.presentation.view.map.MapProviderCatalog;
import com.trackit.presentation.view.map.MapTile;
import com.trackit.presentation.view.map.MapTileGrid;
import com.trackit.presentation.view.map.MapType;
import com.trackit.presentation.view.map.MapsCatalog;
import com.trackit.presentation.view.map.PanDirection;
import com.trackit.presentation.view.map.SphericalMercatorProjection;

public abstract class SphericalMercatorMapProvider implements MapProvider {
	protected static final MapType DEFAULT_MAP_TYPE = MapType.MAP;
	protected static final int DEFAULT_MAP_TILE_GRID_OFFSET = 2;
	protected static final int CACHE_SIZE = 200;

	private SphericalMercatorProjection projection;
	private MapTileGrid mapTileGrid;
	private MapTileCache mapTileCache;
	protected byte zoom;
	protected MapType mapType;
	protected Location centerLocation;
	
	protected MapProviderType mapProviderType;
	protected int minimumZoomLevel;
	protected int maximumZoomLevel;
	protected int defaultZoomLevel;
	protected int tileWidth;
	protected int tileHeight;
	
	protected String tileFileExtension;
	protected String cacheDirectoryName;

	protected static Logger logger = Logger.getLogger(SphericalMercatorMapProvider.class);

	protected static MapsCatalog mapsCatalog = MapsCatalog.getInstance();
	
	public SphericalMercatorMapProvider(MapType mapType, Location centerLocation) {
		this.zoom = getDefaultZoom();
		this.mapType = mapType;
		this.centerLocation = centerLocation;
		projection = new SphericalMercatorProjection(this);
		mapTileCache = new MapTileCache(CACHE_SIZE, this);
	}

	public SphericalMercatorMapProvider (MapProviderType mapProvider, MapType mapType, Location centerLocation) {
		this.mapType         = mapType;
		this.mapProviderType = mapProvider;
		this.centerLocation  = centerLocation;
		projection           = new SphericalMercatorProjection(this);
		mapTileCache         = new MapTileCache(CACHE_SIZE, this);
		cacheDirectoryName   = mapProviderType.getDescription().replaceAll( "\\s", "");
//		System.out.println(mapProviderType + "  " + mapProviderType.getDescription());
		
		MapProviderCatalog mapProviderCatalog = mapsCatalog.getMapProviderCatalog( mapProvider);
		this.zoom        = (byte) mapProviderCatalog.getDefaultZoomLevel( mapType);
		minimumZoomLevel = mapProviderCatalog.getMinimumZoomLevel( mapType);
		maximumZoomLevel = mapProviderCatalog.getMaximumZoomLevel( mapType);
		defaultZoomLevel = this.zoom;
		tileWidth        = mapProviderCatalog.getTileWidth( mapType);
		tileHeight       = mapProviderCatalog.getTileHeight( mapType);
	}
	
	public void fetchTileImage(MapTile tile) {
		SphericalMercatorMapTile mapTile = (SphericalMercatorMapTile) tile;
		BufferedImage bgImage = null;
		BufferedImage tileImage = null;
		try {
			mapTile.setImage( getCachedImageXXX(mapTile));
			if (mapTile.getImage() == null) {
				List<MapRequestTemplate> templates = MapsCatalog.getInstance()
						.getMapRequestTemplates( mapProviderType,  mapTile.getMapType());
				if ( templates != null && ! templates.isEmpty() ) {
					for( MapRequestTemplate template: templates) {
						if ( mapTile.getZoom() >= template.getMinimumZoomLevel() && 
						     mapTile.getZoom() <= template.getMaximumZoomLevel() ) {
							BufferedImage image = getTileImage( template, mapTile);
							if ( tileImage == null )
								tileImage = image;
							else {
								bgImage = image;
								if ( bgImage != null )
									tileImage = ImageUtilities.combineImages( tileImage, bgImage);
							}
						}
					}
					if ( tileImage != null ) {
						mapTile.setImage( filterTileImage(tileImage));
						storeTileImageXXX(   mapTile);
					}
				}
			}
		} catch ( IOException e) {
			e.printStackTrace();
			mapTile.setImage(null);
			return;
		}	
	}
	
	protected BufferedImage filterTileImage( BufferedImage originalBufferedImage) {
		return originalBufferedImage;
	}

	protected BufferedImage getCachedImageXXX(MapTile tile) throws IOException {
//		System.out.println( getTileFilenameXXX( tile));
		File imageFile = new File(getTileFilenameXXX(tile));
		if (imageFile.exists()) {
			return ImageIO.read(imageFile);
		}
		
		return null;
	}
	
	protected void storeTileImageXXX(MapTile tile) throws IOException {
		File imageFile = new File( getTileFilenameXXX(tile));
		imageFile.mkdirs();
		ImageIO.write(tile.getImage(), tileFileExtension, imageFile);
	}

	protected String getTileFilenameXXX( MapTile tile) {
		final String cacheDirectory = TrackIt.getUserCacheLocation() + File.separator + cacheDirectoryName + File.separator;

		SphericalMercatorMapTile mapTile = (SphericalMercatorMapTile) tile;
		
		StringBuilder sb = new StringBuilder();
		sb.append(cacheDirectory).append(mapTile.getZoom()).append(File.separator).append(mapTile.getMapType().getNormalizedName());
		sb.append(File.separator).append(mapTile.getX()).append("_").append(mapTile.getY()).append(".").append( tileFileExtension);
		
		return sb.toString();
	}

	public MapTileGrid getMapTileGrid(long width, long height) {
		MapTile[][] grid = (mapTileGrid != null ? mapTileGrid.getGrid() : null);
		MapTile[][] oldGrid = null;

		if (grid != null) {
			oldGrid = new MapTile[grid.length][];

			for (int i = 0; i < grid.length; i++) {
				oldGrid[i] = new MapTile[grid[i].length];
				System.arraycopy(grid[i], 0, oldGrid[i], 0, grid[0].length);
			}
		}

		if (mapTileGridNeedsInitialisation(width, height)) {
			long widthInTiles = (long) Math.ceil((double) width / getTileWidth()) + DEFAULT_MAP_TILE_GRID_OFFSET;
			long heightInTiles = (long) Math.ceil((double) height / getTileHeight()) + DEFAULT_MAP_TILE_GRID_OFFSET;

			mapTileGrid = new MapTileGrid(widthInTiles, heightInTiles);
		}

		grid = mapTileGrid.getGrid();

		long centerTileX = longitudeToTileX(centerLocation.getLongitude());
		long centerTileY = latitudeToTileY(centerLocation.getLatitude());

		long maxWidthInTiles = (long) Math.pow(2, zoom);
		long maxHeightInTiles = maxWidthInTiles;

		long firstTileX = centerTileX - (grid.length / 2);
		long firstTileY = centerTileY - (grid[0].length / 2);

		SphericalMercatorMapTile mapTile = null;
		int priority;
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				long x = firstTileX + i;
				long y = firstTileY + j;

				if (y < 0 || y >= maxHeightInTiles) {
					grid[i][j] = null;
					continue;
				}

				while (x < 0) {
					x += maxWidthInTiles;
				}
				x %= maxHeightInTiles;

				mapTile = new SphericalMercatorMapTile(x, y, zoom, mapType);
				priority = ((i - (grid.length / 2)) * (i - (grid.length / 2)) + (j - (grid[0].length / 2))
				        * (j - (grid[0].length / 2)));
				priority -= System.currentTimeMillis();
				mapTile.setPriority(priority);

				grid[i][j] = mapTileCache.getTile(mapTile);
			}
		}

		long xOffset = (long) (longitudeToPixelX(centerLocation.getLongitude()) - (tileXToPixelX(centerTileX) + (getTileWidth() / 2)));
		xOffset += (grid.length % 2 == 0) ? (getTileWidth() / 2) : 0;

		long yOffset = (long) (latitudeToPixelY(centerLocation.getLatitude()) - (tileYToPixelY(centerTileY) + (getTileHeight() / 2)));
		yOffset += (grid[0].length % 2 == 0) ? (getTileWidth() / 2) : 0;

		mapTileGrid.setXOffset(xOffset);
		mapTileGrid.setYOffset(yOffset);

		logMapTileGrid(oldGrid, grid);

		return mapTileGrid;
	}

	private void logMapTileGrid(MapTile[][] oldGrid, MapTile[][] grid) {
		boolean toLog = false;

		if ((grid == null && oldGrid != null) || (grid != null && oldGrid == null)) {
			toLog = true;
		} else if (grid.length != oldGrid.length) {
			toLog = true;
		} else if (grid.length > 0 && oldGrid.length > 0 && grid[0].length != oldGrid[0].length) {
			toLog = true;
		} else {
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[i].length; j++) {
					if (grid[i][j] != oldGrid[i][j]) {
						toLog = true;
						break;
					}
				}
			}
		}

		if (!toLog) {
			return;
		}

		if (grid == null || grid.length == 0 || grid[0].length == 0) {
			logger.trace("Empty grid!");
			return;
		}

		logger.trace("");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < grid.length; i++) {
			sb = new StringBuilder();
			for (int j = 0; j < grid[0].length; j++) {
				SphericalMercatorMapTile mapTile = (SphericalMercatorMapTile) grid[i][j];

				if (mapTile != null) {
					sb.append("(").append(i).append(",").append(j).append(")");
					sb.append(" [").append(mapTile.getX()).append(",").append(mapTile.getY()).append("]").append("  ");
				} else {
					sb.append("(").append(i).append(",").append(j).append(")");
					sb.append(" [").append("?").append(",").append("?").append("]").append("  ");
				}
			}
			logger.trace(sb.toString());
		}
	}

	private boolean mapTileGridNeedsInitialisation(long width, long height) {
		if (mapTileGrid == null) {
			return true;
		}

		Pair<Long, Long> mapTileGridTargetSize = getMapTileGridTargetSize(width, height);
		if (mapTileGrid.getWidth() != mapTileGridTargetSize.getFirst()
		        || mapTileGrid.getHeight() != mapTileGridTargetSize.getSecond()) {
			return true;
		}

		return false;
	}

	private Pair<Long, Long> getMapTileGridTargetSize(long width, long height) {
		long targetWidth = (long) Math.ceil(width / getTileWidth()) * getTileWidth()
		        + (DEFAULT_MAP_TILE_GRID_OFFSET * getTileWidth());

		long targetHeight = (long) Math.ceil(height / getTileHeight()) * getTileHeight()
		        + (DEFAULT_MAP_TILE_GRID_OFFSET * getTileHeight());

		return Pair.create(targetWidth, targetHeight);
	}

	public byte getZoom() {
		return zoom;
	}

	@Override
	public byte getMinZoom() {
		return (byte) minimumZoomLevel;
	}

	@Override
	public byte getMaxZoom() {
		return (byte) maximumZoomLevel;
	}

	public byte getDefaultZoom() {
		return (byte) defaultZoomLevel;
	}

	public void increaseZoom(int width, int height) throws MaxZoomExceededException {
		if (zoom >= getMaxZoom()) {
			throw new MaxZoomExceededException();
		}

		zoom++;

		fixCenterLatitudeOutOfBounds(width, height);
	}

	public void decreaseZoom(int width, int height) throws MinZoomExceededException {
		if (zoom <= getMinZoom()) {
			throw new MinZoomExceededException();
		}

		zoom--;

		fixCenterLatitudeOutOfBounds(width, height);
	}

	@Override
	public void pan(PanDirection direction, int numberOfPixels, int width, int height) {
		double centerX = longitudeToPixelX(centerLocation.getLongitude());
		double centerY = latitudeToPixelY(centerLocation.getLatitude());

		switch (direction) {
		case LEFT:
			centerX += numberOfPixels;
			break;
		case RIGHT:
			centerX -= numberOfPixels;
			break;
		case UP:
			centerY -= numberOfPixels;
			break;
		case DOWN:
			centerY += numberOfPixels;
			break;
		default:
			throw new UnsupportedOperationException("Pan in the " + direction + " direction is not supported");
		}

		long maxWidth = (long) Math.pow(2, zoom) * getTileWidth();
		long maxHeight = maxWidth;

		centerY = Math.max(Math.min(centerY, maxHeight), 0);
		while (centerX < 0) {
			centerX += maxWidth;
		}

		centerLocation = new Location(pixelXToLongitude(centerX), pixelYToLatitude(centerY));
		fixCenterLatitudeOutOfBounds(width, height);
	}

	@Override
	public void moveCenterLocation(int xOffset, int yOffset, int width, int height) {
		double centerX = longitudeToPixelX(centerLocation.getLongitude());
		double centerY = latitudeToPixelY(centerLocation.getLatitude());

		centerX += xOffset;
		centerY += yOffset;

		double centerLongitude = pixelXToLongitude(centerX);
		if (centerLongitude > 180.0) {
			centerLongitude -= 360.0;
		} else if (centerLongitude < -180.0) {
			centerLongitude += 360.0;
		}

		double centerLatitude = pixelYToLatitude(centerY);

		centerLocation = new Location(centerLongitude, centerLatitude);

		fixCenterLatitudeOutOfBounds(width, height);
	}

	public void moveCenterLocation(Location location, int width, int height) {
		centerLocation = location;
		fixCenterLatitudeOutOfBounds(width, height);
	}

	public Location getCenterLocation() {
		return centerLocation;
	}

	public Location getLocation(int x, int y, int width, int height) {
		double centerX = longitudeToPixelX(centerLocation.getLongitude());
		double centerY = latitudeToPixelY(centerLocation.getLatitude());

		double xOffset = x - (width / 2.0);
		double yOffset = y - (height / 2.0);

		double pixelX = centerX + xOffset;
		double pixelY = centerY + yOffset;

		double longitude = pixelXToLongitude(pixelX);
		while (longitude < -180.0) {
//			longitude += 180.0;						//12335: 2017-06-20
			longitude += 360.0;
		}
		while (longitude > 180.0) {
//			longitude -= 180.0;						//12335: 2017-06-20
			longitude -= 360.0;
		}
		double latitude = pixelYToLatitude(pixelY);

		return new Location(longitude, latitude);
	}

	public double getGroundResolution(int width) {
		return projection.calculateGroundResolution(centerLocation.getLatitude(), zoom);
	}

	public void setZoom(byte zoom) {
		zoom = (byte) Math.min(zoom, getMaxZoom());
		zoom = (byte) Math.max(zoom, getMinZoom());
		this.zoom = zoom;
	}

	public void setZoom(double groundResolution, int width) {
		byte currentZoom = getMinZoom();
		double currentGroundResolution = Double.MAX_VALUE;

		do {
			currentZoom++;
			currentGroundResolution = projection.calculateGroundResolution(centerLocation.getLatitude(), currentZoom);
		} while (currentZoom <= getMaxZoom() && currentGroundResolution > groundResolution);
		currentZoom--;

		zoom = currentZoom;
	}

	private void fixCenterLatitudeOutOfBounds(int width, int height) {
		long maxHeight = (long) Math.pow(2, zoom) * getTileHeight();
		long screenHeight = height;

		double centerY = latitudeToPixelY(centerLocation.getLatitude());

		if (maxHeight < screenHeight) {
			centerY = maxHeight / 2;
		} else if ((maxHeight - centerY) < (screenHeight / 2)) {
			centerY = maxHeight - (screenHeight / 2);
		} else if (centerY < (screenHeight / 2)) {
			centerY = screenHeight / 2;
		}

		centerLocation.setLatitude(pixelYToLatitude(centerY));
	}

	public void setZoom(int xOffset, int yOffset, int selectionWidth, int selectionHeight, int width, int height) {
		double centerX = longitudeToPixelX(centerLocation.getLongitude());
		double centerY = latitudeToPixelY(centerLocation.getLatitude());

		double newCenterX = centerX + xOffset;
		newCenterX += selectionWidth / 2.0;

		double newCenterY = centerY - yOffset;
		newCenterY += selectionHeight / 2.0;

		centerLocation = new Location(pixelXToLongitude(newCenterX), pixelYToLatitude(newCenterY));

		final double minLongitude = pixelXToLongitude(centerX - xOffset);
		final double maxLongitude = pixelXToLongitude(centerX - xOffset + selectionWidth);
		final double minLatitude = pixelYToLatitude(centerY - yOffset);
		final double maxLatitude = pixelYToLatitude(centerY - yOffset + selectionHeight);

		byte customZoom = getMinZoom();

		int currentWidth;
		int currentHeight;
		do {
			customZoom++;

			currentWidth = (int) Math.abs(projection.longitudeToPixelX(maxLongitude, customZoom)
			        - projection.longitudeToPixelX(minLongitude, customZoom));
			currentHeight = (int) Math.abs(projection.latitudeToPixelY(maxLatitude, customZoom)
			        - projection.latitudeToPixelY(minLatitude, customZoom));
		} while (customZoom <= getMaxZoom() && currentWidth < width && currentHeight < height);
		customZoom--;

		zoom = customZoom;
	}

	@Override
	public void setZoom(BoundingBox2<Location> boundingBox, int width, int height) {
		double minLongitude = boundingBox.getTopLeft().getLongitude();
		double maxLongitude = boundingBox.getTopRight().getLongitude();
		double minLatitude = boundingBox.getBottomLeft().getLatitude();
		double maxLatitude = boundingBox.getTopLeft().getLatitude();
		double centerLongitude = minLongitude + ((maxLongitude - minLongitude) / 2.0);
		double centerLatitude = minLatitude + ((maxLatitude - minLatitude) / 2.0);

		centerLocation = new Location(centerLongitude, centerLatitude);

		byte customZoom = getMaxZoom();
		int currentWidth = Integer.MAX_VALUE;
		int currentHeight = Integer.MAX_VALUE;

		while (customZoom > getMinZoom() && (currentWidth > width || currentHeight > height)) {
			customZoom--;
			currentWidth = (int) Math.abs(projection.longitudeToPixelX(maxLongitude, customZoom)
			        - projection.longitudeToPixelX(minLongitude, customZoom));
			currentHeight = (int) Math.abs(projection.latitudeToPixelY(maxLatitude, customZoom)
			        - projection.latitudeToPixelY(minLatitude, customZoom));
		}

		zoom = (byte) (customZoom > getMinZoom() ? customZoom - 1 : customZoom);
		// zoom = customZoom++;
	}

	public MapType getMapType() {
		return mapType;
	}

	public void setMapType(MapType mapType) {
		this.mapType = mapType;
	}

	public int getTileWidth() {
		return tileWidth;
	}

	@Override
	public int getTileHeight() {
		return tileHeight;
	}

	public Pair<Integer, Integer> getCenterOffsetInPixels(Location location) {
		double firstPixelX = longitudeToPixelX(location.getLongitude());
		double secondPixelX = longitudeToPixelX(centerLocation.getLongitude());
		double firstPixelY = latitudeToPixelY(location.getLatitude());
		double secondPixelY = latitudeToPixelY(centerLocation.getLatitude());

		double xOffset = firstPixelX - secondPixelX;
		double yOffset = firstPixelY - secondPixelY;

		return Pair.create((int) xOffset, (int) yOffset);
	}

	public int[] getCenterOffsetInPixels(double longitude, double latitude) {
		latitude = (latitude > 85.0 ? 85.0 : latitude);
		latitude = (latitude < -85.0 ? -85.0 : latitude);

		double centerX = longitudeToPixelX(centerLocation.getLongitude());
		double centerY = latitudeToPixelY(centerLocation.getLatitude());
		double x = longitudeToPixelX(longitude);
		double y = latitudeToPixelY(latitude);

		double xOffset = x - centerX;
		double yOffset = y - centerY;
		
//		return new int[] { (int) xOffset, (int) yOffset };			/12335: 2017-06-23
		return new int[] { (int) Math.round( xOffset+.5), (int) Math.round( yOffset - .5)};
	}

	public void flushCache() {
		mapTileCache.flush();
	}

	public double longitudeToMetersX(double longitude) {
		return projection.longitudeToMetersX(longitude);
	}

	public double metersXToLongitude(double x) {
		return projection.metersXToLongitude(x);
	}

	public double metersYToLatitude(double y) {
		return projection.metersYToLatitude(y);
	}

	public double latitudeToMetersY(double latitude) {
		return projection.latitudeToMetersY(latitude);
	}

	public double calculateGroundResolution(double latitude) {
		return projection.calculateGroundResolution(latitude, zoom);
	}

	public double latitudeToPixelY(double latitude) {
		return projection.latitudeToPixelY(latitude, zoom);
	}

	public long latitudeToTileY(double latitude) {
		return projection.latitudeToTileY(latitude, zoom);
	}

	public double longitudeToPixelX(double longitude) {
		return projection.longitudeToPixelX(longitude, zoom);
	}

	public long longitudeToTileX(double longitude) {
		return projection.longitudeToTileX(longitude, zoom);
	}

	public double pixelXToLongitude(double pixelX) {
		return projection.pixelXToLongitude(pixelX, zoom);
	}

	public long pixelXToTileX(double pixelX) {
		return projection.pixelXToTileX(pixelX, zoom);
	}

	public double tileXToPixelX(long tileX) {
		return projection.tileXToPixelX(tileX);
	}

	public double tileYToPixelY(long tileY) {
		return projection.tileYToPixelY(tileY);
	}

	public double pixelYToLatitude(double pixelY) {
		return projection.pixelYToLatitude(pixelY, zoom);
	}

	public long pixelYToTileY(double pixelY) {
		return projection.pixelYToTileY(pixelY, zoom);
	}

	public double tileXToLongitude(long tileX) {
		return projection.tileXToLongitude(tileX, zoom);
	}

	//12335: 2018-05-19
	public double tileYToLatitude(long tileY) {
		return projection.tileYToLatitude(tileY, zoom);
	}
	
	//12335: 2018-05-19
	public double getTileCentralLatitude( long tileY) {
		return (tileYToLatitude( tileY) + tileYToLatitude( tileY + 1)) * .5;
	}
	
	//12335: 2018-05-21
	public double getTileCentralLongitude( long tileX) {
		return (tileXToLongitude( tileX) + tileXToLongitude(tileX + 1)) * .5;
	}
	
	public String getTileCentralLocationAsString( long tileX, long tileY) {
//		System.out.println( tileX + "  "+ tileY + "\t\t" + getTileCentralLatitude( tileY) + "  " + getTileCentralLongitude( tileX));
		return Formatters.getDefaultDecimalFormat().format( getTileCentralLatitude( tileY)) + "," +
			   Formatters.getDefaultDecimalFormat().format( getTileCentralLongitude( tileX));
	}

	protected  String composeURL( String template, SphericalMercatorMapTile mapTile) { return null;}
	
	protected BufferedImage getTileImage( MapRequestTemplate template, SphericalMercatorMapTile mapTile) {
		MapProviderCatalog providerCatalog = mapsCatalog.getMapProviderCatalog( template.getProviderType());
		String url = "";
		
		switch ( template.getProviderType() ) {
		case GOOGLE_MAPS:
			url = String.format( template.getUrl(), 
					getTileCentralLocationAsString( mapTile.getX(), mapTile.getY()),
					mapTile.getZoom(), template.getTileWidth(), template.getTileHeight() * 2,
					providerCatalog.getMainKey() );
			break;
		case BING_MAPS:
			url = String.format( template.getUrl(),
					getTileCentralLocationAsString( mapTile.getX(), mapTile.getY()),
					mapTile.getZoom(), String.format( "%d,%d", getTileWidth(), getTileHeight() * 2),
					providerCatalog.getMainKey());
			break;
		case HERE_MAPS:
			url = String.format( template.getUrl(), mapTile.getZoom(), mapTile.getX(), mapTile.getY(), getTileWidth(),
					  			 providerCatalog.getSecondaryKey(), providerCatalog.getMainKey());
			break;
		case OPEN_STREET_MAPS:
			url = String.format( template.getUrl(), mapTile.getZoom(), mapTile.getX(), mapTile.getY());
			break;
		case MAP_BOX_MAPS:
			url = String.format( template.getUrl(), mapTile.getZoom(), mapTile.getX(), mapTile.getY(),
								 providerCatalog.getMainKey());
			break;
		case MAP_QUEST_MAPS:
			url = String.format( template.getUrl(), providerCatalog.getMainKey(), 
					getTileCentralLocationAsString( mapTile.getX(), mapTile.getY()),
					mapTile.getZoom(),
					String.format( "%d,%d", getTileWidth(), getTileHeight() * 2));
			break;
		case ARC_GIS_MAPS:
			url = String.format( template.getUrl(), mapTile.getZoom(), mapTile.getY(), mapTile.getX());
			break;
		case OPEN_MAP_SURFER_MAPS:
			url = String.format( template.getUrl(), mapTile.getX(), mapTile.getY(), mapTile.getZoom());
			break;
		case TRANSAS_MAPS:
			url = String.format( template.getUrl(), mapTile.getZoom(), mapTile.getX(), 
								 (1 << mapTile.getZoom()) - 1 - mapTile.getY(), providerCatalog.getMainKey());
			break;
		case MILITARY_MAPS:
			break;
		default:
			break;
		}
		
		if ( url.isEmpty() )
			return null;
		
//		System.out.println( url);
		BufferedImage image = Connection.getInstance().getImageFromURL(url);
		if ( image != null )
			if ( template.getProviderType().equals( MapProviderType.GOOGLE_MAPS)  ||
				 template.getProviderType().equals( MapProviderType.BING_MAPS)    ||
				 template.getProviderType().equals( MapProviderType.MAP_QUEST_MAPS)  )
				image = image.getSubimage( 0, getTileWidth()/2, getTileWidth(), getTileHeight());
		return image;
	}

	@Override
	public String getName() {
		return mapProviderType.getDescription();
	}

	@Override
	public boolean hasRoutingSupport() {
		return false;
	}

	@Override
	public Course getRoute(Location startLocation, Location endLocation, Map<String, Object> routingOptions)
	        throws TrackItException {
		return null;
	}

	@Override
	public boolean hasGeocodingSupport() {
		return false;
	}

	@Override
	public Location getLocation(String address) {
		return null;
	}
}