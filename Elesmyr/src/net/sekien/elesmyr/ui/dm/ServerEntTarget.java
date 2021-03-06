/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr.ui.dm;

import net.sekien.elesmyr.msgsys.Message;
import net.sekien.elesmyr.msgsys.MessageEndPoint;
import net.sekien.elesmyr.msgsys.MessageReceiver;
import net.sekien.elesmyr.msgsys.MessageSystem;
import net.sekien.elesmyr.system.GameClient;
import net.sekien.hbt.HBTCompound;
import net.sekien.hbt.HBTFlag;
import net.sekien.hbt.HBTString;
import net.sekien.hbt.HBTTag;

/**
 * Created with IntelliJ IDEA. User: matt Date: 19/04/13 Time: 4:13 PM To change this template use File | Settings |
 * File Templates.
 */
public class ServerEntTarget implements DevModeTarget, MessageReceiver {

	private HBTCompound cached = new HBTCompound("DMRcache");

	private String get, set;

	private boolean registered = false;

	public ServerEntTarget() {
		this("_hbt", "_hbtSET");
	}

	public ServerEntTarget(String get, String set) {
		this.get = get;
		this.set = set;
	}

	@Override
	public void set(HBTCompound list, String subTarget, GameClient client) {
		MessageSystem.sendServer(this, new Message(client.player.region+"."+subTarget+
		                                           "."+set, list), false);
		cached = (HBTCompound) list.deepClone();
	}

	@Override
	public HBTCompound getList(GameClient client, String subTarget) {
		if (!registered)
			MessageSystem.registerReceiverClient(this);
		MessageSystem.sendServer(this, new Message(client.player.region+"."+subTarget+"."+get, new HBTCompound("p", new HBTTag[]{new HBTString("receiver", this.getReceiverName()), new HBTFlag("full", "TRUE")})), false);
		return cached;
	}

	@Override
	public void receiveMessage(Message msg, MessageEndPoint receiver) {
		if (msg.getName().equals("hbtResponse")) {
			//Swap cached's contents with the responses.
			cached.getData().clear();
			for (HBTTag tag : msg.getData()) {
				cached.addTag(tag);
			}
		}
	}

	@Override
	public String getReceiverName() {
		return "_DMR"+Integer.toHexString(this.hashCode());
	}

	@Override
	public void fromHBT(HBTCompound tag) {
	}

	@Override
	public HBTCompound toHBT(boolean msg) {
		return null;
	}
}