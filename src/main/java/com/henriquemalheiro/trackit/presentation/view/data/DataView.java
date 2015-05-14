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
package com.henriquemalheiro.trackit.presentation.view.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.presentation.event.EventListener;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;

public class DataView extends JPanel implements EventListener, EventPublisher, ListSelectionListener, MouseListener, ActionListener {
	private static final long serialVersionUID = -8544995782003279481L;
	
	private JTable dataTable;
	private JComboBox<DataType> dataTypeCombo;
	private JLabel selectedItemValue; 
	private DocumentItem selectedElement;
	private List<DataType> displayableElements;
	private List<? extends DocumentItem> displayedElements;
	
	public DataView() {
		setLayout(new GridLayout(1, 0));
		setOpaque(true);
		setBackground(Color.WHITE);
		
		init();
	}
	
	private void init() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		TableModel model = new DefaultTableModel();
		dataTable = new JTable(model);
		dataTable.setBackground(Color.WHITE);
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		dataTable.getSelectionModel().addListSelectionListener(this);
		updateDisplayedElements(DataType.NONE);
		dataTable.addMouseListener(this);
		
		JScrollPane scrollPane = new JScrollPane(dataTable,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBackground(Color.WHITE);
		
		JLabel selectedItemLabel = new JLabel(String.format("%s:", Messages.getMessage("dataView.selectedItemLabel")));
		selectedItemValue = new JLabel();
		JLabel dataTypeLabel = new JLabel(Messages.getMessage("dataView.dataTypeLabel"));
		dataTypeCombo = new JComboBox<DataType>();
		dataTypeCombo.setMaximumSize(new Dimension(150, (int) dataTypeCombo.getPreferredSize().getHeight()));
		dataTypeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DataType type = (DataType) dataTypeCombo.getSelectedItem();
				updateDisplayedElements(type);
			}
		});
		updateDisplayableElements();
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(selectedItemLabel)
								.addComponent(selectedItemValue))
						.addGroup(layout.createSequentialGroup()
								.addComponent(dataTypeLabel)
								.addComponent(dataTypeCombo))
						.addComponent(scrollPane));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(selectedItemLabel)
								.addComponent(selectedItemValue))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(dataTypeLabel)
								.addComponent(dataTypeCombo))
						.addComponent(scrollPane));
		
		add(panel);
	}
	
	private void updateDisplayableElements() {
		DataType displayedElement = (DataType) dataTypeCombo.getSelectedItem();
		
		DefaultComboBoxModel<DataType> model = new DefaultComboBoxModel<DataType>();
		displayableElements = Collections.emptyList();
		
		if (selectedElement != null) {
			displayableElements = selectedElement.getDisplayableElements();
	
			if (displayableElements.size() > 0) {
				DataType[] elements = displayableElements.toArray(new DataType[displayableElements.size()]);
				model = new DefaultComboBoxModel<>(elements);
			}
		}
		
		dataTypeCombo.setModel(model);
		dataTypeCombo.setEnabled(displayableElements.size() > 0);
		if (displayedElement != null) {
			dataTypeCombo.setSelectedItem(displayedElement);
		}

		if (displayableElements.size() > 0) {
			updateDisplayedElements((DataType) dataTypeCombo.getSelectedItem());
		}
	}
	
	private void updateDisplayedElements(DataType dataType) {
		displayedElements = Collections.emptyList();
		
		if (dataType != DataType.NONE && selectedElement != null) {
			displayedElements = selectedElement.getDisplayedElements(dataType);
		}
		
		TableModel model = new DataViewTableModel(dataType, displayedElements);
		dataTable.setModel(model);
	}
	
	/* Event Listener interface implementation */
	
	@Override
	public void process(com.henriquemalheiro.trackit.presentation.event.Event event, DocumentItem item) {
		DataType selectedDataType = (DataType) dataTypeCombo.getSelectedItem();
		
		switch (event) {
		case DOCUMENT_SELECTED:
		case ACTIVITY_SELECTED:
		case ACTIVITY_UPDATED://58406
		case COURSE_SELECTED:
		case COURSE_UPDATED:
			processItemSelected(item);
			break;
		case SESSION_SELECTED:
			processItemSelected(item);
			if (selectedDataType == DataType.SESSION) {
				displaySelection(item);
			}
			break;
		case LAP_SELECTED:
			processItemSelected(item);
			if (selectedDataType == DataType.LAP) {
				displaySelection(item);
			}
			break;
		case SEGMENT_SELECTED:
			processItemSelected(item);
			if (selectedDataType == DataType.TRACK_SEGMENT) {
				displaySelection(item);
			}
			break;
		case TRACKPOINT_SELECTED:
			processItemSelected(item.getParent());
			if (selectedDataType == DataType.TRACKPOINT) {
				displaySelection(item);
			}
			break;
		case TRACKPOINT_HIGHLIGHTED:
			// Do nothing
			break;
		case ZOOM_TO_ITEM:
			// Do nothing
			break;
		default:
			setSelectedItem(null);
			updateDisplayableElements();
			updateDisplayedElements(DataType.NONE);
		}
	}

	private void processItemSelected(DocumentItem item) {
		if (!item.equals(selectedElement)) {
			setSelectedItem(item);
		}
		updateDisplayableElements();
	}
	
	private void setSelectedItem(DocumentItem item) {
		selectedElement = item;
		selectedItemValue.setText(item != null ? item.getDocumentItemName() : "");
	}
	
	private void displaySelection(DocumentItem selectedItem) {
		if (selectedItem != null) {
			int index = displayedElements.indexOf(selectedItem);
			if (index != -1) {
				dataTable.setRowSelectionInterval(index, index);
				dataTable.scrollRectToVisible(new Rectangle(dataTable.getCellRect(index, 0, true)));
			}
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		publishSelectionEvent();
	}

	private void publishSelectionEvent() {
		if (dataTable.getSelectedRowCount() == 1) {
			int selectedRowIndex = dataTable.getSelectedRow();
			if (selectedRowIndex == -1) {
				return;
			}
			
			DocumentItem selectedItem = displayedElements.get(selectedRowIndex);
			selectedItem.publishSelectionEvent(this);
		} else if (dataTable.getSelectedRowCount() > 0) {
			int selectionStart = dataTable.getSelectedRows()[0];
			int selectionEnd = dataTable.getSelectedRows()[dataTable.getSelectedRowCount() - 1] + 1;
			publishItemsList(displayedElements.subList(selectionStart, selectionEnd));
		}
	}
	
	private void publishItemsList(List<? extends DocumentItem> itemsList) {
		DataType dataType = (DataType) dataTypeCombo.getSelectedItem();
		
		switch (dataType) {
		case TRACKPOINT:
			publishSegment(itemsList);
			break;
		default:
		}
	}

	private void publishSegment(List<? extends DocumentItem> itemsList) {
		List<Trackpoint> trackpoints = new ArrayList<>();
		for (DocumentItem item : itemsList) {
			trackpoints.add((Trackpoint) item);
		}
		
		TrackSegment segment = new TrackSegment(selectedElement);
		segment.setTrackpoints(trackpoints);
		segment.consolidate(ConsolidationLevel.RECALCULATION);
		segment.publishSelectionEvent(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1) {
			if (dataTable.getSelectedRowCount() == 1) {
				int selectedRowIndex = dataTable.getSelectedRow();
				if (selectedRowIndex == -1) {
					return;
				}
				
				DocumentItem selectedItem = displayedElements.get(selectedRowIndex);
				EventManager.getInstance().publish(DataView.this,
						com.henriquemalheiro.trackit.presentation.event.Event.ZOOM_TO_ITEM, selectedItem);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
//	    int rowindex = dataTable.getSelectedRow();
//	    if (rowindex < 0)
//	        return;
//	    if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
//	        JPopupMenu popup = createPopupMenu();
//	        popup.show(e.getComponent(), e.getX(), e.getY());
//	    }
	}
	
//	private void publishSegment(List<Trackpoint> trackpoints) {
//		if (trackpoints.isEmpty()) {
//			return;
//		}
//		
//		TrackSegment segment = new TrackSegment(trackpoints.get(0).getParent());
//		segment.setTrackpoints(trackpoints);
//		segment.publishSelectionEvent(DataView.this);
//	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	
//	private JPopupMenu createPopupMenu() {
//		JPopupMenu popupMenu = new JPopupMenu();
//		
//		JMenuItem menuItem = new JMenuItem(Messages.getMessage("dataView.action.delete"));
//		menuItem.setActionCommand("deleteTrackpoints");
//		menuItem.addActionListener(this);
//		popupMenu.add(menuItem);
//		
//		return popupMenu;
//	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("deleteTrackpoints")) {
			deleteTrackpoints();
		}
	}
	
	private void deleteTrackpoints() {
//		int selectionStart = dataTable.getSelectedRows()[0];
//		int selectionEnd = dataTable.getSelectedRows()[dataTable.getSelectedRowCount() - 1];
//		
//		List<Trackpoint> selectedTrackpoints = trackpoints.subList(selectionStart, selectionEnd + 1);
//		if (selectedTrackpoints.size() == 0) {
//			return;
//		}
//		
//		EventManager.getInstance().publish(this, com.henriquemalheiro.trackit.presentation.event.Event.TRACKPOINTS_REMOVED,
//				selectedTrackpoints.get(0).getParent(), selectedTrackpoints);
//		
//		selectedTrackpoints.clear();
	}
	
	@Override
	public void process(com.henriquemalheiro.trackit.presentation.event.Event event,
			DocumentItem parent, List<? extends DocumentItem> items) {
		
		switch (event) {
		case SESSIONS_SELECTED:
		case LAPS_SELECTED:
		case EVENTS_SELECTED:
		case DEVICES_SELECTED:
		case COURSE_POINTS_SELECTED:
			processItemSelected(parent);
			break;
		default:
			setSelectedItem(null);
			updateDisplayableElements();
			updateDisplayedElements(DataType.NONE);
		}
	}
	
	@Override
	public String toString() {
		return Messages.getMessage("view.data.name");
	}
}