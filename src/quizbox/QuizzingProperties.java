/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import quizbox.data.QuizBoxAddress;
import quizbox.data.QuizConstants;
import quizbox.data.QuizTeam;
import quizbox.util.Logger;

public class QuizzingProperties implements QuizConstants {
	private final static Logger log = Logger.getLogger(QuizzingProperties.class);

	private static final String PROP_FNAME = "quizzing.properties";

	private static final String COMPORT_PROP = "quizzing.connect.port";
	private static final String BAUD_PROP = "quizzing.connect.baud";
	private static final String CONNECT_TIMEOUT_PROP = "quizzing.connect.connectionTimeout";
	
	private static final String ADDR_PROP = "quizzing.remote.addresses";
	private static final String PANID_PROP = "quizzing.remote.panid";
	private static final String CHANNEL_PROP = "quizzing.remote.channel";

	private static final String TEAM_PROP = "quizzing.team";

	private static final String IN_FILE_PROP = "quizzing.control.inputfile";
	private static final String LOG_FILE_PROP = "quizzing.control.logfile";
	private static final String RESULTS_FILE_PROP = "quizzing.control.resultsfile";
	private static final String USE_IN_FILE_PROP = "quizzing.control.useInputFile";
	private static final String AUTO_SELECT_PROP = "quizzing.control.autoSelectQuestionType";
	private static final String MC_ON_PROP = "quizzing.control.multChoiceOn";
	private static final String SP_ON_PROP = "quizzing.control.speedOn";
	private static final String SP_SCORING_CORRECT_PROP = "quizzing.control.speedScoringCorrect";
	private static final String SP_SCORING_INCORRECT_PROP = "quizzing.control.speedScoringIncorrect";
	private static final String BONUS_POINTS_PROP = "quizzing.control.BonusPoints";
	private static final String MC_POINTS_LIST_PROP = "quizzing.control.multChoicePointsList";
	private static final String SP_POINTS_LIST_PROP = "quizzing.control.speedPointsList";
	private static final String ADV_ON_CLEAR_PROP = "quizzing.control.advanceQuestionNumberOnClear";
	private static final String QUIZ_TITLE_PROP = "quizzing.control.quizTitle";
	private static final String BEEP_ENABLED_PROP = "quizzing.control.isBeepEnabled";
	private static final String BEEP_FILENAME_PROP = "quizzing.control.beepFilename";
	private static final String START_QUESTION_NUM_PROP = "quizzing.control.startingQuestionNumber";
	private static final String QUIZ_OUT_COUNT_PROP = "quizzing.control.quizoutCount";
	private static final String EXT_FORMAT_REC_PROP = "quizzing.control.extendedFormat";
	private static final String PROCESSING_SLEEP_PROP = "quizzing.control.processingSleep";

	private static final String DISPLAY_FONTSIZE_PROP = "quizzing.display.fontsize";
	private static final String DISPLAY_SPEED_PLACE_PROP = "quizzing.display.speedPlace";
	private static final String DISPLAY_QUESTION_NUM_PROP = "quizzing.display.questionNumber";
	private static final String DISPLAY_QUESTION_TYPE_PROP = "quizzing.display.questionType";
	private static final String DISPLAY_TITLE_PROP = "quizzing.display.questionTitle";
	private static final String DISPLAY_ANNOUNCEMENT_PROP = "quizzing.display.announcement";
	private static final String DISPLAY_ANNOUNCE_AT_PROP = "quizzing.display.announceAtQuestionNumber";
	private static final String DISPLAY_COUNT_MC_PROP = "quizzing.display.showCountMultChoice";
	private static final String DISPLAY_COUNT_SP_PROP = "quizzing.display.showCountSpeed";

	private Properties myProps;
	private boolean myIsTeamLoadRequired;

	public QuizzingProperties() {
		this(PROP_FNAME);
	}

	public QuizzingProperties(String fname) {
		myProps = null;
		myIsTeamLoadRequired = true;
		load(fname);
	}

	public void load() {
		load(PROP_FNAME);
	}

