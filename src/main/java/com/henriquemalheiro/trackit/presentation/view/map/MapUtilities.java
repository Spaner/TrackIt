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
package com.henriquemalheiro.trackit.presentation.view.map;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.ColorScheme;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterStyle;
import com.pg58406.trackit.business.common.ColorSchemeV2;
import com.pg58406.trackit.business.common.ColorSchemeV2Container;

public class MapUtilities {
	
	private List<Color> multiColors;
	
	public List<Color> getMultiColors() {
		return multiColors;
	}

	public void setMultiColors(List<Color> multiColors) {
		this.multiColors = multiColors;
	}

	public MapUtilities(){
		multiColors = new ArrayList<Color>();
	}
	
	public static Map<String, Object> getPaintingAttributes(DocumentItem item) {
		Map<String, Object> paintingAttributes = new HashMap<String, Object>();
		
		paintingAttributes.put(Constants.PAINTING_ATTRIBUTES.MAP_PAINTER_STYLE, getStyle(item));
		addPaintingAttributes(paintingAttributes, getColorScheme(item), item);
		
		return paintingAttributes;
	}
	
	public static Map<String, Object> getPaintingAttributes(DocumentItem item, MapPainterStyle style) {
		Map<String, Object> paintingAttributes = new HashMap<String, Object>();
		
		paintingAttributes.put(Constants.PAINTING_ATTRIBUTES.MAP_PAINTER_STYLE, style);
		addPaintingAttributes(paintingAttributes, getColorScheme(item), item);
		
		return paintingAttributes;
	}
	
	private static MapPainterStyle getStyle(DocumentItem item) {
		MapPainterStyle style = (MapPainterStyle) item.getAttribute(
				Constants.PAINTING_ATTRIBUTES.MAP_PAINTER_STYLE);
		style = (style == null ? MapPainterStyle.REGULAR : style);
		
		return style;
	}

	private static ColorScheme getColorScheme(DocumentItem item) {
		ColorScheme colorScheme = (ColorScheme) item.getAttribute(Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME);
		
		if (colorScheme == null) {
			DocumentItem parent = item.getParent();
			if (parent != null) {
				colorScheme = (ColorScheme) parent.getAttribute(Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME);
			}
		}
		
		if (colorScheme == null) {
			colorScheme = ColorScheme.LIGHT_ORANGE;
		}
		
		return colorScheme;
	}

	private static void addPaintingAttributes(
			Map<String, Object> paintingAttributes, ColorScheme colorScheme, DocumentItem item) {// 58406

		if (item instanceof ColorSchemeV2Container && ((ColorSchemeV2Container) item).getColorSchemeV2()!=null){			
			ColorSchemeV2 cv = ((ColorSchemeV2Container) item).getColorSchemeV2();
			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.REGULAR_LINE_COLOR,
					cv.getLineColor());
			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.REGULAR_FILL_COLOR,
					cv.getFillColor());

			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.SELECTION_LINE_COLOR,
					cv.getSelectionLineColor());
			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.SELECTION_FILL_COLOR,
					cv.getSelectionFillColor());
		} else {			
			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.REGULAR_LINE_COLOR,
					colorScheme.getLineColor());
			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.REGULAR_FILL_COLOR,
					colorScheme.getFillColor());

			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.SELECTION_LINE_COLOR,
					colorScheme.getSelectionLineColor());
			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.SELECTION_FILL_COLOR,
					colorScheme.getSelectionFillColor());
		}
		//MapView mv = TrackIt.getApplicationPanel().getMapView();

	//	if (mv.getMap().getMapMode() == MapMode.MULTI) {

			

		/*} else {

			TrackItPreferences appPreferences = TrackIt.getPreferences();

			int firstFill = Integer.valueOf(appPreferences.getIntPreference(
					Constants.PrefsCategories.COLOR, null,
					Constants.ColorPreferences.FILL_RGB,
					new Color(2, 193, 249).getRGB()));
			int firstLine = Integer.valueOf(appPreferences.getIntPreference(
					Constants.PrefsCategories.COLOR, null,
					Constants.ColorPreferences.LINE_RGB,
					new Color(65, 127, 184).getRGB()));

			Color temp = new Color(firstFill);
			int tempRed = temp.getRed();
			int tempGreen = temp.getGreen();
			int tempBlue = temp.getBlue();
			Color selectionFill = new Color(255 - tempRed, 255 - tempGreen,
					255 - tempBlue);

			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.REGULAR_LINE_COLOR,
					new Color(firstLine));
			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.REGULAR_FILL_COLOR,
					new Color(firstFill));

			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.SELECTION_LINE_COLOR,
					selectionFill.darker());
			paintingAttributes.put(
					Constants.PAINTING_ATTRIBUTES.SELECTION_FILL_COLOR,
					selectionFill);*/
		
		//58406
		/*	paintingAttributes.put(Constants.PAINTING_ATTRIBUTES.REGULAR_LINE_COLOR, colorScheme.getLineColor());
		paintingAttributes.put(Constants.PAINTING_ATTRIBUTES.REGULAR_FILL_COLOR, colorScheme.getFillColor());
		
		paintingAttributes.put(Constants.PAINTING_ATTRIBUTES.SELECTION_LINE_COLOR, colorScheme.getSelectionLineColor());
		paintingAttributes.put(Constants.PAINTING_ATTRIBUTES.SELECTION_FILL_COLOR, colorScheme.getSelectionFillColor());
			
//		paintingAttributes.put(Constants.PAINTING_ATTRIBUTES.WIDTH, 7);
//		paintingAttributes.put(Constants.PAINTING_ATTRIBUTES.LINE_WIDTH, 2);*/
		//}
	}

}
