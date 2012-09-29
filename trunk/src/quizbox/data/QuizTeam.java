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
import java.util.SortedSet;
import java.util.TreeSet;

import quizbox.util.Logger;


/**
 * Contains the state of a quiz team, including: Box Address, Box Number,
 * Total Points, Current Points, Bonus Points, Current Answer, Current Place (for speed).
 * 
 * @author Ted Meyers
 * @since August 2012
 */
public class QuizTeam implements Comparable<QuizTeam>, QuizConstants {
	private final static Logger log = Logger.getLogger(QuizTeam.class);
	private static final Map<QuizBoxAddress, QuizTeam> teams = new HashMap<QuizBoxAddress, QuizTeam>();
	private  static final SortedSet<QuizTeam> sortedTeams = new TreeSet<QuizTeam>();

	private final QuizBoxData myQuizBoxData;
	private final String myTeamName;
	
	private double myTotalPoints;
	private double myCurrentNormalPoints;
	private double myCurrentBonusPoints;
	private int myCorrectSpeedCount;
	
	// Transient
	private int myStanding;		// First, Second, Third, etc
	
	public static QuizTeam createQuizTeam(QuizBoxAddress address, int boxNumber, String teamName) {
		if (teams.containsKey(address)) {
			QuizTeam qt = teams.get(address);
			if (!qt.myTeamName.equals(teamName)) {
				log.warn("CreateQuizTeam found existing object: '" + qt.myTeamName +
					"' which differs from requested name: '" + teamName + "'");
			}
			return qt;
		}
		QuizBoxData qbd = QuizBoxData.createQuizBoxData(address, boxNumber);
		QuizTeam team = new QuizTeam(qbd, teamName);
		teams.put(address, team);
		sortedTeams.add(team);
		return team;
		
	}
	
	public static SortedSet<QuizTeam> getSortedTeams() {
		return sortedTeams;
	}
	
	public static QuizTeam createQuizTeam(QuizBoxAddress address, String teamName) {
		if (teams.containsKey(address)) {
			return teams.get(address);
		}
		QuizBoxData qbd = QuizBoxData.getQuizBoxData(address);
		QuizTeam team = new QuizTeam(qbd, teamName);
		return team;
		
	}
	
	public static QuizTeam getQuizTeam(QuizBoxAddress address) {
		if (teams.containsKey(address)) {
			return teams.get(address);
		}
		return null;
	}
	
	public static Collection<QuizTeam> getValues() {
		return teams.values();
	}
	
	public static Set<QuizBoxAddress> keySet() {
		return teams.keySet();
	}
	
	private QuizTeam(QuizBoxData qbd, String teamName) {
		myQuizBoxData = qbd;
		myTeamName = teamName;
		initValues(0, 0);
	}
	
	public void initValues(double points, int speedCount) {
		myTotalPoints = points;
		myCorrectSpeedCount = speedCount;
		
		myCurrentNormalPoints = 0;
		myCurrentBonusPoints = 0;
		myStanding = 0;		
	}
	
	public int getCorrectSpeedCount() {
		return myCorrectSpeedCount;
	}
	public int getCurrentAnswer() {
		return myQuizBoxData.getButtonState();
	}
	public int getCurrentPlacing() {
		return myQuizBoxData.getPlacing();
	}
	public double getTotalPoints() {
		return myTotalPoints;
	}
	public double getCurrentPoints() {
		return (myCurrentNormalPoints + myCurrentBonusPoints);
	}
	public double getCurrentNormalPoints() {
		return myCurrentNormalPoints;
	}
	public double getCurrentBonusPoints() {
		return myCurrentBonusPoints;
	}
	/*
	 * Returns the standing (First, Second, Third, etc)
	 */
	public int getStanding() {
		return myStanding;
	}
	
	public void incrementCorrectSpeedCount() {
		myCorrectSpeedCount++;
	}
	public void setCurrentAnswer(int a) {
		myQuizBoxData.setButtonState(a);
	}
	public void setCurrentPlacing(int i) {
		myQuizBoxData.setPlacing(i);
	}
	public void setCurrentNormalPoints(double d) {
		myCurrentNormalPoints = d;
	}
	public void setCurrentBonusPoints(double d) {
		myCurrentBonusPoints = d;
	}
	public void setStanding(int i) {
		myStanding = i;
	}
	
	public void incrementTotalPoints(double normalPointValue, double bonusPointValue) {
		double normal = normalPointValue * getCurrentNormalPoints();
		double bonus = bonusPointValue * getCurrentBonusPoints();
		myTotalPoints += normal + bonus;
	}	
	public void clearCurrentPoints() {
		myCurrentNormalPoints = 0.0;
		myCurrentBonusPoints = 0.0;		
	}
	public void clearCurrentInputs() {
		myQuizBoxData.clearStates();
	}
	public void clearCurrentValues() {
		clearCurrentInputs();
		clearCurrentPoints();
	}
	
	public int getBoxNumber() {
		return myQuizBoxData.getBoxNumber();
	}
	
	public String getTeamName() {
		return myTeamName;
	}
	
	public QuizBoxAddress getAddr() {
		return myQuizBoxData.getAddress();
	}
	
	public QuizBoxData getQuizBoxData() {
		return myQuizBoxData;
	}
	
	@Override
	public String toString() {
		String a = "<none>";
		String n = "<none>";
		String b = "<none>";
		if (myQuizBoxData.getAddress() != null) a = myQuizBoxData.getAddress().toShortString();
		if (myTeamName != null) n = myTeamName.toString();
		if (myQuizBoxData.getBoxNumber() > 0) b = "" + b;
		
		String s = "[Box:" + b + " Addr:" + a + " '" + n + "']";
		return s;
	}

	@Override
	public int compareTo(QuizTeam o) {
	    final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;

	    if (this == o) return EQUAL;

	    //primitive numbers follow this form
	    if (this.myQuizBoxData.getBoxNumber() < o.myQuizBoxData.getBoxNumber()) return BEFORE;
	    if (this.myQuizBoxData.getBoxNumber() > o.myQuizBoxData.getBoxNumber()) return AFTER;
		return 0;
	}
}
