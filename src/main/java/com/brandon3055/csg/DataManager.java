package com.brandon3055.csg;

import com.brandon3055.csg.lib.PlayerSlot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class DataManager {
    private static Logger LOGGER = LogManager.getLogger();

    private static Path configFile;
    public static Map<PlayerSlot, CompoundTag> spawnInventory = null;
    public static Map<String, Map<PlayerSlot, CompoundTag>> kits = new LinkedHashMap<>();
    public static List<String> wipeBlacklist = new ArrayList<>();

    //region Init,Save,Load

    public static void initialize() {
        configFile = Paths.get("./config/brandon3055/CSG/Config.json");
        if (!configFile.toFile().getParentFile().exists()) {
            configFile.toFile().getParentFile().mkdirs();
        }

        try {
            loadConfig();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() throws IOException {
        if (spawnInventory == null) {
            LOGGER.error("Could not save config because inventory array was null!");
            return;
        }

        JsonObject obj = new JsonObject();
        JsonObject inv = new JsonObject();

        spawnInventory.forEach((playerSlot, stack) -> inv.addProperty(playerSlot.toString(), stack.toString()));

        obj.add("inventory", inv);

        if (kits.size() > 0) {
            JsonObject kitsObj = new JsonObject();

            kits.forEach((name, kit) -> {
                JsonObject kitObj = new JsonObject();
                kit.forEach((playerSlot, stack) -> kitObj.addProperty(playerSlot.toString(), stack.toString()));
                kitsObj.add(name, kitObj);
            });

            obj.add("kits", kitsObj);
        }

        JsonArray blacklist = new JsonArray();
        wipeBlacklist.forEach(blacklist::add);
        obj.add("wipeBlacklist", blacklist);

        JsonWriter writer = new JsonWriter(new FileWriter(configFile.toFile()));
        writer.setIndent("  ");
        Streams.write(obj, writer);
        writer.flush();
        IOUtils.closeQuietly(writer);
    }

    public static void loadConfig() throws IOException, CommandSyntaxException {
        if (!configFile.toFile().exists()) {
            spawnInventory = null;
            LOGGER.warn("Custom Spawn Inventory has not been configured yet! Canceling config load!");
            return;
        }

        JsonObject obj;
        JsonParser parser = new JsonParser();
        FileReader reader = new FileReader(configFile.toFile());
        JsonElement element = parser.parse(reader);
        IOUtils.closeQuietly(reader);

        if (!element.isJsonObject()) {
            LOGGER.warn("Detected invalid config json! Canceling config load!");
            return;
        }

        obj = element.getAsJsonObject();

        if (obj.has("inventory") && obj.get("inventory").isJsonObject()) {
            LOGGER.info("Reading starting inventory config");
            spawnInventory = new HashMap<>();
            JsonObject inv = obj.get("inventory").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : inv.entrySet()) {
                PlayerSlot slot = PlayerSlot.fromString(entry.getKey());
                CompoundTag stack = TagParser.parseTag(entry.getValue().getAsJsonPrimitive().getAsString());
                spawnInventory.put(slot, stack);
            }
            LOGGER.info("Loaded " + spawnInventory.size() + " starting items.");
        }

        if (obj.has("kits") && obj.get("kits").isJsonObject()) {
            LOGGER.info("Reading kits from config");
            JsonObject kits = obj.get("kits").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : kits.entrySet()) {
                String name = entry.getKey();
                JsonObject items = entry.getValue().getAsJsonObject();
                Map<PlayerSlot, CompoundTag> kitMap = DataManager.kits.computeIfAbsent(name, s -> new HashMap<>());
                for (Map.Entry<String, JsonElement> kitEntry : items.entrySet()) {
                    PlayerSlot slot = PlayerSlot.fromString(kitEntry.getKey());
                    CompoundTag stack = TagParser.parseTag(kitEntry.getValue().getAsJsonPrimitive().getAsString());
                    kitMap.put(slot, stack);
                }
                LOGGER.info("Loaded " + kitMap.size() + " items for kit " + name);
            }
        }

        if (obj.has("wipeBlacklist") && obj.get("wipeBlacklist").isJsonArray()) {
            wipeBlacklist.clear();
            obj.get("wipeBlacklist").getAsJsonArray().forEach(e -> wipeBlacklist.add(e.getAsString()));
        }
    }

    //endregion

    public static void givePlayerStartGear(Player player) {
        if (spawnInventory == null) {
            player.sendSystemMessage(Component.literal("Custom Starting Gear has not been configured!").withStyle(ChatFormatting.DARK_RED));
            player.sendSystemMessage(Component.literal("If you are an operator use /csg_config to get more info."));
            return;
        }

        if (wipeBlacklist.isEmpty()) {
            player.getInventory().clearContent();
        }
        else {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (!stack.isEmpty() && key != null && !wipeBlacklist.contains(key.getNamespace()) && !wipeBlacklist.contains(key.toString())) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }

        for (PlayerSlot slot : spawnInventory.keySet()) {
            ItemStack stack = ItemStack.of(spawnInventory.get(slot).copy());
            if (slot.getStackInSlot(player).isEmpty()){
                slot.setStackInSlot(player, stack);
            }else {
                ItemEntity entity = EntityType.ITEM.create(player.level());
                if (entity != null && !player.level().isClientSide) {
                    entity.setItem(stack);
                    entity.setNoPickUpDelay();
                    entity.setPos(player.getX(), player.getY(), player.getZ());
                    player.level().addFreshEntity(entity);
                }
            }
        }
    }

    public static void givePlayerKit(Player player, String kit) {
        if (!kits.containsKey(kit)) {
            player.sendSystemMessage(Component.literal("The requested kit \"" + kit + "\" does not exist!").withStyle(ChatFormatting.DARK_RED));
            return;
        }

        Map<PlayerSlot, CompoundTag> kitItems = kits.get(kit);
        player.getInventory().clearContent();

        for (PlayerSlot slot : kitItems.keySet()) {
            ItemStack stack = ItemStack.of(kitItems.get(slot).copy());
            slot.setStackInSlot(player, stack);
        }
    }
}
