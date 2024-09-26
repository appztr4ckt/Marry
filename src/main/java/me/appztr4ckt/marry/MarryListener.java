package me.appztr4ckt.marry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class MarryListener implements Listener {

    private final Marry plugin;
    private final FileConfiguration messagesConfig;

    public MarryListener(Marry plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.getMessagesConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Player partner = plugin.getPartner(player);
        if (partner == null){return;}
        if (isPartnerCallable(player)) {
            String welcome = messagesConfig.getString("messages.welcome");
            if (welcome != null) {
                try {
                    welcome = welcome.replace("{partner}", partner.getName());
                    player.sendMessage(welcome);
                }
                catch (Exception ignored){
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Player partner = plugin.getPartner(player);
        if (partner == null){
            return;
        }
        if (isPartnerCallable(player)) {
            partner.sendMessage(messagesConfig.getString("messages.partnerDisconnected").replace("{player}", player.getName()));
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        Player partner = plugin.getPartner(player);
        if (isPartnerCallable(player)) {
            if (!event.getTo().getWorld().equals(partner.getWorld())) {
                player.sendMessage(messagesConfig.getString("messages.teleport.differentWorld").replace("{partner}", partner.getName()));
            }
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Player partner = plugin.getPartner(player);
        if (isPartnerCallable(player)) {
            Action action = event.getAction();
            // Handle any special interactions between married players if needed
        }
    }

    private boolean isPartnerCallable(Player player) {
        if (!hasPartner(player)){return false;}
        return plugin.isPlayerCallable(plugin.getPartner(player));
    }
    public boolean hasPartner(Player player){
        return plugin.getConfig().getString("Marrypartner." + player.getName()) != null;
    }

}