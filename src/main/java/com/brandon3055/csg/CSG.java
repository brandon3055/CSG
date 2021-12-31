package com.brandon3055.csg;

import com.brandon3055.csg.commands.CSGCommand;
import com.brandon3055.csg.commands.KitsCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
