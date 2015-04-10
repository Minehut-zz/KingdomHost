package com.minehut.kingdomhost;

import com.minehut.commons.common.bungee.Bungee;
import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.multimap.MultiMap;
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
public class KingdomServerManager implements Listener {
	KingdomHost host;

	//Player UUID, Kingdom ID, KingdomServerThread
	MultiMap<UUID, KingdomInfo, KingdomServerThread> kingdoms;
	ArrayList<UUID> topKingdoms = new ArrayList<>();

	//        Port,    ID

	HashMap<Integer, Integer> usedPorts;

	void registerPorts() {
		this.usedPorts = new HashMap<>();
		for (int i = 60001; i < 60500; i++) {
			this.usedPorts.put(i, 0);
		}
	}

	boolean roomForMoreKingdoms() {
		int hits = 0;

		for (int port : usedPorts.keySet()) {
			if (usedPorts.get(port) != 0) {
				hits += 1;
			}
		}
		if (hits <= 20) {
			return true;
		} else {
			return true;
		}
	}

	public KingdomServerManager (KingdomHost host) {
		this.host = host;
		this.loadConfig();
		calculateTopServers();
		Bukkit.getPluginManager().registerEvents(this, this.host);

		this.registerPorts();
	}

	int getOpenPort(int id) {
		int choosenPort = 0;
		for (int i = 60001; i< 60500; i++) {
			if(this.usedPorts.get(i) == 0) {
				int port = i;
				this.usedPorts.put(port, id);
				return port;
			}
		}
		return 0;
	}

	void connectToStartedKingdom(Player player, int id) {
		int port = 0;
		for (int i = 60001; i< 60500; i++) {
			if(this.usedPorts.get(i) == id) {
				port = i;
			}
		}
		Bungee.sendToServer(this.host, player, "kingdom" + Integer.toString(port - 60000));
	}

	int getPort(int id) {
		for (int port : this.usedPorts.keySet()) {
			if (this.usedPorts.get(port) == id) {
				return port;
			}
		}
		return 0;
	}

