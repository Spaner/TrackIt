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


class Scale {
	private static final int DEFAULT_STEPS = 10;
	private static final double[] multipliers = { .1, .2, .25, .3, .4, .5, .6, .7, .75, .8, .9, 1.};
	
	private double minValue;
	private double maxValue;
	private double range;
	private double stepSize;
	private double steps;
	private double originalMinValue;
	private double originalMaxValue;
	private double originalSteps;
	private int decimals;
	private boolean margins;
	
	Scale(double minValue, double maxValue, double targetSteps, boolean margins) {
		originalMinValue = minValue;
		originalMaxValue = maxValue;
		originalSteps = Integer.MIN_VALUE;
		this.margins = margins;
		
		init(targetSteps);
	}
	
	Scale(double minValue, double maxValue, boolean margins) {
		this(minValue, maxValue, DEFAULT_STEPS, margins);
	}
	
	void setTargetSteps(int targetSteps) {
		init(targetSteps);
	}

	double getMinValue() {
		return minValue;
	}

	double getMaxValue() {
		return maxValue;
	}

	double getRange() {
		return range;
	}

	double getStepSize() {
		return stepSize;
	}

	double getSteps() {
		return steps;
	}
	
	double getOriginalMinValue() {
		return originalMinValue;
	}

	double getOriginalMaxValue() {
		return originalMaxValue;
	}

	double getOriginalSteps() {
		return originalSteps;
	}

	int getDecimals() {
		return decimals;
	}
	
	private void init(double targetSteps) {
        double tmpStep = Math.max((originalMaxValue - originalMinValue) / targetSteps, 1.0);
        double magPower = Math.pow(10.0, Math.ceil(Math.log10(tmpStep)));
        double magStep = tmpStep / magPower;
        int index = 0;
        
        while (index < multipliers.length && magStep > multipliers[index]) {
            index++;
        }
        stepSize = multipliers[index] * magPower;

        decimals = 1;
        if (index == 2 || index == 8) {
            decimals = 2;
        }
        decimals = Math.max(0, (int) (decimals - Math.log10(magPower))); 

        minValue = (margins ? Math.floor(originalMinValue / stepSize) : originalMinValue / stepSize) * stepSize;
        maxValue = (margins ? Math.ceil(originalMaxValue / stepSize) : originalMaxValue / stepSize) * stepSize;
        range = maxValue - minValue;
        steps = range / stepSize;
	}

	@Override
	public String toString() {
		return String.format("Scale [minValue=%s, maxValue=%s, range=%s, stepSize=%s, steps=%s, originalMinValue=%s,"
				+ " originalMaxValue=%s, originalSteps=%s, decimals=%s]", minValue, maxValue, range, stepSize, steps,
				originalMinValue, originalMaxValue, originalSteps, decimals);
	}
}
