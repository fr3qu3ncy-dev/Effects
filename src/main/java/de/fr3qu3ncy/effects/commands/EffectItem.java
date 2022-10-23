package de.fr3qu3ncy.effects.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.fr3qu3ncy.effects.EffectActivation;
import de.fr3qu3ncy.effects.data.ItemEffectData;
import de.fr3qu3ncy.effects.util.EffectUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

@CommandAlias("effectitem")
@CommandPermission("effects.item")
public class EffectItem extends BaseCommand {

    @Default
    @CommandCompletion("@potionEffects amplifier @effectActivations")
    public static void onEffectItem(Player player, PotionEffectType type, int amplifier,
                                    @Values("@effectActivations") String[] activationsString) {
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand.getType() == Material.AIR) {
            player.sendMessage("§cYou need to hold an item!");
            return;
        }

        EffectActivation[] activations = Arrays.stream(activationsString)
            .map(EffectActivation::valueOf).toList().toArray(new EffectActivation[0]);

        EffectUtils.setInItem(inHand, activations, new ItemEffectData(type, amplifier - 1));
        player.sendMessage("§aEffect has been applied.");
    }

}
