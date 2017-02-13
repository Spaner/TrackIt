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
package com.henriquemalheiro.trackit.presentation.view.map.provider;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.exception.MaxZoomExceededException;
import com.henriquemalheiro.trackit.business.exception.MinZoomExceededException;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.business.utility.geo.Datum;
import com.henriquemalheiro.trackit.business.utility.geo.DatumFactory;
import com.henriquemalheiro.trackit.business.utility.geo.Datums;
import com.henriquemalheiro.trackit.business.utility.geo.MolodenskyTransformation;
import com.henriquemalheiro.trackit.presentation.view.map.MapTile;
import com.henriquemalheiro.trackit.presentation.view.map.MapTileGrid;
import com.henriquemalheiro.trackit.presentation.view.map.MapType;
import com.henriquemalheiro.trackit.presentation.view.map.PanDirection;

public class MilitaryMapsProvider implements MapProvider {
	protected static final int MIN_ZOOM = 2;
	protected static final int MAX_ZOOM = 18;
	protected static final int DEFAULT_ZOOM = 5;
	protected static final int CACHE_SIZE = 2000;

	private static int originalTileWidth;
	private static int originalTileHeight;
	private static int subTileRows;
	private static int subTileCols;
	
	private Location centerLocation;
	private byte zoom;
	private MapType mapType;
	private MolodenskyTransformation transformation;
	private MapTileGrid mapTileGrid;
	private MapTileCache mapTileCache;
	private List<Integer> mapsToSplit = new ArrayList<Integer>();
	
	private Logger logger = Logger.getLogger(MilitaryMapsProvider.class.getName());
	
	static {
		String resolutionName = TrackIt.getPreferences().getPreference(
				Constants.PrefsCategories.MAPS, Constants.PrefsSubCategories.MILITARY_MAPS_PROVIDER,
				Constants.MapPreferences.RESOLUTION, MilitaryMapResolution.JPG_5336x3336.toString());
		MilitaryMapResolution resolution = MilitaryMapResolution.lookup(resolutionName);
		
		originalTileWidth = resolution.getWidth();
		originalTileHeight = resolution.getHeight();
		subTileRows = resolution.getRows();
		subTileCols = resolution.getCols();
	}
	
	public MilitaryMapsProvider(MapType mapType, Location centerLocation) {
		if (!centerLocation.getDatum().getDatumType().equals(Datums.WGS84) || centerLocation.isCartesian()) {
			throw new IllegalArgumentException("Center location must be defined in WGS84 geographic coordinates.");
		}
		
		Datum shgmDatum = DatumFactory.getInstance().getDatum(Datums.SHGM);
		transformation = new MolodenskyTransformation(centerLocation.getDatum(), shgmDatum);
		
		this.centerLocation = transformation.transform(centerLocation).toCartesian();
		this.zoom = DEFAULT_ZOOM;
		this.mapType = mapType;
		
		mapTileCache = new MapTileCache(CACHE_SIZE, this);
	}
	
	public static void setResolution(MilitaryMapResolution resolution) {
		originalTileWidth = resolution.getWidth();
		originalTileHeight = resolution.getHeight();
		subTileRows = resolution.getRows();
		subTileCols = resolution.getCols();
	}

	@Override
	public void moveCenterLocation(int xOffset, int yOffset, int width, int height) {
		centerLocation.setLongitude(centerLocation.getLongitude() + pixelsToLongitude(xOffset));
		centerLocation.setLatitude(centerLocation.getLatitude() - pixelsToLatitude(yOffset));
		
		fixCenterLatitudeOutOfBounds();
	}
	
	@Override
	public void moveCenterLocation(Location location, int width, int height) {
		centerLocation = transformation.transform(location).toCartesian();
		
		fixCenterLatitudeOutOfBounds();
	}
	
	private void fixCenterLatitudeOutOfBounds() {
		return;
	}

	@Override
	public Pair<Integer, Integer> getCenterOffsetInPixels(Location location) {
		if (!location.getDatum().getDatumType().equals(Datums.WGS84) || location.isCartesian()) {
			throw new IllegalArgumentException("Center location must be defined in WGS84 geographic coordinates.");
		}
		
		Location newLocation = transformation.transform(location).toCartesian();
		
		return Pair.create((int) (longitudeToPixels(newLocation.getLongitude() - centerLocation.getLongitude())),
				(int) (latitudeToPixels(centerLocation.getLatitude() - newLocation.getLatitude())));
	}

	@Override
	public int[] getCenterOffsetInPixels(double longitude, double latitude) {
		Location location = new Location(longitude, latitude);
		location = transformation.transform(location).toCartesian();
		
		return new int[] { (int) (longitudeToPixels(location.getLongitude() - centerLocation.getLongitude())),
				(int) (latitudeToPixels(centerLocation.getLatitude() - location.getLatitude())) };
	}