	void connectToStartedKingdomWithDelay(final Player player, final int id) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {
				int port = 0;
				for (int i = 60001; i< 60500; i++) {
					if(usedPorts.get(i) == id) {
						port = i;
					}
				}
				Bungee.sendToServer(host, player, "kingdom" + Integer.toString(port - 60000));
			}
		}, 10 * 20L);
	}

	void calculateTopServers() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this.host, new Runnable() {
			@Override
			public void run() {
//				HashMap<Integer, UUID> tempTop = new HashMap<Integer, UUID>();
				UUID first = null;
				UUID second = null;
				UUID third = null;
				UUID fourth = null;
				UUID fifth = null;


				for (UUID uuid : kingdoms.keySet()) {
					if (kingdoms.getSecondValue(uuid) != null && kingdoms.getSecondValue(uuid).isOnline()) {
						int num = kingdoms.getSecondValue(uuid).getCurrentPlayers();

						if (first == null || num > getPlayers(first)) first = uuid;
						else if (second == null || num > getPlayers(second)) second = uuid;
						else if (third == null || num > getPlayers(third)) third = uuid;
						else if (fourth == null || num > getPlayers(fourth)) fourth = uuid;
						else if (fifth == null || num > getPlayers(fifth)) fifth = uuid;
					}
				}

				topKingdoms.clear();
				topKingdoms.add(first);
				topKingdoms.add(second);
				topKingdoms.add(third);
				topKingdoms.add(fourth);
				topKingdoms.add(fifth);

//				if (first != null) {
//					topKingdoms.set(0, first);
//				}
//				if (second != null) {
//					topKingdoms.set(1, second);
//				}
//				if (third != null) {
//					topKingdoms.set(2, third);
//				}
//				if (fourth != null) {
//					topKingdoms.set(3, fourth);
//				}
//				if (fifth != null) {
//					topKingdoms.set(4, fifth);
//				}
			}
		}, 5 * 20, 3 * 20);
	}

	public void showTopKingdoms(Player player) {
		player.sendMessage(C.divider);
		player.sendMessage("");

		if (this.topKingdoms.get(0) != null) {
			player.sendMessage(C.gold + this.kingdoms.getFirstValue(topKingdoms.get(0)).getName() + C.green + " >> " + kingdoms.getSecondValue(topKingdoms.get(0)).getCurrentPlayers() + " Players Online");
		} else {
			player.sendMessage(C.gold + "EMPTY" + C.green + " >> 0 Players Online");
		}

		if (this.topKingdoms.get(1) != null) {
			player.sendMessage(C.gold + this.kingdoms.getFirstValue(topKingdoms.get(1)).getName() + C.green + " >> " + kingdoms.getSecondValue(topKingdoms.get(1)).getCurrentPlayers() + " Players Online");
		} else {
			player.sendMessage(C.gold + "EMPTY" + C.green + " >> 0 Players Online");
		}

		if (this.topKingdoms.get(2) != null) {
			player.sendMessage(C.gold + this.kingdoms.getFirstValue(topKingdoms.get(2)).getName() + C.green + " >> " + kingdoms.getSecondValue(topKingdoms.get(2)).getCurrentPlayers() + " Players Online");
		} else {
			player.sendMessage(C.gold + "EMPTY" + C.green + " >> 0 Players Online");
		}

		if (this.topKingdoms.get(3) != null) {
			player.sendMessage(C.gold + this.kingdoms.getFirstValue(topKingdoms.get(3)).getName() + C.green + " >> " + kingdoms.getSecondValue(topKingdoms.get(3)).getCurrentPlayers() + " Players Online");
		} else {
			player.sendMessage(C.gold + "EMPTY" + C.green + " >> 0 Players Online");
		}

		if (this.topKingdoms.get(4) != null) {
			player.sendMessage(C.gold + this.kingdoms.getFirstValue(topKingdoms.get(4)).getName() + C.green + " >> " + kingdoms.getSecondValue(topKingdoms.get(4)).getCurrentPlayers() + " Players Online");
		} else {
			player.sendMessage(C.gold + "EMPTY" + C.green + " >> 0 Players Online");
		}
	}

	int getPlayers(UUID uuid) {
		return this.kingdoms.getSecondValue(uuid).getCurrentPlayers();
	}

	public void createServer(final Player player, final String name) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {
				if (roomForMoreKingdoms()) {
					if (!kingdoms.containsKey(player.getUniqueId())) {
						player.sendMessage(C.gold + "Creating your new Kingdom");
						player.sendMessage(C.gold + "This may take a few moments...");

						int id = kingdoms.keySet().size() + 1;
						System.out.println("Copying Sample Kingdom...");
						copySampleServer(id);
						System.out.println("Finished copying Sample Kingdom");

//					int port = getOpenPort(id);

//					System.out.println("found open port: " + Integer.toString(port));

						System.out.println("Starting to edit server.properties...");
						editServerProperties(id);
						System.out.println("Finished editing server.properties");

						KingdomServerThread kingdom = new KingdomServerThread(player.getUniqueId(), id);
						kingdom.start(); //This could be called elsewhere to start the kingdom server using a command or something
						kingdoms.put(player.getUniqueId(), infoCreator(player.getUniqueId(), id, name), kingdom);

						saveKingdomToConfig(kingdom);
						connectToStartedKingdomWithDelay(player, id);
//					tpDelay(player, "kingdom" + Integer.toString(id));
					} else {
						player.sendMessage(C.red + "You already have a Kingdom. Please use " + C.gold + "/kingdom join");
						player.sendMessage(C.red + "To reset your Kingdom, use " + C.gold + "/kingdom reset [name]");
					}
				} else {
					player.sendMessage(C.mHead + "ERROR > " + C.red + "Our servers are currently too full to startup" +
							" a new Kingdom. Feel free to join an existing kingdom.");
					return;
				}
			}
			}, 1);
	}

	public void createResettedServer(final Player player, final String name, final int id) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {
				if (roomForMoreKingdoms()) {
					if (!kingdoms.containsKey(player.getUniqueId())) {
						player.sendMessage(C.gold + "Creating your new Kingdom");
						player.sendMessage(C.gold + "This may take a few moments...");

						System.out.println("Copying Sample Kingdom...");
						copySampleServer(id);
						System.out.println("Finished copying Sample Kingdom");

//					int port = getOpenPort(id);

//					System.out.println("found open port: " + Integer.toString(port));

						System.out.println("Starting to edit server.properties...");
						editServerProperties(id);
						System.out.println("Finished editing server.properties");

						KingdomServerThread kingdom = new KingdomServerThread(player.getUniqueId(), id);

						kingdom.start(); //This could be called elsewhere to start the kingdom server using a command or something
						kingdoms.put(player.getUniqueId(), infoCreator(player.getUniqueId(), id, name), kingdom);

						saveKingdomToConfig(kingdom, id);
						connectToStartedKingdomWithDelay(player, id);
//					tpDelay(player, "kingdom" + Integer.toString(id));
					} else {
						player.sendMessage(C.red + "You already have a Kingdom. Please use " + C.gold + "/kingdom join");
						player.sendMessage(C.red + "To reset your Kingdom, use " + C.gold + "/kingdom reset [name]");
					}
				} else {
					player.sendMessage(C.mHead + "ERROR > " + C.red + "Our servers are currently too full to startup" +
							" a new Kingdom. Feel free to join an existing kingdom.");
					return;
				}
			}
		}, 1);
	}

	@EventHandler
	public void onKingdomShutdown(KingdomShutdownEvent event) {
		int removePort = 0;
		for (int port : this.usedPorts.keySet()) {
			if (this.usedPorts.get(port) == event.getId()) {
				removePort = port;
				break;
			}
		}

		if (removePort != 0) {
			this.usedPorts.put(removePort, 0);
		}
	}

	public void joinServer(final Player player, final String name) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {
				for (UUID uuid : kingdoms.keySet()) {
					if (kingdoms.getFirstValue(uuid).getName().equals(name)) {
						//Checks if server is online
						if (kingdoms.getSecondValue(uuid) == null || !kingdoms.getSecondValue(uuid).isOnline()) {
							player.sendMessage(C.gold + "Finding your requested server...");
							startServer(player, uuid);
						} else {
							connectToStartedKingdom(player, kingdoms.getFirstValue(uuid).getId());
//							Bungee.sendToServer(utilServer.getPlugin(), player, "kingdom" + Integer.toString(kingdoms.getFirstValue(uuid).getId()));
						}
						return;
					}
				}
				player.sendMessage(C.yellow + name + C.red + " is not an existing Kingdom.");
			}
		}, 1);
	}

	public void startServer(final Player player, final UUID ownerUUID) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {
				if (kingdoms.containsKey(ownerUUID)) {
					int id = kingdoms.getFirstValue(ownerUUID).getId();

					//Checks if server is online
					if (kingdoms.getSecondValue(ownerUUID) == null || !kingdoms.getSecondValue(ownerUUID).isOnline()) {
						if(roomForMoreKingdoms()) {
							player.sendMessage(C.gold + "Starting up your requested kingdom");
							player.sendMessage(C.gold + "This may take a few moments...");

//						int port = getOpenPort(id);
							editServerProperties(id);
							KingdomServerThread kingdom = new KingdomServerThread(ownerUUID, id);

							kingdom.start(); //This could be called elsewhere to start the kingdom server using a command or something
							kingdoms.put(ownerUUID, kingdoms.getFirstValue(ownerUUID), kingdom);
							connectToStartedKingdomWithDelay(player, id);
//						tpDelay(player, "kingdom" + Integer.toString(id));
						} else {
							player.sendMessage(C.mHead + "ERROR > " + C.red + "Our servers are currently too full to startup" +
									" a new Kingdom. Feel free to join an existing kingdom.");
							return;
						}
					} else {
						connectToStartedKingdom(player, id);
//						Bungee.sendToServer(utilServer.getPlugin(), player, "kingdom" + Integer.toString(id));
					}

				} else {
					player.sendMessage(C.red + "That is not an existing Kingdom. Create one with /kingdom create [name]");
				}
			}
		}, 1);
	}

	public void startServer(final Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {
				if (kingdoms.containsKey(player.getUniqueId())) {
					int id = kingdoms.getFirstValue(player.getUniqueId()).getId();

					//Checks if server is online
					if (kingdoms.getSecondValue(player.getUniqueId()) == null || !kingdoms.getSecondValue(player.getUniqueId()).isOnline()) {
						if(roomForMoreKingdoms()) {
							player.sendMessage(C.gold + "Starting up your kingdom");
							player.sendMessage(C.gold + "This may take a few moments...");

//						int port = getOpenPort(id);
							editServerProperties(id);
							KingdomServerThread kingdom = new KingdomServerThread(player.getUniqueId(), id);

							kingdom.start(); //This could be called elsewhere to start the kingdom server using a command or something
							kingdoms.put(player.getUniqueId(), kingdoms.getFirstValue(player.getUniqueId()), kingdom);
							connectToStartedKingdomWithDelay(player, id);
//						tpDelay(player, "kingdom" + Integer.toString(id));
						} else {
							player.sendMessage(C.mHead + "ERROR > " + C.red + "Our servers are currently too full to startup" +
									" a new Kingdom. Feel free to join an existing kingdom.");
							return;
						}
					} else {
						connectToStartedKingdom(player, id);
//						Bungee.sendToServer(utilServer.getPlugin(), player, "kingdom" + Integer.toString(id));
					}
				} else {
					player.sendMessage(C.red + "You do not have an existing Kingdom. Create one with /kingdom create [name]");
				}
			}
		}, 1);
	}

	public void resetServer(final Player player, final String name) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.host, new Runnable() {
			@Override
			public void run() {
				if (kingdoms.containsKey(player.getUniqueId())) {
					if(roomForMoreKingdoms()) {
						if (kingdoms.getSecondValue(player.getUniqueId()) == null || !kingdoms.getSecondValue(player.getUniqueId()).isOnline()) {
							player.sendMessage(C.red + "Deleting your current Kingdom...");
							File file = new File("/home/kingdoms/kingdom" + Integer.toString(kingdoms.getFirstValue(player.getUniqueId()).getId()));
							try {
								FileUtils.deleteDirectory(file);
							} catch (IOException e) {
								e.printStackTrace();
							}
							int id = kingdoms.getFirstValue(player.getUniqueId()).getId();
							kingdoms.remove(player.getUniqueId());
							createResettedServer(player, name, id);
						} else {
							player.sendMessage(C.red + "Deleting your current Kingdom...");
							player.sendMessage(C.red + "This may take a few moments...");
							kingdoms.getSecondValue(player.getUniqueId()).runCommand("stop");

							Bukkit.getScheduler().scheduleSyncDelayedTask(host, new Runnable() {
								@Override
								public void run() {
									File file = new File("/home/kingdoms/kingdom" + Integer.toString(kingdoms.getFirstValue(player.getUniqueId()).getId()));
									try {
										FileUtils.deleteDirectory(file);
									} catch (IOException e) {
										e.printStackTrace();
									}
									int id = kingdoms.getFirstValue(player.getUniqueId()).getId();
									kingdoms.remove(player.getUniqueId());
									createResettedServer(player, name, id);
								}
							}, 6 * 20L);
						}
					} else {
						player.sendMessage(C.mHead + "ERROR > " + C.red + "Our servers are currently too full to startup" +
								" a new Kingdom. Feel free to join an existing kingdom.");
						return;
					}
				} else {
					player.sendMessage(C.red + "You do not have an existing Kingdom");
					player.sendMessage(C.red + "Create a Kingdom with " + C.gold + "/kingdom create [name]");
				}
			}
		}, 1);
	}

