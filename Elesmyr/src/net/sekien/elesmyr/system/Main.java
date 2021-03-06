/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr.system;

import com.esotericsoftware.minlog.Log;
import net.sekien.elesmyr.Profiler;
import net.sekien.elesmyr.ScriptRunner;
import net.sekien.elesmyr.util.FileHandler;
import net.sekien.elesmyr.util.HashmapLoader;
import net.sekien.hbt.TagNotFoundException;
import net.sekien.pepper.StateManager;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.*;
import org.newdawn.slick.opengl.renderer.*;
import org.newdawn.slick.util.*;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Elesmyr main class
 *
 * @author DeadlyFugu
 */
public class Main extends BasicGame {

	public static final int INTROSTATE = 0;
	public static final int MENUSTATE = 1;
	public static final int GAMEPLAYSTATE = 2;
	public static final int ERRORSTATE = 3;
	public static final int LOGINSTATE = 4;
	public static final int NUISTATE = 5;

	public static float INTERNAL_ASPECT = (4/3f);
	public static int INTERNAL_RESY = 480; //Internal resolution y
	public static int INTERNAL_RESX = (int) (INTERNAL_RESY*INTERNAL_ASPECT); //Internal resolution x

	public static final String verNum = "0.3.0";
	public static final String verRelease = "PRE-ALPHA";
	public static final String version = "$version.prealpha| "+verNum; //0.0.1 = DEC 16

	private static GameContainer gc;

	private int prev_res = 0;

	public Main() {
		super("Tales of a Star Child: Elesmyr "+verRelease+" "+verNum);

	/*this.addState(new IntroState(INTROSTATE));
	this.addState(new MainMenuState(MENUSTATE));
	this.addState(new GameClient(GAMEPLAYSTATE));
	this.addState(new ErrorState(ERRORSTATE));
	this.addState(new LoginState(LOGINSTATE));
	this.addState(new NUIState(NUISTATE));

	if (Globals.get("showIntro", true)) {
		this.enterState(INTROSTATE);
	} else {
		this.enterState(NUISTATE);
	}*/
	}