	@Override
	public MapTileGrid getMapTileGrid(long width, long height) {
		MapTile[][] grid = (mapTileGrid != null ? mapTileGrid.getGrid() : null);
		
		if (mapTileGridNeedsInitialisation(width, height)) {
			Pair<Integer, Integer> targetSize = getMapTileGridTargetSize(width, height);
			long widthInTiles = targetSize.getFirst();
			long heightInTiles = targetSize.getSecond();
			
			mapTileGrid = new MapTileGrid(widthInTiles, heightInTiles);
		}
		
		grid = mapTileGrid.getGrid();
		if (grid == null || grid.length == 0 || grid[0].length == 0) {
			return mapTileGrid;
		}
		
		/* Find center tile position */
		MilitaryMapsSubTile mapSubTile = getSubTile(centerLocation);
		final int centerX = grid.length / 2;
		final int centerY = grid[0].length / 2;
		
		double subTileCenterLongitude = mapSubTile.getBoundingBox().getTopLeft().getLongitude()
				+ ((mapSubTile.getBoundingBox().getTopRight().getLongitude() - mapSubTile.getBoundingBox().getTopLeft().getLongitude()) / 2.0);
		double longitudeOffset = centerLocation.getLongitude() - subTileCenterLongitude;
		int xOffset = (int) longitudeToPixels(longitudeOffset);
		
		double subTileCenterLatitude = mapSubTile.getBoundingBox().getBottomLeft().getLatitude()
				+ ((mapSubTile.getBoundingBox().getTopLeft().getLatitude() - mapSubTile.getBoundingBox().getBottomLeft().getLatitude()) / 2.0);
		double latitudeOffset = centerLocation.getLatitude() - subTileCenterLatitude;
		int yOffset = (int) latitudeToPixels(latitudeOffset);
		
		
		/* Fill tile grid from top left corner */
		final int subTileWidth = (int) (originalTileWidth / subTileCols * getScale(zoom));
		final int subTileHeight = (int) (originalTileHeight / subTileRows * getScale(zoom));
		
		double initialLongitude = subTileCenterLongitude - pixelsToLongitude(centerX * subTileWidth);
		double initialLatitude = subTileCenterLatitude + pixelsToLatitude(centerY * subTileHeight);
		
		int priority = 0;
		for (int i = 0; i < mapTileGrid.getHeight(); i++) {
			for (int j = 0; j < mapTileGrid.getWidth(); j++) {
				double longitude = initialLongitude + (j * pixelsToLongitude(subTileWidth));
				double latitude = initialLatitude - (i * pixelsToLatitude(subTileHeight));
				Location subTileLeftCorner = new Location(longitude, latitude, DatumFactory.getInstance().getDatum(Datums.SHGM), Location.CARTESIAN);
				mapSubTile = getSubTile(subTileLeftCorner);
				
				if (mapSubTile == null) {
					grid[j][i] = null;
					continue;
				}
				
				priority = ((j - ((int) mapTileGrid.getWidth() / 2)) * (j - ((int) mapTileGrid.getWidth() / 2))
						+ (i - ((int) mapTileGrid.getHeight() / 2)) * (i - ((int) mapTileGrid.getHeight() / 2)));
				mapSubTile.setPriority(priority);
				
				grid[j][i] = mapTileCache.getTile(mapSubTile);
			}
		}
		
		mapTileGrid.setXOffset(xOffset);
		mapTileGrid.setYOffset(-yOffset);
		
		return mapTileGrid;
	}
	
	private double longitudeToPixels(double longitude) {
		return (longitude * originalTileWidth / map25Longitude * getScale(zoom));
	}
	
	private double latitudeToPixels(double latitude) {
		return (latitude * originalTileHeight / map25Latitude * getScale(zoom));
	}
	
	private double pixelsToLongitude(double pixels) {
		return (map25Longitude / getScale(zoom) * pixels / originalTileWidth);
	}
	
	private double pixelsToLatitude(double pixels) {
		return (map25Latitude / getScale(zoom) * pixels / originalTileHeight);
	}
	
	private boolean mapTileGridNeedsInitialisation(long width, long height) {
		if (mapTileGrid == null) {
			return true;
		}
		
		Pair<Integer, Integer> mapTileGridTargetSize = getMapTileGridTargetSize(width, height);
		if (mapTileGrid.getWidth() != mapTileGridTargetSize.getFirst()
				|| mapTileGrid.getHeight() != mapTileGridTargetSize.getSecond()) {
			return true;
		}
		
		return false;
	}
	
