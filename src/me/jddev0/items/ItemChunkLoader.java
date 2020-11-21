package me.jddev0.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class ItemChunkLoader extends ItemStack {
	public ItemChunkLoader() {
		super(Material.END_PORTAL_FRAME);
		
		ItemMeta meta = getItemMeta();
		meta.setDisplayName(ChatColor.BOLD + "Chunk Loader");
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.BLUE + "Place this block to load chunk!");
		lore.add(ChatColor.BLUE + "Break this block to unload chunk!");
		meta.setLore(lore);
		meta.addEnchant(Enchantment.DURABILITY, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		setItemMeta(meta);
	}
}