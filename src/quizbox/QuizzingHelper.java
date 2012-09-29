/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import quizbox.data.QuizBoxAddress;
import quizbox.data.QuizBoxData;
import quizbox.data.QuizConstants;
import quizbox.data.QuizMessage;
import quizbox.data.QuizTeam;
import quizbox.util.Logger;


public class QuizzingHelper implements QuizConstants {
	private final static Logger log = Logger.getLogger(QuizzingHelper.class);
	
	public static QuizMessage INIT_DISP = QuizMessage.createDisplayRequest("TTTT");
	public static QuizMessage CLEAR = QuizMessage.createClearRequest();
	public static QuizMessage STATUS = QuizMessage.createStatusRequest();
	public static QuizMessage QUALITY =  QuizMessage.createLinkQualityRequest();
	public static QuizMessage VERSION =  QuizMessage.createVersionRequest();
	public static QuizMessage POWER_DOWN =  QuizMessage.createPowerDownRequest();
	
	public static QuizMessage READY = QuizMessage.createReadyRequest();
	public static QuizMessage LOCK = QuizMessage.createLockRequest();
	public static QuizMessage DEMO = QuizMessage.createDemoRequest();
	public static QuizMessage TEST = QuizMessage.createTestRequest();
	
	public static QuizMessage READY_CLEAR =  QuizMessage.createReadyClearRequest();
	public static QuizMessage LOCK_CLEAR = QuizMessage.createLockClearRequest();
	public static QuizMessage DEMO_CLEAR =  QuizMessage.createDemoClearRequest();
	public static QuizMessage TEST_CLEAR = QuizMessage.createTestClearRequest();
	
	private QuizzingConnection myQuizConnection;
	private int myPlaceCount;
	private int myResultsCounter;
	private int myConnectionTimeout;
	
	public QuizzingHelper(QuizzingProperties props, QuizzingConnection qc) {
		myQuizConnection = qc;
		myPlaceCount = 0;
		myResultsCounter = 1;
		myConnectionTimeout = props.getConnectionTimeout();
	}
	
	public void reset() {
		if (log.isDebugEnabled()) log.debug("Sending Reset...");
		sendLockClear();
	}
	
	public void sendUnlock() {
		myQuizConnection.sendQuizMessageChecked(READY);
	}

	public void sendInitDisplay() {
		myQuizConnection.sendQuizMessageChecked(INIT_DISP);
	}
	public void sendClear() {
		myQuizConnection.sendQuizMessageChecked(CLEAR);
	}
	public void sendStatus() {
		myQuizConnection.sendQuizMessageChecked(STATUS);
	}
	public void sendQuality() {
		myQuizConnection.sendQuizMessageChecked(QUALITY);
	}
	public void sendVersion() {
		myQuizConnection.sendQuizMessageChecked(VERSION);
	}
	public void sendPowerDown() {
		myQuizConnection.sendQuizMessageChecked(POWER_DOWN);
	}

	public void sendReady() {
		myQuizConnection.sendQuizMessageChecked(READY);
	}
	public void sendLock() {
		myQuizConnection.sendQuizMessageChecked(LOCK);
	}
	public void sendDemo() {
		myQuizConnection.sendQuizMessageChecked(DEMO);
	}
	public void sendTest() {
		myQuizConnection.sendQuizMessageChecked(TEST);
	}

	public void sendReadyClear() {
		myQuizConnection.sendQuizMessageChecked(READY_CLEAR);
	}
	public void sendLockClear() {
		myQuizConnection.sendQuizMessageChecked(LOCK_CLEAR);
	}
	public void sendDemoClear() {
		myQuizConnection.sendQuizMessageChecked(DEMO_CLEAR);
	}
	public void sendTestClear() {
		myQuizConnection.sendQuizMessageChecked(TEST_CLEAR);
	}
	
	public void close() {
		myQuizConnection.close();
	}

	public void updateConnectionStatus() {
		long cur = System.currentTimeMillis();
		for (QuizBoxAddress addr : QuizBoxData.keySet()) {
			Long cTime = QuizBoxData.getQuizBoxData(addr).getConnectionTime();
			if (cTime != null) {
				long diff = cur - cTime;
				if (diff > myConnectionTimeout) {
					setConnectStatusValue(addr, NOT_CONNECTED_STATUS);
				} else {
					setConnectStatusValue(addr, CONNECTED_STATUS);						
				}
			} else {
				setConnectStatusValue(addr, NOT_CONNECTED_STATUS);
			}
		}		
	}

