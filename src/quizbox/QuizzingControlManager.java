/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ButtonModel;

import quizbox.data.QuizBoxAddress;
import quizbox.data.QuizConstants;
import quizbox.data.QuizTeam;
import quizbox.util.Logger;


public class QuizzingControlManager implements QuizConstants {
	private final static Logger log = Logger.getLogger(QuizzingControlManager.class);
	
	private QuizzingHelper myQuizHelper;
	private QuizzingProperties myProps;
	private PrintWriter myLogPrintStream;
	
	private int myQuizoutBonus;
	private int myQuestionNumber;
	private int myQuestionType;
	private int myAnswer;
	private int myPoints;
	private int myWorkingPlace;
	
	private AtomicBoolean myIsLocked;
	private boolean myIsScheduledReset;
	private boolean myIsScheduledQuit;

	public QuizzingControlManager(QuizzingHelper quiz, QuizzingProperties props) {
		myQuizHelper = quiz;
		myProps = props;
		props.loadTeams();
		myQuizoutBonus = props.getBonusPoints();
			
		myIsLocked = new AtomicBoolean(false);
		myIsScheduledReset = false;		
		myIsScheduledQuit = false;
		
		myQuestionNumber = 0;
		myQuestionType = NONE_QUESTION_TYPE;
		myAnswer = NO_BTN;
		myPoints = 0;
		myWorkingPlace = 0;
		myQuizHelper.resetPlaces();
    	
		try {
			File f = new File(props.getLogFile());
			OutputStreamWriter fw = new FileWriter(f, true);
			myLogPrintStream = new PrintWriter(fw);
			myLogPrintStream.println("START" + SEP + getDateString() + SEP + getTimeString());
			myLogPrintStream.println("SAVE" + SEP + props.getResultsFile());	
			myLogPrintStream.flush();
			//clearResultsFile();
			
		} catch (FileNotFoundException fnfex) {
			myLogPrintStream = null;
			log.warn("Log file not found: " + props.getLogFile());
		} catch (IOException e) {
			log.warn("IO problem with log file: " + props.getLogFile());
			e.printStackTrace();
		}
		
		clearCurrentResults();
	}
	
	public boolean hasAnswers() {
		boolean b = false;
		for (QuizTeam qt : QuizTeam.getSortedTeams()) {
			if (qt != null && qt.getCurrentAnswer() != NO_BTN) {
				b = true;
				break;
			}
		}
		return b;
	}
	
	public void clearCurrentResults() {
		for (QuizTeam qt : QuizTeam.getSortedTeams()) {
			if (qt != null) qt.clearCurrentValues();
		}		
	}
	
	public void setCurrentNormalPoints(QuizBoxAddress addr, double points) {
		setCurrentNormalPoints(QuizTeam.getQuizTeam(addr), points);
	}
	public void setCurrentNormalPoints(QuizTeam qt, double points) {
		if (qt != null) qt.setCurrentNormalPoints(points);
	}
	
	public void setCurrentBonusPoints(QuizBoxAddress addr, double points) {
		setCurrentBonusPoints(QuizTeam.getQuizTeam(addr), points);
	}	
	public void setCurrentBonusPoints(QuizTeam qt, double points) {
		if (qt != null) qt.setCurrentBonusPoints(points);
	}	
	
	public void setCurrentAnswer(QuizBoxAddress addr, int answer) {
		setCurrentAnswer(QuizTeam.getQuizTeam(addr), answer);
	}
	public void setCurrentAnswer(QuizTeam qt, int answer) {
		if (qt != null) qt.setCurrentAnswer(answer);
	}
	
	
	public void setCurrentPlace(QuizBoxAddress addr) {
		if (myQuizHelper != null) {
			int placing = myQuizHelper.updatePlace(addr);
			setCurrentPlace(QuizTeam.getQuizTeam(addr), placing);
		}
	}
	public void setCurrentPlace(QuizBoxAddress addr, int place) {
		setCurrentPlace(QuizTeam.getQuizTeam(addr), place);
	}
	public void setCurrentPlace(QuizTeam qt, int place) {
		if (qt != null) qt.setCurrentPlacing(place);
	}
	
