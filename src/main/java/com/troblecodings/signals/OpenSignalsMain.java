package com.troblecodings.signals;

import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.troblecodings.contentpacklib.FileReader;
import com.troblecodings.core.net.NetworkHandler;
import com.troblecodings.guilib.ecs.GuiHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSModels;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.proxy.ClientProxy;
import com.troblecodings.signals.proxy.CommonProxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.libraries.ModList;
import net.minecraftforge.forgespi.locating.IModFile;

@Mod(OpenSignalsMain.MODID)
public class OpenSignalsMain {

	private static OpenSignalsMain instance;
	public static final String MODID = "opensignals";

	public static OpenSignalsMain getInstance() {
		return instance;
	}

	public OpenSignalsMain() {
		instance = this;
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(OSBlocks.class);
		MinecraftForge.EVENT_BUS.register(OSItems.class);
		MinecraftForge.EVENT_BUS.register(OSSounds.class);
		MinecraftForge.EVENT_BUS.register(NameHandler.class);
		MinecraftForge.EVENT_BUS.register(SignalBoxHandler.class);
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> eventBus.register(OSModels.class));
		debug = true;
		log = LoggerContext.getContext().getLogger(MODID);
		ModList.getBasicLists(null);
		file = ModList.get().getModFileById(MODID).getFile();
		contentPacks = new FileReader(MODID, "assets/" + MODID, log, name -> file.findResource(name));
	}

	public static CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	private static Logger log = null;
	public static GuiHandler handler = null;
	public static NetworkHandler network = null;
	public static FileReader contentPacks = null;
	private static boolean debug;
	public IModFile file;

	/**
	 * @return the debug
	 */
    public static boolean isDebug() {
		return debug;
	}

	@EventHandler
	public void preinit(final FMLPreInitializationEvent event) {
		debug = Files.isDirectory(event.getSourceFile().toPath());
		log = event.getModLog();
		proxy.preinit(null);
	}

	@EventHandler
	public void init(final FMLInitializationEvent event) {
		proxy.init(event);
	}

	public static Logger getLogger() {
		if (log == null)
			log = LogManager.getLogger(MODID);
		return log;
	}
}