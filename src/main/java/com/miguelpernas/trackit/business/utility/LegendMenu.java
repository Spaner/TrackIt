/*
 * This file is part of Track It!.
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
package com.miguelpernas.trackit.business.utility;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.presentation.view.map.MapView;
import com.pg58406.trackit.business.common.ColorSchemeV2;
import com.pg58406.trackit.business.common.ColorSchemeV2Container;

public class LegendMenu extends JDialog {
	private static final long serialVersionUID = 1619730283101496436L;

	private Course selectedCourse;

	public LegendMenu() {
		super(TrackIt.getApplicationFrame());
		selectedCourse = DocumentManager.getInstance().getSelectedCourse();
		initComponents();
	}

	public void initComponents() {

		this.add(populateMenu());
		pack();
		setModal(false);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocation(10, getParent().getY() + 100);
	}

	private List<ColorSchemeV2Container> sortSegments(List<ColorSchemeV2Container> items) {
		TrackSegment segment;
		//TrackSegment lowestSegment = (TrackSegment) items.get(1);
		List<ColorSchemeV2Container> sortedItems = new ArrayList<ColorSchemeV2Container>();
		List<TrackSegment> sortedSegments = new ArrayList<TrackSegment>();
		sortedItems.add(items.get(0));
		items.remove(0);
		for(int i = 0; i<items.size();i++){
			segment = (TrackSegment) items.get(i);
			sortedSegments.add(segment);
		}
		Collections.sort(sortedSegments);
		sortedItems.addAll(sortedSegments);
		/*double lowest = lowestSegment.getLowerInterval();
		double secondLowest = lowestSegment.getLowerInterval();
		while (sortedItems.size() != items.size()) {
			for (int i = 1; i < items.size(); i++) {
				segment = (TrackSegment) items.get(i);
				if (segment.getLowerInterval() < lowest && !sortedItems.contains(segment)) {
					secondLowest = lowest;
					lowest = segment.getLowerInterval();
					lowestSegment = segment;
				}
			}
			sortedItems.add(lowestSegment);
			lowest = secondLowest;
		}*/

		return sortedItems;
	}

	public JScrollPane populateMenu() {
		List<ColorSchemeV2Container> items = new ArrayList<ColorSchemeV2Container>();
		List<ColorSchemeV2Container> sortedItems = new ArrayList<ColorSchemeV2Container>();
		/*
		 * for (GPSDocument d : documents) { items.addAll(d.getActivities());
		 * items.addAll(d.getCourses()); }
		 */
		// items.clear();
		items.add(selectedCourse);
		ColorSchemeV2 color;
		int red, usedRed, green, usedGreen, blue, usedBlue;
		boolean containsColor = false;
		List<ColorSchemeV2> usedColors = new ArrayList<ColorSchemeV2>();
		if (selectedCourse.getSegments() != null) {
			for (TrackSegment segment : selectedCourse.getSegments()) {
				color = segment.getColorSchemeV2();
				if (usedColors.isEmpty()) {
					items.add(segment);
					usedColors.add(color);
				} else {
					color: for (ColorSchemeV2 usedColor : usedColors) {
						red = color.getFillColor().getRed();
						usedRed = usedColor.getFillColor().getRed();
						green = color.getFillColor().getGreen();
						usedGreen = usedColor.getFillColor().getGreen();
						blue = color.getFillColor().getBlue();
						usedBlue = usedColor.getFillColor().getBlue();
						// Color current = new Color(red, green, blue, 255);
						// Color iter = new Color(usedRed, usedGreen, usedBlue,
						// 255);
						if (red == usedRed && green == usedGreen && blue == usedBlue) {
							containsColor = true;
							break color;
						}
					}
					if (!containsColor) {
						items.add(segment);
						usedColors.add(color);
						// containsColor = false;
					}
					containsColor = false;
				}
			}
			sortedItems = sortSegments(items);
		}

		return createMenu(sortedItems);
	}

	private JScrollPane createMenu(List<ColorSchemeV2Container> items) {
		setTitle(Messages.getMessage("legendDialog.title"));
		JSeparator titleSeparator = new JSeparator();
		List<JLabel> labelList = new ArrayList<JLabel>();
		List<JButton> buttonList = new ArrayList<JButton>();
		List<Color> colorList = new ArrayList<Color>();

		titleSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));

		JScrollPane outerPanel = new JScrollPane();

		for (final ColorSchemeV2Container item : items) {

			final JButton button = new JButton();

			ColorSchemeV2 colorScheme = item.getColorSchemeV2();
			button.setBackground(colorScheme.getFillColor());

			Dimension d = button.getMaximumSize();
			d.height = (int) Math.floor(d.height * 1.5);
			button.setMaximumSize(d);

			JLabel labelItemSelected = new JLabel(item.getDocumentItemName());

			labelList.add(labelItemSelected);
			buttonList.add(button);
			colorList.add(button.getBackground());
		}

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.SequentialGroup sGroup = layout.createSequentialGroup();
		GroupLayout.ParallelGroup labelBoxGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.ParallelGroup colorButtonGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

		for (int i = 0; i < labelList.size(); i++) {
			labelBoxGroup.addComponent(labelList.get(i));
			colorButtonGroup.addComponent(buttonList.get(i));
		}

		sGroup.addGroup(labelBoxGroup);
		sGroup.addGroup(colorButtonGroup);
		hGroup.addGroup(sGroup);

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

		for (int i = 0; i < labelList.size(); i++) {
			vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelList.get(i))
					.addComponent(buttonList.get(i)));
		}

		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		setLayout(layout);

		return outerPanel;
	}

	public void update() {
		this.selectedCourse = DocumentManager.getInstance().getSelectedCourse();
		this.initComponents();
		this.revalidate();
		this.repaint();
	}

}
