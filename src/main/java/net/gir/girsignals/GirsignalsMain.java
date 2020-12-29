package net.gir.girsignals;

import net.gir.girsignals.controllers.SignalControllerTileEntity;
import net.gir.girsignals.proxy.CommonProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "girsignals", name = "GIRSignals", version = "1.0.0", dependencies = "required-before:opencomputers", acceptedMinecraftVersions = "[1.12.2]", modLanguage = "java")

public class GirsignalsMain {

	@Instance
	private static GirsignalsMain instance;
	public static final String MODID = "girsignals";

	public static GirsignalsMain getInstance() {
		return instance;
	}

	@SidedProxy(serverSide = "net.gir.girsignals.proxy.CommonProxy", clientSide = "net.gir.girsignals.proxy.ClientProxy")
	private static CommonProxy proxy;

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		proxy.preinit(event);
		TileEntity.register("SignalControllerTileEntity", SignalControllerTileEntity.class);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postinit(FMLPostInitializationEvent event) {
		proxy.postinit(event);
	}
}
