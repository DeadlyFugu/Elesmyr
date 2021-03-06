/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr.system;

import net.sekien.elesmyr.util.FileHandler;
import org.newdawn.slick.*;
import org.newdawn.slick.state.*;

public class IntroState extends BasicGameState {

int stateID = -1;

float timer = 0;

Image intro1;
Image intro2;
Image intro3;
Image menubg;
Image bg2;
Image bg3;

IntroState(int stateID) {
	this.stateID = stateID;
}

@Override
public int getID() {
	return stateID;
}

public void init(GameContainer gc, StateBasedGame sbg)
		throws SlickException {
	intro1 = FileHandler.getImage("menu.intro1"); //Sekien
	intro2 = FileHandler.getImage("menu.intro2"); //Alachie
	intro3 = FileHandler.getImage("menu.intro3"); //Legend of the Elements
	menubg = FileHandler.getImage("menu.bg"); //Menu image (BG)
	bg2 = FileHandler.getImage("menu.bg2"); //Menu image (Sekien)
	bg3 = FileHandler.getImage("menu.bg3"); //Menu image (LotE/Alachie)
	gc.setVSync(true); //True in release
	gc.setVerbose(false); //False in release
	gc.setClearEachFrame(false); //Set to false in release!
	gc.setShowFPS(false); //Set to false in release
	gc.setTargetFrameRate(60);
	gc.getInput().initControllers();

	if (Globals.containsKey("resdm")) {
		int dm = Integer.parseInt(Globals.get("resdm", "0"));
		((AppGameContainer) gc).setDisplayMode(MainMenuState.disx[dm], MainMenuState.disy[dm], dm==3 || dm==4);
		((AppGameContainer) gc).setMouseGrabbed(dm==3 || dm==4);
		Main.INTERNAL_ASPECT = ((float) MainMenuState.disx[dm]/(float) MainMenuState.disy[dm]);
		Main.INTERNAL_RESX = (int) (Main.INTERNAL_RESY*Main.INTERNAL_ASPECT); //Internal resolution x
	}
}

public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
		throws SlickException {
	float vw = gc.getWidth();
	float vh = gc.getHeight();
	float ox, oy, w, h;
	//		if (vw/vh < 1.6) {
	//			w = vw;
	//			h = vw*0.625f;
	//			ox = 0;
	//			oy = (vh-h)/2;
	//		} else {
	w = vh*1.778125f;
	h = vh;
	ox = (vw-w)/2;
	oy = 0;
	//}
	if (timer < 600) {
		intro1.setAlpha((timer)/600f);
		intro1.draw(ox, oy, w, h);
	} else if (timer < 1200) {
		intro2.setAlpha((timer-600)/600f);
		intro2.draw(ox, oy, w, h);
	} else if (timer < 2000) {
		intro3.setAlpha((timer-1200)/500f);
		intro3.draw(ox, oy, w, h);
	} else if (timer < 2250) {
		w = vh*(16/9f);
		h = vh;
		ox = (vw-w)/2;
		oy = 0;
		menubg.setAlpha((timer-2000)/500f);
		menubg.draw(ox, oy, w, h);
	} else if (timer < 2400) {
		w = vh*(16/9f);
		h = vh;
		ox = (vw-w)/2;
		oy = 0;
		menubg.setAlpha((timer-2000)/500f);
		menubg.draw(ox, oy, w, h);
		bg2.setAlpha((timer-2250)/200f);
		bg3.setAlpha((timer-2250)/200f);
		bg2.draw(0, 0, vh*(4/3f), h);
		bg3.draw(vw-vh*(4/3f), 0, vh*(4/3f), h);
	} else {
		gc.getInput().clearKeyPressedRecord();
		sbg.enterState(Main.NUISTATE);
	}
}

public void update(GameContainer gc, StateBasedGame sbg, int delta)
		throws SlickException {
	timer += delta/4f;
}

}
