/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.hbt;

import net.sekien.elesmyr.Element;
import org.newdawn.slick.util.*;

/**
 * Created with IntelliJ IDEA. User: matt Date: 30/03/13 Time: 2:12 PM To change this template use File | Settings |
 * File Templates.
 */
public class HBTFlag extends HBTTag {
private static final String[] VALUES = {
		                                       "TRUE",
		                                       "FALSE",
		                                       "NEUTRAL",
		                                       "EARTH",
		                                       "WATER",
		                                       "FIRE",
		                                       "AIR",
		                                       "VOID"
};
public static final HBTFlag TRUE = new HBTFlag("", (byte) 0);
public static final HBTFlag FALSE = new HBTFlag("", (byte) 1);
private byte data;

public HBTFlag(String name, byte data) {
	super(name);
	this.data = data;
}

public HBTFlag(String name, String data) {
	super(name);
	this.data = stringToByte(data);
}

private byte stringToByte(String data) {
	for (byte i = 0; i < VALUES.length; i++) {
		if (VALUES[i].equals(data)) {
			return i;
		}
	}
	Log.error("Unrecognised HBTFlag value '"+data+"'");
	return 0;
}

public static boolean isValid(String data) {
	for (byte i = 0; i < VALUES.length; i++) {
		if (VALUES[i].equals(data)) {
			return true;
		}
	}
	return false;
}

@Override
public String toString() {
	return "flag "+getName()+" = "+VALUES[data];
}

public byte getData() {
	return data;
}

public boolean isTrue() {
	return VALUES[data].equals("TRUE");
}

public Element asElement() {
	if (data==2) return Element.NEUTRAL;
	else if (data==3) return Element.EARTH;
	else if (data==4) return Element.WATER;
	else if (data==5) return Element.FIRE;
	else if (data==6) return Element.AIR;
	else if (data==7) return Element.VOID;
	else {
		Log.error("HBTFlag.asElement called on "+VALUES[data]);
		return Element.NEUTRAL;
	}
}

@Override
public HBTTag deepClone() {
	return new HBTFlag(getName(), data);
}
}
