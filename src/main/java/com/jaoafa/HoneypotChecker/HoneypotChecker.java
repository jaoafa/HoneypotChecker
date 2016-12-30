package com.jaoafa.HoneypotChecker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.HoneypotChecker.Command.Honeypot;
import com.jaoafa.HoneypotChecker.Event.HoneypotBreak;

public class HoneypotChecker extends JavaPlugin {
	public static String sqluser;
	public static String sqlpassword;
	public static Connection c = null;
	/**
	 * プラグインが起動したときに呼び出し
	 * @author mine_book000
	 * @since v0.0.3 2016/12/31
	 */

	@Override
	public void onEnable() {

		getCommand("honeypot").setExecutor(new Honeypot(this));
		getServer().getPluginManager().registerEvents(new HoneypotBreak(this), this);

		FileConfiguration conf = getConfig();
		if(conf.contains("sqluser") && conf.contains("sqlpassword")){
			HoneypotChecker.sqluser = conf.getString("sqluser");
			HoneypotChecker.sqlpassword = conf.getString("sqlpassword");
		}else{
			getLogger().info("MySQL Connect err. [conf NotFound]");
			getLogger().info("Disable HoneypotChecker...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		MySQL MySQL = new MySQL("jaoafa.com", "3306", "jaoafa", sqluser, sqlpassword);

		try {
			c = MySQL.openConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			getLogger().info("MySQL Connect err. [ClassNotFoundException]");
			getLogger().info("Disable HoneypotChecker...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().info("MySQL Connect err. [SQLException: " + e.getSQLState() + "]");
			getLogger().info("Disable HoneypotChecker...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		getLogger().info("MySQL Connect successful.");

	}
	/**
	 * プラグインが終了したときに呼び出し
	 * @author mine_book000
	 * @since v0.0.3 2016/12/31
	 */
	@Override
	public void onDisable() {
		getLogger().info("HoneypotChecker Disable.");
	}

	public static void SendMessage(CommandSender sender, Command cmd, String text) {
		sender.sendMessage("[HoneypotChecker] " + ChatColor.AQUA + text);
	}

	/**
	 * jMSプラグインAPIアクセス
	 * @param filename ファイル名(拡張子無し)
	 * @param arg 引数(最初の?無し)
	 * @return 取得した情報
	 * @author mine_book000
	 */
	public static String url_jaoplugin(String filename, String arg){
		return url_access("http://nubesco.jaoafa.com/plugin/" + filename + ".php?" + arg);
	}
	/**
	 * ネットワークGETアクセス
	 * @param address 取得したいURL
	 * @return 取得した情報
	 * @author mine_book000
	 */
	public static String url_access(String address){
		System.out.println("[HoneypotChecker] URLConnect Start:"+address);
		try{
			URL url=new URL(address);
			// URL接続
			HttpURLConnection connect = (HttpURLConnection)url.openConnection();//サイトに接続
			connect.setRequestMethod("GET");//プロトコルの設定
			InputStream in=connect.getInputStream();//ファイルを開く

			// ネットからデータの読み込み
			String data=readString(in);//1行読み取り
			// URL切断
			in.close();//InputStreamを閉じる
			connect.disconnect();//サイトの接続を切断
			System.out.println("[HoneypotChecker] URLConnect End:"+address);
			System.out.println(data);
			return data;
		}catch(Exception e){
			//例外処理が発生したら、表示する
			System.out.println(e);
			System.out.println("[HoneypotChecker] URLConnect Err:"+address);
			return "";
		}
	}
	/**
	 * InputStreamから1行読む
	 * @param in 読み込み元のInputStream
	 * @return 読み込んだテキスト
	 * @author mine_book000
	 */
	static String readString(InputStream in){
		try{
			int l;//呼んだ長さを記録
			int a;//読んだ一文字の記録に使う
			byte b[]=new byte[2048];//呼んだデータを格納
			a=in.read();//１文字読む
			if (a<0) return null;//ファイルを読みっていたら、nullを返す
			l=0;
			while(a>10){//行の終わりまで読む
				if (a>=' '){//何かの文字であれば、バイトに追加
					b[l]=(byte)a;
					l++;
				}
				a=in.read();//次を読む
			}
			return new String(b,0,l);//文字列に変換
		}catch(IOException e){
			//Errが出たら、表示してnull値を返す
			System.out.println("Err="+e);
			return null;
		}
	}

}
