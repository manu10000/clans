package me.mykindos.betterpvp.clans.progression.perks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.clans.clans.Clan;
import me.mykindos.betterpvp.clans.clans.ClanManager;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import me.mykindos.betterpvp.progression.ProgressionsManager;
import me.mykindos.betterpvp.progression.model.ProgressionPerk;
import me.mykindos.betterpvp.progression.model.ProgressionTree;
import me.mykindos.betterpvp.progression.model.stats.ProgressionData;
import me.mykindos.betterpvp.progression.tree.fishing.Fishing;
import me.mykindos.betterpvp.progression.tree.fishing.event.PlayerStartFishingEvent;
import org.bukkit.Location;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

@Singleton
public class BaseFishingPerk implements Listener, ProgressionPerk {

    @Inject(optional = true)
    private ClanManager manager;

    @Inject(optional = true)
    private ProgressionsManager progressionsManager;

    @Inject(optional = true)
    private Fishing fishing;

    @Override
    public String getName() {
        return "Base Fishing";
    }

    @Override
    public Class<? extends ProgressionTree>[] acceptedTrees() {
        return new Class[] {
                Fishing.class
        };
    }

    @Override
    public boolean canUse(Player player, ProgressionData<?> data) {
        return data.getLevel() > 1;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFish(PlayerStartFishingEvent event) {
        final FishHook hook = event.getPlayer().getFishHook();
        if (hook == null || !hook.isValid()) {
            return;
        }

        final Location fishingLocation = hook.getLocation();
        final Player player = event.getPlayer();
        final Optional<Clan> clan = manager.getClanByLocation(fishingLocation);
        fishing.hasPerk(event.getPlayer(), getClass()).whenComplete((hasPerk, throwable) -> {
            if (hasPerk) {
                return; // Don't cancel if they have the perk, and they're in their base
            }

            // Otherwise, cancel if they're not in fields or lake
            if (clan.map(c -> c.getName().equalsIgnoreCase("Fields") || c.getName().equalsIgnoreCase("Lake")).orElse(false)) {
                return;
            }

            hook.remove();
            UtilMessage.message(player, "Fishing", "<red>You cannot fish in this area!");
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }
}