package com.minehut.kingdomhost.server;

import com.minehut.commons.common.uuid.NameFetcher;
import com.minehut.kingdomhost.KingdomHost;
import com.minehut.kingdomhost.events.ServerShutdownEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Server extends Thread {

	private int runnableID;
	boolean online = false;

	InputStream is;
	ProcessBuilder slave;
	Process theProcess;
	PrintWriter writer;

	public int currentPlayers = 0;
	public String currentMap = "NULL";
	public long lastUpdated;

	//Info
	int id;
	String kingdomName;
	int maxPlayers;
	int borderSize;
	int maxPlugins;
	UUID ownerUUID;
	String ownerName;
	int port;

	public Server(UUID owner, int id, int port, String kingdomName, int maxPlayers, int borderSize, int maxPlugins) {
		this.id = id;
		this.ownerUUID = owner;
		this.online = true;
		this.runnableID = this.monitorOnlineStatus();
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
		if (id==-1)
			return;
		try {
			System.out.println("Starting server..");
			this.online = true;

			final File executorDirectory = new File("/home/kingdoms/kingdom" + Integer.toString(this.id) + "/");

			final List<String> commands = new ArrayList<String>();
			commands.add("cmd -c chmod 775 start.sh");
			commands.add("./start.sh");

			ProcessBuilder chmod = new ProcessBuilder("chmod", "775", "start.sh");
			chmod.directory(executorDirectory);
			Process chmodProcess = chmod.start();
			chmod.start();

			this.slave = new ProcessBuilder("./start.sh");
			this.slave.directory(executorDirectory);

			this.theProcess = this.slave.start();

			this.writer = new PrintWriter(new OutputStreamWriter(this.theProcess.getOutputStream()));
			is = this.theProcess.getInputStream();

			System.out.println("Server started, getting output");

			try {
				this.ownerName = new NameFetcher(Arrays.asList(this.ownerUUID)).call().get(this.ownerUUID);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/* Host Commands */
			runCommand("set_id " + Integer.toString(this.id));
			runCommand("set_owner " + this.ownerUUID.toString());
			runCommand("op " + this.ownerName);
			runCommand("whitelist add " + this.ownerName);

			BufferedReader br = new BufferedReader( new InputStreamReader(is));
			String line = "";

			while ((line = br.readLine()) != null) {

				if (line.contains("kingdom_")) {
					String[] slaveCommandParser = line.split("_")[1].split(":");

					if (slaveCommandParser[0].equalsIgnoreCase("PlayersOnline")) {
						this.currentPlayers = Integer.parseInt(slaveCommandParser[1]);
						this.lastUpdated = System.currentTimeMillis();
					}
					else if (slaveCommandParser[0].equalsIgnoreCase("currentmap")) {
						this.currentMap = slaveCommandParser[1];
						this.lastUpdated = System.currentTimeMillis();
					}
				}
//				System.out.println(line); //prints out server input
			}
			System.out.println("server closed, thread finished");
			this.theProcess.destroy();
			callShutdownEvent();
			this.online = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isOnline() {
		return online;
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
					if (theProcess.isAlive()) {
						theProcess.destroy();
						callShutdownEvent();
						Bukkit.getServer().getScheduler().cancelTask(runnableID);
					}
				}
			}
		}, 20, 20 * 10);
	}

	public void forceShutdown() {
		this.theProcess.destroy();
		callShutdownEvent();
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
		return id;
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
}