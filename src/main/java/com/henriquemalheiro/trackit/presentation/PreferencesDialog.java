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

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.Unit;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.business.operation.ConsolidationOperation;
import com.henriquemalheiro.trackit.business.utility.Connection;
import com.henriquemalheiro.trackit.business.utility.ConnectionProperties;
import com.henriquemalheiro.trackit.business.utility.TrackItPreferences;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.presentation.task.Action;
import com.henriquemalheiro.trackit.presentation.task.Task;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.utilities.swing.TrackItTextField;
import com.henriquemalheiro.trackit.presentation.view.map.MapView;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayerType;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MilitaryMapResolution;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MilitaryMapsProvider;
import com.henriquemalheiro.trackit.presentation.view.map.provider.RoutingType;
import com.miguelpernas.trackit.business.common.JoinOptions;
import com.miguelpernas.trackit.presentation.JoinSpeedOptions;

public class PreferencesDialog extends JDialog {
	private static final long serialVersionUID = -2244160308153565900L;

	private static final String APPLICATION_PREFERENCES = "Application Preferences";
	private static final String CONNECTION_PREFERENCES = "Connection Preferences";
	private static final String MAPS_PREFERENCES = "Maps Preferences";
	private static final String CHARTS_PREFERENCES = "Charts Preferences";
	private static final String MILITARY_MAPS_PREFERENCES = "Military Maps Preferences";
	private static final String JOIN_PREFERENCES = "Join Preferences";
	private static final String COLOR_PREFERENCES = "Color Customization";// 58406
	private static final String PAUSE_PREFERENCES = "Pause Detection Preferences";// 58406
	private int savedTraceColor = appPreferences.getIntPreference(Constants.PrefsCategories.COLOR, null,
			Constants.ColorPreferences.FILL_RGB, 65535);// 58406
	private int savedFrameColor = appPreferences.getIntPreference(Constants.PrefsCategories.COLOR, null,
			Constants.ColorPreferences.THUMBNAIL_FRAME_COLOR, 16711680);// 58406

	private JFrame application;
	private JPanel preferencesContent;
	private Double joinSpeedValue;
	private Double joinTimeValue;
	private static TrackItPreferences appPreferences = TrackIt.getPreferences();
	private List<PreferenceTask> preferencesToApply = new ArrayList<PreferenceTask>();

