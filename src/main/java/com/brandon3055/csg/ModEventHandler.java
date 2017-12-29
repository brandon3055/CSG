package com.brandon3055.csg;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

/**
 * Created by brandon3055 on 11/11/2016.
 */
public class ModEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerLogin(PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) {
            return;
        }

        NBTTagCompound playerData = event.player.getEntityData();
        NBTTagCompound data;

        if (!playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            data = new NBTTagCompound();
        }
        else {
            data = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }

        if (!data.getBoolean("csg:receivedInventory")) {
            DataManager.givePlayerStartGear(event.player);
            data.setBoolean("csg:receivedInventory", true);
            playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
        }
    }
}