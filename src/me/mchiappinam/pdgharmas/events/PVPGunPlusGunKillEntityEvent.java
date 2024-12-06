package me.mchiappinam.pdgharmas.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;

public class PVPGunPlusGunKillEntityEvent extends PVPGunPlusEvent
{
  private Gun gun;
  private GunPlayer shooter;
  private Entity shot;

  public PVPGunPlusGunKillEntityEvent(GunPlayer shooter, Gun gun, Entity killed)
  {
    this.gun = gun;
    this.shooter = shooter;
    this.shot = killed;
  }

  public GunPlayer getKiller() {
    return this.shooter;
  }

  public Player getKillerAsPlayer() {
    return this.shooter.getPlayer();
  }

  public Entity getKilled() {
    return this.shot;
  }

  public Gun getGun() {
    return this.gun;
  }
}