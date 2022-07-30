package me.jddev0.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.jddev0.Plugin;
import me.jddev0.items.ItemChunkLoader;
import me.jddev0.items.ItemElevatorSelector;
import me.jddev0.items.ItemTeleporter;
import me.jddev0.utils.ChatUtils;
import me.jddev0.world.creator.WorldMasterWorldCreator;

public class Commands implements Listener, TabCompleter, CommandExecutor {
	private Plugin plugin;
	
	public final String cmdWorld = "world";
	//FIXME add allow (add to allowed list)/disallow (add to disallowed list) _command <name> <commands...>
	//FIXME \-> cmdProtection.<world name>.allowed -> string list
	//FIXME \-> cmdProtection.<world name>.disallowed -> string list
	public final String cmdPermission = "permission";
	public final String cmdElevator = "elevator";
	public final String cmdTeleporter = "teleporter";
	public final String cmdChunkLoader = "chunk_loader";
	public final String cmdInventory = "inventory";
	public final String cmdReloadConfig = "reload_config";
	
	public Commands(Plugin plugin) {
		this.plugin = plugin;
	}
	
	private void deleteWorld(File file) {
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for(int i = 0;i < files.length;i++) {
				deleteWorld(files[i]);
			}
		}
		
		file.delete();
	}
	private boolean execCmdWorld(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
			sender.sendMessage(ChatUtils.colorMessage(false, "Only players or the server console can use this command!"));
			
			return true;
		}
		
		if(args.length > 0) {
			String action = args[0];
			if(args.length == 1) {
				if(action.equals("list")) {
					sender.sendMessage("Worlds: ");
					String[] worlds = plugin.getSaveConfig().getString("worlds").split("#");
					for(String world:worlds) {
						sender.sendMessage("-> " + world);
					}
					
					return true;
				}
				
				return false;
			}else if(args.length == 2) {
				String name = args[1];
				
				if(action.equals("remove")) {
					if(!sender.hasPermission("world")) {
						sender.sendMessage(ChatUtils.colorMessage(false, "You haven't enought rights!"));
						
						return true;
					}
					
					if(name.equals("spawn") || name.equals("nether") || name.equals("the_end")) {
						sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " is not allowed!"));
							
						return true;
					}
					
					String[] worlds = plugin.getSaveConfig().getString("worlds").split("#");
					for(String world:worlds) {
						if(world.equals(name)) {
							String newWorlds = "spawn";
							for(String worldName:worlds) {
								if(!worldName.equals(name) && !worldName.equals("spawn")) {
									newWorlds += "#" + worldName;
								}
							}
							
							plugin.getSaveConfig().set("worlds", newWorlds);
							plugin.getSaveConfig().set("world.world_" + name, null);
							plugin.saveSaveConfig();
							
							World worldDelete = Bukkit.getWorld("world_" + name);
							
							//TP all players in world to spawn
							List<Player> players = worldDelete.getPlayers();
							players.forEach(player -> {
								player.sendMessage(ChatUtils.colorMessage(false, "This world (", name,
								") will be removed soon!"));
								
								World worldTp = Bukkit.getWorld("world");
								Location pos = worldTp.getSpawnLocation();
								player.teleport(new Location(worldTp, pos.getX(), pos.getY(), pos.getZ())); 
								player.getLocation().setWorld(worldTp);
								
								player.sendMessage("You are now at the spawn");
							});
							
							plugin.getServer().unloadWorld(worldDelete, false);
							File worldDeleteFolder = worldDelete.getWorldFolder();
							deleteWorld(worldDeleteFolder);
							
							sender.sendMessage(ChatUtils.colorMessage(true, "World ", name, " was successfully removed!"));
							
							return true;
						}
					}
					
					sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " wasn't found!"));
					
					return true;
				}else if(action.equals("renew")) {
					if(!sender.hasPermission("world")) {
						sender.sendMessage(ChatUtils.colorMessage(false, "You haven't enought rights!"));
						
						return true;
					}
					
					if(name.equals("spawn") || name.equals("nether") || name.equals("the_end")) {
						sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, "is not allowed!"));
							
						return true;
					}
					
					String[] worlds = plugin.getSaveConfig().getString("worlds").split("#");
					for(String world:worlds) {
						if(world.equals(name)) {
							String newWorlds = "spawn";
							for(String worldName:worlds) {
								if(!worldName.equals(name) && !worldName.equals("spawn")) {
									newWorlds += "#" + worldName;
								}
							}
							
							plugin.getSaveConfig().set("worlds", newWorlds);
							plugin.saveSaveConfig();
							
							World worldRegenerate = Bukkit.getWorld("world_" + name);
							
							//TP all players in world to spawn
							List<Player> players = worldRegenerate.getPlayers();
							players.forEach(player -> {
								player.sendMessage(ChatUtils.colorMessage(false, "This world (", name,
								") will be regenerated soon!"));
								
								World worldTp = Bukkit.getWorld("world");
								Location pos = worldTp.getSpawnLocation();
								player.teleport(new Location(worldTp, pos.getX(), pos.getY(), pos.getZ())); 
								player.getLocation().setWorld(worldTp);
								
								player.sendMessage("You are now at the spawn");
							});
							
							long seed = worldRegenerate.getSeed();
							long time = worldRegenerate.getFullTime();
							Difficulty difficulty = worldRegenerate.getDifficulty();
							
							plugin.getServer().unloadWorld(worldRegenerate, false);
							deleteWorld(worldRegenerate.getWorldFolder());
							
							WorldCreator creator = new WorldMasterWorldCreator("world_" + name, plugin.getSaveConfig());
							creator.seed(seed);
							World regeneratedWorld = plugin.getServer().createWorld(creator);
							regeneratedWorld.save();
							regeneratedWorld.setFullTime(time);
							regeneratedWorld.setDifficulty(difficulty);
							
							//Add world to list later
							plugin.getSaveConfig().set("worlds", plugin.getSaveConfig().getString("worlds") + "#" + name);
							plugin.saveSaveConfig();
							
							sender.sendMessage(ChatUtils.colorMessage(true, "World ", name, " was successfully regenerated!"));
							
							return true;
						}
					}
					
					sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " wasn't found!"));
					
					return true;
				}else if(action.equals("tp")) {
					if(!(sender instanceof Player)) {
						sender.sendMessage(ChatUtils.colorMessage(false, "Only players can use this command!"));
						
						return true;
					}
					
					Player p = (Player)sender;
					String[] worlds = plugin.getSaveConfig().getString("worlds").split("#");
					for(String world:worlds) {
						if(world.equals(name)) {
							World worldTp = Bukkit.getWorld(name.equals("spawn")?"world":("world_" + name));
							p.teleport(worldTp.getSpawnLocation());
							
							sender.sendMessage(ChatUtils.colorMessageStartingWithValue(true, p.getDisplayName(),
							" was teleported to ", name, "!"));
							
							return true;
						}
					}
					
					sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " wasn't found!"));
					
					return true;
				}
			}else if(args.length == 3) {
				String name = args[1];
				
				if(action.equals("set_join_gamemode")) {
					if(!sender.hasPermission("world")) {
						sender.sendMessage(ChatUtils.colorMessage(false, "You haven't enought rights!"));
						
						return true;
					}
					
					String gamemode = args[2];
					
					List<World> worlds = plugin.getServer().getWorlds();
					for(World world:worlds) {
						String worldName = world.getName();
						if(name.equals(worldName)) {
							try {
								GameMode.valueOf(gamemode.toUpperCase());
							}catch(Exception e) {
								if(!gamemode.equals("no_gamemode")) {
									sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, gamemode,
									" wasn't found!"));
									
									return true;
								}
							}
							
							ConfigurationSection joinGamemodes;
							if(!plugin.getSaveConfig().contains("joinGamemodes")) {
								joinGamemodes = plugin.getSaveConfig().createSection("joinGamemodes");
							}else {
								joinGamemodes = plugin.getSaveConfig().getConfigurationSection("joinGamemodes");
							}
							
							if(gamemode.equals("no_gamemode")) {
								joinGamemodes.set(worldName, null);
								if(joinGamemodes.getKeys(false).size() == 0) {
									plugin.getSaveConfig().set("joinGamemodes", null);
								}
							}else {
								joinGamemodes.set(worldName, gamemode);
							}
							plugin.saveSaveConfig();
							
							sender.sendMessage(ChatUtils.colorMessage(true, "The join gamemode of the world ", name,
							" was successfully changed to ", gamemode, "!"));
							return true;
						}
					}
					
					sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " wasn't found!"));
					return true;
				}else if(action.equals("set_time")) {
					if(!sender.hasPermission("world")) {
						sender.sendMessage(ChatUtils.colorMessage(false, "You haven't enought rights!"));
						
						return true;
					}
					
					String timeRaw = args[2];
					long time;
					try {
						time = Long.parseLong(timeRaw);
					}catch(NumberFormatException e) {
						if(timeRaw.equals("day")) {
							time = 1000;
						}else if(timeRaw.equals("noon")) {
							time = 6000;
						}else if(timeRaw.equals("night")) {
							time = 13000;
						}else if(timeRaw.equals("midnight")) {
							time = 18000;
						}else {
							sender.sendMessage(ChatUtils.colorMessage(false, "Time has do be a number!"));
							
							return true;
						}
					}
					
					List<World> worlds = plugin.getServer().getWorlds();
					for(World world:worlds) {
						String worldName = world.getName();
						if(name.equals(worldName)) {
							world.setFullTime(time);
							
							sender.sendMessage(ChatUtils.colorMessage(true, "The time of the world ", name,
							" was successfully changed to ", time, "!"));
							return true;
						}
					}
					
					sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " wasn't found!"));
					
					return true;
				}else if(action.equals("set_difficulty")) {
					if(!sender.hasPermission("world")) {
						sender.sendMessage(ChatUtils.colorMessage(false, "You haven't enought rights!"));
						
						return true;
					}
					
					String difficultyRaw = args[2];
					Difficulty difficulty = Difficulty.valueOf(difficultyRaw.toUpperCase());
					if(difficulty == null) {
						sender.sendMessage(ChatUtils.colorMessage(false, "The difficulty ", difficultyRaw, " doesn't exist!"));
						return true;
					}
					
					List<World> worlds = plugin.getServer().getWorlds();
					for(World world:worlds) {
						String worldName = world.getName();
						if(name.equals(worldName)) {
							world.setDifficulty(difficulty);
							
							sender.sendMessage(ChatUtils.colorMessage(true, "The difficulty of the world ", name,
							" was successfully changed to ", difficultyRaw, "!"));
							return true;
						}
					}
					
					sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " wasn't found!"));
					
					return true;
				}
			}else if(args.length == 4) {
				String name = args[1];
				String type = args[2];
				String permission = args[3];
				
				if(action.equals("set_permission")) {
					if(!sender.hasPermission("world")) {
						sender.sendMessage(ChatUtils.colorMessage(false, "You haven't enought rights!"));
						
						return true;
					}
					
					if(name.equals("world")) {
						sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " isn't allowed!"));
						
						return true;
					}
					
					List<World> worlds = plugin.getServer().getWorlds();
					for(World world:worlds) {
						String worldName = world.getName();
						if(name.equals(worldName)) {
							
							ConfigurationSection worldPermissions;
							if(!plugin.getSaveConfig().contains("worldPermissions")) {
								worldPermissions = plugin.getSaveConfig().createSection("worldPermissions");
							}else {
								worldPermissions = plugin.getSaveConfig().getConfigurationSection("worldPermissions");
							}
							
							if(type.equals("enter")) {
								worldPermissions.set(name + ".enter", permission.equals("no_permission")?null:permission);
								
								//Add custom permission
								if(!permission.equals("no_permission")) {
									try {
										Bukkit.getPluginManager().addPermission(new Permission(permission, "A custom permission"));
									}catch(IllegalArgumentException e) {}
								}
							}else if(type.equals("build")) {
								worldPermissions.set(name + ".build", permission.equals("no_permission")?null:permission);
								
								//Add custom permission
								if(!permission.equals("no_permission")) {
									try {
										Bukkit.getPluginManager().addPermission(new Permission(permission, "A custom permission"));
									}catch(IllegalArgumentException e) {}
								}
							}else {
								sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, type, " isn't allowed!"));
								
								return true;
							}
							plugin.saveSaveConfig();
							
							sender.sendMessage(ChatUtils.colorMessage(true, "The " + type + " permission of the world ",
							name, " was successfully set to ", permission, "!"));
							return true;
						}
					}
					
					sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " wasn't found!"));
				}
			}
			
			if(args.length > 1) {
				if(args.length < 6) {
					String name = args[1];
					
					if(action.equals("add")) {
						if(!sender.hasPermission("world")) {
							sender.sendMessage(ChatUtils.colorMessage(false, "You haven't enought rights!"));
							
							return true;
						}
						if(!name.matches("[0-9a-zA-Z_]*") || name.equals("nether") || name.equals("the_end")) {
							sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " is not allowed!"));
								
							return true;
						}
						
						String[] worlds = plugin.getSaveConfig().getString("worlds").split("#");
						for(String worldName:worlds) {
							if(worldName.equalsIgnoreCase(name)) {
								sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, name, " already exist!"));
								
								return true;
							}
						}
						
						String dimension = null;
						String type = null;
						String blocks = null;
						if(args.length > 2)
							dimension = args[2];
						if(args.length > 3)
							type = args[3];
						if(args.length > 4)
							blocks = args[4];
						
						WorldMasterWorldCreator creator = new WorldMasterWorldCreator("world_" + name, WorldMasterWorldCreator.
						convertDimension(dimension), WorldMasterWorldCreator.convertType(type), blocks);
						sender.sendMessage(ChatUtils.colorMessage(null, "Creating world ", ChatColor.GOLD, name,
						ChatColor.RESET, "!"));
						
						World world = creator.createWorld();
						world.save();
						
						//Add world to list later
						plugin.getSaveConfig().set("worlds", plugin.getSaveConfig().getString("worlds") + "#" + name);
						
						if(dimension != null)
							plugin.getSaveConfig().set("world.world_" + name + ".gen.dimension", dimension);
						if(type != null)
							plugin.getSaveConfig().set("world.world_" + name + ".gen.type", type);
						if(blocks != null)
							plugin.getSaveConfig().set("world.world_" + name + ".gen.blocks", blocks);
						
						plugin.saveSaveConfig();
						
						sender.sendMessage(ChatUtils.colorMessage(true, "World ", name,
						" was successfully created and added to world list!"));
						
						return true;
					}
				}
				
				if(args.length < 3)
					return false;
					
				String worldName = args[1];
				String[] gamemodes = Arrays.copyOfRange(args, 2, args.length);
				
				if(action.equals("set_allowed_gamemodes")) {
					if(!sender.hasPermission("world")) {
						sender.sendMessage(ChatUtils.colorMessage(false, "You haven't enought rights!"));
						
						return true;
					}
					
					ConfigurationSection allowedGamemodes;
					if(!plugin.getSaveConfig().contains("allowedGamemodes")) {
						allowedGamemodes = plugin.getSaveConfig().createSection("allowedGamemodes");
					}else {
						allowedGamemodes = plugin.getSaveConfig().getConfigurationSection("allowedGamemodes");
					}
					
					List<String> gamemodeList = new LinkedList<String>();
					for(String gamemode:gamemodes) {
						if(gamemode.equals("all_gamemodes")) {
							allowedGamemodes.set(worldName, null);
							if(allowedGamemodes.getKeys(false).size() == 0) {
								plugin.getSaveConfig().set("allowedGamemodes", null);
							}
							plugin.saveSaveConfig();
							
							sender.sendMessage(ChatUtils.colorMessage(true, "All gamemodes are now allowed in the world ",
							worldName, "!"));
							return true;
						}
							
						try {
							GameMode.valueOf(gamemode.toUpperCase());
						}catch(Exception e) {
							if(!gamemode.equals("no_gamemode")) {
								sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, gamemode, " wasn't found!"));
								
								return true;
							}
						}
						
						if(!gamemodeList.contains(gamemode))
							gamemodeList.add(gamemode);
					}
					
					allowedGamemodes.set(worldName, gamemodeList);
					plugin.saveSaveConfig();
					
					sender.sendMessage(ChatUtils.colorMessage(true, "The allowed gamemodes of the world ", worldName,
					" were successfully changed!"));
					return true;
				}
			}
		}
		
		return false;
	}
	private List<String> tabCmdWorld(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> autoComplete = new ArrayList<>();
		
		if(!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
			return new ArrayList<>();
		}
		
		if(args.length == 1) {
			autoComplete.add("list");
			if(sender instanceof Player)
				autoComplete.add("tp");
			
			if(sender.hasPermission("world")) {
				autoComplete.add("add");
				autoComplete.add("remove");
				autoComplete.add("renew");
				autoComplete.add("set_join_gamemode");
				autoComplete.add("set_permission");
				autoComplete.add("set_allowed_gamemodes");
				autoComplete.add("set_time");
				autoComplete.add("set_difficulty");
			}
			
			String start = args[0];
			for(int i = autoComplete.size() - 1;i > -1;i--) {
				if(!autoComplete.get(i).startsWith(start)) {
					autoComplete.remove(i);
				}
			}
			
			return autoComplete;
		}else if(args.length == 2) {
			if(((sender instanceof Player) && args[0].equals("tp")) || (sender.hasPermission("world") &&
			(args[0].equals("renew") || args[0].equals("remove")))) {
				String start = args[1];
				
				String[] worlds = plugin.getSaveConfig().getString("worlds").split("#");
				for(String world:worlds) {
					if(!world.startsWith(start) || ((args[0].equals("renew") || args[0].equals("remove")) && world.equals("spawn"))) {
						continue;
					}
					
					autoComplete.add(world);
				}
			}else if(sender.hasPermission("world") && (args[0].equals("set_join_gamemode") ||
			args[0].equals("set_permission") || args[0].equals("set_allowed_gamemodes") || args[0].equals("set_time") ||
			args[0].equals("set_difficulty"))) {
				String start = args[1];
				
				for(World world:plugin.getServer().getWorlds()) {
					if(!world.getName().startsWith(start)) {
						continue;
					}
					
					autoComplete.add(world.getName());
				}
				
				if(args[0].equals("set_permission"))
					autoComplete.remove("world");
			}else if(sender.hasPermission("world") && args[0].equals("add")) {
				return null;
			}
			
			return autoComplete;
		}else if(args.length == 3) {
			if(args[0].equals("add")) {
				if(sender.hasPermission("world")) {
					autoComplete.add("overworld");
					autoComplete.add("nether");
					autoComplete.add("the_end");
					
					return autoComplete;
				}
			}else if(args[0].equals("set_join_gamemode")) {
				if(sender.hasPermission("world")) {
					String start = args[2];
					if("no_gamemode".startsWith(start))
						autoComplete.add("no_gamemode");
					for(GameMode gm:GameMode.values()) {
						String gmTxt = gm.toString();
						if(gmTxt.toLowerCase().startsWith(start)) {
							autoComplete.add(gmTxt.toLowerCase());
						}
					}
					
					return autoComplete;
				}
			}else if(args[0].equals("set_permission")) {
				if(sender.hasPermission("world")) {
					autoComplete.add("enter");
					autoComplete.add("build");
					
					autoComplete.removeIf(str -> {
						return !str.startsWith(args[2]);
					});
					
					return autoComplete;
				}
			}else if(args[0].equals("set_time")) {
				if(sender.hasPermission("world")) {
					autoComplete.add("day");
					autoComplete.add("noon");
					autoComplete.add("night");
					autoComplete.add("midnight");
					
					autoComplete.removeIf(str -> {
						return !str.startsWith(args[2]);
					});
					
					return autoComplete;
				}
			}else if(args[0].equals("set_difficulty")) {
				if(sender.hasPermission("world")) {
					for(Difficulty difficulty:Difficulty.values()) {
						autoComplete.add(difficulty.toString().toLowerCase());
					}
					
					autoComplete.removeIf(str -> {
						return !str.startsWith(args[2]);
					});
					
					return autoComplete;
				}
			}
		}else if(args.length == 4) {
			if(args[0].equals("add")) {
				if(sender.hasPermission("world") && args[2].equals("overworld")) {
					autoComplete.add("default");
					autoComplete.add("superflat");
					
					return autoComplete;
				}
			}else if(args[0].equals("set_permission")) {
				if(sender.hasPermission("world")) {
					for(PermissionAttachmentInfo perm:sender.getEffectivePermissions()) {
						String permission = perm.getPermission();
						if(permission.startsWith(args[3])) {
							autoComplete.add(permission);
						}
					}
					
					if("no_permission".startsWith(args[3])) {
						autoComplete.add("no_permission");
					}
					
					return autoComplete;
				}
			}
		}else if(args.length == 5) {
			if(args[0].equals("add")) {
				if(sender.hasPermission("world") && args[3].equals("superflat")) {
					autoComplete.add("GRASS");
					autoComplete.add("REDSTONE");
					autoComplete.add("AIR");
					
					return autoComplete;
				}
			}
		}
		
		if(args.length > 2 && sender.hasPermission("world") && args[0].equals("set_allowed_gamemodes")) {
			String[] gamemodes = Arrays.copyOfRange(args, 2, args.length);
			
			for(String gamemode:gamemodes) {
				if(gamemode.equals("all_gamemodes"))
					return autoComplete;
			}
			
			String start = args[args.length - 1];
			if("all_gamemodes".startsWith(start))
				autoComplete.add("all_gamemodes");
			
			completeLoop:
			for(GameMode gm:GameMode.values()) {
				String gmTxt = gm.toString();
				for(String gamemode:gamemodes) {
					if(gamemode.equals(gmTxt.toLowerCase()))
						continue completeLoop;
				}
				if(gmTxt.toLowerCase().startsWith(start)) {
					autoComplete.add(gmTxt.toLowerCase());
				}
			}
			
			return autoComplete;
		}
		
		return autoComplete;
	}
	
	private boolean execCmdPermission(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("permission") || !(sender instanceof Player || sender instanceof ConsoleCommandSender)) {
			sender.sendMessage(ChatUtils.colorMessage(false, "You haven't the permission for this command!"));
			return true;
		}
		
		if(args.length == 3) {
			Player player = Bukkit.getPlayer(args[1]);
			UUID uuid = null;
			String permission = args[2];
			
			if(player == null) {
				for(OfflinePlayer offPlayer:plugin.getServer().getOfflinePlayers()) {
					if(offPlayer.getName().equals(args[1])) {
						uuid = offPlayer.getUniqueId();
						break;
					}
				}
				
				if(uuid == null) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The player with the name ", args[1],
					" doesn't exists!"));
					
					return true;
				}
			}else {
				uuid = player.getUniqueId();
			}
			
			if(args[0].equals("allow")) {
				if(plugin.permissions.containsKey(uuid)) {
					plugin.permissions.get(uuid).setPermission(permission, true);
					plugin.savePermissions(player);
					
					//Updates the tab completion list
					player.updateCommands();
				}else {
					List<String> permissions;
					if(plugin.getSaveConfig().contains("player." + uuid + ".permissions")) {
						String configStr = plugin.getSaveConfig().getString("player." + uuid + ".permissions");
						permissions = new LinkedList<>(Arrays.asList(configStr.split("###")));
					}else {
						permissions = new LinkedList<>();
					}
					
					//Set permission "permission" to true
					boolean flag = true;
					for(int i = 0;i < permissions.size();i++) {
						String permissionName = permissions.get(i).split("##")[0];
						
						if(permissionName.equals(permission)) {
							permissions.set(i, permissionName + "##true");
							flag = false;
							break;
						}
					}
					if(flag) {
						permissions.add(permission + "##true");
					}
					
					if(permissions.size() == 0) {
						plugin.getSaveConfig().set("player." + uuid + ".permissions", null);
					}else {
						StringBuilder permissionsBuilder = new StringBuilder();
						for(String perm:permissions) {
							permissionsBuilder.append(perm);
							permissionsBuilder.append("###");
						}
						permissionsBuilder.delete(permissionsBuilder.length() - 3, permissionsBuilder.length());
						plugin.getSaveConfig().set("player." + uuid + ".permissions", permissionsBuilder.toString());
					}
					plugin.saveSaveConfig();
				}
				
				sender.sendMessage(ChatUtils.colorMessage(true, "The value of ", permission, " of ", args[1],
				" is now set to ", "true", "!"));
				
				return true;
			}else if(args[0].equals("disallow")) {
				if(plugin.permissions.containsKey(uuid)) {
					plugin.permissions.get(uuid).setPermission(permission, false);
					plugin.savePermissions(player);
					
					//Updates the tab completion list
					player.updateCommands();
				}else {
					List<String> permissions;
					if(plugin.getSaveConfig().contains("player." + uuid + ".permissions")) {
						String configStr = plugin.getSaveConfig().getString("player." + uuid + ".permissions");
						permissions = new LinkedList<>(Arrays.asList(configStr.split("###")));
					}else {
						permissions = new LinkedList<>();
					}
					
					//Set permission "permission" to true
					boolean flag = true;
					for(int i = 0;i < permissions.size();i++) {
						String permissionName = permissions.get(i).split("##")[0];
						
						if(permissionName.equals(permission)) {
							permissions.set(i, permissionName + "##false");
							flag = false;
							break;
						}
					}
					if(flag) {
						permissions.add(permission + "##false");
					}
					
					if(permissions.size() == 0) {
						plugin.getSaveConfig().set("player." + uuid + ".permissions", null);
					}else {
						StringBuilder permissionsBuilder = new StringBuilder();
						for(String perm:permissions) {
							permissionsBuilder.append(perm);
							permissionsBuilder.append("###");
						}
						permissionsBuilder.delete(permissionsBuilder.length() - 3, permissionsBuilder.length());
						plugin.getSaveConfig().set("player." + uuid + ".permissions", permissionsBuilder.toString());
					}
					plugin.saveSaveConfig();
				}
				
				sender.sendMessage(ChatUtils.colorMessage(true, "The value of ", permission, " of ", args[1],
				" is now set to ", "false", "!"));
				return true;
			}else if(args[0].equals("default")) {
				if(plugin.permissions.containsKey(uuid)) {
					plugin.permissions.get(uuid).unsetPermission(permission);
					plugin.savePermissions(player);
					
					//Updates the tab completion list
					player.updateCommands();
				}else {
					List<String> permissions;
					if(plugin.getSaveConfig().contains("player." + uuid + ".permissions")) {
						String configStr = plugin.getSaveConfig().getString("player." + uuid + ".permissions");
						permissions = new LinkedList<>(Arrays.asList(configStr.split("###")));
					}else {
						permissions = new LinkedList<>();
					}
					
					//Set permission "permission" to true
					for(int i = 0;i < permissions.size();i++) {
						String permissionName = permissions.get(i).split("##")[0];
						
						if(permissionName.equals(permission)) {
							permissions.remove(i);
							break;
						}
					}
					
					if(permissions.size() == 0) {
						plugin.getSaveConfig().set("player." + uuid + ".permissions", null);
					}else {
						StringBuilder permissionsBuilder = new StringBuilder();
						for(String perm:permissions) {
							permissionsBuilder.append(perm);
							permissionsBuilder.append("###");
						}
						permissionsBuilder.delete(permissionsBuilder.length() - 3, permissionsBuilder.length());
						plugin.getSaveConfig().set("player." + uuid + ".permissions", permissionsBuilder.toString());
					}
					plugin.saveSaveConfig();
				}
				
				sender.sendMessage(ChatUtils.colorMessage(true, "The value of ", permission, " of ", args[1],
				" is now set to ", "default", "!"));
				return true;
			}
		}
		return false;
	}
	private List<String> tabCmdPermission(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> autoComplete = new ArrayList<>();
		
		if(!sender.hasPermission("permission") || !(sender instanceof Player || sender instanceof ConsoleCommandSender)) {
			return autoComplete;
		}
		
		if(args.length == 1) {
			autoComplete.add("allow");
			autoComplete.add("disallow");
			autoComplete.add("default");
			
			String start = args[0];
			for(int i = autoComplete.size() - 1;i > -1;i--) {
				if(!autoComplete.get(i).startsWith(start)) {
					autoComplete.remove(i);
				}
			}
			
			return autoComplete;
		}else if(args.length == 2) {
			for(Player player:plugin.getServer().getOnlinePlayers()) {
				if(player.getName().startsWith(args[1])) {
					autoComplete.add(player.getName());
				}
			}for(OfflinePlayer player:plugin.getServer().getOfflinePlayers()) {
				if(player.getName().startsWith(args[1])) {
					autoComplete.add(player.getName());
				}
			}
			
			return autoComplete;
		}else if(args.length == 3) {
			for(PermissionAttachmentInfo perm:sender.getEffectivePermissions()) {
				String permission = perm.getPermission();
				if(permission.startsWith(args[2])) {
					autoComplete.add(permission);
				}
			}
			
			return autoComplete;
		}
		
		return autoComplete;
	}
	
	private Map<UUID, List<Location>> elevatorBlocks = new HashMap<>();
	
	public void removePlayerFromElevatorBlocks(Player p) {
		if(elevatorBlocks.containsKey(p.getUniqueId()))
			elevatorBlocks.remove(p.getUniqueId());
	}
	public boolean containsElevatorBlock(Player p, Location pos) {
		if(elevatorBlocks.containsKey(p.getUniqueId()))
			return elevatorBlocks.get(p.getUniqueId()).contains(pos);
		return false;
	}
	public void addElevatorBlock(Player p, Location pos) {
		if(elevatorBlocks.containsKey(p.getUniqueId()))
			elevatorBlocks.get(p.getUniqueId()).add(pos);
	}
	public void removeElevatorBlock(Player p, Location pos) {
		if(elevatorBlocks.containsKey(p.getUniqueId()))
			elevatorBlocks.get(p.getUniqueId()).remove(pos);
	}
	public void removeElevatorSelector(Player p) {
		PlayerInventory inv = p.getInventory();
		for(int i = 0;i < inv.getSize();i++) {
			if(inv.getItem(i) == null)
				continue;
			if(inv.getItem(i).getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Elevator Selector")) {
				inv.setItem(i, new ItemStack(Material.AIR));
			}
		}
	}
	public List<Vector> getElevatorDoorBlocks(String elevatorName, String floorName) {
		ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
		ConfigurationSection elevator = elevators.getConfigurationSection(elevatorName);
		
		List<Vector> doorBlocks = new LinkedList<>();
		if(elevator.getConfigurationSection("floors").isVector(floorName)) {
			doorBlocks.add(elevator.getConfigurationSection("floors").getVector(floorName));
		}else {
			List<?> elevatorBlocks = new LinkedList<>(elevator.getConfigurationSection("floors").getList(floorName));
			elevatorBlocks.forEach(obj -> {
				String[] data = ((String)obj).split("#");
				doorBlocks.add(new Vector(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])));
			});
			
		}
		
		return doorBlocks;
	}
	public void moveElevator(String elevatorName, String floorName) {
		moveElevator(elevatorName, floorName, 1);
	}
	public void moveElevator(String elevatorName, String floorName, double timePerBlock) {
		ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
		ConfigurationSection elevator = elevators.getConfigurationSection(elevatorName);
		elevator.set("moving", true);
		plugin.saveSaveConfig();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				List<?> elevatorBlocks = new LinkedList<>(elevator.getList("blocks"));
				World world = plugin.getServer().getWorld(elevator.getString("world"));
				
				int elevatorY = elevator.getInt("floorHeight");
				List<Vector> doorBlocks = getElevatorDoorBlocks(elevatorName, floorName);
				Vector mainDoorPos = doorBlocks.get(0);
				int floorY = mainDoorPos.getBlockY();
				double timeNeeds = 20/timePerBlock*Math.abs(floorY - elevatorY);
				Vector velocity = new Vector(0, (floorY - elevatorY)/timeNeeds, 0);
				Map<Vector, FallingBlock> movingElevatorBlocks = new HashMap<>();
				List<Entity> entities = new LinkedList<>();
				List<BlockData> bDataList = new LinkedList<>();
				elevatorBlocks.forEach(obj -> {
					String[] data = ((String)obj).split("#");
					Vector blockPos = new Vector(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]));
					Location location = new Location(world, blockPos.getBlockX(), blockPos.getBlockY(), blockPos.getBlockZ());
					Collection<Entity> nearbyEntities = world.getNearbyEntities(location, 1, 2, 1);
					nearbyEntities.forEach(e -> {
						e.setGravity(false);
					});
					entities.addAll(nearbyEntities);
					
					bDataList.add(world.getBlockAt(location).getBlockData());
				});
				
				for(int i = 0;i < elevatorBlocks.size();i++) {
					Object obj = elevatorBlocks.get(i);
					String[] data = ((String)obj).split("#");
					Vector blockPos = new Vector(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]));
					Location location = new Location(world, blockPos.getBlockX(), blockPos.getBlockY(), blockPos.getBlockZ());
					Block block = world.getBlockAt(location);
					block.setType(Material.AIR);
					
					Location locationWithCenterOffset = new Location(world, location.getBlockX() + .5, location.getBlockY(),
					location.getBlockZ() + .5);
					FallingBlock fallingBlock = world.spawnFallingBlock(locationWithCenterOffset, bDataList.get(i));
					fallingBlock.setGravity(false);
					fallingBlock.setVelocity(velocity);
					fallingBlock.setDropItem(false);
					
					blockPos.setY(location.getBlockY() + floorY - elevatorY);
					movingElevatorBlocks.put(blockPos, fallingBlock);
				}
				int taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
					public void run() {
						movingElevatorBlocks.forEach((v, fb) -> {
							fb.setTicksLived(1);
							fb.setFallDistance(0);
							fb.setVelocity(velocity);
						});
						entities.forEach(e -> {
							e.setFallDistance(0);
							e.setVelocity(e.getVelocity().setY(velocity.getY()));
						});
					}
				}, 0, 0);
				
				new BukkitRunnable() {
					@Override
					public void run() {
						Bukkit.getServer().getScheduler().cancelTask(taskID);
						
						List<String> elevatorBlockVectors = new LinkedList<>();
						
						movingElevatorBlocks.keySet().forEach(vector -> {
							FallingBlock fallingBlock = movingElevatorBlocks.get(vector);
							BlockData bData = fallingBlock.getBlockData();
							fallingBlock.remove();
							
							Location location = new Location(world, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
							Block block = world.getBlockAt(location);
							block.setBlockData(bData);
							elevatorBlockVectors.add(block.getX() + "#" + block.getY() + "#" + block.getZ());
						});
						
						//Open door(s)
						new BukkitRunnable() {
							@Override
							public void run() {
								doorBlocks.forEach(doorPos -> {
									Block doorBlock = world.getBlockAt(doorPos.getBlockX(), doorPos.getBlockY() + 1,
									doorPos.getBlockZ());
									if(doorBlock.getBlockData() instanceof Openable) {
										Openable door = (Openable)doorBlock.getBlockData();
										door.setOpen(true);
										doorBlock.setBlockData(door);
									}
								});
							}
						}.runTaskLater(plugin, 20);
						
						//Stop player movement
						entities.forEach(e -> {
							e.setFallDistance(0);
							e.setVelocity(new Vector(0, .1, 0));
						});
						new BukkitRunnable() {
							@Override
							public void run() {
								entities.forEach(e -> {
									e.setFallDistance(0);
									e.setGravity(true);
									e.setVelocity(new Vector(0, 0, 0));
								});
							}
						}.runTaskLater(plugin, 5);
						
						//Save data
						new BukkitRunnable() {
							@Override
							public void run() {
								elevator.set("moving", false);
								elevator.set("floorHeight", floorY);
								elevator.set("blocks", elevatorBlockVectors);
								plugin.saveSaveConfig();
							}
						}.runTaskLater(plugin, 40);
					}
				}.runTaskLater(plugin, (int)timeNeeds);
			};
		}.runTaskLater(plugin, 20);
	}
	public String getNearestElevator(Player p) {
		return getNearestElevator(p, 5);
	}
	public String getNearestElevator(Player p, double maxDistance) {
		ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
		Vector playerPos = new Vector(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
		String playerWorld = p.getWorld().getName();
		
		double[] fewestDistance = new double[] {maxDistance};
		String[] elevatorName = new String[] {null};
		
		elevators.getKeys(false).forEach(actualElevatorName -> {
			ConfigurationSection elevator = elevators.getConfigurationSection(actualElevatorName);
			if(!playerWorld.equals(elevator.getString("world"))) {
				return;
			}
			List<?> blocks = elevator.getList("blocks");
			blocks.forEach(data -> {
				String[] coords = ((String)data).split("#");
				Vector vector = new Vector(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
				//Without y distance
				double distance = Math.sqrt(Math.pow(playerPos.getX() - vector.getX(), 2) + Math.pow(playerPos.getZ() - vector.getZ(), 2));
				if(distance <= fewestDistance[0]) {
					fewestDistance[0] = distance;
					elevatorName[0] = actualElevatorName;
				}
			});
		});
		
		return elevatorName[0];
	}
	private boolean execCmdElevator(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatUtils.colorMessage(false, "Only players can use this command!"));
			
			return true;
		}
		
		Player p = (Player)sender;
		if(args.length > 0) {
			String action = args[0];
			
			if(action.equals("move")) {
				if(args.length == 2) {
					String elevatorName = getNearestElevator(p);
					if(elevatorName == null) {
						p.sendMessage(ChatUtils.colorMessage(false, "No elevator was found near to you!"));
						return true;
					}
					ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
					ConfigurationSection elevator = elevators.getConfigurationSection(elevatorName);
					if(!elevator.contains("floors")) {
						p.sendMessage(ChatUtils.colorMessage(false, "Elevator ", elevatorName, " hasn't floors!"));
						return true;
					}
					ConfigurationSection floors = elevator.getConfigurationSection("floors");
					if(!floors.contains(args[1])) {
						p.sendMessage(ChatUtils.colorMessage(false, "The Floor ", args[1], " dosen't exist!"));
						return true;
					}
					if(floors.contains("permission." + args[1]) && !p.hasPermission(floors.getString("permission." + args[1]))) {
						p.sendMessage(ChatUtils.colorMessage(false, "You need the permission ", floors.getString(
						"permission." + args[1]), " to go to floor ", args[1], "!"));
						return true;
					}
					
					String[] actualFloor = new String[] {null};
					floors.getKeys(false).forEach(floor -> {
						if(floor.equals("permission"))
							return;
						
						int y = getElevatorDoorBlocks(elevatorName, floor).get(0).getBlockY();
						if(y == elevator.getInt("floorHeight")) {
							actualFloor[0] = floor;
						}
					});
					
					if(elevator.getBoolean("moving")) {
						p.sendMessage(ChatUtils.colorMessage(false, "The elevator ", elevatorName, " is already moving!"));
						return true;
					}
					
					if(actualFloor[0] != null) {
						if(actualFloor[0].equals(args[1])) {
							p.sendMessage(ChatUtils.colorMessage(false, "The elevator ", elevatorName,
							" is already on the floor ", args[1], "!"));
							return true;
						}
						
						//Close door(s)
						List<Vector> doorBlocks = getElevatorDoorBlocks(elevatorName, actualFloor[0]);
						doorBlocks.forEach(doorLocation -> {
							Block doorBlock = plugin.getServer().getWorld(elevator.getString("world")).getBlockAt(doorLocation.
							getBlockX(), doorLocation.getBlockY() + 1, doorLocation.getBlockZ());
							if(doorBlock.getBlockData() instanceof Openable) {
								Openable door = (Openable)doorBlock.getBlockData();
								door.setOpen(false);
								doorBlock.setBlockData(door);
							}
						});
					}
					
					if(elevator.contains("speed")) {
						moveElevator(elevatorName, args[1], elevator.getDouble("speed"));
					}else {
						moveElevator(elevatorName, args[1]);
					}
					
					return true;
				}
			}
			
			if(p.hasPermission("elevator")) {
				if(action.equals("make")) {
					if(args.length == 1) {
						if(elevatorBlocks.containsKey(p.getUniqueId())) {
							sender.sendMessage(ChatUtils.colorMessage(false, "You are already constructing an elevator!"));
							return true;
						}
						
						elevatorBlocks.put(p.getUniqueId(), new LinkedList<>());
						p.getInventory().addItem(new ItemElevatorSelector());
						
						return true;
					}
					
					return false;
				}else if(action.equals("create")) {
					if(args.length == 3) {
						if(!elevatorBlocks.containsKey(p.getUniqueId())) {
							sender.sendMessage(ChatUtils.colorMessage(false, "You have to select blocks with ",
							" /elevator make", "!"));
							return true;
						}
						
						List<Location> elevatorBlocks = this.elevatorBlocks.get(p.getUniqueId());
						if(elevatorBlocks.size() == 0) {
							sender.sendMessage(ChatUtils.colorMessage(false,
							"You have to select blocks with the selector stick!"));
							return true;
						}
						
						String name = args[1];
						int floorHeight;
						try {
							floorHeight = Integer.parseInt(args[2]);
						}catch (NumberFormatException e) {
							sender.sendMessage(ChatUtils.colorMessage(false, "Height has to be a number!"));
							return true;
						}
						
						if(floorHeight < 0) {
							sender.sendMessage(ChatUtils.colorMessage(false, "Height has to be a positive number!"));
							return true;
						}
						World world = elevatorBlocks.get(0).getWorld();
						if(floorHeight >= world.getMaxHeight()) {
							sender.sendMessage(ChatUtils.colorMessage(false, "Floor height is to big! Maximum: ",
							ChatColor.GOLD, world.getMaxHeight() - 1));
							return true;
						}
						
						ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
						if(elevators.contains(name)) {
							sender.sendMessage(ChatUtils.colorMessage(false, "The Elevator ", name," already exist!"));
							return true;
						}
						
						for(Location block:elevatorBlocks) {
							if(!block.getWorld().getName().equals(world.getName())) {
								sender.sendMessage(ChatUtils.colorMessage(false,
								"Blocks of elevator are in different worlds!"));
								return true;
							}
						}
						
						ConfigurationSection elevator = elevators.createSection(name);
						elevator.set("moving", false);
						elevator.set("world", world.getName());
						elevator.set("floorHeight", floorHeight);
						ArrayList<String> elevatorBlockVectors = new ArrayList<String>();
						for(Location block:elevatorBlocks) {
							elevatorBlockVectors.add(block.getBlockX() + "#" + block.getBlockY() + "#" + block.getBlockZ());
						}
						elevator.set("blocks", elevatorBlockVectors);
						plugin.saveSaveConfig();
						removeElevatorSelector(p);
						removePlayerFromElevatorBlocks(p);
						
						sender.sendMessage(ChatUtils.colorMessage(true, "The Elevator ", name,
						" was successfully created with ", elevatorBlocks.size(), " blocks!"));
						return true;
					}
				}else if(action.equals("remove")) {
					if(args.length == 2) {
						String name = args[1];
						ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
						if(elevators.contains(name)) {
							elevators.set(name, null);
							plugin.saveSaveConfig();
							
							sender.sendMessage(ChatUtils.colorMessage(true, "The Elevator ", name,
							" was successfully removed!"));
							return true;
						}else {
							sender.sendMessage(ChatUtils.colorMessage(false, "The Elevator ", name, " doesn't exist!"));
							return true;
						}
					}
				}else if(action.equals("add_floor")) {
					if(args.length == 6 || args.length == 7) {
						String elevatorName = args[1];
						ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
						if(!elevators.contains(elevatorName)) {
							sender.sendMessage(ChatUtils.colorMessage(false, "The Elevator ", elevatorName,
							" doesn't exist!"));
							return true;
						}
						ConfigurationSection elevator = elevators.getConfigurationSection(elevatorName);
						World world = plugin.getServer().getWorld(elevator.getString("world"));
						
						String floorName = args[2];
						if(elevator.contains("floors")) {
							if(elevator.getConfigurationSection("floors").contains(floorName)) {
								sender.sendMessage(ChatUtils.colorMessage(false, "The Floor ", floorName, " already exist!"));
								return true;
							}
						}
						if(floorName.equals("permission")) {
							sender.sendMessage(ChatUtils.colorMessage(false, "The Name ", floorName, " isn't allowed!"));
							return true;
						}
						
						int[] doorPos = new int[3];
						for(int i = 0;i < 3;i++) {
							try {
								doorPos[i] = Integer.parseInt(args[3 + i]);
							}catch (NumberFormatException e) {
								sender.sendMessage(ChatUtils.colorMessageStartingWithValue(false, args[3 + i],
								" isn't a number!"));
								return true;
							}
						}
						if(doorPos[1] < 0) {
							sender.sendMessage(ChatUtils.colorMessage(false, "Height has to be a positive number!"));
							return true;
						}
						if(doorPos[1] >= world.getMaxHeight()) {
							sender.sendMessage(ChatUtils.colorMessage(false, "Height is to big! Maximum: ",
							world.getMaxHeight() - 1));
							return true;
						}
						
						Block block = world.getBlockAt(doorPos[0], doorPos[1], doorPos[2]);
						if(block.getType() != Material.IRON_DOOR) {
							sender.sendMessage(ChatUtils.colorMessage(false, "At the location ", doorPos[0] + " " +
							doorPos[1] + " " + doorPos[2], " is no iron door!"));
							return true;
						}
						
						//Get block under door
						doorPos[1]--;
						if(world.getBlockAt(doorPos[0], doorPos[1], doorPos[2]).getType() == Material.IRON_DOOR) {
							//If block at coordinates was upper half of door
							doorPos[1]--;
						}
						
						//Open door if new floor is at elevator's height
						if(elevator.getInt("floorHeight") == doorPos[1]) {
							Openable door = (Openable)block.getBlockData();
							door.setOpen(true);
							block.setBlockData(door);
						}
						
						if(!elevator.contains("floors")) {
							elevator.createSection("floors");
						}
						ConfigurationSection floors = elevator.getConfigurationSection("floors");
						floors.set(floorName, new Vector(doorPos[0], doorPos[1], doorPos[2]));
						if(args.length == 7) {
							String permission = args[6];
							floors.set("permission." + floorName, permission);
							
							//Add custom permission
							try {
								Bukkit.getPluginManager().addPermission(new Permission(permission, "A custom permission"));
							}catch(IllegalArgumentException e) {}
						}
						plugin.saveSaveConfig();
						
						sender.sendMessage(ChatUtils.colorMessage(true, "The Floor ", floorName,
						" was successfully added to the elevator ", elevatorName, "!"));
						return true;
					}
				}else if(action.equals("remove_floor")) {
					if(args.length == 3) {
						String elevatorName = args[1];
						ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
						if(!elevators.contains(elevatorName)) {
							sender.sendMessage(ChatUtils.colorMessage(false, "The Elevator ", elevatorName,
							" doesn't exist!"));
							return true;
						}
						ConfigurationSection elevator = elevators.getConfigurationSection(elevatorName);
						String floorName = args[2];
						if(floorName.equals("permission") || !elevator.contains("floors") || !elevator.
						getConfigurationSection("floors").contains(floorName)) {
							sender.sendMessage(ChatUtils.colorMessage(false, "The Floor ", floorName, " dosen't exist!"));
							return true;
						}
						
						ConfigurationSection floors = elevator.getConfigurationSection("floors");
						floors.set(floorName, null);
						floors.set("permission." + floorName, null);
						if(floors.contains("permission") && floors.getConfigurationSection("permission").getKeys(false).
						size() == 0)
							floors.set("permission", null);
						
						if(floors.getKeys(false).size() == 0)
							elevator.set("floors", null);
						plugin.saveSaveConfig();
						
						sender.sendMessage(ChatUtils.colorMessage(true, "The Floor ", floorName,
						" was successfully removed from the elevator ", elevatorName, "!"));
						return true;
					}
				}else if(action.equals("set_speed")) {
					if(args.length == 3) {
						String elevatorName = args[1];
						ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
						if(!elevators.contains(elevatorName)) {
							sender.sendMessage(ChatUtils.colorMessage(false, "The Elevator ", elevatorName, "doesn't exist!"));
							return true;
						}
						ConfigurationSection elevator = elevators.getConfigurationSection(elevatorName);
						
						double speed;
						try {
							speed = Double.parseDouble(args[2]);
						}catch (NumberFormatException e) {
							sender.sendMessage(ChatUtils.colorMessage(false, "Speed has to be a number!"));
							return true;
						}
						
						if(speed <= 0) {
							sender.sendMessage(ChatUtils.colorMessage(false, "Speed has to be larger than 0!"));
							return true;
						}
						
						elevator.set("speed", speed);
						plugin.saveSaveConfig();
						
						sender.sendMessage(ChatUtils.colorMessage(true, "The speed of the elevator ", elevatorName,
						" was successfully changed to ", speed + " blocks/sec" , "!"));
						return true;
					}
				}
			}else if(!action.equals("move")) {
				sender.sendMessage(ChatUtils.colorMessage(false, "You haven't the permission to call ", " /elevator " +
				action, "!"));
				return true;
			}
		}
		return false;
	}
	private List<String> tabCmdElevator(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> autoComplete = new ArrayList<>();
		
		if(!(sender instanceof Player)) {
			return autoComplete;
		}
		
		Player p = (Player)sender;
		if(args.length == 1) {
			autoComplete.add("move");
			if(p.hasPermission("elevator")) {
				autoComplete.add("create");
				autoComplete.add("make");
				autoComplete.add("remove");
				autoComplete.add("add_floor");
				autoComplete.add("remove_floor");
				autoComplete.add("set_speed");
			}
			
			String start = args[0];
			for(int i = autoComplete.size() - 1;i > -1;i--) {
				if(!autoComplete.get(i).startsWith(start)) {
					autoComplete.remove(i);
				}
			}
			
			return autoComplete;
		}else if(args.length == 2) {
			if(args[0].equals("move")) {
				String elevatorName = getNearestElevator(p);
				if(elevatorName == null) {
					autoComplete.add(ChatUtils.colorMessage(false, "No elevator was found near to you!"));
					return autoComplete;
				}
				ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
				ConfigurationSection elevator = elevators.getConfigurationSection(elevatorName);
				if(!elevator.contains("floors")) {
					autoComplete.add(ChatUtils.colorMessage(false, "The Elevator ", elevatorName, " hasn't floors!"));
					return autoComplete;
				}
				ConfigurationSection floors = elevator.getConfigurationSection("floors");
				floors.getKeys(false).forEach(floor -> {
					if(floor.startsWith(args[1]) && !floor.equals("permission") && (!floors.contains("permission." + floor) ||
					p.hasPermission(floors.getString("permission." + floor)))) {
						autoComplete.add(floor);
					}
				});
			}
			
			if(p.hasPermission("elevator")) {
				if(args[0].equals("create")) {
					return null;
				}else if(args[0].equals("remove") || args[0].equals("add_floor") || args[0].equals("remove_floor") ||
				args[0].equals("set_speed")) {
					ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
					elevators.getKeys(false).forEach(name -> {
						autoComplete.add(name);
					});
					
					String start = args[1];
					for(int i = autoComplete.size() - 1;i > -1;i--) {
						if(!autoComplete.get(i).startsWith(start)) {
							autoComplete.remove(i);
						}
					}
				}
			}
		}else if(args.length == 3) {
			if(p.hasPermission("elevator")) {
				if(args[0].equals("create")) {
					autoComplete.add((int)(p.getLocation().getY() - 1) + "");
					
					return autoComplete;
				}else if(args[0].equals("add_floor")) {
					return null;
				}
				
				if(args[0].equals("remove_floor")) {
					ConfigurationSection elevators = plugin.getSaveConfig().getConfigurationSection("elevators");
					if(!elevators.contains(args[1])) {
						autoComplete.add(ChatUtils.colorMessage(false, "The Elevator ", args[1], " doesn't exist!"));
						return autoComplete;
					}
					ConfigurationSection elevator = elevators.getConfigurationSection(args[1]);
					if(!elevator.contains("floors")) {
						autoComplete.add(ChatUtils.colorMessage(false, "The Elevator ", args[1], " hasn't floors!"));
						return autoComplete;
					}
					ConfigurationSection floors = elevator.getConfigurationSection("floors");
					floors.getKeys(false).forEach(floor -> {
						if(floor.startsWith(args[2]) && !floor.equals("permission")) {
							autoComplete.add(floor);
						}
					});
				}
			}
		}else if(args.length == 4) {
			if(p.hasPermission("elevator")) {
				if(args[0].equals("add_floor")) {
					Block b = p.getTargetBlock(null, 10);
					if(b == null) {
						autoComplete.add("" + p.getLocation().getBlockX());
					}else {
						autoComplete.add("" + b.getLocation().getBlockX());
					}
				}
			}
		}else if(args.length == 5) {
			if(p.hasPermission("elevator")) {
				if(args[0].equals("add_floor")) {
					Block b = p.getTargetBlock(null, 10);
					if(b == null) {
						autoComplete.add("" + p.getLocation().getBlockY());
					}else {
						autoComplete.add("" + b.getLocation().getBlockY());
					}
				}
			}
		}else if(args.length == 6) {
			if(p.hasPermission("elevator")) {
				if(args[0].equals("add_floor")) {
					Block b = p.getTargetBlock(null, 10);
					if(b == null) {
						autoComplete.add("" + p.getLocation().getBlockZ());
					}else {
						autoComplete.add("" + b.getLocation().getBlockZ());
					}
				}
			}
		}else if(args.length == 7) {
			if(p.hasPermission("elevator")) {
				if(args[0].equals("add_floor")) {
					for(PermissionAttachmentInfo perm:sender.getEffectivePermissions()) {
						String permission = perm.getPermission();
						if(permission.startsWith(args[6])) {
							autoComplete.add(permission);
						}
					}
				}
			}
		}
		
		return autoComplete;
	}
	
	private boolean execCmdTeleporter(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatUtils.colorMessage(false, "Only players can use this command!"));
			
			return true;
		}
		
		Player p = (Player)sender;
		if(args.length > 0) {
			String action = args[0];
			
			if(action.equals("give")) {
				if(args.length == 2) {
					String name = args[1];
					
					ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
					if(!teleporters.contains(name)) {
						p.sendMessage(ChatUtils.colorMessage(false, "The teleporter ", name, " doesn't exsists!"));
						
						return true;
					}
					
					if(name.equals("positions")) {
						p.sendMessage(ChatUtils.colorMessage(false, "The name ", name, " isn't allowed!"));
						
						return true;
					}
					
					p.getInventory().addItem(new ItemTeleporter(ChatColor.BOLD + "" + ChatColor.GOLD + name, teleporters.
					getConfigurationSection(name).getString("icon")));
					
					return true;
				}
			}
			
			if(p.hasPermission("teleporter")) {
				if(action.equals("create")) {
					if(args.length == 3) {
						String name = args[1];
						String icon = args[2];
						
						Material m = Material.matchMaterial(icon);
						if(m == null || m.toString().startsWith("LEGACY_") || !m.isItem() || m.isAir()) {
							p.sendMessage(ChatUtils.colorMessage(false, "The icon ", icon, " doesn't exsists!"));
							
							return true;
						}
						
						if(name.equals("positions")) {
							p.sendMessage(ChatUtils.colorMessage(false, "The name ", name, "isn't allowed!"));
							
							return true;
						}
						
						ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
						if(teleporters.contains(name)) {
							p.sendMessage(ChatUtils.colorMessage(false, "The teleporter ", name + " already exsists!"));
							
							return true;
						}
						
						ConfigurationSection teleporter = teleporters.createSection(name);
						teleporter.set("icon", icon);
						teleporter.createSection("slots");
						plugin.saveSaveConfig();
						
						p.sendMessage(ChatUtils.colorMessage(true, "The teleporter ", name, " with the icon ",
						icon, " was successfully created!"));
						
						return true;
					}
				}else if(action.equals("delete")) {
					if(args.length == 2) {
						String name = args[1];
						
						ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
						if(!teleporters.contains(name)) {
							p.sendMessage(ChatUtils.colorMessage(false, "The teleporter ", name, " doesn't exsists!"));
							
							return true;
						}
						
						if(name.equals("positions")) {
							p.sendMessage(ChatUtils.colorMessage(false, "The name ", name, " isn't allowed!"));
							
							return true;
						}
						
						teleporters.set(name, null);
						plugin.saveSaveConfig();
						
						p.sendMessage(ChatUtils.colorMessage(true, "The teleporter ", name, " was successfully deleted!"));
						
						return true;
					}
				}else if(action.equals("add")) {
					if(args.length == 4) {
						String name = args[1];
						String positionName = args[2];
						String slotTxt = args[3];
						int slot;
						try {
							slot = Integer.parseInt(slotTxt);
						}catch(NumberFormatException e) {
							p.sendMessage(ChatUtils.colorMessageStartingWithValue(false, slotTxt, " isn't a number!"));
							
							return true;
						}
						
						if(slot < 1 || slot > 55) {
							p.sendMessage(ChatUtils.colorMessage(false, "The slot ", slot, " doesn't exist!"));
						}
						slot--;
						
						ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
						if(!teleporters.contains(name)) {
							p.sendMessage(ChatUtils.colorMessage(false, "The teleporter ", name, " doesn't exsists!"));
							
							return true;
						}
						if(name.equals("positions")) {
							p.sendMessage(ChatUtils.colorMessage(false, "The name ", name, " isn't allowed!"));
							
							return true;
						}
						if(!teleporters.contains("positions")) {
							p.sendMessage(ChatUtils.colorMessage(false, "There aren't any positions!"));
							
							return true;
						}
						ConfigurationSection positions = teleporters.getConfigurationSection("positions");
						if(!positions.contains(positionName)) {
							p.sendMessage(ChatUtils.colorMessage(false, "The position ", positionName, " doesn't exsists!"));
							
							return true;
						}
						
						ConfigurationSection slots = teleporters.getConfigurationSection(name).getConfigurationSection("slots");
						slots.set("" + slot, positionName);
						plugin.saveSaveConfig();
						
						p.sendMessage(ChatUtils.colorMessage(true, "The position ", positionName,
						" was successfully added to the teleporter ", name, "!"));
						
						return true;
					}
				}else if(action.equals("remove")) {
					if(args.length == 3) {
						String name = args[1];
						String slotTxt = args[2];
						int slot;
						try {
							slot = Integer.parseInt(slotTxt);
						}catch(NumberFormatException e) {
							p.sendMessage(ChatUtils.colorMessageStartingWithValue(false, slotTxt, " isn't a number!"));
							
							return true;
						}
						
						ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
						if(!teleporters.contains(name)) {
							p.sendMessage(ChatUtils.colorMessage(false, "The teleporter ", name, " doesn't exsists!"));
							
							return true;
						}
						if(name.equals("positions")) {
							p.sendMessage(ChatUtils.colorMessage(false, "The name ", name, " isn't allowed!"));
							
							return true;
						}
						
						ConfigurationSection slots = teleporters.getConfigurationSection(name).getConfigurationSection("slots");
						slot--;
						if(!slots.getKeys(false).contains(slot + "")) {
							p.sendMessage(ChatUtils.colorMessage(false, "The slot ", slot + 1, " doesn't exist!"));
							
							return true;
						}
						slots.set(slot + "", null);
						plugin.saveSaveConfig();
						
						p.sendMessage(ChatUtils.colorMessage(true, "The position in slot ", slot + 1,
						" was successfully removed from the teleporter ", name, "!"));
						
						return true;
					}
				}else if(action.equals("add_position")) {
					if(args.length == 7) {
						String positionName = args[1];
						String icon = args[2];
						String world = args[3];
						String[] coordsTxt = new String[] {args[4], args[5], args[6]};
						double[] coords = new double[3];
						for(int i = 0;i < coordsTxt.length;i++) {
							try {
								coords[i] = Double.parseDouble(coordsTxt[i]);
							}catch(NumberFormatException e) {
								p.sendMessage(ChatUtils.colorMessageStartingWithValue(false, coordsTxt[i],
								" isn't a number!"));
								
								return true;
							}
						}
						
						Material m = Material.matchMaterial(icon);
						if(m == null || m.toString().startsWith("LEGACY_") || !m.isItem() || m.isAir()) {
							p.sendMessage(ChatUtils.colorMessage(false, "The icon ", icon, " doesn't exsists!"));
							
							return true;
						}
						if(plugin.getServer().getWorld(world) == null) {
							p.sendMessage(ChatUtils.colorMessage(false, "The world ", world, " doesn't exsists!"));
							
							return true;
						}
						
						ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
						ConfigurationSection positions;
						if(teleporters.contains("positions"))
							positions = teleporters.getConfigurationSection("positions");
						else
							positions = teleporters.createSection("positions");
						
						if(positions.contains(positionName)) {
							p.sendMessage(ChatUtils.colorMessage(false, "The position ", positionName, " already exsists!"));
							
							return true;
						}
						
						ConfigurationSection position = positions.createSection(positionName);
						position.set("icon", icon);
						position.set("world", world);
						position.set("pos", new Vector(coords[0], coords[1], coords[2]));
						plugin.saveSaveConfig();
						
						p.sendMessage(ChatUtils.colorMessage(true, "The position ", positionName, " with the icon ",
						icon, " was successfully created!"));
						
						return true;
					}
				}else if(action.equals("edit_position")) {
					if(args.length == 7) {
						String positionName = args[1];
						String icon = args[2];
						String world = args[3];
						String[] coordsTxt = new String[] {args[4], args[5], args[6]};
						double[] coords = new double[3];
						for(int i = 0;i < coordsTxt.length;i++) {
							try {
								coords[i] = Double.parseDouble(coordsTxt[i]);
							}catch(NumberFormatException e) {
								p.sendMessage(ChatUtils.colorMessageStartingWithValue(false, coordsTxt[i],
								" isn't a number!"));
								
								return true;
							}
						}
						
						Material m = Material.matchMaterial(icon);
						if(m == null || m.toString().startsWith("LEGACY_") || !m.isItem() || m.isAir()) {
							p.sendMessage(ChatUtils.colorMessage(false, "The icon ", icon, " doesn't exsists!"));
							
							return true;
						}
						if(plugin.getServer().getWorld(world) == null) {
							p.sendMessage(ChatUtils.colorMessage(false, "The world ", world, " doesn't exsists!"));
							
							return true;
						}
						
						ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
						if(!teleporters.contains("positions")) {
							p.sendMessage(ChatUtils.colorMessage(false, "There aren't any positions!"));
							
							return true;
						}
						ConfigurationSection positions = teleporters.getConfigurationSection("positions");
						if(!positions.contains(positionName)) {
							p.sendMessage(ChatUtils.colorMessage(false, "The position ", positionName, " doesn't exsists!"));
							
							return true;
						}
						
						ConfigurationSection position = positions.getConfigurationSection(positionName);
						position.set("icon", icon);
						position.set("world", world);
						position.set("pos", new Vector(coords[0], coords[1], coords[2]));
						plugin.saveSaveConfig();
						
						p.sendMessage(ChatUtils.colorMessage(true, "The position ", positionName,
						" was successfully updated!"));
						
						return true;
					}
				}else if(action.equals("remove_position")) {
					if(args.length == 2) {
						String positionName = args[1];
						
						ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
						if(!teleporters.contains("positions")) {
							p.sendMessage(ChatUtils.colorMessage(false, "There aren't any positions!"));
							
							return true;
						}
						ConfigurationSection positions = teleporters.getConfigurationSection("positions");
						if(!positions.contains(positionName)) {
							p.sendMessage(ChatUtils.colorMessage(false, "The position ", positionName, " doesn't exsists!"));
							
							return true;
						}
						
						teleporters.getKeys(false).forEach(teleporter -> {
							if(teleporter.equals("positions"))
								return;
							
							ConfigurationSection slots = teleporters.getConfigurationSection(teleporter).
							getConfigurationSection("slots");
							slots.getKeys(false).forEach(slot -> {
								if(slots.getString(slot).equals(positionName)) {
									slots.set(slot, null);
								}
							});
						});
						
						positions.set(positionName, null);
						if(positions.getKeys(false).size() == 0) {
							teleporters.set("positions", null);
						}
						plugin.saveSaveConfig();
						
						p.sendMessage(ChatUtils.colorMessage(true, "The position ", positionName,
						" was successfully deleted!"));
						
						return true;
					}
				}
			}else if(!action.equals("give")) {
				sender.sendMessage(ChatUtils.colorMessage(false, "You haven't the permission to call ", "/teleporter " +
				action, "!"));
				return true;
			}
		}
		return false;
	}
	private List<String> tabCmdTeleporter(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> autoComplete = new ArrayList<>();
		
		if(!(sender instanceof Player)) {
			return autoComplete;
		}
		
		Player p = (Player)sender;
		if(args.length == 1) {
			autoComplete.add("give");
			if(sender.hasPermission("teleporter")) {
				autoComplete.add("create");
				autoComplete.add("delete");
				autoComplete.add("add");
				autoComplete.add("remove");
				autoComplete.add("add_position");
				autoComplete.add("edit_position");
				autoComplete.add("remove_position");
			}
			
			String start = args[0];
			for(int i = autoComplete.size() - 1;i > -1;i--) {
				if(!autoComplete.get(i).startsWith(start)) {
					autoComplete.remove(i);
				}
			}
			
			return autoComplete;
		}else if(args.length == 2) {
			if(args[0].equals("give")) {
				ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
				teleporters.getKeys(false).forEach(teleporter -> {
					if(teleporter.equals("positions"))
						return;
					
					if(teleporter.startsWith(args[1]))
						autoComplete.add(teleporter);
				});
				
				return autoComplete;
			}
			
			if(p.hasPermission("teleporter")) {
				if(args[0].equals("create") || args[0].equals("add_position")) {
					return null;
				}else if(args[0].equals("delete") || args[0].equals("add") || args[0].equals("remove")) {
					ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
					teleporters.getKeys(false).forEach(teleporter -> {
						if(teleporter.equals("positions"))
							return;
						
						if(teleporter.startsWith(args[1]))
							autoComplete.add(teleporter);
					});
					
					return autoComplete;
				}else if(args[0].equals("edit_position") || args[0].equals("remove_position")) {
					ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
					if(!teleporters.contains("positions")) {
						autoComplete.add(ChatUtils.colorMessage(false, "There aren't any positions!"));
						return autoComplete;
					}
					ConfigurationSection positions = teleporters.getConfigurationSection("positions");
					positions.getKeys(false).forEach(position -> {
						if(position.startsWith(args[1]))
							autoComplete.add(position);
					});
					
					return autoComplete;
				}
			}
		}else if(args.length == 3) {
			if(p.hasPermission("teleporter")) {
				if(args[0].equals("create") || args[0].equals("add_position") || args[0].equals("edit_position")) {
					for(Material m:Material.values()) {
						if(m.toString().startsWith("LEGACY_") || !m.isItem() || m.isAir())
							continue;
						String name = m.getKey().getKey();
						if(name.startsWith(args[2]))
							autoComplete.add(name);
					}
					
					return autoComplete;
				}else if(args[0].equals("add")) {
					ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
					if(!teleporters.contains("positions")) {
						autoComplete.add(ChatUtils.colorMessage(false, "There aren't any positions!"));
						return autoComplete;
					}
					ConfigurationSection positions = teleporters.getConfigurationSection("positions");
					positions.getKeys(false).forEach(position -> {
						if(position.startsWith(args[2]))
							autoComplete.add(position);
					});
					
					return autoComplete;
				}else if(args[0].equals("remove")) {
					String name = args[1];
					
					ConfigurationSection teleporters = plugin.getSaveConfig().getConfigurationSection("teleporters");
					if(!teleporters.contains(name)) {
						autoComplete.add(ChatUtils.colorMessage(false, "The teleporter ", name, " doesn't exsists!"));
						
						return autoComplete;
					}
					
					ConfigurationSection slots = teleporters.getConfigurationSection(name).getConfigurationSection("slots");
					slots.getKeys(false).forEach(slot -> {
						if(((Integer.parseInt(slot) + 1) + "").startsWith(args[2]))
							autoComplete.add((Integer.parseInt(slot) + 1) + "");
					});
					
					return autoComplete;
				}
			}
		}else if(args.length == 4) {
			if(p.hasPermission("teleporter")) {
				if(args[0].equals("add")) {
					for(int i = 1;i < 55;i++) {
						if((i + "").startsWith(args[3])) {
							autoComplete.add(i + "");
						}
					}
					
					return autoComplete;
				}else if(args[0].equals("add_position") || args[0].equals("edit_position")) {
					autoComplete.add(p.getWorld().getName());
					
					return autoComplete;
				}
			}
		}else if(args.length == 5) {
			if(p.hasPermission("teleporter")) {
				if(args[0].equals("add_position") || args[0].equals("edit_position")) {
					autoComplete.add(p.getLocation().getBlockX() + "");
					
					return autoComplete;
				}
			}
		}else if(args.length == 6) {
			if(p.hasPermission("teleporter")) {
				if(args[0].equals("add_position") || args[0].equals("edit_position")) {
					autoComplete.add(p.getLocation().getBlockY() + "");
					
					return autoComplete;
				}
			}
		}else if(args.length == 7) {
			if(p.hasPermission("teleporter")) {
				if(args[0].equals("add_position") || args[0].equals("edit_position")) {
					autoComplete.add(p.getLocation().getBlockZ() + "");
					
					return autoComplete;
				}
			}
		}
		
		return autoComplete;
	}
	
	private boolean execCmdChunkLoader(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatUtils.colorMessage(false, "Only players can use this command!"));
			
			return true;
		}
		
		Player p = (Player)sender;
		if(!p.hasPermission("chunk_loader")) {
			sender.sendMessage(ChatUtils.colorMessage(false, "You haven't the permission to call ", "/chunk_loader", "!"));
			return true;
		}
		if(args.length == 0) {
			p.getInventory().addItem(new ItemChunkLoader());
			
			return true;
		}
		return false;
	}
	private List<String> tabCmdChunkLoader(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}
	
	private boolean execCmdInventory(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("inventory")) {
			sender.sendMessage(ChatColor.RED + "You haven't the permission for this command!");
			return true;
		}
		
		ConfigurationSection inventoryNames = plugin.getSaveConfig().getConfigurationSection("inventories.names");
		ConfigurationSection inventoryWorlds = plugin.getSaveConfig().getConfigurationSection("inventories.worlds");
		if(args.length == 1) {
			if(args[0].equals("list")) {
				sender.sendMessage("Inventories:");
				sender.sendMessage("-> default");
				
				inventoryNames.getKeys(false).forEach(name -> {
					sender.sendMessage("-> " + name);
				});
				
				return true;
			}
		}else if(args.length == 2) {
			String name = args[1];
			
			if(args[0].equals("add")) {
				if(!name.matches("[0-9a-zA-Z_]*") || name.equals("default")) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The name ", name, " is not allowed!"));
						
					return true;
				}
				
				inventoryNames.createSection(name);
				plugin.saveSaveConfig();
				
				sender.sendMessage(ChatUtils.colorMessage(true, "The inventory ", name, " was successfully added!"));
				
				return true;
			}else if(args[0].equals("remove")) {
				if(!inventoryNames.contains(name)) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The inventory ", name, " doesn't exist!"));
					
					return true;
				}
				
				inventoryNames.set(name, null);
				
				//Remove inventory from all worlds
				inventoryWorlds.getKeys(false).forEach(world -> {
					String inventoryOfWorld = inventoryWorlds.getString(world + ".inventory");
					if(inventoryOfWorld.equals(name)) {
						inventoryWorlds.set(world, null);
						sender.sendMessage(ChatUtils.colorMessage(true, "The inventory of the world ", world,
						" was set to ", "default", "!"));
					}
				});
				
				//Remove inventory form all players
				ConfigurationSection players = plugin.getSaveConfig().getConfigurationSection("player");
				players.getKeys(false).forEach(player -> {
					if(players.contains(player + ".inventory." + name)) {
						players.set(player + ".inventory." + name, null);
						String playerName = plugin.getServer().getOfflinePlayer(UUID.fromString(player)).getName();
						sender.sendMessage(ChatUtils.colorMessage(true, "The inventory ", name, " from the player ",
						playerName, " was succesfully removed!"));
					}
				});
				plugin.saveSaveConfig();
				
				sender.sendMessage(ChatUtils.colorMessage(true, "The inventory ", name, " was successfully removed!"));
				
				return true;
			}else if(args[0].equals("list_world")) {
				if(!inventoryNames.contains(name)) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The inventory ", name, " doesn't exist!"));
					
					return true;
				}
				
				sender.sendMessage("Worlds:");
				boolean[] flag = new boolean[] {true};
				inventoryWorlds.getKeys(false).forEach(world -> {
					String inventoryOfWorld = inventoryWorlds.getString(world + ".inventory");
					if(inventoryOfWorld.equals(name)) {
						flag[0] = false;
						sender.sendMessage("-> " + world);
					}
				});
				
				if(flag[0]) {
					sender.sendMessage("No worlds!");
				}
				
				return true;
			}
		}else if(args.length == 3) {
			String name = args[1];
			String worldOrPlayerName = args[2];
			
			if(args[0].equals("show")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatUtils.colorMessage(false, "Only players can use ", "show", "!"));
					
					return true;
				}
				
				Player p = (Player)sender;
				
				if(!inventoryNames.contains(name) && !name.equals("default")) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The inventory ", name, " doesn't exist!"));
					
					return true;
				}
				
				OfflinePlayer player = null;
				for(OfflinePlayer playerCheck:plugin.getServer().getOfflinePlayers()) {
					if(playerCheck.getName().equals(worldOrPlayerName)) {
						player = playerCheck;
						
						break;
					}
				}
				if(player == null) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The player ", worldOrPlayerName, " wasn't found!"));
					
					return true;
				}
				
				Inventory playerInv = plugin.getServer().createInventory(null, 54, ChatColor.GOLD + "Inventory " +
				ChatColor.RESET + "[" + worldOrPlayerName + "@" + name + "]");
				if(plugin.getSaveConfig().contains("player." + player.getUniqueId() + ".inventory." + name + ".content")) {
					ConfigurationSection itemsSection = plugin.getSaveConfig().getConfigurationSection(
					"player." + player.getUniqueId() + ".inventory." + name + ".content");
					Set<String> savedItems = itemsSection.getKeys(false);
					ItemStack[] items = new ItemStack[54];
					savedItems.forEach(key -> {
						int index = Integer.parseInt(key);
						int indexForLayout = index;
						ItemStack item = itemsSection.getItemStack(key);
						
						//Change index for proper layout
						{
							//Off-hand
							if(index == 40)
								indexForLayout = 8;
							
							//Helmet
							if(index == 39)
								indexForLayout = 0;
							//Chestplate
							if(index == 38)
								indexForLayout = 9;
							//Leggings
							if(index == 37)
								indexForLayout = 1;
							//Boots
							if(index == 36)
								indexForLayout = 10;
							
							//Main inventory
							if(index > 8 && index < 36)
								indexForLayout += 9;
							
							//Bottom inventory bar
							if(index < 9)
								indexForLayout += 45;
						}
						
						items[indexForLayout] = item;
					});
					playerInv.setContents(items);
				}
				
				p.openInventory(playerInv);
				
				return true;
			}else if(args[0].equals("add_world")) {
				if(!inventoryNames.contains(name)) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The inventory ", name, " doesn't exist!"));
					
					return true;
				}
				
				if(plugin.getServer().getWorld(worldOrPlayerName) == null) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The world ", worldOrPlayerName, " wasn't found!"));
					
					return true;
				}
				
				inventoryWorlds.set(worldOrPlayerName + ".inventory", name);
				plugin.saveSaveConfig();
				
				sender.sendMessage(ChatUtils.colorMessage(true, "The world ", worldOrPlayerName,
				" was successfully added to the inventory ", name, "!"));
				
				return true;
			}else if(args[0].equals("remove_world")) {
				if(!inventoryNames.contains(name)) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The inventory ", name, " doesn't exist!"));
					
					return true;
				}
				
				if(plugin.getServer().getWorld(worldOrPlayerName) == null) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The world ", worldOrPlayerName, " wasn't found!"));
					
					return true;
				}
				
				inventoryWorlds.set(worldOrPlayerName, null);
				plugin.saveSaveConfig();
				
				sender.sendMessage(ChatUtils.colorMessage(true, "The world ", worldOrPlayerName,
				" was successfully removed from the inventory ", name, "!"));
				
				return true;
			}else if(args[0].equals("set_default_spawn_point")) {
				if(!inventoryNames.contains(name)) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The inventory ", name, " doesn't exist!"));
					
					return true;
				}
				
				if(!worldOrPlayerName.equals("no_spawn_point")) {
					return false;
				}
				
				ConfigurationSection inventory = inventoryNames.getConfigurationSection(name);
				if(inventory.contains("spawn_point")) {
					inventory.set("spawn_point", null);
					plugin.saveSaveConfig();
				}
				
				sender.sendMessage(ChatUtils.colorMessage(true, "The spawn point of the inventory ", name,
				"was successfully removed from inventory!"));
				
				return true;
			}
		}else if(args.length == 6) {
			String name = args[1];
			String worldName = args[2];
			int x, y, z;
			try {
				x = Integer.parseInt(args[3]);
				y = Integer.parseInt(args[4]);
				z = Integer.parseInt(args[5]);
			}catch(NumberFormatException e) {
				sender.sendMessage(ChatUtils.colorMessage(false, "All coordinates have to be numbers!"));
				
				return true;
			}
			
			if(args[0].equals("set_default_spawn_point")) {
				if(!inventoryNames.contains(name)) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The inventory ", name, " doesn't exist!"));
					
					return true;
				}
				
				if(plugin.getServer().getWorld(worldName) == null) {
					sender.sendMessage(ChatUtils.colorMessage(false, "The world ", worldName, " wasn't found!"));
					
					return true;
				}
				
				ConfigurationSection inventory = inventoryNames.getConfigurationSection(name);
				inventory.set("spawn_point", new Location(plugin.getServer().getWorld(worldName), x, y, z));
				plugin.saveSaveConfig();
				
				sender.sendMessage(ChatUtils.colorMessage(true, "The spawn point of the inventory ", name,
				"was successfully set to inventory!"));
				
				return true;
			}
		}
		
		return false;
	}
	private List<String> tabCmdInventory(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> autoComplete = new ArrayList<>();
		
		if(!sender.hasPermission("inventory")) {
			return autoComplete;
		}
		
		if(args.length == 1) {
			autoComplete.add("list");
			autoComplete.add("show");
			autoComplete.add("add");
			autoComplete.add("remove");
			autoComplete.add("list_world");
			autoComplete.add("add_world");
			autoComplete.add("remove_world");
			if(sender instanceof Player)
				autoComplete.add("set_default_spawn_point");
			
			String start = args[0];
			for(int i = autoComplete.size() - 1;i > -1;i--) {
				if(!autoComplete.get(i).startsWith(start)) {
					autoComplete.remove(i);
				}
			}
			
			return autoComplete;
		}else if(args.length == 2) {
			if(args[0].equals("list")) {
				return autoComplete;
			}
			
			if(args[0].equals("add")) {
				return null;
			}
			
			String start = args[1];
			ConfigurationSection inventoryNames = plugin.getSaveConfig().getConfigurationSection("inventories.names");
			inventoryNames.getKeys(false).forEach(name -> {
				if(name.startsWith(start)) {
					autoComplete.add(name);
				}
			});
			
			if(args[0].equals("show") && "default".startsWith(start)) {
				autoComplete.add("default");
			}
			
			return autoComplete;
		}else if(args.length == 3) {
			if(args[0].equals("show")) {
				String start = args[2];
				
				for(OfflinePlayer player:plugin.getServer().getOfflinePlayers()){
					if(!player.getName().startsWith(start)) {
						continue;
					}
					
					autoComplete.add(player.getName());
				}
				for(Player player:plugin.getServer().getOnlinePlayers()){
					if(!player.getName().startsWith(start)) {
						continue;
					}
					
					autoComplete.add(player.getName());
				}
			}else if(args[0].equals("add_world") || args[0].equals("remove_world")) {
				String start = args[2];
				
				for(World world:plugin.getServer().getWorlds()) {
					if(!world.getName().startsWith(start)) {
						continue;
					}
					
					autoComplete.add(world.getName());
				}
			}else if(args[0].equals("set_default_spawn_point")) {
				if(sender instanceof Player) {
					Player p = (Player)sender;
					autoComplete.add("no_spawn_point");
					autoComplete.add(p.getWorld().getName());
					
					for(int i = autoComplete.size() - 1;i > -1;i--)
						if(!autoComplete.get(i).startsWith(args[2]))
							autoComplete.remove(i);
				}
			}
			
			return autoComplete;
		}else if(args.length == 4) {
			if(sender instanceof Player && args[0].equals("set_default_spawn_point") && !args[2].equals("no_spawn_point")) {
				Player p = (Player)sender;
				autoComplete.add(p.getLocation().getBlockX() + "");
			}
		}else if(args.length == 5 && args[0].equals("set_default_spawn_point") && !args[2].equals("no_spawn_point")) {
			if(sender instanceof Player) {
				Player p = (Player)sender;
				autoComplete.add(p.getLocation().getBlockY() + "");
			}
		}else if(args.length == 6 && args[0].equals("set_default_spawn_point") && !args[2].equals("no_spawn_point")) {
			if(sender instanceof Player) {
				Player p = (Player)sender;
				autoComplete.add(p.getLocation().getBlockZ() + "");
			}
		}
		
		return autoComplete;
	}
	
	private boolean execCmdReloadConfig(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("reload_config")) {
			sender.sendMessage(ChatUtils.colorMessage(false, "You haven't the permission to call ", " /reload_config", "!"));
			return true;
		}
		if(args.length == 0) {
			plugin.reloadConfig();
			plugin.reloadSaveConfig();
			plugin.loadPermissions();
			
			sender.sendMessage(ChatUtils.colorMessage(true, "The config was successfully reloaded!"));
			
			return true;
		}
		return false;
	}
	private List<String> tabCmdReloadConfig(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(label.equals(cmdWorld)) {
			return execCmdWorld(sender, cmd, label, args);
		}else if(label.equals(cmdPermission)) {
			return execCmdPermission(sender, cmd, label, args);
		}else if(label.equals(cmdElevator)) {
			return execCmdElevator(sender, cmd, label, args);
		}else if(label.equals(cmdTeleporter)) {
			return execCmdTeleporter(sender, cmd, label, args);
		}else if(label.equals(cmdChunkLoader)) {
			return execCmdChunkLoader(sender, cmd, label, args);
		}else if(label.equals(cmdInventory)) {
			return execCmdInventory(sender, cmd, label, args);
		}else if(label.equals(cmdReloadConfig)) {
			return execCmdReloadConfig(sender, cmd, label, args);
		}
		
		return false;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(label.equals(cmdWorld)) {
			return tabCmdWorld(sender, cmd, label, args);
		}else if(label.equals(cmdPermission)) {
			return tabCmdPermission(sender, cmd, label, args);
		}else if(label.equals(cmdElevator)) {
			return tabCmdElevator(sender, cmd, label, args);
		}else if(label.equals(cmdTeleporter)) {
			return tabCmdTeleporter(sender, cmd, label, args);
		}else if(label.equals(cmdChunkLoader)) {
			return tabCmdChunkLoader(sender, cmd, label, args);
		}else if(label.equals(cmdInventory)) {
			return tabCmdInventory(sender, cmd, label, args);
		}else if(label.equals(cmdReloadConfig)) {
			return tabCmdReloadConfig(sender, cmd, label, args);
		}
		
		return null;
	}
}