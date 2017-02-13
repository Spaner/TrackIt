/*
 * This file is part of Track It!.
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
 
 
/* Esta classe existe para permitir que cada Activity/Course inicialmente 
 * tenham a cor predefinida nas preferências e simultaneamente alterar a 
 * sua cor definitivamente no menu de apresentacao multipla. 
 * Deste modo evita-se destruir os valores do enumerado ColorScheme.
 * */

package com.pg58406.trackit.business.common;

import java.awt.Color;

public class ColorSchemeV2 {

	private Color fillColor;
	private Color lineColor;
	private Color selectionFillColor;
	private Color selectionLineColor;

	public ColorSchemeV2(Color fillColor, Color lineColor,
			Color selectionFillColor, Color selectionLineColor) {
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

}
