package net.nukebob.chameleon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.MCChameleon;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GameConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(MCChameleon.MOD_ID + "/game_config.json").toFile();
    private static GameConfig config;

    public boolean isWhistleSound = true;
    public int whistleFrequency = 60;
    public int hideTime = 60;
    public int seekTime = 300;
    public int answerCheckTime = 30;
    public float hunterPercent = 0.2f;

    public Identifier mapLevel;
    public Vec3 mapSpawn;
    public Vec2 mapSpawnRotation;

    public static synchronized GameConfig loadConfig() {
        if (config!=null) return config;

        if (!CONFIG_FILE.exists()) {
            config = new GameConfig();
            saveConfig();
        } else {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, GameConfig.class);
            } catch (IOException e) {
                MCChameleon.LOGGER.error("Could not load game config file", e);
            }
        }
        if (config == null) {
            config = new GameConfig();
            saveConfig();
        }
        return config;
    }

    public static synchronized void saveConfig() {
        if (config == null) {
            config = new GameConfig();
        }

        File parent = CONFIG_FILE.getParentFile();
        if (parent!=null && !parent.exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            MCChameleon.LOGGER.error("Could not save config file", e);
        }
    }
}
