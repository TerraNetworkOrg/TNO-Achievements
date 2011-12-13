package com.nidefawl.Achievements;

import java.util.logging.Logger;

public class Achievement {
	public boolean modified = false;
	private int count;
	static final Logger log = Logger.getLogger("Minecraft");

	public Achievement() {
		count = 0;
		modified = false;
	}

	public int getCount() {
		return count;
	}

	public void put(int value) {
		count = value;
		modified = true;
	}

	public void incrementCount() {
		count++;
		modified = true;
	}
}