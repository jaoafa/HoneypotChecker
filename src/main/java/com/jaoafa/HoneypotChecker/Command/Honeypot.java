package com.jaoafa.HoneypotChecker.Command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.HoneypotChecker.HoneypotChecker;
import com.jaoafa.HoneypotChecker.MySQL;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Honeypot implements CommandExecutor {
	JavaPlugin plugin;
	public Honeypot(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (!(sender instanceof Player)) {
			HoneypotChecker.SendMessage(sender, cmd, "このコマンドはゲーム内から実行してください。");
			return true;
		}
		Player player = (Player) sender;
		if(!PermissionsEx.getUser(player).inGroup("Admin")){
			HoneypotChecker.SendMessage(sender, cmd, "このコマンドは管理部のみ使用可能です。");
			return true;
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
				HoneypotChecker.SendMessage(sender, cmd, "操作に失敗しました。(ClassNotFoundException/SQLException)");
				HoneypotChecker.SendMessage(sender, cmd, "詳しくはサーバコンソールをご確認ください");
				return true;
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			HoneypotChecker.SendMessage(sender, cmd, "操作に失敗しました。(SQLException)");
			HoneypotChecker.SendMessage(sender, cmd, "詳しくはサーバコンソールをご確認ください");
			return true;
		}
		if(args.length == 0){
			ResultSet res;
			try {
				res = statement.executeQuery("SELECT * FROM Honeypot_Locations WHERE world = '" + player.getLocation().getWorld().getName() + "' AND  x1 >= " + player.getLocation().getBlockX() + " AND y1 >= " + player.getLocation().getBlockY() + " AND z1 >= " + player.getLocation().getBlockZ() + " AND x2 <= " + player.getLocation().getBlockX() + " AND y2 <= " + player.getLocation().getBlockY() + " AND z2 <= " + player.getLocation().getBlockZ() + ";");
				if(res.next()){
					HoneypotChecker.SendMessage(sender, cmd, "この場所はHoneypotの範囲です。HoneypotLocationID: " + res.getInt("id"));
					return true;
				}else{
					HoneypotChecker.SendMessage(sender, cmd, "この場所はHoneypotの範囲ではありません。");
					return true;
				}
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				HoneypotChecker.SendMessage(sender, cmd, "操作に失敗しました。(SQLException)");
				HoneypotChecker.SendMessage(sender, cmd, "詳しくはサーバコンソールをご確認ください");
				return true;
			}
		}else if(args.length == 2){
			// /honeypot remove HoneypotLocationID
			if(args[0].equalsIgnoreCase("remove")){
				int i;
				try{
					i = Integer.parseInt(args[1]);
				}catch (NumberFormatException e){
					HoneypotChecker.SendMessage(sender, cmd, "HoneypotLocationIDは数値で指定してください。");
					return true;
				}
				ResultSet res;
				try {
					res = statement.executeQuery("SELECT * FROM Honeypot_Locations WHERE id = " + i + ";");
					if(res.next()){
						statement.executeUpdate("DELETE FROM land WHERE id = " + i + ";");
						HoneypotChecker.SendMessage(sender, cmd, "指定されたHoneypotLocationIDを削除しました。");
						return true;
					}else{
						HoneypotChecker.SendMessage(sender, cmd, "指定されたHoneypotLocationIDは見つかりませんでした。");
						return true;
					}
				} catch (SQLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
					HoneypotChecker.SendMessage(sender, cmd, "操作に失敗しました。(SQLException)");
					HoneypotChecker.SendMessage(sender, cmd, "詳しくはサーバコンソールをご確認ください");
					return true;
				}
			}
		}else if(args.length == 7){
			// /honeypot add X1 Y1 Z1 X2 Y2 Z2
			if(args[0].equalsIgnoreCase("add")){
				int x1;
				try{
					x1 = Integer.parseInt(args[1]);
				}catch (NumberFormatException e){
					HoneypotChecker.SendMessage(sender, cmd, "X1は数値で指定してください。");
					return true;
				}
				int y1;
				try{
					y1 = Integer.parseInt(args[2]);
				}catch (NumberFormatException e){
					HoneypotChecker.SendMessage(sender, cmd, "Y1は数値で指定してください。");
					return true;
				}
				int z1;
				try{
					z1 = Integer.parseInt(args[3]);
				}catch (NumberFormatException e){
					HoneypotChecker.SendMessage(sender, cmd, "Z1は数値で指定してください。");
					return true;
				}
				int x2;
				try{
					x2 = Integer.parseInt(args[4]);
				}catch (NumberFormatException e){
					HoneypotChecker.SendMessage(sender, cmd, "X2は数値で指定してください。");
					return true;
				}
				int y2;
				try{
					y2 = Integer.parseInt(args[5]);
				}catch (NumberFormatException e){
					HoneypotChecker.SendMessage(sender, cmd, "Y2は数値で指定してください。");
					return true;
				}
				int z2;
				try{
					z2 = Integer.parseInt(args[6]);
				}catch (NumberFormatException e){
					HoneypotChecker.SendMessage(sender, cmd, "Z2は数値で指定してください。");
					return true;
				}

				if(x1 < x2){
					int x2_ = x1;
				    x1 = x2;
				    x2 = x2_;
				}

				if(y1 < y2){
					int y2_ = x1;
				    y1 = y2;
				    y2 = y2_;
				}

				if(z1 < z2){
					int z2_ = z1;
				    z1 = z2;
				    z2 = z2_;
				}

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

				try {
					statement.executeUpdate("INSERT INTO Honeypot_Locations (`world`, `x1`, `y1`, `z1`, `x2`, `y2`, `z2`, `date`) VALUES ('" + player.getWorld().getName() + "', " + x1 + ", " + y1 + ", " + z1 + ", " + x2 + ", " + y2 + ", " + z2 + ", '" + sdf.format(new Date()) + "');");
					HoneypotChecker.SendMessage(sender, cmd, "操作に成功しました。");
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
					HoneypotChecker.SendMessage(sender, cmd, "操作に失敗しました。(SQLException)");
					HoneypotChecker.SendMessage(sender, cmd, "詳しくはサーバコンソールをご確認ください");
					return true;
				}
			}
		}
		HoneypotChecker.SendMessage(sender, cmd, "--- Honeypot ---");
		HoneypotChecker.SendMessage(sender, cmd, "/honeypot: 立っている位置がHoneypotかどうか判断します。");
		HoneypotChecker.SendMessage(sender, cmd, "/honeypot add X1 Y1 Z1 X2 Y2 Z2: Honeypotを追加します。");
		HoneypotChecker.SendMessage(sender, cmd, "/honeypot remove <HoneypotLocationID>: Honeypotを削除します。");
		return true;

	}
}