	public int getCurrentAnswer(QuizBoxAddress addr) {
		return getCurrentAnswer(QuizTeam.getQuizTeam(addr));
	}
	public int getCurrentAnswer(QuizTeam qt) {
		return (qt == null)?0:qt.getCurrentAnswer();
	}
	
	public int getCurrentPlace(QuizBoxAddress addr) {
		return getCurrentPlace(QuizTeam.getQuizTeam(addr));
	}
	public int getCurrentPlace(QuizTeam qt) {
		return (qt == null)?0:qt.getCurrentPlacing();
	}
	
	public int getCorrectSpeedCount(QuizBoxAddress addr) {
		return getCorrectSpeedCount(QuizTeam.getQuizTeam(addr));
	}
	public int getCorrectSpeedCount(QuizTeam qt) {
		return (qt == null)?0:qt.getCorrectSpeedCount();
	}
	
	public double getCurrentPoints(QuizBoxAddress addr) {
		return getCurrentPoints(QuizTeam.getQuizTeam(addr));		
	}
	public double getCurrentPoints(QuizTeam qt) {
		return (qt == null)?0.0:qt.getCurrentPoints();
	}
	
	public double getCurrentNormalPoints(QuizBoxAddress addr) {
		return getCurrentNormalPoints(QuizTeam.getQuizTeam(addr));		
	}
	public double getCurrentNormalPoints(QuizTeam qt) {
		return (qt == null)?0.0:qt.getCurrentNormalPoints();		
	}

	public double getCurrentBonusPoints(QuizBoxAddress addr) {
		return getCurrentBonusPoints(QuizTeam.getQuizTeam(addr));		
	}
	public double getCurrentBonusPoints(QuizTeam qt) {
		return (qt == null)?0.0:qt.getCurrentBonusPoints();		
	}
		
	public double getCurrentTotal(QuizBoxAddress addr) {
		return getCurrentTotal(QuizTeam.getQuizTeam(addr));		
	}
	public double getCurrentTotal(QuizTeam qt) {
		return (qt == null)?0.0:qt.getTotalPoints();		
	}
	
	
	public String getAnnouncement() {
		String s = "";
		int q = getQuestionNumber();
		int n = myProps.getDisplayAnnounceAt();
		if (q >= n) {
			s = myProps.getDisplayAnnouncement();
		}
		return s;
	}
	
	public int getQuestionNumber() {
		return myQuestionNumber;
	}
	public void setQuestionNumber(int n) {
		myQuestionNumber = n;
		log.info("QuestionNum = " + myQuestionNumber);
	}
	
	public int getQuestionType() {
		return myQuestionType;
	}
	public void setQuestionType(int n) {
		myQuestionType = n;
		log.debug("QuestionType = " + myQuestionType);
	}
	public void setQuesitonType(int n, ButtonModel mc, ButtonModel sp) {
		int mcPoints = 0;
		int spPoints = 0;
		 if (n == MC_QUESTION_TYPE) {
			if (mc != null) {
				String str = mc.getActionCommand();
				mcPoints = Integer.parseInt(str);
			}
		 } else if (n == SPEED_QUESTION_TYPE) {
			if (sp != null) {
				String str = sp.getActionCommand();
				spPoints = Integer.parseInt(str);
			}
		 }
		 setQuestionType(n, mcPoints, spPoints);
	}
	public void setQuestionType(int n, int mcPoints, int speedPoints) {
		setQuestionType(n);
		if (n == MC_QUESTION_TYPE) {
			setPoints(mcPoints);
		} else if (n == SPEED_QUESTION_TYPE) {
			setPoints(speedPoints);
		}
	}
	
