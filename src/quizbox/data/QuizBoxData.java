/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import quizbox.util.Logger;

public class QuizBoxData implements QuizConstants {
	private final static Logger log = Logger.getLogger(QuizBoxData.class);

	private static final Map<QuizBoxAddress, QuizBoxData> data = new HashMap<QuizBoxAddress, QuizBoxData>();
	
	private final QuizBoxAddress myAddress;
	private final int myBoxNumber;
	
	private long myConnectionTime;
	private double myLQ;
	private int myStatus;
	private int myButtonState;	// A, B, C, NO_BTN
	private int myPlacing;		// first, second, third, etc

	public static QuizBoxData createQuizBoxData(QuizBoxAddress address, int boxNumber) {
		if (data.containsKey(address)) {
			QuizBoxData d = data.get(address);
			if (d.myBoxNumber != boxNumber) {
				log.warn("CreateQuizBoxData found existing object, boxnumber = " + 
					d.myBoxNumber + " requested boxnumber = " + boxNumber);
			}
			return d;
		}
		QuizBoxData qbd = new QuizBoxData(address, boxNumber);
		data.put(address, qbd);
		return qbd;
	}
	
	public static QuizBoxData getQuizBoxData(QuizBoxAddress address) {
		if (data.containsKey(address)) {
			return data.get(address);
		}
		return null;
	}
	
	public static Collection<QuizBoxData> getValues() {
		return data.values();
	}
	
	public static Set<QuizBoxAddress> keySet() {
		return data.keySet();
	}
	
	private QuizBoxData(QuizBoxAddress address, int boxnumber) {		
		myAddress = address;
		myBoxNumber = boxnumber;
		myConnectionTime = 0;
		myLQ = 0;
		myStatus = 0;
		clearStates();
	}
	
	public void clearStates() {
		myButtonState = NO_BTN;
		myPlacing = 0;
	}
	
	public QuizBoxAddress getAddress() {
		return myAddress;
	}
	
	public int getBoxNumber() {
		return myBoxNumber;
	}
	
	public void resetConnectionTime() {
		myConnectionTime = System.currentTimeMillis();
	}
	
	public long getConnectionTime() {
		return myConnectionTime;
	}
	
	public void setLQ(int lq1, int lq2) {
		// Do some filtering on the data
		double lq = (lq1+lq2)/2.0;
		if (myLQ==0.0) myLQ = lq;
		else myLQ = 0.8*myLQ + 0.2*lq;
	}
	
	public void setLQ(int lq) {
		// Do some filtering on the data
		if (myLQ==0.0) myLQ = lq;
		else myLQ = 0.9*myLQ + 0.1*(double)lq;
	}
	
	public int getLQ() {
		return (int)Math.round(myLQ);
	}
	
	public void setButtonState(int buttonState) {
		myButtonState = buttonState;
	}
	
	public int getButtonState() {
		return myButtonState;
	}
	
	public void setStatus(int status) {
		myStatus = status;
	}
	
	public int getStatus() {
		return myStatus;
	}
	
	public void setPlacing(int placing) {
		myPlacing = placing;
	}
	
	public int getPlacing() {
		return myPlacing;
	}
}
