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
package com.trackit.presentation.utilities;

public class LineClipper {
	
	protected double  yMin;
	protected double  yMax;
	protected double  xMin;
	protected double  xMax;
	protected boolean isValid = false;
	protected boolean accepted;
	protected short	  outcode1;
	protected short   outcode2;
	protected double  x1, x2, y1, y2;
	
	public LineClipper() {
	}
	
	public LineClipper( double xmin, double xmax, double ymin, double ymax) {
		if ( xmax > xmin && ymax > ymin ) {
			xMin = xmin;
			xMax = xmax;
			yMin = ymin;
			yMax = ymax;
			isValid = true;
		}
		
	}
	
	public boolean clip( double x1, double y1, double x2, double y2) {
		return process( x1, y1, x2, y2);
	}
	
	public boolean accepted() {
		return accepted;
	}
	
	protected boolean process( double x1, double y1, double x2, double y2) {
		accepted = false;
		if ( isValid && (x1 != x2 || y1 != y2) ) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			outcode1 = outcode( x1, y1);
			outcode2 = outcode( x2, y2);
			while( testSegment() > 0  ) {
				clipLine();
			}
		}
		return accepted;
	}
	
	private void clipLine() {
		int code = clippingCase(outcode1);
		boolean isOne = true;
		if ( code < 0 ) {
			code = clippingCase(outcode2);
			isOne = false;
		}
		double x = 0, y = 0;
		switch ( code ) {
		case 0:  	x = (yMax - y1) * (x2 - x1 )/ (y2 - y1) + x1;	y = yMax; 	break;
		case 1:		x = (yMin - y1) * (x2 - x1) / (y2 - y1) + x1;	y = yMin;	break;
		case 2:		y = (xMax - x1) * (y2 - y1) / (x2 - x1) + y1;	x = xMax; 	break;
		case 3:		y = (xMin - x1) * (y2 - y1) / (x2 - x1) + y1;	x = xMin; 	break;
		default:
			break;
		}
		if ( isOne ) {
			x1 = x;		y1 = y;		outcode1 = outcode( x1, y1);
		}
		else {
			x2 = x;		y2 = y;		outcode2 = outcode( x2, y2);
		}
	}
	
	private int testSegment() {
		if ( outcode1 == 0 && outcode2 == 0 ) {
			accepted = true;
			return 0;
		}
		else {
			accepted = false;
			if ( (outcode1 & outcode2) != 0 )
				return -1;
			else
				return 1;
		}
	}

	private short outcode( double x, double y) {
		short outcode = 0;
		if ( y > yMax )
			outcode += 1;
		if ( y < yMin )
			outcode += 2;
		if ( x > xMax )
			outcode += 4;
		if ( x < xMin )
			outcode += 8; 
		return outcode;
	}
	
	private int clippingCase( int outcode) {
		if ( outcode == 0 )
			return -1;
		int code = 0;
		while( outcode != 0 ) {
			if ( (outcode & 1) != 0 )
				break;
			outcode = outcode >> 1;
			code++;
		}
		return code;
	}
	
	public String toString() {
		return String.format( "%10.3f %10.3f %10.3f %10.3f - %10.3f %10.3f %10.3f %10.3f",
				xMin, xMax, yMin, yMax, x1, y1, x2, y2);
	}
	
}