	public int getAnswer() {
		return myAnswer;
	}	
	public String getAnswerString() {
		return translateAnswerND(getAnswer());
	}	
	public void setAnswer(int n) {
		myAnswer = n;
		log.debug("Answer = " + myAnswer);
	}
	
	public int getPoints() {
		return myPoints;
	}	
	public void setPoints(int n) {
		myPoints = n;
		log.debug("Points = " + myPoints);
	}
	
	public boolean isQuit() {
		return myIsScheduledQuit;
	}
	public void resetQuit() {
		myIsScheduledQuit = false;
		log.debug("Reset Quit");
	}
	public void scheduleQuit() {
		myIsScheduledQuit = true;
		log.debug("Quit Scheduled");
		if (myLogPrintStream != null) {
			myLogPrintStream.println("QUIT");
			myLogPrintStream.flush();
		}
	}
	
	public boolean getIsLocked() {
		return myIsLocked.get();
	}
	public boolean setIsLocked(boolean b, boolean doClear) {
		boolean prev = myIsLocked.getAndSet(b);
		if (b) {
			if (doClear) myQuizHelper.sendLockClear();
			else myQuizHelper.sendLock();
		} else {
			if (doClear) myQuizHelper.sendClear();
			myQuizHelper.sendUnlock();
		}
		if (prev != b) log.debug("IsLocked = " + myIsLocked);
		return prev;
	}
	
	public boolean isReset() {
		boolean b = myIsScheduledReset;
		myIsScheduledReset = false;
		return b;
	}
	public void scheduleReset() {
		myIsScheduledReset = true;
		log.debug("Scheduled Reset");
		if (myLogPrintStream != null) {
			myLogPrintStream.println("CLEAR");
			myLogPrintStream.flush();
		}
	}
	
	public void advanceQuestionNumber() {
		myQuestionNumber++;		
	}
	
	public void backupQuestionNumber() {
		myQuestionNumber--;		
	}
	
	public void nextQuestionNumber() {
		resetResults();
		setQuestionNumber(myQuestionNumber);
		setQuestionType(NONE_QUESTION_TYPE);
		setAnswer(NO_BTN);
		setPoints(0);
		
		String s = "NEXT" + SEP + getDateString() + SEP + getTimeString();
		if (myLogPrintStream != null) {
			myLogPrintStream.println(s);
			myLogPrintStream.flush();
		}
	}
	
	public void reset() {
		myQuizHelper.reset();
		resetResults();
	}
	
	public void resetResults() {
		resetPlace();
		clearCurrentResults();
	}
	
	public void resetPlace() {
		myQuizHelper.resetPlaces();
		myWorkingPlace = 1;		
	}
	public void setWorkingPlace(QuizBoxAddress addr, int adjust) {
		myWorkingPlace = myQuizHelper.updatePlace(addr) + adjust;
	}
	public boolean isValidSpeedPlace(QuizBoxAddress addr) {
		int p = getCurrentPlace(addr);
		return isBasicSpeedPlace(addr) && (myWorkingPlace >= p);
	}
	public boolean isBasicSpeedPlace(QuizBoxAddress addr) {
		int p = getCurrentPlace(addr);
		return (myQuestionType == SPEED_QUESTION_TYPE) && (p != 0) && (p < 3);
	}
	public boolean isEnabledSpeedPlace(QuizBoxAddress addr) {
		int p = getCurrentPlace(addr);
		return ((p == 1) || isValidSpeedPlace(addr));
	}
	public boolean isCorrectMultipleChoice(QuizBoxAddress addr) {
		int a = getCurrentAnswer(addr);
		return (a == myAnswer);
	}
	public boolean isCorrectSpeed(QuizBoxAddress addr) {
		double d = getCurrentPoints(addr);
		return (d > 0.0);
	}
	public boolean isIncorrectSpeed(QuizBoxAddress addr) {
		double d = getCurrentPoints(addr);
		return (d < 0.0);
	}
	
