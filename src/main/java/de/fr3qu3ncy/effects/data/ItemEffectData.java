package de.fr3qu3ncy.effects.data;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;

public record ItemEffectData(PotionEffectType type, int amplifier) {

    public String serialize() {
        return type.getKey().getKey() + "%" + amplifier;
    }

    public static ItemEffectData deserialize(String str) {
        String[] split = str.split("%");
        if (split.length == 1) return null;

        return new ItemEffectData(
            PotionEffectType.getByKey(NamespacedKey.minecraft(split[0])),
            Integer.parseInt(split[1]));
    }
}
