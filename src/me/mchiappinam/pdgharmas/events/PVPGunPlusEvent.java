package me.mchiappinam.pdgharmas.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PVPGunPlusEvent extends Event
  implements Cancellable
{
  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;

  public HandlerList getHandlers()
  {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public boolean isCancelled()
  {
    return this.cancelled;
  }

  public void setCancelled(boolean arg0)
  {
    this.cancelled = arg0;
  }
}