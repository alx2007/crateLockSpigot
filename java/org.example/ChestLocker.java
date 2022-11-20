package org.example;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class ChestLocker implements Listener {
    @EventHandler
    public void crateOpen (InventoryInteractEvent event) {
//            Material clickedBlock = event.getClickedBlock().getType();
        Location location = event.getInventory().getLocation();

        // If block attempted to open is a crate
        if (Main.cratesContain(location.getBlock().getType())) {
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();

            // Tests if crate is locked, pretty self-explanatory
            String isLocked = Main.getCustomConfig().getString(String.format("chests.%.0fa%.0fa%.0f.locked", x, y, z));

            if (isLocked.equals("true")) {
                String ownerSaveLocation = String.format("chests.%.0fa%.0fa%.0f.player", x, y, z);

                String playerId = event.getWhoClicked().getUniqueId().toString();
                String ownerId = Main.getCustomConfig().getString(ownerSaveLocation);

                // Only allows opening if player owns crate
                if (!playerId.equals(ownerId)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void blockBreak (BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Again, checks if clicked block is a crate
        if (Main.cratesContain(event.getBlock().getType())) {
            Location location = event.getBlock().getLocation();

            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();

            // Only allows breaking if player owns crate
            String playerID = player.getUniqueId().toString();
            String ownerID = Main.getCustomConfig().getString(String.format("chests.%.0fa%.0fa%.0f.player", x, y, z));

            // If player owns crate, allow breaking and delete crate from yml file
            if (playerID.equals(ownerID)) {
                String crateLocation = String.format("chests.%.0fa%.0fa%.0f", x, y, z);
                Main.getCustomConfig().set(crateLocation, null);
                Main.save();

            // If player does not own crate because another player does, send a message and cancel event
            } else if (ownerID != null) {
                player.sendMessage("Only the owner of a crate can break it!");
                event.setCancelled(true);
            }

            // Otherwise, (i.e. if player does not own crate because it is not registered), allow breaking
        }
    }

    @EventHandler
    public void onExplosion (EntityExplodeEvent event) {
        // Making a list of all blocks affected by explosion
        List<Block> blocks = event.blockList();

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            Material material = block.getType();

            // Testing if block is a crate
            if (Main.cratesContain(material)) {
                Location location = block.getLocation();
                String isLocked = Main.getCustomConfig().getString(String.format("chests.%.0fa%.0fa%.0f.locked", location.getX(), location.getY(), location.getZ()));

                // If block is a registered, locked crate, block its explosion by removing it from affected blocks list
                try {
                    if (isLocked.equals("true")) {
                        event.blockList().remove(block);
                        Bukkit.broadcastMessage("Crate explosion blocked - registered locked crates cannot be exploded!");
                    }
                } catch (NullPointerException exception) {

                }
            }
        }
    }
}