	public PreferencesDialog(JFrame application) {
		super(application, getMessage("preferencesDialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
		initComponents();

		this.application = application;
	}

	private void initComponents() {
		preferencesContent = new JPanel(new CardLayout());
		preferencesContent.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		preferencesContent.setPreferredSize(new Dimension(400, 200));

		JPanel navigation = new JPanel();
		navigation.setLayout(new GridLayout(1, 0));
		DefaultTreeCellRenderer renderer = new TreeRenderer();
		DefaultMutableTreeNode navigationRootNode = new DefaultMutableTreeNode(Constants.APP_NAME);
		createPreferencesNodes(navigationRootNode);
		JTree navigationTree = new JTree(navigationRootNode);
		navigationTree.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		navigationTree.setCellRenderer(renderer);
		navigationTree.addTreeSelectionListener(new TreeSelectionHandler(preferencesContent));
		navigationTree.setSelectionRow(0);

		JScrollPane scrollableNavigationTree = new JScrollPane(navigationTree);
		scrollableNavigationTree.setPreferredSize(new Dimension(200, 300));
		scrollableNavigationTree.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		navigation.add(scrollableNavigationTree);

		JPanel applicationPreferences = createApplicationPreferencesPanel();
		JPanel connectionPreferences = createConnectionPreferencesPanel();
		JPanel mapsPreferences = createMapsPreferencesPanel();
		JPanel chartsPreferences = createChartsPreferencesPanel();
		JPanel militaryMapsPreferences = createMilitaryMapsPreferencesPanel();
		JPanel joinPreferences = createJoinPreferencesPanel();
		JPanel colorCustomization = createColorCustomizationPanel();// 58406
		JPanel pausePreferences = createPausePreferencesPanel();// 58406

		preferencesContent.add(applicationPreferences, APPLICATION_PREFERENCES);
		preferencesContent.add(connectionPreferences, CONNECTION_PREFERENCES);
		preferencesContent.add(mapsPreferences, MAPS_PREFERENCES);
		preferencesContent.add(militaryMapsPreferences, MILITARY_MAPS_PREFERENCES);
		preferencesContent.add(chartsPreferences, CHARTS_PREFERENCES);
		preferencesContent.add(joinPreferences, JOIN_PREFERENCES);
		preferencesContent.add(colorCustomization, COLOR_PREFERENCES);// 58406
		preferencesContent.add(pausePreferences, PAUSE_PREFERENCES);// 58406

		JButton cmdCancel = new JButton(getMessage("trackIt.cmdCancel"));
		cmdCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		JButton cmdOk = new JButton(getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Task(new Action() {
					@Override
					public String getMessage() {
						return Messages.getMessage("preferencesDialog.apply");
					}

					@Override
					public Object execute() throws TrackItException {
						for (PreferenceTask task : preferencesToApply) {
							task.execute();
						}
						return null;
					}

					@Override
					public void done(Object result) {
						dispose();
					}
				}).execute();
			}
		});

		JButton cmdApply = new JButton(getMessage("trackIt.cmdApply"));
		cmdApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				new Task(new Action() {
					@Override
					public String getMessage() {
						return Messages.getMessage("preferencesDialog.apply");
					}

					@Override
					public Object execute() throws TrackItException {
						for (PreferenceTask task : preferencesToApply) {
							task.execute();
							TrackIt.getApplicationFrame().validate();// 58406
							TrackIt.getApplicationFrame().repaint();// 58406
						}

						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
								Messages.getMessage("preferencesDialog.preferencesApplied"),
								Messages.getMessage("preferencesDialog.info"), JOptionPane.INFORMATION_MESSAGE);
						return null;
					}

					@Override
					public void done(Object result) {
						preferencesToApply = new ArrayList<PreferenceTask>();
					}
				}).execute();
			}
		});

		// Layout the components
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(navigation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(preferencesContent)
						.addGroup(layout.createSequentialGroup().addComponent(cmdOk).addComponent(cmdCancel)
								.addComponent(cmdApply))));
		layout.linkSize(SwingConstants.HORIZONTAL, cmdCancel, cmdOk);

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(navigation)
				.addGroup(layout.createSequentialGroup().addComponent(preferencesContent)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(cmdOk)
								.addComponent(cmdCancel).addComponent(cmdApply))));
		layout.linkSize(SwingConstants.VERTICAL, navigation, preferencesContent);

		SwingUtilities.updateComponentTreeUI(preferencesContent);
	}

	private JPanel createApplicationPreferencesPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel lblTitle = new JLabel(getMessage("preferencesDialog.applicationPreferences.title"));
		JSeparator titleSeparator = new JSeparator();
		titleSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));

		JLabel lblLanguage = new JLabel(getMessage("preferencesDialog.applicationPreferences.language"));

		List<Locale> availableLocales = Messages.getAvailableLocales();
		ComboBoxLocaleModel[] model = new ComboBoxLocaleModel[availableLocales.size()];
		for (int i = 0; i < availableLocales.size(); i++) {
			model[i] = new ComboBoxLocaleModel(availableLocales.get(i));
		}

		final JComboBox<ComboBoxLocaleModel> cbLanguages = new JComboBox<ComboBoxLocaleModel>(model);
		// final JComboBox<ComboBoxLocaleModel> cbLanguages = new
		// JComboBox<ComboBoxLocaleModel>();
		// cbLanguages.setModel(new
		// DefaultComboBoxModel<ComboBoxLocaleModel>(model));
		cbLanguages.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				final ComboBoxLocaleModel selectedLocale = (ComboBoxLocaleModel) cbLanguages.getSelectedItem();

				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						String country = selectedLocale.getLocale().getCountry();
						String language = selectedLocale.getLocale().getLanguage();

						appPreferences.setPreference(Constants.PrefsCategories.GLOBAL, null,
								Constants.GlobalPreferences.COUNTRY, country);
						appPreferences.setPreference(Constants.PrefsCategories.GLOBAL, null,
								Constants.GlobalPreferences.LANGUAGE, language);

						Messages.setLocale(new Locale(language, country));

						TrackIt.updateApplicationMenu();
					}
				});
			}
		});
		cbLanguages.setSelectedItem(new ComboBoxLocaleModel(Messages.getLocale()));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblTitle)
				.addComponent(titleSeparator)
				.addGroup(layout.createSequentialGroup().addComponent(lblLanguage).addComponent(cbLanguages)));

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(lblTitle).addComponent(titleSeparator)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblLanguage)
						.addComponent(cbLanguages)));

		return panel;
	}

	private JPanel createConnectionPreferencesPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel lblTitle = new JLabel(getMessage("preferencesDialog.connection.title"));
		JSeparator titleSeparator = new JSeparator();
		titleSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));

		ConnectionProperties connectionProperties = Connection.getConnectionProperties();

		JLabel lblHost = new JLabel(getMessage("preferencesDialog.connection.host"));
		final JTextField txtHost = new JTextField();
		txtHost.setText(connectionProperties.getHost());
		txtHost.setEnabled(connectionProperties.useProxy());
		txtHost.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						appPreferences.setPreference(Constants.PrefsCategories.CONNECTION, null,
								Constants.ConnectionPreferences.HOST, txtHost.getText());
					}
				});
			}
		});

		JLabel lblPort = new JLabel(getMessage("preferencesDialog.connection.port"));
		final JTextField txtPort = new TrackItTextField(7);
		txtPort.setMaximumSize(new Dimension(50, (int) txtPort.getPreferredSize().getWidth()));
		txtPort.setText(String.valueOf(connectionProperties.getPort()));
		txtPort.setEnabled(connectionProperties.useProxy());
		txtPort.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						appPreferences.setPreference(Constants.PrefsCategories.CONNECTION, null,
								Constants.ConnectionPreferences.PORT, Integer.parseInt(txtPort.getText()));
					}
				});
			}
		});

		JLabel lblDomain = new JLabel(getMessage("preferencesDialog.connection.domain"));
		final JTextField txtDomain = new TrackItTextField(20);
		txtDomain.setMaximumSize(new Dimension(150, (int) txtDomain.getPreferredSize().getWidth()));
		txtDomain.setText(connectionProperties.getDomain());
		txtDomain.setEnabled(connectionProperties.useProxy());
		txtDomain.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						appPreferences.setPreference(Constants.PrefsCategories.CONNECTION, null,
								Constants.ConnectionPreferences.DOMAIN, txtDomain.getText());
					}
				});
			}
		});

		JLabel lblUser = new JLabel(getMessage("preferencesDialog.connection.user"));
		final JTextField txtUser = new TrackItTextField(20);
		txtUser.setMaximumSize(new Dimension(150, (int) txtUser.getPreferredSize().getWidth()));
		txtUser.setText(connectionProperties.getUser());
		txtUser.setEnabled(connectionProperties.useProxy());
		txtUser.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						appPreferences.setPreference(Constants.PrefsCategories.CONNECTION, null,
								Constants.ConnectionPreferences.USER, txtUser.getText());
					}
				});
			}
		});

		JLabel lblPass = new JLabel(getMessage("preferencesDialog.connection.pass"));
		final JPasswordField txtPass = new JPasswordField(20);
		txtPass.setMaximumSize(new Dimension(150, (int) txtPass.getPreferredSize().getWidth()));
		txtPass.setText(connectionProperties.getPass());
		txtPass.setEnabled(connectionProperties.useProxy());
		txtPass.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						appPreferences.setPreference(Constants.PrefsCategories.CONNECTION, null,
								Constants.ConnectionPreferences.PASS, new String(txtPass.getPassword()));
					}
				});
			}
		});

		JLabel lblUseProxy = new JLabel(getMessage("preferencesDialog.connection.useProxy"));
		JCheckBox chkUseProxy = new JCheckBox();
		chkUseProxy.setSelected(connectionProperties.useProxy());
		chkUseProxy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final boolean selected = ((JCheckBox) e.getSource()).isSelected();

				if (selected) {
					txtHost.setEnabled(true);
					txtPort.setEnabled(true);
					txtDomain.setEnabled(true);
					txtUser.setEnabled(true);
					txtPass.setEnabled(true);
				} else {
					txtHost.setEnabled(false);
					txtPort.setEnabled(false);
					txtDomain.setEnabled(false);
					txtUser.setEnabled(false);
					txtPass.setEnabled(false);
				}

				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						appPreferences.setPreference(Constants.PrefsCategories.CONNECTION, null,
								Constants.ConnectionPreferences.USE_PROXY, selected);
					}
				});
			}
		});

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblTitle)
				.addComponent(titleSeparator)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(chkUseProxy)
								.addComponent(lblHost).addComponent(lblPort).addComponent(lblDomain)
								.addComponent(lblUser).addComponent(lblPass))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblUseProxy)
								.addComponent(txtHost).addComponent(txtPort).addComponent(txtDomain)
								.addComponent(txtUser).addComponent(txtPass))));

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(lblTitle).addComponent(titleSeparator)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(chkUseProxy)
						.addComponent(lblUseProxy))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblHost)
						.addComponent(txtHost))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblPort)
						.addComponent(txtPort))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblDomain)
						.addComponent(txtDomain))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblUser)
						.addComponent(txtUser))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblPass)
						.addComponent(txtPass)));

		return panel;
	}

	private JPanel createMapsPreferencesPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel lblTitle = new JLabel(getMessage("preferencesDialog.mapsPreferences.title"));
		JSeparator titleSeparator = new JSeparator();
		titleSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));

		JLabel lblTrackSimplificationMaxValue = new JLabel(
				getMessage("preferencesDialog.mapsPreferences.trackSimplificationMaxValue"));
		final JTextField txtTrackSimplificationMaxValue = new JTextField();
		final String trackSimplificationMaxValue = String.valueOf(appPreferences.getIntPreference(
				Constants.PrefsCategories.MAPS, null, Constants.MapPreferences.TRACK_SIMPLIFICATION_MAX_VALUE, 5000));
		txtTrackSimplificationMaxValue.setText(trackSimplificationMaxValue);
		txtTrackSimplificationMaxValue.setMaximumSize(new Dimension(70, txtTrackSimplificationMaxValue.getHeight()));
		txtTrackSimplificationMaxValue.setHorizontalAlignment(JTextField.RIGHT);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblTitle)
				.addComponent(titleSeparator).addGroup(layout.createSequentialGroup()
						.addComponent(lblTrackSimplificationMaxValue).addComponent(txtTrackSimplificationMaxValue)));

		layout.setVerticalGroup(
				layout.createSequentialGroup().addComponent(lblTitle).addComponent(titleSeparator)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(lblTrackSimplificationMaxValue)
								.addComponent(txtTrackSimplificationMaxValue)));

		return panel;
	}

	private JPanel createMilitaryMapsPreferencesPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel lblTitle = new JLabel(getMessage("preferencesDialog.militaryMapsPreferences.title"));
		JSeparator titleSeparator = new JSeparator();
		titleSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));

		JLabel lblLocation = new JLabel(getMessage("preferencesDialog.militaryMapsPreferences.location"));
		final JTextField txtLocation = new JTextField();
		final String location = appPreferences.getPreference(Constants.PrefsCategories.MAPS,
				Constants.PrefsSubCategories.MILITARY_MAPS_PROVIDER,
				Constants.MapPreferences.MILITARY_MAPS_MAP25K_LOCATION, System.getProperty("user.home"));

		txtLocation.setText(location);
		txtLocation.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						appPreferences.setPreference(Constants.PrefsCategories.MAPS,
								Constants.PrefsSubCategories.MILITARY_MAPS_PROVIDER,
								Constants.MapPreferences.MILITARY_MAPS_MAP25K_LOCATION, txtLocation.getText());
					}
				});
			}
		});

		JButton cmdSearchLocation = new JButton(getMessage("trackIt.cmdSearch"));
		cmdSearchLocation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final JFileChooser locationFolderChooser = new JFileChooser();
				locationFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				locationFolderChooser.setCurrentDirectory(new File(location));

				int returnValue = locationFolderChooser.showOpenDialog(application);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File file = locationFolderChooser.getSelectedFile();
					txtLocation.setText(file.getAbsolutePath());

					preferencesToApply.add(new PreferenceTask() {
						@Override
						public void execute() {
							appPreferences.setPreference(Constants.PrefsCategories.MAPS,
									Constants.PrefsSubCategories.MILITARY_MAPS_PROVIDER,
									Constants.MapPreferences.MILITARY_MAPS_MAP25K_LOCATION, txtLocation.getText());
						}
					});
				}
			}
		});

		JLabel lblResolution = new JLabel(getMessage("preferencesDialog.militaryMapsPreferences.resolution"));

		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(
				MilitaryMapResolution.getResolutionNames());
		final JComboBox<String> cbResolutions = new JComboBox<String>(model);
		cbResolutions.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				final String selectedResolution = (String) cbResolutions.getSelectedItem();

				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						MilitaryMapResolution resolution = MilitaryMapResolution.lookup(selectedResolution);

						appPreferences.setPreference(Constants.PrefsCategories.MAPS,
								Constants.PrefsSubCategories.MILITARY_MAPS_PROVIDER,
								Constants.MapPreferences.RESOLUTION, resolution.toString());
						MilitaryMapsProvider.setResolution(resolution);
					}
				});
			}
		});

		String initialResolution = appPreferences.getPreference(Constants.PrefsCategories.MAPS,
				Constants.PrefsSubCategories.MILITARY_MAPS_PROVIDER, Constants.MapPreferences.RESOLUTION,
				MilitaryMapResolution.JPG_5336x3336.toString());
		cbResolutions.setSelectedItem(initialResolution);

		JButton cmdClearCache = new JButton(getMessage("trackIt.cmdClearCache"));
		cmdClearCache.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				int result = JOptionPane.showConfirmDialog(PreferencesDialog.this,
						getMessage("preferencesDialog.militaryMapsPreferences.clearCacheMessage"),
						getMessage("preferencesDialog.militaryMapsPreferences.clearCacheTitle"),
						JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.YES_OPTION) {
					clearCache();
				}
			}

			private void clearCache() {
				String militaryMapsCachePath = TrackIt.getUserCacheLocation() + File.separator + "MilitaryMaps"
						+ File.separator;
				final File cachePath = new File(militaryMapsCachePath);

				new Task(new Action() {
					@Override
					public String getMessage() {
						return Messages.getMessage("preferencesDialog.militaryMapsPreferences.clearingCache");
					}

					@Override
					public Object execute() throws TrackItException {
						Utilities.recursivelyDelete(cachePath);
						return null;
					}

					@Override
					public void done(Object result) {
						JOptionPane.showMessageDialog(PreferencesDialog.this,
								Messages.getMessage("preferencesDialog.militaryMapsPreferences.cacheCleared"));
					}
				}).execute();
			}
		});

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblTitle)
				.addComponent(titleSeparator)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblLocation)
								.addComponent(lblResolution))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup().addComponent(txtLocation)
										.addComponent(cmdSearchLocation))
								.addComponent(cbResolutions).addComponent(cmdClearCache))));

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(lblTitle).addComponent(titleSeparator)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblLocation)
						.addComponent(txtLocation).addComponent(cmdSearchLocation))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblResolution)
						.addComponent(cbResolutions))
				.addComponent(cmdClearCache));

		return panel;
	}

	private JPanel createChartsPreferencesPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel lblTitle = new JLabel(getMessage("preferencesDialog.chartsPreferences.title"));
		JSeparator titleSeparator = new JSeparator();
		titleSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));

		JLabel lblElevationSmoothing = new JLabel(
				getMessage("preferencesDialog.chartsPreferences.elevationSmoothingFactor"));
		final double elevationSmoothing = Double.valueOf(appPreferences.getDoublePreference(
				Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.ELEVATION_SMOOTHING_FACTOR, 34.0));
		final JSpinner elevationSpinner = createSmoothingSpinner(Constants.ChartPreferences.ELEVATION_SMOOTHING_FACTOR,
				elevationSmoothing);

		JLabel lblHeartRateSmoothing = new JLabel(
				getMessage("preferencesDialog.chartsPreferences.heartRateSmoothingFactor"));
		final double heartRateSmoothing = Double.valueOf(appPreferences.getDoublePreference(
				Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.HEART_RATE_SMOOTHING_FACTOR, 54.0));
		final JSpinner heartRateSpinner = createSmoothingSpinner(Constants.ChartPreferences.HEART_RATE_SMOOTHING_FACTOR,
				heartRateSmoothing);

		JLabel lblSpeedSmoothing = new JLabel(getMessage("preferencesDialog.chartsPreferences.speedSmoothingFactor"));
		final double speedSmoothing = Double.valueOf(appPreferences.getDoublePreference(Constants.PrefsCategories.CHART,
				null, Constants.ChartPreferences.SPEED_SMOOTHING_FACTOR, 14.0));
		final JSpinner speedSpinner = createSmoothingSpinner(Constants.ChartPreferences.SPEED_SMOOTHING_FACTOR,
				speedSmoothing);

		JLabel lblCadenceSmoothing = new JLabel(
				getMessage("preferencesDialog.chartsPreferences.cadenceSmoothingFactor"));
		final double cadenceSmoothing = Double.valueOf(appPreferences.getDoublePreference(
				Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.CADENCE_SMOOTHING_FACTOR, 14.0));
		final JSpinner cadenceSpinner = createSmoothingSpinner(Constants.ChartPreferences.CADENCE_SMOOTHING_FACTOR,
				cadenceSmoothing);

		JLabel lblPowerSmoothing = new JLabel(getMessage("preferencesDialog.chartsPreferences.powerSmoothingFactor"));
		final double powerSmoothing = Double.valueOf(appPreferences.getDoublePreference(Constants.PrefsCategories.CHART,
				null, Constants.ChartPreferences.POWER_SMOOTHING_FACTOR, 34.0));
		final JSpinner powerSpinner = createSmoothingSpinner(Constants.ChartPreferences.POWER_SMOOTHING_FACTOR,
				powerSmoothing);

		JLabel lblTemperatureSmoothing = new JLabel(
				getMessage("preferencesDialog.chartsPreferences.temperatureSmoothingFactor"));
		final double temperatureSmoothing = Double.valueOf(appPreferences.getDoublePreference(
				Constants.PrefsCategories.CHART, null, Constants.ChartPreferences.TEMPERATURE_SMOOTHING_FACTOR, 34.0));
		final JSpinner temperatureSpinner = createSmoothingSpinner(
				Constants.ChartPreferences.TEMPERATURE_SMOOTHING_FACTOR, temperatureSmoothing);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblTitle)
				.addComponent(titleSeparator)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(lblElevationSmoothing).addComponent(lblHeartRateSmoothing)
								.addComponent(lblSpeedSmoothing).addComponent(lblCadenceSmoothing)
								.addComponent(lblPowerSmoothing).addComponent(lblTemperatureSmoothing))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(elevationSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(heartRateSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(speedSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(cadenceSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(powerSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(temperatureSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))));

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(lblTitle).addComponent(titleSeparator)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblElevationSmoothing)
						.addComponent(elevationSpinner))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblHeartRateSmoothing)
						.addComponent(heartRateSpinner))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblSpeedSmoothing)
						.addComponent(speedSpinner))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblCadenceSmoothing)
						.addComponent(cadenceSpinner))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblPowerSmoothing)
						.addComponent(powerSpinner))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(lblTemperatureSmoothing).addComponent(temperatureSpinner)));

		return panel;
	}

	private JSpinner createSmoothingSpinner(final String category, final double value) {
		final SpinnerModel model = new SpinnerNumberModel(value, 0.0, 500.0, 0.1);
		final JSpinner spinner = new JSpinner(model);
		spinner.putClientProperty("JComponent.sizeVariant", "small");

		spinner.setEditor(new JSpinner.NumberEditor(spinner, "#0.0"));
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final SpinnerModel model = spinner.getModel();
				if (model instanceof SpinnerNumberModel) {
					final double newValue = ((SpinnerNumberModel) model).getNumber().doubleValue();
					preferencesToApply.add(new PreferenceTask() {
						public void execute() {
							appPreferences.setPreference(Constants.PrefsCategories.CHART, null, category, newValue);
						}
					});
				}
			}
		});

		return spinner;
	}

	private JPanel createJoinPreferencesPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel lblTitle = new JLabel(getMessage("preferencesDialog.joinPreferences.title"));
		JSeparator titleSeparator = new JSeparator();
		titleSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));

		JLabel lblWarningDistance = new JLabel(getMessage("preferencesDialog.joinPreferences.warningDistance"));
		final double warningDistance = Double.valueOf(appPreferences.getDoublePreference(Constants.PrefsCategories.JOIN,
				null, Constants.JoinPreferences.WARNING_DISTANCE, 100.0));

		final SpinnerModel model = new SpinnerNumberModel(warningDistance, 0.0, 2000.0, 100.0);

		final JSpinner warningDistanceSpinner = new JSpinner(model);

		warningDistanceSpinner.putClientProperty("JComponent.sizeVariant", "small");
		warningDistanceSpinner.setEditor(new JSpinner.NumberEditor(warningDistanceSpinner, "#0.0"));
		warningDistanceSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final SpinnerModel model = warningDistanceSpinner.getModel();
				if (model instanceof SpinnerNumberModel) {
					final double newValue = ((SpinnerNumberModel) model).getNumber().doubleValue();
					preferencesToApply.add(new PreferenceTask() {
						public void execute() {
							appPreferences.setPreference(Constants.PrefsCategories.JOIN, null,
									Constants.JoinPreferences.WARNING_DISTANCE, newValue);
						}
					});
				}
			}
		});

		JLabel lblWarningDistanceUnit = new JLabel(Unit.METER.toString());

		final boolean warnDistanceExceeded = Boolean.valueOf(appPreferences.getBooleanPreference(
				Constants.PrefsCategories.JOIN, null, Constants.JoinPreferences.WARN_DISTANCE_EXCEEDED, Boolean.TRUE));

		JCheckBox chkWarnDistanceExceeded = new JCheckBox();
		chkWarnDistanceExceeded.setSelected(warnDistanceExceeded);
		chkWarnDistanceExceeded.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final boolean selected = ((JCheckBox) e.getSource()).isSelected();
				warningDistanceSpinner.setEnabled(selected);

				final SpinnerModel model = warningDistanceSpinner.getModel();
				final double newValue = ((SpinnerNumberModel) model).getNumber().doubleValue();

				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						appPreferences.setPreference(Constants.PrefsCategories.JOIN, null,
								Constants.JoinPreferences.WARN_DISTANCE_EXCEEDED, selected);
						appPreferences.setPreference(Constants.PrefsCategories.JOIN, null,
								Constants.JoinPreferences.WARNING_DISTANCE, newValue);
					}
				});
			}
		});

		JLabel lblMinimumDistance = new JLabel(getMessage("preferencesDialog.joinPreferences.minimumDistance"));
		final double minimumDistance = Double.valueOf(appPreferences.getDoublePreference(Constants.PrefsCategories.JOIN,
				null, Constants.JoinPreferences.MINIMUM_DISTANCE, 1.0));

		final SpinnerModel model2 = new SpinnerNumberModel(minimumDistance, 0.0, 10.0, 0.5);
		final JSpinner minimumDistanceSpinner = new JSpinner(model2);

		minimumDistanceSpinner.putClientProperty("JComponent.sizeVariant", "small");
		minimumDistanceSpinner.setEditor(new JSpinner.NumberEditor(minimumDistanceSpinner, "#0.0"));
		minimumDistanceSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final SpinnerModel model2 = minimumDistanceSpinner.getModel();
				if (model2 instanceof SpinnerNumberModel) {
					final double newValue = ((SpinnerNumberModel) model2).getNumber().doubleValue();
					preferencesToApply.add(new PreferenceTask() {
						public void execute() {
							appPreferences.setPreference(Constants.PrefsCategories.JOIN, null,
									Constants.JoinPreferences.MINIMUM_DISTANCE, newValue);
						}
					});
				}
			}
		});

		JLabel lblMinimumDistanceUnit = new JLabel(Unit.METER.toString());

		JCheckBox chkWarnDistanceBelow = new JCheckBox();
		chkWarnDistanceBelow.setVisible(false);

		
		final NumberFormat joinSpeedFormat = NumberFormat.getNumberInstance();
		final JFormattedTextField joinSpeedValueField = new JFormattedTextField(joinSpeedFormat);

		joinSpeedValue = new Double(String.valueOf(appPreferences.getDoublePreference(Constants.PrefsCategories.JOIN,
				null, Constants.JoinPreferences.JOIN_SPEED, 0.0)));
		joinSpeedValueField.setValue(joinSpeedValue);
		joinSpeedValueField.setColumns(3);
		joinSpeedValueField.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Object source = event.getSource();
				if (source == joinSpeedValueField) {
					joinSpeedValue = ((Number) joinSpeedValueField.getValue()).doubleValue();
					preferencesToApply.add(new PreferenceTask() {
						public void execute() {
							appPreferences.setPreference(Constants.PrefsCategories.JOIN, null,
									Constants.JoinPreferences.JOIN_SPEED, joinSpeedValue);
						}
					});
				}

			}

		});

		joinSpeedValueField.setMaximumSize(new Dimension(70, joinSpeedValueField.getHeight()));
		joinSpeedValueField.setHorizontalAlignment(JTextField.RIGHT);
		final JLabel joinSpeedUnit = new JLabel(Unit.KILOMETER_PER_HOUR.toString());
		
		final NumberFormat joinTimeFormat = NumberFormat.getNumberInstance();
		final JFormattedTextField joinTimeValueField = new JFormattedTextField(joinTimeFormat);

		joinTimeValue = new Double(String.valueOf(appPreferences.getDoublePreference(Constants.PrefsCategories.JOIN,
				null, Constants.JoinPreferences.JOIN_TIME, 0.0)));
		joinTimeValueField.setValue(joinTimeValue);
		joinTimeValueField.setColumns(3);
		joinTimeValueField.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Object source = event.getSource();
				if (source == joinTimeValueField) {
					joinTimeValue = ((Number) joinTimeValueField.getValue()).doubleValue();
					preferencesToApply.add(new PreferenceTask() {
						public void execute() {
							appPreferences.setPreference(Constants.PrefsCategories.JOIN, null,
									Constants.JoinPreferences.JOIN_TIME, joinTimeValue);
						}
					});
				}

			}

		});

		joinTimeValueField.setMaximumSize(new Dimension(70, joinTimeValueField.getHeight()));
		joinTimeValueField.setHorizontalAlignment(JTextField.RIGHT);
		final JLabel joinTimeUnit = new JLabel(Unit.MINUTE.toString());
		

		JLabel lblJoinOptionsTitle = new JLabel(Messages.getMessage("preferencesDialog.joinPreferences.speed"));
		lblJoinOptionsTitle.setFont(lblJoinOptionsTitle.getFont().deriveFont(Font.BOLD));

		String[] availableOptions = JoinOptions.getAvailableOptions().toArray(new String[0]);
		final JComboBox<String> speedOptionsChooser = new JComboBox<String>(availableOptions);

		// speedOptionsChooser.putClientProperty("JComponent.sizeVariant",
		// "mini");
		speedOptionsChooser.setMaximumSize((new Dimension(40, 10)));
		speedOptionsChooser.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
					final String selectedOption = speedOptionsChooser.getSelectedItem().toString();
				
				if(!selectedOption.equals(JoinOptions.getAvailableOptions().get(1)) && !selectedOption.equals(JoinOptions.getAvailableOptions().get(9))){
					joinSpeedValueField.setVisible(false);
					joinSpeedUnit.setVisible(false);
					joinTimeValueField.setVisible(false);
					joinTimeUnit.setVisible(false);
				}
				if(selectedOption.equals(JoinOptions.getAvailableOptions().get(1))){
					joinSpeedValueField.setVisible(true);
					joinSpeedUnit.setVisible(true);
					joinTimeValueField.setVisible(false);
					joinTimeUnit.setVisible(false);
				}
				if(selectedOption.equals(JoinOptions.getAvailableOptions().get(9))){
					joinTimeValueField.setVisible(true);
					joinTimeUnit.setVisible(true);
					joinSpeedValueField.setVisible(false);
					joinSpeedUnit.setVisible(false);
				}
				
			}
			
		});
		speedOptionsChooser.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				final String selectedOption = speedOptionsChooser.getSelectedItem().toString();
				
				preferencesToApply.add(new PreferenceTask() {
					public void execute() {
						String option = selectedOption;

						appPreferences.setPreference(Constants.PrefsCategories.JOIN, null,
								Constants.JoinPreferences.JOIN_OPTIONS, option);

						JoinOptions.setOption(option);

						TrackIt.updateApplicationMenu();
					}
				});
			}
		});

		speedOptionsChooser.setSelectedItem(JoinOptions.getOption());
		
		
		
		

		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lblTitle)
				.addComponent(titleSeparator)
						.addGroup(
								layout.createSequentialGroup()
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(chkWarnDistanceExceeded)
												.addComponent(chkWarnDistanceBelow))

										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(lblWarningDistance)
												.addComponent(lblMinimumDistance))
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(warningDistanceSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
												.addComponent(minimumDistanceSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
										.addGroup(
								layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(lblWarningDistanceUnit)
								.addComponent(lblMinimumDistanceUnit)))

		.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblJoinOptionsTitle)
						.addComponent(speedOptionsChooser)
						.addGroup(layout.createSequentialGroup()
								.addComponent(joinSpeedValueField)
								.addComponent(joinSpeedUnit))
						.addGroup(layout.createSequentialGroup()
								.addComponent(joinTimeValueField)
								.addComponent(joinTimeUnit))
						)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(lblTitle)
				.addComponent(titleSeparator)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(chkWarnDistanceExceeded)
						.addComponent(lblWarningDistance)
						.addComponent(warningDistanceSpinner)
						.addComponent(lblWarningDistanceUnit))

		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(chkWarnDistanceBelow)
				.addComponent(lblMinimumDistance)
				.addComponent(minimumDistanceSpinner)
				.addComponent(lblMinimumDistanceUnit))
		.addComponent(lblJoinOptionsTitle)
				.addComponent(speedOptionsChooser)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(joinSpeedValueField)
						.addComponent(joinSpeedUnit))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(joinTimeValueField)
						.addComponent(joinTimeUnit))
				);

		return panel;
	}

	private void createPreferencesNodes(DefaultMutableTreeNode root) {
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode subCategory = null;

		category = new DefaultMutableTreeNode(getMessage("preferencesDialog.tree.connection.label"));
		root.add(category);

		category = new DefaultMutableTreeNode(getMessage("preferencesDialog.tree.maps.label"));
		root.add(category);

		subCategory = new DefaultMutableTreeNode(getMessage("preferencesDialog.tree.militaryMaps.label"));
		category.add(subCategory);

		category = new DefaultMutableTreeNode(getMessage("preferencesDialog.tree.charts.label"));
		root.add(category);

		category = new DefaultMutableTreeNode(getMessage("preferencesDialog.join.label"));
		root.add(category);

		category = new DefaultMutableTreeNode(getMessage("preferencesDialog.tree.color.label"));
		root.add(category);

		category = new DefaultMutableTreeNode(getMessage("preferencesDialog.tree.pause.label"));
		root.add(category);
	}

	private static class TreeRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;
		private static ImageIcon appIconCollapsed;
		private static ImageIcon appIconExpanded;
		private static ImageIcon connectionIcon;
		private static ImageIcon mapIcon;
		private static ImageIcon militaryMapsIcon;
		private static ImageIcon chartIcon;
		private static ImageIcon colorChooserIcon;// 58406

		public TreeRenderer() {
			appIconCollapsed = ImageUtilities.createImageIcon("compass_plus_16.png");
			appIconExpanded = ImageUtilities.createImageIcon("compass_minus_16.png");
			connectionIcon = ImageUtilities.createImageIcon("connection_16.png");
			mapIcon = ImageUtilities.createImageIcon("map_small.png");
			chartIcon = ImageUtilities.createImageIcon("elevation_profile_16.png");
			militaryMapsIcon = ImageUtilities.createImageIcon("military_maps_16.png");
			colorChooserIcon = ImageUtilities.createImageIcon("palette_icon_16.png");// 58406
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf,
				int row, boolean hasFocus) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			String nodeName = node.getUserObject().toString();

			if (Constants.APP_NAME.equals(nodeName)) {
				setOpenIcon(appIconExpanded);
				setClosedIcon(appIconCollapsed);
			} else if (getMessage("preferencesDialog.tree.connection.label").equals(nodeName)) {
				setLeafIcon(connectionIcon);
			} else if (getMessage("preferencesDialog.tree.maps.label").equals(nodeName)) {
				setOpenIcon(mapIcon);
				setClosedIcon(mapIcon);
			} else if (getMessage("preferencesDialog.tree.militaryMaps.label").equals(nodeName)) {
				setLeafIcon(militaryMapsIcon);
			} else if (getMessage("preferencesDialog.tree.charts.label").equals(nodeName)) {
				setLeafIcon(chartIcon);
			}
			// 58406#################################################################################
			else if (getMessage("preferencesDialog.tree.color.label").equals(nodeName)) {
				setLeafIcon(colorChooserIcon);
				// ######################################################################################
			} else {
				setOpenIcon(getDefaultOpenIcon());
				setClosedIcon(getDefaultClosedIcon());
				setLeafIcon(getDefaultLeafIcon());
			}

			super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);

			return this;
		}
	}

	private static class TreeSelectionHandler implements TreeSelectionListener {
		private JPanel parent;

		public TreeSelectionHandler(JPanel parent) {
			this.parent = parent;
		}

		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) ((JTree) e.getSource())
					.getLastSelectedPathComponent();

			if (selectedNode == null) {
				return;
			}

			updatePreferencesContent(selectedNode);
		}

		private void updatePreferencesContent(DefaultMutableTreeNode node) {
			String nodeName = node.getUserObject().toString();

			if (Constants.APP_NAME.equals(nodeName)) {
				CardLayout layout = (CardLayout) parent.getLayout();
				layout.show(parent, APPLICATION_PREFERENCES);
			} else if (getMessage("preferencesDialog.tree.connection.label").equals(nodeName)) {
				CardLayout layout = (CardLayout) parent.getLayout();
				layout.show(parent, CONNECTION_PREFERENCES);
			} else if (getMessage("preferencesDialog.tree.maps.label").equals(nodeName)) {
				CardLayout layout = (CardLayout) parent.getLayout();
				layout.show(parent, MAPS_PREFERENCES);
			} else if (getMessage("preferencesDialog.tree.militaryMaps.label").equals(nodeName)) {
				CardLayout layout = (CardLayout) parent.getLayout();
				layout.show(parent, MILITARY_MAPS_PREFERENCES);
			} else if (getMessage("preferencesDialog.tree.charts.label").equals(nodeName)) {
				CardLayout layout = (CardLayout) parent.getLayout();
				layout.show(parent, CHARTS_PREFERENCES);
			} else if (getMessage("preferencesDialog.join.label").equals(nodeName)) {
				CardLayout layout = (CardLayout) parent.getLayout();
				layout.show(parent, JOIN_PREFERENCES);
				// 58406######################################################################################
			} else if (getMessage("preferencesDialog.tree.color.label").equals(nodeName)) {
				CardLayout layout = (CardLayout) parent.getLayout();
				layout.show(parent, COLOR_PREFERENCES);

			} else if (getMessage("preferencesDialog.tree.pause.label").equals(nodeName)) {
				CardLayout layout = (CardLayout) parent.getLayout();
				layout.show(parent, PAUSE_PREFERENCES);
				// ###########################################################################################
			} else {
				CardLayout layout = (CardLayout) parent.getLayout();
				layout.show(parent, APPLICATION_PREFERENCES);
			}
		}
	}

	private interface PreferenceTask {
		public void execute();
	}

	private class ComboBoxLocaleModel {
		private Locale locale;

		public ComboBoxLocaleModel(Locale locale) {
			this.locale = locale;
		}

		public Locale getLocale() {
			return locale;
		}

		public String toString() {
			return locale.getDisplayName();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ComboBoxLocaleModel other = (ComboBoxLocaleModel) obj;
			if (locale == null) {
				if (other.locale != null)
					return false;
			} else if (!locale.getDisplayName().equals(other.locale.getDisplayName()))
				return false;
			return true;
		}
	}

	// 58406##############################################################################################

	private JPanel createColorCustomizationPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel lblTitle = new JLabel(getMessage("preferencesDialog.tree.color.title"));
		JSeparator titleSeparator = new JSeparator();
		titleSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));

		JLabel lblTraceColor = new JLabel(getMessage("preferencesDialog.color.trace.label"));
		final JButton traceColorButton = new JButton();
		traceColorButton.setBackground(new Color(savedTraceColor));
		Dimension d = traceColorButton.getMaximumSize();
		d.height = (int) Math.floor(d.height * 1.5);
		traceColorButton.setMaximumSize(d);
		traceColorButton.addActionListener(new ActionListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent e) {
				JColorChooser chooser = getColorChooser(new Color(savedTraceColor));
				JDialog dialog = JColorChooser.createDialog(null, "Change Trace Color", true, chooser, null, null);
				dialog.show();
				Color newColor = chooser.getColor();
				if (newColor != null) {
					saveColor(Constants.ColorPreferences.FILL_RGB, newColor);
					saveColor(Constants.ColorPreferences.LINE_RGB, newColor.darker());
					traceColorButton.setBackground(newColor);
				}
			}
		});

		JLabel lblFrameColor = new JLabel(getMessage("preferencesDialog.color.frame.label"));
		final JButton frameColorButton = new JButton();
		frameColorButton.setBackground(new Color(savedFrameColor));
		frameColorButton.setMaximumSize(d);
		frameColorButton.addActionListener(new ActionListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent e) {
				JColorChooser chooser = getColorChooser(new Color(savedFrameColor));
				JDialog dialog = JColorChooser.createDialog(null, "Change Frame Color", true, chooser, null, null);
				dialog.show();
				Color newColor = chooser.getColor();
				if (newColor != null) {
					saveColor(Constants.ColorPreferences.THUMBNAIL_FRAME_COLOR, newColor);
					frameColorButton.setBackground(newColor);
				}
			}
		});

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblTitle)
				.addComponent(titleSeparator)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblTraceColor)
								.addComponent(lblFrameColor))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(traceColorButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(frameColorButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)))

		);

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(lblTitle).addComponent(titleSeparator)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblTraceColor)
						.addComponent(traceColorButton))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblFrameColor)
						.addComponent(frameColorButton)));

		return panel;
	}

	private void saveColor(final String category, final Color color) {
		preferencesToApply.add(new PreferenceTask() {
			public void execute() {
				appPreferences.setPreference(Constants.PrefsCategories.COLOR, null, category, color.getRGB());
				TrackIt.setDefaultColorScheme();
				MapView mv = TrackIt.getApplicationPanel().getMapView();
				mv.getMap().getLayer(MapLayerType.PHOTO_LAYER).validate();
				mv.getMap().refresh();
			}
		});
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

	private JPanel createPausePreferencesPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel lblTitle = new JLabel(getMessage("preferencesDialog.pause.title"));
		JSeparator titleSeparator = new JSeparator();
		titleSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));

		JLabel lblWarningDistance = new JLabel(getMessage("preferencesDialog.pause.speedThreshold"));
		final double speedThreshold = Double.valueOf(appPreferences.getDoublePreference(Constants.PrefsCategories.PAUSE,
				null, Constants.PausePreferences.SPEED_THRESHOLD, 1.5));

		final SpinnerModel model = new SpinnerNumberModel(speedThreshold, 0.0, 10.0, 0.1);
		final JSpinner speedThresholdSpinner = new JSpinner(model);
		speedThresholdSpinner.putClientProperty("JComponent.sizeVariant", "small");
		speedThresholdSpinner.setEditor(new JSpinner.NumberEditor(speedThresholdSpinner, "#0.0"));
		speedThresholdSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final SpinnerModel model = speedThresholdSpinner.getModel();
				if (model instanceof SpinnerNumberModel) {
					final double newValue = ((SpinnerNumberModel) model).getNumber().doubleValue();
					preferencesToApply.add(new PreferenceTask() {
						public void execute() {
							appPreferences.setPreference(Constants.PrefsCategories.PAUSE, null,
									Constants.PausePreferences.SPEED_THRESHOLD, newValue);

							Map<String, Object> options = new HashMap<String, Object>();
							options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.RECALCULATION);

							List<GPSDocument> documents = DocumentManager.getInstance().getDocuments();
							for (GPSDocument doc : documents) {
								// doc.updateSpeedWithPauseTime(newValue);
								try {
									new ConsolidationOperation(options).process(doc);
								} catch (TrackItException e) {
									e.printStackTrace();
								}
							}
						}
					});
				}
			}
		});

		JLabel lblSpeedThresholdUnit = new JLabel(Unit.KILOMETER_PER_HOUR.toString());

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblTitle)
				.addComponent(titleSeparator)
				.addGroup(layout.createSequentialGroup()
						.addComponent(lblWarningDistance).addComponent(speedThresholdSpinner,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblSpeedThresholdUnit)));

		layout.setVerticalGroup(
				layout.createSequentialGroup().addComponent(lblTitle).addComponent(titleSeparator)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(lblWarningDistance).addComponent(speedThresholdSpinner)
								.addComponent(lblSpeedThresholdUnit)));

		return panel;
	}
}