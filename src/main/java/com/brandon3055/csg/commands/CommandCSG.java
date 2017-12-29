package com.brandon3055.csg.commands;

import com.brandon3055.csg.DataManager;
import com.brandon3055.csg.LogHelper;
import com.brandon3055.csg.lib.PlayerSlot;
import com.brandon3055.csg.lib.StackReference;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.brandon3055.csg.lib.PlayerSlot.EnumInvCategory.ARMOR;
import static com.brandon3055.csg.lib.PlayerSlot.EnumInvCategory.MAIN;
import static com.brandon3055.csg.lib.PlayerSlot.EnumInvCategory.OFF_HAND;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class CommandCSG extends CommandBase {

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getName() {
        return "csg_config";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/csg_config";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            help(sender);
            return;
        }
        if (args[0].equals("set")) {
            EntityPlayer player = getCommandSenderAsPlayer(sender);
            DataManager.spawnInventory = new HashMap<>();

            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                ItemStack stack = player.inventory.mainInventory.get(i);
                if (!stack.isEmpty()) {
                    DataManager.spawnInventory.put(new PlayerSlot(i, MAIN), new StackReference(stack));
                }
            }

            for (int i = 0; i < player.inventory.armorInventory.size(); i++) {
                ItemStack stack = player.inventory.armorInventory.get(i);
                if (!stack.isEmpty()) {
                    DataManager.spawnInventory.put(new PlayerSlot(i, ARMOR), new StackReference(stack));
                }
            }

            for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
                ItemStack stack = player.inventory.offHandInventory.get(i);
                if (!stack.isEmpty()) {
                    DataManager.spawnInventory.put(new PlayerSlot(i, OFF_HAND), new StackReference(stack));
                }
            }
            try {
                DataManager.saveConfig();
            }
            catch (IOException e) {
                LogHelper.error("Something when wrong while saving inventory!");
                e.printStackTrace();
                throw new CommandException(e.getMessage() + " [See console for stacktrace]");
            }
            sender.sendMessage(new TextComponentString("Your current inventory has been saved and will be given to players when they login for the first time!").setStyle(new Style().setColor(TextFormatting.GREEN)));
        }
        else if (args[0].equals("give")) {
            EntityPlayer player;
            if (args.length >= 2) {
                player = getPlayer(server, sender, args[1]);
            }
            else {
                 player = getCommandSenderAsPlayer(sender);
            }

            DataManager.givePlayerStartGear(player);
        }
        else {
            help(sender);
        }
    }

    private void help(ICommandSender sender) {
        sender.sendMessage(new TextComponentString("Usage:"));
        sender.sendMessage(new TextComponentString("/csg_config set"));
        sender.sendMessage(new TextComponentString("- Set the starting inventory to your current inventory.").setStyle(new Style().setColor(TextFormatting.GRAY)));
        sender.sendMessage(new TextComponentString("/csg_config give [player]"));
        sender.sendMessage(new TextComponentString("- Give the configured starting inventory to yourself or a specified player (Will delete all existing items first.)").setStyle(new Style().setColor(TextFormatting.GRAY)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "set", "give");
        }
        else if (args.length == 2 && args[0].equals("give")) {
            return getListOfStringsMatchingLastWord(args, server.getPlayerList().getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }
}
