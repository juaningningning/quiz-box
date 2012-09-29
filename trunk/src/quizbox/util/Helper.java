/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.util;


public class Helper {
	
	public static String formatLine(String line) {
		if (line == null) return "<null>";
		
		String t = line.trim();
		String s = "";
		for (int i=0; i<t.length(); i++) {
			byte c = (byte)t.charAt(i);
			if (c<32 || c>126) {
				int n = (c<0)?c+256:c;
				s += "<" + Integer.toHexString(n).toUpperCase() + ">";
			} else {
				s += t.charAt(i);
			}
		}
		return s;
	}
}
