package com.nidefawl.Achievements;

import java.util.Iterator;
import java.sql.*;

import com.nidefawl.Stats.datasource.StatsSQLConnectionManager;

public class PlayerAchievementSQL extends PlayerAchievement {
	public PlayerAchievementSQL(String name) {
		super(name);
	}

	@Override
	public void save() {

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = StatsSQLConnectionManager.getConnection(true);
			conn.setAutoCommit(false);

			Iterator<String> achIter = achievements.keySet().iterator();
			while (achIter.hasNext()) {
				String achName = achIter.next();
				Achievement ach = achievements.get(achName);
				if (!ach.modified)
					continue;

				ps = conn.prepareStatement("INSERT INTO " + Achievements.dbPlayerAchievementsTable + " (player,achievement,count) VALUES(?,?,?) ON DUPLICATE KEY UPDATE count=?", Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, name);
				ps.setString(2, achName);
				ps.setInt(3, ach.getCount());
				ps.setInt(4, ach.getCount());
				ps.executeUpdate();
			}
			conn.commit();
		} catch (SQLException ex) {
			Achievements.LogError("SQL exception "+ ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				Achievements.LogError("SQL exception (on close) "+ ex);
			}
		}
	}

	@Override
	public void load() {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = StatsSQLConnectionManager.getConnection(true);
			ps = conn.prepareStatement("SELECT * from " + Achievements.dbPlayerAchievementsTable + " where player = ?");
			ps.setString(1, name);
			rs = ps.executeQuery();
			while (rs.next())
				put(rs.getString("achievement"), rs.getInt("count"));
		} catch (SQLException ex) {
			Achievements.LogError("SQL exception "+ ex);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				Achievements.LogError("SQL exception (on close) "+ ex);
			}
		}
	}
}