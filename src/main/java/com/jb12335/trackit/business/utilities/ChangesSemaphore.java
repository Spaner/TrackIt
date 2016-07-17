/*
 * This file is part of Track It!.
 * Copyright (C) 2015 J M Brisson Lopes
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
package com.jb12335.trackit.business.utilities;

public class ChangesSemaphore {
	private static ChangesSemaphore changesSemaphore;
	private boolean changesAreEnabled;
	
	public ChangesSemaphore() {
		changesAreEnabled = true;
	}
	
	static { changesSemaphore = new ChangesSemaphore(); }
	
	public synchronized static void Enable() {
		changesSemaphore.changesAreEnabled = true;
	}
	
	public synchronized static void Disable() {
		changesSemaphore.changesAreEnabled = false;
	}
	
	public static boolean IsEnabled() {
		return changesSemaphore.changesAreEnabled;
	}

}
