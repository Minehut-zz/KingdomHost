package com.minehut.kingdomhost.manager;

import com.minehut.commons.common.bungee.Bungee;
import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.chat.F;
import com.minehut.kingdomhost.KingdomHost;
import com.minehut.kingdomhost.events.ServerShutdownEvent;
import com.minehut.kingdomhost.server.Server;
import com.minehut.kingdomhost.offline.OfflineServer;
import com.minehut.kingdomhost.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.*;
import java.util.*;

/**
 * Created by Luke on 1/22/15.
 */
public class ServerManager implements Listener {
	KingdomHost host;
	ArrayList<Server> servers;
	ArrayList<OfflineServer> offlineServers;

	public ServerManager(KingdomHost host) {
		this.servers = new ArrayList<>();
		this.offlineServers = new ArrayList<>();

		this.host = host;
		this.loadConfig();
		Bukkit.getPluginManager().registerEvents(this, this.host);
	}

	int getOpenPort() {
		for (int i = 60001; i < 60500; i++) {
			for (Server server : this.servers) {
				if (server.getPort() == i) {
					continue;
				}
				return i;
			}
		}
		return 0;
	}

	public boolean connect(Player player, String name) {
		/* Try to join online server */
		for (Server server : this.servers) {
			if (server.getKingdomName().equals(name)) {
				player.sendMessage("Sending you to " + C.aqua + name);
				Bungee.sendToServer(this.host, player, "kingdom" + Integer.toString(server.getPort() - 60000));
				return true;
			}
		}

		/* Server isn't online, try to start it */
		for (OfflineServer os : this.offlineServers) {
			if (os.getKingdomName().equals(name)) {
				Server server = new Server(os.getOwnerUUID(), os.getId(), getOpenPort(), os.getKingdomName(), os.getMaxPlayers(), os.getBorderSize(), os.getMaxPlugins());
				this.servers.add(server);
				server.start();

				/* Attempt to connect once more. Delay for startup time */
				connect(player, name, 10);
				return true;
			}
		}

		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("Your requested server could not be found.");
		player.sendMessage("Please ensure your caps and spelling are correct");
		player.sendMessage("");
		return false;
	}

