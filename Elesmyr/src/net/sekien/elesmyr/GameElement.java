/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr;

import net.sekien.elesmyr.msgsys.MessageEndPoint;
import net.sekien.elesmyr.msgsys.MessageReceiver;
import net.sekien.elesmyr.player.Camera;
import net.sekien.elesmyr.system.GameClient;
import net.sekien.elesmyr.system.GameServer;
import net.sekien.elesmyr.world.Region;
import org.newdawn.slick.*;

public interface GameElement extends MessageReceiver {

/**
 * Initialization. Use for loading resources or setting variables
 *
 * @throws SlickException
 */
public void init(GameContainer gc, MessageEndPoint receiver) throws SlickException;

/**
 * Load this object from a save file
 *
 * @param save
 * 		The Save object to load from
 */
public void load(Save save);

/**
 * Client-side rendering.
 *
 * @throws SlickException
 */
public void render(GameContainer gc, Graphics g, Camera cam, GameClient receiver) throws SlickException;

/** Server-side updating */
public void update(Region region, GameServer receiver);

/** Client-side updating */
public void clientUpdate(GameContainer gc, GameClient receiver);

/** Save data Server-side */
public void save(Save save);
}
