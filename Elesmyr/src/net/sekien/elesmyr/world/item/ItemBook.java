/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr.world.item;

import net.sekien.elesmyr.msgsys.Message;
import net.sekien.elesmyr.msgsys.MessageSystem;
import net.sekien.elesmyr.player.InventoryEntry;
import net.sekien.elesmyr.system.GameServer;
import net.sekien.elesmyr.util.BookParser;
import net.sekien.elesmyr.world.entity.EntityPlayer;
import net.sekien.hbt.HBTTools;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: matt Date: 7/03/13 Time: 4:41 PM To change this template use File | Settings | File
 * Templates.
 */
public class ItemBook extends Item {

@Override
public String getType() { return "Books"; }

@Override
public boolean onUse(GameServer receiver, EntityPlayer player, InventoryEntry entry) {
	ArrayList<String> pages = BookParser.parseBook(extd.getString("extd", "testing"));
	for (String s : pages)
		MessageSystem.sendClient(null, receiver.getPlayerConnection(player), new Message("CLIENT.book", HBTTools.msgString("page", s)), false);
	return false;
}

@Override
public String getName(InventoryEntry entry) { return "book."+extd.getString("extd", "testing"); }
}
