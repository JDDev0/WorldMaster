package me.jddev0.event;

import org.bukkit.NamespacedKey;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.jddev0.Plugin;
import net.md_5.bungee.api.ChatColor;

public class Event implements Listener {
	private Plugin plugin;
	
	public Event(Plugin plugin) {
		this.plugin = plugin;
	}
	
	private void updatePlayerCounter() {
		plugin.getServer().getOnlinePlayers().forEach(p -> {
			//Remove bossbar
			plugin.getServer().getBossBars().forEachRemaining(bb -> {
				bb.removePlayer(p);
			});
		});
		
		BossBar playerCounter = plugin.getServer().getBossBar(new NamespacedKey(plugin, "player_count"));
		playerCounter.setTitle(ChatColor.GOLD + "Players online (" + plugin.getServer().getOnlinePlayers().size() + "/" +
		plugin.getServer().getMaxPlayers() + ")");
		playerCounter.setProgress((double)(plugin.getServer().getOnlinePlayers().size())/plugin.getServer().getMaxPlayers());
		
		plugin.getServer().getOnlinePlayers().forEach(p -> {
			//Set bossbar
			plugin.getServer().getBossBars().forEachRemaining(bb -> {
				bb.addPlayer(p);
			});
		});
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		
		plugin.permissions.put(p.getUniqueId(), p.addAttachment(plugin));
		plugin.loadPermissions(p);
		
		String welcomText = plugin.getConfig().getString("welcome");
		p.sendTitle("Welcome to", welcomText, 30, 60, 30);
		updatePlayerCounter();
		
		if(p.isOp()) {
			p.setDisplayName("" + ChatColor.BOLD + ChatColor.RED + "[Owner] " + p.getName() + ChatColor.RESET);
			p.setPlayerListName("" + ChatColor.BOLD + ChatColor.RED + "[Owner] " + p.getName() + ChatColor.RESET);
		}else {
			p.setDisplayName(ChatColor.BLUE + "[Player] " + p.getName() + ChatColor.RESET);
			p.setPlayerListName(ChatColor.BLUE + "[Player] " + p.getName() + ChatColor.RESET);
		}
		
		event.setJoinMessage(ChatColor.GOLD + "The player " + p.getDisplayName() + ChatColor.GOLD + " joind the game!");
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		event.setQuitMessage(ChatColor.GOLD + "The player " + p.getDisplayName() + ChatColor.GOLD + " left the game!");
		
		plugin.savePermissions(p);
		p.removeAttachment(plugin.permissions.remove(p.getUniqueId()));
		
		plugin.removePlayerFromElevatorBlocks(p);
		
		new Thread(() -> {
			while(true) {
				if(!plugin.getServer().getOnlinePlayers().contains(p)) {
					updatePlayerCounter();
					
					return;
				}
			}
		}).start();
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		String msg = event.getMessage();
		event.setCancelled(true);
		
		plugin.getServer().broadcastMessage(p.getDisplayName() + ChatColor.GOLD + ": " + msg);
	}
	
	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent event) {
		if(plugin.getConfig().getBoolean("disable_portals")) {
			event.getPlayer().sendMessage(ChatColor.RED + "Portals are disabled!");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPortal(EntityPortalEvent event) {
		if(plugin.getConfig().getBoolean("disable_portals")) {
			event.setCancelled(true);
		}
	}
}