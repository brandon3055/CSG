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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;
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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
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
                                            suggestions.addAll(ForgeRegistries.ITEMS.getKeys().stream().map(ResourceLocation::toString).toList());
                                            suggestions.addAll(ModList.get().getMods().stream().map(IModInfo::getModId).toList());
                                            return SharedSuggestionProvider.suggest(suggestions, builder);
                                        })
                                        .executes(context -> blackList(context, StringArgumentType.getString(context, "target")))
                                )
                        )
        );
    }

    private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        DataManager.spawnInventory = new HashMap<>();
        Player player = ctx.getSource().getPlayerOrException();

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                DataManager.spawnInventory.put(new PlayerSlot(i, MAIN), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                DataManager.spawnInventory.put(new PlayerSlot(i, ARMOR), stack.serializeNBT());
            }
        }

        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack stack = player.getInventory().offhand.get(i);
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
            throw new CommandRuntimeException(Component.literal(e.getMessage() + " [See console for stacktrace]"));
        }
        ctx.getSource().sendSuccess(Component.literal("Your current inventory has been saved and will be given to players when they login for the first time!").withStyle(ChatFormatting.GREEN), true);

        return 0;
    }

    private static int give(CommandContext<CommandSourceStack> ctx, Player player) {
        DataManager.givePlayerStartGear(player);
        return 0;
    }

    private static int blackList(CommandContext<CommandSourceStack> ctx, String target) {
        boolean isMod = !target.contains(":");
        if (DataManager.wipeBlacklist.contains(target)) {
            DataManager.wipeBlacklist.remove(target);
            ctx.getSource().sendSuccess(Component.literal("Removed " + (isMod ? "Mod" : "Item") + " " + target + " from wipe black list."), true);
            try {
                DataManager.saveConfig();
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new CommandRuntimeException(Component.literal(e.getMessage()));
            }
            return 0;
        }

        if (isMod) {
            if (!ModList.get().isLoaded(target)) {
                ctx.getSource().sendFailure(Component.literal("Could not find mod with id " + target));
                return 1;
            }
        }
        else if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(target))){
            ctx.getSource().sendFailure(Component.literal("Could not find item with id " + target));
            return 1;
        }

        DataManager.wipeBlacklist.add(target);
        ctx.getSource().sendSuccess(Component.literal("Added " + (isMod ? "Mod" : "Item") + " " + target + " to wipe black list."), true);
        try {
            DataManager.saveConfig();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new CommandRuntimeException(Component.literal(e.getMessage()));
        }
        return 0;
    }
}
