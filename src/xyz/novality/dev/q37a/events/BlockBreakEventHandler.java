package xyz.novality.dev.q37a.events;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.novality.dev.q37a.Main;

public class BlockBreakEventHandler implements Listener {
	
	private Main plugin;
	private ConcurrentHashMap<UUID, Double> pbreak = new ConcurrentHashMap<UUID, Double>();
	
	public BlockBreakEventHandler(Main plugin) {
		
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
		
		new BukkitRunnable() {
			
			@SuppressWarnings("deprecation")
			public void run(){
				
				for (UUID u : pbreak.keySet()) {
					if (pbreak.get(u) > 0) {
						Bukkit.getPlayer(u).sendActionBar("§d" + Main.round(pbreak.get(u).doubleValue(), 2) + "s");
						pbreak.replace(u, pbreak.get(u)-0.05);
					}
					else {
						Bukkit.getPlayer(u).sendActionBar("§d ");
						pbreak.remove(u);
					}
				}
				
			}
			
		}.runTaskTimer(this.plugin, 0, 1);
		
	}
	
	double COOLDOWN = 0.5; //cooldown in seconds for block breakage
	int REACH = 4; //how far the player can reach when breaking blocks
	
	@EventHandler
	public void breakingBlock(PlayerAnimationEvent e) {
		
		if (e.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			
			Player p = e.getPlayer();
			
			if (p.getTargetBlock((Set<Material>) null, REACH).getType() == Material.AIR) return;
			Block b = p.getTargetBlock((Set<Material>) null, REACH);
			
			if (p.getEquipment().getItemInMainHand() == null) return;
			if (!p.getEquipment().getItemInMainHand().getType().toString().toLowerCase().contains("sword")) return;
			if (pbreak.containsKey(e.getPlayer().getUniqueId())) return;
			

			Material mat = Material.BROWN_STAINED_GLASS; // 84% opacity block
			
			Material type = b.getType();
			switch (type) {
			case DIRT: // 100% opacity block
				break;
			case BROWN_STAINED_GLASS: // 84% opacity block
				mat = Material.GREEN_STAINED_GLASS; // 68% opacity block
				break;
			case GREEN_STAINED_GLASS: // 68% opacity block
				mat = Material.LIME_STAINED_GLASS; // 52% opacity block
				break;
			case LIME_STAINED_GLASS: // 52% opacity block
				mat = Material.ORANGE_STAINED_GLASS; // 36% opacity block
				break;
			case ORANGE_STAINED_GLASS: // 36% opacity block
				mat = Material.YELLOW_STAINED_GLASS; // 20% opacity block
				break;
			case YELLOW_STAINED_GLASS: // 20% opacity block
				mat = Material.AIR; // 0% opacity block
				break;
			default:
				return;
			}
			
			pbreak.put(p.getUniqueId(), COOLDOWN);
			
			//the x, y, and z coords of the middle of each 3x3 cube will always leave a remainder of 1 when divided by 3. This way you can
			//tell how to align the map with the cubes.
			
			int xyz[] = {b.getX(), b.getY(), b.getZ()};
			int xyz2[] = {xyz[0] % 3, xyz[1] % 3, xyz[2] % 3};
			xyz[0] = xyz[0]-xyz2[0]; xyz[1] = xyz[1]-xyz2[1]; xyz[2] = xyz[2]-xyz2[2];
			
			for (int x = 0; x <= 2; x++) {
				for (int y = 0; y <= 2; y++) {
					for (int z = 0; z <= 2; z++) {
						if (b.getWorld().getBlockAt(xyz[0]+x, xyz[1]+y, xyz[2]+z).getType() == type) b.getWorld().getBlockAt(xyz[0]+x, xyz[1]+y, xyz[2]+z).setType(mat);
					}
				}
			}
			
		}
	}

}