	public String getBtnString(QuizBoxAddress addr) {
		int r = QuizBoxData.getQuizBoxData(addr).getButtonState();
		String s = (r==NO_BTN?NO_STR:(r==A_BTN?A_STR:(r==B_BTN?B_STR:C_STR)));
		return s;
	}
	
	public int getPlaceCount() {
		return myPlaceCount;
	}
	
	public void resetPlaces() {
		myPlaceCount = 0;
		for (QuizBoxData qbd :  QuizBoxData.getValues()) {
			qbd.setPlacing(0);
		}
	}

	public int updatePlace(QuizBoxAddress addr) {
		int place = 0;
		QuizBoxData qbd = QuizBoxData.getQuizBoxData(addr);
		if (qbd != null) {
			int p = qbd.getPlacing();
			if (p <= 0) {
				if (qbd.getButtonState() != NO_BTN) {
					place = ++myPlaceCount;
					qbd.setPlacing(place);
				}
			} else {
				place = p;
			}
		}
		return place;
	}
	
	public String updatePlaceString(QuizBoxAddress addr) {
		int p = updatePlace(addr);
		if (p <= 0) return "";
		return ("" + p);
	}
	
	public int getConnectStatusValue(QuizBoxAddress addr) {
		QuizBoxData data = QuizBoxData.getQuizBoxData(addr);
		if (data == null) return NOT_CONNECTED_STATUS;
		
		Integer i = data.getStatus();
		if (i == null) return NOT_CONNECTED_STATUS;
		return i;
	}

	public int setConnectStatusValue(QuizBoxAddress addr, int status) {
		if (status != NOT_CONNECTED_STATUS) {
			int selection = QuizBoxData.getQuizBoxData(addr).getButtonState();
			if (selection==NO_BTN) {
				status = CONNECTED_STATUS;
			} else {
				if ((status & SELECTED_STATUS) == 0) {
					status = SELECTED_STATUS;
				}
			}
		}
		QuizBoxData data = QuizBoxData.getQuizBoxData(addr);
		if (data != null) data.setStatus(status);
    	return status;
    }
    
	public String getConnectStatusString(QuizBoxAddress addr, int status) {
		int lq = getLQ(addr);
		String txt = "["+String.format("%03d", lq) +":";
		if (status == NOT_CONNECTED_STATUS) {
			txt += NOT_CONNECT_STATUS_STR;
		} else if (status == CONNECTED_STATUS) {
			txt += CONNECT_STATUS_STR;
		} else if ((status & SELECTED_STATUS) != 0) {
			txt += SELECT_CONNECT_STATUS_STR;
		}
		txt += "]";
		return txt;
    }
	
	public Color getLQStatusColor(QuizBoxAddress addr, int status) {
		int lq = getLQ(addr);
		if (status == NOT_CONNECTED_STATUS) {
			return Color.GRAY;
		} else if (lq < LQ_LOW_SIGNAL_LEVEL) {
			return Color.BLUE;
		} else if (lq < LQ_MID_SIGNAL_LEVEL) {
			return Color.BLACK;
		}
		return Color.RED;		
	}
    
	public boolean getConnectStatusFlag(int status) {
		if (status == NOT_CONNECTED_STATUS) {
			return false;
		} else if (status == CONNECTED_STATUS) {
			return true;
		} else if ((status & SELECTED_STATUS) != 0) {
			return true;
		}
		return false;
    }
    
	public Color getConnectStatusColor(int status) {
		if (status == NOT_CONNECTED_STATUS) {
			return Color.GRAY;
		} else if (status == CONNECTED_STATUS) {
			return Color.BLACK;
		} else if (status == SELECTED_1ST_STATUS) {
			return Color.RED.darker();
		} else if (status == SELECTED_2ND_STATUS) {
			return Color.MAGENTA.darker().darker();
		} else if (status == SELECTED_3RD_STATUS) {
			return Color.BLUE.darker();
		} else if (status == SELECTED_STATUS) {
			return Color.BLUE;
		}
		return Color.GRAY;
    }
	
