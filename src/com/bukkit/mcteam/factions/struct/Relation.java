package com.bukkit.mcteam.factions.struct;

import org.bukkit.ChatColor;

import com.bukkit.mcteam.factions.Conf;

public enum Relation {
	MEMBER(3, "member"),
	ALLY(2, "ally"),
	NEUTRAL(1, "neutral"),
	ENEMY(0, "enemy");
	
	public final int value;
	public final String nicename;
	
	private Relation(final int value, final String nicename) {
        this.value = value;
        this.nicename = nicename;
    }
	
	@Override
	public String toString() {
		return this.nicename;
	}
	
	public ChatColor getColor() {
		if (this == Relation.MEMBER) {
			return Conf.colorMember;
		} else if (this == Relation.ALLY) {
			return Conf.colorAlly;
		} else if (this == Relation.NEUTRAL) {
			return Conf.colorNeutral;
		} else { //if (relation == FactionRelation.ENEMY) {
			return Conf.colorEnemy;
		}
	}
	
	public String getSymbol() {
		if (this == Relation.MEMBER) {
			return "F";
		} else if (this == Relation.ALLY) {
			return "A";
		} else if (this == Relation.NEUTRAL) {
			return "N";
		} else { //if (relation == FactionRelation.ENEMY) {
			return "E";
		}
	}
}
