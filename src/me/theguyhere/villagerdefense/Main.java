package me.theguyhere.villagerdefense;

import me.theguyhere.villagerdefense.game.displays.InfoBoard;
import me.theguyhere.villagerdefense.game.displays.Leaderboard;
import me.theguyhere.villagerdefense.game.displays.Portal;
import me.theguyhere.villagerdefense.game.models.Game;
import me.theguyhere.villagerdefense.game.models.Tasks;
import me.theguyhere.villagerdefense.game.models.arenas.Arena;
import me.theguyhere.villagerdefense.listeners.*;
import me.theguyhere.villagerdefense.tools.DataManager;
import me.theguyhere.villagerdefense.tools.PacketReader;
import me.theguyhere.villagerdefense.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;

public class Main extends JavaPlugin {
	// Yaml file managers
	private final DataManager arenaData = new DataManager(this, "arenaData.yml");
	private final DataManager playerData = new DataManager(this, "playerData.yml");
	private final DataManager languageData = new DataManager(this, "languages/" +
			getConfig().getString("locale") + ".yml");

	private final Leaderboard leaderboard = new Leaderboard(this);
	private final InfoBoard infoBoard = new InfoBoard(this);
	private final PacketReader reader = new PacketReader();

	/**
	 * The amount of debug information to display in the console.
	 *
	 * 3 (Override) - All errors and information tracked will be displayed. Certain behavior will be overridden.
	 * 2 (Verbose) - All errors and information tracked will be displayed.
	 * 1 (Normal) - Errors that drastically reduce performance and important information will be displayed.
	 * 0 (Quiet) - Only the most urgent error messages will be displayed.
	 */
	private static int debugLevel = 0;
	private boolean outdated = false;
	int configVersion = 6;
	int arenaDataVersion = 4;
	int playerDataVersion = 1;
	int spawnTableVersion = 1;
	int languageFileVersion = 9;
	int defaultSpawnVersion = 2;

	// Runs when enabling plugin
	@Override
	public void onEnable() {
		saveDefaultConfig();

		PluginManager pm = getServer().getPluginManager();

		// Set up Game class
		Objects.requireNonNull(getArenaData().getConfigurationSection("")).getKeys(false).forEach(path -> {
			if (path.charAt(0) == 'a' && path.length() < 4)
				Game.arenas[Integer.parseInt(path.substring(1))] = new Arena(this,
						Integer.parseInt(path.substring(1)),
						new Tasks(this, Integer.parseInt(path.substring(1))));
		});
		Game.setLobby(Utils.getConfigLocation(this, "lobby"));

		Commands commands = new Commands(this);

		checkArenas();

		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			Utils.debugError("HolographicDisplays is not installed or not enabled.", 0);
			Utils.debugError("This plugin will be disabled.", 0);
			this.setEnabled(false);
			return;
		}

		// Set up commands and tab complete
		Objects.requireNonNull(getCommand("vd"), "'vd' command should exist").setExecutor(commands);
		Objects.requireNonNull(getCommand("vd"), "'vd' command should exist")
				.setTabCompleter(new CommandTab());

		// Register event listeners
		pm.registerEvents(new InventoryListener(this), this);
		pm.registerEvents(new JoinListener(this), this);
		pm.registerEvents(new DeathListener(this), this);
		pm.registerEvents(new ClickPortalListener(), this);
		pm.registerEvents(new GameListener(this), this);
		pm.registerEvents(new ArenaListener(this), this);
		pm.registerEvents(new AbilityListener(this), this);
		pm.registerEvents(new ChallengeListener(this), this);
		pm.registerEvents(new WorldListener(this), this);

		// Inject online players into packet reader
		if (!Bukkit.getOnlinePlayers().isEmpty())
			for (Player player : Bukkit.getOnlinePlayers()) {
				reader.inject(player);
			}

