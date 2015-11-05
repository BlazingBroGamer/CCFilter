package me.BlazingBroGamer.CCFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CCFilter extends JavaPlugin implements Listener{
	
	FileConfiguration fc;
	HashMap<String, List<String>> trigger;
	
	@Override
	public void onEnable() {
		fc = getConfig();
		fc.addDefault("Prefix", "&0[&aCCFilter&0]&f");
		fc.addDefault("Filters.Example1.Triggers", new String[]{"Can I have op", "Can I haz op"});
		fc.addDefault("Filters.Example1.Message", "&4We do not need any staff right now");
		fc.addDefault("Filters.Example1.Commands", new String[]{"kick %player%"});
		fc.addDefault("Filters.Example2.Triggers", new String[]{"/reload", "/rl"});
		fc.addDefault("Filters.Example2.Message", "&aPlease do not reload the server!");
		fc.addDefault("Filters.Example2.Commands", new String[]{"say This is a message!"});
		fc.options().copyDefaults(true);
		saveConfig();
		getServer().getPluginManager().registerEvents(this, this);
		loadTriggers();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(label.equalsIgnoreCase("ccfilter")){
			if(sender.hasPermission("ccfilter.admin")){
				if(args.length == 1){
					if(args[0].equalsIgnoreCase("reload")){
						reloadConfig();
						fc = getConfig();
						loadTriggers();
						sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the config!");
						return true;
					}
				}
				sender.sendMessage(ChatColor.RED + "Usage: /ccfilter reload");
			}else{
				sender.sendMessage(ChatColor.RED + "You do not have permissions to use this command!");
			}
		}
		return false;
	}
	
	public void loadTriggers(){
		trigger = new HashMap<String, List<String>>();
		for(String s : fc.getConfigurationSection("Filters").getKeys(false)){
			trigger.put(s, fc.getStringList("Filters." + s + ".Triggers"));
		}
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e){
		e.setCancelled(isTriggered(e.getMessage(), e.getPlayer()));
	}
	
	public boolean isTriggered(String cmd, Player p){
		String trigger = getTrigger(cmd);
		if(trigger != null){
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', fc.getString("Filters." + trigger + ".Message")));
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){

				@Override
				public void run() {
					for(String cmds : fc.getStringList("Filters." + trigger + ".Commands")){
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmds.replaceAll("%player%", p.getName()));
					}					
				}
				
			});
			
			return true;
		}
		return false;
	}
	
	public String getTrigger(String msg){
		for(String s : trigger.keySet()){
			for(String trigger : trigger.get(s)){
				if(trigger.equalsIgnoreCase(msg) || msg.toUpperCase().startsWith(trigger.toUpperCase())){
					return s;
				}
			}
		}
		return null;
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		if(isTriggered(e.getMessage(), e.getPlayer())){
			e.setCancelled(true);
			List<Player> remove = new ArrayList<Player>();
			for(Player p : e.getRecipients()){
				remove.add(p);
			}
			for(Player p : remove){
				e.getRecipients().remove(p);
			}
		}
	}
	
}
