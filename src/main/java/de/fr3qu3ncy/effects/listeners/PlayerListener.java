package de.fr3qu3ncy.effects.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import de.fr3qu3ncy.effects.EffectActivation;
import de.fr3qu3ncy.effects.EffectsPlugin;
import de.fr3qu3ncy.effects.PersistenceType;
import de.fr3qu3ncy.effects.util.EffectUtils;
import de.fr3qu3ncy.effects.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler
    public void onWear(PlayerArmorChangeEvent event) {
        final ItemStack oldItem = event.getOldItem() != null ? event.getOldItem() : new ItemStack(Material.AIR);
        final ItemStack newItem = event.getNewItem() != null ? event.getNewItem() : new ItemStack(Material.AIR);

        EffectActivation activation = event.getSlotType() == PlayerArmorChangeEvent.SlotType.HEAD ?
            EffectActivation.HEAD : EffectActivation.BODY;

        EffectsPlugin.getInstance().checkItem(event.getPlayer(), oldItem, newItem, activation);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        EffectsPlugin.getInstance().getAllActiveEffects(event.getPlayer())
            .forEach(effect -> event.getPlayer().removePotionEffect(effect.getType()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        checkEffects(event.getPlayer(), PersistenceType.LOGIN);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(EffectsPlugin.getInstance(),
            () -> checkEffects(event.getPlayer(), PersistenceType.DEATH),
            1L);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        List<String> negatedEffects = EffectUtils.getNegatedEffects(event.getPlayer());

        negatedEffects.forEach(str -> {
            if (EffectUtils.hasToggledEffect(player, str)) {
                PotionEffect effect = EffectUtils.findToggledEffect(player, str);
                if (effect != null) {
                    Utils.removeFromMapList(EffectsPlugin.getActiveToggledEffects(), player.getUniqueId(), effect);
                    player.removePotionEffect(effect.getType());
                }
            }
        });
    }

    private static void checkEffects(Player player, PersistenceType persistenceType) {
        UUID uuid = player.getUniqueId();
        if (!EffectsPlugin.getActiveToggledEffects().containsKey(uuid)) return;

        List<PotionEffect> toRemove = new ArrayList<>();

        EffectsPlugin.getActiveToggledEffects().get(uuid).forEach(potionEffect -> {
            if (persistenceType == PersistenceType.DEATH && !EffectUtils.getEffectData(potionEffect.getType()).isPersistDeath()) {
                toRemove.add(potionEffect);
                return;
            }
            if (persistenceType == PersistenceType.LOGIN && !EffectUtils.getEffectData(potionEffect.getType()).isPersistLogin()) {
                toRemove.add(potionEffect);
            }
        });

        toRemove.forEach(effect ->
            Utils.removeFromMapList(EffectsPlugin.getActiveToggledEffects(), player.getUniqueId(), effect));
    }
}
