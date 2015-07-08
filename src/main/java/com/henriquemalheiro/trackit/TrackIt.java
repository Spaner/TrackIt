/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes, J. Brisson Lopes
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
package com.henriquemalheiro.trackit;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.OperatingSystem;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.TrackItPreferences;
import com.henriquemalheiro.trackit.presentation.ApplicationPanel;
import com.henriquemalheiro.trackit.presentation.task.Action;
import com.henriquemalheiro.trackit.presentation.task.Task;
import com.pg58406.trackit.business.common.ColorSchemeV2;
import com.pg58406.trackit.business.utility.AboutDialog;
import com.pg58406.trackit.business.utility.SaveTools;

public class TrackIt {
	public static Logger logger = Logger.getLogger(TrackIt.class.getName());
	private static TrackItPreferences userPreferences;
	private static JFrame applicationFrame;
	private static ApplicationPanel applicationPanel;
	private static ColorSchemeV2 defaultColorScheme;

	static {
		userPreferences = TrackItPreferences.getInstance();
	}

	public static TrackItPreferences getPreferences() {
		return userPreferences;
	}

	public static String getUserCacheLocation() {
		String defaultUserCacheLocation = "";

		if (OperatingSystem.isMac()) {
			StringBuilder sb = new StringBuilder();
			sb.append(System.getProperty("user.home")).append(File.separator).append("Library").append(File.separator)
					.append("Application Support");
			sb.append(File.separator).append(Constants.APP_NAME_NORMALIZED);
			defaultUserCacheLocation = sb.toString();
		} else if (OperatingSystem.isUnix()) {
			StringBuilder sb = new StringBuilder();
			sb.append(System.getProperty("user.home")).append(File.separator).append(".")
					.append(Constants.APP_NAME_NORMALIZED);
			defaultUserCacheLocation = sb.toString();
		} else if (OperatingSystem.isWindows()) {
			StringBuilder sb = new StringBuilder();
			sb.append(System.getenv("APPDATA")).append(File.separator).append(Constants.APP_NAME_NORMALIZED);
			defaultUserCacheLocation = sb.toString();
		}

		return userPreferences.getPreference(Constants.PrefsCategories.MAPS, null,
				Constants.MapPreferences.CACHE_LOCATION, defaultUserCacheLocation);
	}

	public static JFrame getApplicationFrame() {
		return applicationFrame;
	}

	public static void updateApplicationMenu() {
		applicationPanel.updateApplicationMenu();
	}

	// 58406
	public static ApplicationPanel getApplicationPanel() {
		return applicationPanel;
	}

	public static ColorSchemeV2 getDefaultColorScheme() {
		return defaultColorScheme;
	}

