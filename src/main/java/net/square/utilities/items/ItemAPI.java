package net.square.utilities.items;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.*;

public class ItemAPI {

    public static ItemStack getItem(Material m, String name, int amount) {
        ItemStack is = new ItemStack(m);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        is.setItemMeta(im);
        is.setAmount(amount);
        return is;
    }

    public static ItemStack getItem(Material m, String name, int amount, List<String> lore) {
        ItemStack is = new ItemStack(m, amount);
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        im.setDisplayName(name);
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }


    public static ItemStack getItem(Material m, int meta) {
        ItemStack is = new ItemStack(m, 1, (short) meta);
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack getItem(Material m, String name, String textureUrl) {
        ItemStack is = new ItemStack(m, 1, (short) 3);
        ItemMeta im = is.getItemMeta();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        byte[] bytes = String.format("{textures:{SKIN:{url:\"%s\"}}}", textureUrl).getBytes();
        byte[] encodedBytes = Base64.getEncoder().encode(bytes);
        gameProfile.getProperties().put("textures", new Property("textures", new String(encodedBytes)));
        Field profileField;
        try {
            profileField = im.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(im, gameProfile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        im.setDisplayName(name);
        is.setItemMeta(im);
        return is;
    }
}