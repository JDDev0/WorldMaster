package me.jddev0.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class ItemTeleporter extends ItemStack {
	public ItemTeleporter(String name, String icon) {
		super(Material.matchMaterial(icon));
		
		ItemMeta meta = getItemMeta();
		meta.setDisplayName(name.replaceAll("_", " "));
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.BLUE + "Open the teleport overview with right click");
		meta.setLore(lore);
		meta.addEnchant(Enchantment.DURABILITY, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		setItemMeta(meta);
	}
}