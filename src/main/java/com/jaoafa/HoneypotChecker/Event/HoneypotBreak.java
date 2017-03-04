package com.jaoafa.HoneypotChecker.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.jaoafa.HoneypotChecker.HoneypotChecker;
import com.jaoafa.HoneypotChecker.MySQL;
import com.mcbans.firestar.mcbans.MCBans;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class HoneypotBreak implements Listener {
	JavaPlugin plugin;
	public HoneypotBreak(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onHoneypotBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if(PermissionsEx.getUser(player).inGroup("Regular")){
			return;
		}
		if(PermissionsEx.getUser(player).inGroup("Moderator")){
			return;
		}
		if(PermissionsEx.getUser(player).inGroup("Admin")){
			return;
		}
		new honeypot_delete(plugin, player).runTaskAsynchronously(plugin);
		Statement statement;
		try {
			statement = HoneypotChecker.c.createStatement();
		} catch (NullPointerException e) {
			MySQL MySQL = new MySQL("jaoafa.com", "3306", "jaoafa", HoneypotChecker.sqluser, HoneypotChecker.sqlpassword);
			try {
				HoneypotChecker.c = MySQL.openConnection();
				statement = HoneypotChecker.c.createStatement();
			} catch (ClassNotFoundException | SQLException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
				return;
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return;
		}
		Statement statement1;
		try {
			statement1 = HoneypotChecker.c.createStatement();
		} catch (NullPointerException e) {
			MySQL MySQL = new MySQL("jaoafa.com", "3306", "jaoafa", HoneypotChecker.sqluser, HoneypotChecker.sqlpassword);
			try {
				HoneypotChecker.c = MySQL.openConnection();
				statement1 = HoneypotChecker.c.createStatement();
			} catch (ClassNotFoundException | SQLException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
				return;
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return;
		}

		statement = MySQL.check(statement);
		statement1 = MySQL.check(statement1);

		ResultSet res;
		try {
			res = statement.executeQuery("SELECT * FROM Honeypot_Locations WHERE world = '" + block.getLocation().getWorld().getName() + "' AND x1 >= " + block.getLocation().getBlockX() + " AND y1 >= " + block.getLocation().getBlockY() + " AND z1 >= " + block.getLocation().getBlockZ() + " AND x2 <= " + block.getLocation().getBlockX() + " AND y2 <= " + block.getLocation().getBlockY() + " AND z2 <= " + block.getLocation().getBlockZ() + ";");
			if(res.next()){
				//Honeypot
				int honeylocid = res.getInt("id");
				UUID uuid = player.getUniqueId();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				long unixtime = System.currentTimeMillis();
				if(!block.getType().isSolid()){
					event.setCancelled(true);
					return;
				}
				if(block.getType() == Material.AIR){
					event.setCancelled(true);
					return;
				}
				int i = statement.executeUpdate("INSERT INTO Honeypot_History (`player`, `uuid`, `world`, `x`, `y`, `z`, `block`, `locid`, `unixtime`, `date`) VALUES ('" + player.getName() + "', '" + player.getUniqueId() + "', '" + block.getLocation().getWorld().getName() + "', " + block.getLocation().getBlockX() + ", " + block.getLocation().getBlockY() + ", " + block.getLocation().getBlockZ() + ", '" + block.getType().toString() + "', " + honeylocid + ", " + unixtime + ", '" + sdf.format(new Date()) + "');");
				ResultSet res1 = statement1.executeQuery("SELECT COUNT(id) FROM Honeypot_History WHERE uuid = \"" + uuid + "\";");
				int count = 0;
				if(res1.next()){
					count = res1.getInt(1);
				}
				for(Player p: Bukkit.getServer().getOnlinePlayers()){
					if(PermissionsEx.getUser(player).inGroup("Admin")){
						p.sendMessage("[HoneypotChecker] " + ChatColor.AQUA + player.getName() + "が" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "を破壊しました。あと" + (10 - count) + "回破壊するとBanされます。");
					}
				}

				if(count >= 11){
					return;
				}else if(count == 10){
					//今回で10回目
					//player.chat("I have been caught destroying a honeypot block.");
					Date date = new Date();
					Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "[Honeypot] You have been caught destroying a honeypot block.", date, "[Honeypot]");
					player.setBanned(true);
					MCBans.getInstance().getAPI(plugin).localBan(player.getName(), player.getUniqueId().toString(), "[Honeypot]", "", "[Honeypot] You have been caught destroying a honeypot block.");
					player.kickPlayer("[Honeypot] You have been caught destroying a honeypot block.");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:ban " + player.getName() + " [Honeypot] You have been caught destroying a honeypot block.");
				}else{
					if(!PermissionsEx.getUser(player).inGroup("Limited")){
						if(!PermissionsEx.getUser(player).inGroup("QPPE")){
							player.sendMessage("[HoneypotChecker] " + ChatColor.AQUA + "あなたはHoneypotを破壊しました。");
						}
					}
					HoneypotChecker.url_jaoplugin("honeypot", "p="+player.getName()+"&i="+honeylocid+"&c="+count);
				}
			}else{
				if(PermissionsEx.getUser(player).inGroup("Limited")){
					event.setCancelled(true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onHoneypotPlace(BlockPlaceEvent event){
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if(PermissionsEx.getUser(player).inGroup("Regular")){
			return;
		}
		if(PermissionsEx.getUser(player).inGroup("Moderator")){
			return;
		}
		if(PermissionsEx.getUser(player).inGroup("Admin")){
			return;
		}

		new honeypot_delete(plugin, player).runTaskAsynchronously(plugin);
		Statement statement;
		try {
			statement = HoneypotChecker.c.createStatement();
		} catch (NullPointerException e) {
			MySQL MySQL = new MySQL("jaoafa.com", "3306", "jaoafa", HoneypotChecker.sqluser, HoneypotChecker.sqlpassword);
			try {
				HoneypotChecker.c = MySQL.openConnection();
				statement = HoneypotChecker.c.createStatement();
			} catch (ClassNotFoundException | SQLException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
				return;
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return;
		}
		Statement statement1;
		try {
			statement1 = HoneypotChecker.c.createStatement();
		} catch (NullPointerException e) {
			MySQL MySQL = new MySQL("jaoafa.com", "3306", "jaoafa", HoneypotChecker.sqluser, HoneypotChecker.sqlpassword);
			try {
				HoneypotChecker.c = MySQL.openConnection();
				statement1 = HoneypotChecker.c.createStatement();
			} catch (ClassNotFoundException | SQLException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
				return;
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return;
		}

		statement = MySQL.check(statement);
		statement1 = MySQL.check(statement1);

		ResultSet res;
		try {
			res = statement.executeQuery("SELECT * FROM Honeypot_Locations WHERE world = '" + block.getLocation().getWorld().getName() + "' AND x1 >= " + block.getLocation().getBlockX() + " AND y1 >= " + block.getLocation().getBlockY() + " AND z1 >= " + block.getLocation().getBlockZ() + " AND x2 <= " + block.getLocation().getBlockX() + " AND y2 <= " + block.getLocation().getBlockY() + " AND z2 <= " + block.getLocation().getBlockZ() + ";");
			if(res.next()){
				player.sendMessage("[HC] " + ChatColor.AQUA + "この場所で設置行為をすることは禁止されています。");
				event.setCancelled(true);
			}else{
				if(PermissionsEx.getUser(player).inGroup("Limited")){
					event.setCancelled(true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

	}
	private class honeypot_delete extends BukkitRunnable{
		Player player;
    	public honeypot_delete(JavaPlugin plugin, Player player) {
    		this.player = player;
    	}
		@Override
		public void run() {
			Statement statement;
			try {
				statement = HoneypotChecker.c.createStatement();
			} catch (NullPointerException e) {
				MySQL MySQL = new MySQL("jaoafa.com", "3306", "jaoafa", HoneypotChecker.sqluser, HoneypotChecker.sqlpassword);
				try {
					HoneypotChecker.c = MySQL.openConnection();
					statement = HoneypotChecker.c.createStatement();
				} catch (ClassNotFoundException | SQLException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
					return;
				}
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				return;
			}
			Statement statement1;
			try {
				statement1 = HoneypotChecker.c.createStatement();
			} catch (NullPointerException e) {
				MySQL MySQL = new MySQL("jaoafa.com", "3306", "jaoafa", HoneypotChecker.sqluser, HoneypotChecker.sqlpassword);
				try {
					HoneypotChecker.c = MySQL.openConnection();
					statement1 = HoneypotChecker.c.createStatement();
				} catch (ClassNotFoundException | SQLException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
					return;
				}
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				return;
			}
			String uuid = player.getUniqueId().toString();
			long unixtime = System.currentTimeMillis();
			try {
				ResultSet res = statement.executeQuery("SELECT * FROM Honeypot_History WHERE uuid = \"" + uuid + "\" AND unixtime < " + (unixtime - 604800) + ";");
				while(res.next()){
					statement1.executeUpdate("DELETE FROM `Honeypot_History` WHERE `id` = " + res.getInt("id"));
				}
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}
}
