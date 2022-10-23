package de.fr3qu3ncy.effects.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.fr3qu3ncy.effects.EffectsPlugin;
import de.fr3qu3ncy.effects.config.Config;
import de.fr3qu3ncy.effects.util.EffectUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

@CommandAlias("effecttoggle")
public class EffectToggle extends BaseCommand {

    @Default
    @CommandCompletion("@potionEffects")
    public static void onToggle(Player player, PotionEffectType effectType) {
        if (effectType == null) {
            player.sendMessage(Config.MESSAGE_EFFECT_NOT_FOUND);
            return;
        }

        EffectUtils.toggleEffect(player, effectType);
    }

    @Subcommand("reload")
    @CommandPermission("effects.reload")
    public static void onReload(Player player) {
        EffectsPlugin.getInstance().reloadEasyConfig();
        player.sendMessage("§aConfig has been reloaded.");
    }
}