	private Pair<Integer, Integer> getMapTileGridTargetSize(long width, long height) {
		final double subTileWidthInPixels = (originalTileWidth / subTileCols * getScale(zoom));
		final double subTileHeightInPixels = (originalTileHeight / subTileRows * getScale(zoom));
		
		int gridWidth = (int) Math.ceil(width / subTileWidthInPixels) + 10;
		int gridHeight = (int) Math.ceil(height / subTileHeightInPixels) + 10;
		
		gridWidth += (gridWidth % 2 == 0 ? 1 : 0);
		gridHeight += (gridHeight % 2 == 0 ? 1 : 0);
		logger.trace(String.format("Grid size: %d", gridWidth * gridHeight));
		return Pair.create(gridWidth, gridHeight);
	}
	
	@Override
	public Location getCenterLocation() {
		Datum shgmDatum = DatumFactory.getInstance().getDatum(Datums.SHGM);
		Datum wgs84Datum = DatumFactory.getInstance().getDatum(Datums.WGS84);
		MolodenskyTransformation mTransformation = new MolodenskyTransformation(shgmDatum, wgs84Datum);
		
		Location wgs84CenterLocation = mTransformation.transform(centerLocation.toGeographic());
		
		return wgs84CenterLocation;
	}
	
	@Override
	public Location getLocation(int screenX, int screenY, int width, int height) {
		Datum shgmDatum = DatumFactory.getInstance().getDatum(Datums.SHGM);
		Datum wgs84Datum = DatumFactory.getInstance().getDatum(Datums.WGS84);
		
		double xOffset = screenX - (width / 2.0);
		double yOffset = screenY - (height / 2.0);
		
		double longitude = centerLocation.getLongitude() + pixelsToLongitude(xOffset);
		double latitude = centerLocation.getLatitude() - pixelsToLatitude(yOffset);
		
		Location currentLocation = new Location(longitude, latitude, shgmDatum, Location.CARTESIAN);

		MolodenskyTransformation mTransformation = new MolodenskyTransformation(shgmDatum, wgs84Datum);
		Location wgs84CurrentLocation = mTransformation.transform(currentLocation.toGeographic());

		return wgs84CurrentLocation;
	}
	
	@Override
	public double getGroundResolution(int width) {
		return getGroundResolution(zoom, width);
	}
	
	@Override
	public void setZoom(byte zoom) {
		this.zoom = zoom;
	}
	
	private double getGroundResolution(byte zoom, int width) {
		return ((map25Longitude / getScale(zoom) * width / originalTileWidth)) / width;
	}
	
	@Override
	public void setZoom(double groundResolution, int width) {
		byte currentZoom = MIN_ZOOM;
		double currentGroundResolution = getGroundResolution(currentZoom, width);
		
		while (currentZoom < MAX_ZOOM && currentGroundResolution > groundResolution) {
			currentZoom++;
			currentGroundResolution = getGroundResolution(currentZoom, width);
		}
		
		if (currentZoom > MIN_ZOOM) {
			currentZoom++;
		}
		
		zoom = currentZoom;
	}

