package me.mchiappinam.pdgharmas.gun;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.mchiappinam.pdgharmas.InventoryHelper;
import me.mchiappinam.pdgharmas.PVPGunPlus;
import me.mchiappinam.pdgharmas.PermissionInterface;

public class GunPlayer
{
  private int ticks;
  private Player controller;
  private ItemStack lastHeldItem;
  private ArrayList<Gun> guns;
  private Gun currentlyFiring;
  public boolean enabled = true;

  public GunPlayer(PVPGunPlus plugin, Player player) {
    this.controller = player;
    this.guns = plugin.getLoadedGuns();
    for (int i = 0; i < this.guns.size(); i++)
      ((Gun)this.guns.get(i)).owner = this;
  }

  public boolean isAimedIn()
  {
    if (this.controller == null)
      return false;
    if (!this.controller.isOnline()) {
      return false;
    }
    if (this.controller.hasPotionEffect(PotionEffectType.SLOW)) {
      return true;
    }
    return false;
  }

  public boolean onClick(String clickType) {
    if (!this.enabled) {
      return false;
    }
    Gun holding = null;
    ItemStack hand = this.controller.getItemInHand();
    if (hand != null) {
      ArrayList tempgun = getGunsByType(hand);
      ArrayList canFire = new ArrayList();
      for (int i = 0; i < tempgun.size(); i++) {
        if ((PermissionInterface.checkPermission(this.controller, ((Gun)tempgun.get(i)).node)) || (!((Gun)tempgun.get(i)).needsPermission)) {
          canFire.add((Gun)tempgun.get(i));
        }
      }
      if ((tempgun.size() > canFire.size()) && (canFire.size() == 0)) {
        if ((((Gun)tempgun.get(0)).permissionMessage != null) && (((Gun)tempgun.get(0)).permissionMessage.length() > 0))
          this.controller.sendMessage(((Gun)tempgun.get(0)).permissionMessage);
        return false;
      }
      tempgun.clear();
      for (int i = 0; i < canFire.size(); i++) {
        Gun check = (Gun)canFire.get(i);
        byte gunDat = check.getGunTypeByte();
        byte itmDat = hand.getData().getData();

        if ((gunDat == itmDat) || (check.ignoreItemData))
          holding = check;
      }
      canFire.clear();
    }
    if (holding != null) {
      if (((holding.canClickRight) || (holding.canAimRight())) && (clickType.equals("right"))) {
        if (!holding.canAimRight()) {
          holding.heldDownTicks += 1;
          holding.lastFired = 0;
          if (this.currentlyFiring == null)
            fireGun(holding);
        }
        else {
          checkAim();
        }
      } else if (((holding.canClickLeft) || (holding.canAimLeft())) && (clickType.equals("left"))) {
        if (!holding.canAimLeft()) {
          holding.heldDownTicks = 0;
          if (this.currentlyFiring == null)
            fireGun(holding);
        }
        else {
          checkAim();
        }
      }
    }
    return true;
  }

