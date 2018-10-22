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
package com.trackit.presentation.view.map.layer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.view.map.Map;
import com.trackit.presentation.view.map.provider.MapProvider;
import com.trackit.presentation.view.map.provider.MapProviderFactory;
import com.trackit.presentation.view.map.provider.MapProviderType;

public class MapControlsLayer extends MapLayer implements EventPublisher {
	private static final long serialVersionUID = -910047318410541173L;
	private Map map;
	private JComboBox<MapProviderType> mapProviderChooser;
	
	public MapControlsLayer(Map map) {
		super(map);
		this.map = map;
		init();
	}

	private void init() {
		setPreferredSize(new Dimension(300, 300));
		mapProviderChooser = new JComboBox<>(MapProviderType.values());
		mapProviderChooser.putClientProperty("JComponent.sizeVariant", "mini");
		mapProviderChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				MapProvider oldMapProvider = map.getMapProvider();
				MapProvider newMapProvider =  MapProviderFactory.getInstance().getMapProvider(
						(MapProviderType) mapProviderChooser.getSelectedItem(), oldMapProvider.getMapType(), oldMapProvider.getCenterLocation());
				map.setMapProvider(newMapProvider); 
				validate();//58406
				repaint();
			}
		});
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);

		layout.setHorizontalGroup(
				layout.createSequentialGroup()
						.addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(mapProviderChooser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addComponent(mapProviderChooser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		
		revalidate();
	}
	
	@Override
	public MapLayerType getType() {
		return MapLayerType.MAP_CONTROLS_LAYER;
	}
}