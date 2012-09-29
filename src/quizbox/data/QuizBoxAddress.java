/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Author: Ted Meyers, 2012
 */

package quizbox.data;

import quizbox.util.Logger;

/**
 * Holds the address of a quiz box
 * 
 * @author Ted Meyers
 * @since August 2012
 */
public class QuizBoxAddress {
	private final static Logger log = Logger.getLogger(QuizBoxAddress.class);
	
	String myName;
	String myBinaryAddress;
	
	public QuizBoxAddress(String address) {
		parse(address);
	}
	
	public void setAddress(String line) {
		parse(line);
	}
	
	public void setBinaryAddress(String address) {
		myBinaryAddress = address;
	}
	
	public boolean isValidAddress() {
		return (myBinaryAddress.length() == 3);
	}
	
	public String getName() {
		return myName;
	}
	
	public String getAddressString() {
		if (isValidAddress()) {
			return addressToString(myBinaryAddress);
		}
		return "";
	}
	
	public String getBinaryAddress() {
		return myBinaryAddress;
	}

	@Override
	public String toString() {
		return myName + "_" + getAddressString();
	}
	
	public String toShortString() {
		return addressToShortString(myBinaryAddress);
	}
	
	
	public static String addressToString(String address) {
		return "<" + addressToShortString(address) + ">";
	}
	
	public static String addressToShortString(String address) {
		// Do NOT trim the address!!
		if (address==null || address.length()!=3) {
			return "000000";
		}
		String s = 
				toHex(address.charAt(0)) + 
				toHex(address.charAt(1)) + 
				toHex(address.charAt(2));
		return s.toUpperCase();		
	}
	
	private static String toHex(char c) {
		String s = Integer.toHexString(c);
		int i = s.length();
		if (i < 1) s = "00";
		else if (i < 2) s = "0" + s;
		return s;
	}
	
	private void parse(String s) {
		myName = "";
		myBinaryAddress = "";
		if (s==null || s.trim().length()==0) return;
		
		s = s.trim();
		if (s.endsWith("_")) {
			myName = s.substring(0, s.length()-1);
		} else if (s.startsWith("_")) {
			parseAddress(s.substring(1));
		} else {
			String[] parts = s.split("_");
			if (parts.length >= 2) {
				myName = parts[0];
				parseAddress(parts[1]);
			}
			if (parts.length == 1) {
				parseAddress(parts[0]);
			}
		}
	}
	
	private void parseAddress(String s) {
		if (s.startsWith("0x") || s.startsWith("0X")) {
			s = s.substring(2);
		}
		if (s.length()<6) {
			log.warn("Bad Address: '" + s + "'");
			myBinaryAddress = "";
			return;
		}
		int a1 = Integer.parseInt(s.substring(0, 2), 16);
		int a2 = Integer.parseInt(s.substring(2, 4), 16);
		int a3 = Integer.parseInt(s.substring(4, 6), 16);
		myBinaryAddress = "" + (char)a1 + (char)a2 + (char)a3;
	}
	
	@Override
	public int hashCode() {
		return myBinaryAddress.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o!=null && o instanceof QuizBoxAddress) {
			return myBinaryAddress.equals(((QuizBoxAddress)o).myBinaryAddress);
		}
		return false;
	}
}
