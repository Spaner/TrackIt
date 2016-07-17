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
package com.henriquemalheiro.trackit.presentation;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.henriquemalheiro.trackit.business.common.FileType;

public class FileFilterFactory {
	private static FileFilterFactory instance;
	
	private FileFilterFactory() {
	}
	
	public static synchronized FileFilterFactory getInstance() {
		if (instance == null) {
			instance = new FileFilterFactory();
		}
		return instance;
	}
	
	public FileFilter getFilter(final FileType fileType) {
		FileFilter fileFilter = new FileFilter() {
			
			@Override
			public String getDescription() {
				return fileType.getFilterName();
			}
			
			@Override
			public boolean accept(File file) {
				return (file.getAbsolutePath().endsWith(fileType.getExtension())
						|| file.isDirectory());
			}
		};
		return fileFilter;
	}
	
	public FileFilter[] getFilters() {
		FileFilter[] filters = new FileFilter[FileType.values().length];
		
		int pos = 0;
		for (final FileType fileType : FileType.values()) {
			if (fileType.equals(FileType.ALL)) {
				continue;
			}
			
			filters[pos++] = new FileFilter() {
				@Override
				public String getDescription() {
					return fileType.getFilterName();
				}
				@Override
				public boolean accept(File file) {
					return (file.getAbsolutePath().endsWith(fileType.getExtension())
							|| file.isDirectory());
				}
			};
		}

		filters[filters.length - 1] = new FileFilter() {
			@Override
			public String getDescription() {
				return FileType.ALL.getFilterName();
			}
			@Override
			public boolean accept(File file) {
				boolean result = false;
				
				for (FileType fileType : FileType.values()) {
					if (fileType.equals(FileType.ALL)) {
						continue;
					}
					result |= file.getAbsolutePath().endsWith(fileType.getExtension());
				}
				result |= file.isDirectory();
				
				return result;
			}
		};
		return filters;
	}

	// 12335: 2015-08-08 - return file type given a file filter
	public FileType getFileType( FileFilter filter) {
		String description = filter.getDescription();
		for( FileType type : FileType.values()) {
			if ( type.getFilterName().equals( description) )
				return type;
		}
		return null;
	}
}
