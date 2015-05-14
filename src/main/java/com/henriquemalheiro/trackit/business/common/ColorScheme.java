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
package com.henriquemalheiro.trackit.business.common;

import java.awt.Color;

public enum ColorScheme {
	ELECTRIC_BLUE(new Color(2, 193, 249), new Color(65, 127, 184), new Color(255, 121, 0), new Color(156, 74, 11)), LIGHT_ORANGE(
	        new Color(255, 121, 0), new Color(156, 74, 11), new Color(184, 255, 39), new Color(115, 136, 36)), LIGHT_RED(
	        new Color(255, 0, 21), new Color(161, 0, 7), new Color(241, 255, 2), new Color(144, 153, 0)), LIGHT_GREEN(
	        new Color(184, 255, 39), new Color(115, 136, 36), new Color(255, 121, 0), new Color(156, 74, 11)), LIGHT_BLUE(
	        new Color(1, 164, 255), new Color(0, 92, 149), new Color(255, 121, 0), new Color(156, 74, 11)), DARK_YELLOW(
	        new Color(249, 193, 2), new Color(137, 109, 8), new Color(255, 121, 0), new Color(156, 74, 11));

	private Color fillColor;
	private Color lineColor;
	private Color selectionFillColor;
	private Color selectionLineColor;
	
	private static int currentColorScheme = 0;
	
	private ColorScheme(Color fillColor, Color lineColor, Color selectionFillColor, Color selectionLineColor) {
		this.fillColor = fillColor;
		this.lineColor = lineColor;
		this.selectionFillColor = selectionFillColor;
		this.selectionLineColor = selectionLineColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public Color getSelectionFillColor() {
		return selectionFillColor;
	}

	public Color getSelectionLineColor() {
		return selectionLineColor;
	}
	
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public void setSelectionFillColor(Color selectionFillColor) {
		this.selectionFillColor = selectionFillColor;
	}

	public void setSelectionLineColor(Color selectionLineColor) {
		this.selectionLineColor = selectionLineColor;
	}
	
	public static synchronized ColorScheme getNextColorScheme() {
		ColorScheme colorScheme = ColorScheme.values()[currentColorScheme];
		currentColorScheme = (currentColorScheme + 1) % ColorScheme.values().length;
		
		return colorScheme;
	}
}
