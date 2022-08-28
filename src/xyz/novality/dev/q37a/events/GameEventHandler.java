package xyz.novality.dev.q37a.events;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import xyz.novality.dev.q37a.Main;

public class GameEventHandler implements Listener {
	
	private Main plugin;
	private ConcurrentHashMap<UUID, Double> pshoot = new ConcurrentHashMap<UUID, Double>();
	private static NamespacedKey gunkey;
	
	public GameEventHandler(Main plugin) {
		
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
		
		gunkey = new NamespacedKey(this.plugin, "gun");
		
		new BukkitRunnable() {
			
			@SuppressWarnings("deprecation")
			public void run(){
				
				for (UUID u : pshoot.keySet()) {
					if (pshoot.get(u) > 0) {
						if (isGun(Bukkit.getPlayer(u).getInventory().getItemInMainHand())) Bukkit.getPlayer(u).sendActionBar("§d" + Main.round(pshoot.get(u).doubleValue(), 2) + "s");
						pshoot.replace(u, pshoot.get(u)-0.05);
					}
					else {
						if (isGun(Bukkit.getPlayer(u).getInventory().getItemInMainHand())) Bukkit.getPlayer(u).sendActionBar("§d ");
						pshoot.remove(u);
					}
				}
				
			}
			
		}.runTaskTimer(this.plugin, 0, 1);
		
	}

	double COOLDOWN = 10; //cooldown in seconds for sniper usage
	
	@EventHandler
	public void onFire(PlayerArmSwingEvent e) {
		if (!isGun(e.getPlayer().getEquipment().getItemInMainHand())) return;
		if (pshoot.containsKey(e.getPlayer().getUniqueId())) return;
		
		Player p = e.getPlayer();
		
		pshoot.put(p.getUniqueId(), COOLDOWN);
		
		shoot(p);
		
	}
	
	@EventHandler
	public void onAim(PlayerInteractEvent e) {
		
		Player p = e.getPlayer();
		
		if ((e.getAction() != Action.RIGHT_CLICK_AIR) || (e.getItem() == null)) return;
		if (!isGun(p.getInventory().getItemInMainHand())) return;
		
		if (p.hasPotionEffect(PotionEffectType.SLOW)) {
			p.removePotionEffect(PotionEffectType.SLOW);
		}
		else {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10, false, false));
		}
	    
	}
	
	int RANGE = 100; //how many blocks should the sniper range be?
	
	private int distance(Location l, Location l2) {
		return (int) (Math.abs(l.getX()-l2.getX()) + Math.abs(l.getY()-l2.getY()) + Math.abs(l.getZ()+l2.getZ()));
	}
	
	private void shoot(Player p) {
		if (p.getTargetEntity(RANGE) != null) {
			Location el = p.getTargetEntity(RANGE).getLocation();
			if (!(p.getTargetEntity(RANGE) instanceof Player)) {
				p.getTargetEntity(RANGE).getWorld().spawnParticle(Particle.CLOUD, el.getX(), el.getY(), el.getZ(), 25, 0, 0, 0, 0.05);
				p.getTargetEntity(RANGE).remove();
			}
			else {
				Player attacked = (Player) p.getTargetEntity(RANGE);
				attacked.damage(attacked.getHealth()*999999, p);
				if (!attacked.isDead()) attacked.setHealth(0.0D);
			}
		    this.spawnParticleAlongLine(p.getEyeLocation(), el, Particle.CLOUD, distance(p.getEyeLocation(), el)/2, 1, 0.1D, 0.1D, 0.1D, 0D, null, false, l -> l.getBlock().isPassable());
		}
		else {
		    this.spawnParticleAlongLine(p.getEyeLocation(), p.getTargetBlock(RANGE).getLocation(), Particle.CLOUD, distance(p.getEyeLocation(), p.getTargetBlock(RANGE).getLocation())/2, 1, 0.1D, 0.1D, 0.1D, 0D, null, false, l -> l.getBlock().isPassable());
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void spawnGun(Player p) {
		
		ItemStack gun = new ItemStack(Material.STONE_HOE, 1);
		ItemMeta meta = gun.getItemMeta();
		
		meta.setDisplayName("§6Testing Rifle");
		meta.getPersistentDataContainer().set(gunkey, PersistentDataType.STRING, "true");
		
		gun.setItemMeta(meta);
		
		p.getInventory().addItem(gun);
		
	}
	
	private boolean isGun(ItemStack i) {
		if (i == null) return false;
		if (i.getType() == Material.AIR) return false;
		
		if (!i.getItemMeta().getPersistentDataContainer().has(gunkey)) return false;
		
		return (i.getItemMeta().getPersistentDataContainer().get(gunkey, PersistentDataType.STRING).equals("true"));
	}
	
	//free-use utility method adopted from Player_Schark on the spigot forums, slightly modified.
	public void spawnParticleAlongLine(Location start, Location end, Particle particle, int pointsPerLine, int particleCount, double offsetX, double offsetY, double offsetZ, double extra, @Nullable Double data, boolean forceDisplay,  @Nullable Predicate<Location> operationPerPoint) {
	    double d = start.distance(end) / pointsPerLine;
	    for (int i = 0; i < pointsPerLine; i++) {
	        Location l = start.clone();
	        Vector direction = end.toVector().subtract(start.toVector()).normalize();
	        Vector v = direction.multiply(i * d);
	        l.add(v.getX(), v.getY(), v.getZ());
	        if (operationPerPoint == null) {
	        	if (distance(l, start) > 5) start.getWorld().spawnParticle(particle, l, particleCount, offsetX, offsetY, offsetZ, extra, data, forceDisplay);
	        	continue;
	        }
	        if (operationPerPoint.test(l)) {
	        	if (distance(l, start) > 5) start.getWorld().spawnParticle(particle, l, particleCount, offsetX, offsetY, offsetZ, extra, data, forceDisplay);
	        }
	    }
	}

}
