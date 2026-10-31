package com.veinminer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.veinminer.VeinMiner;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VeinMinerConfigData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("vein-miner.json");

    public static VeinMinerConfigData INSTANCE = new VeinMinerConfigData();

    // Master switch
    public boolean enableVeinMiner = true;

    // Pickaxes
    public boolean woodenPickaxe = true;
    public boolean stonePickaxe = true;
    public boolean ironPickaxe = true;
    public boolean goldenPickaxe = true;
    public boolean diamondPickaxe = true;
    public boolean netheritePickaxe = true;

    // Blocks
    public boolean ores = true;
    public boolean magmaBlock = true;

    // Custom Blocks
    public List<String> customBlocks = new ArrayList<>();

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                VeinMinerConfigData data = GSON.fromJson(reader, VeinMinerConfigData.class);
                if (data != null) {
                    if (data.customBlocks == null) {
                        data.customBlocks = new ArrayList<>();
                    }
                    INSTANCE = data;
                    VeinMiner.LOGGER.info("Vein Miner configuration loaded successfully.");
                    return;
                }
            } catch (Exception e) {
                VeinMiner.LOGGER.error("Failed to load Vein Miner configuration, using defaults.", e);
            }
        }
        INSTANCE = new VeinMinerConfigData();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
            VeinMiner.LOGGER.info("Vein Miner configuration saved successfully.");
        } catch (IOException e) {
            VeinMiner.LOGGER.error("Failed to save Vein Miner configuration.", e);
        }
    }
}
