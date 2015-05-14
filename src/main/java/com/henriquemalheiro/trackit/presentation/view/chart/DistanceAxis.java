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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.henriquemalheiro.trackit.business.common.ColorScheme;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Unit;

class DistanceAxis extends Axis {
	private static final long serialVersionUID = -8855726595439596531L;
	private static final int AXIS_RULER_HEIGHT = 7;
	private static final int AXIS_LABEL_OFFSET = 20;
	private static final int VALUE_LABEL_OFFSET = 40;

	DistanceAxis(Scale scale, String label, Unit unit, ColorScheme colorScheme) {
		super(scale, label, Unit.KILOMETER_PER_HOUR, colorScheme);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), 55);
	}

	@Override
	public void paintAxis(Graphics g) {
		if (getScale() == null) {
			return;
		}
		
		Graphics2D graphics = (Graphics2D) g;
		graphics.setColor(Color.LIGHT_GRAY);
		
		FontMetrics metrics = graphics.getFontMetrics(graphics.getFont());
		int pixelsPerStep = (int) (getWidth() / getScale().getSteps());
		double magPower = Math.pow(10.0, Math.ceil(Math.log10(getScale().getSteps())));
		
		Color[] colors = new Color[] { Color.LIGHT_GRAY, Color.WHITE };
		int offset;
		for (int i = 0; i <= getScale().getSteps(); i++) {
			
			graphics.setColor(colors[i % 2]);
			graphics.fillRect(i * pixelsPerStep + 1, 0, (int) (getScale().getStepSize() / magPower * pixelsPerStep), AXIS_RULER_HEIGHT);
			
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.drawRect(i * pixelsPerStep + 1, 0, (int) (getScale().getStepSize() / magPower * pixelsPerStep), AXIS_RULER_HEIGHT);

			graphics.setColor(Color.DARK_GRAY);
			double value = getScale().getMinValue() + (i * getScale().getStepSize());
			String label = Formatters.getDecimalFormat(1).format(value / 1000.0);
			offset = (i == 0 ? 0 : (i == getScale().getSteps() ? metrics.stringWidth(label) : metrics.stringWidth(label) / 2));
			graphics.drawString(label, (i * pixelsPerStep) - offset, AXIS_LABEL_OFFSET);
		}
		
		String label = String.format("%s (%s)", getLabel(), Unit.KILOMETER.toString());
		graphics.drawString(label, (getWidth() / 2) - (metrics.stringWidth(label) / 2), VALUE_LABEL_OFFSET);
	}
}