	public String getAnswerString(QuizBoxAddress addr) {
		int a = getCurrentAnswer(addr);
		return translateAnswerND(a);
	}	
	public int getAnswerValue(QuizBoxAddress addr) {
		return getCurrentAnswer(addr);
	}	
	public String getPlaceString(QuizBoxAddress addr) {
		int p = getCurrentPlace(addr);
		if (p <= 0) return "";
		return ("" + p);
	}
	public int getPlaceValue(QuizBoxAddress addr) {
		return getCurrentPlace(addr);
	}

	public boolean isQuizOut(QuizBoxAddress addr) {
		return isQuizOut(addr, 0);
	}	
	public boolean isQuizOut(QuizBoxAddress addr, int adjust) {
		boolean result = false;
		boolean isSpeed = (myQuestionType == SPEED_QUESTION_TYPE);
		if (isSpeed && (getCurrentPoints(addr) > 0)) {
			result = isQuizOutBasic(addr, adjust);
		}
		return result;
	}
	
	public boolean isQuizOutBasic(QuizBoxAddress addr, int adjust) {
		Integer cnt = getCorrectSpeedCount(addr)+adjust;
		boolean result = (cnt != null) && (cnt == myProps.getQuizOutCount());
		return result;
	}
	
	public void updateCurrentPoints(QuizBoxAddress addr, int speedAnswer) {
		double score = ZERO_POINTS;
		double bonus = 0.0;
		if (myQuestionType == MC_QUESTION_TYPE) {
			int answer = getCurrentAnswer(addr);
			if (answer == myAnswer && answer != NO_BTN) {
				score = FULL_POINTS;
			}
		} else if (myQuestionType == SPEED_QUESTION_TYPE) {
			double d = 0.0;
			boolean isCorrect = (speedAnswer == CORRECT_SPEED_ANSWER);
			boolean isIncorrect = (speedAnswer == INCORRECT_SPEED_ANSWER);
			if (isCorrect) {
				setWorkingPlace(addr, 0);
			} else if (isIncorrect) {
				setWorkingPlace(addr, 1);
			}
			int p = getCurrentPlace(addr);
			if (p == 1 && isCorrect) {
				// Zero all other scores
				for (QuizTeam qt : QuizTeam.getSortedTeams()) {
					if (qt != null) {
						qt.setCurrentNormalPoints(0.0);
						qt.setCurrentBonusPoints(0.0);
					}
				}
			}
			if (isCorrect) {
				if (isQuizOutBasic(addr, 1)) bonus = 1.0;
				d = myProps.getSpeedScoringCorrect(p-1);
			} else if (isIncorrect) {
				d = myProps.getSpeedScoringIncorrect(p-1);				
			}
			score = (d * FULL_POINTS);
		}
		setCurrentNormalPoints(addr, score);
		setCurrentBonusPoints(addr, bonus);
	}
	
	public void clearAllPoints() {
		for (QuizTeam qt : QuizTeam.getSortedTeams()) {
			if (qt != null) qt.clearCurrentPoints();
		}
	}
	
	public void updateScoresOnlyIfMultChoice() {
		if (myQuestionType == MC_QUESTION_TYPE) {
			for (QuizBoxAddress addr : QuizTeam.keySet()) {
				updateCurrentPoints(addr, NO_SPEED_ANSWER);
			}
		}
	}
	
	public void updateTotals() {
		for (QuizTeam qt : QuizTeam.getValues()) {
			if (qt != null) {
				double cur = getCurrentNormalPoints(qt);
				if (myQuestionType == SPEED_QUESTION_TYPE && (cur > 0)) {
					qt.incrementCorrectSpeedCount();
				}
				qt.incrementTotalPoints(myPoints, myQuizoutBonus);
			}
		}
	}
	
	public void clearTotals() {
		for (QuizTeam qt : QuizTeam.getValues()) {
			if (qt != null) qt.clearCurrentValues();
		}
	}	
	
