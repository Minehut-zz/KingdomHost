package com.minehut.kingdomhost.server;

import com.minehut.commons.common.bungee.Bungee;
import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.chat.F;
import com.minehut.commons.common.uuid.NameFetcher;
import com.minehut.core.Core;
import com.minehut.core.player.Rank;
import com.minehut.kingdomhost.KingdomHost;
import com.minehut.kingdomhost.events.ServerShutdownEvent;
import com.minehut.kingdomhost.offline.OfflineServer;
import com.minehut.kingdomhost.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@SuppressWarnings("ALL")
public class Server extends Thread {

	private int runnableID;
	boolean online = false;

	private InputStream is;
	private ProcessBuilder slave;
	private Process theProcess;
	private PrintWriter writer;

	public int currentPlayers = 0;
	public long lastUpdated;

	//Info
	private int kingdomID;
	private String kingdomName;
	private int maxPlayers;
	private int borderSize;
	private int maxPlugins;
	private UUID ownerUUID;
	private String ownerName;
	private int port;
	private int pid;
	private UUID startupPlayer;
	private String motd;

	private ArrayList<UUID> startupPlayers;

	public Server(UUID owner, int kingdomID, int port, String kingdomName, int maxPlayers, int borderSize, int maxPlugins, UUID startupPlayer) {
		this.kingdomID = kingdomID;
		this.kingdomName = kingdomName;
		this.port = port;
		this.maxPlayers = maxPlayers;
		this.borderSize = borderSize;
		this.maxPlugins = maxPlugins;
		this.ownerUUID = owner;
		this.online = false;

		this.runnableID = 0;

		this.startupPlayers = new ArrayList<>();
		this.startupPlayers.add(startupPlayer);

		runnableID = this.monitorOnlineStatus();
	}

