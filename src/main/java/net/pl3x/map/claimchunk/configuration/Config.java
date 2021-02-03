package net.pl3x.map.claimchunk.configuration;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

public class Config {
    public static String CONTROL_LABEL = "ClaimChunk";
    public static boolean CONTROL_SHOW = true;
    public static boolean CONTROL_HIDE = false;
    public static int UPDATE_INTERVAL = 300;
    public static Color STROKE_COLOR = Color.RED;
    public static int STROKE_WEIGHT = 2;
    public static double STROKE_OPACITY = 0.75D;
    public static Color FILL_COLOR = Color.RED;
    public static double FILL_OPACITY = 0.2D;
    public static String CLAIM_TOOLTIP = "{owner}";
    public static boolean SHOW_CHUNKS = true;

    private static void init() {
        CONTROL_LABEL = getString("settings.control.label", CONTROL_LABEL);
        CONTROL_SHOW = getBoolean("settings.control.show", CONTROL_SHOW);
        CONTROL_HIDE = getBoolean("settings.control.hide-by-default", CONTROL_HIDE);
        UPDATE_INTERVAL = getInt("settings.update-interval", UPDATE_INTERVAL);
        STROKE_COLOR = getColor("settings.style.stroke.color", STROKE_COLOR);
        STROKE_WEIGHT = getInt("settings.style.stroke.weight", STROKE_WEIGHT);
        STROKE_OPACITY = getDouble("settings.style.stroke.opacity", STROKE_OPACITY);
        FILL_COLOR = getColor("settings.style.fill.color", FILL_COLOR);
        FILL_OPACITY = getDouble("settings.style.fill.opacity", FILL_OPACITY);
        CLAIM_TOOLTIP = getString("settings.claim.tooltip", CLAIM_TOOLTIP);
        SHOW_CHUNKS = getBoolean("settings.claim.show-chunks", SHOW_CHUNKS);
    }

    public static void reload(Plugin plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        yaml = new YamlConfiguration();

        try {
            yaml.load(file);
        } catch (IOException ignore) {
        } catch (InvalidConfigurationException ex) {
            plugin.getLogger().severe("Could not load config.yml, please correct your syntax errors");
            throw new RuntimeException(ex);
        }

        yaml.options().copyDefaults(true);

        init();

        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save " + file);
            ex.printStackTrace();
        }
    }

    private static YamlConfiguration yaml;

    private static String getString(String path, String def) {
        yaml.addDefault(path, def);
        return yaml.getString(path, yaml.getString(path));
    }

    private static boolean getBoolean(String path, boolean def) {
        yaml.addDefault(path, def);
        return yaml.getBoolean(path, yaml.getBoolean(path));
    }

    private static int getInt(String path, int def) {
        yaml.addDefault(path, def);
        return yaml.getInt(path, yaml.getInt(path));
    }

    private static double getDouble(String path, double def) {
        yaml.addDefault(path, def);
        return yaml.getDouble(path, yaml.getDouble(path));
    }

    private static Color getColor(String path, Color def) {
        yaml.addDefault(path, Integer.toHexString(def.getRGB()));
        String hex = (yaml.getString(path, yaml.getString(path)));
        return new Color((int) Long.parseLong(hex.replace("#", ""), 16));
    }
}