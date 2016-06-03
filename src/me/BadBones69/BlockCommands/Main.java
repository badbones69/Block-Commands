package me.BadBones69.BlockCommands;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener{
	SettingsManager settings = SettingsManager.getInstance();
	@Override
	public void onEnable(){
		settings.setup(this);
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLable, String[] args){
		if(commandLable.equalsIgnoreCase("BlockCommands")||commandLable.equalsIgnoreCase("BlockCommand")
				||commandLable.equalsIgnoreCase("BC")||commandLable.equalsIgnoreCase("BlockC")||
				commandLable.equalsIgnoreCase("BCommands")||commandLable.equalsIgnoreCase("BlockCommand")){
			FileConfiguration data = settings.getData();
			if(args.length==0){
				if(!hasPermission(sender, "Admin"))return true;
				sender.sendMessage(color("&c/BC Add <Command>"));
				return true;
			}
			if(args.length>=1){
				if(args[0].equalsIgnoreCase("Help")){
					if(!hasPermission(sender, "Admin"))return true;
					sender.sendMessage(color("&6-- Block Commands --"));
					sender.sendMessage(color("&a/BC Help &7- Shows you the Block Commands Menu."));
					sender.sendMessage(color("&a/BC List &7- Shows you the Block Commands Menu."));
					sender.sendMessage(color("&a/BC Add <Command> &7- Shows you the Block Commands Menu."));
					return true;
				}
				if(args[0].equalsIgnoreCase("List")){
					if(!hasPermission(sender, "Admin"))return true;
					String msg = "";
					String part = "";
					String l = "";
					int line = 1;
					for(String L : data.getConfigurationSection("Locations").getKeys(false)){
						String W = data.getString("Locations." + L + ".Location.World");
						String X = data.getString("Locations." + L + ".Location.X");
						String Y = data.getString("Locations." + L + ".Location.Y");
						String Z = data.getString("Locations." + L + ".Location.Z");
						part = color("&8[&6" + line + "&8]: &c" + W +
								"&8, &c" + X + "&8, &c" + Y + "&8, &c" + Z);
						l += part;
						l += "\n";
						line ++;
						part = "";
						
					}
					msg = l;
					line = line - 1;
					sender.sendMessage(color("&6A list of all the Locations."));
					sender.sendMessage(color("&c[World]&8, &c[X]&8, &c[Y]&8, &c[Z]"));
					if(!msg.equals(""))sender.sendMessage(msg);
					sender.sendMessage(color("&3Number of Locations: &6" + line));
					return true;
				}
				if(args[0].equalsIgnoreCase("Add")){
					if(!hasPermission(sender, "Admin"))return true;
					if(!(sender instanceof Player)){
						sender.sendMessage(color("&cYou must be a player to use this command."));
						return true;
					}
					Player player = (Player) sender;
					if(args.length>=2){
						Block block = player.getTargetBlock((HashSet<Byte>)null, 7);
						String command = "";
						int place = 1;
						for(String i : args){
							if(place>1){
								command+=i+" ";
							}
							place++;
						}
						command=command.substring(0, command.length()-1);
						if(block.isEmpty()){
							player.sendMessage(color("&cYou must be looking at a block."));
							return true;
						}
						Location loc = block.getLocation();
						for(String i : data.getConfigurationSection("Locations").getKeys(false)){
							World world = Bukkit.getWorld(data.getString("Locations."+i+".Location.World"));
							int x = data.getInt("Locations."+i+".Location.X");
							int y = data.getInt("Locations."+i+".Location.Y");
							int z = data.getInt("Locations."+i+".Location.Z");
							Location Loc = new Location(world, x, y, z);
							if(loc.equals(Loc)){
								data.set("Locations."+i+".Command", command);
								settings.saveData();
								player.sendMessage(color("&7You have just set a new Block Command."));
								return true;
							}
						}
						int num = 1;
						for(;data.contains("Locations."+num);){
							num++;
						}
						data.set("Locations."+num+".Command", command);
						data.set("Locations."+num+".Location.World", loc.getWorld().getName());
						data.set("Locations."+num+".Location.X", loc.getBlockX());
						data.set("Locations."+num+".Location.Y", loc.getBlockY());
						data.set("Locations."+num+".Location.Z", loc.getBlockZ());
						settings.saveData();
						player.sendMessage(color("&7You have just added a new Block Command."));
						return true;
					}
					player.sendMessage(color("&c/BC Add <Command>"));
					return true;
				}
			}
			if(!hasPermission(sender, "Admin"))return true;
			sender.sendMessage(color("&c/BC Add <Command>"));
			return true;
		}
		return false;
	}
	@EventHandler
	public void onBlockClick(PlayerInteractEvent e){
		Player player = e.getPlayer();
		FileConfiguration data = settings.getData();
		if(e.getAction()==Action.RIGHT_CLICK_BLOCK){
			Block block = e.getClickedBlock();
			for(String i : data.getConfigurationSection("Locations").getKeys(false)){
				World world = Bukkit.getWorld(data.getString("Locations."+i+".Location.World"));
				int x = data.getInt("Locations."+i+".Location.X");
				int y = data.getInt("Locations."+i+".Location.Y");
				int z = data.getInt("Locations."+i+".Location.Z");
				Location Loc = new Location(world, x, y, z);
				if(block.getLocation().equals(Loc)){
					e.setCancelled(true);
					String command = data.getString("Locations."+i+".Command")
							.replaceAll("%Player%", player.getName()).replaceAll("%player%", player.getName());
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
					return;
				}
			}
		}
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		Player player = e.getPlayer();
		FileConfiguration data = settings.getData();
		Block block = e.getBlock();
		for(String i : data.getConfigurationSection("Locations").getKeys(false)){
			World world = Bukkit.getWorld(data.getString("Locations."+i+".Location.World"));
			int x = data.getInt("Locations."+i+".Location.X");
			int y = data.getInt("Locations."+i+".Location.Y");
			int z = data.getInt("Locations."+i+".Location.Z");
			Location Loc = new Location(world, x, y, z);
			if(block.getLocation().equals(Loc)){
				if(player.isSneaking()&&player.getGameMode()==GameMode.CREATIVE&&player.hasPermission("BlockCommands.Admin")){
					data.set("Locations."+i, null);
					settings.saveData();
					player.sendMessage(color("&cYou have just removed a Block Command."));
					return;
				}else{
					e.setCancelled(true);
				}
			}
		}
	}
	String color(String msg){
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}
	boolean hasPermission(CommandSender sender, String perm){
		if(sender instanceof Player){
			Player player = (Player) sender;
			if(!player.hasPermission("BlockCommands." + perm)){
				player.sendMessage(color("&cYou need permission to use this command."));
				return false;
			}else{
				return true;
			}
		}else{
			return true;
		}
	}
}