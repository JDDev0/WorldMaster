package me.jddev0.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import me.jddev0.Plugin;
import net.md_5.bungee.api.ChatColor;

public class ItemTeleporterEvent implements Listener {
private Plugin plugin;
	
	public ItemTeleporterEvent(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack inHand = event.getItem();
			if(inHand == null)
				return;
			
			if(inHand.getItemMeta().getLore() != null && inHand.getItemMeta().getLore().get(0).equals(ChatColor.BLUE +
			"Open the teleport overview with right click")) {
				Inventory teleporterMenu = plugin.getServer().createInventory(null, 54, inHand.getItemMeta().getDisplayName());
				
				String name = inHand.getItemMeta().getDisplayName().split(ChatColor.GOLD + "", 2)[1].replaceAll(" ", "_");
				ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
				ConfigurationSection slots = teleporters.getConfigurationSection(name).getConfigurationSection("slots");
				slots.getKeys(false).forEach(slot -> {
					ConfigurationSection position = teleporters.getConfigurationSection("positions").
					getConfigurationSection(slots.getString(slot));
					
					Vector vec = position.getVector("pos");
					Location pos = new Location(plugin.getServer().getWorld(position.getString("world")), vec.getX(), vec.getY(),
					vec.getZ());
					
					ItemStack item = new ItemStack(Material.matchMaterial(position.getString("icon")));
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GOLD + slots.getString(slot).replaceAll("_", " "));
					List<String> lore = new ArrayList<>();
					lore.add(ChatColor.DARK_GREEN + "Teleport to " + ChatColor.GOLD + pos.getX() + " " + pos.getY() + " " +
					pos.getZ() + ChatColor.DARK_GREEN + " in the world " + ChatColor.GOLD + pos.getWorld().getName() +
					ChatColor.DARK_GREEN + "!");
					meta.setLore(lore);
					item.setItemMeta(meta);
					teleporterMenu.setItem(Integer.parseInt(slot), item);
				});
				
				p.openInventory(teleporterMenu);
				
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!(event.getWhoClicked() instanceof Player))
			return;
		
		Player p = (Player)event.getWhoClicked();
		InventoryView inv = event.getView();
		
		String name = inv.getTitle();
		if(!name.contains(ChatColor.GOLD + ""))
			return;
		name = name.split(ChatColor.GOLD + "", 2)[1].replaceAll(" ", "_");
		if(name.equals("positions"))
			return;
		ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
		if(!teleporters.contains(name))
			return;
		if(!event.getClickedInventory().equals(inv.getTopInventory())) {
			event.setCancelled(true);
			return;
		}
		ItemStack item = inv.getItem(event.getSlot());
		if(item == null)
			return;
		String locationName = item.getItemMeta().getDisplayName().split(ChatColor.GOLD + "", 2)[1].replaceAll(" ", "_");
		ConfigurationSection positions = teleporters.getConfigurationSection("positions");
		ConfigurationSection position = positions.getConfigurationSection(locationName);
		
		event.setCancelled(true);
		
		Vector vec = position.getVector("pos");
		Location pos = new Location(plugin.getServer().getWorld(position.getString("world")), vec.getX(), vec.getY(),
		vec.getZ());
		
		p.closeInventory();
		p.teleport(pos);
	}
}