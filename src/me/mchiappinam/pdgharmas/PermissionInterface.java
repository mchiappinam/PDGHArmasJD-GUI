package me.mchiappinam.pdgharmas;

import org.bukkit.entity.Player;

public class PermissionInterface
{
  public static boolean checkPermission(Player player, String command)
  {
    try
    {
      if ((player.isOp()) || (player.hasPermission(command))) {
        return true;
      }
      return false;
    }
    catch (Exception localException)
    {
    }
    return true;
  }
}