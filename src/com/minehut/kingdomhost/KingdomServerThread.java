package com.minehut.kingdomhost;

import com.minehut.commons.common.uuid.NameFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class KingdomServerThread extends Thread {

	private UUID uuid;
	boolean online = false;
	private int id = -1;

	InputStream is;
	ProcessBuilder slave;
	Process theProcess;
	PrintWriter writer;

	public int currentPlayers = 0;
	public String currentMap = "NULL";
	public UUID ownerUUID;
	public long lastUpdated;

	public KingdomServerThread(UUID owner, int id) {
		this.uuid = UUID.randomUUID();
		this.id = id;
		this.ownerUUID = owner;
		this.online = true;
	}

	private UUID getUUID() {
		return this.uuid;
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

			if (Bukkit.getPlayer(getOwnerUUID()) != null) {

				String name = "";
				try {
					name = new NameFetcher(Arrays.asList(getOwnerUUID())).call().get(getOwnerUUID());
				} catch (Exception e) {
					e.printStackTrace();
				}
				runCommand("c-settings " + "setOwner " + name);
				runCommand("whitelist add " + name);
				runCommand("op " + name);
				runCommand("Enjoy!");

				Player player = Bukkit.getPlayer(getOwnerUUID());
			}

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
			Bukkit.getPluginManager().callEvent(new KingdomShutdownEvent(id));
			this.online = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	public int getKingdomID() {
		return this.id;
	}

	public int getCurrentPlayers() {
		return currentPlayers;
	}
}