/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.gui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import quizbox.QuizzingHelper;
import quizbox.QuizzingProperties;
import quizbox.data.QuizBoxAddress;
import quizbox.data.QuizTeam;
import quizbox.data.QuizConstants;

public class QuizzingDisplayGUI extends JFrame implements QuizConstants {
	private static final long serialVersionUID = 6147515243074279111L;
	
    private Map<QuizBoxAddress, QuizTeamDisplayGuiItem> myTeamGuiItems;
	private JLabel myCorrectAnswerLabel;
	private JLabel myPossiblePointsLabel;
	private JLabel myTitleLabel;
	private JLabel myQuestionTypeLabel;
	private JLabel myQuestionNumberLabel;
	private JLabel myAnnouncementLabel;
	private JLabel myCountLabel;
	private QuizzingProperties myProps;
	private int myFontSize;
	

	public QuizzingDisplayGUI(QuizzingProperties props) {
		myProps = props;
		myProps.loadTeams();
		SortedSet<QuizTeam> teams = QuizTeam.getSortedTeams();
		myFontSize = (props != null) ? props.getFontsize() : 12;
		myTeamGuiItems = new HashMap<QuizBoxAddress, QuizTeamDisplayGuiItem>();
		myCorrectAnswerLabel = new JLabel();
		myPossiblePointsLabel = new JLabel();
		myTitleLabel = new JLabel();
		myQuestionTypeLabel = new JLabel();
		myQuestionNumberLabel = new JLabel();
		myAnnouncementLabel = new JLabel();
		myCountLabel = new JLabel();
		
		initGUI(teams);
	}
	
	public void setAnnouncement(String s) {
		myAnnouncementLabel.setText(s);
	}
	public void setCorrectAnswer(String s) {
		myCorrectAnswerLabel.setText(s);
	}

	public void setPossiblePoints(String s) {
		myPossiblePointsLabel.setText(s);
	}
	
	public void conditionallySetQuestionNumber(int n) {
		String txt = myQuestionNumberLabel.getText();
		if (n <= 0 || txt == null || txt.trim().length() == 0) {
			myQuestionNumberLabel.setText("");
		} else {
			myQuestionNumberLabel.setText("" + n);
		}
	}
	
	public void setQuestionNumber(int n) {
		if (n <= 0) {
			myQuestionNumberLabel.setText("");
		} else {
			myQuestionNumberLabel.setText("" + n);
		}
	}
	
	public void setQuestionType(String s) {
		myQuestionTypeLabel.setText(s);
	}
	
	public void setAnswer(QuizBoxAddress addr, String answer) {
		QuizTeamDisplayGuiItem item = myTeamGuiItems.get(addr);
		item.myAnswerLabel.setText(answer);
	}
	
	public void setPoints(QuizBoxAddress addr, double points) {
		String s = QuizzingHelper.formatPoints(points);
		QuizTeamDisplayGuiItem item = myTeamGuiItems.get(addr);
		item.myPointsLabel.setText(s);
	}
	
	public void setBonus(QuizBoxAddress addr, double points, double bonus) {
		QuizTeamDisplayGuiItem item = myTeamGuiItems.get(addr);
		if (bonus > 0) {
			String s1 = QuizzingHelper.formatPoints(points);
			String s2 = QuizzingHelper.formatPoints(bonus);
			item.myPointsLabel.setText(s1 + "+" + s2);
		} else {
			item.myBonusLabel.setText("");			
		}
	}
	
	public void setCount(String s) {
		myCountLabel.setText(s);
	}
	
	public void clearPoints(QuizBoxAddress addr) {
		QuizTeamDisplayGuiItem item = myTeamGuiItems.get(addr);
		item.myPointsLabel.setText("");
		item.myBonusLabel.setText("");
	}
	
