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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.trackit.business.common.Constants;
import com.trackit.business.common.OperatingSystem;
import com.trackit.business.utilities.TrackItPreferences;

class VerticalAxis extends Axis {
	private static final long serialVersionUID = 8219427301469784674L;
	private static final int AXIS_LABEL_OFFSET = 12;
	private static final int LABEL_OFFSET = 20;
	
	private double defaultSteps;
	private double dataSeriesVerticalSpace;
	
	VerticalAxis(DataSeries dataSeries) {
		super(dataSeries.getScale(), dataSeries.getLabel(), dataSeries.getUnit(), dataSeries.getColorScheme());
		defaultSteps = TrackItPreferences.getInstance().getDoublePreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.VERTICAL_AXIS_STEPS, 7.0);
		dataSeriesVerticalSpace = TrackItPreferences.getInstance().getDoublePreference(Constants.PrefsCategories.CHART, null,
				Constants.ChartPreferences.DATA_SERIES_VERTICAL_SPACE, 0.8);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(55, (int) (getParent().getHeight()));
	}
	
	@Override
	protected void paintAxis(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;
		graphics.setColor(getColorScheme().getLineColor());
		
		double height = getHeight() * dataSeriesVerticalSpace;
		double pixelsPerStep = height / defaultSteps;

		for (int i = 0; i < defaultSteps; i++) {
			graphics.drawString(String.valueOf((int) (getScale().getMinValue() + (i * getScale().getStepSize()))), 
					LABEL_OFFSET, (int) (getHeight() - (pixelsPerStep * i)));
		}
		
		/* Draw label vertically */
		String label = String.format("%s (%s)", getLabel(), getUnit().toString());
		drawVerticalLabel(graphics, label);
	}
	
	private void drawVerticalLabel(Graphics2D graphics, String label) {
		Font originalFont = graphics.getFont();
		FontMetrics metrics = graphics.getFontMetrics(originalFont);
		
		final int labelWidth = metrics.stringWidth(label);
		final int labelHeight = metrics.getHeight();
		
		
		//58406#########################################################################################
		double height = getHeight() * dataSeriesVerticalSpace;
		int y = (int) (getHeight() - ((height - labelWidth) / 2.0) + (labelHeight / 2.0));
		//Don't know what's wrong with Mac OS Java implementation
		if(OperatingSystem.isMac()){
			graphics.rotate(-Math.PI / 2, AXIS_LABEL_OFFSET, y);
			graphics.drawString(label, AXIS_LABEL_OFFSET, y);
	        graphics.rotate(Math.PI / 2, AXIS_LABEL_OFFSET, y); 
		}
		else {
			AffineTransform fontAT = new AffineTransform();
		    fontAT.rotate(270 * java.lang.Math.PI / 180);
		    Font rotatedFont = originalFont.deriveFont(fontAT);
		    graphics.setFont(rotatedFont);
		    graphics.drawString(label, AXIS_LABEL_OFFSET, y);
		    graphics.setFont(originalFont);
		}
		//##############################################################################################
		
		/*
		AffineTransform fontAT = new AffineTransform();
	    fontAT.rotate(270 * java.lang.Math.PI / 180);
		if(OperatingSystem.isMac()){
			label = new StringBuilder(label).reverse().toString();//58406
		}
	    Font rotatedFont = originalFont.deriveFont(fontAT);
	    graphics.setFont(rotatedFont);
	    
	    double height = getHeight() * dataSeriesVerticalSpace;
	    
	    graphics.drawString(label, AXIS_LABEL_OFFSET, (int) (getHeight() - ((height - labelWidth) / 2.0) + (labelHeight / 2.0)));

	    graphics.setFont(originalFont);*/
	}
}
