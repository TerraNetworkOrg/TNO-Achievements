package com.nidefawl.Achievements;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.entity.Player;


public class AchConditionals {
	private boolean empty;
	protected ArrayList<String> condList = new ArrayList<String>();

	static final Logger log = Logger.getLogger("Minecraft");

	AchConditionals(Achievements plugin, String conditions) {
		this.empty = true;
		if (conditions == null)
			return;

		String[] split = conditions.split(";");
		for (String c : split) {
			c = c.replaceAll("<semicolon>", ":");
			if (c.length() <= 1)
				continue;
			this.condList.add(c);
			this.empty = false;
		}
	}

	public boolean isEmpty() {
		return this.empty;
	}

	public boolean meets(Achievements plugin, Player p, PlayerAchievement pa) {
		if (isEmpty())
			return true;

		boolean meets = false;

		for (String c : condList) {
			String cond;
			boolean not = false;
			if (c.startsWith("!")) {
				not = true;
				cond = c.substring(1);
			} else {
				cond = c;
			}
			if(cond.contains("group")&&cond.split(" ").length>1) {
				String groupName = cond.split(" ")[1];
				if(not && (!plugin.Stats().permission.playerInGroup(p, groupName))) {
					meets = true;
					continue;
				}
				if(!not && (plugin.Stats().permission.playerInGroup(p, groupName))) {
					meets = true;
					continue;
				}
			}
			if(cond.contains("permission")&&cond.split(" ").length>1) {
				String permission = cond.split(" ")[1];
				if(not && (!plugin.Stats().permission.has(p, permission))) {
					meets = true;
					continue;
				}
				if(!not && (plugin.Stats().permission.has(p, permission))) {
					meets = true;
					continue;
				}
			}
			AchievementListData ach = plugin.getAchievement(cond);
			if (ach == null || pa == null)
				continue;

			if (!not && pa.hasAchievement(ach)) {
				meets = true;
				continue;
			}
			if (not && !pa.hasAchievement(ach)) {
				meets = true;
				continue;
			}

			return false;
		}

		return meets;
	}

	@Override
	public String toString() {
		boolean second = false;

		if (isEmpty())
			return "";

		String ret = "";
		for (String s : condList) {
			if (second)
				ret = ret + ";";
			ret = ret + s;
			second = true;
		}
		return ret;
	}
}