	@Override
	public byte getZoom() {
		return zoom;
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
	public void increaseZoom(int width, int height) throws MaxZoomExceededException {
		if (zoom >= MAX_ZOOM) {
			throw new MaxZoomExceededException();
		}
		
		zoom++;
		fixCenterLatitudeOutOfBounds();
	}

	@Override
	public void decreaseZoom(int width, int height) throws MinZoomExceededException {
		if (zoom <= MIN_ZOOM) {
			throw new MinZoomExceededException();
		}
		
		zoom--;
		fixCenterLatitudeOutOfBounds();
	}

	@Override
	public void setZoom(int x, int y, int selectionWidth, int selectionHeight, int width, int height) {
		double centerX = longitudeToPixels(centerLocation.getLongitude());
		double centerY = latitudeToPixels(centerLocation.getLatitude());
		
		double newCenterX = centerX + x;
		newCenterX += selectionWidth / 2.0;
		
		double newCenterY = centerY + y;
		newCenterY -= selectionHeight / 2.0;
		
		centerLocation.setLongitude(pixelsToLongitude(newCenterX));
		centerLocation.setLatitude(pixelsToLatitude(newCenterY));
		
		final double minLongitude = pixelsToLongitude((int) centerX - x);
		final double maxLongitude = pixelsToLongitude((int) centerX - x + selectionWidth);
		final double minLatitude = pixelsToLatitude((int) centerY - y);
		final double maxLatitude = pixelsToLatitude((int) centerY - y + selectionHeight);

		zoom = MIN_ZOOM;
		
		int currentWidth;
		int currentHeight;
		do {
			zoom++;

			currentWidth = (int) Math.abs(longitudeToPixels(maxLongitude) - longitudeToPixels(minLongitude));
			currentHeight = (int) Math.abs(latitudeToPixels(maxLatitude) - latitudeToPixels(minLatitude));
		} while (zoom < MAX_ZOOM && currentWidth < width && currentHeight < height);
		zoom--;
		
		mapTileCache.flush();
	}
	
	@Override
	public void setZoom(BoundingBox2<Location> boundingBox, int width, int height) {
		Location topLeft = transformation.transform(boundingBox.getTopLeft()).toCartesian();
		Location topRight = transformation.transform(boundingBox.getTopRight()).toCartesian();
		Location bottomLeft = transformation.transform(boundingBox.getBottomLeft()).toCartesian();
		
		final double minLongitude = topLeft.getLongitude();
		final double maxLongitude = topRight.getLongitude();
		final double minLatitude = bottomLeft.getLatitude();
		final double maxLatitude = topLeft.getLatitude();
		
		if (minLongitude < 72000.0
				|| minLongitude > 216000.0
				|| maxLongitude < 72000.0
				|| maxLongitude > 216000.0
				|| minLatitude < -20000.0
				|| minLatitude > 470000.0
				|| maxLatitude < -20000.0
				|| maxLatitude > 470000.0) {
			return;
		}

		centerLocation.setLongitude(minLongitude + ((maxLongitude - minLongitude) / 2.0));
		centerLocation.setLatitude(minLatitude + ((maxLatitude - minLatitude) / 2.0));

		zoom = MIN_ZOOM;
		int currentWidth;
		int currentHeight;
		do {
			zoom++;

			currentWidth = (int) Math.abs(longitudeToPixels(maxLongitude) - longitudeToPixels(minLongitude));
			currentHeight = (int) Math.abs(latitudeToPixels(maxLatitude) - latitudeToPixels(minLatitude));
		} while (zoom < MAX_ZOOM && currentWidth < width && currentHeight < height);
		zoom--;
		zoom = (byte) Math.max(zoom, MIN_ZOOM);
		
		mapTileCache.flush();
	}

	@Override
	public void pan(PanDirection direction, int numberOfPixels, int width, int height) {
		switch (direction) {
		case LEFT:
			centerLocation.setLongitude(centerLocation.getLongitude() - pixelsToLongitude(numberOfPixels));
			break;
		case RIGHT:
			centerLocation.setLongitude(centerLocation.getLongitude() + pixelsToLongitude(numberOfPixels));
			break;
		case UP:
			centerLocation.setLatitude(centerLocation.getLatitude() + pixelsToLatitude(numberOfPixels));
			break;
		case DOWN:
			centerLocation.setLatitude(centerLocation.getLatitude() - pixelsToLatitude(numberOfPixels));
			break;
		default:
			throw new UnsupportedOperationException("Pan in the " + direction + " direction is not supported");
		}
	}

	@Override
	public MapType getMapType() {
		return mapType;
	}

	@Override
	public void setMapType(MapType mapType) {
		logger.warn("Military maps only support map view.");
		this.mapType = mapType;
	}
	
	@Override
	public int getTileWidth() {
		return (int) (originalTileWidth / subTileCols * getScale(zoom));
	}

	@Override
	public int getTileHeight() {
		return (int) (originalTileHeight / subTileRows * getScale(zoom));
	}
	
	private int getTileWidth(byte zoom) {
		return (int) (originalTileWidth / subTileCols * getScale(zoom));
	}

	private int getTileHeight(byte zoom) {
		return (int) (originalTileHeight / subTileRows * getScale(zoom));
	}
	
	public synchronized void fetchTileImage(final MapTile mapTile) {
		MilitaryMapsSubTile tile = (MilitaryMapsSubTile) mapTile;
		
		try {
			BufferedImage subTileImage = getCachedSubTileImage(tile);
			
			if (subTileImage == null && tile.getNumber() != 0 && !mapsToSplit.contains(tile.getNumber())) {
				int tileNumber = tile.getNumber();
				mapsToSplit.add(tileNumber);
				
				mapTileCache.flush();
				System.gc();
				
				splitTileInSubTiles(tile);
				subTileImage = getCachedSubTileImage(tile);
			}
			
			if (getScale(mapTile.getZoom()) != 1.0 && subTileImage != null) {
				BufferedImage bi = new BufferedImage(getTileWidth(mapTile.getZoom()), getTileHeight(mapTile.getZoom()), BufferedImage.TYPE_INT_ARGB);
				Graphics2D grph = (Graphics2D) bi.getGraphics();
				grph.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				grph.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				grph.scale(getScale(mapTile.getZoom()), getScale(mapTile.getZoom()));
				grph.drawImage(subTileImage, 0, 0, null);
				grph.dispose();
				subTileImage.flush();
				subTileImage = bi;
				bi = null;
			}
			
			mapTile.setImage(subTileImage);
			
		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
					getMessage("militaryMapsProvider.mapNotFound", tile.getNumber()),
					getMessage("militaryMapsProvider.warning"), JOptionPane.WARNING_MESSAGE);
		
		} catch (IOException e) {
			logger.error("Couldn't get sub tiles from tile ", e);
		}
	}

