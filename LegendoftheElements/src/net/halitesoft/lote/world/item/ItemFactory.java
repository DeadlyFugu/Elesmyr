package net.halitesoft.lote.world.item;


import java.util.HashMap;
import java.util.Map.Entry;

import net.halitesoft.lote.util.HashmapLoader;

public class ItemFactory {
	private static HashMap<String,Item> items = null;
	public static void init() {
		if (items!=null)
			return;
		items = new HashMap<String,Item>();
		HashMap<String,String> item_str = HashmapLoader.readHashmap("data/item_def");
		for (Entry<String, String> e : item_str.entrySet()) {
			items.put(e.getKey(), parseItem(e.getKey(),e.getValue()));
		}
	}
	private static Item parseItem(String name, String str) {
		String[] parts = str.split(",",3);
		if (parts.length == 3)
			try {
				Item i = (Item) Class.forName("net.halitesoft.lote.world.item."+parts[0]).newInstance();
				i.ctor(name,parts[1],parts[2]);
				return i;
			} catch (Exception e) {
				e.printStackTrace();
			}
		return new Item().ctor("Null","null","");
	}
	
	public static Item getItem(String str) {
		return items.get(str);
	}
}