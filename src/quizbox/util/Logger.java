/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.util;

import java.util.HashMap;
import java.util.Map;

public class Logger {
	private static Map<String, Logger> loggerMap = new HashMap<String, Logger>();
	
	private static Level defaultLogLevel = Level.INFO;
	
	private String myClass;
	private Level myLogLevel;
	
	public Logger(String c) {
		myClass = c;
		myLogLevel = defaultLogLevel;
	}
	
	public static Logger getLogger(Class<?> c) {
		Logger logger = null;
		String k = c.getName();
		if (loggerMap.containsKey(k)) {
			logger = loggerMap.get(k);
		} else {
			logger = new Logger(c.getName());
			loggerMap.put(k, logger);
		}
		return logger;
	}
	
	public static void setAllLevels(Level logLevel) {
		for (Logger logger : loggerMap.values()) {
			logger.setLevel(logLevel);
		}
	}
	
	public static void setDefaultLevel(Level logLevel) {
		defaultLogLevel = logLevel;
	}
	
	public static Level getDefaultLevel() {
		return defaultLogLevel;
	}
	
	public void setLevel(Level logLevel) {
		myLogLevel = logLevel;
	}
	
	public Level getLevel() {
		return myLogLevel;
	}
	
	public boolean isTraceEnabled() {
		return isLog(Level.TRACE);
	}
	
	public boolean isDebugEnabled() {
		return isLog(Level.DEBUG);
	}
	
	public boolean isInfoEnabled() {
		return isLog(Level.INFO);
	}
	
	public boolean isWarnEnabled() {
		return isLog(Level.WARN);
	}
	
	public boolean isErrorEnabled() {
		return isLog(Level.ERROR);
	}
	
	public boolean isFatalEnabled() {
		return isLog(Level.FATAL);
	}
	
	public boolean isLog(Level test) {
		return (myLogLevel.getValue() <= test.getValue());
	}
	
	public void trace(String s) {
		if (isTraceEnabled()) printLogLine(s);
	}
	public void trace(String s, Exception e) {
		if (isTraceEnabled()) printLogLine(s, e);
	}
	public void trace(String s, Throwable t) {
		if (isTraceEnabled()) printLogLine(s, t);
	}
	
	public void debug(String s) {
		if (isDebugEnabled()) printLogLine(s);
	}
	public void debug(String s, Exception e) {
		if (isDebugEnabled()) printLogLine(s, e);
	}
	public void debug(String s, Throwable t) {
		if (isDebugEnabled()) printLogLine(s, t);
	}
	
	public void info(String s) {
		if (isInfoEnabled()) printLogLine(s);
	}
	public void info(String s, Exception e) {
		if (isInfoEnabled()) printLogLine(s, e);
	}
	public void info(String s, Throwable t) {
		if (isInfoEnabled()) printLogLine(s, t);
	}
	
	public void warn(String s) {
		if (isWarnEnabled()) printLogLine(s);
	}
	public void warn(String s, Exception e) {
		if (isWarnEnabled()) printLogLine(s, e);
	}
	public void warn(String s, Throwable t) {
		if (isWarnEnabled()) printLogLine(s, t);
	}
	
	public void error(String s) {
		if (isErrorEnabled()) printLogLine(s);
	}
	public void error(String s, Exception e) {
		if (isErrorEnabled()) printLogLine(s, e);
	}
	public void error(String s, Throwable t) {
		if (isErrorEnabled()) printLogLine(s, t);
	}
	
	public void fatal(String s) {
		if (isFatalEnabled()) printLogLine(s);
	}
	public void fatal(String s, Exception e) {
		if (isFatalEnabled()) printLogLine(s, e);
	}
	public void fatal(String s, Throwable t) {
		if (isFatalEnabled()) printLogLine(s, t);
	}
	
	public void printLogLine(String s) {
		String typ = myLogLevel.getName();
		System.out.println(typ + " <" + myClass + ">:  "  + s);
	}
	public void printLogLine(String s, Exception e) {
		String typ = myLogLevel.getName();
		System.out.println(typ + " <" + myClass + ">:  "  + s + " - " + e);
		e.printStackTrace();
	}
	public void printLogLine(String s, Throwable t) {
		String typ = myLogLevel.getName();
		System.out.println(typ + " <" + myClass + ">:  "  + s + " - " + t);
	}
}
