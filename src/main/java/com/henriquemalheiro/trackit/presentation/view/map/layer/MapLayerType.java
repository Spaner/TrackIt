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
package com.henriquemalheiro.trackit.presentation.view.map.layer;

import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.pg58406.trackit.presentation.view.map.layer.AreaLayer;
import com.pg58406.trackit.presentation.view.map.layer.MultiLayer;
import com.pg58406.trackit.presentation.view.map.layer.PhotoLayer;

public enum MapLayerType implements MapLayerFactory {
	MAP_CONTROLS_LAYER(100) {
		@Override
		public MapLayer createLayer(Map map) {
			return new MapControlsLayer(map);
		}
	},
	// 58406
	PHOTO_LAYER(24) {
		@Override
		public MapLayer createLayer(Map map) {
			return new PhotoLayer(map);
		}
	},
	// 58406
	AREA_LAYER(25) {
		@Override
		public MapLayer createLayer(Map map) {
			return new AreaLayer(map);
		}
	},
	//58406
	MULTI_LAYER(9) {
		@Override
		public MapLayer createLayer(Map map) {
			return new MultiLayer(map);
		}
	},
	EDITION_LAYER(26) {
		@Override
		public MapLayer createLayer(Map map) {
			return new EditionLayer(map);
		}
	},
	EVENTS_LAYER(23) {
		@Override
		public MapLayer createLayer(Map map) {
			return new EventsLayer(map);
		}
	},
	CUSTOM_SELECTION_LAYER(22) {
		@Override
		public MapLayer createLayer(Map map) {
			return new CustomSelectionLayer(map);
		}
	},
	START_FINISH_LAYER(20) {
		@Override
		public MapLayer createLayer(Map map) {
			return new StartFinishLayer(map);
		}
	},
	HIGHLIGHT_LAYER(19) {
		@Override
		public MapLayer createLayer(Map map) {
			return new HighlightLayer(map);
		}
	},
	COURSE_POINTS_LAYER(18) {
		@Override
		public MapLayer createLayer(Map map) {
			return new CoursePointsLayer(map);
		}
	},
	WAYPOINTS_LAYER(17) {
		@Override
		public MapLayer createLayer(Map map) {
			return new WaypointsLayer(map);
		}
	},
	SEGMENTS_LAYER(16) {
		@Override
		public MapLayer createLayer(Map map) {
			throw new UnsupportedOperationException();
		}
	},
	LAPS_LAYER(15) {
		@Override
		public MapLayer createLayer(Map map) {
			return new LapsLayer(map);
		}
	},
	SELECTION_LAYER(14) {
		@Override
		public MapLayer createLayer(Map map) {
			return new SelectionLayer(map);
		}
	},
	ROOT_ITEMS_LAYER(13) {
		@Override
		public MapLayer createLayer(Map map) {
			return new RootItemsLayer(map);
		}
	},
	BACKGROUND_MAP_LAYER(10) {
		@Override
		public MapLayer createLayer(Map map) {
			return new BackgroundMapLayer(map);
		}
	};

	private int priority;

	private MapLayerType(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}
}