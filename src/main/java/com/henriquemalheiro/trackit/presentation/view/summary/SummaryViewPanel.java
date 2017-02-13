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
package com.henriquemalheiro.trackit.presentation.view.summary;

import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class SummaryViewPanel extends JScrollPane {
	private static final long serialVersionUID = -3939066890047844708L;
	private List<DisplayValue> displayValues;
	
	public SummaryViewPanel(JPanel panel, List<DisplayValue> displayValues) {
		super(panel);
		this.displayValues = displayValues;
		initComponents();
	}

	private void initComponents() {
		getVerticalScrollBar().setUnitIncrement(16);
		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.WHITE);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
	
	public List<DisplayValue> getDisplayValues() {
		return displayValues;
	}
}