	private void connect(Player player, String name, int delay) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {
				connect(player, name);
			}
		}, 20 * delay);
	}

	public void createServer(final Player player, final String name) {
		Bukkit.getScheduler().runTaskAsynchronously(this.host, new Runnable() {
			@Override
			public void run() {

				/* Check if name is available */
				if (!isNewName(player, name)) {
					return;
				}

				/* Check if maxed out */
				if (maxRuntimeServers()) {
					player.sendMessage("");
					player.sendMessage("");
					player.sendMessage(C.white + "Our servers are currently full. Please wait to create a new Server");
					player.sendMessage("");
					return;
				}

				/* Check for existing server */
				for (Server server : servers) {
					if (server.getOwnerUUID() == player.getUniqueId()) {
						player.sendMessage("");
						player.sendMessage("");
						player.sendMessage(C.white + "You already have a Kingdom. Please use " + C.aqua + "/join");
						player.sendMessage(C.white + "To reset your Kingdom, use " + C.aqua + "/reset");
						player.sendMessage("");
						return;
					}
				}

				/* Begin creation process */
				player.sendMessage("");
				player.sendMessage("");
				player.sendMessage(C.white + "Creating your new Kingdom");
				player.sendMessage(C.white + "This may take a few moments...");
				player.sendMessage("");

				/* Size is greater than index */
				int id = offlineServers.size();
				int port = getOpenPort();

				/* todo: rank-based perks */
				OfflineServer offlineServer = new OfflineServer(id, name, player.getUniqueId(), 10, 500, 5);

				/* Copy files */
				FileUtil.copySampleServer(id);
				FileUtil.editServerProperties(id, port);

				/* Start up server */
				Server server = new Server(player.getUniqueId(), id, port, name, offlineServer.getMaxPlayers(), offlineServer.getBorderSize(), offlineServer.getMaxPlugins());
				servers.add(server);
				server.start();

				/* Save to config */
				saveKingdomToConfig(offlineServer);

				/* Connect after server has started (delay) */
				connect(player, name, 10);
			}
		});
	}

	public void resetServer(final Player player, final String name, final int id) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {
				/* Check if name is available */
				if (!isNewName(player, name)) {
					return;
				}

				/* Check if maxed out */
				if (maxRuntimeServers()) {
					player.sendMessage(C.red + "Our servers are currently full. Please wait to create a new Server");
				}

				/* Shutdown server */
				for (Server server : servers) {
					if (server.getOwnerUUID() == player.getUniqueId()) {
						server.forceShutdown();
						break;
					}
				}

				/* Use data from offline-server */
				OfflineServer offlineServer = null;
				for (OfflineServer os : offlineServers) {
					if (os.getOwnerUUID() == player.getUniqueId()) {
						offlineServer = os;
					}
				}

				/* Check to see if server existed */
				if (offlineServer == null) {
					player.sendMessage(C.red + "You do not have an existing server.");
					return;
				}

				/* Begin creation process */
				player.sendMessage(C.gold + "Creating your new Kingdom");
				player.sendMessage(C.gold + "This may take a few moments...");

				/* Size is greater than index */
				int id = offlineServer.getId();
				int port = getOpenPort();

				/* Copy files */
				FileUtil.copySampleServer(id);
				FileUtil.editServerProperties(id, port);

				/* Start up server */
				Server server = new Server(player.getUniqueId(), id, port, name, offlineServer.getMaxPlayers(), offlineServer.getBorderSize(), offlineServer.getMaxPlugins());
				servers.add(server);
				server.start();

				/* Save to config */
				saveKingdomToConfig(offlineServer);

				/* Connect after server has started (delay) */
				connect(player, name, 10);

			}
		}, 1);
	}

	public void changeName(Player player, String name) {
		OfflineServer offlineServer = null;
		for (OfflineServer os : this.offlineServers) {
			if (os.getOwnerUUID() == player.getUniqueId()) {
				offlineServer = os;
				break;
			}
		}

		if (offlineServer == null) {
			player.sendMessage(C.red + "You do not have a Kingdom.");
			return;
		}

		offlineServer.setKingdomName(name);

		/* Modify online server */
		for (Server server : this.servers) {
			if (server.getKingdomID() == offlineServer.getId()) {
				server.setKingdomName(name);
				break;
			}
		}

		/* Update config */
		saveKingdomToConfig(offlineServer);
	}

	@EventHandler
	public void onKingdomShutdown(ServerShutdownEvent event) {
		this.servers.remove(event.getServer());
	}

	boolean isNewName(Player player, String name) {

		if (!(name.length() <= 11)) {
			player.sendMessage(C.red + "A kingdom name must be less than 12 characters in length.");
			return false;
		}

		for (OfflineServer offlineServer : this.offlineServers) {
			if (offlineServer.getKingdomName().equals(name)) {
				player.sendMessage(C.red + "That name is already in use!");
				return false;
			}
		}

		return true;
	}

	void loadConfig() {
		File mapConfigFile = new File(this.host.getDataFolder(), "servers.yml");

		if (!mapConfigFile.exists()) {
			try {
				mapConfigFile.createNewFile();
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Had to create new servers.yml!");
				return;
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create servers.yml!");
			}
		}

		FileConfiguration config = YamlConfiguration.loadConfiguration(mapConfigFile);
		this.offlineServers = new ArrayList<>();

		if(config.getConfigurationSection("kingdoms") != null) {
			// uuid, kingdom id
			HashMap<String, String> locationsInString = new HashMap<String, String>();

			for (String id : config.getConfigurationSection("kingdoms").getKeys(false)) {
				String name = config.getString("kingdoms." + id + ".name");
				System.out.println(name);
				String ownerUUID = config.getString("kingdoms." + id + ".ownerUUID");
				System.out.println("uuid: " + ownerUUID);
				int maxPlayers = config.getInt("kingdoms." + id + ".maxPlayers");
				int borderSize = config.getInt("kingdoms." + id + ".borderSize");
				int maxPlugins = config.getInt("kingdoms." + id + ".maxPlugins");

				OfflineServer offlineServer = new OfflineServer(Integer.parseInt(id), name, UUID.fromString(ownerUUID), maxPlayers, borderSize, maxPlugins);
				offlineServers.add(offlineServer);

				System.out.println("Successfully loaded offline-server: " + name);
			}
		}
	}

	void saveKingdomToConfig(OfflineServer server) {
		File mapConfigFile = new File(this.host.getDataFolder(), "servers.yml");

		if (!mapConfigFile.exists()) {
			try {
				mapConfigFile.createNewFile();
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Had to create new servers.yml!");
				return;
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create servers.yml!");
			}
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(mapConfigFile);
		int id = server.getId();

		if (config.getConfigurationSection("kingdoms." + id) != null) {
			F.debug("Creating new values in config");
			/* Doesn't exist, make new section */
			config.createSection("kingdoms." + id);
			config.getConfigurationSection("kingdoms." + id).set("name", server.getKingdomName());
			config.getConfigurationSection("kingdoms." + id).set("ownerUUID", server.getOwnerUUID().toString());
			config.getConfigurationSection("kingdoms." + id).set("maxPlayers", server.getMaxPlayers());
			config.getConfigurationSection("kingdoms." + id).set("borderSize", server.getBorderSize());
			config.getConfigurationSection("kingdoms." + id).set("maxPlugins", server.getMaxPlugins());

			try {
				config.save(mapConfigFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			F.debug("Changing existing values of config");
			/* Exists, modify it */
			config.getConfigurationSection("kingdoms." + id).set("name", server.getKingdomName());
			config.getConfigurationSection("kingdoms." + id).set("ownerUUID", server.getOwnerUUID().toString());
			config.getConfigurationSection("kingdoms." + id).set("maxPlayers", server.getMaxPlayers());
			config.getConfigurationSection("kingdoms." + id).set("borderSize", server.getBorderSize());
			config.getConfigurationSection("kingdoms." + id).set("maxPlugins", server.getMaxPlugins());
		}

	}

	public boolean maxRuntimeServers() {
		if (this.servers.size() >= 30) {
			return true;
		}
		return false;
	}

	public ArrayList<OfflineServer> getOfflineServers() {
		return offlineServers;
	}

	public ArrayList<Server> getServers() {
		return servers;
	}

	public KingdomHost getHost() {
		return host;
	}

	public Server getServer(String name) {
		for (Server server : this.servers) {
			if (server.getName().equals(name)) {
				return  server;
			}
		}
		return null;
	}

	public OfflineServer getServer(Player player) {
		for (OfflineServer offlineServer : this.offlineServers) {
			if (offlineServer.getOwnerUUID() == player.getUniqueId()) {
				return offlineServer;
			}
		}
		return null;
	}
}
