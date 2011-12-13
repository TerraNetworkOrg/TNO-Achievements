package com.nidefawl.Achievements;

import java.util.Iterator;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class PlayerAchievementFile extends PlayerAchievement {
	private String location;
	public PlayerAchievementFile(String directory, String name) {
		super(name);
		this.location = directory + File.separator + name + ".txt";
	}

	@Override
	public void save() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(location));
			writer.write("# " + location);
			writer.newLine();

			Iterator<String> achIter = achievements.keySet().iterator();
			while (achIter.hasNext()) {
				String achName = achIter.next();
				Achievement ach = achievements.get(achName);
				writer.write(achName + ":" + ach.getCount());
				writer.newLine();
			}
		} catch (Exception e) {
			Achievements.LogError("Exception while creating " + location +" "+ e);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				Achievements.LogError("Exception while closing " + location +" "+ e);
			}
		}
	}

	@Override
	public void load() {
		if (!new File(location).exists())
			return;

		try {
			Scanner scanner = new Scanner(new File(location));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith("#") || line.equals(""))
					continue;

				String[] split = line.split(":");
				if (split.length < 1) {
					Achievements.LogError("Malformed line (" + line + ") in " + location);
					continue;
				}

				int count = 1;
				if (split.length >= 2)
					count = Integer.parseInt(split[1]);

				Achievement ach = newAchievement(split[0]);
				ach.put(count);
			}
			scanner.close();
		} catch (Exception ex) {
			Achievements.LogError("Exception	while	reading " + location +" "+ ex);
		}
	}
}