	public double calcRealNormalPoints(QuizBoxAddress addr) {
		// Scores are stored (temporarily) as a fraction of the full points
		// To adjust to "real" score, multiply by myPoints.
		//
		double raw = getCurrentNormalPoints(addr);
		return (raw * myPoints);
	}
	public double calcRealNormalPoints(QuizTeam qt) {
		// Scores are stored (temporarily) as a fraction of the full points
		// To adjust to "real" score, multiply by myPoints.
		//
		double raw = (qt == null)?0.0:qt.getCurrentNormalPoints();
		return (raw * myPoints);
	}
	
	public double calcRealBonusPoints(QuizBoxAddress addr, int quizout_adj) {
		// Scores are stored (temporarily) as a fraction of the full points
		// To adjust to "real" score, multiply by myPoints.
		//
		double raw = getCurrentBonusPoints(addr);
		return (raw * myQuizoutBonus);
	}
	public double calcRealBonusPoints(QuizTeam qt, int quizout_adj) {
		// Scores are stored (temporarily) as a fraction of the full points
		// To adjust to "real" score, multiply by myPoints.
		//
		double raw = (qt == null)?0.0:qt.getCurrentBonusPoints();
		return (raw * myQuizoutBonus);
	}
	
	public void recordResults() {
		if (log.isInfoEnabled()) {
			String results = "Results: " + myQuizHelper.getResults();
			log.info(results);
		}
		updateTotals();
		QuizzingHelper.fillTeamStandings(QuizTeam.getSortedTeams());
		
		if (myLogPrintStream != null) myLogPrintStream.println("#");
		
		int questionNum = myQuestionNumber;
		String s = "# MSG" + SEP + "Question#" + SEP +
			"Type" + SEP + "-" + SEP + "-" + SEP + "-" + 
			SEP + "Answer" + SEP + "-" + SEP + "Points";
		if (myLogPrintStream != null) myLogPrintStream.println(s);
		
		s = "SUMMARY" + SEP + questionNum + SEP + 
		translateQuestionType(myQuestionType) + SEP + "-" + SEP + "-" + SEP + "-" + SEP +
		translateAnswer(myAnswer) + SEP + "-" + SEP + myPoints;
		if (myLogPrintStream != null) myLogPrintStream.println(s);
		
		s = "# MSG" + SEP + "Question#" + SEP +
			"Box#" + SEP + "Address" + SEP + "Name" + SEP + "SpCnt" + SEP + 
			"Answer" + SEP + "Place" + SEP +
			"Points" + SEP + "Bonus" + SEP + "Sum" + SEP + 
			"Previous" + SEP + "Current" + SEP + "Standing";
		if (myLogPrintStream != null) myLogPrintStream.println(s);
				
		for (QuizTeam qt : QuizTeam.getSortedTeams()) {
			if (qt != null) {
				QuizBoxAddress addr = qt.getAddr();
				String addrStr = addr.toShortString();
				String name = translateName(qt.getTeamName());
				double cur = calcRealNormalPoints(qt);
				double bonus = calcRealBonusPoints(qt, 0);
				double sum = cur+bonus;
				double total = qt.getTotalPoints();
				double prev = total - sum;
				int cnt = qt.getCorrectSpeedCount();
				String answer = translateAnswer(qt.getCurrentAnswer());
				int place = qt.getCurrentPlacing();
				int standing = qt.getStanding();
				
				String cstr = QuizzingHelper.formatLongPoints(cur);
				String bstr = QuizzingHelper.formatLongPoints(bonus);
				String sstr = QuizzingHelper.formatLongPoints(sum);
				String tstr = QuizzingHelper.formatLongPoints(total);
				String pstr = QuizzingHelper.formatLongPoints(prev);
				String str = "RESULTS" + SEP + questionNum + 
					SEP + qt.getBoxNumber() + SEP + "0x" + addrStr + SEP + name + 
					SEP + cnt + SEP + answer + SEP + place +
					SEP + cstr + SEP + bstr + SEP + sstr +
					SEP + pstr + SEP + tstr + SEP + standing;
				if (myLogPrintStream != null) myLogPrintStream.println(str);
			}
		}
		if (myLogPrintStream != null) myLogPrintStream.flush();
		
		publishResultsToFile();
	}
	
