package com.minehut.kingdomhost.util;

import com.minehut.commons.common.chat.F;
import com.minehut.core.player.Rank;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by luke on 4/11/15.
 */
public class FileUtil {

    public static void editServerProperties(int id, int port) {
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
            String newPort = Integer.toString(port);
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

    public static void allocatePerks(int id, String kingdomName, Rank rank) {
        try {
            //Edit server.properties
            Properties props = new Properties();
            String propsFileName = "/home/kingdoms/kingdom" + Integer.toString(id) + "/server.properties";

            /* Player Slots */
            int playerSlots = getPlayerSlots(rank);
            F.log(kingdomName, "Allocated Player Slots: " + Integer.toString(playerSlots));

			/* World Border */
            int worldBorder = getWorldBorder(rank);
            F.log(kingdomName, "Allocated World Border: " + Integer.toString(worldBorder));

            //first load old port:
            FileInputStream configStream = new FileInputStream(propsFileName);
            props.load(configStream);
            configStream.close();

            //modifies existing or adds new property
            props.setProperty("max-players", Integer.toString(playerSlots));
            props.setProperty("max-world-size", Integer.toString(worldBorder));

            //save modified property file
            FileOutputStream output = new FileOutputStream(propsFileName);
            props.store(output, "This description goes to the header of a file");
            output.close();

            /* Success */
            F.log(kingdomName, "Successfully allocated perks");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMOTD(int id, String kingdomName, Rank rank) {
        try {

            /* Ignore for non-donors */
            if (rank != null && rank != Rank.regular) {
                //Edit server.properties
                Properties props = new Properties();
                String propsFileName = "/home/kingdoms/kingdom" + Integer.toString(id) + "/server.properties";

                //first load old one:
                FileInputStream configStream = new FileInputStream(propsFileName);
                props.load(configStream);
                configStream.close();

                /* Select MOTD */
                String motd = props.getProperty("motd");
                F.log(kingdomName, "Detected MOTD: " + motd);

                return motd;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void copyFile(File source, File target) {
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

    public static void copySampleServer(int id) {

        try {
            File source = new File("/home/kingdoms/sampleKingdom");
            File target = new File("/home/kingdoms/kingdom" + Integer.toString(id));

            FileUtils.deleteDirectory(target);

//            ArrayList<String> ignore = new ArrayList<String>(Arrays.asList("uid.dat", "session.dat"));
//            if (!ignore.contains(source.getName())) {
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
//            }

        } catch (IOException e) {

        }
    }

    public static void checkAndExecuteActions(String folder) {

        /* Reset World */
        File resetAction = new File(folder + "actions/resetmap.action");
        if (resetAction.exists()) {
            File world = new File(folder + "world");
            try {
                FileUtils.deleteDirectory(world);
                resetAction.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            F.log("##############################");
            F.log("Deleted World in " + folder);
            F.log("##############################");
        }
    }

    private static int getPlayerSlots(Rank rank) {
        if (rank.has(null, Rank.Champ, false)) {
            return 40;
        } else if (rank.has(null, Rank.Legend, false)) {
            return 35;
        } else if (rank.has(null, Rank.Super, false)) {
            return 28;
        } else if (rank.has(null, Rank.Mega, false)) {
            return 20;
        } else {
            return 10;
        }
    }

    private static int getWorldBorder(Rank rank) {
        if (rank.has(null, Rank.Champ, false)) {
            return 4000;
        } else if (rank.has(null, Rank.Legend, false)) {
            return 3000;
        } else if (rank.has(null, Rank.Super, false)) {
            return 2000;
        } else if (rank.has(null, Rank.Mega, false)) {
            return 1000;
        } else {
            return 500;
        }
    }
}
