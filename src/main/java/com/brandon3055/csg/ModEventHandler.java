package com.brandon3055.csg;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by brandon3055 on 11/11/2016.
 */
public class ModEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        CompoundTag playerData = event.getEntity().getPersistentData();
        CompoundTag data;

        if (!playerData.contains(Player.PERSISTED_NBT_TAG)) {
            data = new CompoundTag();
        }
        else {
            data = playerData.getCompound(Player.PERSISTED_NBT_TAG);
        }

        if (!data.getBoolean("csg:receivedInventory")) {
            DataManager.givePlayerStartGear(event.getEntity());
            data.putBoolean("csg:receivedInventory", true);
            playerData.put(Player.PERSISTED_NBT_TAG, data);
        }
    }
}