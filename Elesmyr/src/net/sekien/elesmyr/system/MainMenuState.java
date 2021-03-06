/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr.system;

import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.minlog.Log;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.GaussianFilter;
import net.sekien.elesmyr.util.FileHandler;
import org.newdawn.slick.*;
import org.newdawn.slick.gui.*;
import org.newdawn.slick.opengl.*;
import org.newdawn.slick.state.*;
import org.newdawn.slick.util.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MainMenuState extends BasicGameState {
	int stateID = -1;

	Image background = null;
	Image bg2 = null;
	Image bg3 = null;
	Image alphabg = null;

	private String[] levels = null;
	private String[] showList;

	private boolean debug;

	static int subMenu = 0; //0=main 1=play 2=options 3=join 4=controls
	static int[] entryCount = {4, 2, 9, 2, 8};
	static String[][] entryString = {{"#menu.singleplayer", "#menu.join", "#menu.settings", "#menu.exit"},
	                                {"#menu.back", "#menu.newgame"},
	                                {"#$menu.video|: 480p |$menu.video.windowed", "#$menu.name|: Player", "#$menu.vsync|: |$false", "Volume: 10", "#$menu.debug|: |$false", "#$menu.lres| 24p", "#$menu.lang|: |$lang.EN_US", "#menu.controls", "#menu.back"},
	                                {"#menu.back", "#menu.enterip"},
	                                {"Input method: Keyboard/Mouse", "Walk: Arrow keys", "Interact: Z", "Attack: X", "Inventory: E", "Select: Enter", "Back/Pause: Esc", "#menu.back"}};
	static String[][] entryDesc = {{"#menu.singleplayer.desc", "#menu.join.desc", "#menu.settings.desc", "#menu.exit.desc"},
	                              {"#menu.back.desc"},
	                              {"#menu.video.desc", "#menu.name.desc", "#menu.vsync.desc", "#menu.volume.desc", "#menu.debug.desc", "#menu.lres.desc", "#menu.lang.desc", "#menu.controls.desc", "#menu.back.desc2"},
	                              {"#menu.back.desc"}};
	static int selection = 0;
	private static int dm = 0;
	public static int[] disx = {(int) (480*Main.INTERNAL_ASPECT), (int) (720*Main.INTERNAL_ASPECT), (int) (960*Main.INTERNAL_ASPECT), 0, (int) (480*Main.INTERNAL_ASPECT)};
	public static int[] disy = {480, 720, 960, 0, 480};
	private static boolean fullscreen;
	public static int lres = 24;
	private static Image[] button = new Image[2];
	private TextField textField;
	private boolean showTextField;
	private ArrayList<InetAddress> lanServers;

	private boolean serverOnly = false;

	private int dx = -256;
	private int tdx = 64;

	private boolean waitingForKeyPress;
	private String inKey = null;

	private InputListener getKeyListener;

	MainMenuState(int stateID) {
		this.stateID = stateID;
		getKeyListener = new GetKeyInputListener(this);
	}

	@Override
	public int getID() {
		return stateID;
	}

	/**
	 * This method will retrieve all the -sub folders- (Changed to .txt files) from the current directory that was passed
	 * in. Taken from http://www.dreamincode.net/code/snippet4167.htm
	 *
	 * @param cur
	 * - Current directory that the user is in
	 * @return A list of folders
	 */
	private ArrayList<File> getSubs(File cur) {

		//Get a list of all the files in folder
		ArrayList<File> temp = new ArrayList<File>();
		File[] fileList = cur.listFiles();

		//for each file in the folder
		for (int i = 0; i < fileList.length; i++) {

			//If the file is a Directory(folder) add it to return, if not done so already
			File choose = fileList[i];
			if (choose.isFile() && !temp.contains(choose) && (!choose.getName().equals("new") || debug) && choose.getName().endsWith(".hbt")) {
				temp.add(choose);
			}
		}
		return temp;
	}

	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		//background=FileHandler.getImage("menu.bg");
		resetBGImage();
		bg2 = FileHandler.getImage("menu.bg2");
		bg3 = FileHandler.getImage("menu.bg3");
		alphabg = FileHandler.getImage("ui.alphabg");

		ArrayList<File> files = getSubs(new File("save"));
		levels = new String[files.size()+2];
		entryCount[1] = files.size()+2;
		levels[0] = "#menu.back";
		levels[1] = "#menu.newgame";
		int i = 2;
		for (File f : files) {
			levels[i] = f.getName().substring(0, f.getName().lastIndexOf('.'));
			i++;
		}

		dm = Integer.parseInt(Globals.get("resdm", "0"));
		lres = Integer.parseInt(Globals.get("lres", "24"));
		debug = Globals.get("debug", false);

		Image temp = FileHandler.getImage("menu.button");
		button[0] = temp.getSubImage(0, 1, 256, 32);
		button[1] = temp.getSubImage(0, 33, 256, 32);
		//temp.destroy();
		//disx[3]=gc.getScreenWidth();
		//disy[3]=gc.getScreenHeight();

		if (dm != 3 && dm != 4)
			entryString[2][0] = "#$menu.video|: "+disy[dm]+"p |$menu.video.windowed";
		else
			entryString[2][0] = "#$menu.video|: "+disy[dm]+"p |$menu.video.fullscreen";
		entryString[2][1] = "#$menu.name|: "+Globals.get("name", "Player");
		entryString[2][2] = "#$menu.vsync|: |$"+Globals.get("vsync", false);
		if (Integer.parseInt(Globals.get("volume", "10")) == 0)
			entryString[2][3] = "#$menu.volume|: |$menu.volume.mute";
		else
			entryString[2][3] = "#$menu.volume|: "+Globals.get("volume", "10");
		entryString[2][4] = "#$menu.debug|: |$"+debug;
		entryString[2][5] = "#$menu.lres|: "+lres+"p";
		entryString[2][6] = "#$menu.lang|: |$lang."+FontRenderer.getLang().name();

		setControlText();

		textField = new TextField(gc, FontRenderer.getFont(), tdx, 128+19, 256, 16);
		textField.setBorderColor(null);
		textField.setBackgroundColor(null);
		textField.setTextColor(Color.white);
		textField.setAcceptingInput(false);
		textField.setMaxLength(15);
	}

	@Override
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
		resetBGImage();
	}

	public void resetBGImage() throws SlickException {
		File file = new File("save/thumb/"+Globals.get("lastSave", "")+".png");
		if (file.exists()) {
			try {
				BufferedImage src = ImageIO.read(file);
				AbstractBufferedImageOp filter = new GaussianFilter(8);
				BufferedImage filtered = filter.filter(src, null);
				Texture texture = BufferedImageUtil.getTexture("", filtered);
				background = new Image(texture.getImageWidth(), texture.getImageHeight());
				background.setTexture(texture);
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		} else {
			background = FileHandler.getImage("menu.bg");
		}
	}

	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		g.setColor(Color.black);
		float vw = gc.getWidth();
		float vh = gc.getHeight();
		float ox, oy, w, h;
		/*if (vw/vh < 1.6) { //Old aspect-ratio correction
			w = vw;
			h = vw*0.625f;
			ox = 0;
			oy = (vh-h)/2;
			g.fillRect(0, 0, w, oy);
			g.fillRect(0, oy+h, w, oy);
		} else {*/
		w = vh*(16/9f);
		h = vh;
		ox = (vw-w)/2;
		oy = 0;
		g.fillRect(0, 0, ox, h);
		g.fillRect(ox+w, 0, ox, h);
		//}*/

		g.setColor(Color.white);
		//background.draw(ox, oy, w, h);
		background.draw(0, 0, vw, vh);
		alphabg.draw(0, 0, vw, vh);
		alphabg.draw(0, 0, vw, vh);
		alphabg.draw(0, 0, vw, vh);
		bg2.draw(0, 0, vh*(4/3f), h);
		bg3.draw(vw-vh*(4/3f), 0, vh*(4/3f), h);
		//g.translate(ox, oy);
		g.scale(vw/Main.INTERNAL_RESX, vh/Main.INTERNAL_RESY);
		dx += (tdx-dx)/20;
		int menuSpace = 19;
		if (subMenu == 3) { //Join
			for (int i = 0; i < showList.length; i++) {
				int buttonState = 0;
				if (i == selection) buttonState = 1;
				int size = (int) (FontRenderer.getWidth(showList[i]))+16;
				button[buttonState].draw(dx+64-size/2, (float) (128+(i-Math.max(selection-12, 0))*menuSpace), size, 16);
				FontRenderer.drawString(dx+64-(FontRenderer.getWidth(showList[i])/2), (float) (128+(i-Math.max(selection-12, 0))*menuSpace), showList[i], g);
			}
		} else if (subMenu != 1) { //normal
			for (int i = 0; i < entryCount[subMenu]; i++) {
				int buttonState = 0;
				if (i == selection) buttonState = 1;
				int size = (int) (FontRenderer.getWidth(entryString[subMenu][i]))+16;
				button[buttonState].draw(dx+64-size/2, (float) (128+(i-Math.max(selection-12, 0))*menuSpace), size, 16);
				FontRenderer.drawString(dx+64-(FontRenderer.getWidth(entryString[subMenu][i])/2), (float) (128+(i-Math.max(selection-12, 0))*menuSpace), entryString[subMenu][i], g);
			}
		} else { //Levels
			for (int i = 0; i < levels.length; i++) {
				int buttonState = 0;
				if (i == selection) buttonState = 1;
				int size = (int) (FontRenderer.getWidth(levels[i]))+16;
				button[buttonState].draw(dx+64-size/2, (float) (128+(i-Math.max(selection-12, 0))*menuSpace), size, 16);
				FontRenderer.drawString(dx+64-(FontRenderer.getWidth(levels[i])/2), (float) (128+(i-Math.max(selection-12, 0))*menuSpace), levels[i], g);
			}
		}
		if (showTextField) {
			textField.setLocation(dx+64-(FontRenderer.getWidth(textField.getText())/2), textField.getY());
			textField.render(gc, g);
			textField.setFocus(true);
		}
		FontRenderer.drawString(Main.INTERNAL_RESX-(FontRenderer.getWidth(getDescription(subMenu, selection))+128), 380, getDescription(subMenu, selection), g);
		FontRenderer.drawString(0, 0, "#$bar.title| |"+Main.version, g);
		g.scale(2, 2);
		FontRenderer.drawString((dx+64-(FontRenderer.getWidth(menuName(subMenu))))/2, 56/2, menuName(subMenu), g);
	}

	private String menuName(int menu) {
		switch (menu) {
			case 0:
				return "#menu.menu";
			case 1:
				if (serverOnly) { return "#menu.server"; } else { return "#menu.singleplayer"; }
			case 2:
				return "#menu.settings";
			case 3:
				return "#menu.join";
			case 4:
				return "#menu.controls";
			default:
				return "#null";
		}
	}

	private String getDescription(int sub, int sel) {
		try {
			return entryDesc[sub][sel];
		} catch (Exception e) {
			if (sub == 1) { //levels
				if (serverOnly) {
					if (sel == 1)
						if (showTextField) {
							return "#menu.newgame.enter";
						} else {
							return "#menu.newgame.desc";
						}
					return "#$menu.game.play.prefix|"+levels[sel]+"|$menu.game.play.suffix ";
				} else {
					if (sel == 1)
						if (showTextField) {
							return "#menu.newgame.enter";
						} else {
							return "#menu.newgame.desc";
						}
					return "#$menu.game.play.prefix|"+levels[sel]+"|$menu.game.play.suffix";
				}
			} else if (sub == 3) { //join
				if (sel == 1)
					if (showTextField) {
						return "#menu.enterip.enter";
					} else {
						return "#menu.enterip.desc";
					}
				return "#$menu.join.lan.prefix|"+showList[sel]+"|$menu.join.lan.suffix";
			} else if (sub == 4) { //controls
				if (waitingForKeyPress)
					return "#menu.controls.waitkey";
			}
		}
		return "NO DESC";
	}

	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		//org.newdawn.slick.Input input=gc.getInput();
		if (waitingForKeyPress) {
			if (gc.getInput().isKeyPressed(org.newdawn.slick.Input.KEY_RETURN)) {    //For some odd reason (bug?), InputListener doesn't receive return key presses
				inKey = "KEY_"+org.newdawn.slick.Input.KEY_RETURN;            //So this here checks for them instead.
				waitingForKeyPress = false;
			}
			return; //Do nothing if waiting for a key press
		} else if (inKey != null) { //"Input method: Keyboard/Mouse","Walk: Arrow keys","Interact: Z","Attack: X","Inventory: E","Select: Enter","Back/Pause: Esc","Back"
			switch (selection) {
				case 2:
					GameInput.setKey("int", inKey);
					break;
				case 3:
					GameInput.setKey("atk", inKey);
					break;
				case 4:
					GameInput.setKey("inv", inKey);
					break;
				case 5:
					GameInput.setKey("sel", inKey);
					break;
				case 6:
					GameInput.setKey("pause", inKey);
					break;
			}
			waitingForKeyPress = false;
			inKey = null;
			gc.getInput().removeListener(getKeyListener);
			gc.getInput().clearKeyPressedRecord();
			setControlText();
		}

		if (GameInput.isDownPressed(gc) && !showTextField) {
			if (selection < entryCount[subMenu]-1) {
				selection++;
			}
		}
		if (GameInput.isUpPressed(gc) && !showTextField) {
			if (selection > 0) {
				selection--;
			}
		}
		if (gc.getInput().isKeyPressed(org.newdawn.slick.Input.KEY_F2))
			sbg.enterState(Main.NUISTATE);
		if (GameInput.isKeyPressed(gc, "pause")) {
			if (showTextField) {
				showTextField = false;
				textField.setText("");
				textField.setAcceptingInput(false);
				textField.setFocus(false);
				if (subMenu == 1)
					levels[1] = "#menu.newgame";
				else if (subMenu == 3)
					showList[1] = "#menu.enterip";
			} else if (subMenu != 0) {
				selection = 0;
				subMenu = 0;
				dx = -256;
			} else {
				gc.exit();
			}
		}
		if (GameInput.isKeyPressed(gc, "sel")) {
			switch ((subMenu*100)+selection) {
				case 000: { //Singleplayer
					selection = 0;
					subMenu = 1;
					dx = -256;
					serverOnly = false;
				}
				break;
				case 001: { //Join
					selection = 0;
					subMenu = 3;
					dx = -256;
					try {
						Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
						while (nis.hasMoreElements()) {
							Log.info(((NetworkInterface) nis.nextElement()).toString());
						}
					} catch (SocketException e) {
						e.printStackTrace();
					}
					lanServers = (ArrayList<InetAddress>) discoverHosts(37021, 1000);
					showList = new String[lanServers.size()+2];
					showList[0] = entryString[3][0];
					showList[1] = entryString[3][1];
					for (int i = 2; i < showList.length; i++) {
						showList[i] = lanServers.get(i-2).toString();
					}
					entryCount[3] = showList.length;
				}
				break;
			/*case 002: { //Server
				selection = 0;
				subMenu = 1;
				dx=-256;
				serverOnly = true;
			} break;*/
				case 002: {
					selection = 0;
					subMenu = 2;
					dx = -256;
				}
				break;
				case 003: {
					gc.exit();
				}
				break;
				case 200: {
					dm++;
					if (dm == 5) dm = 0;
					fullscreen = (dm == 3 || dm == 4);
					//entryString[2][0] = "Resolution: "+disx[dm]+"x"+disy[dm];
					if (!fullscreen)
						entryString[2][0] = "#$menu.video|: "+disy[dm]+"p |$menu.video.windowed";
					else
						entryString[2][0] = "#$menu.video|: "+disy[dm]+"p |$menu.video.fullscreen";
					Globals.set("resdm", String.valueOf(dm));
				}
				break;
				case 201: {
					//Enter name
					if (!showTextField) {
						entryString[2][1] = "";
						showTextField = true;
						textField.setText("");
						textField.setAcceptingInput(true);
						textField.setFocus(true);
					} else {
						entryString[2][1] = "#$menu.name|: "+textField.getText();
						Globals.set("name", textField.getText());
						showTextField = false;
						textField.setText("");
						textField.setAcceptingInput(false);
						textField.setFocus(false);
					}
				}
				break;
				case 202: {
					boolean vsync = Boolean.parseBoolean(Globals.get("vsync", "false"));
					//entryString[2][0] = "Resolution: "+disx[dm]+"x"+disy[dm];
					entryString[2][2] = "#$menu.vsync|: |$"+!vsync;
					Globals.set("vsync", String.valueOf(!vsync));
				}
				break;
				case 203: {
					int vol = Integer.parseInt(Globals.get("volume", "10"));
					vol++;
					if (vol > 10) {
						vol = 0;
					}
					//entryString[2][0] = "Resolution: "+disx[dm]+"x"+disy[dm];
					if (vol == 0)
						entryString[2][3] = "#$menu.volume|: |$menu.volume.mute";
					else
						entryString[2][3] = "#$menu.volume|: "+vol;
					Globals.set("volume", String.valueOf(vol));
				}
				break;
				case 204: {
					debug = Globals.get("debug", false);
					//entryString[2][0] = "Resolution: "+disx[dm]+"x"+disy[dm];
					debug = !debug;
					entryString[2][4] = "#$menu.debug|: |$"+debug;
					Globals.set("debug", String.valueOf(debug));
					ArrayList<File> files = getSubs(new File("save"));
					levels = new String[files.size()+2];
					entryCount[1] = files.size()+2;
					levels[0] = "#menu.back";
					levels[1] = "#menu.newgame";
					int i = 2;
					for (File f : files) {
						levels[i] = f.getName().substring(0, f.getName().lastIndexOf('.'));
						i++;
					}
				}
				break;
				case 205: {
					if (lres >= 24)
						lres += 12;
					else
						lres += 6;
					if (lres > 48) lres = 6;
					entryString[2][5] = "#$menu.lres| "+lres+"p";
					Globals.set("lres", String.valueOf(lres));
				}
				break;
				case 206: {
					FontRenderer.setLang(FontRenderer.Language.values()[(FontRenderer.getLang().ordinal()+1)%FontRenderer.Language.values().length]);
					entryString[2][6] = "#$menu.lang|: |$lang."+FontRenderer.getLang().name();
					Globals.set("lang", FontRenderer.getLang().name());
				}
				break;
				case 207: {
					selection = 0;
					subMenu = 4;
					dx = -256;
				}
				break;
				case 208: {
					if (((AppGameContainer) gc).getHeight() != disy[dm]) {
						((AppGameContainer) gc).setDisplayMode(disx[dm], disy[dm], fullscreen);
						Main.INTERNAL_ASPECT = ((float) MainMenuState.disx[dm]/(float) MainMenuState.disy[dm]);
						Main.INTERNAL_RESX = (int) (Main.INTERNAL_RESY*Main.INTERNAL_ASPECT); //Internal resolution x
					}
					((AppGameContainer) gc).setMouseGrabbed(dm == 3 || dm == 3);
					FontRenderer.reset(gc);
					Globals.save();
					selection = 0;
					subMenu = 0;
					dx = -256;
				}
				break;
				case 402:
				case 403:
				case 404:
				case 405:
				case 406: { //"Input method: Keyboard/Mouse","Walk: Arrow keys","Interact: Z","Attack: X","Inventory: E","Select: Enter","Back/Pause: Esc","Back"
					gc.getInput().clearKeyPressedRecord();
					gc.getInput().addListener(getKeyListener);
					waitingForKeyPress = true;
				}
				break;
				case 407: {
					selection = 0;
					subMenu = 2;
					dx = -256;
				}
				break;
				case 100: {
					selection = 0;
					subMenu = 0;
					dx = -256;
				}
				break;
				case 101: {
					//New game
					if (!showTextField) {
						levels[1] = "";
						showTextField = true;
						textField.setText("");
						textField.setAcceptingInput(true);
						textField.setFocus(true);
					} else {
						if (textField.getText().equals("")) {
						} else if (new File("save/"+textField.getText()).exists()) {
							gc.getInput().clearKeyPressedRecord();
							((ErrorState) sbg.getState(Main.ERRORSTATE)).errorText = "Save "+textField.getText()+" already exists!";
							sbg.enterState(Main.ERRORSTATE);
						} else {
							try {
								copyDirectory(new File("save/new"), new File("save/"+textField.getText()));
								ArrayList<File> files = getSubs(new File("save"));
								levels = new String[files.size()+2];
								entryCount[1] = files.size()+2;
								levels[0] = "#menu.back";
								levels[1] = "#menu.newgame";
								int i = 2;
								for (File f : files) {
									levels[i] = f.getName().substring(0, f.getName().lastIndexOf('.'));
									i++;
								}
							} catch (IOException e) {
								gc.getInput().clearKeyPressedRecord();
								((ErrorState) sbg.getState(Main.ERRORSTATE)).errorText = "#$error.makesaveioe|\n"+e.getLocalizedMessage();
								sbg.enterState(Main.ERRORSTATE);
							}
						}
						levels[1] = "#menu.newgame";
						showTextField = false;
						textField.setText("");
						textField.setAcceptingInput(false);
						textField.setFocus(false);
					}
				}
				break;
				case 300: {
					selection = 0;
					subMenu = 0;
					dx = -256;
				}
				break;
				case 301: {
					//Enter IP
					if (!showTextField) {
						showList[1] = "";
						showTextField = true;
						textField.setText("");
						textField.setAcceptingInput(true);
						textField.setFocus(true);
					} else {
						try {
							((GameClient) sbg.getState(Main.GAMEPLAYSTATE)).join(InetAddress.getByName(textField.getText()));
							gc.getInput().clearKeyPressedRecord();
							sbg.enterState(Main.LOGINSTATE);
						} catch (UnknownHostException e) {
							gc.getInput().clearKeyPressedRecord();
							((ErrorState) sbg.getState(Main.ERRORSTATE)).errorText = "#$error.unknownhost.prefix|"+textField.getText()+"|$error.unknownhost.suffix";
							sbg.enterState(Main.ERRORSTATE);
						} catch (IOException e) {
							gc.getInput().clearKeyPressedRecord();
							((ErrorState) sbg.getState(Main.ERRORSTATE)).errorText = "#$error.joinioe.prefix|"+textField.getText()+"|$error.joinioe.suffix";
							sbg.enterState(Main.ERRORSTATE);
						} catch (Exception e) {
							Main.handleCrash(e);
							gc.exit();
						}
						showList[1] = entryString[3][1];
						showTextField = false;
						textField.setText("");
						textField.setAcceptingInput(false);
						textField.setFocus(false);
					}
				}
				break;
				default: {
					if (subMenu == 1) {
						Globals.set("save", levels[selection]);
						try {
							//((GameClient) sbg.getState(Main.GAMEPLAYSTATE)).loadSave(gc, levels[selection], serverOnly, sbg);
							//((GameClient) sbg.getState(Main.GAMEPLAYSTATE)).init(gc, sbg);
							gc.getInput().clearKeyPressedRecord();
							sbg.enterState(Main.GAMEPLAYSTATE);
						} catch (Exception e) {
							if (e.getLocalizedMessage() != null && e.getLocalizedMessage().equals("__BIND_EXCEPTION")) {
								Log.error(e.getLocalizedMessage());
								gc.getInput().clearKeyPressedRecord();
								((ErrorState) sbg.getState(Main.ERRORSTATE)).errorText =
								"#error.bindport";
								sbg.enterState(Main.ERRORSTATE);
								return;
							} else {
								Main.handleCrash(e);
								gc.exit();
							}
						}
					} else if (subMenu == 3) {
						try {
							((GameClient) sbg.getState(Main.GAMEPLAYSTATE)).join(lanServers.get(selection-2));
							//((GameClient) sbg.getState(Main.GAMEPLAYSTATE)).init(gc, sbg);
							gc.getInput().clearKeyPressedRecord();
							sbg.enterState(Main.LOGINSTATE);
						} catch (IOException e) {
							gc.getInput().clearKeyPressedRecord();
							((ErrorState) sbg.getState(Main.ERRORSTATE)).errorText =
							"IOException caught joining server:\n"+
							e.getLocalizedMessage();
							sbg.enterState(Main.ERRORSTATE);
							return;
						} catch (Exception e) {
							Main.handleCrash(e);
							gc.exit();
						}
					}
				}
			}
		}
	}

	private void setControlText() {
		entryString[4] = new String[]{"Input method: Keyboard/Mouse",
		                             "Walk: Arrow keys",
		                             "Interact: "+resolveKeyName(Globals.get("IN_int", "KEY_"+org.newdawn.slick.Input.KEY_Z)),
		                             "Attack: "+resolveKeyName(Globals.get("IN_atk", "KEY_"+org.newdawn.slick.Input.KEY_X)),
		                             "Inventory: "+resolveKeyName(Globals.get("IN_inv", "KEY_"+org.newdawn.slick.Input.KEY_C)),
		                             "Select: "+resolveKeyName(Globals.get("IN_sel", "KEY_"+org.newdawn.slick.Input.KEY_ENTER)),
		                             "Back/Pause: "+resolveKeyName(Globals.get("IN_pause", "KEY_"+org.newdawn.slick.Input.KEY_ESCAPE)),
		                             "#menu.back"};
	}

	private String resolveKeyName(String keyn) {
		if (keyn.split("_")[0].equals("KEY"))
			return org.newdawn.slick.Input.getKeyName(Integer.parseInt(keyn.split("_")[1]));
		return keyn;
	}

	public List<InetAddress> discoverHosts(int udpPort, int timeoutMillis) {
		List<InetAddress> hosts = new ArrayList<InetAddress>();
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			ByteBuffer dataBuffer = ByteBuffer.allocate(64);
			new KryoSerialization().write(null, dataBuffer, new FrameworkMessage.DiscoverHost());
			dataBuffer.flip();
			byte[] data = new byte[dataBuffer.limit()];
			dataBuffer.get(data);
			for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue; // Don't want to broadcast to the loopback interface
				}

				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) {
						continue;
					}

					// Send the broadcast package!
					try {
						DatagramPacket sendPacket = new DatagramPacket(data, data.length, broadcast, udpPort);
						socket.send(sendPacket);
					} catch (Exception e) {
					}

					Log.info("Request packet sent to: "+broadcast.getHostAddress()+"; Interface: "+networkInterface.getDisplayName());
				}
				//try {
				/*for (InetAddress address : Collections.list(iface.getInetAddresses())) {
					//Log.info(iface.toString()+": "+address);
					// Java 1.5 doesn't support getting the subnet mask, so try the two most common.
					//byte[] ip = address.getAddress();
					//ip[3] = -1;

					socket.send(new DatagramPacket(data, data.length, InetAddress.getByAddress(ip), udpPort));
				}*/
				//} catch (Exception e) { e.printStackTrace();}
			}
			Log.debug("Broadcasted host discovery on port: "+udpPort);
			socket.setSoTimeout(timeoutMillis);
			while (true) {
				DatagramPacket packet = new DatagramPacket(new byte[0], 0);
				try {
					socket.receive(packet);
				} catch (SocketTimeoutException ex) {
					Log.info("Host discovery timed out.");
					return hosts;
				}
				Log.info("Discovered server: "+packet.getAddress());
				hosts.add(packet.getAddress());
			}
		} catch (IOException ex) {
			if (Log.ERROR) Log.error("kryonet", "Host discovery failed.", ex);
			return hosts;
		} finally {
			if (socket != null) socket.close();
		}
	}

	// If targetLocation does not exist, it will be created.
	public static void copyDirectory(File sourceLocation, File targetLocation)
	throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
				             new File(targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public class GetKeyInputListener implements InputListener {
		private MainMenuState mms;

		public GetKeyInputListener(MainMenuState mms) {
			this.mms = mms;
		}

		private void setControl(String control) {
			mms.inKey = control;
			mms.waitingForKeyPress = false;
		}

		@Override
		public void mousePressed(int arg0, int arg1, int arg2) {
			setControl("MOUSE_"+arg0);
		}

		@Override
		public void keyPressed(int arg0, char arg1) {
			Log.info(arg0+","+arg1);
			setControl("KEY_"+arg0);
		}

		@Override
		public void controllerButtonPressed(int arg0, int arg1) {
			setControl("GC_"+arg1);
		}

		@Override
		public void setInput(org.newdawn.slick.Input arg0) {}

		@Override
		public void inputEnded() {}

		@Override
		public void inputStarted() {}

		@Override
		public boolean isAcceptingInput() {return true;}

		@Override
		public void mouseClicked(int arg0, int arg1, int arg2, int arg3) {}

		@Override
		public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {}

		@Override
		public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {}

		@Override
		public void mouseReleased(int arg0, int arg1, int arg2) {}

		@Override
		public void mouseWheelMoved(int arg0) {}

		@Override
		public void keyReleased(int arg0, char arg1) {}

		@Override
		public void controllerButtonReleased(int arg0, int arg1) {}

		@Override
		public void controllerDownPressed(int arg0) {}

		@Override
		public void controllerDownReleased(int arg0) {}

		@Override
		public void controllerLeftPressed(int arg0) {}

		@Override
		public void controllerLeftReleased(int arg0) {}

		@Override
		public void controllerRightPressed(int arg0) {}

		@Override
		public void controllerRightReleased(int arg0) {}

		@Override
		public void controllerUpPressed(int arg0) {}

		@Override
		public void controllerUpReleased(int arg0) {}
	}
}