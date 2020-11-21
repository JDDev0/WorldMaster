package me.jddev0.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class ItemElevatorSelector extends ItemStack {
	public ItemElevatorSelector() {
		super(Material.STICK);
		
		ItemMeta meta = getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Elevator Selector");
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.BLUE + "Select/Deselect blocks with right click");
		meta.setLore(lore);
		meta.addEnchant(Enchantment.DURABILITY, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		setItemMeta(meta);
	}
}