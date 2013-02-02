package net.halitesoft.lote.ui;

import java.util.ArrayList;
import java.util.Map.Entry;

import net.halitesoft.lote.Message;
import net.halitesoft.lote.MessageReceiver;
import net.halitesoft.lote.MessageSystem;
import net.halitesoft.lote.system.Camera;
import net.halitesoft.lote.system.GameClient;
import net.halitesoft.lote.system.Main;
import net.halitesoft.lote.system.PlayerData;
import net.halitesoft.lote.util.HashmapLoader;
import net.halitesoft.lote.world.Region;
import net.halitesoft.lote.world.entity.EntityPlayer;
import net.halitesoft.lote.world.item.Item;
import net.halitesoft.lote.world.item.ItemFactory;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class CraftUI implements UserInterface {
	
	public static class Craftable {
		private String result;
		private String[] ingredients;
		public Craftable(String result, String recipe) {
			this.result=result;
			this.ingredients=recipe.split("\\|");
		}
		
		public boolean isCraftable(ArrayList<PlayerData.InventoryEntry> inv) {
			for (String s : ingredients) {
				boolean matched = false;
				String[] parts = parseIng(s);
				for (PlayerData.InventoryEntry ie : inv) {
					if (ie.getItem().name.equals(parts[1]) && ie.getExtd().equals(parts[2]) && ie.getCount()>=Integer.parseInt(parts[0])) {
						matched=true;
						break;
					}
				}
				if (!matched)
					return false;
			}
			return true;
		}

		private String[] parseIng(String s) {
			String[] ret = new String[] {"1","",""};
			if (s.contains("*")) {
				ret[0] = s.split("\\*")[0];
				s=s.split("\\*")[1];
			}
			if (s.contains(":")) {
				ret[1]=s.split(":")[0];
				ret[2]=s.split(":")[1];
			} else {
				ret[1]=s;
			}
			return ret;
		}
		
		public String toString() {
			String out = result;
			for (String s : ingredients) {
				String[] parts = parseIng(s);
				out = out+" "+parts[0]+"x "+parts[1]+(parts[2].equals("")?"":": "+parts[2]);
			}
			return out;
		}
		
		public void addToPDAT(PlayerData pdat,Region r, String ent) {
			for (String s : ingredients) {
				boolean matched = false;
				String[] parts = parseIng(s);
				for (PlayerData.InventoryEntry ie : pdat.inventory) {
					if (ie.getItem().name.equals(parts[1]) && ie.getExtd().equals(parts[2]) && ie.getCount()>=Integer.parseInt(parts[0])) {
						matched=true;
						for (int i=0;i<Integer.parseInt(parts[0]);i++)
							pdat.removeItem(pdat.inventory.indexOf(ie), r, ent);
						break;
					}
				}
				if (!matched)
					return;
			}
			pdat.put(ItemFactory.getItem(result), "", r, ent);
		}
	}

	private static ArrayList<Craftable> recipes;
	
	static {
		recipes = new ArrayList<Craftable>();
		for (Entry<String,String> e : HashmapLoader.readHashmap("data/craft_def").entrySet())
			recipes.add(new Craftable(e.getKey(),e.getValue()));
	}

	private int isel = 0;
	private int smax = 1;

	@Override
	public void ctor(String extd) {
		
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg,
			MessageReceiver receiver) throws SlickException {
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g,
			Camera cam, GameClient receiver) throws SlickException {
		int xoff = (Main.INTERNAL_RESX/2)-320;
		
		Main.font.drawString(xoff+77,17,"Crafting");
		
		int i=0;
		ArrayList<PlayerData.InventoryEntry> inv = ((EntityPlayer) receiver.player.region.entities.get(receiver.player.entid)).pdat.inventory;
		for (Craftable c : recipes) {
			if (c.isCraftable(inv)) {
				if (i==isel)
					g.fillRect(xoff+67, 116+i*38, 506, 36);
				//iei.spr.draw(xoff+78,120+i*38);
				Main.font.drawString(xoff+117,128+i*38, c.toString());
				//Main.font.drawString(xoff+450,128+i*38, ""+ie.getCount());
				//Main.font.drawString(526,128+i*40,"$"+ie.getValue()); //TODO: Value thingies
				//Main.font.drawString(xoff+526,128+i*38, ie.getExtd());
				
				i++;
			}
		}
		smax=i;
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, GameClient receiver) {
		Input in = gc.getInput();
		if (in.isKeyPressed(Input.KEY_UP))
			if (isel>0)
				isel--;
		if (in.isKeyPressed(Input.KEY_DOWN))
			if (isel<smax-1)
				isel++;
		if (in.isKeyPressed(Input.KEY_X)) {
			ArrayList<PlayerData.InventoryEntry> inv = ((EntityPlayer) receiver.player.region.entities.get(receiver.player.entid)).pdat.inventory;
			int i=0;
			int iv=-1;
			for (Craftable c : recipes) {
				if (c.isCraftable(inv))
					iv++;
				if (iv==isel) {
					System.out.println("Craft send");
					MessageSystem.sendServer(null,new Message(receiver.player.region.name+"."+receiver.player.entid+".craftItem",""+i),false);
				}
				i++;
			}
		}
		if (isel>smax-1)
			isel=smax-1;
	}

	@Override
	public boolean blockUpdates() {
		return true;
	}
	
	public static Craftable getRecipe(int index) {
		System.out.println(index);
		return recipes.get(index);
	}
}
