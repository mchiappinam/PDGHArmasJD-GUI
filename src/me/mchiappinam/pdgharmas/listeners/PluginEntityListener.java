package me.mchiappinam.pdgharmas.listeners;

import me.mchiappinam.pdgharmas.PVPGunPlus;
import me.mchiappinam.pdgharmas.events.PVPGunPlusBulletCollideEvent;
import me.mchiappinam.pdgharmas.events.PVPGunPlusGunDamageEntityEvent;
import me.mchiappinam.pdgharmas.events.PVPGunPlusGunKillEntityEvent;
import me.mchiappinam.pdgharmas.gun.Bullet;
import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

public class PluginEntityListener
  implements Listener
{
  PVPGunPlus plugin;

  public PluginEntityListener(PVPGunPlus plugin)
  {
    this.plugin = plugin;
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onProjectileHit(ProjectileHitEvent event) {
    Projectile check = event.getEntity();
    Bullet bullet = PVPGunPlus.getPlugin().getBullet(check);
    if (bullet != null) {
      bullet.onHit();
      bullet.setNextTickDestroy();
      Projectile p = event.getEntity();
      Block b = p.getLocation().getBlock();
      int id = b.getTypeId();
      for (double i = 0.2D; i < 4.0D; i += 0.2D) {
        if (id == 0) {
          b = p.getLocation().add(p.getVelocity().normalize().multiply(i)).getBlock();
          id = b.getTypeId();
        }
      }
      if (id > 0) {
        p.getLocation().getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, id);
      }

      PVPGunPlusBulletCollideEvent evv = new PVPGunPlusBulletCollideEvent(bullet.getShooter(), bullet.getGun(), b);
      this.plugin.getServer().getPluginManager().callEvent(evv);
    }
    event.getEntity().remove();
  }

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onEntityDeath(EntityDeathEvent event) {
    Entity dead = event.getEntity();
    if (dead.getLastDamageCause() != null) {
      EntityDamageEvent e = dead.getLastDamageCause();
      if ((e instanceof EntityDamageByEntityEvent)) {
        EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent)e;
        Entity damager = ede.getDamager();
        if ((damager instanceof Projectile)) {
          Projectile proj = (Projectile)damager;
          Bullet bullet = PVPGunPlus.getPlugin().getBullet(proj);
          if (bullet != null) {
            Gun used = bullet.getGun();
            GunPlayer shooter = bullet.getShooter();

            PVPGunPlusGunKillEntityEvent pvpgunkill = new PVPGunPlusGunKillEntityEvent(shooter, used, dead);
            this.plugin.getServer().getPluginManager().callEvent(pvpgunkill);
          }
        }
      }
    }
  }

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.isCancelled())
      return;
    Entity damager = event.getDamager();
    if ((event.getEntity() instanceof LivingEntity)) {
      LivingEntity hurt = (LivingEntity)event.getEntity();
      if ((damager instanceof Projectile)) {
        Projectile proj = (Projectile)damager;
        Bullet bullet = PVPGunPlus.getPlugin().getBullet(proj);
        if (bullet != null) {
          boolean headshot = false;
          if ((isNear(proj.getLocation(), hurt.getEyeLocation(), 0.26D)) && (bullet.getGun().canHeadShot())) {
            headshot = true;
          }
          PVPGunPlusGunDamageEntityEvent pvpgundmg = new PVPGunPlusGunDamageEntityEvent(event, bullet.getShooter(), bullet.getGun(), event.getEntity(), headshot);
          this.plugin.getServer().getPluginManager().callEvent(pvpgundmg);
          if (!pvpgundmg.isCancelled()) {
            double damage = pvpgundmg.getDamage();
            double mult = 1.0D;
            if (pvpgundmg.isHeadshot()) {
              PVPGunPlus.playEffect(Effect.ZOMBIE_DESTROY_DOOR, hurt.getLocation(), 3);
              mult = 2.0D;
            }
            hurt.setLastDamage(0);
            event.setDamage((int)Math.ceil(damage * mult));
            int armorPenetration = bullet.getGun().getArmorPenetration();
            if (armorPenetration > 0) {
              int health = hurt.getHealth();
              int newHealth = health - armorPenetration;
              if (newHealth < 0)
                newHealth = 0;
              if (newHealth > 20)
                newHealth = 20;
              hurt.setHealth(newHealth);
            }

            bullet.getGun().doKnockback(hurt, bullet.getVelocity());

            bullet.remove();
          } else {
            event.setCancelled(true);
          }
        }
      }
    }
  }

  private boolean isNear(Location location, Location eyeLocation, double d) {
    return Math.abs(location.getY() - eyeLocation.getY()) <= d;
  }
}