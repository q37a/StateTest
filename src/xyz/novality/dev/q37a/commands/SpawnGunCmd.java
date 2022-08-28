package xyz.novality.dev.q37a.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.novality.dev.q37a.Main;
import xyz.novality.dev.q37a.events.GameEventHandler;

public class SpawnGunCmd implements CommandExecutor {

		private Main plugin;
		
		public SpawnGunCmd(Main plugin) {
			this.plugin = plugin;
			this.plugin.getCommand("gun").setExecutor(this);
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("§4Only players can use this command.");
				return true;
			}
			Player p = (Player) sender;
			
			if (!p.hasPermission("gun.spawn")) {
				p.sendMessage("§4You don't have permission to use this command.");
				return true;
			}
			
			p.sendMessage("§6Enjoy your new rifle!");
			GameEventHandler.spawnGun(p);
			return true;
		}
	
}
