package com.brandon3055.csg.commands;

import com.brandon3055.csg.DataManager;
import com.brandon3055.csg.lib.PlayerSlot;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.brandon3055.csg.lib.PlayerSlot.EnumInvCategory.*;

/**
 * Created by brandon3055 on 1/2/21
 */
public class KitsCommand {
    private static Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("csg_kits")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.literal("add")
                                .then(Commands.argument("kit-name", StringArgumentType.string())
                                        .executes(ctx -> add(ctx, StringArgumentType.getString(ctx, "kit-name")))
                                )
                        )
                        .then(Commands.literal("give")
                                .then(Commands.argument("kit-name", StringArgumentType.string())
                                        .suggests((context, builder) -> ISuggestionProvider.suggest(DataManager.kits.keySet(), builder))
                                        .executes(ctx -> give(ctx, StringArgumentType.getString(ctx, "kit-name"), ctx.getSource().getPlayerOrException()))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> give(ctx, StringArgumentType.getString(ctx, "kit-name"), EntityArgument.getPlayer(ctx, "target")))
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("kit-name", StringArgumentType.string())
                                        .suggests((context, builder) -> ISuggestionProvider.suggest(DataManager.kits.keySet(), builder))
                                        .executes(ctx -> remove(ctx, StringArgumentType.getString(ctx, "kit-name")))
                                )
                        )
                        .then(Commands.literal("list")
                                .executes(KitsCommand::list))
        );
    }

    private static int add(CommandContext<CommandSource> ctx, String name) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().getPlayerOrException();
        Map<PlayerSlot, CompoundNBT> newKit = new HashMap<>();

        for (int i = 0; i < player.inventory.items.size(); i++) {
            ItemStack stack = player.inventory.items.get(i);
            if (!stack.isEmpty()) {
                newKit.put(new PlayerSlot(i, MAIN), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.inventory.armor.size(); i++) {
            ItemStack stack = player.inventory.armor.get(i);
            if (!stack.isEmpty()) {
                newKit.put(new PlayerSlot(i, ARMOR), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.inventory.offhand.size(); i++) {
            ItemStack stack = player.inventory.offhand.get(i);
            if (!stack.isEmpty()) {
                newKit.put(new PlayerSlot(i, OFF_HAND), stack.serializeNBT());
            }
        }

        DataManager.kits.put(name, newKit);

        try {
            DataManager.saveConfig();
        }
        catch (IOException e) {
            LOGGER.error("Something when wrong while saving inventory!");
            e.printStackTrace();
            throw new CommandException(new StringTextComponent(e.getMessage() + " [See console for stacktrace]"));
        }
        ctx.getSource().sendSuccess(new StringTextComponent("Your current inventory has been saved to kit " + name).withStyle(TextFormatting.GREEN), false);

        return 0;
    }

    private static int give(CommandContext<CommandSource> ctx, String name, ServerPlayerEntity player) {
        if (!DataManager.kits.containsKey(name)) {
            throw new CommandException(new StringTextComponent("The specified kit does not exist!"));
        }

        DataManager.givePlayerKit(player, name);
        return 0;
    }

    private static int remove(CommandContext<CommandSource> ctx, String name) {
        if (!DataManager.kits.containsKey(name)) {
            throw new CommandException(new StringTextComponent("The specified kit does not exist!"));
        }

        DataManager.kits.remove(name);
        try {
            DataManager.saveConfig();
        }
        catch (IOException e) {
            LOGGER.error("Something when wrong while saving inventory!");
            e.printStackTrace();
            throw new CommandException(new StringTextComponent(e.getMessage() + " [See console for stacktrace]"));
        }
        ctx.getSource().sendSuccess(new StringTextComponent("Kit removed successfully!").withStyle(TextFormatting.GREEN), false);
        return 0;
    }


    private static int list(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ctx.getSource().getPlayerOrException().sendMessage(new StringTextComponent("### Kits ###").withStyle(TextFormatting.GOLD), Util.NIL_UUID);
        for (String name : DataManager.kits.keySet()) {
            ctx.getSource().getPlayerOrException().sendMessage(new StringTextComponent(name).withStyle(TextFormatting.GREEN), Util.NIL_UUID);
        }
        return 0;
    }
}