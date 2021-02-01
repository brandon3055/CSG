package com.brandon3055.csg;

import com.brandon3055.csg.commands.CSGCommand;
import com.brandon3055.csg.commands.KitsCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(CSG.MODID)
public class CSG {
    private static Logger LOGGER = LogManager.getLogger(CSG.MODID);

    public static final String MODID = "customstartinggear";
    public static final String MODNAME = "Custom Starting Gear";
    public static final String VERSION = "${mod_version}";

    public CSG() {
        MinecraftForge.EVENT_BUS.addListener(CSG::registerCommands);
        MinecraftForge.EVENT_BUS.register(new ModEventHandler());

        DataManager.initialize();

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        CSGCommand.register(event.getDispatcher());
        KitsCommand.register(event.getDispatcher());
    }
}
