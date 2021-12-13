package me.vaape.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;

import net.md_5.bungee.api.ChatColor;

public class Rewards extends JavaPlugin implements Listener{
	
	public static Rewards plugin;
	private FileConfiguration config = this.getConfig();
	
	public void onEnable() {
		loadConfiguration();
		plugin = this;
		getLogger().info(ChatColor.GREEN + "Rewards has been enabled!");
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable(){
		plugin = null;
	}
	
	public static Rewards getInstance() {
		return plugin;
	}
	
	public void loadConfiguration() {
		final FileConfiguration config = this.getConfig();
		
		saveDefaultConfig();
		//config.options().copyDefaults(true);
		//saveConfig();
	}
	
	//Give reward
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				
			//givereward
			if (label.equalsIgnoreCase("givereward")) {
				
				if (!sender.hasPermission("rewards.givereward")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
					return false;
				}
				
				if (args.length > 0) { //Give to self
					String reward = args[0];
					if (config.getString("rewards." + reward + ".name") == null) {
						sender.sendMessage(ChatColor.RED + "Reward does not exist.");
						return true;
					}
					if (args.length > 1) {
						if (Bukkit.getOfflinePlayer(args[1]) != null) {
							OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
							giveReward(reward, target, true);
							sender.sendMessage(ChatColor.GREEN + "Reward given to " + target.getName());
						}
						else {
							sender.sendMessage(ChatColor.RED + "No such player.");
						}
					}
					else {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							giveReward(reward, Bukkit.getOfflinePlayer(player.getUniqueId()), true);
						}
						else {
							sender.sendMessage(ChatColor.RED + "You must specify a player or be a player.");
						}
					}
				}
				
				//Random reward
				else {		
					if (sender instanceof Player) {
						Player player = (Player) sender;
						String reward = pickReward();
						giveReward(reward, Bukkit.getOfflinePlayer(player.getUniqueId()), true);
					}
					else {
						sender.sendMessage(ChatColor.RED + "You must specify a player or be a player to give a random reward.");
					}
				}
			}
			
			else if (label.equalsIgnoreCase("giveminingreward")) {
				
				if (!sender.hasPermission("rewards.giveminingreward")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
					return false;
				}
				
				if (args.length > 0) { //Give to self
					String reward = args[0];
					if (config.getString("rewards." + reward + ".name") == null) {
						sender.sendMessage(ChatColor.RED + "Reward does not exist.");
						return true;
					}
					if (args.length > 1) {
						if (Bukkit.getOfflinePlayer(args[1]) != null) {
							OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
							giveMiningReward(reward, target, true);
							sender.sendMessage(ChatColor.GREEN + "Reward given to " + target.getName());
						}
						else {
							sender.sendMessage(ChatColor.RED + "No such player.");
						}
					}
					else {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							giveMiningReward(reward, Bukkit.getOfflinePlayer(player.getUniqueId()), true);
						}
						else {
							sender.sendMessage(ChatColor.RED + "You must specify a player or be a player.");
						}
					}
				}
				