		// Check config version
		if (getConfig().getInt("version") < configVersion) {
			Utils.debugError("Your config.yml is outdated!", 0);
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "[VillagerDefense] " +
					"Please update to the latest version (" + ChatColor.BLUE + configVersion + ChatColor.RED +
					") to ensure compatibility.");
			outdated = true;
		}

		// Check if arenaData.yml is outdated
		if (getConfig().getInt("arenaData") < arenaDataVersion) {
			Utils.debugError("Your arenaData.yml is no longer supported with this version!", 0);
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "[VillagerDefense] " +
					"Please manually transfer arena data to version " + ChatColor.BLUE + arenaDataVersion +
					ChatColor.RED + ".");
			Utils.debugError("Please do not update your config.yml until your arenaData.yml has been updated.",
					0);
			outdated = true;
		}

		// Check if playerData.yml is outdated
		if (getConfig().getInt("playerData") < playerDataVersion) {
			Utils.debugError("Your playerData.yml is no longer supported with this version!", 0);
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "[VillagerDefense] " +
					"Please manually transfer player data to version " + ChatColor.BLUE + playerDataVersion +
					ChatColor.BLUE + ".");
			Utils.debugError("Please do not update your config.yml until your playerData.yml has been updated.",
					0);
			outdated = true;
		}

		// Check if spawn tables are outdated
		if (getConfig().getInt("spawnTableStructure") < spawnTableVersion) {
			Utils.debugError("Your spawn tables are no longer supported with this version!", 0);
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "[VillagerDefense] " +
					"Please manually transfer spawn table data to version " + ChatColor.BLUE + spawnTableVersion +
					ChatColor.RED + ".");
			Utils.debugError("Please do not update your config.yml until your spawn tables have been updated.",
					0);
			outdated = true;
		}

		// Check if default spawn table has been updated
		if (getConfig().getInt("spawnTableDefault") < defaultSpawnVersion) {
			Utils.debugInfo("The default.yml spawn table has been updated!", 0);
			getServer().getConsoleSender().sendMessage("[VillagerDefense] " +
					"Updating to version" + ChatColor.BLUE + defaultSpawnVersion + ChatColor.WHITE +
					" is optional but recommended.");
			Utils.debugInfo("Please do not update your config.yml unless your default.yml has been updated.",
					0);
		}

		// Check if language files are outdated
		if (getConfig().getInt("languageFile") < languageFileVersion) {
			Utils.debugError("You language files are no longer supported with this version!", 0);
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "[VillagerDefense] " +
					"Please update en_US.yml and update any other language files to version " + ChatColor.BLUE +
					languageFileVersion + ChatColor.RED + ".");
			Utils.debugError("Please do not update your config.yml until your language files have been updated.",
					0);
			outdated = true;
		}

		// Spawn in portals
		leaderboard.loadLeaderboards();
		infoBoard.loadInfoBoards();
	}

	// Runs when disabling plugin
	@Override
	public void onDisable() {
		// Remove uninject players
		for (Player player : Bukkit.getOnlinePlayers())
			reader.uninject(player);

		// Clear every valid arena and remove all portals
		Game.cleanAll();
		Portal.removePortals();
	}

	public InfoBoard getInfoBoard() {
		return infoBoard;
	}

	public Leaderboard getLeaderboard() {
		return leaderboard;
	}

	public PacketReader getReader() {
		return reader;
	}

	// Returns arena data
	public FileConfiguration getArenaData() {
		return arenaData.getConfig();
	}

	// Saves arena data changes
	public void saveArenaData() {
		arenaData.saveConfig();
	}

	// Returns player data
	public FileConfiguration getPlayerData() {
		return playerData.getConfig();
	}

	// Saves arena data changes
	public void savePlayerData() {
		playerData.saveConfig();
	}

	public FileConfiguration getLanguageData() {
		return languageData.getConfig();
	}

	// Check arenas for close
	private void checkArenas() {
		Arrays.stream(Game.arenas).filter(Objects::nonNull).forEach(Arena::checkClose);
	}

	public boolean isOutdated() {
		return outdated;
	}

	public static int getDebugLevel() {
		return debugLevel;
	}

	public static void setDebugLevel(int newDebugLevel) {
		debugLevel = newDebugLevel;
	}
}
