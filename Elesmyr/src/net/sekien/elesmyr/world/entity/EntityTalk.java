/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr.world.entity;

import com.esotericsoftware.minlog.Log;
import net.sekien.elesmyr.msgsys.Connection;
import net.sekien.elesmyr.msgsys.Message;
import net.sekien.elesmyr.msgsys.MessageEndPoint;
import net.sekien.elesmyr.msgsys.MessageSystem;
import net.sekien.elesmyr.player.Camera;
import net.sekien.elesmyr.system.GameClient;
import net.sekien.elesmyr.system.GameServer;
import net.sekien.elesmyr.util.FileHandler;
import net.sekien.elesmyr.world.Region;
import net.sekien.hbt.HBTCompound;
import org.newdawn.slick.*;

public class EntityTalk extends Entity {
Image spr;
private DialogueHandler dh;

@Override
public void init(GameContainer gc, MessageEndPoint receiver)
		throws SlickException {
	spr = FileHandler.getImage("ent."+inst_dat.getString("extd", "").split(",", 2)[0]); //todo n extd
}

@Override
public void initSERV(GameServer server, Region region) {
	dh = new DialogueHandler("test");
}

@Override
public void render(GameContainer gc, Graphics g,
                   Camera cam, GameClient receiver) throws SlickException {
	if (spr==null)
		init(gc, receiver);
	spr.draw(xs+cam.getXOff(), ys+cam.getYOff());
}

@Override
public void interact(Region region, EntityPlayer entityPlayer, MessageEndPoint receiver, Message msg) {
	MessageSystem.sendClient(this, msg.getConnection(), new Message("CLIENT.echointwl", new HBTCompound("p")), false);
}

@Override
public void receiveMessageExt(Message msg, MessageEndPoint receiver) {
	if (msg.getName().equals("tresponse")) {
		dh.response(this, msg.getDataStr().split("\\|", 2)[0], msg.getDataStr().split("\\|", 2)[1], msg.getConnection());
	} else {
		Log.warn("EntityPlayer ingored message "+msg);
	}
}

private void response(String lang, String call, Connection connection) {
	if (call.equals("int")) {
		if (lang.equals("JP"))
			MessageSystem.sendClient(this, connection, new Message("CLIENT.talk", "ask:お元気ですか?|good:お元気です|bad:お元気じゃない|meh:無関心"), false);
		else
			MessageSystem.sendClient(this, connection, new Message("CLIENT.talk", "ask:How are you?|good:Good.|bad:Bad.|meh:Meh."), false);
	} else if (call.equals("good")) {
		MessageSystem.sendClient(this, connection, new Message("CLIENT.talk", "talk:That's great!"), false);
	} else if (call.equals("bad")) {
		MessageSystem.sendClient(this, connection, new Message("CLIENT.talk", "talkwf:again:Aww :("), false);
	} else if (call.equals("meh")) {
		MessageSystem.sendClient(this, connection, new Message("CLIENT.talk", "talk:Oh, okay."), false);
	} else if (call.equals("again")) {
		MessageSystem.sendClient(this, connection, new Message("CLIENT.talk", "ask:Are you still bad?|bad:Yes.|good:No."), false);
	} else {
		Log.warn("Unrecognised tresponse call '"+call+"'");
	}
}
}
