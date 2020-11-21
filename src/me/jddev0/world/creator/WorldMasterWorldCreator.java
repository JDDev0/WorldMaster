package me.jddev0.world.creator;

import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.jddev0.world.generator.SuperflatChunkGenerator;

public class WorldMasterWorldCreator extends WorldCreator {
	public static Environment convertDimension(String dimension) {
		if(dimension == null)
			return Environment.NORMAL;
		
		if(dimension.equals("overworld"))
			return Environment.NORMAL;
		
		if(dimension.equals("nether"))
			return Environment.NETHER;
		
		if(dimension.equals("the_end"))
			return Environment.THE_END;
		
		return Environment.NORMAL;
	}
	
	public static WorldType convertType(String type) {
		if(type == null)
			return WorldType.NORMAL;
		
		if(type.equals("default"))
			return WorldType.NORMAL;
		
		if(type.equals("superflat"))
			return WorldType.FLAT;
		
		return WorldType.NORMAL;
	}
	
	public WorldMasterWorldCreator(String name, FileConfiguration saveConfig) {
		super(name);
		
		if(!saveConfig.contains("world")) {
			setCreatorParameter(convertDimension(null), convertType(null), null);
			
			return;
		}
		
		ConfigurationSection worlds = saveConfig.getConfigurationSection("world");
		if(!worlds.contains(name)) {
			setCreatorParameter(convertDimension(null), convertType(null), null);
			
			return;
		}
		
		ConfigurationSection world = worlds.getConfigurationSection(name);
		
		String dimension = null;
		String type = null;
		String blocks = null;
		if(world.contains("gen.dimension"))
			dimension = world.getString("gen.dimension");
		if(world.contains("gen.type"))
			type = world.getString("gen.type");
		if(world.contains("gen.blocks"))
			blocks = world.getString("gen.blocks");
		
		setCreatorParameter(convertDimension(dimension), convertType(type), blocks);
	}
	
	public WorldMasterWorldCreator(String name, Environment dimension, WorldType type, String blocks) {
		super(name);
		
		setCreatorParameter(dimension, type, blocks);
	}
	
	private void setCreatorParameter(Environment dimension, WorldType type, String blocks) {
		environment(dimension);
		type(type);
		
		if(dimension.equals(Environment.NORMAL) && type.equals(WorldType.FLAT))
			generator(new SuperflatChunkGenerator(blocks));
	}
}