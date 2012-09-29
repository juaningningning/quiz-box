/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import quizbox.QuizzingConnection;
import quizbox.QuizzingControlManager;
import quizbox.QuizzingHelper;
import quizbox.QuizzingProperties;
import quizbox.data.QuizBoxAddress;
import quizbox.data.QuizBoxData;
import quizbox.data.QuizConstants;
import quizbox.data.QuizMessage;
import quizbox.data.QuizTeam;
import quizbox.io.IOutilities;
import quizbox.io.SerialListenerInterface;
import quizbox.util.Level;
import quizbox.util.Logger;


public class QuizzingControlGUI extends JFrame implements QuizConstants, SerialListenerInterface {
	private static final long serialVersionUID = 499006117212801232L;
	private final static Logger log = Logger.getLogger(QuizzingControlGUI.class);	
	
	private static final String MC_QUESTION_STR = "Multiple Choice";
	private static final String SPEED_QUESTION_STR = "Speed";
	private static final String QUIT_BTN_STR = "Quit";
	private static final String CLEAR_BTN_STR = "Clear";
	private static final String UNDO_BTN_STR = "Undo";
	private static final String BTN_LOCK_STR = "Lock";
	private static final String BTN_UNLOCK_STR = "Unlock";
	private static final String BTN_RECORD_STR = "Record";
	private static final String BTN_DISPLAY_STR = "Display";
	private static final String OPEN_STATUS_STR = "Input: Opened";
	private static final String LOCKED_STATUS_STR = "Input: Locked";
	private static final String ACTIONS_BTN_STR = "Actions";

	private static final String NO_CARD = "none";
	private static final String SP_CARD = "speed";
	private static final String MC_CARD = "multchoice";
	
	private QuizzingProperties myProps;
	private QuizzingHelper myQuizHelper;
	private QuizzingControlManager myControl;
	private QuizzingDisplayGUI myDisplayWindow;
	private QuizzingConnection myQuizConnection;
    private Set<QuizBoxAddress> myWarnedAddresses = new HashSet<QuizBoxAddress>();
		
    private Map<QuizBoxAddress, QuizTeamControlGuiItem> myTeamGuiItems;
	private List<JComponent> myComponentsList;
	private JPanel myPointsPanel;
	private ButtonGroup myQuestionTypeGroup;
	private ButtonGroup myAnswerGroup;
	private ButtonGroup myMultChoicePointsGroup;
	private ButtonGroup mySpeedPointsGroup;
	private JButton myClearButton;
	private JButton myUndoButton;
	private JButton myLockButton;
	private JButton myDisplayButton;
	private JButton myRecordButton;
	private JTextField myStatusField;
	private JLabel myLockStatusLabel;
	private JLabel myRoundTitleLabel;
	private JLabel myQuestionNumberLabel;
	private JRadioButton myMCQuestionButton;
	private JRadioButton mySpeedQuestionButton;
	private int myLastQT;
	private int myMCDefaultPoints;
	private int mySPDefaultPoints;
	private boolean myIsDisplayed;
	private boolean myIsBeeped;
	private boolean myIsBeepEnabled;
	
	public QuizzingControlGUI(String fname) {
		if (fname != null) {
			myProps = new QuizzingProperties(fname);			
		} else {
			myProps = new QuizzingProperties();
		}
		myProps.loadTeams();
		
		myTeamGuiItems = new HashMap<QuizBoxAddress, QuizTeamControlGuiItem>(); 
		myQuizConnection = openQuizzingConnection();
		myQuizHelper = new QuizzingHelper(myProps, myQuizConnection);
		myControl = new QuizzingControlManager(myQuizHelper, myProps);
		myIsBeepEnabled = myProps.getIsBeepEnabled();
		myIsBeeped = false;
		myIsDisplayed = false;
		myMCDefaultPoints = 0;
		mySPDefaultPoints = 0;
		myLastQT = NONE_QUESTION_TYPE;
		
		QuizzingHelper.setupBeep(myProps.getBeepFilename());
		
		if (myQuizConnection != null && !myQuizConnection.isConnected()) {
			String ports = "";
			for (String p : IOutilities.getPorts()) {
				if (ports.length() > 0) ports += ",  ";
				ports += p;
			}
			if (ports.length() == 0) ports = "None Found!";
			String msg = "USB quiz device not found, check connection";
			msg += " & configurations, then restart.";
			msg += "\nAvailable ports: " + ports;
			JOptionPane.showMessageDialog(this, //JOptionPane.WARNING_MESSAGE, 
					msg, "Quizzing Problem", JOptionPane.WARNING_MESSAGE);
		} else {
			myControl.setIsLocked(true, true);
		}
		
		myClearButton = new JButton(CLEAR_BTN_STR);
		myUndoButton = new JButton(UNDO_BTN_STR);
		myLockButton = new JButton(BTN_LOCK_STR);
		myRecordButton = new JButton(BTN_RECORD_STR);
		myDisplayButton = new JButton(BTN_DISPLAY_STR);
		myLockStatusLabel = new JLabel(OPEN_STATUS_STR);
		myComponentsList = new ArrayList<JComponent>();
		
		myPointsPanel = new JPanel(new CardLayout());
		myQuestionTypeGroup = new ButtonGroup();
		myAnswerGroup = new ButtonGroup();
		myMultChoicePointsGroup = new ButtonGroup();
		mySpeedPointsGroup = new ButtonGroup();

    	myStatusField = new JTextField();
    	myRoundTitleLabel = new JLabel(myProps.getQuizTitle());
    	myQuestionNumberLabel = new JLabel("");
    	
		int max = 0;
	    for (QuizTeam team : QuizTeam.getSortedTeams()) {
	    	String teamName = team.getTeamName();
	    	int sz = teamName.length();
	    	if (sz > max) max = sz;
	    }
    	sleep(100);
	    myDisplayWindow = new QuizzingDisplayGUI(myProps);
	    myDisplayWindow.validate();
	    myDisplayWindow.setVisible(true);
    	sleep(100);
		initGUI(max+5);
	}
	
