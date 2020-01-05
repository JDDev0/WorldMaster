package me.jddev0;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import me.jddev0.commands.Commands;
import me.jddev0.event.Event;
import me.jddev0.event.ShopEvent;
import me.jddev0.event.WorldEvent;
import me.jddev0.items.ItemChunkLoaderEvent;
import me.jddev0.items.ItemElevatorSelectorEvent;
import me.jddev0.items.ItemTeleporterEvent;
import net.md_5.bungee.api.ChatColor;

public class Plugin extends JavaPlugin {
	private final String PLUGIN_NAME = "[" + ChatColor.RED + ChatColor.BOLD + "World" + ChatColor.RESET + ChatColor.BLUE +
	ChatColor.ITALIC + "Master" + ChatColor.RESET + "]: ";
	
	private Commands cmds;
	private ConfigManager configManager;
	
	public Map<UUID, PermissionAttachment> permissions = new HashMap<>();
	
	public FileConfiguration getSaveConfig() {
		return configManager.getSaveConfig();
	}
	
	public void removePlayerFromElevatorBlocks(Player p) {
		cmds.removePlayerFromElevatorBlocks(p);
	}
	public boolean containsElevatorBlock(Player p, Location pos) {
		return cmds.containsElevatorBlock(p, pos);
	}
	public void addElevatorBlock(Player p, Location pos) {
		cmds.addElevatorBlock(p, pos);
	}
	public void removeElevatorBlock(Player p, Location pos) {
		cmds.removeElevatorBlock(p, pos);
	}
	public void removeElevatorSelector(Player p) {
		cmds.removeElevatorSelector(p);
	}
	
	public void saveInventory(Player p, String invName) {
		ItemStack[] items = p.getInventory().getContents();
		for(int i = 0;i < items.length;i++) {
			getSaveConfig().set("player." + p.getUniqueId() + ".inventory." + invName + ".content." + i, items[i]);
		}
		
		items = p.getEnderChest().getContents();
		for(int i = 0;i < items.length;i++) {
			getSaveConfig().set("player." + p.getUniqueId() + ".inventory." + invName + ".ender.chest." + i, items[i]);
		}
		
		getSaveConfig().set("player." + p.getUniqueId() + ".inventory." + invName + ".level", p.getLevel());
		getSaveConfig().set("player." + p.getUniqueId() + ".inventory." + invName + ".xp", p.getExp());
		getSaveConfig().set("player." + p.getUniqueId() + ".inventory." + invName + ".health", p.getHealth());
		getSaveConfig().set("player." + p.getUniqueId() + ".inventory." + invName + ".food_level", p.getFoodLevel());
		getSaveConfig().set("player." + p.getUniqueId() + ".inventory." + invName + ".saturation", p.getSaturation());
		saveSaveConfig();
	}
	public void loadInventory(Player p, String invName) {
		if(getSaveConfig().contains("player." + p.getUniqueId() + ".inventory." + invName)) {
			if(getSaveConfig().contains("player." + p.getUniqueId() + ".inventory." + invName + ".content")) {
				ConfigurationSection itemsSection = getSaveConfig().getConfigurationSection("player." + p.getUniqueId() + ".inventory." + invName + ".content");
				Set<String> savedItems = itemsSection.getKeys(false);
				ItemStack[] items = new ItemStack[p.getInventory().getSize()];
				savedItems.forEach(key -> {
					items[Integer.parseInt(key)] = itemsSection.getItemStack(key);
				});
				p.getInventory().setContents(items);
			}else {
				p.getInventory().clear();
			}
			
			if(getSaveConfig().contains("player." + p.getUniqueId() + ".inventory." + invName + ".ender.chest")) {
				ConfigurationSection itemsSection = getSaveConfig().getConfigurationSection("player." + p.getUniqueId() + ".inventory." + invName + ".ender.chest");
				Set<String> savedItems = itemsSection.getKeys(false);
				ItemStack[] items = new ItemStack[p.getEnderChest().getSize()];
				savedItems.forEach(key -> {
					items[Integer.parseInt(key)] = itemsSection.getItemStack(key);
				});
				p.getEnderChest().setContents(items);
			}else {
				p.getEnderChest().clear();
			}
			
			p.setLevel(getSaveConfig().getInt("player." + p.getUniqueId() + ".inventory." + invName + ".level"));
			p.setExp((float)getSaveConfig().getDouble("player." + p.getUniqueId() + ".inventory." + invName + ".xp"));
			p.setHealth(getSaveConfig().getDouble("player." + p.getUniqueId() + ".inventory." + invName + ".health"));
			p.setFoodLevel(getSaveConfig().getInt("player." + p.getUniqueId() + ".inventory." + invName + ".food_level"));
			p.setSaturation((float)getSaveConfig().getDouble("player." + p.getUniqueId() + ".inventory." + invName + ".saturation"));
		}else {
			p.getInventory().clear();
			p.getEnderChest().clear();
			p.setExp(0);
			p.setLevel(0);
		}
	}
	