	private void publishResultsToFile() {
		try {
			String fname = myProps.getResultsFile();
			File f = new File(fname);
			PrintStream ps = new PrintStream(f);
			
			String title = translateName(myProps.getQuizTitle());
			int questnum = getQuestionNumber();
			String date = getDateString();
			String time = getTimeString();
			String header = "# " + "Box" + SEP + "Team" + SEP + "Score";
			if (myProps.getUseExtendedFormatRecording()) {
				header += SEP + "Place" + SEP + "SpCnt";
			}
			header += SEP + title + SEP + "Question #: " + SEP + questnum + SEP + date + SEP + time;
			ps.println(header);
			
			for (QuizTeam qt : QuizTeam.getSortedTeams()) {
				if (qt != null) {
					int box = qt.getBoxNumber();
					String addrStr = box + "_0x" + qt.getAddr().toShortString();
					String name = translateName(qt.getTeamName());
					double score = fixDouble(qt.getTotalPoints());
					String scorestr = QuizzingHelper.formatPoints(score);
					String str = addrStr + SEP + name + SEP + scorestr;
					if (myProps.getUseExtendedFormatRecording()) {
						int quizOutCnt = qt.getCorrectSpeedCount();
						int standing = qt.getStanding();
						str += SEP + standing + SEP + quizOutCnt;
					}
					ps.println(str);
				}
			}
			ps.flush();
			ps.close();
		} catch (FileNotFoundException fnfex) {
			log.warn("Results file not found: " + myProps.getResultsFile());
		}		
	}
	
	protected void clearResultsFile() {
		try {
			String fname = myProps.getResultsFile();
			File f = new File(fname);
			PrintStream ps = new PrintStream(f);			
			ps.println("");
			ps.flush();
			ps.close();
		} catch (FileNotFoundException fnfex) {
			log.warn("Results file not found: " + myProps.getResultsFile());
		}		
	}
	
	public String translateName(String s) {
		String t = s.replaceAll(",", "_");
		return t;
	}
	
	public String translateQuestionType(int n) {
		if (n == MC_QUESTION_TYPE) return "MC";
		if (n == SPEED_QUESTION_TYPE) return "SP";
		return "None";
	}
	
	
	public String translateAnswer(int n) {
		if (n == A_BTN) return A_STR;
		if (n == B_BTN) return B_STR;
		if (n == C_BTN) return C_STR;
		return N_STR;
	}
	
	private String translateAnswerND(int r) {
		String s = (r==NO_BTN?NO_STR:(r==A_BTN?A_STR:(r==B_BTN?B_STR:C_STR)));
		return s;		
	}
	
	public static final int fixInt(Integer i) {
		int r = (i==null)?0:i;
		return r;
	}
	
	public static final double fixDouble(Double d) {
		double r = (d==null)?0.0:d;
		return r;
	}
	
		
	public String getDateString() {
		Date date = new Date(System.currentTimeMillis());
		String s = DATE_FORMATTER.format(date);
		return s;
	}
	
	public String getTimeString() {
		Date date = new Date(System.currentTimeMillis());
		String s = TIME_FORMATTER.format(date);
		return s;
	}

	public void logToFile(String msg, boolean isTime) {
		if (isTime) {
			myLogPrintStream.println(msg + SEP + getDateString() + SEP + getTimeString());
		} else {
			myLogPrintStream.println(msg);
		}
	}
	
	public void close() {
		if (myLogPrintStream != null) {
			myLogPrintStream.println("CLOSE" + SEP + getDateString() + SEP + getTimeString());
			myLogPrintStream.flush();
			myLogPrintStream.close();
		}
		myQuizHelper.close();
	}
	
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM_dd_yyyy");
	public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");
}
