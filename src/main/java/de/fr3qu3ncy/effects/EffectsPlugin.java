package de.fr3qu3ncy.effects;

import co.aikar.commands.PaperCommandManager;
import de.fr3qu3ncy.easyconfig.core.EasyConfig;
import de.fr3qu3ncy.easyconfig.core.registry.ConfigRegistry;
import de.fr3qu3ncy.easyconfig.spigot.SpigotConfig;
import de.fr3qu3ncy.effects.commands.EffectItem;
import de.fr3qu3ncy.effects.commands.EffectToggle;
import de.fr3qu3ncy.effects.config.Config;
import de.fr3qu3ncy.effects.config.EffectConfig;
import de.fr3qu3ncy.effects.data.ItemEffectData;
import de.fr3qu3ncy.effects.listeners.PlayerListener;
import de.fr3qu3ncy.effects.util.EffectUtils;
import lombok.Getter;
import net.minecraft.util.Tuple;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.*;

public class EffectsPlugin extends JavaPlugin {

    @Getter
    private static final Map<UUID, List<PotionEffect>> activeItemEffects = new HashMap<>();

    @Getter
    private static final Map<UUID, List<PotionEffect>> activeToggledEffects = new HashMap<>();

    @Getter
    private static final Map<Player, Tuple<ItemStack, ItemStack>> savedInventories = new HashMap<>();

    private EasyConfig config;

    @Override
    public void onEnable() {
        //Setup commands
        registerCommands();

        //Register listeners
        registerListeners();

        //Setup config
        setupConfig();

        //Check inventories
        startEffectChecker();
    }

    private void startEffectChecker() {
        savedInventories.keySet().removeIf(player -> !player.isOnline());

        getServer().getScheduler().runTaskTimer(this,
            this::checkEffects,
            1L,
            1L);
    }

    private void checkEffects() {
        savedInventories.keySet().removeIf(player -> !player.isOnline());

        getServer().getOnlinePlayers().forEach(this::checkInventory);

        //Apply effects
        getServer().getOnlinePlayers().forEach(player -> getAllActiveEffects(player).forEach(player::addPotionEffect));
    }

    private void checkInventory(Player player) {
        savedInventories.computeIfAbsent(player,
            p -> new Tuple<>(p.getInventory().getItemInMainHand(), p.getInventory().getItemInOffHand()));

        Tuple<ItemStack, ItemStack> savedInventory = savedInventories.get(player);
        ItemStack oldMainHand = savedInventory.a();
        ItemStack oldOffHand = savedInventory.b();

        ItemStack newMainHand = player.getInventory().getItemInMainHand();
        ItemStack newOffHand = player.getInventory().getItemInOffHand();

        checkItem(player, oldMainHand, newMainHand, EffectActivation.MAIN_HAND);
        checkItem(player, oldOffHand, newOffHand, EffectActivation.OFF_HAND);

        checkItem(player, oldMainHand, newMainHand, EffectActivation.HOLDING);
        checkItem(player, oldOffHand, newOffHand, EffectActivation.HOLDING);

        savedInventories.put(player, new Tuple<>(newMainHand.clone(), newOffHand.clone()));
    }

    public void checkItem(Player player, @Nonnull ItemStack oldItem, @Nonnull ItemStack newItem, EffectActivation activation) {
        if (oldItem.equals(newItem)) return;

        //Remove old effects
        List<Optional<ItemEffectData>> optionalRemoveTypes = EffectUtils.getFromItem(oldItem, activation);

        //Apply new effects
        List<Optional<ItemEffectData>> optionalTypes = EffectUtils.getFromItem(newItem, activation);

        //Check if same
        if (!optionalRemoveTypes.equals(optionalTypes)) {
            optionalRemoveTypes.forEach(removeType ->
                removeType.ifPresent(data -> EffectUtils.removeItemEffect(player, data)));

            optionalTypes.forEach(optionalType ->
                optionalType.ifPresent(data -> EffectUtils.addItemEffect(player, data)));
        }
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.getCommandContexts().registerContext(PotionEffectType.class, c -> {
            String name = c.popFirstArg();
            return PotionEffectType.getByKey(NamespacedKey.minecraft(name));
        });

        commandManager.getCommandContexts().registerContext(EffectActivation.class, c -> EffectActivation.valueOf(c.popFirstArg()));

        commandManager.getCommandContexts().registerContext(EffectActivation[].class, c -> {
            List<EffectActivation> activations = new ArrayList<>();
            while (!c.isLastArg()) {
                activations.add(EffectActivation.valueOf(c.popFirstArg()));
            }
            activations.add(EffectActivation.valueOf(c.popLastArg()));
            return activations.toArray(new EffectActivation[0]);
        });

        commandManager.getCommandCompletions().registerCompletion("potionEffects",
            c -> Arrays.stream(PotionEffectType.values()).map(type -> type.getKey().getKey()).toList());

        commandManager.getCommandCompletions().registerCompletion("effectActivations",
            c -> Arrays.stream(EffectActivation.values()).map(Enum::name).toList());

        commandManager.registerCommand(new EffectItem());
        commandManager.registerCommand(new EffectToggle());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    private void setupConfig() {
        ConfigRegistry.register(EffectConfig.class);

        config = new SpigotConfig(getDataFolder(), "config", Config.class);
        config.load();
    }

    public void reloadEasyConfig() {
        config.reloadConfig();
    }

    public List<PotionEffect> getAllActiveEffects(Player player) {
        List<PotionEffect> allEffects = new ArrayList<>();
        allEffects.addAll(activeItemEffects.getOrDefault(player.getUniqueId(), new ArrayList<>()));
        allEffects.addAll(activeToggledEffects.getOrDefault(player.getUniqueId(), new ArrayList<>()));

        return allEffects;
    }

    public static EffectsPlugin getInstance() {
        return getPlugin(EffectsPlugin.class);
    }
}
