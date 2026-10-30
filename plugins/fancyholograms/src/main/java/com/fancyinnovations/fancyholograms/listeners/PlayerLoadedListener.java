package com.fancyinnovations.fancyholograms.listeners;

import com.fancyinnovations.fancyholograms.api.FancyHolograms;
import com.fancyinnovations.fancyholograms.api.hologram.Hologram;
import com.fancyinnovations.fancyholograms.util.FoliaSchedulerHelper;
import de.oliver.fancylib.serverSoftware.ServerSoftware;
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class PlayerLoadedListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoaded(@NotNull final PlayerClientLoadedWorldEvent event) {
        final Player player = event.getPlayer();
        FancyHolograms.get().getHologramThread().submit(() -> FoliaSchedulerHelper.playerScheduler(player, () -> {
            for (final Hologram hologram : FancyHolograms.get().getRegistry().getAll()) {
                // on Folia, re-send holograms the client might have missed while it was still loading the world
                // (refreshHologram alone skips holograms the player is already a viewer of)
                if (ServerSoftware.isFolia() && hologram.isViewer(player)) {
                    hologram.despawnFrom(player);
                }
                FancyHolograms.get().getController().refreshHologram(hologram, player);
            }
        }));
    }
}
