package org.example;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {
    private static File customConfigFile;
    private static FileConfiguration customConfig;

    public static Material[] crates = {
            Material.CHEST,
            Material.ENDER_CHEST,
            Material.SHULKER_BOX,
            Material.BARREL
    };

    @Override
    public void onEnable () {
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Example]: Plugin enabled!");

        ChestLocker locker = new ChestLocker();
        getServer().getPluginManager().registerEvents(locker, this);

        CommandKit commandKit = new CommandKit(locker);
        getCommand("kit").setExecutor(commandKit);
        getCommand("gettargeted").setExecutor(commandKit);
        getCommand("lock").setExecutor(commandKit);

        createCustomConfig();
        save();
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "lockedChests.yml");

        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("lockedChests.yml", false);
        }

        customConfig = new YamlConfiguration();

        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration getCustomConfig() {
        return customConfig;
    }

    public static boolean cratesContain (Material material) {
        for (int i = 0; i < crates.length; i++) {
            if (crates[i].equals(material)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDisable () {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Example]: Plugin disabled!");
    }
}
