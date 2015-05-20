package net.lethargiclion.stashyourxp.listeners;

import java.util.Random;

import net.lethargiclion.stashyourxp.StashYourXP;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ExpBottleThrown implements Listener {
    
    Random random = new Random();
    
    public ExpBottleThrown(StashYourXP plugin) {
        // Register the listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onExpBottleThrown(ProjectileLaunchEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();
        if(event.getEntityType() == EntityType.THROWN_EXP_BOTTLE && shooter instanceof Player) {
            Player player = (Player) shooter;
            if(player.isSneaking()) {
                event.setCancelled(true);
                // TODO: Decrement item in hand
                
                // Slurping sounds
                player.getWorld().playSound(player.getLocation(), Sound.DRINK, 0.7f,
                        (random.nextFloat()*0.5f) + 0.75f);
                // go "ding"
                player.getWorld().playSound(player.getLocation(), Sound.ORB_PICKUP, 0.3f,
                        (random.nextFloat()*0.75f) + 0.5f);
                
                // Roll the dice to get a value between 3 and 11 (like a thrown XP bottle)
                int exp = 3 + random.nextInt(5) + random.nextInt(5);
                
                int beforeLevel = player.getLevel();
                StashYourXP.addExperience(player, exp);
                int level = player.getLevel();
                
                // Replicate Mojang sound behaviour
                float vol = level > 30 ? 1.0f : (float) level / 30.0f;
                if((level % 5 == 0) && (beforeLevel % 5 != 0)) {
                    player.getWorld().playSound(player.getLocation(), Sound.LEVEL_UP, vol*0.75f, 1.0f);
                }

            }
        }
    }
    
}