				//Random reward
				else {		
					sender.sendMessage(ChatColor.RED + "Please specify player and reward.");
				}
			}
			
			//Add reward
			else if (label.equalsIgnoreCase("addreward")) {
				
				if (sender instanceof Player) {
					Player player = (Player) sender;
					
					if (!player.hasPermission("rewards.addreward")) {
						player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return false;
					}
					
					if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
						player.sendMessage(ChatColor.RED + "Must be holding an item to use /addreward.");
					}
					else {
						ItemStack item = player.getInventory().getItemInMainHand();
						
						// /addreward reward probability god name
						if (args.length < 4) {
							player.sendMessage(ChatColor.RED + "Incorrect usage, try /addreward [reward] [likelihood] [isGod] [name]");
							return false;
						}
						else {
							//Creating variables
							String reward = args[0];
							
							//likelihood
							try {
								Double.parseDouble(args[1]);
							}
							catch (NumberFormatException e) {
								player.sendMessage(ChatColor.RED + "Invalid likelihood (number), try /addreward [reward] [likelihood] [isGod] [name]");
								return false;
							}
							double probability = Double.parseDouble(args[1]);
							
							//isGod
							if (args[2].toLowerCase().equals("true") || args[2].toLowerCase().equals("false")) {
								Boolean isGod = Boolean.parseBoolean(args[2].toLowerCase());
							
								//name
								StringBuilder nameBuilder = new StringBuilder();
								for (int i = 3; i < args.length; i++) {
									nameBuilder.append(args[i] + " ");
								}
								String name = nameBuilder.toString();
								name = StringUtils.chop(name); //Remove extra space from the end
								
								if (config.get("probabilities." + reward) == null) {
									//Create new reward
									config.set("probabilities." + reward, probability);
									config.set("rewards." + reward + ".name", name);
									if (isGod) {
										config.set("rewards." + reward + ".god", true);
									}
									List<ItemStack> items = new ArrayList<>();
									items.add(item);
									config.set("rewards." + reward + ".items", items);
									config.getList("rewards." + reward + ".items");
								}
								else {
									List<ItemStack> items = (List<ItemStack>) config.getList("rewards." + reward + ".items");
									items.add(item);								
								}
								player.sendMessage(ChatColor.GREEN + "Added " + item.getType().toString() + ChatColor.GREEN + " to reward " + reward);
								saveConfig();
							}
							else {
								player.sendMessage(ChatColor.RED + "Invalid isGod value (true/false), try /addreward [reward] [likelihood] [isGod] [name]");
								return false;
							}
						}
					}
				}
				
				
			}
			
			//Collect
			if (label.equalsIgnoreCase("collect")) {
				
				if (sender instanceof Player) {
					Player player = (Player) sender;
					
					if (config.getStringList("offline." + player.getName()) == null || config.getStringList("offline." + player.getName()).size() == 0) {
						player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rewards] " + ChatColor.BLUE + "You don't have any rewards to collect.");
					}
					else {
						List<String> offlineRewards = config.getStringList("offline." + player.getName());
						
						//Loop through offlineRewards and remove reward from offlineRewards when reward is given
			        	Iterator<String> iterator = offlineRewards.iterator();
			        	
			        	while (iterator.hasNext()) {
			        		String reward = iterator.next();
			        		
			        		if (giveReward(reward, Bukkit.getOfflinePlayer(player.getName()), true)) { //Returns false when reward is not given
			        			iterator.remove();
			        		}
			        	}
			        	
			        	config.set("offline." + player.getName(), offlineRewards);
						saveConfig();
					}
				}
				
			}

		return true;
	}
	
	private void addToOfflineRewards(String reward, OfflinePlayer player) {
		List<String> offlineRewards;
		if (config.getStringList("offline." + player.getName()) == null) {
			offlineRewards = new ArrayList<>();
		}
		else {
			offlineRewards = config.getStringList("offline." + player.getName());
		}
		offlineRewards.add(reward);
		config.set("offline." + player.getName(), offlineRewards);
		saveConfig();
	}
	
	public boolean giveReward(String reward, OfflinePlayer player, Boolean showMessage) { //Boolean is whether the reward was given or not
		
		//If player offline remember what rewards they have
		if (!player.isOnline()) {
			addToOfflineRewards(reward, player);
			return false;
		}
		Player onlinePlayer = player.getPlayer();
		
		//Rank Upgrade
		if (config.get("rewards." + reward + ".name").equals("Rank Upgrade")) {
			onlinePlayer.sendMessage("" + ChatColor.BLUE + "Rank upgraded!");
			return true;
		}
		else {
			//Count how many free slots in inventory
			ItemStack[] contents = onlinePlayer.getInventory().getStorageContents();
			int freeSlots = 0;
			for(ItemStack slot : contents) {
				if (slot == null) {
					freeSlots++;
				}
			}
			
			String name = config.getString("rewards." + reward + ".name");
			List<ItemStack> items = (List<ItemStack>) config.getList("rewards." + reward + ".items");
			
			if (items.size() <= freeSlots) { //If has enough free slots
				for (ItemStack item : items) {
					if (item == null || item.getType() == Material.AIR) continue;
					onlinePlayer.getInventory().addItem(item);
				}
				if (showMessage) {
					Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "[Rewards] " + ChatColor.BLUE + player.getName() + ChatColor.BLUE + " recieved " + ChatColor.ITALIC + name + "!");
				}
				return true;
			}
			else {
				if ((items.size() - freeSlots) == 1) {
					onlinePlayer.sendMessage(ChatColor.RED + "You must free 1 more inventory slot to collect " + config.getString("rewards." + reward + ".name") + ".");
				}
				else {
					onlinePlayer.sendMessage(ChatColor.RED + "You must free " + (items.size() - freeSlots) + " more inventory slots to collect " + config.getString("rewards." + reward + ".name") + ".");
				}
				addToOfflineRewards(reward, player);
				return false;
			}
		}
	}
	
	public boolean giveMiningReward(String reward, OfflinePlayer player, Boolean showMessage) { //Boolean is whether the reward was given or not
		
		//If player offline remember what rewards they have
		if (!player.isOnline()) {
			addToOfflineRewards(reward, player);
			return false;
		}
		Player onlinePlayer = player.getPlayer();
		
		//Rank Upgrade
		if (config.get("rewards." + reward + ".name").equals("Rank Upgrade")) {
			onlinePlayer.sendMessage("" + ChatColor.BLUE + "Rank upgraded!");
			return true;
		}
		else {
			//Count how many free slots in inventory
			ItemStack[] contents = onlinePlayer.getInventory().getStorageContents();
			int freeSlots = 0;
			for(ItemStack slot : contents) {
				if (slot == null) {
					freeSlots++;
				}
			}
			
			String name = config.getString("rewards." + reward + ".name");
			List<ItemStack> items = (List<ItemStack>) config.getList("rewards." + reward + ".items");
			
			if (items.size() <= freeSlots) { //If has enough free slots
				for (ItemStack item : items) {
					if (item == null || item.getType() == Material.AIR) continue;
					onlinePlayer.getInventory().addItem(item);
				}
				if (showMessage) {
					Bukkit.getServer().broadcastMessage(ChatColor.of("#3df090") + "[Mining Rewards] " + ChatColor.BLUE + player.getName() + ChatColor.BLUE + " found " + ChatColor.ITALIC + name + " while mining!");
				}
				return true;
			}
			else {
				if ((items.size() - freeSlots) == 1) {
					onlinePlayer.sendMessage(ChatColor.RED + "You must free 1 more inventory slot to /collect " + config.getString("rewards." + reward + ".name") + ".");
				}
				else {
					onlinePlayer.sendMessage(ChatColor.RED + "You must free " + (items.size() - freeSlots) + " more inventory slots to /collect " + config.getString("rewards." + reward + ".name") + ".");
				}
				addToOfflineRewards(reward, player);
				return false;
			}
		}
	}
	
	public String pickReward() {
		Set<String> rewards = config.getConfigurationSection("probabilities").getKeys(false);
		double total = 0; //Total probability pool
		for (String reward : rewards) {
			double likelihood = 1 / config.getDouble("probabilities." + reward);
			total += likelihood;
		}
		
		//Count up from 0 with increment = each individual likelihood, when random < counter choose that reward
		double random = Math.random() * total; //Random number between 0 and total
		double counter = 0;
		String generatedReward = null;
		for (String reward : rewards) {
			double likelihood = 1 / config.getDouble("probabilities." + reward);
			counter += likelihood;
			if (random <= counter) {							
				generatedReward = reward;
				break;
			}
		}
		return generatedReward;
	}
	
	@EventHandler
	public void onJoin (PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (config.getString("offline." + player.getName()) != null || config.getStringList("offline." + player.getName()).size() != 0) {
			List<String> offlineRewards = config.getStringList("offline." + player.getName());
			player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rewards] " + ChatColor.BLUE + "You have " + offlineRewards.size() + " rewards ready to /collect.");
		}
	}
}
