package me.jddev0;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	private File saveConfigFile;
	private FileConfiguration saveConfig;
	
	public ConfigManager(Plugin plugin) {
		if(!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdir();
		}
		
		saveConfigFile = new File(plugin.getDataFolder(), "save.yml");
		if(!saveConfigFile.exists()) {
			try {
				saveConfigFile.createNewFile();
				String defaults = "worlds: 'spawn'\n" + 
				"shops: {}\n" + 
				"elevators: {}\n" + 
				"teleporters: {}\n" + 
				"loaded_chunks: {}\n" + 
				"inventories:\n" + 
				"  names: {}\n" + 
				"  worlds: {}";
				
				FileWriter fw = new FileWriter(saveConfigFile);
				fw.write(defaults);
				fw.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		saveConfig = YamlConfiguration.loadConfiguration(saveConfigFile);
	}
	
	public FileConfiguration getSaveConfig() {
		return saveConfig;
	}
	
	public void reloadSaveConfig() {
		saveConfig = YamlConfiguration.loadConfiguration(saveConfigFile);
	}
	
	public void saveSaveConfig() {
		try {
			saveConfig.save(saveConfigFile);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}