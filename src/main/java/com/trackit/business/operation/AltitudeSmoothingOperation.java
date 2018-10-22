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
package com.trackit.business.operation;



public class AltitudeSmoothingOperation {
	public static void smoothing(double[][] data, double tau, boolean keepStartEnd) {
//	    double deltaStart, deltaEnd;

	    /* First smoothing with tau */
	    smoothing1(data, tau);

	    /* Second smoothing with tau/4 */
	    smoothing1(data, tau / 4);

	    /* Keep start and end values */
//	    if (keepStartEnd) {
//	        deltaStart = trackpoints.get(0).get[0] - field_sc[0];
//	        Delta_end = field[SIZE - 1] - field_sc[SIZE - 1];
//	        for (i = 0; i < SIZE; i++) {
//	            field_sc[i] = field_sc[i] + Delta_start + (Delta_end - Delta_start) / (double)(SIZE - 1) * (double) (i);
//	        }
//	    }
	}
	
	private static void smoothing1(double[][] data, double tau) {
	    double[] field_sf = new double[data.length];
	    double[] field_sb = new double[data.length];
	    double dt;

	    /* Forward smoothing */
	    field_sf[0] = data[0][1];
	    for (int i = 1; i < data.length; i++) {
	        dt = (data[i][0] - data[i - 1][0]) / tau;
	        field_sf[i] = (field_sf[i - 1] + dt * data[i][1]) / (1. + dt);
	    }

	    // Backward smoothing
	    //-------------------
	    field_sb[data.length - 1] = data[data.length - 1][1];
	    for (int i = 2; i < data.length + 1; i++) {
	        dt = (data[data.length - i + 1][0] - data[data.length - i][0]) / tau;
	        field_sb[data.length - i] = (field_sb[data.length - i + 1]
	        		+ dt * data[data.length - i][1]) / (1. + dt);
	    }

	    // Centered smoothing
	    //-------------------
	    for (int i = 0; i < data.length; i++) {
	        data[i][1] = (field_sf[i] + field_sb[i]) / 2.0;
	    }
	}
}
