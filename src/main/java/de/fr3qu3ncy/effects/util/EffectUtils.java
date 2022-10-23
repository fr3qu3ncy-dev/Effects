package de.fr3qu3ncy.effects.util;

import de.fr3qu3ncy.effects.EffectActivation;
import de.fr3qu3ncy.effects.EffectsPlugin;
import de.fr3qu3ncy.effects.config.Config;
import de.fr3qu3ncy.effects.config.EffectConfig;
import de.fr3qu3ncy.effects.data.ItemEffectData;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class EffectUtils {

    private EffectUtils() {
    }

    public static void addItemEffect(Player player, ItemEffectData data) {
        Utils.addInMapList(EffectsPlugin.getActiveItemEffects(), player.getUniqueId(),
            new PotionEffect(data.type(), Integer.MAX_VALUE, data.amplifier()));
    }

    public static void removeItemEffect(Player player, ItemEffectData data) {
        Utils.removeFromMapList(EffectsPlugin.getActiveItemEffects(), player.getUniqueId(), findItemEffect(player, data.type()));
        player.removePotionEffect(data.type());
    }

    public static void toggleEffect(Player player, PotionEffectType type) {
        //Get the highest allowed amplifier
        int allowedAmplifier = getAllowedAmplifier(player, type);

        //Check if player already has same type applied
        if (hasToggledEffect(player, type)) {
            PotionEffect toRemove = findToggledEffect(player, type);
            if (toRemove != null) {
                Utils.removeFromMapList(EffectsPlugin.getActiveToggledEffects(), player.getUniqueId(), toRemove);
                player.removePotionEffect(type);

                if (!Config.MESSAGE_EFFECT_TOGGLED_OFF.isBlank()) {
                    player.sendMessage(Config.MESSAGE_EFFECT_TOGGLED_OFF);
                    return;
                }
            }
        }

        if (allowedAmplifier <= 0) {
            player.sendMessage(Config.MESSAGE_EFFECT_NOT_ALLOWED);
            return;
        }

        //Check if player already has same or better effects
        List<PotionEffect> allActiveEffects = EffectsPlugin.getInstance().getAllActiveEffects(player);
        if (allActiveEffects.stream().anyMatch(
            potionEffect -> potionEffect.getType() == type && potionEffect.getAmplifier() > allowedAmplifier)) {
            return;
        }

        PotionEffect effect = new PotionEffect(type, Integer.MAX_VALUE, Math.min(255, allowedAmplifier - 1));
        Utils.addInMapList(EffectsPlugin.getActiveToggledEffects(), player.getUniqueId(), effect);

        if (!Config.MESSAGE_EFFECT_TOGGLED_ON.isBlank()) player.sendMessage(Config.MESSAGE_EFFECT_TOGGLED_ON);
    }

    public static boolean hasToggledEffect(Player player, String effectName) {
        PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(effectName));
        if (type != null) {
            return hasToggledEffect(player, type);
        }
        return false;
    }

    public static boolean hasToggledEffect(Player player, PotionEffectType type) {
        return EffectsPlugin.getActiveToggledEffects().containsKey(player.getUniqueId())
            && EffectsPlugin.getActiveToggledEffects().get(player.getUniqueId()).stream()
            .anyMatch(effect -> effect.getType() == type);
    }

    public static PotionEffect findToggledEffect(Player player, String effectName) {
        PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(effectName));
        if (type == null) return null;

        return EffectsPlugin.getActiveToggledEffects().getOrDefault(player.getUniqueId(), new ArrayList<>()).stream()
            .filter(effect -> effect.getType() == type)
            .findFirst()
            .orElse(null);
    }

    private static PotionEffect findToggledEffect(Player player, PotionEffectType type) {
        return EffectsPlugin.getActiveToggledEffects().getOrDefault(player.getUniqueId(), new ArrayList<>()).stream()
            .filter(effect -> effect.getType() == type)
            .findFirst()
            .orElse(null);
    }

    private static PotionEffect findItemEffect(Player player, PotionEffectType type) {
        return EffectsPlugin.getActiveItemEffects().getOrDefault(player.getUniqueId(), new ArrayList<>()).stream()
            .filter(effect -> effect.getType() == type)
            .findFirst()
            .orElse(null);
    }

    public static void setInItem(ItemStack item, EffectActivation[] activations, ItemEffectData data) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String oldEffectsString = meta.getPersistentDataContainer().get(effectsKey(), PersistentDataType.STRING);
        if (oldEffectsString == null) oldEffectsString = "";

        List<ItemEffectData> effects = Arrays.stream(oldEffectsString.split(";")).map(ItemEffectData::deserialize)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        //Remove effects with lower or same amplifier
        effects = effects.stream().filter(effect -> effect.type() != data.type())
            .collect(Collectors.toList());
        effects.add(data);

        String newEffectsString = effects.stream().map(ItemEffectData::serialize).collect(Collectors.joining(";"));

        String activationsString = Arrays.stream(activations).map(act -> String.valueOf(act.ordinal()))
            .collect(Collectors.joining(","));

        meta.getPersistentDataContainer().set(effectsKey(), PersistentDataType.STRING, newEffectsString);
        meta.getPersistentDataContainer().set(activationKey(), PersistentDataType.STRING, activationsString);

        List<String> lore = new ArrayList<>();

        List<ItemEffectData> finalEffects = effects;
        Arrays.stream(activations).forEach(activation -> {
            lore.add(Config.EFFECT_ITEM_LORE_HEADER
                .replace("%slot%", activation.getName())
                .replace("%slotDescription%", activation.getDescription()));

            finalEffects.forEach(effect -> lore.add(Config.EFFECT_ITEM_LORE_LIST
                .replace("%effectName%", Config.EFFECT_DATA.get(effect.type().getKey().getKey()).getDisplayName())
                .replace("%amplifier%", String.valueOf(data.amplifier() + 1))));
        });
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    public static List<Optional<ItemEffectData>> getFromItem(ItemStack item, EffectActivation activation) {
        if (item.getItemMeta() == null) return Collections.emptyList();

        String activationsString = item.getItemMeta().getPersistentDataContainer()
            .get(activationKey(), PersistentDataType.STRING);
        if (activationsString == null) return Collections.emptyList();

        List<EffectActivation> activations = Arrays.stream(activationsString.split(","))
            .mapToInt(Integer::parseInt)
            .mapToObj(ordinal -> EffectActivation.values()[ordinal]).toList();
        if (activations.isEmpty()) return Collections.emptyList();

        boolean matchFound = false;
        for (EffectActivation itemActivation : activations) {
            if (itemActivation == activation) {
                matchFound = true;
                break;
            }
        }
        if (!matchFound) {
            return Collections.emptyList();
        }

        String effects = item.getItemMeta().getPersistentDataContainer().get(effectsKey(), PersistentDataType.STRING);
        if (effects == null) effects = "";

        return Arrays.stream(effects.split(";"))
            .filter(str -> !str.isBlank() && !str.isEmpty())
            .map(str -> Optional.of(ItemEffectData.deserialize(str)))
            .toList();
    }

    private static NamespacedKey effectsKey() {
        return new NamespacedKey(EffectsPlugin.getInstance(), "effect_list");
    }

    private static NamespacedKey activationKey() {
        return new NamespacedKey(EffectsPlugin.getInstance(), "effect_activation");
    }

    public static EffectConfig getEffectData(PotionEffectType type) {
        return Config.EFFECT_DATA.getOrDefault(type.getKey().getKey(), new EffectConfig("Unknown", false, false));
    }

    private static int getAllowedAmplifier(Player player, PotionEffectType type) {
        final String lowerCaseEffectName = type.getKey().getKey().toLowerCase();
        return player.getEffectivePermissions().stream().filter(perm ->
                perm.getPermission().toLowerCase().startsWith("buff." + lowerCaseEffectName + "."))
            .mapToInt(perm -> {
                try {
                    return Integer.parseInt(perm.getPermission().split("buff." + lowerCaseEffectName + ".")[1]);
                } catch (NumberFormatException ex) {
                    return -1;
                }
            }).max().orElse(-1);
    }

    public static List<String> getNegatedEffects(Player player) {
        return player.getEffectivePermissions().stream()
            .filter(
                perm -> perm.getPermission().startsWith("-")
                    && perm.getPermission().replaceFirst("-", "").startsWith("buff."))
            .map(perm -> perm.getPermission().substring(6))
            .toList();
    }
}