	private void initGUI(int max) {
    	Dimension d = myLockStatusLabel.getPreferredSize();
    	d.width += 10;
    	myLockStatusLabel.setPreferredSize(d);
    	myLockStatusLabel.setMinimumSize(d);
    	
	    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    
	    JButton incQNBtn = new JButton(">");
	    incQNBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doNextQuestion(); }
	    });
	    JButton decQNBtn = new JButton("<");
	    decQNBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doPreviousQuestion(); }
	    });
	    
	    JPanel titlePanel = new JPanel();
	    titlePanel.add(myRoundTitleLabel);
	    titlePanel.add(Box.createHorizontalStrut(40));
	    titlePanel.add(myQuestionNumberLabel);
	    
	    final List<JComponent> pointsList = new ArrayList<JComponent>();
	    final List<JComponent> answerList = new ArrayList<JComponent>();
	    final List<JComponent> bothList = new ArrayList<JComponent>();
	    final List<JComponent> noneList = new ArrayList<JComponent>();
		    
	    JPanel answerPanel = new JPanel();
	    answerPanel.setLayout(new BoxLayout(answerPanel, BoxLayout.X_AXIS));
	    answerList.add(addLabel("Correct Answer: ", answerPanel));
	    answerPanel.add(Box.createHorizontalStrut(10));
	    answerList.add(addAnswerRadio(A_BTN, "A", answerPanel));
	    answerPanel.add(Box.createHorizontalStrut(20));
	    answerList.add(addAnswerRadio(B_BTN, "B", answerPanel));
	    answerPanel.add(Box.createHorizontalStrut(20));
	    answerList.add(addAnswerRadio(C_BTN, "C", answerPanel));
	    answerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    JPanel mcPointsPanel = new JPanel();
	    mcPointsPanel.setLayout(new BoxLayout(mcPointsPanel, BoxLayout.X_AXIS));
	    pointsList.add(addLabel("Points: ", mcPointsPanel));
     	for (int i : myProps.getMultChoicePointsList()) {
     		myMCDefaultPoints = i;
    		pointsList.add(addPointsRadio(i, ""+i, mcPointsPanel, myMultChoicePointsGroup));
    		mcPointsPanel.add(Box.createHorizontalStrut(10));
    	}
	    mcPointsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    JPanel speedPointsPanel = new JPanel();
	    speedPointsPanel.setLayout(new BoxLayout(speedPointsPanel, BoxLayout.X_AXIS));
	    pointsList.add(addLabel("Points: ", speedPointsPanel));
    	for (int i : myProps.getSpeedPointsList()) {
     		mySPDefaultPoints = i;
    		pointsList.add(addPointsRadio(i, ""+i, speedPointsPanel, mySpeedPointsGroup));
    		speedPointsPanel.add(Box.createHorizontalStrut(10));
    	}
	    speedPointsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    myPointsPanel.add(new JPanel(), NO_CARD);
	    myPointsPanel.add(mcPointsPanel, MC_CARD);
	    myPointsPanel.add(speedPointsPanel, SP_CARD);
		
	    bothList.addAll(answerList);
	    bothList.addAll(pointsList);
		myMCQuestionButton = addQuestionTypeRadio(MC_QUESTION_STR, MC_QUESTION_TYPE, bothList, noneList);
		mySpeedQuestionButton = addQuestionTypeRadio(SPEED_QUESTION_STR, SPEED_QUESTION_TYPE, pointsList, answerList);
	    	    
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.anchor = GridBagConstraints.WEST;
	    c.insets = new Insets(2, 5, 5, 2);
	    c.ipadx = 5; c.ipady = 0; c.weightx = 1.0;
	    c.gridx = 0; c.gridy = 0;
	    controlPanel.add(myLockStatusLabel, c);
	    c.gridx = 0; c.gridy = 1;	    
	    controlPanel.add(myLockButton, c);
	    c.gridx = 1; c.gridy = 0; c.gridwidth = 1;
	    controlPanel.add(myMCQuestionButton, c);
	    c.gridx = 1; c.gridy = 1; c.gridwidth = 1;
	    controlPanel.add(mySpeedQuestionButton, c);
	    c.gridx = 2; c.gridy = 0; c.gridwidth = 3;
	    controlPanel.add(answerPanel, c);
	    c.gridx = 2; c.gridy = 1; c.gridwidth = 3;
	    controlPanel.add(myPointsPanel, c);
	    c.gridx = 5; c.gridy = 0; c.gridwidth = 1; c.weightx = 1000.0;
	    controlPanel.add(new JLabel(""), c);
	    c.gridx = 5; c.gridy = 1; c.gridwidth = 1; c.weightx = 1000.0;
	    controlPanel.add(new JLabel(""), c);	    
	    c.gridwidth = 1; c.weightx = 1.0;
	    	        
	    this.setTitle("Quizzing Control");
	    this.setSize(new Dimension(WIDTH_WINDOW_SZ, HEIGHT_WINDOW_SZ));
	      
	    myLockButton.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) { doLock(); }
	    	}
		);	    
	    myRecordButton.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) { doRecord(); }
	    	}
		);    
	    myDisplayButton.addActionListener(
		    	new ActionListener() {
					public void actionPerformed(ActionEvent e) { doDisplay(); }
		    	}
			);

	    final Component parent = this;
	    myClearButton.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int option = JOptionPane.showConfirmDialog(parent, 
							"Are you sure you want to clear inputs?",
							"Clear Inputs", JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						doClear();
					}
				}
	    	}
		);
	    myClearButton.setEnabled(true);
	    
	    myUndoButton.addActionListener(
	    	new ActionListener() {
	    		public void actionPerformed(ActionEvent e) { doUndo(); }
	    	}
		);
	    
	    JButton quit = new JButton(QUIT_BTN_STR);
	    quit.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int option = JOptionPane.showConfirmDialog(parent, 
							"Are you sure you want to quit?",
							"Quit Quizzing", JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						myControl.scheduleQuit();
					}
				}
	    	}
		);
	    
	    JButton actions = new JButton(ACTIONS_BTN_STR);
	    actions.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doActions();
				}
	    	}
		);
	    
	    JPanel qnPanel = new JPanel(new GridLayout(1, 2));
	    qnPanel.add(decQNBtn);
	    qnPanel.add(incQNBtn);
	    
	    JPanel btnPanel = new JPanel(new GridLayout(1, 7));
	    btnPanel.add(myDisplayButton);
	    btnPanel.add(myRecordButton);
	    btnPanel.add(new JLabel(""));
	    btnPanel.add(myUndoButton);
	    btnPanel.add(myClearButton);
	    btnPanel.add(qnPanel);
	    btnPanel.add(actions);
	    btnPanel.add(quit);	    

	    SortedSet<QuizTeam> teams = QuizTeam.getSortedTeams();
	    int sz = teams.size()+1;
	    if (sz < 4) sz = 4;

	    SpringLayout spring = new SpringLayout();
	    JPanel resultsPanel = new JPanel(spring);
	    
	    Component last = null;
	    for (QuizTeam team : teams) {
	    	final QuizBoxAddress addr = team.getAddr();
	    	String num = String.format("%02d: ", team.getBoxNumber());

	    	JLabel statusLabel = new JLabel(DEFAULT_STATUS_LABEL);
	    	JLabel teamLabel = new JLabel(num + team.getTeamName());	    	
	    	JTextField answerTF = new JTextField("");
	    	JTextField placeTF = new JTextField("");
	    	JRadioButton correct = new JRadioButton("correct");
	    	JRadioButton incorrect = new JRadioButton("incorrect");
	    	JLabel quizout = new JLabel();
	    	JLabel points = new JLabel();
	    	ButtonGroup resultBG = new ButtonGroup();
	    	    	
	    	QuizTeamControlGuiItem qti = new QuizTeamControlGuiItem(statusLabel, teamLabel,
	    		answerTF, placeTF, correct, incorrect, quizout, points, resultBG);
	    	myTeamGuiItems.put(addr, qti);
	    	
	    	setConnectionStatus(addr, NOT_CONNECTED_STATUS);    	
	    	points.setPreferredSize(new Dimension(120, 20));
	    	correct.setEnabled(false);
	    	incorrect.setEnabled(false);
	    	quizout.setEnabled(false);
	    	points.setEnabled(false);
	    	resultBG.add(correct);
	    	resultBG.add(incorrect);
	    	
	    	correct.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { doSpeedCorrect(addr); }
	    	});
	    	
	    	incorrect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { doSpeedIncorrect(addr); }
	    	});
		    updatePointsLabel(addr, 0, 0, 0);
		    updateQuizoutLabel(addr, 0, false);

		    answerTF.setMinimumSize(new Dimension(30, 20));
	    	placeTF.setMinimumSize(new Dimension(30, 20));
		    answerTF.setPreferredSize(new Dimension(30, 20));
	    	placeTF.setPreferredSize(new Dimension(30, 20));
		    answerTF.setMaximumSize(new Dimension(30, 20));
	    	placeTF.setMaximumSize(new Dimension(30, 20));
		    
		    JPanel panel = new JPanel();
		    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		    panel.add(Box.createHorizontalStrut(5));
	    	panel.add(answerTF);
	    	panel.add(Box.createHorizontalStrut(5));
	    	panel.add(placeTF);
	    	panel.add(Box.createHorizontalStrut(5));
	    	panel.add(correct);
	    	panel.add(Box.createHorizontalStrut(5));
	    	panel.add(incorrect);
	    	panel.add(Box.createHorizontalStrut(20));
	    	panel.add(quizout);
	    	panel.add(Box.createHorizontalStrut(20));
	    	panel.add(points);
	    	panel.setOpaque(true);
	    	
	    	resultsPanel.add(panel);
	    	resultsPanel.add(statusLabel);
	    	resultsPanel.add(teamLabel);

	    	if (last == null) {
	    		spring.putConstraint(SpringLayout.NORTH, statusLabel, 8, SpringLayout.NORTH, resultsPanel);
	    	} else {
	    		spring.putConstraint(SpringLayout.NORTH, statusLabel, 8, SpringLayout.SOUTH, last);	    		
	    	}
    		spring.putConstraint(SpringLayout.VERTICAL_CENTER, panel, 0, SpringLayout.VERTICAL_CENTER, statusLabel);
    		spring.putConstraint(SpringLayout.BASELINE, teamLabel, 0, SpringLayout.BASELINE, statusLabel);	    	
	    	spring.putConstraint(SpringLayout.WEST, statusLabel, 10, SpringLayout.WEST, resultsPanel);
		    spring.putConstraint(SpringLayout.WEST, teamLabel, 10, SpringLayout.EAST, statusLabel);
		    spring.putConstraint(SpringLayout.EAST, panel, 10, SpringLayout.EAST, resultsPanel);
	    	last = statusLabel; 
	    }
 	
    	JPanel bottomPanel = new JPanel(new BorderLayout());
    	bottomPanel.add(controlPanel, BorderLayout.NORTH);
    	bottomPanel.add(btnPanel, BorderLayout.CENTER);
       	bottomPanel.add(myStatusField, BorderLayout.SOUTH);
	    
		JPanel contentPanel = (JPanel) this.getContentPane();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(titlePanel, BorderLayout.NORTH);
		contentPanel.add(resultsPanel, BorderLayout.CENTER);
	    contentPanel.add(bottomPanel, BorderLayout.SOUTH);
	    
	    setAllConnectionStatus(NOT_CONNECTED_STATUS);
	    myControl.setQuestionNumber(myProps.getStartingQuestionNumber());
	    myControl.advanceQuestionNumber();
	    reset(true);
	}
	
	private void setCorrectSpeedResult(QuizBoxAddress addr) {
		myControl.updateCurrentPoints(addr, CORRECT_SPEED_ANSWER);
		updateAllGUI();
	}
	private void setIncorrectSpeedResult(QuizBoxAddress addr) {
		myControl.updateCurrentPoints(addr, INCORRECT_SPEED_ANSWER);
		updateAllGUI();
	}
	private void clearDisplayedResults() {
		for (QuizTeamControlGuiItem item : myTeamGuiItems.values()) {
			item.myCorrectRB.setEnabled(false);
			item.myResultGroup.clearSelection();
		}
		myControl.resetPlace();
	}
	private void updateGUIResults() {
		for (QuizBoxAddress addr : QuizTeam.keySet()) {
			updateGUIResult(addr);
		}
	}
	private void updateGUIResult(QuizBoxAddress addr) {
		QuizTeamControlGuiItem item = myTeamGuiItems.get(addr);
		JRadioButton correctCB = item.myCorrectRB;
		JRadioButton incorrectCB = item.myIncorrectRB;
		ButtonGroup bg = item.myResultGroup;
		boolean isSpeed = (myControl.getQuestionType() == SPEED_QUESTION_TYPE);
		boolean isMultChoice = (myControl.getQuestionType() == MC_QUESTION_TYPE);
		if (isSpeed) {
			boolean isQuizOut = myControl.isQuizOut(addr);
			boolean b = myControl.isValidSpeedPlace(addr);
			if (check(correctCB, addr)) correctCB.setEnabled(b);
			if (check(incorrectCB, addr)) incorrectCB.setEnabled(b);
			if (myControl.isCorrectSpeed(addr)) {
				if (check(correctCB, addr)) correctCB.setSelected(true);
			} else if (myControl.isIncorrectSpeed(addr)) {
				if (check(incorrectCB, addr)) incorrectCB.setSelected(true);
			} else {
				if (check(bg, addr)) bg.clearSelection();
			}
			int cnt = myControl.getCorrectSpeedCount(addr);
			isQuizOut = updateQuizoutLabel(addr, cnt, isQuizOut);
			JLabel quizout = item.myQuizoutCB;
			if (check(quizout, addr)) {
				int qcnt = myProps.getQuizOutCount();
				if (isQuizOut || (cnt >= qcnt)) {
					quizout.setEnabled(true);
					quizout.setForeground(Color.red);
				} else if (cnt >= (qcnt-1)) {
					quizout.setEnabled(true);
					quizout.setForeground(Color.black);
				} else {
					quizout.setEnabled(false);
					quizout.setForeground(Color.black);
				}
			}
		} else if (isMultChoice) {
			int correct = myControl.getAnswer();
			if (check(correctCB, addr)) correctCB.setEnabled(false);
			if (check(incorrectCB, addr)) incorrectCB.setEnabled(false);
			if (correct == X_BTN) {
				if (check(bg, addr)) bg.clearSelection();
			} else {
				if (myControl.isCorrectMultipleChoice(addr)) {
					if (check(correctCB, addr)) correctCB.setSelected(true);
				} else {
					if (check(incorrectCB, addr)) incorrectCB.setSelected(true);
				}
			}
		} else {
			if (check(correctCB, addr)) correctCB.setEnabled(false);
			if (check(incorrectCB, addr)) incorrectCB.setEnabled(false);
			if (check(bg, addr)) bg.clearSelection();
		}
	}

	private JLabel addLabel(final String labelStr, JPanel panel) {
	    JLabel label = new JLabel(labelStr);
	    label.setEnabled(false);
	    panel.add(label);
	    myComponentsList.add(label);
	    return label;
	}
	
	private JRadioButton addQuestionTypeRadio(String label,
			final int questionType, final List<JComponent> listOn, final List<JComponent> listOff) {
	    JRadioButton radio = new JRadioButton(label);
	    myQuestionTypeGroup.add(radio);
	    radio.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		doQuestionType(questionType, listOn, listOff);
	    	}
	    });
	    return radio;
	}
	
	private JRadioButton addPointsRadio(final int points, String label, JPanel panel, ButtonGroup bg) {
	    final JRadioButton radio = new JRadioButton(label);
	    bg.add(radio);
	    panel.add(radio);
	    radio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doSetPoints(points); }
		});		
	    radio.setActionCommand(""+points);
	    radio.setEnabled(false);
	    myComponentsList.add(radio);
	    return radio;
	}
	
	private JRadioButton addAnswerRadio(final int val, String label, JPanel panel) {
	    final JRadioButton radio = new JRadioButton(label);
	    myAnswerGroup.add(radio);
	    panel.add(radio);
	    radio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doSetAnswer(val); }
		});		
	    radio.setEnabled(false);
	    myComponentsList.add(radio);
	    return radio;
	}
	
	public void doProcessing() {
		myControl.resetQuit();
		
		int sleep_ms = myProps.getProcessingSleepTime(PROCESSING_DEF_SLEEP_MSEC);
		sleep_ms = Math.min(sleep_ms, PROCESSING_MAX_SLEEP_MSEC);
		
		int i = 0;
		while (!myControl.isQuit()) {
			if (i%UPDATE_STATUS_RATE==0) {
				updateStatus();
			}			
			if (i%UPDATE_DISPLAY_GUI_RATE==0) {
				inBetweenDisplayUpdate();
			}
			if (i%UPDATE_GUI_RATE==0) {
				for (QuizBoxAddress addr : QuizTeam.keySet()) {
					if (addr != null) {
						updateGUI(addr);
					}
				}
				updateControlButtons();
			}
			if (sleep_ms > 0) {
			    sleep(sleep_ms);
			}			
			i++;
		}
		quit();
	}
	
	private void updateGUI(QuizBoxAddress addr) {
		QuizTeamControlGuiItem item = myTeamGuiItems.get(addr);
		JTextField atf = item.myAnswerTF;
		if (check(atf, addr)) atf.setText(myControl.getAnswerString(addr));
		JTextField ptf = item.myPlaceTF;
		if (check(ptf, addr)) ptf.setText(myControl.getPlaceString(addr));
		updatePoints(addr);
		updateGUIResult(addr);
		updateQuizoutLabel(addr);

		int p = myControl.getPlaceValue(addr);
		if (myIsBeepEnabled && !myIsBeeped && p==1 &&
			myControl.getQuestionType()==SPEED_QUESTION_TYPE) {
			myIsBeeped = true;
			QuizzingHelper.playBeep();
		}
		if (myControl.getAnswerValue(addr) != NO_BTN) {
			updateConnectionStatus(addr, false);
		}
	}
	
	private void updateStatus() {		
		if (myControl.isReset()) {
			if (myProps.getAdvanceOnClear()) {
				myControl.advanceQuestionNumber();
			}
			reset(false);
		}
		myQuizHelper.updateConnectionStatus();
		updateAllConnectionStatus(false);
		String status = myQuizConnection.getConnectionMessage();
		myStatusField.setText(status);
	}
	
	private void doSetPoints(int points) {
		doSetupDisplay();
		myControl.setPoints(points);
		updateAllGUI();		
	}
	
	private void doSetAnswer(int answer) {
		doSetupDisplay();
		myControl.setAnswer(answer);
		updateAllGUI();		
	}
	
	private void doSpeedCorrect(QuizBoxAddress addr) {
		doSetupDisplay();
		setCorrectSpeedResult(addr);
	}
	
	private void doSpeedIncorrect(QuizBoxAddress addr) {
		doSetupDisplay();
		setIncorrectSpeedResult(addr);		
	}
	
	private void doLock() {
		doSetupDisplay();
		toggleLock();
	}
	
	private void doNextQuestion() {
		myControl.advanceQuestionNumber();
		myDisplayWindow.conditionallySetQuestionNumber(myControl.getQuestionNumber());
		updateAllGUI();		
	}
	
	private void doPreviousQuestion() {
		myControl.backupQuestionNumber();
		myDisplayWindow.conditionallySetQuestionNumber(myControl.getQuestionNumber());
		updateAllGUI();		
	}
	
	private void doQuestionType(int questionType,  List<JComponent> listOn, List<JComponent> listOff) {
		setupIgnoreUpdates(false);
		showComponents(listOn, true);
		showComponents(listOff, false);
		ButtonModel mc = myMultChoicePointsGroup.getSelection();
		ButtonModel sp = mySpeedPointsGroup.getSelection();
		myControl.setQuesitonType(questionType, mc, sp);
		myLastQT = questionType;
		if (myProps.getSpeedSelectionEnabled() && (questionType == SPEED_QUESTION_TYPE)) {
			myIsBeeped = false;
			myMCQuestionButton.setEnabled(false);
			myControl.setAnswer(X_BTN);
			myAnswerGroup.clearSelection();
			CardLayout cl = (CardLayout)(myPointsPanel.getLayout());
			cl.show(myPointsPanel, SP_CARD);
			if (mySpeedPointsGroup.getButtonCount() == 1) {
				mySpeedPointsGroup.getElements().nextElement().setSelected(true);
				myControl.setPoints(mySPDefaultPoints);
			}
		} else if (myProps.getMultChoiceSelectionEnabled() && (questionType == MC_QUESTION_TYPE)) {
			mySpeedQuestionButton.setEnabled(false);
			CardLayout cl = (CardLayout)(myPointsPanel.getLayout());
			cl.show(myPointsPanel, MC_CARD);
			if (myMultChoicePointsGroup.getButtonCount() == 1) {
				myMultChoicePointsGroup.getElements().nextElement().setSelected(true);
				myControl.setPoints(myMCDefaultPoints);
			}
		}
		doSetupDisplay();
		clearAllPoints();
		updateAllPoints();
		//clearDisplayedResults();
		updateAllGUI();
		clearIgnoreUpdates(false, false);		
	}
	
	private void updateLock(boolean b) {
		if (b) {
			myLockButton.setText(BTN_UNLOCK_STR);
			myLockStatusLabel.setText(LOCKED_STATUS_STR);
			myLockStatusLabel.setForeground(Color.red);
		} else {
			myLockButton.setText(BTN_LOCK_STR);
			myLockStatusLabel.setText(OPEN_STATUS_STR);
			myLockStatusLabel.setForeground(Color.green);
		}
	}	
	private void toggleLock() {
		boolean b = !myControl.getIsLocked();
		setLock(b, false);
		updateAllGUI();
	}
	private void setLock(boolean b, boolean doClear) {
		myControl.setIsLocked(b, doClear);
		myControl.updateScoresOnlyIfMultChoice();
		updateLock(b);
	}
	private void doRecord() {
		setupIgnoreUpdates(false);
		
		myControl.updateScoresOnlyIfMultChoice();
		myControl.recordResults();
		updateGUIResults();
		myControl.advanceQuestionNumber();
		myControl.clearTotals();
		
		resetInternal(true);
		clearIgnoreUpdates(true, true);		
	}
	private void doSetupDisplay() {
		myIsDisplayed = false;
		updateDisplayGUIQuestionNumber();
		updateDisplayGUIQuestionType();
		updateDisplayGUIAnnouncement();
		myDisplayWindow.setCorrectAnswer("");
		myDisplayWindow.setPossiblePoints("");
		for (QuizBoxAddress addr : QuizTeam.keySet()) {
			myDisplayWindow.clearPoints(addr);
			myDisplayWindow.setAnswer(addr, "");
		}
	}
	private void doDisplay() {
		myIsDisplayed = true;
		myControl.setIsLocked(true, false);
		myControl.updateScoresOnlyIfMultChoice();
		updateGUIResults();
		updateDisplayWindow(1);
		updateAllGUI();
	}
	
	
	private void updateQuestionLabel() {
		String str = "";
		str += " Question: " + myControl.getQuestionNumber();
		myQuestionNumberLabel.setText(str);
	}
	
	private void updateControlButtons() {
		int rt = myControl.getQuestionType();
		int answer = myControl.getAnswer();
		int points = myControl.getPoints();
		boolean isEnabled = !(points <= 0 || (rt == MC_QUESTION_TYPE && answer == NO_BTN));
		//boolean isClearEnabled = (rt != NONE_QUESTION_TYPE) || myControl.hasAnswers();
		if (rt == SPEED_QUESTION_TYPE && isEnabled) {
			for (QuizTeamControlGuiItem item : myTeamGuiItems.values()) {
				JRadioButton rb1 = item.myCorrectRB;
				JRadioButton rb2 = item.myIncorrectRB;
				if (rb1.isEnabled() && rb2.isEnabled() && !rb1.isSelected() && !rb2.isSelected()) {
					isEnabled = false;
					break;
				}
			}
		}
		boolean isDisp = isEnabled && !myIsDisplayed;
		myDisplayButton.setEnabled(isDisp);
		boolean isRec = isEnabled && myControl.getIsLocked() && myIsDisplayed;
		myClearButton.setEnabled(true);
		myRecordButton.setEnabled(isRec);
		updateLock(myControl.getIsLocked());
	}
	
	private void inBetweenDisplayUpdate() {
		int qtyp = myControl.getQuestionType();
		boolean isMC = (qtyp == MC_QUESTION_TYPE) && myProps.getDisplayCountMultChoice();
		boolean isSP = (qtyp == SPEED_QUESTION_TYPE) && myProps.getDisplayCountSpeed();
		if (isMC || isSP) {
			int i = 0;
			for (QuizTeam qt : QuizTeam.getSortedTeams()) {
				if (qt.getCurrentAnswer() == NO_BTN) i++;
			}
			myDisplayWindow.setCount("" + i);
		} else {
			myDisplayWindow.setCount("");
		}
	}
	
	private void updateDisplayWindow(int quizoutAdj) {
		boolean isSpeedPlace = myProps.getDisplaySpeedPlace();
		boolean isMultChoice = (myControl.getQuestionType() == MC_QUESTION_TYPE);
		updateDisplayGUIQuestionNumber();
		updateDisplayGUIQuestionType();
		updateDisplayGUIAnnouncement();
		updateDisplayGUICorrectAnswer();
		updateDisplayGUIPossiblePoints();
		for (QuizBoxAddress addr : QuizTeam.keySet()) {
			double points = myControl.calcRealNormalPoints(addr);
			double bonus = myControl.calcRealBonusPoints(addr, quizoutAdj);
			String answer = myControl.getAnswerString(addr);
			String place = myControl.getPlaceString(addr);
			myDisplayWindow.setPoints(addr, points);
			myDisplayWindow.setBonus(addr, points, bonus);
			if (isMultChoice) {
				myDisplayWindow.setAnswer(addr, answer);
			} else if (isSpeedPlace && myControl.isBasicSpeedPlace(addr) && (points != 0)) {
				myDisplayWindow.setAnswer(addr, place);
			}
		}
		updateControlButtons();
	}
	private void updateDisplayGUICorrectAnswer() {
		String s = myControl.getAnswerString();
		myDisplayWindow.setCorrectAnswer(s);
	}
	private void updateDisplayGUIPossiblePoints() {
		int n = myControl.getPoints();
		myDisplayWindow.setPossiblePoints(""+n);
	}
	
	private void updateDisplayGUIAnnouncement() {
		String s = myControl.getAnnouncement();
		myDisplayWindow.setAnnouncement(s);
	}
	private void updateDisplayGUIQuestionNumber() {
		int n = myControl.getQuestionNumber();
		myDisplayWindow.setQuestionNumber(n);
	}
	private void updateDisplayGUIQuestionType() {
		int n = myControl.getQuestionType();
		if (n == MC_QUESTION_TYPE) {
			myDisplayWindow.setQuestionType(MC_DISPLAY_LABEL);
		} else if (n == SPEED_QUESTION_TYPE) {
			myDisplayWindow.setQuestionType(SP_DISPLAY_LABEL);
		} else {
			myDisplayWindow.setQuestionType("");
		}
	}
	
	private void updateAllGUI() {
		updateGUIResults();
		updateControlButtons();
		updateQuestionLabel();
		updateAllPoints();
		updateAllQuizoutLabel();
		updateAllConnectionStatus(true);
	}
	
	private void updateMinGUI() {
		updateGUIResults();
		updateQuestionLabel();
		updateAllPoints();
		updateAllConnectionStatus(true);
	}
	
	private void clearAllPoints() {
		myControl.clearAllPoints();
	}
	
	private void updateAllPoints() {
		for (QuizBoxAddress addr : QuizTeam.keySet()) {
			updatePoints(addr);
		}
	}
	
	private void updatePoints(QuizBoxAddress addr) {
		myControl.updateScoresOnlyIfMultChoice();
		double points = myControl.calcRealNormalPoints(addr);
		double bonus = myControl.calcRealBonusPoints(addr, 1);
		double total = myControl.getCurrentTotal(addr);
		updatePointsLabel(addr, points, bonus, total);
	}
	
	private void updatePointsLabel(QuizBoxAddress addr, double points, double bonus, double total) {
		String p_str = QuizzingHelper.formatPoints(points+bonus);
		String t_str = QuizzingHelper.formatPoints(total);
		String s = "[points: " + p_str + " | " + t_str + "]";
		QuizTeamControlGuiItem item = myTeamGuiItems.get(addr);
		JLabel p = item.myPointsLabel;
		if (check(p, addr)) p.setText(s);
	}
	
	private void updateAllQuizoutLabel() {
		for (QuizBoxAddress addr : QuizTeam.keySet()) {
			updateQuizoutLabel(addr);
		}
	}

	private void updateQuizoutLabel(QuizBoxAddress addr) {
		boolean isQuizOut = myControl.isQuizOut(addr);
		int cnt = myControl.getCorrectSpeedCount(addr);
		updateQuizoutLabel(addr, cnt, isQuizOut);
	}
	
	private boolean updateQuizoutLabel(QuizBoxAddress addr, int count, boolean isQuizout) {
		String s = "[" + count + "] quizout";
		isQuizout = (isQuizout || count >= myProps.getQuizOutCount());
		if (isQuizout) {
			s = "[x] quizout";
		}
		QuizTeamControlGuiItem item = myTeamGuiItems.get(addr);
		JLabel cb = item.myQuizoutCB;
		if (check(cb, addr)) cb.setText(s);
		return isQuizout;
	}
	
	private void doUndo() {
		if (myIsDisplayed) {
			doSetupDisplay();
			myDisplayButton.setEnabled(true);
			myRecordButton.setEnabled(false);
		} else {
			setLock(true, false);
			
			myControl.setQuestionType(NONE_QUESTION_TYPE);
			myControl.setAnswer(X_BTN);
			myControl.setPoints(0);
			myAnswerGroup.clearSelection();
			mySpeedPointsGroup.clearSelection();
			myMultChoicePointsGroup.clearSelection();
			myQuestionTypeGroup.clearSelection();
			myMCQuestionButton.setEnabled(myProps.getMultChoiceSelectionEnabled());
			mySpeedQuestionButton.setEnabled(myProps.getSpeedSelectionEnabled());
			myDisplayButton.setEnabled(false);
			myRecordButton.setEnabled(false);
			showComponents(myComponentsList, false);
			CardLayout cl = (CardLayout)(myPointsPanel.getLayout());
			cl.show(myPointsPanel, NO_CARD);
			for (QuizTeamControlGuiItem item : myTeamGuiItems.values()) {
				JRadioButton correctCB = item.myCorrectRB;
				correctCB.setSelected(false);
				correctCB.setEnabled(false);
				JRadioButton incorrectCB = item.myIncorrectRB;
				incorrectCB.setSelected(false);
				incorrectCB.setEnabled(false);
				item.myResultGroup.clearSelection();
			}
		}
	}
	
	private void doClear() {
		boolean isLocked = setupIgnoreUpdates(true);
		clearDisplayInternal();
		clearIgnoreUpdates(isLocked, true);		
	}
	
	private void clearDisplayInternal() {
		myIsDisplayed = false;
		myControl.resetResults();
		updateDisplayGUIQuestionNumber();
		updateDisplayGUIQuestionType();
		updateDisplayGUIAnnouncement();
		myDisplayWindow.setCorrectAnswer("");
		myDisplayWindow.setPossiblePoints("");
		for (QuizBoxAddress addr : QuizTeam.keySet()) {
			myDisplayWindow.clearPoints(addr);
			myDisplayWindow.setAnswer(addr, "");
		}
		int rt = myControl.getQuestionType();
		if (rt == SPEED_QUESTION_TYPE) {
			myDisplayButton.setEnabled(true);
			myIsBeeped = false;
		}
		updateAllGUI();
		for (QuizTeamControlGuiItem item : myTeamGuiItems.values()) {
			item.myAnswerTF.setText("");
			item.myPlaceTF.setText("");
		}
	}
	
	private void reset(boolean isFull) {
		boolean isLocked = setupIgnoreUpdates(true);
		resetInternal(isFull);
		clearIgnoreUpdates(isLocked, true);		
	}
	
	private void resetInternal(boolean isFull) {
		myIsDisplayed = false;
		if (isFull) {
			myControl.nextQuestionNumber();
			myQuestionTypeGroup.clearSelection();
			myAnswerGroup.clearSelection();
			mySpeedPointsGroup.clearSelection();
			myMultChoicePointsGroup.clearSelection();			
			myMCQuestionButton.setEnabled(myProps.getMultChoiceSelectionEnabled());
			mySpeedQuestionButton.setEnabled(myProps.getSpeedSelectionEnabled());
			showComponents(myComponentsList, false);
			CardLayout cl = (CardLayout)(myPointsPanel.getLayout());
			cl.show(myPointsPanel, NO_CARD);
			
			updateAllGUI();
			
			if (myProps.getIsAutoSelectQuestionType()) {
				if (myLastQT == MC_QUESTION_TYPE) {
					myMCQuestionButton.doClick();
				} else if (myLastQT == SPEED_QUESTION_TYPE) {
					mySpeedQuestionButton.doClick();
				}
			}
		} else {
			myControl.resetResults();
			updateMinGUI();
		}

		for (QuizTeamControlGuiItem item : myTeamGuiItems.values()) {
			item.myAnswerTF.setText("");
			item.myPlaceTF.setText("");
		}
		myIsBeeped = false;
	}
	
    private void quit() {
    	myControl.close();
	    sleep(QUIT_SLEEP_MSEC);
	    System.exit(0);
    }
	
	private boolean setupIgnoreUpdates(boolean isClear) {
		// Lock so we don't get unexpected updates
		boolean isLocked = myControl.setIsLocked(true, isClear);
		
		// Sleep to allow all boxes to get the lock message
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		return isLocked;
	}
	
	private void clearIgnoreUpdates(boolean isLocked, boolean isClear) {
		// If necessary, unlock
		if (!isLocked) {
			myControl.setIsLocked(false, isClear);
		} else if (isClear) {
			myQuizHelper.sendClear();
		}
	}

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e) {
      super.processWindowEvent(e);
      if (e.getID() == WindowEvent.WINDOW_CLOSING) { myControl.scheduleQuit(); }
    }
	
	private void showComponents(List<JComponent> list, boolean state) {
		for (JComponent comp : list) {
			comp.setEnabled(state);
		}	
	}
    
    private void setAllConnectionStatus(int status) {
    	for (QuizBoxAddress addr : QuizTeam.keySet()) {
    		setConnectionStatus(addr, status);
		}
    }

    private void updateAllConnectionStatus(boolean isReset) {
		for (QuizBoxAddress addr : QuizTeam.keySet()) {
			updateConnectionStatus(addr, isReset);
		}
    }
	
	private void updateConnectionStatus(QuizBoxAddress addr, boolean isReset) {
		int i = myQuizHelper.getConnectStatusValue(addr);
		if (i == SELECTED_STATUS && isReset) {
			setConnectionStatus(addr, CONNECTED_STATUS);
		} else if (i == NOT_CONNECTED_STATUS) {
			setConnectionStatus(addr, i);
		} else {
			int p = myControl.getPlaceValue(addr);
			if (p == 1) i |= SELECTED_1ST_STATUS;
			else if (p == 2) i |= SELECTED_2ND_STATUS;
			else if (p == 3) i |= SELECTED_3RD_STATUS;
			setConnectionStatus(addr, i);
		}
	}

    private void setConnectionStatus(QuizBoxAddress addr, int status) {		
    	int newstatus = myQuizHelper.setConnectStatusValue(addr, status);
    	
    	QuizTeamControlGuiItem item = myTeamGuiItems.get(addr);
		JLabel statusLabel = item.myStatusLabel;
    	if (statusLabel != null) {
    		statusLabel.setText(myQuizHelper.getConnectStatusString(addr, newstatus));
    		statusLabel.setForeground(myQuizHelper.getLQStatusColor(addr, newstatus));
    	}

    	JLabel teamLabel = item.myTeamLabel;
    	if (teamLabel != null) {
        	teamLabel.setEnabled(myQuizHelper.getConnectStatusFlag(newstatus));
        	teamLabel.setForeground(myQuizHelper.getConnectStatusColor(newstatus));
    	}
     }
    
    private boolean check(Object obj, QuizBoxAddress addr) {
    	boolean b = (obj!=null);
    	if (!b && !myWarnedAddresses.contains(addr)) {
    		myWarnedAddresses.add(addr);
    		log.warn("Could not find address: " + addr.toString());
    	}
    	return b;
    }

	@Override
	public void processLine(String line) {
		QuizMessage msg = new QuizMessage(line);
		if (msg.isButtonMessage() || msg.isStatusMessage()) {
			QuizBoxAddress address = msg.getAddress();
			if (address.isValidAddress()) {
				QuizBoxData data = QuizBoxData.getQuizBoxData(address);
				if (data != null) {
					int btn = msg.getButton();
					int lq1 = msg.getLQ1();
					data.resetConnectionTime();
					data.setLQ(lq1);
					if (!myControl.getIsLocked()) {
						data.setButtonState(btn);
						if (myControl != null) {
							myControl.setCurrentAnswer(address, btn);
							myControl.setCurrentPlace(address);
						}
					}
					updateGUI(address);
					updateControlButtons();
					if (log.isDebugEnabled()) {
						log.debug("Button press, box: " + address.getAddressString() + " = " + btn);
					}
				} else {
					log.warn("Unknown address: " + address);
					for (QuizBoxAddress addr : QuizBoxData.keySet()) {
						System.out.println("Address: " + addr);
					}
				}
			}
		}
	}
	
	private QuizzingConnection openQuizzingConnection() {
		QuizzingConnection qc = null;
		try {
			int bps = myProps.getBaud();
			String port = myProps.getComPort();
			qc = new QuizzingConnection();
			boolean connected = qc.open(bps,port);
			if (connected) qc.addMessageListener(this);
		} catch (Exception e) {
			log.error("Unable to open quizzing connection", e);
		}
		return qc;
	}
  
    public static void sleep(int msec) {
    	if (msec > 0) {
			try {
				Thread.sleep(msec);
			} catch (InterruptedException e) {
			}  	
    	}
    }
    
    private void doActions() {
	    JButton demo = new JButton("Demo");
	    demo.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myQuizHelper.sendDemoClear();
				}
	    	}
		);
	    
	    JButton test = new JButton("Test");
	    test.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myQuizHelper.sendTestClear();
				}
	    	}
		);
	    
	    JButton ready = new JButton("Ready");
	    ready.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myQuizHelper.sendReady();
				}
	    	}
		);
	    
	    JButton lock = new JButton("Lock");
	    lock.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myQuizHelper.sendLock();
				}
	    	}
		);
	    
	    JButton clear = new JButton("Clear");
	    clear.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myQuizHelper.sendClear();
				}
	    	}
		);
	    
	    JButton power = new JButton("Power Off");
	    power.addActionListener(
	    	new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myQuizHelper.sendPowerDown();
				}
	    	}
		);
	    
	    JPanel panel = new JPanel(new BorderLayout());
	    JPanel btnPanel = new JPanel(new GridLayout(4,2));
	    panel.add(new JLabel("Send command to all quiz boxes:"), BorderLayout.NORTH);
	    panel.add(btnPanel, BorderLayout.CENTER);
	    btnPanel.add(demo);
	    btnPanel.add(test);
	    btnPanel.add(ready);
	    btnPanel.add(lock);
	    btnPanel.add(clear);
	    btnPanel.add(power);
	    
	    JOptionPane.showMessageDialog(this,
	    	    panel,
	    	    "Action Command",
	    	    JOptionPane.PLAIN_MESSAGE);
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.setLevel(Level.INFO);
		Logger.getLogger(QuizzingControlManager.class).setLevel(Level.INFO);
		Logger.getLogger(QuizzingControlGUI.class).setLevel(Level.INFO);

		//PropertyConfigurator.configure("log4j.properties");
