package net.halitesoft.lote.world.item;

import net.halitesoft.lote.system.GameServer;
import net.halitesoft.lote.world.entity.EntityPlayer;

public class ItemFood extends Item {
	@Override public String getType() { return "Food"; }
	@Override public boolean onUse(GameServer receiver, EntityPlayer player) {
		player.pdat.health+=Integer.parseInt(extd);
		return true;
	};
}