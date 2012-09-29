/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.gui;

import javax.swing.JLabel;

import quizbox.data.QuizBoxAddress;

public class QuizTeamDisplayGuiItem {
	QuizBoxAddress myAddress;
	
	JLabel myNameLabel;
	JLabel myAnswerLabel;
	JLabel myPointsLabel;
	JLabel myBonusLabel;
	
	public QuizTeamDisplayGuiItem(JLabel name, JLabel answer, JLabel points, JLabel bonus) {
		myNameLabel = name;
		myAnswerLabel = answer;
		myPointsLabel = points;
		myBonusLabel = bonus;
	}
}
