/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.data;

/**
 * Constants for use in quizzing
 * 
 * @author Ted Meyers
 * @since August 2012
 */
public interface QuizConstants {
	// In QuizzingProperties
	public static final boolean DEFAULT_USE_16 = false;
	public static final String DEFAULT_HIGH_64 = "0013a200";
	
	// In QuizzingHelper
	public static final int CONNECTION_TIMEOUT_MSEC = 5000;		// Default for property
	public static final int LQ_LOW_SIGNAL_LEVEL = 150;
	public static final int LQ_MID_SIGNAL_LEVEL = 200;
	public static final String CONNECT_STATUS_STR = "C";
	public static final String NOT_CONNECT_STATUS_STR = "N";
	public static final String SELECT_CONNECT_STATUS_STR = "S";
	
	// In QuizzingGUI
	public static final int PROCESSING_MAX_SLEEP_MSEC = 20;		// 20
	public static final int PROCESSING_DEF_SLEEP_MSEC = 1;		// 2, 1
	public static final int UPDATE_STATUS_RATE = 100;
	public static final int UPDATE_DISPLAY_GUI_RATE = 100;
	public static final int UPDATE_GUI_RATE = 200;
	public static final int MAX_UPDATE_COUNT = 20;
	public static final int QUIT_SLEEP_MSEC = 200;
	public static final int GRID_ROW_SZ = 16;
	public static final int GRID_COL_SZ = 3;
	public static final int WIDTH_WINDOW_SZ = 720;
	public static final int HEIGHT_WINDOW_SZ = 500;
	public static final String DEFAULT_STATUS_LABEL = "[" + "--" + ":"+ NOT_CONNECT_STATUS_STR + "]";
	
	public static final String MC_DISPLAY_LABEL = "M/C";
	public static final String SP_DISPLAY_LABEL = "SPEED";
	
	public static final String SEP = ", ";
	
	public static final int DISP_WIDTH_WINDOW_SZ = 720;
	public static final int DISP_HEIGHT_WINDOW_SZ = 500;
	
	public static final int LO_STATE = 0;
	public static final int HI_STATE = 1;			

	public static final int NOT_CONNECTED_STATUS = 0x0;
	public static final int CONNECTED_STATUS = 0x01;
	public static final int SELECTED_STATUS = 0x02;
	public static final int SELECTED_1ST_STATUS = SELECTED_STATUS | 0x04;
	public static final int SELECTED_2ND_STATUS = SELECTED_STATUS | 0x08;
	public static final int SELECTED_3RD_STATUS = SELECTED_STATUS | 0x10;
	
	public static final String X_STR = "";
	public static final String A_STR = "A";
	public static final String B_STR = "B";
	public static final String C_STR = "C";	
	public static final String N_STR = "N";	
	public static final String NO_STR = X_STR;
	
	public static final int X_PIN_ON = 0x0;
	public static final int A_PIN_ON = 0x08;
	public static final int B_PIN_ON = 0x20;
	public static final int C_PIN_ON = 0x10;
	public static final int NO_PIN_ON = X_PIN_ON;
	
	public static final int X_BTN = 0;		// No button press
	public static final int A_BTN = 1;
	public static final int B_BTN = 2;
	public static final int C_BTN = 4;
	public static final int NO_BTN = X_BTN;
	
	public static final int A_PIN = 3;
	public static final int B_PIN = 5;
	public static final int C_PIN = 4;
	
	public static final int NONE_QUESTION_TYPE = 0;
	public static final int MC_QUESTION_TYPE = 1;
	public static final int SPEED_QUESTION_TYPE = 2;
	
	public static final int NO_SPEED_ANSWER = 0;
	public static final int CORRECT_SPEED_ANSWER = 1;
	public static final int INCORRECT_SPEED_ANSWER = 2;
	
	public static final double FULL_POINTS = 1.0;
	public static final double ZERO_POINTS = 0.0;
}
