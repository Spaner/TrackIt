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
package com.henriquemalheiro.trackit.business.utility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.business.common.TrackItTask;


public class ThreadPool {
	private static final int NUMBER_OF_THREADS = 25;
	private static ExecutorService executorService;
	private static BlockingQueue<TrackItTask> waitingTasks;
	private static Logger logger = Logger.getLogger(ThreadPool.class.getName());
	
	static {
		executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		waitingTasks = new PriorityBlockingQueue<TrackItTask>();
		new Thread(new ThreadPoolMonitor()).start();
	}
	
	public synchronized static void enqueueTask(TrackItTask task) {
		try {
			waitingTasks.put(task);
		} catch (InterruptedException e) {
			// do nothing
		}
	}
	
	private static class ThreadPoolMonitor implements Runnable {
		public void run() {
			try {
				while (true) {
					executorService.execute(waitingTasks.take().getTask());
				}
			} catch (InterruptedException ie) {
				logger.debug("Thread Pool Monitor finishing...");
				return;
			}
		}
	}
}
