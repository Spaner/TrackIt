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
package com.trackit.business.exception;

public class TrackItException extends Exception {
	private static final long serialVersionUID = -772739788700749336L;

	public TrackItException() {
	}
	
	public TrackItException(String message) {
		super(message);
	}
	
	public TrackItException(Throwable cause) {
		super(cause);
	}
	
	public TrackItException(String message, Throwable cause) {
		super(message, cause);
	}
}
