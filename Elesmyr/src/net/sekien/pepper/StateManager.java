package net.sekien.pepper;

import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.GaussianFilter;
import net.sekien.elesmyr.system.FontRenderer;
import net.sekien.elesmyr.system.GameClient;
import net.sekien.elesmyr.system.Globals;
import net.sekien.elesmyr.system.Main;
import net.sekien.elesmyr.util.FileHandler;
import org.newdawn.slick.*;
import org.newdawn.slick.gui.TextField;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.BufferedImageUtil;
import org.newdawn.slick.util.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA. User: matt Date: 20/04/13 Time: 10:40 AM To change this template use File | Settings |
 * File Templates.
 */
public class StateManager {
private static HashMap<String, Node> states;
private static Renderer renderer;
private static Image background;
private static Image newBG = null;
private static int bgAnim = 0;
private static Stack<Node> stateTrace;
private static List<PopupNode> popup;
private static boolean enterGame = false;
private static String gameSettings;

private static int animtimer = 0;
private static String newState = null;

private static TextField textField;
private static Node textLock = null;

public static void init(GameContainer gc) {
	states = new HashMap<String, Node>();
	stateTrace = new Stack<Node>();
	popup = new LinkedList<PopupNode>();
	renderer = new Renderer();
	try {
		background = FileHandler.getImage("menu.bg");
	} catch (Exception e) {}

	textField = new TextField(gc, FontRenderer.getFont(), 0, 16, 530, 16);
	textField.setBorderColor(null);
	textField.setBackgroundColor(null);
	textField.setTextColor(Color.white);
	textField.setAcceptingInput(false);
	textField.setMaxLength(57);
}

public static void render(GameContainer gc, Graphics g) {
	renderer.setGraphicsAndGC(gc, g);
	background.draw(0, 0, gc.getWidth(), gc.getHeight());
	if (newBG!=null) {
		bgAnim++;
		if (bgAnim < 10) {
			newBG.setAlpha(bgAnim/10f);
			newBG.draw(0, 0, gc.getWidth(), gc.getHeight());
		} else {
			newBG.setAlpha(1);
			bgAnim = 0;
			background = newBG;
			newBG = null;
			background.draw(0, 0, gc.getWidth(), gc.getHeight());
		}
	}
	g.scale(gc.getWidth()/(float) (Main.INTERNAL_RESX), gc.getHeight()/(float) (Main.INTERNAL_RESY));
	if (!stateTrace.empty()) {
		if (newState!=null) {
			animtimer++;
			if (animtimer < 20) {
				stateTrace.peek().transitionLeave(renderer, Main.INTERNAL_RESX, Main.INTERNAL_RESY, true, animtimer/20f);
			} else if (animtimer==20) {
				stateTrace.peek().transitionLeave(renderer, Main.INTERNAL_RESX, Main.INTERNAL_RESY, true, animtimer/20f);
				if (newState.equals("_POP")) {
					stateTrace.pop();
				} else {
					stateTrace.push(states.get(newState));
					stateTrace.peek().update(gc); //This is to update DynamicListNodes, otherwise they won't animate properly.
				}
			} else if (animtimer < 40) {
				stateTrace.peek().transitionEnter(renderer, Main.INTERNAL_RESX, Main.INTERNAL_RESY, true, (animtimer-20)/20f);
			} else {
				stateTrace.peek().transitionEnter(renderer, Main.INTERNAL_RESX, Main.INTERNAL_RESY, true, (animtimer-20)/20f);
				newState = null;
				animtimer = 0;
			}
		} else {
			stateTrace.peek().render(renderer, Main.INTERNAL_RESX, Main.INTERNAL_RESY, true);
		}
	}
	for (PopupNode node : popup) {
		node.render(renderer, Main.INTERNAL_RESX, Main.INTERNAL_RESY, false);
	}
}

public static void update(GameContainer gc, StateBasedGame sbg) {
	if (filtered!=null) {
		try {
			Texture texture = BufferedImageUtil.getTexture("", filtered);
			newBG = new Image(texture.getImageWidth(), texture.getImageHeight());
			newBG.setTexture(texture);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SlickException e) {
			e.printStackTrace();
		}
		filtered = null;
	}
	if (enterGame) {
		enterGame = false;
		String mode = gameSettings;
		String arg = "";
		if (gameSettings.contains(" ")) {
			mode = gameSettings.split(" ", 2)[0];
			arg = gameSettings.split(" ", 2)[1];
		}
		if (mode.equals("SAVE")) {
			String savename = arg.substring(0, arg.lastIndexOf('.'));
			Globals.set("save", savename);
			try {
				((GameClient) sbg.getState(Main.GAMEPLAYSTATE)).loadSave(gc, savename, false, sbg);
				((GameClient) sbg.getState(Main.GAMEPLAYSTATE)).init(gc, sbg);
				gc.getInput().clearKeyPressedRecord();
				sbg.enterState(Main.GAMEPLAYSTATE);
			} catch (Exception e) {
				if (e.getLocalizedMessage()!=null && e.getLocalizedMessage().equals("__BIND_EXCEPTION")) {
					com.esotericsoftware.minlog.Log.error(e.getLocalizedMessage());
					error("Could not bind to port.\nThis most likely means another\ncopy of the game is already running\n\n\n\nYeah", false);
					return;
				} else {
					error(e.toString(), false);
				}
			}
		} else if (mode.equals("MAINMENU")) {
			sbg.enterState(Main.MENUSTATE);
		}
	}
	if (!stateTrace.empty()) {
		if (newState==null) {
			Input input = gc.getInput();
			Action action = getAction(input);
			boolean popupHasFocus = false;
			List<PopupNode> removePopups = new ArrayList<PopupNode>(5);
			for (PopupNode node : popup) {
				if (node.receiveActions() && !popupHasFocus) {
					if (action!=null)
						node.onAction(action);
					node.update(gc);
					popupHasFocus = true;
				}
				if (node.isClosed())
					removePopups.add(node);
			}
			for (PopupNode node : removePopups) {
				popup.remove(node);
			}
			if (!popupHasFocus) {
				if (action==Action.BACK)
					back();
				else if (action!=null)
					stateTrace.peek().onAction(action);
				if (!stateTrace.empty())
					stateTrace.peek().update(gc);
			}
		}
	} else {
		gc.exit();
	}
}

private static Action getAction(Input input) {
	if (input.isKeyPressed(Input.KEY_ENTER)) {
		return Action.SELECT;
	} else if (input.isKeyPressed(Input.KEY_ESCAPE)) {
		return Action.BACK;
	} else if (input.isKeyPressed(Input.KEY_UP)) {
		return Action.UP;
	} else if (input.isKeyPressed(Input.KEY_DOWN)) {
		return Action.DOWN;
	} else if (input.isKeyPressed(Input.KEY_LEFT)) {
		return Action.LEFT;
	} else if (input.isKeyPressed(Input.KEY_RIGHT)) {
		return Action.RIGHT;
	}
	return null;
}

public static void setState(String name) {
	if (states.containsKey(name)) {
		newState = name;
	} else {
		error("State not found: "+name, false);
		//throw new StateNotFoundException(name);
	}
}

public static void setStateInitial(String name) {
	stateTrace.push(states.get(name));
	newState = name;
	animtimer = 21; //Skip closing of previous state;
}

public static void registerState(Node node) {
	if (!states.containsKey(node.getName())) {
		states.put(node.getName(), node);
	} else {
		throw new StateAlreadyExistsException(node.getName());
	}
}

private static BufferedImage filtered;

public static void setBackground(String name) {
	try {
		final File file = new File("save/thumb/"+name+".png");
		if (file.exists()) {
			new Thread() {
				public void run() {
					try {
						BufferedImage src = ImageIO.read(file);
						AbstractBufferedImageOp filter = new GaussianFilter(8);
						filtered = filter.filter(src, null);
					} catch (IOException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			}.start();
		} else {
			newBG = FileHandler.getImage("menu.bg");
		}
	} catch (SlickException e) {
		Log.error(e);
	}
}

public static void back() {
	if (stateTrace.size() > 1)
		newState = "_POP";
	else
		popup.add(new DialogPopup("_quitys", "Close the game and return to the desktop?", 1) {
			@Override
			protected void onSelect(int sel) {
				if (sel==0)
					newState = "_POP";
			}
		});
}

public static void error(String string, boolean goBack) {
	popup.add(new ErrorPopup("Error", string, goBack));
}

public static void updFunc(String func) {
	enterGame = true;
	gameSettings = func;
}

public static boolean getTextLock(Node node) {
	if (textLock==null) {
		textLock = node;
		textField.setText("");
		textField.setCursorPos(0);
		textField.setAcceptingInput(true);
		textField.setFocus(true);
		return true;
	} else if (textLock==node) {
		Log.error(node.getName()+" already has textLock!");
		return true;
	} else {
		Log.error(node.getName()+" cannot get textLock, already obtained by "+textLock.getName());
		return false;
	}
}

public static void freeTextLock(Node node) {
	if (textLock==null) {
		Log.error(node+" cannot free textLock, already freed!");
	} else if (textLock==node) {
		textLock = null;
		textField.setText("");
		textField.setCursorPos(0);
		textField.setAcceptingInput(false);
		textField.setFocus(false);
	} else {
		Log.error(node+" cannot free textLock, it was obtained by "+textLock);
	}
}

public static void setTextBox(Node node, int x, int y) {
	if (textLock==node) {
		textField.setLocation(x, y);
	} else {
		Log.error(node+" cannot setTextBox, it doesn't have the lock!"+(textLock==null?"":" The lock was obtained by "+textLock));
	}
}

public static void setTextBoxText(Node node, String text) {
	if (textLock==node) {
		textField.setText(text);
		textField.setCursorPos(text.length());
	} else {
		Log.error(node+" cannot setTextBox, it doesn't have the lock!"+(textLock==null?"":" The lock was obtained by "+textLock));
	}
}

public static void renderTextBox(Node node, Renderer renderer) {
	if (textLock==node) {
		textField.render(renderer.gc, renderer.g);
		textField.setFocus(true);
	} else {
		Log.error(node+" cannot renderTextBox, it doesn't have the lock!"+(textLock==null?"":" The lock was obtained by "+textLock));
	}
}

public static void setTextBoxCentered(Node node, int x, int y) {
	if (textLock==node) {
		textField.setLocation(x-renderer.textWidth(textField.getText())/2, y);
	} else {
		Log.error(node+" cannot setTextBoxCentered, it doesn't have the lock!"+(textLock==null?"":" The lock was obtained by "+textLock));
	}
}

public static String getTextBoxText(Node node) {
	if (textLock==node) {
		return textField.getText();
	} else {
		Log.error(node+" cannot setTextBox, it doesn't have the lock!"+(textLock==null?"":" The lock was obtained by "+textLock));
		return "";
	}
}

private static class StateNotFoundException extends RuntimeException {
	public StateNotFoundException(String name) {super(name);}
}

private static class StateAlreadyExistsException extends RuntimeException {
	public StateAlreadyExistsException(String name) {super(name);}
}
}