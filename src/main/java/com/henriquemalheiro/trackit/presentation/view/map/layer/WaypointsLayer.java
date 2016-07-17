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
package com.henriquemalheiro.trackit.presentation.view.map.layer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.utilities.Operation;
import com.henriquemalheiro.trackit.presentation.utilities.swing.TrackItTextField;
import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.MapUtilities;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterStyle;

public class WaypointsLayer extends MapLayer implements EventPublisher {
	private static final long serialVersionUID = -3345263425389015474L;

	private Set<GPSDocument> documents;

	public WaypointsLayer(Map map) {
		super(map);
		init();
	}

	private void init() {
		documents = new HashSet<GPSDocument>();
	}

	@Override
	public MapLayerType getType() {
		return MapLayerType.WAYPOINTS_LAYER;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;

		super.paintComponent(graphics);

		documents.clear();
		List<Waypoint> waypoints = getWaypoints();

		for (Waypoint waypoint : waypoints) {
			if (getItems().contains(waypoint)) {
				continue;
			}

			java.util.Map<String, Object> attributes = MapUtilities.getPaintingAttributes(waypoint,
			        MapPainterStyle.REGULAR);
			waypoint.paint(graphics, this, attributes);
		}
	}

	@Override
	public List<DocumentItem> getItems(Location location) {
		List<Waypoint> waypoints = getWaypoints();
		List<DocumentItem> items = new ArrayList<DocumentItem>();
		for (Waypoint waypoint : waypoints) {
			double distance = Utilities.getGreatCircleDistance(location.getLatitude(), location.getLongitude(),
			        waypoint.getLatitude(), waypoint.getLongitude()) * 1000.0;
			if (distance < 10.0) {
				items.add(waypoint);
			}
		}

		return items;
	}

	private List<Waypoint> getWaypoints() {
		for (DocumentItem item : getItems()) {
			GPSDocument document = getDocument(item);

			if (document != null) {
				documents.add(document);
			}
		}

		List<Waypoint> waypoints = new ArrayList<Waypoint>();
		for (GPSDocument document : documents) {
			waypoints.addAll(document.getWaypoints());
		}
		return waypoints;
	}

	private GPSDocument getDocument(DocumentItem item) {
		GPSDocument document = null;

		while (item != null && !(item instanceof GPSDocument)) {
			item = item.getParent();
		}

		if (item != null && item instanceof GPSDocument) {
			document = (GPSDocument) item;
		}

		return document;
	}

	@Override
	public List<Operation> getSupportedOperations(final Location location) {
		List<Operation> supportedOperations = new ArrayList<Operation>();

		if (!documents.isEmpty()/* && map.getMapMode().equals(MapMode.EDITION) */) {
			Operation createWaypointOperation = new Operation(Messages.getMessage("operation.group.createWaypoint"),
			        Messages.getMessage("operation.name.createWaypoint"),
			        Messages.getMessage("operation.description.createWaypoint"), new Runnable() {
				        @Override
				        public void run() {
					        JDialog dialog = new CreateWaypointDialog(documents.iterator().next(), location);
					        dialog.setVisible(true);
				        }
			        });
			supportedOperations.add(createWaypointOperation);
		}

		return supportedOperations;
	}

	private class CreateWaypointDialog extends JDialog {
		private static final long serialVersionUID = 1878215331845182583L;
		private GPSDocument document;
		private Location location;

		CreateWaypointDialog(GPSDocument document, Location location) {
			super(TrackIt.getApplicationFrame());

			this.document = document;
			this.location = location;

			initComponents();
		}

		private void initComponents() {
			GroupLayout layout = new GroupLayout(getContentPane());
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			setLayout(layout);
			setTitle(Messages.getMessage("dialog.createWaypoint.title"));

			JLabel lblDescription = new JLabel(Messages.getMessage("dialog.waypoint.create"));
			JLabel lblName = new JLabel(Messages.getMessage("dialog.waypoint.name"));
			final JTextField txtName = new TrackItTextField(15);
			txtName.setColumns(15);

			JButton cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
			cmdOk.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					if (txtName.getText() == null || txtName.getText().isEmpty()) {
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
						        Messages.getMessage("dialog.waypointName.mandatory"),
						        Messages.getMessage("messages.warning"), JOptionPane.WARNING_MESSAGE);
						return;
					}

					Waypoint waypoint = new Waypoint();
					waypoint.setLatitude(location.getLatitude());
					waypoint.setLongitude(location.getLongitude());
					waypoint.setAltitude(location.getAltitude());
					waypoint.setName(txtName.getText());
					document.add(waypoint);

					EventManager.getInstance().publish(WaypointsLayer.this, Event.DOCUMENT_UPDATED, document);

					CreateWaypointDialog.this.dispose();
				}
			});
			JButton cmdCancel = new JButton(Messages.getMessage("trackIt.cmdCancel"));
			cmdCancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					dispose();
				}
			});

			layout.setHorizontalGroup(layout
			        .createParallelGroup(GroupLayout.Alignment.LEADING)
			        .addComponent(lblDescription)
			        .addGroup(
			                layout.createSequentialGroup()
			                        .addGroup(
			                                layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
			                                        lblName))
			                        .addGroup(
			                                layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			                                        .addComponent(txtName)
			                                        .addGroup(
			                                                layout.createSequentialGroup().addComponent(cmdOk)
			                                                        .addComponent(cmdCancel)))));

			layout.setVerticalGroup(layout
			        .createSequentialGroup()
			        .addComponent(lblDescription)
			        .addGroup(
			                layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblName)
			                        .addComponent(txtName))
			        .addGroup(
			                layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(cmdOk)
			                        .addComponent(cmdCancel)));

			pack();
			setModal(true);
			setLocationRelativeTo(TrackIt.getApplicationFrame());
		}
	}
}
