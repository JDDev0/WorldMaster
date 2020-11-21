package me.jddev0.items;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValueAdapter;

import me.jddev0.Plugin;
import net.md_5.bungee.api.ChatColor;

public class ItemChunkLoaderEvent implements Listener {
private Plugin plugin;
	
	public ItemChunkLoaderEvent(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlockPlaced();
		Chunk chunk = block.getChunk();
		ItemStack item = event.getItemInHand();
		if(block.getType() == Material.END_PORTAL_FRAME) {
			if(item.getType() == Material.END_PORTAL_FRAME) {
				if(item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Chunk Loader")) {
					if(event.getPlayer().hasPermission("chunk_loader")) {
						block.setMetadata("chunk_loader", new MetadataValueAdapter(plugin) {
							@Override
							public Object value() {
								return null;
							}
							
							@Override
							public void invalidate() {}
						});
						
						ConfigurationSection chunk_loader = plugin.getSaveConfig().getConfigurationSection("loaded_chunks");
						chunk_loader.set(chunk.getWorld().getName() + " x " + chunk.getX() + " x " + chunk.getZ(), true);
						chunk.setForceLoaded(true);
						chunk.load(true);
						
						event.getPlayer().sendMessage(ChatColor.GREEN + "The chunk " + ChatColor.GOLD + chunk.getX() + " " +
						chunk.getZ() + ChatColor.GREEN + " in the world " + ChatColor.GOLD + chunk.getWorld().getName() +
						ChatColor.GREEN + " is now loaded!");
						
						plugin.saveSaveConfig();
					}
				}
			}
		}
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Chunk chunk = block.getChunk();
		if(block.getType() == Material.END_PORTAL_FRAME) {
			if(block.hasMetadata("chunk_loader")) {
				if(event.getPlayer().hasPermission("chunk_loader")) {
					ConfigurationSection chunk_loader = plugin.getSaveConfig().getConfigurationSection("loaded_chunks");
					chunk_loader.set(chunk.getWorld().getName() + " x " + chunk.getX() + " x " + chunk.getZ(), null);
					chunk.setForceLoaded(false);
					
					event.getPlayer().sendMessage(ChatColor.GREEN + "The chunk " + ChatColor.GOLD + chunk.getX() + " " +
					chunk.getZ() + ChatColor.GREEN + " in the world " + ChatColor.GOLD + chunk.getWorld().getName() +
					ChatColor.GREEN + " is no longer loaded!");
					
					plugin.saveSaveConfig();
				}
			}
		}
	}
}