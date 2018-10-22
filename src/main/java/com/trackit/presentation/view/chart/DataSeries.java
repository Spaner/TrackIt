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

import java.util.ArrayList;
import java.util.List;

import com.trackit.business.common.ColorScheme;
import com.trackit.business.common.ColorSchemeV2;
import com.trackit.business.common.Pair;
import com.trackit.business.common.Unit;

class DataSeries {
	private String label;
	private Scale scale;
	private Unit unit;
	private ColorScheme colorScheme;
	private boolean solid;
	private double[][] data;
	private int selectionStart;
	private int selectionLength;
	private List<Pair<Integer, Integer>> shadings;
	private List<Pair<ColorSchemeV2, Pair<Integer, Integer>>> colorShadings;
	boolean doColors;
	private Pair<Double, Double> highlight;
	
	DataSeries(String label, Scale scale, Unit unit, ColorScheme colorScheme, boolean solid, double[][] data) {
		this.label = label;
		this.scale = scale;
		this.unit = unit;
		this.colorScheme = colorScheme;
		this.solid = solid;
		this.data = data;
		
		selectionStart = 0;
		selectionLength = 0;
		shadings = new ArrayList<Pair<Integer,Integer>>();
		colorShadings = new ArrayList<Pair<ColorSchemeV2, Pair<Integer, Integer>>>();
		highlight = null;
		doColors = false;
	}

	String getLabel() {
		return label;
	}

	Scale getScale() {
		return scale;
	}

	Unit getUnit() {
		return unit;
	}

	ColorScheme getColorScheme() {
		return colorScheme;
	}
	
	boolean isSolid() {
		return solid;
	}

	double[][] getData() {
		return data;
	}
	
	double[][] getSelection() {
		return getSegmentData(selectionStart, selectionLength);
	}
	
	void setSelection(int startIndex, int endIndex) {
		this.selectionStart = startIndex;
		this.selectionLength = endIndex - startIndex + 1;
	}
	
	List<double[][]> getShadings() {
		List<double[][]> shadingsData = new ArrayList<double[][]>();
		
		for (Pair<Integer, Integer> shading : shadings) {
			double[][] data = getSegmentData(shading.getFirst(), shading.getSecond());
			shadingsData.add(data);
		}
		
		return shadingsData;
	}
	
	void setShadings(List<Pair<Integer, Integer>> shadings) {
		this.shadings.clear();
		
		for (Pair<Integer, Integer> shading : shadings) {
			int start = shading.getFirst();
			int length = shading.getSecond() - shading.getFirst() + 1;
			Pair<Integer, Integer> newShading = Pair.create(start, length);
			this.shadings.add(newShading);
		}
	}
	
	List<Pair<ColorSchemeV2, double[][]>> getColorShadings() {
		List<Pair<ColorSchemeV2, double[][]>> shadingsData = new ArrayList<Pair<ColorSchemeV2, double[][]>>();
		
		for (Pair<ColorSchemeV2, Pair<Integer, Integer>> shading : colorShadings) {
			double[][] data = getSegmentData(shading.getSecond().getFirst(), shading.getSecond().getSecond());
			Pair<ColorSchemeV2, double[][]> pairData = Pair.create(shading.getFirst(), data);
			shadingsData.add(pairData);
		}
		return shadingsData;
	}
	
	void setColorShadings(List<Pair<ColorSchemeV2, Pair<Integer, Integer>>> colorShadings) {
		this.colorShadings.clear();
		
		for (Pair<ColorSchemeV2, Pair<Integer, Integer>> shading : colorShadings) {
			int start = shading.getSecond().getFirst();
			int length = shading.getSecond().getSecond() - shading.getSecond().getFirst() + 1;
			ColorSchemeV2 color = shading.getFirst();
			Pair<ColorSchemeV2, Pair<Integer, Integer>> newShading = Pair.create(color, Pair.create(start, length));
			this.colorShadings.add(newShading);
		}
	}
	
	void setDoColors(boolean doColors){
		this.doColors = doColors;
	}
	
	 boolean doColors(){
		return this.doColors;
	}
	
	double[][] getHighlight() {
		if (highlight == null) {
			return null;
		}
		
		double[][] highlightData = new double[1][2];
		
		highlightData[0][0] = highlight.getFirst();
		highlightData[0][1] = highlight.getSecond();		

		return highlightData;
	}
	
	void setHighlight(Pair<Double, Double> highlight) {
		this.highlight = highlight;
	}
	
	private double[][] getSegmentData(int start, int length) {
		if (data == null || length == 0 || start == -1) {
			return new double[0][0];
		}
		
		double[][] segmentData = new double[length][data[0].length];
		for (int i = 0; i < segmentData.length; i++) {
			System.arraycopy(data[start + i], 0, segmentData[i], 0, segmentData[0].length);
		}
		
		return segmentData;
	}
}