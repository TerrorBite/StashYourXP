package net.lethargiclion.stashyourxp.listeners;

import java.util.Random;

import net.lethargiclion.stashyourxp.StashYourXP;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.FurnaceAndDispenser;
import org.bukkit.util.Vector;

/*
 * This is a sample event listener
 */
public class ExpBottler implements Listener {
    private static final int INPUT_SLOT = 0;
    private static final int FUEL_SLOT = 1;
    //private static final int RESULT_SLOT = 2;
    private static final Vector STUPID_OFFSET = new Vector(0.1, 0, 0.1);
    
    private final StashYourXP plugin;
    private final int expPerBottle;
    private final Material fuelMaterial;
    
    /*
     * This listener needs to know about the plugin which it came from
     */
    public ExpBottler(StashYourXP plugin) {
        // Register the listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        this.plugin = plugin;
        
        this.expPerBottle = 7 + plugin.getConfig().getInt("expLoss", 0);
        String fuelName = plugin.getConfig().getString("fuel", "GLOWSTONE_DUST");
        Material m = Material.matchMaterial(fuelName);
        if(m == null) {
            plugin.getLogger().warning(String.format("I don't recognise the fuel material \"%s\". Using GLOWSTONE_DUST instead.", fuelName));                    
            this.fuelMaterial = Material.GLOWSTONE_DUST;
        } else this.fuelMaterial = m;
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onItemInserted(InventoryMoveItemEvent event) {
        if(event.isCancelled()) return;
        
        Inventory dest = event.getDestination();
        if(dest instanceof FurnaceInventory) {
            FurnaceInventory inv = (FurnaceInventory) dest;
            
            // Check if this furnace has a gold pressure plate on top
            if(inv.getHolder().getBlock().getRelative(BlockFace.UP, 1).getType() == Material.GOLD_PLATE) {
                ItemStack inserting = event.getItem();
                Inventory src = event.getSource();

                if(inserting.getType() == fuelMaterial) {
                    plugin.getServer().getScheduler().runTask(plugin,
                            new MoveOneToSlotTask(src, inv, FUEL_SLOT, fuelMaterial));
                    event.setCancelled(true);
                }
                else if(inserting.getType() == Material.GLASS_BOTTLE) {
                    plugin.getServer().getScheduler().runTask(plugin,
                            new MoveOneToSlotTask(src, inv, INPUT_SLOT, Material.GLASS_BOTTLE));
                    event.setCancelled(true);
                }
            }
        }
    }
    /*
    private static int removeFrom(Inventory inv, Material mat, int quantity) {
        int index = inv.first(mat);
        ItemStack newStack = inv.getItem(index);
        int amount = newStack.getAmount();
        if(quantity > amount) quantity = amount;
        
        newStack.setAmount(amount-1);
        inv.setItem(index, newStack);
        return quantity;
    }*/
    
    protected class MoveOneToSlotTask implements Runnable {
        private Inventory source, dest;
        private int slot;
        private Material material;
        
        public MoveOneToSlotTask(Inventory source, Inventory dest, int slot, Material material) {
            this.source = source;
            this.dest = dest;
            this.slot = slot;
            this.material = material;
        }

        public void run() {
            moveOneToSlot(source, dest, slot, material);
        }
    }
    
     
    private static void moveOneToSlot(Inventory source, Inventory dest, int slot, Material mat) {
        ItemStack destStack = dest.getItem(slot);
        // don't move anything if destination slot is full or occupied by another item
        // Stop at 63 items to allow hoppers to still attempt to place items into inventory
        if(destStack != null && (destStack.getType() != mat || destStack.getAmount() > 62)) return;
        
        final int srcIndex = source.first(mat);
        if(srcIndex == -1) return; // can't find material in source
        
        // get ItemStack from source
        final ItemStack srcStack = source.getItem(srcIndex);
        
        if(srcStack.getAmount() == 1) source.setItem(srcIndex, null);
        else srcStack.setAmount(srcStack.getAmount()-1);
        //source.setItem(srcIndex, srcStack);
        
        if(destStack == null) dest.setItem(slot, new ItemStack(mat, 1));
        else destStack.setAmount(destStack.getAmount()+1);
        //dest.setItem(slot, destStack)
    }
    
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        
        if(event.getAction().equals(Action.PHYSICAL)) {
            // Player stood on something, find out what
            Block interacted = event.getClickedBlock();
            
            if(interacted.getType() == Material.GOLD_PLATE) {
                // Player stood on gold pressure plate. Check what's beneath
                Block beneath = interacted.getRelative(BlockFace.DOWN, 1);
                
                if(beneath.getType() == Material.FURNACE || beneath.getType() == Material.BURNING_FURNACE) {
                    // The gold pressure plate is on top of a furnace. Try and store XP
                    stashXP(player, beneath);
                }
                
            }
        }
    }
    
