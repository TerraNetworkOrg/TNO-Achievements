package com.nidefawl.Achievements;

import com.nidefawl.Achievements.Commands.AchCommandHandler;

public class AchievementListData {
	private boolean enabled = false;
	private String name;
	public String category;
	public String key;
	public int value;
	private String description;
	private int maxawards;
	public AchCommandHandler commands;
	public AchConditionals conditions;
	Achievements plugin;

	AchievementListData(Achievements plugin, int enabled, String name, int maxawards, String category, String key, int value, String description, String commands, String conditionals) {
		this.plugin = plugin;
		if (enabled > 0)
			this.enabled = true;
		else
			this.enabled = false;
		this.name = name;
		this.category = category;
		this.key = key;
		this.value = value;
		this.description = description;
		this.maxawards = maxawards;
		this.commands = new AchCommandHandler(plugin, commands);
		this.conditions = new AchConditionals(plugin, conditionals);

	}

	public String getName() {
		return this.name;
	}

	public String getCategory() {
		return this.category;
	}

	public String getKey() {
		return this.key;
	}

	public int getValue() {
		return this.value;
	}

	public String getDescription() {
		return this.description;
	}

	public int getMaxawards() {
		return this.maxawards;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	protected void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}