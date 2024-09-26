package me.appztr4ckt.marry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class MarryCommand implements CommandExecutor {

    private final Marry plugin;
    private final FileConfiguration messagesConfig;

    public MarryCommand(Marry plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.getMessagesConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(replacePlaceholders(messagesConfig.getString("messages.onlyPlayers"), null, null));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "antrag":
            case "apply":
                handleApply(player, args);
                break;
            case "ja":
            case "yes":
                handleAccept(player, args);
                break;
            case "scheiden":
            case "divorce":
                handleDivorce(player, args);
                break;
            case "teleport":
            case "tp":
                handleTeleport(player, args);
                break;
            case "kuss":
            case "kiss":
                handleKiss(player, args);
                break;
            case "kind":
            case "child":
                handleChild(player, args);
                break;
            case "status":
                handleStatus(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.help.header"), player, null));
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.help.apply"), player, null));
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.help.accept"), player, null));
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.help.divorce"), player, null));
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.help.teleport"), player, null));
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.help.kiss"), player, null));
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.help.child"), player, null));
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.help.status"), player, null));
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.help.footer"), player, null));
    }

    private void handleApply(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.usage"), player, null));
            return;
        }
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.playerNotFound"), player, null));
            return;
        }

        if (player.equals(target)) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.selfMarriage"), player, null));
            return;
        }

        if (plugin.getConfig().contains("Marrypartner." + player.getName())) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.alreadyMarried"), player, null));
            return;
        }

        if (plugin.getConfig().contains("Marrypartner." + target.getName())) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.targetMarried"), player, null));
            return;
        }

        if (plugin.getConfig().contains("Marryrequests." + player.getName() + "." + target.getName())) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.requestAlreadySent"), player, null));
            return;
        }

        plugin.getConfig().set("Marryrequests." + target.getName() + "." + player.getName(), true);
        plugin.saveConfig();
        target.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.requestReceived"), target, player.getName()));
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.requestSent"), player, target.getName()));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getConfig().contains("Marryrequests." + player.getName() + "." + target.getName())) {
                    if (!plugin.getConfig().contains("Marrypartner." + target.getName())) {
                        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.requestExpired"), player, target.getName()));
                        target.sendMessage(replacePlaceholders(messagesConfig.getString("messages.apply.requestExpired"), target, player.getName()));
                    }
                    plugin.getConfig().set("Marryrequests." + player.getName() + "." + target.getName(), null);
                    plugin.saveConfig();
                }
            }
        }.runTaskLater(plugin, 20 * 15); // 15 seconds
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.accept.usage"), player, null));
            return;
        }
        String requesterName = args[1];
        Player requester = Bukkit.getPlayer(requesterName);

        if (requester == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.accept.requesterNotFound"), player, null));
            return;
        }

        if (plugin.getConfig().contains("Marrypartner." + player.getName())) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.accept.alreadyMarried"), player, null));
            return;
        }

        if (plugin.getConfig().contains("Marryrequests." + requester.getName() + "." + player.getName())) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.accept.noRequest"), player, null));
            return;
        }

        plugin.getConfig().set("Marrypartner." + player.getName(), requester.getName());
        plugin.getConfig().set("Marrypartner." + requester.getName(), player.getName());
        plugin.getConfig().set("Marryrequests." + requester.getName() + "." + player.getName(), null);
        plugin.saveConfig();
        Bukkit.broadcastMessage(replacePlaceholders(messagesConfig.getString("messages.accept.married"), player, requester.getName()));
    }

    private void handleDivorce(Player player, String[] args) {
        String partnerName = plugin.getConfig().getString("Marrypartner." + player.getName());

        if (partnerName == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.divorce.notMarried"), player, null));
            return;
        }

        Player partner = Bukkit.getPlayer(partnerName);
        if (partner != null) {
            partner.sendMessage(replacePlaceholders(messagesConfig.getString("messages.divorce.partnerLeft"), partner, player.getName()));
        }

        plugin.getConfig().set("Marrypartner." + player.getName(), null);
        plugin.getConfig().set("Marrypartner." + partnerName, null);
        plugin.saveConfig();
        Bukkit.broadcastMessage(replacePlaceholders(messagesConfig.getString("messages.divorce.divorced"), player, null));
    }

    private void handleTeleport(Player player, String[] args) {
        String partnerName = plugin.getConfig().getString("Marrypartner." + player.getName());

        if (partnerName == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.teleport.notMarried"), player, null));
            return;
        }

        Player partner = Bukkit.getPlayer(partnerName);

        if (partner == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.teleport.partnerOffline"), player, null));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && partner.isOnline()) {
                    player.teleport(partner);
                    player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.teleport.success"), player, null));
                } else {
                    player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.teleport.failed"), player, null));
                }
            }
        }.runTaskLater(plugin, 20 * 3); // 3 seconds delay
    }

    private void handleKiss(Player player, String[] args) {
        String partnerName = plugin.getConfig().getString("Marrypartner." + player.getName());

        if (partnerName == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.kiss.notMarried"), player, null));
            return;
        }

        Player partner = Bukkit.getPlayer(partnerName);

        if (partner == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.kiss.partnerOffline"), player, null));
            return;
        }

        String messageToPartner = replacePlaceholders(messagesConfig.getString("messages.kiss.received"), partner, player.getName());
        String messageToPlayer = replacePlaceholders(messagesConfig.getString("messages.kiss.sent"), player, partnerName);

        partner.sendMessage(messageToPartner);
        player.sendMessage(messageToPlayer);

        partner.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.LEGACY_RED_ROSE, 1));
        player.getWorld().spawnParticle(org.bukkit.Particle.HEART, partner.getLocation(), 10);
        player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation(), 10);

        if (Math.random() < 0.02) {
            Bukkit.broadcastMessage(replacePlaceholders(messagesConfig.getString("messages.kiss.broadcast"), player, partnerName));
        }
    }

    private void handleChild(Player player, String[] args) {
        String partnerName = plugin.getConfig().getString("Marrypartner." + player.getName());

        if (partnerName == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.child.notMarried"), player, null));
            return;
        }

        Player partner = Bukkit.getPlayer(partnerName);

        if (partner == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.child.partnerOffline"), player, null));
            return;
        }

        if (plugin.getConfig().contains("Children." + player.getName()) || plugin.getConfig().contains("Children." + partnerName)) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.child.alreadyHasChildren"), player, null));
            return;
        }

        plugin.getConfig().set("Children." + player.getName(), "SomeChild");
        plugin.getConfig().set("Children." + partnerName, "SomeChild");
        plugin.saveConfig();
        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.child.success"), player, null));
        partner.sendMessage(replacePlaceholders(messagesConfig.getString("messages.child.success"), partner, null));
    }

    private void handleStatus(Player player, String[] args) {
        String partnerName = plugin.getConfig().getString("Marrypartner." + player.getName());

        if (partnerName == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.status.notMarried"), player, null));
            return;
        }

        Player partner = Bukkit.getPlayer(partnerName);

        if (partner == null) {
            player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.status.partnerOffline"), player, null));
            return;
        }

        player.sendMessage(replacePlaceholders(messagesConfig.getString("messages.status.currentStatus"), player, partnerName));
    }

    private String replacePlaceholders(String message, Player player, String partnerName) {
        String prefix = messagesConfig.getString("messages.prefix");
        message = message.replace("{prefix}", prefix);
        if (player != null) {
            message = message.replace("{player}", player.getName());
        }
        if (partnerName != null) {
            message = message.replace("{partner}", partnerName);
        }
        return message;
    }
    //public String replace(String text, String var)
}