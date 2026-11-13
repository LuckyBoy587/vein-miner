package com.veinminer.config;

import com.veinminer.apis.VeinMinerConfig;
import com.veinminer.classification.TargetClassifier;
import com.veinminer.classification.VanillaTargetClassifier;
import com.veinminer.config.model.ClassifiedTarget;
import com.veinminer.config.model.ExactTargetSelector;
import com.veinminer.config.model.GlobalSetting;
import com.veinminer.config.model.TargetKind;
import com.veinminer.config.model.TargetSelector;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Composition root for the configuration system and the runtime-facing
 * implementation of {@link VeinMinerConfig}.
 *
 * <p>Consumers only see the small {@link VeinMinerConfig} API. Classification,
 * selectors, rule precedence and JSON persistence remain implementation
 * details behind this facade.</p>
 */
public final class ConfigService implements VeinMinerConfig {
    public static final ConfigService INSTANCE = new ConfigService();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("vein-miner.json");

    private final TargetClassifier classifier = new VanillaTargetClassifier();
    private volatile ConfigStore store = ConfigStore.empty();

    private ConfigService() {
    }

    public void load() {
        store = ConfigPersistence.load(CONFIG_PATH);
    }

    public void save() {
        ConfigPersistence.save(CONFIG_PATH, store);
    }

    @Override
    public boolean isVeinMinerEnabled() {
        return getGlobal(GlobalSetting.ENABLE_VEIN_MINER);
    }

    @Override
    public boolean isCrouchRequired() {
        return getGlobal(GlobalSetting.CROUCH_REQUIRED);
    }

    @Override
    public boolean isVeinBlock(BlockState blockState) {
        return classifier.classifyBlock(blockState)
                .map(this::isEnabled)
                .orElse(false);
    }

    @Override
    public boolean isVeinMinerTool(ItemStack heldItem) {
        return classifier.classifyItem(heldItem)
                .map(this::isEnabled)
                .orElse(false);
    }

    public boolean isEnabled(ClassifiedTarget target) {
        return store.resolve(target).orElseGet(() -> ConfigDefaults.fallback(target));
    }

    public Optional<ClassifiedTarget> classifyBlock(BlockState state) {
        return classifier.classifyBlock(state);
    }

    public Optional<ClassifiedTarget> classifyItem(ItemStack stack) {
        return classifier.classifyItem(stack);
    }

    public boolean getGlobal(GlobalSetting setting) {
        return store.get(setting);
    }

    public synchronized void setGlobal(GlobalSetting setting, boolean value) {
        store = store.withSetting(setting, value);
    }

    public boolean isBlockEnabled(Block block) {
        return classifier.classifyBlock(block.defaultBlockState())
                .map(this::isEnabled)
                .orElse(false);
    }

    public boolean isToolEnabled(Item item) {
        return classifier.classifyItem(new ItemStack(item))
                .map(this::isEnabled)
                .orElse(false);
    }

    public synchronized void setBlockEnabled(Block block, boolean value) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        store = store.withRule(new ExactTargetSelector(TargetKind.BLOCK, id), value);
    }

    public synchronized void setToolEnabled(Item item, boolean value) {
        classifier.classifyItem(new ItemStack(item)).ifPresent(target ->
                store = store.withRule(new ExactTargetSelector(target.kind(), target.id()), value)
        );
    }

    public boolean isRuleEnabled(TargetSelector selector, boolean fallback) {
        return store.explicitRule(selector)
                .map(ConfigRule::enabled)
                .orElse(fallback);
    }

    public synchronized void setRule(TargetSelector selector, boolean value) {
        store = store.withRule(selector, value);
    }

    /**
     * Exact block ids explicitly enabled by the user. Used by the block search
     * UI instead of reverse-parsing configuration keys.
     */
    public Set<Identifier> explicitlyEnabledBlocks() {
        Set<Identifier> ids = new LinkedHashSet<>();
        for (ConfigRule rule : store.rules()) {
            if (!rule.enabled() || !(rule.selector() instanceof ExactTargetSelector(TargetKind kind, Identifier id))) {
                continue;
            }
            if (kind.equals(TargetKind.BLOCK)) {
                ids.add(id);
            }
        }
        return ids;
    }

    public Map<String, Boolean> explicitBlockStates() {
        Map<String, Boolean> states = new LinkedHashMap<>();
        for (ConfigRule rule : store.rules()) {
            if (rule.selector() instanceof ExactTargetSelector(TargetKind kind, Identifier id)
                    && kind.equals(TargetKind.BLOCK)) {
                states.put(id.toString(), rule.enabled());
            }
        }
        return states;
    }

    public Optional<ConfigRule> explicitRule(TargetSelector selector) {
        return store.explicitRule(selector);
    }
}
