package com.nidefawl.Achievements;

public class AchievementsCheckerTask implements Runnable {
	private Achievements achievements;

	public AchievementsCheckerTask(Achievements plugin) {
		achievements = plugin;
	}

	public void run() {
		achievements.checkAchievements();
	}
}