	public void loadPermissions(Player p) {
		//Unset all permissions
		PermissionAttachment permAtt = this.permissions.get(p.getUniqueId());
		permAtt.getPermissions().forEach((name, value) -> {
			permAtt.unsetPermission(name);
		});
		
		if(!getSaveConfig().contains("player." + p.getUniqueId() + ".permissions"))
			return;
		
		//Load permissions
		String configStr = getSaveConfig().getString("player." + p.getUniqueId() + ".permissions");
		String[] permissions = configStr.split("###");
		for(String permission:permissions) {
			String[] data = permission.split("##");
			permAtt.setPermission(data[0], Boolean.parseBoolean(data[1]));
		}
	}
	public void loadPermissions() {
		for(Player p:getServer().getOnlinePlayers()) {
			loadPermissions(p);
		}
	}
	public void savePermissions(Player p) {
		StringBuilder permissions = new StringBuilder();
		PermissionAttachment permAtt = this.permissions.get(p.getUniqueId());
		permAtt.getPermissions().forEach((name, value) -> {
			permissions.append(name + "##" + value + "###");
		});
		if(permissions.length() == 0) {
			getSaveConfig().set("player." + p.getUniqueId() + ".permissions", null);
		}else {
			permissions.delete(permissions.length() - 3, permissions.length());
			getSaveConfig().set("player." + p.getUniqueId() + ".permissions", permissions.toString());
		}
		saveSaveConfig();
	}
	public void savePermissions() {
		for(Player p:getServer().getOnlinePlayers()) {
			savePermissions(p);
		}
	}
	
	private void loadConfig() {
		configManager = new ConfigManager(this);
		
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		//Load worlds
		String[] worlds = getSaveConfig().getString("worlds").split("#");
		for(String world:worlds) {
			if(!world.equals("spawn")) {
				new WorldCreator("world_" + world).createWorld();
			}
		}
		
		loadPermissions();
	}
	
	public void reloadSaveConfig() {
		configManager.reloadSaveConfig();
	}
	public void saveSaveConfig() {
		configManager.saveSaveConfig();
	}
	
	@Override
	public void onEnable() {
		cmds = new Commands(this);
		
		for(Player player:getServer().getOnlinePlayers()) {
			permissions.put(player.getUniqueId(), player.addAttachment(this));
		}
		loadConfig();
		
		getCommand(cmds.cmdWorld).setExecutor(cmds);
		getCommand(cmds.cmdPermission).setExecutor(cmds);
		getCommand(cmds.cmdElevator).setExecutor(cmds);
		getCommand(cmds.cmdTeleporter).setExecutor(cmds);
		getCommand(cmds.cmdChunkLoader).setExecutor(cmds);
		getCommand(cmds.cmdInventory).setExecutor(cmds);
		getCommand(cmds.cmdReloadConfig).setExecutor(cmds);
		
		getServer().createBossBar(new NamespacedKey(this, "player_count"), ChatColor.GOLD + "Players online (" + getServer().
		getOnlinePlayers().size() + "/" + getServer().getMaxPlayers() + ")", BarColor.YELLOW, BarStyle.SEGMENTED_20);
		getServer().getBossBar(new NamespacedKey(this, "player_count")).setProgress((double)(getServer().getOnlinePlayers().
		size())/getServer().getMaxPlayers());
		{ //Double player count bossbar fix
			getServer().getOnlinePlayers().forEach(p -> {
				//Remove bossbar
				getServer().getBossBars().forEachRemaining(bb -> {
					bb.removePlayer(p);
				});
			});
			getServer().getOnlinePlayers().forEach(p -> {
				//Set bossbar
				getServer().getBossBars().forEachRemaining(bb -> {
					bb.addPlayer(p);
				});
			});
		}
		
		getServer().getPluginManager().registerEvents(new Event(this), this);
		getServer().getPluginManager().registerEvents(new WorldEvent(this), this);
		getServer().getPluginManager().registerEvents(new ShopEvent(this), this);
		getServer().getPluginManager().registerEvents(new ItemElevatorSelectorEvent(this), this);
		getServer().getPluginManager().registerEvents(new ItemTeleporterEvent(this), this);
		getServer().getPluginManager().registerEvents(new ItemChunkLoaderEvent(this), this);
		
		{
			ConfigurationSection chunk_loader = getSaveConfig().getConfigurationSection("loaded_chunks");
			chunk_loader.getKeys(false).forEach(posTxt -> {
				String[] data = posTxt.split(" x ");
				World world = getServer().getWorld(data[0]);
				Chunk chunk = world.getChunkAt(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
				chunk.setForceLoaded(chunk_loader.getBoolean(posTxt));
				chunk.load(chunk_loader.getBoolean(posTxt));
			});
		}
		
		getServer().getConsoleSender().sendMessage(PLUGIN_NAME + ChatColor.GREEN + "Plugin has been enabled.\n");
	}
	
	@Override
	public void onDisable() {
		savePermissions();
		for(Player player:getServer().getOnlinePlayers()) {
			player.removeAttachment(permissions.remove(player.getUniqueId()));
		}
		
		//Remove all players before deleting
		getServer().getBossBar(new NamespacedKey(this, "player_count")).removeAll();
		getServer().removeBossBar(new NamespacedKey(this, "player_count"));
		
		getServer().getConsoleSender().sendMessage(PLUGIN_NAME + ChatColor.GREEN + "Plugin has been disabled.\n");
	}
}