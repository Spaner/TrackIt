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
package com.henriquemalheiro.trackit.presentation.view.map.layer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.ImageObserver;
import java.net.URL;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.MapTile;
import com.henriquemalheiro.trackit.presentation.view.map.MapTileGrid;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProvider;


public class BackgroundMapLayer extends MapLayer implements ImageObserver {
	private static final long serialVersionUID = 1L;
	
	private static ImageIcon spinnerIcon;
	private static ImageIcon warningIcon;
	
	private boolean isSpinning = false;

	public BackgroundMapLayer(Map map) {
		super(map);
		init();
	}
	
	private void init() {
		setOpaque(true);
		setDoubleBuffered(true);
		initIcons();
		
		map.refresh();
	}
	
	private void initIcons() {
		URL iconLocation = BackgroundMapLayer.class.getResource("/icons/spinner.gif");
		spinnerIcon = new ImageIcon(iconLocation);
		
		iconLocation = BackgroundMapLayer.class.getResource("/icons/warning_24.png");
		warningIcon = new ImageIcon(iconLocation);
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.BACKGROUND_MAP_LAYER;
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
		if (isSpinning) {
			map.refresh();
		}
		
		return isSpinning;
    }
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);
		
		Graphics2D graphics = (Graphics2D) g;
		graphics.setColor(new Color(255, 255, 255));
		graphics.fillRect(0, 0, getWidth(), getHeight());
		graphics.setClip(1, 1, getWidth() - 1, getHeight() - 1);
		
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		paintTiles(graphics);
	}
	
	private void paintTiles(Graphics2D graphics) {
		MapProvider mapProvider = getMapProvider();
		
		isSpinning = false;
		MapTileGrid mapTileGrid = mapProvider.getMapTileGrid(getWidth(), getHeight());
		if (mapTileGrid == null) {
			return;
		}
		
		MapTile[][] grid = mapTileGrid.getGrid();
		if (grid == null || grid.length == 0 || grid[0].length == 0) {
			return;
		}
		
		final int tileWidth = mapProvider.getTileWidth();
		final int tileHeight = mapProvider.getTileHeight();
		
		long widthInPixels = grid.length * tileWidth;
		long heightInPixels = grid[0].length * tileHeight;
		
		long initialX = (getWidth() / 2) - (widthInPixels / 2) - mapTileGrid.getXOffset();
		long initialY = (getHeight() / 2) - (heightInPixels / 2) - mapTileGrid.getYOffset();
		
		for (int i = 0; i < grid.length; i++) {
			MapTile[] column = grid[i];
			
			for (int j = 0; j < column.length; j++) {
				MapTile tile = grid[i][j];
				
				if (tile != null) {
					long tilePixelX = initialX + (i * tileWidth);
					long tilePixelY = initialY + (j * tileHeight);

					graphics.setColor(new Color(200, 200, 200));
					graphics.drawRect((int) tilePixelX, (int) tilePixelY, mapProvider.getTileWidth(), mapProvider.getTileHeight());
					
					if (tile.isImageReady()) {
						if (tile.getImage() != null) {
							graphics.drawImage(tile.getImage(), (int) tilePixelX, (int) tilePixelY, null);
						} else {
							int x = (int) (tilePixelX + (tileWidth / 2) - (warningIcon.getIconWidth() / 2));
							int y = (int) (tilePixelY + (tileHeight / 2) - (warningIcon.getIconHeight() / 2));
							
							graphics.drawImage(warningIcon.getImage(), x, y, warningIcon.getIconWidth(), warningIcon.getIconHeight(), null);
						}
					} else {
						int x = (int) (tilePixelX + (tileWidth / 2) - (spinnerIcon.getIconWidth() / 2));
						int y = (int) (tilePixelY + (tileHeight / 2) - (spinnerIcon.getIconHeight() / 2));
						
						graphics.drawImage(spinnerIcon.getImage(), x, y, spinnerIcon.getIconWidth(), spinnerIcon.getIconHeight(), this);
						isSpinning = true;
					}
				}
			}
		}
	}
}
