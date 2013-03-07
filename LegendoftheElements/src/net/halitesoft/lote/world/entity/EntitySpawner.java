package net.halitesoft.lote.world.entity;

import com.esotericsoftware.minlog.Log;
import net.halitesoft.lote.system.GameServer;
import net.halitesoft.lote.world.Region;

import java.util.Random;

public class EntitySpawner extends Entity {
	
	private Random rand;
	public EntitySpawner() {
		tellClient = false;
		rand = new Random();
	}

	@Override
	public void update(Region region, GameServer receiver) {
		int se = 0;
		for (Entity e : region.entities.values())
			if (e.getClass().getSimpleName().equals(extd.split(",",2)[0]))
				se++;
		if (rand.nextInt((int) Math.ceil(Math.pow(se+1,3)/2))==1) {
			region.addEntityServer(extd.split(",",2)[0]+","+x+","+y+","+extd.split(",",2)[1]);
		}
	}
}
