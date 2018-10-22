/*
 * This file is part of Track It!.
 * Copyright (C) 2018 J Brisson Lopes
 * based on: Copyright (C) 2015 Pedro Gomes
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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.trackit.TrackIt;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Messages;
import com.trackit.business.common.OperatingSystem;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.Pause;
import com.trackit.business.domain.TrackStatus;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.OperationConfirmationDialog;

public class NonReportingPauseDetectionOperation extends PauseDetectionOperation {
	
	public NonReportingPauseDetectionOperation() {
		super();
	}

	public NonReportingPauseDetectionOperation(Map<String, Object> options) {
		super(options);
		// TODO Auto-generated constructor stub
	}
	
	public int process( DocumentItem item) {
		Map<String, Object> consolidationOptions = new HashMap<String, Object>();
		consolidationOptions.put( Constants.ConsolidationOperation.LEVEL,
								  ConsolidationLevel.SUMMARY);
		int noPauses = 0;
		try {
			TrackStatus.disableChanges();
			if ( item.isCourse() ) {
				new ConsolidationOperation( consolidationOptions).process( (Course) item);
				noPauses = ( (Course) item).getPauses().size();
			}
			else {
				new ConsolidationOperation( consolidationOptions).process( (Activity) item);
				noPauses = ( (Activity) item).getPauses().size();
			}
		} catch (TrackItException e) {
			e.printStackTrace();
		} finally {
			TrackStatus.enableChanges();
		}
		return noPauses;
	}
	}
