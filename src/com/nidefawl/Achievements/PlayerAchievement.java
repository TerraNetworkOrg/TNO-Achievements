package com.nidefawl.Achievements;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

public abstract class PlayerAchievement {
	public String name;
	public HashMap<String, Achievement> achievements;
	static final Logger log = Logger.getLogger("Minecraft");

	public PlayerAchievement(String name) {
		this.name = name;
		this.achievements = new HashMap<String, Achievement>();
	}

	public boolean isEmpty() {
		return achievements.isEmpty();
	}

	public Achievement get(String name) {
		return achievements.get(name);
	}

	public String getName(int index) {
		if (index > achievements.size() || index < 0)
			return null;
		for (String a : achievements.keySet()) {
			if (index == 0)
				return a;
			index--;
		}
		return null;
	}

	public Achievement newAchievement(String name) {
		Achievement achievement = new Achievement();
		achievements.put(name, achievement);
		return achievement;
	}

	public void award(String achievement) {
		Achievement ach;
		if (!achievements.containsKey(achievement))
			ach = newAchievement(achievement);
		else
			ach = achievements.get(achievement);

		ach.incrementCount();
	}

	public void put(String achievement, int count) {
		Achievement ach;
		if (!achievements.containsKey(achievement))
			ach = newAchievement(achievement);
		else
			ach = achievements.get(achievement);

		ach.put(count);
	}

	public boolean hasAchievement(AchievementListData ach) {
		if (achievements.containsKey(ach.getName()))
			return true;
		return false;
	}

	public void copy(PlayerAchievement from) {
		this.name = from.name;
		this.achievements = new HashMap<String, Achievement>(from.achievements);
	}

	Iterator<String> iterator() {
		return achievements.keySet().iterator();
	}

	public int size() {
		if (this.achievements == null)
			return 0;
		return this.achievements.size();
	}

	public abstract void save();

	public abstract void load();
}