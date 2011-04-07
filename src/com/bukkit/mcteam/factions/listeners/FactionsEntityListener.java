package com.bukkit.mcteam.factions.listeners;

import java.text.MessageFormat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.Conf;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.FPlayer;
import com.bukkit.mcteam.factions.struct.Relation;
import com.bukkit.mcteam.util.EntityUtil;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class FactionsEntityListener extends EntityListener {
	
	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		
		Entity entity = event.getEntity();
		
		if (!(entity instanceof Monster)) {
			event.getDrops().clear();
			return;
		}		
		
		if (!(entity instanceof Player)) {
			return;
		}
	
		Player player = (Player) entity;
		FPlayer fplayer = FPlayer.get(player);

		if (fplayer.getLastDamangedBy() instanceof Player) {
			
			Entity eld = fplayer.getLastDamangedBy();
			if (eld != null) {
				
				FPlayer e = FPlayer.get((Player)eld);
				
				Account victimAccount = iConomy.getBank()
					.getAccount(fplayer.getName());
				
				Account enemyAccount = iConomy.getBank()
					.getAccount(e.getName());
				
				if (victimAccount.getBalance() > 0) {
					double cost = (int)(victimAccount.getBalance() * 0.05);
					
					victimAccount.subtract(cost);
					enemyAccount.add(cost);
					
					player.getServer().broadcastMessage(ChatColor.DARK_GRAY + e.getName() + " stole $" + cost + " from " + fplayer.getName());
				}
				
				if (fplayer.getPower() > 0) {
					e.addPower(Conf.powerPerDeath);
					e.sendMessage("You stole " + Conf.powerPerDeath + " power from " + fplayer.getName());
				}
				
				fplayer.setLastDamangedBy(null);
			}
		}
		
		fplayer.onDeath();
		fplayer.sendMessage("Your power is now "+fplayer.getPowerRounded()+" / "+fplayer.getPowerMaxRounded());
	}
	
	/**
	 * Who can I hurt?
	 * I can never hurt members or allies.
	 * I can always hurt enemies.
	 * I can hurt neutrals as long as they are outside their own territory.
	 */
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if ( event.isCancelled()) {
			return;
		}
		
		if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent)event;
                        
            if ( ! this.canDamagerHurtDamagee(sub)) {
    			event.setCancelled(true);
    		}
            
            if ((event.getEntity() instanceof Player)) {
    			Player p = (Player)event.getEntity();
    			FPlayer.get(p).setLastDamangedBy(sub.getDamager());
    		}
            
        } else if (event instanceof EntityDamageByProjectileEvent) {
        	EntityDamageByProjectileEvent sub = (EntityDamageByProjectileEvent)event;
        	
            if ( ! this.canDamagerHurtDamagee(sub)) {
    			event.setCancelled(true);
    		}
            
            if ((event.getEntity() instanceof Player)) {
    			Player p = (Player)event.getEntity();
    			FPlayer.get(p).setLastDamangedBy(sub.getDamager());
    		}
        }
	}

	
	// TODO what happens with the creeper or fireball then? Must we delete them manually?
	@Override
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if ( event.isCancelled()) {
			return;
		}
		
		if (event.getEntity() instanceof LivingEntity)
			event.setCancelled(true);
		/*
		Faction faction = Board.getFactionAt(new FLocation(event.getLocation()));
		
		// Explosions may happen in the wilderness
		if (faction.isNone()) {
			return;
		}
		
		if ((Conf.territoryBlockCreepers || faction.isSafeZone()) && event.getEntity() instanceof Creeper) {
			// creeper which might need prevention, if inside faction territory
			event.setCancelled(true);
		} else if ((Conf.territoryBlockFireballs || faction.isSafeZone()) && event.getEntity() instanceof Fireball) {
			// ghast fireball which might need prevention, if inside faction territory
			event.setCancelled(true);
		}*/
	}

	public boolean canDamagerHurtDamagee(EntityDamageByEntityEvent sub) {
		Entity damager = sub.getDamager();
		Entity damagee = sub.getEntity();
		int damage = sub.getDamage();
		
		if ( ! (damagee instanceof Player)) {
			return true;
		}
		
		FPlayer defender = FPlayer.get((Player)damagee);
		
		// Players can not take attack damage in a SafeZone
		if (Board.getFactionAt(new FLocation(defender)).isSafeZone()) {
			if (damager instanceof Player) {
				FPlayer attacker = FPlayer.get((Player)damager);
				attacker.sendMessage("You cant hurt other players in a SafeZone.");
				defender.sendMessage(attacker.getNameAndRelevant(defender)+Conf.colorSystem+" tried to hurt you.");
			}
			return false;
		}
		
		if ( ! (damager instanceof Player)) {
			return true;
		}
		
		FPlayer attacker = FPlayer.get((Player)damager);
		Relation relation = defender.getRelation(attacker);
		
		// Players without faction may be hurt anywhere
		if (defender.getFaction().isNone()) {
			return true;
		}
		
		// You can never hurt faction members or allies
		if (relation == Relation.MEMBER || relation == Relation.ALLY) {
			attacker.sendMessage(Conf.colorSystem+"You can't hurt "+defender.getNameAndRelevant(attacker));
			return false;
		}
		
		// You can not hurt neutrals in their own territory.
		if (relation == Relation.NEUTRAL && defender.isInOwnTerritory()) {
			attacker.sendMessage(Conf.colorSystem+"You can't hurt "+relation.getColor()+defender.getNameAndRelevant(attacker)+Conf.colorSystem+" in their own territory.");
			defender.sendMessage(attacker.getNameAndRelevant(defender)+Conf.colorSystem+" tried to hurt you.");
			return false;
		}
		
		// Damage will be dealt. However check if the damage should be reduced.
		if (defender.isInOwnTerritory() && Conf.territoryShieldFactor > 0) {
			int newDamage = (int)Math.ceil(damage * (1D - Conf.territoryShieldFactor));
			sub.setDamage(newDamage);
			
			// Send message
		    String perc = MessageFormat.format("{0,number,#%}", (Conf.territoryShieldFactor)); // TODO does this display correctly??
		    defender.sendMessage("Enemy damage reduced by "+ChatColor.RED+perc+Conf.colorSystem+".");
		}
		
		return true;
	}
	
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (Conf.safeZoneNerfedCreatureTypes.contains(event.getCreatureType()) && Board.getFactionAt(new FLocation(event.getLocation())).isSafeZone()) {
			event.setCancelled(true);
		}
	}
	
	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		// if there is a target
		Entity target = event.getTarget();
		if (target == null) {
			return;
		}
		
		// We are interested in blocking targeting for certain mobs:
		if ( ! Conf.safeZoneNerfedCreatureTypes.contains(EntityUtil.creatureTypeFromEntity(event.getEntity()))) {
			return;
		}
		
		// in case the target is in a safe zone.
		if (Board.getFactionAt(new FLocation(target.getLocation())).isSafeZone()) {
			event.setCancelled(true);
		}
	}
}
