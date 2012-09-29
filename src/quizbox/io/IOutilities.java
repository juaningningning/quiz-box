/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.io;

import gnu.io.CommPortIdentifier;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import quizbox.util.Helper;
import quizbox.util.Logger;

public class IOutilities {
	private final static Logger log = Logger.getLogger(IOutilities.class);
	
	public static void sendOutput(SerialConnection connection, String data) throws IOException {
		if (connection ==null || !connection.isConnected()) {
			//throw new RuntimeException("Device is not connected");
			log.error("Device is not connected");
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("sending data: '" + data.trim() + "'");	
		}
		DataOutputStream output = connection.getOutputStream();
		output.writeBytes(data);
		output.flush();
	}

	public static String readInput(SerialConnection connection) throws IOException, InterruptedException {
		log.debug("About to read from input stream");
		
		InputStream input = connection.getInputStream();
		if (!connection.ready()) return null;
		String s = "";
		boolean isMsgComplete = false;
		while (connection.ready() && input.read()!='(');
		while (!isMsgComplete) {
			if (connection.ready()) {
				int c = input.read();
				isMsgComplete = (c == ')');
				if (isMsgComplete) break;
				s += (char)c;
			} else {
				Thread.sleep(2);
			}
		}
		
		log.debug("Received input: '" + Helper.formatLine(s) + "'");
		return s;
	}
	
    public static void listPorts() {
    	for (String port : getPorts()) {
    		System.out.println(port);
    	}
    }
    
    public static List<String> getPorts() {
    	List<String> ports = new ArrayList<String>();
    	
		@SuppressWarnings("unchecked")
		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
        	CommPortIdentifier portIdentifier = portEnum.nextElement();
        	String port = portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType());
        	ports.add(port);
        }            	
    	return ports;
    }
    
    public static String getPortTypeName (int portType) {
        switch (portType)  {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

}