  public void checkAim() {
    if (isAimedIn())
      this.controller.removePotionEffect(PotionEffectType.SLOW);
    else
      this.controller.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 12000, 4));
  }

  private void fireGun(Gun gun)
  {
    if ((PermissionInterface.checkPermission(this.controller, gun.node)) || (!gun.needsPermission)) {
      if (gun.timer <= 0) {
        this.currentlyFiring = gun;
        gun.firing = true;
      }
    }
    else if ((gun.permissionMessage != null) && (gun.permissionMessage.length() > 0))
      this.controller.sendMessage(gun.permissionMessage);
  }

  public void tick()
  {
    this.ticks += 1;
    if (this.controller != null) {
      ItemStack hand = this.controller.getItemInHand();
      this.lastHeldItem = hand;
      if ((this.ticks % 10 == 0) && (hand != null)) {
        Gun g = PVPGunPlus.getPlugin().getGun(hand.getTypeId());
        if (g == null) {
          this.controller.removePotionEffect(PotionEffectType.SLOW);
        }
      }
      for (int i = this.guns.size() - 1; i >= 0; i--) {
        Gun g = (Gun)this.guns.get(i);
        if (g != null) {
          g.tick();

          if (this.controller.isDead()) {
            g.finishReloading();
          }

          if ((hand != null) && 
            (g.getGunType() == hand.getTypeId()) && 
            (isAimedIn()) && (!g.canAimLeft()) && (!g.canAimRight())) {
            this.controller.removePotionEffect(PotionEffectType.SLOW);
          }

          if ((this.currentlyFiring != null) && (g.timer <= 0) && (this.currentlyFiring.equals(g)))
            this.currentlyFiring = null;
        }
      }
    }
    renameGuns(this.controller);
  }

  public void renameGuns(Player p) {
    Inventory inv = p.getInventory();
    ItemStack[] items = inv.getContents();
    for (int i = 0; i < items.length; i++)
      if (items[i] != null) {
        String name = getGunName(items[i]);
        if ((name != null) && (name.length() > 0))
          setName(items[i], name);
      }
  }

  public ArrayList<Gun> getGunsByType(ItemStack item)
  {
    ArrayList ret = new ArrayList();
    for (int i = 0; i < this.guns.size(); i++) {
      if (((Gun)this.guns.get(i)).getGunMaterial().equals(item.getType())) {
        ret.add((Gun)this.guns.get(i));
      }
    }

    return ret;
  }

  public String getGunName(ItemStack item) {
    String ret = "";
    ArrayList tempgun = getGunsByType(item);
    int amtGun = tempgun.size();
    if (amtGun > 0) {
      for (int i = 0; i < tempgun.size(); i++) {
        if ((PermissionInterface.checkPermission(this.controller, ((Gun)tempgun.get(i)).node)) || (!((Gun)tempgun.get(i)).needsPermission)) {
          Gun current = (Gun)tempgun.get(i);
          if ((current.getGunMaterial() != null) && (current.getGunMaterial().getId() == item.getTypeId())) {
            byte gunDat = ((Gun)tempgun.get(i)).getGunTypeByte();
            byte itmDat = item.getData().getData();

            if ((gunDat == itmDat) || (((Gun)tempgun.get(i)).ignoreItemData)) {
              return getGunName(current);
            }
          }
        }
      }
    }
    return ret;
  }

  private String getGunName(Gun current) {
    String add = "";
    String refresh = "";
    if (current.hasClip) {
      int leftInClip = 0;
      int ammoLeft = 0;
      int maxInClip = current.maxClipSize;

      int currentAmmo = (int)Math.floor(InventoryHelper.amtItem(this.controller.getInventory(), current.getAmmoType(), current.getAmmoTypeByte()) / current.getAmmoAmtNeeded());
      ammoLeft = currentAmmo - maxInClip + current.roundsFired;
      if (ammoLeft < 0)
        ammoLeft = 0;
      leftInClip = currentAmmo - ammoLeft;
      add = "§r    §e« §b§l" + Integer.toString(leftInClip) + "§r §f§l│§r §b§l" + Integer.toString(ammoLeft) + "§r §e»";
      if (current.reloading) {
        int reloadSize = 4;
        double reloadFrac = (current.getReloadTime() - current.gunReloadTimer) / current.getReloadTime();
        int amt = (int)Math.round(reloadFrac * reloadSize);
        for (int ii = 0; ii < amt; ii++) {
          refresh = refresh + "▪";
        }
        for (int ii = 0; ii < reloadSize - amt; ii++) {
          refresh = refresh + "▫";
        }

        add = ChatColor.RED + "    " + new StringBuffer(refresh).reverse() + " RECARREGANDO " + refresh;
      }
    }
    String name = current.getName();
    return name + add;
  }

  public ItemStack setName(ItemStack item, String name) {
    ItemMeta im = item.getItemMeta();
    im.setDisplayName(name);
    item.setItemMeta(im);

    return item;
  }

  public Player getPlayer() {
    return this.controller;
  }

  public void unload() {
    this.controller = null;
    this.currentlyFiring = null;
    for (int i = 0; i < this.guns.size(); i++)
      ((Gun)this.guns.get(i)).clear();
  }

  public void reloadAllGuns()
  {
    for (int i = this.guns.size() - 1; i >= 0; i--) {
      Gun g = (Gun)this.guns.get(i);
      if (g != null) {
        g.reloadGun();
        g.finishReloading();
      }
    }
  }

  public boolean checkAmmo(Gun gun, int amount) {
    return InventoryHelper.amtItem(this.controller.getInventory(), gun.getAmmoType(), gun.getAmmoTypeByte()) >= amount;
  }

  public void removeAmmo(Gun gun, int amount) {
    if (amount == 0)
      return;
    InventoryHelper.removeItem(this.controller.getInventory(), gun.getAmmoType(), gun.getAmmoTypeByte(), amount);
  }

  public ItemStack getLastItemHeld() {
    return this.lastHeldItem;
  }

  public Gun getGun(int typeId) {
    for (int i = this.guns.size() - 1; i >= 0; i--) {
      Gun check = (Gun)this.guns.get(i);
      if (check.getGunType() == typeId) {
        return check;
      }
    }
    return null;
  }
}