package de.fr3qu3ncy.effects.config;

import de.fr3qu3ncy.easyconfig.core.annotations.ConfigPath;
import lombok.Setter;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Config {

    private Config() {}

    static {
        Map<String, EffectConfig> dataMap = new LinkedHashMap<>();
        Arrays.stream(PotionEffectType.values()).sorted(Comparator.comparing(o -> o.getKey().getKey())).forEach(type ->
            dataMap.put(type.getKey().getKey(), new EffectConfig(type.getName(), true, true)));
        EFFECT_DATA = dataMap;
    }

    @ConfigPath("messages.effect_not_found")
    public static String MESSAGE_EFFECT_NOT_FOUND = "&cEffect not found!";

    @ConfigPath("messages.effect_not_allowed")
    public static String MESSAGE_EFFECT_NOT_ALLOWED = "&cYou're not allowed to have this effect!";

    @ConfigPath("messages.effect_toggled_on")
    public static String MESSAGE_EFFECT_TOGGLED_ON = "&aEffect has been toggled on.";

    @ConfigPath("messages.effect_toggled_off")
    public static String MESSAGE_EFFECT_TOGGLED_OFF = "&7Effect has been toggled off.";

    @ConfigPath("effect_item_lore_header")
    public static String EFFECT_ITEM_LORE_HEADER = "&f%slotDescription%:";

    @ConfigPath("effect_item_lore_list")
    public static String EFFECT_ITEM_LORE_LIST = "  &7- %effectName% %amplifier%";

    @ConfigPath("effects")
    @Setter
    public static Map<String, EffectConfig> EFFECT_DATA;
}
