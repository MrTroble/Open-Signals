package eu.gir.girsignals.guis;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GUIHandler implements IGuiHandler {
	
	public static final int GUI_PLACEMENTTOOL = 0;
	public static final int GUI_SIGNAL_CONTROLLER = 1;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case GUI_PLACEMENTTOOL:
			return new GuiPlacementtool(player.getHeldItemMainhand());
		case GUI_SIGNAL_CONTROLLER:
			return new GuiSignalController(new BlockPos(x, y, z), world);
		default:
			return null;
		}
	}

}
