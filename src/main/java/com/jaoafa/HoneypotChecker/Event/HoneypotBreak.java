package com.jaoafa.HoneypotChecker.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.HoneypotChecker.HoneypotChecker;
import com.jaoafa.HoneypotChecker.MySQL;
import com.mcbans.firestar.mcbans.MCBans;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class HoneypotBreak implements Listener {
	JavaPlugin plugin;
	public HoneypotBreak(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHoneypotBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if(PermissionsEx.getUser(player).inGroup("Regular")){
			return;
		}
		if(PermissionsEx.getUser(player).inGroup("Admin")){
			return;
		}

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

		ResultSet res;
		try {
			res = statement.executeQuery("SELECT * FROM Honeypot_Locations WHERE world = '" + block.getLocation().getWorld().getName() + "' AND x1 >= " + block.getLocation().getBlockX() + " AND y1 >= " + block.getLocation().getBlockY() + " AND z1 >= " + block.getLocation().getBlockZ() + " AND x2 <= " + block.getLocation().getBlockX() + " AND y2 <= " + block.getLocation().getBlockY() + " AND z2 <= " + block.getLocation().getBlockZ() + ";");
			if(res.next()){
				//Honeypot
				int honeylocid = res.getInt("id");
				UUID uuid = player.getUniqueId();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				statement.executeUpdate("INSERT INTO Honeypot_History (`player`, `uuid`, `world`, `x`, `y`, `z`, `block`, `locid`, `date`) VALUES ('" + player.getName() + "', '" + player.getUniqueId() + "', '" + block.getLocation().getWorld().getName() + "', " + block.getLocation().getBlockX() + ", " + block.getLocation().getBlockY() + ", " + block.getLocation().getBlockZ() + ", '" + block.getType().toString() + "', " + honeylocid + ", '" + sdf.format(new Date()) + "');");
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
					MCBans.getInstance().getAPI(plugin).globalBan(player.getName(), player.getUniqueId().toString(), "[Honeypot]", "", "[Honeypot] You have been caught destroying a honeypot block.");
					//player.sendMessage("MCBANS BANNED!!!!!!");
				}else{
					player.sendMessage("[HoneypotChecker] " + ChatColor.AQUA + "あなたはHoneypotを破壊しました。あと" + (10 - count) + "回破壊するとBanされます。");
					HoneypotChecker.url_jaoplugin("honeypot", "p="+player.getName()+"&i="+honeylocid+"&c="+count);
				}
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return;
		}


	}
}