//	void deleteFromConfig(int id) {
//		File mapConfigFile = new File(this.utilServer.getPlugin().getDataFolder(), "kingdoms.yml");
//
//		if (!mapConfigFile.exists()) {
//			try {
//				mapConfigFile.createNewFile();
//				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Had to create new kingdoms.yml!");
//				return;
//			} catch (IOException e) {
//				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create kingdoms.yml!");
//			}
//		}
//		FileConfiguration config = YamlConfiguration.loadConfiguration(mapConfigFile);
//
//		config.set("kingdoms." + id, null);
//		try {
//			config.save(mapConfigFile);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	boolean isNewName(Player player, String name) {

		if (!(name.length() <= 10)) {
			player.sendMessage(C.red + "A kingdom name must be less than 10 characters in length.");
			return false;
		}

		for (UUID uuid : this.kingdoms.keySet()) {
			if (this.kingdoms.getFirstValue(uuid).getName().equals(name)) {
				player.sendMessage(C.red + "That name is already in use!");
				return false;
			}
		}
		return true;
	}

	void loadConfig() {
		File mapConfigFile = new File(this.host.getDataFolder(), "kingdoms.yml");

		if (!mapConfigFile.exists()) {
			try {
				mapConfigFile.createNewFile();
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Had to create new kingdoms.yml!");
				return;
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create kingdoms.yml!");
			}
		}

		FileConfiguration config = YamlConfiguration.loadConfiguration(mapConfigFile);
		this.kingdoms = new MultiMap<>();
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

				KingdomInfo kingdomInfo = infoCreator(UUID.fromString(ownerUUID), Integer.parseInt(id), name);
				kingdoms.put(UUID.fromString(ownerUUID), kingdomInfo, null);
				System.out.println("Successfully loaded kingdom: " + name);
			}

