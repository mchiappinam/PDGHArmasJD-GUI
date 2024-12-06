package me.mchiappinam.pdgharmas.listeners;

import me.mchiappinam.pdgharmas.PVPGunPlus;
import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PluginPlayerListener
  implements Listener
{
  private PVPGunPlus plugin;

  public PluginPlayerListener(PVPGunPlus plugin)
  {
    this.plugin = plugin;
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerJoin(PlayerJoinEvent event) {
    this.plugin.onJoin(event.getPlayer());
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerQuit(PlayerQuitEvent event) {
    this.plugin.onQuit(event.getPlayer());
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerKick(PlayerKickEvent event) {
    this.plugin.onQuit(event.getPlayer());
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Item dropped = event.getItemDrop();
    Player dropper = event.getPlayer();
    GunPlayer gp = this.plugin.getGunPlayer(dropper);
    if (gp != null) {
      ItemStack lastHold = gp.getLastItemHeld();
      if (lastHold != null) {
        Gun gun = gp.getGun(dropped.getItemStack().getTypeId());
        if ((gun != null) && (lastHold.equals(dropped.getItemStack())) && (gun.hasClip) && (gun.changed) && (gun.reloadGunOnDrop)) {
          gun.reloadGun();
      	event.setCancelled(true);
        }else if ((gun != null) && (lastHold.equals(dropped.getItemStack())) && (gun.hasClip) && (!gun.changed) && (gun.reloadGunOnDrop)) {
          	event.getPlayer().sendMessage("§bVocê consegue dropar a arma com o comando /arma drop");
          	event.getPlayer().sendMessage("§7(pode ser usado em combate)");
        	event.setCancelled(true);
            }
      }
    }
  }

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onPlayerInteract(PlayerInteractEvent event) {
    Action action = event.getAction();
    Player player = event.getPlayer();
    ItemStack itm1 = player.getItemInHand();
    if ((itm1 != null) && (
      (action.equals(Action.LEFT_CLICK_AIR)) || (action.equals(Action.LEFT_CLICK_BLOCK)) || (action.equals(Action.RIGHT_CLICK_AIR)) || (action.equals(Action.RIGHT_CLICK_BLOCK)))) {
      String clickType = "left";
      if ((action.equals(Action.RIGHT_CLICK_AIR)) || (action.equals(Action.RIGHT_CLICK_BLOCK)))
        clickType = "right";
      GunPlayer gp = this.plugin.getGunPlayer(player);
      if (gp != null)
        gp.onClick(clickType);
    }
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
  {
    Player p = event.getPlayer();
    String[] split = event.getMessage().split(" ");
    split[0] = split[0].substring(1);
    String label = split[0];
    String[] args = new String[split.length - 1];
    for (int i = 1; i < split.length; i++) {
      args[(i - 1)] = split[i];
    }
    try
    {
      if ((label.equalsIgnoreCase("arma")) && (args[0].equals("drop")) && (args.length==1)) {
          Gun g = plugin.getGun(p.getItemInHand().getTypeId());
          if(g != null) {
        	  if(p.getItemInHand().getAmount() > 1) {
        		  p.sendMessage("§bVocê dropou uma unidade da arma com sucesso!");
        		  p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(p.getItemInHand().getType(), 1));
        		  p.setItemInHand(new ItemStack(p.getItemInHand().getType(), (p.getItemInHand().getAmount() - 1)));
        	  }else {
        		  p.sendMessage("§bVocê dropou a arma!");
        		  p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(p.getItemInHand().getType(), 1));
        		  p.setItemInHand(null);
        	  }
          }else{
    		  p.sendMessage("§cIsso não é uma arma!");
          }
          event.setCancelled(true);
          return;
      }
      if ((label.equalsIgnoreCase("arma"))) {
        p.sendMessage("§3§lPDGH Armas - Comandos:");
        p.sendMessage("§2/arma drop -§a- Dropa a arma que está em sua mão.");
        event.setCancelled(true);
      }
    }
    catch (Exception localException)
    {
    }
  }
}