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
package com.trackit.presentation.view.summary;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.trackit.presentation.utilities.ImageUtilities;

class SummaryViewSection extends JPanel {
	private static final long serialVersionUID = -2902014184403857135L;
	
	private ImageIcon expandedIcon;
	private ImageIcon collapsedIcon;
	private JLabel collapseButton;
	private boolean collapsed;
	private JPanel titlePanel;
	private JPanel contentPanel;
	
	SummaryViewSection(String title, ImageIcon icon, JPanel content, boolean collapsed) {
		this.collapsed = collapsed;
		createTitlePanel(title, icon);
		createContentPanel(content);
		initPanel();
	}
	
	private void createTitlePanel(String title, ImageIcon icon) {
		titlePanel = new JPanel();
		titlePanel.setOpaque(false);
		titlePanel.setLayout(new GridBagLayout());
		
		addCollapseButton();
		addTitle(title, icon);
		addTitleSeparator();
	}

	private void addTitleSeparator() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(5, 10, 5, 10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 0.0;
		
		JSeparator titleSeparator = new JSeparator();
		titlePanel.add(titleSeparator, c);
	}

	private void addTitle(String title, ImageIcon icon) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(5, 0, 0, 5);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 0.0;
		
		JLabel titleLabel = new JLabel(title);
		titleLabel.setIcon(icon);
		titleLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					updateState();
				}
			}
		});
		titlePanel.add(titleLabel, c);
	}

	private void addCollapseButton() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(5, 10, 0, 5);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.weighty = 0.0;
		
		collapseButton = new JLabel();
		collapseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateState();
			}
		});
		titlePanel.add(collapseButton, c);
	}
	
	private void createContentPanel(JPanel content) {
		this.contentPanel = content;
		this.contentPanel.setOpaque(false);
	}
	
	private void initPanel() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBackground(Color.WHITE);
		add(titlePanel);
		add(contentPanel);
		
		collapsedIcon = ImageUtilities.createImageIcon("collapsed_16.png");
		expandedIcon = ImageUtilities.createImageIcon("expanded_16.png");
		updateState();
	}

	private void updateState() {
		if (collapsed) {
			expand();
		} else {
			collapse();
		}
	}
	
    void collapse() {
        if (!collapsed) {
            contentPanel.setVisible(false);
            collapseButton.setIcon(collapsedIcon);
            collapsed = !collapsed;
            validate();
        }
    }

    void expand() {
        if (collapsed) {
            contentPanel.setVisible(true);
            collapseButton.setIcon(expandedIcon);
            collapsed = !collapsed;
            validate();
        }
    }
}