	//

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		logger.debug("Starting gui...");
		// 58406################################################################################
		applicationFrame = new JFrame(Constants.APP_NAME);
		applicationFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		applicationPanel = new ApplicationPanel(applicationFrame);
		applicationFrame.getContentPane().add(applicationPanel);
		applicationFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				SaveTools.getInstance().saveAndExit();
			}
		});
		// #####################################################################################
		setWindowSizeAndPosition();
	}

	private static void setWindowSizeAndPosition() {
		int xPosition = TrackIt.getPreferences().getIntPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.APPLICATION_X, 0);
		int yPosition = TrackIt.getPreferences().getIntPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.APPLICATION_Y, 0);

		int windowWidth = TrackIt.getPreferences().getIntPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.APPLICATION_WIDTH, 640);
		int windowHeight = TrackIt.getPreferences().getIntPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.APPLICATION_HEIGHT, 480);

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		applicationFrame.setMaximizedBounds(env.getMaximumWindowBounds());
		applicationFrame.setBounds(new Rectangle(xPosition, yPosition, windowWidth, windowHeight));
		applicationFrame.setVisible(true);
		applicationFrame.setExtendedState(applicationFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		applicationFrame.addComponentListener(new TrackItListener());
	}

	private static class TrackItListener implements ComponentListener {
		@Override
		public void componentResized(ComponentEvent e) {
			storeSizeAndPosition();
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			storeSizeAndPosition();
		}

		@Override
		public void componentShown(ComponentEvent e) {
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}

		private void storeSizeAndPosition() {
			Rectangle bounds = applicationFrame.getBounds();

			TrackItPreferences prefs = TrackItPreferences.getInstance();

			prefs.setPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.APPLICATION_X,
					(int) bounds.getX());
			prefs.setPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.APPLICATION_Y,
					(int) bounds.getY());
			prefs.setPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.APPLICATION_WIDTH,
					(int) bounds.getWidth());
			prefs.setPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.APPLICATION_HEIGHT,
					(int) bounds.getHeight());
		}
	}

	private static void init() {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", Constants.APP_NAME);

		// try {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// } catch (InstantiationException e) {
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// e.printStackTrace();
		// } catch (UnsupportedLookAndFeelException e) {
		// e.printStackTrace();
		// }
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look
			// and feel.
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}
		}

		PropertyConfigurator.configure(TrackIt.class.getResource("/log4j.properties"));

		UIManager.put("OptionPane.cancelButtonText", Messages.getMessage("messages.cancel"));
		UIManager.put("OptionPane.noButtonText", Messages.getMessage("messages.no"));
		UIManager.put("OptionPane.okButtonText", Messages.getMessage("messages.ok"));
		UIManager.put("OptionPane.yesButtonText", Messages.getMessage("messages.yes"));

		int savedTraceColor = userPreferences.getIntPreference(Constants.PrefsCategories.COLOR, null,
				Constants.ColorPreferences.FILL_RGB, 65535);
		Color newColor = new Color(savedTraceColor);

		int tempRed = newColor.getRed();
		int tempGreen = newColor.getGreen();
		int tempBlue = newColor.getBlue();
		Color selectionFill = new Color(255 - tempRed, 255 - tempGreen, 255 - tempBlue);
		defaultColorScheme = new ColorSchemeV2(newColor, newColor.darker(), selectionFill.darker(), selectionFill);

		if (OperatingSystem.isMac()) {
			Application macApp = Application.getApplication();
			macApp.setAboutHandler(new AboutHandler() {

				@Override
				public void handleAbout(AboutEvent arg0) {
					new AboutDialog(applicationFrame);
				}
			});
			// 12335 start
			macApp.setQuitHandler(new QuitHandler() {

				@Override
				public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
					SaveTools.getInstance().saveAndExit();
				}
			});
			// 12335 end
		}
	}

	// 58406
	private static void Load() throws TrackItException {
		new Task(new Action() {
			@Override
			public String getMessage() {
				return Messages.getMessage("trackIt.open.load");
			}

			@Override
			public Object execute() throws TrackItException {
				DocumentManager.getInstance().initFromDB();
				return null;
			}

			@Override
			public void done(Object result) {
			}
		}).execute();
	}

	public static void main(String[] args) {
		init();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
				try {
					Load();
				} catch (TrackItException e) {
				}
			}
		});
	}

	public static void restartApplication() {
		try {
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			final File currentJar = new File(TrackIt.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			/* is it a jar file? */
			if (!currentJar.getName().endsWith(".jar"))
				return;

			/* Build command: java -jar application.jar */
			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());

			final ProcessBuilder builder = new ProcessBuilder(command);

			builder.start();
			System.exit(0);
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void setDefaultColorScheme() {
		int savedTraceColor = userPreferences.getIntPreference(Constants.PrefsCategories.COLOR, null,
				Constants.ColorPreferences.FILL_RGB, 65535);
		Color newColor = new Color(savedTraceColor);

		int tempRed = newColor.getRed();
		int tempGreen = newColor.getGreen();
		int tempBlue = newColor.getBlue();
		Color selectionFill = new Color(255 - tempRed, 255 - tempGreen, 255 - tempBlue);

		defaultColorScheme.setFillColor(newColor);
		defaultColorScheme.setLineColor(newColor.darker());
		defaultColorScheme.setSelectionFillColor(selectionFill.darker());
		defaultColorScheme.setSelectionLineColor(selectionFill);
	}
}
