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

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.domain.TrackItBaseType;
import com.henriquemalheiro.trackit.business.domain.Visitor;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;

public class ListItem extends TrackItBaseType implements FolderTreeItem {
	private String itemName;
	private ImageIcon openIcon;
	private ImageIcon closedIcon;
	private ImageIcon leafIcon;
	private Runnable onSelectionEvent;
	
	public ListItem(String itemName, ImageIcon openIcon, ImageIcon closedIcon, ImageIcon leafIcon,
			Runnable onSelectionEvent) {
		this.itemName = itemName;
		this.openIcon = openIcon;
		this.closedIcon = closedIcon;
		this.leafIcon = leafIcon;
		this.onSelectionEvent = onSelectionEvent;
	}

	public ListItem(String itemName, ImageIcon icon, Runnable onSelectionEvent) {
		this(itemName, icon, icon, icon, onSelectionEvent);
	}

	@Override
	public String getFolderTreeItemName() {
		return itemName;
	}

	@Override
	public ImageIcon getOpenIcon() {
		return openIcon;
	}

	@Override
	public ImageIcon getClosedIcon() {
		return closedIcon;
	}

	@Override
	public ImageIcon getLeafIcon() {
		return leafIcon;
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		onSelectionEvent.run();
	}
	
	public FileType[] getExportFileTypes() {
		return new FileType[0];
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}
}
