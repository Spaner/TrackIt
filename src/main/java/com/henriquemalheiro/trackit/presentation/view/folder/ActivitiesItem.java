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
package com.henriquemalheiro.trackit.presentation.view.folder;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.TrackItBaseType;
import com.henriquemalheiro.trackit.business.domain.Visitor;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;

public class ActivitiesItem extends TrackItBaseType implements FolderTreeItem {
	private static ImageIcon icon = ImageUtilities.createImageIcon("activities_16.png");
	private static FileType[] exportFileTypes;
	private GPSDocument parent;
	
	static {
		exportFileTypes = new FileType[] { FileType.FIT, FileType.TCX, FileType.GPX,
				FileType.KML, FileType.CSV };
	}
	
	public ActivitiesItem(GPSDocument document) {
		this.parent = document;
	}
	
	@Override
	public DocumentItem getParent() {
		return parent;
	}

	@Override
	public String getFolderTreeItemName() {
		return getMessage("folderView.label.activities");
	}

	@Override
	public ImageIcon getOpenIcon() {
		return icon;
	}

	@Override
	public ImageIcon getClosedIcon() {
		return icon;
	}

	@Override
	public ImageIcon getLeafIcon() {
		return icon;
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.ACTIVITIES_SELECTED, parent, parent.getActivities());
	}
	
	public FileType[] getExportFileTypes() {
		return exportFileTypes;
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}
	
	/* FolderTreeItem Interface Implementation */
	
	@Override
	public boolean acceptItem(FolderTreeItem item) {
		boolean result = false;
		
		result |= (item.isActivity() && !parent.getActivities().contains(item));
		result |= (item instanceof ActivitiesItem);
		
		return result;
	}
}
