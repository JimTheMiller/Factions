package com.bukkit.mcteam.factions.listeners;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.Claim;
import com.bukkit.mcteam.factions.Conf;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.FPlayer;
import com.bukkit.mcteam.factions.Faction;
import com.bukkit.mcteam.factions.Factions;
import com.bukkit.mcteam.factions.util.TextUtil;


public class FactionsPlayerListener extends PlayerListener {

	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		if ((event.getMessage().startsWith(Factions.instance.getBaseCommand()+" ") || event.getMessage().equals(Factions.instance.getBaseCommand())) && Conf.allowNoSlashCommand) {
			List<String> parameters = TextUtil.split(event.getMessage().trim());
			parameters.remove(0);
			CommandSender sender = event.getPlayer();			
			Factions.instance.handleCommand(sender, parameters);
			event.setCancelled(true);
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		Player talkingPlayer = event.getPlayer();
		String msg = event.getMessage();
		
		// ... it was not a command. This means that it is a chat message!
		FPlayer me = FPlayer.get(talkingPlayer);
		
		// Is it a faction chat message?
		if (me.isFactionChatting()) {
			String message = String.format(Conf.factionChatFormat, me.getNameAndRelevant(me), msg);
			me.getFaction().sendMessage(message);
			Logger.getLogger("Minecraft").info("FactionChat "+me.getFaction().getTag()+": "+message);
			event.setCancelled(true);
			return;
		}
		
		if (me.isAllyChatting()) {
			Faction myFaction = me.getFaction();
			String message = String.format(ChatColor.BLUE + myFaction.getTag() + " " + Conf.allyChatFormat, me.getNameAndRelevant(me), msg);
			me.getFaction().sendMessageIncludingAllies(message);
			Logger.getLogger("Minecraft").info("AllyChat "+me.getFaction().getTag()+": "+message);
			event.setCancelled(true);
			return;
		}
		
		// Are we to insert the Faction tag into the format?
		// If we are not to insert it - we are done.
		if ( ! Conf.chatTagEnabled) {
			return;
		}
		
		String formatStart = event.getFormat().substring(0, Conf.chatTagInsertIndex);
		String formatEnd = event.getFormat().substring(Conf.chatTagInsertIndex);
		
		String nonColoredMsgFormat = formatStart + me.getChatTag() + formatEnd;
		
		// Relation Colored?
		if (Conf.chatTagRelationColored) {
			// We must choke the standard message and send out individual messages to all players
			// Why? Because the relations will differ.
			event.setCancelled(true);
			
			for (Player listeningPlayer : Factions.instance.getServer().getOnlinePlayers()) {
				FPlayer you = FPlayer.get(listeningPlayer);
				
				if (you.isMapAutoUpdating())
					continue;
				
				String yourFormat = formatStart + me.getChatTag(you) + formatEnd;
				listeningPlayer.sendMessage(String.format(yourFormat, talkingPlayer.getDisplayName(), msg));
			}
			
			// Write to the log... We will write the non colored message.
			String nonColoredMsg = ChatColor.stripColor(String.format(nonColoredMsgFormat, talkingPlayer.getDisplayName(), msg));
			Logger.getLogger("Minecraft").info(nonColoredMsg);
		} else {
			// No relation color.
			event.setFormat(nonColoredMsgFormat);
		}
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Make sure that all online players do have a fplayer.
		FPlayer me = FPlayer.get(event.getPlayer());
		me.justRespawned = false;
		
		// Update the lastLoginTime for this fplayer
		me.setLastLoginTime(System.currentTimeMillis());
		
		// Run the member auto kick routine. Twice to get to the admins...
		FPlayer.autoLeaveOnInactivityRoutine();
		FPlayer.autoLeaveOnInactivityRoutine();
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		FPlayer me = FPlayer.get(event.getPlayer());
		
		/*if (me.justRespawned)
		{
			me.justRespawned = false;
			event.getPlayer().kickPlayer("You were killed, rejoin.");
		}*/
		// Did we change coord?
		FLocation from = me.getLastStoodAt();
		FLocation to = new FLocation(event.getTo());
		
		if (from.equals(to)) {
			return;
		}
		
		// Yes we did change coord (:
		
		me.setLastStoodAt(to);
		
		if (me.isMapAutoUpdating()) {
			Player player = me.getPlayer();
			if (player != null)
				me.sendMessage(Board.getMap(me.getFaction(), to, player.getLocation().getYaw()));
		} else {
			me.sendFactionHereMessage();
		}
	}

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;  // only interested on right-clicks on blocks, whether player is using an item or interacting with a block
		}

		Block block = event.getClickedBlock();
		Player player = event.getPlayer();

		if ( ! canPlayerUseRightclickBlock(player, block)) {
			event.setCancelled(true);
			return;
		}
		// this check below might no longer be needed... bucket detection is now necessarily handled separately in onPlayerBucketXXX() events, and
		// Flint&Steel is somehow detected before this in onBlockPlace(), and that's currently it for the default territoryDenyUseageMaterials
		if ( ! this.playerCanUseItemHere(player, block, event.getMaterial())) {
			event.setCancelled(true);
			return;
		}
	}

	public boolean playerCanUseItemHere(Player player, Block block, Material material) {

		if ( ! Conf.territoryDenyUseageMaterials.contains(material)) {
			return true; // Item isn't one we're preventing.
		}

		FLocation location = new FLocation(block);
		Faction otherFaction = Board.getFactionAt(location);

		if (otherFaction.isNone()) {
			return true; // This is not faction territory. Use whatever you like here.
		}

		FPlayer me = FPlayer.get(player);
		
		if (otherFaction.isSafeZone()) {
			if (Factions.hasPermManageSafeZone(player)) {
				return true;
			}
			me.sendMessage("You can't use "+TextUtil.getMaterialName(material)+" in a safe zone.");
			return false;
		}
		
		Faction myFaction = me.getFaction();

		Claim claim = Board.getClaimAt(location);
		
		if (claim == null)
			return true;
		
		if (claim.canInteract(me)) {
			return true;
		} 
		
		me.sendMessage("You can't use "+TextUtil.getMaterialName(material)+" in the territory of "+otherFaction.getTag(myFaction));
		return false;
	}

	public boolean canPlayerUseRightclickBlock(Player player, Block block) {
		Material material = block.getType();

		// We only care about some material types.
		if ( ! Conf.territoryProtectedMaterials.contains(material)) {
			return true;
		}

		FPlayer me = FPlayer.get(player);
		Faction myFaction = me.getFaction();
		
		FLocation location = new FLocation(block);
		Faction otherFaction = Board.getFactionAt(location);
		
		if (!otherFaction.isNormal())
			return true;
		
		Claim claim = Board.getClaimAt(location);
		
		if (claim == null)
			return true;
		
		if (claim.canInteract(me)) {
			return true;
		} 
		
		me.sendMessage("You can't use "+TextUtil.getMaterialName(material)+" in the territory of "+otherFaction.getTag(myFaction));
		return false;
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Factions.log("' " + event.getPlayer().getName() + "' has just respawned");
		Player player = event.getPlayer();
		FPlayer me = FPlayer.get(player);
		me.setLastDamangedBy(null);
		me.justRespawned = true;
		Location home = me.getFaction().getHome();
		if (Conf.homesEnabled && Conf.homesTeleportToOnDeath && home != null) {
			event.setRespawnLocation(home);
		}
	}

	// For some reason onPlayerInteract() sometimes misses bucket events depending on distance (something like 2-3 blocks away isn't detected),
	// but these separate bucket events below always fire without fail
	@Override
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Block block = event.getBlockClicked();
		Player player = event.getPlayer();

		if ( ! this.playerCanUseItemHere(player, block, event.getBucket())) {
			event.setCancelled(true);
			return;
		}
	}
	
	@Override
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Block block = event.getBlockClicked();
		Player player = event.getPlayer();

		if ( ! this.playerCanUseItemHere(player, block, event.getBucket())) {
			event.setCancelled(true);
			return;
		}
	}
	
	@Override
	public void onPlayerQuit (PlayerQuitEvent event) {
		if ((System.currentTimeMillis() - FPlayer.get(event.getPlayer()).getLastDamagedTime()) < 15000) {
			Logger.getLogger("Minecraft").info("Player ran from battle: " + event.getPlayer().getName());
			Factions.instance.startLogOffTimer(event.getPlayer().getName());
		} else {
			Logger.getLogger("Minecraft").info("Player just quit");
		}
	}
}
