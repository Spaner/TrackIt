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
package com.pg58406.trackit.business.utility;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.presentation.view.map.MapView;
import com.pg58406.trackit.business.common.ColorSchemeV2;
import com.pg58406.trackit.business.common.ColorSchemeV2Container;

public class MultipleViewMenu extends JDialog {
	private static final long serialVersionUID = 1619730283101496436L;

	private final List<DocumentItem> selectedItems;

	public MultipleViewMenu() {
		super(TrackIt.getApplicationFrame());
		selectedItems = new ArrayList<DocumentItem>();
		initComponents();
	}

	public void initComponents() {
		// JLabel lblOptions = new
		// JLabel(Messages.getMessage("editionToolbar.title.options"));
		// lblOptions.setFont(lblOptions.getFont().deriveFont(Font.BOLD));
		this.add(populateMenu());
		pack();
		setModal(false);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocation(10, getParent().getY() + 100);
	}

	public JScrollPane populateMenu() {
		List<GPSDocument> documents = DocumentManager.getInstance()
				.getDocuments();
		List<ColorSchemeV2Container> items = new ArrayList<ColorSchemeV2Container>();
		for (GPSDocument d : documents) {
			items.addAll(d.getActivities());
			items.addAll(d.getCourses());
		}
		return createMenu(items);
	}

	private JScrollPane createMenu(List<ColorSchemeV2Container> items) {
		JSeparator titleSeparator = new JSeparator();
		List<JCheckBox> chkBoxList = new ArrayList<JCheckBox>();
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
			button.addActionListener(new ActionListener() {
				
				@SuppressWarnings("deprecation")
				@Override
				public void actionPerformed(ActionEvent e) {
					JColorChooser chooser = getColorChooser(button.getBackground());				
					JDialog dialog = JColorChooser.createDialog(null, "Change Trace Color", true, chooser, null, null);
					dialog.show();
					Color newColor = chooser.getColor();
					if (newColor != null) {
						button.setBackground(newColor);
						
						//Color newColor = button.getBackground();
											
						int tempRed = newColor.getRed();
						int tempGreen = newColor.getGreen();
						int tempBlue = newColor.getBlue();
						Color selectionFill = new Color(255-tempRed, 255-tempGreen, 255-tempBlue);						
						
						ColorSchemeV2 colorScheme = new ColorSchemeV2(newColor, newColor.darker(), selectionFill.darker(), selectionFill);
						item.setColorSchemeV2(colorScheme);
						MapView mv = TrackIt.getApplicationPanel().getMapView();
						mv.updateDisplay();;
					}
				}
			});

			JCheckBox chkItemSelected = new JCheckBox(
					item.getDocumentItemName(), false);
			chkItemSelected.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final boolean selected = ((JCheckBox) e.getSource())
							.isSelected();
					if (selected) {
						selectedItems.add((DocumentItem) item);						
					} else {
						selectedItems.remove(item);
					}
					MapView mv = TrackIt.getApplicationPanel().getMapView();
					mv.setItems(selectedItems);
					mv.updateDisplay();;
				}
			});			
			
			chkBoxList.add(chkItemSelected);
			buttonList.add(button);
			colorList.add(button.getBackground());
		}

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.ParallelGroup hGroup = layout
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.SequentialGroup sGroup = layout.createSequentialGroup();
		GroupLayout.ParallelGroup chkBoxGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.ParallelGroup colorButtonGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);  

		for (int i = 0; i < chkBoxList.size(); i++) {
			chkBoxGroup.addComponent(chkBoxList.get(i));
			colorButtonGroup.addComponent(buttonList.get(i));
		}
		
		sGroup.addGroup(chkBoxGroup);
		sGroup.addGroup(colorButtonGroup);		
		hGroup.addGroup(sGroup);

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

		for (int i = 0; i < chkBoxList.size(); i++) {
			vGroup.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(chkBoxList.get(i))
					.addComponent(buttonList.get(i)));
		}

		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		setLayout(layout);

		return outerPanel;
	}
	
	private JColorChooser getColorChooser(Color color) {
		JColorChooser chooser = new JColorChooser(color);
		chooser.setPreviewPanel(new JPanel());

		AbstractColorChooserPanel[] panels = chooser.getChooserPanels();

		for (AbstractColorChooserPanel p : panels) {
			String displayName = p.getDisplayName();
			switch (displayName) {
			case "HSV":
				chooser.removeChooserPanel(p);
				break;
			case "HSL":
				chooser.removeChooserPanel(p);
				break;
			case "CMYK":
				chooser.removeChooserPanel(p);
				break;
			}
		}
				
		return chooser;
	}

}
