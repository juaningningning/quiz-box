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
	public static final int MODE_TYPE = 6;
	public static final int INFO_TYPE = 7;
	
	private String myLine;
	private int myType;
	private int myButton;
	private int myLQ1;
	private int myLQ2;
	private QuizBoxAddress myAddress;
	private QuizBoxAddress myBaseAddress;
	private String myVersion;
	private String myInfo;
	private String myInfoType;
	private String myMode;
	
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
		myInfo = "";
		myInfoType = "";
		myType = 0;
		myMode = "";
		myAddress = new QuizBoxAddress("");
		myBaseAddress = new QuizBoxAddress("");
		myButton = 0;
		myLQ1 = 0;
		myLQ2 = 0;
	}
	
	public String getDataLine() {
		return myLine;
	}
	
	public int getMessageType() {
		return myType;
	}
	
	public boolean isButtonSelectMessage() {
		return myType == BUTTON_TYPE;
	}
	
	public boolean isButtonStatusMessage() {
		return myType == UPDATE_TYPE;
	}
	
	public boolean isButtonMessage() {
		return myType == BUTTON_TYPE || myType == UPDATE_TYPE;
	}
	
	public boolean isRequestMessage() {
		return myType == VERSION_TYPE;
	}
	
	public boolean isLinkStatusMessage() {
		return myType == STATUS_TYPE;
	}
	
	public boolean isVersionMessage() {
		return myType == VERSION_TYPE;
	}
	
	public boolean isModeMessage() {
		return myType == MODE_TYPE;
	}
	
	public boolean isInfoMessage() {
		return myType == INFO_TYPE;
	}
	
	public String getVersion() {
		return myVersion;
	}
	
	public String getInfoString() {
		return myInfo;
	}
	
	public String getInfoType() {
		return myInfoType;
	}
	
	public String getMode() {
		return myMode;
	}
	
	public QuizBoxAddress getAddress() {
		return myAddress;
	}
	
	public QuizBoxAddress getBaseAddress() {
		return myBaseAddress;
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
		
		char c = ' ';
		int len = myLine.length();
		if (len > 0) c = myLine.charAt(0);
		if (c=='^') {
			myType = VERSION_TYPE;
			if (len >= 3) {
				String[] fields = myLine.substring(1).split(":");
				if (fields.length > 0) {
					myVersion = fields[0];
					if (fields.length > 1) myInfo = fields[1];
				}
			}
		} else if (c == '#') {
			myType = BUTTON_TYPE;
			if (len >= 6) {
				myAddress.setBinaryAddress(myLine.substring(1,4));
				myButton = Integer.parseInt(myLine.substring(4, 6));
				if (len > 6) myLQ1 = myLine.charAt(6);
			}
		} else if (c == '+') {
			myType = UPDATE_TYPE;
			if (len >= 6) {
				myAddress.setBinaryAddress(myLine.substring(1,4));
				myButton = Integer.parseInt(myLine.substring(4, 6));
				if (len > 6) myLQ1 = myLine.charAt(6);
			}
 		} else if (c == '@') {
			myType = STATUS_TYPE;
			if (len >= 7) {
				myAddress.setBinaryAddress(myLine.substring(1,4));
				myBaseAddress.setBinaryAddress(myLine.substring(4,7));
				if (len > 7) myLQ1 = myLine.charAt(7);
				if (len > 7) myLQ2 = myLine.charAt(8);
			}
 		} else if (c == '!') {
			myType = MODE_TYPE;
			if (len >= 2) {
				myMode = ""+myLine.charAt(1);
				if (len > 2) myLQ1 = myLine.charAt(2);
			}
 		} else if (c=='%') {
			myType = INFO_TYPE;
			if (len >= 2) {
				myInfoType = "" + myLine.charAt(1);
				// skip ":"
				if (len > 3) myInfo = myLine.substring(3);
			}
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
		return createRequest("s");
	}
	
	public static QuizMessage createVersionRequest() {
		return createRequest("v");
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
		return createRequest("q");
	}
	
	public static QuizMessage createLinkQualityRequest(String addr) {
		return createRequest("q" + addr);
	}
	
	@Override
	public String toString() {
		String s = "[QuizMessage=Type:" + myType + ", Line: '" + myLine + "']";
		return s;
	}
}
