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
package com.trackit.presentation.view.folder;

import javax.swing.ImageIcon;

import com.trackit.business.common.FileType;
import com.trackit.business.domain.TrackItBaseType;
import com.trackit.business.domain.Visitor;
import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;

public class TextItem extends TrackItBaseType implements FolderTreeItem {
	private String itemName;
	private ImageIcon openIcon;
	private ImageIcon closedIcon;
	private ImageIcon leafIcon;
	private Runnable eventPublisher;
	
	public TextItem(String itemName, ImageIcon openIcon, ImageIcon closedIcon, ImageIcon leafIcon, Runnable eventPublisher) {
		this.itemName = itemName;
		this.openIcon = openIcon;
		this.closedIcon = closedIcon;
		this.leafIcon = leafIcon;
		this.eventPublisher = eventPublisher;
	}

	public TextItem(String itemName, ImageIcon icon, Runnable eventPublisher) {
		this(itemName, icon, icon, icon, eventPublisher);
	}
	
	public String getFolderTreeItemName() {
		return itemName;
	}

	public ImageIcon getOpenIcon() {
		return openIcon;
	}

	public ImageIcon getClosedIcon() {
		return closedIcon;
	}

	public ImageIcon getLeafIcon() {
		return leafIcon;
	}

	public void publishSelectionEvent(FolderView view) {
		eventPublisher.run();
	}
	
	public FileType[] getExportFileTypes() {
		return new FileType[0];
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.NOTHING_SELECTED, this);
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}
}
