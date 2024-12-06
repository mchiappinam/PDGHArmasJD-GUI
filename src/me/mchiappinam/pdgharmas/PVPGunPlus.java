package me.mchiappinam.pdgharmas;

import me.mchiappinam.pdgharmas.gun.Bullet;
import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;
import me.mchiappinam.pdgharmas.gun.WeaponReader;
import me.mchiappinam.pdgharmas.listeners.PluginEntityListener;
import me.mchiappinam.pdgharmas.listeners.PluginPlayerListener;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class PVPGunPlus extends JavaPlugin
{
  private PluginPlayerListener playerListener = new PluginPlayerListener(this);
  private PluginEntityListener entityListener = new PluginEntityListener(this);
  private ArrayList<Bullet> bullets = new ArrayList();
  private ArrayList<Gun> loadedGuns = new ArrayList();
  private ArrayList<GunPlayer> players = new ArrayList();
  private String pluginName = "PDGHArmas";
  public int UpdateTimer;
  public Random random;
  public static PVPGunPlus plugin;

  public void onDisable()
  {
    System.out.println(this.pluginName + " ativado");
    clearMemory(true);
  }

  public void onEnable() {
    System.out.println(this.pluginName + " desativado");
    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(this.playerListener, this);
    pm.registerEvents(this.entityListener, this);

    startup(true);
  }

  public void clearMemory(boolean init) {
    getServer().getScheduler().cancelTask(this.UpdateTimer);
    for (int i = this.bullets.size() - 1; i >= 0; i--) {
      ((Bullet)this.bullets.get(i)).destroy();
    }
    for (int i = this.players.size() - 1; i >= 0; i--) {
      ((GunPlayer)this.players.get(i)).unload();
    }
    if (init) {
      this.loadedGuns.clear();
    }
    this.bullets.clear();
    this.players.clear();
  }

  public void startup(boolean init) {
    this.UpdateTimer = getServer().getScheduler().scheduleSyncRepeatingTask(this, new UpdateTimer(), 20L, 1L);

    this.random = new Random();
    plugin = this;

    File dir = new File(getPluginFolder());
    if (!dir.exists()) {
      dir.mkdir();
    }

    File dir2 = new File(getPluginFolder() + "/guns");
    if (!dir2.exists()) {
      dir2.mkdir();
    }

    dir2 = new File(getPluginFolder() + "/projectile");
    if (!dir2.exists()) {
      dir2.mkdir();
    }

    if (init) {
      loadGuns();
      loadProjectile();
    }

    getOnlinePlayers();
  }

  private String getPluginFolder() {
    return getDataFolder().getAbsolutePath();
  }

  private void loadProjectile() {
    String path = getPluginFolder() + "/projectile";
    File dir = new File(path);
    String[] children = dir.list();
    if (children != null)
      for (int i = 0; i < children.length; i++) {
        String filename = children[i];
        WeaponReader f = new WeaponReader(this, new File(path + "/" + filename), "gun");
        if (f.loaded) {
          f.ret.node = ("pdgharmas." + filename.toLowerCase());
          this.loadedGuns.add(f.ret);
          f.ret.setIsThrowable(true);
          System.out.println("Arma arremessável carregada - " + f.ret.getName());
        } else {
          System.out.println("Falha ao carregar a arma arremessável - " + f.ret.getName());
        }
      }
  }

  private void loadGuns()
  {
    String path = getPluginFolder() + "/guns";
    File dir = new File(path);
    String[] children = dir.list();
    if (children != null)
      for (int i = 0; i < children.length; i++) {
        String filename = children[i];
        WeaponReader f = new WeaponReader(this, new File(path + "/" + filename), "gun");
        if (f.loaded) {
          f.ret.node = ("pdgharmas." + filename.toLowerCase());
          this.loadedGuns.add(f.ret);
          System.out.println("Arma carregada - " + f.ret.getName());
        } else {
          System.out.println("Falha ao carregar a arma " + f.ret.getName());
        }
      }
  }

  public void reload(boolean b)
  {
    clearMemory(b);
    startup(b);
  }

  public void reload() {
    reload(false);
  }

  public static void playEffect(Effect e, Location l, int num) {
    for (int i = 0; i < Bukkit.getServer().getOnlinePlayers().length; i++)
      Bukkit.getServer().getOnlinePlayers()[i].playEffect(l, e, num);
  }

  public void getOnlinePlayers() {
    Player[] plist = Bukkit.getOnlinePlayers();
    for (int i = 0; i < plist.length; i++) {
      GunPlayer g = new GunPlayer(this, plist[i]);
      this.players.add(g);
    }
  }

  public static PVPGunPlus getPlugin()
  {
    return plugin;
  }

  public GunPlayer getGunPlayer(Player player) {
    for (int i = this.players.size() - 1; i >= 0; i--) {
      if (((GunPlayer)this.players.get(i)).getPlayer().equals(player)) {
        return (GunPlayer)this.players.get(i);
      }
    }
    return null;
  }

  public Gun getGun(int typeId) {
    for (int i = this.loadedGuns.size() - 1; i >= 0; i--) {
      if ((((Gun)this.loadedGuns.get(i)).getGunMaterial() != null) && 
        (((Gun)this.loadedGuns.get(i)).getGunMaterial().getId() == typeId)) {
        return (Gun)this.loadedGuns.get(i);
      }
    }

    return null;
  }

  public Gun getGun(String gunName) {
    for (int i = this.loadedGuns.size() - 1; i >= 0; i--) {
      if ((((Gun)this.loadedGuns.get(i)).getName().toLowerCase().equals(gunName)) || (((Gun)this.loadedGuns.get(i)).getFilename().toLowerCase().equals(gunName))) {
        return (Gun)this.loadedGuns.get(i);
      }
    }
    return null;
  }

  public void onJoin(Player player) {
    if (getGunPlayer(player) == null) {
      GunPlayer gp = new GunPlayer(this, player);
      this.players.add(gp);
    }
  }

  public void onQuit(Player player) {
    for (int i = this.players.size() - 1; i >= 0; i--)
      if (((GunPlayer)this.players.get(i)).getPlayer().getName().equals(player.getName()))
        this.players.remove(i);
  }

  public ArrayList<Gun> getLoadedGuns()
  {
    ArrayList ret = new ArrayList();
    for (int i = this.loadedGuns.size() - 1; i >= 0; i--) {
      ret.add(((Gun)this.loadedGuns.get(i)).copy());
    }
    return ret;
  }

  public void removeBullet(Bullet bullet) {
    this.bullets.remove(bullet);
  }

  public void addBullet(Bullet bullet) {
    this.bullets.add(bullet);
  }

  public Bullet getBullet(Entity proj) {
    for (int i = this.bullets.size() - 1; i >= 0; i--) {
      if (((Bullet)this.bullets.get(i)).getProjectile().getEntityId() == proj.getEntityId()) {
        return (Bullet)this.bullets.get(i);
      }
    }
    return null;
  }

  public static Sound getSound(String gunSound) {
    String snd = gunSound.toUpperCase().replace(" ", "_");
    Sound sound = Sound.valueOf(snd);
    return sound;
  }

  public ArrayList<Gun> getGunsByType(ItemStack item) {
    ArrayList ret = new ArrayList();
    for (int i = 0; i < this.loadedGuns.size(); i++) {
      if (((Gun)this.loadedGuns.get(i)).getGunMaterial().equals(item.getType())) {
        ret.add((Gun)this.loadedGuns.get(i));
      }
    }
    return ret;
  }

  class UpdateTimer
    implements Runnable
  {
    public UpdateTimer()
    {
    }

    public void run()
    {
      for (int i = PVPGunPlus.this.players.size() - 1; i >= 0; i--) {
        GunPlayer gp = (GunPlayer)PVPGunPlus.this.players.get(i);
        if (gp != null) {
          gp.tick();
        }
      }
      for (int i = PVPGunPlus.this.bullets.size() - 1; i >= 0; i--) {
        Bullet t = (Bullet)PVPGunPlus.this.bullets.get(i);
        if (t != null)
          t.tick();
      }
    }
  }
}