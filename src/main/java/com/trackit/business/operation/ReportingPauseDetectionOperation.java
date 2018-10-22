/*
 * This file is part of Track It!.
 * Copyright (C) 2017, 2018 J Brisson Lopes
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
import java.util.Map;

import javax.swing.JOptionPane;

import com.trackit.TrackIt;
import com.trackit.business.common.Messages;
import com.trackit.business.common.OperatingSystem;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.utilities.OperationConfirmationDialog;

public class ReportingPauseDetectionOperation extends NonReportingPauseDetectionOperation {

	public ReportingPauseDetectionOperation() {
		super();
	}
	
	public ReportingPauseDetectionOperation( Map<String, Object> options) {
		super( options);
	}
	
	public int process( DocumentItem item) {
		int noPauses = super.process( item);
		if ( noPauses > 0 ) {
			if ( OperationConfirmationDialog.showOperationConfirmationDialog( 
					Messages.getMessage( "applicationPanel.menu.detectPauses.success", getTextFilename()),
					Messages.getMessage( "applicationPanel.menu.detectPauses.success.description")) ) {
				try {
					if ( OperatingSystem.isWindows() ) {
						String cmd = "rundll32 url.dll,FileProtocolHandler " + getTextFilename();
						Runtime.getRuntime().exec( cmd);
					}
					else {
						Desktop.getDesktop().edit( new File( getTextFilename()));
					}
				} catch ( IOException e) {
					logger.error( e.getClass().getName() + ": " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		// no pauses detected
		else {
			logger.warn( Messages.getMessage( "applicationPanel.menu.detectPauses.failure"));
			JOptionPane.showMessageDialog( TrackIt.getApplicationFrame(), 
					                       Messages.getMessage( "applicationPanel.menu.detectPauses.failure"));
		}
		return noPauses;
	}
}
