package xyz.novality.dev.q37a;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.novality.dev.q37a.events.GameEventHandler;

public class Main extends JavaPlugin {

	public static Random r = new Random();
	
	public static void log(String msg) {
		Bukkit.getLogger().info(msg);
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	@Override
	public void onEnable() {
		log("Plugin author - Q37A");
		
		//events
		new GameEventHandler(this);
	}
	
}
