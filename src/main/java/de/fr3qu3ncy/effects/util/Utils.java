package de.fr3qu3ncy.effects.util;

import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Utils {

    private Utils() {}

    public static void addInMapList(Map<UUID, List<PotionEffect>> map, UUID key, PotionEffect value) {
        List<PotionEffect> effects = map.getOrDefault(key, new ArrayList<>());
        effects.add(value);
        map.put(key, effects);
    }

    public static void removeFromMapList(Map<UUID, List<PotionEffect>> map, UUID key, PotionEffect value) {
        List<PotionEffect> effects = map.getOrDefault(key, new ArrayList<>());
        effects.remove(value);
        map.put(key, effects);
    }
}
