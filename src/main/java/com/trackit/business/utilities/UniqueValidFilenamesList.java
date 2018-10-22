package com.trackit.business.utilities;

/*
 * This file is part of Track It!.
 * Copyright (C) 2017, 2018 João Brisson
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

// 2018-03-09, by J. Brisson
//			   - uses Files to check file existence and uniqueness of file names

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UniqueValidFilenamesList {
	
	private ArrayList<String> names = null;
	private ArrayList<Path>   paths = null;		//12335: 2018-03-09
	
	public UniqueValidFilenamesList() {
		names = new ArrayList<>();
		paths = new ArrayList<>();
	}
	
	//12335: 2018-03-09
	public UniqueValidFilenamesList( List<String> filenames) {
		names = new ArrayList<>();
		paths = new ArrayList<>();				//12335: 2018-03-09
		add( filenames);
	}
	
	//12335: 2018-03-09 - convenience function
	public ArrayList<String> add( List<String> filenames) {
		for( String filename: filenames)
			add( filename);
		return names;
	}
	
	public boolean add( String filename) {
		boolean exists = false;
		if ( ! names.contains( filename) )
//			if ( new File( filename).exists() ) {				/12335: 2018-03-09
			try {
				Path filePath = Paths.get( filename);
				if ( Files.exists( Paths.get( filename)) ) {
					for( Path path: paths)
						if ( Files.isSameFile( path, filePath))
							return false;
					names.add( filename);
					paths.add( filePath);
					exists = true;
				}
			} catch (Exception e) {}
		return exists;
	}
	
	public ArrayList<String> getFilenames() {
		return names;	
	}
}
