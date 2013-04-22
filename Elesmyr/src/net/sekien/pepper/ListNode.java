package net.sekien.pepper;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: matt Date: 20/04/13 Time: 12:56 PM To change this template use File | Settings |
 * File Templates.
 */
public class ListNode extends Node {

protected int sel = 0;
private static final org.newdawn.slick.Color titleColor = new org.newdawn.slick.Color(1, 1, 1, 0.75f);

public ListNode(String name) {
	super(name);
}

private int selh = 0;

@Override
public void render(Renderer renderer, int w, int h, boolean sel) {
	renderer.rectPos(100, 0, w-100, h, false, false, true, true, Renderer.BoxStyle.FULL);

	int basey = 100-selh;
	int addy = 0;
	int i = 0;

	renderer.pushPos(100, basey-100);
	renderer.g.pushTransform();
	renderer.g.scale(4, 4);
	renderer.text((220-renderer.textWidth(name)/2*4)/4, 5, name, titleColor);
	renderer.g.popTransform();
	renderer.popPos();

	for (Node child : children) {
		renderer.pushPos(100, basey+addy);
		child.render(renderer, 440, -1, i==this.sel);
		if (i==this.sel)
			selh += (addy-selh)/4;
		addy += child.getDimensions(i==this.sel).height;
		i++;
		renderer.popPos();
	}
}

@Override
public void transitionEnter(Renderer renderer, int w, int h, boolean sel, float time) {
	renderer.rectPos(100, 0, w-100, h, false, false, true, true, Renderer.BoxStyle.FULL);

	int basey = (int) ((100-selh)-(children.size()*32+100)*(1-time));
	int addy = 0;
	int i = 0;

	renderer.pushPos(100, basey-100);
	renderer.g.pushTransform();
	renderer.g.scale(4, 4);
	renderer.text((220-renderer.textWidth(name)/2*4)/4, 5, name, titleColor);
	renderer.g.popTransform();
	renderer.popPos();

	for (Node child : children) {
		renderer.pushPos(100, basey+addy);
		child.render(renderer, 440, -1, i==this.sel);
		addy += child.getDimensions(i==this.sel).height;
		if (i==this.sel)
			selh += (addy-selh)/2;
		i++;
		renderer.popPos();
	}
}

@Override
public Node nodeAt(int x, int y) {
	return this;
}

@Override
public void setSel(int x, int y) {
}

@Override
public void onAction(Action action) {
	if (action==Action.UP) {
		if (sel > 0)
			sel--;
	} else if (action==Action.DOWN) {
		if (sel < children.size()-1) {
			sel++;
		}
	} else {
		if (children.size() > 0)
			children.get(sel).onAction(action);
	}
}

@Override
public Dimension getDimensions(boolean sel) {
	return new Dimension(440, 480);
}
}
