/*
 * This file is part of Track It!.
 * Copyright (C) 2018 João Brisson
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
package com.trackit.business.utilities;

import static com.trackit.business.common.Messages.getMessage;

import javax.swing.JOptionPane;

import com.trackit.TrackIt;

public class OperationConfirmationDialog {

	public static boolean showOperationConfirmationDialog( String message, String title) {
		Object[] options = { getMessage( "messages.yes"), getMessage( "messages.no")};
		int result = JOptionPane.showOptionDialog( 
				TrackIt.getApplicationFrame(), 
				message, title, 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, options[0]);
		return result == JOptionPane.YES_OPTION;
	}
	
	public static void showOperationCompletionDialog( String message, String title) {
		JOptionPane.showMessageDialog( TrackIt.getApplicationFrame(),
				message, title, JOptionPane.INFORMATION_MESSAGE);
	}
}
