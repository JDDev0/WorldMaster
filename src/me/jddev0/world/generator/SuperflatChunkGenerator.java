package me.jddev0.world.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class SuperflatChunkGenerator extends ChunkGenerator {
	private String preset;
	
	public SuperflatChunkGenerator(String preset) {
		this.preset = preset;
	}
	
	@Override
	public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
		ChunkData chunk = createChunkData(world);
		
		if(preset.equals("AIR") && (x < -1 || x > 1 || z < -1 || z > 1))
			return chunk;
		
		for(int xB = 0;xB < 16;xB++) {
			for(int zB = 0;zB < 16;zB++) {
				if(preset.equals("GRASS")) {
					chunk.setBlock(xB, 0, zB, Material.BEDROCK);
					chunk.setBlock(xB, 1, zB, Material.DIRT);
					chunk.setBlock(xB, 2, zB, Material.DIRT);
					chunk.setBlock(xB, 3, zB, Material.GRASS_BLOCK);
				}else if(preset.equals("REDSTONE")) {
					chunk.setBlock(xB, 0, zB, Material.BEDROCK);
					for(int yB = 1;yB < 4;yB++)
						chunk.setBlock(xB, yB, zB, Material.STONE);
					for(int yB = 4;yB < 56;yB++)
						chunk.setBlock(xB, yB, zB, Material.SANDSTONE);
				}else if(preset.equals("AIR")) {
					chunk.setBlock(xB, 0, zB, Material.STONE);
				}
			}
		}
		
		return chunk;
	}
	
	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		double x = 0, y = 0, z = 0;
		
		if(preset.equals("GRASS")) {
			y = 4;
			x = random.nextInt(1001) - 500;
			z = random.nextInt(1001) - 500;
		}else if(preset.equals("REDSTONE")) {
			y = 56;
			x = random.nextInt(1001) - 500;
			z = random.nextInt(1001) - 500;
		}else if(preset.equals("GRASS")) {
			y = 1;
			x = random.nextInt(17) - 8;
			z = random.nextInt(17) - 8;
		}
		
		return new Location(world, x, y, z);

	}
	
	@Override
	public boolean shouldGenerateCaves() {
		return false;
	}
	
	@Override
	public boolean shouldGenerateDecorations() {
		return false;
	}
	
	@Override
	public boolean shouldGenerateMobs() {
		return !preset.equals("AIR");
	}
	
	@Override
	public boolean shouldGenerateStructures() {
		return false;
	}
	
	@Override
	public boolean isParallelCapable() {
		return true;
	}
	
	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return new ArrayList<>();
	}
}