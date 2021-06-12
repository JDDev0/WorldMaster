package me.jddev0.utils;

import org.bukkit.ChatColor;

public final class ChatUtils {
	private ChatUtils() {}
	
	public static String colorMessage(ChatColor[] colors, Object... messageParts) {
		StringBuilder message = new StringBuilder();
		for(int i = 0;i < messageParts.length;i++) {
			if(colors != null && colors.length > 0)
				message.append(colors[i % colors.length]);
			
			message.append(messageParts[i]);
		}
		message.append(ChatColor.RESET);
		
		return message.toString();
	}
	public static String colorMessage(boolean good, Object... messageParts) {
		return colorMessage(new ChatColor[] {good?ChatColor.GREEN:ChatColor.RED, ChatColor.GOLD}, messageParts);
	}
	public static String colorMessageStartingWithValue(boolean good, Object... messageParts) {
		return colorMessage(new ChatColor[] {ChatColor.GOLD, good?ChatColor.GREEN:ChatColor.RED}, messageParts);
	}
}