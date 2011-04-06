package com.bukkit.mcteam.factions.listeners;

/*
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
*/
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

import com.bukkit.mcteam.factions.Factions;

public class FactionsChunkListener extends WorldListener {

	@Override
	public void onChunkLoad(ChunkLoadEvent event) { // only called on gen.
		// remove ore.
		/*
		Chunk chunk = event.getChunk();
		
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 128; y++) {
				for (int z = 0; z < 16; z++) {
					Block block = chunk.getBlock(x, y, z);
					Material m = block.getType();
					boolean replace = false;
					switch(m) {
						case CLAY :
						case IRON_ORE :
						case GOLD_ORE :
						case DIAMOND_ORE :
						case LAPIS_ORE : 
						case COAL_ORE :
							replace = true;
					}
					
					if (replace)
					{
						block.setType(Material.STONE);
					}
				}
			}
		}
		
		event.getWorld().save();*/
	}
	
	@Override
	public void onChunkUnload(ChunkUnloadEvent event) {
		
		if (Factions.instance.isEnabled())
			event.setCancelled(true);
	}
	
}
