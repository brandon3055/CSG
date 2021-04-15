package com.brandon3055.csg.commands;

import com.brandon3055.csg.DataManager;
import com.brandon3055.csg.lib.PlayerSlot;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.brandon3055.csg.lib.PlayerSlot.EnumInvCategory.*;

/**
 * Created by brandon3055 on 1/2/21
 */
public class CSGCommand {
    private static Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("csg_config")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.literal("set")
                                .executes(CSGCommand::set))
                        .then(Commands.literal("give")
                                .executes(context -> give(context, context.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> give(context, EntityArgument.getPlayer(context, "target")))
                                )
                        )
                        .then(Commands.literal("item_deletion_blacklist")
                                .then(Commands.argument("target", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            List<String> suggestions = new ArrayList<>();
                                            suggestions.addAll(ForgeRegistries.ITEMS.getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()));
                                            suggestions.addAll(ModList.get().getMods().stream().map(ModInfo::getModId).collect(Collectors.toList()));
                                            return ISuggestionProvider.suggest(suggestions, builder);
                                        })
                                        .executes(context -> blackList(context, StringArgumentType.getString(context, "target")))
                                )
                        )
        );
    }

    private static int set(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        DataManager.spawnInventory = new HashMap<>();
        PlayerEntity player = ctx.getSource().getPlayerOrException();

        for (int i = 0; i < player.inventory.items.size(); i++) {
            ItemStack stack = player.inventory.items.get(i);
            if (!stack.isEmpty()) {
                DataManager.spawnInventory.put(new PlayerSlot(i, MAIN), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.inventory.armor.size(); i++) {
            ItemStack stack = player.inventory.armor.get(i);
            if (!stack.isEmpty()) {
                DataManager.spawnInventory.put(new PlayerSlot(i, ARMOR), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.inventory.offhand.size(); i++) {
            ItemStack stack = player.inventory.offhand.get(i);
            if (!stack.isEmpty()) {
                DataManager.spawnInventory.put(new PlayerSlot(i, OFF_HAND), stack.serializeNBT());
            }
        }
        try {
            DataManager.saveConfig();
        }
        catch (IOException e) {
            LOGGER.error("Something when wrong while saving inventory!");
            e.printStackTrace();
            throw new CommandException(new StringTextComponent(e.getMessage() + " [See console for stacktrace]"));
        }
        ctx.getSource().sendSuccess(new StringTextComponent("Your current inventory has been saved and will be given to players when they login for the first time!").withStyle(TextFormatting.GREEN), true);

        return 0;
    }

    private static int give(CommandContext<CommandSource> ctx, PlayerEntity player) {
        DataManager.givePlayerStartGear(player);
        return 0;
    }

    private static int blackList(CommandContext<CommandSource> ctx, String target) {
        boolean isMod = !target.contains(":");
        if (DataManager.wipeBlacklist.contains(target)) {
            DataManager.wipeBlacklist.remove(target);
            ctx.getSource().sendSuccess(new StringTextComponent("Removed " + (isMod ? "Mod" : "Item") + " " + target + " from wipe black list."), true);
            try {
                DataManager.saveConfig();
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new CommandException(new StringTextComponent(e.getMessage()));
            }
            return 0;
        }

        if (isMod) {
            if (!ModList.get().isLoaded(target)) {
                ctx.getSource().sendFailure(new StringTextComponent("Could not find mod with id " + target));
                return 1;
            }
        }
        else if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(target))){
            ctx.getSource().sendFailure(new StringTextComponent("Could not find item with id " + target));
            return 1;
        }

        DataManager.wipeBlacklist.add(target);
        ctx.getSource().sendSuccess(new StringTextComponent("Added " + (isMod ? "Mod" : "Item") + " " + target + " to wipe black list."), true);
        try {
            DataManager.saveConfig();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new CommandException(new StringTextComponent(e.getMessage()));
        }
        return 0;
    }
}
