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
package com.pg58406.trackit.business.operation;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.derby.tools.sysinfo;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.OperatingSystem;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Session;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.business.operation.ConsolidationOperation;
import com.henriquemalheiro.trackit.business.operation.Operation;
import com.henriquemalheiro.trackit.business.operation.OperationBase;
import com.pg58406.trackit.business.domain.Pause;

public class PauseDetectionOperation extends OperationBase implements Operation {

	public PauseDetectionOperation() {
		super();
	}

	public PauseDetectionOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
	}

	private void setup(List<Trackpoint> trkpoints) {
		// detector = new DetPausesOpDistanceDescAVG(trkpoints);
		detector = new DetPausesNewVersionOperation(trkpoints);
	}

	private DetPausesNewVersionOperation detector;

	// private DetectPausesInterface detector;

	@Override
	public String getName() {
		return Constants.PauseDetectionOperation.NAME;
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		if (processActivities) {
			for (Activity activity : document.getActivities()) {
				DetectPauses(activity);
			}
		}

		if (processCourses) {
			for (Course course : document.getCourses()) {
				DetectPauses(course);
			}
		}
	}

	public void process(DocumentItem item) {
		DetectPauses(item);
	}

	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		for (GPSDocument document : documents) {
			try {
				process(document);
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	public void DetectPauses(DocumentItem item) {
		double limit = TrackIt.getPreferences().getDoublePreference(
				Constants.PrefsCategories.PAUSE, null,
				Constants.PausePreferences.SPEED_THRESHOLD, 1.5);
		limit = limit / 3.6;
		List<Trackpoint> trackpoints = item.getTrackpoints();
		setup(trackpoints);
		int trackLength = trackpoints.size();
		List<Pause> pauses = new ArrayList<Pause>();
		boolean b = false;
		b = detector.DetectPauses(1, trackLength - 1, 1.0, limit, pauses);
		/*
		 * detector = new DetPausesOpSpeedNoAVG(trackpoints);
		 * detector.DetectPauses(1, trackLength - 1, 1.0, 0.416, pauses);
		 */
		if (b) {
			Map<String, Object> operationOptions = new HashMap<String, Object>();
			operationOptions
					.put(com.henriquemalheiro.trackit.business.common.Constants.ConsolidationOperation.LEVEL,
//							ConsolidationLevel.RECALCULATION);
							ConsolidationLevel.SUMMARY);         // 12335 : 2015-07-18
			if (item instanceof Activity) {
				Activity a = (Activity) item;

				for (Session s : a.getSessions()) {
					for (Lap l : s.getLaps()) {
						l.getPauses().clear();
					}
				}

				for (Pause p : pauses) {
					for (Session s : a.getSessions()) {
						for (Lap l : s.getLaps()) {
							if ((p.getStart().after(l.getStartTime()) || 
									p.getStart().equals(l.getStartTime()))
									&& (p.getEnd().after(l.getEndTime()) || 
											p.getEnd().equals(l.getEndTime()))) {
								l.addPause(p);
							}
						}
					}
				}
				a.setPauses(pauses);
				try {
					new ConsolidationOperation(operationOptions).process(a);
				} catch (TrackItException e) {
					e.printStackTrace();
				}
				a.publishSelectionEvent(null);
			}
			if (item instanceof Course) {
				Course c = (Course) item;
				c.setPauses(pauses);
				for(int ii=0;ii<trackLength;ii++)
					System.out.println(ii + " $ " + trackpoints.get(ii).getTimeFromPrevious());			
				try {
					boolean unsaved = c.getUnsavedChanges();               // 12335 : 2015-07-18
					new ConsolidationOperation(operationOptions).process(c);
					c.setUnsavedChanges( unsaved);                         // 12335 : 2015-07-18
				} catch (TrackItException e) {
					e.printStackTrace();
				}
				for(int ii=0;ii<trackLength;ii++)
					System.out.println(ii + " - " + trackpoints.get(ii).getTimeFromPrevious());

			}
			
			File logFile = new File("Pauses Log.txt");
			String logPath = logFile.getAbsolutePath();
			Object[] options = { "Open File", "Ok" };
			int n = JOptionPane
					.showOptionDialog(
							TrackIt.getApplicationFrame(),
							Messages.getMessage(
									"applicationPanel.menu.detectPauses.success",
									logPath),
							Messages.getMessage("applicationPanel.menu.detectPauses.success.description"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.INFORMATION_MESSAGE, null, options,
							options[1]);
			if (n == 0) {
				try {
					if (OperatingSystem.isWindows()) {
						String cmd = "rundll32 url.dll,FileProtocolHandler "
								+ logFile.getAbsolutePath();
						Runtime.getRuntime().exec(cmd);
					} else {
						Desktop.getDesktop().edit(logFile);
					}
				} catch (IOException e) {
					logger.error(e.getClass().getName() + ": "
							+ e.getMessage());
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("No Pauses Detected!");
			JOptionPane
					.showMessageDialog(
							TrackIt.getApplicationFrame(),
							Messages.getMessage("applicationPanel.menu.detectPauses.failure"));
		}
	}

	@SuppressWarnings("unused")
	private boolean validate(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub
		
	}
}
