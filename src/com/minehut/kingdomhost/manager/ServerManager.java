package com.minehut.kingdomhost.manager;

import com.minehut.api.API;
import com.minehut.api.util.player.Rank;
import com.minehut.commons.common.bungee.Bungee;
import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.chat.F;
import com.minehut.kingdomhost.KingdomHost;
import com.minehut.kingdomhost.events.ServerShutdownEvent;
import com.minehut.kingdomhost.server.Server;
import com.minehut.kingdomhost.offline.OfflineServer;
import com.minehut.kingdomhost.util.FileUtil;
import org.apache.commons.io.FileUtils;
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
	ArrayList<Integer> progressPorts;

	private static int defaultRam = 1024;

	public ServerManager(KingdomHost host) {
		this.servers = new ArrayList<>();
		this.offlineServers = new ArrayList<>();
		this.progressPorts = new ArrayList<>();

		this.host = host;
		this.loadConfig();
		Bukkit.getPluginManager().registerEvents(this, this.host);
	}

	boolean usedPort(int port) {
		for (Server server : this.servers) {
			if (server.getPort() == port) {
				return true;
			}
		}
		return false;
	}

	int getOpenPort() {

		for (int port = 60001; port < 60500; port++) {
			if (!usedPort(port) && !this.progressPorts.contains(port)) {
				this.progressPorts.add(port);
				return port;
			}
		}

		return 0;
	}

	public boolean connect(Player player, String name) {
		/* Try to join online server */
		for (Server server : this.servers) {
			if (server.getKingdomName().equals(name)) {
				if(server.isOnline()) {
					player.sendMessage("Sending you to " + C.aqua + name);
					Bungee.sendToServer(this.host, player, "kingdom" + Integer.toString(server.getPort() - 60000));
					return true;
				} else {
					player.sendMessage(C.aqua + server.getKingdomName() + C.white + " is currently starting up.");
					server.addStartupPlayer(player);
					return true;
				}
			}
		}

		/* Check if maxed out */
		if (maxRuntimeServers()) {
			player.sendMessage("");
			player.sendMessage(C.white + "Our servers are currently full. Please wait startup a server");
			player.sendMessage("");
			return true;
		}

		/* Server isn't online, try to start it */
		for (OfflineServer os : this.offlineServers) {
			if (os.getKingdomName().equals(name)) {
				int port = getOpenPort();
				FileUtil.editServerProperties(os.getId(), port);

				Server server = new Server(os.getOwnerUUID(), os.getId(), port, os.getKingdomName(), os.getMaxPlayers(), os.getBorderSize(), os.getMaxPlugins(), player.getUniqueId());
				this.servers.add(server);
				server.start();

				removeProgressPort(port);

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
					player.sendMessage(C.white + "Our servers are currently full. Please wait to create a new Server");
					player.sendMessage("");
					return;
				}

				/* Check for existing server */
				for (Server server : servers) {
					if (server.getOwnerUUID().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
						player.sendMessage("");
						player.sendMessage(C.white + "You already have a Kingdom. Please use " + C.aqua + "/join");
						player.sendMessage(C.white + "To reset your Kingdom, use " + C.aqua + "/reset");
						player.sendMessage("");
						return;
					}
				}

				/* Begin creation process */
				player.sendMessage("");
				player.sendMessage(C.white + "Creating your new Kingdom");
				player.sendMessage("This may take " + C.aqua + "several moments" + C.white + " to complete.");
				player.sendMessage("");

				int id = offlineServers.size() + 1;
				int port = getOpenPort();

				/* todo: rank-based perks */
				OfflineServer offlineServer = new OfflineServer(id, name, player.getUniqueId(), 10, 500, 5);

				/* Copy files */
				FileUtil.copySampleServer(id);
				FileUtil.editServerProperties(id, port);

				/* Add to offline list */
				offlineServers.add(offlineServer);

				/* Start up server */
				Server server = new Server(player.getUniqueId(), id, port, name, offlineServer.getMaxPlayers(), offlineServer.getBorderSize(), offlineServer.getMaxPlugins(), player.getUniqueId());
				servers.add(server);
				server.start();

				removeProgressPort(port);

				/* Save to config */
				saveKingdomToConfig(offlineServer);

				/* Connect after server has started (delay) */
				connect(player, name, 20);
			}
		});
	}

	public void resetServer(final Player player) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {

				/* Check if maxed out */
				if (maxRuntimeServers()) {
					player.sendMessage(C.red + "Our servers are currently full. Please wait to create a new Server");
					return;
				}

				/* Shutdown server */
				for (Server server : servers) {
					if (server.getOwnerUUID().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
						server.forceShutdown();
						break;
					}
				}

				/* Use data from offline-server */
				OfflineServer offlineServer = null;
				for (OfflineServer os : offlineServers) {
					if (os.getOwnerUUID().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
						offlineServer = os;
					}
				}

				/* Check to see if server existed */
				if (offlineServer == null) {
					player.sendMessage("");
					player.sendMessage("You do not have an existing server.");
					player.sendMessage("Create one with " + C.aqua + "/create (name)");
					player.sendMessage("");
					return;
				}

				/* Begin creation process */
				player.sendMessage("");
				player.sendMessage("Creating your new Kingdom.");
				player.sendMessage("This may take " + C.aqua + "several moments" + C.white + " to complete.");
				player.sendMessage("");

				/* Size is greater than index */
				int id = offlineServer.getId();
				int port = getOpenPort();

				/* Delete */
				try {
					FileUtils.deleteDirectory(new File("/home/kingdoms/kingdom" + offlineServer.getId()));
				} catch (IOException e) {
					e.printStackTrace();
				}

				/* Copy files */
				FileUtil.copySampleServer(id);
				FileUtil.editServerProperties(id, port);

				/* Start up server */
				Server server = new Server(player.getUniqueId(), id, port, offlineServer.getKingdomName(), offlineServer.getMaxPlayers(), offlineServer.getBorderSize(), offlineServer.getMaxPlugins(), player.getUniqueId());
				servers.add(server);
				server.start();

				removeProgressPort(port);

				/* Connect after server has started (delay) */
				connect(player, offlineServer.getKingdomName(), 20);

			}
		}, 1);
	}

	public void removeProgressPort(int port) {
		for (int i = 0; i < this.progressPorts.size(); i++) {
			int markedIndex = -1;
			if (this.progressPorts.get(i) == port) {
				markedIndex = i;
			}

			if (markedIndex != -1) {
				this.progressPorts.remove(markedIndex);
				return;
			}
		}
	}

	public void changeName(Player player, String name) {
		OfflineServer offlineServer = null;
		for (OfflineServer os : this.offlineServers) {
			if (os.getOwnerUUID().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
				offlineServer = os;
				break;
			}
		}

		if (offlineServer == null) {
			player.sendMessage(C.red + "You do not have a Kingdom.");
			return;
		}

		/* Check if name is available */
		if (!isNewName(player, name)) {
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
		updateConfig(offlineServer);

		/* Notify Player */
		player.sendMessage("");
		player.sendMessage("You have changed your server name to " + C.aqua + name);
		player.sendMessage("");
	}

	@EventHandler
	public void onKingdomShutdown(ServerShutdownEvent event) {
		event.getServer().forceShutdown();
		this.servers.remove(event.getServer());
	}

	boolean isNewName(Player player, String name) {

		if (!(name.length() <= 11)) {
			player.sendMessage(C.red + "A server name must be less than 12 characters in length.");
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

				/* New stuff */
				int ram = config.getInt("kingdoms." + id + ".ram");
				if(ram == 0) {
					/* Did not contain */
					config.getConfigurationSection("kingdoms." + id).set("ram", defaultRam);
					ram = defaultRam;
				}

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

	}

	void updateConfig(OfflineServer server) {
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

		/* Exists, no need to create new section */

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
	}

	public boolean maxRuntimeServers() {
		if (this.servers.size() >= 50) {
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

	public ArrayList<OfflineServer> getServer(Player player) {
		ArrayList<OfflineServer> ownedServers = new ArrayList<>();

		for (OfflineServer offlineServer : this.offlineServers) {
			if (offlineServer.getOwnerUUID().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
				ownedServers.add(offlineServer);
			}
		}
		return ownedServers;
	}



	/* Multi server reset */
	public void resetServer(final Player player, String name) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {

				/* Check if maxed out */
				if (maxRuntimeServers()) {
					player.sendMessage(C.red + "Our servers are currently full. Please wait to create a new Server");
					return;
				}

				/* Shutdown server */
				for (Server server : servers) {
					if (server.getKingdomName().equalsIgnoreCase(name)) {
						server.forceShutdown();
						break;
					}
				}

				/* Use data from offline-server */
				OfflineServer offlineServer = null;
				for (OfflineServer os : offlineServers) {
					if (os.getKingdomName().equalsIgnoreCase(name)) {
						offlineServer = os;
					}
				}

				/* Check to see if server existed */
				if (offlineServer == null) {
					player.sendMessage("");
					player.sendMessage("You do not have an existing server.");
					player.sendMessage("Create one with " + C.aqua + "/create (name)");
					player.sendMessage("");
					return;
				}

				/* Begin creation process */
				player.sendMessage("");
				player.sendMessage("Creating your new Kingdom.");
				player.sendMessage("This may take " + C.aqua + "several moments" + C.white + " to complete.");
				player.sendMessage("");

				/* Size is greater than index */
				int id = offlineServer.getId();
				int port = getOpenPort();

				/* Delete */
				try {
					FileUtils.deleteDirectory(new File("/home/kingdoms/kingdom" + offlineServer.getId()));
				} catch (IOException e) {
					e.printStackTrace();
				}

				/* Copy files */
				FileUtil.copySampleServer(id);
				FileUtil.editServerProperties(id, port);

				/* Start up server */
				Server server = new Server(player.getUniqueId(), id, port, offlineServer.getKingdomName(), offlineServer.getMaxPlayers(), offlineServer.getBorderSize(), offlineServer.getMaxPlugins(), player.getUniqueId());
				servers.add(server);
				server.start();

				removeProgressPort(port);

				/* Connect after server has started (delay) */
				connect(player, offlineServer.getKingdomName(), 20);

			}
		}, 1);
	}

	public void changeName(Player player, String serverName, String name) {
		OfflineServer offlineServer = null;
		for (OfflineServer os : this.offlineServers) {
			if (os.getKingdomName().equals(serverName)) {
				offlineServer = os;
				break;
			}
		}

		if (offlineServer == null) {
			player.sendMessage(C.red + "You do not have a server named " + C.aqua + serverName);
			return;
		}

		/* Check if name is available */
		if (!isNewName(player, name)) {
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
		updateConfig(offlineServer);

		/* Notify Player */
		player.sendMessage("");
		player.sendMessage("You have changed your server name to " + C.aqua + name);
		player.sendMessage("");
	}
}
