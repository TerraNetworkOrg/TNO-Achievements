package com.nidefawl.Achievements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

import com.nidefawl.Stats.datasource.StatsSQLConnectionManager;

public class AchievementsLoader {

	public static HashMap<String, AchievementListData> LoadAchievementsList(Achievements plugin, String directory, String listLocation) {
		HashMap<String, AchievementListData> achievementList = new HashMap<String, AchievementListData>();
		if (!Achievements.useSQLDefinitions) {
			if (!new File(plugin.getDataFolder(), listLocation).exists()) {
				FileWriter writer = null;
				try {
					writer = new FileWriter(new File(plugin.getDataFolder(), listLocation));
					writer.write("# Achievements \r\n");
					writer.write("# Format is: enabled:name:maxawards:category:key:value:description[:commands[:conditions]]\r\n");
					writer.write("# commands are optional, separated by semicolons (;), available commands: item group\r\n");
					writer.write("# Example: 1:ClownPuncher:1:stats:armswing:1000:Awarded for punching the air 1000 times.[:item goldblock 1]\r\n");
					writer.write("# Example: 1:ClownPuncher:1:stats:armswing:1000:Awarded for punching the air 1000 times.[:item goldblock 1]\r\n");
					writer.write("1:50k Walker!:1:stats:move:50000:You have walked 50,000 feet!:item diamondchestplate 1\r\n");
					writer.write("1:100 Grand:1:stats:move:100000:You have walked 100,000 feet!:item diamond 10\r\n");
					writer.write("1:200k Steps:1:stats:move:200000:You have walked 200,000 feet! WOW...:item glass 200\r\n");
					writer.write("1:ONE MILLION FEET:1:stats:move:1000000:You have walked 1,000,000 feet! You are God.:item diamond 64\r\n");
					writer.write("1:What's above god?:1:stats:armswing:100000:You have swung your arms 100,000 times! I'm shocked. Figuratively.:item diamond 20\r\n");
					writer.write("1:What is this I dont even...:1:stats:armswing:200000:You have swung your arms 200,000 times! Christ!:item diamond 30\r\n");
					writer.write("1:Dear lord:1:stats:armswing:500000:You have swung your arms 500,000 times! OMFG!:item diamond 50\r\n");
					writer.write("1:FUCK:1:stats:armswing:1000000:You have swung your arms 1,000,000 times! You are now God.:item bedrock 500\r\n");
					writer.write("1:Frequent Flyer:1:stats:login:40:You have logged in 40 times!:item pumpkin 6\r\n");
					writer.write("1:200 logs:1:stats:login:200:You have logged in 200 times!:item mobspawner 2\r\n");
					writer.write("1:Aww Nutsack:1:stats:login:1000:You have logged in 1,000 times!:item diamond 64\r\n");
					writer.write("1:Sup Creeper!:1:damagetaken:Creeper:10: Take 10 creeper damage:item bread 2\r\n");
					writer.write("1:Invisible Mobs!:1:damagetaken:Creeper:20: Take 20 creeper damage:item bread 2:Sup Creeper!\r\n");
					writer.write("1:Fucking Creepers...:1:damagetaken:Creeper:40: Take 40 creeper damage:item bread 2:Invisible Mobs!\r\n");
					writer.write("1:It was lag:1:deaths:total:50:Die a lot \\:P <colon>D:item ironboots 1;addpermission worldguard.heal.self;/god *\r\n");
					writer.write("1:You talk too fucking much:1:stats:chatletters:20000:Awarded for typing 20k chat letters!:item diamondboots 1\r\n");
					writer.write("1:You talk way too fucking much:1:stats:chatletters:30000:Awarded for typing 30k chat letters!:item goldpickaxe 1\r\n");
					writer.write("1:You really talk way too fucking much:1:stats:chatletters:40000:Awarded for typing 40k chat letters!:item diamondpants 1\r\n");
					writer.write("1:50k Chat letters:1:stats:chatletters:50000:Awarded for typing 50k chat letters!:item sponge 6\r\n");
					writer.write("1:60k Chat letters:1:stats:chatletters:60000:Awarded for typing 60k chat letters!:item lightstone 64\r\n");
					writer.write("1:70k Chat letters:1:stats:chatletters:70000:Awarded for typing 70k chat letters!:item netherstone 64\r\n");
					writer.write("1:80k Chat letters:1:stats:chatletters:80000:Awarded for typing 80k chat letters!:item diamond 10\r\n");
					writer.write("1:90k Chat letters:1:stats:chatletters:90000:Awarded for typing 90k chat letters!:item goldblock 10\r\n");
					writer.write("1:100k Chat letters:1:stats:chatletters:100000:Awarded for typing 100k chat letters!:item diamond 20\r\n");
					writer.write("1:110k Chat letters:1:stats:chatletters:110000:Awarded for typing 110k chat letters!:item goldblock 10\r\n");
					writer.write("1:120k Chat letters:1:stats:chatletters:120000:Awarded for typing 120k chat letters!:item coal 5\r\n");
					writer.write("1:140k Chat letters:1:stats:chatletters:140000:Awarded for typing 140k chat letters!:item coal 5\r\n");
					writer.write("1:150k Chat letters:1:stats:chatletters:150000:Awarded for typing 150k chat letters!:item diamondpickaxe 1\r\n");
					writer.write("1:160k Chat letters:1:stats:chatletters:160000:Awarded for typing 160k chat letters!:item diamondhoe 1\r\n");
					writer.write("1:Let there be light!:1:blockcreate:torch:100:Placed 100 torches.:item lightstone 10\r\n");
					writer.write("1:Can you see this from space?:1:blockcreate:torch:1000:Placed 1,000 torches.:item diamondhelmet 1\r\n");
					writer.write("1:Free Diamond Pickaxes:1:blockcreate:cobblestone:1500:Placed over 1,500 blocks of cobblestone.:item diamondpickaxe 2\r\n");
					writer.write("1:I was raised in the slop:1:blockcreate:dirt:1000:Placed 1,000 blocks of dirt.:item diamondshovel 1\r\n");
					writer.write("1:Well Read:1:blockcreate:bookshelf:5:Placed 1 Bookshelf.:item irondoor 2\r\n");
					writer.write("1:Higher Learning:1:blockcreate:bookshelf:50:Placed 2 Bookshelves.:item minecart 2\r\n");
					writer.write("1:I like to cut my pappy:1:blockcreate:bookshelf:100:Placed 4 Bookshelves.:item bookshelf 50\r\n");
					writer.write("1:Mending Fences:1:blockcreate:fence:50:Placed 50 Fences.:item watch 1\r\n");
					writer.write("1:And the cow jumped over the...fence??:1:blockcreate:fence:250:Placed 250 Fences.:item lightstone 64\r\n");
					writer.write("1:How much wood...:1:blockcreate:wood:1500:Placed over 1500 wood.:item goldrecord 1\r\n");
					writer.write("1:Glass Blower:1:blockcreate:glass:1500:Placed over 1,500 glass blocks.:item goldhoe 1\r\n");
					writer.write("1:Building the Chunnel!:1:blockdestroy:rock:1000:Mined 1,000 Smooth Stone.:item diamondpickaxe 1\r\n");
					writer.write("1:Smokey the bear hates you:1:blockdestroy:log:500:Chopped 500 wood.:item ironaxe 2\r\n");
					writer.write("1:Global Warming:1:blockdestroy:log:1000:Chopped 1,000 wood.:item diamondaxe 1\r\n");
					writer.write("1:TIME TO REPLANT:1:blockdestroy:log:1500:Chopped over 1,500 wood!:item diamondaxe 2\r\n");
					writer.write("1:Off roading:1:blockdestroy:dirt:1000:Dug up 1,000 dirt.:item diamondshovel 1\r\n");
					writer.write("1:I need to wash my hands now:1:blockdestroy:dirt:1500:Dug up over 1,500 dirt!:item diamondshovel 2\r\n");
					writer.write("1:I suck at placement:1:blockdestroy:cobblestone:1000:Mined 1,000 Cobblestone.:item goldpickaxe 1\r\n");
					writer.write("1:Oops, I did it again:1:blockdestroy:cobblestone:1500:Mined over 1,500 Cobblestone.:item goldshovel 1\r\n");
					writer.write("1:7 days:1:stats:playedfor:604800:Have your play time make 1 week:item diamond 10\r\n");
					writer.write("1:Now thats Hot:1:damagetaken:lava:1:Take lava damage:item waterbucket 1\r\n");
					writer.write("1:Americas Most wanted:1:kills:total:250: Kill 250 monsters :item diamond 15\r\n");
					writer.write("1:I kill shit:1:damagedealt:total:2000: Deal 2000 damage:item arrow 200\r\n");
					writer.write("1:Cold Blooded:1:damagedealt:total:4000: Deal 6000 damage:item arrow 500\r\n");
					writer.write("1:I like to fry:1:damagetaken:fire:500: Catch on fire numerous times:item snowblock 10\r\n");
					writer.write("1:Slaughterhouse:1:itempickup:pork:50: Pickup 50 pieces of Pork:item goldsword 1\r\n");
					writer.write("1:You are literally made of dicks:1:stats:lighter:15:PissLizard Achievement:item lever 2\r\n");
					writer.write("1:If only I had a record:1:blockcreate:jukebox:1:Well...:item greenrecord 1\r\n");
					writer.write("1:Slimes? lol:1:itempickup:slimeorb:5:Useless:item slimeorb 5\r\n");
					writer.write("1:Hmm...I've stopped liking the idea:1:blockdestroy:bookshelf:4:Not sure why though...:item diamondaxe 1\r\n");
					writer.write("1:Lightstone:1:blockcreate:lightstone:10:I like it too:item lightstone 64\r\n");
					writer.write("1:There's issue with the circuit:1:blockdestroy:redstonewire:50:Ah, there we go.:item redstonewire 64\r\n");
					writer.write("1:Welcome Visit:1:stats:login:1:Joined this server for the first time:money 1000\r\n");
					writer.write("1:Treekiller!:1:blockdestroy:log:5:Destroyed 5 logs:money -100\r\n");
					writer.write("1:Respect the Environment!:1:stats:lighter:10:You light up everything :/kick *:!group VIP+;Global Warming\r\n");
					writer.write("1:I'm safe for the night:1:blockcreate:torch:5:Its getting dark...:/time set 13500\r\n");
				} catch (Exception e) {
					Achievements.LogError("Exception while creating " + listLocation+" "+e);
				} finally {
					try {
						if (writer != null) {
							writer.close();
						}
					} catch (IOException e) {
						Achievements.LogError("Exception while closing writer for " + listLocation+" "+e);
					}
				}
				Achievements.LogInfo("created default list " + listLocation);
			}
			String line = "";
			try {
				Scanner scanner = new Scanner(new File(directory + File.separator + listLocation));
				while (scanner.hasNextLine()) {
					line = scanner.nextLine();
					if (line.startsWith("#") || line.equals(""))
						continue;
					if (!(line.startsWith("0") || line.startsWith("1"))) {
						Achievements.LogError("Malformed line, does not start with 0 or 1 (check whitspaces) (" + line + ") in " + listLocation);
						continue;
					}
					line = line.replaceAll("\\\\:", "<colon>");
					line = line.replaceAll("\\\\;", "<semicolon>");
					String[] split = line.split(":");
					if (split.length < 7) {
						Achievements.LogError("Malformed line, not enough fields (" + line + ") in " + listLocation);
						continue;
					}
					for(int i=0;i<split.length;i++) {
						split[i] = split[i].replaceAll("<colon>", ":");
					}
					int enabled = Integer.parseInt(split[0]);
					int maxawards = Integer.parseInt(split[2]);
					int value = Integer.parseInt(split[5]);
					String commands = null;
					if (split.length > 7)
						commands = split[7];
					String conditions = null;
					if (split.length > 8)
						conditions = split[8];
					AchievementListData achEntry = new AchievementListData(plugin, enabled, split[1], maxawards, split[3], split[4], value, split[6], commands, conditions);
					achievementList.put(split[1], achEntry);
				}

				scanner.close();
			} catch (Exception e) {
				Achievements.LogError("Exception while reading " + listLocation + " (" + line + "): " + e);
				return null;
			}

		} else {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = StatsSQLConnectionManager.getConnection(true);
				ps = conn.prepareStatement("SELECT * from " + Achievements.dbAchievementsTable);
				rs = ps.executeQuery();
				while (rs.next()) {
					AchievementListData achEntry = new AchievementListData(plugin, rs.getInt("enabled"), rs.getString("name"), rs.getInt("maxawards"), rs.getString("category"), rs.getString("stat"), rs.getInt("value"), rs.getString("description"), rs.getString("commands"), rs.getString("conditions"));
					achievementList.put(rs.getString("name"), achEntry);
				}
			} catch (SQLException ex) {
				Achievements.LogError("SQL exception while loading Achievements from table " + Achievements.dbAchievementsTable + ": " + ex);
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (ps != null)
						ps.close();
					if (conn != null)
						conn.close();
				} catch (SQLException ex) {
					Achievements.LogError("SQL exception (on close) while loading Achievements from table " + Achievements.dbAchievementsTable + ": " + ex);
				}
			}
		}
		for (AchievementListData check : achievementList.values()) {
			if (!check.commands.preCheck()) {
				Achievements.LogInfo("Disabling achievement '" + check.getName() + "'...");
				check.setEnabled(false);
			}
			if (check.conditions.isEmpty())
				continue;
			for (String cond : check.conditions.condList) {
				if(cond.startsWith("!")) cond = cond.substring(1);
				if(cond.startsWith("group ")) continue;
				if(cond.startsWith("permission ")) continue;
				if (!achievementList.containsKey(cond)) {
					Achievements.LogInfo("Condition '" + cond + "' not found! Disabling achievement '" + check.getName() + "'...");
					check.setEnabled(false);
				}
			}
		}
		return achievementList;

	}

}
