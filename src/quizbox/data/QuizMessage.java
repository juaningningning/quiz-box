/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.data;


public class QuizMessage {
	public static final int BUTTON_A = 30;
	public static final int BUTTON_B = 29;
	public static final int BUTTON_C = 28;
	public static final int NONE_TYPE = 0;
	public static final int REQUEST_TYPE = 1;
	public static final int VERSION_TYPE = 2;
	public static final int BUTTON_TYPE = 3;
	public static final int STATUS_TYPE = 4;
	public static final int UPDATE_TYPE = 5;
	
	private String myLine;
	private int myType;
	private String myVersion;
	private QuizBoxAddress myAddress;
	private int myButton;
	private int myLQ1;
	private int myLQ2;
	
	public QuizMessage() {
		clear();
	}
	
	public QuizMessage(String line) {
		parseLine(line);
	}
	
	public void setMessage(String line) {
		parseLine(line);
	}
	
	public void clear() {
		myLine = "";
		myVersion = "";
		myType = 0;
		myAddress = new QuizBoxAddress("");
		myButton = 0;
		myLQ1 = 0;
		myLQ2 = 0;
	}
	
	public String getDataLine() {
		return myLine;
	}
	
	public boolean isRequestMessage() {
		return myType == VERSION_TYPE;
	}
	
	public boolean isVersionMessage() {
		return myType == VERSION_TYPE;
	}
	
	public boolean isButtonMessage() {
		return myType == BUTTON_TYPE || myType == UPDATE_TYPE;
	}
	
	public boolean isStatusMessage() {
		return myType == STATUS_TYPE;
	}
	
	public String getVersion() {
		return myVersion;
	}
	
	public QuizBoxAddress getAddress() {
		return myAddress;
	}
	
	public int getButton() {
		int btn = QuizConstants.X_BTN;
		if (myButton==BUTTON_A) btn = QuizConstants.A_BTN;
		else if (myButton==BUTTON_B) btn = QuizConstants.B_BTN;
		else if (myButton==BUTTON_C) btn = QuizConstants.C_BTN;
		return btn;
	}
	
	public int getLQ1() {
		return myLQ1;
	}
	
	public int getLQ2() {
		return myLQ2;
	}
	
	public void parseLine(String line) {
		clear();
		myLine = line;
		if (myLine==null || myLine.trim().length()==0) {
			return;
		}
		char c = myLine.charAt(0);
		if (c=='^') {
			myType = VERSION_TYPE;
			myVersion = myLine.substring(1);
		} else if (c == '#') {
			myType = BUTTON_TYPE;
			myAddress.setBinaryAddress(myLine.substring(1,4));
			myButton = Integer.parseInt(myLine.substring(4, 6));
		} else if (c == '+') {
			myType = UPDATE_TYPE;
			myAddress.setBinaryAddress(myLine.substring(1,4));
			myButton = Integer.parseInt(myLine.substring(4, 6));
 		} else if (c == '@') {
			myType = STATUS_TYPE;
			myAddress.setBinaryAddress(myLine.substring(1,4));
			myLQ1 = Integer.parseInt(myLine.substring(4, 7))-255;
		} else {
			myType = NONE_TYPE;
		}
	}
	
	public static QuizMessage createRequest(String requestString) {
		QuizMessage qm = new QuizMessage();
		qm.myType = REQUEST_TYPE;
		qm.myLine = requestString;
		return qm;
	}
	
	public static QuizMessage createStatusRequest() {
		return createRequest("S");
	}
	
	public static QuizMessage createVersionRequest() {
		return createRequest("V");
	}
	
	public static QuizMessage createLockRequest() {
		return createRequest("L");
	}
	
	public static QuizMessage createLockClearRequest() {
		return createRequest("l");
	}
	
	public static QuizMessage createReadyRequest() {
		return createRequest("R");
	}
	
	public static QuizMessage createReadyClearRequest() {
		return createRequest("r");
	}
	
	public static QuizMessage createTestRequest() {
		return createRequest("T");
	}
	
	public static QuizMessage createTestClearRequest() {
		return createRequest("t");
	}
	
	public static QuizMessage createDemoRequest() {
		return createRequest("D");
	}
	
	public static QuizMessage createDemoClearRequest() {
		return createRequest("d");
	}
	
	public static QuizMessage createPowerDownRequest() {
		return createRequest("P");
	}
	
	public static QuizMessage createClearRequest() {
		return createRequest("C");
	}
	
	public static QuizMessage createDisplayRequest(boolean a, boolean b, boolean c, boolean p) {
		String str = ((a)?"T":"F") + ((b)?"T":"F") + ((c)?"T":"F") + ((p)?"T":"F");
		return createDisplayRequest(str);
	}
	
	public static QuizMessage createDisplayRequest(String s) {
		return createRequest("U" + s);
	}
	
	public static QuizMessage createLinkQualityRequest() {
		return createRequest("Q");
	}
	
	public static QuizMessage createLinkQualityRequest(String addr) {
		return createRequest("Q" + addr);
	}
	
	@Override
	public String toString() {
		String s = "[QuizMessage=Type:" + myType + ", Line: '" + myLine + "']";
		return s;
	}
}
