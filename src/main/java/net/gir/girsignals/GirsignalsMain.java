package net.gir.girsignals;

import net.gir.girsignals.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "girsignals", name = "GIRSignals", version = "1.0.0", dependencies = "after:opencomputers", acceptedMinecraftVersions = "[1.12.2]", modLanguage = "java")

public class GirsignalsMain {

	@Instance
	private static GirsignalsMain instance;
	public static final String MODID = "girsignals";

	public static GirsignalsMain getInstance() {
		return instance;
	}

	@SidedProxy(serverSide = "net.gir.girsignals.proxy.CommonProxy", clientSide = "net.gir.girsignals.proxy.ClientProxy")
	public static CommonProxy PROXY;

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		PROXY.preinit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		PROXY.init(event);
	}

	@EventHandler
	public void postinit(FMLPostInitializationEvent event) {
		PROXY.postinit(event);
	}
}
