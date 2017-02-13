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

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

class FolderTree extends JTree {
	private static final long serialVersionUID = 8851456967186889956L;
	
	public FolderTree(DefaultTreeModel model) {
		super(model);
	}

	@Override
	public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		FolderTreeItem item = (FolderTreeItem) node.getUserObject();

		return item.getFolderTreeItemName();
	}
}

class TreeRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 501645313479142300L;

	public TreeRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
    		boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
        
    	DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        FolderTreeItem item = (FolderTreeItem) node.getUserObject();
        
        setOpenIcon(item.getOpenIcon());
        setClosedIcon(item.getClosedIcon());
        setLeafIcon(item.getLeafIcon());

        super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
        
        return this;
    }
}

class FolderTreeModelListener implements TreeModelListener {
	
	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		DefaultMutableTreeNode node;
		node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());

		int index = e.getChildIndices()[0];
		node = (DefaultMutableTreeNode) (node.getChildAt(index));
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
	}
}