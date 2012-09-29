/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.io;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import quizbox.util.Helper;
import quizbox.util.Logger;


public class InputStreamThread implements Runnable {
	
	private final static Logger log = Logger.getLogger(InputStreamThread.class);
	public static boolean isReadingStream = false;	
	
	private volatile boolean done;
	
	private Thread thread;
	private final BlockingQueue<String> inputLineQueue;
	private final List<SerialListenerInterface> serialListenerList;
	private ExecutorService listenerPool;
	private SerialConnection connection;
	private int maxQueueSize;	


	public InputStreamThread(int queueSize, SerialConnection connect) {
		done = false;
		inputLineQueue = new LinkedBlockingQueue<String>();
		serialListenerList = new LinkedList<SerialListenerInterface>();

		// Create an executor to deliver incoming lines to listeners.
		listenerPool = Executors.newSingleThreadExecutor();
		
		maxQueueSize = queueSize;
		connection = connect;
		
		thread = new Thread(this);
		thread.setName("InputStreamThread");
		thread.start();		
		
		log.debug("Starting InputStreamThread");
	}
	
	public SerialConnection getConnection() {
		return connection;
	}
	
	public void setDone(boolean done) {
		this.done = done;
	}
	
	public List<SerialListenerInterface> getSerialListenerList() {
		return serialListenerList;
	}

	public BlockingQueue<String> getInputLineQueue() {
		return inputLineQueue;
	}
	
	private void addInputLine(final String line) throws InterruptedException {
		// Trim input queue
		while (inputLineQueue.size() >= (maxQueueSize - 1)) {
			log.debug("Input line queue has reached the maximum size of " + maxQueueSize + 
				" lines.  Trimming a line from head of queue to make room");
			inputLineQueue.poll();
		}
		if (line != null && line.trim().length() > 0) {
			inputLineQueue.put(line.trim());	
		}
		
		listenerPool.submit(new Runnable() {
			public void run() {
				// Synchronize to avoid error if packet listener add/remove is 
				// called while we are iterating
				synchronized (serialListenerList) {
					for (SerialListenerInterface listener : serialListenerList) {
						if (listener != null) {
							listener.processLine(line);	
						} else {
							log.warn("SerialListener is null, size is " + serialListenerList.size());
						}
					}			
				}				
			}
		});
	}
	
	public void run() {
		try {
			while (!done) {
				try {
					if (connection.ready()) {
						isReadingStream = true;
						String line = IOutilities.readInput(connection);
						if (line != null) {
							if (log.isDebugEnabled()) {
								log.debug("Read: '" + Helper.formatLine(line) + "' from input stream");
							}						
							this.addInputLine(line);
						}
						isReadingStream = false;
					} else {
						synchronized (this.connection) {
							// There's a chance that we got notified after the first in.available check
							if (connection.ready()) {
								continue;
							}
							connection.wait();		// wait for data
						}	
					}				
				} catch (Exception e) {
					if (e instanceof InterruptedException) throw ((InterruptedException)e);					
					log.error("Problem reading line:", e);
					
					if (e instanceof IOException) {
						// this is thrown by RXTX if the serial device is 
						// unplugged while we are reading data; 
						// if we are waiting then it will waiting forever
						log.error("Serial device IOException.. exiting");
						break;
					}
				}
			}
		} catch(InterruptedException ie) {
			// We've been told to stop -- the user called the close() method			
			log.info("Packet parser thread was interrupted.  This occurs when close() is called");
		} finally {
			if (connection != null) {
				connection.close();
			}
			if (listenerPool != null) {
				try {
					listenerPool.shutdownNow();
				} catch (Throwable t) {
					log.warn("Failed to shutdown listener thread pool", t);
				}
			}
		}
		
		log.info("InputStreamThread is exiting");
	}
	
	public void interrupt() {
		if (thread != null) {
			try {
				thread.interrupt();	
			} catch (Exception e) {
				log.warn("Error interrupting parser thread", e);
			}
		}
	}
}