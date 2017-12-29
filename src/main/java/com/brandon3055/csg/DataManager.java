package com.brandon3055.csg;

import com.brandon3055.csg.lib.PlayerSlot;
import com.brandon3055.csg.lib.StackReference;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class DataManager {

    private static File configFile;
    public static Map<PlayerSlot, StackReference> spawnInventory = null;
    public static Map<String, Map<PlayerSlot, StackReference>> kits = new LinkedHashMap<>();

    //region Init,Save,Load

    public static void initialize(File config) {
        File cManager = new File(config, "brandon3055/CSG");
        if (!cManager.exists()) {
            cManager.mkdirs();
        }

        configFile = new File(cManager, "Config.json");

        try {
            loadConfig();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() throws IOException {
        if (spawnInventory == null) {
            LogHelper.error("Could not save config because inventory array was null!");
            return;
        }

        JsonObject obj = new JsonObject();
        JsonObject inv = new JsonObject();

        spawnInventory.forEach((playerSlot, stackReference) -> inv.addProperty(playerSlot.toString(), stackReference.toString()));

        obj.add("inventory", inv);

        if (kits.size() > 0) {
            JsonObject kitsObj = new JsonObject();

            kits.forEach((name, kit) -> {
                JsonObject kitObj = new JsonObject();
                kit.forEach((playerSlot, stackReference) -> kitObj.addProperty(playerSlot.toString(), stackReference.toString()));
                kitsObj.add(name, kitObj);
            });

            obj.add("kits", kitsObj);
        }

        JsonWriter writer = new JsonWriter(new FileWriter(configFile));
        writer.setIndent("  ");
        Streams.write(obj, writer);
        writer.flush();
        IOUtils.closeQuietly(writer);
    }

    public static void loadConfig() throws IOException {
        if (!configFile.exists()) {
            spawnInventory = null;
            LogHelper.warn("Custom Spawn Inventory has not been configured yet! Canceling config load!");
            return;
        }

        JsonObject obj;
        JsonParser parser = new JsonParser();
        FileReader reader = new FileReader(configFile);
        JsonElement element = parser.parse(reader);
        IOUtils.closeQuietly(reader);

        if (!element.isJsonObject()) {
            LogHelper.warn("Detected invalid config json! Canceling config load!");
            return;
        }

        obj = element.getAsJsonObject();

        if (obj.has("inventory") && obj.get("inventory").isJsonObject()) {
            LogHelper.info("Reading starting inventory config");
            spawnInventory = new HashMap<>();
            JsonObject inv = obj.get("inventory").getAsJsonObject();
            inv.entrySet().forEach(entry -> {
                PlayerSlot slot = PlayerSlot.fromString(entry.getKey());
                StackReference stack = StackReference.fromString(entry.getValue().getAsJsonPrimitive().getAsString());
                spawnInventory.put(slot, stack);
            });
            LogHelper.info("Loaded " + spawnInventory.size() + " starting items.");
        }

        if (obj.has("kits") && obj.get("kits").isJsonObject()) {
            LogHelper.info("Reading kits from config");
            JsonObject kits = obj.get("kits").getAsJsonObject();
            kits.entrySet().forEach(entry -> {
                String name = entry.getKey();
                JsonObject items = entry.getValue().getAsJsonObject();
                Map<PlayerSlot, StackReference> kitMap = DataManager.kits.computeIfAbsent(name, s -> new HashMap<>());

                items.entrySet().forEach(kitEntry -> {
                    PlayerSlot slot = PlayerSlot.fromString(kitEntry.getKey());
                    StackReference stack = StackReference.fromString(kitEntry.getValue().getAsJsonPrimitive().getAsString());
                    kitMap.put(slot, stack);
                });

                LogHelper.info("Loaded " + kitMap.size() + " items for kit " + name);
            });
        }
    }

    //endregion

    public static void givePlayerStartGear(EntityPlayer player) {
        if (spawnInventory == null) {
            player.sendMessage(new TextComponentString("Custom Starting Gear has not been configured!").setStyle(new Style().setColor(TextFormatting.DARK_RED)));
            player.sendMessage(new TextComponentString("If you are an operator use /csg_config to get more info."));
            return;
        }

        player.inventory.clear();

        for (PlayerSlot slot : spawnInventory.keySet()) {
            ItemStack stack = spawnInventory.get(slot).createStack();
            if (stack == null) {
                player.sendMessage(new TextComponentString("[CSG] Something went wrong! Could not create stack - " + spawnInventory.get(slot).toString()));
                continue;
            }

            slot.setStackInSlot(player, stack);
        }
    }

    public static void givePlayerKit(EntityPlayer player, String kit) {
        if (!kits.containsKey(kit)) {
            player.sendMessage(new TextComponentString("The requested kit \"" + kit + "\" does not exist!").setStyle(new Style().setColor(TextFormatting.DARK_RED)));
            return;
        }

        Map<PlayerSlot, StackReference> kitItems = kits.get(kit);
        player.inventory.clear();

        for (PlayerSlot slot : kitItems.keySet()) {
            ItemStack stack = kitItems.get(slot).createStack();
            if (stack == null) {
                player.sendMessage(new TextComponentString("[CSG] Something went wrong! Could not create stack - " + kitItems.get(slot).toString()));
                continue;
            }

            slot.setStackInSlot(player, stack);
        }
    }
}
