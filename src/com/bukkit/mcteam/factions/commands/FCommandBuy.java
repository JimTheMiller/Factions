package com.bukkit.mcteam.factions.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.Faction;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class FCommandBuy extends FBaseCommand  {
	
	public FCommandBuy() {
		aliases.add("buy");
		aliases.add("b");
		helpDescription = "*NEW* sell items in the safe zone.";
	}
	
	public void perform() {
		
		Map<Material, Double> prices = new HashMap<Material, Double>();
		
		prices.put(Material.TNT, 100.0);
		prices.put(Material.STRING, 59.99);
		
		/*prices.put(Material.WATER, 100.0);
		prices.put(Material.STATIONARY_WATER, 100.0);
		
		prices.put(Material.LAVA, 100.0);
		prices.put(Material.STATIONARY_LAVA, 100.0);
		
		prices.put(Material.GOLD_BLOCK, 9000.0);
		
		prices.put(Material.IRON_INGOT, 100.0);
		prices.put(Material.IRON_ORE, 100.0);
		prices.put(Material.IRON_BLOCK, 900.0);
		
		prices.put(Material.GOLDEN_APPLE, -1.0);
		
		prices.put(Material.SPONGE, -1.0);
		
		prices.put(Material.OBSIDIAN, -1.0);
		prices.put(Material.TNT, -1.0);
		
		prices.put(Material.GOLD_ORE, 1000.0);
		prices.put(Material.GOLD_PICKAXE, 3000.0);
		prices.put(Material.GOLD_INGOT, 1000.0);
		*/
		
		
		FLocation flocation = new FLocation(me);
		Faction faction = Board.getFactionAt(flocation);
		
		if (!faction.isSafeZone()) { 
			me.sendMessage("[shop] You can only use shops in the safe zone.");
			return;
		}
		
		String command =  parameters.get(0);
		
		Material m = Material.matchMaterial(command);
		
		if (m != null) {
			int n = 1;
			
			try
			{
				if (parameters.size() > 1)
					n = Math.abs(Integer.parseInt(parameters.get(1)));
			} catch (NumberFormatException ex) {
			}
			
			double price = 10;
			
			if (prices.containsKey(m)) {
				price = prices.get(m);
			} else {
				return;
			}
				

			if (price == -1)
				return;
			
			Account account = iConomy.getBank()
				.getAccount(me.getName());
		
			if (account.getBalance() < price * n)
				n = (int)Math.floor(account.getBalance() / price);
			
			double total = n * price;
			account.subtract(total);
			
			me.getPlayer()
				.getServer()
				.broadcastMessage(me.getName() + " bought " + n + " " + m.name() + " for " + total);
			
			while (n > 0)
			{
				int stackSize = n > 64 ? 64 : n;
				
				ItemStack s = new ItemStack(m, stackSize);
				me
				.getPlayer()
				.getWorld()
				.dropItem(me.getPlayer().getLocation(), s);
				
				n -= 64;
			}

		}  else if (command.equalsIgnoreCase("price") && parameters.size() > 1) {
			Material priceM = Material.matchMaterial(parameters.get(1));
			if (priceM != null) {

				double price = 10;
				if (m == Material.TNT)
					price = 100;
				
				me.sendMessage("[shop] " + priceM.toString() + " costs " + price); 
				
				
			} else {
				me.sendMessage("[shop] No such product called " + parameters.get(1));
			}
			
		} else if (command.equalsIgnoreCase("list")) {
			
			me.sendMessage("[shop] feature not done yet :D");
			
		} else {
			me.sendMessage("[shop] No such product called " + command);
		}
			
		
		//Material m = Material.getMaterial(parameters.get(1));
		
		
		
		
		
		/*
		if (command.equalsIgnoreCase("price"))
			Price(ItemNameToId(parameters.get(1)));
		else if (command.equalsIgnoreCase("list"))
			List(ItemNameToId(parameters.get(1)));
		else
			Buy(ItemNameToId(parameters.get(1)));
		*/
		// buy price name
		// buy list [page]
		// buy item [number]
	}
	
	/*
	private void Price(Material material) {
	}
	
	private void List(int page) {
	}
	
	private void Buy(Material material, int page) { 
	}*/
	
}
