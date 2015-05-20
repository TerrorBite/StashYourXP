package net.lethargiclion.stashyourxp.listeners;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.lethargiclion.stashyourxp.StashYourXP;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.Vector;

public class BottlerManual implements Listener {
    
    private final StashYourXP plugin;
    private Set<UUID> players = new HashSet<UUID>();

    public BottlerManual(StashYourXP plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("Registered BottlerManual handler");
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Block below = block.getRelative(BlockFace.DOWN, 1);
        if(block.getType() == Material.GOLD_PLATE &&
                (below.getType() == Material.FURNACE ||
                below.getType() == Material.BURNING_FURNACE)) {
            
            // don't give them another book if they already built one this session
            if(players.contains(player.getUniqueId())) return;
            
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
            BookMeta bookData = (BookMeta) book.getItemMeta();

            bookData.setTitle("XP Bottler Instructions");
            bookData.setAuthor("TerrorBite Industries");
            
            int loss = plugin.getConfig().getInt("expLoss", 0);
            
            String lossMsg = (loss==0) ?
                    "Our bottling process is 100% efficient, though there may be small variations in the amount of experience stored in each bottle." :
                        String.format("The bottling process isn't perfect, and only about %d%% of the experience taken from you will make it into the bottles.",
                                Math.round(100*(1f-(float)loss / 7.0f)));
            
            bookData.setLore(Arrays.asList("Everything you need to know about", "storing your experience in bottles!",
                    MessageFormat.format("{0}Version {1}", ChatColor.DARK_GRAY, plugin.getDescription().getVersion())));
            
            Material fuel = Material.matchMaterial(plugin.getConfig().getString("fuel", "GLOWSTONE_DUST"));
            String fuelName = (fuel == null) ? "Glowstone Dust" : WordUtils.capitalizeFully(fuel.name().replace('_', ' '));
            
            bookData.setPages(Arrays.asList(
                    "Congratulations on constructing your very own Experience Bottler! In these pages you will learn how to use your new device.\n\n"
                    + "First, your Bottler requires bottles to fill. Simply insert empty glass bottles into the top slot of the furnace.",
                    
                    MessageFormat.format("Bottling experience is a complex process and the Bottler requires special fuel.\n\n" +
                    "The Bottler consumes one {0} per bottle filled.\n\n{0} can be inserted into the bottom slot of the furnace.", fuelName),
                    
                    "Finally, just stand on the pressure plate and listen to the chimes as those bottles fill up!\n\n" + lossMsg,
                    
                    "You can protect the contents of your Bottler from would-be thieves the same way you'd protect any other furnace.\n\n"
                    + "To retrieve experience from a bottle, you can right-click while sneaking to drink it... or just throw it at something solid.",
                    
                    MessageFormat.format("Hoppers facing into the side of the furnace block will automatically place {0} and Bottles into their"
                    + " appropriate slots.\n\n{1}Please Note:{2}\nHoppers won't work while standing on the pressure"
                    + " plate, because of the redstone signal.", fuelName, ChatColor.UNDERLINE, ChatColor.RESET),
                    
                    "That's all. Enjoy your new Experience Bottler from TerrorBite Industries!"
                    ));
            
            //plugin.getLogger().info("bookData is of type: "+ bookData.getClass().getName());

            try {
                Field f = bookData.getClass().getSuperclass().getDeclaredField("generation");
                f.setAccessible(true);
                f.set(bookData, 3);
            } catch (NoSuchFieldException e) {
                // Possible to happen if internals change or we're on an older version
                // No harm done to ignore this
            } catch (SecurityException e) {
                // Minecraft shouldn't be running with a security manager
            } catch (IllegalArgumentException e) {
                // This shouldn't happen (unless the type of the field changes or similar)
            } catch (IllegalAccessException e) {
                // This shouldn't happen because we setAccessible(true)
            }
            
            book.setItemMeta(bookData);
            
            // give book to player
            Item item = player.getWorld().dropItem(block.getLocation().add(new Vector(0.5, 0.05, 0.5)), book);
            item.setVelocity(new Vector(0.0, 0.0, 0.0));
            item.setCustomName("Instructions");
            item.setCustomNameVisible(true);
            
            // remember not to give them another one later
            players.add(player.getUniqueId());

        }
    }
    

}