	public static void main(String[] args) throws SlickException {
		Log.info("Tales of a Star Child: Elesmyr "+verRelease+" "+verNum);

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				Main.handleCrash(e);
				System.exit(1);
			}
		});

		if (!new File("pack").exists()) {
			Log.error("Please run this from the correct directory");
			System.exit(0);
		}

		System.setProperty("org.lwjgl.librarypath", System.getProperty("user.dir")+"/lib/native");

		org.newdawn.slick.util.Log.setLogSystem(new SlickToMinLogSystem());

		if (new File("conf.csv").exists())
			Globals.setMap(HashmapLoader.readHashmap("conf.csv"));
		else
			Globals.setMap(new HashMap<String, String>());

		try {
			//HBTOutputStream os = new HBTOutputStream(new FileOutputStream("save/TestOut2.hbtc"),true);
			FileHandler.readData();
		} catch (TagNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e) {
			e.printStackTrace();
		}

		ScriptRunner.init();

		AppGameContainer app = new CustomAppGameContainer(new Main());

		MainMenuState.disx[3] = app.getScreenWidth();
		MainMenuState.disy[3] = app.getScreenHeight();

		app.setResizable(true);
		app.setUpdateOnlyWhenVisible(true);

		if (Globals.containsKey("resdm")) {
			int dm = Integer.parseInt(Globals.get("resdm", "0"));
			app.setDisplayMode(MainMenuState.disx[dm], MainMenuState.disy[dm], dm == 3 || dm == 4);
			app.setMouseGrabbed(dm == 3 || dm == 4);
			Main.INTERNAL_ASPECT = ((float) MainMenuState.disx[dm]/(float) MainMenuState.disy[dm]);
			Main.INTERNAL_RESX = (int) (Main.INTERNAL_RESY*Main.INTERNAL_ASPECT); //Internal resolution x
		} else {
			app.setDisplayMode(MainMenuState.disx[0], MainMenuState.disy[0], false);
		}
		//app.setIcons(new String[]{"data/icon32.tga", "data/icon16.tga"}); //TODO: Make this work

		app.start();
	}

	public static void handleCrash(Throwable e) {
		Log.info("Elesmyr crashed");
		StringWriter writer = new StringWriter(256);
		e.printStackTrace(new PrintWriter(writer));
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("ELESMYR_CRASH_LOG"));
			bw.write("ELESMYR CRASH LOG\n");
			bw.write(writer.toString());
			bw.write("at "+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())));
			bw.flush();
			bw.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			String msg = simplifyStackTrace(writer);
			JOptionPane.showMessageDialog(null, msg, "Elesmyr just kinda stopped working. :(", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		e.printStackTrace();
	}

	private static String simplifyStackTrace(StringWriter writer) {
		String[] parts = writer.toString().trim().split("\n");
		String out = parts[0];
		boolean ellipsisYet = false;
		boolean ignoreRest = false;
		boolean server = false;
		for (int i = 1; i < parts.length; i++) {
			String s = parts[i];
			if (s.trim().startsWith("at GameServer."))
				server = true;
			if (s.trim().startsWith("at ") && !(ignoreRest|s.trim().startsWith("at java.") || s.trim().startsWith("at sun."))) {
				out = out+"\n    "+s.trim();
				ellipsisYet = false;
				if (s.trim().matches("at net\\.sekien\\.(elesmyr\\.system\\.(GameClient|GameServer)|pepper\\.StateManager).*"))
					ignoreRest = true;
			} else if (!ellipsisYet) {
				out = out+"\n    ...";
				ellipsisYet = true;
			}
		}
		return "The Elesmyr "+(server?"server":"client")+" has crashed. Info for geeks:\n"+out+"\nA full log can be found at ./LOTE_CRASH_LOG";
	}

	@Override
	public void init(GameContainer gameContainer) throws SlickException {
		gameContainer.setVSync(true); //True in release
		gameContainer.setVerbose(false); //False in release
		gameContainer.setClearEachFrame(false); //Set to false in release!
		gameContainer.setShowFPS(false); //Set to false in release
		gameContainer.setTargetFrameRate(60);
		gameContainer.getInput().initControllers();
		FontRenderer.setLang(FontRenderer.Language.valueOf(Globals.get("lang", "EN_US")));
		FontRenderer.initialise(gameContainer);
		Main.gc = gameContainer;
		gc.getInput().enableKeyRepeat();
		(new NUIState(5)).init(gameContainer, null);
	}

	@Override
	public void update(GameContainer gameContainer, int i) throws SlickException {
		StateManager.update(gameContainer);
	}

	@Override
	public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
		if (gc.getWidth()+gc.getHeight() != prev_res) {
			handleResize(gc);
			prev_res = gc.getWidth()+gc.getHeight();
			//System.out.println("IRES = "+INTERNAL_RESX+"x"+INTERNAL_RESY+" ("+INTERNAL_ASPECT+")");
		}
		Renderer.render(gameContainer, graphics);
	}

	public static void handleError(Exception e) {
		Log.error("Main.handleError(): ", e);
		StringWriter writer = new StringWriter(256);
		e.printStackTrace(new PrintWriter(writer));

		handleError(simplifyStackTrace(writer));
	}

	public static void handleError(String error) {
		gc.getInput().clearKeyPressedRecord();
		StateManager.error(error, true);
	}

	public static void handleResize(GameContainer gc) {
		INTERNAL_ASPECT = ((float) gc.getWidth()/(float) gc.getHeight());
		int ires = gc.getHeight();
		int i = 1;
		while (ires/i > 600) {
			i++;
		}
		ires /= i;
		INTERNAL_RESY = ires; //Internal resolution y
		INTERNAL_RESX = (int) (INTERNAL_RESY*INTERNAL_ASPECT); //Internal resolution x
		Renderer.onResize();
	}

	private static class SlickToMinLogSystem implements LogSystem {

		@Override
		public void debug(String arg0) {
			Log.debug(arg0);
		}

		@Override
		public void error(Throwable arg0) {
			Log.error(arg0.getClass().getSimpleName()+":", arg0);
		}

		@Override
		public void error(String arg0) {
			Log.error(arg0);
		}

		@Override
		public void error(String arg0, Throwable arg1) {
			Log.error(arg0, arg1);
		}

		@Override
		public void info(String arg0) {
			Log.info(arg0);
		}

		@Override
		public void warn(String arg0) {
			Log.warn(arg0);
		}

		@Override
		public void warn(String arg0, Throwable arg1) {
			Log.warn(arg0, arg1);
		}

	}

	private static class CustomAppGameContainer extends AppGameContainer {
		public CustomAppGameContainer(Game game) throws SlickException {super(game);}

		protected void updateAndRender(int delta) throws SlickException {
			Profiler.startSection("Update");
			if (smoothDeltas) {
				if (getFPS() != 0) {
					delta = 1000/getFPS();
				}
			}

			input.poll(width, height);

			Music.poll(delta);
			if (!paused) {
				storedDelta += delta;

				if (storedDelta >= minimumLogicInterval) {
					try {
						if (maximumLogicInterval != 0) {
							long cycles = storedDelta/maximumLogicInterval;
							for (int i = 0; i < cycles; i++) {
								game.update(this, (int) maximumLogicInterval);
							}

							int remainder = (int) (storedDelta%maximumLogicInterval);
							if (remainder > minimumLogicInterval) {
								game.update(this, (int) (remainder%maximumLogicInterval));
								storedDelta = 0;
							} else {
								storedDelta = remainder;
							}
						} else {
							game.update(this, (int) storedDelta);
							storedDelta = 0;
						}

					} catch (Throwable e) {
						running = false;
						Main.handleCrash(e);
					}
				}
			} else {
				game.update(this, 0);
			}

			Profiler.endSection();
			Profiler.startSection("Render");

			if (hasFocus() || getAlwaysRender()) {
				if (clearEachFrame) {
					GL.glClear(SGL.GL_COLOR_BUFFER_BIT|SGL.GL_DEPTH_BUFFER_BIT);
				}

				GL.glLoadIdentity();
				Graphics graphics = getGraphics();
				graphics.resetTransform();
				graphics.resetFont();
				graphics.resetLineWidth();
				graphics.setAntiAlias(false);
				try {
					game.render(this, graphics);
				} catch (Throwable e) {
					running = false;
					Main.handleCrash(e);
				}
				graphics.resetTransform();

				//if (this.isShowingFPS()) {
				//	this.getDefaultFont().drawString(10, 10, "FPS: "+recordedFPS);
				//}

				GL.flush();
			}
			Profiler.endSection();
			Profiler.flush();

			if (targetFPS != -1) {
				Display.sync(targetFPS);
			}
		}
	}
}