    private void stashXP(Player player, Block furnace) {
        FurnaceInventory inv = ((Furnace) furnace.getState()).getInventory();
        
        ItemStack input = inv.getSmelting();
        ItemStack fuel = inv.getFuel();
        ItemStack output = inv.getResult();
        
        // Check that glass bottles are provided
        if(input != null && input.getType() == Material.GLASS_BOTTLE &&
                // and the correct fuel is provided
                fuel != null && fuel.getType() == fuelMaterial &&
                // and output is either empty or a non-full stack of xp bottles
                (output == null || output.getType() == Material.AIR ||
                (output.getType() == Material.EXP_BOTTLE && output.getAmount() < 64)
            )) {

            int xp = StashYourXP.getExperience(player);
            if(xp < expPerBottle) return;
            
            // Update player's experience value
            StashYourXP.setExperience(player, xp - expPerBottle);
            
            // go "ding"
            player.getWorld().playSound(player.getLocation(), Sound.ORB_PICKUP, 0.3f,
                    (new Random().nextFloat()*0.75f) + 0.5f);
            
            // particles
            BlockFace facing = ((FurnaceAndDispenser)inv.getHolder().getData()).getFacing();
            for(int i = 0; i < 3; i++) {
                player.getWorld().playEffect(furnace.getLocation().add(getOffsetForFacing(facing).add(STUPID_OFFSET)),
                        Effect.HAPPY_VILLAGER, 0);
            }
            
            int amount;
            if((amount = input.getAmount()) == 1) inv.setSmelting(null);
            input.setAmount(amount - 1);
            
            if((amount = fuel.getAmount()) == 1) inv.setFuel(null);
            else fuel.setAmount (amount - 1);
            
            if(output == null || output.getType() != Material.EXP_BOTTLE)
                inv.setResult(new ItemStack(Material.EXP_BOTTLE, 1));
            else output.setAmount(output.getAmount() + 1);
        }
        //else: Invalid materials in furnace, do nothing
    }
    
    private Vector getOffsetForFacing(BlockFace face) {
        Random random = new Random();
        switch(face) {
        case NORTH:
            // distribute particles along x
            return new Vector(0.1+random.nextFloat()*0.8, random.nextFloat()*0.4, -0.1);
        case WEST:
            // distribute particles along z
            return new Vector(-0.1, random.nextFloat()*0.4, 0.1+random.nextFloat()*0.8);
        case SOUTH:
            // add 1 to y and distribute particles along x
            return new Vector(0.1+random.nextFloat()*0.8, random.nextFloat()*0.4, 1.1);
        case EAST:
            // add 1 to x and distribute particles along z
            return new Vector(1.1, random.nextFloat()*0.4, 0.1+random.nextFloat()*0.8);
        default:
            return new Vector(0, 0, 0);
        }
    }

    /**
     * Allows players to place Redstone Dust into the fuel slot of a Furnace
     * which is set up as an Experience Collector.
     * @param event The event object.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        ItemStack slot = event.getCurrentItem();
        ItemStack cursor = player.getItemOnCursor();
        Inventory inv = event.getInventory();
        
        /* Prerequisites:
         * - Inventory is a furnace
         * - Player is clicking the fuel slot
         * - The player has redstone dust on their cursor
         */
        if(inv instanceof FurnaceInventory &&
                event.getRawSlot() == 1 &&
                player.getItemOnCursor().getType() == fuelMaterial) {
            
            FurnaceInventory furnace = (FurnaceInventory) event.getInventory();
            
            // The furnace also needs to have a gold pressure plate on top
            if(furnace.getHolder().getBlock().getRelative(BlockFace.UP, 1).getType() == Material.GOLD_PLATE) {
                
                // Move the item stack into the slot and clear the player's cursor
                if(slot == null || slot.getType() == Material.AIR) {
                    furnace.setFuel(cursor);
                    player.setItemOnCursor(null);
                }
                else if(slot.getType() == fuelMaterial) {
                    int total = slot.getAmount() + cursor.getAmount();
                    if(total > 64) cursor.setAmount(total % 64);
                    else player.setItemOnCursor(null);
                    slot.setAmount(total>64 ? 64 : total);
                }
                else return;
                
                // Counterintuitively, we must deny the event so that Minecraft doesn't
                // prevent the fuel item from going in (as it would normally)
                event.setResult(Result.DENY);
            }
        }
        /*
        else {
            if(!(event.getInventory() instanceof FurnaceInventory))
                player.sendMessage("Problem: inventory isn't a furnace");
            else if (event.getRawSlot() != 1)
                player.sendMessage(MessageFormat.format("Problem: Expected slot 1, got {0}", event.getRawSlot()));
            else if(event.getCurrentItem() != null) {
                player.sendMessage(MessageFormat.format("Problem: Expected empty slot, got {0}", event.getCurrentItem()));
            }
            else if(player.getItemOnCursor().getType() != Material.REDSTONE) {
                player.sendMessage(MessageFormat.format("Problem: Expected REDSTONE, got {0}", player.getItemOnCursor().getType()));
            }
            else player.sendMessage("Unknown problem");
        }
        */
            
    }
        
}
