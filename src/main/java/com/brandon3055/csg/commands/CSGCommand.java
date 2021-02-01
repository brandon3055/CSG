package com.brandon3055.csg.commands;

import com.brandon3055.csg.DataManager;
import com.brandon3055.csg.lib.PlayerSlot;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

import static com.brandon3055.csg.lib.PlayerSlot.EnumInvCategory.*;

/**
 * Created by brandon3055 on 1/2/21
 */
public class CSGCommand {
    private static Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("csg_config")
                        .requires(cs -> cs.hasPermissionLevel(2))
                        .then(Commands.literal("set")
                                .executes(CSGCommand::set))
                        .then(Commands.literal("give")
                                .executes(context -> give(context, context.getSource().asPlayer()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> give(context, EntityArgument.getPlayer(context, "target")))
                                )
                        )
        );
    }

    private static int set(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        DataManager.spawnInventory = new HashMap<>();
        PlayerEntity player = ctx.getSource().asPlayer();

        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (!stack.isEmpty()) {
                DataManager.spawnInventory.put(new PlayerSlot(i, MAIN), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.inventory.armorInventory.size(); i++) {
            ItemStack stack = player.inventory.armorInventory.get(i);
            if (!stack.isEmpty()) {
                DataManager.spawnInventory.put(new PlayerSlot(i, ARMOR), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
            ItemStack stack = player.inventory.offHandInventory.get(i);
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
        ctx.getSource().sendFeedback(new StringTextComponent("Your current inventory has been saved and will be given to players when they login for the first time!").mergeStyle(TextFormatting.GREEN), true);

        return 0;
    }

    private static int give(CommandContext<CommandSource> ctx, PlayerEntity player) {
        DataManager.givePlayerStartGear(player);
        return 0;
    }
}
