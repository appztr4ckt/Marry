package me.appztr4ckt.marry;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class Marry extends JavaPlugin {

    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messagesConfig = getConfig();
        getCommand("marry").setExecutor(new MarryCommand(this));
        getServer().getPluginManager().registerEvents(new MarryListener(this), this);
    }

    @Override
    public void onDisable() {
        // Optional: Save any necessary data here
    }
    public Player getPartner(Player player){
        String partnerName = getConfig().getString("Marrypartner." + player.getName());
        if (partnerName == null){return null;}return getServer().getPlayer(partnerName);
    }
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    public boolean isPlayerCallable(Player player){return player != null;}
}