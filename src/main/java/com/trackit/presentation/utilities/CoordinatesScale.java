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

import com.trackit.business.domain.CoordinatesType;

public class CoordinatesScale extends Scale {
	private static final double[] degreesMultipliers        = {  1., 2., 2.5, 5., 6., 10., 15., 20., 30., 45., 90., 180.};
	private static final double[] minutesSecondsMultipliers = {  1., 2., 5., 6., 10., 15., 20., 30., 60.};
	private static final double[] fractionMultipliers       = { 1., 2., 2.5, 5.,  10.};
	private static final double[] utmMultipliers            = { 1., 2., 2.5, 5., 10.};
	
	private double coordinatesStep [];
	private double coordinatesBigStep [];

	private static CoordinatesType coordinatesType = CoordinatesType.DECIMAL_DEGREES;

	public CoordinatesScale(double minValue, double maxValue) {
		super(minValue, maxValue);
	}

	public CoordinatesScale(double minValue, double maxValue, int noTargetSteps) {
		super(minValue, maxValue, noTargetSteps);
	}

	public CoordinatesScale(double minValue, double maxValue, double stepSize) {
		super(minValue, maxValue, stepSize);
	}
	
	public static void setCoordinatesType( CoordinatesType type) {
		coordinatesType = type;
	}
	
	public static CoordinatesType getCoordinatesType() {
		return coordinatesType;
	}
		
	public String toString() {
		return String.format( "%s\n%d %f %f %f %f %f %f", super.toString(), steps, coordinatesStep[0], coordinatesStep[1], coordinatesStep[2],
				coordinatesBigStep[0], coordinatesBigStep[1], coordinatesBigStep[2]);
	}
	@Override
	protected double computeStepSize( double oldStep, double exponent, double power) {
		coordinatesStep    = new double[3];
		coordinatesBigStep = new double[3];
		double step = 1.;
		switch ( coordinatesType ) {
		case DECIMAL_DEGREES:
			step = stepForDecimalDegrees( oldStep, exponent, power);
			break;
		case DEGREES_DECIMAL_MINUTES:
			step = stepForDecimalMinutes( oldStep, exponent, power);
			break;
		case DEGREES_MINUTES_SECONDS:
			step = stepForDecimalSeconds( oldStep, exponent, power);
			break;
		case UTM:
		case MGRS:
			step = stepForUTM( oldStep, exponent, power);
			break;
		}
		return step;
	}

	private double stepForDecimalDegrees( double oldStep, double exponent, double power) {
		double decimalStep;
		if ( exponent < 0. )
			decimalStep = computeFraction( oldStep, power, exponent);
		else
			decimalStep = computeDegrees( oldStep, exponent, power);
		setSteps( decimalStep, 0., 0., bigStepDegrees( decimalStep), 0., 0.);
		return decimalStep;
	}
	
	private static double bigStepDegrees( double step) {
		double bigStep  = 1.;
		if ( step >= 1. ) {
			if ( step < 5. )
				bigStep = 6.;
			else
				if ( step < 10 )
					bigStep = 30.;
				else
					if ( step < 45. )
						bigStep = 60.;
					else
						if ( step < 90. )
							bigStep = 90.;
						else
							bigStep = (step < 180. ) ? 180.: 360.;
		}
		else
			bigStep = bigStepOfFraction( step);
		return bigStep;
	}

	private double stepForDecimalMinutes( double oldStep, double exponent, double power) {
		if ( oldStep > .5 )
			return stepForDecimalDegrees(oldStep, exponent, power);
		else {
			double localStep = oldStep * 60.;
			double newStep = 1.;
			double bigStep = 1.;
			if ( localStep > .5 ) {
				 newStep      = getMultiplier( localStep, minutesSecondsMultipliers);
				 noOfDecimals = noShifts( newStep);
				 bigStep      = ( newStep < 6. ) ? 10.: 60.;
			}
			else {
				double localExponent = getExponent( localStep);
				double localPower    = getPower( localExponent);
				newStep    			 = computeFraction( localStep, localPower, localExponent);
				bigStep   			 = bigStepOfFraction( localStep);
			}
			setSteps( 0., newStep, 0., 0., bigStep, 0.);
			return newStep / 60.;
		}
	}
	
	private double stepForDecimalSeconds( double oldStep, double exponent, double power) {
		double tmpStep = oldStep * 3600.;
		if ( tmpStep > 30. )
			return stepForDecimalMinutes(oldStep, exponent, power);
		else {
			double newStep = stepForDecimalMinutes( oldStep*60., exponent, power);
			double tmp1 = coordinatesStep[1];
			double tmp2 = coordinatesBigStep[1];
			setSteps( 0., 0., tmp1, 0., 0., tmp2);
			return newStep / 60.;
		}
	}
	
	private double stepForUTM( double oldStep, double exponent, double power) {
		bigTickStep       = power * 10.;
		double multiplier = getMultiplier(  oldStep/power, utmMultipliers);
		noOfDecimals      = noOfDecimalDigits( multiplier, exponent);
		return multiplier * power;
	}
	
	private double computeDegrees( double oldStep, double exponent, double power) {
		double newStep = getMultiplier( oldStep, degreesMultipliers);
		noOfDecimals   = noShifts( newStep);
		return newStep;
	}
	
	private double computeFraction( double oldStep, double power, double exponent) {
		double multiplier = getMultiplier( oldStep/power, fractionMultipliers);
		noOfDecimals = noOfDecimalDigits( multiplier, exponent);
		return multiplier * power;
	}	
	
	private static double bigStepOfFraction( double step){
		return Math.pow( 10., Math.floor( Math.log10(step)) + 1);
	}
	
	private void setSteps( double stepDegrees,    double stepMinutes,    double stepSeconds,
						   double bigStepDegrees, double bigStepMinutes, double bigStepSeconds) {
		coordinatesStep[0]    = stepDegrees;
		coordinatesStep[1]    = stepMinutes;
		coordinatesStep[2]    = stepSeconds;
		coordinatesBigStep[0] = bigStepDegrees;
		coordinatesBigStep[1] = bigStepMinutes;
		coordinatesBigStep[2] = bigStepSeconds;
		bigTickStep = ( bigStepSeconds / 60. + bigStepMinutes ) / 60. + bigStepDegrees;
	}
}