	public void load(String fname) {
		if (myProps == null) {
			myProps = new Properties();
		}

		try {
			// try retrieve data from file
			myProps.load(new FileInputStream(fname));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getString(String propName) {
		String s = myProps.getProperty(propName);
		return s;
	}

	public String getString(String propName, String def) {
		String s = myProps.getProperty(propName);
		if (s == null)
			s = def;
		return s;
	}

	public int getInt(String propName, int def) {
		int i = def;
		boolean isHex = false;
		String s = myProps.getProperty(propName);
		if (s != null) {
			s= s.trim();
			if (s.startsWith("0x")) {
				isHex = true;
				s = s.substring(2);
			}
			try {
				if (isHex) {
					i = Integer.parseInt(s, 16);
				} else {
					i = Integer.parseInt(s);
				}
			} catch (NumberFormatException e) {
				i = def;
			}
		}
		return i;
	}

	public double getDouble(String propName, double def) {
		double d = def;
		String s = myProps.getProperty(propName);
		try {
			d = Double.parseDouble(s);
		} catch (NumberFormatException e) {
			d = def;
		}
		return d;
	}

	public boolean getBool(String propName, boolean def) {
		String s = myProps.getProperty(propName);
		boolean b = def;
		if (s != null && s.trim().length() > 0) {
			Boolean t = Boolean.parseBoolean(s.trim());
			if (t != null)
				b = t;
		}
		return b;
	}

	public String getInputFile() {
		return getString(IN_FILE_PROP);
	}

	public String getLogFile() {
		return getString(LOG_FILE_PROP);
	}

	public String getResultsFile() {
		return getString(RESULTS_FILE_PROP);
	}

	public String getComPort() {
		return getString(COMPORT_PROP);
	}

	public int getBaud() {
		return getInt(BAUD_PROP, 0);
	}

	public int getFontsize() {
		return getInt(DISPLAY_FONTSIZE_PROP, 12);
	}

	public boolean getIsAutoSelectQuestionType() {
		return getBool(AUTO_SELECT_PROP, false);
	}

	public boolean getMultChoiceSelectionEnabled() {
		return getBool(MC_ON_PROP, true);
	}

	public boolean getSpeedSelectionEnabled() {
		return getBool(SP_ON_PROP, true);
	}

	public double getSpeedScoringCorrect(int i) {
		return parseDoubleArray(SP_SCORING_CORRECT_PROP, i, 0.0);
	}

	public double getSpeedScoringIncorrect(int i) {
		return parseDoubleArray(SP_SCORING_INCORRECT_PROP, i, 0.0);
	}

	public int getBonusPoints() {
		return getInt(BONUS_POINTS_PROP, 0);
	}

	public boolean getAdvanceOnClear() {
		return getBool(ADV_ON_CLEAR_PROP, false);
	}

	public boolean getDisplaySpeedPlace() {
		return getBool(DISPLAY_SPEED_PLACE_PROP, false);
	}

	public boolean getDisplayCountSpeed() {
		return getBool(DISPLAY_COUNT_SP_PROP, false);
	}

	public boolean getDisplayCountMultChoice() {
		return getBool(DISPLAY_COUNT_MC_PROP, false);
	}

	public String getDisplayAnnouncement() {
		return getString(DISPLAY_ANNOUNCEMENT_PROP, "");
	}

	public int getDisplayAnnounceAt() {
		return getInt(DISPLAY_ANNOUNCE_AT_PROP, 0);
	}

	public boolean getDisplayTitle() {
		return getBool(DISPLAY_TITLE_PROP, false);
	}

	public boolean getDisplayQuestionType() {
		return getBool(DISPLAY_QUESTION_TYPE_PROP, false);
	}

	public boolean getDisplayQuestionNumber() {
		return getBool(DISPLAY_QUESTION_NUM_PROP, false);
	}

	public String getQuizTitle() {
		return getString(QUIZ_TITLE_PROP);
	}

	public boolean getIsBeepEnabled() {
		return getBool(BEEP_ENABLED_PROP, false);
	}

	public String getBeepFilename() {
		return getString(BEEP_FILENAME_PROP, "beep.wav");
	}

	public int getStartingQuestionNumber() {
		return getInt(START_QUESTION_NUM_PROP, 0);
	}

	public int getQuizOutCount() {
		return getInt(QUIZ_OUT_COUNT_PROP, 3);
	}

	public boolean getUseExtendedFormatRecording() {
		return getBool(EXT_FORMAT_REC_PROP, false);
	}
	
	public int getProcessingSleepTime(int def) {
		return getInt(PROCESSING_SLEEP_PROP, def);
	}
	

	public List<Integer> getMultChoicePointsList() {
		List<Integer> list = parseIntegerList(MC_POINTS_LIST_PROP);
		return list;
	}

	public List<Integer> getSpeedPointsList() {
		List<Integer> list = parseIntegerList(SP_POINTS_LIST_PROP);
		return list;
	}
		
	public int getConnectionTimeout() {
		int i = getInt(CONNECT_TIMEOUT_PROP, CONNECTION_TIMEOUT_MSEC);
		return i;
	}

	
	public int getPanID() {
		int i = getInt(PANID_PROP, -1);
		return i;
	}
	
	public int getChannel() {
		int i = getInt(CHANNEL_PROP, -1);
		return i;
	}
		
	public Map<String, QuizBoxAddress> getAllAddressesMap() {
		Map<String, QuizBoxAddress> map = new HashMap<String, QuizBoxAddress>();
		for (int i = 0; i < 100; i++) {
			Map<String, QuizBoxAddress> m = getAllAddressesMap(i);
			for (String k : m.keySet()) {
				map.put(k, m.get(k));
			}
		}
		return map;
	}

	public List<QuizBoxAddress> getAllAddresses() {
		List<QuizBoxAddress> addresses = new ArrayList<QuizBoxAddress>();
		for (int i = 0; i < 100; i++) {
			List<QuizBoxAddress> addr = getAllAddresses(i);
			addresses.addAll(addr);
		}
		return addresses;
	}
	
	private Map<String, QuizBoxAddress> getAllAddressesMap(int i) {
		Map<String, QuizBoxAddress> map = new HashMap<String, QuizBoxAddress>();
		String name = ADDR_PROP;
		if (i != 0) {
			name += String.format("%02d", i);
		}
		String s = getString(name);
		if (s != null && s.length() > 0) {
			String[] arr = s.split(",");
			for (String v : arr) {
				QuizBoxAddress addr = new QuizBoxAddress(v);
				map.put(addr.getName(), addr);
			}
		}
		return map;		
	}

	private List<QuizBoxAddress> getAllAddresses(int i) {
		List<QuizBoxAddress> set = new ArrayList<QuizBoxAddress>();
		String name = ADDR_PROP;
		if (i != 0) {
			name += String.format("%02d", i);
		}
		String s = getString(name);
		if (s != null && s.length() > 0) {
			String[] arr = s.split(",");
			for (String v : arr) {
				QuizBoxAddress addr = new QuizBoxAddress(v);
				set.add(addr);
			}
		}
		return set;
	}

	public Set<QuizBoxAddress> getAddressSet() {
		Set<QuizBoxAddress> addresses = new HashSet<QuizBoxAddress>();
		loadTeams();
		for (QuizTeam team : QuizTeam.getSortedTeams()) {
			addresses.add(team.getAddr());
		}
		return addresses;
	}

	public List<QuizBoxAddress> getAddressList() {
		List<QuizBoxAddress> addresses = new ArrayList<QuizBoxAddress>();
		loadTeams();
		for (QuizTeam team : QuizTeam.getSortedTeams()) {
			addresses.add(team.getAddr());
		}
		return addresses;
	}

	public boolean useInputFile() {
		Boolean b = getBool(USE_IN_FILE_PROP, false);
		return b;
	}

	public synchronized void loadTeams() {
		if (myIsTeamLoadRequired) {
			boolean b = useInputFile();
			if (b) {
				loadTeamsFromFile();
			} else {
				loadTeamsFromProperties();
			}
		}
	}

	private void loadTeamsFromFile() {
		myIsTeamLoadRequired = false;
		String fname = getInputFile();
		QuizzingHelper.loadTeamsFromFile(fname);
	}

	private void loadTeamsFromProperties() {
		myIsTeamLoadRequired = false;
		for (int i=0; i<100; i++) {
			loadQuizTeamFromProperties(i);
		}
	}

	private void loadQuizTeamFromProperties(int i) {
		String propname = TEAM_PROP + String.format("%02d", i);
		String s = getString(propname);
		if (s != null && s.trim().length() > 0) {
			String[] arr = s.trim().split(",");
			QuizBoxAddress addr = null;
			String name = "";
			double points = 0;
			int count = 0;
			if (arr.length >= 2) {
				addr = new QuizBoxAddress(arr[0]);
				name = arr[1];
			}
			if (arr.length >= 3) {
				String n = arr[2];
				if (n != null) {
					points = Double.parseDouble(n.trim());
				}
			}
			if (arr.length >= 4) {
				String n = arr[2];
				if (n != null) {
					count = Integer.parseInt(n.trim());
				}
			}

			if (addr != null) {
				if (name == null) name = "<none>";
				QuizTeam qteam = QuizTeam.createQuizTeam(addr, i, name.trim());
				qteam.initValues(points, count);
			} else {
				log.warn("Problem parsing team string: " + s);
			}
		}
	}

	public double parseDoubleArray(String key, int i, double def) {
		double d = def;
		String s = getString(key, "");
		if (s != null && s.trim().length() > 0) {
			String[] arr = s.split(",");
			if (arr.length > i && arr[i] != null && arr[i].trim().length() > 0) {
				try {
					d = Double.parseDouble(arr[i]);
				} catch (NumberFormatException nfex) {
					log.warn("Bad double: '" + arr[i] + "'");
				}
			}
		} else {
			log.warn("Bad key: '" + key + "'");
		}
		return d;
	}

	public List<Integer> parseIntegerList(String key) {
		List<Integer> list = new ArrayList<Integer>();
		String s = getString(key);
		if (s == null || s.trim().length() <= 0) {
			log.warn("Bad key: '" + key + "'");
			return list;
		}
		String[] arr = s.split(",");
		for (String v : arr) {
			v = v.trim();
			try {
				list.add(Integer.parseInt(v));
			} catch (NumberFormatException e) {
				log.warn("Unable to parse integer in " + key + ": '" + v + "'");
			}
		}
		return list;
	}
}