//			for (String uuid : config.getConfigurationSection("kingdoms").getKeys(false)) {
//				//Inputting null for KingdomServerThread. On create we will check if null and replace.
//				KingdomInfo kingdomInfo = infoCreator(uuid, Integer.parseInt(config.getString("kingdoms." + uuid)), )
//				kingdoms.put(UUID.fromString(uuid), Integer.parseInt(config.getString("kingdoms." + uuid)), null);
//				System.out.println("Successfully loaded kingdom config for owner: " + uuid);
//			}
		}
	}

	void saveKingdomToConfig(KingdomServerThread kingdom) {
		File mapConfigFile = new File(this.host.getDataFolder(), "kingdoms.yml");

		if (!mapConfigFile.exists()) {
			try {
				mapConfigFile.createNewFile();
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Had to create new kingdoms.yml!");
				return;
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create kingdoms.yml!");
			}
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(mapConfigFile);
		KingdomInfo kingdomInfo = this.kingdoms.getFirstValue(kingdom.getOwnerUUID());
		int id = kingdomInfo.getId();

		config.createSection("kingdoms." + id);
		config.getConfigurationSection("kingdoms." + id).set("name", kingdomInfo.getName());
		config.getConfigurationSection("kingdoms." + id).set("ownerUUID", kingdomInfo.getOwnerUUID().toString());
		config.getConfigurationSection("kingdoms." + id).set("maxPlayers", kingdomInfo.getMaxPlayers());
		config.getConfigurationSection("kingdoms." + id).set("borderSize", kingdomInfo.getBorderSize());
		config.getConfigurationSection("kingdoms." + id).set("maxPlugins", kingdomInfo.getMaxPlugins());

		try {
			config.save(mapConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void saveKingdomToConfig(KingdomServerThread kingdom, int id) {
		File mapConfigFile = new File(this.host.getDataFolder(), "kingdoms.yml");

		if (!mapConfigFile.exists()) {
			try {
				mapConfigFile.createNewFile();
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Had to create new kingdoms.yml!");
				return;
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create kingdoms.yml!");
			}
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(mapConfigFile);
		KingdomInfo kingdomInfo = this.kingdoms.getFirstValue(kingdom.getOwnerUUID());

		config.createSection("kingdoms." + id);
		config.getConfigurationSection("kingdoms." + id).set("name", kingdomInfo.getName());
		config.getConfigurationSection("kingdoms." + id).set("ownerUUID", kingdomInfo.getOwnerUUID().toString());
		config.getConfigurationSection("kingdoms." + id).set("maxPlayers", kingdomInfo.getMaxPlayers());
		config.getConfigurationSection("kingdoms." + id).set("borderSize", kingdomInfo.getBorderSize());
		config.getConfigurationSection("kingdoms." + id).set("maxPlugins", kingdomInfo.getMaxPlugins());

		try {
			config.save(mapConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void copySampleServer(int id) {
		try {
			File source = new File("/home/kingdoms/sampleKingdom");
			File target = new File("/home/kingdoms/kingdom" + Integer.toString(id));

			ArrayList<String> ignore = new ArrayList<String>(Arrays.asList("uid.dat", "session.dat"));
			if (!ignore.contains(source.getName())) {
				if (source.isDirectory()) {
					if (!target.exists())
						target.mkdirs();
					String files[] = source.list();
					for (String file : files) {
						File srcFile = new File(source, file);
						File destFile = new File(target, file);
						copyFile(srcFile, destFile);
					}
				} else {
					InputStream in = new FileInputStream(source);
					OutputStream out = new FileOutputStream(target);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = in.read(buffer)) > 0)
						out.write(buffer, 0, length);
					in.close();
					out.close();
				}
			}

		} catch (IOException e) {

		}
	}

	public void shutdownAllKingdoms() {
		for (UUID uuid : this.kingdoms.keySet()) {
			if (kingdoms.getSecondValue(uuid) != null) {
				kingdoms.getSecondValue(uuid).runCommand("stop");
			}
		}
	}

	private void editServerProperties(int id) {
		try {
			//Edit server.properties
			Properties props = new Properties();
			String propsFileName = "/home/kingdoms/kingdom" + Integer.toString(id) + "/server.properties";

			//first load old one:
			FileInputStream configStream = new FileInputStream(propsFileName);
			props.load(configStream);
			configStream.close();
			System.out.println("Detected old port: " + props.getProperty("server-port"));

			//modifies existing or adds new property
			String newPort = Integer.toString(getOpenPort(id));
			System.out.println("Setting new port to: " + newPort);
			props.setProperty("server-port", newPort);

			//save modified property file
			FileOutputStream output = new FileOutputStream(propsFileName);
			props.store(output, "This description goes to the header of a file");
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	void tpDelay(final Player player, final String server) {
//		Bukkit.getScheduler().scheduleSyncDelayedTask(utilServer.getPlugin(), new Runnable() {
//			@Override
//			public void run() {
//				if(player.isOnline()) {
//					player.sendMessage(C.green + "Sending you to your kingdom...");
//					Bungee.sendToServer(utilServer.getPlugin(), player, server);
//				}
//			}
//		}, 10 * 20L);
//	}

	private void copyFile(File source, File target) {
		try {
			ArrayList<String> ignore = new ArrayList<String>(Arrays.asList("uid.dat", "session.dat"));
			if (!ignore.contains(source.getName())) {
				if (source.isDirectory()) {
					if (!target.exists())
						target.mkdirs();
					String files[] = source.list();
					for (String file : files) {
						File srcFile = new File(source, file);
						File destFile = new File(target, file);
						copyFile(srcFile, destFile);
					}
				} else {
					InputStream in = new FileInputStream(source);
					OutputStream out = new FileOutputStream(target);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = in.read(buffer)) > 0)
						out.write(buffer, 0, length);
					in.close();
					out.close();
				}
			}
		} catch (IOException e) {

		}
	}

	KingdomInfo infoCreator(UUID uuid, int id, String name) {
		//TODO: Check player perms for server upgrades.

		return new KingdomInfo(uuid, id, name, 10, 500, 3);
	}
}
