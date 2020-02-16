package me.jddev0.event;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import me.jddev0.Plugin;
import net.md_5.bungee.api.ChatColor;

public class WorldEvent implements Listener {
	private Plugin plugin;
	
	public WorldEvent(Plugin plugin) {
		this.plugin = plugin;
	}
	
	//Command protection
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		String cmd = event.getMessage().substring(1);
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("cmdProtection")) {
			ConfigurationSection cmdProtection = plugin.getSaveConfig().getConfigurationSection("cmdProtection");
			if(cmdProtection.contains(worldName)) {
				ConfigurationSection worldCmdProtection = cmdProtection.getConfigurationSection(worldName);
				boolean flag = false;
				if(worldCmdProtection.contains("allowed")) {
					flag = true;
					List<String> allowedCmds = worldCmdProtection.getStringList("allowed");
					for(String allowedCmd:allowedCmds) {
						if(cmd.startsWith(allowedCmd)) {
							flag = false;
							break;
						}
					}
				}
				
				if(worldCmdProtection.contains("disallowed")) {
					List<String> disallowedCmds = worldCmdProtection.getStringList("disallowedCmds");
					for(String disallowedCmd:disallowedCmds) {
						if(cmd.startsWith(disallowedCmd)) {
							flag = true;
							break;
						}
					}
				}
				
				if(flag) {
					event.setCancelled(true);
					
					p.sendMessage(ChatColor.RED + "The command " + ChatColor.GOLD + cmd + ChatColor.RED +
					" isn't allowed in the world " + ChatColor.GOLD + worldName + ChatColor.RED + "!");
				}
			}
		}
	}
	
	//World enter protection
	@EventHandler
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		String worldNameFrom = event.getFrom().getName();
		String worldName = event.getPlayer().getWorld().getName();
		
		//Set gamemode
		if(plugin.getSaveConfig().contains("joinGamemodes")) {
			ConfigurationSection joinGamemodes = plugin.getSaveConfig().getConfigurationSection("joinGamemodes");
			if(joinGamemodes.contains(worldName)) {
				event.getPlayer().setGameMode(GameMode.valueOf(joinGamemodes.getString(worldName).toUpperCase()));
			}
		}
		
		//Can enter
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".enter")) {
			String permission = plugin.getSaveConfig().getString("worldPermissions." + worldName + ".enter");
			if(!event.getPlayer().hasPermission(permission)) {
				event.getPlayer().sendMessage(ChatColor.RED + "You haven't enought rights to enter the world " + ChatColor.GOLD +
				worldName + ChatColor.RED + "!");
				
				event.getPlayer().teleport(plugin.getServer().getWorld("world").getSpawnLocation());
				
				return;
			}
		}
		
		//Set inventory
		String oldInventory = "default";
		String newInventory = "default";
		ConfigurationSection inventoryWorlds = plugin.getSaveConfig().getConfigurationSection("inventories.worlds");
		if(inventoryWorlds.contains(worldNameFrom)) {
			oldInventory = inventoryWorlds.getString(worldNameFrom + ".inventory");
		}
		if(inventoryWorlds.contains(worldName)) {
			newInventory = inventoryWorlds.getString(worldName + ".inventory");
		}
		if(!oldInventory.equals(newInventory)) {
			plugin.saveInventory(event.getPlayer(), oldInventory);
			plugin.loadInventory(event.getPlayer(), newInventory);
		}
	}
	
	//Player respawn event
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		String worldName = event.getPlayer().getWorld().getName();
		
		ConfigurationSection inventoryNames = plugin.getSaveConfig().getConfigurationSection("inventories.names");
		ConfigurationSection inventoryWorlds = plugin.getSaveConfig().getConfigurationSection("inventories.worlds");
		
		if(inventoryWorlds.contains(worldName)) {
			ConfigurationSection inventory = inventoryNames.getConfigurationSection(inventoryWorlds.getString(worldName + ".inventory"));
			
			if(inventory.contains("spawn_point")) {
				event.setRespawnLocation(inventory.getLocation("spawn_point"));
				//FIXME save bed location from players in inventory system
			}
		}
	}
	
	//Change gamemode protection
	@EventHandler
	public void onPlayerChangedGamemode(PlayerGameModeChangeEvent event) {
		String worldName = event.getPlayer().getWorld().getName();
		
		//Can set gamemode
		if(plugin.getSaveConfig().contains("allowedGamemodes")) {
			ConfigurationSection allowedGamemodes = plugin.getSaveConfig().getConfigurationSection("allowedGamemodes");
			if(allowedGamemodes.contains(worldName)) {
				List<String> gamemodes = allowedGamemodes.getStringList(worldName);
				for(String gamemode:gamemodes) {
					if(event.getNewGameMode().equals(GameMode.valueOf(gamemode.toUpperCase()))) {
						return;
					}
				}
				
				event.setCancelled(true);
				
				event.getPlayer().sendMessage(ChatColor.RED + "You can't change your gamemode to " + ChatColor.GOLD +
				event.getNewGameMode().toString().toLowerCase() + ChatColor.RED + " in the world " + ChatColor.GOLD + worldName +
				ChatColor.RED + "!");
			}
		}
	}
	
	
	//Build protection
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player p = event.getPlayer();
		if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can't use buckets in the spawn world!");
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't use buckets in the world " + worldName + "!");
			}
		}
	}
	
	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Player p = event.getPlayer();
		if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can't use buckets in the spawn world!");
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't use buckets in the world " + worldName + "!");
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity e = event.getDamager();
		if(e instanceof Player) {
			Player p = (Player)e;
			if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't hurt entities in the spawn world!");
			}
			
			String worldName = p.getWorld().getName();
			if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
				if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
					event.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You can't hurt entities in the world " + worldName + "!");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player p = event.getPlayer();
		if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can't interact with entities in the spawn world!");
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't interact with entities in the world " + worldName + "!");
			}
		}
	}
	
	@EventHandler
	public void onArmorStandMainpulate(PlayerArmorStandManipulateEvent event) {
		Player p = event.getPlayer();
		if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can't interact with armor stands in the spawn world!");
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't interact with armor stands in the world " + worldName + "!");
			}
		}
	}
	
	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		Entity e = event.getAttacker();
		if(e instanceof Player) {
			Player p = (Player)e;
			if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't hurt entities in the spawn world!");
			}
			
			String worldName = p.getWorld().getName();
			if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
				if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
					event.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You can't hurt entities in the world " + worldName + "!");
				}
			}
		}
	}
	
	@EventHandler
	public void onVehicleCollision(VehicleEntityCollisionEvent event) {
		Entity e = event.getEntity();
		if(e instanceof Player) {
			Player p = (Player)e;
			if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't move vehicles in the spawn world!");
			}
			
			String worldName = p.getWorld().getName();
			if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
				if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
					event.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You can't move vehicles in the world " + worldName + "!");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't break blocks in the spawn world!");
			}else if(event.getAction() == Action.PHYSICAL) {
				if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BIRCH_SIGN)
					return;
				
				event.setCancelled(true);
			}else if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Material inHand = event.getMaterial();
				//"EGG": Spawn eggs and eggs
				//"BOW": Bows and crossbows
				if(inHand.toString().endsWith("EGG") || inHand.toString().endsWith("BOW") || inHand.toString().endsWith("BOAT") ||
				inHand.toString().endsWith("MINECART") || inHand == Material.ENDER_PEARL|| inHand == Material.TRIDENT ||
				inHand == Material.FIREWORK_ROCKET || inHand == Material.ARMOR_STAND || inHand == Material.SPLASH_POTION ||
				inHand == Material.LINGERING_POTION || inHand == Material.EXPERIENCE_BOTTLE ||
				inHand == Material.FLINT_AND_STEEL || inHand == Material.FISHING_ROD) {
					event.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You can't use tools in the spawn world!");
				}
				
				if(event.getClickedBlock().getType().isInteractable()) {
					if(event.getClickedBlock().getType().toString().endsWith("SIGN"))
						return;
					
					event.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You can't open block inventories in the spawn world!");
				}
			}
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
					event.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You can't break blocks in the world " + worldName + "!");
				}else if(event.getAction() == Action.PHYSICAL) {
					if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BIRCH_SIGN)
						return;
					
					event.setCancelled(true);
				}else if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					Material inHand = event.getMaterial();
					//"EGG": Spawn eggs and eggs
					//"BOW": Bows and crossbows
					if(inHand.toString().endsWith("EGG") || inHand.toString().endsWith("BOW") || inHand.toString().
					endsWith("BOAT") || inHand.toString().endsWith("MINECART") || inHand == Material.ENDER_PEARL||
					inHand == Material.TRIDENT || inHand == Material.FIREWORK_ROCKET || inHand == Material.ARMOR_STAND ||
					inHand == Material.SPLASH_POTION || inHand == Material.LINGERING_POTION || inHand ==
					Material.EXPERIENCE_BOTTLE || inHand == Material.FLINT_AND_STEEL || inHand == Material.FISHING_ROD) {
						event.setCancelled(true);
						p.sendMessage(ChatColor.RED + "You can't use tools in the world " + worldName + "!");
					}
					
					if(event.getClickedBlock().getType().isInteractable()) {
						if(event.getClickedBlock().getType().toString().endsWith("SIGN"))
							return;
						
						event.setCancelled(true);
						p.sendMessage(ChatColor.RED + "You can't open block inventories in the world " + worldName + "!");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can't set blocks in the spawn world!");
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't set blocks in the world " + worldName + "!");
			}
		}
	}
	
	@EventHandler
	public void onPlayerBreakeBlock(BlockBreakEvent event) {
		Player p = event.getPlayer();
		if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can't break blocks in the spawn world!");
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't break blocks in the world " + worldName + "!");
			}
		}
	}
	
	@EventHandler
	public void onFramePlace(HangingPlaceEvent event) {
		Player p = event.getPlayer();
		if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can't set blocks in the spawn world!");
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't set blocks in the world " + worldName + "!");
			}
		}
	}
	
	@EventHandler
	public void onFrameBreake(HangingBreakByEntityEvent event) {
		if(!(event.getRemover() instanceof Player))
			return;
		
		Player p = (Player)event.getRemover();
		if(p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can't break blocks in the spawn world!");
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't break blocks in the world " + worldName + "!");
			}
		}
	}
	
	@EventHandler
	public void onItemFrameItemRemoval(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player))
			return;
		
		Player p = (Player)event.getDamager();
		if(event.getEntity() instanceof ItemFrame && p.getWorld().getName().equals("world") && !p.hasPermission("build_spawn")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can't break blocks in the spawn world!");
		}
		
		String worldName = p.getWorld().getName();
		if(plugin.getSaveConfig().contains("worldPermissions." + worldName + ".build")) {
			if(!p.hasPermission(plugin.getSaveConfig().getString("worldPermissions." + worldName + ".build"))) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You can't break blocks in the world " + worldName + "!");
			}
		}
	}
}