	private double getScale(byte zoom) {
		if (zoom < MIN_ZOOM || zoom > MAX_ZOOM) {
			return 1.0;
		} else {
			return (zoom * 1.0 / 10);
		}
	}
		
	private MilitaryMapsSubTile getSubTile(Location location) {
		int i250Map = getMap250Number(portugalZone, location.getLongitude(), location.getLatitude());
        
        if (i250Map > 0) {
            int is = i250Map - 1;
            double xoff = location.getLongitude() - xOrigin[is];
            double yoff = location.getLatitude() - yOrigin[is];
            int idx = (int) ((15 - ((int) (yoff / map25Latitude))) * 8 + xoff / map25Longitude);
            
            int mapNumber = map25Numbers[is][idx];
            char mapLetter = getMapLetter(i250Map - 1, mapNumber, location.getLongitude());
            
            int rowNum = (int) Math.ceil((idx + 1) / 8.0);
            int colNum = (idx + 1) % 8;
            colNum = (colNum == 0 ? 8 : colNum);
            
            final double subTileLongitude = map25Longitude / subTileCols;
            final double subTileLatitude = map25Latitude / subTileRows;
            
            double minLongitude = xOrigin[is] + ((colNum - 1) * map25Longitude);
            int x = (int) ((location.getLongitude() - minLongitude) / subTileLongitude);
            minLongitude += (x * subTileLongitude);
            
            double maxLongitude = minLongitude + subTileLongitude;
            
            double minLatitude = yOrigin[is] + ((16 - rowNum) * map25Latitude);
            int y = (int) ((location.getLatitude() - minLatitude) / subTileLatitude);
            minLatitude += (y * subTileLatitude);
            
            if (x < 0 || x > subTileCols || y < 0 || y > subTileRows) {
            	return null;
            }
            
            double maxLatitude = minLatitude + subTileLatitude;
            
            Location topLeft = new Location(minLongitude, maxLatitude);
            Location topRight = new Location(maxLongitude, maxLatitude);
            Location bottomRight = new Location(maxLongitude, minLatitude);
            Location bottomLeft = new Location(minLongitude, minLatitude);
            
            BoundingBox2<Location> tileBoundingBox = new BoundingBox2<Location>(topLeft, topRight, bottomRight, bottomLeft);
            
            MilitaryMapsSubTile subTile = new MilitaryMapsSubTile(mapNumber, mapLetter, x + 1, subTileRows - y, tileBoundingBox, zoom, MapType.MAP);

            return subTile;
        }
        
        return null;
	}

    private int getMap250Number(int zone, double longitude, double latitude) {
        if (zone == portugalZone && latitude >= 0.0 && latitude < 580000.0) {
            int iy = 3 - (int) ((latitude + 20000.0) / map250Latitude);
            int iy2 = iy + iy;
            
            if (longitude >= xOrigin[iy2] && longitude < (xOrigin[iy2] + 2.0 * map250Longitude)) {
                int imap = iy2 + 1;
                if (longitude >= (xOrigin[iy2] + map250Longitude)) {
                    imap++;
                }
                
                return imap;
            }
        }
        
        return 0;
    }

	private char getMapLetter(int groupNumber, int mapNumber, double longitude) {
        char letter = ' ';
        if (mapNumber < 0) {
            switch (mapNumber) {
                case -9:
                	letter = 'a';
                    break;
                case -67:
                case -108:
                case -142:
                case -162:
                case -227:
                case -238:
                case -401:
                case -483:
                case -515:
                case -525:
                case -583:
                	letter = 'A';
                    break;
                case -248:
                    if (groupNumber == 3) {
                    	letter = 'A';
                    } else {
                    	letter = 'B';
                    }
                    break;
                case -306:
                    if (groupNumber == 3) {
                    	letter = 'A';
                    } else {
                    	letter = 'B';
                    }
                    break;
                case -441:
                    if (groupNumber == 5) {
                    	letter = 'A';
                    } else {
                    	letter = 'B';
                    }
                    break;
                case -315:
                    if (longitude < 280000.0) {
                    	letter = 'A';
                    } else {
                    	letter = 'B';
                    }
                    break;
                case -325:
                    if (groupNumber == 5) {
                    	letter = 'A';
                    } else if (longitude >= 88000.0) {
                    	letter = 'C';
                    } else {
                    	letter = 'B';
                    }
                    break;
                default:
            }
        }
        
        return letter;
    }
	