	public void runCommand(String cmd) {
		try {
			this.writer.println(cmd);
			this.writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if (kingdomID ==-1)
			return;
		try {
			try {
				this.ownerName = new NameFetcher(Arrays.asList(this.ownerUUID)).call().get(this.ownerUUID);
			} catch (Exception e) {
				F.log("error getting player name");
			}

			/* Setup KingdomClient config */
			this.setupClientConfig();

			System.out.println("Starting server..");

			/* Retrieve Rank */
			Rank rank = Core.getInstance().getRank(ownerUUID);

			/* MOTD */
			this.motd = FileUtil.getMOTD(kingdomID, kingdomName, rank);

			/* Ram */
			int ram = getRam(rank);
			F.log(this.kingdomName, "Allocated Ram: " + Integer.toString(ram));

			/* Allocate Perks */
			FileUtil.allocatePerks(kingdomID, kingdomName, rank);

			/* Start Process */
			final File executorDirectory = new File("/home/kingdoms/kingdom" + Integer.toString(this.kingdomID) + "/");
			this.theProcess = Runtime.getRuntime().exec("java -XX:MaxPermSize=128M -Xmx" + Integer.toString(ram) + "M -Xms" + Integer.toString(ram) + "M -jar spigot.jar", null, executorDirectory);
			this.pid = getPid(this.theProcess);

			this.writer = new PrintWriter(new OutputStreamWriter(this.theProcess.getOutputStream()));
			is = this.theProcess.getInputStream();
			System.out.println("Server started, getting output");

			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("kingdom_")) {
					String[] slaveCommandParser = line.split("_")[1].split(":");

					if (slaveCommandParser[0].equalsIgnoreCase("PlayersOnline")) {
						this.currentPlayers = Integer.parseInt(slaveCommandParser[1]);
						this.lastUpdated = System.currentTimeMillis();

						if (!this.online) {
							this.online = true;

							if (this.isNoStartupPlayers()) {
								this.callShutdownEvent();
							} else {
								for (UUID uuid : this.startupPlayers) {
									Player player = Bukkit.getServer().getPlayer(uuid);
									if (player != null) {
										player.sendMessage("sending you to " + C.aqua + this.kingdomName);
										Bungee.sendToServer(KingdomHost.getPlugin(), player, "kingdom" + Integer.toString(this.port - 60000));
									}
								}
							}
						}

					}
				}

				else if (line.contains("Preparing spawn area: ")) {
					String[] slaveCommandParser = line.split("Preparing spawn area: ");

					for(UUID uuid : this.startupPlayers) {
						Player player = Bukkit.getPlayer(uuid);
						if (player != null) {
							player.sendMessage("Kingdom Startup: " + C.aqua + slaveCommandParser[1]);
						}
					}

				}

				else if (line.toLowerCase().contains("failed to bind")) {
					for(UUID uuid : this.startupPlayers) {
						Player player = Bukkit.getPlayer(uuid);
						if (player != null) {
							player.sendMessage("The error " + C.red + "Port Bind" + C.white + " has occured. Please try again.");
							player.sendMessage("Politely alert a staff member that port " + C.aqua + Integer.toString(this.port) + C.white
								+ " is " + C.red + "faulty" + C.white + ".");
						}
					}

					F.log("------------------------------");
					F.log("- FAILED TO PIND TO PORT -");
					F.log("Port: " + Integer.toString(this.port));
					F.log("Kingdom: " + this.kingdomName);
					F.log("PID: " + Integer.toString(this.pid));
					F.log("------------------------------");
				}
//				System.out.println("[" + this.kingdomName + "]: " + line + "\n"); //prints out server input
			}
			System.out.println("server closed, thread finished");
			this.callShutdownEvent();
			this.online = false;
		} catch (IOException e) {
			e.printStackTrace();
			this.callShutdownEvent();
		}
	}

	public boolean isNoStartupPlayers() {
		int i = 0;
		for (UUID uuid : this.startupPlayers) {
			if (Bukkit.getServer().getPlayer(uuid) == null) {
				i++;
			}
		}

		if (i >= this.startupPlayers.size()) {
			return true;
		}
		return false;
	}

	public boolean isOnline() {
		return online;
	}

	private void setupClientConfig() {
		File mapConfigFile = new File("/home/kingdoms/kingdom" + Integer.toString(this.kingdomID) + "/plugins/KingdomClient/", "config.yml");

		if (!mapConfigFile.exists()) {
			try {
				mapConfigFile.createNewFile();
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Had to create new client config.yml!");
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create client config.yml!");
			}
		}

		FileConfiguration config = YamlConfiguration.loadConfiguration(mapConfigFile);
		config.set("owner-name", this.ownerName);
		config.set("owner-uuid", this.ownerUUID.toString());
		config.set("kingdom-id", this.kingdomID);
		config.set("kingdom-name", this.kingdomName);

		try {
			config.save(mapConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void callShutdownEvent() {
		Bukkit.getPluginManager().callEvent(new ServerShutdownEvent(this));
	}

	private int monitorOnlineStatus() {
		return Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(KingdomHost.getPlugin(), new Runnable() {
			@Override
			public void run() {
				long difference = (System.currentTimeMillis() - lastUpdated) / 1000;
				if (difference >= 10) {
					callShutdownEvent();
					Bukkit.getServer().getScheduler().cancelTask(runnableID);
				}

			}
		}, 180 * 60, 20 * 10);
	}

	public void forceShutdown() {
		try {

			F.log("------------------------------");
			F.log("- FORCE SHUTTING DOWN KINGDOM -");
			F.log("PID: " + Integer.toString(this.pid));
			F.log("Kingdom: " + this.kingdomName);
			F.log("------------------------------");

			Runtime.getRuntime().exec("kill -SIGKILL " + Integer.toString(this.pid));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	public int getCurrentPlayers() {
		return currentPlayers;
	}

	public int getKingdomID() {
		return kingdomID;
	}

	public String getKingdomName() {
		return kingdomName;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getBorderSize() {
		return borderSize;
	}

	public int getMaxPlugins() {
		return maxPlugins;
	}

	public int getPort() {
		return port;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setKingdomName(String kingdomName) {
		this.kingdomName = kingdomName;
	}

	public int getPid(Process process) {
		try {
			Class<?> ProcessImpl = process.getClass();
			Field field = ProcessImpl.getDeclaredField("pid");
			field.setAccessible(true);
			int pid = field.getInt(process);

			F.log("HACKED PID: " + Integer.toString(pid));

			return field.getInt(process);
		} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
			F.log("HACKED PID: -1 (something went wrong)");
			return -1;
		}
	}

	public void addStartupPlayer(Player player) {
		if(!this.startupPlayers.contains(player.getUniqueId())) {
			this.startupPlayers.add(player.getUniqueId());
		}
	}

	public int getPid() {
		return pid;
	}

	private static int getRam(Rank rank) {
		if (rank.has(null, Rank.Champ, false)) {
			return 3072; // 3.0 gb
		} else if (rank.has(null, Rank.Legend, false)) {
			return 2048; // 2.0 gb
		} else if (rank.has(null, Rank.Super, false)) {
			return 1792; // 1.75 gb
		} else if (rank.has(null, Rank.Mega, false)) {
			return 1024; // 1.0 gb
		} else {
			return 768; // 0.75 gb
		}
	}

	public String getMotd() {
		return motd;
	}
}
