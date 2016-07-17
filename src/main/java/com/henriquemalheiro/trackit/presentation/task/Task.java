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
package com.henriquemalheiro.trackit.presentation.task;

import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.utilities.ProgressDialog;

public class Task extends SwingWorker<Object, Void> {
	private ProgressDialog progress;
	private boolean showProgress;
	private Action action;
	private Object result;
	private static Logger logger = Logger.getLogger(Task.class.getName());
	
	public Task(Action action) {
		this(action, true);
	}
	
	public Task(Action action, Boolean showProgress) {
		this.action = action;
		this.showProgress = showProgress;
		
		if (showProgress) {
			progress = new ProgressDialog(TrackIt.getApplicationFrame(), "");
			progress.setLocationRelativeTo(TrackIt.getApplicationFrame());
		}
	}
	
	@Override
	protected final Object doInBackground() throws TrackItException {
		startProgress();
		return action.execute();
	}

	private void startProgress() {
		if (showProgress) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					synchronized (Task.this) {
						
						if (progress != null) {
							progress.setMessage(action.getMessage());
							progress.setVisible(true);
						}
					}
				}
			});
		}
	}

	@Override
	protected final void done() {
		try {
            result = get();
            this.notify();
            action.done(result);
        } catch (InterruptedException ignore) {
        } catch (ExecutionException e) {
            String why = null;
            Throwable cause = e.getCause();
            if (cause != null) {
                why = cause.getMessage();
            } else {
                why = e.getMessage();
            }
            logger.error("Error executing task: " + why);
            
            JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), Messages.getMessage("task.message.executionError")
            		, Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
        } finally {
        	synchronized (Task.this) {
        		if (showProgress) {
        			progress.setVisible(false);
        			progress = null;
        		}
			}
        }
	}
}