//		Logger.getLogger(RxTxSerialComm.class).setLevel(Level.ERROR);
//		
//		Logger.getLogger(PacketParser.class).setLevel(Level.WARN);
//		Logger.getLogger(Checksum.class).setLevel(Level.INFO);
//		Logger.getLogger(RxResponseIoSample.class).setLevel(Level.INFO);
//		Logger.getLogger(InputStreamThread.class).setLevel(Level.ERROR);
//		Logger.getLogger(PacketParser.class).setLevel(Level.FATAL);

		
//		Logger.getLogger(XBeeHelper.class).setLevel(Level.WARN);
//		Logger.getLogger(XBee.class).setLevel(Level.WARN);
//		Logger.getLogger(XBeePacket.class).setLevel(Level.INFO);
//		Logger.getLogger(CryptoController.class).setLevel(Level.INFO);
//		Logger.getLogger(XBeeATCommand.class).setLevel(Level.INFO);
//		Logger.getLogger(MultipleChoiceXBee.class).setLevel(Level.WARN);
		
		String fname = null;
		if (args.length >= 1) {
			fname = args[0];
			System.out.println("Using properties file: '" + fname + "'");
		}

		QuizzingControlGUI obj = new QuizzingControlGUI(fname);
	    obj.validate();
	    obj.setVisible(true);	
	    sleep(300);
	    
		obj.doProcessing();
	}
}
