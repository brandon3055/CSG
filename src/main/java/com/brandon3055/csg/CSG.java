package com.brandon3055.csg;

import com.brandon3055.csg.commands.CSGCommand;
import com.brandon3055.csg.commands.KitsCommand;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Mod(CSG.MODID)
public class CSG {
    private static Logger LOGGER = LogManager.getLogger(CSG.MODID);

    public static final String MODID = "customstartinggear";
    public static final String MODNAME = "Custom Starting Gear";
//    public static final String VERSION = "${mod_version}";
    public static final String VERSION = "2.0.3";

    public CSG() {
        MinecraftForge.EVENT_BUS.addListener(CSG::registerCommands);
        MinecraftForge.EVENT_BUS.register(new ModEventHandler());

        DataManager.initialize();
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        CSGCommand.register(event.getDispatcher());
        KitsCommand.register(event.getDispatcher());
    }
}
