package com.bukkit.mcteam.factions;

import com.bukkit.mcteam.factions.struct.Relation;
import com.bukkit.mcteam.factions.struct.Role;

public class Claim {

	public Claim(int factionId, ClaimAccess access) {
		this.factionId = factionId;
		this.access = access;
	}
	
	public Claim() {
	}
	
	public int factionId;
	public ClaimAccess access;
	
	public boolean canInteract(FPlayer me) {
		
		Faction otherFaction = Faction.get(factionId);
		Faction myFaction = me.getFaction();
		
		if (this.access == ClaimAccess.FACTION) {
			
			if (myFaction != otherFaction) {
				me.sendMessage("You can't do this in the territory of " + otherFaction.getTag(myFaction));
				return false;
			}
			
		} else if (this.access == ClaimAccess.ALLY) {
			
			if (myFaction != otherFaction) {
				Relation relation = otherFaction.getRelation(me);
				if (relation != Relation.ALLY)
				{
					me.sendMessage("You can't do this in the territory of " + otherFaction.getTag(myFaction));				
					return false;
				}	
			} else {
				return true;
			}
			
		} else if (this.access == ClaimAccess.MOD) {
			
			if (myFaction != otherFaction) {
				me.sendMessage("You can't do this in the territory of " + otherFaction.getTag(myFaction));
				return false;
			}
			
			if (me.getRole() == Role.NORMAL)
				return false;
		}
		
		return true;
		
	}
}