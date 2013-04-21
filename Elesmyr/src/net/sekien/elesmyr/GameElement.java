package net.sekien.elesmyr;

import net.sekien.elesmyr.msgsys.MessageEndPoint;
import net.sekien.elesmyr.msgsys.MessageReceiver;
import net.sekien.elesmyr.player.Camera;
import net.sekien.elesmyr.system.GameClient;
import net.sekien.elesmyr.system.GameServer;
import net.sekien.elesmyr.world.Region;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public interface GameElement extends MessageReceiver {

/**
 * Initialization. Use for loading resources or setting variables
 *
 * @throws SlickException
 */
public void init(GameContainer gc, StateBasedGame sbg, MessageEndPoint receiver) throws SlickException;

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
public void render(GameContainer gc, StateBasedGame sbg, Graphics g, Camera cam, GameClient receiver) throws SlickException;

/** Server-side updating */
public void update(Region region, GameServer receiver);

/** Client-side updating */
public void clientUpdate(GameContainer gc, StateBasedGame sbg, GameClient receiver);

/** Save data Server-side */
public void save(Save save);
}