package eu.gir.girsignals.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class Debug extends CommandBase {

	public static final boolean DEBUG = false;

	public static final HashMap<String, Runnable> SUBCOMMANDS = new HashMap<>();

	private Debug() {
	}

	private static final Debug INSTANCE = new Debug();

	@SubscribeEvent
	public static void register(FMLServerStartingEvent event) {
		if (!DEBUG)
			return;
		SUBCOMMANDS.clear();
		SUBCOMMANDS.put("network", NetworkDebug::trigger);
		event.registerServerCommand(INSTANCE);
	}

	@Override
	public String getName() {
		return "signaldebug";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Use to debug the signal system!";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1)
			throw new WrongUsageException("Not enough parameters!");
		String name = args[0];
		if (!SUBCOMMANDS.containsKey(name))
			throw new WrongUsageException("Subcommand does not exist!");
		SUBCOMMANDS.get(name).run();
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		if (args.length < 1)
			return new ArrayList<>(SUBCOMMANDS.keySet());
		return super.getTabCompletions(server, sender, args, targetPos);
	}

}
