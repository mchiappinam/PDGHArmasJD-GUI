package me.mchiappinam.pdgharmas.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;

public class PVPGunPlusGunDamageEntityEvent extends PVPGunPlusEvent
{
  private Gun gun;
  private GunPlayer shooter;
  private Entity shot;
  private boolean isHeadshot;
  private int damage;
  private EntityDamageByEntityEvent event;

  public PVPGunPlusGunDamageEntityEvent(EntityDamageByEntityEvent event, GunPlayer shooter, Gun gun, Entity shot, boolean headshot)
  {
    this.gun = gun;
    this.shooter = shooter;
    this.shot = shot;
    this.isHeadshot = headshot;
    this.damage = gun.getGunDamage();
  }

  public EntityDamageByEntityEvent getEntityDamageEntityEvent() {
    return this.event;
  }

  public boolean isHeadshot() {
    return this.isHeadshot;
  }

  public void setHeadshot(boolean b) {
    this.isHeadshot = b;
  }

  public GunPlayer getShooter() {
    return this.shooter;
  }

  public Entity getEntityDamaged() {
    return this.shot;
  }

  public Player getKillerAsPlayer() {
    return this.shooter.getPlayer();
  }

  public Gun getGun() {
    return this.gun;
  }

  public int getDamage() {
    return this.damage;
  }
}