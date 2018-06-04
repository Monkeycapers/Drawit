package Jesty;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Evan on 11/3/2016.
 *
 * Loads and saves application settings. Format:
 *
 * [property name]:[property value]
 *
 * Used for the client and server
 */
public class Settings {

    private static HashMap<String, String> propertys;

    private static File settingsfile = new File(Settings.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "\\settings.txt");
    //Set the settings file to the file, if the file does not exist save defaults
    public static void setFile(File file, HashMap<String, String> defaults) {
        settingsfile = file;
        try {
            if (file.createNewFile()) {
                propertys = defaults;
                save();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Load the settings from the settings file into the propertys HashMap
    public static void load() {
        propertys = new HashMap<String, String>();
        try {
            //The file should exist, but does not
            if (settingsfile.createNewFile()) {
                System.err.println("File should already be created");
            }
            else {
                //Read each line in and fill propertys
                BufferedReader in = new BufferedReader(new FileReader(settingsfile));
                String line = "";
                line = in.readLine();
                while (line != null) {
                    String[] split = line.split(":");
                    propertys.put(split[0], split[1]);
                    line = in.readLine();
                }
                in.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Save all of the propertys into the file
    public static void save() {
        try {
            PrintWriter out = new PrintWriter((new PrintWriter(settingsfile)));
            for (Map.Entry<String, String> entry: propertys.entrySet()) {
                out.println(entry.getKey() + ":" + entry.getValue());
            }
            out.close();
        }
        catch (Exception e) {

        }
    }
    //Get the property by name
    public static String getProperty(String name) {
        return propertys.get(name);
    }
    //Get a integer property
    public static int getIntProperty(String name) {
        try {
            return Integer.parseInt(propertys.get(name));
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    //check if there is the property
    public static boolean hasProperty(String name) {
        return propertys.containsKey(name);
    }
    //change a property and save the file
    public static void setProperty(String name, String value) {
        propertys.replace(name, value);
        save();
    }
    //get the property list
    public static HashMap<String, String> getPropertys () {
        return propertys;
    }
    //List the propertys
    public static String listPropertys() {
        String toSend = "Propertys: " + "\n";
        for (Map.Entry<String, String> entry:getPropertys().entrySet()) {
            toSend += entry.getKey() + ":" + entry.getValue() + "\n";
        }
        return toSend;
    }
}