    private static final int portugalZone = 29; 
    private static final double xOrigin[] = { 136000.0, 264000.0, 72000.0, 200000.0, 72000.0, 200000.0, 72000.0, 200000.0};
    private static final double yOrigin[] = { 460000.0, 460000.0, 300000.0, 300000.0, 140000.0, 140000.0, -20000.0, -20000.0};
    
    private static final double map25Longitude = 16000.0;
    private static final double map25Latitude = 10000.0;
    private static final double map250Longitude = 128000.0;
    private static final double map250Latitude = 160000.0;

    private static final int[][] map25Numbers = {
    	
        // Section 1
        {  0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   1,   0,   0,   0,   0,
           0,   2,   3,   4,   5,   0,   0,   0,
           6,   7,   8,   9,   0,   0,   0,   0,
          14,  15,  16,  17,  18,  19,  20,  21,
          27,  28,  29,  30,  31,  32,  33,  34,
          40,  41,  42,  43,  44,  45,  46,  47,
          54,  55,  56,  57,  58,  59,  60,  61,
          68,  69,  70,  71,  72,  73,  74,  75,
          82,  83,  84,  85,  86,  87,  88,  89,
          96,  97,  98,  99, 100, 101, 102, 103,
         109, 110, 111, 112, 113, 114, 115, 116,
           0, 122, 123, 124, 125, 126, 127, 128},

        // Section 2
        {  0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,
          -9,  10,  11,  12,  13,   0,   0,   0,
          22,  23,  24,  25,  26,   0,   0,   0,
          35,  36,  37,  38,  39,   0,   0,   0,
          48,  49,  50,  51,  52,  53,   0,   0,
          62,  63,  64,  65,  66,  67, -67,   0,
          76,  77,  78,  79,  80,  81,   0,   0,
          90,  91,  92,  93,  94,  95,   0,   0,
         104, 105, 106, 107, 108,-108,   0,   0,
         117, 118, 119, 120, 121,   0,   0,   0,
         129, 130, 131, 132,   0,   0,   0,   0},

        // Section 3
        {0,   0,   0,   0,   0, 133, 134, 135,
         0,   0,   0,   0,   0, 143, 144, 145,
         0,   0,   0,   0,   0, 153, 154, 155,
         0,   0,   0,   0,-162, 163, 164, 165,
         0,   0,   0,   0, 173, 174, 175, 176,
         0,   0,   0,   0, 184, 185, 186, 187,
         0,   0,   0,   0, 195, 196, 197, 198,
         0,   0,   0,   0, 206, 207, 208, 209, 
         0,   0,   0,   0, 217, 218, 219, 220,
         0,   0,   0,   0, 228, 229, 230, 231,
         0,   0,   0,-238, 239, 240, 241, 242,
         0,   0,   0,-248, 249, 250, 251, 252,
         0,   0,   0, 260, 261, 262, 263, 264,
         0,   0,   0, 272, 273, 274, 275, 276,
         0,   0,   0, 284, 285, 286, 287, 288,
         0,   0,   0, 296, 297, 298, 299, 300},

        // Section 4
        {136, 137, 138, 139, 140, 141, 142,-142,
         146, 147, 148, 149, 150, 151, 152,   0,
         156, 157, 158, 159, 160, 161, 162,   0,
         166, 167, 168, 169, 170, 171, 172,   0,
         177, 178, 179, 180, 181, 182, 183,   0,
         188, 189, 190, 191, 192, 193, 194,   0,
         199, 200, 201, 202, 203, 204, 205,   0,
         210, 211, 212, 213, 214, 215, 216,   0,
         221, 222, 223, 224, 225, 226, 227,-227,
         232, 233, 234, 235, 236, 237, 238,   0,
         243, 244, 245, 246, 247, 248,   0,   0,
         253, 254, 255, 256, 257, 258, 259,   0,
         265, 266, 267, 268, 269, 270, 271,   0,
         277, 278, 279, 280, 281, 282, 283,   0,
         289, 290, 291, 292, 293, 294, 295,   0,
         301, 302, 303, 304, 305, 306,-306,   0},

        // Section 5
        {   0,   0,-306, 307, 308, 309, 310, 311,
            0,   0, 316, 317, 318, 319, 320, 321,
         -325,-325, 326, 327, 328, 329, 330, 331,
            0, 337, 338, 339, 340, 341, 342, 343,
            0, 349, 350, 351, 352, 353, 354, 355,
            0, 361, 362, 363, 364, 365, 366, 367,
            0, 374, 375, 376, 377, 378, 379, 380,
            0, 388, 389, 390, 391, 392, 393, 394,
         -401, 402, 403, 404, 405, 406, 407, 408,
          415, 416, 417, 418, 419, 420, 421, 422,
          429, 430, 431, 432, 433, 434, 435, 436,
            0,-441, 442, 443, 444, 445, 446, 447,
            0,   0, 453, 454, 455, 456, 457, 458,
            0,   0, 464, 465, 466, 467, 468, 469,
            0,   0,   0,   0, 475, 476, 477, 478,
            0,   0,   0,   0, 484, 485, 486, 487},

        // Section 6
        {312, 313, 314, 315,-315,-315,   0,   0,
         322, 323, 324, 325,-325,   0,   0,   0,
         332, 333, 334, 335, 336,   0,   0,   0,
         344, 345, 346, 347, 348,   0,   0,   0,
         356, 357, 358, 359, 360,   0,   0,   0,
         368, 369, 370, 371, 372, 373,   0,   0,
         381, 382, 383, 384, 385, 386, 387,   0,
         395, 396, 397, 398, 399, 400, 401,   0,
         409, 410, 411, 412, 413, 414,   0,   0,
         423, 424, 425, 426, 427, 428,   0,   0,
         437, 438, 439, 440, 441,-441,   0,   0,
         448, 449, 450, 451, 452,   0,   0,   0,
         459, 460, 461, 462, 463,   0,   0,   0,
         470, 471, 472, 473, 474,   0,   0,   0,
         479, 480, 481, 482, 483,-483,   0,   0,
         488, 489, 490, 491, 492, 493,   0,   0},

        // Section 7
        {0,   0,   0,   0, 494, 495, 496, 497,
         0,   0,   0,   0, 505, 506, 507, 508,
         0,   0,   0,-515, 516, 517, 518, 519,
         0,   0,   0,   0, 526, 527, 528, 529,
         0,   0,   0,   0, 535, 536, 537, 538,
         0,   0,   0,   0, 544, 545, 546, 547,
         0,   0,   0,   0, 552, 553, 554, 555,
         0,   0,   0,   0, 560, 561, 562, 563,
         0,   0,   0,   0, 568, 569, 570, 571,
         0,   0,   0,   0, 576, 577, 578, 579,
         0,   0,   0,-583, 584, 585, 586, 587,
         0,   0,   0, 592, 593, 594, 595, 596,
         0,   0,   0, 601, 602, 603, 604, 605,
         0,   0,   0, 609,	 0,   0,   0,   0,
         0,   0,   0,   0,	 0,   0,   0,   0,
         0,   0,   0,   0,	 0,   0,   0,   0},

        // Section 8
        {498, 499, 500, 501, 502, 503, 504,   0,
         509, 510, 511, 512, 513, 514, 515,   0,
         520, 521, 522, 523, 524, 525,-525,   0,
         530, 531, 532, 533, 534,   0,   0,   0,
         539, 540, 541, 542, 543,   0,   0,   0,
         548, 549, 550, 551,   0,   0,   0,   0,
         556, 557, 558, 559,   0,   0,   0,   0,
         564, 565, 566, 567,   0,   0,   0,   0,
         572, 573, 574, 575,   0,   0,   0,   0,
         580, 581, 582, 583,   0,   0,   0,   0,
         588, 589, 590, 591,   0,   0,   0,   0,
         597, 598, 599, 600,   0,   0,   0,   0,
         606, 607, 608,   0,   0,   0,   0,   0,
         610, 611, 612,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0}
    };
    
