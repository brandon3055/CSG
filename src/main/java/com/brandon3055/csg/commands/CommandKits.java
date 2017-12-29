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
import java.util.Map;

import static com.brandon3055.csg.lib.PlayerSlot.EnumInvCategory.*;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class CommandKits extends CommandBase {

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getName() {
        return "csg_kit";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/csg_kit";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            help(sender);
            return;
        }
        if (args[0].equals("add") && args.length == 2) {
            EntityPlayer player = getCommandSenderAsPlayer(sender);
            Map<PlayerSlot, StackReference> newKit = new HashMap<>();
            String name = args[1];

            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                ItemStack stack = player.inventory.mainInventory.get(i);
                if (!stack.isEmpty()) {
                    newKit.put(new PlayerSlot(i, MAIN), new StackReference(stack));
                }
            }

            for (int i = 0; i < player.inventory.armorInventory.size(); i++) {
                ItemStack stack = player.inventory.armorInventory.get(i);
                if (!stack.isEmpty()) {
                    newKit.put(new PlayerSlot(i, ARMOR), new StackReference(stack));
                }
            }

            for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
                ItemStack stack = player.inventory.offHandInventory.get(i);
                if (!stack.isEmpty()) {
                    newKit.put(new PlayerSlot(i, OFF_HAND), new StackReference(stack));
                }
            }

            DataManager.kits.put(name, newKit);

            try {
                DataManager.saveConfig();
            }
            catch (IOException e) {
                LogHelper.error("Something when wrong while saving inventory!");
                e.printStackTrace();
                throw new CommandException(e.getMessage() + " [See console for stacktrace]");
            }
            sender.sendMessage(new TextComponentString("Your current inventory has been saved to kit " + name).setStyle(new Style().setColor(TextFormatting.GREEN)));
        }
        else if (args[0].equals("give") && args.length > 1) {
            EntityPlayer player;
            String kit = args[1];

            if (!DataManager.kits.containsKey(kit)) {
                throw new CommandException("The specified kit does not exist!");
            }

            if (args.length >= 3) {
                player = getPlayer(server, sender, args[2]);
            }
            else {
                player = getCommandSenderAsPlayer(sender);
            }

            DataManager.givePlayerKit(player, kit);
        }
        else if (args[0].equals("remove") && args.length == 2) {
            String kit = args[1];

            if (!DataManager.kits.containsKey(kit)) {
                throw new CommandException("The specified kit does not exist!");
            }

            DataManager.kits.remove(kit);
            try {
                DataManager.saveConfig();
            }
            catch (IOException e) {
                LogHelper.error("Something when wrong while saving inventory!");
                e.printStackTrace();
                throw new CommandException(e.getMessage() + " [See console for stacktrace]");
            }
            sender.sendMessage(new TextComponentString("Kit removed successfully!").setStyle(new Style().setColor(TextFormatting.GREEN)));
        }
        else if (args[0].equals("list")) {
            sender.sendMessage(new TextComponentString("### Kits ###").setStyle(new Style().setColor(TextFormatting.GOLD)));
            for (String name : DataManager.kits.keySet()) {
                sender.sendMessage(new TextComponentString(name).setStyle(new Style().setColor(TextFormatting.GREEN)));
            }
        }
        else {
            help(sender);
        }
    }

    private void help(ICommandSender sender) {
        sender.sendMessage(new TextComponentString("Usage:"));
        sender.sendMessage(new TextComponentString("/csg_kit add <kit name>"));
        sender.sendMessage(new TextComponentString("- adds/overwrite the specified kit with your current player inventory.").setStyle(new Style().setColor(TextFormatting.GRAY)));
        sender.sendMessage(new TextComponentString("/csg_kit give <kit name> [player]"));
        sender.sendMessage(new TextComponentString("- Give the specified kit to the target player or your self if no player is specified (Will delete all existing items first.)").setStyle(new Style().setColor(TextFormatting.GRAY)));
        sender.sendMessage(new TextComponentString("/csg_kit remove <kit name>"));
        sender.sendMessage(new TextComponentString("- Removes the specified kit from the system.").setStyle(new Style().setColor(TextFormatting.GRAY)));
        sender.sendMessage(new TextComponentString("/csg_kit list"));
        sender.sendMessage(new TextComponentString("- lists all kits.").setStyle(new Style().setColor(TextFormatting.GRAY)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "add", "give", "remove", "list");
        }
        else if (args.length == 2 && (args[0].equals("give") || args[0].equals("remove"))) {
            return getListOfStringsMatchingLastWord(args, DataManager.kits.keySet());
        }
        else if (args.length == 3 && args[0].equals("give")) {
            return getListOfStringsMatchingLastWord(args, server.getPlayerList().getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }
}
