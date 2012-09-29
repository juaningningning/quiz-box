/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.util;

public enum Level {
	ALWAYS(0, "ALWAYS"),
	TRACE(1, "TRACE"),
	DEBUG(2, "DEBUG"),
	INFO(3, "INFO"),
	WARN(4, "WARN"),
	ERROR(5, "ERROR"),
	FATAL(6, "FATAL"),
	NEVER(7, "NEVER");
	
	private int level;
	private String name;
	
	private Level(int i, String n) {
		level = i;
		name = n;
	}
	
	public String getName() {
		return name;
	}
	public int getValue() {
		return level;
	}
}