    private String getTileFilename(MilitaryMapsTile tile) throws FileNotFoundException {
    	final String rootPath = TrackIt.getPreferences().getPreference(Constants.PrefsCategories.MAPS,
    			Constants.PrefsSubCategories.MILITARY_MAPS_PROVIDER, Constants.MapPreferences.MILITARY_MAPS_MAP25K_LOCATION, null);
    	final String fileExtension = ".jpg";
    	
    	if (rootPath == null) {
    		throw new FileNotFoundException("Military Maps 25k location not defined!");
    	}
		
		StringBuilder filename = new StringBuilder();
		filename.append(rootPath).append(File.separator);
		
		filename.append(Utilities.pad(String.valueOf(Math.abs(tile.getNumber())), '0', 3, Utilities.LEFT_PAD));

		if (tile.getLetter() != ' ') {
			filename.append(tile.getLetter());
		}
		
		filename.append(fileExtension);
		
		File file = new File(filename.toString());
		if (!file.exists()) {
			throw new FileNotFoundException("Military Maps 25k location not defined!");
		}
		
		return filename.toString();
	}
	
	private String getSubTileFilename(MilitaryMapsSubTile tile) {
		final String ROOT_PATH = TrackIt.getUserCacheLocation() + File.separator + "MilitaryMaps" + File.separator;
		final String FILE_EXTENSION = ".jpg";
		
		StringBuilder filename = new StringBuilder();
		filename.append(ROOT_PATH);
		
		if (tile.getLetter() != ' ') {
			filename.append(Math.abs(tile.getNumber())).append(tile.getLetter());
		} else {
			filename.append(tile.getNumber());
		}
		
		filename.append(File.separator).append(Utilities.pad(String.valueOf(tile.getX()), '0', 2, Utilities.LEFT_PAD));
		filename.append("_").append(Utilities.pad(String.valueOf(tile.getY()), '0', 2, Utilities.LEFT_PAD));
		filename.append(FILE_EXTENSION);
		
		return filename.toString();
	}
    
