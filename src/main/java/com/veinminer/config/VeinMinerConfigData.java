package com.veinminer.config;

import com.google.gson.*;
import com.google.gson.internal.bind.MapTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.veinminer.VeinMiner;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VeinMinerConfigData extends HashMap<String, Boolean> {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("vein-miner.json");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static VeinMinerConfigData INSTANCE = new VeinMinerConfigData();

    public VeinMinerConfigData() {
        super();
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                VeinMinerConfigData data = GSON.fromJson(reader, VeinMinerConfigData.class);
                if (data != null) {
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
