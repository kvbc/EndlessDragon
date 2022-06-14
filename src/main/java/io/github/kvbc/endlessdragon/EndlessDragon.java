package io.github.kvbc.endlessdragon;

import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_18_R1.boss.CraftDragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class EndlessDragon extends JavaPlugin implements Listener {
    private final Time dragon_reset_interval = new Time(1, 0, 0); // 1 hour (>30s)
    private final Map<UUID, Time> players_end_enter_time = new HashMap<UUID, Time>();

    private World get_the_end_world () {
        for (World world : getServer().getWorlds()) {
            if (world.getEnvironment() == World.Environment.THE_END) {
                return world;
            }
        }
        return null;
    }

    private boolean ender_dragon_exists () {
        return ! get_the_end_world().getEntitiesByClasses(EnderDragon.class).isEmpty();
    }

    private void respawn_ender_dragon () {
        /*
         * Okay so what the fuck is going on and why am I not using getEnderDragonBattle().initiateRespawn()?
         * For some reason the CraftBukkit implementation of this function is kinda broken and doesn't work exactly as one would expect.
         * It checks if the 4 ender crystals are placed on the end portal (as you would respawn the dragon manually) and ONLY THEN proceeds to respawn the dragon.
         * Why is it not skipping this check? no idea...
         *
         * The NMS function used by getEnderDragonBattle().initiateRespawn() is EnderDragonBattle.e().
         * With a little bit of digging and help from the community, it turns out that the PRIVATE EnderDragonBattle.a(List<EntityEnderCrystal>) function
         * will respawn the ender dragon without the crystal check (the .a() function is called by .e() with the 4 placed crystals entities as the argument, but we can just give it an empty list!)
         * Since this is exactly what we want, we just gotta call this function, but since it's private we've gotta use Reflection.
         * More info: https://www.spigotmc.org/threads/re-spawning-the-ender-dragon.255183/
         *
         * So naturally we have to use NMS
         * 1. Get the Bukkit respresentation of the DragonBattle (CraftDragonBattle).
         * 2. Use Reflection to retrieve the PRIVATE NMS handle of CraftDragonBattle (EnderDragonBattle)
         * 3. Call the previously mentioned PRIVATE EnderDragonBattle.a(List<...>) function with an empty list, using Reflection
         *
         * That's it! Continuous days of endless pain and misery shortened into 5 lines of code.
         */
        CraftDragonBattle craftBattle = (CraftDragonBattle)get_the_end_world().getEnderDragonBattle();
        EnderDragonBattle mcBattle = (EnderDragonBattle)ReflectionUtil.getPrivateField(craftBattle, "handle");
        try {
            ReflectionUtil.getPrivateMethod(EnderDragonBattle.class, "a", List.class).invoke(mcBattle, (Object)Collections.emptyList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public void onEnable () {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onLoad () {
        if (!ender_dragon_exists()) {
            getServer().broadcastMessage(
                ChatColor.DARK_RED + "" + ChatColor.BOLD + "[EndlessDragon] " +
                ChatColor.DARK_PURPLE + "Ender Dragon " +
                ChatColor.RED + "nie istnieje, respawnowanie..."
            );
            respawn_ender_dragon();
        }
    }

    @EventHandler
    public void onPlayerChangedWorld (PlayerChangedWorldEvent event) {
        Player plr = event.getPlayer();
        if (plr.getWorld().getEnvironment() == World.Environment.THE_END) {
            Time now = new Time();
            players_end_enter_time.put(plr.getUniqueId(), now);
            getServer().broadcastMessage(
                ChatColor.GREEN + "Gracz " +
                ChatColor.GOLD + event.getPlayer().getName() +
                ChatColor.GREEN + " wszedł do endu o " +
                ChatColor.GOLD + now.toTimeString()
            );
        }
    }

    @EventHandler
    public void onEntityDeath (EntityDeathEvent e) {
        if (e.getEntity().getType() == EntityType.ENDER_DRAGON) {
            Time now = new Time();
            Player klr = e.getEntity().getKiller();
            if (klr == null) {
                getServer().broadcastMessage(
                    ChatColor.GREEN + "Ender Dragon został zabity o " +
                    ChatColor.GOLD + now.toTimeString()
                );
            }
            else {
                String suffix = "";
                Time end_enter_time = players_end_enter_time.get(klr.getUniqueId());
                if (end_enter_time != null) {
                    suffix = ChatColor.GREEN + " (po " +
                             ChatColor.GOLD + now.diff(end_enter_time).toDurationString() +
                             ChatColor.GREEN + ")";
                    players_end_enter_time.remove(klr.getUniqueId());
                }
                getServer().broadcastMessage(
                    ChatColor.GREEN + "Gracz " +
                    ChatColor.GOLD + klr.getName() +
                    ChatColor.GREEN + " zabil " +
                    ChatColor.DARK_PURPLE + "Ender Dragona" +
                    ChatColor.GREEN + " o " +
                    ChatColor.GOLD + now.toTimeString() + suffix
                );
                getServer().broadcastMessage(
                    ChatColor.RED + "Reset " +
                    ChatColor.DARK_PURPLE + "Ender Dragona " +
                    ChatColor.RED + "nastąpi za " +
                    ChatColor.GOLD + dragon_reset_interval.toDurationString() +
                    ChatColor.RED + " (o " +
                    ChatColor.GOLD + now.add(dragon_reset_interval).toTimeString() +
                    ChatColor.RED + ")"
                );
                Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                    @Override
                    public void run() {
                        respawn_ender_dragon();
                        getServer().broadcastMessage(
                            ChatColor.RED + "Zrespawnowano " +
                            ChatColor.DARK_PURPLE + "Ender Dragona" +
                            ChatColor.RED + "!"
                        );
                    }
                }, dragon_reset_interval.ticks());
            }
        }
    }
}
