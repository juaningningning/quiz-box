/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.gui;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import quizbox.data.QuizBoxAddress;

public class QuizTeamControlGuiItem {
	QuizBoxAddress myAddress;
	
	JLabel myStatusLabel;
	JLabel myTeamLabel;
	JTextField myAnswerTF;
	JTextField myPlaceTF;
	JRadioButton myCorrectRB;
	JRadioButton myIncorrectRB;
	JLabel myQuizoutCB;
	JLabel myPointsLabel;
	ButtonGroup myResultGroup;
	
	public QuizTeamControlGuiItem(JLabel status, JLabel team, JTextField answer, 
			JTextField place, JRadioButton correct, JRadioButton incorrect, 
			JLabel quizout, JLabel points, ButtonGroup  buttons) {
		myStatusLabel = status;
		myTeamLabel = team;
		myAnswerTF = answer;
		myPlaceTF = place;
		myCorrectRB = correct;
		myIncorrectRB = incorrect;
		myQuizoutCB = quizout;
		myPointsLabel = points;
		myResultGroup = buttons;
	}
}
