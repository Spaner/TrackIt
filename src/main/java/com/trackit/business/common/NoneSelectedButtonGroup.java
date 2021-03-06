package com.trackit.business.common;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

import com.trackit.TrackIt;
import com.trackit.presentation.view.map.MapView;
import com.trackit.presentation.view.map.Map.MapMode;

public class NoneSelectedButtonGroup extends ButtonGroup {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4834042136947349221L;

	@Override
	public void setSelected(ButtonModel model, boolean selected) {
		if (selected) {
			super.setSelected(model, selected);
		} else {
			clearSelection();
			MapView mv = TrackIt.getApplicationPanel().getMapView();
			mv.getMap().setMode(MapMode.SELECTION);
			//mv.getSelectButton().setSelected(true);
		}
	}
}