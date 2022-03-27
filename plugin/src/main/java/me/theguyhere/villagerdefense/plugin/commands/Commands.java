package me.theguyhere.villagerdefense.plugin.commands;

import me.theguyhere.villagerdefense.common.CommunicationManager;
import me.theguyhere.villagerdefense.common.Utils;
import me.theguyhere.villagerdefense.plugin.inventories.Inventories;
import me.theguyhere.villagerdefense.plugin.Main;
import me.theguyhere.villagerdefense.plugin.events.GameEndEvent;
import me.theguyhere.villagerdefense.plugin.events.LeaveArenaEvent;
import me.theguyhere.villagerdefense.plugin.exceptions.PlayerNotFoundException;
import me.theguyhere.villagerdefense.plugin.game.models.GameManager;
import me.theguyhere.villagerdefense.plugin.game.models.Tasks;
import me.theguyhere.villagerdefense.plugin.game.models.arenas.Arena;
import me.theguyhere.villagerdefense.plugin.game.models.arenas.ArenaStatus;
import me.theguyhere.villagerdefense.plugin.game.models.kits.Kit;
import me.theguyhere.villagerdefense.plugin.game.models.players.PlayerStatus;
import me.theguyhere.villagerdefense.plugin.game.models.players.VDPlayer;
import me.theguyhere.villagerdefense.plugin.tools.DataManager;
import me.theguyhere.villagerdefense.plugin.tools.LanguageManager;
import me.theguyhere.villagerdefense.plugin.tools.PlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class Commands implements CommandExecutor {
	private final Main plugin;
	private final FileConfiguration playerData;
	private final FileConfiguration arenaData;

	public Commands(Main plugin) {
		this.plugin = plugin;
		playerData = plugin.getPlayerData();
		arenaData = plugin.getArenaData();
	}
	
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
							 String[] args) {
		try {
			if (label.equalsIgnoreCase("vd")) {
				Player player;
				if (sender instanceof Player)
					player = (Player) sender;
				else player = null;

				// No arguments
				if (args.length == 0) {
					if (player != null)
						PlayerManager.notifyFailure(player, LanguageManager.errors.command, ChatColor.AQUA,
								"/vd help");
					else CommunicationManager.debugError(String.format(LanguageManager.errors.command, "vd help"),
							0);
					return true;
				}

				// Admin panel
				if (args[0].equalsIgnoreCase("admin")) {
					// Check for player executing command
					if (player == null) {
						sender.sendMessage(LanguageManager.errors.playerOnlyCommand);
						return true;
					}

					// Check for permission to use the command
					if (!player.hasPermission("vd.use")) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
						return true;
					}

					player.openInventory(Inventories.createArenasDashboard());
					return true;
				}

				// Redirects to wiki for help
				if (args[0].equalsIgnoreCase("help")) {
					if (player != null) {
						PlayerManager.notifyAlert(player, LanguageManager.messages.infoAboutWiki);
						TextComponent message = new TextComponent(LanguageManager.messages.visitWiki + "!");
						message.setBold(true);
						message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
								"https://github.com/Theguyhere0/villager-defense-minigame/wiki"));
						player.spigot().sendMessage(message);

					} else CommunicationManager.debugInfo(
							String.format("%s: https://github.com/Theguyhere0/villager-defense-minigame/wiki",
									LanguageManager.messages.visitWiki), 0);
					return true;
				}

				// Player leaves a game
				if (args[0].equalsIgnoreCase("leave")) {
					// Check for player executing command
					if (player == null) {
						sender.sendMessage(LanguageManager.errors.playerOnlyCommand);
						return true;
					}

					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
							Bukkit.getPluginManager().callEvent(new LeaveArenaEvent(player)));
					return true;
				}

				// Player checks stats
				if (args[0].equalsIgnoreCase("stats")) {
					// Check for player executing command
					if (player == null) {
						sender.sendMessage(LanguageManager.errors.playerOnlyCommand);
						return true;
					}

					if (args.length == 1)
						player.openInventory(Inventories.createPlayerStatsMenu(player));
					else if (plugin.getPlayerData().contains(args[1]))
						player.openInventory(Inventories.createPlayerStatsMenu(
								Objects.requireNonNull(Bukkit.getPlayer(args[1]))));
					else PlayerManager.notifyFailure(player, LanguageManager.messages.noStats,
								ChatColor.AQUA, args[1]);
					return true;
				}

				// Player checks kits
				if (args[0].equalsIgnoreCase("kits")) {
					// Check for player executing command
					if (player == null) {
						sender.sendMessage(LanguageManager.errors.playerOnlyCommand);
						return true;
					}

					player.openInventory(Inventories.createPlayerKitsMenu(player, player.getName()));
					return true;
				}

				// Player joins as phantom
				if (args[0].equalsIgnoreCase("join")) {
					// Check for player executing command
					if (player == null) {
						sender.sendMessage(LanguageManager.errors.playerOnlyCommand);
						return true;
					}

					Arena arena;
					VDPlayer gamer;

					// Attempt to get arena and player
					try {
						arena = GameManager.getArena(player);
						gamer = arena.getPlayer(player);
					} catch (Exception err) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.inGame);
						return true;
					}

					// Check if player owns the phantom kit if late arrival is not on
					if (!playerData.getBoolean(player.getName() + ".kits." + Kit.phantom().getName()) &&
							!arena.hasLateArrival()) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.phantomOwn);
						return true;
					}

					// Check if arena is not ending
					if (arena.getStatus() == ArenaStatus.ENDING) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.phantomArena);
						return true;
					}

					// Check for useful phantom use
					if (gamer.getStatus() != PlayerStatus.SPECTATOR) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.phantomPlayer);
						return true;
					}

					// Check for arena capacity if late arrival is on
					if (arena.hasLateArrival() && arena.getActiveCount() >= arena.getMaxPlayers()) {
						PlayerManager.notifyAlert(player, LanguageManager.messages.maxCapacity);
						return true;
					}

					// Let player join using phantom kit
					PlayerManager.teleAdventure(player, arena.getPlayerSpawn().getLocation());
					gamer.setStatus(PlayerStatus.ALIVE);
					arena.getTask().giveItems(gamer);
					GameManager.createBoard(gamer);
					gamer.setJoinedWave(arena.getCurrentWave());
					gamer.setKit(Kit.phantom());
					player.closeInventory();
					return true;
				}

				// Change crystal balance
				if (args[0].equalsIgnoreCase("crystals")) {
					// Check for permission to use the command
					if (player != null && !player.hasPermission("vd.crystals")) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
						return true;
					}

					// Check for valid command format
					if (args.length != 3) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.messages.commandFormat, ChatColor.AQUA,
									"/vd crystals [player] [change amount]");
						else CommunicationManager.debugError(String.format(LanguageManager.messages.commandFormat,
								"vd crystals [player] [change amount]"), 0);
						return true;
					}

					// Check for valid player
					if (!plugin.getPlayerData().contains(args[1])) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.errors.invalidPlayer);
						else CommunicationManager.debugError(LanguageManager.errors.invalidPlayer, 0);
						return true;
					}

					// Check for valid amount
					try {
						int amount = Integer.parseInt(args[2]);
						playerData.set(args[1] + ".crystalBalance", Math.max(playerData.getInt(args[1] +
								".crystalBalance") + amount, 0));
						plugin.savePlayerData();
						if (player != null)
							PlayerManager.notifySuccess(player, LanguageManager.confirms.balanceSet,
									ChatColor.AQUA, args[1],
									String.valueOf(playerData.getInt(args[1] + ".crystalBalance")));
						else CommunicationManager.debugInfo(String.format(LanguageManager.confirms.balanceSet, args[1],
								String.valueOf(playerData.getInt(args[1] + ".crystalBalance"))), 0);
						return true;
					} catch (Exception e) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.errors.integer);
						else CommunicationManager.debugError(LanguageManager.errors.integer, 0);
						return true;
					}
				}

				// Force start
				if (args[0].equalsIgnoreCase("start")) {
					// Start current arena
					if (args.length == 1) {
						// Check for player executing command
						if (player == null) {
							sender.sendMessage(LanguageManager.errors.playerOnlyCommand);
							return true;
						}

						// Check for permission to use the command
						if (!player.hasPermission("vd.start")) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
							return true;
						}

						Arena arena;
						VDPlayer gamer;

						// Attempt to get arena and player
						try {
							arena = GameManager.getArena(player);
							gamer = arena.getPlayer(player);
						} catch (Exception e) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.inGame);
							return true;
						}

						// Check if player is an active player
						if (!arena.getActives().contains(gamer)) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.activePlayer);
							return true;
						}

						// Check if arena already started
						if (arena.getStatus() != ArenaStatus.WAITING) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.arenaInProgress);
							return true;
						}

						Tasks task = arena.getTask();
						Map<Runnable, Integer> tasks = task.getTasks();
						BukkitScheduler scheduler = Bukkit.getScheduler();

						// Bring game to quick start if not already
						if (tasks.containsKey(task.full10) || tasks.containsKey(task.sec10) &&
								!scheduler.isQueued(tasks.get(task.sec10))) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.startingSoon);
							return true;
						} else {
							// Remove all tasks
							tasks.forEach((runnable, id) -> scheduler.cancelTask(id));
							tasks.clear();

							// Schedule accelerated countdown tasks
							task.sec10.run();
							tasks.put(task.sec10, 0); // Dummy task id to note that quick start condition was hit
							tasks.put(task.sec5,
									scheduler.scheduleSyncDelayedTask(plugin, task.sec5, Utils.secondsToTicks(5)));
							tasks.put(task.start,
									scheduler.scheduleSyncDelayedTask(plugin, task.start, Utils.secondsToTicks(10)));
						}
					}

					// Start specific arena
					else {
						// Check for permission to use the command
						if (player != null && !player.hasPermission("vd.admin")) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
							return true;
						}

						StringBuilder name = new StringBuilder(args[1]);
						for (int i = 0; i < args.length - 2; i++)
							name.append(" ").append(args[i + 2]);
						Arena arena;

						// Check if this arena exists
						try {
							arena = Objects.requireNonNull(GameManager.getArena(name.toString()));
						} catch (Exception e) {
							if (player != null)
								PlayerManager.notifyFailure(player, LanguageManager.errors.noArena);
							else CommunicationManager.debugError(LanguageManager.errors.noArena, 
									0);
							return true;
						}

						// Check if arena already started
						if (arena.getStatus() != ArenaStatus.WAITING) {
							if (player != null)
								PlayerManager.notifyFailure(player, 
										LanguageManager.errors.arenaInProgress);
							else CommunicationManager.debugError(
									LanguageManager.errors.arenaInProgress, 0);
							return true;
						}

						// Check if there is at least 1 player
						if (arena.getActiveCount() == 0) {
							if (player != null)
								PlayerManager.notifyFailure(player, LanguageManager.errors.arenaNoPlayers);
							else CommunicationManager.debugError(LanguageManager.errors.arenaNoPlayers, 0);
							return true;
						}

						Tasks task = arena.getTask();
						Map<Runnable, Integer> tasks = task.getTasks();
						BukkitScheduler scheduler = Bukkit.getScheduler();

						// Bring game to quick start if not already
						if (tasks.containsKey(task.full10) || tasks.containsKey(task.sec10) &&
								!scheduler.isQueued(tasks.get(task.sec10))) {
							if (player != null)
								PlayerManager.notifyFailure(player, 
										LanguageManager.errors.startingSoon);
							else CommunicationManager.debugError(LanguageManager.errors.startingSoon,
									0);
							return true;
						} else {
							// Remove all tasks
							tasks.forEach((runnable, id) -> scheduler.cancelTask(id));
							tasks.clear();

							// Schedule accelerated countdown tasks
							task.sec10.run();
							tasks.put(task.sec10, 0); // Dummy task id to note that quick start condition was hit
							tasks.put(task.sec5,
									scheduler.scheduleSyncDelayedTask(plugin, task.sec5, Utils.secondsToTicks(5)));
							tasks.put(task.start,
									scheduler.scheduleSyncDelayedTask(plugin, task.start, Utils.secondsToTicks(10)));

							// Notify console
							CommunicationManager.debugInfo(arena.getArena() + " was force started.", 1);
						}
					}

					return true;
				}

				// Force end
				if (args[0].equalsIgnoreCase("end")) {
					// End current arena
					if (args.length == 1) {
						// Check for player executing command
						if (player == null) {
							sender.sendMessage(LanguageManager.errors.playerOnlyCommand);
							return true;
						}

						// Check for permission to use the command
						if (!player.hasPermission("vd.admin")) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
							return true;
						}

						Arena arena;

						// Attempt to get arena
						try {
							arena = GameManager.getArena(player);
						} catch (Exception e) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.inGame);
							return true;
						}

						// Check if arena has a game in progress
						if (arena.getStatus() != ArenaStatus.ACTIVE && arena.getStatus() != ArenaStatus.ENDING) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.noGameEnd);
							return true;
						}

						// Check if game is about to end
						if (arena.getStatus() == ArenaStatus.ENDING) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.endingSoon);
							return true;
						}

						// Force end
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
								Bukkit.getPluginManager().callEvent(new GameEndEvent(arena)));

						// Notify console
						CommunicationManager.debugInfo(arena.getArena() + " was force ended.", 1);
					}

					// End specific arena
					else {
						// Check for permission to use the command
						if (player != null && !player.hasPermission("vd.admin")) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
							return true;
						}

						StringBuilder name = new StringBuilder(args[1]);
						for (int i = 0; i < args.length - 2; i++)
							name.append(" ").append(args[i + 2]);
						Arena arena;

						// Check if this arena exists
						try {
							arena = Objects.requireNonNull(GameManager.getArena(name.toString()));
						} catch (Exception e) {
							if (player != null)
								PlayerManager.notifyFailure(player, LanguageManager.errors.noArena);
							else CommunicationManager.debugError(LanguageManager.errors.noArena, 
									0);
							return true;
						}

						// Check if arena has a game in progress
						if (arena.getStatus() != ArenaStatus.ACTIVE && arena.getStatus() != ArenaStatus.ENDING) {
							if (player != null)
								PlayerManager.notifyFailure(player, LanguageManager.errors.noGameEnd);
							else CommunicationManager.debugError(LanguageManager.errors.noGameEnd,
									0);
							return true;
						}

						// Check if game is about to end
						if (arena.getStatus() == ArenaStatus.ENDING) {
							if (player != null)
								PlayerManager.notifyFailure(player, LanguageManager.errors.endingSoon);
							else CommunicationManager.debugError(LanguageManager.errors.endingSoon,
									0);
							return true;
						}

						// Force end
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
								Bukkit.getPluginManager().callEvent(new GameEndEvent(arena)));

						// Notify console
						CommunicationManager.debugInfo(arena.getArena() + " was force ended.", 1);

						return true;
					}
				}

				// Force delay start
				if (args[0].equalsIgnoreCase("delay")) {
					// Delay current arena
					if (args.length == 1) {
						// Check for player executing command
						if (player == null) {
							sender.sendMessage(LanguageManager.errors.playerOnlyCommand);
							return true;
						}

						// Check for permission to use the command
						if (!player.hasPermission("vd.start")) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
							return true;
						}

						Arena arena;

						// Attempt to get arena
						try {
							arena = GameManager.getArena(player);
						} catch (Exception e) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.inGame);
							return true;
						}

						// Check if arena already started
						if (arena.getStatus() != ArenaStatus.WAITING) {
							PlayerManager.notifyFailure(player, 
									LanguageManager.errors.arenaInProgress);
							return true;
						}

						Tasks task = arena.getTask();
						Map<Runnable, Integer> tasks = task.getTasks();
						BukkitScheduler scheduler = Bukkit.getScheduler();

						// Remove all tasks
						tasks.forEach((runnable, id) -> scheduler.cancelTask(id));
						tasks.clear();

						// Reschedule countdown tasks
						task.min2.run();
						tasks.put(task.min1, scheduler.scheduleSyncDelayedTask(plugin, task.min1,
								Utils.secondsToTicks(Utils.minutesToSeconds(1))));
						tasks.put(task.sec30, scheduler.scheduleSyncDelayedTask(plugin, task.sec30,
								Utils.secondsToTicks(Utils.minutesToSeconds(2) - 30)));
						tasks.put(task.sec10, scheduler.scheduleSyncDelayedTask(plugin, task.sec10,
								Utils.secondsToTicks(Utils.minutesToSeconds(2) - 10)));
						tasks.put(task.sec5, scheduler.scheduleSyncDelayedTask(plugin, task.sec5,
								Utils.secondsToTicks(Utils.minutesToSeconds(2) - 5)));
						tasks.put(task.start, scheduler.scheduleSyncDelayedTask(plugin, task.start,
								Utils.secondsToTicks(Utils.minutesToSeconds(2))));
					}

					// Delay specific arena
					else {
						// Check for permission to use the command
						if (player != null && !player.hasPermission("vd.admin")) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
							return true;
						}

						StringBuilder name = new StringBuilder(args[1]);
						for (int i = 0; i < args.length - 2; i++)
							name.append(" ").append(args[i + 2]);
						Arena arena;

						// Check if this arena exists
						try {
							arena = Objects.requireNonNull(GameManager.getArena(name.toString()));
						} catch (Exception e) {
							if (player != null)
								PlayerManager.notifyFailure(player, LanguageManager.errors.noArena);
							else CommunicationManager.debugError(LanguageManager.errors.noArena, 0);
							return true;
						}

						// Check if arena already started
						if (arena.getStatus() != ArenaStatus.WAITING) {
							if (player != null)
								PlayerManager.notifyFailure(player, LanguageManager.errors.arenaInProgress);
							else CommunicationManager.debugError(LanguageManager.errors.arenaInProgress, 0);
							return true;
						}

						// Check if there is at least 1 player
						if (arena.getActiveCount() == 0) {
							if (player != null)
								PlayerManager.notifyFailure(player, LanguageManager.errors.emptyArena);
							else CommunicationManager.debugError(LanguageManager.errors.emptyArena, 0);
							return true;
						}

						Tasks task = arena.getTask();
						Map<Runnable, Integer> tasks = task.getTasks();
						BukkitScheduler scheduler = Bukkit.getScheduler();

						// Remove all tasks
						tasks.forEach((runnable, id) -> scheduler.cancelTask(id));
						tasks.clear();

						// Reschedule countdown tasks
						task.min2.run();
						tasks.put(task.min1, scheduler.scheduleSyncDelayedTask(plugin, task.min1,
								Utils.secondsToTicks(Utils.minutesToSeconds(1))));
						tasks.put(task.sec30, scheduler.scheduleSyncDelayedTask(plugin, task.sec30,
								Utils.secondsToTicks(Utils.minutesToSeconds(2) - 30)));
						tasks.put(task.sec10, scheduler.scheduleSyncDelayedTask(plugin, task.sec10,
								Utils.secondsToTicks(Utils.minutesToSeconds(2) - 10)));
						tasks.put(task.sec5, scheduler.scheduleSyncDelayedTask(plugin, task.sec5,
								Utils.secondsToTicks(Utils.minutesToSeconds(2) - 5)));
						tasks.put(task.start, scheduler.scheduleSyncDelayedTask(plugin, task.start,
								Utils.secondsToTicks(Utils.minutesToSeconds(2))));

						// Notify console
						CommunicationManager.debugInfo(arena.getArena() + " was delayed.", 1);
					}

					return true;
				}

				// Fix certain default files
				if (args[0].equalsIgnoreCase("fix")) {
					boolean fixed = false;

					// Check for permission to use the command
					if (player != null && !player.hasPermission("vd.admin")) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
						return true;
					}

					// Check for correct format
					if (args.length > 1) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.messages.commandFormat,
									ChatColor.AQUA, "/vd fix");
						else CommunicationManager.debugError(String.format(LanguageManager.messages.commandFormat,
								"vd fix"), 0);
						return true;
					}

					// Check if config.yml is outdated
					int configVersion = plugin.getConfig().getInt("version");
					if (configVersion < Main.configVersion)
						if (player != null)
							PlayerManager.notifyAlert(player, 
									LanguageManager.messages.manualUpdateWarn, ChatColor.AQUA, "config.yml");
						else CommunicationManager.debugError(String.format(LanguageManager.messages.manualUpdateWarn,
								"config.yml"), 0);

					// Check if arenaData.yml is outdated
					int arenaDataVersion = plugin.getConfig().getInt("arenaData");
					if (arenaDataVersion < 4) {
						try {
							// Transfer portals
							Objects.requireNonNull(arenaData.getConfigurationSection("portal"))
									.getKeys(false).forEach(arenaID -> {
										Location location = DataManager.getConfigLocation(plugin,
												"portal." + arenaID);
										DataManager.setConfigurationLocation(plugin, "a" + arenaID + ".portal",
												location);
										arenaData.set("portal." + arenaID, null);
									});
							arenaData.set("portal", null);

							// Transfer arena boards
							Objects.requireNonNull(arenaData.getConfigurationSection("arenaBoard"))
									.getKeys(false).forEach(arenaID -> {
										Location location = DataManager.getConfigLocation(plugin,
												"arenaBoard." + arenaID);
										DataManager.setConfigurationLocation(plugin, "a" + arenaID + ".arenaBoard",
												location);
										arenaData.set("arenaBoard." + arenaID, null);
									});
							arenaData.set("arenaBoard", null);

							plugin.saveArenaData();

							// Reload portals
							GameManager.refreshPortals();

							// Flip flag and update config.yml
							fixed = true;
							plugin.getConfig().set("arenaData", 4);
							plugin.saveConfig();

							// Notify
							if (player != null)
								PlayerManager.notifySuccess(player,
										LanguageManager.confirms.autoUpdate, ChatColor.AQUA,
										"arenaData.yml", "4");
							CommunicationManager.debugInfo(String.format(LanguageManager.confirms.autoUpdate,
									"arenaData.yml", "4"), 0);
						} catch (Exception e) {
							if (player != null)
								PlayerManager.notifyAlert(player, LanguageManager.messages.manualUpdateWarn,
										ChatColor.AQUA, "arenaData.yml");
							else CommunicationManager.debugError(String.format(
									LanguageManager.messages.manualUpdateWarn, "arenaData.yml"), 0);
						}
					} else if (arenaDataVersion < 5) {
						try {
							// Translate waiting sounds
							Objects.requireNonNull(arenaData.getConfigurationSection("")).getKeys(false)
									.forEach(key -> {
										String path = key + ".sounds.waiting";
										if (key.charAt(0) == 'a' && key.length() < 4 && arenaData.contains(path)) {
											int oldValue = arenaData.getInt(path);
											switch (oldValue) {
												case 0:
													arenaData.set(path, "cat");
													break;
												case 1:
													arenaData.set(path, "blocks");
													break;
												case 2:
													arenaData.set(path, "far");
													break;
												case 3:
													arenaData.set(path, "strad");
													break;
												case 4:
													arenaData.set(path, "mellohi");
													break;
												case 5:
													arenaData.set(path, "ward");
													break;
												case 9:
													arenaData.set(path, "chirp");
													break;
												case 10:
													arenaData.set(path, "stal");
													break;
												case 11:
													arenaData.set(path, "mall");
													break;
												case 12:
													arenaData.set(path, "wait");
													break;
												case 13:
													arenaData.set(path, "pigstep");
													break;
												default:
													arenaData.set(path, "none");
											}
										}
									});
							plugin.saveArenaData();

							// Flip flag and update config.yml
							fixed = true;
							plugin.getConfig().set("arenaData", 5);
							plugin.saveConfig();

							// Notify
							if (player != null)
								PlayerManager.notifySuccess(player,
										LanguageManager.confirms.autoUpdate, ChatColor.AQUA, "arenaData.yml",
										"5");
							CommunicationManager.debugInfo(String.format(LanguageManager.confirms.autoUpdate,
									"arenaData.yml", "5"), 0);
						} catch (Exception e) {
							if (player != null)
								PlayerManager.notifyAlert(player,
										LanguageManager.messages.manualUpdateWarn, ChatColor.AQUA,
										"arenaData.yml");
							else CommunicationManager.debugError(
									String.format(LanguageManager.messages.manualUpdateWarn,
											"arenaData.yml"), 0);
						}
					}

					// Check if playerData.yml is outdated
					if (plugin.getConfig().getInt("playerData") < Main.playerDataVersion)
						if (player != null)
							PlayerManager.notifyAlert(player, 
									LanguageManager.messages.manualUpdateWarn, ChatColor.AQUA,
									"playerData.yml");
						else CommunicationManager.debugError(
								String.format(LanguageManager.messages.manualUpdateWarn,
								"playerData.yml"), 0);

					// Update default spawn table
					if (plugin.getConfig().getInt("spawnTableStructure") < Main.spawnTableVersion ||
							plugin.getConfig().getInt("spawnTableDefault") < Main.defaultSpawnVersion) {
						// Flip flag
						fixed = true;

						// Fix
						plugin.saveResource("default.yml", true);
						plugin.getConfig().set("spawnTableStructure", Main.spawnTableVersion);
						plugin.getConfig().set("spawnTableDefault", Main.defaultSpawnVersion);
						plugin.saveConfig();

						// Notify
						if (player != null) {
							PlayerManager.notifySuccess(player, LanguageManager.confirms.autoUpdate,
									ChatColor.AQUA, "default.yml", String.valueOf(Main.defaultSpawnVersion));
							PlayerManager.notifyAlert(player, 
									LanguageManager.messages.manualUpdateWarn, ChatColor.AQUA, 
									"All other spawn files");
						}
						CommunicationManager.debugInfo(String.format(LanguageManager.confirms.autoUpdate,
								"default.yml", String.valueOf(Main.defaultSpawnVersion)), 0);
						CommunicationManager.debugError(
								String.format(LanguageManager.messages.manualUpdateWarn,
										"All other spawn files"), 0);
					}

					// Update default language file
					if (plugin.getConfig().getInt("languageFile") < Main.languageFileVersion) {
						// Flip flag
						fixed = true;

						// Fix
						plugin.saveResource("languages/en_US.yml", true);
						plugin.getConfig().set("languageFile", Main.languageFileVersion);
						plugin.saveConfig();

						// Notify
						if (player != null) {
							PlayerManager.notifySuccess(player, LanguageManager.confirms.autoUpdate,
									ChatColor.AQUA, "en_US.yml", String.valueOf(Main.languageFileVersion));
							PlayerManager.notifyAlert(player, 
									LanguageManager.messages.manualUpdateWarn, ChatColor.AQUA, 
									"All other language files");
							PlayerManager.notifyAlert(player, LanguageManager.messages.restartPlugin);
						}
						CommunicationManager.debugInfo(String.format(LanguageManager.confirms.autoUpdate, "en_US.yml",
								String.valueOf(Main.languageFileVersion)), 0);
						CommunicationManager.debugError(
								String.format(LanguageManager.messages.manualUpdateWarn,
										"All other language files"), 0);
						CommunicationManager.debugError(LanguageManager.messages.restartPlugin, 0);
					}

					// Message to player depending on whether the command fixed anything
					if (!fixed)
						if (player != null)
							PlayerManager.notifyAlert(player, LanguageManager.messages.noAutoUpdate);
						else CommunicationManager.debugInfo(LanguageManager.messages.noAutoUpdate, 0);

					return true;
				}

				// Change plugin debug level
				if (args[0].equalsIgnoreCase("debug")) {
					// Check for permission to use the command
					if (player != null && !player.hasPermission("vd.admin")) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
						return true;
					}

					// Check for correct format
					if (args.length != 2) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.messages.commandFormat,
									ChatColor.AQUA, "/vd debug [debug level (0-3)]");
						else CommunicationManager.debugError(String.format(LanguageManager.messages.commandFormat,
										"vd debug [debug level (0-3)]"), 0);
						return true;
					}

					// Set debug level
					try {
						CommunicationManager.setDebugLevel(Integer.parseInt(args[1]));
					} catch (Exception e) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.messages.commandFormat,
									ChatColor.AQUA, "/vd debug [debug level (0-3)]");
						else CommunicationManager.debugError(String.format(LanguageManager.messages.commandFormat,
										"vd debug [debug level (0-3)]"), 0);
						return true;
					}

					// Notify
					if (player != null)
						PlayerManager.notifySuccess(player, LanguageManager.messages.debugLevelSet, ChatColor.AQUA,
								args[1]);
					else CommunicationManager.debugInfo(String.format(LanguageManager.messages.debugLevelSet, args[1]),
							0);

					return true;
				}

				// Player kills themselves
				if (args[0].equalsIgnoreCase("die")) {
					// Check for player executing command
					if (player == null) {
						sender.sendMessage(LanguageManager.errors.playerOnlyCommand);
						return true;
					}

					// Check for player in a game
					if (!GameManager.checkPlayer(player)) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.notInGame);
						return true;
					}

					// Check for player in an active game
					if (Arrays.stream(GameManager.getArenas()).filter(Objects::nonNull)
							.filter(arena -> arena.getStatus() == ArenaStatus.ACTIVE)
							.noneMatch(arena -> arena.hasPlayer(player))) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.suicideActive);
						return true;
					}

					// Check for alive player
					try {
						if (GameManager.getArena(player).getPlayer(player).getStatus() != PlayerStatus.ALIVE) {
							PlayerManager.notifyFailure(player, LanguageManager.errors.suicide);
							return true;
						}
					} catch (PlayerNotFoundException err) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.suicide);
						return true;
					}

					// Create a player death and make sure it gets detected
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
							Bukkit.getPluginManager().callEvent(new EntityDamageEvent(player,
									EntityDamageEvent.DamageCause.SUICIDE, 99)));
					return true;
				}

				// Reload internal plugin data
				if (args[0].equalsIgnoreCase("reload")) {
					// Check for permission to use the command
					if (player != null && !player.hasPermission("vd.admin")) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
						return true;
					}

					// Notify of reload
					if (player != null)
						PlayerManager.notifyAlert(player, "Reloading plugin data");
					else CommunicationManager.debugInfo("Reloading plugin data", 0);

					plugin.reload();
					return true;
				}

				// Attempt to open arena
				if (args[0].equalsIgnoreCase("open")) {
					// Check for permission to use the command
					if (player != null && !player.hasPermission("vd.use")) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
						return true;
					}

					// Check for valid command format
					if (args.length < 2) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.messages.commandFormat,
									ChatColor.AQUA, "/vd open [arena name]");
						else
							CommunicationManager.debugError(String.format(LanguageManager.messages.commandFormat,
											"vd open [arena name]"), 0);
						return true;
					}

					StringBuilder name = new StringBuilder(args[1]);
					for (int i = 0; i < args.length - 2; i++)
						name.append(" ").append(args[i + 2]);
					Arena arena;

					// Check if this arena exists
					try {
						arena = Objects.requireNonNull(GameManager.getArena(name.toString()));
					} catch (Exception e) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.errors.noArena);
						else CommunicationManager.debugError(LanguageManager.errors.noArena,
								0);
						return true;
					}

					// Check if arena is already open
					if (!arena.isClosed()) {
						if (player != null)
							PlayerManager.notifyFailure(player, "Arena is already open!");
						else CommunicationManager.debugError("Arena is already open!", 0);
						return true;
					}

					// No lobby
					if (!plugin.getArenaData().contains("lobby")) {
						if (player != null)
							PlayerManager.notifyFailure(player, "Arena cannot open without a lobby!");
						else CommunicationManager.debugError("Arena cannot open without a lobby!", 0);
						return true;
					}

					// No arena portal
					if (arena.getPortalLocation() == null) {
						if (player != null)
							PlayerManager.notifyFailure(player, "Arena cannot open without a portal!");
						else CommunicationManager.debugError("Arena cannot open without a portal!", 0);
						return true;
					}

					// No player spawn
					if (arena.getPlayerSpawn() == null) {
						if (player != null)
							PlayerManager.notifyFailure(player, "Arena cannot open without a player spawn!");
						else CommunicationManager.debugError("Arena cannot open without a player spawn!",
								0);
						return true;
					}

					// No monster spawn
					if (arena.getMonsterSpawns().isEmpty()) {
						if (player != null)
							PlayerManager.notifyFailure(player, "Arena cannot open without a monster spawn!");
						else CommunicationManager.debugError("Arena cannot open without a monster spawn!",
								0);
						return true;
					}

					// No villager spawn
					if (arena.getVillagerSpawns().isEmpty()) {
						if (player != null)
							PlayerManager.notifyFailure(player, "Arena cannot open without a villager spawn!");
						else CommunicationManager.debugError("Arena cannot open without a villager spawn!",
								0);
						return true;
					}

					// No shops
					if (!arena.hasCustom() && !arena.hasNormal()) {
						if (player != null)
							PlayerManager.notifyFailure(player, "Arena cannot open without a shop!");
						else CommunicationManager.debugError("Arena cannot open without a shop!", 0);
						return true;
					}

					// Invalid arena bounds
					if (arena.getCorner1() == null || arena.getCorner2() == null ||
							!Objects.equals(arena.getCorner1().getWorld(), arena.getCorner2().getWorld())) {
						if (player != null)
							PlayerManager.notifyFailure(player, "Arena cannot open without valid arena bounds!");
						else CommunicationManager.debugError("Arena cannot open without valid arena bounds!",
								0);
						return true;
					}

					// Open arena
					arena.setClosed(false);

					// Notify console and possibly player
					if (player != null)
						PlayerManager.notifySuccess(player, arena.getName() +  " was opened.");
					CommunicationManager.debugInfo(arena.getArena() + " was opened.", 1);

					return true;
				}

				// Attempt to close arena
				if (args[0].equalsIgnoreCase("close")) {
					// Check for permission to use the command
					if (player != null && !player.hasPermission("vd.use")) {
						PlayerManager.notifyFailure(player, LanguageManager.errors.permission);
						return true;
					}

					// Check for valid command format
					if (args.length < 2) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.messages.commandFormat,
									ChatColor.AQUA, "/vd close [arena name]");
						else
							CommunicationManager.debugError(String.format(LanguageManager.messages.commandFormat,
											"vd close [arena name]"), 0);
						return true;
					}

					StringBuilder name = new StringBuilder(args[1]);
					for (int i = 0; i < args.length - 2; i++)
						name.append(" ").append(args[i + 2]);
					Arena arena;

					// Check if this arena exists
					try {
						arena = Objects.requireNonNull(GameManager.getArena(name.toString()));
					} catch (Exception e) {
						if (player != null)
							PlayerManager.notifyFailure(player, LanguageManager.errors.noArena);
						else CommunicationManager.debugError(LanguageManager.errors.noArena,
								0);
						return true;
					}

					// Check if arena is already closed
					if (arena.isClosed()) {
						if (player != null)
							PlayerManager.notifyFailure(player, "Arena is already closed!");
						else CommunicationManager.debugError("Arena is already closed!", 0);
						return true;
					}

					// Close arena
					arena.setClosed(true);

					// Notify console and possibly player
					if (player != null)
						PlayerManager.notifySuccess(player, arena.getName() +  " was closed.");
					CommunicationManager.debugInfo(arena.getArena() + " was closed.", 1);

					return true;
				}

				// No valid command sent
				if (player != null)
					PlayerManager.notifyFailure(player, LanguageManager.errors.command, ChatColor.AQUA,
							"/vd help");
				else CommunicationManager.debugError(String.format(LanguageManager.errors.command, "vd help"),
						0);
				return true;
			}
		} catch (NullPointerException e) {
			CommunicationManager.debugError("The language file is missing some attributes, please update it!",
					0);
		}
		return false;
	}
}
