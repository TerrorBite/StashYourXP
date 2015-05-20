package net.lethargiclion.stashyourxp;

import java.util.logging.Logger;

import net.lethargiclion.stashyourxp.listeners.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * This is the main class of the sample plug-in
 */
public final class StashYourXP extends JavaPlugin {
    /*
     * This is called when your plug-in is enabled
     */
    
    @Override
    public void onEnable() {
        // save the configuration file
        saveDefaultConfig();
        
        // Listener for the Experience Bottler device
        new ExpBottler(this);
        new ExpBottleThrown(this);
        
        // Also add an extra listener if books are enabled
        if(getConfig().getBoolean("giveBook", false)) new BottlerManual(this);
        
        // set the command executor for sample
        //this.getCommand("sample").setExecutor(new stashyourxpCommandExecutor(this));
        
        /*
        int counting = 0, ours = 0, ourprev = 0, lvlxp = 0, delta = 0, deltaprev = 0;
        for(int lvl = 0; lvl < 40; lvl++) {
            lvlxp = getExpWithinLevel(lvl);
            counting += lvlxp;
            ourprev = ours;
            ours = getTotalExpForLevel(lvl);
            deltaprev = delta;
            delta = ours-ourprev;
            getLogger().info(String.format("\nLevel %2d, we need:\n %4d to advance, %4d total | %4d total (delta %4d, rate %d)",
                    lvl, lvlxp, counting, ours, delta, delta-deltaprev));
            getLogger().info(String.format("Expecting %d, got %d", lvl, getLevelForExp(counting)));
        }
        */
    }

    public static Logger getLoggerStatic() {
        return Bukkit.getServer().getPluginManager().getPlugin(StashYourXP.class.getSimpleName()).getLogger();
    }
    
    /*
     * This is called when your plug-in shuts down
     */
    @Override
    public void onDisable() {
        
    }
    
    /**
     * <p>Gets the player's total experience, based on their level and progress towards the next level.</p>
     * @param player The player to examine.
     * @return The player's total experience.
     */
    public static int getExperience(Player player) {
        int level = player.getLevel();
        // calculate experience required to reach this level
        int exp = getTotalExpForLevel(level);
        // add progress made towards next level
        exp += (int) (player.getExp() * getExpWithinLevel(level));
        return exp;
    }

    /**
     * Calculates how much experience is needed to reach the next level after this one.
     * @param level The level number to query
     * @return The quantity of experience required to reach the next level
     */
    public static int getExpWithinLevel(int level) {
        // Copied from Mojang code:
        return level >= 30 ? 112 + (level - 30) * 9 : (level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2);
    }
    
    /**
     * Calculates how much experience is contained within a given number of levels.
     * @param level The number of levels
     * @return The quantity of experience required to reach this level from level 0
     */
    public static int getTotalExpForLevel(int level) {
        //return level >= 30 ? ((level-29)*(9*(level-30)+224))/2 + 1395 : (level >= 15 ? ((level-14)*(5*(level-15)+74))/2 + 315 : ((level+1)*(2*level+14))/2);
        return level >= 30 ? (level*(9*level-307)+4124)/2 : (level >= 15 ? (level*(5*level-71)+644)/2 : level*(level+8)+7);
        
        // this is horribly wrong:
        /*if (level >= 30) return (int) (3.5 * Math.pow(level, 2) - 151.5 * level + 2220);
        else if (level >= 15) return (int) (1.5 * Math.pow(level, 2) - 29.5 * level + 360);
        else return level * 17;
        */
    }
    
    public static double getExactLevelForExp(int exp) {
        return exp >= 1395 ? (Math.sqrt(23562.25 - 18*(2062-exp))+153.5)/9.0 : (exp >= 315 ? (Math.sqrt(1260.25 - 10*(322-exp))+35.5)/5.0 : (Math.sqrt(64 - 4*(7-exp))-8)/2.0);
    }
    public static int getLevelForExp(int exp) {
        return (int)getExactLevelForExp(exp);
    }
    
    /**
     * <p>Sets a player's levels and level progress to match a total experience value.</p>
     * <p>Use this function instead of {@link org.bukkit.entity.Player#setTotalExperience() Player.setTotalExperience()}
     * because Bukkit's {@code setTotalExperience()} does not update the level and progress values. 
     * @param player The player to modify
     * @param exp The experience value to use when setting the level
     */
    public static void setExperience(Player player, int exp) {
        int level = getLevelForExp(exp);
        int remaining = exp - getTotalExpForLevel(level);
        int tonext = getExpWithinLevel(level);
        player.setLevel(level);
        player.setExp((float)remaining / (float)tonext);
    }

    public static void addExperience(Player player, int exp) {
        // Separately update totalExperience and actual level+expbar
        int current = getExperience(player);
        int currentTotal = player.getTotalExperience();
        setExperience(player, current + exp);
        player.setTotalExperience(currentTotal + exp);
    }

}
