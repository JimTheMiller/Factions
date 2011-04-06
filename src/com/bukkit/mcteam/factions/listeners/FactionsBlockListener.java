package com.bukkit.mcteam.factions.listeners;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
//import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.Claim;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.FPlayer;
import com.bukkit.mcteam.factions.Faction;
import com.bukkit.mcteam.factions.Factions;

public class FactionsBlockListener extends BlockListener {
	
	public static Map<Material, Double> WoodDropRates = new LinkedHashMap<Material, Double>();
	public static Map<Material, Double> StoneDropRates = new LinkedHashMap<Material, Double>();
	public static Map<Material, Double> IronDropRates = new LinkedHashMap<Material, Double>();
	public static Map<Material, Double> DiamondDropRates = new LinkedHashMap<Material, Double>();
	public static Map<Material, Double> GoldDropRates = new LinkedHashMap<Material, Double>();
	
	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		
		if (event.isCancelled()) {
			return;
		}
		if (!event.canBuild()) {
			return;
		}
		
		Faction faction = FPlayer.get(event.getPlayer()).getFaction();
		if (faction.isNone()) {
			event.getPlayer().sendMessage(ChatColor.YELLOW + " You cannot interact with the world unless you create or join a faction. /f help for more information. ");
			event.setCancelled(true);
			return;
		}
		
		if ( ! this.playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock(), "build")) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Faction faction = FPlayer.get(event.getPlayer()).getFaction();
		if (faction.isNone()) {
			event.getPlayer().sendMessage(ChatColor.YELLOW + " You cannot interact with the world unless you create or join a faction. /f help for more information. ");
			event.setCancelled(true);
			return;
		}

		if ( ! this.playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock(), "destroy")) {
			event.setCancelled(true);
		}
		
		
		
		if (event.getBlock().getType() == Material.STONE) {
			
			
			Material tool = event.getPlayer().getItemInHand().getType();

			Map<Material, Double> dropRates = null;
			double baseRate = 0.0;
			double pickBonus = 1.0;
			
			if (tool == Material.WOOD_PICKAXE) {
				dropRates = WoodDropRates;
				baseRate = 0.60;
			}
			
			if (tool == Material.STONE_PICKAXE) {
				dropRates = StoneDropRates;
				baseRate = 0.50;
			}
			
			if (tool == Material.IRON_PICKAXE) { 
				dropRates = IronDropRates;
				baseRate = 0.30;
			}

			if (tool == Material.DIAMOND_PICKAXE) {
				dropRates = DiamondDropRates;
				baseRate = 0.20;
			}
			
			if (tool == Material.GOLD_PICKAXE) {
				dropRates = GoldDropRates;
				pickBonus = 3;
				baseRate = 1.00;
			}
						
			if (dropRates == null)
				return;
		
			Random rnd = new Random();
			if (rnd.nextDouble() <= baseRate)
			{
				//Factions.log("" + dropRates.keySet().size());
				Material item = null;
				
				int blockY = event.getBlock().getY();
				
				for(Material m : dropRates.keySet())
				{
					
					if (m == Material.DIAMOND)
						if (blockY > 40)
							continue;
					
					if (m == Material.GOLD_ORE)
						if (blockY > 30)
							continue;
					
					if (m == Material.IRON_ORE)
						if (blockY > 50)
							continue;
						
					double itemR = rnd.nextDouble();
					
					if (itemR <= dropRates.get(m) * pickBonus) {
						item = m;
						break;
					}
				}
				
				if (item != null)
				{
					if (item == Material.DIAMOND) {
						//TODO add bounty
						event.getPlayer().getServer().broadcastMessage(ChatColor.YELLOW + event.getPlayer().getName() + " found a diamond.");
					}
					
					if (item == Material.INK_SACK) {
						ItemStack stack = new MaterialData(Material.INK_SACK, (byte)1).toItemStack(); 
						event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), stack);
					} else {
						ItemStack stack = new ItemStack(item, 1);
						event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), stack);
					}
				}
			}
		}
			
	}

	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (event.getInstaBreak() && ! this.playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock(), "destroy")) {
			event.setCancelled(true);
		}
	}
	
	public boolean playerCanBuildDestroyBlock(Player player, Block block, String action) {
		
		
		FLocation location = new FLocation(block);
		Faction otherFaction = Board.getFactionAt(location);
		
		if (otherFaction.isNone()) {
			return true;
		}
		
		FPlayer me = FPlayer.get(player);
		
		if (otherFaction.isSafeZone()) {
			if (Factions.hasPermManageSafeZone(player)) {
				return true;
			}
			me.sendMessage("You can't "+action+" in a safe zone.");
			return false;
		}
		
		Claim claim = Board.getClaimAt(location);
		
		boolean result = claim.canInteract(me);
		
		return result;
	}
}
