package com.nidefawl.Achievements;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AchPropertiesFile {
	private HashMap<String, PropertiesEntry> map;
	private File file;
	private boolean modified;
	static final Logger log = Logger.getLogger("Minecraft");
	public final String logprefix = Achievements.logprefix;

	public AchPropertiesFile(File file) {
		this.file = file;
		map = new HashMap<String, PropertiesEntry>();
		Scanner scan;
		try {
			if (!file.exists())
				file.createNewFile();
			scan = new Scanner(file);
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (!line.contains("="))
					continue;
				if (line.length() == 0)
					continue;
				if (line.trim().charAt(0) == '#')
					continue;
				int equals = line.indexOf("=");
				int commentIndex = line.length();
				if (line.contains("#")) {
					commentIndex = line.indexOf("#");
				}

				String key = line.substring(0, equals).trim();
				if (key.equals(""))
					continue;
				String value = line.substring(equals + 1, commentIndex).trim();
				String comment = "";
				if (commentIndex < line.length() - 1) {
					comment = line.substring(commentIndex + 1, line.length()).trim();
				}
				map.put(key, new PropertiesEntry(value, comment));
			}
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, logprefix + " Cannot read file " + file.getName());
		} catch (IOException e) {
			log.log(Level.SEVERE, logprefix + " Cannot create file " + file.getName());
		}
	}

	public boolean getBoolean(String key, Boolean defaultValue, String defaultComment) {
		if (map.containsKey(key)) {
			return Boolean.parseBoolean(map.get(key).value);
		} else {
			map.put(key, new PropertiesEntry(defaultValue.toString(), defaultComment));
			modified = true;
			return defaultValue;
		}
	}

	public String getString(String key, String defaultValue, String defaultComment) {
		if (map.containsKey(key)) {
			return map.get(key).value;
		} else {
			map.put(key, new PropertiesEntry(defaultValue.toString(), defaultComment));
			modified = true;
			return defaultValue;
		}
	}

	public int getInt(String key, Integer defaultValue, String defaultComment) {
		if (map.containsKey(key)) {
			try {
				return Integer.parseInt(map.get(key).value);
			} catch (Exception e) {
				log.log(Level.WARNING, logprefix + " Trying to get Integer from " + key + ": " + map.get(key).value);
				return 0;
			}
		} else {
			map.put(key, new PropertiesEntry(defaultValue.toString(), defaultComment));
			modified = true;
			return defaultValue;
		}
	}

	public double getDouble(String key, Double defaultValue, String defaultComment) {
		if (map.containsKey(key)) {
			try {
				return Double.parseDouble(map.get(key).value);
			} catch (Exception e) {
				log.log(Level.WARNING, logprefix + " Trying to get Double from " + key + ": " + map.get(key).value);
				return 0;
			}
		} else {
			map.put(key, new PropertiesEntry(defaultValue.toString(), defaultComment));
			modified = true;
			return defaultValue;
		}
	}

	public void save() {
		if (!modified)
			return;
		BufferedWriter bwriter = null;
		FileWriter fwriter = null;
		try {
			if (!file.exists())
				file.createNewFile();
			fwriter = new FileWriter(file);
			bwriter = new BufferedWriter(fwriter);
			SortedSet<Map.Entry<String, PropertiesEntry>> results = new TreeSet<Map.Entry<String, PropertiesEntry>>(new Comparator<Map.Entry<String, PropertiesEntry>>() {
				@Override
				public int compare(Map.Entry<String, PropertiesEntry> a, Map.Entry<String, PropertiesEntry> b) {
					// int d = a.getValue().compareTo(b.getValue());
					int d = a.getKey().compareTo(b.getKey());
					return d;
				}

			});
			results.addAll(map.entrySet());
			for (Entry<String, PropertiesEntry> entry : results) {
				StringBuilder builder = new StringBuilder();
				builder.append(entry.getKey());
				builder.append(" = ");
				builder.append(entry.getValue().value);
				if (!entry.getValue().comment.equals("")) {
					builder.append("   #");
					builder.append(entry.getValue().comment);
				}
				bwriter.write(builder.toString());
				bwriter.newLine();
			}
			bwriter.flush();
		} catch (IOException e) {
			log.log(Level.SEVERE, logprefix + " IO Exception with file " + file.getName());
		} finally {
			try {
				if (bwriter != null) {
					bwriter.flush();
					bwriter.close();
				}
				if (fwriter != null) {
					fwriter.close();
				}
			} catch (IOException e) {
				log.log(Level.SEVERE, logprefix + " IO Exception with file " + file.getName() + " (on close)");
			}
		}

	}

	private class PropertiesEntry {
		public String value;
		public String comment;

		public PropertiesEntry(String value, String comment) {
			this.value = value;
			this.comment = comment;
		}
	}

	public void remove(String string) {
		map.remove(string);
	}
}