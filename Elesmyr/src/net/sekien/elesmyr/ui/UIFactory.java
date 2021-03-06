/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr.ui;

import groovy.lang.GroovyObject;
import net.sekien.elesmyr.ScriptRunner;

public class UIFactory {
public static UserInterface getUI(String str) {
	String[] parts = str.split(",", 2);
	if (parts.length==2)
		try {
			UserInterface ui = (UserInterface) Class.forName("net.sekien.elesmyr.ui."+parts[0]).newInstance();
			ui.ctor(parts[1]);
			return ui;
		} catch (ClassNotFoundException e) {
			GroovyObject go = ScriptRunner.get(parts[0]);
			if (go!=null) {
				go.invokeMethod("ctor", new Object[]{parts[1]});
				return (UserInterface) go.invokeMethod("toUI", new Object[0]);
			}
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
	//return new EntityEnemy(parts[1],Integer.parseInt(parts[2]),Integer.parseInt(parts[3]));
	return null;
}
}
