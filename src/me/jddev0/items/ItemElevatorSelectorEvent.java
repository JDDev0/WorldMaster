package me.jddev0.items;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import me.jddev0.Plugin;

public class ItemElevatorSelectorEvent implements Listener {
private Plugin plugin;
	
	public ItemElevatorSelectorEvent(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		plugin.removeElevatorSelector(p);
		plugin.removePlayerFromElevatorBlocks(p);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack inHand = event.getItem();
			if(inHand == null)
				return;
			
			if(inHand.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Elevator Selector")) {
				Location pos = event.getClickedBlock().getLocation();
				if(plugin.containsElevatorBlock(p, pos)) {
					plugin.removeElevatorBlock(p, pos);
					
					p.sendMessage("Block at " + pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ() +
					" is no longer an elevator block!");
				}else {
					plugin.addElevatorBlock(p, pos);
					
					p.sendMessage("Block at " + pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ() +
					" is now an elevator block!");
				}
			}
		}
	}
}