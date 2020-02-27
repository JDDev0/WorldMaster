package me.jddev0.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.jddev0.Plugin;
import net.md_5.bungee.api.ChatColor;

public class ShopEvent implements Listener {
	private Plugin plugin;
	
	public ShopEvent(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerSignChanged(SignChangeEvent event) {
		Player p = event.getPlayer();
		Block block = event.getBlock();
		if(block.getType().toString().endsWith("WALL_SIGN")) {
			Block chestBlock = block.getRelative(((Directional)block.getBlockData()).getFacing().getOppositeFace());
			if(chestBlock.getType() == Material.CHEST) {
				if(event.getLine(0).equals("[Shop]")) {
					if(!p.hasPermission("shop_owner")) {
						p.sendMessage(ChatColor.RED + "You haven't enough rights to build a shop!");
						
						return;
					}
					
					ConfigurationSection shops = plugin.getSaveConfig().getConfigurationSection("shops");
					String pos = chestBlock.getWorld().getName() + "." + chestBlock.getX() + "." + chestBlock.getY() + "." +
					chestBlock.getZ();
					if(shops.contains(pos)) {
						p.sendMessage(ChatColor.RED + "The shop with the ID " + ChatColor.GOLD + pos + ChatColor.RED +
						" already exists!");
						
						return;
					}
					shops.set(pos + ".owner", p.getUniqueId() + "");
					
					Chest chest = (Chest)chestBlock.getState();
					Inventory chestInv = chest.getBlockInventory();
					for(int i = 0;i < 4;i++) {
						ItemStack item = chestInv.getItem(i);
						if(item == null)
							break;
						
						shops.set(pos + ".in." + i, item);
					}
					
					for(int i = 0;i < 4;i++) {
						ItemStack item = chestInv.getItem(i + 18);
						if(item == null)
							break;
						
						shops.set(pos + ".out." + i, item);
					}
					
					//Remove chest inventory
					for(int i = 0;i < 27;i++) {
						ItemStack item = chestInv.getItem(i);
						if(item != null)
							shops.set(pos + ".content." + i, item);
					}
					chestInv.clear();
					
					p.sendMessage(ChatColor.GREEN + "The shop was successfully created!");
					plugin.saveSaveConfig();
					
					block.setType(Material.AIR);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerBreakeBlock(BlockBreakEvent event) {
		Player p = event.getPlayer();
		Block block = event.getBlock();
		String pos = block.getWorld().getName() + "." + block.getX() + "." + block.getY() + "." + block.getZ();
		ConfigurationSection shops = plugin.getSaveConfig().getConfigurationSection("shops");
		if(shops.contains(pos)) {
			if(shops.get(pos + ".owner").equals(p.getUniqueId() + "")) {
				Chest chest = (Chest)block.getState();
				Inventory chestInv = chest.getInventory();
				for(int i = 0;i < 27;i++) {
					if(shops.contains(pos + ".content." + i)) {
						chestInv.setItem(i, shops.getItemStack(pos + ".content." + i));
					}
				}
			
				p.sendMessage(ChatColor.GREEN + "The shop was successfully removed!");
				
				shops.set(pos, null);
				if(shops.getConfigurationSection(block.getWorld().getName() + "." + block.getX() + "." + block.getY()).
				getKeys(false).size() == 0) {
					shops.set(block.getWorld().getName() + "." + block.getX() + "." + block.getY(), null);
					
					if(shops.getConfigurationSection(block.getWorld().getName() + "." + block.getX()).getKeys(false).
					size() == 0) {
						shops.set(block.getWorld().getName() + "." + block.getX(), null);
						
						if(shops.getConfigurationSection(block.getWorld().getName()).getKeys(false).size() == 0) {
							shops.set(block.getWorld().getName(), null);
						}
					}
				}
				plugin.saveSaveConfig();
			}else {
				p.sendMessage(ChatColor.RED + "You aren't the owener of the Shop!");
				
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(!(event.getPlayer() instanceof Player) || !(event.getInventory().getHolder() instanceof Chest)) {
			return;
		}
		
		Player p = (Player)event.getPlayer();
		Chest chest = (Chest)event.getInventory().getHolder();
		Block block = p.getWorld().getBlockAt(chest.getLocation());
		String pos = block.getWorld().getName() + "." + block.getX() + "." + block.getY() + "." + block.getZ();
		ConfigurationSection shops = plugin.getSaveConfig().getConfigurationSection("shops");
		if(shops.contains(pos)) {
			if(shops.get(pos + ".owner").equals(p.getUniqueId() + "")) {
				event.setCancelled(true);
				
				Inventory shopMenu = plugin.getServer().createInventory(null, 54, ChatColor.RED + "Mine" + ChatColor.BOLD +
				ChatColor.GOLD + "Shop" + ChatColor.RESET + "-" + ChatColor.BLUE + "Owner" + ChatColor.RESET +
				" [ID: " + pos + "]");
				
				//Buy interface
				for(int i = 0;i < 4;i++) {
					String itemKey = pos + ".in." + i;
					if(!shops.contains(itemKey))
						break;
					
					shopMenu.setItem(i, shops.getItemStack(itemKey));
				}
				
				ItemStack buyItem = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
				ItemMeta meta = buyItem.getItemMeta();
				meta.setDisplayName(ChatColor.GOLD + "Pay left items, get right items!");
				buyItem.setItemMeta(meta);
				shopMenu.setItem(13, buyItem);
				
				for(int i = 0;i < 4;i++) {
					String itemKey = pos + ".out." + i;
					if(!shops.contains(itemKey))
						break;
					shopMenu.setItem(i + 5, shops.getItemStack(itemKey));
				}
				
				ItemStack placeHolder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
				meta = placeHolder.getItemMeta();
				meta.setDisplayName(" ");
				placeHolder.setItemMeta(meta);
				for(int i = 0;i < 9;i++) {
					for(int j = 0;j < 3;j += 2) {
						int index = 9*j + i;
						if(shopMenu.getItem(index) == null) {
							shopMenu.setItem(index, placeHolder);
						}
					}
				}
				placeHolder = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
				meta = placeHolder.getItemMeta();
				meta.setDisplayName(" ");
				placeHolder.setItemMeta(meta);
				for(int i = 9;i < 18;i++) {
					if(shopMenu.getItem(i) == null) {
						shopMenu.setItem(i, placeHolder);
					}
				}
				
				//Storage interface
				for(int i = 0;i < 27;i++) {
					if(shops.contains(pos + ".content." + i)) {
						shopMenu.setItem(i + 27, shops.getItemStack(pos + ".content." + i));
					}
				}
				
				p.openInventory(shopMenu);
			}else {
				event.setCancelled(true);
				
				Inventory shopMenu = plugin.getServer().createInventory(null, 27, ChatColor.RED + "Mine" + ChatColor.BOLD +
				ChatColor.GOLD + "Shop" + ChatColor.RESET);
				
				for(int i = 0;i < 4;i++) {
					String itemKey = pos + ".in." + i;
					if(!shops.contains(itemKey))
						break;
					
					shopMenu.setItem(i, shops.getItemStack(itemKey));
				}
				
				ItemStack posItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
				ItemMeta meta = posItem.getItemMeta();
				meta.setDisplayName(pos);
				posItem.setItemMeta(meta);
				shopMenu.setItem(4, posItem);
				
				ItemStack buyItem = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
				meta = buyItem.getItemMeta();
				meta.setDisplayName(ChatColor.GOLD + "Pay left items, get right items!");
				buyItem.setItemMeta(meta);
				shopMenu.setItem(13, buyItem);
				
				for(int i = 0;i < 4;i++) {
					String itemKey = pos + ".out." + i;
					if(!shops.contains(itemKey))
						break;
					shopMenu.setItem(i + 5, shops.getItemStack(itemKey));
				}
				
				ItemStack placeHolder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
				meta = placeHolder.getItemMeta();
				meta.setDisplayName(" ");
				placeHolder.setItemMeta(meta);
				for(int i = 0;i < 9;i++) {
					for(int j = 0;j < 3;j += 2) {
						int index = 9*j + i;
						if(shopMenu.getItem(index) == null) {
							shopMenu.setItem(index, placeHolder);
						}
					}
				}
				p.openInventory(shopMenu);
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
		if(name.startsWith(ChatColor.RED + "Mine" + ChatColor.BOLD + ChatColor.GOLD + "Shop" + ChatColor.RESET + "-" + ChatColor.BLUE +
		"Owner" + ChatColor.RESET)) {
			if(event.getClickedInventory() != null && event.getClickedInventory().equals(inv.getTopInventory())) {
				int clickedSlot = event.getSlot();
				if(clickedSlot < 27) {
					event.setCancelled(true);
					
					return;
				}
			}
			
			if(event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT ||
			event.getClick() == ClickType.DOUBLE_CLICK) {
				event.setCancelled(true);
				
				return;
			}
		}
		
		if(name.equals(ChatColor.RED + "Mine" + ChatColor.BOLD + ChatColor.GOLD + "Shop" + ChatColor.RESET)) {
			ItemStack posItem = inv.getTopInventory().getItem(4);
			if(posItem != null && posItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
				String pos = posItem.getItemMeta().getDisplayName();
				ConfigurationSection shops = plugin.getSaveConfig().getConfigurationSection("shops");
				if(shops.contains(pos)) {
					if(event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT ||
					event.getClick() == ClickType.DOUBLE_CLICK) {
						event.setCancelled(true);
						
						return;
					}
					
					int inputCount;
					for(inputCount = 0;inputCount < 4;inputCount++) {
						String itemKey = pos + ".in." + inputCount;
						if(!shops.contains(itemKey))
							break;
					}
					
					if(event.getClickedInventory() != null && event.getClickedInventory().equals(inv.getTopInventory())) {
						if(inputCount == 0) {
							event.setCancelled(true);
							
							return;
						}
						
						int clickedSlot = event.getSlot();
						if(clickedSlot < 9 || clickedSlot > 8 + inputCount) {
							event.setCancelled(true);
							
							return;
						}
					}
					
					final int inputCountCopy = inputCount;
					new BukkitRunnable() {
						@Override
						public void run() {
							boolean flag = true;
							for(int i = 0;i < inputCountCopy;i++) {
								String itemKey = pos + ".in." + i;
								ItemStack input = shops.getItemStack(itemKey);
								if(!input.equals(inv.getTopInventory().getItem(i + 9))) {
									flag = false;
								}
							}
							
							String[] data = pos.split("\\.");
							int x = Integer.parseInt(data[1]);
							int y = Integer.parseInt(data[2]);
							int z = Integer.parseInt(data[3]);
							Block chestBlock = plugin.getServer().getWorld(data[0]).getBlockAt(x, y, z);
							if(chestBlock.getType() == Material.CHEST) {
								Inventory shopInventory = plugin.getServer().createInventory(null, 27);
								for(int i = 0;i < 27;i++) {
									if(shops.contains(pos + ".content." + i)) {
										shopInventory.setItem(i, shops.getItemStack(pos + ".content." + i));
									}
								}
								
								{
									Map<ItemStack, Integer> itemsForRemove = new HashMap<>();
									for(int i = 0;i < 4;i++) {
										String itemKey = pos + ".out." + i;
										if(!shops.contains(itemKey))
											break;
										
										ItemStack item = shops.getItemStack(itemKey);
										boolean[] itemNotExistFlag = new boolean[] {true};
										itemsForRemove.forEach((itemTest, amount) -> {
											if(itemTest.isSimilar(item)) {
												itemsForRemove.put(item, amount + item.getAmount());
												itemNotExistFlag[0] = false;
											}
										});
										if(itemNotExistFlag[0]) {
											itemsForRemove.put(item, item.getAmount());
										}
									}
									boolean[] deleteFlag = new boolean[] {false};
									itemsForRemove.forEach((item, amount) -> {
										if(!shopInventory.containsAtLeast(item, amount)) {
											deleteFlag[0] = true;
										}
									});
									if(deleteFlag[0]) {
										flag = false;
										
										p.sendMessage(ChatColor.RED + "Shop hasn't enough items for selling!");
									}
									
									ItemStack[] inputItems = new ItemStack[inputCountCopy];
									for(int i = 0;i < inputCountCopy;i++) {
										String itemKey = pos + ".in." + i;
										ItemStack input = shops.getItemStack(itemKey);
										inputItems[i] = input;
									}
									Map<Integer, ItemStack> notAddedItems = shopInventory.addItem(inputItems);
									List<Integer> indexDeletedList = new LinkedList<>();
									if(notAddedItems.size() > 0) {
										flag = false;
										
										p.sendMessage(ChatColor.RED + "Shop is to full for accepting the payment!");
										
										//Remove added items
										notAddedItems.forEach((index, item) -> {
											int amount = inputItems[index].getAmount() - item.getAmount();
											ItemStack newItem = item.clone();
											newItem.setAmount(amount);
											shopInventory.removeItem(newItem);
											indexDeletedList.add(index);
										});
									}
									
									//Remove added items left
									for(int i = 0;i < inputCountCopy;i++) {
										if(!indexDeletedList.contains(i)) {
											shopInventory.removeItem(inputItems[i]);
										}
									}
								}
								
								if(flag) {
									for(int i = 14;i < 18;i++) {
										//Give items to player if he didn't do it
										ItemStack item = inv.getTopInventory().getItem(i);
										if(item != null) {
											p.getInventory().addItem(item);
										}
									}
									
									for(int i = 0;i < 4;i++) {
										ItemStack payed = inv.getTopInventory().getItem(i + 9);
										if(payed != null && payed.getType() != Material.AIR) {
											shopInventory.addItem(payed);
										}
										inv.getTopInventory().setItem(i + 9, new ItemStack(Material.AIR));
										
										String itemKey = pos + ".out." + i;
										if(!shops.contains(itemKey))
											continue; //Skip bottom part, but not top part of loop
										
										inv.getTopInventory().setItem(i + 14, shops.getItemStack(itemKey));
										ItemStack sold = inv.getTopInventory().getItem(i + 14);
										shopInventory.removeItem(sold);
									}
									
									//Save items of shop Inventory
									for(int i = 0;i < 27;i++) {
										ItemStack item = shopInventory.getItem(i);
										if(item == null)
											shops.set(pos + ".content." + i, null);
										else
											shops.set(pos + ".content." + i, item);
									}
									plugin.saveSaveConfig();
								}
							}
						}
					}.runTaskLater(plugin, 5);
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(!(event.getPlayer() instanceof Player))
			return;
		
		Player p = (Player)event.getPlayer();
		InventoryView inv = event.getView();
		
		String name = inv.getTitle();
		if(name.equals(ChatColor.RED + "Mine" + ChatColor.BOLD + ChatColor.GOLD + "Shop" + ChatColor.RESET)) {
			ItemStack posItem = inv.getTopInventory().getItem(4);
			if(posItem != null && posItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
				for(int i = 9;i < 18;i++) {
					if(i == 13)
						continue;
					
					//Give items to player if he didn't do it
					ItemStack item = inv.getTopInventory().getItem(i);
					if(item != null) {
						p.getInventory().addItem(item);
					}
				}
			}
		}
		
		if(name.startsWith(ChatColor.RED + "Mine" + ChatColor.BOLD + ChatColor.GOLD + "Shop" + ChatColor.RESET + "-" +
		ChatColor.BLUE + "Owner" + ChatColor.RESET)) {
			String[] data = name.split("\\[ID: ");
			if(data.length == 2) {
				data = data[1].split("\\]");
				if(data.length == 1) {
					String pos = data[0];
					
					ConfigurationSection shops = plugin.getSaveConfig().getConfigurationSection("shops");
					if(shops.contains(pos)) {
						for(int i = 0;i < 27;i++) {
							ItemStack item = inv.getTopInventory().getItem(i + 27);
							if(item == null)
								shops.set(pos + ".content." + i, null);
							else
								shops.set(pos + ".content." + i, item);
						}
						
						plugin.saveSaveConfig();
					}
				}
			}
		}
	}
}