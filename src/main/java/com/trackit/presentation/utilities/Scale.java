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

package com.trackit.presentation.utilities;

import org.apache.log4j.Logger;

public class Scale {

	private static Logger logger = Logger.getLogger(Scale.class.getName());
	
	private static final int DEFAULT_STEPS    = 10;
	private static final double[] multipliers = { 1., 2., 2.5, 4., 5., 7.5, 8.,  10.};

	private   double   minValue;
	private   double   maxValue;
	private   double   range;
	protected double   stepSize;
	protected double   bigTickStep;
	protected int      steps;
	protected int	   noOfDecimals;
	private   double   originalMinValue;
	private   double   originalMaxValue;
	private   int      originalSteps;
	protected double[] customMultipliers = null;
	protected boolean  useCustomMultipliers;
	
	public Scale( double minValue, double maxValue) {
		this( minValue, maxValue, DEFAULT_STEPS);
	}
	
	public Scale( double minValue, double maxValue, int noTargetSteps) {
		originalMinValue = minValue;
		originalMaxValue = maxValue;
		computeGivenNoOfSteps( noTargetSteps);
	}
	
	public Scale( double minValue, double maxValue, double specifiedStepSize) {
		originalMinValue = minValue;
		originalMaxValue = maxValue;
		computeGivenStepSize( specifiedStepSize);
	}
	
	public void setNoSteps( int noTargetSteps) {
		computeGivenNoOfSteps( noTargetSteps);
	}
	
	public void setStepSize( double newStepSize) {
		computeGivenStepSize( newStepSize);
	}
		
	public void setCustomMultipliers( double [] multipliers) {
		if ( multipliers != null ) {
			customMultipliers    = multipliers;
			useCustomMultipliers = true;
		}
	}
	
	public boolean enableCustomMultipliers( boolean enable) {
		if ( enable && customMultipliers != null )
			useCustomMultipliers = true;
		else
			useCustomMultipliers = false;
		return useCustomMultipliers;
	}
	
	public double getMinimumValue() {
		return minValue;
	}
	
	public double getMaximumValue() {
		return maxValue;
	}
	
	public double getStepSize() {
		return stepSize;
	}
	
	public double getBigTickStep() {
		return bigTickStep;
	}
	
	public int getNoSteps() {
		return steps;
	}
	
	public int getNoDecimalDigits() {
		return noOfDecimals;
	}
	
	public double getOriginalMinimumValue() {
		return originalMinValue;
	}
	
	public double getOriginalMaximumValue() {
		return originalMaxValue;
	}
	
	public boolean customMultipliersEnabled() {
		return useCustomMultipliers;
	}
	
	public double[] getMultipliers() {
		return useCustomMultipliers ? customMultipliers : multipliers;
	}

	public String toString() {
		return String.format("Scale [minValue=%.6f, maxValue=%.6f, range=%.6f,"
				+ " stepSize=%.6f, steps=%s, decimals=%d, originalMinValue=%.6f,"
				+ " originalMaxValue=%.6f, originalSteps=%s]", 
				minValue, maxValue, range, stepSize, steps, noOfDecimals,
				originalMinValue, originalMaxValue, originalSteps);
	}
	
	private void computeGivenNoOfSteps( int noTargetSteps) {
		double tmpStep = (originalMaxValue - originalMinValue) / noTargetSteps;
		originalSteps   = noTargetSteps;
		compute( tmpStep);
	}
	
	private void computeGivenStepSize( double newStepSize) {
		originalSteps =  (int) ((originalMaxValue - originalMinValue) / newStepSize);
		stepSize      = newStepSize;
		compute( newStepSize);
	}

	private void compute( double tmpStep) {
		double exponent = getExponent( tmpStep);
		double power    = getPower( exponent);
		stepSize        = computeStepSize( tmpStep, exponent, power);
        minValue        = Math.floor( originalMinValue / stepSize) * stepSize;
        maxValue        = Math.ceil(  originalMaxValue / stepSize) * stepSize;
        range           = maxValue - minValue;
        steps           = (int) Math.round ( range / stepSize);
	}
	
	protected double computeStepSize( double oldStep, double exponent, double power) {
		bigTickStep       = power;
		double multiplier = getMultiplier( oldStep/power, 
							  			   useCustomMultipliers ? customMultipliers: multipliers);
		noOfDecimals = noOfDecimalDigits( multiplier, exponent);
		return multiplier * power;
	}
	
	protected double getMultiplier( double tmpStep, double[] multipliers) {
		try {
			int index = 0;
			while ( index < multipliers.length && tmpStep > multipliers[index] )
				index++;
			return multipliers[index];
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			logger.error( "Scale[getMultiplier]-IndexOutOfBounds min: " + originalMinValue +
					      " max: " + originalMaxValue + " step: " + tmpStep + " no: " + originalSteps);
			return multipliers[ multipliers.length -1];
		}
	}
	
	protected static int noOfDecimalDigits( double multiplier, double exponent) {
		return (int) Math.max( noShifts( multiplier) - exponent, 0);
	}
	
	protected static int noShifts( double value) {
		int noShifts = 0;
		double test = value;
		while( test - (int) test != 0 && noShifts < 6 ) {
			test *= 10.;
			noShifts ++;
		}
		return noShifts;
	}
	
	protected static double getExponent( double value) {
		return Math.floor( Math.log10( value));
	}
	
	protected static double getPower( double exponent) {
		return Math.pow( 10.,  exponent);
	}
}