	public int getLQ(QuizBoxAddress addr) {
		QuizBoxData data = QuizBoxData.getQuizBoxData(addr);
		if (data != null) return data.getLQ();
		return 0;
	}
	
	public String getResults() {
		String results = "[" + (myResultsCounter++) + "]";
		for (QuizBoxAddress addr :  QuizBoxData.keySet()) {
			QuizBoxData qbd = QuizBoxData.getQuizBoxData(addr);
			int lq = getLQ(addr);
			String s = getBtnString(addr);
			String p = "" + qbd.getPlacing();
			String n = "" + qbd.getBoxNumber();
			results += "" + n + "," + s + "," + p + "," + lq + ";";
		}
	    return results;
	}
	
	public static String formatPoints(double points) {
		String s = String.format("%5.1f", points);
		if (s.endsWith(".0")) s = s.substring(0, s.length()-2).trim();
		return s;
	}
	
	public static String formatLongPoints(double points) {
		String s = String.format("%6.2f", points).trim();
		return s;
	}
	
	
	private static Clip ourBeepClip = null;
    public static synchronized void setupBeep(final String filename) {
        try {
        	if (ourBeepClip == null) ourBeepClip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(filename));
            ourBeepClip.open(inputStream);
          } catch (Exception e) {
            System.err.println(e.getMessage());
          }
    }
    
    public static synchronized void playBeep() {
        new Thread(new Runnable() { // the wrapper thread is unnecessary, unless it blocks on the Clip finishing, see comments
          public void run() {
            try {
            	ourBeepClip.setFramePosition(0);
            	ourBeepClip.start();
            } catch (Exception e) {
              System.err.println(e.getMessage());
            }
          }
        }).start();
    }
    
	public static void loadTeamsFromFile(String fname) {
		try {			
			File f = new File(fname);
			InputStream in = new FileInputStream(f);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			int i = 0;
			String s = null;
			while ((s = reader.readLine()) != null) {
				if (s.startsWith("#")) {
					// Ignore comment
				} else {
					String[] arr = s.split(",");
					
					QuizBoxAddress addr = null;
					String name = "";
					double points = 0;
					int count = 0;
					if (arr.length >= 1) {
						if (arr[0].contains("_")) {
							String[] arr2 = arr[0].split("_");
							String t = arr2[0].trim();
							i = Integer.parseInt(t);
						} else {
							i = i+1;
						}
					}
					if (arr.length >= 2) {
						addr = new QuizBoxAddress(arr[0]);
						name = arr[1];
					}
					if (arr.length >= 3){
						String n = arr[2];
						if (n != null) {
							points = Double.parseDouble(n.trim());
						}
					}
					if (arr.length >= 4) {
						// Do not read standing, it is transient
					}
					if (arr.length >= 5) {
						String n = arr[4];
						if (n != null) {
							count = Integer.parseInt(n.trim());
						}
					}
					
					if (addr != null) {
						if (name == null) name = "<none>";
						QuizTeam qteam = QuizTeam.createQuizTeam(addr, i, name.trim());
						qteam.initValues(points, count);
					} else {
						System.err.println("Problem parsing team string: " + s);
					}
				}
			}
			in.close();
			
		} catch (FileNotFoundException fnfex) {
			System.err.println("Log file not found: " + fname);
		} catch (IOException e) {
			System.err.println("IO problem with log file: " + fname);
			e.printStackTrace();
		} finally {
		}
    }
    
    // Fills in the standing field (1st, 2nd, 3rd, etc)
    public static void fillTeamStandings(Set<QuizTeam> teams) {
   		for (QuizTeam qt : teams) {
   			if (qt != null) qt.setStanding(0);
   		}
   		int next = 1;
   		int cur = 0;
    	while (next != cur) {
    		cur = next;
    		double best = -999.0;
    		for (QuizTeam qt : teams) {
    			if (qt != null && qt.getStanding() == 0) {
    				double d = qt.getTotalPoints();
    				if (d > best) {
    					best = d;
    				}
    			}
    		}
    		for (QuizTeam qt : teams) {
    			if (qt != null && qt.getTotalPoints() == best) {
    				qt.setStanding(cur);
    				next++;
    			}
    		}
    	}
    }
}

