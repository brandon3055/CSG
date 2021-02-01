package com.brandon3055.csg;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by brandon3055 on 11/11/2016.
 */
public class ModEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer().world.isRemote) {
            return;
        }

        CompoundNBT playerData = event.getPlayer().getPersistentData();
        CompoundNBT data;

        if (!playerData.contains(PlayerEntity.PERSISTED_NBT_TAG)) {
            data = new CompoundNBT();
        }
        else {
            data = playerData.getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        }

        if (!data.getBoolean("csg:receivedInventory")) {
            if (event.getPlayer().inventory.isEmpty()){
                DataManager.givePlayerStartGear(event.getPlayer());
            }
            data.putBoolean("csg:receivedInventory", true);
            playerData.put(PlayerEntity.PERSISTED_NBT_TAG, data);
        }
    }
}