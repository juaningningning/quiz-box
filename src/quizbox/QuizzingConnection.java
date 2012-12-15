/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import quizbox.data.QuizMessage;
import quizbox.io.ConnectionException;
import quizbox.io.ConnectionTimeoutException;
import quizbox.io.IOutilities;
import quizbox.io.InputStreamThread;
import quizbox.io.SerialConnection;
import quizbox.io.SerialListenerInterface;
import quizbox.util.Logger;

public class QuizzingConnection {
	private final static Logger log = Logger.getLogger(QuizzingConnection.class);
	
	private SerialConnection connection;
	private InputStreamThread reader;
	
	private String myVersion;
	private String myInfo;
	
	public QuizzingConnection() {
		myVersion = "";
		myInfo = "";
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	if (isConnected()) {
		    		log.info("Closing quizzing device connection");
		    		close();
		    	}
		    }
		});
	}
	
	public String getConnectionMessage() {
		if (isConnected()) return "Connected: " + myVersion + " [" + myInfo + "]";
		return "Not connected";
	}

	private void doStartupChecks() {
		try {
			sendQuizMessage(QuizMessage.createVersionRequest());
			
			boolean found = false;
			while (!found) {
				QuizMessage qm = getNextInputMessage(5000);
				found = qm.isVersionMessage();
				if (found) {
					myVersion = qm.getVersion();
					myInfo = qm.getInfoString();
					log.info("Quizzing device found: version = '" + myVersion + "' / '" + myInfo + "'");
				}
			}
		} catch (IOException e) {
			log.warn("Startup check failed", e);
		} catch (ConnectionException e) {
			log.warn("Startup check unable to connect", e);
		} catch (ConnectionTimeoutException e) {
			log.warn("Startup check timed out", e);
		}
	}
	
	public boolean open(int bps, String port) {
		try {
			if (isConnected()) {
				throw new IllegalStateException("Cannot open new connection --" + 
						" existing connection is still open.");
			}
			
			SerialConnection serial = new SerialConnection(); 
			serial.connect(bps, port);
			
			initConnection(serial);
		} catch (Exception e) {		
			log.warn("Unable to open connection to port: " + port + " - " + e);
			System.out.println("Unable to open serial port, available ports are: ");
			IOutilities.listPorts();
			System.out.println("Selected Port = '" + port + "' and BPS = " + bps);
			
			closeReader();
			closeSerialConnection();
			return false;
		}
		return true;
	}
			
	private void initConnection(SerialConnection conn) {			
		connection = conn;
		reader = new InputStreamThread(101, connection);
		doStartupChecks();
	}

	public void addMessageListener(SerialListenerInterface listener) { 
		if (reader == null) {
			throw new IllegalStateException("No connection");
		}
		
		synchronized (reader.getSerialListenerList()) {
			reader.getSerialListenerList().add(listener);	
		}
	}

	public void removeMessageListener(SerialListenerInterface listener) {
		if (reader == null) {
			throw new IllegalStateException("No connection");
		}
		
		synchronized (reader.getSerialListenerList()) {
			reader.getSerialListenerList().remove(listener);
		}
	}
	
	/** 
	 * Not thread safe.
	 * @param msg
	 * @throws IOException
	 */
	public void sendQuizMessage(QuizMessage msg) throws IOException {
		IOutilities.sendOutput(connection, msg.getDataLine()+"\n");
	}
	
	/** 
	 * Not thread safe.
	 * @param msg
	 */
	public void sendQuizMessageChecked(QuizMessage msg) {
		try {
			sendQuizMessage(msg);
		} catch (IOException e) {
			log.warn("Problem sending message: " + msg, e);
		}
	}
		
	public QuizMessage getNextInputMessage() throws ConnectionException, ConnectionTimeoutException {
		return getNextInputLineTimeout(null);
	}
	
	public QuizMessage getNextInputMessage(int timeout) throws ConnectionException, ConnectionTimeoutException {
		return getNextInputLineTimeout(timeout);
	}
	
	private QuizMessage getNextInputLineTimeout(Integer timeout) throws ConnectionException, ConnectionTimeoutException {
		QuizMessage qm = null;
		try {
			String line = null;
			if (timeout != null) {
				line = reader.getInputLineQueue().poll(timeout, TimeUnit.MILLISECONDS);	
			} else {
				line = reader.getInputLineQueue().take();
			}
			if (line != null) {
				qm = new QuizMessage(line);
			}
		} catch (InterruptedException e) {
			throw new ConnectionException("Error while attempting to remove packet from queue", e);
		}
		
		if (timeout != null && timeout > 0) {
			log.info("Input stream timed out");
			//throw new ConnectionTimeoutException();
		}
		
		return qm;
	}
	
	
	/**
	 * Shuts down RXTX and packet parser thread
	 */
	public void close() {		
		if (!isConnected()) {
			//throw new IllegalStateException("Serial Device is not connected");
			log.warn("Serial Device is not connected");
			return;
		}
		closeReader();
		closeSerialConnection();
	}
		
	private void closeReader() {
		// shutdown parser thread
		if (reader != null) {
			reader.setDone(true);
			// interrupts thread, if waiting.  does not interrupt thread if blocking on read
			// serial port close will be closed prior to thread exit
			reader.interrupt();
		}		
		reader = null;
	}
	
	private void closeSerialConnection() {
		if (connection != null) connection.close();
		connection = null;	
	}

	/**
	 * Indicates if serial port connection has been established.
	 * The open method may be called if true it returned
	 * 
	 * @return
	 */
	public boolean isConnected() {
		if (reader == null) return false;
		return reader.getConnection().isConnected();
	}
	
	/**
	 * Removes all lines off of the response queue
	 */
	public void clearInputLineQueue() {
		reader.getInputLineQueue().clear();
	}
}
