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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.presentation.utilities.Operation;
import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProvider;

public abstract class MapLayer extends JPanel {
	private static final long serialVersionUID = 8619529830886552901L;
	protected Map map;
	
	public MapLayer(Map map) {
		this.map = map;
		setOpaque(false);
	}
	
	public abstract MapLayerType getType();
	
	public MapProvider getMapProvider() {
		return map.getMapProvider();
	}
	
	public List<DocumentItem> getItems() {
		return map.getItems();
	}
	
	public List<DocumentItem> getItems(Location location) {
		return new ArrayList<DocumentItem>();
	}
	
	public List<Operation> getSupportedOperations(Location location) {
		return new ArrayList<Operation>();
	}
	
	@Override
	public int getWidth() {
		return super.getWidth();
	}
	
	@Override
	public int getHeight() {
		return super.getHeight();
	}
	
	public void finish() {
	}
}
