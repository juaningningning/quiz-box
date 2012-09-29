/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.io;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import quizbox.util.Logger;

public class SerialConnection implements SerialPortEventListener {
	private final static Logger log = Logger.getLogger(SerialConnection.class);
	
	private boolean serialErrorLogged = false;
	SerialPort serialPort;
	InputStream input;
	DataOutputStream output;
	
	public SerialConnection() {
		serialPort = null;
		input = null;
		output = null;
	}
	
	public InputStream getInputStream() {
		return input;
	}
	
	public DataOutputStream getOutputStream() {
		return output;
	}
	
	public int available() {
		if (input == null) return -2;
		try {
			return input.available();
		} catch (IOException e) {
			if (!serialErrorLogged) {
				this.close();
				log.error("Problem with input stream", e);
				serialErrorLogged = true;
			}
		}
		return -1;
	}
	
	public boolean ready() {
		int a = available();
		return a>0;
	}
	
	public boolean isConnected() {
		try {
			if (input != null && output != null && serialPort != null) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void connect(int bps, String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        
        if (portIdentifier.isCurrentlyOwned()) {
        	log.error("Error: Port is currently in use: " + portName);
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(bps,
                	SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        		serialPort.notifyOnDataAvailable(true);
        		serialPort.addEventListener(this);
        		
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                input = in;
                output = new DataOutputStream(new BufferedOutputStream(out));
                log.info("CONNECTED to: " + portName);
             } else {
            	log.error("Error: Only serial ports are handled, not: " + portName);
            }
        }     
    }

	public void close() {
		try {
			input = null;
			serialPort.getInputStream().close();
		} catch (Exception e) {
			log.warn("Exception while closing input stream", e);
		}

		try {
			output.close();
			serialPort.getOutputStream().close();
		} catch (Exception e) {
			log.warn("Exception while closing output stream", e);
		}
		
		try {
			// this call blocks while thread is attempting to read from inputstream
			serialPort.close();
		} catch (Exception e) {
			log.warn("Exception while closing serial port");
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {	
		case SerialPortEvent.DATA_AVAILABLE:
			boolean isReading = InputStreamThread.isReadingStream;
			if (ready()) {
				try {
					if (log.isDebugEnabled()) {
						log.debug("serialEvent: " + serialPort.getInputStream().available() + " bytes available");
					}
					synchronized (this) {
						this.notify();										
					}
				} catch (Exception e) {
					log.error("Error in handleSerialData method", e);
				}
			} else {
				if (!isReading) log.debug("We were notified of new data but available() is returning 0 *****************");
			}
			break;
		default:
			log.info("Ignoring serial port event type: " + event.getEventType());
		}
	}
}
