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
package com.trackit.presentation.view.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.trackit.business.common.ColorScheme;
import com.trackit.business.common.Formatters;
import com.trackit.business.common.Unit;

class TimeAxis extends Axis {
	private static final long serialVersionUID = -8855726595439596531L;
	
	private static final int TICK_MARK_LENGTH = 5;
	private static final int AXIS_LABEL_OFFSET = 20;
	private static final int VALUE_LABEL_OFFSET = 40;

	TimeAxis(Scale scale, String label, Unit unit, ColorScheme colorScheme) {
		super(scale, label, Unit.DATE_TIME, colorScheme);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), 55);
	}

	@Override
	public void paintAxis(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;
		graphics.setColor(Color.LIGHT_GRAY);
		
		FontMetrics metrics = graphics.getFontMetrics(graphics.getFont());
		int pixelsPerStep = (int) (getWidth() / getScale().getSteps());
		
		int offset;
		graphics.setColor(Color.DARK_GRAY);
		for (int i = 0; i <= getScale().getSteps(); i++) {
			graphics.setStroke(new BasicStroke(1.0f));
			graphics.drawLine(0, 0, getWidth(), 0);

			graphics.setStroke(new BasicStroke(1.9f));
			graphics.drawLine(i * pixelsPerStep, -TICK_MARK_LENGTH, i * pixelsPerStep, TICK_MARK_LENGTH);

			double value = getScale().getMinValue() + (i * getScale().getStepSize());
			String label = Formatters.getFormatedDuration(value);
			offset = (i == 0 ? 0 : (i == getScale().getSteps() ? metrics.stringWidth(label) : metrics.stringWidth(label) / 2));
			graphics.drawString(label, (i * pixelsPerStep) - offset, AXIS_LABEL_OFFSET);
		}
		
		String label = String.format("%s (%s)", getLabel(), getUnit().toString());
		graphics.drawString(label, (getWidth() / 2) - (metrics.stringWidth(label) / 2), VALUE_LABEL_OFFSET);
	}
}
