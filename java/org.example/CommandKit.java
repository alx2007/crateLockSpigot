package org.example;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.example.Main;

public class CommandKit implements CommandExecutor {
    Player player;
    ChestLocker chestLocker;

    CommandKit (ChestLocker chestLocker) {
        this.chestLocker = chestLocker;
    }

    // This method is called when somebody uses the command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            player = (Player) sender;

            // KIT
            if (command.getName().equalsIgnoreCase("kit")) {
                ItemStack diamonds = new ItemStack(Material.DIAMOND, 64);
                player.getInventory().addItem(diamonds);
            }

            // GETTARGETED
            if (command.getName().equalsIgnoreCase("gettargeted")) {
                String material = this.getTargetedBlock().getType().name();

                Bukkit.broadcastMessage(material);
            }

            // LOCK
            if (command.getName().equalsIgnoreCase("lock")) {
                Block targetedBlock = this.getTargetedBlock();

                // Runs only if targeted block is a crate
                if (Main.cratesContain(targetedBlock.getType())) {
                    Location location = targetedBlock.getLocation();

                    double x = location.getX();
                    double y = location.getY();
                    double z = location.getZ();

                    String playerSaveLocation = String.format("chests.%.0fa%.0fa%.0f.player", x, y, z);

                    String playerID = player.getUniqueId().toString();
                    String ownerID = Main.getCustomConfig().getString(playerSaveLocation);

                    // Only allows lock status to be changed if player owns crate or crate is not registered
                    if (playerID.equals(ownerID) || ownerID == null) {
                        Main.getCustomConfig().set(playerSaveLocation, player.getUniqueId().toString());

                        String lockedSaveLocation = String.format("chests.%.0fa%.0fa%.0f.locked", x, y, z);

                        // Changes chest lock status based on argument provided in command
                        if (args[0].equals("true")) {
                            Main.getCustomConfig().set(lockedSaveLocation, "true");
                            Main.save();

                            player.sendMessage(String.format("Crate locked at coordinates %.0f, %.0f, %.0f!", x, y, z));
                        } else if (args[0].equals("false")) {
                            Main.getCustomConfig().set(lockedSaveLocation, "false");
                            Main.save();

                            player.sendMessage(String.format("Crate unlocked at coordinates %.0f, %.0f, %.0f!", x, y, z));
                        } else {
                            return false;
                        }

                    // If player is not owner, send a message telling them so
                    } else {
                        player.sendMessage("You do not own this crate!");
                    }

                // If block cannot be locked, then tell player
                } else {
                    player.sendMessage("Could not execute lock command on non-crate block");
                }
            }
        }

        return true;
    }

    private Block getTargetedBlock() {
        Location location = player.getEyeLocation();
        Vector vector = location.getDirection().normalize();

        for (int i = 0; i <= 5; i++) {
            location.add(vector);

            if (location.getBlock().getType() != Material.AIR) {
                break;
            }
        }

        Block block = location.getBlock();

        return block;
    }
}
