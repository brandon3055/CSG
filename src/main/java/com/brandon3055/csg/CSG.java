package com.brandon3055.csg;

import com.brandon3055.csg.commands.CommandCSG;
import com.brandon3055.csg.commands.CommandKits;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;


@Mod(modid = CSG.MODID, name = CSG.MODNAME, version = CSG.VERSION)
public class CSG {
    public static final String MODID = "customstartinggear";
    public static final String MODNAME = "Custom Starting Gear";
    public static final String VERSION = "${mod_version}";
    public static Configuration configuration;

    @Mod.Instance(CSG.MODID)
    public static CSG instance;

    @NetworkCheckHandler
    public boolean networkCheck(Map<String, String> map, Side side) {
        return true;
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandCSG());
        event.registerServerCommand(new CommandKits());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        DataManager.initialize(event.getModConfigurationDirectory());
        MinecraftForge.EVENT_BUS.register(new ModEventHandler());

//        try {
//            chunkloadCommand = configuration.get(Configuration.CATEGORY_GENERAL, "chunkloadCommand", chunkloadCommand, "This allows you to change the name of the chunkload command.").getString();
//            chunkManagerCommand = configuration.get(Configuration.CATEGORY_GENERAL, "chunkManagerCommand", chunkManagerCommand, "This allows you to change the name of the chunkmanager command.").getString();
//
//            if (configuration.hasChanged()) {
//                configuration.save();
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