	private void initGUI(SortedSet<QuizTeam> teams) {
	    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	    
	    this.setTitle("Quizzing Points");
	    this.setSize(new Dimension(DISP_WIDTH_WINDOW_SZ, DISP_HEIGHT_WINDOW_SZ));		// Width, Height
	    
	    boolean isDisplayTitle = (myProps != null) ? myProps.getDisplayTitle() : false;
	    boolean isDisplayQuestionType = (myProps != null) ? myProps.getDisplayQuestionType() : false;
	    boolean isDisplayQuestionNumber = (myProps != null) ? myProps.getDisplayQuestionNumber() : false;
	    boolean isExtra = (isDisplayTitle || isDisplayQuestionType || isDisplayQuestionNumber);
	    int sz = teams.size()+3;
	    if (isExtra) sz++;
    	JPanel namePanel = new JPanel(new GridLayout(sz, 1));
    	JPanel answerPanel = new JPanel(new GridLayout(sz, 1));
    	JPanel scorePanel = new JPanel(new GridLayout(sz, 1));
    	//JPanel bonusPanel = new JPanel(new GridLayout(sz, 1));
    	
    	JPanel contentPanel = (JPanel) this.getContentPane();
    	contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
    	contentPanel.add(Box.createHorizontalStrut(10));
    	contentPanel.add(namePanel);
    	contentPanel.add(Box.createHorizontalStrut(10));
    	contentPanel.add(Box.createHorizontalGlue());
    	contentPanel.add(answerPanel);
    	contentPanel.add(Box.createHorizontalStrut(10));
    	contentPanel.add(scorePanel);
    	contentPanel.add(Box.createHorizontalStrut(10));
    	//contentPanel.add(bonusPanel);
    	contentPanel.add(Box.createHorizontalStrut(10));
    	answerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    	scorePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    	//bonusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    	
    	if (isExtra) {
	    	if (isDisplayTitle) {
	        	if (myProps != null) myTitleLabel.setText(myProps.getQuizTitle());
	    		namePanel.add(myTitleLabel);
	    	} else {
	    		namePanel.add(new JLabel());
	    	}
		    if (isDisplayQuestionType) {
		    	answerPanel.add(myQuestionTypeLabel);
		    } else {
		    	answerPanel.add(new JLabel());
		    }
	    	if (isDisplayQuestionNumber) {
	    		scorePanel.add(myQuestionNumberLabel);
	    	} else {
	    		scorePanel.add(new JLabel());
	    	}
	    	//bonusPanel.add(new JLabel());
    	}
    	
		Font titleFont = new Font("Arial Bold", Font.BOLD | Font.ITALIC, myFontSize);
		Font teamFont = new Font("Arial Bold", Font.PLAIN, myFontSize);
		myCorrectAnswerLabel.setFont(titleFont);
		myCorrectAnswerLabel.setForeground(Color.red);
		myPossiblePointsLabel.setFont(titleFont);
		myPossiblePointsLabel.setForeground(Color.red);
		myTitleLabel.setFont(titleFont);
		myTitleLabel.setForeground(Color.gray);
    	myQuestionTypeLabel.setFont(titleFont);
    	myQuestionTypeLabel.setForeground(Color.gray);
    	myQuestionNumberLabel.setFont(titleFont);
    	myQuestionNumberLabel.setForeground(Color.gray);
		
    	JLabel nameTitle = new JLabel("Team Name");
    	JLabel answerTitle = new JLabel("Answer");
    	JLabel scoreTitle = new JLabel("Points");
    	JLabel bonusTitle = new JLabel("Bonus");
    	JLabel correctTitle = new JLabel("The correct answer is:");
    	nameTitle.setFont(titleFont);
    	answerTitle.setFont(titleFont);
    	scoreTitle.setFont(titleFont);
    	bonusTitle.setFont(titleFont);
    	correctTitle.setFont(titleFont);
    	nameTitle.setForeground(Color.gray);
    	answerTitle.setForeground(Color.gray);
    	scoreTitle.setForeground(Color.gray);
    	bonusTitle.setForeground(Color.gray);
    	correctTitle.setForeground(Color.red);
    	
    	namePanel.add(nameTitle);
    	answerPanel.add(answerTitle);
    	scorePanel.add(scoreTitle);
    	//bonusPanel.add(bonusTitle);
    	
    	namePanel.add(correctTitle);
    	answerPanel.add(myCorrectAnswerLabel);
    	scorePanel.add(myPossiblePointsLabel);
    	//bonusPanel.add(new JLabel());
    	
    	int i = 0;
	    for (QuizTeam qt : teams) {
	    	QuizBoxAddress addr = qt.getAddr();
	    	String name = qt.getTeamName();
	    	
	    	JLabel nameLabel = new JLabel(name + ": ");
	    	JLabel answerLabel = new JLabel("");
	    	JLabel scoreLabel = new JLabel("");
	    	JLabel bonusLabel = new JLabel("");
	    	if (i++%2 == 0) {
	    		nameLabel.setForeground(Color.black);
	    		answerLabel.setForeground(Color.black);
	    		scoreLabel.setForeground(Color.black);
	    		bonusLabel.setForeground(Color.black);
	    	} else {
	    		nameLabel.setForeground(Color.blue);
	    		answerLabel.setForeground(Color.blue);	    		
	    		scoreLabel.setForeground(Color.blue);	    		
	    		bonusLabel.setForeground(Color.blue);	    		
	    	}
	    	nameLabel.setFont(teamFont);
	    	answerLabel.setFont(teamFont);
	    	scoreLabel.setFont(teamFont);
	    	bonusLabel.setFont(teamFont);	    	
			QuizTeamDisplayGuiItem item = new QuizTeamDisplayGuiItem(nameLabel,
					answerLabel, scoreLabel, bonusLabel);
			myTeamGuiItems.put(addr, item);

	    	namePanel.add(nameLabel);
	    	answerPanel.add(answerLabel);
	    	scorePanel.add(scoreLabel);
	    	//bonusPanel.add(bonusLabel);
	    }
	    namePanel.add(myAnnouncementLabel);
	    answerPanel.add(myCountLabel);
	    scorePanel.add(new JLabel());
	    //bonusPanel.add(new JLabel());
	    myAnnouncementLabel.setFont(titleFont);
    	myAnnouncementLabel.setForeground(Color.red);
    	myCountLabel.setFont(titleFont);
    	myCountLabel.setForeground(Color.gray.brighter());
	    
    	setQuestionType("");
	    setQuestionNumber(0);
	    setCorrectAnswer("");
	    setPossiblePoints("");
	}
}
