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

import javax.imageio.ImageIO;

import com.trackit.TrackIt;
import com.trackit.business.common.Location;
import com.trackit.business.utilities.Connection;
import com.trackit.presentation.view.map.MapTile;
import com.trackit.presentation.view.map.MapType;
import com.trackit.presentation.view.map.provider.MapProvider;
import com.trackit.presentation.view.map.provider.MapProviderType;
import com.trackit.presentation.view.map.provider.SphericalMercatorMapProvider;
import com.trackit.presentation.view.map.provider.SphericalMercatorMapTile;

public class ArcGISMapsProviderOriginal extends SphericalMercatorMapProvider implements MapProvider {
	protected static final int MIN_ZOOM = 0;
	protected static final int MAX_ZOOM = 23;
	protected static final int DEFAULT_ZOOM = 4;
	private static final int TILE_WIDTH = 256;
	private static final int TILE_HEIGHT = 256;

	private static final String mapTypeUrlTemplate = "http://%s/ArcGIS/rest/services/World_Street_Map/MapServer/tile/%d/%d/%d";
	private static final String satelliteTypeUrlTemplate = "http://%s/arcgis/rest/services/World_Imagery/MapServer/tile/%d/%d/%d";
	private static final String terrainTypeUrlTemplate = "http://%s/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/%d/%d/%d";

	public ArcGISMapsProviderOriginal(MapType mapType, Location centerLocation) {
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
					url = String.format(mapTypeUrlTemplate, getServer(), mapTile.getZoom(), mapTile.getY(),
					        mapTile.getX());
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
					url = String.format(terrainTypeUrlTemplate, getServer(), mapTile.getZoom(), mapTile.getY(),
					        mapTile.getX());
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
					url = String.format(satelliteTypeUrlTemplate, "services.arcgisonline.com", mapTile.getZoom(),
					        mapTile.getY(), mapTile.getX());
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
					url = String.format(terrainTypeUrlTemplate, getServer(), mapTile.getZoom(), mapTile.getY(),
					        mapTile.getX());
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
		final String serverName = "server.arcgisonline.com";

		return serverName;
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
		final String cacheDirectory = TrackIt.getUserCacheLocation() + File.separator + "ArcGISMaps" + File.separator;
		final String fileExtension = ".jpg";

		SphericalMercatorMapTile mapTile = (SphericalMercatorMapTile) tile;

		StringBuilder sb = new StringBuilder();
		sb.append(cacheDirectory).append(mapTile.getZoom()).append(File.separator)
		        .append(mapTile.getMapType().getNormalizedName());
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
		return MapProviderType.HERE_MAPS.getDescription();
	}
}