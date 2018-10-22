/*
 * This file is part of Track It!.
 * Copyright (C) 2017 J Brisson Lopes
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

import java.awt.FontMetrics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.trackit.business.common.Location;
import com.trackit.business.domain.CoordinatesFormatter;
import com.trackit.business.domain.CoordinatesType;
import com.trackit.business.domain.GeographicLocation;
import com.trackit.business.domain.MGRSLocation;
import com.trackit.business.domain.UTMLocation;
import com.trackit.business.utilities.Utilities;
import com.trackit.presentation.utilities.CoordinatesScale;
import com.trackit.presentation.utilities.GeographicLineClipper;
import com.trackit.presentation.view.map.Map;
import com.trackit.presentation.view.map.MapType;
import com.trackit.presentation.view.map.provider.MapProvider;
import com.trackit.presentation.view.map.provider.MapProviderType;

public class GridLayer extends MapLayer {
	
	private static Color  mapAndTerrainColour 	  = new Color( 255, 0, 0);
	private static Color  satelliteAndHybridColor = new Color( 250, 218, 94);
	private static Color  militarySurveyColor	  = new Color( 139, 69, 19);
	private static Color  utmMgrsMapAndTerrainGridColor		 = new Color( 64, 64, 64);
	private static Color  utmMgrsSatelliteAndHybridGridColor = new Color( 255, 255, 0);
	private static String militaryTypeDescription = MapProviderType.MILITARY_MAPS.getDescription();
	private static BasicStroke normalStroke       = new BasicStroke( 1);
	private static BasicStroke thickStroke 		  = new BasicStroke( 2);
	
	private static double utmStripWidth = 6.;
	private static double utmBandHeight = 8.;
	
	private MapProvider provider;
	private int			mapWidth;
	private int			mapHeight;
	private int 		centerX;
	private int			centerY;
	private long        tileWidth;
	private boolean		normalTiledMap = true;
	
	private TreeMap<Integer, Double> bottomLabels = new TreeMap<>();
	private TreeMap<Integer, Double> topLabels	  = new TreeMap<>();
	private TreeMap<Integer, Double> leftLabels	  = new TreeMap<>();
	private TreeMap<Integer, Double> rightLabels  = new TreeMap<>();
	
	private double 		minLatitude;
	private double		maxLatitude;
	private double		minLongitude;
	private double		maxLongitude;
	private double 		areaMinXValue;
	private double		areaMaxXValue;
	private double		areaMinYValue;
	private double		areaMaxYValue;
	private double 		minXvalue;
	private double 		maxXvalue;
	private double 		minYValue;
	private double 		maxYvalue;
	private double 		deltaX;
	private double 		widerDeltaX;
	private double		deltaY;
	private double		widerDeltaY;
	private int    		noDecimalDigits;
	private Font		hugeFont;
	private Font		largeFont;
	private Font   		normalFont;
	private Font   		smallFont;
	private FontMetrics hugeFontMetrics;
	private FontMetrics largeFontMetrics;
	private FontMetrics normalFontMetrics;
	private FontMetrics smallFontMetrics;
	private int			hugeFontHeight;
	private int			largeFontHeight;
	private int    		normalFontHeight	= -1;
	private int    		smallFontHeight;
	private CoordinatesType coordinatesType;
	private boolean 		useDegrees;
	private int[]			offset1 = new int[2];
	private int[]			offset2 = new int[2];
	private int				currentZone;
	private char			currentBand;
	private Graphics2D		graphics;
	private Color			normalColor;
	private Color  			utmMgrsGridColor;
	
	private GeographicLineClipper clipper;
	
	private static CoordinatesFormatter coordinatesFormatter = CoordinatesFormatter.getInstance();

	public GridLayer(Map map) {
		super(map);
		hugeFont   = new Font( "SansSerif", Font.PLAIN, 22);
		largeFont  = new Font( "SansSerif", Font.PLAIN, 16);
		normalFont = new Font( "SansSerif", Font.PLAIN, 12);
		smallFont  = new Font( "SansSerif", Font.PLAIN, 10);
	}

	@Override
	public MapLayerType getType() {
		return MapLayerType.GRID_LAYER;
	}
	
	@Override
	protected void paintComponent( Graphics g) {
		graphics = (Graphics2D) g;
		super.paintComponent( graphics);
		if ( normalFontHeight < 0 )
			initMetrics( g);
		
		coordinatesType = coordinatesFormatter.getCoordinatesType();
		provider  		= getMapProvider();
		mapWidth  		= getWidth();
		mapHeight 		= getHeight();
		centerX   		= mapWidth / 2;
		centerY	 		= mapHeight / 2;
		tileWidth 		= provider.getTileWidth() << provider.getZoom();
		
		if ( provider.getName().equals( militaryTypeDescription) ) {
			normalColor    = militarySurveyColor;
			normalTiledMap = false;
		}
		else {
			MapType mapType = provider.getMapType();
			if ( mapType == MapType.SATELLITE || mapType == MapType.HYBRID ) {
				normalColor 	 = satelliteAndHybridColor;
				utmMgrsGridColor = utmMgrsSatelliteAndHybridGridColor;
			}
			else {
				normalColor 	 = mapAndTerrainColour;
				utmMgrsGridColor = utmMgrsMapAndTerrainGridColor;
			}
		}
		graphics.setColor( normalColor);

				// handle one or more 360º spans				
		Location location = provider.getLocation( 0, 0, mapWidth, mapHeight);
		maxLatitude  = location.getLatitude();
		minLongitude = location.getLongitude();
		location     = provider.getLocation( mapWidth, mapHeight, mapWidth, mapHeight);
		minLatitude  = location.getLatitude();
		maxLongitude = location.getLongitude();
		List<Integer> limits = getLongitudeSectionsLimits();
		// Compute longitude total span
		double longitudeSpan = maxLongitude - minLongitude;
		if ( limits.size() > 2 )
			longitudeSpan += (limits.size() - 2) * 360.;
		
		// Use decimal degrees when UTM or MGRS would result in a closely packed grid
		// Use UTM if MGRS grid is too packed
		useDegrees = true;
		if ( coordinatesType == CoordinatesType.UTM || coordinatesType == CoordinatesType.MGRS ) {
			double degreesToPixelsRatio = mapWidth / longitudeSpan;
			if ( utmStripWidth * degreesToPixelsRatio < 75 ) {
				useDegrees = true;
				coordinatesType = CoordinatesType.DECIMAL_DEGREES;
			}
			else {
				useDegrees = false;
				if ( utmStripWidth * .1 * degreesToPixelsRatio < 50 )
					coordinatesType = CoordinatesType.UTM;
			}
		}
		
		// process grid lines
		if ( useDegrees )
			plotGeographicGridLines( limits, longitudeSpan);
		else {
			GeographicLocation.acceptOutofBoundsCoordinates( true);
			plotCartesianGridLines( limits);
			GeographicLocation.acceptOutofBoundsCoordinates( false);
		}
	}
	
	private void plotGeographicGridLines( List<Integer> longitudeLimits, double longitudeSpan) {
		CoordinatesScale scale = computeAppropriateScale( 0, longitudeSpan, coordinatesType);
		deltaX    	           = scale.getStepSize();
		widerDeltaX            = scale.getBigTickStep();
		scale 		           = new CoordinatesScale( minLatitude, maxLatitude, Math.min( deltaX, 45.));
		minLatitude            = scale.getMinimumValue();
		maxLatitude            = scale.getMaximumValue();
		deltaY                 = scale.getStepSize();
		widerDeltaY            = scale.getBigTickStep();
		noDecimalDigits		   = scale.getNoDecimalDigits();
		// Process 360º spans, one at a time
		bottomLabels.clear();
		topLabels.clear();
		int xmin = longitudeLimits.get(0);
		int xmax;
		for( int i=1; i<(longitudeLimits.size()); i++) {
			xmax           = longitudeLimits.get(i);
			double xOffset = xOffsetFromMapCenter( xmin, xmax);
			double minSectionLongitude = geographicSectionMinimumLongitude( xmin, xmax);
			double maxSectionLongitude = geographicSectionMaximumLongitude( xmin, xmax);
			clipper = new GeographicLineClipper( minLatitude, maxLatitude, minSectionLongitude, maxSectionLongitude);
			computeSectionGridLines( minLatitude, maxLatitude, minSectionLongitude, maxSectionLongitude);
						
			double xValue = minXvalue;
			while( xValue <= maxXvalue ) {
				if ( plotGridLine( minLatitude, xValue,  maxLatitude, xValue, 
						           xOffset, largeTic( xValue, widerDeltaX)) ) {
					bottomLabels.put( offset1[0], xValue);
					topLabels.put(    offset2[0], xValue);
				}
				xValue += deltaX;
			}
			
			leftLabels.clear();
			rightLabels.clear();
			double yValue = minLatitude;
			while( yValue <= maxLatitude ) {
					if ( plotGridLine( yValue, minXvalue, yValue, maxXvalue,
						               xOffset, largeTic( yValue, widerDeltaY)) ) {
					leftLabels.put(  offset1[1], yValue);
					rightLabels.put( offset2[1], yValue);
				}
				yValue += deltaY;
			}
			xmin = xmax;
		}
		
		plotLabels();
	}
	
	private void plotCartesianGridLines( List<Integer> longitudeLimits) {
		// find UTM band limits to plot zones a band at a time
		List<Double> bandLimits = getBandLatitudeLimits( minLatitude, maxLatitude);
		
		graphics.setColor( utmMgrsGridColor);
		for( double latitude: bandLimits) {
			if ( latitude > minLatitude && latitude < maxLatitude ) {
				offset1 = getXYCoordinates( minLongitude, latitude, 0);
				graphics.drawLine( 0, offset1[1], mapWidth, offset1[1]);
			}
		}
		
		// get the scale to use and plot zone separation lines
		int xmin = longitudeLimits.get(0);
		int xmax;
		double distance    = 0.;
		double midLatitude = (maxLatitude + minLatitude) * .5;
		for( int i=1; i<longitudeLimits.size(); i++) {
			xmax                       = longitudeLimits.get(i);
			// get distance
			double minSectionLongitude = geographicSectionMinimumLongitude( xmin, xmax);
			double maxSectionLongitude = geographicSectionMaximumLongitude( xmin, xmax);
			distance                  += Utilities.getGreatCircleDistance( midLatitude, minSectionLongitude,
																		   midLatitude, maxSectionLongitude);
			// plot zone separation lines
			double xOffset    = xOffsetFromMapCenter( xmin, xmax);
			double longitude  = (Math.floor( minSectionLongitude / utmStripWidth) + 1) * utmStripWidth;
			while( longitude <= maxSectionLongitude ) {
				offset1 = getXYCoordinates( longitude, minLatitude, xOffset);
				graphics.drawLine( offset1[0], 0, offset1[0], mapWidth);
				longitude += utmStripWidth;
			}
			xmin = xmax;
		}
		// get the scale
		CoordinatesScale scale = computeAppropriateScale( 0., distance * 1000., coordinatesType);
		deltaX 		           = scale.getStepSize();
		widerDeltaX            = scale.getBigTickStep();
		deltaY                 = deltaX;
		widerDeltaY			   = widerDeltaX;
		// We use km in plotting so we need to adjust the number of decimal places
		scale           = new CoordinatesScale( 0., distance, deltaX * .001);
		noDecimalDigits = scale.getNoDecimalDigits();
		
		// process each (-180º to +180º) longitude span at a time
		graphics.setStroke(normalStroke);
		graphics.setColor( normalColor);
		topLabels.clear();
		bottomLabels.clear();
		leftLabels.clear();
		rightLabels.clear();
		xmin = longitudeLimits.get(0);
		for( int i=1; i<longitudeLimits.size(); i++) {
			xmax                             = longitudeLimits.get(i);
			double xOffset                   = xOffsetFromMapCenter( xmin, xmax);
			double minSectionLongitude       = geographicSectionMinimumLongitude(xmin, xmax);
			double maxSectionLongitude       = geographicSectionMaximumLongitude(xmin, xmax);
			List<Double> zoneLongitudeLimits = getZoneLongitudeLimits( minSectionLongitude, maxSectionLongitude,
											  						   false);
			double minZoneLongitude = zoneLongitudeLimits.get(0);
			double maxZoneLongitude;
			// for each UTM zone (half zone in this case)
			for( int k=1; k<zoneLongitudeLimits.size(); k++) {
				maxZoneLongitude       = zoneLongitudeLimits.get(k);
				currentZone			   = UTMLocation.getZone( (minZoneLongitude + maxZoneLongitude) * .5);
				double minBandLatitude = bandLimits.get(0);
				double maxBandLatitude;
				// for each UTM band
				for( int b=1; b<bandLimits.size(); b++) {
					maxBandLatitude = bandLimits.get(b);
					currentBand     = UTMLocation.getBand( (minBandLatitude + maxBandLatitude) * .5);
					computeSectionGridLines( minBandLatitude, maxBandLatitude, minZoneLongitude, maxZoneLongitude);
					clipper         = new GeographicLineClipper( minBandLatitude,  maxBandLatitude, 
							                                     minZoneLongitude, maxZoneLongitude);

					double xValue = minXvalue;
					while( xValue <= maxXvalue ) {
						if ( plotGridLine( xValue,  minYValue, xValue, maxYvalue, 
								           xOffset, largeTic( xValue, widerDeltaX)) ) {
							if ( b == 1 )
								bottomLabels.put( offset1[0], xValue);
							if ( b == bandLimits.size()-1 )
								topLabels.put( offset2[0], xValue);
						}
						xValue += deltaX;
					}
					
					double yValue = minYValue;
					while( yValue <= maxYvalue ) {
						if ( plotGridLine(  minXvalue, yValue, maxXvalue, yValue, 
								            xOffset, largeTic( yValue, widerDeltaY)) ) {
							if ( k == 1 )
								leftLabels.put( offset1[1], yValue);
							if ( k == zoneLongitudeLimits.size()-1 )
								rightLabels.put( offset2[1], yValue);
						}
						yValue += deltaY;
					}
					minBandLatitude = maxBandLatitude;
				}
				minZoneLongitude = maxZoneLongitude;
			}
			xmin = xmax;
		}
		
		plotLabels();
		traverseUTMGrid( longitudeLimits);
	}
	
	
	private void traverseUTMGrid( List<Integer> geographicSectionLimits) {
		// get band limits
		List<Double> bandLatitudeLimits = getBandLatitudeLimits( minLatitude, maxLatitude);
		boolean singleBandChart         = bandLatitudeLimits.size() == 2;
		
		graphics.setColor( utmMgrsGridColor);
		//for each geographic section
		int xMin = geographicSectionLimits.get( 0);
		int xMax;
		for( int i=1; i<geographicSectionLimits.size(); i++) {
			xMax                       = geographicSectionLimits.get( i);
			double xOffset             = xOffsetFromMapCenter( xMin, xMax);
			double minSectionLongitude = geographicSectionMinimumLongitude( xMin, xMax);
			double maxSectionLongitude = geographicSectionMaximumLongitude( xMin, xMax);
			List<Double> zonesLongitudeLimits 
									   = getZoneLongitudeLimits( minSectionLongitude, maxSectionLongitude,
											   					 coordinatesType == CoordinatesType.UTM);
			double minZoneLongitude    = zonesLongitudeLimits.get( 0);
			double maxZoneLongitude;
			// for each UTM zone or half zone
			for( int k=1; k<zonesLongitudeLimits.size(); k++) {
				maxZoneLongitude       = zonesLongitudeLimits.get( k);
				currentZone            = UTMLocation.getZone( (minZoneLongitude + maxZoneLongitude) * .5);
				double minBandLatitude = bandLatitudeLimits.get( 0);
				double maxBandLatitude;
				// for each UTM band
				for( int band=1; band<bandLatitudeLimits.size(); band++) {
					maxBandLatitude = bandLatitudeLimits.get( band);
					currentBand     = UTMLocation.getBand( (minBandLatitude+ maxBandLatitude) * .5);
					if ( coordinatesType == CoordinatesType.UTM )
						labelUTMZoneAndBand( minZoneLongitude, maxZoneLongitude, minBandLatitude, maxBandLatitude, 
								             xOffset, singleBandChart);
					else
						labelMGRSSquares( minZoneLongitude, maxZoneLongitude, minBandLatitude, maxBandLatitude,
							          	  xOffset, singleBandChart);
					minBandLatitude = maxBandLatitude;
				}
				minZoneLongitude = maxZoneLongitude;
			}
			xMin = xMax;
		}
	}
	
	private void labelUTMZoneAndBand( double minimumLongitude, double maximumLongitude,
									  double minimumLatitude,  double maximumLatitude,
									  double xOffset,          boolean singleLabelLine) {
		double meanLongitude = (minimumLongitude + maximumLongitude) * .5;
		double meanLatitude  = (minimumLatitude  + maximumLatitude ) * .5;
		String label         = String.format( "%d%c", currentZone, currentBand);
		offset1              = getXYCoordinates( meanLongitude, meanLatitude, xOffset);
		plotUTMGRSLabels( offset1[0], singleLabelLine ? mapHeight - 30: offset1[1],
				          label, "", singleLabelLine);
	}
	
	private void labelMGRSSquares( double minimumLongitude, double maximumLongitude, 
								   double minimumLatitude,  double maximumLatitude,
								   double xOffset,		    boolean singleBandChart) {
		deltaX = 100000.;
		deltaY = 100000.;
		computeSectionGridLines( minimumLatitude, maximumLatitude, minimumLongitude, maximumLongitude);
		double easting1 = minXvalue;
		double easting2 = minXvalue + deltaX;
		double northing1;
		double northing2;
		boolean singleLineChart = (maxYvalue == minYValue + deltaY) && singleBandChart;
		while( easting1 < maxXvalue ) {
			double eastingLeft   = Math.max( easting1, areaMinXValue);
			double eastingRight  = Math.min( easting2, areaMaxXValue);
			double eastingMiddle = (eastingLeft + eastingRight) * .5;
			northing1            = minYValue;
			northing2            = minYValue + deltaX;
			while( northing1 < maxYvalue ) {
				double northingBottom = Math.max( northing1, areaMinYValue);
				double northingTop    = Math.min( northing2, areaMaxYValue);
				double northingMiddle = (northingBottom + northingTop) * .5;
				UTMLocation location = new UTMLocation( eastingMiddle, northingMiddle,
						                                currentZone,   currentBand);
				GeographicLocation geo = location.toGeographic();
				if ( geo.getLatitude() >= minimumLatitude && geo.getLatitude() <= maximumLatitude ) {
					MGRSLocation mgrs = location.toMGRS();
					String zone       = String.format( "%d%c", mgrs.getZone(),   mgrs.getBand());
					String square     = String.format( "%c%c", mgrs.getColumn(), mgrs.getRow());
					offset1           = getXYCoordinates( geo.getLongitude(), geo.getLatitude(), xOffset);
					plotUTMGRSLabels( offset1[0], offset1[1], square, zone, singleLineChart);
				}
				northing1  = northing2;
				northing2 += deltaX;
			}
			easting1  = easting2;
			easting2 += deltaX;
		}
	}
	
	private void plotUTMGRSLabels( int atX, int atY, String first, String second, 
			                       boolean drawAtBottom) {
		Font 		bigFont 		= hugeFont;
		FontMetrics bigfontMetrics  = hugeFontMetrics;
		int			bigFontHeight   = hugeFontHeight;
		int			smallFontHeight = largeFontHeight;
		
		int x = atX - bigfontMetrics.stringWidth( first) / 2;
		int y = drawAtBottom ? mapHeight - 60: atY;
		y    += (int) (second.isEmpty() ? bigFontHeight*.5: (bigFontHeight+smallFontHeight)*.5);
		graphics.setFont( bigFont);
		graphics.drawString( first, x, y);
		
		if ( second != null && !second.isEmpty() ) {
			Font        smallFont        = largeFont;
			FontMetrics smallFontMetrics = largeFontMetrics;
			y -= bigFontHeight;
			x  = atX - smallFontMetrics.stringWidth( second) / 2;
			graphics.setFont( smallFont);
			graphics.drawString( second, x, y);
		}
	}
		
	private boolean plotGridLine( double x1, double y1, double x2, double y2,
			                      double xOffset, boolean isLargeTick) {
		GeographicLocation geographic1, geographic2;
		if ( useDegrees ) {
			geographic1 = new GeographicLocation( x1, y1);
			geographic2 = new GeographicLocation( x2, y2);
		}
		else {
			geographic1 = (new UTMLocation( x1, y1, currentZone, currentBand)).toGeographic();
			geographic2 = (new UTMLocation( x2, y2, currentZone, currentBand)).toGeographic();
		}
		List<GeographicLocation> locations = clipper.clip( geographic1, geographic2);
		if ( locations != null ) {
			GeographicLocation location = locations.get( 0);
			offset1 = getXYCoordinates( location.getLongitude(), location.getLatitude(), xOffset);
			location = locations.get( 1);
			offset2 = getXYCoordinates( location.getLongitude(), location.getLatitude(), xOffset);
			graphics.setStroke( isLargeTick ? thickStroke: normalStroke);
			graphics.drawLine( offset1[0], offset1[1], offset2[0], offset2[1]);
			return true;
		}
		return false;
	}
	
	private void plotLabels() {
		for( int key: bottomLabels.keySet())
			plotLabel( bottomLabels.get( key), false, key, mapHeight);
		for( int key: topLabels.keySet())
			plotLabel( topLabels.get( key), false, key, 0);
		for( int key: leftLabels.keySet())
			plotLabel( leftLabels.get( key), true, 0, key);
		for( int key: rightLabels.keySet())
			plotLabel( rightLabels.get( key), true, mapWidth, key);
	}
	
	private void plotLabel( double value, boolean plotLatitude, int atX, int atY) {
		double coordinate = value;
		String text;
		String prefix = "";
		switch ( coordinatesType ) {
		case DECIMAL_DEGREES:
		case DEGREES_DECIMAL_MINUTES:
		case DEGREES_MINUTES_SECONDS:
			if ( plotLatitude )
				text = coordinatesFormatter.formatLatitude( coordinate, noDecimalDigits);
			else
				text = coordinatesFormatter.formatLongitude( coordinate, noDecimalDigits);
			break;
		case UTM:
		case MGRS:
			if ( coordinatesType == CoordinatesType.UTM )
				prefix = String.format( "%.0f", Math.floor( coordinate / 100000.));
			coordinate %= 100000.;
			coordinate /= 1000.;
			text = coordinatesFormatter.formatCoordinate( coordinate, noDecimalDigits);
			if ( coordinate < 10. )
				text = "0" + text;
			break;
		default:
			text = "";
			break;
		}
		
		int x = atX;
		int y = atY;
		int prefixWidth = (!prefix.isEmpty()) ? smallFontMetrics.stringWidth( prefix): 0;
		if ( plotLatitude ) {
			if ( atX == 0) {
				x += 5;
			}
			else
				x -=  5 + normalFontMetrics.stringWidth( text) + prefixWidth;
			y -= 5;
		}
		else {
			x += 5;
			if ( atY != 0 )
				y -= 6;
			else
				y += normalFontHeight;
		}
		
		if ( !prefix.isEmpty() ) {
			graphics.setFont( smallFont);
			graphics.drawString( prefix, x, y - (normalFontHeight - smallFontHeight));
		}
		graphics.setFont( normalFont);
		graphics.drawString( text, x+prefixWidth, y);
	}
	
	private double geographicSectionMinimumLongitude( int xmin, int xmax) {
		double minimum = -180.;
		if ( xmax - xmin != tileWidth && xmin == 0 )
			minimum = minLongitude;
		return minimum;
	}
	
	private double geographicSectionMaximumLongitude( int xmin, int xmax) {
		double maximum = 180.;
		if ( xmax - xmin != tileWidth && xmax == mapWidth )
			maximum = maxLongitude;
		return maximum;
	}
	
	private static CoordinatesScale computeAppropriateScale( double minValue, double maxValue,
													  CoordinatesType typeOfCoordinatesToUse) {
		CoordinatesScale.setCoordinatesType( typeOfCoordinatesToUse);
		CoordinatesScale scale = new CoordinatesScale( minValue, maxValue, 15);
		while ( scale.getNoSteps() > 12 ) {
			scale.setStepSize( scale.getStepSize() * 1.4142);
		}
		return scale;
	}
	
	private int computeSectionGridLines( double minimumLatitude,  double maximumLatitude,
		                 				 double minimumLongitude, double maximumLongitude) {
		int zone = 0;
		if ( useDegrees ) {
			minXvalue = minimumLongitude;
			maxXvalue = maximumLongitude;
			minYValue = minimumLatitude;
			maxYvalue = maximumLatitude;
		}
		else {
			minXvalue =   1000000.;
			maxXvalue =  -1000000.;
			minYValue =  20000000.;
			maxYvalue = -20000000.;
			double rightLongitude = maximumLongitude;
			if ( rightLongitude % 6 == 0.)
				rightLongitude -= .0000001;
			double topLatitude = maximumLatitude;
			if ( topLatitude % 8 == 0 )
				topLatitude -= .0000001;
			setUTMExtremes( minimumLongitude, minimumLatitude);
			setUTMExtremes( minimumLongitude, topLatitude);
			setUTMExtremes( rightLongitude, minimumLatitude);
			setUTMExtremes( rightLongitude, topLatitude);
			zone = UTMLocation.getZone( (minimumLongitude + maximumLongitude) * .5);
		}
		CoordinatesScale.setCoordinatesType( coordinatesType);
		CoordinatesScale scale = new CoordinatesScale( minXvalue, maxXvalue, deltaX);
		minXvalue     = scale.getMinimumValue();
		maxXvalue     = scale.getMaximumValue();
		areaMinXValue = scale.getOriginalMinimumValue();
		areaMaxXValue = scale.getOriginalMaximumValue();
		widerDeltaX = widerDeltaY = scale.getBigTickStep();
		scale = new CoordinatesScale( minYValue, maxYvalue, deltaY);
		minYValue     = scale.getMinimumValue();
		maxYvalue     = scale.getMaximumValue();
		areaMinYValue = scale.getOriginalMinimumValue();
		areaMaxYValue = scale.getOriginalMaximumValue();
		return zone;
	}
	
	private void setUTMExtremes( double longitude, double latitude) {
		UTMLocation utm = new GeographicLocation( latitude, longitude).toUTM();
		minXvalue = Math.min( minXvalue, utm.getEasting());
		maxXvalue = Math.max( maxXvalue, utm.getEasting());
		minYValue = Math.min( minYValue, utm.getNorthing());
		maxYvalue = Math.max( maxYvalue, utm.getNorthing());
	}

	private static boolean largeTic( double value, double widerDelta) {
		double rest = Math.abs( value) % widerDelta;
		double test = Math.min( rest, widerDelta - rest) / widerDelta;
		if ( test < .05 )
			return true;
		return false;
	}
	
	private void initMetrics (Graphics graphics) {
		hugeFontMetrics   = graphics.getFontMetrics( hugeFont);
		hugeFontHeight    = hugeFontMetrics.getHeight();
		largeFontMetrics  = graphics.getFontMetrics( largeFont);
		largeFontHeight   = largeFontMetrics.getHeight();
		normalFontMetrics = graphics.getFontMetrics( normalFont);
		normalFontHeight  = normalFontMetrics.getHeight();
		smallFontMetrics  = graphics.getFontMetrics( smallFont);
		smallFontHeight   = smallFontMetrics.getHeight();
	}
	
	private List<Integer> getLongitudeSectionsLimits() {
		List<Integer> limits = new ArrayList<>();
		limits.add( 0);
		if ( normalTiledMap ) {
			int [] xy = provider.getCenterOffsetInPixels( -180., 0.);
			int min = xy[0] + mapWidth / 2;
			if ( min >= 0 ) {
				while( min >= 0 )
					min -= tileWidth;
			} else {
				while( min + tileWidth < 0 )
					min += tileWidth;
			}
			while( min+tileWidth < mapWidth ) {
				min += tileWidth;
				limits.add( min);
			}
		}
		limits.add( mapWidth);
		return limits;
	}
	
	private static List<Double> getZoneLongitudeLimits( double minLongitude, double maxLongitude,
														boolean fullZone) {
		double delta = utmStripWidth;
		// Note: the following subdivides longitude limit into half zones (3º wide) instead of 6º
		if ( !fullZone )
			delta /= 2;
		List<Double> limits = new ArrayList<>();
		double longitude = ((int) ((minLongitude + 180) / delta) ) * delta - 180.;
		limits.add( minLongitude);
		while( longitude <= minLongitude )
			longitude += delta;
		while( longitude < maxLongitude ) {
			limits.add( longitude);
			longitude += delta;
		}
		limits.add( maxLongitude);
		return limits;
	}
	
	private static List<Double> getBandLatitudeLimits( double minLatitude, double maxLatitude) {
		List<Double> bandLimits = new ArrayList<>();
		bandLimits.add( minLatitude);
		double latitude = (Math.floor( minLatitude / utmBandHeight) + 1.) * utmBandHeight;
		while( latitude < maxLatitude ) {
			bandLimits.add( latitude);
			latitude += utmBandHeight;
		}
		bandLimits.add( maxLatitude);
		return bandLimits;
	}
	
	private double xOffsetFromMapCenter( int xmin, int xmax) {
		// offset from map center
		double xOffset = 0.;
		if ( centerX > xmax ) {
			while( centerX + xOffset > xmax)
				xOffset -= tileWidth;
		}
		else
		{
			while ( centerX + xOffset < xmin ) {
				xOffset += tileWidth;			}
		}
		return xOffset;
	}
	
	private int[] getXYCoordinates( double longitude, double latitude, double xOffset) {
		int [] offset = provider.getCenterOffsetInPixels( longitude, latitude);
		offset[0] += (int) (centerX + xOffset);
		offset[1] += centerY;
		return offset;
	}
}
