package net.sekien.elesmyr.ui;

import net.sekien.elesmyr.msgsys.Message;
import net.sekien.elesmyr.msgsys.MessageEndPoint;
import net.sekien.elesmyr.msgsys.MessageSystem;
import net.sekien.elesmyr.player.Camera;
import net.sekien.elesmyr.player.InventoryEntry;
import net.sekien.elesmyr.player.PlayerData;
import net.sekien.elesmyr.system.FontRenderer;
import net.sekien.elesmyr.system.GameClient;
import net.sekien.elesmyr.system.Main;
import net.sekien.elesmyr.util.FileHandler;
import net.sekien.elesmyr.world.entity.EntityPlayer;
import net.sekien.elesmyr.world.item.Item;
import net.sekien.hbt.HBTTools;
import org.newdawn.slick.*;

import java.util.ArrayList;

public class InventoryUI implements UserInterface {
Image bg;
Image invsel;
int sel = 0;
int isel = 0;
int smax = 1;
String[] types = {"All", "Weapons", "Armor", "Potions", "Food", "Books", "Misc"};

@Override
public void init(GameContainer gc,
                 MessageEndPoint receiver) throws SlickException {
	inited = true;
	bg = FileHandler.getImage("ui.inv");
	invsel = FileHandler.getImage("ui.invsel");
}

private boolean inited = false;

@Override
public boolean inited() {
	return inited;
}

@Override
public void render(GameContainer gc, Graphics g,
                   Camera cam, GameClient receiver) throws SlickException {
	int xoff = (Main.INTERNAL_RESX/2)-320;
	bg.draw(xoff, 0);
	invsel.draw(xoff+84+sel*35, 66);
	FontRenderer.drawString(xoff+77, 17, types[sel], g);
	try {
		PlayerData pdat = ((EntityPlayer) receiver.player.region.entities.get(receiver.player.entid)).pdat;
		int i = 0;
		int ir = 0-Math.max(0, isel-4);
		int iequip = 0;
		for (InventoryEntry ie : pdat.inventory) {
			Item iei = ie.getItem();
			if (iei.getType().equalsIgnoreCase(types[sel]) || sel==0) {
				if (ir >= 0 && ir <= 7) {
					g.setColor(Color.lightGray);
					if (i==isel)
						g.fillRect(xoff+67, 116+ir*38, 506, 36);
					g.setColor(Color.white);
					iei.spr.draw(xoff+78, 120+ir*38);
					FontRenderer.drawString(xoff+117, 128+ir*38, "#$item."+iei.getName(ie)+(ie.equals(pdat.getEquipped())?"| (|$inventory.equip|)":"|"), g);
					FontRenderer.drawString(xoff+450, 128+ir*38, ""+ie.getCount(), g);
					//Main.font.drawString(526,128+ir*40,"$"+ie.getValue()); //TODO: Value thingies
					//FontRenderer.drawString(xoff+526, 128+ir*38, ie.getExtd(), g);
				}
				i++;
				ir++;
			}
			iequip++;
		}
		smax = i;
	} catch (Exception e) { e.printStackTrace(); }
}

@Override
public void update(GameContainer gc, GameClient receiver) {
	Input in = gc.getInput();
	if (in.isKeyPressed(Input.KEY_LEFT))
		if (sel > 0) {
			sel--;
			isel = 0;
		}
	if (in.isKeyPressed(Input.KEY_RIGHT))
		if (sel < types.length-1) {
			sel++;
			isel = 0;
		}
	if (in.isKeyPressed(Input.KEY_UP))
		if (isel > 0)
			isel--;
	if (in.isKeyPressed(Input.KEY_DOWN))
		if (isel < smax-1)
			isel++;
	if (in.isKeyPressed(Input.KEY_X)) {
		InventoryEntry i = getItem(isel, receiver);
		if (i!=null) {
			EntityPlayer ep = ((EntityPlayer) receiver.player.region.entities.get(receiver.player.entid));
			ArrayList<InventoryEntry> inv = ((EntityPlayer) receiver.player.region.entities.get(receiver.player.entid)).pdat.inventory;
			if (i.getItem().canEquip()) {
				MessageSystem.sendServer(null, new Message(ep.getReceiverName()+".equip", HBTTools.msgInt("i", inv.indexOf(i))), false);
			} else {
				MessageSystem.sendServer(null, new Message(ep.getReceiverName()+".use", HBTTools.msgInt("i", inv.indexOf(i))), false);
			}
		}
	}
	if (in.isKeyPressed(Input.KEY_Z)) {
		InventoryEntry i = getItem(isel, receiver);
		if (i!=null) {
			EntityPlayer ep = ((EntityPlayer) receiver.player.region.entities.get(receiver.player.entid));
			ArrayList<InventoryEntry> inv = ((EntityPlayer) receiver.player.region.entities.get(receiver.player.entid)).pdat.inventory;
			MessageSystem.sendServer(null, new Message(ep.getReceiverName()+".drop", HBTTools.msgInt("i", inv.indexOf(i))), false);
		}
	}
	if (isel > smax-1)
		isel = smax-1;
	if (isel==-1 && smax!=0)
		isel = 0;
}

private InventoryEntry getItem(int isel, GameClient receiver) {
	int i = 0;
	for (InventoryEntry ie : ((EntityPlayer) receiver.player.region.entities.get(receiver.player.entid)).pdat.inventory) {
		if (ie.getItem().getType().equalsIgnoreCase(types[sel]) || sel==0) {
			if (i==isel)
				return ie;
			i++;
		}
	}
	return null;
}

@Override
public boolean blockUpdates() {return true;}

@Override
public void ctor(String extd) {
}
}
