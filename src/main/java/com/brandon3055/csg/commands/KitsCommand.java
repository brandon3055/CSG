package com.brandon3055.csg.commands;

import com.brandon3055.csg.DataManager;
import com.brandon3055.csg.lib.PlayerSlot;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.brandon3055.csg.lib.PlayerSlot.EnumInvCategory.*;

/**
 * Created by brandon3055 on 1/2/21
 */
public class KitsCommand {
    private static Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
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
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(DataManager.kits.keySet(), builder))
                                        .executes(ctx -> give(ctx, StringArgumentType.getString(ctx, "kit-name"), ctx.getSource().getPlayerOrException()))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> give(ctx, StringArgumentType.getString(ctx, "kit-name"), EntityArgument.getPlayer(ctx, "target")))
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("kit-name", StringArgumentType.string())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(DataManager.kits.keySet(), builder))
                                        .executes(ctx -> remove(ctx, StringArgumentType.getString(ctx, "kit-name")))
                                )
                        )
                        .then(Commands.literal("list")
                                .executes(KitsCommand::list))
        );
    }

    private static int add(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        Map<PlayerSlot, CompoundTag> newKit = new HashMap<>();

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                newKit.put(new PlayerSlot(i, MAIN), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                newKit.put(new PlayerSlot(i, ARMOR), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack stack = player.getInventory().offhand.get(i);
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
            throw new CommandRuntimeException(Component.literal(e.getMessage() + " [See console for stacktrace]"));
        }
        ctx.getSource().sendSuccess(Component.literal("Your current inventory has been saved to kit " + name).withStyle(ChatFormatting.GREEN), false);

        return 0;
    }

    private static int give(CommandContext<CommandSourceStack> ctx, String name, ServerPlayer player) {
        if (!DataManager.kits.containsKey(name)) {
            throw new CommandRuntimeException(Component.literal("The specified kit does not exist!"));
        }

        DataManager.givePlayerKit(player, name);
        return 0;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx, String name) {
        if (!DataManager.kits.containsKey(name)) {
            throw new CommandRuntimeException(Component.literal("The specified kit does not exist!"));
        }

        DataManager.kits.remove(name);
        try {
            DataManager.saveConfig();
        }
        catch (IOException e) {
            LOGGER.error("Something when wrong while saving inventory!");
            e.printStackTrace();
            throw new CommandRuntimeException(Component.literal(e.getMessage() + " [See console for stacktrace]"));
        }
        ctx.getSource().sendSuccess(Component.literal("Kit removed successfully!").withStyle(ChatFormatting.GREEN), false);
        return 0;
    }


    private static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("### Kits ###").withStyle(ChatFormatting.GOLD));
        for (String name : DataManager.kits.keySet()) {
            ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal(name).withStyle(ChatFormatting.GREEN));
        }
        return 0;
    }
}