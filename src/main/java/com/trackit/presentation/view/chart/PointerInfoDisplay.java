/*
 * This file is part of Track It!.
 * Copyright (C) 2017 J M Brisson Lopes
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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import com.trackit.business.common.ColorScheme;
import com.trackit.business.common.Formatters;
import com.trackit.business.common.Unit;
import com.trackit.business.domain.Trackpoint;
import com.trackit.presentation.view.chart.ChartView.ChartType;

public class PointerInfoDisplay {

	private List<String>        labels;
	private List<BufferedImage> images;
	private long         startTime;
	private ChartView    chartView;
	private BufferedImage timeImage;
	private BufferedImage distanceImage;
	private BufferedImage altitudeImage;
	private BufferedImage speedImage;
	private BufferedImage heartRateImage;
	private Date		  timeStamp;
	
	private static int guard    = 12;
	private static int padding  = 5;
	private static int iconSize = 10;
	private static int iconXStart = guard + padding;
	private static int textXStart = iconXStart + iconSize + padding;
	
	public PointerInfoDisplay( ChartView chartView) {
		this.chartView = chartView;
		labels         = new ArrayList<>();
		images         = new ArrayList<>();
		startTime      = -1L;
		try {
			URL url = PointerInfoDisplay.class.getResource( "/icons/chronometer_16.png");
			timeImage = ImageIO.read( url);
			url = PointerInfoDisplay.class.getResource( "/icons/ruler_16.png");
			distanceImage = ImageIO.read( url);
			url = PointerInfoDisplay.class.getResource( "/icons/mountains_16.png");
			altitudeImage = ImageIO.read( url);
			url = PointerInfoDisplay.class.getResource( "/icons/speedmeter_16.png");
			speedImage = ImageIO.read( url);
			url = PointerInfoDisplay.class.getResource( "/icons/heart_16.png");
			heartRateImage = ImageIO.read( url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void show( Graphics2D graphics, Trackpoint trackpoint, Point mousePosition, int chartWidth) {
		if ( trackpoint != null ) {
			if ( startTime < 0 ) {
				Trackpoint zero;
				if ( chartView.getChartMode().equals( ChartType.TIME))
					zero = chartView.getTrackpointAtDuration( 0);
				else
					zero = chartView.getTrackpointAtDistance(0);
				startTime = zero.getTimestamp().getTime();
			}
			labels.clear();
			images.clear();
			timeStamp = trackpoint.getTimestamp();
			addLine( (double) (timeStamp.getTime() - startTime) * .001, 
					 Unit.SECOND, timeImage);
			addLine( trackpoint.getDistance(), Unit.KILOMETER, distanceImage);
			if ( chartView.showElevation() )
				addLine( trackpoint.getAltitude(), Unit.METER, altitudeImage);
			if ( chartView.showSpeed() )
				addLine( trackpoint.getSpeed(), Unit.KILOMETER_PER_HOUR, speedImage);
			if ( chartView.showHeartRate() )
				addLine( trackpoint.getHeartRate(), Unit.BPM, heartRateImage);
//			log();
			
			Font font = new Font(null, Font.BOLD, 9);
			graphics.setFont(font);
			int width   = 0;
			for( String label: labels)
				width = Math.max( width, graphics.getFontMetrics().stringWidth( label));
			width += padding * 3 + iconSize;
			int lineHeight = graphics.getFontMetrics().getHeight();
			int height =  lineHeight* labels.size() + graphics.getFontMetrics().getDescent();
			int atX = (int) Math.round( mousePosition.getX()) - width - guard * 2;
			if ( atX < 0 )
				atX = (int) Math.round( mousePosition.getX());
			int atY = (int) Math.round( mousePosition.getY()) - lineHeight;
			graphics.setColor(ColorScheme.LIGHT_ORANGE.getSelectionFillColor());
			graphics.fill(new RoundRectangle2D.Float( atX + guard, atY,
					width, height, 10, 10));

			graphics.setColor(ColorScheme.LIGHT_ORANGE.getSelectionLineColor());
			graphics.setStroke(new BasicStroke(1.0f));
			graphics.draw(new RoundRectangle2D.Float( atX + guard, atY,
					width, height, 10, 10));
			
			graphics.setColor(Color.BLACK);
			int y = atY + lineHeight;
			for( String label: labels) {
				graphics.drawString( label, textXStart + atX, y);
				y += lineHeight;
			}
			y = atY + lineHeight - iconSize;
			for( BufferedImage image: images) {
				graphics.drawImage( image, atX + iconXStart, y,  iconSize, iconSize, null);
				y += lineHeight;
			}
		}
	}
	
	private void addLine( Double value, Unit unit, BufferedImage image) {
		if ( value != null ) {
			String valueString = "";
			switch ( unit ) {
			case SECOND:
				valueString = Formatters.getFormatedDuration( value) + "  (" +
							  Formatters.formatTimeOfTheDay( timeStamp) + ")";
				break;
			case KILOMETER:
				valueString = Formatters.getFormatedDistance( value);
				break;
			case KILOMETER_PER_HOUR:
				valueString = Formatters.getFormatedSpeed( value);
				break;
			case  METER:
				valueString = Formatters.getFormatedAltitude( value);
				break;
			default:
				break;
			}
//			labels.add( Messages.getMessage( label) + " " + valueString);
			labels.add( valueString);
			images.add( image);
		}
	}
	
	private void addLine( Short value, Unit unit, BufferedImage image) {
		if ( value != null ) {
			String valueString = Formatters.getDecimalFormat(0).format( value);
//			labels.add( Messages.getMessage( label) + " " + valueString + " " + unit);
			labels.add( valueString + " " + unit);
			images.add( image);
		}
	}
	
	private void log() {
		for( String line: labels)
			System.out.println( line);
	}
}
