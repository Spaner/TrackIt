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
package com.henriquemalheiro.trackit.presentation.view.chart;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import com.henriquemalheiro.trackit.business.common.ColorScheme;
import com.henriquemalheiro.trackit.business.common.Unit;

abstract class Axis extends JComponent {
	private static final long serialVersionUID = 8120074410435976078L;
	
	private Scale scale;
	private String label;
	private Unit unit;
	private ColorScheme colorScheme;
	
	Axis(Scale scale, String label, Unit unit, ColorScheme colorScheme) {
		this.scale = scale;
		this.label = label;
		this.unit = unit;
		this.colorScheme = colorScheme;
	}
	
	Scale getScale() {
		return scale;
	}
	
	String getLabel() {
		return label;
	}
	
	Unit getUnit() {
		return unit;
	}

	ColorScheme getColorScheme() {
		return colorScheme;
	}

	@Override
	final protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		setOpaque(true);
		
		Graphics2D graphics = (Graphics2D) g;
		graphics.setColor(new Color(255, 255, 255));
		graphics.fillRect(0, 0, getWidth(), getHeight());
		
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		paintAxis(g);
	}
	
	protected abstract void paintAxis(Graphics g);

	@Override
	public String toString() {
		return String.format("Axis [scale=%s, colorScheme=%s]", scale, colorScheme);
	}
}