    private BufferedImage getCachedSubTileImage(MilitaryMapsSubTile tile) throws IOException {
		File imageFile = new File(getSubTileFilename(tile));
		if (imageFile.exists()) {
			return ImageIO.read(imageFile);
		} else {
			return null;
		}
	}
	
    public void splitTileInSubTiles(MilitaryMapsSubTile tile) throws IOException {
    	final String tileId = String.valueOf(Math.abs(tile.getNumber())) + (tile.getLetter() != ' ' ? tile.getLetter() : "");
    	
    	ProgressMonitor progressMonitor = new ProgressMonitor(TrackIt.getApplicationFrame(), "Splitting map " + tileId + " in tiles...",
    			"", 0, 100);
    	int progress = 0;
    	progressMonitor.setProgress(progress);
    	
		try {
			logger.info("Splitting map " + tileId + " in tiles...");
			
			File file = new File(getTileFilename(tile));
			URL imageLocation = file.toURI().toURL();
			
			progressMonitor.setNote("Reading map...");
			BufferedImage tileImage = ImageIO.read(imageLocation);
			progress += 10;
			progressMonitor.setProgress(progress);

			int rows = subTileRows;  
			int cols = subTileCols;  

			int subTileWidth = tileImage.getWidth() / cols;  
			int subTileHeight = tileImage.getHeight() / rows;
			
			final int step = 90 / rows;
			
			BufferedImage img = null;
			MilitaryMapsSubTile splitSubTile = null;
			for (int y = 1; y <= rows; y++) {  
				for (int x = 1; x <= cols; x++) {  
					progressMonitor.setNote("Splitting tile (" + x + ", " + y + ")...");
					img = new BufferedImage(subTileWidth, subTileHeight, tileImage.getType());  

					Graphics2D gr = img.createGraphics();
					gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					gr.drawImage(tileImage, 0, 0, subTileWidth, subTileHeight, subTileWidth * (x - 1), subTileHeight * (y - 1),
							subTileWidth * (x - 1) + subTileWidth, subTileHeight * (y - 1) + subTileHeight, null);  
					gr.dispose();
					
					splitSubTile = new MilitaryMapsSubTile(tile, x, y);
					file = new File(getSubTileFilename(splitSubTile));
					file.mkdirs();
					ImageIO.write(img, "jpg", file);
					img.flush();
					img = null;
				}
				
				if (progressMonitor.isCanceled()) {
					logger.info("Splitting of map " + tileId + " cancelled!");
					
					File parent = file.getParentFile();
					for (File subTileFile : parent.listFiles()) {
						subTileFile.delete();
					}
					parent.delete();
					
					progressMonitor.setProgress(101);
					return;
				}
				
				progress += step;
				progressMonitor.setProgress(progress);
			}
			progressMonitor.setProgress(101);
			progressMonitor.setNote("Map successfully splitted in tiles.");
			
			logger.info("Splitting of map " + tileId + " complete!");
		} catch (FileNotFoundException fnfe) {
			logger.debug(getMessage("militaryMapsProvider.mapNotFound", tileId));
			progressMonitor.setProgress(101);
			
			throw fnfe;
		} catch (IOException ioe) {
			logger.error("Error splitting map in tiles: " + ioe.getMessage(), ioe);
			progressMonitor.setProgress(101);
			
			throw ioe;
		}
    }
    
    @Override
    public void flushCache() {
		mapTileCache.flush();
	}
    
    @Override
    public String getName() {
    	return MapProviderType.MILITARY_MAPS.getDescription();
    }

	@Override
	public boolean hasRoutingSupport() {
		return false;
	}

	@Override
	public Course getRoute(Location startLocation, Location endLocation, Map<String, Object> routingOptions) {
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